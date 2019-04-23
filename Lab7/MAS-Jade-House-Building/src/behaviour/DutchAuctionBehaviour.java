/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviour;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import launcher.ContractingStatus;
import my.ACMEAgent;

/**
 *
 * @author andrei
 */
public class DutchAuctionBehaviour extends ContractNetInitiator {

    private String serviceName;
    private ContractingStatus status;

    public DutchAuctionBehaviour(Agent a, ACLMessage cfp, String service, ContractingStatus status) {
        super(a, cfp);

        //super(a, null);
        this.serviceName = service;
        this.status = status;

    }

    protected void handleAllResponses(Vector responses, Vector acceptances) {
        Enumeration e = responses.elements();
        ArrayList<ACLMessage> proposals=new ArrayList<>();
        
        ACMEAgent agent=(ACMEAgent) myAgent;
                
        while (e.hasMoreElements()) {
            ACLMessage msg = (ACLMessage) e.nextElement();
            
            if (msg.getPerformative()==ACLMessage.PROPOSE){
                proposals.add(msg);
            }else
                System.out.println(/*getLocalName()+*/": Received unhandled response " + msg.getContent() + ";" + msg.getPerformative() + "  from " + msg.getSender().getLocalName());
        }
        
        if (proposals.size()>0){
            System.out.println("Starting Hard negociations with "+proposals.get(0).getSender().getLocalName());
            status.setPartner(proposals.get(0).getSender());
            status.updatePhase(ContractingStatus.ContractingPhase.NEGOTIATING);
            
            ACLMessage accept = proposals.get(0).createReply();
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            acceptances.add(accept);
            
            proposals.remove(0);
            for(ACLMessage m:proposals){
                ContractingStatus c=new ContractingStatus(status.getConstructionItem(), status.getCostInformation());
                c.setPartner(m.getSender());
                c.updatePhase(ContractingStatus.ContractingPhase.NEGOTIATING);
                agent.addContractingStatus(serviceName, c);
                
                System.out.println("Starting Hard negociations with "+m.getSender().getLocalName());
                ACLMessage accept2 = m.createReply();
                accept2.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                acceptances.add(accept2);
                
            }

        }else{
            System.out.println("No real bidders for "+serviceName);
            if (status.getNegotiationRound()>=ACMEAgent.MAX_DUTCH_ITERATIONS) {
                System.out.println("Sorry, no contract possible for '"+serviceName+"'");
                status.updatePhase(ContractingStatus.ContractingPhase.DONE);
            }else{
                agent.continueAuction(serviceName);
            }
        }
        
    }

    protected void handleRefuse(ACLMessage refuse) {
        System.out.println("Refusal " + refuse.getContent() + " received from " + refuse.getSender().getLocalName());
    }

    protected void handleFailure(ACLMessage failure) {
        System.out.println("Failure " + failure.getContent() + " received from " + failure.getSender().getLocalName());
    }

    protected void handleInform(ACLMessage inform) {
        System.out.println("Inform " + inform.getContent() + " received from " + inform.getSender().getLocalName());
    }
    protected void handlePropose(ACLMessage propose, Vector acceptances) {
        System.out.println("Inform " + propose.getContent() + " received from " + propose.getSender().getLocalName());
    }

}
