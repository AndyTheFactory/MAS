package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import platform.AgentConfig;
import platform.Log;
import agents.behaviors.TokenPassingControl;

public class CoalitionManager extends Agent {
	
    private static final long serialVersionUID = 1L;
    public static final String PUBLISH_COALITION = "publish_coalition";
    public static final String DELETE_COALITION = "delete_coalition";
    public static final String BROADCAST_COALITION = "broadcast_coalition";
    public static final String TOKEN_PASSING_PROTOCOL = "coalition-formation-protocol";
	
    List<AgentConfig> buyerAgentConfigurations;
    List<AID> buyerAgents;
    MessageTemplate coalitionPublishTemplate;
    MessageTemplate coalitionDeleteTemplate;
    
    /**
     * maintaining status of coalition formations
     */
    Set<CoalitionInfo> coalitionConfigurations = new HashSet<>();
    boolean coalitionSetupChanged = false;
    
    private List<AID> getAgentIDsFromConfig(List<AgentConfig> agentConfigurations) {
		List<AID> agentIDs = new ArrayList<AID>();
		for (AgentConfig agConfig : agentConfigurations) {
			agentIDs.add(new AID(agConfig.getName(), AID.ISLOCALNAME));
		}
		
		return agentIDs;
    }
    
    @Override
	public void setup() {
		buyerAgentConfigurations = (List<AgentConfig>)getArguments()[0];
		buyerAgents = getAgentIDsFromConfig(buyerAgentConfigurations);
		
		coalitionPublishTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST), 
				MessageTemplate.MatchProtocol(PUBLISH_COALITION));
		
		coalitionDeleteTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST), 
				MessageTemplate.MatchProtocol(DELETE_COALITION));
		
		
		registerCoalitionListener();
		
		Log.log(this, "Hello.");
		
		addBehaviour(new TokenPassingControl(this, buyerAgentConfigurations, coalitionConfigurations));
	}
    
    
	private void registerCoalitionListener() {
		addBehaviour(new CyclicBehaviour(this) {
            private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				// look for a coalition publish message
				ACLMessage coalitionMsg = receive(coalitionPublishTemplate);
				if (coalitionMsg != null) {
					try {
						// add coalition to configuration set
	                    boolean isnew = coalitionConfigurations.add((CoalitionInfo)coalitionMsg.getContentObject());
	                    if (isnew) {
	                    	setCoalitionSetupChanged(true);
	                    	
	                    	// broadcast new formation
							broadcastCoalitionChange(coalitionMsg, PUBLISH_COALITION);
	                    }
                    }
                    catch (UnreadableException e) {
	                    e.printStackTrace();
                    }
				}
				else {
					// look for a coalition delete message
					coalitionMsg = receive(coalitionDeleteTemplate);
					if (coalitionMsg != null) {
						try {
							// add coalition to configuration set
		                    coalitionConfigurations.remove((CoalitionInfo)coalitionMsg.getContentObject());
		                    setCoalitionSetupChanged(true);
		                    
		                    // broadcast new formation
							broadcastCoalitionChange(coalitionMsg, DELETE_COALITION);
	                    }
	                    catch (UnreadableException e) {
		                    e.printStackTrace();
	                    }
					}
					else {
						block();
					}
				}
			}
		});
    }
	
	
	private void broadcastCoalitionChange(ACLMessage coalitionPublishMsg, String protocol) {
		try {
			ACLMessage coalitionBroadcastMsg = new ACLMessage(ACLMessage.INFORM);
			coalitionBroadcastMsg.setProtocol(protocol);
	        coalitionBroadcastMsg.setContentObject(coalitionPublishMsg.getContentObject());
	        
	        for (AID buyerAID : buyerAgents) {
	        	coalitionBroadcastMsg.addReceiver(buyerAID);
	        }
	        
	        send(coalitionBroadcastMsg);
        }
        catch (IOException e) {
	        e.printStackTrace();
        }
        catch (UnreadableException e) {
	        e.printStackTrace();
        }
	}
	
	
	public boolean coalitionSetupChanged() {
		return coalitionSetupChanged;
	}
	
	public void setCoalitionSetupChanged(boolean status) {
		coalitionSetupChanged = status;
	}

	public void announceFinalCoalitions() {
	    Log.log(this, "Final coalition formations: " + coalitionConfigurations);
    }
}
