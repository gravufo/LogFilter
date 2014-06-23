package connection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.swing.JTextPane;
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
    public RemoteConsumerTelnet(JTextPane console, Session session, String serverName, String logName)
    {
	super(console, serverName, logName);

	in = new BufferedReader(new InputStreamReader(((TelnetSession) session).getSession().getInputStream()));
    }

    @Override
    protected void initialise()
    {
	super.initialise();

	monitoringConnection = new ServerConnectionTelnet(Preferences.getInstance().getServer(serverName).getHostname(), Preferences.getInstance().getServerAccount());
	monitoringConnection.connect();
	monitoringSession = monitoringConnection.getSession();
	monitoringIn = ((TelnetSession) monitoringSession).getSession().getInputStream();
    }

    @Override
    protected void ensureConnection()
    {
	super.ensureConnection();

	monitoringIn = ((TelnetSession) monitoringSession).getSession().getInputStream();
    }
}
