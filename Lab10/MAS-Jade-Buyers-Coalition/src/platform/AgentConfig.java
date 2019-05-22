package platform;

import java.io.Serializable;

public class AgentConfig implements Serializable, Comparable<AgentConfig> {
	
    private static final long serialVersionUID = 66850053936786037L;
	
    String name;
	int resources;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the resources
	 */
	public int getResources() {
		return resources;
	}
	
	/**
	 * @param resources the resources to set
	 */
	public void setResources(int resources) {
		this.resources = resources;
	}

	@Override
    public int compareTo(AgentConfig other) {
	    if (resources < other.resources)
	    	return 1;
	    else if (resources > other.resources)
	    	return -1;
	    else
	    	return name.compareTo(other.name);
    }
	
}
