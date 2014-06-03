package connection;

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
	printStream.close();
    }

    @Override
    public void execCommand(String cmd)
    {
	printStream.println(cmd);
	printStream.flush();
    }

    public void setOutputStream(OutputStream outputStream)
    {
	this.printStream = new PrintStream(outputStream);
    }

//    public void setInputStream(InputStream inputStream)
//    {
//	this.inputStream = inputStream;
//    }
}
