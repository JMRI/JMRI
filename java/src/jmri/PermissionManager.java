package jmri;

/**
 * A manager for permissions.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public interface PermissionManager {

    void addUser(String username, String password)
            throws UserAlreadyExistsException;

    void removeUser(String username)
            throws UserDoesNotExistException;

    void changePassword(String newPassword, String oldPassword)
            throws BadPasswordException;

    void login(String username, String password)
            throws BadUserOrPasswordException;

    void logout();

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
