package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import platform.Log;
import platform.Product;

public class BuyerAgent extends Agent {
	
	/**
	 * The serial UID.
	 */
	private static final long serialVersionUID = -4316893632718883072L;

	Map<String, List<Product>> productsMap;
	int resources;
	
	AID coalitionManager;
	
	public Map<String, List<Product>> getProductsMap() {
		return productsMap;
	}
	
	public List<Product> getProducts(String type) {
		return productsMap.get(type);
	}
	
	public int getResources() {
		return resources;
	}
	
	MessageTemplate coalitionBroadcastTemplate;
	MessageTemplate tokenReceiveTemplate;
	ACLMessage tokenAssignMsg;
	
	@Override
	public void setup() {
		resources = (int)getArguments()[0];	
		productsMap = (Map<String, List<Product>>)getArguments()[1];
		coalitionManager = (AID)getArguments()[2];
		
		
		coalitionBroadcastTemplate = new MessageTemplate(new MatchExpression() {
			@Override
			public boolean match(ACLMessage msg) {
                try {
	                if (msg.getPerformative() == ACLMessage.INFORM && 
	                		(msg.getProtocol().equals(CoalitionManager.BROADCAST_COALITION) || msg.getProtocol().equals(CoalitionManager.DELETE_COALITION))) {
	                	AID coalitionLeader = ((CoalitionInfo)msg.getContentObject()).getCoalitionLeader();
	                	
	                	return !coalitionLeader.equals(getAID()); 
	                }
	                
	                return false;
                }
                catch (UnreadableException e) {
	                e.printStackTrace();
                }
				
				return false;
			}
		});
		
		tokenReceiveTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol(CoalitionManager.TOKEN_PASSING_PROTOCOL), 
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		// register listeners
		registerTokenListener();
		registerCoalitionSubscriber();
		
		Log.log(this, "Hello. I have " + resources + " dollars and I know about: " + productsMap);
	}
	
	
	void publishCoalition(CoalitionInfo coalitionInfo) {
		ACLMessage coalitionPublishMsg = new ACLMessage(ACLMessage.REQUEST);
		coalitionPublishMsg.setProtocol(CoalitionManager.PUBLISH_COALITION);
		coalitionPublishMsg.addReceiver(coalitionManager);
		
		try {
	        coalitionPublishMsg.setContentObject(coalitionInfo);
	        send(coalitionPublishMsg);
		}
        catch (IOException e) {
	        e.printStackTrace();
        }
	}
	
	
	void deleteCoalition(CoalitionInfo coalitionInfo) {
		ACLMessage coalitionDeleteMsg = new ACLMessage(ACLMessage.REQUEST);
		coalitionDeleteMsg.setProtocol(CoalitionManager.DELETE_COALITION);
		coalitionDeleteMsg.addReceiver(coalitionManager);
		
		try {
	        coalitionDeleteMsg.setContentObject(coalitionInfo);
	        send(coalitionDeleteMsg);
		}
        catch (IOException e) {
	        e.printStackTrace();
        }
	}
	
	void endNegotiationStep() {
		if (tokenAssignMsg != null) {
			ACLMessage turnEndMsg = tokenAssignMsg.createReply();
			turnEndMsg.addReceiver(coalitionManager);
			send(turnEndMsg);
			
			tokenAssignMsg = null;
		}
	}
	
	
	void registerTokenListener() {
		addBehaviour(new CyclicBehaviour(this) {
            private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				tokenAssignMsg = receive(tokenReceiveTemplate);
				if (tokenAssignMsg != null) {
					handleTokenAssigned();
				}
				else {
					block();
				}
			}
		});
	}
	
	
	void registerCoalitionSubscriber() {
		addBehaviour(new CyclicBehaviour(this) {
            private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				ACLMessage coalitionBroadcast = receive(coalitionBroadcastTemplate);
				if (coalitionBroadcast != null) {
					try {
						if (coalitionBroadcast.getProtocol().equals(CoalitionManager.BROADCAST_COALITION)) {
							handleCoalitionCreated((CoalitionInfo)coalitionBroadcast.getContentObject());
						}
						else if (coalitionBroadcast.getProtocol().equals(CoalitionManager.DELETE_COALITION)) {
							handleCoalitionDeleted((CoalitionInfo)coalitionBroadcast.getContentObject());
						}
                    }
                    catch (UnreadableException e) {
	                    e.printStackTrace();
                    }
				}
				else {
					block();
				}
			}
		});
	}
	
	
	protected void handleCoalitionCreated(CoalitionInfo coalitionInfo) {
		Log.log(this, "Received new coalition info: " + coalitionInfo);
	}
	
	protected void handleCoalitionDeleted(CoalitionInfo coalitionInfo) {
		Log.log(this, "Deleted existing coalition: " + coalitionInfo);
	}
	
	/**
	 * Method called when the CoalitionManager passes the negotiation turn to this agent
	 */
	protected void handleTokenAssigned() {
		List<AID> initialCoalitionMembers = new ArrayList<>();
		initialCoalitionMembers.add(getAID());
		
		CoalitionInfo coalitionInfo = new CoalitionInfo(getAID(), initialCoalitionMembers, resources); 
		publishCoalition(coalitionInfo);
		Log.log(this, "Publishing coalition and ending turn with conv id: " + tokenAssignMsg.getConversationId());
		endNegotiationStep();
	}
	
	
	@Override
	protected void takeDown() {
		// Printout a dismissal message
		Log.log(this, "terminating.");
	}
	
	
	
}
