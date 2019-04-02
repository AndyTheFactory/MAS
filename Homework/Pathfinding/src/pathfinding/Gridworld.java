/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinding;

/**
 *
 * @author andrei
 */
public class Gridworld {
    private Map map;
    private int timestep;
    
    public Gridworld(int nrAgents){
        map=new Map(nrAgents);
        timestep=0;
    }
    public void step(){
        
        
    }
    public boolean success(){
        boolean res=true;
        for(Agent ag:map.getAllAgents())
            if (!ag.isSuccess()){
                res=false;
                break;
            }
        return res;
    }
    
    public String toString(){
        StringBuilder sb=new StringBuilder();
        
        sb.append(String.format("At Step %d our map looks like: \n", timestep));
        sb.append(map);
        return sb.toString();
        
    }
}
