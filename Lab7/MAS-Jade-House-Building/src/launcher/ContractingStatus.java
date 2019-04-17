package launcher;

import jade.core.AID;

/**
 * An instance encapsulates information about the negotiation regarding a particular construction item.
 */
public class ContractingStatus
{
	/**
	 * The phase of negotiations.
	 */
	public static enum ContractingPhase {
		/**
		 * No negotiations happened so far.
		 */
		INITIAL,
		/**
		 * A contract net protocol has been initiated.
		 */
		CONTRACTING,
		/**
		 * 1-to-1 negotiations are underway.
		 */
		NEGOTIATING,
		/**
		 * Negotiations completed.
		 */
		DONE
	}
	
	/**
	 * The item for which we are negotiating.
	 */
	String				constructionItem;
	/**
	 * For contractors - the cost; for ACME - the budget.
	 */
	Integer				costInformation;
	/**
	 * The phase of negotiations.
	 */
	ContractingPhase	contractingPhase;
	/**
	 * The partner for negotiations, after the CNet protocol is completed.
	 */
	AID					negotiationPartner;
	/**
	 * The number of the negotiation round (should be 1-based).
	 */
	int					negotiationRound;
	/**
	 * The price last proposed by the partner.
	 */
	int					partnerProposedPrice;
	/**
	 * The price last proposed by this entity.
	 */
	int					myProposedPrice;
	
	/**
	 * @param item
	 *            - the item.
	 * @param budgetOrCost
	 *            - for contractors - the cost; for ACME - the budget.
	 */
	public ContractingStatus(String item, Integer budgetOrCost)
	{
		constructionItem = item;
		costInformation = budgetOrCost;
		updatePhase(ContractingPhase.INITIAL);
	}
	
	/**
	 * @return the item.
	 */
	public String getConstructionItem()
	{
		return constructionItem;
	}
	
	/**
	 * @return for contractors - the cost; for ACME - the budget.
	 */
	public int getCostInformation()
	{
		return costInformation.intValue();
	}
	
	/**
	 * If the phase has changed, the negotiation round is automatically set to zero.
	 * 
	 * @param phase
	 *            - the current phase of negotiations.
	 * @return the instance itself.
	 */
	public ContractingStatus updatePhase(ContractingPhase phase)
	{
		if(contractingPhase != phase)
		{
			contractingPhase = phase;
			negotiationRound = 0;
		}
		if(contractingPhase == ContractingPhase.INITIAL)
		{
			negotiationPartner = null;
			partnerProposedPrice = 10000; // not cool
			myProposedPrice = -1;
		}
		return this;
	}
	
	/**
	 * @return the current partner, if any.
	 */
	public AID getPartner()
	{
		return negotiationPartner;
	}
	
	/**
	 * @param partner
	 *            - the partner.
	 * @return the instance itself.
	 */
	public ContractingStatus setPartner(AID partner)
	{
		negotiationPartner = partner;
		return this;
	}
	
	/**
	 * @return the current round.
	 */
	public int getNegotiationRound()
	{
		return negotiationRound;
	}
	
	/**
	 * Increments the negotiation round.
	 * 
	 * @return the instance itself.
	 */
	public ContractingStatus newNegotiationRound()
	{
		negotiationRound++;
		return this;
	}
	
	/**
	 * Updates the last prices.
	 * 
	 * @param mine
	 *            - my price.
	 * @param partner
	 *            - the price proposed by the partner.
	 * @return the instance itself.
	 */
	public ContractingStatus updateProposedPrices(int mine, int partner)
	{
		partnerProposedPrice = partner;
		myProposedPrice = mine;
		return this;
	}
	
	/**
	 * @return my last price.
	 */
	public int getMyLastPrice()
	{
		return myProposedPrice;
	}
	
	/**
	 * @return the partner's last price.
	 */
	public int getPartnerLastPrice()
	{
		return partnerProposedPrice;
	}
}
