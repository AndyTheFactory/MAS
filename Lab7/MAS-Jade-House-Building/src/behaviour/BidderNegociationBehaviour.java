/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviour;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import my.ContractorAgent;

/**
 *
 * @author andrei
 */
public class BidderNegociationBehaviour extends ContractNetResponder 
{
    
    public BidderNegociationBehaviour(Agent a, MessageTemplate mt)
    {
        super(a, mt);
    }
    
    protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        ContractorAgent agent=(ContractorAgent)myAgent;
        int proposal = Integer.parseInt(cfp.getContent());
        String serviceName=cfp.getConversationId();
        int cost=agent.getMonotonicCost(serviceName);
        ACLMessage reply = cfp.createReply();
        
        if (proposal>=cost){
            reply.setPerformative(ACLMessage.PROPOSE);
            reply.setContent(String.valueOf(proposal));
        }else{
            throw new RefuseException("Not good enough");
        }
        return reply;
    }
}
