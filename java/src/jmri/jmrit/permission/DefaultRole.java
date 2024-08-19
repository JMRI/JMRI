package jmri.jmrit.permission;

import java.util.*;

import jmri.*;

/**
 * A role in the permission system.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class DefaultRole implements Role {

    public static final Role ROLE_GUEST =
            new DefaultRole(Bundle.getMessage("Role_Guest"),50,"GUEST");

    public static final Role ROLE_STANDARD_USER =
            new DefaultRole(Bundle.getMessage("Role_StandardUser"),10,"STANDARD_USER");

    public static final Role ROLE_ADMIN =
            new DefaultRole(Bundle.getMessage("Role_Admin"),100,"ADMIN");

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

    @Override
    public String getSystemName() {
        return this._systemName;
    }

    @Override
    public Map<Permission,Boolean> getPermissions() {
        return Collections.unmodifiableMap(_permissions);
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

    void setPermissionWithoutCheck(Permission permission, boolean enable) {
        _permissions.put(permission, enable);
    }

    @Override
    public boolean isGuestRole() {
        return this == ROLE_GUEST;
    }

    @Override
    public boolean isStandardUserRole() {
        return this == ROLE_STANDARD_USER;
    }

    @Override
    public boolean isAdminRole() {
        return this == ROLE_ADMIN;
    }

}
