package my;

import behaviour.DutchAuctionBehaviour;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import launcher.ContractingStatus;
import launcher.Log;

/**
 * The agent managing construction of the ACME headquarters.
 */
public class ACMEAgent extends Agent {

    /**
     * The serial UID.
     */
    private static final long serialVersionUID = 2897763463127840876L;
    private static float EPSILON=.03f;
    /**
     * Statuses.
     */
    Map<String, ContractingStatus> statuses = new LinkedHashMap<>();
    /**
     * Who can do each item.
     */
    Map<String, Set<AID>> potentialContractors = new HashMap<>();
    Map<String, Integer> bugets = new HashMap<>();

    ArrayList<String> serviceNames = new ArrayList<>();

    Map<String, AID> Contractors = new HashMap<>();

    @Override
    protected void setup() {
        @SuppressWarnings("unchecked")
        Map<String, Integer> items = (Map<String, Integer>) getArguments()[0];

        Log.log(this, "Construction phases/budget:", items);

        for (String item : items.keySet()) {
            // create item information
            statuses.put(item, new ContractingStatus(item, items.get(item)));
            potentialContractors.put(item, new HashSet<>());
            serviceNames.add(item);
            bugets.put(item, items.get(item));

            // search available agents
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(item);
            template.addServices(sd);
            try {
                DFAgentDescription[] results = DFService.search(this, template);
                if (results != null) {
                    for (DFAgentDescription result : results) {
                        potentialContractors.get(item).add(result.getName());
                    }
                }
                Log.log(this, "<", item, ">", "Available agents for construction item:",
                        potentialContractors.get(item));
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ACMEAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (serviceNames.size() > 0) {
            startAuction(serviceNames.get(0));
        }
    }

    private void startAuction(String serviceName) {

        if (potentialContractors.get(serviceName) == null || potentialContractors.get(serviceName).size() <= 0) {
            Log.log(this, String.format("<%s> there are no agens available...", serviceName));
            return;
        }

        int budget = this.bugets.get(serviceName);

        int startPrice = (int) Math.round((float) (budget / 3) + ((Math.random() - 0.5) * (EPSILON * budget)));

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        for (AID a : potentialContractors.get(serviceName)) {
            msg.addReceiver(a);
        }
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // We want to receive a reply in 10 secs
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));
        msg.setConversationId(serviceName);
        msg.setContent(String.valueOf(startPrice) + "\n1"); //Price <CR> Request #

        addBehaviour(new DutchAuctionBehaviour(this, msg, serviceName, startPrice));
        
        ContractingStatus status=getContractingStatus(serviceName);
        status.updatePhase(ContractingStatus.ContractingPhase.CONTRACTING);
        status.updateProposedPrices(startPrice, -1);
        status.newNegotiationRound();
        
        Log.log(this, String.format("Round %d <%s> asking for '%s' ... %d Price",status.getNegotiationRound(), this.getLocalName(), serviceName,startPrice));
        this.send(msg);

    }
    public void continueAuction(String serviceName) {

        if (potentialContractors.get(serviceName) == null || potentialContractors.get(serviceName).size() <= 0) {
            Log.log(this, String.format("<%s> there are no agens available...", serviceName));
            return;
        }

        int budget = this.bugets.get(serviceName);
        ContractingStatus status=getContractingStatus(serviceName);
        
        int lastPrice=status.getMyLastPrice();

        int newPrice = lastPrice+(int) Math.round((float) (budget / 3) + ((Math.random() - 0.5) * (EPSILON * budget)));
        
        if (newPrice>budget) newPrice=budget;
        
        status.newNegotiationRound();

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        for (AID a : potentialContractors.get(serviceName)) {
            msg.addReceiver(a);
        }
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // We want to receive a reply in 10 secs
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));
        msg.setConversationId(serviceName);
        msg.setContent(String.valueOf(newPrice) + "\n"+status.getNegotiationRound()); //Price <CR> Request #

        addBehaviour(new DutchAuctionBehaviour(this, msg, serviceName, newPrice));
        
        status.updatePhase(ContractingStatus.ContractingPhase.CONTRACTING);
        status.updateProposedPrices(newPrice, -1);
        
        Log.log(this, String.format("Round %d <%s> asking for '%s' ... %d Price",status.getNegotiationRound(), this.getLocalName(), serviceName,newPrice));
        this.send(msg);

    }

    public ContractingStatus getContractingStatus(String serviceName) {
        if (!statuses.containsKey(serviceName)) {
            throw new IllegalArgumentException("Unknown Service Name for getStatus: " + serviceName);
        }
        return statuses.get(serviceName);
    }

    public void setContractingStatus(String serviceName, ContractingStatus status) {
        if (!statuses.containsKey(serviceName)) {
            throw new IllegalArgumentException("Unknown Service Name for setStatus: " + serviceName);
        }
        statuses.put(serviceName, status);
    }

}
