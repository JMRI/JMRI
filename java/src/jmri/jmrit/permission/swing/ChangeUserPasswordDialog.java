package jmri.jmrit.permission.swing;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.*;

import jmri.*;
import jmri.util.swing.JmriJOptionPane;

/**
 * Dialog to change a user password.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class ChangeUserPasswordDialog extends JDialog {

    private final PermissionManager _mngr;
    private final JPasswordField _passwordTextField;
    private final JPasswordField _secondPasswordTextField;


    public ChangeUserPasswordDialog(
            PermissionManager mngr, Frame owner, User user, Runnable passwordChangedRunnable) {

        super(owner, Bundle.getMessage("ChangeUserPasswordDialog_ChangePasswordTitle"), true);

        this._mngr = mngr;

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
        c.gridy = 2;
        contentPanel.add(new JLabel(Bundle.getMessage("AddUserDialog_PasswordAgain")), c);

        c.gridx = 1;
        c.gridy = 0;
        contentPanel.add(Box.createHorizontalStrut(5), c);

        c.gridx = 2;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        contentPanel.add(new JLabel(user.getUserName()), c);
        c.gridy = 1;
        _passwordTextField = new JPasswordField(20);
        contentPanel.add(_passwordTextField, c);
        c.gridy = 2;
        _secondPasswordTextField = new JPasswordField(20);
        contentPanel.add(_secondPasswordTextField, c);

        JPanel buttonPanel = new JPanel();
        JButton buttonCancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        buttonPanel.add(buttonCancel);
        buttonCancel.addActionListener((ActionEvent e) -> {
            dispose();
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        buttonCancel.setToolTipText("CancelLogixButtonHint");      // NOI18N

        // OK
        JButton buttonOK = new JButton(Bundle.getMessage("ButtonOK"));    // NOI18N
        buttonPanel.add(buttonOK);
        buttonOK.addActionListener((ActionEvent e) -> {
            if (okPressed(user)) {
                passwordChangedRunnable.run();
                dispose();
            }
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        buttonOK.setToolTipText("CancelLogixButtonHint");      // NOI18N

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        contentPanel.add(buttonPanel, c);

        setLocationRelativeTo(owner);
        pack();
    }

    private boolean okPressed(User user) {
        String passwd1 = new String(_passwordTextField.getPassword());
        String passwd2 = new String(_secondPasswordTextField.getPassword());

        if (passwd1.isBlank() && !_mngr.isAllowEmptyPasswords()) {
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("AddUserDialog_PasswordEmpty"),
                    jmri.Application.getApplicationName(),
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!passwd1.equals(passwd1.trim())) {
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("AddUserDialog_SpaceNotAllowedInPassword"),
                    jmri.Application.getApplicationName(),
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!passwd1.equals(passwd2)) {
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("AddUserDialog_PasswordsAreNotEqual"),
                    jmri.Application.getApplicationName(),
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }

        user.setPassword(passwd1);
        return true;
    }

}
