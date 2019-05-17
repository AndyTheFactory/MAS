package agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import platform.Product;

public class Utils {
	
	
	/**
	 * Compute the payoff that a coalition is able to obtain, based on the "pooled" resources it has.
	 * @param coalitionInfo 
	 * @return the total value the coalition can obtain
	 */
	public static List<Product> computeCoalitionValue(Map<String, List<Product>> productsMap, CoalitionInfo coalitionInfo) {
		List<List<Product>> productChoices = new ArrayList<>(productsMap.values());
		
		List<Product> runningResult = new ArrayList<>();
		List<Product> bestResult = new ArrayList<>();
		
		computeValue(productChoices, coalitionInfo.getCoalitionResources(), 
				0, runningResult, bestResult);
		
		return bestResult;
	}
	
	private static void computeValue(List<List<Product>> productChoices, int coalitionResources, 
			int idx, List<Product> runningResult, List<Product> bestResult) {
		
		if (idx == productChoices.size()) {
			int val = 0;
			for (Product p : runningResult)
				val += p.getValue();
			
			int bestVal = 0;
			for (Product p : bestResult)
				bestVal += p.getValue();
			
			int cost = 0;
			for (Product p : runningResult)
				cost += p.getPrice();
			
			if (val > bestVal && cost <= coalitionResources) {
				bestResult.clear();
				bestResult.addAll(runningResult);
			}
		}
		else {
			for (Product prod : productChoices.get(idx)) {
				runningResult.add(prod);
				
				int cost = 0;
				for (Product p : runningResult)
					cost += p.getPrice();
				
				if (cost <= coalitionResources) 
					computeValue(productChoices, coalitionResources, idx + 1, runningResult, bestResult);
				
				runningResult.remove(prod);
			}
		}
	}
}
