/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviours;

import agents.InformMessages;
import agents.RegionRepAgent;
import agents.Regions;
import agents.RequestType;
import agents.VoteCollectorAgent;
import com.sun.tools.sjavac.Log;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;
import jade.wrapper.ControllerException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import platform.Launcher;

/**
 *
 * @author Dell
 */
public class VoteCollectorListener extends AchieveREResponder {
    
    public VoteCollectorListener(Agent a, MessageTemplate mt) {
        super(a, MessageTemplate.MatchConversationId(RequestType.SEND_VOTE_COLLECTOR));
    }
    @Override
   protected ACLMessage handleRequest(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
       
       System.out.println("Agent " + myAgent.getLocalName() + ": received from " + cfp.getSender().getName() );
       String regionVoteKey=cfp.getContent();
       ACLMessage reply=cfp.createReply();
        
        VoteCollectorAgent votecollector=(VoteCollectorAgent)this.myAgent;
        String currentContainer="";
        try {
            currentContainer=votecollector.getContainerController().getContainerName();
            System.out.println(String.format(" Vote collector currently in region %s", currentContainer));
        } catch (ControllerException ex) {
            Logger.getLogger(VoteCollectorListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (currentContainer.equals(Regions.getCentralElection()) ||
                    currentContainer.equals(Regions.getContainerForRegion(regionVoteKey))
                    ){
            reply.setPerformative(ACLMessage.AGREE);
            reply.setContent(regionVoteKey);
            votecollector.doMove(new ContainerID(Regions.getContainerForRegion(regionVoteKey), null));
        }else{
            throw new RefuseException("Not at home");
        }
        
        return reply;
   }
}
