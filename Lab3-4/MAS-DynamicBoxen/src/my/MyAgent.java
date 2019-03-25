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
import blocksworld.Element;
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
        
        LinkedList<Block> bottomBlocksTodo;
        
        LinkedList<Stack> desiredStack;
        
        LinkedList<MyLongTermAction> longTermPlan;
        
    
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
                desiredStack=new LinkedList<>();
                bottomBlocksTodo=new LinkedList<>();
                longTermPlan=new LinkedList<>();
		// TODO
	}
	
	@Override
	public Action response(Perceptions input)
	{
		@SuppressWarnings("unused")
                        
                
		BlocksWorldPerceptions perceptions = (BlocksWorldPerceptions) input;
		if (perceptions.hasPreviousActionSucceeded())
                {
                    boolean something_changed=reviseBeliefs(perceptions);
                    // TODO: revise beliefs; if necessary, make a plan; return an action.
                    if (something_changed || currentplan.size()<=0){
                        currentplan=(LinkedList<BlocksWorldAction>)plan();
                    }
                }else
                    currentplan=(LinkedList<BlocksWorldAction>)plan();
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
	protected List<Block> getBottomBlocks(List<Stack> wrld){
            LinkedList<Block> res=new LinkedList<>();
            
            for(Stack st:wrld){
                res.add(st.getBottomBlock());
            }
            return res;
        }
        protected List<BlocksWorldAction> bottomRowPlan(){
            LinkedList<Block> bottomRow=(LinkedList<Block>) getBottomBlocks(desiredStack);
            LinkedList<Block> knownBottoms=(LinkedList<Block>) getBottomBlocks((List<Stack>)beliefs.values());

            LinkedList<BlocksWorldAction> plan=new LinkedList<>();
            for(Block bl:knownBottoms){
                bottomRow.remove(bl);
            }
            
            for(Block bl:bottomRow){
                longTermPlan.add(new MyLongTermAction(MyLongTermAction.MyType.BOTTOMIZE,bl));
                
            }
            
            
            return plan;
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
                ArrayList<Stack> stacks_todo=new ArrayList<>();
                stacks_todo.addAll(desired.getStacks());
                for(Stack st:beliefs.values()){
                    for(Stack st_d:stacks_todo)
                        if (st_d.equals(st)){
                            stacks_todo.remove(st_d);
                            break;
                        }
                }
                if (stacks_todo.size()<=0){
                    plan.add(new BlocksWorldAction(Type.AGENT_COMPLETED));
                    
                }else{
                    plan.addAll(subPlan(stacks_todo.get(0)));
                }
            }else{
                //keep searching
                plan.add(new BlocksWorldAction(Type.NEXT_STATION));
                
            }
            
            return plan;
	}
	protected List<BlocksWorldAction> subPlan(Stack stack_todo){
            List<BlocksWorldAction> subplan=new LinkedList<>();
            Block bottom=stack_todo.getBottomBlock();
            for(Map.Entry<Character,Stack> p: beliefs.entrySet()){
                if (p.getValue().contains(bottom)){
                    subplan.addAll(subPlan_Bottom(p.getKey(), p.getValue(), bottom));
                    
                    if (subplan.size()>0){
                        return subplan;
                    }
                    LinkedList<Block> my=new LinkedList<>();
                    my.addAll(stack_todo.getBlocks());
                    Block top=my.pollLast();
                    
                    Block bl=my.pollLast();
                    
                    while(bl!=null){
                        subplan.addAll(subPlan_BringBlockTo(p.getKey(), bl,top));
                        top=bl;
                        bl=my.pollLast();
                    }
                    
                }
            }
            
            return subplan;
        }
	protected List<BlocksWorldAction> subPlan_BringBlockTo(Character station,Block target,Block topmost){
            List<BlocksWorldAction> subplan=new LinkedList<>();
            for(Map.Entry<Character,Stack> p: beliefs.entrySet()){
                Stack stack=p.getValue();
                
                if (stack.contains(target)){
                    Block bl=stack.getTopBlock();
                    while (!bl.equals(target)){
                        subplan.add(new BlocksWorldAction(Type.GO_TO_STATION,new BlocksWorldEnvironment.Station(p.getKey())));
                        subplan.add(new BlocksWorldAction(Type.UNSTACK,bl,stack.getBelow(bl)));
                        subplan.add(new BlocksWorldAction(Type.PUTDOWN,bl));
                    }
                    subplan.add(new BlocksWorldAction(Type.GO_TO_STATION,new BlocksWorldEnvironment.Station(p.getKey())));
                    if (stack.getBelow(bl)==null)                       
                        subplan.add(new BlocksWorldAction(Type.PICKUP,bl));
                    else
                        subplan.add(new BlocksWorldAction(Type.UNSTACK,bl,stack.getBelow(bl)));
                    subplan.add(new BlocksWorldAction(Type.GO_TO_STATION,new BlocksWorldEnvironment.Station(station)));
                    subplan.add(new BlocksWorldAction(Type.STACK,bl,topmost));
                }
            }
            
            return subplan;
        }
	protected List<BlocksWorldAction> subPlan_Bottom(Character station,Stack stack,Block target){
            List<BlocksWorldAction> subplan=new LinkedList<>();

            LinkedList<Block> my=new LinkedList<>();
            my.addAll(stack.getBlocks());
            if (stack.getBottomBlock().equals(target)){
                if (stack.isSingleBlock()){
                    return subplan;
                }
                my.pollLast(); //Last sau first?
                for(Block bl:my){
                    subplan.add(new BlocksWorldAction(Type.GO_TO_STATION,new BlocksWorldEnvironment.Station(station)));
                    subplan.add(new BlocksWorldAction(Type.UNSTACK,bl,stack.getBelow(bl)));
                    subplan.add(new BlocksWorldAction(Type.PUTDOWN,bl));
                }
            }else{
                for(Block bl:my){
                    
                    subplan.add(new BlocksWorldAction(Type.GO_TO_STATION,new BlocksWorldEnvironment.Station(station)));
                    subplan.add(new BlocksWorldAction(Type.UNSTACK,bl,stack.getBelow(bl)));
                    subplan.add(new BlocksWorldAction(Type.PUTDOWN,bl));
                    
                    if (bl.equals(target))
                        break;
                }
                
            }
            return subplan;
        }
	@Override
	public String statusString()
	{
		// TODO: return information about the agent's current state and current plan.
                StringBuilder sb=new StringBuilder();
                
                sb.append(String.format("Current belief size = %d\n", beliefs.size()));
                sb.append(String.format("Current plan size = %d\n", currentplan.size()));
                int i=1;
                for(BlocksWorldAction ac: currentplan){
                    sb.append(String.format("       %d.  Do = %s\n",i++, ac.toString()));
                }
		return toString() + sb.toString();
	}
	
	@Override
	public String toString()
	{
		return "" + agentName;
	}
}
