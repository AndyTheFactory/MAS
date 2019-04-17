package launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONObject;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * Launches a JADE container and offers some other helpful methods.
 */
public class Launcher
{
	public static final String COMPANY_COSTS_V1 = "data/Companies.json";
	public static final String COMPANY_COSTS_V2 = "data/Companies2.json";
	
	/**
	 * The main container.
	 */
	AgentContainer			container;
	
	/**
	 * The agents to be started by this container.
	 */
	List<AgentController>	agents	= new LinkedList<>();
	
	/**
	 * The ID of the platform.
	 */
	static String			platformID;
	
	/**
	 * @param id
	 *            - the id of the platform.
	 */
	protected static void setPlatformID(String id)
	{
		platformID = id;
	}
	
	/**
	 * Configures and launches the main container.
	 * 
	 * @param isMain
	 *            - <code>true</code> if this is the main container.
	 * @param name
	 *            - the name of the container.
	 * @param mainIP
	 *            - if not main, the IP of the main container.
	 * @param mainPort
	 *            - if not main, the port of the main container.
	 * @param localIP
	 *            - the IP of the local container.
	 * @param localPort
	 *            - the port of the local container.
	 */
	protected void startContainer(boolean isMain, String name, String mainIP, int mainPort, String localIP,
			int localPort)
	{
		Properties props = new ExtendedProperties();
		props.setProperty(Profile.CONTAINER_NAME, name);
		if(isMain)
		{
			props.setProperty(Profile.GUI, "true");
			props.setProperty(Profile.MAIN, "true");
		}
		else
		{
			props.setProperty(Profile.MAIN_HOST, mainIP);
			props.setProperty(Profile.MAIN_PORT, new Integer(mainPort).toString());
		}
		props.setProperty(Profile.LOCAL_HOST, localIP);
		props.setProperty(Profile.LOCAL_PORT, new Integer(localPort).toString());
		if(platformID != null)
			props.setProperty(Profile.PLATFORM_ID, platformID);
		
		ProfileImpl mainProfile = new ProfileImpl(props);
		if(isMain)
			container = Runtime.instance().createMainContainer(mainProfile);
		else
			container = Runtime.instance().createAgentContainer(mainProfile);
	}
	
	/**
	 * @param name
	 *            - the name of the container.
	 * @param mainIP
	 *            - the IP of the container.
	 * @param mainPort
	 *            - the port of the container.
	 */
	protected void startMainContainer(String name, String mainIP, int mainPort)
	{
		startContainer(true, name, null, 0, mainIP, mainPort);
	}
	
	/**
	 * @param name
	 *            - the name of the container.
	 * @param mainIP
	 *            - if not main, the IP of the main container.
	 * @param mainPort
	 *            - if not main, the port of the main container.
	 * @param localIP
	 *            - the IP of the local container.
	 * @param localPort
	 *            - the port of the local container.
	 */
	protected void startSlaveContainer(String name, String mainIP, int mainPort, String localIP, int localPort)
	{
		startContainer(false, name, mainIP, mainPort, localIP, localPort);
	}
	
	/**
	 * Starts the agents assigned to the main container.
	 * 
	 * @param name
	 *            - the name of the agent.
	 * @param cls
	 *            - the class of the agent implementation.
	 * @param arguments
	 *            - arguments to send to the agents.
	 */
	protected void addAgent(String name, Class<?> cls, Object... arguments)
	{
		try
		{
			agents.add(container.createNewAgent(name, cls.getName(), arguments));
		} catch(StaleProxyException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Start all agents previously added with {@link #addAgent}.
	 */
	protected void startAllAgents()
	{
		for(AgentController a : agents)
			try
			{
				a.start();
			} catch(StaleProxyException e)
			{
				e.printStackTrace();
			}
	}
	
	/**
	 * Reads a JSON file.
	 * 
	 * @param filename
	 *            - the file.
	 * @return the {@link JSONObject}.
	 */
	public static JSONObject readJson(String filename)
	{
		try (Scanner scanner = new Scanner(new File(filename)))
		{
			String content = scanner.useDelimiter("\\Z").next();
			return new JSONObject(content);
		} catch(FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns a time at a certain amount of time (delta) in the future.
	 * 
	 * @param delta
	 *            - the offset into the future.
	 * @return the future time.
	 */
	public static Date deltaFromNow(long delta)
	{
		return new Date(new Date().getTime() + delta);
	}
}
