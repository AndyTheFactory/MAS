package agents.behaviors;

import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import platform.AgentConfig;
import agents.CoalitionInfo;
import agents.CoalitionManager;

public class TokenPassingControl extends SimpleBehaviour {

    private static final long serialVersionUID = 1L;
    
    private static final int START_ROUND = 0;
    private static final int AWAIT_STEP_RESPONSE = 1;
    
    
    
    CoalitionManager coalitionManager;
    
    List<AgentConfig> buyerAgentConfigs; 
    Set<CoalitionInfo> coalitionConfigs;
    
    
    int state = START_ROUND;
    int tokenIndex = 0;
    int round = 1;
    MessageTemplate stepResponseTemplate = null;
    
	public TokenPassingControl(CoalitionManager a, List<AgentConfig> buyerAgentConfigs, Set<CoalitionInfo> coalitionConfigs) {
		super(a);
		
		coalitionManager = a;
		
		this.buyerAgentConfigs = buyerAgentConfigs;
		this.coalitionConfigs = coalitionConfigs;
		
		Collections.sort(this.buyerAgentConfigs);
	}
	
	@Override
	public void action() {
		switch(state) {
			case START_ROUND: {
				tokenIndex = 0;
				coalitionManager.setCoalitionSetupChanged(false);
				
				AID currentAgent = new AID(buyerAgentConfigs.get(tokenIndex).getName(), 
						AID.ISLOCALNAME);
				String currentConversationId = "round-" + round + "-" + currentAgent.getLocalName();
				
				// send token to agent
				ACLMessage tokenAssignMsg =  createTokenMessage(currentAgent, CoalitionManager.TOKEN_PASSING_PROTOCOL, currentConversationId);
				coalitionManager.send(tokenAssignMsg);
				
				// change state
				state = AWAIT_STEP_RESPONSE;
				stepResponseTemplate = createMessageTemplate(
						currentAgent, 
						CoalitionManager.TOKEN_PASSING_PROTOCOL,
						currentConversationId
				);
				
				break;
			}
			case AWAIT_STEP_RESPONSE: {
				// check for message notifying end of coalition negotiations step from current agent
				ACLMessage tokenResponseMsg = myAgent.receive(stepResponseTemplate);
				if (tokenResponseMsg != null) {
					if (tokenIndex == buyerAgentConfigs.size() - 1) {
						// it means we have completed a round
						round += 1;
						state = START_ROUND;
					}
					else {
						// the current agent has finished it coalition negotiation step,
						// so send the token to next agent
						tokenIndex += 1;
						AID nextAgent = new AID(buyerAgentConfigs.get(tokenIndex).getName(), 
								AID.ISLOCALNAME);
						String nextConversationId = "round-" + round + "-" + nextAgent.getLocalName();
						
						// send token to next agent
						ACLMessage tokenAssignMsg =  createTokenMessage(nextAgent, CoalitionManager.TOKEN_PASSING_PROTOCOL, nextConversationId);
						coalitionManager.send(tokenAssignMsg);
						
						// update the stepResponseTemplate
						stepResponseTemplate = createMessageTemplate(
								nextAgent, 
								CoalitionManager.TOKEN_PASSING_PROTOCOL,
								nextConversationId
						);
					}
				}
				else
					block();
				
				break;
			}
			default: break;
		}
	}
	
	@Override
	public boolean done() {
		return tokenIndex == (buyerAgentConfigs.size() - 1) && round > 1 && !coalitionManager.coalitionSetupChanged();
	}
	
	private ACLMessage createTokenMessage(AID agent, String protocol, String conversationId) {
		ACLMessage tokenAssignMsg = new ACLMessage(ACLMessage.INFORM);
		tokenAssignMsg.addReceiver(agent);
		tokenAssignMsg.setProtocol(CoalitionManager.TOKEN_PASSING_PROTOCOL);
		tokenAssignMsg.setConversationId(conversationId);
		
		return tokenAssignMsg;
	}
	
	private MessageTemplate createMessageTemplate(AID sender, String protocol, String convId) {
		return MessageTemplate.and(
					MessageTemplate.and(MessageTemplate.MatchSender(sender), 
							MessageTemplate.MatchProtocol(protocol)), 
					MessageTemplate.MatchConversationId(convId)
			);
	}
	
	@Override
	public int onEnd() {
		coalitionManager.announceFinalCoalitions();
		
		return super.onEnd();
	}
}
