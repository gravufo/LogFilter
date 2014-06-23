package connection;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
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
    private static final int MAX_CHAR_BUFF = 50000;
    private static final Pattern pattern = Pattern.compile("^\\s*.*[0-9]{4}\\.[0-9]{2}\\.[0-9]{2}\\b [0-9]{2}:[0-9]{2}:[0-9]{2}.*\\s*$");
//    private static final String messageHeader = "^.*[0-9]{4}\\.[0-9]{2}\\.[0-9]{2}\\b [0-9]{2}:[0-9]{2}:[0-9]{2}.*$";

    protected BufferedReader in;
    protected InputStream monitoringIn;
    protected JTextPane console;
    protected boolean canConsume;
    protected Map<String, Filter> filterMap;
    protected String logName;
    protected String serverName;
    protected ServerConnection monitoringConnection;
    protected Session monitoringSession;
    protected String currentFileName;
    private String savedBuffer;
    private Timer stateTimer;
    private UpdaterDaemon updaterDaemonThread;

    protected RemoteConsumer(JTextPane console, String serverName, String logName)
    {
	super("Consumer-" + serverName + "-" + logName);

	this.console = console;
	this.logName = logName;
	this.serverName = serverName;
	filterMap = Preferences.getInstance().getLog(logName).getEnabledFilters();
	savedBuffer = "";
	currentFileName = "";
    }

    /**
     * Writes the specified message to the console by using the Swing thread
     *
     * @param message The message to append to the console
     * @param highlightText Determines whether we should look for keywords or
     *                      not
     */
    protected void writeToConsole(final String message, final boolean highlightText)
    {
	SwingUtilities.invokeLater(new Runnable()
	{
	    private int min = -2;
	    private int length;
	    private String text;
	    private Color highlightColor;


	    @Override
	    public void run()
	    {
		try
		{
		    text = message;
		    StyledDocument doc = console.getStyledDocument();

		    if (highlightText)
		    {
			while (min != -1)
			{
			    findFirstKeyword();

			    if (min > 0) // if a keyword was found
			    {
				doc.insertString(doc.getLength(), text.substring(0, min), null);

				SimpleAttributeSet keyWord = new SimpleAttributeSet();
				StyleConstants.setForeground(keyWord, highlightColor);
//				StyleConstants.setForeground(keyWord, Color.RED);
//				StyleConstants.setBackground(keyWord, Color.YELLOW);
				StyleConstants.setBold(keyWord, true);

				doc.insertString(doc.getLength(), text.substring(min, min + length), keyWord);

				text = text.substring(min + length);
				min = -2;
				length = 0;
			    }
			    else if (min == -1) // no more keyword, so we just ouput the rest
			    {
				doc.insertString(doc.getLength(), text, null);
			    }
			}
		    }
		    else
		    {
			doc.insertString(doc.getLength(), text, null);
		    }

		    console.repaint();
		}
		catch (BadLocationException ex)
		{
		    Logger.getLogger(RemoteConsumer.class.getName()).log(Level.SEVERE, null, ex);
		}
	    }

	    private void findFirstKeyword()
	    {
		boolean modified = false;
		for (Filter f : filterMap.values())
		{
		    if (text.toLowerCase().contains(f.getKeyword().toLowerCase()))
		    {
			int index = text.toLowerCase().indexOf(f.getKeyword().toLowerCase());
			if (min == -2 || index < min)
			{
			    modified = true;
			    min = index;
			    length = f.getKeyword().length();
			    highlightColor = f.getHighlightColor();
			}
		    }
		}
		if (!modified)
		{
		    min = -1;
		}
	    }
	});

	// Write to log file if enabled
	if (!message.trim().isEmpty() && Preferences.getInstance().isOutputToDisk() && persistence.Logger.getInstance().isOpen())
	{
	    persistence.Logger.getInstance().write(message);
	}
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
	writeToConsole(serverName + " : " + logName + "\n", false);

	int refreshInterval = 1000 * Preferences.getInstance().getRefreshInterval();
	updaterDaemonThread = new UpdaterDaemon();
	stateTimer = new Timer(true);
	stateTimer.scheduleAtFixedRate(updaterDaemonThread, refreshInterval, refreshInterval);
    }

    protected void cleanup()
    {
	monitoringConnection.closeConnection();

	// Stop the updater daemon
	stateTimer.cancel();
	updaterDaemonThread.cancel();

	writeToConsole(serverName + " : " + logName + "\n", false);
    }

    @Override
    public void run()
    {
	// Initialise the connection
	initialise();

	currentFileName = getFileName();

	canConsume = true;

	while (canConsume)
	{
	    writeToConsole(readUntilPattern(), true);
//	    readUntilPattern();
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
	    StringBuilder sb = new StringBuilder(MAX_CHAR_BUFF);

	    // Append the saved buffer in case there was anything
	    sb.append(savedBuffer);

	    String line = savedBuffer;

	    //Clear it afterwards
	    savedBuffer = null;

	    while (canConsume)
	    {
		// Process each filter for this log file
		for (Filter f : filterMap.values())
		{
		    // If we found a match with the current filter
		    if (line.toLowerCase().contains(f.getKeyword().toLowerCase()))
		    {
			if (!canConsume)
			{
			    return "";
			}

			StringBuilder finalMessage = new StringBuilder();

			// Append the message banner
			finalMessage.append(addServerBanner());

			// Split our buffer by line. Last line will contain
			// the keyword we were looking for
			String receivedLines[] = sb.toString().split("\\n");

			int firstLine = receivedLines.length - 1;
			int message = 0;

			while (firstLine >= 0 && message < f.getMessagesBefore() + 1 && receivedLines.length > 1)
			{
			    // Verify if the line matches "YYYY.MM.DD .................."
			    if (pattern.matcher(receivedLines[firstLine]).matches())
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
			    finalMessage.append(receivedLines[firstLine++]);
			    finalMessage.append("\n");
			}

			// Append the line with the actual keyword
			finalMessage.append(line);

			int numMessagesAfter = f.getMessagesAfter();
			boolean matchFound = false;
			// Wait for the number of messages after, as well as
			// the current line which contained the keyword,
			// hence the + 1
			for (int i = 0; i < numMessagesAfter + 1; ++i)
			{
			    String receivedLinesAfter = "";

			    do
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

				// Read the next character
				receivedLinesAfter += in.readLine() + "\n";

				// Rerun all the filters for the lines after our found keyword
				for (Filter filter : filterMap.values())
				{
				    if (receivedLinesAfter.toLowerCase().contains(filter.getKeyword().toLowerCase()))
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
				for (String s : receivedLinesAfter.split("\\n"))
				{
				    if (pattern.matcher(s).matches())
				    {
					matchFound = true;
				    }
				}
			    } while (!matchFound);

			    matchFound = false;

			    // If this is not the last message AND we did not reach our buffer limit
			    if (i != numMessagesAfter && finalMessage.length() <= MAX_CHAR_BUFF)
			    {
				// We simply append the message
				// including the header of the next message
				finalMessage.append(receivedLinesAfter);
			    }
			    else // This is the last message
			    {
				// Make sure we don't continue looking for messages
				// (in case we reached buffer limit)
				i = numMessagesAfter;

				String[] splitMessage = receivedLinesAfter.split("\\n");
				for (String s : splitMessage)
				{
				    // If it isn't the header of the next
				    // message
				    if (!pattern.matcher(s).matches())
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

		// Make sure we haven't reached the buffer limit yet
		if (sb.length() > MAX_CHAR_BUFF)
		{
//		    String[] lines = sb.toString().split("\\n");
//		    sb = new StringBuilder();
//
//		    // remove 15 lines
//		    for (int i = 15; i < lines.length; ++i)
//		    {
//			sb.append(lines[i]);
//			sb.append("\n");
//		    }

//		    for (int i = 0; i < lines.length && i < 15; ++i)
//		    {
//			charCounter -= lines[i].length();
//		    }
		    int charsToDelete = sb.length() - MAX_CHAR_BUFF + MAX_CHAR_BUFF / 4;

		    sb.delete(0, charsToDelete);
//		    sb.delete(0, sb.indexOf("\\n") + 2);

		    System.gc();
		}

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

		line = in.readLine() + "\n";
		sb.append(line);
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
     * about the monitored server as well as the monitored log file
     *
     * @return The banner in the form of a string
     */
    private String addServerBanner()
    {
	StringBuilder sb = new StringBuilder();
	sb.append("\n\n\n\n---------------------------------------------- ");
	sb.append(serverName);
	sb.append(" : ");
	sb.append(logName);
	sb.append(" (");
	sb.append(currentFileName.split("\\n")[0]);
	sb.append(") ----------------------------------------------\n\n");

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

	String s = "";
	BufferedReader br = new BufferedReader(new InputStreamReader(monitoringIn));
	// Receive the answer
	while (true)
	{
	    try
	    {
//		sb.append((char) monitoringIn.read());
		s += br.readLine();

		if (!s.equals("null"))
		{
		    if (s.contains("$ "))
		    {
			s = s.substring(s.indexOf("$ ") + 2);
		    }
		    break;
		}
		else
		{
		    writeToConsole("\n\n[" + serverName + " : " + logName + "] " + "Error: File or path does not exist\n\n", false);
		    return currentFileName;
		}
	    }
	    catch (IOException ex)
	    {
		Logger.getLogger(RemoteConsumer.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}

	monitoringConnection.closeSession();

	return s;
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
		writeToConsole("\n\n[INFO] Monitoring log file updated on " + serverName + ": " + newFileName + "\n\n", false);
		ConnectionManager.getInstance().restartConnection(serverName, logName);
	    }
	}
    }
}
