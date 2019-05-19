package agents;

import behaviours.RegionRepInitiator;
import behaviours.RegionRepListener;
import behaviours.VoteCollectorInitiator;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.Iterator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import platform.Log;
import platform.VoteReader;
import platform.VoteResult;

/**
 * The Region Representative Agent.
 */
public class RegionRepAgent extends Agent {

    /**
     * The serial UID.
     */
    private static final long serialVersionUID = 2081456560111009192L;

    /**
     * Known election manager agent
     */
    AID electionManagerAgent;

    /**
     * key name for the region vote results from json input file
     */
    String regionVoteKey;

    VoteResult voteResult;
    
    Boolean voteSent=false; 
    Boolean hasCalledColector=false;

    @Override
    protected void setup() {
        Log.log(this, "Hello from RegionRepresantative: " + this.getLocalName());
        Log.log(this, "Adding DF subscribe behaviors");

        regionVoteKey = (String) getArguments()[0];

        AID dfAgent = getDefaultDF();
        Log.log(this, "Default DF Agent: " + dfAgent);

        // Create election service discovery behavior
        // Build the DFAgentDescription which holds the service descriptions for the the ambient-agent service
        VoteReader voteReader = new VoteReader();
        voteResult = voteReader.getVoteResult(regionVoteKey);

        DFAgentDescription DFDesc = new DFAgentDescription();
        ServiceDescription serviceDesc = new ServiceDescription();
        serviceDesc.setType(ServiceType.ELECTION_MANAGEMENT);
        DFDesc.addServices(serviceDesc);

        SearchConstraints cons = new SearchConstraints();
        cons.setMaxResults(new Long(1));

        // add sub behavior for ambient-agent service discovery
        this.addBehaviour(new SubscriptionInitiator(this,
                DFService.createSubscriptionMessage(this, dfAgent, DFDesc, cons)) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void handleInform(ACLMessage inform) {
                Log.log(myAgent, "Notification received from DF");

                try {
                    DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
                    if (results.length > 0) {
                        for (DFAgentDescription dfd : results) {
                            AID provider = dfd.getName();
                            // The same agent may provide several services; we are interested
                            // in the election-management one
                            for (Iterator it = dfd.getAllServices(); it.hasNext();) {
                                ServiceDescription sd = (ServiceDescription) it.next();
                                if (sd.getType().equals(ServiceType.ELECTION_MANAGEMENT)) {
                                    Log.log(myAgent, ServiceType.ELECTION_MANAGEMENT, "service found: Service \"", sd.getName(),
                                            "\" provided by agent ", provider.getName());
                                    addServiceAgent(ServiceType.ELECTION_MANAGEMENT, provider);

                                    // if we found the ElectionManager we can cancel the subscription
                                    cancel(inform.getSender(), true);
                                }
                            }
                        }
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(ServiceType.REGION_REP);
        sd.setName("region-rep");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        this.addBehaviour(new RegionRepListener(this, null));
    }

    /**
     * This method will be called when all the needed agents have been
     * discovered.
     */
    protected void onDiscoveryCompleted() {
        // TODO: add the RequestInitiator behavior for asking the VoteCollectorAgent to come collect the results
        try {
            Thread.sleep((long)(Math.random() * 5000));
        } catch (InterruptedException ex) {
            Logger.getLogger(RegionRepAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        addBehaviour( 
                new TickerBehaviour(this,10000) {
                    protected void onTick() {
                        askForVoteCollector();
                        if (voteSent) block();
                    }
                }
        );

    }
    public void askForVoteCollector()
    {
        if (voteSent || hasCalledColector) //already sent votes
            return;
        
        
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // We want to receive a reply in 10 secs
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));
        msg.setConversationId(RequestType.ASK_VOTE_COLLECTOR);
        msg.setContent(regionVoteKey); //Price <CR> Request #
        msg.setReplyWith(RequestType.ASK_VOTE_COLLECTOR + System.currentTimeMillis());
        msg.addReceiver(electionManagerAgent);

        Log.log(this, String.format("Agent %s Region <%s> calling for Collector ",this.getName(), regionVoteKey));
        
        //this.send(msg);
        
        hasCalledColector=true;
        
        addBehaviour(new AchieveREInitiator(this, msg){
            @Override
            protected void handleAllResponses(Vector responses) {
                hasCalledColector=false;
                if (responses.size() <= 0) {
                    //No responses or timeout
                    //go home
                    System.out.println(this.myAgent.getName() + " Received no response ");
                } else {
                    //
                    Enumeration e = responses.elements();
                    while (e.hasMoreElements()) {
                        ACLMessage msg = (ACLMessage) e.nextElement();
                        if (msg.getPerformative()==ACLMessage.AGREE){
                            System.out.println(myAgent.getName()+" - Vote collector will come.. can't wait !");
                        }else{
                            System.out.println(myAgent.getName()+" - Vote collector is busy :( will call him later !");

                        }
                    }

                }
                
            }            
        });
        //this.addBehaviour(new RegionRepListener(this, null));

        //addBehaviour(new RegionRepInitiator(this, msg));
        
    }

    /**
     * Retains an agent provided a service.
     *
     * @param serviceType - the service type.
     * @param agent - the agent providing a service.
     */
    public void addServiceAgent(String serviceType, AID agent) {

        if (serviceType.equals(ServiceType.ELECTION_MANAGEMENT)) {
            if (electionManagerAgent != null) {
                Log.log(this, "Warning: a second preference agent found.");
            }
            electionManagerAgent = agent;
        }

        if (electionManagerAgent != null) {
            onDiscoveryCompleted();
        }
    }

    @Override
    protected void takeDown() {
        // Printout a dismissal message
        Log.log(this, "terminating.");
    }
    public String getRegionVoteKey(){
        return regionVoteKey;
    }
    public VoteResult getVoteResult(){
        return voteResult;
    }
    public void setVotesSent(){
        this.voteSent=true;
    }
}
