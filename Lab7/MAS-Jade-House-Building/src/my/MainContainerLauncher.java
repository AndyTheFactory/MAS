package my;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import launcher.Launcher;

/**
 * Launches a main container and associated agents.
 */
public class MainContainerLauncher extends Launcher
{
	{
		setPlatformID("ACME");
	}
	
	
	/**
	 * Creates the container and adds the contractor agents.
	 */
	public MainContainerLauncher()
	{
		JSONObject config = Launcher.readJson(COMPANY_COSTS_V1);
		System.out.println("Finished reading configuration.");
		
		// TODO: fill-in correct IP
		startMainContainer("ACME", "localhost", 1099);
		
		for(Object company : config.getJSONArray("companies"))
		{
			Map<String, Integer> specialties = new HashMap<>();
			for(Object spec : ((JSONObject)company).getJSONArray("specialties"))
				specialties.put(((JSONObject) spec).getString("specialty"), new Integer(((JSONObject) spec).getInt("cost")));
			addAgent(((JSONObject)company).getString("name"), ContractorAgent.class, specialties);
		}
		startAllAgents();
	}
	
	/**
	 * Launches the main container.
	 * 
	 * @param args
	 *            - not used.
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		new MainContainerLauncher();
	}
	
}
