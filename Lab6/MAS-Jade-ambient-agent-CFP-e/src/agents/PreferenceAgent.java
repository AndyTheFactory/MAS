package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class PreferenceAgent extends Agent {
	
	
    private static final long serialVersionUID = -3397689918969697329L;

	@Override
	public void setup() {
		System.out.println("Hello from PreferenceAgent");
		
		// Register the preference-agent service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		
		// TODO: register PreferenceAgent to DF with service type: preference-agent and service-name: ambient-wake-up-call
		ServiceDescription sd = new ServiceDescription();
		sd.setType("preference-agent");
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
		System.out.println("PreferenceAgent " + getAID().getName() + " terminating.");
	}
}
