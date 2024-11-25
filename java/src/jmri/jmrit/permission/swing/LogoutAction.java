package jmri.jmrit.permission.swing;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import jmri.InstanceManager;
import jmri.PermissionManager;
import jmri.util.swing.*;

/**
 * Let a user logout from the permission manager.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class LogoutAction extends JmriAbstractAction {

    public LogoutAction(String s, WindowInterface wi) {
        super(s, wi);
        checkPermission();
    }

    public LogoutAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
        checkPermission();
    }

    public LogoutAction() {
        super(Bundle.getMessage("LogoutAction_Title"));
        checkPermission();
    }

    private void checkPermission() {
        var permissionManager = InstanceManager.getDefault(PermissionManager.class);
        if (permissionManager.isEnabled()) {
            setEnabled(permissionManager.isLoggedIn());
            permissionManager.addLoginListener((isLogin) -> {
                setEnabled(isLogin);
            });
        } else {
            setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        InstanceManager.getDefault(PermissionManager.class).logout();

        JmriJOptionPane.showMessageDialog(null,
                Bundle.getMessage("LogoutAction_UserLoggedOut"),
                jmri.Application.getApplicationName(),
                JmriJOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

}
