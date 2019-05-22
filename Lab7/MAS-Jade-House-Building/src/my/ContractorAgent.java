package my;

import behaviour.BidderBehaviour;
import behaviour.BidderNegociationBehaviour;
import java.util.HashMap;
import java.util.Map;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import launcher.ContractingStatus;
import launcher.Log;

/**
 * The agent representing a company which can contract building phases.
 */
public class ContractorAgent extends Agent {

    /**
     * The serial UID.
     */
    private static final long serialVersionUID = -5898373540918676720L;
    /**
     * Statuses for provided construction items.
     */
    Map<String, ContractingStatus> statuses = new HashMap<>();
    Map<String, Integer> costs = new HashMap<>();
    Map<String, Integer> monoton = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    protected void setup() {
        Map<String, Integer> providedItems = (Map<String, Integer>) getArguments()[0];
        Log.log(this, "Specialties/cost:", providedItems);

        // Register the ambient-agent service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        for (String item : providedItems.keySet()) {
            ServiceDescription sd = new ServiceDescription();
            sd.setType(item);
            sd.setName(item + "-Company " + getLocalName());
            dfd.addServices(sd);

            statuses.put(item, new ContractingStatus(item, providedItems.get(item)));
            costs.put(item, providedItems.get(item));
            monoton.put(item, 2*providedItems.get(item));
        }
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        Log.log(this, "Successfully registered:", dfd);
        
        addBehaviour(new BidderBehaviour(this));
        addBehaviour(new BidderNegociationBehaviour(this,null));
    }

    public int getCost(String serviceName) {
        if (!costs.containsKey(serviceName)) {
            throw new IllegalArgumentException("Unknown Service Name: " + serviceName);
        }
        return costs.get(serviceName);
    }
    
    public int getMonotonicCost(String serviceName) {
        if (!monoton.containsKey(serviceName)) {
            throw new IllegalArgumentException("Unknown Service Name: " + serviceName);
        }
        return monoton.get(serviceName);
    }
    public void doRoundMonotonic(String serviceName) {
        if (!monoton.containsKey(serviceName)) {
            throw new IllegalArgumentException("Unknown Service Name: " + serviceName);
        }
        
        int lastprice= monoton.get(serviceName);
        int increment=Integer.divideUnsigned(costs.get(serviceName),5+(int)Math.round(Math.random()*10));
        lastprice-=increment;
        
        monoton.put(serviceName,lastprice);
        
        
    }

    public ContractingStatus getContractingStatus(String serviceName) {
        if (!statuses.containsKey(serviceName)) {
            throw new IllegalArgumentException("Unknown Service Name for getStatus: " + serviceName);
        }
        return statuses.get(serviceName);
    }

    public void setContractingStatus(String serviceName, ContractingStatus status) {
        if (!statuses.containsKey(serviceName)) {
            throw new IllegalArgumentException("Unknown Service Name for setStatus: " + serviceName);
        }
        statuses.put(serviceName, status);
    }
}
