package jmri.managers;

import java.awt.GraphicsEnvironment;
import java.io.*;
import java.util.*;

import jmri.*;
import jmri.jmrit.XmlFile;
import static jmri.jmrit.XmlFile.newDocument;
import jmri.util.FileUtil;
import jmri.util.swing.JmriJOptionPane;

import org.jdom2.*;
import org.springframework.util.DigestUtils;

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
public class DefaultPermissionManager
        implements PermissionManager, PermissionOwner {

    private static final User GUEST_USER =
            new User(PermissionManager.GUEST_USERNAME, null);

    private final Map<String, User> _users = new HashMap<>();
    private final Set<PermissionOwner> _owners = new HashSet<>();
    private final Set<Permission> _permissions = new HashSet<>();

    private boolean _permissionsEnabled = false;
    private User _currentUser = GUEST_USER;


    public DefaultPermissionManager init() {
        _users.put("daniel", new User("daniel", "12345678"));
        _users.put("kalle", new User("kalle", "testtest"));
        _users.put("sven", new User("sven", "testtest"));

        DefaultPermissionManager.this.registerOwner(this);
        DefaultPermissionManager.this.registerPermission(new PermissionAdmin());
        for (PermissionFactory factory : ServiceLoader.load(PermissionFactory.class)) {
            factory.register(this);
        }
        loadPermissionSettings();
        return this;
    }

    private void loadPermissionSettings() {
        File file = new File(FileUtil.getPreferencesPath() + ".permissions.xml");
        log.warn(file.getAbsolutePath());

        Properties p = new Properties();
        if (file.exists() && file.length() != 0) {
            try {
                Element root = new XmlFile().rootFromFile(file);


                // writeXML

            } catch (JDOMException | IOException ex) {
                log.error("Exception during loading of permissions", ex);
            }

            log.error("Loading permissions");
        } else {
            log.error("Not loading permissions");
        }

        storePermissionSettings();
    }

    private void storePermissionSettings() {
        File file = new File(FileUtil.getPreferencesPath() + ".permissions.xml");

        try {
            // Document doc = newDocument(root, dtdLocation+"layout-config-"+dtdVersion+".dtd");
            Element root = new Element("Permissions");

            Element settings = new Element("Settings");
            settings.addContent(new Element("Enabled")
                    .addContent(this._permissionsEnabled ? "yes" : "no"));
            root.addContent(settings);

            Element users = new Element("Users");
            for (User user : _users.values()) {
                Element userElement = new Element("User");
                userElement.addContent(new Element("Username").addContent(user._username));
                userElement.addContent(new Element("Password").addContent(user._passwordMD5));
                userElement.addContent(new Element("Seed").addContent(user._seed));
                users.addContent(userElement);
            }
            root.addContent(users);

            Document doc = newDocument(root);
            new XmlFile().writeXML(file, doc);

        } catch (java.io.FileNotFoundException ex3) {
            log.error("FileNotFound error writing file: {}", file);
        } catch (java.io.IOException ex2) {
            log.error("IO error writing file: {}", file);
        }
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


    public static class User {

        public final String _username;
        public final String _seed;
        public String _passwordMD5;

        private final Set<Permission> _permissions = new HashSet<>();

        public User(String username, String password) {
            this._username = username;
            if (password != null) {
                this._seed = getRandomString(10);
                String passwd = this._seed + password;
                this._passwordMD5 = DigestUtils.md5DigestAsHex(passwd.getBytes()).toUpperCase();
            } else {
                this._seed = null;
            }
        }

        public User(String username, String passwordMD5, String seed) {
            this._username = username;
            this._passwordMD5 = passwordMD5;
            this._seed = seed;
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
                this._passwordMD5 = newPassword;
            }
        }

        public boolean checkPassword(String password) {
            // Later we might store the password one way encrypted
            return _passwordMD5.equals(password);
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
