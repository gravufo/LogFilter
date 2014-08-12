package connection;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import java.net.PasswordAuthentication;
import java.util.logging.Level;
import java.util.logging.Logger;
import ui.MainWindow;

/**
 * This class represents a SSH connection to a server
 *
 * @author cartin
 */
public class ServerConnectionSSH extends ServerConnection
{
    private com.jcraft.jsch.Session connection;
    private boolean isShell;

    /**
     * Constructor
     *
     * @param hostname The hostname of the server to connect to
     * @param account  The username+password used to authenticate
     */
    public ServerConnectionSSH(String hostname, PasswordAuthentication account)
    {
	super(hostname, account);
	this.isShell = true;
    }

    @Override
    public boolean connect()
    {
	if (!connected)
	{
	    try
	    {
		/*
		 * CONNECTION PHASE
		 */
		connection = new JSch().getSession(account.getUserName(), hostname, 22);

		/*
		 * AUTHENTICATION PHASE
		 *
		 * Here, we are just using Username/Password authentication with
		 * absolutely no certificate verification or other type of auth.
		 */
		connection.setPassword(new String(account.getPassword()));

		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		connection.setConfig(config);

		connection.connect();

		/*
		 * AUTHENTICATION SUCCESSFUL
		 */
	    }
	    catch (JSchException e)
	    {
		if (e.getMessage().contains("denied"))
		{
		    MainWindow.writeToConsole("The username or password is incorrect\n");

		    // User must change prefs, so we just cancel
		    closeConnection();
		}
		else
		{
		    MainWindow.writeToConsole(hostname + ": Connection failed. Host not found or connection refused.\n");
		}

		return false;
	    }
	}

	return connected = true;
    }

    /*
     * This function starts a session on this connection.
     *
     * NOTE: We only want one session per connection, because we don't want to
     * have to deal with the hardcoded limit of 10 sessions per connection
     * (limited by most SSH implementations). Thus, this function will always
     * return the same session for a connection until that session is closed.
     */
    @Override
    public Session getSession()
    {
	if (!sessionActive)
	{
	    try
	    {
		session = new SSHSession(connection);

		if (isShell)
		{
		    session.readUntil("$ ");
		    session.execCommand("stty -echo");
		    session.readUntil("$ ");
		}

		sessionActive = true;
	    }
	    catch (JSchException ex)
	    {
		Logger.getLogger(ServerConnectionSSH.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}

	return session;
    }

    /**
     * Closes the active session so that a new one may be initiated
     */
    @Override
    public void closeSession()
    {
	/**
	 * CLOSE THE SESSION
	 */
	if (sessionActive)
	{
	    session.closeSession();
	    sessionActive = false;
	}
    }

    /**
     * Closes an active connection. Does nothing if the connection is inactive.
     */
    @Override
    public void closeConnection()
    {
	/*
	 * CLOSE THE CONNECTION
	 */
	if (connected)
	{
	    connected = false;
	    connection.disconnect();
	}
    }
}
