/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviors;

import agents.MyAgent;
import static behaviors.RegistrationReceiveBehavior.MAX_TICKS;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.List;

/**
 *
 * @author andrei
 */
public class AllListenToMessages extends CyclicBehaviour{
    private static final long serialVersionUID = 2343434449458693623L;
    private MessageTemplate msgTemplate;

    public AllListenToMessages(Agent a){
        super(a);
        createMessageTemplate();
    }
    private void createMessageTemplate() {
        msgTemplate = new MessageTemplate(new MessageTemplate.MatchExpression() {
        private static final long serialVersionUID = 1L;

                    @Override
                    public boolean match(ACLMessage msg) {
                            return (msg.getPerformative() == ACLMessage.REQUEST && msg.getConversationId().equals("request-value"));
                    }
            });
	    
    }

    public void action() {
        ACLMessage receivedMsg = myAgent.receive(msgTemplate);

        // register the agent if message received
        if (receivedMsg != null) {
            if(receivedMsg.getReplyWith()=="ask-value"){
                List<AID> children=((MyAgent)myAgent).getChildAgents();
                if(children.size()>0){
                    myAgent.addBehaviour(new ParentOneShotBehaviour(myAgent));
                    
                }else{
                    myAgent.addBehaviour(new LeafOneShotBehaviour(myAgent));
                }
                
            }
            if(receivedMsg.getInReplyTo()=="ask-value"){
        	System.out.println("Agent " + receivedMsg.getSender().getName() + " has sent his value");
                Integer rcvdval=Integer.parseInt(receivedMsg.getReplyWith());
                
                if (((MyAgent)myAgent).getValue()<rcvdval)
                    ((MyAgent)myAgent).setValue(rcvdval);
                ((MyAgent)myAgent).delChildAgent(receivedMsg.getSender());
                if (((MyAgent)myAgent).getChildAgents().size()<=0){
                    System.out.println(myAgent.getName()+" Got all messages");
                    myAgent.addBehaviour(new LeafOneShotBehaviour(myAgent));
                }
                
            }
        }
    }
    
}
