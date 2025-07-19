package jmri.jmrit.permission;

import java.awt.GraphicsEnvironment;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.xml.bind.DatatypeConverter;

import jmri.*;
import jmri.PermissionValue;
import jmri.util.swing.JmriJOptionPane;

/**
 * The default implementation of User.
 *
 * @author Daniel Bergqvist (C) 2024
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value="DMI_RANDOM_USED_ONLY_ONCE",
    justification = "False positive. The Random instance is kept by the iterator.")

public class DefaultUser implements User {

    private final String _username;
    private final String _systemUserName;
    private final boolean _systemUser;
    private final int _priority;
    private String _seed;
    private String _passwordMD5;
    private String _name = "";
    private String _comment = "";

    private final Set<Role> _roles = new TreeSet<>((a,b) -> {return a.getName().compareTo(b.getName());});

    public DefaultUser(String username, String password) {
        this(username, password, 0, null, new Role[]{});
        DefaultUser.this.addRole(DefaultRole.ROLE_STANDARD_USER);
    }

    DefaultUser(DefaultUser u) {
        _username = u._username;
        _systemUserName = u._systemUserName;
        _systemUser = u._systemUser;
        _priority = u._priority;
        _seed = u._seed;
        _passwordMD5 = u._passwordMD5;
        _name = u._name;
        _comment = u._comment;
        _roles.addAll(u._roles);
    }

    DefaultUser(String username, String password, int priority, String systemUserName, Role[] roles) {
        this._username = username;
        this._priority = priority;
        this._systemUser = priority != 0;
        this._systemUserName = systemUserName;
        if (password != null) {
            this._seed = getRandomString(10);
            try {
                this._passwordMD5 = getPasswordSHA256(password);
            } catch (NoSuchAlgorithmException e) {
                log.error("MD5 algoritm doesn't exists", e);
            }
        } else {
            this._seed = null;
        }
        for (Role role : roles) {
            _roles.add(role);
        }
    }

    public DefaultUser(String username, String passwordMD5, String seed) {
        this._username = username;
        this._systemUserName = null;
        this._systemUser = false;
        this._priority = 0;
        this._passwordMD5 = passwordMD5;
        this._seed = seed;
    }

    private static final PrimitiveIterator.OfInt iterator =
            new Random().ints('a', 'z'+10).iterator();

    public static String getRandomString(int count) {
        StringBuilder s = new StringBuilder();
        for (int i=0; i < count; i++) {
            int r = iterator.nextInt();
            char c = (char) (r > 'z' ? r-'z'+'0' : r);
            s.append(c);
        }
        return s.toString();
    }

    @Override
    public String getUserName() {
        return this._username;
    }

    @Override
    public boolean isSystemUser() {
        return this._systemUser;
    }

    @Override
    public int getPriority() {
        return this._priority;
    }

    String getSystemUsername() {
        return this._systemUserName;
    }

    String getPassword() {
        return this._passwordMD5;
    }

    void setPasswordMD5(String passwordMD5) {
        this._passwordMD5 = passwordMD5;
    }

    String getSeed() {
        return this._seed;
    }

    void setSeed(String seed) {
        this._seed = seed;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public void setName(String name) {
        this._name = name;
    }

    @Override
    public String getComment() {
        return _comment;
    }

    @Override
    public void setComment(String comment) {
        this._comment = comment;
    }

    @Override
    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(_roles);
    }

    @Override
    public void addRole(Role role) {
        if (! InstanceManager.getDefault(PermissionManager.class)
                .ensureAtLeastPermission(PermissionsSystemAdmin.PERMISSION_EDIT_PREFERENCES,
                        BooleanPermission.BooleanValue.TRUE)) {
            return;
        }
        _roles.add(role);
    }

    @Override
    public void removeRole(Role role) {
        if (! InstanceManager.getDefault(PermissionManager.class)
                .ensureAtLeastPermission(PermissionsSystemAdmin.PERMISSION_EDIT_PREFERENCES,
                        BooleanPermission.BooleanValue.TRUE)) {
            return;
        }
        _roles.remove(role);
    }

    void setRoles(Set<Role> roles) {
        _roles.clear();
        _roles.addAll(roles);
    }

    private String getPasswordSHA256(String password) throws NoSuchAlgorithmException {
        String passwd = this._seed + password;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(passwd.getBytes());
        return DatatypeConverter
                .printHexBinary(md.digest()).toUpperCase();
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value="SLF4J_FORMAT_SHOULD_BE_CONST",
        justification="The text is from an exception")
    public void setPassword(String newPassword) {
        PermissionManager pMngr = InstanceManager.getDefault(PermissionManager.class);

        if (!pMngr.hasAtLeastPermission(
                PermissionsSystemAdmin.PERMISSION_EDIT_PERMISSIONS,
                BooleanPermission.BooleanValue.TRUE)) {
            log.warn("The current user has not permission to change password for user {}", getUserName());

            if (!GraphicsEnvironment.isHeadless()) {
                JmriJOptionPane.showMessageDialog(null,
                        Bundle.getMessage("DefaultPermissionManager_PermissionDenied"),
                        jmri.Application.getApplicationName(),
                        JmriJOptionPane.ERROR_MESSAGE);
            }
        }

        try {
            this._passwordMD5 = getPasswordSHA256(newPassword);
        } catch (NoSuchAlgorithmException e) {
            String msg = "MD5 algoritm doesn't exists";
            log.error(msg);
            throw new RuntimeException(msg);
        }
    }

    @Override
    public boolean isPermittedToChangePassword() {
        PermissionManager pMngr = InstanceManager.getDefault(PermissionManager.class);

        boolean isCurrentUser = pMngr.isCurrentUser(this);
        boolean hasEditPasswordPermission = pMngr.hasAtLeastPermission(
                PermissionsSystemAdmin.PERMISSION_EDIT_OWN_PASSWORD,
                BooleanPermission.BooleanValue.TRUE);
        boolean hasAdminPermission = pMngr.hasAtLeastPermission(
                PermissionsSystemAdmin.PERMISSION_EDIT_PERMISSIONS,
                BooleanPermission.BooleanValue.TRUE);

        return (hasAdminPermission || (isCurrentUser && hasEditPasswordPermission));
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value="SLF4J_FORMAT_SHOULD_BE_CONST",
        justification="The text is from an exception")
    public boolean changePassword(String oldPassword, String newPassword) {
        PermissionManager pMngr = InstanceManager.getDefault(PermissionManager.class);

        if (isPermittedToChangePassword()) {
            if (!checkPassword(oldPassword)) {
                String msg = new PermissionManager.BadPasswordException().getMessage();

                if (!GraphicsEnvironment.isHeadless()) {
                    JmriJOptionPane.showMessageDialog(null,
                            msg,
                            jmri.Application.getApplicationName(),
                            JmriJOptionPane.ERROR_MESSAGE);
                } else {
                    log.error(msg);
                }
            } else {
                try {
                    this._passwordMD5 = getPasswordSHA256(newPassword);
                    return true;
                } catch (NoSuchAlgorithmException e) {
                    String msg = "MD5 algoritm doesn't exists";
                    log.error(msg);
                    throw new RuntimeException(msg);
                }
            }
        } else {
            if (pMngr.isCurrentUser(this)) {
                log.warn("User {} has not permission to change its own password", getUserName());
            } else {
                log.warn("The current user has not permission to change password for user {}", getUserName());
            }
            if (!GraphicsEnvironment.isHeadless()) {
                JmriJOptionPane.showMessageDialog(null,
                        Bundle.getMessage("DefaultPermissionManager_PermissionDenied"),
                        jmri.Application.getApplicationName(),
                        JmriJOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    public boolean checkPassword(String password) {
        try {
            return _passwordMD5.equals(getPasswordSHA256(password));
        } catch (NoSuchAlgorithmException e) {
            String msg = "MD5 algoritm doesn't exists";
            log.error(msg);
            throw new RuntimeException(msg);
        }
    }

    @Override
    public boolean hasAtLeastPermission(Permission permission, PermissionValue minValue) {
        PermissionValue lastValue = null;
        // Try to find the highest permission this user has
        for (Role role : _roles) {
            PermissionValue value = role.getPermissionValue(permission);
            if (lastValue == null || permission.compare(lastValue, value) < 0) {
                lastValue = value;
            }
        }
        if (lastValue == null || lastValue.isDefault()) {
            // No role of this user have a permission value set, or the
            // permission value is the default.
            // Try to find the highest default permission this user has
            for (Role role : _roles) {
                PermissionValue value = permission.getDefaultPermission(role);
                if (lastValue == null || permission.compare(lastValue, value) < 0) {
                    lastValue = value;
                }
            }
        }
        if (lastValue == null || lastValue.isDefault()) {
            // This user has no roles or the permission value is the default.
            // Get the default permission if no role.
            lastValue = permission.getDefaultPermission();
        }
        return permission.compare(minValue, lastValue) <= 0;
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value="SLF4J_FORMAT_SHOULD_BE_CONST",
        justification="The text is from a bundle")
    public boolean ensureAtLeastPermission(Permission permission, PermissionValue minValue) {
        if (!hasAtLeastPermission(permission, minValue)) {
            log.warn("User {} has not permission {}", this.getUserName(), permission.getName());
            if (!GraphicsEnvironment.isHeadless()) {
                JmriJOptionPane.showMessageDialog(null,
                        Bundle.getMessage("DefaultPermissionManager_PermissionDenied"),
                        jmri.Application.getApplicationName(),
                        JmriJOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultUser.class);
}
