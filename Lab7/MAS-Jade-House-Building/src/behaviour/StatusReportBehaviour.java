/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviour;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import my.ACMEAgent;

/**
 *
 * @author andrei
 */
public class StatusReportBehaviour extends TickerBehaviour{

    public StatusReportBehaviour(Agent a, long period) {
        super(a, period);
    }

    @Override
    protected void onTick() {
        ACMEAgent agent=(ACMEAgent)myAgent;
        
        System.out.println("=========================STATUS REPORT ==========================");
        
        System.out.println(" Services: ");
        System.out.println(agent.printContractingStatuses());
        
        System.out.println("-------------------------END REPORT ----------------------------");
        
    }
    
}
