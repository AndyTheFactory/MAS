package my;

import java.util.HashMap;
import java.util.Map;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import launcher.ContractingStatus;
import launcher.Log;

/**
 * The agent representing a company which can contract building phases.
 */
public class ContractorAgent extends Agent
{
	/**
	 * The serial UID.
	 */
	private static final long		serialVersionUID	= -5898373540918676720L;
	/**
	 * Statuses for provided construction items.
	 */
	Map<String, ContractingStatus>	statuses			= new HashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	protected void setup()
	{
		Map<String, Integer> providedItems = (Map<String, Integer>) getArguments()[0];
		Log.log(this, "Specialties/cost:", providedItems);
		
		// Register the ambient-agent service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		
		for(String item : providedItems.keySet())
		{
			ServiceDescription sd = new ServiceDescription();
			sd.setType(item);
			sd.setName(item + "-Company " + getLocalName());
			dfd.addServices(sd);
			
			statuses.put(item, new ContractingStatus(item, providedItems.get(item)));
		}
		try
		{
			DFService.register(this, dfd);
		} catch(FIPAException fe)
		{
			fe.printStackTrace();
		}
		Log.log(this, "Successfully registered:", dfd);
	}
}
