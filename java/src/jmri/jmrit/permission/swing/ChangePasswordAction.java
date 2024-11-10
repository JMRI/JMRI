package jmri.jmrit.permission.swing;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import jmri.*;
import jmri.util.swing.*;


/**
 * Let a user change his own password.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class ChangePasswordAction extends JmriAbstractAction {

    public ChangePasswordAction(String s, WindowInterface wi) {
        super(s, wi);
        checkPermission();
    }

    public ChangePasswordAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
        checkPermission();
    }

    public ChangePasswordAction() {
        super(Bundle.getMessage("ChangePasswordAction_Title"));
        checkPermission();
    }

    private void checkPermission() {
        var permissionManager = InstanceManager.getDefault(PermissionManager.class);

        if (permissionManager.isEnabled()) {
            setEnabled(permissionManager.isLoggedIn());
            permissionManager.addLoginListener((isLogin) -> {
                setEnabled(isLogin && permissionManager.isCurrentUserPermittedToChangePassword());
            });
        } else {
            setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        var permissionManager = InstanceManager.getDefault(PermissionManager.class);
        if (!permissionManager.hasAtLeastPermission(PermissionsSystemAdmin.PERMISSION_EDIT_PERMISSIONS,
                BooleanPermission.BooleanValue.TRUE)) {
            // Note that the line above _asks_ about the permission and the line below
            // _checks_ for the permission. If the line below doesn't have the permission,
            // a JOptionPane will show an error message.
            if (!permissionManager.ensureAtLeastPermission(PermissionsSystemAdmin.PERMISSION_EDIT_OWN_PASSWORD,
                    BooleanPermission.BooleanValue.TRUE)) {
                return;
            }
        }
        new ChangePasswordDialog().setVisible(true);
    }

    @Override
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

}
