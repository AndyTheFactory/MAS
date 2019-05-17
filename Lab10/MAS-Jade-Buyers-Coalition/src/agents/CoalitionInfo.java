package agents;

import jade.core.AID;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CoalitionInfo implements Serializable {
	
	public static final String SERVICE_TYPE = "coalition_setup";
	public static final String LEADER = "coalition_leader";
	public static final String MEMBERS = "coalition_members";
	public static final String RESOURCES = "coalition_resources";
	
	public static final String PRODUCT_TYPE_1 = "r1";
	public static final String PRODYCT_TYPE_2 = "r2";
	
    private static final long serialVersionUID = 1L;
	
    AID coalitionLeader;
	List<AID> coalitionMembers;
	
	Map<String, Map<AID, Double>> valueDistribution = new HashMap<>();
	int coalitionResources;
	
	public CoalitionInfo() {}
	
	public CoalitionInfo(AID coalitionLeader, List<AID> coalitionMembers, int coalitionResources) {
		this.coalitionLeader = coalitionLeader;
		this.coalitionMembers = coalitionMembers;
		this.coalitionResources = coalitionResources;
	}

	/**
	 * @return the coalitionLeader
	 */
	public AID getCoalitionLeader() {
		return coalitionLeader;
	}

	/**
	 * @param coalitionLeader the coalitionLeader to set
	 */
	public void setCoalitionLeader(AID coalitionLeader) {
		this.coalitionLeader = coalitionLeader;
	}

	/**
	 * @return the coalitionMembers
	 */
	public List<AID> getCoalitionMembers() {
		return coalitionMembers;
	}

	/**
	 * @param coalitionMembers the coalitionMembers to set
	 */
	public void setCoalitionMembers(List<AID> coalitionMembers) {
		this.coalitionMembers = coalitionMembers;
	}

	/**
	 * @return the coalitionResources
	 */
	public int getCoalitionResources() {
		return coalitionResources;
	}

	/**
	 * @param coalitionResources the coalitionResources to set
	 */
	public void setCoalitionResources(int coalitionResources) {
		this.coalitionResources = coalitionResources;
	}
	
    /**
	 * @return the valueDistribution
	 */
	public Map<String, Map<AID, Double>> getValueDistribution() {
		return valueDistribution;
	}

	/**
	 * @param valueDistribution the valueDistribution to set
	 */
	public void setValueDistribution(Map<String, Map<AID, Double>> valueDistribution) {
		this.valueDistribution = valueDistribution;
	}

	public void setValueShare(AID agent, String productType, double valueShare) {
		if (valueDistribution.containsKey(productType)) {
			valueDistribution.get(productType).put(agent, valueShare);
		}
		else {
			Map<AID, Double> valueShareMap = new HashMap<AID, Double>();
			valueShareMap.put(agent, valueShare);
			
			valueDistribution.put(productType, valueShareMap);
		}
	}
	
	
	@Override
    public String toString() {
	    return "CoalitionInfo [coalitionLeader=" + coalitionLeader.getLocalName()
	            + ", coalitionMembers=" + coalitionMembers.stream().map(aid -> aid.getLocalName()).collect(Collectors.toList())
	            + ", coalitionResources=" + coalitionResources + "]";
    }

	
    @Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime
	            * result
	            + ((coalitionMembers == null) ? 0 : coalitionMembers.hashCode());
	    return result;
    }

	
    @Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    CoalitionInfo other = (CoalitionInfo) obj;
	    if (coalitionMembers == null) {
		    if (other.coalitionMembers != null)
			    return false;
	    }
	    else if (!coalitionMembers.equals(other.coalitionMembers))
		    return false;
	    return true;
    }
	
	
}
