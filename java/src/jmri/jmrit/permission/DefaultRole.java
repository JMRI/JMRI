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
    private final int _priority;
    private final String _systemName;

    private final Map<Permission, Boolean> _permissions = new TreeMap<>((a,b) -> {return a.getName().compareTo(b.getName());});

    public DefaultRole(String name) {
        this._name = name;
        this._priority = 0;
        this._systemRole = false;
        this._systemName = null;
    }

    public DefaultRole(String name, int priority, String systemName) {
        this._name = name;
        this._priority = priority;
        this._systemRole = priority != 0;
        this._systemName = systemName;
    }

    @Override
    public String getName() {
        return this._name;
    }

    @Override
    public boolean isSystemRole() {
        return this._systemRole;
    }

    @Override
    public int getPriority() {
        return this._priority;
    }

    String getSystemName() {
        return this._systemName;
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
                .checkPermission(PermissionsSystemAdmin.PERMISSION_EDIT_PERMISSIONS)) {
            return;
        }
        _permissions.put(permission, enable);
    }

    void checkThatRoleKnowsAllPermissions(Set<Permission> permissions) {
        for (Permission p : permissions) {
            if (!_permissions.containsKey(p)) {
                _permissions.put(p, false);
            }
        }
    }

}
