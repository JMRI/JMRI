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

/**
 * Default permission manager.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class DefaultPermissionManager implements PermissionManager {

    private static final DefaultUser USER_GUEST =
            new DefaultUser(Bundle.getMessage("PermissionManager_User_Guest").toLowerCase(),
                    null, 50, "GUEST", new Role[]{DefaultRole.ROLE_GUEST});

    private static final DefaultUser REMOTE_USER_GUEST =
            new DefaultUser(Bundle.getMessage("PermissionManager_Remote_User_Guest"),
                    null, 50, "REMOTE_GUEST", new Role[]{DefaultRole.ROLE_REMOTE_GUEST});

    private static final DefaultUser USER_ADMIN =
            new DefaultUser(Bundle.getMessage("PermissionManager_User_Admin").toLowerCase(),
                    "jmri", 100, "ADMIN", new Role[]{DefaultRole.ROLE_ADMIN, DefaultRole.ROLE_STANDARD_USER});

    private final Map<String, DefaultRole> _roles = new HashMap<>();
    private final Map<String, DefaultUser> _users = new HashMap<>();
    private final Set<PermissionOwner> _owners = new HashSet<>();
    private final Set<Permission> _permissions = new HashSet<>();
    private final Map<String, Permission> _permissionClassNames = new HashMap<>();
    private final List<LoginListener> _loginListeners = new ArrayList<>();
    private final Map<String, DefaultUser> _remoteUsers = new HashMap<>();

    private boolean _permissionsEnabled = false;
    private boolean _allowEmptyPasswords = false;
    private User _currentUser = USER_GUEST;


    DefaultPermissionManager() {
        // Do nothing. The class is initialized by the method init().
    }

    DefaultPermissionManager(DefaultPermissionManager source) {
        _permissionsEnabled = source._permissionsEnabled;
        _allowEmptyPasswords = source._allowEmptyPasswords;
        _owners.addAll(source._owners);
        _permissions.addAll(source._permissions);
        _permissionClassNames.putAll(source._permissionClassNames);
        for (var entry : source._roles.entrySet()) {
            _roles.put(entry.getKey(), new DefaultRole(entry.getValue()));
        }
        for (var entry : source._users.entrySet()) {
            _users.put(entry.getKey(), new DefaultUser(entry.getValue()));
        }
    }

    /**
     * Return a copy of this PermissionManager.
     * @return the copy
     */
    public DefaultPermissionManager getTemporaryInstance() {
        return new DefaultPermissionManager(this);
    }

    synchronized DefaultPermissionManager init() {
        _roles.put(DefaultRole.ROLE_GUEST.getName(), DefaultRole.ROLE_GUEST);
        _roles.put(DefaultRole.ROLE_REMOTE_GUEST.getName(), DefaultRole.ROLE_REMOTE_GUEST);
        _roles.put(DefaultRole.ROLE_STANDARD_USER.getName(), DefaultRole.ROLE_STANDARD_USER);
        _roles.put(DefaultRole.ROLE_ADMIN.getName(), DefaultRole.ROLE_ADMIN);

        _users.put(USER_GUEST.getUserName(), USER_GUEST);
        _users.put(REMOTE_USER_GUEST.getUserName(), REMOTE_USER_GUEST);
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

    public synchronized Collection<Role> getRoles() {
        return Collections.unmodifiableSet(new HashSet<>(_roles.values()));
    }

    public synchronized Collection<DefaultUser> getUsers() {
        return Collections.unmodifiableSet(new HashSet<>(_users.values()));
    }

    public synchronized Set<PermissionOwner> getOwners() {
        return Collections.unmodifiableSet(new HashSet<>(_owners));
    }

    public synchronized Set<Permission> getPermissions(PermissionOwner owner) {
        Set<Permission> set = new HashSet<>();
        for (Permission p : _permissions) {
            if (p.getOwner().equals(owner)) {
                set.add(p);
            }
        }
        return Collections.unmodifiableSet(set);
    }

    private DefaultRole getSystemRole(String systemName) {
        for (DefaultRole role : _roles.values()) {
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
                    DefaultRole role;
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
                        Permission permission = _permissionClassNames.get(className);
                        if (permission != null) {
                            PermissionValue value = permission.valueOf(permissionElement.getChild("Enabled").getValue());
                            role.setPermissionWithoutCheck(permission, value);
                        } else {
                            log.error("Permission class {} does not exists", className);
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
                            role = _roles.get(roleElement.getChild("Name").getValue());
                            if (role == null) {
                                log.error("UserRole {} is not found.", roleElement.getValue());
                                continue;
                            }
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
    public synchronized void storePermissionSettings() {
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
                for (java.util.Map.Entry<jmri.Permission, jmri.PermissionValue> entry : role.getPermissions().entrySet()) {
                    Permission permission = entry.getKey();
                    PermissionValue permissionValue = entry.getValue();
                    Element userPermission = new Element("Permission");
                    userPermission.addContent(new Element("Class").addContent(entry.getKey().getClass().getName()));
                    userPermission.addContent(new Element("Enabled").addContent(permission.getValue(permissionValue)));
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
    public synchronized Role addRole(String name) throws RoleAlreadyExistsException {
        if (_users.containsKey(name)) {
            throw new RoleAlreadyExistsException();
        }
        DefaultRole role = new DefaultRole(name);
        _roles.put(name, role);
        return role;
    }

    @Override
    public synchronized void removeRole(String name) throws RoleDoesNotExistException {

        if (!_roles.containsKey(name)) {
            throw new RoleDoesNotExistException();
        }
        _roles.remove(name);
    }

    @Override
    public synchronized User addUser(String username, String password)
            throws UserAlreadyExistsException {

        String u = username.toLowerCase();
        if (_users.containsKey(u)) {
            throw new UserAlreadyExistsException();
        }
        DefaultUser user = new DefaultUser(u, password);
        _users.put(u, user);
        return user;
    }

    @Override
    public synchronized void removeUser(String username)
            throws UserDoesNotExistException {

        if (!_users.containsKey(username)) {
            throw new UserDoesNotExistException();
        }
        _users.remove(username);
    }

    @Override
    public synchronized void changePassword(String newPassword, String oldPassword) {
        if (_currentUser.changePassword(newPassword,  oldPassword)) {
            storePermissionSettings();
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value="SLF4J_FORMAT_SHOULD_BE_CONST",
        justification="The text is from an exception")
    @Override
    public boolean login(String username, String password) {

        synchronized(this) {
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
            }

            _currentUser = newUser;
        }
        notifyLoginListeners(true);
        return true;
    }

    @Override
    public boolean remoteLogin(StringBuilder sessionId, Locale locale,
            String username, String password) {

        synchronized(this) {
            DefaultUser newUser = _users.get(username);
            if (newUser == null || !newUser.checkPassword(password)) {
                return false;
            }
            if (sessionId.length() == 0) {
                sessionId.append(DefaultUser.getRandomString(10));
            }
            _remoteUsers.put(sessionId.toString(), newUser);
        }
        // notifyLoginListeners(true);
        return true;
    }

    @Override
    public void logout() {
        synchronized(this) {
            _currentUser = USER_GUEST;
        }
        notifyLoginListeners(false);
    }

    @Override
    public synchronized void remoteLogout(String sessionId) {
        if (sessionId == null || sessionId.isBlank() || !_remoteUsers.containsKey(sessionId)) {
            return;
        }
        _remoteUsers.remove(sessionId);
    }

    private void notifyLoginListeners(boolean isLogin) {
        for (LoginListener listener : _loginListeners) {
            listener.loginLogout(isLogin);
        }
    }

   @Override
    public synchronized boolean isLoggedIn() {
        return _currentUser != USER_GUEST;
    }

    @Override
    public synchronized boolean isRemotelyLoggedIn(String sessionId) {
        return sessionId != null
                && !sessionId.isBlank()
                && _remoteUsers.containsKey(sessionId);
    }

    @Override
    public synchronized boolean isCurrentUser(String username) {
        return _currentUser.getUserName().equals(username);
    }

    @Override
    public synchronized boolean isCurrentUser(User user) {
        return _currentUser == user;
    }

    @Override
    public synchronized String getCurrentUserName() {
        return _currentUser != null ? _currentUser.getUserName() : null;
    }

    @Override
    public synchronized boolean isAGuestUser(String username) {
        DefaultUser user = _users.get(username);
        if (user != null) {
            String systemUsername = user.getSystemUsername();
            return USER_GUEST.getSystemUsername().equals(systemUsername)
                    || REMOTE_USER_GUEST.getSystemUsername().equals(systemUsername);
        }
        return false;
    }

    @Override
    public synchronized boolean isAGuestUser(User user) {
        if (user instanceof DefaultUser) {
            String systemUsername = ((DefaultUser)user).getSystemUsername();
            return USER_GUEST.getSystemUsername().equals(systemUsername)
                    || REMOTE_USER_GUEST.getSystemUsername().equals(systemUsername);
        }
        return false;
    }

    @Override
    public synchronized boolean isCurrentUserPermittedToChangePassword() {
        return _currentUser != null && _currentUser.isPermittedToChangePassword();
    }

    @Override
    public synchronized void addLoginListener(LoginListener listener) {
        _loginListeners.add(listener);
    }

    @Override
    public synchronized boolean isEnabled() {
        return _permissionsEnabled;
    }

    @Override
    public synchronized void setEnabled(boolean enabled) {
        if (! InstanceManager.getDefault(PermissionManager.class)
                .ensureAtLeastPermission(PermissionsSystemAdmin.PERMISSION_EDIT_PREFERENCES,
                        BooleanPermission.BooleanValue.TRUE)) {
            return;
        }
        _permissionsEnabled = enabled;
    }

    @Override
    public synchronized boolean isAllowEmptyPasswords() {
        return _allowEmptyPasswords;
    }

    @Override
    public synchronized void setAllowEmptyPasswords(boolean value) {
        if (! InstanceManager.getDefault(PermissionManager.class)
                .ensureAtLeastPermission(PermissionsSystemAdmin.PERMISSION_EDIT_PREFERENCES,
                        BooleanPermission.BooleanValue.TRUE)) {
            return;
        }
        _allowEmptyPasswords = value;
    }

    @Override
    public synchronized boolean hasAtLeastPermission(
            Permission permission, PermissionValue minValue) {
        return !_permissionsEnabled || _currentUser.hasAtLeastPermission(permission, minValue);
    }

    @Override
    public synchronized boolean hasAtLeastRemotePermission(
            String sessionId, Permission permission, PermissionValue minValue) {

        if (!_permissionsEnabled) return true;

        DefaultUser user = REMOTE_USER_GUEST;
        if (sessionId != null && !sessionId.isBlank() && _remoteUsers.containsKey(sessionId)) {
            user = _remoteUsers.get(sessionId);
        }
//        log.error("hasPermission: sessionId: {}, user: {}, permission: {}, has: {}", sessionId, user.getUserName(), permission.getName(), user.hasAtLeastPermission(permission, minValue));
        return user.hasAtLeastPermission(permission, minValue);
    }

    @Override
    public synchronized boolean ensureAtLeastPermission(
            Permission permission, PermissionValue minValue) {
        return !_permissionsEnabled || _currentUser.ensureAtLeastPermission(permission, minValue);
    }

    @Override
    public synchronized void registerOwner(PermissionOwner owner) {
        _owners.add(owner);
    }

    @Override
    public synchronized void registerPermission(Permission permission) {
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
                    ((DefaultRole)role).setPermissionWithoutCheck(
                            p, p.getDefaultPermission(role));
                }
            }
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultPermissionManager.class);
}
