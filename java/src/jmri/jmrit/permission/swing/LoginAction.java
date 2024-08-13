package jmri.jmrit.permission.swing;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Let a user login to the permission manager.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class LoginAction extends AbstractAction {

    public LoginAction() {
        super(Bundle.getMessage("LoginAction_Title"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
//        new LoginDialog((Frame) e.getSource()).setVisible(true);
        new LoginDialog(null).setVisible(true);
    }

}
