package connection;

import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import logfilter.Log;
import logfilter.Server;
import persistence.Preferences;

/**
 * Connection Manager manages connections to servers and (de)registers them
 * automatically to the RemoteConsumerManager.
 * This class is a singleton.
 *
 * @author cartin
 */
public class ConnectionManager
{
    private Map<String, Map<String, ServerConnection>> connectionsMap;
    private RemoteConsumerManager remoteConsumerManager;
    private static ConnectionManager instance = null;

    /**
     * Private constructor. Will only be called once, because of the Singleton
     */
    private ConnectionManager()
    {
	remoteConsumerManager = RemoteConsumerManager.getInstance();
	connectionsMap = new HashMap<>();
    }

    public static ConnectionManager getInstance()
    {
	if (instance == null)
	{
	    instance = new ConnectionManager();
	}
	return instance;
    }

    public synchronized ServerConnection getConnection(String serverName, String logName)
    {
	return connectionsMap.get(serverName).get(logName);
    }

    /**
     * Closes and removes a single connection to the server specified
     *
     * @param serverName The name of the server to which there is a connection
     * @param logName    The name of the log that is monitored
     *
     * @return true if the removal is successful, false otherwise
     */
    public synchronized boolean removeConnection(String serverName, String logName)
    {
	Map<String, ServerConnection> connection = connectionsMap.get(serverName);

	ServerConnection con;
	if ((con = connection.remove(logName)) != null)
	{
	    if (con.isConnected)
	    {
		con.closeConnection();
		if (con.isSessionActive)
		{
		    con.closeSession();
		    remoteConsumerManager.removeRemoteConsumer(serverName, logName);
		}
	    }

	    if (connection.isEmpty())
	    {
		connectionsMap.remove(serverName);
	    }

	    return true;
	}
	else
	{
	    return false;
	}
    }

    /**
     * Closes and removes all connections associated to the server specified
     *
     * @param serverName The name of the server to which there is a connection
     *
     * @return true if the removal is successful, false otherwise
     */
    public synchronized boolean removeConnection(String serverName)
    {
	boolean success = true;

	Map<String, ServerConnection> connection = connectionsMap.get(serverName);

	for (String logName : new HashSet<>(connection.keySet()))
	{
	    removeConnection(serverName, logName);
	}
	return success;
    }

    /**
     * This method removes all connections registered with the ConnectionManager
     * Its implementation is similar to a loop calling removeConnection()
     *
     * @return true if all the connections were removes successfully, false
     *         otherwise
     */
    public synchronized boolean removeConnections()
    {
	boolean success = true;

	// For every server
	for (Map.Entry<String, Map<String, ServerConnection>> entry : new HashSet<>(connectionsMap.entrySet()))
	{
	    // Remove the connections on every log of that server
	    if (!removeConnection(entry.getKey()))
	    {
		success = false;
	    }
	}

	return success;
    }

    /**
     * This method creates a new connection for each log file on the server
     * specified
     *
     * @param server  The server to connect to
     * @param account The account to use to authenticate to that server
     *
     * @return true if the connections were all successful, false otherwise
     */
    public synchronized boolean addConnection(Server server, PasswordAuthentication account)
    {
	boolean success = true;

	if (!connectionsMap.containsKey(server.getName()))
	{
	    connectionsMap.put(server.getName(), new HashMap<String, ServerConnection>());
	}

	for (String logName : server.getLogList())
	{
	    if (!addConnection(server, logName, account))
	    {
		success = false;
	    }
	}

	return success;
    }

    /**
     * This method creates a new connection for a single log file on the server
     * specified
     *
     * @param server  The server to connect to
     * @param logName The name of the log to create a connection for
     * @param account The account to use to authenticate to that server
     *
     * @return true if the connections were all successful, false otherwise
     */
    public synchronized boolean addConnection(Server server, String logName, PasswordAuthentication account)
    {
	if (!connectionsMap.containsKey(server.getName()))
	{
	    connectionsMap.put(server.getName(), new HashMap<String, ServerConnection>());
	}

	ServerConnection connection;

	if (server.isUseSSH())
	{
	    connection = new ServerConnectionSSH(server.getHostname(), account);
	}
	else
	{
	    connection = new ServerConnectionTelnet(server.getHostname(), account);
	}

	return connectionsMap.get(server.getName()).put(logName, connection) == null;
    }

    /**
     * This method allows the addition of multiple connections in a single call.
     * The implementation is similar to a loop that would call addConnection()
     *
     * @param servers The list of servers to create a connection for
     * @param account The account that will be used for all the connections
     *                created
     *
     * @return true if all the connections were successfully created, false
     *         otherwise
     */
    public synchronized boolean addConnections(ArrayList<Server> servers, PasswordAuthentication account)
    {
	boolean success = true;

	for (Server s : servers)
	{
	    if (!addConnection(s, account))
	    {
		success = false;
	    }
	}

	return success;
    }

    /**
     * Starts the connection and a session on this connection. The latter is
     * added to the RemoveConsumerManager instance
     *
     * @param serverName The name of the server to connect to
     * @param logName    The name of the log that will be monitored by this
     *                   connection
     * @param sc         The connection to use
     *
     * @return true if the operation is successful, false otherwise
     */
    private synchronized boolean startConnection(String serverName, String logName, ServerConnection sc)
    {
	// Make sure the connection works
	if (sc.connect())
	{
	    // Add the session to the consumer manager
	    remoteConsumerManager.addRemoteConsumer(serverName, logName, sc.getSession());

	    return true;
	}

	// If the connection fails we return false
	return false;
    }

    /**
     * Starts the connection to a single server
     *
     * @param serverName The name of the server registered to this manager
     * @param logName    The name of the log that will be monitored by this
     *                   connection
     *
     * @return true if the connection was successful, false otherwise
     */
    public synchronized boolean startConnection(String serverName, String logName)
    {
	return startConnection(serverName, logName, connectionsMap.get(serverName).get(logName));
    }

    /**
     * Starts all the connections registered with this manager
     *
     * @return true if all the connection were successful, false otherwise
     */
    public synchronized boolean startConnections()
    {
	boolean success = true;

	// Map.Entry<K, V> entry : map.entrySet()
	for (Map.Entry<String, Map<String, ServerConnection>> sc : connectionsMap.entrySet())
	{
	    for (Map.Entry<String, ServerConnection> connection : sc.getValue().entrySet())
	    {
		if (!startConnection(sc.getKey(), connection.getKey(), connection.getValue()))
		{
		    success = false;
		}
	    }
	}

	return success;
    }

    /**
     * This method executes the command of a single registered server
     *
     * @param serverName Name of the server registered to this manager
     * @param logName    Name of the log to monitor
     */
    public void executeCommand(String serverName, String logName)
    {
	Log log = Preferences.getInstance().getLog(logName);
	remoteConsumerManager.executeCommand(serverName, logName, "tail -f `ls -tr " + log.getFilePath() + log.getNamePrefix() + "* | tail -1`");
    }

    /**
     * This method executes the commands of all the servers registered to this
     * manager
     */
    public synchronized void executeCommands()
    {
	for (Map.Entry<String, Map<String, ServerConnection>> sc : connectionsMap.entrySet())
	{
	    for (Map.Entry<String, ServerConnection> connection : sc.getValue().entrySet())
	    {
		executeCommand(sc.getKey(), connection.getKey());
	    }
	}
    }
}
