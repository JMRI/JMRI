package jmri;

import java.util.*;

import javax.annotation.Nonnull;

/**
 * A role in the permission system.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public interface Role {

    @Nonnull
    String getName();

    boolean isSystemRole();

    int getPriority();

    @Nonnull
    String getSystemName();

    @Nonnull
    Map<Permission,PermissionValue> getPermissions();

    @Nonnull
    PermissionValue getPermissionValue(@Nonnull Permission permission);

    void setPermission(@Nonnull Permission permission, @Nonnull PermissionValue value);

    boolean isGuestRole();

    boolean isStandardUserRole();

    boolean isAdminRole();

}
