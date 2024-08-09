package jmri.managers;

import java.awt.GraphicsEnvironment;
import java.util.*;

import jmri.*;
import jmri.util.swing.JmriJOptionPane;

/**
 * Default permission manager.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class DefaultPermissionManager
        implements PermissionManager, PermissionOwner {

    private static final User GUEST_USER =
            new User(PermissionManager.GUEST_USERNAME, "");

    private final Map<String, User> _users = new HashMap<>();
    private final Set<PermissionOwner> _owners = new HashSet<>();
    private final Set<Permission> _permissions = new HashSet<>();

    private boolean permissionsEnabled = false;
    private User _currentUser = GUEST_USER;


    public DefaultPermissionManager init() {
        DefaultPermissionManager.this.registerOwner(this);
        DefaultPermissionManager.this.registerPermission(new PermissionAdmin());
        for (PermissionFactory factory : ServiceLoader.load(PermissionFactory.class)) {
            factory.register(this);
        }
        return this;
    }

    @Override
    public String getName() {
        return Bundle.getMessage("DefaultPermissionManager_Owner");
    }

    @Override
    public void addUser(String username, String password)
            throws UserAlreadyExistsException {

        if (_users.containsKey(username)) {
            throw new UserAlreadyExistsException();
        }
        _users.put(username, new User(username,password));
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
        User newUser = _users.get(username);
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
        _currentUser = GUEST_USER;
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return !permissionsEnabled || _currentUser.hasPermission(permission);
    }

    @Override
    public boolean checkPermission(Permission permission) {
        return !permissionsEnabled || _currentUser.checkPermission(permission);
    }

    @Override
    public void registerOwner(PermissionOwner owner) {
        _owners.add(owner);
    }

    @Override
    public void registerPermission(Permission permission) {
        _permissions.add(permission);
    }


    public static class User {

        public final String _username;
        public String _password;

        private final Set<Permission> _permissions = new HashSet<>();

        public User(String username, String password) {
            this._username = username;
            this._password = password;
        }

        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value="SLF4J_FORMAT_SHOULD_BE_CONST",
            justification="The text is from an exception")
        public void changePassword(String newPassword, String oldPassword) {
            if (!checkPassword(oldPassword)) {
                String msg = new BadPasswordException().getMessage();

                if (!GraphicsEnvironment.isHeadless()) {
                    JmriJOptionPane.showMessageDialog(null,
                            msg,
                            jmri.Application.getApplicationName(),
                            JmriJOptionPane.ERROR_MESSAGE);
                } else {
                    log.error(msg);
                }
            } else {
                this._password = newPassword;
            }
        }

        public boolean checkPassword(String password) {
            // Later we might store the password one way encrypted
            return _password.equals(password);
        }

        public boolean hasPermission(Permission permission) {
            return _permissions.contains(permission);
        }

        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value="SLF4J_FORMAT_SHOULD_BE_CONST",
            justification="The text is from an exception")
        public boolean checkPermission(Permission permission) {
            if (!hasPermission(permission)) {
                String msg = new PermissionDeniedException().getMessage();

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
            return true;
        }
    }


    public class PermissionAdmin implements Permission {

        @Override
        public PermissionOwner getOwner() {
            return DefaultPermissionManager.this;
        }

        @Override
        public String getName() {
            return Bundle.getMessage("DefaultPermissionManager_PermissionAdmin");
        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultPermissionManager.class);
}
