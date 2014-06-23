package connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
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
//    private InputStream inputStream;

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

//	readUntil("$ ", session.getInputStream());
    }

    public void setOutputStream(OutputStream outputStream)
    {
	this.printStream = new PrintStream(outputStream);
    }

//    public void setInputStream(InputStream inputStream)
//    {
//	this.inputStream = inputStream;
//    }
    public String readUntil(String pattern, InputStream in)
    {
	try
	{
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
		ch = (char) in.read();
	    }
	}
	catch (IOException ex)
	{
	}

	return null;
    }
}
