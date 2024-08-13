package jmri.jmrit.permission.swing;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.*;

import jmri.*;

/**
 * Dialog to add user
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class AddUserDialog extends JDialog {

    public interface UserAdded {
        void userAdded(User user);
    }

    private final UserAdded _userAdded;
    private final JTextField _usernameTextField;
    private final JTextField _passwordTextField;
    private final JTextField _secondPasswordTextField;
    private final JTextField _nameTextField;
    private final JTextField _commentTextField;


    public AddUserDialog(Frame owner, UserAdded userAdded) {
        super(owner, Bundle.getMessage("AddUserDialog_AddUserTitle"), true);

        this._userAdded = userAdded;

        JPanel contentPanel = new JPanel();
        rootPane.getContentPane().add(contentPanel);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));

        contentPanel.add(new JLabel(Bundle.getMessage("AddUserDialog_UserName")));
        _usernameTextField = new JTextField(20);
        contentPanel.add(_usernameTextField);

        contentPanel.add(new JLabel(Bundle.getMessage("AddUserDialog_Password")));
        _passwordTextField = new JTextField(40);
        contentPanel.add(_passwordTextField);

        contentPanel.add(new JLabel(Bundle.getMessage("AddUserDialog_PasswordAgain")));
        _secondPasswordTextField = new JTextField(40);
        contentPanel.add(_secondPasswordTextField);

        contentPanel.add(new JLabel(Bundle.getMessage("AddUserDialog_Name")));
        _nameTextField = new JTextField(40);
        contentPanel.add(_nameTextField);

        contentPanel.add(new JLabel(Bundle.getMessage("AddUserDialog_Comment")));
        _commentTextField = new JTextField(40);
        contentPanel.add(_commentTextField);

        JPanel buttonPanel = new JPanel();
        JButton buttonCancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        buttonPanel.add(buttonCancel);
        buttonCancel.addActionListener((ActionEvent e) -> {
            this.dispose();
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        buttonCancel.setToolTipText("CancelLogixButtonHint");      // NOI18N

        // OK
        JButton buttonOK = new JButton(Bundle.getMessage("ButtonOK"));    // NOI18N
        buttonPanel.add(buttonOK);
        buttonOK.addActionListener((ActionEvent e) -> {
            okPressed();
            this.dispose();
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        buttonOK.setToolTipText("CancelLogixButtonHint");      // NOI18N

        contentPanel.add(buttonPanel);

        setLocationRelativeTo(owner);
        pack();
    }

    private void okPressed() {
        PermissionManager mngr = InstanceManager.getDefault(PermissionManager.class);

        try {
            User user = mngr.addUser(_usernameTextField.getText(), _passwordTextField.getText());
            _userAdded.userAdded(user);
        } catch (PermissionManager.UserAlreadyExistsException e) {
            // Do something!!!
        }

//        secondPasswordTextField
//        nameTextField
//        commentTextField
    }

}
