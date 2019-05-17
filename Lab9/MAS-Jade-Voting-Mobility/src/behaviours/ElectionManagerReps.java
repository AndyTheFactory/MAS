/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviours;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

/**
 *
 * @author Dell
 */
public class ElectionManagerReps extends ContractNetResponder {
    
    public ElectionManagerReps(Agent a, MessageTemplate mt) {
        super(a, mt);
    }
   protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        System.out.println("Agent " + myAgent.getLocalName() + ": received from " + cfp.getSender().getName() );
        
        ACLMessage reply = cfp.createReply();
        
        return reply;
   }
}
