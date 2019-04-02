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
public class ParentOneShotBehaviour extends OneShotBehaviour {
    private static final long serialVersionUID = -8741442323235457781L;
    MyAgent agent;
    public ParentOneShotBehaviour(Agent a){
        agent=(MyAgent)a;
    }
    public void action() {
        for(AID child:agent.getChildAgents()){
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.setConversationId("request-value");
            msg.setReplyWith("ask-value");
            msg.addReceiver(child);

            myAgent.send(msg);
        }
    }
}
