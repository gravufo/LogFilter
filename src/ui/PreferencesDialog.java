/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ui;

import java.awt.Rectangle;
import java.net.PasswordAuthentication;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import logfilter.Filter;
import logfilter.Log;
import logfilter.Server;
import persistence.Preferences;

/**
 *
 * @author cartin
 */
public class PreferencesDialog extends javax.swing.JDialog
{

    /**
     * Creates new form NewJDialog
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
	
	initComponents();
	
	jListServers.setModel(serverListModel);
	jListLogFiles.setModel(serverLogFilesListModel);
	jListTemplateLogFiles.setModel(templateLogFilesListModel);
	jListLogFileFilters.setModel(logFileFilterModel);
	
	Rectangle r = Preferences.getInstance().getUIPreference(id);
	
	if(r != null)
	{
	    setBounds(r);
	}
	else
	{
	    setLocationRelativeTo(parent);
	}
	
	// TODO Load the saved values (servers, logs, properties, etc.)
	// Make sure they exist before doing so. Else, load defaults
	for(String s : Preferences.getInstance().getServerMap().keySet())
	    serverListModel.addElement(s);
	
	for(String s : Preferences.getInstance().getLogMap().keySet())
	    templateLogFilesListModel.addElement(s);
	
	jTextFieldServerUsername.setText(Preferences.getInstance().getServerAccount().getUserName());
	jPasswordFieldServerPassword.setText(String.valueOf(Preferences.getInstance().getServerAccount().getPassword()));
    }

    private void loadServerProperties()
    {
	if(serverListModel.isEmpty())
	{
	    jButtonAddLogFile.setEnabled(false);
	    jButtonEditServer.setEnabled(false);
	    jButtonRemoveServer.setEnabled(false);
	    jCheckBoxServerPropertiesEnabled.setEnabled(false);
	    jRadioButtonServerConnectionSSH.setEnabled(false);
	    jRadioButtonServerConnectionTelnet.setEnabled(false);
	    jListLogFiles.setEnabled(false);
	    
	    jCheckBoxServerPropertiesEnabled.setSelected(false);
	    jRadioButtonServerConnectionSSH.setSelected(false);
	    jRadioButtonServerConnectionTelnet.setSelected(false);
	    
	    serverLogFilesListModel.clear();
	}
	else
	{
	    Server server = Preferences.getInstance().getServer((String) jListServers.getSelectedValue());

	    jCheckBoxServerPropertiesEnabled.setSelected(server.isEnabled());
	    jRadioButtonServerConnectionSSH.setSelected(server.isUseSSH());
	    jRadioButtonServerConnectionTelnet.setSelected(!server.isUseSSH());

	    // TODO: improvement: sort the list by enabled first (or by user choice, saved index)
	    serverLogFilesListModel.clear();

	    for(String s : server.getLogMap().keySet())
		serverLogFilesListModel.addElement(s);

	    jButtonAddLogFile.setEnabled(true);
	    jButtonEditServer.setEnabled(true);
	    jButtonRemoveServer.setEnabled(true);
	    jCheckBoxServerPropertiesEnabled.setEnabled(true);
	    jRadioButtonServerConnectionSSH.setEnabled(true);
	    jRadioButtonServerConnectionTelnet.setEnabled(true);
	    jListLogFiles.setEnabled(true);
	}
    }
    
    private void loadLogTemplateProperties()
    {
	if(templateLogFilesListModel.isEmpty())
	{
	    jTextFieldLogFileName.setEnabled(false);
	    jTextFieldLogFilePrefix.setEnabled(false);
	    jTextFieldLogFilePath.setEnabled(false);
	    jButtonLogFileFilterAdd.setEnabled(false);
	    jButtonLogFileTemplateRemove.setEnabled(false);
	    
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
	    jButtonLogFileTemplateRemove.setEnabled(true);

	    jTextFieldLogFileName.setText(log.getName());
	    jTextFieldLogFilePrefix.setText(log.getNamePrefix());
	    jTextFieldLogFilePath.setText(log.getFilePath());

	    jListLogFileFilters.setEnabled(true);
	    logFileFilterModel.clear();

	    for(String s : log.getFilterMap().keySet())
		logFileFilterModel.addElement(s);
	}
    }
    
    private void loadLogFileFiltersProperties()
    {
	if(logFileFilterModel.isEmpty())
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
	    jSpinnerLogFileFilterPrePrint.setValue(filter.getLinesBefore());
	    jSpinnerLogFileFilterPostPrint.setValue(filter.getLineAfter());
	}
    }
    
    private void loadServerLogProperties()
    {
	if(serverLogFilesListModel.isEmpty())
	    jButtonRemoveLogFile.setEnabled(false);
	else
	    jButtonRemoveLogFile.setEnabled(true);
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
        jCheckBoxServerPropertiesEnabled = new javax.swing.JCheckBox();
        jRadioButtonServerConnectionSSH = new javax.swing.JRadioButton();
        jLabelServerConnectionProtocol = new javax.swing.JLabel();
        jSeparatorServerConnectionProtocol = new javax.swing.JSeparator();
        jLabelServerMonitoring = new javax.swing.JLabel();
        jSeparatorServerMonitoring = new javax.swing.JSeparator();
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
        jLabelLogFileAlias = new javax.swing.JLabel();
        jTextFieldLogFileName = new javax.swing.JTextField();
        jButtonLogFilesAdd = new javax.swing.JButton();
        jButtonLogFileTemplateRemove = new javax.swing.JButton();
        jPanelMisc = new javax.swing.JPanel();
        jLabelCredentials = new javax.swing.JLabel();
        jSeparatorCredentials = new javax.swing.JSeparator();
        jLabelServerUsername = new javax.swing.JLabel();
        jTextFieldServerUsername = new javax.swing.JTextField();
        jLabelServerPassword = new javax.swing.JLabel();
        jPasswordFieldServerPassword = new javax.swing.JPasswordField();
        jButtonCancel = new javax.swing.JButton();
        jButtonOK = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Preferences");
        setFocusable(false);
        setIconImage(null);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                formWindowClosing(evt);
            }
        });

        jListServers.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListServers.setToolTipText("This list contains all the servers added to the monitoring list, but not necessarily activated.");
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

        jListLogFiles.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListLogFiles.setToolTipText("This list contains the log files to monitor on the selected server.");
        jListLogFiles.setEnabled(false);
        jListLogFiles.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                jListLogFilesValueChanged(evt);
            }
        });
        jScrollPaneLogFiles.setViewportView(jListLogFiles);

        jButtonAddServer.setText("Add...");
        jButtonAddServer.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonAddServerActionPerformed(evt);
            }
        });

        jButtonEditServer.setText("Edit...");
        jButtonEditServer.setEnabled(false);
        jButtonEditServer.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonEditServerActionPerformed(evt);
            }
        });

        jButtonRemoveServer.setText("Remove");
        jButtonRemoveServer.setEnabled(false);
        jButtonRemoveServer.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonRemoveServerActionPerformed(evt);
            }
        });

        jButtonAddLogFile.setText("Add...");
        jButtonAddLogFile.setEnabled(false);
        jButtonAddLogFile.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonAddLogFileActionPerformed(evt);
            }
        });

        jButtonRemoveLogFile.setText("Remove");
        jButtonRemoveLogFile.setEnabled(false);
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
        jRadioButtonServerConnectionTelnet.setOpaque(false);
        jRadioButtonServerConnectionTelnet.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jRadioButtonServerConnectionTelnetActionPerformed(evt);
            }
        });

        jCheckBoxServerPropertiesEnabled.setText("Enabled");
        jCheckBoxServerPropertiesEnabled.setToolTipText("When checked, the selected server will be monitored using the log files shown on the right.");
        jCheckBoxServerPropertiesEnabled.setEnabled(false);
        jCheckBoxServerPropertiesEnabled.setOpaque(false);
        jCheckBoxServerPropertiesEnabled.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxServerPropertiesEnabledActionPerformed(evt);
            }
        });

        buttonGroupServerProperties.add(jRadioButtonServerConnectionSSH);
        jRadioButtonServerConnectionSSH.setText("SSH");
        jRadioButtonServerConnectionSSH.setEnabled(false);
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

        jLabelServerMonitoring.setText("Server Monitoring");
        jLabelServerMonitoring.setFocusable(false);

        javax.swing.GroupLayout jPanelServerPropertiesLayout = new javax.swing.GroupLayout(jPanelServerProperties);
        jPanelServerProperties.setLayout(jPanelServerPropertiesLayout);
        jPanelServerPropertiesLayout.setHorizontalGroup(
            jPanelServerPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparatorServerConnectionProtocol)
            .addGroup(jPanelServerPropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelServerPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelServerConnectionProtocol)
                    .addComponent(jCheckBoxServerPropertiesEnabled)
                    .addComponent(jLabelServerMonitoring))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanelServerPropertiesLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jRadioButtonServerConnectionTelnet)
                .addGap(18, 18, 18)
                .addComponent(jRadioButtonServerConnectionSSH)
                .addGap(0, 60, Short.MAX_VALUE))
            .addComponent(jSeparatorServerMonitoring)
        );
        jPanelServerPropertiesLayout.setVerticalGroup(
            jPanelServerPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelServerPropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelServerMonitoring)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparatorServerMonitoring, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxServerPropertiesEnabled)
                .addGap(40, 40, 40)
                .addComponent(jLabelServerConnectionProtocol)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparatorServerConnectionProtocol, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelServerPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonServerConnectionTelnet)
                    .addComponent(jRadioButtonServerConnectionSSH))
                .addContainerGap(177, Short.MAX_VALUE))
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
                        .addGroup(jPanelServersAndLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelServersAndLogsLayout.createSequentialGroup()
                                .addComponent(jButtonAddLogFile)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButtonRemoveLogFile)
                                .addGap(0, 57, Short.MAX_VALUE))
                            .addComponent(jLabelLogFiles, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelServersAndLogsLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPaneLogFiles, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jTabbedPaneRoot.addTab("Servers", jPanelServersAndLogs);

        jListTemplateLogFiles.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
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
        jLabelTemplateLogFiles.setText("Log Files");
        jLabelTemplateLogFiles.setFocusable(false);

        jPanelTemplateLogFileProperties.setBackground(new java.awt.Color(204, 204, 204));

        jLabelTemplateLogFileName.setText("Prefix of the log file:");
        jLabelTemplateLogFileName.setFocusable(false);

        jLabelTemplateLogFilePath.setText("Path to the log file location:");
        jLabelTemplateLogFilePath.setFocusable(false);

        jTextFieldLogFilePath.setEnabled(false);
        jTextFieldLogFilePath.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                jTextFieldLogFilePathFocusLost(evt);
            }
        });

        jTextFieldLogFilePrefix.setEnabled(false);
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
        jButtonLogFileFilterAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonLogFileFilterAddActionPerformed(evt);
            }
        });

        jButtonLogFileFilterRemove.setText("Remove");
        jButtonLogFileFilterRemove.setEnabled(false);
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
        jCheckBoxLogFileFilterEnabled.setOpaque(false);

        jLabelLogFileFilterPostPrint.setText("Number of lines to display after:");
        jLabelLogFileFilterPostPrint.setFocusable(false);

        jLabelLogFileFilterPrePrint.setText("Number of lines to display before:");
        jLabelLogFileFilterPrePrint.setFocusable(false);

        jSpinnerLogFileFilterPrePrint.setEnabled(false);

        jSpinnerLogFileFilterPostPrint.setEnabled(false);

        jLabelLogFileFilterKeyword.setText("String to find:");
        jLabelLogFileFilterKeyword.setFocusable(false);

        jTextFieldLogFileFilterKeyword.setEnabled(false);

        jLabelLogFileFilterName.setText("Name of the filter:");
        jLabelLogFileFilterName.setFocusable(false);

        jTextFieldLogFileFilterName.setEnabled(false);

        javax.swing.GroupLayout jPanelFilterPropertiesLayout = new javax.swing.GroupLayout(jPanelFilterProperties);
        jPanelFilterProperties.setLayout(jPanelFilterPropertiesLayout);
        jPanelFilterPropertiesLayout.setHorizontalGroup(
            jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFilterPropertiesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelFilterPropertiesLayout.createSequentialGroup()
                        .addGroup(jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelFilterPropertiesLayout.createSequentialGroup()
                                .addGroup(jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabelLogFileFilterPrePrint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabelLogFileFilterPostPrint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jSpinnerLogFileFilterPrePrint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jSpinnerLogFileFilterPostPrint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jCheckBoxLogFileFilterEnabled))
                        .addGap(0, 74, Short.MAX_VALUE))
                    .addGroup(jPanelFilterPropertiesLayout.createSequentialGroup()
                        .addGroup(jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelLogFileFilterName)
                            .addComponent(jLabelLogFileFilterKeyword))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldLogFileFilterName)
                            .addComponent(jTextFieldLogFileFilterKeyword))))
                .addContainerGap())
        );
        jPanelFilterPropertiesLayout.setVerticalGroup(
            jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFilterPropertiesLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jCheckBoxLogFileFilterEnabled)
                .addGap(18, 18, 18)
                .addGroup(jPanelFilterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelLogFileFilterName)
                    .addComponent(jTextFieldLogFileFilterName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
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

        jLabelLogFileAlias.setText("Name or alias:");

        jTextFieldLogFileName.setEnabled(false);
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
                    .addComponent(jScrollPaneLogFileFilters)
                    .addGroup(jPanelTemplateLogFilePropertiesLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanelFilterProperties, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelTemplateLogFilePropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonLogFileFilterAdd)
                            .addComponent(jButtonLogFileFilterRemove))))
                .addContainerGap())
        );

        jButtonLogFilesAdd.setText("Add");
        jButtonLogFilesAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonLogFilesAddActionPerformed(evt);
            }
        });

        jButtonLogFileTemplateRemove.setText("Remove");
        jButtonLogFileTemplateRemove.setEnabled(false);
        jButtonLogFileTemplateRemove.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonLogFileTemplateRemoveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelTemplateLogFilesLayout = new javax.swing.GroupLayout(jPanelTemplateLogFiles);
        jPanelTemplateLogFiles.setLayout(jPanelTemplateLogFilesLayout);
        jPanelTemplateLogFilesLayout.setHorizontalGroup(
            jPanelTemplateLogFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTemplateLogFilesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelTemplateLogFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelTemplateLogFiles, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPaneTemplateLogFiles, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanelTemplateLogFilesLayout.createSequentialGroup()
                        .addComponent(jButtonLogFilesAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonLogFileTemplateRemove)))
                .addGap(18, 18, 18)
                .addComponent(jPanelTemplateLogFileProperties, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanelTemplateLogFilesLayout.setVerticalGroup(
            jPanelTemplateLogFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTemplateLogFilesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelTemplateLogFiles)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPaneTemplateLogFiles, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelTemplateLogFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonLogFilesAdd)
                    .addComponent(jButtonLogFileTemplateRemove))
                .addContainerGap())
            .addComponent(jPanelTemplateLogFileProperties, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPaneRoot.addTab("Template Log Files", jPanelTemplateLogFiles);

        jPanelMisc.setFocusable(false);

        jLabelCredentials.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabelCredentials.setText("Credentials");
        jLabelCredentials.setFocusable(false);

        jLabelServerUsername.setText("Username:");
        jLabelServerUsername.setFocusable(false);

        jTextFieldServerUsername.setText("bwadmin");
        jTextFieldServerUsername.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                jTextFieldServerUsernameFocusLost(evt);
            }
        });
        jTextFieldServerUsername.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jTextFieldServerUsernameActionPerformed(evt);
            }
        });

        jLabelServerPassword.setText("Password:");
        jLabelServerPassword.setFocusable(false);

        jPasswordFieldServerPassword.setText("bwadmin");
        jPasswordFieldServerPassword.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(java.awt.event.FocusEvent evt)
            {
                jPasswordFieldServerPasswordFocusLost(evt);
            }
        });

        javax.swing.GroupLayout jPanelMiscLayout = new javax.swing.GroupLayout(jPanelMisc);
        jPanelMisc.setLayout(jPanelMiscLayout);
        jPanelMiscLayout.setHorizontalGroup(
            jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparatorCredentials)
            .addGroup(jPanelMiscLayout.createSequentialGroup()
                .addContainerGap()
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
                .addContainerGap(494, Short.MAX_VALUE))
        );
        jPanelMiscLayout.setVerticalGroup(
            jPanelMiscLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMiscLayout.createSequentialGroup()
                .addContainerGap()
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
                .addContainerGap(321, Short.MAX_VALUE))
        );

        jTabbedPaneRoot.addTab("Miscellaneous", jPanelMisc);

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonCancelActionPerformed(evt);
            }
        });

        jButtonOK.setText("OK");
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
                .addComponent(jTabbedPaneRoot, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCancel)
                    .addComponent(jButtonOK))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonAddServerActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAddServerActionPerformed
    {//GEN-HEADEREND:event_jButtonAddServerActionPerformed
        boolean success;
	
	do
	{
	    String[] server = new ServerDialog(this, true, false, null).showDialog();
	
	    // Verify if the user cancelled
	    if(server[0].equals("") || server[1].equals(""))
	    {
		break;
	    }
	    
	    // Verify the input
	    if(!serverListModel.contains(server[0]))
	    {
		// Add it to the respective places
		serverListModel.addElement(server[0]);
		Preferences.getInstance().addServer(new Server(server[0], server[1]));
		
		// Set the added object to selected
		jListServers.setSelectedIndex(serverListModel.size() - 1);
		
		// Set added server as selected and load its properties
		jListServers.setSelectedIndex(serverListModel.indexOf(server[0]));
		
		success = true;
	    }
	    else // Restart if the input was wrong
	    {
		JOptionPane.showMessageDialog(this, "There is another server with the same name!");
		success = false;
	    }
	}while(!success);
	
    }//GEN-LAST:event_jButtonAddServerActionPerformed

    private void jButtonLogFilesAddActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonLogFilesAddActionPerformed
    {//GEN-HEADEREND:event_jButtonLogFilesAddActionPerformed
	String name = "New Log File";
	
	if(templateLogFilesListModel.contains(name))
	{
	    int increment = 1;
	    while(templateLogFilesListModel.contains(name + " " + increment))
		++increment;
	    templateLogFilesListModel.addElement(name += " " + increment);
	}
	else
	    templateLogFilesListModel.addElement(name);
	
	// Select the new item and save in prefs
	Preferences.getInstance().addLog(new Log(name));
	jListTemplateLogFiles.setSelectedIndex(templateLogFilesListModel.size() - 1);
    }//GEN-LAST:event_jButtonLogFilesAddActionPerformed

    private void jTextFieldServerUsernameActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jTextFieldServerUsernameActionPerformed
    {//GEN-HEADEREND:event_jTextFieldServerUsernameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldServerUsernameActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonCancelActionPerformed
    {//GEN-HEADEREND:event_jButtonCancelActionPerformed
	// Cancel changes before saving window prefs
	Preferences.getInstance().cancel();

	// For consistency, we save window location and size
	Preferences.getInstance().setUIPreference(id, getBounds());
	Preferences.getInstance().save();

	// No need to save, so we just close pref window
        dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonOKActionPerformed
    {//GEN-HEADEREND:event_jButtonOKActionPerformed
        // Save window location and size
	Preferences.getInstance().setUIPreference(id, getBounds());
	
	// Save all the modifications
	Preferences.getInstance().save();
	
        // Close pref window
        dispose();
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
    {//GEN-HEADEREND:event_formWindowClosing
        // Cancel changes before saving window prefs
	Preferences.getInstance().cancel();
	
	Preferences.getInstance().setUIPreference(id, getBounds());
	
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
	    String[] server = new ServerDialog(this, true, true, serverToEdit).showDialog();

	    // Remove the server temporarily
	    serverListModel.removeElement((String) jListServers.getSelectedValue());
	    
	    // If the user cancelled
	    if(server[0].equals("") || server[1].equals(""))
	    {
		serverListModel.add(index, serverToEdit.getName());
		break;
	    }
	    
	    // Else we verify the input
	    if(!serverListModel.contains(server[0]))
	    {
		// Insert if correct
		serverListModel.add(index, server[0]);
		
		// Apply the changes to the prefs
		Preferences.getInstance().removeServer(serverToEdit.getName());
		Preferences.getInstance().addServer(new Server(server[0], server[1], serverToEdit));
		
		success = true;
	    }
	    else // Restart if the input was wrong
	    {
		JOptionPane.showMessageDialog(this, "There is another server with the same name!");
		success = false;
	    }
	}while(!success);
	
	// Reselect the server in the list no matter what
	jListServers.setSelectedIndex(index);
    }//GEN-LAST:event_jButtonEditServerActionPerformed

    private void jButtonRemoveServerActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonRemoveServerActionPerformed
    {//GEN-HEADEREND:event_jButtonRemoveServerActionPerformed
        // Find the ID of the server we want to edit
	Server serverToEdit = Preferences.getInstance().getServer((String) jListServers.getSelectedValue());
	
	// Remove it from the list
	serverListModel.removeElement((String) jListServers.getSelectedValue());
	
	// Remove it from the prefs
	Preferences.getInstance().removeServer(serverToEdit.getName());
	
	// Change list selection to first element and load its properties
	jListServers.setSelectedIndex(0);
	
	// Reload properties
	loadServerProperties();
    }//GEN-LAST:event_jButtonRemoveServerActionPerformed

    private void jCheckBoxServerPropertiesEnabledActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxServerPropertiesEnabledActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxServerPropertiesEnabledActionPerformed
        if(jCheckBoxServerPropertiesEnabled.isSelected())
	    Preferences.getInstance().getServer((String) jListServers.getSelectedValue()).setEnabled(true);
	else
	    Preferences.getInstance().getServer((String) jListServers.getSelectedValue()).setEnabled(false);
    }//GEN-LAST:event_jCheckBoxServerPropertiesEnabledActionPerformed

    private void jRadioButtonServerConnectionTelnetActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jRadioButtonServerConnectionTelnetActionPerformed
    {//GEN-HEADEREND:event_jRadioButtonServerConnectionTelnetActionPerformed
	if(jRadioButtonServerConnectionTelnet.isSelected())
	    Preferences.getInstance().getServer((String) jListServers.getSelectedValue()).setUseSSH(false);
	else
	    Preferences.getInstance().getServer((String) jListServers.getSelectedValue()).setUseSSH(true);
    }//GEN-LAST:event_jRadioButtonServerConnectionTelnetActionPerformed

    private void jRadioButtonServerConnectionSSHActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jRadioButtonServerConnectionSSHActionPerformed
    {//GEN-HEADEREND:event_jRadioButtonServerConnectionSSHActionPerformed
        if(jRadioButtonServerConnectionSSH.isSelected())
	    Preferences.getInstance().getServer((String) jListServers.getSelectedValue()).setUseSSH(true);
	else
	    Preferences.getInstance().getServer((String) jListServers.getSelectedValue()).setUseSSH(false);
    }//GEN-LAST:event_jRadioButtonServerConnectionSSHActionPerformed

    private void jListServersValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jListServersValueChanged
    {//GEN-HEADEREND:event_jListServersValueChanged
	if(jListServers.getSelectedValue() != null)
	    loadServerProperties();
    }//GEN-LAST:event_jListServersValueChanged

    private void jTextFieldServerUsernameFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_jTextFieldServerUsernameFocusLost
    {//GEN-HEADEREND:event_jTextFieldServerUsernameFocusLost
        Preferences.getInstance().setServerAccount(new PasswordAuthentication(jTextFieldServerUsername.getText(), jPasswordFieldServerPassword.getPassword()));
    }//GEN-LAST:event_jTextFieldServerUsernameFocusLost

    private void jPasswordFieldServerPasswordFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_jPasswordFieldServerPasswordFocusLost
    {//GEN-HEADEREND:event_jPasswordFieldServerPasswordFocusLost
        Preferences.getInstance().setServerAccount(new PasswordAuthentication(jTextFieldServerUsername.getText(), jPasswordFieldServerPassword.getPassword()));
    }//GEN-LAST:event_jPasswordFieldServerPasswordFocusLost

    private void jButtonLogFileTemplateRemoveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonLogFileTemplateRemoveActionPerformed
    {//GEN-HEADEREND:event_jButtonLogFileTemplateRemoveActionPerformed
        templateLogFilesListModel.removeElement((String) jListTemplateLogFiles.getSelectedValue());
	Preferences.getInstance().removeLog((String) jListTemplateLogFiles.getSelectedValue());
	jListTemplateLogFiles.setSelectedIndex(0);
	loadLogTemplateProperties();
    }//GEN-LAST:event_jButtonLogFileTemplateRemoveActionPerformed

    private void jListTemplateLogFilesValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jListTemplateLogFilesValueChanged
    {//GEN-HEADEREND:event_jListTemplateLogFilesValueChanged
	if(jListTemplateLogFiles.getSelectedValue() != null)
	    loadLogTemplateProperties();
    }//GEN-LAST:event_jListTemplateLogFilesValueChanged

    private void jTextFieldLogFileNameFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_jTextFieldLogFileNameFocusLost
    {//GEN-HEADEREND:event_jTextFieldLogFileNameFocusLost
	// Remove old value, but save the index for the new value
	int index = jListTemplateLogFiles.getSelectedIndex();
	
	Log editedLog = Preferences.getInstance().getLog((String) jListTemplateLogFiles.getSelectedValue());
	
	// Make sure there actually is a log selected
	if(editedLog == null || index == -1 || jTextFieldLogFileName.getText().equals((String) jListLogFiles.getSelectedValue()))
	{
	    return;
	}
	
	if(jTextFieldLogFileName.getText().equals(""))
	{
	    JOptionPane.showMessageDialog(this, "Name cannot be empty!");
	    jTextFieldLogFileName.setText((String) jListTemplateLogFiles.getSelectedValue());
	}
	
	Preferences.getInstance().removeLog((String) jListTemplateLogFiles.getSelectedValue());
	
	// Modify the value
	editedLog.setName(jTextFieldLogFileName.getText());
	
	// Insert into list and prefs
	Preferences.getInstance().addLog(editedLog);
	templateLogFilesListModel.set(index, editedLog.getName());
	
	// Select it
	jListTemplateLogFiles.setSelectedIndex(index);
    }//GEN-LAST:event_jTextFieldLogFileNameFocusLost

    private void jTextFieldLogFilePrefixFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_jTextFieldLogFilePrefixFocusLost
    {//GEN-HEADEREND:event_jTextFieldLogFilePrefixFocusLost
        int index = jListTemplateLogFiles.getSelectedIndex();
	
	Log logToEdit = Preferences.getInstance().getLog((String) jListTemplateLogFiles.getSelectedValue());
	
	// Make sure there actually is a log selected
	if(index == -1 || logToEdit == null)
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
	if(index == -1 || logToEdit == null)
	{
	    return;
	}
	
	logToEdit.setFilePath(jTextFieldLogFilePath.getText());
    }//GEN-LAST:event_jTextFieldLogFilePathFocusLost

    private void jButtonLogFileFilterAddActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonLogFileFilterAddActionPerformed
    {//GEN-HEADEREND:event_jButtonLogFileFilterAddActionPerformed
        // Create a new Filter
	
	String name = "New Filter";
	
	if(logFileFilterModel.contains(name))
	{
	    int increment = 1;
	    while(logFileFilterModel.contains(name + " " + increment))
		++increment;
	    logFileFilterModel.addElement(name += " " + increment);
	}
	else
	    logFileFilterModel.addElement(name);
	
	// Add the filter to the log template
	Preferences.getInstance().getLog((String) jListTemplateLogFiles.getSelectedValue()).addFilter(new Filter(name));
	
	// Select the filter (properties update will be done automagically)
	jListLogFileFilters.setSelectedIndex(logFileFilterModel.size() - 1);
    }//GEN-LAST:event_jButtonLogFileFilterAddActionPerformed

    private void jListLogFileFiltersValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jListLogFileFiltersValueChanged
    {//GEN-HEADEREND:event_jListLogFileFiltersValueChanged
	if(jListLogFileFilters.getSelectedValue() != null)
	    loadLogFileFiltersProperties();
    }//GEN-LAST:event_jListLogFileFiltersValueChanged

    private void jButtonLogFileFilterRemoveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonLogFileFilterRemoveActionPerformed
    {//GEN-HEADEREND:event_jButtonLogFileFilterRemoveActionPerformed
        // Find the log that owns this filter
	Log log = Preferences.getInstance().getLog((String)jListTemplateLogFiles.getSelectedValue());
	
	// Remove the filter from that log and the list
	log.removeFilter((String) jListLogFileFilters.getSelectedValue());
	logFileFilterModel.removeElement((String) jListLogFileFilters.getSelectedValue());
	
	// Update selection in the list
	jListLogFileFilters.setSelectedIndex(0);
	
	// Reload properties in case list is empty (then it won't update)
	loadLogFileFiltersProperties();
    }//GEN-LAST:event_jButtonLogFileFilterRemoveActionPerformed

    private void jButtonAddLogFileActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAddLogFileActionPerformed
    {//GEN-HEADEREND:event_jButtonAddLogFileActionPerformed
        // Let the user select the right log
	String selectedLog = new LogFileChooser(this, true, templateLogFilesListModel, serverLogFilesListModel).showDialog();
	
	// Verify if the user cancelled
	if(selectedLog.equals(""))
	    return;
	
	// Add it to the server
	Preferences.getInstance().getServer((String) jListServers.getSelectedValue());
	
	serverLogFilesListModel.addElement(selectedLog);
	jListLogFiles.setSelectedIndex(serverLogFilesListModel.indexOf(selectedLog));
    }//GEN-LAST:event_jButtonAddLogFileActionPerformed

    private void jListLogFilesValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jListLogFilesValueChanged
    {//GEN-HEADEREND:event_jListLogFilesValueChanged
        loadServerLogProperties();
    }//GEN-LAST:event_jListLogFilesValueChanged

    private void jButtonRemoveLogFileActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonRemoveLogFileActionPerformed
    {//GEN-HEADEREND:event_jButtonRemoveLogFileActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonRemoveLogFileActionPerformed

    // <editor-fold defaultstate="collapsed" desc="Generated Variables">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupServerProperties;
    private javax.swing.JButton jButtonAddLogFile;
    private javax.swing.JButton jButtonAddServer;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonEditServer;
    private javax.swing.JButton jButtonLogFileFilterAdd;
    private javax.swing.JButton jButtonLogFileFilterRemove;
    private javax.swing.JButton jButtonLogFileTemplateRemove;
    private javax.swing.JButton jButtonLogFilesAdd;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JButton jButtonRemoveLogFile;
    private javax.swing.JButton jButtonRemoveServer;
    private javax.swing.JCheckBox jCheckBoxLogFileFilterEnabled;
    private javax.swing.JCheckBox jCheckBoxServerPropertiesEnabled;
    private javax.swing.JLabel jLabelCredentials;
    private javax.swing.JLabel jLabelLogFileAlias;
    private javax.swing.JLabel jLabelLogFileDescription;
    private javax.swing.JLabel jLabelLogFileFilterKeyword;
    private javax.swing.JLabel jLabelLogFileFilterName;
    private javax.swing.JLabel jLabelLogFileFilterPostPrint;
    private javax.swing.JLabel jLabelLogFileFilterPrePrint;
    private javax.swing.JLabel jLabelLogFileFilters;
    private javax.swing.JLabel jLabelLogFiles;
    private javax.swing.JLabel jLabelServerConnectionProtocol;
    private javax.swing.JLabel jLabelServerMonitoring;
    private javax.swing.JLabel jLabelServerPassword;
    private javax.swing.JLabel jLabelServerProperties;
    private javax.swing.JLabel jLabelServerUsername;
    private javax.swing.JLabel jLabelServers;
    private javax.swing.JLabel jLabelTemplateLogFileName;
    private javax.swing.JLabel jLabelTemplateLogFilePath;
    private javax.swing.JLabel jLabelTemplateLogFiles;
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
    private javax.swing.JScrollPane jScrollPaneLogFileFilters;
    private javax.swing.JScrollPane jScrollPaneLogFiles;
    private javax.swing.JScrollPane jScrollPaneServers;
    private javax.swing.JScrollPane jScrollPaneTemplateLogFiles;
    private javax.swing.JSeparator jSeparatorCredentials;
    private javax.swing.JSeparator jSeparatorLogFileDescription;
    private javax.swing.JSeparator jSeparatorLogFileFilters;
    private javax.swing.JSeparator jSeparatorServerConnectionProtocol;
    private javax.swing.JSeparator jSeparatorServerMonitoring;
    private javax.swing.JSpinner jSpinnerLogFileFilterPostPrint;
    private javax.swing.JSpinner jSpinnerLogFileFilterPrePrint;
    private javax.swing.JTabbedPane jTabbedPaneRoot;
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
}
