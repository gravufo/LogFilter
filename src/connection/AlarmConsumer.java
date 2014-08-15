package connection;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import logfilter.Server;
import persistence.Preferences;
import ui.MainWindow;

/**
 *
 * @author cartin
 */
public class AlarmConsumer extends Thread
{
    private ServerConnection connection;
    private Session session;
    private final JTextPane console;
    private boolean canConsume;
    private final String serverName;
    private static final Pattern pattern = Pattern.compile("^\\s*\\w*\\s*\\w*\\s*[0-9]{2}\\s*[0-9]{2}:[0-9]{2}:[0-9]{2}\\s*\\w*\\s*[0-9]{4}\\s*$");
    private BufferedReader in;
    private ArrayList<String> filters;

    public AlarmConsumer(String serverName)
    {
	super("Alarm thread: " + serverName);

	this.serverName = serverName;
	console = MainWindow.getConsolePane();
    }

    @Override
    public void run()
    {
	// Initialise the connection
	initialise();

	while (canConsume)
	{
	    writeToConsole(readUntilPattern(), true);
	}

	cleanup();
    }

    private void initialise()
    {
	// Get the filters from the prefs
	filters = Preferences.getInstance().getAlarmFiltersList();

	// Create the connection for the alarm monitoring
	Server server = Preferences.getInstance().getServer(serverName);
	String expect1, expect2;

	if (server.isUseSSH())
	{
	    connection = new ServerConnectionSSH(server.getHostname(), Preferences.getInstance().getServerAccount());
	    expect1 = "_CLI> ";
	    expect2 = "_CLI/Monitoring/Alarm> ";
	}
	else
	{
	    connection = new ServerConnectionTelnet(server.getHostname(), Preferences.getInstance().getServerAccount());
	    expect1 = "Reading initial CLI command file...";
	    expect2 = "...Done";
	}

	connection.connect();

	session = connection.getSession();

	in = new BufferedReader(new InputStreamReader(session.getInputStream()));
	canConsume = true;

	writeToConsole("Started alarm monitoring on " + serverName + "\n", false);

	// Start the CLI
	session.execCommand("bwcli");
	session.readUntil(expect1);

	// Start monitoring the alarms
	session.execCommand("monitor;alarm;open;show on");
	session.readUntil(expect2);
    }

    private void cleanup()
    {
	try
	{
	    in.close();
	}
	catch (IOException ex)
	{
	    Logger.getLogger(AlarmConsumer.class.getName()).log(Level.SEVERE, null, ex);
	}

	connection.closeSession();
	connection.closeConnection();

	writeToConsole("Stopped alarm monitoring on " + serverName + "\n", false);
    }

    /**
     * This function will alert the thread to stop. There is no guarantee that
     * the thread will stop immediately or will even stop at all.
     */
    public void stopConsumer()
    {
	canConsume = false;
    }

    private void writeToConsole(final String text, final boolean useColor)
    {
	SwingUtilities.invokeLater(new Runnable()
	{
	    @Override
	    public void run()
	    {
		try
		{
		    StyledDocument doc = console.getStyledDocument();

		    if (useColor)
		    {
			SimpleAttributeSet keyWord = new SimpleAttributeSet();
			StyleConstants.setForeground(keyWord, Color.red);
			StyleConstants.setBold(keyWord, true);

			doc.insertString(doc.getLength(), text, keyWord);
		    }
		    else
		    {
			doc.insertString(doc.getLength(), text, null);
		    }

		    console.repaint();
		}
		catch (BadLocationException ex)
		{
		    Logger.getLogger(AlarmConsumer.class.getName()).log(Level.SEVERE, null, ex);
		}
	    }
	});

	// Write to log file if enabled
	if (!text.trim().isEmpty() && Preferences.getInstance().isOutputToDisk() && persistence.Logger.getInstance().isOpen())
	{
	    persistence.Logger.getInstance().write(text);
	}
    }

    private String readUntilPattern()
    {
	String line = "";
	try
	{
	    while (canConsume)
	    {
		// Make sure we have data waiting (we don't
		// want to block on the in.read)
		while (!in.ready())
		{
		    // Wait another 10 ms
		    sleep(10);

		    // If a stop was called
		    if (!canConsume)
		    {
			return "";
		    }
		}

		line += (char) in.read();

		String[] lines = line.split("\\r\\n");

		// If we found a match with the alarm header
		if (lines.length > 0 && pattern.matcher(lines[lines.length - 1]).matches())
		{
		    StringBuilder finalMessage = new StringBuilder();

		    // Append the message banner and the first line
		    finalMessage.append(getServerBanner());

		    finalMessage.append(lines[lines.length - 1]);
		    finalMessage.append("\n");

		    line = "";

		    for (boolean isEndOfMessage = false; !isEndOfMessage;)
		    {
			// Make sure we have data waiting (we don't
			// want to block on the in.read)
			while (!in.ready())
			{
			    // Wait another 10 ms
			    sleep(10);

			    // If a stop was called
			    if (!canConsume)
			    {
				return "";
			    }
			}

			char c = (char) in.read();
			line += c;

			String lcLine = line.toLowerCase();

			if (line.contains("_CLI/Monitoring/Alarm> "))
			{
			    isEndOfMessage = true;

			    finalMessage.append(line.substring(0, line.indexOf("_CLI/Monitoring/Alarm> ") - 2));
			}
			else if (lcLine.contains("trapname"))
			{
			    for (String f : filters)
			    {
				if (lcLine.contains(f))
				{
				    return "";
				}
			    }
			}
		    }

		    // Return the final string
		    return finalMessage.toString();
		}
		else if (line.contains("The communication channel with the SNMP agent just went down."))
		{
		    session.execCommand("open");
		}
	    }
	}
	catch (IOException ex)
	{
	    Logger.getLogger(ServerConnectionTelnet.class.getName()).log(Level.SEVERE, null, ex);
	}
	catch (InterruptedException ex)
	{
	    Logger.getLogger(RemoteConsumer.class.getName()).log(Level.SEVERE, null, ex);
	}

	return "";
    }

    /**
     * This function generates a banner for a message containing information
     * about the monitored server
     *
     * @return The banner in the form of a string
     */
    private String getServerBanner()
    {
	StringBuilder sb = new StringBuilder();
	sb.append("\n\n\n\n============================================== ALARM: ");
	sb.append(serverName);
	sb.append(" ==============================================\n\n");

	return sb.toString();
    }
}
