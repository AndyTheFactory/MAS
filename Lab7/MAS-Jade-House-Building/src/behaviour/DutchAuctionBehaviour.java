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

    private String service;
    private int price;

    public DutchAuctionBehaviour(Agent a, ACLMessage cfp, String service, int price) {
        super(a, cfp);

        this.service = service;
        this.price = price;

    }

    protected void handleAllResponses(Vector responses, Vector acceptances) {
        Enumeration e = responses.elements();
        ArrayList<ACLMessage> proposals=new ArrayList<>();
        
        ACMEAgent agent=(ACMEAgent) myAgent;
        String serviceName="";
                
        while (e.hasMoreElements()) {
            ACLMessage msg = (ACLMessage) e.nextElement();
            serviceName=msg.getConversationId();
            
            if (msg.getPerformative()==ACLMessage.PROPOSE){
                proposals.add(msg);
            }else
                System.out.println(/*getLocalName()+*/": Received unhandled response " + msg.getContent() + ";" + msg.getPerformative() + "  from " + msg.getSender().getLocalName());
        }
        ContractingStatus status=agent.getContractingStatus(serviceName);
        
        if (proposals.size()>0){
            System.out.println("Starting Hard negociations with "+proposals.get(0).getSender().getLocalName());


        }else{
            System.out.println("No real bidders for "+serviceName);
            if (status.getNegotiationRound()>=3) {
                System.out.println("Sorry, no contract possible for "+serviceName);
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

}
