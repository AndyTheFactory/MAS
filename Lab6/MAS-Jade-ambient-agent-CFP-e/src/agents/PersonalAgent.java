package agents;

import agents.behaviors.AgentCheckPreferencesBehavior;
import agents.behaviors.AgentInitiatorBehavior;
import agents.behaviors.AgentResponderBehavior;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.Iterator;

import java.util.LinkedList;
import java.util.List;

import agents.behaviors.AmbientServiceDiscoveryBehavior;
import data.Preferences;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.MessageTemplate;
import java.util.Calendar;

/**
 * The PersonalAgent.
 */
public class PersonalAgent extends Agent {

    /**
     * The serial UID.
     */
    private static final long serialVersionUID = 2081456560111009192L;
    
    private static int STATE_NONE=0;
    private static int STATE_ASKING=1;
    private static int STATE_DELEGATED=2;
    
    /**
     * Known ambient agents.
     */
    List<AID> ambientAgents = new LinkedList<>();

    /**
     * Known preference agent
     */
    AID preferenceAgent;
    String wakeupPreferences;
    
    int currentState;
    
    
    @Override
    protected void setup() {
        System.out.println("Hello from PersonalAgent");
        System.out.println("Adding DF subscribe behaviors");

        // Build the DFAgentDescription which holds the service descriptions for the the ambient-agent service
        // and the preference-agent description
        DFAgentDescription ambientAgentDesc = new DFAgentDescription();
        ServiceDescription ambientAgentSd = new ServiceDescription();
        ambientAgentSd.setType("ambient-agent");
        ambientAgentDesc.addServices(ambientAgentSd);

        DFAgentDescription preferenceAgentDesc = new DFAgentDescription();
        ServiceDescription preferenceAgentSd = new ServiceDescription();
        preferenceAgentSd.setType("preference-agent");
        preferenceAgentDesc.addServices(preferenceAgentSd);

        AID dfAgent = getDefaultDF();
        System.out.println("Default DF Agent: " + dfAgent);

        SearchConstraints ambientSC = new SearchConstraints();
        ambientSC.setMaxResults(new Long(2));

        SearchConstraints preferenceSC = new SearchConstraints();
        preferenceSC.setMaxResults(new Long(1));

        // Create a parallel behavior to handle the two DF subscriptions: one for the two ambient-agent and one for the preference-agent services
        AmbientServiceDiscoveryBehavior ambientDiscoveryBehavior = new AmbientServiceDiscoveryBehavior(this, ParallelBehaviour.WHEN_ALL);

        // add sub behavior for ambient-agent service discovery
        ambientDiscoveryBehavior.addSubBehaviour(new SubscriptionInitiator(this,
                DFService.createSubscriptionMessage(this, dfAgent, ambientAgentDesc, ambientSC)) {

            private static final long serialVersionUID = 1L;

            protected void handleInform(ACLMessage inform) {

                System.out.println("Agent " + getLocalName() + ": Notification received from DF");

                try {
                    DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());

                    if (results.length > 0) {
                        for (int i = 0; i < results.length; ++i) {
                            DFAgentDescription dfd = results[i];

                            AID provider = dfd.getName();

                            // The same agent may provide several services; we are interested
                            // in the ambient-agent and preference-agent ones
                            Iterator it = dfd.getAllServices();
                            while (it.hasNext()) {
                                ServiceDescription sd = (ServiceDescription) it.next();
                                if (sd.getType().equals("ambient-agent")) {
                                    System.out.println("Ambient agent service found:");
                                    System.out.println("- Service \"" + sd.getName() + "\" provided by agent " + provider.getName());

                                    ambientAgents.add(provider);
                                }
                            }
                        }
                    }

                    // the behavior can be removed if we have at least two providers for the ambient-agent service
                    if (ambientAgents.size() >= 2) {
                        cancel(inform.getSender(), true);
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });

        // add sub behavior for the preference-agent service discovery
        ambientDiscoveryBehavior.addSubBehaviour(new SubscriptionInitiator(this,
                DFService.createSubscriptionMessage(this, dfAgent, preferenceAgentDesc, preferenceSC)) {

            private static final long serialVersionUID = 1L;

            protected void handleInform(ACLMessage inform) {

                System.out.println("Agent " + getLocalName() + ": Notification received from DF");

                try {
                    DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());

                    if (results.length > 0) {
                        for (int i = 0; i < results.length; ++i) {
                            DFAgentDescription dfd = results[i];

                            AID provider = dfd.getName();

                            // The same agent may provide several services; we are interested
                            // in the ambient-agent and preference-agent ones
                            Iterator it = dfd.getAllServices();
                            while (it.hasNext()) {
                                ServiceDescription sd = (ServiceDescription) it.next();
                                if (sd.getType().equals("preference-agent")) {
                                    System.out.println("Ambient agent service found:");
                                    System.out.println("- Service \"" + sd.getName() + "\" provided by agent " + provider.getName());

                                    preferenceAgent = provider;
                                }
                            }
                        }
                    }

                    // the behavior can be removed if we have at least two providers for the ambient-agent service
                    if (preferenceAgent != null) {
                        cancel(inform.getSender(), true);
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });

        // add the parallel behavior
        addBehaviour(ambientDiscoveryBehavior);
        
        addBehaviour(new AgentCheckPreferencesBehavior(this));

        addBehaviour(
                new CyclicBehaviour()
                {
                    @Override
                    public void action()
                    {
                        ACLMessage receivedMsg = myAgent.receive(getMessageTemplate());

                        // register the agent if message received
                        if (receivedMsg != null) {
                            
                                Preferences prefs=new Preferences(receivedMsg.getReplyWith());
                                ((PersonalAgent) myAgent).wakeupPreferences=receivedMsg.getReplyWith();
                                
                                //if (prefs.getWakeMinute(Calendar.DAY_OF_WEEK)==Preferences.getMinuteInDay()){
                                if (((PersonalAgent) myAgent).getCurrentState()==PersonalAgent.STATE_NONE && Math.random()>0.7){
                                    //Start Wake up
                                    int stil=prefs.getWakeStyle(Calendar.DAY_OF_WEEK-1);
                                    ((PersonalAgent) myAgent).setCurrentState(PersonalAgent.STATE_ASKING);
                                    
                                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                                    cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                                    for(AID a:((PersonalAgent) myAgent).getAmbientAgents())
                                        cfp.addReceiver(a);
        
                                    cfp.setConversationId("request-service");
                                stil=Preferences.WAKE_HARD;//DEBUG
                                    cfp.setContent(String.valueOf(stil));
                                    
                                    addBehaviour(new AgentInitiatorBehavior(myAgent, cfp));
                                    
                                    
                                }
                        }
                    }
                }
        );

        
    }

    @Override
    protected void takeDown() {
        // Printout a dismissal message
        System.out.println("PersonalAgent " + getAID().getName() + " terminating.");
    }
    public AID getPreferenceAgent(){
        return preferenceAgent;
    }
    private MessageTemplate getMessageTemplate(){
            return new MessageTemplate(new MessageTemplate.MatchExpression() {
                    private static final long serialVersionUID = 3L;

                    @Override
                    public boolean match(ACLMessage msg) {
                   //         return true;
                            return (msg.getPerformative() == ACLMessage.INFORM && 
                                        msg.getConversationId().equals("preference-value")
                                    );
                    }
            });        
        
    }
    public List<AID> getAmbientAgents(){
        return ambientAgents;
    }
    public int getCurrentState(){
        return currentState;
    }
    public void setCurrentState(int state){
        currentState=state;
    }
}
