package jmri;

import java.util.Locale;

/**
 * A manager for permissions.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public interface PermissionManager {

    /**
     * Listener for when the JMRI user login and logout.
     */
    interface LoginListener {

        /**
         * The JMRI user has either logged in or logged out.
         * @param isLogin true if the user has logged in, false otherwise.
         */
        void loginLogout(boolean isLogin);
    }

    /**
     * Add a new role.
     * @param name the name of the role
     * @return the new role
     * @throws RoleAlreadyExistsException if the role already exists
     */
    Role addRole(String name)
            throws RoleAlreadyExistsException;

    /**
     * Removes a role.
     * @param name the name of the role
     * @throws RoleDoesNotExistException if the role doesn't exist
     */
    void removeRole(String name)
            throws RoleDoesNotExistException;

    /**
     * Add a new user.
     * @param username the username
     * @param password the password
     * @return the new user
     * @throws UserAlreadyExistsException if the username already exists
     */
    User addUser(String username, String password)
            throws UserAlreadyExistsException;

    /**
     * Removes a role.
     * @param username the username
     * @throws UserDoesNotExistException if the user doesn't exist
     */
    void removeUser(String username)
            throws UserDoesNotExistException;

    /**
     * Change an user's password.
     * @param newPassword the new password
     * @param oldPassword the old password
     */
    void changePassword(String newPassword, String oldPassword);

    /**
     * Login locally to JMRI.
     * @param username the username
     * @param password the password
     * @return true if login was successful, false otherwise
     */
    boolean login(String username, String password);

    /**
     * Login remotely to JMRI.
     * This is for the web server, WiThrottle server, and other remote
     * connections.
     * @param sessionId the session ID. If empty string, a new session ID will
     *                  be created.
     * @param locale    the locale to be used for messages.
     * @param username  the username
     * @param password  the password
     * @return          true if successful, false otherwise
     */
    boolean remoteLogin(StringBuilder sessionId, Locale locale, String username,
                        String password);

    /**
     * Logout locally from JMRI.
     */
    void logout();

    /**
     * Logout remotely from JMRI.
     * This is for the web server, WiThrottle server, and other remote
     * connections.
     * @param sessionId the session ID
     */
    void remoteLogout(String sessionId);

    /**
     * Is an user logged in locally?
     * @return true if any user except guest is logged in, false otherwise.
     */
    boolean isLoggedIn();

    /**
     * Is an user logged in locally?
     * @param sessionId the session ID
     * @return true if any user except guest is logged in to this session,
     *         false otherwise.
     */
    boolean isRemotelyLoggedIn(String sessionId);

    /**
     * Is the user username the current user?
     * @param username the username to check
     * @return true if the current user is username, false otherwise.
     */
    boolean isCurrentUser(String username);

    /**
     * Is the user 'user' the current user?
     * @param user the user to check
     * @return true if the current user is 'user', false otherwise.
     */
    boolean isCurrentUser(User user);

    /**
     * Get the current username.
     * @return the username of the user that's currently logged in or null if
     *         no user is logged in.
     */
    String getCurrentUserName();

    /**
     * Is the current user allowed to change his password?
     * @return true if a user has logged in and that user is permitted to change
     *              his password, false otherwise
     */
    boolean isCurrentUserPermittedToChangePassword();

    /**
     * Is the user 'username' the guest user?
     * @param username the username to check
     * @return true if 'username' is the guest user
     */
    boolean isAGuestUser(String username);

    /**
     * Is the user 'user' the guest user?
     * @param user the user to check
     * @return true if 'user' is the guest user
     */
    boolean isAGuestUser(User user);

    /**
     * Add a login listener.
     * @param listener the listener
     */
    void addLoginListener(LoginListener listener);

    /**
     * Is the permission manager enabled?
     * @return true if it's enabled, false otherwise.
     */
    boolean isEnabled();

    /**
     * Set if the permission manager should be enabled or not.
     * @param enabled true if it should be enabled, false otherwise.
     */
    void setEnabled(boolean enabled);

    /**
     * Get if empty passwords is allowed.
     * @return true if empty passwords is allowed, false otherwise.
     */
    boolean isAllowEmptyPasswords();

    /**
     * Set if empty passwords should be allowed.
     * @param value true if empty passwords should be allowed, false otherwise.
     */
    void setAllowEmptyPasswords(boolean value);

    /**
     * Has the current user permission?
     * @param permission  the permission to check
     * @param minValue    the minimum value
     * @return true if the user has the permission, false otherwise
     */
    boolean hasAtLeastPermission(Permission permission, PermissionValue minValue);

    /**
     * Has the current user of this session permission?
     * @param sessionId   the session ID
     * @param permission  the permission to check
     * @param minValue    the minimum value
     * @return true if the user has the permission, false otherwise
     */
    boolean hasAtLeastRemotePermission(String sessionId, Permission permission, PermissionValue minValue);

    /**
     * Checks if the current user has the permission.
     * If not, show a message dialog if not headless. Otherwise log a message.
     * @param permission  the permission to check
     * @param minValue    the minimum value
     * @return true if the user has the permission, false otherwise
     */
    boolean ensureAtLeastPermission(Permission permission, PermissionValue minValue);

    /**
     * Register a permission owner.
     * @param owner the owner
     */
    void registerOwner(PermissionOwner owner);

    /**
     * Register a permission.
     * @param permission the permission
     */
    void registerPermission(Permission permission);

    /**
     * Store permission settings.
     */
    void storePermissionSettings();


    public static class RoleAlreadyExistsException extends JmriException {
        public RoleAlreadyExistsException() {
            super(Bundle.getMessage("PermissionManager_RoleAlreadyExistsException"));
        }
    }

    public static class RoleDoesNotExistException extends JmriException {
        public RoleDoesNotExistException() {
            super(Bundle.getMessage("PermissionManager_RoleDoesNotExistException"));
        }
    }

    public static class UserAlreadyExistsException extends JmriException {
        public UserAlreadyExistsException() {
            super(Bundle.getMessage("PermissionManager_UserAlreadyExistsException"));
        }
    }

    public static class UserDoesNotExistException extends JmriException {
        public UserDoesNotExistException() {
            super(Bundle.getMessage("PermissionManager_UserDoesNotExistException"));
        }
    }

    public static class BadUserOrPasswordException extends JmriException {
        public BadUserOrPasswordException() {
            super(Bundle.getMessage("PermissionManager_BadUserOrPasswordException"));
        }
    }

    public static class BadPasswordException extends JmriException {
        public BadPasswordException() {
            super(Bundle.getMessage("PermissionManager_BadPasswordException"));
        }
    }

}
