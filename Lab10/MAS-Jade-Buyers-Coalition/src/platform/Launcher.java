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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import agents.BuyerAgent;
import agents.CoalitionManager;

public class Launcher {
	
	static final String PRODUCTS_FILE = "data/products_config.json";
	static final String AGENT_CONFIG_FILE = "data/agent_config_1.json";
	
	
	static final String PLATFORM_ID = "coalition-agents";
	
	static final String HOST = "localhost";
	static final int PORT = 1099;
	
	/**
	 * The agent container.
	 */
	AgentContainer mainContainer;

	
	/**
	 * Configures and launches the main container.
	 */
	void setupMainContainer() {
		Properties mainProps = new ExtendedProperties();
		mainProps.setProperty(Profile.GUI, "true"); // start the JADE GUI
		mainProps.setProperty(Profile.MAIN, "true"); // is main container
		mainProps.setProperty(Profile.CONTAINER_NAME, "coalition"); // you can rename it

		mainProps.setProperty(Profile.LOCAL_HOST, HOST);
		mainProps.setProperty(Profile.LOCAL_PORT, "" + PORT);
		mainProps.setProperty(Profile.PLATFORM_ID, PLATFORM_ID);

		ProfileImpl mainProfile = new ProfileImpl(mainProps);
		mainContainer = Runtime.instance().createMainContainer(mainProfile);
	}
		
	
	/**
	 * Read agent setup from json files, send parameters and start agents
	 */
	void startAgents() {
		try {
			Map<String, List<Product>> productsMap = SetupReader.readProductConfig(PRODUCTS_FILE);
			List<AgentConfig> agentConfigurations = SetupReader.readAgentConfigurations(AGENT_CONFIG_FILE);
			
			// create the CoalitionManager
			AgentController coalitionManagerCtrl = mainContainer.createNewAgent("coalition_mgr", 
					CoalitionManager.class.getName(), new Object[] {new ArrayList<AgentConfig>(agentConfigurations)});
			coalitionManagerCtrl.start();
			
			for (AgentConfig agConf : agentConfigurations) {
				AgentController agentCtrl = mainContainer.createNewAgent(agConf.name, BuyerAgent.class.getName(), 
						new Object[] {agConf.resources, productsMap, new AID("coalition_mgr", AID.ISLOCALNAME)});
				agentCtrl.start();
			}
			
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Launches the container and agents.
	 * 
	 * @param args
	 *            - not used.
	 */
	public static void main(String[] args) {
		Launcher launcher = new Launcher();

		launcher.setupMainContainer();
		launcher.startAgents();
	}
}
