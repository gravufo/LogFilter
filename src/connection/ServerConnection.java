/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connection;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import java.io.IOException;
import java.net.PasswordAuthentication;
import javax.swing.JOptionPane;
import ui.MainWindow;

/**
 *
 * @author cartin
 */
public class ServerConnection extends Thread
{
    String hostname;
    PasswordAuthentication account;
    Connection connection;

    public ServerConnection(String hostname, PasswordAuthentication account)
    {
	this.hostname = hostname;
	this.account = account;
    }

    @Override
    public void run()
    {
	connection = new Connection(hostname);

	try
	{
	    /*
	     *
	     * CONNECT
	     *
	     */
	    connection.connect();

	    /*
	     *
	     * AUTHENTICATION PHASE
	     *
	     */

	    boolean res = connection.authenticateWithPassword(account.getUserName(), new String(account.getPassword()));

	    if (res == false)
	    {
		System.out.println("The username or password is incorrect");
	    }

	    /*
	     *
	     * AUTHENTICATION OK. DO SOMETHING.
	     *
	     */
	    Session session = connection.openSession();

	    // TODO: Verify if we really need this or not!
//	    int x_width = 90;
//	    int y_width = 30;
//
//	    sess.requestPTY("dumb", x_width, y_width, 0, 0, null);
	    // Start the shell on the remote host
	    session.startShell();

	    //TerminalDialog td = new TerminalDialog(MainWindow.getFrames()[0], username + "@" + hostname, sess, x_width, y_width);
	    
	    JOptionPane.showMessageDialog(MainWindow.getFrames()[0], "Exception: ");
	    
	    /*
	     * The following call blocks until the dialog has been closed
	     */
	    //td.setVisible(true);

	}
	catch (IOException e)
	{
	    JOptionPane.showMessageDialog(MainWindow.getFrames()[0], "Exception: " + e.getMessage());
	}
    }
    
    public void closeConnection()
    {
	/*
	 * CLOSE THE CONNECTION.
	 */
	connection.close();
    }
}
