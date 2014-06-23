package persistence;

import collections.Pair;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
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

	maxNumLines = 30000;
	flashTaskbar = true;
	refreshInterval = 15;

	outputToDisk = false;
	logPath = "logs/";

	// Set the default green on black colors
	backgroundColor = new Color(0, 0, 0);
	foregroundColor = new Color(0, 255, 0);

	terminalFont = new Font("Monospace", Font.PLAIN, 13);

	// Create new default log templates
	/**
	 * APPLICATION SERVER LOGS
	 */
	Log defaultLog = new Log("AS-PSLog");
	defaultLog.setFilePath("/var/broadworks/logs/appserver/");
	defaultLog.setNamePrefix("PSLog");
	logMap.put("AS-PSLog", defaultLog);

	defaultLog = new Log("AS-PSOutput");
	defaultLog.setFilePath("/var/broadworks/logs/appserver/");
	defaultLog.setNamePrefix("PSOutput");
	logMap.put("AS-PSOutput", defaultLog);

	defaultLog = new Log("AS-XSLog");
	defaultLog.setFilePath("/var/broadworks/logs/appserver/");
	defaultLog.setNamePrefix("XSLog");
	logMap.put("AS-XSLog", defaultLog);

	defaultLog = new Log("AS-XSOutput");
	defaultLog.setFilePath("/var/broadworks/logs/appserver/");
	defaultLog.setNamePrefix("XSOutput");
	logMap.put("AS-XSOutput", defaultLog);

	/**
	 * EM SERVER LOGS
	 */
	defaultLog = new Log("EMS-EmsBeLog");
	defaultLog.setFilePath("/var/broadworks/logs/emsBackEnd/");
	defaultLog.setNamePrefix("EmsBeLog");
	logMap.put("EMS-EmsBeLog", defaultLog);

	defaultLog = new Log("EMS-emsbeOutput");
	defaultLog.setFilePath("/var/broadworks/logs/emsBackEnd/");
	defaultLog.setNamePrefix("emsbeOutput");
	logMap.put("EMS-emsbeOutput", defaultLog);

	/**
	 * MEDIA SERVER LOGS
	 */
	defaultLog = new Log("MS-msfe");
	defaultLog.setFilePath("/var/broadworks/logs/mediaserver0/");
	defaultLog.setNamePrefix("msfe*.txt");
	logMap.put("MS-msfe", defaultLog);

	defaultLog = new Log("MS-msfeOutput");
	defaultLog.setFilePath("/var/broadworks/logs/mediaserver0/");
	defaultLog.setNamePrefix("msfeOutput");
	logMap.put("MS-msfeOutput", defaultLog);

	/**
	 * NETWORK SERVER LOGS
	 */
	defaultLog = new Log("NS-PSLog");
	defaultLog.setFilePath("/var/broadworks/logs/routingserver/");
	defaultLog.setNamePrefix("NSPSLog");
	logMap.put("NS-PSLog", defaultLog);

	defaultLog = new Log("NS-PSOutput");
	defaultLog.setFilePath("/var/broadworks/logs/routingserver/");
	defaultLog.setNamePrefix("NSPSOutput");
	logMap.put("NS-PSOutput", defaultLog);

	defaultLog = new Log("NS-XSLog");
	defaultLog.setFilePath("/var/broadworks/logs/routingserver/");
	defaultLog.setNamePrefix("NSXSLog");
	logMap.put("NS-XSLog", defaultLog);

	defaultLog = new Log("NS-XSOutput");
	defaultLog.setFilePath("/var/broadworks/logs/routingserver/");
	defaultLog.setNamePrefix("NSXSOutput");
	logMap.put("NS-XSOutput", defaultLog);

	defaultLog = new Log("NS-NSPortalLog");
	defaultLog.setFilePath("/var/broadworks/logs/nsportal/");
	defaultLog.setNamePrefix("NSPortalLog");
	logMap.put("NS-NSPortalLog", defaultLog);

	/**
	 * PROFILE SERVER LOGS
	 */
	defaultLog = new Log("PS-CCFileReposLog");
	defaultLog.setFilePath("/var/broadworks/logs/profileserver/");
	defaultLog.setNamePrefix("CCFileReposLog");
	logMap.put("PS-CCFileReposLog", defaultLog);

	defaultLog = new Log("PS-CCReportingLog");
	defaultLog.setFilePath("/var/broadworks/logs/profileserver/");
	defaultLog.setNamePrefix("CCReportingLog");
	logMap.put("PS-CCReportingLog", defaultLog);

	defaultLog = new Log("PS-EnhancedCallLogsDBLog");
	defaultLog.setFilePath("/var/broadworks/logs/profileserver/");
	defaultLog.setNamePrefix("EnhancedCallLogsDBLog");
	logMap.put("PS-EnhancedCallLogsDBLog", defaultLog);

	defaultLog = new Log("PS-FileReposLog");
	defaultLog.setFilePath("/var/broadworks/logs/profileserver/");
	defaultLog.setNamePrefix("FileReposLog");
	logMap.put("PS-FileReposLog", defaultLog);

	/**
	 * XSP LOGS
	 */
	defaultLog = new Log("XSP-BWCallCenter");
	defaultLog.setFilePath("/var/broadworks/logs/xsp/callcenter/");
	defaultLog.setNamePrefix("BWCallCenter");
	logMap.put("XSP-BWCallCenter", defaultLog);

	defaultLog = new Log("XSP-CCPublicReportingLog");
	defaultLog.setFilePath("/var/broadworks/logs/xsp/");
	defaultLog.setNamePrefix("CCPublicReportingLog");
	logMap.put("XSP-CCPublicReportingLog", defaultLog);

	defaultLog = new Log("XSP-CommPilotLog");
	defaultLog.setFilePath("/var/broadworks/logs/xsp/");
	defaultLog.setNamePrefix("CommPilotLog");
	logMap.put("XSP-CommPilotLog", defaultLog);

	defaultLog = new Log("XSP-dmsDebugLog");
	defaultLog.setFilePath("/var/broadworks/logs/xsp/");
	defaultLog.setNamePrefix("dmsDebugLog");
	logMap.put("XSP-dmsDebugLog", defaultLog);

	defaultLog = new Log("XSP-XsiActionsLog");
	defaultLog.setFilePath("/var/broadworks/logs/xsp/");
	defaultLog.setNamePrefix("XsiActionsLog");
	logMap.put("XSP-XsiActionsLog", defaultLog);

	defaultLog = new Log("XSP-XsiEventsLog");
	defaultLog.setFilePath("/var/broadworks/logs/xsp/");
	defaultLog.setNamePrefix("XsiEventsLog");
	logMap.put("XSP-XsiEventsLog", defaultLog);
    }

    /**
     * Copy constructor
     *
     * @param preferences The instance to copy
     */
    private Preferences(Preferences preferences)
    {
	uIPreferences = new HashMap<>(preferences.uIPreferences);

	serverMap = new HashMap<>();

	for (Server s : preferences.serverMap.values())
	{
	    Server newServer = new Server(s);
	    serverMap.put(newServer.getName(), newServer);
	}

	logMap = new HashMap<>();

	for (Log log : preferences.logMap.values())
	{
	    Log newLog = new Log(log);
	    logMap.put(newLog.getName(), newLog);
	}
	
	serverUsername = preferences.serverUsername;
	serverPassword = preferences.serverPassword;
	maxNumLines = preferences.maxNumLines;
	flashTaskbar = preferences.flashTaskbar;

	foregroundColor = preferences.foregroundColor;
	backgroundColor = preferences.backgroundColor;

	terminalFont = preferences.terminalFont;
	refreshInterval = preferences.refreshInterval;

	outputToDisk = preferences.outputToDisk;
	logPath = preferences.logPath;
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
	FileOutputStream fos = null;

	try
	{
	    fos = new FileOutputStream(fileName);
	    try (ObjectOutputStream oos = new ObjectOutputStream(fos))
	    {
		// Save every modification we did to the right instance
		savedInstance = new Preferences(instance);
		// Écrit toute l'instance (incluant ses attributs publics/privés)
		oos.writeObject(savedInstance);

		oos.close();
	    }

	    fos.close();
	}
	catch (FileNotFoundException ex)
	{
	}
	catch (IOException ex)
	{
	    Logger.getLogger(Preferences.class.getName()).log(Level.SEVERE, null, ex);
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
	FileInputStream fis;
	ObjectInputStream ois;
	try
	{
	    fis = new FileInputStream(fileName);

	    ois = new ObjectInputStream(fis);
	    Preferences.instance = new Preferences((Preferences) ois.readObject());

	    ois.close();

	    fis.close();
	}
	catch (InvalidClassException ex)
	{
	    int choice = JOptionPane.showConfirmDialog(null, "Old preference file detected. Click yes to open the program with new default preferences, or cancel to abort.");

	    if (choice == JOptionPane.YES_OPTION)
	    {
		File oldPref = new File("preferences.ser");

		oldPref.delete();

		instance = null;
	    }
	    else
	    {
		System.exit(-1);
	    }
	}
	catch (FileNotFoundException ex)
	{
	}
	catch (IOException | ClassNotFoundException ex)
	{
	    Logger.getLogger(Preferences.class.getName()).log(Level.SEVERE, null, ex);
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
	
	for (Server s : instance.serverMap.values())
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

    public int getMaxNumLines()
    {
	return instance.maxNumLines;
    }

    public void setMaxNumLines(int maxNumLines)
    {
	instance.maxNumLines = maxNumLines;
    }

    public boolean isFlashTaskbar()
    {
	return instance.flashTaskbar;
    }

    public void setFlashTaskbar(boolean flashTaskbar)
    {
	instance.flashTaskbar = flashTaskbar;
    }

    public Color getForegroundColor()
    {
	return instance.foregroundColor;
    }

    public void setForegroundColor(Color foregroundColor)
    {
	instance.foregroundColor = foregroundColor;
    }

    public Color getBackgroundColor()
    {
	return instance.backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor)
    {
	instance.backgroundColor = backgroundColor;
    }

    public Font getTerminalFont()
    {
	return instance.terminalFont;
    }

    public void setTerminalFont(Font terminalFont)
    {
	instance.terminalFont = terminalFont;
    }

    public int getRefreshInterval()
    {
	return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval)
    {
	this.refreshInterval = refreshInterval;
    }

    public boolean isOutputToDisk()
    {
	return outputToDisk;
    }

    public void setOutputToDisk(boolean outputToDisk)
    {
	this.outputToDisk = outputToDisk;
    }

    public String getLogPath()
    {
	return logPath;
    }

    public void setLogPath(String logPath)
    {
	this.logPath = logPath;
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

    /**
     * Maximum number of lines to display in the terminal text area
     */
    private int maxNumLines;

    /**
     * Determines whether we should flash the task bar when terminal is updated
     */
    private boolean flashTaskbar;

    /**
     * Color used to display text in the terminal
     */
    private Color foregroundColor;

    /**
     * The color of the background of the terminal
     */
    private Color backgroundColor;

    /**
     * Font of the text displayed in the terminal
     */
    private Font terminalFont;

    /**
     * Interval (in seconds) at which to check for a new version of the log
     */
    private int refreshInterval;

    /**
     * True if the user wants to output terminal text to a log file on disk
     */
    private boolean outputToDisk;

    /**
     * The path of the log files that will be created (relative path)
     */
    private String logPath;
}
