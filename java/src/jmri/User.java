package jmri;

import java.util.Set;

/**
 * An user in the permission system.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public interface User {

    String getName();

    void changePassword(String oldPassword, String newPassword);

    Set<Role> getRoles();

    void addRole(Role role);

    void removeRole(Role role);

    boolean hasPermission(Permission permission);

    /**
     * Checks if the current user has the permission.
     * If not, show a message dialog if not headless. Otherwise log a message.
     * @param permission the permission to check
     * @return true if the user has the permission, false otherwise
     */
    boolean checkPermission(Permission permission);

}
