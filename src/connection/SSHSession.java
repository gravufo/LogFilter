package connection;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a SSH session (abstraction from external library)
 *
 * @author cartin
 */
public class SSHSession extends Session
{
    private ChannelExec session;


    /**
     * Constructor
     *
     * @param session The connection to open a session to
     *
     * @throws JSchException Will be thrown if the session fails to open
     */
    public SSHSession(com.jcraft.jsch.Session session) throws JSchException
    {
	this.session = (ChannelExec) session.openChannel("exec");
    }

    public Channel getSession()
    {
	return session;
    }

    @Override
    public void execCommand(String cmd)
    {
	try
	{
	    session.setCommand(cmd);
	    session.connect();
	}
	catch (JSchException ex)
	{
	    Logger.getLogger(SSHSession.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    @Override
    public void closeSession()
    {
	session.disconnect();
    }
}
