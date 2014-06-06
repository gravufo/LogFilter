package connection;

import ch.ethz.ssh2.StreamGobbler;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import logfilter.Filter;
import logfilter.Log;
import persistence.Preferences;

/**
 * This class lets you actively listen and consume data coming from a session
 *
 * @author cartin
 */
public class RemoteConsumer extends Thread
{
    protected char[][] lines;
    protected int posy;
    protected int posx;
    protected int x, y;
    protected StreamGobbler in;
    protected InputStream monitoringIn;
    protected JTextArea console;
    protected boolean canConsume;
    protected Map<String, Filter> filterMap;
    protected String logName;
    protected String serverName;
    protected ServerConnection monitoringConnection;
    protected Session monitoringSession;
    protected String currentFileName;
    private String savedBuffer;
    private final int MAX_CHAR_BUFF = 10000;
    private Timer stateTimer;
    private UpdaterDaemon updaterDaemonThread;

    protected RemoteConsumer(JTextArea console, String serverName, String logName)
    {
	posy = 0;
	posx = 0;
	this.x = console.getColumns();
	this.y = console.getRows();
	this.console = console;
	this.logName = logName;
	this.serverName = serverName;
	filterMap = Preferences.getInstance().getLog(logName).getEnabledFilters();
	savedBuffer = "";

	lines = new char[y][];
    }

    protected void addText(byte[] data, int len)
    {
	StringBuilder sb = new StringBuilder(x * y);

	for (int i = 0; i < len; i++)
	{
	    sb.append((char) (data[i] & 0xff));
	}

	writeToConsole(sb.toString());
    }

    /**
     * Writes the specified message to the console by using the Swing thread
     *
     * @param message The message to append to the console
     */
    protected void writeToConsole(final String message)
    {
	SwingUtilities.invokeLater(new Runnable()
	{
	    @Override
	    public void run()
	    {
		console.append(message);
	    }
	});
    }

    /**
     * This function will alert the thread to stop. There is no guarantee that
     * the thread will stop immediately or will even stop at all.
     */
    public void stopConsumer()
    {
	canConsume = false;
    }

    protected void initialise()
    {
	writeToConsole(serverName + " : " + logName + "\n");

	currentFileName = getFileName();

	updaterDaemonThread = new UpdaterDaemon();
	stateTimer = new Timer(true);
	stateTimer.scheduleAtFixedRate(updaterDaemonThread, 4000, 4000);
    }

    protected void cleanup()
    {
	monitoringConnection.closeConnection();

	// Stop the updater daemon
	stateTimer.cancel();
	updaterDaemonThread.cancel();

	writeToConsole(serverName + " : " + logName + "\n");
    }

    @Override
    public void run()
    {
	// Initialise the connection
	initialise();

	canConsume = true;

	while (canConsume)
	{
	    writeToConsole(readUntilPattern());
	}

	cleanup();
    }

    protected void clearErrs()
    {

    }

    /**
     * Function that reads the input stream until one of the log patterns is
     * found
     *
     * @return The string that contains the text that matched the pattern and
     *         possibly a few lines before and after (depending on the user's
     *         preference)
     */
    public String readUntilPattern()
    {
	try
	{
	    StringBuilder sb = new StringBuilder();

	    int charCounter = 0;
	    char ch;

	    while (true)
	    {
		ch = (char) in.read();
		++charCounter;

		sb.append(ch);

		// Process each filter for this log file
		for (Filter f : filterMap.values())
		{
		    char lastChar = f.getKeyword().charAt(f.getKeyword().length() - 1);

		    // If the last character of our filter is the same as the
		    // last character received...
		    // This is for performance concerns, prevents the checking
		    // of each single character when we have no clue if it is
		    // even remotely close to our filter
		    if (ch == lastChar)
		    {
			// If we found a match with the current filter
			if (sb.toString().toLowerCase().endsWith(f.getKeyword()))
			{
			    StringBuilder finalMessage = new StringBuilder();

			    // Append the message banner
			    finalMessage.append(addServerBanner());

			    // Append the saved buffer in case there was anything
			    finalMessage.append(savedBuffer);

			    //Clear it afterwards
			    savedBuffer = "";

			    // Split our buffer by line. Last line will contain
			    // the keyword we were looking for
			    String receivedLines[] = sb.toString().split("\\n");

			    // -2 because we don't want to search the line
			    // containing the keyword found (aka the latest line)
			    int firstLine = receivedLines.length - 1 - 1;
			    int message = 0;

			    while (firstLine >= 0 && message < f.getLinesBefore() && receivedLines.length > 1)
			    {
				// Verify if the line matches "YYYY.MM.DD .................."
				if (receivedLines[firstLine].matches("^.*[0-9]{4}\\.(3[01]|[12][0-9]|0[1-9])\\.(1[0-2]|0[1-9])\\b [0-9]{2}:[0-9]{2}:[0-9]{2}.*$"))
				{
				    // Increment the number of messages found
				    ++message;
				}

				// change line
				--firstLine;
			    }

			    // cancel out the last -- of the while
			    ++firstLine;

			    while (firstLine < receivedLines.length - 1)
			    {
				// Append the line with the actual keyword
				finalMessage.append(receivedLines[firstLine++]);
				finalMessage.append("\n");
			    }

			    // Append the line with the actual keyword
			    finalMessage.append(receivedLines[receivedLines.length - 1]);

			    int numMessagesAfter = f.getMessagesAfter();
			    boolean matchFound = false;
			    // Wait for the number of messages after, as well as
			    // the current line which contained the keyword,
			    // hence the + 1
			    for (int i = 0; i < numMessagesAfter + 1; ++i)
			    {
				StringBuilder receivedLinesAfter = new StringBuilder();

				do
				{
				    // Read the next character
				    receivedLinesAfter.append((char) in.read());

				    // Rerun all the filters for the lines after our found keyword
				    for (Filter filter : filterMap.values())
				    {
					if (receivedLinesAfter.toString().toLowerCase().endsWith(filter.getKeyword()))
					{
					    if (filter.getMessagesAfter() > numMessagesAfter - i)
					    {
						// X lines before are already done, so
						// we just reset the counter for the lines
						// after the keyword so that it does Y
						// lines after this new detection
						i = 0;
						numMessagesAfter = filter.getMessagesAfter();
					    }
					}
				    }

				    // Look for the header of the next message
				    for (String s : receivedLinesAfter.toString().split("\\n"))
				    {
					if (s.matches("^.*[0-9]{4}\\.(3[01]|[12][0-9]|0[1-9])\\.(1[0-2]|0[1-9])\\b [0-9]{2}:[0-9]{2}:[0-9]{2}.*$"))
					{
					    matchFound = true;
					}
				    }
				} while (!matchFound);

				matchFound = false;

				// If this is not the last message
				if (i != numMessagesAfter)
				{
				    // We simply append the message
				    // including the header of the next message
				    finalMessage.append(receivedLinesAfter);
				}
				else // This is the last message
				{
				    String[] splitMessage = receivedLinesAfter.toString().split("\\n");
				    for (String s : splitMessage)
				    {
					// If it isn't the header of the next
					// message
					if (!s.matches("^.*[0-9]{4}\\.(3[01]|[12][0-9]|0[1-9])\\.(1[0-2]|0[1-9])\\b [0-9]{2}:[0-9]{2}:[0-9]{2}.*$"))
					{
					    // Display it for the current loop
					    finalMessage.append(s);
					    finalMessage.append("\n");
					}
					else // This is the header of the next message
					{
					    // Save it for the next iteration!
					    savedBuffer = s;
					}
				    }
				}

				// NEVERMIND, this is bad, because we will have
				// issues with prints from one server occuring
				// during prints of other servers (or log files)
				//
//				// After a certain percentage (ex: 25%) of
//				// numLinesAfter, display it to the console in
//				// order to improve the response time and not make
//				// the user wait for another event/more lines to come
//				if (i == numLinesAfter / 4)
//				{
//				    writeToConsole(finalMessage.toString());
//				    finalMessage = new StringBuilder();
//				}
			    }

			    // Return the final string
			    return finalMessage.toString();
			}
		    }
		}

		// Make sure we haven't reached the buffer limit yet
		if (charCounter == MAX_CHAR_BUFF)
		{
		    return "";
		}
	    }
	}
	catch (IOException ex)
	{
	    Logger.getLogger(ServerConnectionTelnet.class.getName()).log(Level.SEVERE, null, ex);
	}

	return null;
    }

    /**
     * This function generates a banner for a message containing information
     * about the monitored server as well as the monitored log file
     *
     * @return The banner in the form of a string
     */
    private String addServerBanner()
    {
	StringBuilder sb = new StringBuilder();
	sb.append("\n\n----------------------------- ");
	sb.append(serverName);
	sb.append(" : ");
	sb.append(logName);
	sb.append(" -----------------------------\n\n");

	return sb.toString();
    }

    /**
     * This function will ensure we are using the right session for the
     * monitoring connection
     */
    protected void ensureConnection()
    {
	monitoringSession = monitoringConnection.getSession();
    }

    /**
     * Gets the latest log file's name
     *
     * @return The full name of the latest log file. Ex.: "test-[date].log"
     */
    protected String getFileName()
    {
	ensureConnection();

	Log log = Preferences.getInstance().getLog(logName);

	monitoringSession.execCommand("ls -rt " + log.getFilePath() + " | grep " + log.getNamePrefix() + "* | tail -1");

	StringBuilder sb = new StringBuilder();
	// Receive the answer
	while (true)
	{
	    try
	    {
		sb.append((char) monitoringIn.read());

		if (sb.toString().endsWith("\n"))
		{
		    break;
		}
	    }
	    catch (IOException ex)
	    {
		Logger.getLogger(RemoteConsumer.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}

	monitoringConnection.closeSession();

	return sb.toString();
    }

    /**
     * This updater Daemon will look for a newer version of the log file we are
     * monitoring in order to restart the connection.
     */
    public class UpdaterDaemon extends TimerTask
    {
	@Override
	public void run()
	{
	    String newFileName = getFileName();
	    if (!currentFileName.equals(newFileName))
	    {
		writeToConsole("\n\n[INFO] Monitoring log file updated on " + serverName + ": " + newFileName + "\n\n");
		ConnectionManager.getInstance().restartConnection(serverName, logName);
	    }
	}
    }
}
