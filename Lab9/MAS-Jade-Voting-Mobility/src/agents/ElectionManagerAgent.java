package agents;

import behaviours.ElectionManagerReps;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import platform.Log;

/**
 * ElectionManager agent.
 */
public class ElectionManagerAgent extends Agent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3397689918969697329L;
	
	@Override
	public void setup()
	{
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
		try
		{
			DFService.register(this, dfd);
		} catch(FIPAException fe)
		{
			fe.printStackTrace();
		}
                
                addBehaviour(new ElectionManagerReps(this, null));
	}
	
	@Override
	protected void takeDown()
	{
		// De-register from the yellow pages
		try
		{
			DFService.deregister(this);
		} catch(FIPAException fe)
		{
			fe.printStackTrace();
		}
		
		// Printout a dismissal message
		Log.log(this, "terminating.");
	}
}
