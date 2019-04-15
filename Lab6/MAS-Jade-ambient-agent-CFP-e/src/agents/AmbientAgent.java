/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.Agent;

/**
 *
 * @author Dell
 */
public abstract class AmbientAgent extends Agent{
    abstract public boolean hasCapability(int style);
}
