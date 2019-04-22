/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviour;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import launcher.ContractingStatus;
import my.ContractorAgent;

/**
 *
 * @author andrei
 */
public class BidderBehaviour extends ContractNetResponder {

    public BidderBehaviour(Agent a) {
        super(a, MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP)
        )
        );
    }

    protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        System.out.println("Agent " + myAgent.getLocalName() + ": CFP received from " + cfp.getSender().getName() + ". Action is " + cfp.getContent());
        ContractorAgent agent=(ContractorAgent)myAgent;
        String[] content=cfp.getContent().split("\n");
        int proposal = Integer.parseInt(content[0]);
        String serviceName=cfp.getConversationId();
        int cost=agent.getCost(serviceName);
        
        if (proposal > cost) {
            // We provide a proposal
            System.out.println("Agent " + myAgent.getLocalName() + ": Proposing " + proposal);
            ACLMessage propose = cfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);
            //propose.setConversationId(cfp.getConversationId());
            propose.setContent(String.valueOf(proposal));
            
            ContractingStatus status=agent.getContractingStatus(serviceName);
            
            status.updatePhase(ContractingStatus.ContractingPhase.NEGOTIATING);
            
            
            return propose;
        } else {
            // We refuse to provide a proposal
            System.out.println("Agent " + myAgent.getLocalName() + ": Refuse");
            throw new RefuseException("evaluation-failed");
        }
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
        System.out.println("Agent " + myAgent.getLocalName() + ": Proposal accepted");

        ContractorAgent agent=(ContractorAgent)myAgent;
        String[] content=cfp.getContent().split("\n");
        int proposal = Integer.parseInt(content[0]);
        String serviceName=cfp.getConversationId();
        
        ACLMessage inform = accept.createReply();
	inform.setPerformative(ACLMessage.INFORM);
        ContractingStatus status=agent.getContractingStatus(serviceName);

        status.updatePhase(ContractingStatus.ContractingPhase.DONE);
            
        return inform;        
    }

    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        System.out.println("Agent " + myAgent.getLocalName() + ": Proposal rejected");
        ContractorAgent agent=(ContractorAgent)myAgent;
        String[] content=cfp.getContent().split("\n");
        int proposal = Integer.parseInt(content[0]);
        String serviceName=cfp.getConversationId();
        
        ContractingStatus status=agent.getContractingStatus(serviceName);

        status.updatePhase(ContractingStatus.ContractingPhase.DONE);
            
    }

}
