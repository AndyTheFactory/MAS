/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviours;

import agents.ElectionManagerAgent;
import agents.InformMessages;
import agents.RequestType;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.domain.FIPAService;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

/**
 *
 * @author Dell
 */
public class ElectionManagerListener extends AchieveREResponder {

    public ElectionManagerListener(Agent a, MessageTemplate mt) {
        super(a,MessageTemplate.MatchConversationId(RequestType.ASK_VOTE_COLLECTOR));
    }

    @Override
    protected ACLMessage handleRequest(ACLMessage cfp) throws NotUnderstoodException, RefuseException {

        ACLMessage reply = cfp.createReply();
        String convId = cfp.getConversationId();
        ElectionManagerAgent electionAgent = (ElectionManagerAgent) this.myAgent;
        System.out.println("Agent " + myAgent.getLocalName() + ": received " + convId + " from " + cfp.getSender().getName());

        String regionVoteKey = cfp.getContent();

        System.out.println("   Ask Vote collect received for region: " + regionVoteKey);

	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(electionAgent.getVoteCollector());
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // We want to receive a reply in 10 secs
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 30000));
        msg.setConversationId(RequestType.SEND_VOTE_COLLECTOR);
        msg.setReplyWith(RequestType.SEND_VOTE_COLLECTOR + System.currentTimeMillis());
        msg.setContent(regionVoteKey); 
        
        reply.setContent(regionVoteKey);
        
        electionAgent.addBehaviour(new AchieveREInitiator(electionAgent, msg){
            @Override
            protected void handleAllResponses(Vector responses) {
                if (responses.size() <= 0) {
                    //No responses or timeout
                    //go home
                    System.out.println(this.myAgent.getName() + " Received no response ");
                    reply.setPerformative(ACLMessage.REFUSE);

                } else {
                    //
                    Enumeration e = responses.elements();
                    while (e.hasMoreElements()) {
                        ACLMessage msg = (ACLMessage) e.nextElement();
                        if (msg.getPerformative()==ACLMessage.AGREE){
                            System.out.println(myAgent.getName()+" - Vote collector will come.. can't wait !");
                            reply.setPerformative(ACLMessage.AGREE);
                        }else{
                            reply.setPerformative(ACLMessage.REFUSE);
                            System.out.println(myAgent.getName()+" - Vote collector is busy :( will call him later !");

                        }
                    }

                }
                electionAgent.send(reply);
                
            }            
        });
        
/*
        if (electionAgent.sendVoteCollector(regionVoteKey)) {
            reply.setPerformative(ACLMessage.AGREE);
            reply.setContent(regionVoteKey);
        } else {
            throw new RefuseException(InformMessages.INFORM_COLLECTOR_BUSY);
        }
*/
        return null;
    }
}
