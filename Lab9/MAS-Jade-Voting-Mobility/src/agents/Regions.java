/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author andrei
 */
public class Regions {
    static final String CENTRAL_ELECTION = "CentralElection";
    static final String VOTING_REGION_1 = "VotingRegion1";
    static final String VOTING_REGION_2 = "VotingRegion2";
    static final String VOTING_REGION_3 = "VotingRegion3";
    static final String VOTING_REGION_4 = "VotingRegion4";

    static final String[] REGION_CONTAINERS = {
            VOTING_REGION_1, VOTING_REGION_2, VOTING_REGION_3, VOTING_REGION_4
    };
    private static final Map<String, String> REGION_TO_CONTAINERS;
    static {
        Map<String, String> aMap = new HashMap<>();
        aMap.put("region1", VOTING_REGION_1);
        aMap.put("region2", VOTING_REGION_2);
        aMap.put("region3", VOTING_REGION_3);
        aMap.put("region4", VOTING_REGION_4);
        REGION_TO_CONTAINERS = Collections.unmodifiableMap(aMap);
    }    
    public static String getContainerForRegion(String ContainerName){
        return REGION_TO_CONTAINERS.get(ContainerName);
    }
    public static String getCentralElection(){
        return CENTRAL_ELECTION;
    }
    public static String getRegionForContainer(String RegionName){
        for(Entry<String,String> entry:REGION_TO_CONTAINERS.entrySet()){
            if (entry.getValue().equals(RegionName))
                return entry.getKey();
        }
        return "";
    }
}
