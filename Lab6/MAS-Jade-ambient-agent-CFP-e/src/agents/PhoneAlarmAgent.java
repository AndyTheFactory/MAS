package agents;

import agents.behaviors.AgentResponderBehavior;
import data.Preferences;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * The PhoneAlarmAgent.
 */
public class PhoneAlarmAgent extends AmbientAgent {

    /**
     * The serial UID.
     */
    private static final long serialVersionUID = -4316893632718883072L;

    @Override
    public void setup() {
        System.out.println("Hello from PhoneAlarmAgent");

        // Register the ambient-agent service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("ambient-agent");
        sd.setName("ambient-wake-up-call");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));
        addBehaviour(new AgentResponderBehavior(this, template));

    }

    @Override
    protected void takeDown() {
        // Unregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Printout a dismissal message
        System.out.println("PhoneAlarmAgent " + getAID().getName() + " terminating.");
    }

    @Override
    public boolean hasCapability(int style) {
        return (style == Preferences.WAKE_SOFT || style == Preferences.WAKE_SUPERSOFT);
    }

}
