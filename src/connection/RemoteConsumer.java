package connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import logfilter.Filter;
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
    protected InputStream in;
    protected InputStream monitoringIn;
    protected JTextArea console;
    protected boolean canConsume;
    protected Map<String, Filter> filterMap;
    protected String logName;
    protected String serverName;
    protected ServerConnection monitoringConnection;
    protected Session monitoringSession;
    protected String currentFileName;

    protected RemoteConsumer(JTextArea console, String serverName, String logName)
    {
	posy = 0;
	posx = 0;
	this.x = console.getColumns();
	this.y = console.getRows();
	this.console = console;
	this.logName = logName;
	this.serverName = serverName;
	filterMap = Preferences.getInstance().getLog(logName).getFilterMap();
	
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

    public void stopConsumer()
    {
	canConsume = false;
    }

    @Override
    public void run()
    {
	byte[] buff = new byte[8192];
	canConsume = true;

	new Thread()
	{
	    @Override
	    public void run()
	    {
		// TODO : monitoring thread

		while (true)
		{

		}
	    }
	}.start();

	while (canConsume)
	{
//		int len = in.read(buff);
//
//		clearErrs();
//
//		if (len == -1)
//		{
//		    return;
//		}
//		addText(buff, len);

	    writeToConsole(readUntilPattern());
	}
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

	    char ch = (char) in.read();
	    while (true)
	    {
		sb.append(ch);
		for (Filter f : filterMap.values())
		{
		    char lastChar = f.getKeyword().charAt(f.getKeyword().length() - 1);

		    if (ch == lastChar)
		    {
			if (sb.toString().toLowerCase().endsWith(f.getKeyword()))
			{
			    StringBuilder finalMessage = new StringBuilder();

			    finalMessage.append(addServerBanner());

			    String receivedLines[] = sb.toString().split("\\n");

			    // Get the number of lines before
			    for (int i = f.getLinesBefore(); i > 0; --i)
			    {
				if (i == f.getLinesBefore() && receivedLines.length - 1 - i < 0)
				{
				    i += receivedLines.length - 1 - i;
				}

				finalMessage.append(receivedLines[(receivedLines.length - 1) - i]);
				finalMessage.append("\n");
			    }

			    finalMessage.append(receivedLines[receivedLines.length - 1]);


			    int numLinesAfter = f.getLineAfter();
			    // Wait for the number of lines after (as well as the current line which contained the keyword)
			    for (int i = 0; i < numLinesAfter + 1; ++i)
			    {
				StringBuilder receivedLinesAfter = new StringBuilder();

				do
				{
				    receivedLinesAfter.append((char) in.read());

				    // Rerun all the filters for the lines after our found keyword
				    int maxNumLinesAfter = 0;
				    for (Filter filter : filterMap.values())
				    {
					if (receivedLinesAfter.toString().toLowerCase().endsWith(filter.getKeyword()))
					{
					    // X lines before are already done, so
					    // we just reset the counter for the lines
					    // after the keyword so that it does Y
					    // lines after this new detection
					    i = 0;

					    if (filter.getLineAfter() > maxNumLinesAfter)
					    {
						numLinesAfter = filter.getLineAfter();
						maxNumLinesAfter = numLinesAfter;
					    }
					}
				    }
				}
				while (!receivedLinesAfter.toString().endsWith("\n"));

				finalMessage.append(receivedLinesAfter);

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
		ch = (char) in.read();
	    }
	}
	catch (IOException ex)
	{
	    Logger.getLogger(ServerConnectionTelnet.class.getName()).log(Level.SEVERE, null, ex);
	}

	return null;
    }

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

    protected void getFileName()
    {
	monitoringSession.execCommand("ls -rt " + logName + "* | tail -1");

	StringBuilder sb = new StringBuilder();
	// Receive the answer
	while (true)
	{
	    try
	    {
		sb.append((char) in.read());

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

	currentFileName = sb.toString();
    }

    // TODO: create subclass that will extend TimerTask
    // Initialise it this way:
//    OfflineStateManager stateManagerThread = new OfflineStateManager();
//    Timer stateTimer = new Timer(true);
//    stateTimer.scheduleAtFixedRate(stateManagerThread, 4000, 4000);
}
