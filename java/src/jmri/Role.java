package jmri;

import java.util.*;

/**
 * A role in the permission system.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class Role {

    public static final Role ROLE_GUEST =
            new Role(Bundle.getMessage("Role_Guest"),50,"GUEST");

    public static final Role ROLE_STANDARD_USER =
            new Role(Bundle.getMessage("Role_StandardUser"),10,"STANDARD_USER");

    public static final Role ROLE_ADMIN =
            new Role(Bundle.getMessage("Role_Admin"),100,"ADMIN");

    private final String _name;
    private final boolean _systemRole;
    private final int _priority;
    private final String _systemName;

    private final Map<Permission, Boolean> _permissions = new TreeMap<>((a,b) -> {return a.getName().compareTo(b.getName());});

    public Role(String name) {
        this._name = name;
        this._priority = 0;
        this._systemRole = false;
        this._systemName = null;
    }

    public Role(String name, int priority, String systemName) {
        this._name = name;
        this._priority = priority;
        this._systemRole = priority != 0;
        this._systemName = systemName;
    }

    public String getName() {
        return this._name;
    }

    public boolean isSystemRole() {
        return this._systemRole;
    }

    public int getPriority() {
        return this._priority;
    }

    public String getSystemName() {
        return this._systemName;
    }

    public Map<Permission,Boolean> getPermissions() {
        return this._permissions;
    }

    public boolean hasPermission(Permission permission) {
        return _permissions.getOrDefault(permission, false);
    }

    public void setPermission(Permission permission, boolean enable) {
        if (! InstanceManager.getDefault(PermissionManager.class)
                .checkPermission(PermissionsSystemAdmin.PERMISSION_EDIT_PERMISSIONS)) {
            return;
        }
        _permissions.put(permission, enable);
    }

}
