package my;

import java.util.List;

import base.Action;
import base.Agent;
import base.Perceptions;
import blocksworld.Block;
import blocksworld.BlocksWorld;
import blocksworld.BlocksWorldAction;
import blocksworld.BlocksWorldAction.Type;
import blocksworld.BlocksWorldEnvironment;
import blocksworld.BlocksWorldPerceptions;
import blocksworld.Stack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Agent to implement.
 */
public class MyAgent implements Agent
{
	/**
	 * Name of the agent.
	 */
	String agentName;
        
        Map<Character,Stack> beliefs;
        
        BlocksWorld desired;
        
        int changedBelief;
	/**
	 * Constructor for the agent.
	 * 
	 * @param desiredState
	 *            - the desired state of the world.
	 * @param name
	 *            - the name of the agent.
	 */
	public MyAgent(BlocksWorld desiredState, String name)
	{
		agentName = name;
                beliefs=new HashMap<>();
                desired=desiredState;
                changedBelief=0;
		// TODO
	}
	
	@Override
	public Action response(Perceptions input)
	{
		@SuppressWarnings("unused")
                        
                
		BlocksWorldPerceptions perceptions = (BlocksWorldPerceptions) input;
		
                reviseBeliefs(perceptions);
		// TODO: revise beliefs; if necessary, make a plan; return an action.
		
		return new BlocksWorldAction(Type.AGENT_COMPLETED);
	}
	
	/**
	 * @param perceivedWorldState
	 *            - the blocks that the agent can see.
         * @return beliefs changed
	 */
	protected boolean reviseBeliefs(BlocksWorldPerceptions perceivedWorldState)
	{
		// TODO: check if what the agent knows corresponds to what the agent sees.
            char curr = perceivedWorldState.getCurrentStation().getLabel();
            if (beliefs.containsKey(curr)){
                //i know the stack
                Stack known=beliefs.get(curr);
                Stack reality=perceivedWorldState.getVisibleStack();
                //known.getBlocks();
                if (known.equals(reality)){
                    return false;
                }
                
                beliefs.put(curr, reality);
                return true;
            }else{
                beliefs.put(curr, perceivedWorldState.getVisibleStack());
            }
            return false;
	}
	
	/**
	 * @return a new plan, as a sequence of {@link BlocksWorldAction} instances, based on the agent's knowledge.
	 */
	@SuppressWarnings("static-method")
	protected List<BlocksWorldAction> plan()
	{
		// TODO
		return null;
	}
	
	@Override
	public String statusString()
	{
		// TODO: return information about the agent's current state and current plan.
		return toString() + ": PLAN MISSING.";
	}
	
	@Override
	public String toString()
	{
		return "" + agentName;
	}
}
