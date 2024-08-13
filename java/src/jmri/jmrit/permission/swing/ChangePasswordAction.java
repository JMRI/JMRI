package jmri.jmrit.permission.swing;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


/**
 * Let a user login to the permission manager.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class ChangePasswordAction extends AbstractAction {

    public ChangePasswordAction() {
        super(Bundle.getMessage("ChangePasswordAction_Title"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new ChangePasswordDialog().setVisible(true);
    }

}
