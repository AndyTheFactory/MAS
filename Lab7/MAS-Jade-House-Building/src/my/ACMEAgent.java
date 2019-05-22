package my;

import behaviour.AcmeNegociationBehaviour;
import behaviour.DutchAuctionBehaviour;
import behaviour.StatusReportBehaviour;
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
    private static float EPSILON = .03f;
    public static float MAX_DUTCH_ITERATIONS=3;
    /**
     * Statuses.
     */
    Map<String, ArrayList<ContractingStatus>> statuses = new LinkedHashMap<>();
    /**
     * Who can do each item.
     */
    Map<String, Set<AID>> potentialContractors = new HashMap<>();
    Map<String, Integer> bugets = new HashMap<>();
    Map<String, Integer> bugets_monoton = new HashMap<>();

    ArrayList<String> serviceNames = new ArrayList<>();

    Map<String, AID> Contractors = new HashMap<>();

    @Override
    protected void setup() {
        @SuppressWarnings("unchecked")
        Map<String, Integer> items = (Map<String, Integer>) getArguments()[0];

        Log.log(this, "Construction phases/budget:", items);

        for (String item : items.keySet()) {
            // create item information
            ArrayList<ContractingStatus> st=new ArrayList<>();
            st.add(new ContractingStatus(item, items.get(item)));
            statuses.put(item, st);
            
            potentialContractors.put(item, new HashSet<>());
            serviceNames.add(item);
            bugets.put(item, items.get(item));
            bugets_monoton.put(item, Integer.divideUnsigned(items.get(item),2));

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

        addBehaviour(new StatusReportBehaviour(this, 5000));

        for (String serviceName : serviceNames) {
            startAuction(serviceName);
        }
        /*if (serviceNames.size() > 0) {
            startAuction(serviceNames.get(0));
        }*/
    }

    private void startAuction(String serviceName) {

        if (potentialContractors.get(serviceName) == null || potentialContractors.get(serviceName).size() <= 0) {
            Log.log(this, String.format("<%s> there are no agens available...", serviceName));
            return;
        }

        int budget = this.bugets.get(serviceName);

        int startPrice = (int) Math.round((float) (budget / MAX_DUTCH_ITERATIONS) + ((Math.random() - 0.5) * (EPSILON * budget)));

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        for (AID a : potentialContractors.get(serviceName)) {
            msg.addReceiver(a);
        }
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // We want to receive a reply in 10 secs
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));
        msg.setConversationId(serviceName);
        msg.setContent(String.valueOf(startPrice)); //Price <CR> Request #

        ContractingStatus status = getContractingStatus(serviceName);
        status.updatePhase(ContractingStatus.ContractingPhase.INITIAL);
        status.updateProposedPrices(startPrice, -1);
        status.newNegotiationRound();

        Log.log(this, String.format("Round %d <%s> asking for '%s' ... %d Price", status.getNegotiationRound(), this.getLocalName(), serviceName, startPrice));

        addBehaviour(new DutchAuctionBehaviour(this, msg, serviceName, status));

    }

    public void continueAuction(String serviceName) {

        if (potentialContractors.get(serviceName) == null || potentialContractors.get(serviceName).size() <= 0) {
            Log.log(this, String.format("<%s> there are no agens available...", serviceName));
            return;
        }

        int budget = this.bugets.get(serviceName);
        ContractingStatus status = getContractingStatus(serviceName);

        int lastPrice = status.getMyLastPrice();

        int newPrice = lastPrice + (int) Math.round((float) (budget / MAX_DUTCH_ITERATIONS) + ((Math.random() - 0.5) * (EPSILON * budget)));

        if (newPrice > budget) {
            newPrice = budget;
        }

        status.newNegotiationRound();

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        for (AID a : potentialContractors.get(serviceName)) {
            msg.addReceiver(a);
        }
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // We want to receive a reply in 10 secs
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));
        msg.setConversationId(serviceName);
        msg.setContent(String.valueOf(newPrice)); //Price <CR> Request #

        status.updatePhase(ContractingStatus.ContractingPhase.CONTRACTING);
        status.updateProposedPrices(newPrice, -1);

        Log.log(this, String.format("Round %d <%s> asking for '%s' ... %d Price", status.getNegotiationRound(), this.getLocalName(), serviceName, newPrice));

        addBehaviour(new DutchAuctionBehaviour(this, msg, serviceName, status));

    }
    public void makeMonotonic(String serviceName){

        ContractingStatus status = getContractingStatus(serviceName);
        
        if (status.getContractingPhase()!=ContractingStatus.ContractingPhase.NEGOTIATING) {
            Log.log(this, String.format("<%s> there are no negociations in progress...", serviceName));
            return;
        }
        
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        for (ContractingStatus s : getContractingStatusList(serviceName)) {
            if (s.getContractingPhase()==ContractingStatus.ContractingPhase.NEGOTIATING)
                msg.addReceiver(s.getPartner());
        }
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // We want to receive a reply in 10 secs
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));
        msg.setConversationId(serviceName);
        int newPrice= bugets_monoton.get(serviceName);
        msg.setContent(String.valueOf(newPrice)); //Price <CR> Request #

        addBehaviour(new AcmeNegociationBehaviour(this, msg, serviceName,status));
        
        
    }

    public ContractingStatus getContractingStatus(String serviceName) {
        if (!statuses.containsKey(serviceName)) {
            throw new IllegalArgumentException("Unknown Service Name for getStatus: " + serviceName);
        }
        return statuses.get(serviceName).get(0);
    }
    public ArrayList<ContractingStatus> getContractingStatusList(String serviceName) {
        if (!statuses.containsKey(serviceName)) {
            throw new IllegalArgumentException("Unknown Service Name for getStatus: " + serviceName);
        }
        return statuses.get(serviceName);
    }


    public void addContractingStatus(String serviceName, ContractingStatus status) {
        if (!statuses.containsKey(serviceName)) {
            throw new IllegalArgumentException("Unknown Service Name for setStatus: " + serviceName);
        }
        getContractingStatusList(serviceName).add(status);
    }

    public String printContractingStatuses() {
        StringBuilder sb = new StringBuilder();
        for (String s : serviceNames) {
            ContractingStatus status = getContractingStatus(s);
            sb.append("  " + s + ": \n");

            if (status.getContractingPhase() == ContractingStatus.ContractingPhase.INITIAL) {
                sb.append("    Initiating ...");
            }
            if (status.getContractingPhase() == ContractingStatus.ContractingPhase.DONE) {
                if (status.getPartner() == null) {
                    sb.append("    No contractor found ... END");
                } else {
                    sb.append("    Contractor '" + status.getPartner().getLocalName() + "' final price " + status.getPartnerLastPrice());
                }
            }
            if (status.getContractingPhase() == ContractingStatus.ContractingPhase.CONTRACTING) {
                sb.append("    proposing price " + status.getMyLastPrice());
            }
            if (status.getContractingPhase() == ContractingStatus.ContractingPhase.NEGOTIATING) {
                sb.append("    negociating with '" + status.getPartner().getLocalName() + "' prices:\n");
                sb.append("                                       myprice " + bugets_monoton.get(s) + "\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    public void  monotonicRound(String serviceName){
        
        int lastprice= bugets_monoton.get(serviceName);
        int increment=Integer.divideUnsigned(bugets.get(serviceName),5+(int)Math.round(Math.random()*10));
        lastprice+=increment;
        
        bugets_monoton.put(serviceName,lastprice);
        
    }
}
