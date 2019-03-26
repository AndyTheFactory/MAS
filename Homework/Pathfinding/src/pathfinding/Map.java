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
    
    class Position{
        int x,y;
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
            x+=step;
        }
        public void step(int stepX,int stepY){
            x+=stepX;
            y+=stepY;
        }
    }
    
    public Map(int nrAgents){
        this.nragents=nrAgents;
        cells=new int[SIZE+2][SIZE+2];
        startingPos=new ArrayList<>();
        targetPos=new ArrayList<>();
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
         
        for (int i=0;i<nrAgents;i++){
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
    } 
    private void ensurePath(Position pos1,Position pos2){
        Position p=new Position(pos1.x,pos1.y);
        
        while (!p.equals(pos2)){
            int dx=(int)Math.signum(pos2.x-p.x);            
            int dy=(int)Math.signum(pos2.y-p.y);
            
            double r=Math.random();
            if (r<0.20)
                dx=-dx;
            if (r>=0.15&&r<0.4)
                dy=-dy;
            if (cells[p.x+dx][p.y+dy]!=1){
                p.step(dx, dy);
                cells[p.x][p.y]=5;
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
                    
                    if (cells[i][j]==5){
                        line1+=" *  ";
                        line2+=" *  ";
                    }else
                    if (startingPos.contains(new Position(i, j)))
                    {
                        line1+=" S  ";
                        line2+=" S  ";
                    }else
                    if (targetPos.contains(new Position(i, j)))
                    {
                        line1+=" G  ";
                        line2+=" G  ";
                    }else
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
