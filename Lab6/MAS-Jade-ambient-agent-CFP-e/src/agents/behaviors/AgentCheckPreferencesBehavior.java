/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents.behaviors;

import agents.PersonalAgent;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author andrei
 */
public class AgentCheckPreferencesBehavior extends TickerBehaviour
{
    public static final int TICK_PERIOD = 5000;

    public AgentCheckPreferencesBehavior(Agent a)
    {
        super(a, TICK_PERIOD);
    }

    @Override
    protected void onTick()
    {
        if (((PersonalAgent)myAgent).getPreferenceAgent()==null)
            return;
  	System.out.println("Agent "+this.myAgent.getLocalName()+" polling \"preference-agent\"");
        
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setConversationId("request-preferences");
        msg.addReceiver(((PersonalAgent)myAgent).getPreferenceAgent());
        myAgent.send(msg);
        
        
    }
    
}
