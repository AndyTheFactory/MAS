/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviours;

import agents.InformMessages;
import agents.Regions;
import agents.RequestType;
import agents.VoteCollectorAgent;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.proto.ContractNetInitiator;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import platform.VoteResult;

/**
 *
 * @author andrei
 */
public class VoteCollectorInitiator extends AchieveREInitiator {
    String regionVoteKey;
    public VoteCollectorInitiator(Agent a, ACLMessage cfp) {
        super(a, cfp);
        regionVoteKey=cfp.getContent();
    }

    @Override
    protected void handleAllResponses(Vector responses) {
        VoteCollectorAgent votecollector = (VoteCollectorAgent) this.myAgent;

        if (responses.size() <= 0) {
            //No responses or timeout
            //go home
            System.out.println(this.myAgent.getName() + " Received no response ");
        } else {
            //
            Enumeration e = responses.elements();
            while (e.hasMoreElements()) {
                ACLMessage msg = (ACLMessage) e.nextElement();
                if (msg.getPerformative()==ACLMessage.AGREE){

                    try {
                        VoteResult votes = (VoteResult) msg.getContentObject();
                        votecollector.setVoteResults(regionVoteKey, votes);
                        System.out.println(this.myAgent.getName() + " Got votes for region "+regionVoteKey+" : " + votes);
                        
                        ACLMessage confirmMsg=new ACLMessage(ACLMessage.CONFIRM);
                        confirmMsg.setConversationId(RequestType.CONFIRM_COLLECTED);
                        confirmMsg.setContent(regionVoteKey);
                        confirmMsg.addReceiver(msg.getSender());
                        votecollector.send(confirmMsg);
                        
                    } catch (UnreadableException ex) {
                        Logger.getLogger(VoteCollectorInitiator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }else{
                   // System.out.println(this.myAgent.getName() + " Got reply : " + msg);
                    
                }
            }
        }
        votecollector.doMove(new ContainerID(Regions.getCentralElection(), null));

    }

}
