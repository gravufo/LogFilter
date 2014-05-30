package ui;

import connection.ConnectionManager;
import connection.RemoteConsumerManager;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.BoundedRangeModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import logfilter.Server;
import persistence.Preferences;

/**
 * This is the main window of the program
 *
 * @author cartin
 */
public class MainWindow extends JFrame
{

    /**
     * Creates new form MainWindow
     */
    public MainWindow()
    {
        initComponents();
	
	Rectangle r = Preferences.getInstance().getUIPreference(id);
	
	if(r != null)
	{
	    setBounds(r);
	}

	// Disable the auto scroll EDIT: Don't, we want to be able to write correctly.
//	((DefaultCaret) jTextAreaOutput.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

	// Set the listener for the scrollpane to enhance auto-scroll functionality
	jScrollPaneOutputText.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
	{
	    private int _val = 0;
	    private int _ext = 0;
	    private int _max = 0;

	    private final BoundedRangeModel _model = jScrollPaneOutputText.getVerticalScrollBar().getModel();

	    @Override
	    public void adjustmentValueChanged(AdjustmentEvent e)
	    {

		// Get the new max :
		int newMax = _model.getMaximum();

		// If the new max has changed and if we were scrolled to bottom :
		if (newMax != _max && (_val + _ext == _max))
		{

		    // Scroll to bottom :
		    _model.setValue(_model.getMaximum() - _model.getExtent());
		}

		// Save the new values :
		_val = _model.getValue();
		_ext = _model.getExtent();
		_max = _model.getMaximum();
	    }
	});

	// Remove Java icon in title bar and place a crappy looking custom one
	ImageIcon img = new ImageIcon(getClass().getResource("/images/Log_icon.png"));
	setIconImage(img.getImage());

	// Set the console to output on
	RemoteConsumerManager.getInstance().setConsole(jTextAreaOutput);

	// Initialise the connection manager
	connectionManager = ConnectionManager.getInstance();

	// Load current properties (server enabled, etc.)
	loadProperties();
    }

    private void loadProperties()
    {
	serverList = new ArrayList<>(Preferences.getInstance().getEnabledServers());

	// TODO: Do a delta instead for better performance and user friendliness
	new Thread()
	{
	    @Override
	    public void run()
	    {
		connectionManager.removeConnections();
		connectionManager.addConnections(serverList, Preferences.getInstance().getServerAccount());
	    }
	}.start();

	if (serverList.isEmpty())
	{
	    jTextFieldServersToMonitor.setText("No servers enabled");
	    jButtonConnect.setEnabled(false);
	    jButtonRefresh.setEnabled(false);
	}
	else
	{
	    String serversToDisplay = "";

	    for (int i = 0; i < serverList.size(); ++i)
	    {
		if (i > 0)
		{
		    serversToDisplay += ", ";
		}

		serversToDisplay += serverList.get(i).getName();
	    }

	    jTextFieldServersToMonitor.setText(serversToDisplay);

	    jButtonConnect.setEnabled(true);
	    jButtonRefresh.setEnabled(true);
	}
    }

    private void terminationCleanup()
    {
	// No need to cancel, since no prefs have been modified if we're here
	// For consistency, we save window location and size
	Preferences.getInstance().setUIPreference(id, getBounds());
	Preferences.getInstance().save();

    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jPanelRoot = new javax.swing.JPanel();
        jButtonConnect = new javax.swing.JButton();
        jScrollPaneOutputText = new javax.swing.JScrollPane();
        jTextAreaOutput = new javax.swing.JTextArea();
        jLabelServersToMonitor = new javax.swing.JLabel();
        jTextFieldServersToMonitor = new javax.swing.JTextField();
        jButtonRefresh = new javax.swing.JButton();
        jMenuBarMain = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuEdit = new javax.swing.JMenu();
        jMenuItemPreferences = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Log Monitor");
        setFocusTraversalPolicyProvider(true);
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                formWindowClosing(evt);
            }
        });

        jPanelRoot.setFocusable(false);

        jButtonConnect.setText("Connect");
        jButtonConnect.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonConnectActionPerformed(evt);
            }
        });

        jTextAreaOutput.setBackground(new java.awt.Color(0, 0, 0));
        jTextAreaOutput.setColumns(20);
        jTextAreaOutput.setForeground(new java.awt.Color(0, 255, 0));
        jTextAreaOutput.setLineWrap(true);
        jTextAreaOutput.setRows(5);
        jTextAreaOutput.setWrapStyleWord(true);
        jTextAreaOutput.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jTextAreaOutput.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyTyped(java.awt.event.KeyEvent evt)
            {
                jTextAreaOutputKeyTyped(evt);
            }
        });
        jScrollPaneOutputText.setViewportView(jTextAreaOutput);

        jLabelServersToMonitor.setLabelFor(jTextFieldServersToMonitor);
        jLabelServersToMonitor.setText("Servers to monitor:");
        jLabelServersToMonitor.setFocusable(false);

        jTextFieldServersToMonitor.setEditable(false);
        jTextFieldServersToMonitor.setToolTipText("Displays the server profiles that will be connected when you press connect");
        jTextFieldServersToMonitor.setFocusable(false);

        jButtonRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/refresh-icon.png"))); // NOI18N

        javax.swing.GroupLayout jPanelRootLayout = new javax.swing.GroupLayout(jPanelRoot);
        jPanelRoot.setLayout(jPanelRootLayout);
        jPanelRootLayout.setHorizontalGroup(
            jPanelRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelRootLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelRootLayout.createSequentialGroup()
                        .addComponent(jLabelServersToMonitor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldServersToMonitor, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonConnect, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonRefresh))
                    .addComponent(jScrollPaneOutputText))
                .addContainerGap())
        );
        jPanelRootLayout.setVerticalGroup(
            jPanelRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelRootLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanelRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonConnect)
                        .addComponent(jLabelServersToMonitor)
                        .addComponent(jTextFieldServersToMonitor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButtonRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneOutputText, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                .addContainerGap())
        );

        jMenuFile.setMnemonic(KeyEvent.VK_F);
        jMenuFile.setText("File");

        jMenuItemExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        jMenuItemExit.setText("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        jMenuBarMain.add(jMenuFile);

        jMenuEdit.setMnemonic(KeyEvent.VK_E);
        jMenuEdit.setText("Edit");

        jMenuItemPreferences.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.ALT_MASK));
        jMenuItemPreferences.setText("Preferences...");
        jMenuItemPreferences.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemPreferencesActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemPreferences);

        jMenuBarMain.add(jMenuEdit);

        jMenuHelp.setMnemonic(KeyEvent.VK_H);
        jMenuHelp.setText("Help");

        jMenuItem1.setText("About...");
        jMenuHelp.add(jMenuItem1);

        jMenuBarMain.add(jMenuHelp);

        setJMenuBar(jMenuBarMain);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelRoot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelRoot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemExitActionPerformed
    {//GEN-HEADEREND:event_jMenuItemExitActionPerformed
	// Clean up
	terminationCleanup();

	// Close the window
	dispose();
    }//GEN-LAST:event_jMenuItemExitActionPerformed

    private void jMenuItemPreferencesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemPreferencesActionPerformed
    {//GEN-HEADEREND:event_jMenuItemPreferencesActionPerformed
        new PreferencesDialog(MainWindow.this, true).showDialog();
	loadProperties();
//	new Thread()
//	{
//	    @Override
//	    public void run()
//	    {
//		new PreferencesWindow().setVisible(true);
//	    }
//	}.start();
    }//GEN-LAST:event_jMenuItemPreferencesActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
    {//GEN-HEADEREND:event_formWindowClosing
	terminationCleanup();
    }//GEN-LAST:event_formWindowClosing

    private void jButtonConnectActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonConnectActionPerformed
    {//GEN-HEADEREND:event_jButtonConnectActionPerformed
	jButtonConnect.setEnabled(false);

	new Thread()
	{
	    @Override
	    public void run()
	    {
		connectionManager.startConnections();
		connectionManager.executeCommands();
	    }
	}.start();
    }//GEN-LAST:event_jButtonConnectActionPerformed

    private void jTextAreaOutputKeyTyped(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTextAreaOutputKeyTyped
    {//GEN-HEADEREND:event_jTextAreaOutputKeyTyped
        // TODO: send every character to the STDOut of the ssh/telnet session (TEMPORARY)
    }//GEN-LAST:event_jTextAreaOutputKeyTyped

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonConnect;
    private javax.swing.JButton jButtonRefresh;
    private javax.swing.JLabel jLabelServersToMonitor;
    private javax.swing.JMenuBar jMenuBarMain;
    private javax.swing.JMenu jMenuEdit;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemPreferences;
    private javax.swing.JPanel jPanelRoot;
    private javax.swing.JScrollPane jScrollPaneOutputText;
    private javax.swing.JTextArea jTextAreaOutput;
    private javax.swing.JTextField jTextFieldServersToMonitor;
    // End of variables declaration//GEN-END:variables

    private final int id = 0;
    private ArrayList<Server> serverList;
    private ConnectionManager connectionManager;
}
