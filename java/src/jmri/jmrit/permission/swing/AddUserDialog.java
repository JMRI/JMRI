package jmri.jmrit.permission.swing;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.*;

import jmri.*;
import jmri.util.swing.JmriJOptionPane;

/**
 * Dialog to add user
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class AddUserDialog extends JDialog {

    public interface UserAdded {
        void userAdded(User user);
    }

    private final PermissionManager _mngr;
    private final UserAdded _userAdded;
    private final JTextField _usernameTextField;
    private final JPasswordField _passwordTextField;
    private final JPasswordField _secondPasswordTextField;
    private final JTextField _nameTextField;
    private final JTextField _commentTextField;


    public AddUserDialog(PermissionManager mngr, Frame owner, UserAdded userAdded) {
        super(owner, Bundle.getMessage("AddUserDialog_AddUserTitle"), true);

        this._mngr = mngr;
        this._userAdded = userAdded;

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
        c.gridy = 3;
        contentPanel.add(new JLabel(Bundle.getMessage("AddUserDialog_Name")), c);
        c.gridy = 4;
        contentPanel.add(new JLabel(Bundle.getMessage("AddUserDialog_Comment")), c);

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
        c.gridy = 2;
        _secondPasswordTextField = new JPasswordField(20);
        contentPanel.add(_secondPasswordTextField, c);
        c.gridy = 3;
        _nameTextField = new JTextField(40);
        contentPanel.add(_nameTextField, c);
        c.gridy = 4;
        _commentTextField = new JTextField(40);
        contentPanel.add(_commentTextField, c);

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
            if (okPressed()) {
                dispose();
            }
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        buttonOK.setToolTipText("CancelLogixButtonHint");      // NOI18N

        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 2;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        contentPanel.add(buttonPanel, c);

        setLocationRelativeTo(owner);
        pack();
    }

    private boolean okPressed() {
        String username = _usernameTextField.getText();
        String passwd1 = new String(_passwordTextField.getPassword());
        String passwd2 = new String(_secondPasswordTextField.getPassword());
        String name = _nameTextField.getText();
        String comment = _commentTextField.getText();

        if (username.isBlank()) {
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("AddUserDialog_UsernameEmpty"),
                    jmri.Application.getApplicationName(),
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!username.equals(username.trim())) {
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("AddUserDialog_SpaceNotAllowedInUsername"),
                    jmri.Application.getApplicationName(),
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }

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

        try {
            User user = _mngr.addUser(_usernameTextField.getText(), passwd1);
            user.setName(name);
            user.setComment(comment);
            _userAdded.userAdded(user);
            return true;
        } catch (PermissionManager.UserAlreadyExistsException e) {
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("AddUserDialog_UsernameExists", username.toLowerCase()),
                    jmri.Application.getApplicationName(),
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

}
