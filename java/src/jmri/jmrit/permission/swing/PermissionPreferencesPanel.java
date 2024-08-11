package jmri.jmrit.permission.swing;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import jmri.*;
import jmri.jmrit.permission.DefaultPermissionManager;
import jmri.jmrit.permission.swing.Bundle;
import jmri.swing.PreferencesPanel;

import org.openide.util.lookup.ServiceProvider;

/**
 * Preferences panel for Permission manager.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
@ServiceProvider(service = PreferencesPanel.class)
public class PermissionPreferencesPanel extends JPanel implements PreferencesPanel {

    private final DefaultPermissionManager permissionManager;

    private boolean dirty = false;

    public PermissionPreferencesPanel() {
        PermissionManager mngr = InstanceManager.getDefault(PermissionManager.class);
        if (!(mngr instanceof DefaultPermissionManager)) {
            throw new RuntimeException("PermissionManager is not of type DefaultPermissionManager");
        }
        permissionManager = (DefaultPermissionManager)mngr;
        initGUI();
    }

    private void initGUI() {
        JPanel rolesPanel = new JPanel();
        rolesPanel.setLayout(new BoxLayout(rolesPanel, BoxLayout.PAGE_AXIS));

        JTabbedPane rolesTabbedPane = new JTabbedPane();

        for (Role role : permissionManager.getRoles()) {
            JPanel rolePanel = new JPanel();
            rolePanel.setLayout(new BoxLayout(rolePanel, BoxLayout.PAGE_AXIS));

            rolesTabbedPane.addTab(role.getName(), new JScrollPane(rolePanel));

            for (PermissionOwner owner : permissionManager.getOwners()) {
                JPanel ownerPanel = new JPanel();
                ownerPanel.setLayout(new BoxLayout(ownerPanel, BoxLayout.PAGE_AXIS));
                TitledBorder title = BorderFactory.createTitledBorder(owner.getName());
                ownerPanel.setBorder(title);

                for (Permission permission : permissionManager.getPermissions(owner)) {
                    JCheckBox checkBox = new JCheckBox(permission.getName());
                    ownerPanel.add(checkBox);
                }
                rolePanel.add(ownerPanel);
            }

            JButton removeRoleButton = new JButton(Bundle.getMessage("PermissionPreferencesPanel_RemoveRole"));
            rolePanel.add(removeRoleButton);

//            rolesPanel.add(rolePanel);
        }

        rolesPanel.add(rolesTabbedPane);

        JButton addRoleButton = new JButton(Bundle.getMessage("PermissionPreferencesPanel_AddRole"));
        rolesPanel.add(addRoleButton);


        JPanel usersPanel = new JPanel();
        usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.PAGE_AXIS));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(Bundle.getMessage("PermissionPreferencesPanel_Roles"),
                new JScrollPane(rolesPanel));
        tabbedPane.addTab(Bundle.getMessage("PermissionPreferencesPanel_Users"),
                new JScrollPane(usersPanel));
        add(tabbedPane);
    }

    @Override
    public String getPreferencesItem() {
        return "PREFERENCES"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("MenuPermission"); // NOI18N
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return getPreferencesItemText();
    }

    @Override
    public String getLabelKey() {
        return null;
    }

    @Override
    public JComponent getPreferencesComponent() {
        return this;
    }

    @Override
    public boolean isPersistant() {
        return false;
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        permissionManager.storePermissionSettings();
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public boolean isRestartRequired() {
        return false;
    }

    @Override
    public boolean isPreferencesValid() {
        return true;
    }

//    @Override
//    public int getSortOrder() {
//        return PreferencesPanel.super.getSortOrder();
//    }

}
