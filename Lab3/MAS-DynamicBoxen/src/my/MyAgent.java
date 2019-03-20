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
        
        LinkedList<BlocksWorldAction> currentplan;
        
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
                currentplan=new LinkedList<>();
		// TODO
	}
	
	@Override
	public Action response(Perceptions input)
	{
		@SuppressWarnings("unused")
                        
                
		BlocksWorldPerceptions perceptions = (BlocksWorldPerceptions) input;
		
                boolean something_changed=reviseBeliefs(perceptions);
		// TODO: revise beliefs; if necessary, make a plan; return an action.
		if (something_changed || currentplan.size()<=0){
                    currentplan=(LinkedList<BlocksWorldAction>)plan();
                }
                if (currentplan.size()>0)
                    return currentplan.pollFirst();
                else
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
            LinkedList<BlocksWorldAction> plan=new LinkedList<>();
            
            //check if i know all necessary blocks
            LinkedList<Block> known=new LinkedList<>();
            LinkedList<Block> search=new LinkedList<>();
            
            search.addAll(desired.allBlocks());
            for(Map.Entry<Character,Stack> pair:beliefs.entrySet()){
                known.addAll(pair.getValue().getBlocks());                
            }
            for(Block bl:known){
                search.remove(bl);
            }
            if (search.size()<=0){
                //know them all
                //plan.add(new BlocksWorldAction(Type.AGENT_COMPLETED));
                ArrayList
            }else{
                //keep searching
                plan.add(new BlocksWorldAction(Type.NEXT_STATION));
                
            }
            
            return plan;
	}
	
	@Override
	public String statusString()
	{
		// TODO: return information about the agent's current state and current plan.
                StringBuilder sb=new StringBuilder();
                
                sb.append(String.format("Current belief size = %d\n", beliefs.size()));
                sb.append(String.format("Current plan size = %d\n", beliefs.size()));
                int i=0;
                for(BlocksWorldAction ac: currentplan){
                    sb.append(String.format("       %d.  plan size = %d\n",i++, beliefs.size()));
                }
		return toString() + sb.toString();
	}
	
	@Override
	public String toString()
	{
		return "" + agentName;
	}
}
