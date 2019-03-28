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
import java.util.Set;
import org.omg.CORBA.BAD_CONTEXT;

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
        
        LinkedList<LinkedList<Block>> lockedStacks;
        
        BlocksWorld desired;
        
        LinkedList<BlocksWorldAction> currentplan;
        
        LinkedList<Block> bottomBlocksTodo;
        
        
        LinkedList<MyLongTermAction> longTermPlan;
        
        BlocksWorldAction previousAction=null;
        Block holding=null;
        
    
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
                bottomBlocksTodo=new LinkedList<>();
                longTermPlan=new LinkedList<>();
                lockedStacks=new LinkedList<LinkedList<Block>>() ;
                previousAction=new BlocksWorldAction(Type.NONE);
		// TODO
	}
	private void addToLocked(BlocksWorldPerceptions perceptions,Block block){
            
            for(LinkedList<Block> st:lockedStacks){
                if (perceptions.getVisibleStack().getBottomBlock().equals(st.getFirst())){
                    st.addLast(block);
                    return;
                }
            }
            LinkedList<Block> n=new LinkedList<Block>();
            n.addFirst(block);
            lockedStacks.add(n);
        }
	@Override
	public Action response(Perceptions input)
	{
		@SuppressWarnings("unused")
                BlocksWorldAction action=new BlocksWorldAction(Type.AGENT_COMPLETED);        
                
		BlocksWorldPerceptions perceptions = (BlocksWorldPerceptions) input;

		if (perceptions.hasPreviousActionSucceeded())
                {
                    
                    if (previousAction.getType()==Type.LOCK){  //Keep track of locks  
                        addToLocked(perceptions, previousAction.getArgument());
                    }
                    if (previousAction.getType()==Type.PICKUP){  //Keep track of locks                      
                    
                        holding=previousAction.getArgument();
                    }
                    if (previousAction.getType()==Type.UNSTACK){  //Keep track of locks                      
                    
                        holding=previousAction.getFirstArgument();
                    }
                    if (previousAction.getType()==Type.PUTDOWN){  //Keep track of locks                      
                    
                        holding=null;
                    }
                    if (previousAction.getType()==Type.STACK){  //Keep track of locks                      
                    
                        holding=null;
                    }
                    boolean something_changed=reviseBeliefs(perceptions);
                    // TODO: revise beliefs; if necessary, make a plan; return an action.
                    //something_changed || 
                    if (currentplan.size()<=0){
                        currentplan=(LinkedList<BlocksWorldAction>)plan(perceptions);
                    }
                }else{
                    if (previousAction.getType()==Type.GO_TO_STATION){
                        //Station is gone
                        Character station=((BlocksWorldEnvironment.Station)previousAction.getArgument()).getLabel();
                        beliefs.remove(station);
                        if (holding!=null){
                            action=new BlocksWorldAction(Type.PUTDOWN,previousAction.getArgument());
                            previousAction=action;
                            return action;
                        }    
                        
                    }
                    reviseBeliefs(perceptions);
                    currentplan.clear();
                    if (previousAction.getType()==Type.STACK){
                        action=new BlocksWorldAction(Type.PUTDOWN,previousAction.getFirstArgument());
                    }else{
                        
                        currentplan=(LinkedList<BlocksWorldAction>)plan(perceptions);
                    }
                }
                if (currentplan.size()>0)
                    action=currentplan.pollFirst();
                else{
                    while(longTermPlan.size()>0){
                        currentplan=(LinkedList<BlocksWorldAction>)plan(perceptions);
                        if (currentplan.size()>0){
                            action=currentplan.pollFirst();
                            break;
                        }
                    }
                }
                previousAction=action;
                return action;
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
        protected List<MyLongTermAction> bottomRowPlan(){
            LinkedList<Block> bottomRow=(LinkedList<Block>) getBottomBlocks(desired.getStacks());
            LinkedList<Block> knownBottoms=(LinkedList<Block>) getBottomBlocks(new ArrayList<>(beliefs.values()));

            LinkedList<MyLongTermAction> plan=new LinkedList<>();
            for(Block bl:knownBottoms){
                bottomRow.remove(bl);
            }
            
            for(Block bl:bottomRow){
                plan.add(new MyLongTermAction(MyLongTermAction.MyType.BOTTOMIZE,bl));
            }
            return plan;
        }
        protected List<MyLongTermAction> stackPlan(BlocksWorldPerceptions perceptions,Stack stack_todo){
            LinkedList<MyLongTermAction> plan=new LinkedList<>();
            for(LinkedList<Block> st:lockedStacks){
                Block blBottom=st.getFirst();
                if (blBottom.equals(stack_todo.getBottomBlock())){
                    Block bl1=st.getLast();
                    Block bl2=stack_todo.getAbove(bl1);
                    plan.add(new MyLongTermAction(MyLongTermAction.MyType.STACK,bl2,bl1));
                    break;
                }
            }
            return plan;
        }
        protected Character getStationForBlock(Block bl){
            for(Map.Entry<Character,Stack> pair:beliefs.entrySet()){
                if (pair.getValue().contains(bl))
                    return pair.getKey();
            }
            return '\0';
        }
	/**
	 * @return a new plan, as a sequence of {@link BlocksWorldAction} instances, based on the agent's knowledge.
	 */
	@SuppressWarnings("static-method")
	protected List<BlocksWorldAction> plan(BlocksWorldPerceptions perceptions)
	{
            LinkedList<BlocksWorldAction> plan=new LinkedList<>();
            
            for(Stack st:desired.getStacks()){
                if (st.equals(perceptions.getVisibleStack())){ //The stack is ok!
                    Block bl=perceptions.getVisibleStack().getBottomBlock();
                    while (bl!=null){
                        if (!perceptions.getVisibleStack().isLocked(bl))
                            plan.add(new BlocksWorldAction(Type.LOCK,bl));
                        bl=perceptions.getVisibleStack().getAbove(bl);
                    }
                    if (plan.size()>0)
                        return plan; //Quick LOCK it!
                }
            }
            
            
            while (longTermPlan.size()>0){
                //try next long term plan
                MyLongTermAction action=longTermPlan.pollFirst();
                Block bl=action.getFirstArgument();
                Character station;
                
                switch(action.getType()){
                    case BOTTOMIZE:
                        station=getStationForBlock(bl);
                        if (station!='\0'){
                            //I know where the block is
                            if (station==perceptions.getCurrentStation().getLabel()){
                                //  it's under me!
                                if (perceptions.getVisibleStack().isSingleBlock() || perceptions.getVisibleStack().isOnTable(bl)){
                                    if (!perceptions.getVisibleStack().isLocked(bl))
                                        plan.add(new BlocksWorldAction(Type.LOCK,bl));
                                }else{
                                    plan.addAll(subPlan_Bottom(perceptions, bl));
                                    longTermPlan.addFirst(new MyLongTermAction(MyLongTermAction.MyType.BOTTOMIZE,bl)); 
                                }
                            }else{
                                plan.add(new BlocksWorldAction(Type.GO_TO_STATION,
                                    new BlocksWorldEnvironment.Station(station)
                                ));
                                longTermPlan.addFirst(new MyLongTermAction(MyLongTermAction.MyType.BOTTOMIZE,bl)); 
                            }
                            
                        }else{
                            longTermPlan.addFirst(new MyLongTermAction(MyLongTermAction.MyType.BOTTOMIZE,bl)); 
                            longTermPlan.addFirst(new MyLongTermAction(MyLongTermAction.MyType.FIND,bl)); //search and bottomize
                            plan.add(new BlocksWorldAction(Type.NEXT_STATION));
                        }
                        break;
                    case FIND:
                        station=getStationForBlock(bl);
                        if (station!='\0'){
                            if (station!=perceptions.getCurrentStation().getLabel()){
                                plan.add(new BlocksWorldAction(Type.GO_TO_STATION,
                                        new BlocksWorldEnvironment.Station(station)
                                    ));
                                longTermPlan.addFirst(new MyLongTermAction(MyLongTermAction.MyType.FIND,bl)); //search and bottomize
                            }
                        }else{
                            longTermPlan.addFirst(new MyLongTermAction(MyLongTermAction.MyType.FIND,bl)); //search and bottomize
                            plan.add(new BlocksWorldAction(Type.NEXT_STATION));
                            
                        }
                        
                        break;
                    case SCAN:
                        if (!perceptions.getVisibleStack().contains(bl)){
                            longTermPlan.addFirst(new MyLongTermAction(MyLongTermAction.MyType.SCAN,bl)); //scan until reaching the block again
                            plan.add(new BlocksWorldAction(Type.NEXT_STATION));
                        }
                        
                        break;
                    case STACK:
                            Block bl2=action.getSecondArgument();//Put on this block
                            Stack foundinstack=null;
                            //Clear target Station 
                            for(Stack st:beliefs.values()){
                                if (st.contains(bl2)){
                                    foundinstack=st;    
                                    if(!st.isClear(bl2)){
                                        Character targetstation=getStationForBlock(bl2);
                                        if(!perceptions.getVisibleStack().contains(bl2))
                                            plan.add(new BlocksWorldAction(Type.GO_TO_STATION,new BlocksWorldEnvironment.Station(targetstation)));
                                        Block bb=st.getTopBlock();
                                        while(!bb.equals(bl2)){
                                            plan.add(new BlocksWorldAction(Type.UNSTACK,bb,st.getBelow(bb)));
                                            plan.add(new BlocksWorldAction(Type.PUTDOWN,bb));
                                            plan.add(new BlocksWorldAction(Type.GO_TO_STATION,new BlocksWorldEnvironment.Station(targetstation)));
                                            bb=st.getBelow(bb);
                                        }
                                        longTermPlan.addFirst(new MyLongTermAction(MyLongTermAction.MyType.STACK,bl,bl2)); //keep the long termplan
                                        return plan; // that
                                    }
                                    break;
                                }
                            }
                            // bl2 = destination
                            // bl= block to move
                            if (foundinstack!=null){
                                //i know where the destination block is and it is clear
                                if (perceptions.getVisibleStack().contains(bl)){
                                    //source block is under me
                                    if (perceptions.getVisibleStack().isClear(bl)){                                        
                                        Character targetstation=getStationForBlock(bl2);
                                        if (perceptions.getVisibleStack().isOnTable(bl))
                                            plan.add(new BlocksWorldAction(Type.PICKUP,bl));
                                        else
                                            plan.add(new BlocksWorldAction(Type.UNSTACK,bl,perceptions.getVisibleStack().getBelow(bl)));
                                        plan.add(new BlocksWorldAction(Type.GO_TO_STATION,new BlocksWorldEnvironment.Station(targetstation)));
                                        plan.add(new BlocksWorldAction(Type.STACK,bl,bl2));
                                        plan.add(new BlocksWorldAction(Type.LOCK,bl));
                                    }else{
                                        Block bl3=perceptions.getVisibleStack().getTopBlock();
                                        plan.add(new BlocksWorldAction(Type.UNSTACK,bl3,perceptions.getVisibleStack().getBelow(bl3)));
                                        plan.add(new BlocksWorldAction(Type.PUTDOWN,bl3));
                                        plan.add(new BlocksWorldAction(Type.GO_TO_STATION,perceptions.getCurrentStation()));
                                        longTermPlan.addFirst(new MyLongTermAction(MyLongTermAction.MyType.STACK,bl,bl2)); //keep the long termplan
                                    }
                                    
                                }else{
                                    Character targetstation=getStationForBlock(bl);
                                    if (targetstation=='\0'){
                                        longTermPlan.addFirst(new MyLongTermAction(MyLongTermAction.MyType.STACK,bl,bl2)); //keep the long termplan
                                        longTermPlan.addFirst(new MyLongTermAction(MyLongTermAction.MyType.FIND,bl)); //search and bottomize
                                        plan.add(new BlocksWorldAction(Type.NEXT_STATION));
                                        
                                    }else{
                                        plan.add(new BlocksWorldAction(Type.GO_TO_STATION,new BlocksWorldEnvironment.Station(targetstation)));
                                        longTermPlan.addFirst(new MyLongTermAction(MyLongTermAction.MyType.STACK,bl,bl2)); //keep the long termplan
                                    }
                                }
                            }else{
                                //find target station
                                longTermPlan.addFirst(new MyLongTermAction(MyLongTermAction.MyType.STACK,bl,bl2)); //keep the long termplan
                                longTermPlan.addFirst(new MyLongTermAction(MyLongTermAction.MyType.FIND,bl)); //search and bottomize
                                plan.add(new BlocksWorldAction(Type.NEXT_STATION));
                            }
                            
                        break;
                        
                }
                
                if (plan.size()>0)
                    return plan;
                
            }
            
            longTermPlan.addAll(bottomRowPlan());
            
            if (longTermPlan.size()<=0){
                ArrayList<Stack> stacks_todo=new ArrayList<>();
                stacks_todo.addAll(desired.getStacks());
                for(LinkedList<Block> st:lockedStacks){
                    for(Stack st_d:stacks_todo)
                        if (st_d.getTopBlock().equals(st.getLast())){
                            stacks_todo.remove(st_d);
                            break;
                        }
                }
                if (stacks_todo.size()<=0){
                    plan.add(new BlocksWorldAction(Type.AGENT_COMPLETED));
                    return plan;
                }
                
                longTermPlan.addAll(stackPlan(perceptions,stacks_todo.get(0)));
                
                if (longTermPlan.size()<=0) 
                    longTermPlan.addFirst(new MyLongTermAction(MyLongTermAction.MyType.SCAN,
                              perceptions.getVisibleStack().getBottomBlock()
                        )); //scan if we have no other idea
                
                
            }
            return plan;
	}
        /*
            Plan LAB 3
        *
        */
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
	protected List<BlocksWorldAction> subPlan_Bottom(BlocksWorldPerceptions perception,Block target){
            List<BlocksWorldAction> subplan=new LinkedList<>();

            LinkedList<Block> my=new LinkedList<>();
            
            Stack stack=perception.getVisibleStack();
            BlocksWorldEnvironment.Station station=perception.getCurrentStation();
                    
            my.addAll(stack.getBlocks());
            if (stack.getBottomBlock().equals(target)){
                if (stack.isSingleBlock()){
                    return subplan;
                }
                my.pollLast(); //Last sau first?
                for(Block bl:my){
                    subplan.add(new BlocksWorldAction(Type.UNSTACK,bl,stack.getBelow(bl)));
                    subplan.add(new BlocksWorldAction(Type.PUTDOWN,bl));
                    subplan.add(new BlocksWorldAction(Type.GO_TO_STATION,station));
                }
            }else{
                for(Block bl:my){
                    
                    subplan.add(new BlocksWorldAction(Type.UNSTACK,bl,stack.getBelow(bl)));
                    subplan.add(new BlocksWorldAction(Type.PUTDOWN,bl));
                    if (bl.equals(target))
                        subplan.add(new BlocksWorldAction(Type.LOCK,bl));
                    subplan.add(new BlocksWorldAction(Type.GO_TO_STATION,station));
                    
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
                sb.append(String.format("Current Long term plan size = %d\n", longTermPlan.size()));
                i=1;
                for(MyLongTermAction ac: longTermPlan){
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
