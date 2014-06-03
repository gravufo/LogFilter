package connection;

import ch.ethz.ssh2.Connection;
import java.io.IOException;
import java.net.PasswordAuthentication;
import javax.swing.JOptionPane;
import ui.MainWindow;

/**
 * This class represents a SSH connection to a server
 *
 * @author cartin
 */
public class ServerConnectionSSH extends ServerConnection
{
    private Connection connection;

    /**
     * Constructor
     *
     * @param hostname The hostname of the server to connect to
     * @param account  The username+password used to authenticate
     */
    public ServerConnectionSSH(String hostname, PasswordAuthentication account)
    {
	super(hostname, account);
    }

    @Override
    public boolean connect()
    {
	if (!connected)
	{
	    connection = new Connection(hostname);

	    try
	    {
		/*
		 * CONNECTION PHASE
		 */
		connection.connect();

		/*
		 * AUTHENTICATION PHASE
		 *
		 * Here, we are just using Username/Password authentication with
		 * absolutely no certificate verification or other type of auth.
		 */
		boolean res = connection.authenticateWithPassword(account.getUserName(), new String(account.getPassword()));

		if (res == false)
		{
		    System.out.println("The username or password is incorrect");

		    // User must change prefs, so we just cancel
		    closeConnection();
		    return false;
		}

		/*
		 * AUTHENTICATION SUCCESSFUL
		 */
	    }
	    catch (IOException e)
	    {
		JOptionPane.showMessageDialog(MainWindow.getFrames()[0], "Exception: " + e.getMessage());
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
		sessionActive = true;
	    }
	    catch (IOException e)
	    {
		JOptionPane.showMessageDialog(MainWindow.getFrames()[0], "Exception: " + e.getMessage());
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
	    connection.close();
	    connected = false;
	}
    }
}
