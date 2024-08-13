package jmri.jmrit.permission.swing;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.InstanceManager;
import jmri.PermissionManager;
import jmri.util.swing.JmriJOptionPane;

/**
 * Let a user logout from the permission manager.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class LogoutAction extends AbstractAction {

    public LogoutAction() {
        super(Bundle.getMessage("LogoutAction_Title"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        InstanceManager.getDefault(PermissionManager.class).logout();

        JmriJOptionPane.showMessageDialog(null,
                Bundle.getMessage("LogoutAction_UserLoggedOut"),
                jmri.Application.getApplicationName(),
                JmriJOptionPane.INFORMATION_MESSAGE);
    }

}
