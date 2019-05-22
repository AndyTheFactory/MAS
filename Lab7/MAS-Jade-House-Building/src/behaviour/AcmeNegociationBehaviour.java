/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviour;

import jade.core.Agent;
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
public class AcmeNegociationBehaviour extends ContractNetInitiator 
{
    String serviceName;
    ContractingStatus status;
    public AcmeNegociationBehaviour(Agent a, ACLMessage cfp,String serviceName, ContractingStatus status)
    {
        super(a, cfp);
        this.serviceName=serviceName;
        this.status=status;
    }

    
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        Enumeration e = responses.elements();
        ArrayList<ACLMessage> proposals=new ArrayList<>();
        
        ACMEAgent agent=(ACMEAgent) myAgent;
                
        Boolean deal=false;
        
        while (e.hasMoreElements()) {
            ACLMessage msg = (ACLMessage) e.nextElement();
            
            if (!deal && msg.getPerformative()==ACLMessage.PROPOSE){
                ACLMessage accept = msg.createReply();
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                acceptances.add(accept);
                System.out.println("DEAL  " + msg.getContent() + ";" + msg.getContent() + "  from " + msg.getSender().getLocalName());
                deal=true;
                ContractingStatus c=new ContractingStatus(status.getConstructionItem(), status.getCostInformation());
                c.setPartner(msg.getSender());
                c.updatePhase(ContractingStatus.ContractingPhase.DONE);
                agent.addContractingStatus(serviceName, c);
            }
        }
        if (!deal){
            agent.makeMonotonic(serviceName);
        }
    }


    
}

