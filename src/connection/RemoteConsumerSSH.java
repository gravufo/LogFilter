package connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import persistence.Preferences;

/**
 * This class represents the remote consumer for a SSH connection
 *
 * @author cartin
 */
public class RemoteConsumerSSH extends RemoteConsumer
{
//    private InputStream err;

    /**
     * Constructor
     *
     * @param console The console to display in
     * @param session The session to consume
     * @param serverName The name of the server we are monitoring
     * @param logName    The name of the log we are monitoring
     */
    public RemoteConsumerSSH(JTextPane console, Session session, String serverName, String logName)
    {
	super(console, serverName, logName);

	try
	{
	    in = new BufferedReader(new InputStreamReader(((SSHSession) session).getSession().getInputStream()));

	    //err = new StreamGobbler(((SSHSession) session).getSession().getStderr());
	}
	catch (IOException ex)
	{
	    Logger.getLogger(RemoteConsumerSSH.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    @Override
    protected void initialise()
    {
	super.initialise();

	monitoringConnection = new ServerConnectionSSH(Preferences.getInstance().getServer(serverName).getHostname(), Preferences.getInstance().getServerAccount());
	monitoringConnection.connect();
    }

    @Override
    protected void ensureConnection()
    {
	super.ensureConnection();

	try
	{
	    monitoringIn = ((SSHSession) monitoringSession).getSession().getInputStream();
	}
	catch (IOException ex)
	{
	    Logger.getLogger(RemoteConsumerSSH.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    @Override
    protected void clearErrs()
    {
//	try
//	{
//	    err.read();
//	}
//	catch (IOException ex)
//	{
//	    Logger.getLogger(RemoteConsumerSSH.class.getName()).log(Level.SEVERE, null, ex);
//	}
    }
}
