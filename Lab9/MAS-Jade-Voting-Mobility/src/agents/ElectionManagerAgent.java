package agents;

import behaviours.ElectionManagerInitiator;
import behaviours.ElectionManagerListener;
import behaviours.ElectionManagerResultsListener;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Collections.reverseOrder;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import platform.Ballot;
import platform.Log;
import platform.VoteResult;

/**
 * ElectionManager agent.
 */
public class ElectionManagerAgent extends Agent {

    /**
     *
     */
    private static final long serialVersionUID = -3397689918969697329L;

    Map<String, VoteResult> votes = new HashMap<>();

    final static int SEATS_PER_REGION = 3;
    final static int CANDIDATES_PER_REGION = 5;

    @Override
    public void setup() {
        Log.log(this, "Hello");

        // Register the preference-agent service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        // TODO: register PreferenceAgent to DF with service type: preference-agent and service-name:
        // ambient-wake-up-call
        ServiceDescription sd = new ServiceDescription();
        sd.setType(ServiceType.ELECTION_MANAGEMENT);
        sd.setName("election-management");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new ElectionManagerListener(this, null));
        addBehaviour(new ElectionManagerResultsListener(this, null));

        addBehaviour(new TickerBehaviour(this, 60000) {
            @Override
            protected void onTick() {
                ((ElectionManagerAgent) this.myAgent).printVotes();
            }
        });

    }

    @Override
    protected void takeDown() {
        // De-register from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Printout a dismissal message
        Log.log(this, "terminating.");
    }

    public AID getVoteCollector() {
        // search available agents
        ServiceDescription sd = new ServiceDescription();
        DFAgentDescription template = new DFAgentDescription();
        sd.setType(ServiceType.VOTE_COLLECTOR);
        template.addServices(sd);
        AID result = null;
        try {
            DFAgentDescription[] results = DFService.search(this, template);
            if (results != null && results.length > 0) {
                result = results[0].getName();
                //Log.log(this, "Found vote Collector agent! Ready to go!");
            } else {
                Log.log(this, this.getName() + ": COLLECTOR!! Where are you??!");
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return result;
    }

    public boolean sendVoteCollector(String regionVoteKey) {

        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(getVoteCollector());
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // We want to receive a reply in 10 secs
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));
        msg.setConversationId(RequestType.SEND_VOTE_COLLECTOR);
        msg.setReplyWith(RequestType.SEND_VOTE_COLLECTOR + System.currentTimeMillis());
        msg.setContent(regionVoteKey);

        Boolean res = false;
        return res;
        /*
        this.send(msg);
        
        ACLMessage reply=this.blockingReceive(
                MessageTemplate.and(
                        MessageTemplate.MatchConversationId(RequestType.SEND_VOTE_COLLECTOR), 
                        MessageTemplate.MatchContent(regionVoteKey)
                ),
                30000
        );
        if (reply!=null && reply.getPerformative()==ACLMessage.AGREE){
            System.out.println(this.getName() + ": Vote Collector agreed - he'll travel to "+regionVoteKey);
            return true;
        }else{
            System.out.println(this.getName() + ": Vote Collector said Nooooooooooo! ");
            return false;
        }
         */
    }

    public void addVotes(String regionVoteKey, VoteResult vote) {
        this.votes.put(regionVoteKey, vote);
        System.out.println("Got votes for:" + regionVoteKey);
    }

    public void printVotes() {
        System.out.println("=========================");
        System.out.println("Election Manager Report: ");
        System.out.println("     got Votes from:");
        for (Map.Entry<String, VoteResult> entry : this.votes.entrySet()) {
            System.out.println("         " + entry.getKey());
            System.out.println("           Winners: " + getWinners(entry.getKey()));
        }
        System.out.println("=========================");

    }

    int getDroopQuota(String region) {
        VoteResult r = votes.get(region);
        if (r == null) {
            return Math.floorDiv(250, SEATS_PER_REGION + 1) + 1;
        } else {
            int total = 0;
            for (Ballot b : r.getBallots()) {
                total += b.getCount();
            }
            return Math.floorDiv(total, SEATS_PER_REGION + 1) + 1;
        }

    }

    String getWinners(String region) {
        StringBuilder res = new StringBuilder();
        int droop = getDroopQuota(region);
        VoteResult r = votes.get(region);

        Map<String, Integer> candidates = new HashMap<>();

        List<String> clist = r.getBallots().get(0).getCandidates();

        for (String c : clist) {
            candidates.put(c, 0);
        }
        //Round 1
        for (Ballot b : r.getBallots()) {
            String c = b.getCandidates().get(0);
            int total = b.getCount() + candidates.get(c);
            candidates.put(c, total);
        }
        Map<String, Integer> cand_orig = new HashMap<>(candidates);
        int nr = 0;
        while (nr < SEATS_PER_REGION) {
            if (SEATS_PER_REGION - nr >= candidates.size()) {
                //there are seats for everybody in candidates
                for (Map.Entry<String, Integer> e : candidates.entrySet()) {
                    res.append(e.getKey() + ",");
                }
                break;
            }
            Map.Entry<String, Integer> ebest = null, eworst = null;
            for (Map.Entry<String, Integer> e : candidates.entrySet()) {
                if (ebest == null || ebest.getValue() < e.getValue()) {
                    ebest = e;
                }
                if (eworst == null || eworst.getValue() > e.getValue()) {
                    eworst = e;
                }
            }
            int transfer = 0, tvotes = 0;
            String from_cand = "";
            if (ebest.getValue() >= droop) {
                //elected
                tvotes = ebest.getValue();
                res.append(ebest.getKey() + ",");
                nr++;
                transfer = ebest.getValue() - droop;
                from_cand = ebest.getKey();
                candidates.remove(ebest.getKey());

            } else {
                tvotes = eworst.getValue();
                candidates.remove(eworst.getKey());
                transfer = eworst.getValue();
                from_cand = eworst.getKey();
            }

            for (Ballot b : r.getBallots()) {
                if (b.getCandidates().get(0).equals(from_cand) && b.getCandidates().size() > 1) {
                    String to_cand = "";
                    for (int i = 1; i < b.getCandidates().size(); i++) {
                        if (candidates.get(b.getCandidates().get(i)) != null) {
                            to_cand = b.getCandidates().get(i);
                        }
                    }
                    if (candidates.get(to_cand) != null) {
                        int add_votes = Integer.divideUnsigned(b.getCount(), tvotes) * transfer;
                        int newvotes = candidates.get(to_cand) + add_votes;
                        candidates.put(to_cand, newvotes);
                    }

                }
            }

        }
        return res.toString();
    }
}
