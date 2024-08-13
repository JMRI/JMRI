package jmri.jmrit.permission;

import java.awt.GraphicsEnvironment;
import java.io.*;
import java.util.*;

import jmri.*;
import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;
import jmri.util.ThreadingUtil;
import jmri.util.swing.JmriJOptionPane;

import org.jdom2.*;

/*
    TO DO

    * Add/remove role
    * Add/remove user
    * Admin change user password
    * Store changes in the preference panel
    * Login, logut and change password menu items
    * Move documentation to the correct folder
*/

/**
 * Default permission manager.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class DefaultPermissionManager implements PermissionManager {

    private static final DefaultUser USER_GUEST =
            new DefaultUser(Bundle.getMessage("PermissionManager_User_Guest"),
                    null, 50, "GUEST", new Role[]{DefaultRole.ROLE_GUEST});

    private static final DefaultUser USER_ADMIN =
            new DefaultUser(Bundle.getMessage("PermissionManager_User_Admin"),
                    "", 100, "ADMIN", new Role[]{DefaultRole.ROLE_ADMIN, DefaultRole.ROLE_STANDARD_USER});

    private final Map<String, Role> _roles = new HashMap<>();
    private final Map<String, DefaultUser> _users = new HashMap<>();
    private final Set<PermissionOwner> _owners = new HashSet<>();
    private final Set<Permission> _permissions = new HashSet<>();
    private final Map<String, Permission> _permissionClassNames = new HashMap<>();
    private final List<LoginListener> _loginListeners = new ArrayList<>();

    private boolean _permissionsEnabled = false;
    private boolean _allowEmptyPasswords = false;
    private User _currentUser = USER_GUEST;


    public DefaultPermissionManager init() {
        _roles.put(DefaultRole.ROLE_GUEST.getName(), DefaultRole.ROLE_GUEST);
        _roles.put(DefaultRole.ROLE_STANDARD_USER.getName(), DefaultRole.ROLE_STANDARD_USER);
        _roles.put(DefaultRole.ROLE_ADMIN.getName(), DefaultRole.ROLE_ADMIN);

        _users.put(USER_GUEST.getUserName(), USER_GUEST);
        _users.put(USER_ADMIN.getUserName(), USER_ADMIN);

        for (PermissionFactory factory : ServiceLoader.load(PermissionFactory.class)) {
            factory.register(this);
        }
        loadPermissionSettings();
        ThreadingUtil.runOnGUIEventually(() -> {
            checkThatAllRolesKnowsAllPermissions();
        });
        return this;
    }

    public Collection<Role> getRoles() {
        return _roles.values();
    }

    public Collection<DefaultUser> getUsers() {
        return _users.values();
    }

    public Set<PermissionOwner> getOwners() {
        return _owners;
    }

    public Set<Permission> getPermissions(PermissionOwner owner) {
        Set<Permission> set = new TreeSet<>((a,b) -> {return a.getName().compareTo(b.getName());});
        for (Permission p : _permissions) {
            if (p.getOwner().equals(owner)) {
                set.add(p);
            }
        }
        return set;
    }

    private Role getSystemRole(String systemName) {
        for (Role role : _roles.values()) {
            if (role.isSystemRole() && role.getSystemName().equals(systemName)) {
                return role;
            }
        }
        return null;
    }

    private DefaultUser getSystemUser(String systemUsername) {
        for (User u : _users.values()) {
            DefaultUser du = (DefaultUser)u;
            if (du.isSystemUser() && du.getSystemUsername().equals(systemUsername)) {
                return du;
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
                _allowEmptyPasswords = "yes".equals(settings.getChild("AllowEmptyPasswords").getValue());
                log.info("Permission system is enabled: {}", _permissionsEnabled ? "yes" : "no");

                List<Element> roleElementList = root.getChild("Roles").getChildren("Role");
                for (Element roleElement : roleElementList) {
                    Element systemNameElement = roleElement.getChild("SystemName");
                    Role role;
                    if (systemNameElement != null) {
                        role = getSystemRole(systemNameElement.getValue());
                        if (role == null) {
                            log.error("SystemRole {} is not found.", systemNameElement.getValue());
                            continue;
                        }
                    } else {
                        role = new DefaultRole(roleElement.getChild("Name").getValue());
                        _roles.put(role.getName(), role);
                    }

                    List<Element> permissionElementList = roleElement
                            .getChild("Permissions").getChildren("Permission");
                    for (Element permissionElement : permissionElementList) {
                        String className = permissionElement.getChild("Class").getValue();
                        boolean enabled = "yes".equals(permissionElement.getChild("Enabled").getValue());
                        Permission permission = _permissionClassNames.get(className);
                        if (permission != null) {
                            ((DefaultRole)role).setPermissionWithoutCheck(permission, enabled);
                        } else {
                            String msg = String.format("Permission class %s does not exists", className);
                            if (!GraphicsEnvironment.isHeadless()) {
                                JmriJOptionPane.showMessageDialog(null,
                                        msg,
                                        jmri.Application.getApplicationName(),
                                        JmriJOptionPane.ERROR_MESSAGE);
                            }
                            log.error(msg);
                        }
                    }
                }

                List<Element> userElementList = root.getChild("Users").getChildren("User");
                for (Element userElement : userElementList) {

                    Element systemNameElement = userElement.getChild("SystemUsername");
                    DefaultUser user;
                    if (systemNameElement != null) {
                        user = getSystemUser(systemNameElement.getValue());
                        if (user == null) {
                            log.error("SystemUser {} is not found.", systemNameElement.getValue());
                            continue;
                        }
                        Element passwordElement = userElement.getChild("Password");
                        if (passwordElement != null) {
                            user.setPasswordMD5(passwordElement.getValue());
                            user.setSeed(userElement.getChild("Seed").getValue());
                        }
                    } else {
                        user = new DefaultUser(
                                userElement.getChild("Username").getValue(),
                                userElement.getChild("Password").getValue(),
                                userElement.getChild("Seed").getValue());
                        _users.put(user.getUserName(), user);
                    }

                    user.setName(userElement.getChild("Name").getValue());
                    user.setComment(userElement.getChild("Comment").getValue());

                    Set<Role> roles = new HashSet<>();

                    List<Element> userRoleElementList = userElement.getChild("Roles").getChildren("Role");
                    for (Element roleElement : userRoleElementList) {
                        Element roleSystemNameElement = roleElement.getChild("SystemName");
                        Role role;
                        if (roleSystemNameElement != null) {
                            role = getSystemRole(roleSystemNameElement.getValue());
                            if (role == null) {
                                log.error("SystemRole {} is not found.", roleSystemNameElement.getValue());
                                continue;
                            }
                        } else {
                            role = new DefaultRole(roleElement.getChild("Name").getValue());
                        }
                        roles.add(role);
                    }
                    user.setRoles(roles);
                }

            } catch (JDOMException | IOException ex) {
                log.error("Exception during loading of permissions", ex);
            }
        } else {
            log.info("Permission file not found or empty");
        }
    }

    @Override
    public void storePermissionSettings() {
        File file = new File(FileUtil.getPreferencesPath() + ".permissions.xml");

        try {
            // Document doc = newDocument(root, dtdLocation+"layout-config-"+dtdVersion+".dtd");
            Element rootElement = new Element("Permissions");

            Element settings = new Element("Settings");
            settings.addContent(new Element("Enabled")
                    .addContent(this._permissionsEnabled ? "yes" : "no"));
            settings.addContent(new Element("AllowEmptyPasswords")
                    .addContent(this._allowEmptyPasswords ? "yes" : "no"));
            rootElement.addContent(settings);

            checkThatAllRolesKnowsAllPermissions();

            Element rolesElement = new Element("Roles");
            for (Role role : _roles.values()) {
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
                    userElement.addContent(new Element("SystemUsername").addContent(user.getSystemUsername()));
                }
                userElement.addContent(new Element("Username").addContent(user.getUserName()));

                if (user.getPassword() != null) {   // Guest user password is null
                    userElement.addContent(new Element("Password").addContent(user.getPassword()));
                    userElement.addContent(new Element("Seed").addContent(user.getSeed()));
                }

                userElement.addContent(new Element("Name").addContent(user.getName()));
                userElement.addContent(new Element("Comment").addContent(user.getComment()));

                Element userRolesElement = new Element("Roles");
                for (Role role : user.getRoles()) {
                    Element roleElement = new Element("Role");
                    if (role.isSystemRole()) {
                        roleElement.addContent(new Element("SystemName")
                                .addContent(role.getSystemName()));
                    }
                    roleElement.addContent(new Element("Name").addContent(role.getName()));
                    userRolesElement.addContent(roleElement);
                }
                userElement.addContent(userRolesElement);
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
    public Role addRole(String name) throws RoleAlreadyExistsException {
        if (_users.containsKey(name)) {
            throw new RoleAlreadyExistsException();
        }
        Role role = new DefaultRole(name);
        _roles.put(name, role);
        return role;
    }

    @Override
    public void removeRole(String name) throws RoleDoesNotExistException {

        if (!_roles.containsKey(name)) {
            throw new RoleDoesNotExistException();
        }
        _roles.remove(name);
    }

    @Override
    public User addUser(String username, String password)
            throws UserAlreadyExistsException {

        if (_users.containsKey(username)) {
            throw new UserAlreadyExistsException();
        }
        DefaultUser user = new DefaultUser(username,password);
        _users.put(username, user);
        return user;
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
        if (_currentUser.changePassword(newPassword,  oldPassword)) {
            storePermissionSettings();
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value="SLF4J_FORMAT_SHOULD_BE_CONST",
        justification="The text is from an exception")
    @Override
    public boolean login(String username, String password) {
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
            return false;
        } else {
            _currentUser = newUser;
            notifyLoginListeners(true);
            return true;
        }
    }

    @Override
    public void logout() {
        _currentUser = USER_GUEST;
        notifyLoginListeners(false);
    }

    private void notifyLoginListeners(boolean isLogin) {
        for (LoginListener listener : _loginListeners) {
            listener.loginLogout(isLogin);
        }
    }

   @Override
    public boolean isLoggedIn() {
        return _currentUser != USER_GUEST;
    }

    @Override
    public boolean isCurrentUser(String username) {
        return _currentUser.getUserName().equals(username);
    }

    @Override
    public boolean isCurrentUser(User user) {
        return _currentUser == user;
    }

    @Override
    public String getCurrentUserName() {
        return _currentUser.getUserName();
    }

    @Override
    public void addLoginListener(LoginListener listener) {
        _loginListeners.add(listener);
    }

    @Override
    public boolean isEnabled() {
        return _permissionsEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (! InstanceManager.getDefault(PermissionManager.class)
                .checkPermission(PermissionsSystemAdmin.PERMISSION_EDIT_PREFERENCES)) {
            return;
        }
        _permissionsEnabled = enabled;
    }

    @Override
    public boolean isAllowEmptyPasswords() {
        return _allowEmptyPasswords;
    }

    @Override
    public void setAllowEmptyPasswords(boolean value) {
        if (! InstanceManager.getDefault(PermissionManager.class)
                .checkPermission(PermissionsSystemAdmin.PERMISSION_EDIT_PREFERENCES)) {
            return;
        }
        _allowEmptyPasswords = value;
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
        if (!_owners.contains(permission.getOwner())) {
            throw new RuntimeException(String.format(
                    "Permission class %s has an owner that's not known: %s",
                    permission.getClass().getName(), permission.getOwner()));
        }
        _permissions.add(permission);
        _permissionClassNames.put(permission.getClass().getName(), permission);
    }

    private void checkThatAllRolesKnowsAllPermissions() {
        for (Role role : _roles.values()) {
            for (Permission p : _permissions) {
                if (!role.getPermissions().containsKey(p)) {
                    role.setPermission(p, p.getDefaultPermission(role));
                }
            }
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultPermissionManager.class);
}
