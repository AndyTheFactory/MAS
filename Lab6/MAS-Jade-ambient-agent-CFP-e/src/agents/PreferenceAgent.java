package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class PreferenceAgent extends Agent
{
    private static final long serialVersionUID = -3397689918969697329L;

    static final int WAKE_SUPERSOFT = 0;
    static final int WAKE_SOFT = 1;
    static final int WAKE_HARD = 2;

    private MyPreferences preference;

    class MyPreferences
    {

        int[] wakeHours;//Minute of the day
        int[] wakeStyle;

        public MyPreferences()
        {
            wakeHours = new int[7];
            wakeStyle = new int[7];
            for (int i = 0; i < wakeHours.length; i++)
            { // Random
                wakeHours[i] = (int) (Math.random() * 24 * 60);
                wakeStyle[i] = (int) (Math.random() * 3);
            }

        }
    }
    public MyPreferences getPreferences(){
        return this.preference;
                
    }
    private static MessageTemplate getMessageTemplate() {
        return new MessageTemplate(new MessageTemplate.MatchExpression() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public boolean match(ACLMessage msg) {
                            return (msg.getPerformative() == ACLMessage.REQUEST && msg.getConversationId().equals("request-preferences"));
                    }
            });
	    
    }

    
    @Override
    public void setup()
    {
        System.out.println("Hello from PreferenceAgent");

        preference=new MyPreferences();
        
        addBehaviour(new CyclicBehaviour()
                {
                    @Override
                    public void action()
                    {
                        ACLMessage receivedMsg = myAgent.receive(getMessageTemplate());

                        // register the agent if message received
                        if (receivedMsg != null) {
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

        // Register the preference-agent service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        // TODO: register PreferenceAgent to DF with service type: preference-agent and service-name: ambient-wake-up-call
        ServiceDescription sd = new ServiceDescription();
        sd.setType("preference-agent");
        sd.setName("ambient-wake-up-call");
        dfd.addServices(sd);
        try
        {
            DFService.register(this, dfd);
        } catch (FIPAException fe)
        {
            fe.printStackTrace();
        }
    }

    @Override
    protected void takeDown()
    {
        // De-register from the yellow pages
        try
        {
            DFService.deregister(this);
        } catch (FIPAException fe)
        {
            fe.printStackTrace();
        }

        // Printout a dismissal message
        System.out.println("PreferenceAgent " + getAID().getName() + " terminating.");
    }
}
