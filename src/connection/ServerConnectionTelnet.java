package connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.PasswordAuthentication;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a Telnet connection to a server
 *
 * @author cartin
 */
public class ServerConnectionTelnet extends ServerConnection
{
    private final int DEFAULT_TELNET_PORT = 23;

    public ServerConnectionTelnet(String hostname, PasswordAuthentication account)
    {
	super(hostname, account);

	session = new TelnetSession();
    }

    @Override
    public boolean connect()
    {
	try
	{
	    ((TelnetSession) session).getSession().connect(hostname, DEFAULT_TELNET_PORT);

	    InputStream in = ((TelnetSession) session).getSession().getInputStream();
	    PrintStream out = new PrintStream(((TelnetSession) session).getSession().getOutputStream());

	    ((TelnetSession) session).setOutputStream(out);
//	    ((TelnetSession) session).setInputStream(in);

            // Log the user on
            readUntil("login: ", in);
	    out.println(account.getUserName());
	    out.flush();

	    readUntil("Password: ", in);
	    out.println(account.getPassword());
	    out.flush();

            // Advance to a prompt
            readUntil("$", in);
	}
	catch (IOException ex)
	{
	    Logger.getLogger(ServerConnectionTelnet.class.getName()).log(Level.SEVERE, null, ex);
	    return false;
	}

	return connected = true;
    }

    @Override
    public Session getSession()
    {
	if (!sessionActive)
	{
	    sessionActive = true;
	    return session;
	}

	return null;
    }

    @Override
    public void closeSession()
    {
	if (sessionActive)
	{
	    session.closeSession();
	    sessionActive = false;
	}
    }

    /**
     * Closes an active connection. Does nothing if the connection is inactive.
     */
    @Override
    public void closeConnection()
    {
	/*
	 * CLOSE THE CONNECTION
	 */
	if (connected)
	{
	    try
	    {
		((TelnetSession) session).getSession().disconnect();
		connected = false;
	    }
	    catch (IOException ex)
	    {
		Logger.getLogger(ServerConnectionTelnet.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
    }

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
	    Logger.getLogger(ServerConnectionTelnet.class.getName()).log(Level.SEVERE, null, ex);
	}

	return null;
    }
}
