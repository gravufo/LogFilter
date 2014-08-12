package connection;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a SSH session (abstraction from external library)
 *
 * @author cartin
 */
public class SSHSession extends Session
{
    private Channel session;
    private PrintStream printStream;
    private InputStream inStream;
    private boolean isShell;

    /**
     * Constructor
     *
     * @param session The connection to open a session to
     * @param isShell Specifies if the Session will be a shell or an exec
     *                channel
     *
     * @throws JSchException Will be thrown if the session fails to open
     */
    public SSHSession(com.jcraft.jsch.Session session) throws JSchException
    {
	try
	{
	    if (this.isShell = true)
	    {
		this.session = (ChannelShell) session.openChannel("shell");
		((ChannelShell) this.session).setPtyType("dumb");
		this.session.connect();
	    }
	    else
	    {
		this.session = (ChannelExec) session.openChannel("exec");
	    }

	    inStream = this.session.getInputStream();
	    this.printStream = new PrintStream(this.session.getOutputStream());
	}
	catch (IOException ex)
	{
	    Logger.getLogger(SSHSession.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    public Channel getSession()
    {
	return session;
    }

    @Override
    public void execCommand(String cmd)
    {
	if (isShell)
	{
	    printStream.print(cmd + "\r\n");
	    printStream.flush();
	}
	else
	{
	    try
	    {
		((ChannelExec) session).setCommand(cmd + "\r\n");
		session.connect();
	    }
	    catch (JSchException ex)
	    {
		Logger.getLogger(SSHSession.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
    }

    @Override
    public void execCommand(int cmd)
    {
	if (isShell)
	{
	    printStream.print(cmd);
	    printStream.flush();
	}
	else
	{
	    try
	    {
		if (session.isConnected())
		{
		    return;
		}

		((ChannelExec) session).setCommand(Integer.toString(cmd));
		session.connect();
	    }
	    catch (JSchException ex)
	    {
		Logger.getLogger(SSHSession.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
    }

    @Override
    public void closeSession()
    {
	inStream = null;
	session.disconnect();
    }

    @Override
    public InputStream getInputStream()
    {
	try
	{
	    if (inStream == null)
	    {
		inStream = session.getInputStream();
	    }
	}
	catch (IOException ex)
	{
	    Logger.getLogger(SSHSession.class.getName()).log(Level.SEVERE, null, ex);
	}

	return inStream;
    }

    @Override
    public String readUntil(String pattern)
    {
	try
	{
	    int counter = 0;
	    char lastChar = pattern.charAt(pattern.length() - 1);
	    StringBuilder sb = new StringBuilder();

	    char ch = (char) inStream.read();
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

		while (inStream.available() == 0)
		{
		    Thread.sleep(100);

		    counter++;

		    if (counter == 10)
		    {
			return sb.toString();
		    }
		}

		ch = (char) inStream.read();
	    }
	}
	catch (IOException ex)
	{
	}
	catch (InterruptedException ex)
	{
	    Logger.getLogger(SSHSession.class.getName()).log(Level.SEVERE, null, ex);
	}

	return null;
    }
}
