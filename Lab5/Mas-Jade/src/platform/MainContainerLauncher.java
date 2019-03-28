package platform;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import agents.MyAgent;

/**
 * Launches a main container and associated agents.
 */
public class MainContainerLauncher {

	/**
	 * The main container.
	 */
	AgentContainer mainContainer;

	Map<Integer, Map<String, Object>> agentConfig = new HashMap<>();
	
	/**
	 * Configures and launches the main container.
	 */
	void setupPlatform() {
		Properties mainProps = new ExtendedProperties();
		mainProps.setProperty(Profile.GUI, "true"); // start the JADE GUI
		mainProps.setProperty(Profile.MAIN, "true"); // is main container
		mainProps.setProperty(Profile.CONTAINER_NAME, "Intro-Main"); // you can rename it
		
		mainProps.setProperty(Profile.LOCAL_HOST, "localhost");
		mainProps.setProperty(Profile.LOCAL_PORT, "1099");
		mainProps.setProperty(Profile.PLATFORM_ID, "intro");

		ProfileImpl mainProfile = new ProfileImpl(mainProps);
		mainContainer = Runtime.instance().createMainContainer(mainProfile);
	}
	
	/**
	 * Read agent parent-child relationship configuration from the data/config.csv file.
	 * @throws IOException 
	 */
	void readConfig() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("data/config.csv")); 
	    String line;
	    
	    while ((line = br.readLine()) != null) {
	        String[] values = line.split(",");
	        int agentId = Integer.parseInt(values[0]);
	        int parentId = Integer.parseInt(values[1]);
	        int agentValue = Integer.parseInt(values[2]);
	        
	        agentConfig.put(agentId, new HashMap<String, Object>(){{
	        	put("parentId", parentId);
	        	put("value", agentValue);
	        }});
	    }
	    
	    br.close();
	}
	
	
	/**
	 * Starts the agents assigned to the main container.
	 */
	void startAgents() {
		try {
			for (Integer agId : agentConfig.keySet()) {
				String agentName = "agent_" + agId;
				
				int parentId = (Integer)agentConfig.get(agId).get("parentId");
				int value = (Integer)agentConfig.get(agId).get("value");
				
				AID parentAID = null; 
				
				if (parentId != 0) {
					parentAID = new AID("agent_" + parentId, AID.ISLOCALNAME);
				}
				
				AgentController agentCtrl = mainContainer.createNewAgent(agentName, MyAgent.class.getName(), 
						new Object[] {parentAID, value});
				agentCtrl.start();
			}
			
			
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Launches the main container.
	 * 
	 * @param args
	 *            - not used.
	 */
	public static void main(String[] args) {
		MainContainerLauncher launcher = new MainContainerLauncher();
		
		try {
	        launcher.readConfig();
	        launcher.setupPlatform();
			launcher.startAgents();
		}
        catch (IOException e) {
	        e.printStackTrace();
        }
		
		
	}

}
