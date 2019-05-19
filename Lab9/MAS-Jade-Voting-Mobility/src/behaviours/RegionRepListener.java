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
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andrei
 */
public class RegionRepListener extends AchieveREResponder {

    public RegionRepListener(Agent a, MessageTemplate mt) {
        
        super(a, MessageTemplate.MatchConversationId(RequestType.ASK_VOTES_FROM_REP));
    }
    @Override
    protected ACLMessage handleRequest(ACLMessage cfp) throws NotUnderstoodException, RefuseException {

        String regionVoteKey = cfp.getContent();

        ACLMessage reply = cfp.createReply();

        RegionRepAgent regionrep = (RegionRepAgent) this.myAgent;
        
        if (regionVoteKey.equals(regionrep.getRegionVoteKey())) {
            System.out.println("Agent " + myAgent.getLocalName() + ": sending Votes to " + cfp.getSender().getName());
            reply.setPerformative(ACLMessage.AGREE);
            try {
                reply.setContent(regionVoteKey);
                reply.setContentObject(regionrep.getVoteResult());
                regionrep.setVotesSent();
            } catch (IOException ex) {
                Logger.getLogger(VoteCollectorListener.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } else {
            throw new RefuseException(InformMessages.INFORM_NOT_MY_REGION);
        }

        return reply;
    }
}
