package jmri.jmrit.permission;


import java.awt.GraphicsEnvironment;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.xml.bind.DatatypeConverter;

import jmri.*;
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
    private String _seed;
    private String _passwordMD5;
    private final boolean _systemUser;
    private final String _systemUserName;

    private final Set<Role> _roles = new TreeSet<>((a,b) -> {return a.getName().compareTo(b.getName());});

    public DefaultUser(String username, String password) {
        this(username, password, false, null);
        DefaultUser.this.addRole(DefaultPermissionManager.ROLE_STANDARD_USER);
    }

    public DefaultUser(String username, String password, boolean systemUser, String systemUserName) {
        this._username = username;
        this._systemUser = systemUser;
        this._systemUserName = systemUserName;
        if (password != null) {
            this._seed = getRandomString(10);
            try {
                this._passwordMD5 = getPasswordMD5(this._seed + password);
            } catch (NoSuchAlgorithmException e) {
                log.error("MD5 algoritm doesn't exists", e);
            }
        } else {
            this._seed = null;
        }
    }

    public DefaultUser(String username, String passwordMD5, String seed) {
        this._username = username;
        this._passwordMD5 = passwordMD5;
        this._seed = seed;
        this._systemUser = false;
        this._systemUserName = null;
    }

    private static final PrimitiveIterator.OfInt iterator =
            new Random().ints('a', 'z'+10).iterator();

    private String getRandomString(int count) {
        StringBuilder s = new StringBuilder();
        for (int i=0; i < count; i++) {
            int r = iterator.nextInt();
            char c = (char) (r > 'z' ? r-'z'+'0' : r);
            s.append(c);
        }
        return s.toString();
    }

    @Override
    public String getName() {
        return this._username;
    }

    boolean isSystemUser() {
        return this._systemUser;
    }

    String getSystemUsername() {
        return this._systemUserName;
    }

    String getPassword() {
        return this._passwordMD5;
    }

    void setPassword(String passwordMD5) {
        this._passwordMD5 = passwordMD5;
    }

    String getSeed() {
        return this._seed;
    }

    void setSeed(String seed) {
        this._seed = seed;
    }

    @Override
    public Set<Role> getRoles() {
        return _roles;
    }

    @Override
    public void addRole(Role role) {
        _roles.add(role);
    }

    @Override
    public void removeRole(Role role) {
        InstanceManager.getDefault(PermissionManager.class).checkPermission(StandardPermissions.PERMISSION_ADMIN);
        _roles.remove(role);
    }

    private String getPasswordMD5(String password) throws NoSuchAlgorithmException {
        String passwd = this._seed + password;
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(passwd.getBytes());
        return DatatypeConverter
                .printHexBinary(md.digest()).toUpperCase();
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value="SLF4J_FORMAT_SHOULD_BE_CONST",
        justification="The text is from an exception")
    public void changePassword(String newPassword, String oldPassword) {
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
                this._passwordMD5 = getPasswordMD5(newPassword);
            } catch (NoSuchAlgorithmException e) {
                String msg = "MD5 algoritm doesn't exists";
                log.error(msg);
                throw new RuntimeException(msg);
            }
        }
    }

    public boolean checkPassword(String password) {
        try {
            return _passwordMD5.equals(getPasswordMD5(password));
        } catch (NoSuchAlgorithmException e) {
            String msg = "MD5 algoritm doesn't exists";
            log.error(msg);
            throw new RuntimeException(msg);
        }
    }

    @Override
    public boolean hasPermission(Permission permission) {
        for (Role role : _roles) {
            if (role.hasPermission(permission)) return true;
        }
        return false;
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value="SLF4J_FORMAT_SHOULD_BE_CONST",
        justification="The text is from a bundle")
    public boolean checkPermission(Permission permission) {
        if (!hasPermission(permission)) {
            log.error("User {} has not permission {}", this.getName(), permission.getName());
            if (!GraphicsEnvironment.isHeadless()) {
                JmriJOptionPane.showMessageDialog(null,
                        Bundle.getMessage("DefaultPermissionManager_PermissionDenied"),
                        jmri.Application.getApplicationName(),
                        JmriJOptionPane.ERROR_MESSAGE);
            } else {
                log.warn(Bundle.getMessage("DefaultPermissionManager_PermissionDenied"));
            }
            return false;
        }
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultUser.class);
}
