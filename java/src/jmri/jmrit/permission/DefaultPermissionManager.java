package jmri.jmrit.permission;

import java.awt.GraphicsEnvironment;
import java.io.*;
import java.util.*;

import jmri.*;
import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;
import jmri.util.swing.JmriJOptionPane;

import org.jdom2.*;

/*
    TO DO

    * Load/Store settings in JMRI system preference folder.
    * Enable/disable permission manager
    * Add/remove user.
    * Let user login, logout, change password.
    * Edit user preferences. Checkbox for each preference.
*/

/**
 * Default permission manager.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class DefaultPermissionManager implements PermissionManager {

    private static final DefaultRole ROLE_GUEST =
            new DefaultRole(Bundle.getMessage("PermissionManager_Role_Guest"),true,"GUEST");

    public static final DefaultRole ROLE_STANDARD_USER =
            new DefaultRole(Bundle.getMessage("PermissionManager_Role_StandardUser"),true,"STANDARD_USER");

    private static final DefaultRole ROLE_ADMIN =
            new DefaultRole(Bundle.getMessage("PermissionManager_Role_Admin"),true,"ADMIN");

    private static final DefaultUser USER_GUEST =
            new DefaultUser(Bundle.getMessage("PermissionManager_User_Guest"), null, true, "GUEST");

    private static final DefaultUser USER_ADMIN =
            new DefaultUser(Bundle.getMessage("PermissionManager_User_Admin"), "", true, "ADMIN");

    private final Map<String, DefaultRole> _roles = new HashMap<>();
    private final Map<String, DefaultUser> _users = new HashMap<>();
    private final Set<PermissionOwner> _owners = new HashSet<>();
    private final Set<Permission> _permissions = new HashSet<>();
    private final Map<String, Permission> _permissionClassNames = new HashMap<>();

    private boolean _permissionsEnabled = false;
    private User _currentUser = USER_GUEST;


    public DefaultPermissionManager init() {
        _roles.put(ROLE_GUEST.getName(), ROLE_GUEST);
        _roles.put(ROLE_STANDARD_USER.getName(), ROLE_STANDARD_USER);
        _roles.put(ROLE_ADMIN.getName(), ROLE_ADMIN);

        USER_GUEST.addRole(ROLE_GUEST);
        _users.put(USER_GUEST.getName(), USER_GUEST);

        USER_ADMIN.addRole(ROLE_ADMIN);
        _users.put(USER_ADMIN.getName(), USER_ADMIN);

        _roles.put("Test role", new DefaultRole("Test role"));

        _users.put("daniel", new DefaultUser("daniel", "12345678"));
        _users.put("kalle", new DefaultUser("kalle", "testtest"));
        _users.put("sven", new DefaultUser("sven", "testtest"));

        DefaultPermissionManager.this.registerOwner(StandardPermissions.PERMISSION_OWNER_ADMIN);
        DefaultPermissionManager.this.registerPermission(StandardPermissions.PERMISSION_ADMIN);
        for (PermissionFactory factory : ServiceLoader.load(PermissionFactory.class)) {
            factory.register(this);
        }
        loadPermissionSettings();
        return this;
    }

    private DefaultRole getSystemRole(String systemName) {
        for (Role r : _roles.values()) {
            DefaultRole df = (DefaultRole)r;
            if (df.isSystemRole() && df.getSystemName().equals(systemName)) {
                return df;
            }
        }
        return null;
    }

    private void loadPermissionSettings() {
        File file = new File(FileUtil.getPreferencesPath() + ".permissions.xml");

        log.info("Permission file: {}", file.getAbsolutePath());

        if (file.exists() && file.length() != 0) {
            try {
                Element root = new XmlFile().rootFromFile(file);

                Element settings = root.getChild("Settings");
                _permissionsEnabled = "yes".equals(settings.getChild("Enabled").getValue());
                log.info("Permission system is enabled: {}", _permissionsEnabled ? "yes" : "no");

                List<Element> roleElementList = root.getChild("Roles").getChildren("Role");
                for (Element roleElement : roleElementList) {
                    Element systemNameElement = roleElement.getChild("SystemName");
                    DefaultRole role;
                    if (systemNameElement != null) {
                        role = getSystemRole(systemNameElement.getValue());
                        if (role == null) {
                            log.error("SystemRole {} is not found.", systemNameElement.getValue());
                            continue;
                        }
                    } else {
                        role = new DefaultRole(roleElement.getChild("Name").getValue());
                    }

                    var permissions = role.getPermissions();

                    List<Element> permissionElementList = root.getChildren("Permissions");
                    for (Element permissionElement : permissionElementList) {
                        String className = permissionElement.getChild("Name").getValue();
                        boolean enabled = "yes".equals(permissionElement.getChild("Enabled").getValue());
                        Permission permission = _permissionClassNames.get(className);
                        permissions.put(permission, enabled);
                    }
                }

            } catch (JDOMException | IOException ex) {
                log.error("Exception during loading of permissions", ex);
            }
        } else {
            log.info("Permission file not found or empty");
        }

        storePermissionSettings();
    }

    private void storePermissionSettings() {
        File file = new File(FileUtil.getPreferencesPath() + ".permissions.xml");

        try {
            // Document doc = newDocument(root, dtdLocation+"layout-config-"+dtdVersion+".dtd");
            Element rootElement = new Element("Permissions");

            Element settings = new Element("Settings");
            settings.addContent(new Element("Enabled")
                    .addContent(this._permissionsEnabled ? "yes" : "no"));
            rootElement.addContent(settings);

            checkThatAllRolesKnowsAllPermissions();

            Element rolesElement = new Element("Roles");
            for (DefaultRole role : _roles.values()) {
                Element roleElement = new Element("Role");
                if (role.isSystemRole()) {
                    roleElement.addContent(new Element("SystemName").addContent(role.getSystemName()));
                }
                roleElement.addContent(new Element("Name").addContent(role.getName()));

                Element rolePermissions = new Element("Permissions");
                for (var entry : role.getPermissions().entrySet()) {
                    Element userPermission = new Element("Permission");
                    userPermission.addContent(new Element("Class").addContent(entry.getKey().getClass().getName()));
                    userPermission.addContent(new Element("Enabled").addContent(entry.getValue() ? "yes" : "no"));
                    rolePermissions.addContent(userPermission);
                }
                roleElement.addContent(rolePermissions);
                rolesElement.addContent(roleElement);
            }
            rootElement.addContent(rolesElement);


            Element usersElement = new Element("Users");
            for (DefaultUser user : _users.values()) {
                Element userElement = new Element("User");
                if (user.isSystemUser()) {
                    userElement.addContent(new Element("SystemUsername").addContent(user.getSystemUserName()));
                }
                userElement.addContent(new Element("Username").addContent(user.getName()));
                userElement.addContent(new Element("Password").addContent(user.getPassword()));
                userElement.addContent(new Element("Seed").addContent(user.getSeed()));

                Element rolePermissionsElement = new Element("Roles");
                for (Role role : user.getRoles()) {
                    Element userPermission = new Element("Role");
                    userPermission.addContent(new Element("Name").addContent(role.getName()));
                    rolePermissionsElement.addContent(userPermission);
                }
                userElement.addContent(rolePermissionsElement);
                usersElement.addContent(userElement);
            }
            rootElement.addContent(usersElement);

            Document doc = XmlFile.newDocument(rootElement);
            new XmlFile().writeXML(file, doc);

        } catch (java.io.FileNotFoundException ex3) {
            log.error("FileNotFound error writing file: {}", file);
        } catch (java.io.IOException ex2) {
            log.error("IO error writing file: {}", file);
        }
    }

    @Override
    public void addUser(String username, String password)
            throws UserAlreadyExistsException {

        if (_users.containsKey(username)) {
            throw new UserAlreadyExistsException();
        }
        _users.put(username, new DefaultUser(username,password));
    }

    @Override
    public void removeUser(String username)
            throws UserDoesNotExistException {

        if (!_users.containsKey(username)) {
            throw new UserDoesNotExistException();
        }
        _users.remove(username);
    }

    @Override
    public void changePassword(String newPassword, String oldPassword) {
        _currentUser.changePassword(newPassword,  oldPassword);
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value="SLF4J_FORMAT_SHOULD_BE_CONST",
        justification="The text is from an exception")
    @Override
    public void login(String username, String password) {
        DefaultUser newUser = _users.get(username);
        if (newUser == null || !newUser.checkPassword(password)) {
            String msg = new BadUserOrPasswordException().getMessage();

            if (!GraphicsEnvironment.isHeadless()) {
                JmriJOptionPane.showMessageDialog(null,
                        msg,
                        jmri.Application.getApplicationName(),
                        JmriJOptionPane.ERROR_MESSAGE);
            } else {
                log.error(msg);
            }
        }
        _currentUser = newUser;
    }

    @Override
    public void logout() {
        _currentUser = USER_GUEST;
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return !_permissionsEnabled || _currentUser.hasPermission(permission);
    }

    @Override
    public boolean checkPermission(Permission permission) {
        return !_permissionsEnabled || _currentUser.checkPermission(permission);
    }

    @Override
    public void registerOwner(PermissionOwner owner) {
        _owners.add(owner);
    }

    @Override
    public void registerPermission(Permission permission) {
        _permissions.add(permission);
        _permissionClassNames.put(permission.getClass().getName(), permission);
    }

    public void checkThatAllRolesKnowsAllPermissions() {
        for (DefaultRole role : _roles.values()) {
            role.checkThatRoleKnowsAllPermissions(_permissions);
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultPermissionManager.class);
}
