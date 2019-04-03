package agents.behaviors;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;

public class AmbientServiceDiscoveryBehavior extends ParallelBehaviour {
	
    private static final long serialVersionUID = 7816281320324746190L;
    
        
    public AmbientServiceDiscoveryBehavior(Agent a, int endCondition) {
	    super(a, endCondition);
    }

	@Override
	public int onEnd() {
		// TODO add the RequestInitiator behavior  for asking the PreferenceAgent about preferred wake up mode
    	
    	return super.onEnd();
	}
	
}
