package behaviors;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

public class RegistrationSendBehavior extends WakerBehaviour {
	
    private static final long serialVersionUID = -8741441435805457781L;
	
    AID parentAID;
    
	public RegistrationSendBehavior(Agent a, long timeout, AID parentAID) {
		super(a, timeout);
		this.parentAID = parentAID;
	}
	
	@Override
	protected void onWake() {
		// Create the registration message as a simple INFORM message
		// with a conversation-id of "register-child"
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setConversationId("register-child");
		msg.addReceiver(parentAID);
		
		myAgent.send(msg);
	
	}
	
	@Override
	public int onEnd() {
		System.out.println("Agent " + myAgent.getAID().getName() + " has sent registration message.");
		
	    return super.onEnd();
	}
	
}
