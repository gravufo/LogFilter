package connection;

import ch.ethz.ssh2.Connection;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a SSH session (abstraction from external library)
 *
 * @author cartin
 */
public class SSHSession extends Session
{
    private ch.ethz.ssh2.Session session;

    /**
     * Constructor
     *
     * @param connectionSSH The connection to open a session to
     *
     * @throws IOException Will be thrown if the session fails to open
     */
    public SSHSession(Connection connectionSSH) throws IOException
    {
	session = connectionSSH.openSession();
    }

    public ch.ethz.ssh2.Session getSession()
    {
	return session;
    }

    @Override
    public void execCommand(String cmd)
    {
	try
	{
	    session.execCommand(cmd);
	}
	catch (IOException ex)
	{
	    Logger.getLogger(SSHSession.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    @Override
    public void closeSession()
    {
	session.close();
    }
}
