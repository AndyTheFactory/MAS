package behaviors;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import agents.MyAgent;

public class RegistrationReceiveBehavior extends TickerBehaviour {
	
    private static final long serialVersionUID = 2088300789458693623L;
    
    public static final int TICK_PERIOD = 100;
    public static final int MAX_TICKS = 50;
    
    private MessageTemplate msgTemplate;
    
    
	public RegistrationReceiveBehavior(Agent a) {
		super(a, TICK_PERIOD);
		
		createMessageTemplate();
	}
	
	private void createMessageTemplate() {
	    msgTemplate = new MessageTemplate(new MatchExpression() {
            private static final long serialVersionUID = 1L;

			@Override
			public boolean match(ACLMessage msg) {
				return (msg.getPerformative() == ACLMessage.INFORM && msg.getConversationId().equals("register-child"));
			}
		});
	    
    }

	@Override
	protected void onTick() {
		ACLMessage receivedMsg = myAgent.receive(msgTemplate);
		
		// register the agent if message received
		if (receivedMsg != null) {
			AID childAID = receivedMsg.getSender();
			((MyAgent)myAgent).addChildAgent(childAID);
		}
		
		// if number of ticks surpassed, take down the agent
		if (getTickCount() >= MAX_TICKS) {			
			stop();
			
			// TODO: comment this out once you add the other behaviors as well
			myAgent.doDelete();
		}
	}
	
}
