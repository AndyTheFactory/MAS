package my;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import launcher.ContractingStatus;
import launcher.Log;

/**
 * The agent managing construction of the ACME headquarters.
 */
public class ACMEAgent extends Agent
{
	/**
	 * The serial UID.
	 */
	private static final long		serialVersionUID		= 2897763463127840876L;
	/**
	 * Statuses.
	 */
	Map<String, ContractingStatus>	statuses				= new LinkedHashMap<>();
	/**
	 * Who can do each item.
	 */
	Map<String, Set<AID>>			potentialContractors	= new HashMap<>();
	
	@Override
	protected void setup()
	{
		@SuppressWarnings("unchecked")
		Map<String, Integer> items = (Map<String, Integer>) getArguments()[0];
		
		Log.log(this, "Construction phases/budget:", items);
		
		for(String item : items.keySet())
		{
			// create item information
			statuses.put(item, new ContractingStatus(item, items.get(item)));
			potentialContractors.put(item, new HashSet<>());
			
			// search available agents
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType(item);
			template.addServices(sd);
			try
			{
				DFAgentDescription[] results = DFService.search(this, template);
				if(results != null)
					for(DFAgentDescription result : results)
						potentialContractors.get(item).add(result.getName());
				Log.log(this, "<", item, ">", "Available agents for construction item:",
						potentialContractors.get(item));
			} catch(FIPAException fe)
			{
				fe.printStackTrace();
			}
		}
	}
}
