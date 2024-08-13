package jmri;


import org.openide.util.lookup.ServiceProvider;

/**
 * Standard permissions.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class PermissionsSystemAdmin {

    public static final PermissionOwnerSystemAdmin PERMISSION_OWNER_SYSTEM_ADMIN =
            new PermissionOwnerSystemAdmin();

    public static final PermissionEditPreferences PERMISSION_EDIT_PREFERENCES =
            new PermissionEditPreferences();

    public static final PermissionEditPermissions PERMISSION_EDIT_PERMISSIONS =
            new PermissionEditPermissions();


    @ServiceProvider(service = PermissionFactory.class)
    public static class Factory implements PermissionFactory {

        @Override
        public void register(PermissionManager manager) {
            manager.registerOwner(PERMISSION_OWNER_SYSTEM_ADMIN);
            manager.registerPermission(PERMISSION_EDIT_PREFERENCES);
            manager.registerPermission(PERMISSION_EDIT_PERMISSIONS);
        }

    }


    public static class PermissionOwnerSystemAdmin implements PermissionOwner {

        @Override
        public String getName() {
            return Bundle.getMessage("PermissionsSystemAdmin_PermissionOwnerSystemAdmin");
        }

    }

    public static class PermissionEditPermissions implements Permission {

        @Override
        public PermissionOwner getOwner() {
            return PERMISSION_OWNER_SYSTEM_ADMIN;
        }

        @Override
        public String getName() {
            return Bundle.getMessage("PermissionsSystemAdmin_PermissionEditPermissions");
        }

    }

    public static class PermissionEditPreferences implements Permission {

        @Override
        public PermissionOwner getOwner() {
            return PERMISSION_OWNER_SYSTEM_ADMIN;
        }

        @Override
        public String getName() {
            return Bundle.getMessage("PermissionsSystemAdmin_PermissionEditPreferences");
        }

    }

    // This class should never be instantiated.
    private PermissionsSystemAdmin() {}


}
