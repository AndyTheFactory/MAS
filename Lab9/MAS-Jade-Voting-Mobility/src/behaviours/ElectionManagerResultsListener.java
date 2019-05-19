/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviours;

import agents.ElectionManagerAgent;
import agents.RequestType;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;
import java.util.AbstractMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import platform.VoteResult;

/**
 *
 * @author andrei
 */
public class ElectionManagerResultsListener  extends AchieveREResponder {
    
    public ElectionManagerResultsListener(Agent a, MessageTemplate mt) {
        super(a,MessageTemplate.MatchConversationId(RequestType.REPORT_VOTES));
    }

    @Override
    protected ACLMessage handleRequest(ACLMessage cfp) throws NotUnderstoodException, RefuseException {

        ACLMessage reply = cfp.createReply();
        String convId = cfp.getConversationId();
        ElectionManagerAgent electionAgent = (ElectionManagerAgent) this.myAgent;

        System.out.println("Agent " + myAgent.getLocalName() + ": received " + convId + " from " + cfp.getSender().getName());
        
        try {
            AbstractMap.SimpleEntry<String,VoteResult> e   = (AbstractMap.SimpleEntry<String,VoteResult>) cfp.getContentObject();
            electionAgent.addVotes(e.getKey(), e.getValue());
            
        } catch (UnreadableException ex) {
            Logger.getLogger(ElectionManagerResultsListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        return reply;
    }
}
