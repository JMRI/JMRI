package jmri.jmrit.permission.swing;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.*;

import jmri.*;
import jmri.util.swing.JmriJOptionPane;

/**
 * Dialog for user login.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class LoginDialog extends JDialog {

    private final JTextField _usernameTextField;
    private final JPasswordField _passwordTextField;


    public LoginDialog(Frame owner) {
        super(owner, Bundle.getMessage("LoginAction_Title"), true);

        JPanel contentPanel = new JPanel();
        rootPane.getContentPane().add(contentPanel);

        JPanel p = contentPanel;

        p.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        contentPanel.add(new JLabel(Bundle.getMessage("AddUserDialog_UserName")), c);
        c.gridy = 1;
        contentPanel.add(new JLabel(Bundle.getMessage("AddUserDialog_Password")), c);

        c.gridx = 1;
        c.gridy = 0;
        contentPanel.add(Box.createHorizontalStrut(5), c);

        c.gridx = 2;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        _usernameTextField = new JTextField(20);
        contentPanel.add(_usernameTextField, c);
        c.gridy = 1;
        _passwordTextField = new JPasswordField(20);
        contentPanel.add(_passwordTextField, c);

        JPanel buttonPanel = new JPanel();
        JButton buttonCancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        buttonPanel.add(buttonCancel);
        buttonCancel.addActionListener((ActionEvent e) -> {
            dispose();
        });
        //buttonCancel.setToolTipText(Bundle.getMessage("LoginCancelButtonHint");      // NOI18N

        // OK
        JButton buttonOK = new JButton(Bundle.getMessage("ButtonOK"));    // NOI18N
        buttonPanel.add(buttonOK);
        buttonOK.addActionListener((ActionEvent e) -> {
            PermissionManager mngr = InstanceManager.getDefault(PermissionManager.class);
            if (mngr.isAGuestUser(_usernameTextField.getText())) {
                // default guest user does log in
                JmriJOptionPane.showMessageDialog(null,
                        Bundle.getMessage("LoginAction_GuestMessage"),
                        jmri.Application.getApplicationName(),
                        JmriJOptionPane.ERROR_MESSAGE);
            } else {
                String password = new String(_passwordTextField.getPassword());
                if (mngr.login(_usernameTextField.getText(), password)) {

                    JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("LoginAction_UserLoggedIn"), jmri.Application.getApplicationName(), JmriJOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            }
        });
        //buttonOK.setToolTipText(Bundle.getMessage("LoginOkButtonHint");      // NOI18N
        getRootPane().setDefaultButton(buttonOK);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        contentPanel.add(buttonPanel, c);

        setLocationRelativeTo(owner);
        pack();
    }

}
