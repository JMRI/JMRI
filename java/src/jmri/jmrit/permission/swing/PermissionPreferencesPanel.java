package jmri.jmrit.permission.swing;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import jmri.*;
import jmri.jmrit.permission.DefaultPermissionManager;
import jmri.swing.PreferencesPanel;
import jmri.util.swing.JmriJOptionPane;

import org.openide.util.lookup.ServiceProvider;

/**
 * Preferences panel for Permission manager.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
@ServiceProvider(service = PreferencesPanel.class)
public class PermissionPreferencesPanel extends JPanel implements PreferencesPanel {

    private final DefaultPermissionManager permissionManager;
    private final Map<User, UserFields> _userFieldsMap = new HashMap<>();
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

        List<Role> roleList = new ArrayList<>(permissionManager.getRoles());
        roleList.sort((a,b) -> {
            if (a.getPriority() != b.getPriority()) {
                return Integer.compare(b.getPriority(), a.getPriority());
            }
            return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
        });

        JPanel outerPanel = new JPanel();

        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.PAGE_AXIS));

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.PAGE_AXIS));
        settingsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.black, 1), new EmptyBorder(4,4,4,4)));

        JCheckBox enablePermissionManagerCheckBox = new JCheckBox(Bundle.getMessage(
                "PermissionPreferencesPanel_EnablePermissionManager"));
        enablePermissionManagerCheckBox.setSelected(permissionManager.isEnabled());
        enablePermissionManagerCheckBox.addActionListener((evt) -> {
            permissionManager.setEnabled(enablePermissionManagerCheckBox.isSelected());
            dirty = true;
        });
        settingsPanel.add(enablePermissionManagerCheckBox);

        JCheckBox allowEmptyPasswordsCheckBox = new JCheckBox(Bundle.getMessage(
                "PermissionPreferencesPanel_AllowEmptyPasswords"));
        allowEmptyPasswordsCheckBox.setSelected(permissionManager.isAllowEmptyPasswords());
        allowEmptyPasswordsCheckBox.addActionListener((evt) -> {
            permissionManager.setAllowEmptyPasswords(allowEmptyPasswordsCheckBox.isSelected());
            dirty = true;
        });
        settingsPanel.add(allowEmptyPasswordsCheckBox);

        outerPanel.add(settingsPanel);

        outerPanel.add(Box.createVerticalStrut(10));

        JPanel rolesPanel = new JPanel();
        rolesPanel.setLayout(new BoxLayout(rolesPanel, BoxLayout.PAGE_AXIS));

        JTabbedPane rolesTabbedPane = new JTabbedPane();

        for (Role role : roleList) {
            rolesTabbedPane.addTab(role.getName(), new JScrollPane(getRolePanel(role)));
        }

        rolesPanel.add(rolesTabbedPane);

        JButton addRoleButton = new JButton(Bundle.getMessage("PermissionPreferencesPanel_AddRole"));
        addRoleButton.addActionListener((evt) -> { createNewRole(rolesTabbedPane); });
        rolesPanel.add(addRoleButton);


        List<User> userList = new ArrayList<>(permissionManager.getUsers());
        userList.sort((a,b) -> {
            if (a.getPriority() != b.getPriority()) {
                return Integer.compare(b.getPriority(), a.getPriority());
            }
            return a.getUserName().toLowerCase().compareTo(b.getUserName().toLowerCase());
        });

        JPanel usersPanel = new JPanel();
        usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.PAGE_AXIS));

        JTabbedPane usersTabbedPane = new JTabbedPane();

        for (User user : userList) {
            usersTabbedPane.addTab(user.getUserName(), new JScrollPane(getUserPanel(user, roleList)));
        }

        usersPanel.add(usersTabbedPane);

        JButton addUserButton = new JButton(Bundle.getMessage("PermissionPreferencesPanel_AddUser"));
        addUserButton.addActionListener((evt) -> {
            new AddUserDialog(getFrame(), (user) -> {
                // Find the index of the new user
                List<User> newUserList = new ArrayList<>(permissionManager.getUsers());
                newUserList.sort((a,b) -> {
                    if (a.getPriority() != b.getPriority()) {
                        return Integer.compare(b.getPriority(), a.getPriority());
                    }
                    return a.getUserName().toLowerCase().compareTo(b.getUserName().toLowerCase());
                });
                usersTabbedPane.insertTab(user.getUserName(), null,
                        new JScrollPane(getUserPanel(user, roleList)),
                        null, newUserList.indexOf(user));
                getFrame().pack();
            }).setVisible(true);
        });
        usersPanel.add(addUserButton);


        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(Bundle.getMessage("PermissionPreferencesPanel_Roles"),
                new JScrollPane(rolesPanel));
        tabbedPane.addTab(Bundle.getMessage("PermissionPreferencesPanel_Users"),
                new JScrollPane(usersPanel));

        JPanel outerTabbedPanel = new JPanel();
        outerTabbedPanel.add(tabbedPane);
        outerPanel.add(outerTabbedPanel);
        add(outerPanel);
    }

    private Frame getFrame() {
        Container c = this;
        while (c != null && !(c instanceof Frame)) {
            c = c.getParent();
        }
        // c is either a Frame or null
        return (Frame)c;
    }

    private void createNewRole(JTabbedPane rolesTabbedPane) {
        String roleName = JOptionPane.showInputDialog(getFrame(),
                Bundle.getMessage("PermissionPreferencesPanel_EnterRoleName"));

        if (roleName == null) {
            return;     // User selected "Cancel"
        }

        if (roleName.isBlank()) {
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("PermissionPreferencesPanel_NameEmpty"),
                    jmri.Application.getApplicationName(),
                    JmriJOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!roleName.equals(roleName.trim())) {
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("PermissionPreferencesPanel_SpaceNotAllowedInRoleName"),
                    jmri.Application.getApplicationName(),
                    JmriJOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Role role = permissionManager.addRole(roleName);

            // Find the index of the new role
            List<Role> newRoleList = new ArrayList<>(permissionManager.getRoles());
            newRoleList.sort((a,b) -> {
                if (a.getPriority() != b.getPriority()) {
                    return Integer.compare(b.getPriority(), a.getPriority());
                }
                return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
            });

            rolesTabbedPane.insertTab(role.getName(), null,
                    new JScrollPane(getRolePanel(role)),
                    null, newRoleList.indexOf(role));
            getFrame().pack();


        } catch (PermissionManager.RoleAlreadyExistsException e) {
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("PermissionPreferencesPanel_RoleNameExists"),
                    jmri.Application.getApplicationName(),
                    JmriJOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel getRolePanel(Role role) {
        JPanel rolePanel = new JPanel();
        rolePanel.setLayout(new BoxLayout(rolePanel, BoxLayout.PAGE_AXIS));

        JLabel roleLabel = new JLabel("<html><font size=\"+1\"><b>"+role.getName()+"</b></font></html>");
        roleLabel.setBorder(new EmptyBorder(4,4,0,4));
        rolePanel.add(roleLabel);

        for (PermissionOwner owner : permissionManager.getOwners()) {
            JPanel ownerPanel = new JPanel();
            ownerPanel.setLayout(new BoxLayout(ownerPanel, BoxLayout.PAGE_AXIS));

            JLabel ownerLabel = new JLabel("<html><font size=\"0.5\"><b>"+owner.getName()+"</b></font></html>");
            ownerLabel.setBorder(new EmptyBorder(15,4,4,4));
            rolePanel.add(ownerLabel);

            for (Permission permission : permissionManager.getPermissions(owner)) {
                JCheckBox checkBox = new JCheckBox(permission.getName());
                checkBox.setSelected(role.hasPermission(permission));
                checkBox.addActionListener((evt) -> {
                    dirty = true;
                    role.setPermission(permission, checkBox.isSelected());
                });
                ownerPanel.add(checkBox);
            }
            rolePanel.add(ownerPanel);
        }

        rolePanel.add(Box.createVerticalStrut(10));
        JButton removeRoleButton = new JButton(Bundle.getMessage("PermissionPreferencesPanel_RemoveRole"));
        if (role.isSystemRole()) {
            removeRoleButton.setEnabled(false);
        }
        rolePanel.add(removeRoleButton);

        return rolePanel;
    }

    private JPanel getUserPanel(User user, List<Role> roleList) {
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.PAGE_AXIS));

        UserFields userFields = new UserFields();
        _userFieldsMap.put(user, userFields);

        JLabel usernameLabel = new JLabel("<html><font size=\"+1\"><b>"+user.getUserName()+"</b></font></html>");
        usernameLabel.setBorder(new EmptyBorder(4,4,4,4));
        userPanel.add(usernameLabel);
        userPanel.add(new JLabel(Bundle.getMessage("PermissionPreferencesPanel_Name")));
        userFields._nameTextField = new JTextField(20);
        userFields._nameTextField.setText(user.getName());
        userPanel.add(userFields._nameTextField);
        userPanel.add(new JLabel(Bundle.getMessage("PermissionPreferencesPanel_Comment")));
        userFields._commentTextField = new JTextField(40);
        userFields._commentTextField.setText(user.getComment());
        userPanel.add(userFields._commentTextField);

        userPanel.add(Box.createVerticalStrut(10));

        userPanel.add(new JLabel(Bundle.getMessage("PermissionPreferencesPanel_Roles")));
        userPanel.add(Box.createVerticalStrut(5));

        int lastPriority = 0;
        for (Role role : roleList) {
            if (role.getPriority() == 0 && lastPriority != 0) {
                userPanel.add(Box.createVerticalStrut(10));
            }
            JCheckBox checkBox = new JCheckBox(role.getName());
            checkBox.setSelected(user.getRoles().contains(role));
            checkBox.addActionListener((evt) -> {
                if (checkBox.isSelected()) {
                    user.addRole(role);
                } else {
                    user.removeRole(role);
                }
                dirty = true;
            });
            userPanel.add(checkBox);
            lastPriority = role.getPriority();
        }

        userPanel.add(Box.createVerticalStrut(10));

        JButton changePasswordButton = new JButton(Bundle.getMessage("PermissionPreferencesPanel_ChangePassword"));
        changePasswordButton.setEnabled(!permissionManager.isGuestUser(user));
        changePasswordButton.addActionListener((evt) -> {
            new ChangeUserPasswordDialog(getFrame(), user).setVisible(true);
        });
        userPanel.add(changePasswordButton);

        JButton removeUserButton = new JButton(Bundle.getMessage("PermissionPreferencesPanel_RemoveUser"));
        if (user.isSystemUser()) {
            removeUserButton.setEnabled(false);
        }
        userPanel.add(removeUserButton);

        return userPanel;
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
        for (var entry : _userFieldsMap.entrySet()) {
            entry.getKey().setName(entry.getValue()._nameTextField.getText());
            entry.getKey().setComment(entry.getValue()._commentTextField.getText());
        }
        permissionManager.storePermissionSettings();
        dirty = false;
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

    @Override
    public BooleanSupplier getIsEnabled() {
        return () -> {
            return InstanceManager.getDefault(PermissionManager.class)
                    .checkPermission(PermissionsSystemAdmin.PERMISSION_EDIT_PERMISSIONS);
        };
    }


    private static class UserFields {
        JTextField _nameTextField;
        JTextField _commentTextField;
    }

}
