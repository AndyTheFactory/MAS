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
public class Agent {
    int id;
    int nrsteps;
    private Map.Position start;
    private Map.Position goal;
    private Map.Position pos;
    
    enum Move{
        E(1,0),
        W(-1,0),
        N(0,-1),
        S(0,1),
        NE(1,-1),
        SE(1,1),
        SW(-1,1),
        NW(-1,-1),
        WAIT(0,0),
        ;
        
        int dx;
        int dy;
        private Move(int dx,int dy){
            this.dx=dx;
            this.dy=dy;
        }
        public void doMove(Map.Position pos){
            pos.step(this.dx, this.dy);
        }
    }
    
    public Agent(int id,Map.Position start,Map.Position goal){
        this.id=id;
        this.start=start;
        this.pos=start;
        this.goal=goal;
        this.nrsteps=0;
    }
    public void moveTo(Map.Position newpos){
        if (!Map.isAdjacent(pos, newpos))
            throw new IllegalArgumentException(String.format("Agent %d cannot teleport from %s to %s",id,pos,newpos));
        
        pos.step(nrsteps, nrsteps);
        nrsteps+=1;
    }
    
    public Map.Position getStart(){
        return start;
    }
    public Map.Position getGoal(){
        return goal;
    }
    public Map.Position getPosition(){
        return pos;
    }
    public boolean isSuccess(){
        return pos.equals(goal);
    }
    public int getId(){
        return id;
    }
    public int hashCode(){
        return id;
    }
    
}
