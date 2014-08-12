package connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.PasswordAuthentication;
import java.util.logging.Level;
import java.util.logging.Logger;
import ui.MainWindow;

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
//	    TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
//	    EchoOptionHandler echoopt = new EchoOptionHandler(false, false, true, false);
//	    SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);
//
//	    ((TelnetSession) session).getSession().addOptionHandler(ttopt);
//	    ((TelnetSession) session).getSession().addOptionHandler(echoopt);
//	    ((TelnetSession) session).getSession().addOptionHandler(gaopt);

	    ((TelnetSession) session).getSession().connect(hostname, DEFAULT_TELNET_PORT);

	    // Test if the connection is open
	    if (!((TelnetSession) session).getSession().sendAYT(5000))
	    {
		throw new IOException(hostname + ": Connection failed. Host not found or connection refused.\n");
	    }

	    InputStream in = ((TelnetSession) session).getSession().getInputStream();
	    PrintStream out = new PrintStream(((TelnetSession) session).getSession().getOutputStream());

	    ((TelnetSession) session).setOutputStream(out);
	    ((TelnetSession) session).setInputStream(in);

            // Log the user on
            ((TelnetSession) session).readUntil("login: ");
	    out.println(account.getUserName());
	    out.flush();

	    ((TelnetSession) session).readUntil("Password: ");
	    out.println(account.getPassword());
	    out.flush();

            // Advance to a prompt
            if (((TelnetSession) session).readUntil("$ ") == null)
	    {
		MainWindow.writeToConsole("The username or password is incorrect\n");
	    }

	    session.execCommand("stty -echo");
	    ((TelnetSession) session).readUntil("$ ");
	}
	catch (IOException ex)
	{
	    MainWindow.writeToConsole(ex.getMessage());
	    return false;
	}
	catch (IllegalArgumentException | InterruptedException ex)
	{
	    Logger.getLogger(ServerConnectionTelnet.class.getName()).log(Level.SEVERE, null, ex);
	}

	return connected = true;
    }

    @Override
    public Session getSession()
    {
	if (!sessionActive)
	{
	    sessionActive = true;
	}

	return session;
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
}
