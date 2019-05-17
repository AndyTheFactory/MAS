package platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SetupReader {
	
	public static Map<String, List<Product>> readProductConfig(String filename) {
		try {
            final InputStream in = new FileInputStream(new File(filename));
            ObjectMapper mapper =  new ObjectMapper();
            
            Map<String, List<Product>> productMap = new java.util.HashMap<>();
            
            final JsonNode productsNode = mapper.readTree(in);
            
            for (JsonNode prodNode : productsNode) {
                Product prod = mapper.treeToValue(prodNode.get("product"), Product.class);
                
                if (productMap.containsKey(prod.type)) {
                	productMap.get(prod.type).add(prod);
                }
                else {
                	List<Product> prodList = new ArrayList<>();
                	prodList.add(prod);
                	productMap.put(prod.type, prodList);
                }
            }
            
            return productMap;

        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return null;
	}
	
	
	public static List<AgentConfig> readAgentConfigurations(String filename) {
		try {
            final InputStream in = new FileInputStream(new File(filename));
            ObjectMapper mapper =  new ObjectMapper();
            
            List<AgentConfig> agentConfigurations = new ArrayList<>();
            
            final JsonNode agentsNode = mapper.readTree(in);
            
            for (JsonNode agNode : agentsNode) {
                AgentConfig agConfig = mapper.treeToValue(agNode.get("agent"), AgentConfig.class);
                
                agentConfigurations.add(agConfig);
            }
            
            return agentConfigurations;

        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return null;
	}
	
}

