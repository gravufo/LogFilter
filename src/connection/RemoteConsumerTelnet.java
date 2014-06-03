package connection;

import ch.ethz.ssh2.StreamGobbler;
import javax.swing.JTextArea;
import persistence.Preferences;

/**
 * This class represents the remote consumer for a Telnet connection
 *
 * @author cartin
 */
public class RemoteConsumerTelnet extends RemoteConsumer
{
    /**
     * Constructor
     *
     * @param console The console to display to
     * @param session    The session to consume
     * @param serverName The name of the server we are monitoring
     * @param logName    The name of the log we are monitoring
     */
    public RemoteConsumerTelnet(JTextArea console, Session session, String serverName, String logName)
    {
	super(console, serverName, logName);

	in = new StreamGobbler(((TelnetSession) session).getSession().getInputStream());
    }

    @Override
    protected void initialise()
    {
	monitoringConnection = new ServerConnectionTelnet(Preferences.getInstance().getServer(serverName).getHostname(), Preferences.getInstance().getServerAccount());
	monitoringConnection.connect();
	monitoringSession = monitoringConnection.getSession();
	monitoringIn = ((TelnetSession) monitoringSession).getSession().getInputStream();
    }
}
