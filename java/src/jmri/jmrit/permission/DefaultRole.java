package jmri.jmrit.permission;

import java.util.*;

import jmri.*;

/**
 * The default implementation of Role.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class DefaultRole implements Role {

    private final String _name;
    private final boolean _systemRole;

    private final Map<Permission, Boolean> _permissions = new HashMap<>();

    public DefaultRole(String name, boolean systemRole) {
        this._name = name;
        this._systemRole = systemRole;
    }

    @Override
    public String getName() {
        return this._name;
    }

    Map<Permission,Boolean> getPermissions() {
        return this._permissions;
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return _permissions.getOrDefault(permission, false);
    }

    @Override
    public void setPermission(Permission permission, boolean enable) {
        if (! InstanceManager.getDefault(PermissionManager.class)
                .checkPermission(StandardPermissions.PERMISSION_ADMIN)) {
            return;
        }
        _permissions.put(permission, enable);
    }

    void checkThatRoleKnowsAllPermissions(Set<Permission> permissions) {
        for (Permission p : permissions) {
            if (!_permissions.containsKey(p)) {
                _permissions.put(p, true);
            }
        }
    }

}
