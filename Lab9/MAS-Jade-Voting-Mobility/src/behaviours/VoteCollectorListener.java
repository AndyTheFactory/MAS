/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviours;

import jade.core.Agent;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

/**
 *
 * @author Dell
 */
public class VoteCollectorListener extends ContractNetResponder {
    
    public VoteCollectorListener(Agent a, MessageTemplate mt) {
        super(a, mt);
    }
    
}
