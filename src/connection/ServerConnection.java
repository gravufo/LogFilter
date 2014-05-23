/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connection;

import ui.MainWindow;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 *
 * @author cartin
 */
public class ServerConnection extends Thread
{
    String hostname,
	    username,
	    password;
    Connection conn;

    public ServerConnection(String hostname, String username, String password)
    {
	this.hostname = hostname;
	this.username = username;
    }

    @Override
    public void run()
    {
	conn = new Connection(hostname);

	try
	{
	    /*
	     *
	     * CONNECT
	     *
	     */
	    conn.connect();

	    /*
	     *
	     * AUTHENTICATION PHASE
	     *
	     */
	    boolean enableKeyboardInteractive = true;

	    boolean res = conn.authenticateWithPassword(username, password);

	    if (res == false)
	    {
		System.out.println("The username or password is incorrect");
	    }

	    /*
	     *
	     * AUTHENTICATION OK. DO SOMETHING.
	     *
	     */
	    Session sess = conn.openSession();

//	    int x_width = 90;
//	    int y_width = 30;
//
//	    sess.requestPTY("dumb", x_width, y_width, 0, 0, null);
	    sess.startShell();

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
	conn.close();
    }
}
