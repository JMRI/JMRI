package jmri;

import java.util.Comparator;

import javax.annotation.Nonnull;

/**
 * Defines a permission.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public interface Permission extends Comparator<PermissionValue> {

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

    String getValue(PermissionValue value);

    PermissionValue valueOf(String value);

    /**
     * Get the default permission if the user has no role.
     * @return the default
     */
    PermissionValue getDefaultPermission();

    /**
     * Get the default permission for a role.
     * @param role the role
     * @return the default
     */
    PermissionValue getDefaultPermission(Role role);

}
