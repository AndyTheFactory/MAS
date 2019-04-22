/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents.behaviors;

import agents.PersonalAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;

/**
 *
 * @author Dell
 */
public class AgentInitiatorBehavior extends ContractNetInitiator {
    private int wakeUpStyle=-1;
    
    public AgentInitiatorBehavior(Agent a,  ACLMessage cfp) {
        super(a, cfp);
    }

    protected void handlePropose(ACLMessage propose, Vector v) {
        System.out.println("Agent " + propose.getSender().getName() + " proposed " + propose.getContent());
        Enumeration e = v.elements();
        int accepted = 0;
    }

    protected void handleRefuse(ACLMessage refuse) {
        System.out.println("Agent " + refuse.getSender().getName() + " refused");
    }

    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            // does not exist
            System.out.println("Responder does not exist");
        } else {
            System.out.println("Agent " + failure.getSender().getName() + " failed");
        }
    }

    protected void handleAllResponses(Vector responses, Vector acceptances) {
        System.out.println("Handle Responses: " + responses.size() + " responses " + responses.toString());
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
    }
}
