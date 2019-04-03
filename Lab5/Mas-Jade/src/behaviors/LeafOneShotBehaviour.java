/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviors;

import agents.MyAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author andrei
 */
public class LeafOneShotBehaviour extends OneShotBehaviour{
    private static final long serialVersionUID = -8741412321321357781L;
    public LeafOneShotBehaviour(Agent a){
        super(a);
    }
    public void action() {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.setConversationId("request-value");
            msg.setReplyWith(((MyAgent)myAgent).getValue().toString());
            msg.setInReplyTo("ask-value");
            msg.addReceiver(((MyAgent)myAgent).getParentAID());
            
            myAgent.send(msg);
            myAgent.doDelete();
    }
    
}
