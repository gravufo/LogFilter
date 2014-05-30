package connection;

import javax.swing.JTextArea;
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
    public RemoteConsumerSSH(JTextArea console, Session session, String serverName, String logName)
    {
	super(console, serverName, logName);

	monitoringConnection = new ServerConnectionSSH(Preferences.getInstance().getServer(serverName).getHostname(), Preferences.getInstance().getServerAccount());
	monitoringSession = monitoringConnection.getSession();

	in = ((SSHSession) session).getSession().getStdout();
	//err = new StreamGobbler(((SSHSession) session).getSession().getStderr());
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
