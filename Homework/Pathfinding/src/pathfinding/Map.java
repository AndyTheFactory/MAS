/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinding;

import java.util.ArrayList;

/**
 *
 * @author andrei
 */
public class Map {
    final int SIZE=10;
    final int OBSTACLES=10;
    
        
    private int[][] cells;
    private int nragents;
    
    private ArrayList<Position> startingPos;
    private ArrayList<Position> targetPos;
    private ArrayList<Agent> agents;
   
    class Position{
        private int x,y;
        public Position(int x,int y){
            this.x=x;
            this.y=y;
        }
        @Override
	public boolean equals(Object obj)
	{
            if (obj instanceof Position)
                return (this.x==((Position)obj).x)&&(this.y==((Position)obj).y);
            return false;
        }
        @Override
	public int hashCode()
	{
		return this.x*SIZE + this.y;
	}   
        public void stepX(int step){
            x+=step;
        }
        public void stepY(int step){
            y+=step;
        }
        public void step(int stepX,int stepY){
            this.stepX(stepX);
            this.stepY(stepY);
        }
        public void set(Position newpos){
            x=newpos.x;
            y=newpos.y;
        }
        public int getX(){
            return x;
        }
        public int getY(){
            return y;
        }
        public void setX(int x){
            this.x=x;
        }
        public void setY(int y){
            this.y=y;
        }
        public String toString(){
            return String.format("(%d,%d)", x,y);
        }
    }
    
    public Map(int nrAgents){
        this.nragents=nrAgents;
        cells=new int[SIZE+2][SIZE+2];
        startingPos=new ArrayList<>();
        targetPos=new ArrayList<>();
        agents=new ArrayList<>();
        
        System.out.println(String.format("Generating Map (%d x %d) with %d Agents",SIZE,SIZE,this.nragents));
        
        for (int i=0;i<cells.length;i++){
            cells[i][0]=1;
            cells[i][cells[0].length-1]=1;
        }
        for (int i=0;i<cells[0].length;i++){
            cells[0][i]=1;
            cells[cells[0].length-1][i]=1;
        }
        
        while(startingPos.size()<nrAgents){
            Position p=new Position((int)(Math.random()*SIZE)+1, (int)(Math.random()*SIZE)+1);
            if (!startingPos.contains(p))
                startingPos.add(p);
        }
                
                
        while(targetPos.size()<nrAgents){
            Position p=new Position((int)(Math.random()*SIZE)+1, (int)(Math.random()*SIZE)+1);
            if (!targetPos.contains(p) && !startingPos.contains(p))
                targetPos.add(p);
        }
         
        System.out.println("Making sure paths are possible");
        for (int i=0;i<nrAgents;i++){
            Agent ag=new Agent(i, startingPos.get(i), targetPos.get(i));
            agents.add(ag);
            ensurePath(startingPos.get(i), targetPos.get(i));
        }
        for (int i=0;i<OBSTACLES;i++){
            int x,y;
            do{
                x=(int)(Math.random()*SIZE);
                y=(int)(Math.random()*SIZE);
            }while(cells[x][y]!=0);
            
            cells[x][y]=1;
        }
        for (int i=0;i<cells.length;i++)
            for (int j=0;j<cells[0].length;j++)
                if (cells[i][j]==5)
                    cells[i][j]=0;
        System.out.println("Map done");
    } 
    private void ensurePath(Position pos1,Position pos2){
        Position p=new Position(pos1.getX(),pos1.getY());
        
        while (!p.equals(pos2)){
            int dx=(int)Math.signum(pos2.getX()-p.getX());            
            int dy=(int)Math.signum(pos2.getY()-p.getY());


            double r=Math.random();
            if (r<0.20 && p.getX()-dx>=0 && p.getX()-dx<SIZE)
                dx=-dx;
            if (r>=0.15&&r<0.4 && p.getY()-dy>=0 && p.getY()-dy<SIZE)
                dy=-dy;
            if (cells[p.getX()+dx][p.getY()+dy]!=1){
                p.step(dx, dy);
                cells[p.getX()][p.getY()]=5;
            }
        }       
    }
    public ArrayList<Position> getTargetPositions()
    {
        return targetPos;
    }
    public ArrayList<Position> getStartingPositions()
    {
        return startingPos;
    }
    public static boolean isAdjacent(Position p1,Position p2){
        if (p1.equals(p2))
            return false;
        return (Math.abs(p1.getX()-p2.getX())<=1)&& (Math.abs(p1.getY()-p2.getY())<=1);
    }
    public Agent getAgent(int i){
        if (i>=agents.size())
            throw new IllegalArgumentException(String.format("There is no Keyser Soze - Agent %d",i));
        return agents.get(i);
    }
    public ArrayList<Agent> getAllAgents(){
        return agents;
    }
    
    public String toString()
    {
        StringBuilder sb=new StringBuilder();
        
        for (int i=0;i<cells.length;i++){
            String line1="";
            String line2="";
            for (int j=0;j<cells[0].length;j++)
            {
                if (cells[i][j]==1){
                    line1+="####";
                    line2+="####";
                }else{
                    boolean b=false;
                    for(Agent ag:agents){
                        if (ag.getPosition().equals(new Position(i, j))){
                            line1+=String.format(" A%d ",ag.getId());
                            line2+=" AA ";
                            b=true;
                            break;
                        }else
                            if (ag.getGoal().equals(new Position(i, j))){
                                line1+=String.format(" G%d ",ag.getId());
                                line2+=" GG ";
                                b=true;
                                break;
                            }
                    }
                    if (!b)
                    {
                        line1+="    ";
                        line2+="    ";
                    }
                }
            }
            sb.append(line1+"\n");
            sb.append(line2+"\n");
        }
        return sb.toString();
    }
    
}
