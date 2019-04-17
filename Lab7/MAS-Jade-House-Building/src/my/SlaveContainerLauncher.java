package my;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import launcher.Launcher;

/**
 * Launches a slave container and associated agents.
 */
public class SlaveContainerLauncher extends Launcher
{
	{
		setPlatformID("ACME");
	}
	
	/**
	 * Creates the container and adds the ACME agent.
	 */
	public SlaveContainerLauncher()
	{
		Map<String, Integer> elements = new HashMap<>();
		JSONObject config = Launcher.readJson("data/ACME-project.json");
		for(Object element : config.getJSONArray("elements"))
			elements.put(((JSONObject) element).getString("name"),
					new Integer(((JSONObject) element).getInt("budget")));
		System.out.println("Finished reading configuration.");
		String mainIP, localIP;
		
		// TODO: fill-in correct IP
		mainIP = "localhost";
		localIP = "localhost";
		
		startSlaveContainer("ACME", mainIP, 1099, localIP, 1100);
		
		addAgent("ACME", ACMEAgent.class, elements);
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
		new SlaveContainerLauncher();
	}
}
