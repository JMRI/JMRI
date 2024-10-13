package jmri.swing;

import javax.swing.*;

import jmri.*;

/**
 * The parent interface for configuring permissions with Swing.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
public interface PermissionSwing {

    /**
     * Get a label for the permission component.
     *
     * @param  permission the permission to configure with this component
     * @return a component that configures the permission or null if no label
     * @throws IllegalArgumentException if this class does not support the class
     *                                  with the name given in parameter 'className'
     */
    public default JLabel getLabel(Permission permission)
            throws IllegalArgumentException {
        return null;
    }

    /**
     * Get a component that configures this permission.
     * This method initializes the panel with an empty configuration.
     *
     * @param  role        the role
     * @param  permission  the permission to configure with this component
     * @param  onChange     executes on change, used mainly to set dirty flag
     * @return a component that configures the permission
     * @throws IllegalArgumentException if this class does not support the class
     *                                  with the name given in parameter 'className'
     */
    public JComponent getComponent(Role role, Permission permission, Runnable onChange)
            throws IllegalArgumentException;


    /**
     * The default swing implementation for BooleanPermission.
     * The class is abstract since it should never be instantiated directly.
     */
    public static abstract class BooleanPermissionSwing implements PermissionSwing {

        @Override
        public JComponent getComponent(Role role, Permission permission, Runnable onChange) throws IllegalArgumentException {
            JCheckBox checkBox = new JCheckBox(permission.getName());
            PermissionValue value = role.getPermissionValue(permission);
            if (!(value instanceof BooleanPermission.BooleanValue)) {
                throw new IllegalArgumentException("Permission value is not a BooleanValue: " + value.getClass().getName());
            }
            checkBox.setSelected(((BooleanPermission.BooleanValue)value).get());
            checkBox.addActionListener((evt) -> {
                role.setPermission(permission, BooleanPermission.BooleanValue.get(checkBox.isSelected()));
                onChange.run();
            });
            return checkBox;
        }

    }

}
