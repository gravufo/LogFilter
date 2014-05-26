/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 *
 * @author cartin
 */
public class ServerDialog extends javax.swing.JDialog
{

    /**
     * Creates new form NewJDialog
     *
     * @param parent Parent component
     * @param modal  Modality of this dialog
     * @param isEdit True if this is a server edit, false otherwise
     * @param name     The name of the server to edit, if isEdit is true
     * @param hostname The hostname of the server to edit, if isEdit is true
     */
    public ServerDialog(JDialog parent, boolean modal, boolean isEdit, String name, String hostname)
    {
	super(parent, modal);
	initComponents();

	if (isEdit)
	{
	    jTextFieldServerName.setText(name);
	    jTextFieldHostname.setText(hostname);
	}

	setLocationRelativeTo(parent);
	setResizable(false);

	ActionListener escListener = new ActionListener()
	{
	    @Override
	    public void actionPerformed(ActionEvent e)
	    {
		cancel();
	    }
	};

	getRootPane().registerKeyboardAction(escListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
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
        jLabelHostname = new javax.swing.JLabel();
        jTextFieldHostname = new javax.swing.JTextField();
        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jLabelServerName = new javax.swing.JLabel();
        jTextFieldServerName = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("New Server");
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                formWindowClosing(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyReleased(java.awt.event.KeyEvent evt)
            {
                formKeyReleased(evt);
            }
        });

        jPanelRoot.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyReleased(java.awt.event.KeyEvent evt)
            {
                jPanelRootKeyReleased(evt);
            }
        });

        jLabelHostname.setText("Hostname or IP address:");
        jLabelHostname.setFocusable(false);

        jTextFieldHostname.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyPressed(java.awt.event.KeyEvent evt)
            {
                jTextFieldHostnameKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt)
            {
                jTextFieldHostnameKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt)
            {
                jTextFieldHostnameKeyTyped(evt);
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

        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonCancelActionPerformed(evt);
            }
        });

        jLabelServerName.setText("Server name:");

        jTextFieldServerName.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyPressed(java.awt.event.KeyEvent evt)
            {
                jTextFieldServerNameKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt)
            {
                jTextFieldServerNameKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt)
            {
                jTextFieldServerNameKeyTyped(evt);
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
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonOK, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButtonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelRootLayout.createSequentialGroup()
                        .addGroup(jPanelRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelHostname)
                            .addComponent(jLabelServerName))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldHostname, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .addComponent(jTextFieldServerName))))
                .addContainerGap())
        );
        jPanelRootLayout.setVerticalGroup(
            jPanelRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelRootLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelServerName)
                    .addComponent(jTextFieldServerName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanelRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelHostname)
                    .addComponent(jTextFieldHostname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanelRootLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonOK)
                    .addComponent(jButtonCancel))
                .addContainerGap())
        );

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

    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonOKActionPerformed
    {//GEN-HEADEREND:event_jButtonOKActionPerformed
	verifyInput();
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonCancelActionPerformed
    {//GEN-HEADEREND:event_jButtonCancelActionPerformed
	cancel();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
    {//GEN-HEADEREND:event_formWindowClosing
    }//GEN-LAST:event_formWindowClosing

    private void jTextFieldHostnameKeyTyped(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTextFieldHostnameKeyTyped
    {//GEN-HEADEREND:event_jTextFieldHostnameKeyTyped
    }//GEN-LAST:event_jTextFieldHostnameKeyTyped

    private void jTextFieldServerNameKeyTyped(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTextFieldServerNameKeyTyped
    {//GEN-HEADEREND:event_jTextFieldServerNameKeyTyped
    }//GEN-LAST:event_jTextFieldServerNameKeyTyped

    private void jTextFieldServerNameKeyReleased(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTextFieldServerNameKeyReleased
    {//GEN-HEADEREND:event_jTextFieldServerNameKeyReleased
    }//GEN-LAST:event_jTextFieldServerNameKeyReleased

    private void jTextFieldHostnameKeyReleased(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTextFieldHostnameKeyReleased
    {//GEN-HEADEREND:event_jTextFieldHostnameKeyReleased
    }//GEN-LAST:event_jTextFieldHostnameKeyReleased

    private void jPanelRootKeyReleased(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jPanelRootKeyReleased
    {//GEN-HEADEREND:event_jPanelRootKeyReleased
    }//GEN-LAST:event_jPanelRootKeyReleased

    private void formKeyReleased(java.awt.event.KeyEvent evt)//GEN-FIRST:event_formKeyReleased
    {//GEN-HEADEREND:event_formKeyReleased
    }//GEN-LAST:event_formKeyReleased

    private void jTextFieldServerNameKeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTextFieldServerNameKeyPressed
    {//GEN-HEADEREND:event_jTextFieldServerNameKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
	{
	    verifyInput();
	}
    }//GEN-LAST:event_jTextFieldServerNameKeyPressed

    private void jTextFieldHostnameKeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event_jTextFieldHostnameKeyPressed
    {//GEN-HEADEREND:event_jTextFieldHostnameKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
	{
	    verifyInput();
	}
    }//GEN-LAST:event_jTextFieldHostnameKeyPressed

    /**
     *
     * @return Two dimensional string where [0] is the name/alias and [1] is the
     *         hostname/ip
     */
    public String[] showDialog()
    {
	setVisible(true);

	return new String[]
	{
	    jTextFieldServerName.getText(), jTextFieldHostname.getText()
	};
    }

    private void verifyInput()
    {
	// Verify the input
	if (jTextFieldHostname.getText().equals("") || jTextFieldServerName.getText().equals(""))
	{
	    JOptionPane.showMessageDialog(this, "The hostname and/or name fields cannot be empty");
	}
	else
	{
	    dispose();
	}
    }

    private void cancel()
    {
	setVisible(false);
	jTextFieldServerName.setText("");
	jTextFieldHostname.setText("");
	dispose();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JLabel jLabelHostname;
    private javax.swing.JLabel jLabelServerName;
    private javax.swing.JPanel jPanelRoot;
    private javax.swing.JTextField jTextFieldHostname;
    private javax.swing.JTextField jTextFieldServerName;
    // End of variables declaration//GEN-END:variables

    private final int id = 2;
}
