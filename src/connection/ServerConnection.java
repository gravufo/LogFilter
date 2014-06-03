package connection;

import java.net.PasswordAuthentication;

/**
 * This class represents a SSH connection using a single session
 *
 * @author cartin
 */
public abstract class ServerConnection
{
    protected String hostname;
    protected PasswordAuthentication account;
    protected boolean connected;
    protected boolean sessionActive;
    protected Session session;

    public ServerConnection(String hostname, PasswordAuthentication account)
    {
	this.hostname = hostname;
	this.account = account;
	connected = false;
	sessionActive = false;
    }

    /**
     * Initiates the connection to the server
     *
     * @return true if connection is successful, false otherwise
     */
    public abstract boolean connect();

    public abstract Session getSession();

    public abstract void closeSession();

    /**
     * Closes an active connection. Does nothing if the connection is inactive.
     */
    public abstract void closeConnection();

    public boolean isConnected()
    {
	return connected;
    }

    public boolean isSessionActive()
    {
	return sessionActive;
    }

}
