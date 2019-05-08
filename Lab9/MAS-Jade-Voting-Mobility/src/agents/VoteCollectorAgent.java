package agents;

import jade.core.Agent;
import platform.Log;

/**
 * The Vote Collector Agent.
 */
public class VoteCollectorAgent extends Agent {
	/**
	 * The serial UID.
	 */
	private static final long serialVersionUID = -4316893632718883072L;

	@Override
	public void setup() {
		Log.log(this, "Hello");
	}

	@Override
	protected void takeDown() {
		// Printout a dismissal message
		Log.log(this, "terminating.");
	}
}
