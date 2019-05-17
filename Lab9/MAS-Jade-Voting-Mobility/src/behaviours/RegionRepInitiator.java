/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import java.util.Vector;

/**
 *
 * @author Dell
 */
public class RegionRepInitiator  extends ContractNetInitiator {
    
    public RegionRepInitiator(Agent a, ACLMessage cfp) {
        super(a, cfp);
    }
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        System.out.println("Received unhandled responses " + responses);
        
        
        
    }
}
