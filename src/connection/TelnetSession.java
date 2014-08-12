package connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.telnet.TelnetClient;

/**
 * This class represents a Telnet "session" (since there are no sessions in
 * telnet, we just wrap the connection itself here)
 *
 * @author cartin
 */
public class TelnetSession extends Session
{
    private TelnetClient session;
    private PrintStream printStream;
    private InputStream in;

    public TelnetSession()
    {
	session = new TelnetClient("dumb");
    }

    public TelnetClient getSession()
    {
	return session;
    }

    @Override
    public void closeSession()
    {
	// Do nothing since Telnet does not have any sessions
//	printStream.close();
    }

    @Override
    public void execCommand(String cmd)
    {
	printStream.print(cmd + "\r\n");
	printStream.flush();
    }

    @Override
    public void execCommand(int cmd)
    {
	printStream.print(cmd);
	printStream.flush();
    }

    public void setOutputStream(OutputStream outputStream)
    {
	this.printStream = new PrintStream(outputStream);
    }

    public void setInputStream(InputStream inputStream)
    {
	in = inputStream;
    }

    @Override
    public InputStream getInputStream()
    {
	return in;
    }

    @Override
    public String readUntil(String pattern)
    {
	try
	{
	    int counter = 0;
	    char lastChar = pattern.charAt(pattern.length() - 1);
	    StringBuilder sb = new StringBuilder();

	    char ch = (char) in.read();
	    while (true)
	    {
		sb.append(ch);
		if (ch == lastChar)
		{
		    if (sb.toString().endsWith(pattern))
		    {
			return sb.toString();
		    }
		}

		while (in.available() == 0)
		{
		    Thread.sleep(100);

		    counter++;

		    if (counter == 10)
		    {
			return sb.toString();
		    }
		}

		ch = (char) in.read();
	    }
	}
	catch (InterruptedException | IOException ex)
	{
	    Logger.getLogger(TelnetSession.class.getName()).log(Level.SEVERE, null, ex);
	}

	return null;
    }
}
