package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * The PhoneAlarmAgent.
 */
public class PhoneAlarmAgent extends Agent {
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
}
