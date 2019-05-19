package agents;

import behaviours.ElectionManagerInitiator;
import behaviours.ElectionManagerListener;
import behaviours.ElectionManagerResultsListener;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
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
    
    Map<String,VoteResult> votes=new HashMap<>();

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
                Log.log(this, this.getName()+": COLLECTOR!! Where are you??!");
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return result;
    }
    public boolean sendVoteCollector(String regionVoteKey){
        
	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(getVoteCollector());
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // We want to receive a reply in 10 secs
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));
        msg.setConversationId(RequestType.SEND_VOTE_COLLECTOR);
        msg.setReplyWith(RequestType.SEND_VOTE_COLLECTOR + System.currentTimeMillis());
        msg.setContent(regionVoteKey); 
        
        Boolean res=false;
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
    public void addVotes(String regionVoteKey,VoteResult vote){
        this.votes.put(regionVoteKey, vote);
        System.out.println("Got votes for:"+regionVoteKey);
    }
}
