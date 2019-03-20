package my;

import base.Action;
import base.Perceptions;
import communication.AgentID;
import communication.AgentMessage;
import communication.SocialAction;
import gridworld.AbstractGridEnvironment;
import gridworld.GridOrientation;
import gridworld.GridPosition;
import gridworld.GridRelativeOrientation;
import gridworld.ProbabilityMap;
import hunting.AbstractWildlifeAgent;
import hunting.WildlifeAgentType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import my.MyEnvironment.MyAction;

/**
 * Implementation for predator agents.
 *
 * @author Alexandru Sorici
 */
public class MyPredator extends AbstractWildlifeAgent {

    /**
     * Default constructor.
     */
    ArrayList<AgentID> PredatorFriends;

    private class PreyLocationMessage {

        GridPosition preyData;
        GridPosition predatorData;

        public PreyLocationMessage(GridPosition preyData, GridPosition predatorData) {
            this.predatorData = predatorData;
            this.preyData = preyData;
        }
    }

    public MyPredator() {
        super(WildlifeAgentType.PREDATOR);
        PredatorFriends = new ArrayList<>();
    }

    private SocialAction createSocialAction(MyAction action, Set<AgentMessage> messages) {
        SocialAction res = new SocialAction(action);
        res.getOutgoingMessages().addAll(messages);
        return res;
    }

    @Override
    public Action response(Perceptions perceptions) {
        // TODO Auto-generated method stub
        MyEnvironment.MyPerceptions wildlifePerceptions = (MyEnvironment.MyPerceptions) perceptions;
        GridPosition myPos = wildlifePerceptions.getAgentPos();
        ProbabilityMap probMap = new ProbabilityMap();


        Set<AgentMessage> messages = new HashSet<>();

        Map<AgentID, GridPosition> predators = wildlifePerceptions.getNearbyPredators();

        for (AgentID predator : predators.keySet()) { //Remember seen predators
            if (!PredatorFriends.contains(predator) && !predator.equals(AgentID.getAgentID(this))  ) {
                PredatorFriends.add(predator);
            }
        }

        GridPosition bestPrey = null;
        for (GridPosition prey : wildlifePerceptions.getNearbyPrey()) {

            if (bestPrey == null) {
                bestPrey = prey;
            } else {
                bestPrey = (myPos.getDistanceTo(prey) < myPos.getDistanceTo(bestPrey)) ? prey : bestPrey;
            }

            for (AgentID friend : PredatorFriends) {
                messages.add(new AgentMessage(AgentID.getAgentID(this), friend, new PreyLocationMessage(prey, myPos)));
            }

        }
        if (this.id % 4 == 0){
            //sweeper
            Set<GridPosition> obstacles=wildlifePerceptions.getObstacles();
            if (obstacles.contains(myPos.getNeighborPosition(GridOrientation.WEST)) || 
                    obstacles.contains(myPos.getNeighborPosition(GridOrientation.SOUTH))){
                if (!obstacles.contains(myPos.getNeighborPosition(GridOrientation.SOUTH)))
                    probMap.put(MyAction.SOUTH, 1);
                else
                    probMap.put(MyAction.EAST, 1);
            
            }else{
                if (obstacles.contains(myPos.getNeighborPosition(GridOrientation.NORTH))){
                    probMap.put(MyAction.WEST, 0.6);
                    probMap.put(MyAction.SOUTH, 0.4);
                }else{
                    probMap.put(MyAction.NORTH, 0.8);
                    probMap.put(MyAction.WEST, 0.2);
                }
            }
            
        }else if (this.id % 2 == 0) {
            probMap.put(MyAction.EAST, 0.22);
            probMap.put(MyAction.WEST, 0.28);
            probMap.put(MyAction.NORTH, 0.25);
            probMap.put(MyAction.SOUTH, 0.25);
        } else {
            probMap.put(MyAction.EAST, 0.28);
            probMap.put(MyAction.WEST, 0.22);
            probMap.put(MyAction.NORTH, 0.25);
            probMap.put(MyAction.SOUTH, 0.25);
        }
               
        if (bestPrey == null) {
            // check messages
            for (AgentMessage message : wildlifePerceptions.getMessages()) {
                PreyLocationMessage pLocation = (PreyLocationMessage) message.getContent();

                if (bestPrey == null) {
                    bestPrey = pLocation.preyData;
                } else {
                    bestPrey = (myPos.getDistanceTo(pLocation.preyData) < myPos.getDistanceTo(bestPrey)) ? pLocation.preyData : bestPrey;
                }

            }

        }

        if (bestPrey != null) {
            probMap.clear();
            if (bestPrey.getX() < myPos.getX())
                probMap.put(MyAction.WEST, 0.50);
            if (bestPrey.getX() > myPos.getX())
                probMap.put(MyAction.EAST, 0.50);
            if (bestPrey.getY() > myPos.getY()) 
                probMap.put(MyAction.NORTH, 0.50);
            if (bestPrey.getY() < myPos.getY()) 
                probMap.put(MyAction.SOUTH, 0.50);

        }
        // remove actions which are unavailable because of obstacles
        for(GridPosition obs : wildlifePerceptions.getObstacles())
        {
                if(myPos.getDistanceTo(obs) > 1)
                        continue;
                GridRelativeOrientation relativeOrientation = myPos.getRelativeOrientation(obs);

                switch(relativeOrientation)
                {
                case FRONT:
                        // don't go up
                        probMap.removeAction(MyAction.NORTH);
                        break;
                case BACK:
                        // don't go down
                        probMap.removeAction(MyAction.SOUTH);
                        break;
                case LEFT:
                        // don't go left
                        probMap.removeAction(MyAction.WEST);
                        break;
                case RIGHT:
                        // don't go right
                        probMap.removeAction(MyAction.EAST);
                        break;
                default:
                        break;
                }
        }

        MyAction action=probMap.size()>0?(MyAction)probMap.choice():null;
        
        return createSocialAction(action, messages);
    }

}
