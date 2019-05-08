package platform;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;

import agents.ElectionManagerAgent;
import agents.RegionRepAgent;
import agents.VoteCollectorAgent;

public class Launcher {
	
	static final String CENTRAL_ELECTION = "CentralElection";
	static final String VOTING_REGION_1 = "VotingRegion1";
	static final String VOTING_REGION_2 = "VotingRegion2";
	static final String VOTING_REGION_3 = "VotingRegion3";
	static final String VOTING_REGION_4 = "VotingRegion4";
	
	static final String[] REGION_CONTAINERS = {
		VOTING_REGION_1, VOTING_REGION_2, VOTING_REGION_3, VOTING_REGION_4
	};
	
	static final String PLATFORM_ID = "voting-agents";
	
	static final String HOST = "localhost";
	static final int PORT = 1099;
	
	/**
	 * The voting agent containers.
	 */
	AgentContainer containerCentralElection;
	ArrayList<AgentContainer> regionContainers = new ArrayList<>();

	
	/**
	 * Configures and launches the central election container.
	 */
	void setupCentralElectionContainer() {
		Properties mainProps = new ExtendedProperties();
		mainProps.setProperty(Profile.GUI, "true"); // start the JADE GUI
		mainProps.setProperty(Profile.MAIN, "true"); // is main container
		mainProps.setProperty(Profile.CONTAINER_NAME, CENTRAL_ELECTION); // you can rename it
		// TODO: replace with actual IP of the current machine
		mainProps.setProperty(Profile.LOCAL_HOST, HOST);
		mainProps.setProperty(Profile.LOCAL_PORT, "" + PORT);
		mainProps.setProperty(Profile.PLATFORM_ID, PLATFORM_ID);

		ProfileImpl mainProfile = new ProfileImpl(mainProps);
		containerCentralElection = Runtime.instance().createMainContainer(mainProfile);
	}
	
	/**
	 * Configures and launches the central election container.
	 */
	void setupRegionContainers() {
		int portCt = 1;
		
		for (String regionContainer : REGION_CONTAINERS) {
			Properties containerProps = new ExtendedProperties();
			containerProps.setProperty(Profile.CONTAINER_NAME, regionContainer);

			containerProps.setProperty(Profile.LOCAL_HOST, HOST);
			containerProps.setProperty(Profile.LOCAL_PORT, "" + (PORT + portCt));
			containerProps.setProperty(Profile.PLATFORM_ID, PLATFORM_ID);
			
			containerProps.setProperty(Profile.MAIN_HOST, HOST);
			containerProps.setProperty(Profile.MAIN_PORT, "" + PORT);

			ProfileImpl containerProfile = new ProfileImpl(containerProps);
			AgentContainer agentContainer = Runtime.instance().createAgentContainer(containerProfile);
			
			regionContainers.add(agentContainer);
			
			portCt++;
		}
	}
	
	
	/**
	 * Starts the agents assigned to the central election container.
	 */
	void startCentralElectionAgents() {
		String electionManagerName = "election_mgr";
		String voteCollectorName = "vote_collector";
		
		try {
			AgentController electionMgrAgentCtrl = containerCentralElection.createNewAgent(electionManagerName, ElectionManagerAgent.class.getName(), null);
			electionMgrAgentCtrl.start();
			
			AgentController voteCollectorAgentCtrl = containerCentralElection.createNewAgent(voteCollectorName, VoteCollectorAgent.class.getName(), null);
			voteCollectorAgentCtrl.start();
			
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Starts the region representative agents
	 */
	void startRegionRepresentativeAgents() {
		int id = 1;
		
		for (AgentContainer agContainer : regionContainers) {
			String regionRepName = "region_rep_" + id;
			String regionVoteName = "region" + (id++);
			
			try {
				AgentController regionRepAgentCtrl = agContainer.createNewAgent(regionRepName, RegionRepAgent.class.getName(), new Object[] {regionVoteName});
				regionRepAgentCtrl.start();
				
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Launches the containers and agents.
	 * 
	 * @param args
	 *            - not used.
	 */
	public static void main(String[] args) {
		Launcher launcher = new Launcher();

		launcher.setupCentralElectionContainer();
		launcher.setupRegionContainers();
		
		launcher.startCentralElectionAgents();
		launcher.startRegionRepresentativeAgents();
	}
}
