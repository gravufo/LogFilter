/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package persistence;

import java.awt.Rectangle;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;
import logfilter.Log;
import logfilter.Server;

/**
 *
 * @author cartin
 */
public class Preferences implements Serializable
{
    private Preferences()
    {
	serverMap = new HashMap<>();
	logMap = new HashMap<>();
	uIPreferences = new HashMap<>();
	
	serverUsername = "bwadmin";
	serverPassword = new char[]{'b', 'w', 'a', 'd', 'm', 'i', 'n'};
    }
    
    private Preferences(Preferences preferences)
    {
	uIPreferences = new HashMap<>(preferences.uIPreferences);
	serverMap = new HashMap<>(preferences.serverMap);
	logMap = new HashMap<>(preferences.logMap);
	
	serverUsername = preferences.serverUsername;
	serverPassword = preferences.serverPassword;
    }
    
    public static Preferences getInstance()
    {
	if(instance == null)
	{
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
    
    public void cancel()
    {
	// TODO: Reset every modification we did
	instance = new Preferences(savedInstance);
    }
    
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

    public Server getServer(String name)
    {
	return instance.serverMap.get(name);
    }

    public boolean addServer(Server server)
    {
	return instance.serverMap.putIfAbsent(server.getName(), server) == null;
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
	return instance.logMap.putIfAbsent(log.getName(), log) == null;
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
    
    public void setUIPreference(int id, Rectangle pref)
    {
	instance.uIPreferences.put(id, pref);
    }
    
    public Rectangle getUIPreference(int id)
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
    
    private static Preferences instance = null;
    
    private static Preferences savedInstance;
    
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
     * Server password
     */
    private char[] serverPassword;
    
    /**
     * Position and size of each window in the UI
     */
    private Map<Integer, Rectangle> uIPreferences;
}
