package jmri.jmrit.display.swing;

import javax.swing.*;

import jmri.*;
import jmri.jmrit.display.EditorPermissions.EditorPermissionEnum;
import jmri.swing.PermissionSwing;

/**
 * Swing configurations for PermissionsSystemAdmin permissions.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
public class EditorPermissionsSwing {

    public static class EditorPermissionSwing implements PermissionSwing {

        @Override
        public JLabel getLabel(Permission permission) throws IllegalArgumentException {
            return new JLabel(permission.getName());
        }

        @Override
        public JComponent getComponent(Role role, Permission permission, Runnable onChange)
                throws IllegalArgumentException {
            JComboBox<EditorPermissionEnum> comboBox = new JComboBox<>(EditorPermissionEnum.values());
            PermissionValue value = role.getPermissionValue(permission);
            if (!(value instanceof EditorPermissionEnum)) {
                throw new IllegalArgumentException("Permission value is not a EditorPermissionEnum: " + value.getClass().getName());
            }
            comboBox.setSelectedItem(value);
            comboBox.addActionListener((evt) -> {
                role.setPermission(permission, comboBox.getItemAt(comboBox.getSelectedIndex()));
                onChange.run();
            });
            return comboBox;
        }

    }

}
