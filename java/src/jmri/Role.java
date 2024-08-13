package jmri;

import java.util.*;

/**
 * A role in the permission system.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public interface Role {

    String getName();

    boolean isSystemRole();

    int getPriority();

    String getSystemName();

    Map<Permission,Boolean> getPermissions();

    boolean hasPermission(Permission permission);

    void setPermission(Permission permission, boolean enable);

    boolean isGuestRole();

    boolean isStandardUserRole();

    boolean isAdminRole();

}
