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
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DocumentFilter;
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
	((AbstractDocument) jTextAreaOutput.getDocument()).setDocumentFilter(new ConsoleDocumentFilter(jTextAreaOutput, Preferences.getInstance().getMaxNumLines()));

	jTextAreaOutput.getDocument().addDocumentListener(new DocumentListener()
	{

	    @Override
	    public void insertUpdate(DocumentEvent de)
	    {
		if (!isFocused() && Preferences.getInstance().isFlashTaskbar())
		{
		    toFront();
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
	((DefaultCaret) jTextAreaOutput.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
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
		((DefaultCaret) jTextAreaOutput.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

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

		if (jTextAreaOutput.getSelectedText() == null || jTextAreaOutput.getSelectedText().isEmpty())
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

	jTextAreaOutput.setComponentPopupMenu(jPopupTerminal);

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
	}

	// Update the maximum number of lines from the prefs in case it changed
	((AbstractDocument) jTextAreaOutput.getDocument()).setDocumentFilter(new ConsoleDocumentFilter(jTextAreaOutput, Preferences.getInstance().getMaxNumLines()));

	jTextAreaOutput.setFont(Preferences.getInstance().getTerminalFont());
	jTextAreaOutput.setForeground(Preferences.getInstance().getForegroundColor());
	jTextAreaOutput.setBackground(Preferences.getInstance().getBackgroundColor());
    }

    private void terminationCleanup()
    {
	(new Thread()
	{
	    @Override
	    public void run()
	    {
		connectionManager.removeConnections();
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
	jTextAreaOutput.setSelectionStart(0);
	jTextAreaOutput.setSelectionEnd(jTextAreaOutput.getText().length());
    }

    private void clearSelection()
    {
	if (jTextAreaOutput.getSelectedText() == null)
	{
	    jTextAreaOutput.setText("");
	    jTextAreaOutput.setSelectionStart(0);
	    jTextAreaOutput.setSelectionEnd(0);
	}
	else
	{
	    jTextAreaOutput.replaceSelection("");
	    jTextAreaOutput.setSelectionStart(jTextAreaOutput.getCaretPosition());
	    jTextAreaOutput.setSelectionEnd(jTextAreaOutput.getCaretPosition());
	}
    }

    private void copy()
    {
	StringSelection selection = new StringSelection(jTextAreaOutput.getSelectedText());
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
		if (jTextAreaOutput.getSelectedText() == null || jTextAreaOutput.getSelectedText().isEmpty())
		{
		    jTextAreaOutput.insert((String) o, jTextAreaOutput.getCaretPosition());
		}
		else
		{
		    jTextAreaOutput.replaceSelection((String) o);
		}
	    }
	    catch (UnsupportedFlavorException | IOException ex)
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
	    jButtonConnect.setEnabled(true);
	    jButtonRefresh.setEnabled(false);
	    jButtonDisconnect.setEnabled(false);
	    jTextAreaOutput.append("\n\nDisconnected.\n\n");

	    // Verify if there is a connection alive - kill it if so
	    connectionManager.removeConnections();
	    jTextAreaOutput.append("\nMonitoring daemon stopped for:\n");
	}
    }

    private void connect()
    {
	jButtonConnect.setEnabled(false);
	jButtonRefresh.setEnabled(true);
	jButtonDisconnect.setEnabled(true);

	jTextAreaOutput.append("\nInitiating connections...\n");

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
			    jTextAreaOutput.append("Connections successful!\n");

			    jTextAreaOutput.append("Starting monitoring daemons...\n\n");

			    jTextAreaOutput.append("Monitoring daemon started for:\n");
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
			    jTextAreaOutput.append("Connections failed...aborting\n\n");
			}
		    });

		    connectionManager.removeConnections();
		    connectionManager.addConnections(serverList, Preferences.getInstance().getServerAccount());
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
        jScrollPaneOutputText = new javax.swing.JScrollPane();
        jTextAreaOutput = new javax.swing.JTextArea();
        jLabelServersToMonitor = new javax.swing.JLabel();
        jTextFieldServersToMonitor = new javax.swing.JTextField();
        jButtonRefresh = new javax.swing.JButton();
        jButtonDisconnect = new javax.swing.JButton();
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
                        .addComponent(jTextFieldServersToMonitor, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonConnect)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonDisconnect)
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
                        .addComponent(jTextFieldServersToMonitor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonDisconnect))
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

    private void jTextAreaOutputKeyTyped(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTextAreaOutputKeyTyped
    {//GEN-HEADEREND:event_jTextAreaOutputKeyTyped
//        Point newPosition = jTextAreaOutput.getLocation();
//
//	if(evt.getK)
//
//	jTextAreaOutput.getCaret().setMagicCaretPosition();

	// Small hack to make the writing work WITH the custom auto scrolling
	DefaultCaret caret = (DefaultCaret) jTextAreaOutput.getCaret();
	caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }//GEN-LAST:event_jTextAreaOutputKeyTyped

    private void jButtonDisconnectActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonDisconnectActionPerformed
    {//GEN-HEADEREND:event_jButtonDisconnectActionPerformed
        disconnect();
    }//GEN-LAST:event_jButtonDisconnectActionPerformed

    /**
     * This class represents a filter for the number of lines to display in the
     * console output.
     */
    public class ConsoleDocumentFilter extends DocumentFilter
    {
	private JTextArea console;
	private int max;

	public ConsoleDocumentFilter(JTextArea console, int max)
	{
	    this.console = console;
	    this.max = max;
	}

	@Override
	public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
	{
	    super.replace(fb, offset, length, text, attrs);
	    int lines = console.getLineCount();

	    if (lines > max)
	    {
		int linesToRemove = lines - max - 1;
		int lengthToRemove = console.getLineStartOffset(linesToRemove);
		remove(fb, 0, lengthToRemove);
	    }
	}

	@Override
	public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr)
	{
	    try
	    {
		super.insertString(fb, offset, string, attr);
		int lines = console.getLineCount();

		if (lines > max)
		{
		    int linesToRemove = lines - max - 1;
		    int lengthToRemove = console.getLineStartOffset(linesToRemove);
		    remove(fb, 0, lengthToRemove);
		}
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
    private javax.swing.JScrollPane jScrollPaneOutputText;
    private javax.swing.JTextArea jTextAreaOutput;
    private javax.swing.JTextField jTextFieldServersToMonitor;
    // End of variables declaration//GEN-END:variables

    private final int id = 0;
    private ArrayList<Server> serverList;
    private ConnectionManager connectionManager;
    private Rectangle bounds;
    private JPopupMenu jPopupTerminal;
}
