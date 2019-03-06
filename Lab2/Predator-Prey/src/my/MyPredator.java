package my;

import base.Action;
import base.Perceptions;
import gridworld.GridPosition;
import hunting.AbstractWildlifeAgent;
import hunting.WildlifeAgentType;
import java.util.Set;
import my.MyEnvironment.MyAction;

/**
 * Implementation for predator agents.
 * 
 * @author Alexandru Sorici
 */
public class MyPredator extends AbstractWildlifeAgent
{
	/**
	 * Default constructor.
	 */
	public MyPredator()
	{
		super(WildlifeAgentType.PREDATOR);
	}
	
	@Override
	public Action response(Perceptions perceptions)
	{
		// TODO Auto-generated method stub
               MyEnvironment.MyPerceptions wildlifePerceptions = (MyEnvironment.MyPerceptions) perceptions;
               GridPosition myPos=wildlifePerceptions.getAgentPos();
               GridPosition bestPrey=null;
               for(GridPosition prey: wildlifePerceptions.getNearbyPrey()){
                   
                   if (bestPrey==null)
                       bestPrey=prey;
                   else
                       bestPrey=(myPos.getDistanceTo(prey)<myPos.getDistanceTo(bestPrey))?prey:bestPrey;
                           
                   
               }
               
               if (bestPrey!=null){
                   if (bestPrey.getX()<myPos.getX())
                       return MyAction.WEST;
                   else
                   if (bestPrey.getX()>myPos.getX())
                       return MyAction.EAST;
                   else
                   if (bestPrey.getY()>myPos.getY())
                       return MyAction.NORTH;
                   else
                   if (bestPrey.getY()<myPos.getY())
                       return MyAction.SOUTH;
                               
               }
               double choice = Math.random()*100;
               if (choice<=10)
                   return MyAction.EAST;
               if (choice>10 && choice<=30)
                   return MyAction.WEST;
               if (choice>30 && choice<=60)
                   return MyAction.NORTH;
               if (choice>60 && choice<=100)
                   return MyAction.SOUTH;
		return null;
	}
	
}
