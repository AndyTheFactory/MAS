package my;

import base.Action;
import base.Agent;
import base.Perceptions;
import gridworld.GridOrientation;
import gridworld.GridRelativeOrientation;

/**
 * Your implementation of a reactive cleaner agent.
 * 
 * @author Andrei Olaru
 */
public class MyAgent implements Agent
{
	@Override
	public Action response(Perceptions perceptions)
	{
		// TODO Auto-generated method stub
		MyEnvironment.MyAgentPerceptions percept = (MyEnvironment.MyAgentPerceptions) perceptions;
		System.out.println("Agent sees current tile is " + (percept.isOverJtile() ? "dirty" : "clean")
				+ "; current orientation is " + percept.getAbsoluteOrientation() + "; obstacles at: "
				+ percept.getObstacles());
		// // clean
		if(percept.isOverJtile())
			return MyEnvironment.MyAction.PICK;
		// // turn
		if(percept.getObstacles().contains(GridRelativeOrientation.FRONT)){                    
                    if(percept.getAbsoluteOrientation()==GridOrientation.SOUTH ){
                        if(percept.getObstacles().contains(GridRelativeOrientation.FRONT_LEFT) &&
                                percept.getObstacles().contains(GridRelativeOrientation.LEFT) &&
                                percept.getObstacles().contains(GridRelativeOrientation.BACK_LEFT)&&
                                percept.getObstacles().contains(GridRelativeOrientation.FRONT_RIGHT)
                                ) //SOUTH wall RIGHT corner
                            return MyEnvironment.MyAction.TURN_RIGHT;             
                        else
                            return MyEnvironment.MyAction.TURN_LEFT;             
                    }
                    if(percept.getAbsoluteOrientation()==GridOrientation.NORTH )
                        return MyEnvironment.MyAction.TURN_RIGHT;             

                    if(percept.getAbsoluteOrientation()==GridOrientation.EAST )
                            return MyEnvironment.MyAction.TURN_RIGHT;             

                    if(percept.getAbsoluteOrientation()==GridOrientation.WEST ){
                        if(percept.getObstacles().contains(GridRelativeOrientation.FRONT_LEFT) &&
                                percept.getObstacles().contains(GridRelativeOrientation.LEFT) &&
                                percept.getObstacles().contains(GridRelativeOrientation.BACK_LEFT)
                                ) //SOUTH wall LEFT corner
                            return MyEnvironment.MyAction.TURN_RIGHT;             
                        else
                            return MyEnvironment.MyAction.TURN_LEFT;             
                    }
                }
                if(percept.getAbsoluteOrientation()==GridOrientation.EAST ){
                    if(percept.getObstacles().contains(GridRelativeOrientation.FRONT_LEFT) &&
                            percept.getObstacles().contains(GridRelativeOrientation.LEFT) &&
                            percept.getObstacles().contains(GridRelativeOrientation.BACK_LEFT)
                            ){ //North wall
                        if (Math.random()>0.8)
                            return MyEnvironment.MyAction.TURN_RIGHT;
                        else
                            return MyEnvironment.MyAction.FORWARD;
                    }

                    if(percept.getObstacles().contains(GridRelativeOrientation.FRONT_RIGHT) &&
                            percept.getObstacles().contains(GridRelativeOrientation.RIGHT) &&
                            percept.getObstacles().contains(GridRelativeOrientation.BACK_RIGHT)
                            ){ //SOUTH wall
                        if (Math.random()>0.5)
                            return MyEnvironment.MyAction.TURN_LEFT;
                        else
                            return MyEnvironment.MyAction.FORWARD;
                    }
                    
                    if(!percept.getObstacles().contains(GridRelativeOrientation.LEFT) &&
                            percept.getObstacles().contains(GridRelativeOrientation.BACK_LEFT)
                            ) //SOUTH wall
                        return MyEnvironment.MyAction.TURN_LEFT;                        
                    if(!percept.getObstacles().contains(GridRelativeOrientation.RIGHT) &&
                            percept.getObstacles().contains(GridRelativeOrientation.BACK_RIGHT)
                            ) //SOUTH wall
                        return MyEnvironment.MyAction.TURN_RIGHT;                        
                }

                if(percept.getAbsoluteOrientation()==GridOrientation.WEST){
                    if(percept.getObstacles().contains(GridRelativeOrientation.RIGHT) &&
                            !percept.getObstacles().contains(GridRelativeOrientation.BACK) &&
                            !percept.getObstacles().contains(GridRelativeOrientation.BACK_RIGHT) &&
                            !percept.getObstacles().contains(GridRelativeOrientation.FRONT_RIGHT))//just passed an obstacle, coming from north
                                return MyEnvironment.MyAction.TURN_LEFT;
                    
                    if(percept.getObstacles().contains(GridRelativeOrientation.LEFT) &&
                            !percept.getObstacles().contains(GridRelativeOrientation.BACK) &&
                            !percept.getObstacles().contains(GridRelativeOrientation.BACK_LEFT) &&
                            !percept.getObstacles().contains(GridRelativeOrientation.FRONT_LEFT))//just passed an obstacle, coming from north
                                return MyEnvironment.MyAction.TURN_RIGHT;
                }

                if(percept.getAbsoluteOrientation()==GridOrientation.NORTH){
                    if(percept.getObstacles().contains(GridRelativeOrientation.BACK_LEFT) &&
                            !percept.getObstacles().contains(GridRelativeOrientation.BACK) &&
                            !percept.getObstacles().contains(GridRelativeOrientation.LEFT))//just passed an obstacle
                                return MyEnvironment.MyAction.TURN_LEFT;
                }

                if(percept.getAbsoluteOrientation()==GridOrientation.SOUTH){
                    if(percept.getObstacles().contains(GridRelativeOrientation.BACK_RIGHT) &&
                            !percept.getObstacles().contains(GridRelativeOrientation.BACK) &&
                            !percept.getObstacles().contains(GridRelativeOrientation.RIGHT))//just passed an obstacle
                                return MyEnvironment.MyAction.TURN_RIGHT;
                }

                // forward
		return MyEnvironment.MyAction.FORWARD;
	}
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		// please use a single character
		return "M";
	}
	
}
