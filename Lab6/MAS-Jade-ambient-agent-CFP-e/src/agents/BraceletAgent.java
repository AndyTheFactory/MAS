package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * The BraceletAgent.
 */
public class BraceletAgent extends Agent {
	/**
	 * The serial UID.
	 */
	private static final long serialVersionUID = 5088484951993491457L;

	@Override
	public void setup() {
		System.out.println("Hello from BraceletAgent");
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
		// De-register from the yellow pages
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Printout a dismissal message
		System.out.println("BraceletAgent " + getAID().getName() + " terminating.");
	}

}
