package jmri;

import javax.annotation.Nonnull;

/**
 * Defines a permission.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public interface Permission {

    /**
     * Get the owner
     * @return the owner
     */
    @Nonnull
    PermissionOwner getOwner();

    /**
     * Get the name of the permission
     * @return the name
     */
    @Nonnull
    String getName();

    /**
     * Get the default permission for a role.
     * @param role the role
     * @return the default
     */
    boolean getDefaultPermission(Role role);

}
