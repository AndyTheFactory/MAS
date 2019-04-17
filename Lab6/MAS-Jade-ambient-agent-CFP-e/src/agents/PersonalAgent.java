package agents;

import agents.behaviors.AgentCheckPreferencesBehavior;
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
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.MessageTemplate;

/**
 * The PersonalAgent.
 */
public class PersonalAgent extends Agent {

    /**
     * The serial UID.
     */
    private static final long serialVersionUID = 2081456560111009192L;

    /**
     * Known ambient agents.
     */
    List<AID> ambientAgents = new LinkedList<>();

    /**
     * Known preference agent
     */
    AID preferenceAgent;
    String wakeupPreferences;

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

        addBehaviour(new CyclicBehaviour()
                {
                    @Override
                    public void action()
                    {
                        ACLMessage receivedMsg = myAgent.receive(
                            new MessageTemplate(new MessageTemplate.MatchExpression() {
                                    private static final long serialVersionUID = 2L;

                                    @Override
                                    public boolean match(ACLMessage msg)
                                    {
                                        return (msg.getPerformative() == ACLMessage.INFORM && msg.getConversationId().equals("preference-value"));
                                    }
                                })
                        );

                        // register the agent if message received
                        if (receivedMsg != null) {
                            ((PersonalAgent)myAgent).
                                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                                msg.setConversationId("preference-value");
                                msg.setReplyWith(((PreferenceAgent)myAgent).getPreferences().toString());
                                msg.setInReplyTo("request-preferences");                                
                                msg.addReceiver(msg.getSender());

                                myAgent.send(msg);
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
    
}
