package ui;

import collections.Pair;
import connection.ConnectionManager;
import connection.RemoteConsumerManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoundedRangeModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
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

	Pair<Rectangle, Integer> temp = Preferences.getInstance().getUIPreference(id);
	if (temp != null)
	{
	    Rectangle r = temp.getKey();

	    if (r != null)
	    {
		setBounds(r);
		bounds = r;
		setExtendedState(temp.getValue());
	    }
	}
	else
	{
	    bounds = getBounds();
	}

	// Set the document filter to limit the number of lines
	((AbstractDocument) jTextPaneOutput.getDocument()).setDocumentFilter(new ConsoleDocumentFilter(jTextPaneOutput, Preferences.getInstance().getMaxNumLines()));

	jTextPaneOutput.getDocument().addDocumentListener(new DocumentListener()
	{

	    @Override
	    public void insertUpdate(DocumentEvent de)
	    {
		if (!isFocused() && Preferences.getInstance().isFlashTaskbar())
		{
		    try
		    {
			// Verify if the modification is a message from the server
			String modification = de.getDocument().getText(de.getOffset(), de.getLength());

			if (modification.contains("-------------------------------"))
			{
			    // If it is, then it is relevant enough to flash taskbar
			    toFront();
			}
		    }
		    catch (BadLocationException ex)
		    {
			Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
		    }
		}
	    }

	    @Override
	    public void removeUpdate(DocumentEvent de)
	    {
	    }

	    @Override
	    public void changedUpdate(DocumentEvent de)
	    {
	    }
	});

	// Disable the auto scroll EDIT: Don't, we want to be able to write correctly.
//	((DefaultCaret) jTextPaneOutput.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

//	jScrollPaneOutput.getVerticalScrollBar().getModel().setValue(jScrollPaneOutput.getVerticalScrollBar().getModel().getMaximum() - jScrollPaneOutput.getVerticalScrollBar().getModel().getExtent());

	// Set the listener for the scrollpane to enhance auto-scroll functionality
	jScrollPaneOutput.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
	{
	    private int _val = 0;
	    private int _ext = 0;
	    private int _max = 0;

	    private final BoundedRangeModel _model = jScrollPaneOutput.getVerticalScrollBar().getModel();

	    @Override
	    public void adjustmentValueChanged(AdjustmentEvent e)
	    {
//		((DefaultCaret) jTextPaneOutput.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

		// Get the new max :
		int newMax = _model.getMaximum();

		// If the new max has changed and if we were scrolled to bottom
		if (newMax != _max && (_val + _ext == _max))
		{
		    // Scroll to bottom
		    _model.setValue(_model.getMaximum() - _model.getExtent());
		}

		// Save the new values :
		_val = _model.getValue();
		_ext = _model.getExtent();
		_max = _model.getMaximum();

//		((DefaultCaret) jTextPaneOutput.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	    }
	});

	// Set the popup menu for right click on the terminal
	jPopupTerminal = new JPopupMenu();

	final JMenuItem jMenuSelectAll = new JMenuItem("Select all");
	final JMenuItem jMenuClear = new JMenuItem("Clear selection");
	final JMenuItem jMenuCopy = new JMenuItem("Copy");
	final JMenuItem jMenuPaste = new JMenuItem("Paste");

	jPopupTerminal.addPopupMenuListener(new PopupMenuListener()
	{

	    @Override
	    public void popupMenuWillBecomeVisible(PopupMenuEvent pme)
	    {
		try
		{
		    Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
		    Transferable t = c.getContents(null);
		    Object o = t.getTransferData(DataFlavor.stringFlavor);
		    String data = (String) t.getTransferData(DataFlavor.stringFlavor);

		    if (data == null || data.isEmpty())
		    {
			jMenuPaste.setEnabled(false);
		    }
		    else
		    {
			jMenuPaste.setEnabled(true);
		    }
		}
		catch (UnsupportedFlavorException | IOException ex)
		{
		    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
		}

		if (jTextPaneOutput.getSelectedText() == null || jTextPaneOutput.getSelectedText().isEmpty())
		{
		    jMenuClear.setText("Clear screen");
		    jMenuCopy.setEnabled(false);
		}
		else
		{
		    jMenuClear.setText("Clear selection");
		    jMenuCopy.setEnabled(true);
		}
	    }

	    @Override
	    public void popupMenuWillBecomeInvisible(PopupMenuEvent pme)
	    {
	    }

	    @Override
	    public void popupMenuCanceled(PopupMenuEvent pme)
	    {
	    }
	});

	jMenuSelectAll.addActionListener(new ActionListener()
	{
	    @Override
	    public void actionPerformed(ActionEvent ae)
	    {
		selectAll();
	    }
	});

	jMenuClear.addActionListener(new ActionListener()
	{
	    @Override
	    public void actionPerformed(ActionEvent ae)
	    {
		clearSelection();
	    }
	});

	jMenuCopy.addActionListener(new ActionListener()
	{
	    @Override
	    public void actionPerformed(ActionEvent ae)
	    {
		copy();
	    }
	});

	jMenuPaste.addActionListener(new ActionListener()
	{
	    @Override
	    public void actionPerformed(ActionEvent ae)
	    {
		paste();
	    }
	});

	jPopupTerminal.add(jMenuSelectAll);
	jPopupTerminal.add(jMenuClear);
	jPopupTerminal.add(jMenuCopy);
	jPopupTerminal.add(jMenuPaste);

	jTextPaneOutput.setComponentPopupMenu(jPopupTerminal);

	// Remove Java icon in title bar and place a crappy looking custom one
	ImageIcon img = new ImageIcon(getClass().getResource("/images/Log_icon.png"));
	setIconImage(img.getImage());

	// Set the console to output on
	RemoteConsumerManager.getInstance().setConsole(jTextPaneOutput);

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
	}

	// Update the maximum number of lines from the prefs in case it changed
	((AbstractDocument) jTextPaneOutput.getDocument()).setDocumentFilter(new ConsoleDocumentFilter(jTextPaneOutput, Preferences.getInstance().getMaxNumLines()));

	jTextPaneOutput.setFont(Preferences.getInstance().getTerminalFont());
	jTextPaneOutput.setForeground(Preferences.getInstance().getForegroundColor());
	jTextPaneOutput.setBackground(Preferences.getInstance().getBackgroundColor());

	if (Preferences.getInstance().isOutputToDisk())
	{
	    if (!persistence.Logger.getInstance().isOpen())
	    {
		persistence.Logger.getInstance().open(Preferences.getInstance().getLogPath());
	    }
	}
	else if (persistence.Logger.getInstance().isOpen())
	{
	    persistence.Logger.getInstance().close();
	}
    }

    private void terminationCleanup()
    {
	(new Thread()
	{
	    @Override
	    public void run()
	    {
		connectionManager.removeConnections();

		if (persistence.Logger.getInstance().isOpen())
		{
		    persistence.Logger.getInstance().close();
		}
	    }
	}).start();

	// No need to cancel, since no prefs have been modified if we're here
	//
	// For consistency, we save window location and size
	Preferences.getInstance().setUIPreference(id, bounds, getExtendedState());

	Preferences.getInstance().save();
    }

    private void selectAll()
    {
	jTextPaneOutput.setSelectionStart(0);
	jTextPaneOutput.setSelectionEnd(jTextPaneOutput.getText().length());
    }

    private void clearSelection()
    {
	if (jTextPaneOutput.getSelectedText() == null)
	{
	    jTextPaneOutput.setText("");
	    jTextPaneOutput.setSelectionStart(0);
	    jTextPaneOutput.setSelectionEnd(0);

	    // Reset scroll bar to minimum
//	    jScrollPaneOutput.getVerticalScrollBar().getModel().setValue(jScrollPaneOutput.getVerticalScrollBar().getModel().getMaximum() - jScrollPaneOutput.getVerticalScrollBar().getModel().getExtent());
	}
	else
	{
	    jTextPaneOutput.replaceSelection("");
	    jTextPaneOutput.setSelectionStart(jTextPaneOutput.getCaretPosition());
	    jTextPaneOutput.setSelectionEnd(jTextPaneOutput.getCaretPosition());
	}
    }

    private void copy()
    {
	StringSelection selection = new StringSelection(jTextPaneOutput.getSelectedText());
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	clipboard.setContents(selection, selection);
    }

    private void paste()
    {
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	Transferable t = clipboard.getContents(null);
	if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor))
	{
	    try
	    {
		Object o = t.getTransferData(DataFlavor.stringFlavor);
		if (jTextPaneOutput.getSelectedText() == null || jTextPaneOutput.getSelectedText().isEmpty())
		{
		    jTextPaneOutput.getDocument().insertString(jTextPaneOutput.getCaretPosition(), (String) o, null);
		}
		else
		{
		    jTextPaneOutput.replaceSelection((String) o);
		}
	    }
	    catch (UnsupportedFlavorException | IOException | BadLocationException ex)
	    {
		Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
    }

    private void disconnect()
    {
	// Re-enable the connect button if it was disabled
	if (jButtonDisconnect.isEnabled())
	{
	    try
	    {
		jButtonConnect.setEnabled(true);
		jButtonRefresh.setEnabled(false);
		jButtonDisconnect.setEnabled(false);

		Document doc = jTextPaneOutput.getDocument();
		doc.insertString(doc.getLength(), "\nDisconnected.\n\n", null);

//		new Thread(new Runnable()
//		{
//		    @Override
//		    public void run()
//		    {
			// Verify if there is a connection alive - kill it if so
			connectionManager.removeConnections();
			connectionManager.addConnections(serverList, Preferences.getInstance().getServerAccount());
//		    }
//		}).start();
	    }
	    catch (BadLocationException ex)
	    {
		Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
    }

    private void connect()
    {
	jButtonConnect.setEnabled(false);
	jButtonRefresh.setEnabled(true);
	jButtonDisconnect.setEnabled(true);

	try
	{
	    Document doc = jTextPaneOutput.getDocument();
	    doc.insertString(doc.getLength(), "\nInitiating connections...\n", null);
	}
	catch (BadLocationException ex)
	{
	    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
	}

	new Thread()
	{
	    @Override
	    public void run()
	    {
		if (connectionManager.startConnections())
		{
		    SwingUtilities.invokeLater(new Runnable()
		    {
			@Override
			public void run()
			{
			    try
			    {
				Document doc = jTextPaneOutput.getDocument();
				doc.insertString(doc.getLength(), "Connections successful!\n", null);

				doc.insertString(doc.getLength(), "Starting monitoring daemons...\n\n", null);
			    }
			    catch (BadLocationException ex)
			    {
				Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
			    }
			}
		    });

		    connectionManager.executeCommands();
		}
		else
		{
		    SwingUtilities.invokeLater(new Runnable()
		    {
			@Override
			public void run()
			{
			    try
			    {
				Document doc = jTextPaneOutput.getDocument();
				doc.insertString(doc.getLength(), "Connections failed...aborting\n\n", null);
			    }
			    catch (BadLocationException ex)
			    {
				Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
			    }
			}
		    });

		    disconnect();
		}
	    }
	}.start();
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
        jLabelServersToMonitor = new javax.swing.JLabel();
        jTextFieldServersToMonitor = new javax.swing.JTextField();
        jButtonRefresh = new javax.swing.JButton();
        jButtonDisconnect = new javax.swing.JButton();
        jScrollPaneOutput = new javax.swing.JScrollPane();
        jTextPaneOutput = new javax.swing.JTextPane();
        jMenuBarMain = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuEdit = new javax.swing.JMenu();
        jMenuItemPreferences = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Broadsoft Test Monitoring Tool");
        setFocusTraversalPolicyProvider(true);
        addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentMoved(java.awt.event.ComponentEvent evt)
            {
                formComponentMoved(evt);
            }
            public void componentResized(java.awt.event.ComponentEvent evt)
            {
                formComponentResized(evt);
            }
        });
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

        jLabelServersToMonitor.setLabelFor(jTextFieldServersToMonitor);
        jLabelServersToMonitor.setText("Servers to monitor:");
        jLabelServersToMonitor.setFocusable(false);

        jTextFieldServersToMonitor.setEditable(false);
        jTextFieldServersToMonitor.setToolTipText("Displays the server profiles that will be connected when you press connect");
        jTextFieldServersToMonitor.setFocusable(false);

        jButtonRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/refresh-icon.png"))); // NOI18N
        jButtonRefresh.setToolTipText("Forces a verification of newer versions of the log files being monitored");
        jButtonRefresh.setEnabled(false);
        jButtonRefresh.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonRefreshActionPerformed(evt);
            }
        });

        jButtonDisconnect.setText("Disconnect");
        jButtonDisconnect.setEnabled(false);
        jButtonDisconnect.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonDisconnectActionPerformed(evt);
            }
        });

        jTextPaneOutput.setBackground(new java.awt.Color(0, 0, 0));
        jTextPaneOutput.setForeground(new java.awt.Color(0, 255, 0));
        jTextPaneOutput.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyPressed(java.awt.event.KeyEvent evt)
            {
                jTextPaneOutputKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt)
            {
                jTextPaneOutputKeyTyped(evt);
            }
        });
        jScrollPaneOutput.setViewportView(jTextPaneOutput);

        javax.swing.GroupLayout jPanelRootLayout = new javax.swing.GroupLayout(jPanelRoot);
        jPanelRoot.setLayout(jPanelRootLayout);
        jPanelRootLayout.setHorizontalGroup(
            jPanelRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelRootLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPaneOutput)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelRootLayout.createSequentialGroup()
                        .addComponent(jLabelServersToMonitor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldServersToMonitor, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonConnect)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonDisconnect)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonRefresh)))
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
                        .addComponent(jTextFieldServersToMonitor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonDisconnect))
                    .addComponent(jButtonRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPaneOutput, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
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
        jMenuItem1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem1ActionPerformed(evt);
            }
        });
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
	disconnect();

	new PreferencesDialog(MainWindow.this, true).showDialog();

	loadProperties();
    }//GEN-LAST:event_jMenuItemPreferencesActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
    {//GEN-HEADEREND:event_formWindowClosing
	terminationCleanup();
    }//GEN-LAST:event_formWindowClosing

    private void jButtonConnectActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonConnectActionPerformed
    {//GEN-HEADEREND:event_jButtonConnectActionPerformed
	connect();
    }//GEN-LAST:event_jButtonConnectActionPerformed

    private void jButtonRefreshActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonRefreshActionPerformed
    {//GEN-HEADEREND:event_jButtonRefreshActionPerformed
	connectionManager.restartConnections();
    }//GEN-LAST:event_jButtonRefreshActionPerformed

    private void formComponentMoved(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_formComponentMoved
    {//GEN-HEADEREND:event_formComponentMoved
	if (getExtendedState() == JFrame.NORMAL)
	{
	    bounds = getBounds();
	}
    }//GEN-LAST:event_formComponentMoved

    private void formComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_formComponentResized
    {//GEN-HEADEREND:event_formComponentResized
	if (getExtendedState() == JFrame.NORMAL)
	{
	    bounds = getBounds();
	}
    }//GEN-LAST:event_formComponentResized

    private void jButtonDisconnectActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonDisconnectActionPerformed
    {//GEN-HEADEREND:event_jButtonDisconnectActionPerformed
	disconnect();
    }//GEN-LAST:event_jButtonDisconnectActionPerformed

    private void jTextPaneOutputKeyTyped(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTextPaneOutputKeyTyped
    {//GEN-HEADEREND:event_jTextPaneOutputKeyTyped
	
    }//GEN-LAST:event_jTextPaneOutputKeyTyped

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem1ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem1ActionPerformed
	new AboutDialog(this, true).setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jTextPaneOutputKeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTextPaneOutputKeyPressed
    {//GEN-HEADEREND:event_jTextPaneOutputKeyPressed
        // Small hack to make the writing work WITH the custom auto scrolling
//	DefaultCaret caret = (DefaultCaret) jTextPaneOutput.getCaret();
//	caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }//GEN-LAST:event_jTextPaneOutputKeyPressed

    /**
     * This class represents a filter for the number of lines to display in the
     * console output.
     */
    public class ConsoleDocumentFilter extends DocumentFilter
    {
	private JTextPane console;
	private final int max;

	public ConsoleDocumentFilter(JTextPane console, int max)
	{
	    this.console = jTextPaneOutput;
	    this.max = max;
	}

	@Override
	public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
	{
	    super.replace(fb, offset, length, text, attrs);
	    int lines = console.getDocument().getDefaultRootElement().getElementCount();

	    if (lines > max)
	    {
		int linesToRemove = lines - max;

		Element map = console.getDocument().getDefaultRootElement();
		Element lineElem = map.getElement(linesToRemove);

		int lengthToRemove = lineElem.getStartOffset();

		((DefaultCaret) jTextPaneOutput.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		super.remove(fb, 0, lengthToRemove);
		((DefaultCaret) jTextPaneOutput.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

//		jScrollPaneOutput.getVerticalScrollBar().getAdjustmentListeners()[0].adjustmentValueChanged(null);
	    }
	}

	@Override
	public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr)
	{
	    try
	    {
		super.insertString(fb, offset, string, attr);
		int lines = console.getDocument().getDefaultRootElement().getElementCount();

		if (lines > max)
		{
		    int linesToRemove = lines - max;

		    Element map = console.getDocument().getDefaultRootElement();
		    Element lineElem = map.getElement(linesToRemove);

		    int lengthToRemove = lineElem.getStartOffset();

		    ((DefaultCaret) jTextPaneOutput.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		    super.remove(fb, 0, lengthToRemove);
		    ((DefaultCaret) jTextPaneOutput.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

//		    jScrollPaneOutput.getVerticalScrollBar().getAdjustmentListeners()[0].adjustmentValueChanged(null);
		}
	    }
	    catch (BadLocationException ex)
	    {
		Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}

//	@Override
//	public void remove(FilterBypass fb, int offset, int length)
//	{
//	    try
//	    {
//		super.remove(fb, id, id);
//
//		jScrollPaneOutput.getVerticalScrollBar().getAdjustmentListeners()[0].adjustmentValueChanged(null);
//	    }
//	    catch (BadLocationException ex)
//	    {
//		Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
//	    }
//	}
    }

    public static void writeToConsole(String text)
    {
	if (jTextPaneOutput != null)
	{
	    try
	    {
		Document doc = jTextPaneOutput.getStyledDocument();
		doc.insertString(doc.getLength(), text, null);
	    }
	    catch (BadLocationException ex)
	    {
		Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonConnect;
    private javax.swing.JButton jButtonDisconnect;
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
    private javax.swing.JScrollPane jScrollPaneOutput;
    private javax.swing.JTextField jTextFieldServersToMonitor;
    private static javax.swing.JTextPane jTextPaneOutput;
    // End of variables declaration//GEN-END:variables

    private final int id = 0;
    private ArrayList<Server> serverList;
    private ConnectionManager connectionManager;
    private Rectangle bounds;
    private JPopupMenu jPopupTerminal;
}
