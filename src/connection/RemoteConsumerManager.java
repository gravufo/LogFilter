package connection;

import collections.Pair;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTextArea;
import persistence.Preferences;

/**
 * This class manages the RemoteConsumer instances for each session (and in our
 * case, connection). It will automatically start and stop the consumers when
 * you register or de-register a session
 *
 * @author cartin
 */
public class RemoteConsumerManager
{
    private Map<String, Map<String, Pair<Session, RemoteConsumer>>> remoteConsumerMap;
    private static RemoteConsumerManager instance = null;
    private JTextArea console;

    /**
     * Private constructor, will only be called once: Singleton
     */
    private RemoteConsumerManager()
    {
	remoteConsumerMap = new HashMap<>();
    }

    /**
     * Singleton access method
     *
     * @return The single instance of this class
     */
    public static RemoteConsumerManager getInstance()
    {
	if (instance == null)
	{
	    instance = new RemoteConsumerManager();
	}

	return instance;
    }

    /**
     * Registers a session to this manager and automatically creates a
     * RemoteConsumer for it
     *
     * @param serverName The name of the server that will be registered to this
     *                   session
     * @param logName    The name of the log to monitor
     * @param session    The session to register
     */
    public synchronized void addRemoteConsumer(String serverName, String logName, Session session)
    {
	RemoteConsumer temp;

	if (Preferences.getInstance().getServer(serverName).isUseSSH())
	{
	    temp = new RemoteConsumerSSH(console, session, serverName, logName);
	}
	else
	    temp = new RemoteConsumerTelnet(console, session, serverName, logName);

	Pair<Session, RemoteConsumer> pair = new Pair<>(session, temp);

	// If the server has not been registered yet
	if (!remoteConsumerMap.containsKey(serverName))
	{
	    Map<String, Pair<Session, RemoteConsumer>> map = new HashMap<>();
	    map.put(logName, pair);

	    remoteConsumerMap.put(serverName, map);
	}
	else
	{
	    remoteConsumerMap.get(serverName).put(logName, pair);
	}
    }

    /**
     * Executes the command of a single connection
     *
     * @param serverName The name of the server to connect to
     * @param logName    The name of the log file to monitor
     * @param cmd        The command to execute
     */
    public synchronized void executeCommand(String serverName, String logName, String cmd)
    {
	Pair<Session, RemoteConsumer> temp = remoteConsumerMap.get(serverName).get(logName);
	temp.getKey().execCommand(cmd);
	temp.getValue().start();
    }

    /**
     * Removes a single RemoteConsumer on a registered session and de-registers
     * that session
     *
     * @param serverName The name of the registered server to remove
     * @param logName    The name of the log file monitored by the connection to
     *                   remove
     */
    public synchronized void removeRemoteConsumer(String serverName, String logName)
    {
	Pair<Session, RemoteConsumer> temp = remoteConsumerMap.remove(serverName).get(logName);

	// The session is closed in the Connection Manager (yeah it's bad, oh well)
	//temp.getKey().close();
	// Stop the consumer from its infinite loop
	temp.getValue().stopConsumer();
    }

    /**
     * Set the graphical component to output on
     *
     * @param console JTextArea that will contain the output
     */
    public void setConsole(JTextArea console)
    {
	this.console = console;
    }
}
