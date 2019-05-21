/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package platform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author andrei
 */
public class test {

    static Map<String, VoteResult> votes = new HashMap<>();

    final static int SEATS_PER_REGION = 3;
    final static int CANDIDATES_PER_REGION = 5;

    public static void main(String[] args) {
        VoteResult voteResult;
    
        VoteReader voteReader = new VoteReader();
        
        voteResult = voteReader.getVoteResult("region1");        
        votes.put("region1", voteResult);        
        
        voteResult = voteReader.getVoteResult("region2");       
        votes.put("region2", voteResult);        
        
        voteResult = voteReader.getVoteResult("region3");       
        votes.put("region3", voteResult);        
        
        voteResult = voteReader.getVoteResult("region4");       
        votes.put("region4", voteResult);        
        
        System.out.println("    Region1 Winners: "+test.getWinners("region1"));
        System.out.println("    Region2 Winners: "+test.getWinners("region2"));
        System.out.println("    Region3 Winners: "+test.getWinners("region3"));
        System.out.println("    Region4 Winners: "+test.getWinners("region4"));
        
        
    }

    static int getDroopQuota(String region) {
        VoteResult r = votes.get(region);
        if (r == null) {
            return Math.floorDiv(250, SEATS_PER_REGION + 1) + 1;
        } else {
            int total = 0;
            for (Ballot b : r.getBallots()) {
                total += b.getCount();
            }
            return Math.floorDiv(total, SEATS_PER_REGION + 1) + 1;
        }

    }

    static String getWinners(String region) {
        StringBuilder res = new StringBuilder();
        int droop = getDroopQuota(region);
        VoteResult r = votes.get(region);

        Map<String, Integer> candidates = new HashMap<>();

        List<String> clist = r.getBallots().get(0).getCandidates();

        for (String c : clist) {
            candidates.put(c, 0);
        }
        //Round 1
        for (Ballot b : r.getBallots()) {
            String c = b.getCandidates().get(0);
            int total = b.getCount()+candidates.get(c);
            candidates.put(c, total);
        }
        Map<String, Integer> cand_orig = new HashMap<>(candidates);
        int nr=0;
        while (nr < SEATS_PER_REGION) {
            if (SEATS_PER_REGION-nr>=candidates.size()){
                //there are seats for everybody in candidates
                for (Map.Entry<String, Integer> e : candidates.entrySet()) {
                    res.append(e.getKey()+",");
                }
                break;
            }
            Map.Entry<String, Integer> ebest = null, eworst = null;
            for (Map.Entry<String, Integer> e : candidates.entrySet()) {
                if (ebest == null || ebest.getValue() < e.getValue()) {
                    ebest = e;
                }
                if (eworst == null || eworst.getValue() > e.getValue()) {
                    eworst = e;
                }
            }
            int  transfer = 0, tvotes=0;
            String from_cand = "";
            if (ebest.getValue() >= droop) {
                //elected
                tvotes=ebest.getValue();
                res.append(ebest.getKey() + ",");
                nr++;
                transfer = ebest.getValue() - droop ;
                from_cand = ebest.getKey();
                candidates.remove(ebest.getKey());

            } else {
                tvotes=eworst.getValue();
                candidates.remove(eworst.getKey());
                transfer = eworst.getValue();
                from_cand = eworst.getKey();
            }
            
            for (Ballot b : r.getBallots()) {
                if (b.getCandidates().get(0).equals(from_cand) && b.getCandidates().size()>1 ){
                    String to_cand="";
                    for(int i=1;i<b.getCandidates().size();i++)
                        if (candidates.get(b.getCandidates().get(i))!=null)
                            to_cand=b.getCandidates().get(i);
                    if (candidates.get(to_cand)!=null){
                        int add_votes=Integer.divideUnsigned(b.getCount(), tvotes)*transfer;
                        int newvotes=candidates.get(to_cand)+add_votes;
                        candidates.put(to_cand,newvotes);
                    }
                    
                }
            }

        }
        return res.toString();
    }
}
