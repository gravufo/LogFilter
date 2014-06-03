package persistence;

import collections.Pair;
import java.awt.Rectangle;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import logfilter.Log;
import logfilter.Server;

/**
 * This class manages the preferences (UI, servers, log templates, filters,
 * etc.) of the program and can provide persistence and cancel functionality.
 *
 * Serialization is used as the persistence method for now. Java properties file
 * is not suitable, because of the complex structures we have (Maps)
 *
 * @author cartin
 */
public class Preferences implements Serializable
{
    /**
     * Main constructor. Should only be called once, because this class is a
     * Singleton.
     */
    private Preferences()
    {
	serverMap = new HashMap<>();
	logMap = new HashMap<>();
	uIPreferences = new HashMap<>();
	
	serverUsername = "bwadmin";
	serverPassword = new char[]
	{
	    'b', 'w', 'a', 'd', 'm', 'i', 'n'
	};

	// Create new default log templates
	Log defaultLog = new Log("xtail");
	defaultLog.setFilePath("/var/broadworks/logs/appserver/");
	defaultLog.setNamePrefix("XSLog");
	logMap.put("xtail", defaultLog);

	defaultLog = new Log("xotail");
	defaultLog.setFilePath("/var/broadworks/logs/appserver/");
	defaultLog.setNamePrefix("XSOutput");
	logMap.put("xotail", defaultLog);

	defaultLog = new Log("ptail");
	defaultLog.setFilePath("/var/broadworks/logs/appserver/");
	defaultLog.setNamePrefix("PSLog");
	logMap.put("ptail", defaultLog);

	defaultLog = new Log("potail");
	defaultLog.setFilePath("/var/broadworks/logs/appserver/");
	defaultLog.setNamePrefix("PSOutput");
	logMap.put("potail", defaultLog);

    }

    /**
     * Copy constructor
     *
     * @param preferences The instance to copy
     */
    private Preferences(Preferences preferences)
    {
	uIPreferences = new HashMap<>(preferences.uIPreferences);
	serverMap = new HashMap<>(preferences.serverMap);
	logMap = new HashMap<>(preferences.logMap);
	
	serverUsername = preferences.serverUsername;
	serverPassword = preferences.serverPassword;
    }

    /**
     * Singleton access function
     *
     * @return The single instance of this class
     */
    public static Preferences getInstance()
    {
	if(instance == null)
	{
	    // Try to load pref file
	    load();
	    
	    if(instance == null) // If the loading failed
	    {
		// We build new default prefs
		Preferences.instance = new Preferences();
	    }
	    
	    savedInstance = new Preferences(instance);
	}
	
	return instance;
    }

    /**
     * This function will immediately serialize a copy of the instance of this
     * class into the preference file for persistence.
     */
    public void save()
    {
	try
	{
	    FileOutputStream fos = new FileOutputStream(fileName);

	    // TODO: Save every modification we did to the right instance
	    try (ObjectOutputStream oos = new ObjectOutputStream(fos))
	    {
		// TODO: Save every modification we did to the right instance
		savedInstance = new Preferences(instance);
		
		// Écrit toute l'instance (incluant ses attributs publics/privés)
		oos.writeObject(savedInstance);
	    }
	}
	catch (IOException e)
	{
	}
    }

    /**
     * This function will completely cancel any change that was made since the
     * last call to save()
     */
    public void cancel()
    {
	// Create a new instance from the saved copy
	// Last one will be destroyed by the garbage collector
	instance = new Preferences(savedInstance);
    }

    /**
     * This function will attempt to load a serialized instance of this class
     * from the preference file (if it exists)
     */
    private static void load()
    {
	try
	{
	    FileInputStream fis = new FileInputStream(fileName);

	    try (ObjectInputStream ois = new ObjectInputStream(fis))
	    {
		Preferences.instance = new Preferences((Preferences) ois.readObject());
	    }
	}
	catch (IOException | ClassNotFoundException e)
	{
	}
    }

    /**
     * This function looks for all the enabled servers in the servers list
     *
     * @return An ArrayList of enabled servers
     */
    public ArrayList<Server> getEnabledServers()
    {
	ArrayList<Server> enabledServers = new ArrayList<>();
	
	for(Server s : serverMap.values())
	{
	    if (s.isEnabled() && !s.getLogList().isEmpty())
		enabledServers.add(s);
	}
	
	return enabledServers;
    }
    
    public Server getServer(String name)
    {
	return instance.serverMap.get(name);
    }

    public boolean addServer(Server server)
    {
	return instance.serverMap.put(server.getName(), server) == null;
    }
    
    public boolean removeServer(String name)
    {
	return instance.serverMap.remove(name) != null;
    }
    
    public Log getLog(String name)
    {
	return instance.logMap.get(name);
    }

    public boolean addLog(Log log)
    {
	return instance.logMap.put(log.getName(), log) == null;
    }
    
    public boolean removeLog(String name)
    {
	return instance.logMap.remove(name) != null;
    }

    public PasswordAuthentication getServerAccount()
    {
	return new PasswordAuthentication(instance.serverUsername, instance.serverPassword);
    }

    public void setServerAccount(PasswordAuthentication serverAccount)
    {
	instance.serverUsername = serverAccount.getUserName();
	instance.serverPassword = serverAccount.getPassword();
    }
    
    public void setUIPreference(int id, Rectangle pref, int extendedState)
    {
	instance.uIPreferences.put(id, new Pair<>(pref, extendedState));
    }
    
    public Pair<Rectangle, Integer> getUIPreference(int id)
    {
	return instance.uIPreferences.get(id);
    }

    public Map<String, Server> getServerMap()
    {
	return instance.serverMap;
    }

    public Map<String, Log> getLogMap()
    {
	return instance.logMap;
    }

    /**
     * Active instance of this class
     */
    private static Preferences instance = null;

    /**
     * Latest saved instance (i.e. a copy of the version on disk)
     */
    private static Preferences savedInstance;

    /**
     * Name of the preference file
     */
    private static String fileName = "preferences.ser";
    
    // Preferences
    
    /**
     * List of servers
     */
    private Map<String, Server> serverMap;
    
    /**
     * List of template log files
     */
    private Map<String, Log> logMap;
    
    /**
     * Server username
     */
    private String serverUsername;
    
    /**
     * Server password. Using char[] for security reasons.
     */
    private char[] serverPassword;
    
    /**
     * Position and size of each window in the UI
     */
    private Map<Integer, Pair<Rectangle, Integer>> uIPreferences;
}
