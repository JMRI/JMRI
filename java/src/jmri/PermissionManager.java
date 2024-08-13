package jmri;

/**
 * A manager for permissions.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public interface PermissionManager {

    interface LoginListener {
        void loginLogout(boolean isLogin);
    }

    Role addRole(String name)
            throws RoleAlreadyExistsException;

    void removeRole(String name)
            throws RoleDoesNotExistException;

    User addUser(String username, String password)
            throws UserAlreadyExistsException;

    void removeUser(String username)
            throws UserDoesNotExistException;

    void changePassword(String newPassword, String oldPassword);

    boolean login(String username, String password);

    void logout();

    boolean isLoggedIn();

    boolean isCurrentUser(String username);

    boolean isCurrentUser(User user);

    String getCurrentUserName();

    boolean isGuestUser(User user);

    void addLoginListener(LoginListener listener);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    boolean isAllowEmptyPasswords();

    void setAllowEmptyPasswords(boolean value);

    boolean hasPermission(Permission permission);

    /**
     * Checks if the current user has the permission.
     * If not, show a message dialog if not headless. Otherwise log a message.
     * @param permission the permission to check
     * @return true if the user has the permission, false otherwise
     */
    boolean checkPermission(Permission permission);

    void registerOwner(PermissionOwner owner);

    void registerPermission(Permission permission);

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
