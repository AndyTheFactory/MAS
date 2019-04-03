package agents;

import behaviors.AllListenToMessages;
import behaviors.ParentOneShotBehaviour;
import jade.core.AID;
import jade.core.Agent;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import behaviors.RegistrationReceiveBehavior;
import behaviors.RegistrationSendBehavior;



/**
 * The Agent.
 */
public class MyAgent extends Agent {
	/**
	 * The serial UID.
	 */
	private static final long serialVersionUID = 2081456560111009192L;

	/**
	 * Known child agents.
	 */
	List<AID> childAgents = new LinkedList<>();
	
	AID parentAID;
	int agentValue;

	
	public void addChildAgent(AID childAID) {
		childAgents.add(childAID);
	}
	public void delChildAgent(AID childAID) {
		childAgents.remove(childAID);
	}
	
	public List<AID> getChildAgents() {
		return childAgents;
	}
	public AID getParentAID(){
            return parentAID;
        }
        public Integer getValue(){
            return agentValue;
        }
        public void setValue(Integer val){
            agentValue=val;
        }
	@Override
	protected void setup() {
		parentAID = (AID)getArguments()[0];
		agentValue = (int)getArguments()[1];
		
		System.out.println("Hello from agent: " + getAID().getName() + " with parentAID: " + parentAID);
		
		// add the RegistrationSendBehavior
		if (parentAID != null) {
			System.out.println("Registration sender behavior for this agent starts in 1 second");
			addBehaviour(new RegistrationSendBehavior(this, 1000, parentAID));
		}
		else {
			System.out.println("Registration sender behavior need not start for agent " + getAID().getName());
		}
		
		// add the RegistrationReceiveBehavior
		addBehaviour(new RegistrationReceiveBehavior(this));
                addBehaviour(new AllListenToMessages(this));
	}

	
	@Override
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Agent " + getAID().getName() + " knows this max value "+getValue().toString());
		System.out.print("\t");
		
		for (AID childAID : childAgents) {
			System.out.print(childAID.getName() + " ");
		}
		
		System.out.println();
		System.out.println();
	}
}
