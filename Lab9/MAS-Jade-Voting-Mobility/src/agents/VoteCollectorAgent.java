package agents;

import behaviours.ElectionManagerInitiator;
import behaviours.ElectionManagerListener;
import behaviours.VoteCollectorInitiator;
import behaviours.VoteCollectorListener;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.wrapper.ControllerException;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import platform.Log;
import platform.VoteResult;

/**
 * The Vote Collector Agent.
 */
public class VoteCollectorAgent extends Agent {

    /**
     * The serial UID.
     */
    private static final long serialVersionUID = -4316893632718883072L;
    Map<String, VoteResult> voteResults = new HashMap<>();
    String prevContainer = "";

    @Override
    public void setup() {
        Log.log(this, "Hello");
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(ServiceType.VOTE_COLLECTOR);
        sd.setName("vote-collector");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        addBehaviour(new VoteCollectorListener(this, null));
    }

    @Override
    protected void takeDown() {
        // Printout a dismissal message
        Log.log(this, "terminating.");
    }

    @Override
    protected void beforeMove() {
        System.out.println("Voting agent packing bags... ");
        prevContainer = getCurrentContainer();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(VoteCollectorAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void afterMove() {
        String currentContainer = getCurrentContainer();
        System.out.println(this.getName() + ": Voting Arrived in " + currentContainer + " and starting collecting Votes...");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(VoteCollectorAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (currentContainer.equals(Regions.getCentralElection())) {
            //Arrived home, give votes
            String votingRegionKey = Regions.getRegionForContainer(prevContainer);
            VoteResult v = voteResults.get(votingRegionKey);
            if (v != null) {
                // send votes to election manager
                System.out.println(this.getName() + ": Sending votes to  " + getElectionManager());
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(getElectionManager());
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                // We want to receive a reply in 10 secs
                msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));
                msg.setConversationId(RequestType.REPORT_VOTES);
                msg.setReplyWith(RequestType.REPORT_VOTES + System.currentTimeMillis());
                AbstractMap.SimpleEntry<String, VoteResult> e = new AbstractMap.SimpleEntry<>(votingRegionKey, v);
                msg.setContent(votingRegionKey); //Price <CR> Request #
                try {
                    msg.setContentObject(e);
                } catch (IOException ex) {
                    Logger.getLogger(VoteCollectorAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.addBehaviour(new AchieveREInitiator(this, msg) {
                    @Override
                    protected void handleAllResponses(Vector responses) {
                        System.out.println(this.myAgent.getName() + " Received  "+responses);
                    }
                });

            }
        } else {
            //Arrived in region, collect votes
            String regionVoteKey = Regions.getRegionForContainer(currentContainer);
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

            for (AID a : this.getRegionReps()) {
                msg.addReceiver(a);
            }

            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            // We want to receive a reply in 10 secs
            msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));
            msg.setConversationId(RequestType.ASK_VOTES_FROM_REP);
            msg.setReplyWith(RequestType.ASK_VOTES_FROM_REP + System.currentTimeMillis());
            msg.setContent(regionVoteKey); //Price <CR> Request #
            addBehaviour(new VoteCollectorInitiator(this, msg));
        }

    }

    protected String getCurrentContainer() {
        String res = "";
        try {
            res = this.getContainerController().getContainerName();
        } catch (ControllerException ex) {
            Logger.getLogger(VoteCollectorAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    protected Set<AID> getRegionReps() {
        // search available agents
        Set<AID> res = new HashSet<>();
        ServiceDescription sd = new ServiceDescription();
        DFAgentDescription template = new DFAgentDescription();
        sd.setType(ServiceType.REGION_REP);
        template.addServices(sd);
        AID result = null;
        try {
            DFAgentDescription[] results = DFService.search(this, template);
            for (int i = 0; i < results.length; i++) {
                res.add(results[i].getName());
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return res;
    }

    protected AID getElectionManager() {
        // search available agents
        ServiceDescription sd = new ServiceDescription();
        DFAgentDescription template = new DFAgentDescription();
        sd.setType(ServiceType.ELECTION_MANAGEMENT);
        template.addServices(sd);
        AID result = null;
        try {
            DFAgentDescription[] results = DFService.search(this, template);
            if (results.length > 0) {
                result = results[0].getName();
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return result;
    }

    public void setVoteResults(String regionVoteKey, VoteResult votes) {
        this.voteResults.put(regionVoteKey, votes);
    }
}
