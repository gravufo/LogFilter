package ui;

import collections.Pair;
import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import logfilter.Filter;
import logfilter.Log;
import logfilter.Server;
import persistence.Preferences;

/**
 * This class is represents the Preferences dialog and all its logic
 *
 * @author cartin
 */
public class PreferencesDialog extends javax.swing.JDialog
{

    /**
     * Creates new Preferences dialog
     *
     * @param parent
     * @param modal
     */
    public PreferencesDialog(JFrame parent, boolean modal)
    {
	super(parent, modal);

	templateLogFilesListModel = new DefaultListModel();
	serverListModel = new DefaultListModel();

	serverLogFilesListModel = new DefaultListModel();
	logFileFilterModel = new DefaultListModel();

	alarmFilterListModel = new DefaultListModel();

	initComponents();

	jListServers.setModel(serverListModel);
	jListLogFiles.setModel(serverLogFilesListModel);
	jListTemplateLogFiles.setModel(templateLogFilesListModel);
	jListLogFileFilters.setModel(logFileFilterModel);
	jListAlarmFilters.setModel(alarmFilterListModel);

	Pair<Rectangle, Integer> temp = Preferences.getInstance().getUIPreference(id);
	if (temp != null)
	{
	    Rectangle r = temp.getKey();

	    if (r != null)
	    {
		setBounds(r);
	    }
	}
	else
	{
	    setLocationRelativeTo(parent);
	}

	// Load the saved values (servers, logs, properties, etc.)
	// Make sure they exist before doing so. Else, load defaults
	for (String s : Preferences.getInstance().getServerMap().keySet())
	{
	    serverListModel.addElement(s);
	}

	sortList(jListServers, ListComparator.Type.SERVER);

	for (String s : Preferences.getInstance().getLogMap().keySet())
	{
	    templateLogFilesListModel.addElement(s);
	}

	sortList(jListTemplateLogFiles, ListComparator.Type.LOG);

	jListLogFileFilters.setCellRenderer(new LogFileFilterCellRenderer());
	jListServers.setCellRenderer(new ServerCellRenderer());

	jListLogFileFilters.setSelectedIndex(0);
	jListLogFiles.setSelectedIndex(0);
	jListServers.setSelectedIndex(0);
	jListTemplateLogFiles.setSelectedIndex(0);
	jListAlarmFilters.setSelectedIndex(0);

	// Set the escape character to close the dialog
	ActionListener escListener = new ActionListener()
	{
	    @Override
	    public void actionPerformed(ActionEvent e)
	    {
		cancel();
	    }
	};

	getRootPane().registerKeyboardAction(escListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

	// Make CTRL-TAB and CTRL-SHIFT-TAB work
	setupTabTraversalKeys(jTabbedPaneRoot);

	loadMiscellaneous();
    }

    public void showDialog()
    {
	setVisible(true);

//	ArrayList<String> list = new ArrayList<>();
//	Enumeration<String> serverList = serverListModel.elements();
//	while(serverList.hasMoreElements())
//	{
//	    String currentServer = serverList.nextElement();
//	    if(Preferences.getInstance().getServer(currentServer).isEnabled())
//		list.add(currentServer);
//	}
//	return list;
    }

    private void loadServerProperties()
    {
	if (serverListModel.isEmpty() || jListServers.getSelectedIndex() == -1)
	{
	    jButtonAddLogFile.setEnabled(false);
	    jButtonEditServer.setEnabled(false);
	    jButtonRemoveServer.setEnabled(false);
	    jCheckBoxLogMonitoringEnabled.setEnabled(false);
	    jCheckBoxAlarmMonitoring.setEnabled(false);
	    jRadioButtonServerConnectionSSH.setEnabled(false);
	    jRadioButtonServerConnectionTelnet.setEnabled(false);
	    jListLogFiles.setEnabled(false);

	    jCheckBoxLogMonitoringEnabled.setSelected(false);
	    jCheckBoxAlarmMonitoring.setSelected(false);
	    jRadioButtonServerConnectionSSH.setSelected(false);
	    jRadioButtonServerConnectionTelnet.setSelected(false);

	    serverLogFilesListModel.clear();
	}
	else
	{
	    Server server = Preferences.getInstance().getServer((String) jListServers.getSelectedValue());

	    jCheckBoxLogMonitoringEnabled.setSelected(server.isMonitorLogs());
	    jCheckBoxAlarmMonitoring.setSelected(server.isMonitorAlarms());
	    jRadioButtonServerConnectionSSH.setSelected(server.isUseSSH());
	    jRadioButtonServerConnectionTelnet.setSelected(!server.isUseSSH());

	    jListLogFiles.setEnabled(false);

	    serverLogFilesListModel.clear();

	    for (String s : server.getLogList())
	    {
		serverLogFilesListModel.addElement(s);
	    }

	    sortList(jListLogFiles, ListComparator.Type.LOG);

	    jButtonAddLogFile.setEnabled(true);
	    jButtonEditServer.setEnabled(true);
	    jButtonRemoveServer.setEnabled(true);

	    if (server.getLogList().isEmpty())
	    {
		jCheckBoxLogMonitoringEnabled.setEnabled(false);
	    }
	    else
	    {
		jCheckBoxLogMonitoringEnabled.setEnabled(true);
	    }

	    jCheckBoxAlarmMonitoring.setEnabled(true);
	    jRadioButtonServerConnectionSSH.setEnabled(true);
	    jRadioButtonServerConnectionTelnet.setEnabled(true);
	    jListLogFiles.setEnabled(true);
	}
    }

    private void loadLogTemplateProperties()
    {
	if (templateLogFilesListModel.isEmpty() || jListTemplateLogFiles.getSelectedIndex() == -1)
	{
	    jTextFieldLogFileName.setEnabled(false);
	    jTextFieldLogFilePrefix.setEnabled(false);
	    jTextFieldLogFilePath.setEnabled(false);
	    jButtonLogFileFilterAdd.setEnabled(false);
	    jButtonTemplateLogFileRemove.setEnabled(false);

	    jTextFieldLogFileName.setText("");
	    jTextFieldLogFilePrefix.setText("");
	    jTextFieldLogFilePath.setText("");

	    jListLogFileFilters.setEnabled(false);
	    logFileFilterModel.clear();
	}
	else
	{
	    Log log = Preferences.getInstance().getLog((String) jListTemplateLogFiles.getSelectedValue());

	    jTextFieldLogFileName.setEnabled(true);
	    jTextFieldLogFilePrefix.setEnabled(true);
	    jTextFieldLogFilePath.setEnabled(true);
	    jButtonLogFileFilterAdd.setEnabled(true);
	    jButtonTemplateLogFileRemove.setEnabled(true);

	    jTextFieldLogFileName.setText(log.getName());
	    jTextFieldLogFilePrefix.setText(log.getNamePrefix());
	    jTextFieldLogFilePath.setText(log.getFilePath());

	    jListLogFileFilters.setEnabled(true);
	    logFileFilterModel.clear();

	    for (String s : log.getFilterMap().keySet())
	    {
		logFileFilterModel.addElement(s);
	    }

	    sortList(jListLogFileFilters, ListComparator.Type.FILTER);

	    loadLogFileFiltersProperties();
	}
    }

    private void loadLogFileFiltersProperties()
    {
	if (logFileFilterModel.isEmpty() || jListLogFileFilters.getSelectedIndex() == -1)
	{
	    jCheckBoxLogFileFilterEnabled.setEnabled(false);
	    jTextFieldLogFileFilterName.setEnabled(false);
	    jTextFieldLogFileFilterKeyword.setEnabled(false);
	    jSpinnerLogFileFilterPrePrint.setEnabled(false);
	    jSpinnerLogFileFilterPostPrint.setEnabled(false);

	    jButtonLogFileFilterRemove.setEnabled(false);

	    jCheckBoxLogFileFilterEnabled.setSelected(false);
	    jTextFieldLogFileFilterName.setText("");
	    jTextFieldLogFileFilterKeyword.setText("");
	    jSpinnerLogFileFilterPrePrint.setValue(0);
	    jSpinnerLogFileFilterPostPrint.setValue(0);

	    jColorFieldHighlight.setBackground(Color.WHITE);
	}
	else
	{
	    Log log = Preferences.getInstance().getLog((String) jListTemplateLogFiles.getSelectedValue());
	    Filter filter = log.getFilter((String) jListLogFileFilters.getSelectedValue());

	    jCheckBoxLogFileFilterEnabled.setEnabled(true);
	    jTextFieldLogFileFilterName.setEnabled(true);
	    jTextFieldLogFileFilterKeyword.setEnabled(true);
	    jSpinnerLogFileFilterPrePrint.setEnabled(true);
	    jSpinnerLogFileFilterPostPrint.setEnabled(true);

	    jButtonLogFileFilterRemove.setEnabled(true);

	    jCheckBoxLogFileFilterEnabled.setSelected(filter.isEnabled());
	    jTextFieldLogFileFilterName.setText(filter.getName());
	    jTextFieldLogFileFilterKeyword.setText(filter.getKeyword());
	    jSpinnerLogFileFilterPrePrint.setValue(filter.getMessagesBefore());
	    jSpinnerLogFileFilterPostPrint.setValue(filter.getMessagesAfter());

	    jColorFieldHighlight.setBackground(filter.getHighlightColor());
	}
    }

    private void loadServerLogProperties()
    {
	if (serverLogFilesListModel.isEmpty())
	{
	    if (jListServers.getSelectedIndex() != -1)
	    {
		jCheckBoxLogMonitoringEnabled.setEnabled(false);
		Preferences.getInstance().getServer((String) jListServers.getSelectedValue()).setMonitorLogs(false);
		jCheckBoxLogMonitoringEnabled.setSelected(false);
	    }

	    jButtonRemoveLogFile.setEnabled(false);
	}
	else
	{
	    if (jListServers.getSelectedIndex() != -1)
	    {
		jCheckBoxLogMonitoringEnabled.setEnabled(true);
	    }

	    jButtonRemoveLogFile.setEnabled(true);
	}
    }

    private void loadMiscellaneous()
    {
	jTextFieldServerUsername.setText(Preferences.getInstance().getServerAccount().getUserName());
	jPasswordFieldServerPassword.setText(String.valueOf(Preferences.getInstance().getServerAccount().getPassword()));
	jSpinnerMaxNumLines.setValue(Preferences.getInstance().getMaxNumLines());
	jSpinnerAutoRefresh.setValue(Preferences.getInstance().getRefreshInterval());
	jColorFieldBackground.setBackground(Preferences.getInstance().getBackgroundColor());
	jColorFieldForeground.setBackground(Preferences.getInstance().getForegroundColor());
	jCheckBoxLogToDisk.setSelected(Preferences.getInstance().isOutputToDisk());
	jCheckBoxFlashTaskbar.setSelected(Preferences.getInstance().isFlashTaskbar());

	// Load the Alarm filters
	Iterator<String> it = Preferences.getInstance().getAlarmFiltersList().iterator();
	while (it.hasNext())
	{
	    alarmFilterListModel.addElement(it.next());
	}
    }

    private void ensureConsistency()
    {
	jTextFieldLogFileFilterName.getFocusListeners()[0].focusLost(null);
	jTextFieldLogFileFilterKeyword.getFocusListeners()[0].focusLost(null);
	jTextFieldLogFileName.getFocusListeners()[0].focusLost(null);
	jTextFieldLogFilePath.getFocusListeners()[0].focusLost(null);
	jTextFieldLogFilePrefix.getFocusListeners()[0].focusLost(null);
	jTextFieldServerUsername.getFocusListeners()[0].focusLost(null);
	jPasswordFieldServerPassword.getFocusListeners()[0].focusLost(null);
    }

    private void cancel()
    {
	// Cancel changes before saving window prefs
	Preferences.getInstance().cancel();

	// For consistency, we save window location and size
	Preferences.getInstance().setUIPreference(id, getBounds(), 0);
	Preferences.getInstance().save();

	// No need to save, so we just close pref window
	dispose();
    }

    private void sortList(JList list, ListComparator.Type type)
    {
	list.setEnabled(false);

	DefaultListModel model = (DefaultListModel) list.getModel();
	String name = (String) list.getSelectedValue();

	ListComparator lc = new ListComparator(type);
	TreeSet<String> sortedSet = new TreeSet<>(lc);
	sortedSet.addAll(new ArrayList<>(Collections.list(model.elements())));

	model.clear();

	for (String s : sortedSet)
	{
	    model.addElement(s);
	}

	list.setSelectedValue(name, true);

	list.setEnabled(true);
    }

    private static void setupTabTraversalKeys(JTabbedPane tabbedPane)
    {
	KeyStroke ctrlTab = KeyStroke.getKeyStroke("ctrl TAB");
	KeyStroke ctrlShiftTab = KeyStroke.getKeyStroke("ctrl shift TAB");

	// Remove ctrl-tab from normal focus traversal
	Set<AWTKeyStroke> forwardKeys = new HashSet<>(tabbedPane.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
	forwardKeys.remove(ctrlTab);
	tabbedPane.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);

	// Remove ctrl-shift-tab from normal focus traversal
	Set<AWTKeyStroke> backwardKeys = new HashSet<>(tabbedPane.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
	backwardKeys.remove(ctrlShiftTab);
	tabbedPane.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);

	// Add keys to the tab's input map
	InputMap inputMap = tabbedPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	inputMap.put(ctrlTab, "navigateNext");
	inputMap.put(ctrlShiftTab, "navigatePrevious");
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

        buttonGroupServerProperties = new javax.swing.ButtonGroup();
        jTabbedPaneRoot = new javax.swing.JTabbedPane();
        jPanelServersAndLogs = new javax.swing.JPanel();
        jScrollPaneServers = new javax.swing.JScrollPane();
        jListServers = new javax.swing.JList();
        jLabelServers = new javax.swing.JLabel();
        jLabelLogFiles = new javax.swing.JLabel();
        jScrollPaneLogFiles = new javax.swing.JScrollPane();
        jListLogFiles = new javax.swing.JList();
        jButtonAddServer = new javax.swing.JButton();
        jButtonEditServer = new javax.swing.JButton();
        jButtonRemoveServer = new javax.swing.JButton();
        jButtonAddLogFile = new javax.swing.JButton();
        jButtonRemoveLogFile = new javax.swing.JButton();
        jLabelServerProperties = new javax.swing.JLabel();
        jPanelServerProperties = new javax.swing.JPanel();
        jRadioButtonServerConnectionTelnet = new javax.swing.JRadioButton();
        jCheckBoxLogMonitoringEnabled = new javax.swing.JCheckBox();
        jRadioButtonServerConnectionSSH = new javax.swing.JRadioButton();
        jLabelServerConnectionProtocol = new javax.swing.JLabel();
        jSeparatorServerConnectionProtocol = new javax.swing.JSeparator();
        jLabelLogMonitoring = new javax.swing.JLabel();
        jSeparatorServerMonitoring = new javax.swing.JSeparator();
        jLabelAlarmMonitoring = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jCheckBoxAlarmMonitoring = new javax.swing.JCheckBox();
        jPanelTemplateLogFiles = new javax.swing.JPanel();
        jScrollPaneTemplateLogFiles = new javax.swing.JScrollPane();
        jListTemplateLogFiles = new javax.swing.JList();
        jLabelTemplateLogFiles = new javax.swing.JLabel();
        jPanelTemplateLogFileProperties = new javax.swing.JPanel();
        jLabelTemplateLogFileName = new javax.swing.JLabel();
        jLabelTemplateLogFilePath = new javax.swing.JLabel();
        jTextFieldLogFilePath = new javax.swing.JTextField();
        jTextFieldLogFilePrefix = new javax.swing.JTextField();
        jLabelLogFileDescription = new javax.swing.JLabel();
        jSeparatorLogFileDescription = new javax.swing.JSeparator();
        jLabelLogFileFilters = new javax.swing.JLabel();
        jSeparatorLogFileFilters = new javax.swing.JSeparator();
        jScrollPaneLogFileFilters = new javax.swing.JScrollPane();
        jListLogFileFilters = new javax.swing.JList();
        jButtonLogFileFilterAdd = new javax.swing.JButton();
        jButtonLogFileFilterRemove = new javax.swing.JButton();
        jPanelFilterProperties = new javax.swing.JPanel();
        jCheckBoxLogFileFilterEnabled = new javax.swing.JCheckBox();
        jLabelLogFileFilterPostPrint = new javax.swing.JLabel();
        jLabelLogFileFilterPrePrint = new javax.swing.JLabel();
        jSpinnerLogFileFilterPrePrint = new javax.swing.JSpinner();
        jSpinnerLogFileFilterPostPrint = new javax.swing.JSpinner();
        jLabelLogFileFilterKeyword = new javax.swing.JLabel();
        jTextFieldLogFileFilterKeyword = new javax.swing.JTextField();
        jLabelLogFileFilterName = new javax.swing.JLabel();
        jTextFieldLogFileFilterName = new javax.swing.JTextField();
        jLabelHighlightColor = new javax.swing.JLabel();
        jColorFieldHighlight = new javax.swing.JTextField();
        jButtonHighlightColor = new javax.swing.JButton();
        jLabelLogFileAlias = new javax.swing.JLabel();
        jTextFieldLogFileName = new javax.swing.JTextField();
        jButtonTemplateLogFileAdd = new javax.swing.JButton();
        jButtonTemplateLogFileRemove = new javax.swing.JButton();
        jPanelMisc = new javax.swing.JPanel();
        jLabelCredentials = new javax.swing.JLabel();
        jSeparatorCredentials = new javax.swing.JSeparator();
        jLabelServerUsername = new javax.swing.JLabel();
        jTextFieldServerUsername = new javax.swing.JTextField();
        jLabelServerPassword = new javax.swing.JLabel();
        jPasswordFieldServerPassword = new javax.swing.JPasswordField();
        jLabelTerminalOptions = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabelMaxNumLines = new javax.swing.JLabel();
        jSpinnerMaxNumLines = new javax.swing.JSpinner();
        jLabelTerminalForegroundColor = new javax.swing.JLabel();
        jLabelTerminalBackgroundColor = new javax.swing.JLabel();
        jButtonTerminalForegroundColor = new javax.swing.JButton();
        jButtonTerminalBackgroundColor = new javax.swing.JButton();
        jCheckBoxFlashTaskbar = new javax.swing.JCheckBox();
        jLabelFont = new javax.swing.JLabel();
        jButtonFont = new javax.swing.JButton();
        jLabelAutoRefresh = new javax.swing.JLabel();
        jSpinnerAutoRefresh = new javax.swing.JSpinner();
        jColorFieldForeground = new javax.swing.JTextField();
        jColorFieldBackground = new javax.swing.JTextField();
        jCheckBoxLogToDisk = new javax.swing.JCheckBox();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jLabelAlarmFilters = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListAlarmFilters = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaAlarmFilters = new javax.swing.JTextArea();
        jTextFieldAlarmFilters = new javax.swing.JTextField();
        jButtonAlarmFilterRemove = new javax.swing.JButton();
        jButtonAlarmFilterAdd = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jButtonOK = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Preferences");
        setFocusTraversalPolicyProvider(true);
        setIconImage(null);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                formWindowClosing(evt);
            }
        });

        jTabbedPaneRoot.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jTabbedPaneRootStateChanged(evt);
            }
        });

        jPanelServersAndLogs.setFocusable(false);
        jPanelServersAndLogs.setNextFocusableComponent(jListServers);

        jListServers.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListServers.setToolTipText("This list contains all the servers added to the monitoring list, but not necessarily activated.");
        jListServers.setNextFocusableComponent(jButtonAddServer);
        jListServers.setRequestFocusEnabled(false);
        jListServers.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                jListServersValueChanged(evt);
            }
        });
        jScrollPaneServers.setViewportView(jListServers);

        jLabelServers.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelServers.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelServers.setText("Servers / Profiles");
        jLabelServers.setFocusable(false);

        jLabelLogFiles.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelLogFiles.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelLogFiles.setText("Log Files");
        jLabelLogFiles.setFocusable(false);

        jListLogFiles.setToolTipText("This list contains the log files to monitor on the selected server.");
        jListLogFiles.setEnabled(false);
        jListLogFiles.setNextFocusableComponent(jButtonAddLogFile);
        jListLogFiles.setRequestFocusEnabled(false);
        jListLogFiles.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                jListLogFilesValueChanged(evt);
            }
        });
        jScrollPaneLogFiles.setViewportView(jListLogFiles);

        jButtonAddServer.setText("Add...");
        jButtonAddServer.setNextFocusableComponent(jButtonEditServer);
        jButtonAddServer.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonAddServerActionPerformed(evt);
            }
        });

        jButtonEditServer.setText("Edit...");
        jButtonEditServer.setEnabled(false);
        jButtonEditServer.setNextFocusableComponent(jButtonRemoveServer);
        jButtonEditServer.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonEditServerActionPerformed(evt);
            }
        });

        jButtonRemoveServer.setText("Remove");
        jButtonRemoveServer.setEnabled(false);
        jButtonRemoveServer.setNextFocusableComponent(jCheckBoxLogMonitoringEnabled);
        jButtonRemoveServer.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonRemoveServerActionPerformed(evt);
            }
        });

        jButtonAddLogFile.setText("Add...");
        jButtonAddLogFile.setEnabled(false);
        jButtonAddLogFile.setNextFocusableComponent(jButtonRemoveLogFile);
        jButtonAddLogFile.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonAddLogFileActionPerformed(evt);
            }
        });

        jButtonRemoveLogFile.setText("Remove");
        jButtonRemoveLogFile.setEnabled(false);
        jButtonRemoveLogFile.setNextFocusableComponent(jButtonOK);
        jButtonRemoveLogFile.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonRemoveLogFileActionPerformed(evt);
            }
        });

        jLabelServerProperties.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelServerProperties.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelServerProperties.setText("Server Properties");
        jLabelServerProperties.setFocusable(false);

        jPanelServerProperties.setBackground(new java.awt.Color(204, 204, 204));
        jPanelServerProperties.setToolTipText("This box contains the properties for the selected server.");
        jPanelServerProperties.setFocusable(false);

        buttonGroupServerProperties.add(jRadioButtonServerConnectionTelnet);
        jRadioButtonServerConnectionTelnet.setText("Telnet");
        jRadioButtonServerConnectionTelnet.setEnabled(false);
        jRadioButtonServerConnectionTelnet.setNextFocusableComponent(jRadioButtonServerConnectionSSH);
        jRadioButtonServerConnectionTelnet.setOpaque(false);
        jRadioButtonServerConnectionTelnet.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jRadioButtonServerConnectionTelnetActionPerformed(evt);
            }
        });

        jCheckBoxLogMonitoringEnabled.setText("Enabled");
        jCheckBoxLogMonitoringEnabled.setToolTipText("When checked, the selected server will be monitored using the log files shown on the right.");
        jCheckBoxLogMonitoringEnabled.setContentAreaFilled(false);
        jCheckBoxLogMonitoringEnabled.setEnabled(false);
        jCheckBoxLogMonitoringEnabled.setNextFocusableComponent(jCheckBoxAlarmMonitoring);
        jCheckBoxLogMonitoringEnabled.setOpaque(false);
        jCheckBoxLogMonitoringEnabled.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxLogMonitoringEnabledActionPerformed(evt);
            }
        });

        buttonGroupServerProperties.add(jRadioButtonServerConnectionSSH);
        jRadioButtonServerConnectionSSH.setText("SSH");
        jRadioButtonServerConnectionSSH.setEnabled(false);
        jRadioButtonServerConnectionSSH.setNextFocusableComponent(jListLogFiles);
        jRadioButtonServerConnectionSSH.setOpaque(false);
        jRadioButtonServerConnectionSSH.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jRadioButtonServerConnectionSSHActionPerformed(evt);
            }
        });

        jLabelServerConnectionProtocol.setText("Connection Protocol");
        jLabelServerConnectionProtocol.setFocusable(false);

        jLabelLogMonitoring.setText("Log Monitoring");
        jLabelLogMonitoring.setFocusable(false);

        jLabelAlarmMonitoring.setText("Alarm Monitoring");

        jCheckBoxAlarmMonitoring.setText("Enabled");
        jCheckBoxAlarmMonitoring.setToolTipText("When checked, the currently selected server will be monitored for alarms.");
        jCheckBoxAlarmMonitoring.setContentAreaFilled(false);
        jCheckBoxAlarmMonitoring.setEnabled(false);
        jCheckBoxAlarmMonitoring.setName(""); // NOI18N
        jCheckBoxAlarmMonitoring.setNextFocusableComponent(jRadioButtonServerConnectionTelnet);
        jCheckBoxAlarmMonitoring.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxAlarmMonitoringActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelServerPropertiesLayout = new javax.swing.GroupLayout(jPanelServerProperties);
        jPanelServerProperties.setLayout(jPanelServerPropertiesLayout);
        jPanelServerPropertiesLayout.setHorizontalGroup(
            jPanelServerPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparatorServerConnectionProtocol)
            .addComponent(jSeparatorServerMonitoring)
            .addComponent(jSeparator2)
            .addGroup(jPanelServerPropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelServerPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelServerConnectionProtocol)
                    .addComponent(jCheckBoxLogMonitoringEnabled)
                    .addComponent(jLabelLogMonitoring)
                    .addComponent(jLabelAlarmMonitoring))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanelServerPropertiesLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanelServerPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxAlarmMonitoring)
                    .addGroup(jPanelServerPropertiesLayout.createSequentialGroup()
                        .addComponent(jRadioButtonServerConnectionTelnet)
                        .addGap(18, 18, 18)
                        .addComponent(jRadioButtonServerConnectionSSH)))
                .addGap(0, 83, Short.MAX_VALUE))
        );
        jPanelServerPropertiesLayout.setVerticalGroup(
            jPanelServerPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelServerPropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelLogMonitoring)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparatorServerMonitoring, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxLogMonitoringEnabled)
                .addGap(13, 13, 13)
                .addComponent(jLabelAlarmMonitoring)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxAlarmMonitoring)
                .addGap(13, 13, 13)
                .addComponent(jLabelServerConnectionProtocol)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparatorServerConnectionProtocol, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelServerPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonServerConnectionSSH)
                    .addComponent(jRadioButtonServerConnectionTelnet))
                .addContainerGap(121, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelServersAndLogsLayout = new javax.swing.GroupLayout(jPanelServersAndLogs);
        jPanelServersAndLogs.setLayout(jPanelServersAndLogsLayout);
        jPanelServersAndLogsLayout.setHorizontalGroup(
            jPanelServersAndLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelServersAndLogsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelServersAndLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelServersAndLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanelServersAndLogsLayout.createSequentialGroup()
                            .addComponent(jButtonAddServer)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonEditServer)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButtonRemoveServer))
                        .addComponent(jLabelServers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPaneServers, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanelServersAndLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelServerProperties, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelServerProperties, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(jPanelServersAndLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelServersAndLogsLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabelLogFiles, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelServersAndLogsLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                        .addGroup(jPanelServersAndLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelServersAndLogsLayout.createSequentialGroup()
                                .addComponent(jButtonAddLogFile)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonRemoveLogFile))
                            .addComponent(jScrollPaneLogFiles, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanelServersAndLogsLayout.setVerticalGroup(
            jPanelServersAndLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelServersAndLogsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelServersAndLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelServers)
                    .addComponent(jLabelLogFiles)
                    .addComponent(jLabelServerProperties))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelServersAndLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPaneServers)
                    .addComponent(jScrollPaneLogFiles)
                    .addComponent(jPanelServerProperties, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelServersAndLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonAddServer)
                    .addComponent(jButtonEditServer)
                    .addComponent(jButtonRemoveServer)
                    .addComponent(jButtonAddLogFile)
                    .addComponent(jButtonRemoveLogFile))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPaneRoot.addTab("Servers", jPanelServersAndLogs);

        jPanelTemplateLogFiles.setFocusable(false);

        jListTemplateLogFiles.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListTemplateLogFiles.setNextFocusableComponent(jButtonTemplateLogFileAdd);
        jListTemplateLogFiles.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                jListTemplateLogFilesValueChanged(evt);
            }
        });
        jScrollPaneTemplateLogFiles.setViewportView(jListTemplateLogFiles);

        jLabelTemplateLogFiles.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelTemplateLogFiles.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelTemplateLogFiles.setText("Template Log Files");
        jLabelTemplateLogFiles.setFocusable(false);

        jPanelTemplateLogFileProperties.setBackground(new java.awt.Color(204, 204, 204));

        jLabelTemplateLogFileName.setText("Prefix of the log file name:");
        jLabelTemplateLogFileName.setFocusable(false);

        jLabelTemplateLogFilePath.setText("Path to the log file location:");
        jLabelTemplateLogFilePath.setFocusable(false);

        jTextFieldLogFilePath.setToolTipText("Absolute path to the log file (including trailing slash)");
        jTextFieldLogFilePath.setEnabled(false);
        jTextFieldLogFilePath.setNextFocusableComponent(jListLogFileFilters);
        jTextFieldLogFilePath.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                jTextFieldLogFilePathFocusLost(evt);
            }
        });

        jTextFieldLogFilePrefix.setToolTipText("Prefix of the log file name (e.g.: XSLog)");
        jTextFieldLogFilePrefix.setEnabled(false);
        jTextFieldLogFilePrefix.setNextFocusableComponent(jTextFieldLogFilePath);
        jTextFieldLogFilePrefix.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                jTextFieldLogFilePrefixFocusLost(evt);
            }
        });

        jLabelLogFileDescription.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelLogFileDescription.setText("Log File Description");
        jLabelLogFileDescription.setFocusable(false);

        jLabelLogFileFilters.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabelLogFileFilters.setText("Filters");
        jLabelLogFileFilters.setFocusable(false);

        jListLogFileFilters.setEnabled(false);
        jListLogFileFilters.setNextFocusableComponent(jButtonLogFileFilterAdd);
        jListLogFileFilters.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                jListLogFileFiltersValueChanged(evt);
            }
        });
        jScrollPaneLogFileFilters.setViewportView(jListLogFileFilters);

        jButtonLogFileFilterAdd.setText("Add Filter...");
        jButtonLogFileFilterAdd.setEnabled(false);
        jButtonLogFileFilterAdd.setNextFocusableComponent(jButtonLogFileFilterRemove);
        jButtonLogFileFilterAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonLogFileFilterAddActionPerformed(evt);
            }
        });

        jButtonLogFileFilterRemove.setText("Remove");
        jButtonLogFileFilterRemove.setEnabled(false);
        jButtonLogFileFilterRemove.setNextFocusableComponent(jCheckBoxLogFileFilterEnabled);
        jButtonLogFileFilterRemove.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonLogFileFilterRemoveActionPerformed(evt);
            }
        });

        jPanelFilterProperties.setBackground(new java.awt.Color(153, 153, 153));

        jCheckBoxLogFileFilterEnabled.setText("Enabled");
        jCheckBoxLogFileFilterEnabled.setEnabled(false);
        jCheckBoxLogFileFilterEnabled.setNextFocusableComponent(jTextFieldLogFileFilterName);
        jCheckBoxLogFileFilterEnabled.setOpaque(false);
        jCheckBoxLogFileFilterEnabled.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxLogFileFilterEnabledActionPerformed(evt);
            }
        });

        jLabelLogFileFilterPostPrint.setText("Number of messages to display after:");
        jLabelLogFileFilterPostPrint.setFocusable(false);

        jLabelLogFileFilterPrePrint.setText("Number of messages to display before:");
        jLabelLogFileFilterPrePrint.setFocusable(false);

        jSpinnerLogFileFilterPrePrint.setModel(new javax.swing.SpinnerNumberModel(2, 0, 15, 1));
        jSpinnerLogFileFilterPrePrint.setToolTipText("");
        jSpinnerLogFileFilterPrePrint.setEnabled(false);
        jSpinnerLogFileFilterPrePrint.setNextFocusableComponent(jSpinnerLogFileFilterPostPrint);
        jSpinnerLogFileFilterPrePrint.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerLogFileFilterPrePrintStateChanged(evt);
            }
        });

        jSpinnerLogFileFilterPostPrint.setModel(new javax.swing.SpinnerNumberModel(2, 0, 15, 1));
        jSpinnerLogFileFilterPostPrint.setEnabled(false);
        jSpinnerLogFileFilterPostPrint.setNextFocusableComponent(jButtonOK);
        jSpinnerLogFileFilterPostPrint.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerLogFileFilterPostPrintStateChanged(evt);
            }
        });

        jLabelLogFileFilterKeyword.setText("String to find:");
        jLabelLogFileFilterKeyword.setFocusable(false);

        jTextFieldLogFileFilterKeyword.setToolTipText("Keyword that will be used to match text in the logs");
        jTextFieldLogFileFilterKeyword.setEnabled(false);
        jTextFieldLogFileFilterKeyword.setNextFocusableComponent(jSpinnerLogFileFilterPrePrint);
        jTextFieldLogFileFilterKeyword.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                jTextFieldLogFileFilterKeywordFocusLost(evt);
            }
        });

        jLabelLogFileFilterName.setText("Profile name:");
        jLabelLogFileFilterName.setFocusable(false);

        jTextFieldLogFileFilterName.setToolTipText("Name of the profile");
        jTextFieldLogFileFilterName.setEnabled(false);
        jTextFieldLogFileFilterName.setNextFocusableComponent(jTextFieldLogFileFilterKeyword);
        jTextFieldLogFileFilterName.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                jTextFieldLogFileFilterNameFocusLost(evt);
            }
        });

        jLabelHighlightColor.setText("Foreground highlight color:");

        jColorFieldHighlight.setEditable(false);
        jColorFieldHighlight.setBackground(new java.awt.Color(255, 255, 255));
        jColorFieldHighlight.setEnabled(false);
        jColorFieldHighlight.setFocusable(false);

        jButtonHighlightColor.setText("Choose color...");
        jButtonHighlightColor.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonHighlightColorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelFilterPropertiesLayout = new javax.swing.GroupLayout(jPanelFilterProperties);
        jPanelFilterProperties.setLayout(jPanelFilterPropertiesLayout);
        jPanelFilterPropertiesLayout.setHorizontalGroup(
            jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFilterPropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelFilterPropertiesLayout.createSequentialGroup()
                        .addGroup(jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelLogFileFilterName)
                            .addComponent(jLabelLogFileFilterKeyword))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldLogFileFilterName)
                            .addComponent(jTextFieldLogFileFilterKeyword)))
                    .addGroup(jPanelFilterPropertiesLayout.createSequentialGroup()
                        .addGroup(jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBoxLogFileFilterEnabled)
                            .addGroup(jPanelFilterPropertiesLayout.createSequentialGroup()
                                .addGroup(jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabelLogFileFilterPrePrint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabelLogFileFilterPostPrint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jSpinnerLogFileFilterPrePrint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jSpinnerLogFileFilterPostPrint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanelFilterPropertiesLayout.createSequentialGroup()
                                .addComponent(jLabelHighlightColor)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jColorFieldHighlight, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButtonHighlightColor, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 7, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanelFilterPropertiesLayout.setVerticalGroup(
            jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFilterPropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBoxLogFileFilterEnabled)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelHighlightColor)
                    .addComponent(jColorFieldHighlight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonHighlightColor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelLogFileFilterName)
                    .addComponent(jTextFieldLogFileFilterName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelLogFileFilterKeyword)
                    .addComponent(jTextFieldLogFileFilterKeyword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelLogFileFilterPrePrint)
                    .addComponent(jSpinnerLogFileFilterPrePrint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelLogFileFilterPostPrint)
                    .addComponent(jSpinnerLogFileFilterPostPrint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jLabelLogFileAlias.setText("Profile name:");

        jTextFieldLogFileName.setToolTipText("Name of the profile");
        jTextFieldLogFileName.setEnabled(false);
        jTextFieldLogFileName.setNextFocusableComponent(jTextFieldLogFilePrefix);
        jTextFieldLogFileName.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                jTextFieldLogFileNameFocusLost(evt);
            }
        });

        javax.swing.GroupLayout jPanelTemplateLogFilePropertiesLayout = new javax.swing.GroupLayout(jPanelTemplateLogFileProperties);
        jPanelTemplateLogFileProperties.setLayout(jPanelTemplateLogFilePropertiesLayout);
        jPanelTemplateLogFilePropertiesLayout.setHorizontalGroup(
            jPanelTemplateLogFilePropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparatorLogFileDescription)
            .addComponent(jSeparatorLogFileFilters)
            .addGroup(jPanelTemplateLogFilePropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelTemplateLogFilePropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelTemplateLogFilePropertiesLayout.createSequentialGroup()
                        .addComponent(jScrollPaneLogFileFilters, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanelTemplateLogFilePropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelTemplateLogFilePropertiesLayout.createSequentialGroup()
                                .addComponent(jButtonLogFileFilterAdd)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonLogFileFilterRemove)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jPanelFilterProperties, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanelTemplateLogFilePropertiesLayout.createSequentialGroup()
                        .addGroup(jPanelTemplateLogFilePropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelLogFileDescription)
                            .addComponent(jLabelLogFileFilters))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanelTemplateLogFilePropertiesLayout.createSequentialGroup()
                        .addGroup(jPanelTemplateLogFilePropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabelLogFileAlias, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelTemplateLogFileName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelTemplateLogFilePath, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jPanelTemplateLogFilePropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelTemplateLogFilePropertiesLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jTextFieldLogFilePath))
                            .addGroup(jPanelTemplateLogFilePropertiesLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(jPanelTemplateLogFilePropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextFieldLogFilePrefix)
                                    .addComponent(jTextFieldLogFileName))))))
                .addContainerGap())
        );
        jPanelTemplateLogFilePropertiesLayout.setVerticalGroup(
            jPanelTemplateLogFilePropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTemplateLogFilePropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelLogFileDescription)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparatorLogFileDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addGroup(jPanelTemplateLogFilePropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelLogFileAlias)
                    .addComponent(jTextFieldLogFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelTemplateLogFilePropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelTemplateLogFileName)
                    .addComponent(jTextFieldLogFilePrefix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelTemplateLogFilePropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelTemplateLogFilePath)
                    .addComponent(jTextFieldLogFilePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelLogFileFilters)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparatorLogFileFilters, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelTemplateLogFilePropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneLogFileFilters, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                    .addGroup(jPanelTemplateLogFilePropertiesLayout.createSequentialGroup()
                        .addComponent(jPanelFilterProperties, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelTemplateLogFilePropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonLogFileFilterAdd)
                            .addComponent(jButtonLogFileFilterRemove))))
                .addContainerGap())
        );

        jButtonTemplateLogFileAdd.setText("Add");
        jButtonTemplateLogFileAdd.setNextFocusableComponent(jButtonTemplateLogFileRemove);
        jButtonTemplateLogFileAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonTemplateLogFileAddActionPerformed(evt);
            }
        });

        jButtonTemplateLogFileRemove.setText("Remove");
        jButtonTemplateLogFileRemove.setEnabled(false);
        jButtonTemplateLogFileRemove.setNextFocusableComponent(jTextFieldLogFileName);
        jButtonTemplateLogFileRemove.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonTemplateLogFileRemoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelTemplateLogFilesLayout = new javax.swing.GroupLayout(jPanelTemplateLogFiles);
        jPanelTemplateLogFiles.setLayout(jPanelTemplateLogFilesLayout);
        jPanelTemplateLogFilesLayout.setHorizontalGroup(
            jPanelTemplateLogFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTemplateLogFilesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelTemplateLogFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelTemplateLogFiles, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                    .addComponent(jScrollPaneTemplateLogFiles, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanelTemplateLogFilesLayout.createSequentialGroup()
                        .addComponent(jButtonTemplateLogFileAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonTemplateLogFileRemove)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addComponent(jPanelTemplateLogFileProperties, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanelTemplateLogFilesLayout.setVerticalGroup(
            jPanelTemplateLogFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTemplateLogFilesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelTemplateLogFiles)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPaneTemplateLogFiles, javax.swing.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelTemplateLogFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonTemplateLogFileAdd)
                    .addComponent(jButtonTemplateLogFileRemove))
                .addContainerGap())
            .addComponent(jPanelTemplateLogFileProperties, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPaneRoot.addTab("Template Log Files", jPanelTemplateLogFiles);

        jPanelMisc.setFocusable(false);
        jPanelMisc.setNextFocusableComponent(jTextFieldServerUsername);

        jLabelCredentials.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelCredentials.setText("Credentials");
        jLabelCredentials.setFocusable(false);

        jLabelServerUsername.setText("Username:");
        jLabelServerUsername.setFocusable(false);

        jTextFieldServerUsername.setText("bwadmin");
        jTextFieldServerUsername.setToolTipText("Enter the username to use when we connect to any of the servers");
        jTextFieldServerUsername.setNextFocusableComponent(jPasswordFieldServerPassword);
        jTextFieldServerUsername.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                jTextFieldServerUsernameFocusLost(evt);
            }
        });

        jLabelServerPassword.setText("Password:");
        jLabelServerPassword.setFocusable(false);

        jPasswordFieldServerPassword.setText("bwadmin");
        jPasswordFieldServerPassword.setToolTipText("Enter the password to use when we connect to any of the servers");
        jPasswordFieldServerPassword.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                jPasswordFieldServerPasswordFocusLost(evt);
            }
        });

        jLabelTerminalOptions.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelTerminalOptions.setText("Terminal Options");

        jLabelMaxNumLines.setText("Maximum number of lines:");

        jSpinnerMaxNumLines.setModel(new javax.swing.SpinnerNumberModel(30000, 30, 100000, 10));
        jSpinnerMaxNumLines.setToolTipText("The maxmimum number of lines to display in the terminal window");
        jSpinnerMaxNumLines.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerMaxNumLinesStateChanged(evt);
            }
        });

        jLabelTerminalForegroundColor.setText("Foreground color:");

        jLabelTerminalBackgroundColor.setText("Background color:");

        jButtonTerminalForegroundColor.setText("Select color...");
        jButtonTerminalForegroundColor.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonTerminalForegroundColorActionPerformed(evt);
            }
        });

        jButtonTerminalBackgroundColor.setText("Select color...");
        jButtonTerminalBackgroundColor.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonTerminalBackgroundColorActionPerformed(evt);
            }
        });

        jCheckBoxFlashTaskbar.setSelected(true);
        jCheckBoxFlashTaskbar.setText("Flash task bar on new text");
        jCheckBoxFlashTaskbar.setToolTipText("If enabled, the task bar icon will flash if a new message is received while this window is out of focus");
        jCheckBoxFlashTaskbar.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jCheckBoxFlashTaskbarStateChanged(evt);
            }
        });

        jLabelFont.setText("Font:");

        jButtonFont.setText("Select font...");
        jButtonFont.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonFontActionPerformed(evt);
            }
        });

        jLabelAutoRefresh.setText("Automatic file refresh interval (in seconds):");

        jSpinnerAutoRefresh.setModel(new javax.swing.SpinnerNumberModel(4, 1, 600, 1));
        jSpinnerAutoRefresh.setToolTipText("Modifies the interval at which the program will verify if a newer log file is available");
        jSpinnerAutoRefresh.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerAutoRefreshStateChanged(evt);
            }
        });

        jColorFieldForeground.setEditable(false);
        jColorFieldForeground.setBackground(new java.awt.Color(255, 255, 255));
        jColorFieldForeground.setEnabled(false);
        jColorFieldForeground.setFocusable(false);

        jColorFieldBackground.setEditable(false);
        jColorFieldBackground.setBackground(new java.awt.Color(255, 255, 255));
        jColorFieldBackground.setEnabled(false);
        jColorFieldBackground.setFocusable(false);

        jCheckBoxLogToDisk.setText("Log terminal to disk");
        jCheckBoxLogToDisk.setToolTipText("Enables the output of the terminal text to a log in the log folder relative to the executable");
        jCheckBoxLogToDisk.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxLogToDiskActionPerformed(evt);
            }
        });

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabelAlarmFilters.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelAlarmFilters.setText("Alarms");

        jListAlarmFilters.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                jListAlarmFiltersValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jListAlarmFilters);

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        jTextAreaAlarmFilters.setEditable(false);
        jTextAreaAlarmFilters.setBackground(new java.awt.Color(240, 240, 240));
        jTextAreaAlarmFilters.setColumns(20);
        jTextAreaAlarmFilters.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jTextAreaAlarmFilters.setLineWrap(true);
        jTextAreaAlarmFilters.setRows(5);
        jTextAreaAlarmFilters.setText("This list contains the \"trapName\" of the alarms you do not want to receive.");
        jTextAreaAlarmFilters.setWrapStyleWord(true);
        jTextAreaAlarmFilters.setFocusable(false);
        jTextAreaAlarmFilters.setOpaque(false);
        jTextAreaAlarmFilters.setRequestFocusEnabled(false);
        jTextAreaAlarmFilters.setVerifyInputWhenFocusTarget(false);
        jScrollPane2.setViewportView(jTextAreaAlarmFilters);

        jTextFieldAlarmFilters.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyTyped(java.awt.event.KeyEvent evt)
            {
                jTextFieldAlarmFiltersKeyTyped(evt);
            }
        });

        jButtonAlarmFilterRemove.setText("-");
        jButtonAlarmFilterRemove.setEnabled(false);
        jButtonAlarmFilterRemove.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonAlarmFilterRemoveActionPerformed(evt);
            }
        });

        jButtonAlarmFilterAdd.setText("+");
        jButtonAlarmFilterAdd.setEnabled(false);
        jButtonAlarmFilterAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonAlarmFilterAddActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelMiscLayout = new javax.swing.GroupLayout(jPanelMisc);
        jPanelMisc.setLayout(jPanelMiscLayout);
        jPanelMiscLayout.setHorizontalGroup(
            jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMiscLayout.createSequentialGroup()
                .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelMiscLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelCredentials)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMiscLayout.createSequentialGroup()
                                        .addComponent(jLabelServerUsername)
                                        .addGap(18, 18, 18)
                                        .addComponent(jTextFieldServerUsername)))
                                .addGroup(jPanelMiscLayout.createSequentialGroup()
                                    .addComponent(jLabelServerPassword)
                                    .addGap(20, 20, 20)
                                    .addComponent(jPasswordFieldServerPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabelTerminalOptions)
                            .addGroup(jPanelMiscLayout.createSequentialGroup()
                                .addComponent(jCheckBoxFlashTaskbar)
                                .addGap(18, 18, 18)
                                .addComponent(jCheckBoxLogToDisk))
                            .addGroup(jPanelMiscLayout.createSequentialGroup()
                                .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanelMiscLayout.createSequentialGroup()
                                        .addComponent(jLabelMaxNumLines)
                                        .addGap(93, 93, 93))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMiscLayout.createSequentialGroup()
                                        .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(jPanelMiscLayout.createSequentialGroup()
                                                .addComponent(jLabelTerminalForegroundColor)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jColorFieldForeground, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanelMiscLayout.createSequentialGroup()
                                                .addComponent(jLabelTerminalBackgroundColor)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jColorFieldBackground, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(18, 18, 18)))
                                .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButtonTerminalBackgroundColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButtonTerminalForegroundColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jSpinnerMaxNumLines, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelMiscLayout.createSequentialGroup()
                                    .addComponent(jLabelAutoRefresh)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jSpinnerAutoRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelMiscLayout.createSequentialGroup()
                                    .addComponent(jLabelFont)
                                    .addGap(191, 191, 191)
                                    .addComponent(jButtonFont, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE)
                        .addComponent(jSeparatorCredentials, javax.swing.GroupLayout.Alignment.LEADING)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMiscLayout.createSequentialGroup()
                        .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMiscLayout.createSequentialGroup()
                                .addComponent(jTextFieldAlarmFilters)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButtonAlarmFilterAdd)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonAlarmFilterRemove))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabelAlarmFilters)
                        .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        jPanelMiscLayout.setVerticalGroup(
            jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator3)
            .addGroup(jPanelMiscLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelMiscLayout.createSequentialGroup()
                        .addComponent(jLabelCredentials)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparatorCredentials, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelServerUsername)
                            .addComponent(jTextFieldServerUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelServerPassword)
                            .addComponent(jPasswordFieldServerPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabelTerminalOptions)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelMaxNumLines)
                            .addComponent(jSpinnerMaxNumLines, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckBoxFlashTaskbar)
                            .addComponent(jCheckBoxLogToDisk))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelTerminalForegroundColor)
                            .addComponent(jButtonTerminalForegroundColor)
                            .addComponent(jColorFieldForeground, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelTerminalBackgroundColor)
                            .addComponent(jButtonTerminalBackgroundColor)
                            .addComponent(jColorFieldBackground, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelFont)
                            .addComponent(jButtonFont))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelAutoRefresh)
                            .addComponent(jSpinnerAutoRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanelMiscLayout.createSequentialGroup()
                        .addComponent(jLabelAlarmFilters)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldAlarmFilters, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButtonAlarmFilterRemove)
                        .addComponent(jButtonAlarmFilterAdd)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPaneRoot.addTab("Miscellaneous", jPanelMisc);

        jButtonCancel.setText("Cancel");
        jButtonCancel.setNextFocusableComponent(jTabbedPaneRoot);
        jButtonCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonCancelActionPerformed(evt);
            }
        });

        jButtonOK.setText("OK");
        jButtonOK.setNextFocusableComponent(jButtonCancel);
        jButtonOK.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonOKActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPaneRoot)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonOK)
                .addGap(18, 18, 18)
                .addComponent(jButtonCancel)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPaneRoot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCancel)
                    .addComponent(jButtonOK))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAddServerActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAddServerActionPerformed
    {//GEN-HEADEREND:event_jButtonAddServerActionPerformed
	boolean success = true;
	String[] server =
	{
	    "", ""
	};
	ArrayList<String> newServer;
	do
	{
	    if (!success)
	    {
		newServer = new ServerDialog(this, true, true, server[0], server[1], templateLogFilesListModel.elements(), serverLogFilesListModel.elements()).showDialog();
	    }
	    else
	    {
		newServer = new ServerDialog(this, true, false, null, null, templateLogFilesListModel.elements(), null).showDialog();
	    }

	    server[0] = newServer.get(0);
	    server[1] = newServer.get(1);

	    // Verify if the user cancelled
	    if (server[0].equals("") || server[1].equals(""))
	    {
		break;
	    }

	    // Verify the input
	    if (!serverListModel.contains(server[0]))
	    {
		// Create the server
		Server s = new Server(server[0], server[1]);

		// Add its log files
		for (int i = 2; i < newServer.size(); ++i)
		{
		    s.addLog(newServer.get(i));
		}

		// Add it to the respective places
		Preferences.getInstance().addServer(s);
		serverListModel.addElement(server[0]);

		// Set added server as selected and load its properties
		jListServers.setSelectedValue(server[0], true);

		// Sort
		sortList(jListServers, ListComparator.Type.SERVER);

		success = true;
	    }
	    else // Restart if the input was wrong
	    {
		JOptionPane.showMessageDialog(this, "There is another server with the same name!");
		success = false;
	    }
	} while (!success);

    }//GEN-LAST:event_jButtonAddServerActionPerformed

    private void jButtonTemplateLogFileAddActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonTemplateLogFileAddActionPerformed
    {//GEN-HEADEREND:event_jButtonTemplateLogFileAddActionPerformed
	String name = "New Log File";

	if (templateLogFilesListModel.contains(name))
	{
	    int increment = 1;
	    while (templateLogFilesListModel.contains(name + " " + increment))
	    {
		++increment;
	    }
	    templateLogFilesListModel.addElement(name += " " + increment);
	}
	else
	{
	    templateLogFilesListModel.addElement(name);
	}

	// Select the new item and save in prefs
	Preferences.getInstance().addLog(new Log(name));
	jListTemplateLogFiles.setSelectedValue(name, true);

	sortList(jListTemplateLogFiles, ListComparator.Type.LOG);
    }//GEN-LAST:event_jButtonTemplateLogFileAddActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonCancelActionPerformed
    {//GEN-HEADEREND:event_jButtonCancelActionPerformed
	cancel();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonOKActionPerformed
    {//GEN-HEADEREND:event_jButtonOKActionPerformed
	// Save window location and size
	Preferences.getInstance().setUIPreference(id, getBounds(), 0);

	// Make sure every pref tab is saved
	ensureConsistency();

	// Save all the modifications
	Preferences.getInstance().save();

	// Close pref window
	dispose();
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
    {//GEN-HEADEREND:event_formWindowClosing
	// Cancel changes before saving window prefs
	Preferences.getInstance().cancel();

	Preferences.getInstance().setUIPreference(id, getBounds(), 0);

	Preferences.getInstance().save();
    }//GEN-LAST:event_formWindowClosing

    private void jButtonEditServerActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonEditServerActionPerformed
    {//GEN-HEADEREND:event_jButtonEditServerActionPerformed
	// Find the ID of the server we want to edit
	Server serverToEdit = Preferences.getInstance().getServer((String) jListServers.getSelectedValue());
	boolean success;

	// Keep the index if we need to put it back
	int index = serverListModel.indexOf(serverToEdit.getName());

	do
	{
	    ArrayList<String> server = new ServerDialog(this, true, true, serverToEdit.getName(), serverToEdit.getHostname(), templateLogFilesListModel.elements(), serverLogFilesListModel.elements()).showDialog();

	    // Remove the server temporarily
	    serverListModel.removeElement((String) jListServers.getSelectedValue());

	    // If the user cancelled
	    if (server.get(0).equals("") || server.get(1).equals(""))
	    {
		serverListModel.add(index, serverToEdit.getName());
		break;
	    }
	    // Else we verify the input
	    else if (!serverListModel.contains(server.get(0)))
	    {
		// Update the logs on the server
		serverToEdit.removeAllLogs();
		for(int i = 2; i < server.size(); ++i)
		{
		    serverToEdit.addLog(server.get(i));
		}

		// Apply the changes to the prefs
		Preferences.getInstance().removeServer(serverToEdit.getName());
		serverToEdit.setName(server.get(0));
		serverToEdit.setHostname(server.get(1));
		Preferences.getInstance().addServer(serverToEdit);

		// Insert if correct
		serverListModel.addElement(server.get(0));

		// Sort the list
		sortList(jListServers, ListComparator.Type.SERVER);

		success = true;
	    }
	    else // Restart if the input was wrong
	    {
		JOptionPane.showMessageDialog(this, "There is another server with the same name!");
		success = false;
	    }
	} while (!success);

	// Reselect the server in the list no matter what
	jListServers.setSelectedValue(serverToEdit.getName(), true);
    }//GEN-LAST:event_jButtonEditServerActionPerformed

    private void jButtonRemoveServerActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonRemoveServerActionPerformed
    {//GEN-HEADEREND:event_jButtonRemoveServerActionPerformed
	// Find the ID of the server we want to edit
	Server serverToEdit = Preferences.getInstance().getServer((String) jListServers.getSelectedValue());

	// Remove it from the list
	int index = serverListModel.indexOf((String) jListServers.getSelectedValue());
	serverListModel.removeElement((String) jListServers.getSelectedValue());

	// Remove it from the prefs
	Preferences.getInstance().removeServer(serverToEdit.getName());

	// Change list selection to the previous element and load its properties
	if (index != 0)
	{
	    --index;
	}

	jListServers.setSelectedIndex(index);

	// Reload properties
	loadServerProperties();
    }//GEN-LAST:event_jButtonRemoveServerActionPerformed

    private void jCheckBoxLogMonitoringEnabledActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxLogMonitoringEnabledActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxLogMonitoringEnabledActionPerformed
	Preferences.getInstance().getServer((String) jListServers.getSelectedValue()).setMonitorLogs(jCheckBoxLogMonitoringEnabled.isSelected());

	sortList(jListServers, ListComparator.Type.SERVER);
	jListServers.repaint();
    }//GEN-LAST:event_jCheckBoxLogMonitoringEnabledActionPerformed

    private void jRadioButtonServerConnectionTelnetActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jRadioButtonServerConnectionTelnetActionPerformed
    {//GEN-HEADEREND:event_jRadioButtonServerConnectionTelnetActionPerformed
	if (jRadioButtonServerConnectionTelnet.isSelected())
	{
	    Preferences.getInstance().getServer((String) jListServers.getSelectedValue()).setUseSSH(false);
	}
	else
	{
	    Preferences.getInstance().getServer((String) jListServers.getSelectedValue()).setUseSSH(true);
	}
    }//GEN-LAST:event_jRadioButtonServerConnectionTelnetActionPerformed

    private void jRadioButtonServerConnectionSSHActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jRadioButtonServerConnectionSSHActionPerformed
    {//GEN-HEADEREND:event_jRadioButtonServerConnectionSSHActionPerformed
	if (jRadioButtonServerConnectionSSH.isSelected())
	{
	    Preferences.getInstance().getServer((String) jListServers.getSelectedValue()).setUseSSH(true);
	}
	else
	{
	    Preferences.getInstance().getServer((String) jListServers.getSelectedValue()).setUseSSH(false);
	}
    }//GEN-LAST:event_jRadioButtonServerConnectionSSHActionPerformed

    private void jListServersValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jListServersValueChanged
    {//GEN-HEADEREND:event_jListServersValueChanged
	if (jListServers.isEnabled() && jListServers.getSelectedValue() != null)
	{
	    loadServerProperties();
	}
    }//GEN-LAST:event_jListServersValueChanged

    private void jTextFieldServerUsernameFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_jTextFieldServerUsernameFocusLost
    {//GEN-HEADEREND:event_jTextFieldServerUsernameFocusLost
	Preferences.getInstance().setServerAccount(new PasswordAuthentication(jTextFieldServerUsername.getText(), jPasswordFieldServerPassword.getPassword()));
    }//GEN-LAST:event_jTextFieldServerUsernameFocusLost

    private void jPasswordFieldServerPasswordFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_jPasswordFieldServerPasswordFocusLost
    {//GEN-HEADEREND:event_jPasswordFieldServerPasswordFocusLost
	Preferences.getInstance().setServerAccount(new PasswordAuthentication(jTextFieldServerUsername.getText(), jPasswordFieldServerPassword.getPassword()));
    }//GEN-LAST:event_jPasswordFieldServerPasswordFocusLost

    private void jButtonTemplateLogFileRemoveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonTemplateLogFileRemoveActionPerformed
    {//GEN-HEADEREND:event_jButtonTemplateLogFileRemoveActionPerformed
	String name = (String) jListTemplateLogFiles.getSelectedValue();

	// Remove this entry from all the servers, if they have it
	for (int i = 0; i < serverListModel.size(); ++i)
	{
	    Preferences.getInstance().getServer((String) serverListModel.get(i)).removeLog(name);
	}

	// Remove from the preferences and the list
	int index = templateLogFilesListModel.indexOf(name);
	templateLogFilesListModel.removeElement(name);
	Preferences.getInstance().removeLog(name);

	// Select first element in the list
	if (index != 0)
	{
	    --index;
	}

	jListTemplateLogFiles.setSelectedIndex(index);

	// Reload properties in case there are no more elements
	loadLogTemplateProperties();
    }//GEN-LAST:event_jButtonTemplateLogFileRemoveActionPerformed

    private void jListTemplateLogFilesValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jListTemplateLogFilesValueChanged
    {//GEN-HEADEREND:event_jListTemplateLogFilesValueChanged
	if (jListTemplateLogFiles.getSelectedIndex() != -1)
	{
	    loadLogTemplateProperties();
	}
    }//GEN-LAST:event_jListTemplateLogFilesValueChanged

    private void jTextFieldLogFileNameFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_jTextFieldLogFileNameFocusLost
    {//GEN-HEADEREND:event_jTextFieldLogFileNameFocusLost
	// Remove old value, but save the index for the new value
	int index = jListTemplateLogFiles.getSelectedIndex();

	Log editedLog = Preferences.getInstance().getLog((String) jListTemplateLogFiles.getSelectedValue());

	String newName = jTextFieldLogFileName.getText();

	// Make sure there actually is a log selected
	if (editedLog == null || index == -1 || newName.equals((String) jListLogFiles.getSelectedValue()))
	{
	    return;
	}

	if (newName.equals(""))
	{
	    JOptionPane.showMessageDialog(this, "Name cannot be empty!");
	    jTextFieldLogFileName.setText((String) jListTemplateLogFiles.getSelectedValue());
	    return;
	}
	else if (newName.equals((String) jListTemplateLogFiles.getSelectedValue()))
	{
	    // No change has been done, so we just disregard this event
	    return;
	}
	else if (templateLogFilesListModel.contains(newName))
	{
	    JOptionPane.showMessageDialog(this, "There is another entry with the same name!");
	    jTextFieldLogFileName.setText((String) jListTemplateLogFiles.getSelectedValue());
	    return;
	}

	Preferences.getInstance().removeLog(editedLog.getName());

	// Verify if any of the servers has this log
	for (int i = 0; i < serverListModel.size(); ++i)
	{
	    Server server = Preferences.getInstance().getServer((String) serverListModel.get(i));

	    if (server.removeLog(editedLog.getName()))
	    {
		server.addLog(newName);
	    }
	}

	// Modify the value
	editedLog.setName(newName);

	// Insert into list and prefs
	Preferences.getInstance().addLog(editedLog);
	templateLogFilesListModel.set(index, editedLog.getName());

	// Select it
	jListTemplateLogFiles.setSelectedIndex(index);

	// Sort the list with the new value
	sortList(jListTemplateLogFiles, ListComparator.Type.LOG);
    }//GEN-LAST:event_jTextFieldLogFileNameFocusLost

    private void jTextFieldLogFilePrefixFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_jTextFieldLogFilePrefixFocusLost
    {//GEN-HEADEREND:event_jTextFieldLogFilePrefixFocusLost
	int index = jListTemplateLogFiles.getSelectedIndex();

	Log logToEdit = Preferences.getInstance().getLog((String) jListTemplateLogFiles.getSelectedValue());

	// Make sure there actually is a log selected
	if (index == -1 || logToEdit == null)
	{
	    return;
	}

	logToEdit.setNamePrefix(jTextFieldLogFilePrefix.getText());
    }//GEN-LAST:event_jTextFieldLogFilePrefixFocusLost

    private void jTextFieldLogFilePathFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_jTextFieldLogFilePathFocusLost
    {//GEN-HEADEREND:event_jTextFieldLogFilePathFocusLost
	int index = jListTemplateLogFiles.getSelectedIndex();

	Log logToEdit = Preferences.getInstance().getLog((String) jListTemplateLogFiles.getSelectedValue());

	// Make sure there actually is a log selected
	if (index == -1 || logToEdit == null)
	{
	    return;
	}

	String logFilePath;

	if (!(logFilePath = jTextFieldLogFilePath.getText()).endsWith("/") && !logFilePath.isEmpty())
	{
	    logFilePath += "/";
	    jTextFieldLogFilePath.setText(logFilePath);
	}

	logToEdit.setFilePath(logFilePath);
    }//GEN-LAST:event_jTextFieldLogFilePathFocusLost

    private void jButtonLogFileFilterAddActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonLogFileFilterAddActionPerformed
    {//GEN-HEADEREND:event_jButtonLogFileFilterAddActionPerformed
	// Create a new Filter

	String name = "New Filter";

	if (logFileFilterModel.contains(name))
	{
	    int increment = 1;
	    while (logFileFilterModel.contains(name + " " + increment))
	    {
		++increment;
	    }
	    logFileFilterModel.addElement(name += " " + increment);
	}
	else
	{
	    logFileFilterModel.addElement(name);
	}

	// Add the filter to the log template
	Preferences.getInstance().getLog((String) jListTemplateLogFiles.getSelectedValue()).addFilter(new Filter(name));

	// Select the filter (properties update will be done automagically)
	jListLogFileFilters.setSelectedIndex(logFileFilterModel.size() - 1);
    }//GEN-LAST:event_jButtonLogFileFilterAddActionPerformed

    private void jListLogFileFiltersValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jListLogFileFiltersValueChanged
    {//GEN-HEADEREND:event_jListLogFileFiltersValueChanged
	if (jListLogFileFilters.getSelectedValue() != null)
	{
	    loadLogFileFiltersProperties();
	}
    }//GEN-LAST:event_jListLogFileFiltersValueChanged

    private void jButtonLogFileFilterRemoveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonLogFileFilterRemoveActionPerformed
    {//GEN-HEADEREND:event_jButtonLogFileFilterRemoveActionPerformed
	// Find the log that owns this filter
	Log log = Preferences.getInstance().getLog((String) jListTemplateLogFiles.getSelectedValue());

	// Remove the filter from that log and the list
	int index = logFileFilterModel.indexOf((String) jListLogFileFilters.getSelectedValue());
	log.removeFilter((String) jListLogFileFilters.getSelectedValue());
	logFileFilterModel.removeElement((String) jListLogFileFilters.getSelectedValue());

	// Update selection in the list
	if (index != 0)
	{
	    --index;
	}
	jListLogFileFilters.setSelectedIndex(index);

	// Reload properties in case list is empty (then it won't update)
	loadLogFileFiltersProperties();
    }//GEN-LAST:event_jButtonLogFileFilterRemoveActionPerformed

    private void jButtonAddLogFileActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAddLogFileActionPerformed
    {//GEN-HEADEREND:event_jButtonAddLogFileActionPerformed
	// Let the user select the right log
	ArrayList<String> selectedLogs = new LogFileChooser(this, true, templateLogFilesListModel, serverLogFilesListModel).showDialog();

	for (String logName : selectedLogs)
	{
	    // Verify if the user cancelled
	    if (logName.equals(""))
	    {
		return;
	    }

	    // Add it to the server
	    Preferences.getInstance().getServer((String) jListServers.getSelectedValue()).addLog(logName);

	    serverLogFilesListModel.addElement(logName);
	    jListLogFiles.setSelectedValue(logName, true);
	}

	sortList(jListLogFiles, ListComparator.Type.LOG);
    }//GEN-LAST:event_jButtonAddLogFileActionPerformed

    private void jListLogFilesValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jListLogFilesValueChanged
    {//GEN-HEADEREND:event_jListLogFilesValueChanged
	if(jListLogFiles.isEnabled())
	    loadServerLogProperties();
    }//GEN-LAST:event_jListLogFilesValueChanged

    private void jButtonRemoveLogFileActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonRemoveLogFileActionPerformed
    {//GEN-HEADEREND:event_jButtonRemoveLogFileActionPerformed

	for (String logName : new ArrayList<String>(jListLogFiles.getSelectedValuesList()))
	{
	    // Remove it from the respective server
	    Preferences.getInstance().getServer((String) jListServers.getSelectedValue()).removeLog(logName);

	    // Remove it from the current list
	    int index = serverLogFilesListModel.indexOf(logName);
	    serverLogFilesListModel.removeElement(logName);

	    // Select previous object
	    if (index != 0)
	    {
		--index;
	    }

	    jListLogFiles.setSelectedIndex(index);
	}

	// Reload properties in case there are no other objects
	loadServerLogProperties();
    }//GEN-LAST:event_jButtonRemoveLogFileActionPerformed

    private void jTabbedPaneRootStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jTabbedPaneRootStateChanged
    {//GEN-HEADEREND:event_jTabbedPaneRootStateChanged
	JTabbedPane source = (JTabbedPane) evt.getSource();

	switch (source.getSelectedIndex())
	{
	    case 0:
		loadServerProperties();
		loadServerLogProperties();
		//jButtonAddServer.requestFocus();
		break;
	    case 1:
		loadLogTemplateProperties();
		loadLogFileFiltersProperties();
		//jButtonTemplateLogFileAdd.requestFocus();
		break;
	    case 2:
		//jTextFieldServerUsername.requestFocus();
		break;
	    default:
		break;
	}
    }//GEN-LAST:event_jTabbedPaneRootStateChanged

    private void jCheckBoxLogFileFilterEnabledActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxLogFileFilterEnabledActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxLogFileFilterEnabledActionPerformed
	String filterName = (String) jListLogFileFilters.getSelectedValue();
	String logName = (String) jListTemplateLogFiles.getSelectedValue();
	Preferences.getInstance().getLog(logName).getFilter(filterName).setEnabled(jCheckBoxLogFileFilterEnabled.isSelected());

	// Repaint the list to update BOLD display
	jListLogFileFilters.repaint();
    }//GEN-LAST:event_jCheckBoxLogFileFilterEnabledActionPerformed

    private void jTextFieldLogFileFilterNameFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_jTextFieldLogFileFilterNameFocusLost
    {//GEN-HEADEREND:event_jTextFieldLogFileFilterNameFocusLost
	Log currentLog = Preferences.getInstance().getLog((String) jListTemplateLogFiles.getSelectedValue());

	if (currentLog == null)
	{
	    return;
	}

	Filter currentFilter = currentLog.getFilter((String) jListLogFileFilters.getSelectedValue());

	if (currentFilter == null)
	{
	    return;
	}

	if (jTextFieldLogFileFilterName.getText().equals(""))
	{
	    JOptionPane.showMessageDialog(this, "The name cannot be empty!");
	    jTextFieldLogFileFilterName.setText((String) jListLogFileFilters.getSelectedValue());
	    return;
	}
	else if (jTextFieldLogFileFilterName.getText().equals((String) jListLogFileFilters.getSelectedValue()))
	{
	    // No change has been done, so we just disregard this event
	    return;
	}
	else if (logFileFilterModel.contains(jTextFieldLogFileFilterName.getText()))
	{
	    JOptionPane.showMessageDialog(this, "There is another entry with the same name!");
	    jTextFieldLogFileFilterName.setText((String) jListLogFileFilters.getSelectedValue());
	    return;
	}

	currentFilter.setName(jTextFieldLogFileFilterName.getText());

	currentLog.removeFilter((String) jListLogFileFilters.getSelectedValue());
	currentLog.addFilter(currentFilter);

	int index = jListLogFileFilters.getSelectedIndex();
	logFileFilterModel.remove(index);
	logFileFilterModel.add(index, currentFilter.getName());
	jListLogFileFilters.setSelectedIndex(index);
    }//GEN-LAST:event_jTextFieldLogFileFilterNameFocusLost

    private void jTextFieldLogFileFilterKeywordFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_jTextFieldLogFileFilterKeywordFocusLost
    {//GEN-HEADEREND:event_jTextFieldLogFileFilterKeywordFocusLost
	Log currentLog = Preferences.getInstance().getLog((String) jListTemplateLogFiles.getSelectedValue());

	if (currentLog == null)
	{
	    return;
	}

	Filter currentFilter = currentLog.getFilter((String) jListLogFileFilters.getSelectedValue());

	if (currentFilter == null)
	{
	    return;
	}
	
	currentFilter.setKeyword(jTextFieldLogFileFilterKeyword.getText());
    }//GEN-LAST:event_jTextFieldLogFileFilterKeywordFocusLost

    private void jSpinnerLogFileFilterPrePrintStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerLogFileFilterPrePrintStateChanged
    {//GEN-HEADEREND:event_jSpinnerLogFileFilterPrePrintStateChanged
	if (jListLogFileFilters.getSelectedValue() == null)
	{
	    return;
	}

	Log currentLog = Preferences.getInstance().getLog((String) jListTemplateLogFiles.getSelectedValue());
	Filter currentFilter = currentLog.getFilter((String) jListLogFileFilters.getSelectedValue());

	currentFilter.setMessagesBefore((int) jSpinnerLogFileFilterPrePrint.getValue());
    }//GEN-LAST:event_jSpinnerLogFileFilterPrePrintStateChanged

    private void jSpinnerLogFileFilterPostPrintStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerLogFileFilterPostPrintStateChanged
    {//GEN-HEADEREND:event_jSpinnerLogFileFilterPostPrintStateChanged
	if (jListLogFileFilters.getSelectedValue() == null)
	{
	    return;
	}

	Log currentLog = Preferences.getInstance().getLog((String) jListTemplateLogFiles.getSelectedValue());
	Filter currentFilter = currentLog.getFilter((String) jListLogFileFilters.getSelectedValue());

	currentFilter.setMessagesAfter((int) jSpinnerLogFileFilterPostPrint.getValue());
    }//GEN-LAST:event_jSpinnerLogFileFilterPostPrintStateChanged

    private void jSpinnerMaxNumLinesStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerMaxNumLinesStateChanged
    {//GEN-HEADEREND:event_jSpinnerMaxNumLinesStateChanged
	Preferences.getInstance().setMaxNumLines((int) jSpinnerMaxNumLines.getValue());
    }//GEN-LAST:event_jSpinnerMaxNumLinesStateChanged

    private void jCheckBoxFlashTaskbarStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jCheckBoxFlashTaskbarStateChanged
    {//GEN-HEADEREND:event_jCheckBoxFlashTaskbarStateChanged
	Preferences.getInstance().setFlashTaskbar(jCheckBoxFlashTaskbar.isSelected());
    }//GEN-LAST:event_jCheckBoxFlashTaskbarStateChanged

    private void jButtonTerminalForegroundColorActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonTerminalForegroundColorActionPerformed
    {//GEN-HEADEREND:event_jButtonTerminalForegroundColorActionPerformed
	ColorChooser cc = new ColorChooser(this, true, Preferences.getInstance().getForegroundColor());
	Color color = cc.showDialog();
	Preferences.getInstance().setForegroundColor(color);
	jColorFieldForeground.setBackground(color);
    }//GEN-LAST:event_jButtonTerminalForegroundColorActionPerformed

    private void jButtonTerminalBackgroundColorActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonTerminalBackgroundColorActionPerformed
    {//GEN-HEADEREND:event_jButtonTerminalBackgroundColorActionPerformed
	ColorChooser cc = new ColorChooser(this, true, Preferences.getInstance().getBackgroundColor());
	Color color = cc.showDialog();
	Preferences.getInstance().setBackgroundColor(color);
	jColorFieldBackground.setBackground(color);
    }//GEN-LAST:event_jButtonTerminalBackgroundColorActionPerformed

    private void jButtonFontActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonFontActionPerformed
    {//GEN-HEADEREND:event_jButtonFontActionPerformed
	JFontChooser fc = new JFontChooser();
	fc.setSelectedFont(Preferences.getInstance().getTerminalFont());
	int result = fc.showDialog(this);
	if (result == JFontChooser.OK_OPTION)
	{
	    Preferences.getInstance().setTerminalFont(fc.getSelectedFont());
	}
    }//GEN-LAST:event_jButtonFontActionPerformed

    private void jSpinnerAutoRefreshStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerAutoRefreshStateChanged
    {//GEN-HEADEREND:event_jSpinnerAutoRefreshStateChanged
	Preferences.getInstance().setRefreshInterval((int) jSpinnerAutoRefresh.getValue());
    }//GEN-LAST:event_jSpinnerAutoRefreshStateChanged

    private void jButtonHighlightColorActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonHighlightColorActionPerformed
    {//GEN-HEADEREND:event_jButtonHighlightColorActionPerformed
	Log log = Preferences.getInstance().getLog((String) jListTemplateLogFiles.getSelectedValue());
	Filter filter = log.getFilter((String) jListLogFileFilters.getSelectedValue());
	ColorChooser cc = new ColorChooser(this, true, filter.getHighlightColor());
	Color color = cc.showDialog();
	filter.setHighlightColor(color);
	jColorFieldHighlight.setBackground(color);
    }//GEN-LAST:event_jButtonHighlightColorActionPerformed

    private void jCheckBoxLogToDiskActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxLogToDiskActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxLogToDiskActionPerformed
        Preferences.getInstance().setOutputToDisk(jCheckBoxLogToDisk.isSelected());
    }//GEN-LAST:event_jCheckBoxLogToDiskActionPerformed

    private void jCheckBoxAlarmMonitoringActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxAlarmMonitoringActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxAlarmMonitoringActionPerformed
	Preferences.getInstance().getServer((String) jListServers.getSelectedValue()).setMonitorAlarms(jCheckBoxAlarmMonitoring.isSelected());
	
	sortList(jListServers, ListComparator.Type.SERVER);
	jListServers.repaint();
    }//GEN-LAST:event_jCheckBoxAlarmMonitoringActionPerformed

    private void jTextFieldAlarmFiltersKeyTyped(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTextFieldAlarmFiltersKeyTyped
    {//GEN-HEADEREND:event_jTextFieldAlarmFiltersKeyTyped
        jButtonAlarmFilterAdd.setEnabled(jTextFieldAlarmFilters.getText().length() > 0);
    }//GEN-LAST:event_jTextFieldAlarmFiltersKeyTyped

    private void jButtonAlarmFilterAddActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAlarmFilterAddActionPerformed
    {//GEN-HEADEREND:event_jButtonAlarmFilterAddActionPerformed
	String name = ((String) jTextFieldAlarmFilters.getText()).toLowerCase();
	if (alarmFilterListModel.contains(name))
	{
	    JOptionPane.showMessageDialog(this, "An alarm with the same name already exists.");
	    return;
	}
	Preferences.getInstance().addAlarmFilter(name);
	alarmFilterListModel.addElement(name);
	jTextFieldAlarmFilters.setText("");
	jButtonAlarmFilterAdd.setEnabled(false);
    }//GEN-LAST:event_jButtonAlarmFilterAddActionPerformed

    private void jButtonAlarmFilterRemoveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAlarmFilterRemoveActionPerformed
    {//GEN-HEADEREND:event_jButtonAlarmFilterRemoveActionPerformed
	int index = jListAlarmFilters.getSelectedIndex();
	Preferences.getInstance().removeAlarmFilter((String) jListAlarmFilters.getSelectedValue());
	alarmFilterListModel.remove(index);
	if (index > 0)
	{
	    --index;
	}
	jListAlarmFilters.setSelectedIndex(index);
    }//GEN-LAST:event_jButtonAlarmFilterRemoveActionPerformed

    private void jListAlarmFiltersValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jListAlarmFiltersValueChanged
    {//GEN-HEADEREND:event_jListAlarmFiltersValueChanged
	jButtonAlarmFilterRemove.setEnabled(jListAlarmFilters.getSelectedIndex() >= 0);
    }//GEN-LAST:event_jListAlarmFiltersValueChanged

    class ServerCellRenderer extends DefaultListCellRenderer
    {
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
	    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

	    if (!serverListModel.isEmpty() && (Preferences.getInstance().getServer((String) value)).isEnabled())
	    {
		c.setFont(c.getFont().deriveFont(Font.BOLD));
	    }
	    else
	    {
		c.setFont(c.getFont().deriveFont(Font.PLAIN));
	    }
	    return c;
	}
    }

    class LogFileFilterCellRenderer extends DefaultListCellRenderer
    {
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
	    Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

	    Filter f = Preferences.getInstance().getLog((String) jListTemplateLogFiles.getSelectedValue()).getFilter((String) value);
	    
	    if (f != null && f.isEnabled())
	    {
		c.setFont(c.getFont().deriveFont(Font.BOLD));
	    }
	    else
	    {
		c.setFont(c.getFont().deriveFont(Font.PLAIN));
	    }
	    return c;
	}
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Variables">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupServerProperties;
    private javax.swing.JButton jButtonAddLogFile;
    private javax.swing.JButton jButtonAddServer;
    private javax.swing.JButton jButtonAlarmFilterAdd;
    private javax.swing.JButton jButtonAlarmFilterRemove;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonEditServer;
    private javax.swing.JButton jButtonFont;
    private javax.swing.JButton jButtonHighlightColor;
    private javax.swing.JButton jButtonLogFileFilterAdd;
    private javax.swing.JButton jButtonLogFileFilterRemove;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JButton jButtonRemoveLogFile;
    private javax.swing.JButton jButtonRemoveServer;
    private javax.swing.JButton jButtonTemplateLogFileAdd;
    private javax.swing.JButton jButtonTemplateLogFileRemove;
    private javax.swing.JButton jButtonTerminalBackgroundColor;
    private javax.swing.JButton jButtonTerminalForegroundColor;
    private javax.swing.JCheckBox jCheckBoxAlarmMonitoring;
    private javax.swing.JCheckBox jCheckBoxFlashTaskbar;
    private javax.swing.JCheckBox jCheckBoxLogFileFilterEnabled;
    private javax.swing.JCheckBox jCheckBoxLogMonitoringEnabled;
    private javax.swing.JCheckBox jCheckBoxLogToDisk;
    private javax.swing.JTextField jColorFieldBackground;
    private javax.swing.JTextField jColorFieldForeground;
    private javax.swing.JTextField jColorFieldHighlight;
    private javax.swing.JLabel jLabelAlarmFilters;
    private javax.swing.JLabel jLabelAlarmMonitoring;
    private javax.swing.JLabel jLabelAutoRefresh;
    private javax.swing.JLabel jLabelCredentials;
    private javax.swing.JLabel jLabelFont;
    private javax.swing.JLabel jLabelHighlightColor;
    private javax.swing.JLabel jLabelLogFileAlias;
    private javax.swing.JLabel jLabelLogFileDescription;
    private javax.swing.JLabel jLabelLogFileFilterKeyword;
    private javax.swing.JLabel jLabelLogFileFilterName;
    private javax.swing.JLabel jLabelLogFileFilterPostPrint;
    private javax.swing.JLabel jLabelLogFileFilterPrePrint;
    private javax.swing.JLabel jLabelLogFileFilters;
    private javax.swing.JLabel jLabelLogFiles;
    private javax.swing.JLabel jLabelLogMonitoring;
    private javax.swing.JLabel jLabelMaxNumLines;
    private javax.swing.JLabel jLabelServerConnectionProtocol;
    private javax.swing.JLabel jLabelServerPassword;
    private javax.swing.JLabel jLabelServerProperties;
    private javax.swing.JLabel jLabelServerUsername;
    private javax.swing.JLabel jLabelServers;
    private javax.swing.JLabel jLabelTemplateLogFileName;
    private javax.swing.JLabel jLabelTemplateLogFilePath;
    private javax.swing.JLabel jLabelTemplateLogFiles;
    private javax.swing.JLabel jLabelTerminalBackgroundColor;
    private javax.swing.JLabel jLabelTerminalForegroundColor;
    private javax.swing.JLabel jLabelTerminalOptions;
    private javax.swing.JList jListAlarmFilters;
    private javax.swing.JList jListLogFileFilters;
    private javax.swing.JList jListLogFiles;
    private javax.swing.JList jListServers;
    private javax.swing.JList jListTemplateLogFiles;
    private javax.swing.JPanel jPanelFilterProperties;
    private javax.swing.JPanel jPanelMisc;
    private javax.swing.JPanel jPanelServerProperties;
    private javax.swing.JPanel jPanelServersAndLogs;
    private javax.swing.JPanel jPanelTemplateLogFileProperties;
    private javax.swing.JPanel jPanelTemplateLogFiles;
    private javax.swing.JPasswordField jPasswordFieldServerPassword;
    private javax.swing.JRadioButton jRadioButtonServerConnectionSSH;
    private javax.swing.JRadioButton jRadioButtonServerConnectionTelnet;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPaneLogFileFilters;
    private javax.swing.JScrollPane jScrollPaneLogFiles;
    private javax.swing.JScrollPane jScrollPaneServers;
    private javax.swing.JScrollPane jScrollPaneTemplateLogFiles;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparatorCredentials;
    private javax.swing.JSeparator jSeparatorLogFileDescription;
    private javax.swing.JSeparator jSeparatorLogFileFilters;
    private javax.swing.JSeparator jSeparatorServerConnectionProtocol;
    private javax.swing.JSeparator jSeparatorServerMonitoring;
    private javax.swing.JSpinner jSpinnerAutoRefresh;
    private javax.swing.JSpinner jSpinnerLogFileFilterPostPrint;
    private javax.swing.JSpinner jSpinnerLogFileFilterPrePrint;
    private javax.swing.JSpinner jSpinnerMaxNumLines;
    private javax.swing.JTabbedPane jTabbedPaneRoot;
    private javax.swing.JTextArea jTextAreaAlarmFilters;
    private javax.swing.JTextField jTextFieldAlarmFilters;
    private javax.swing.JTextField jTextFieldLogFileFilterKeyword;
    private javax.swing.JTextField jTextFieldLogFileFilterName;
    private javax.swing.JTextField jTextFieldLogFileName;
    private javax.swing.JTextField jTextFieldLogFilePath;
    private javax.swing.JTextField jTextFieldLogFilePrefix;
    private javax.swing.JTextField jTextFieldServerUsername;
    // End of variables declaration//GEN-END:variables
    // </editor-fold>

    private static final int id = 1;
    private DefaultListModel templateLogFilesListModel;
    private DefaultListModel serverListModel;
    private DefaultListModel serverLogFilesListModel;
    private DefaultListModel logFileFilterModel;
    private DefaultListModel alarmFilterListModel;
}
