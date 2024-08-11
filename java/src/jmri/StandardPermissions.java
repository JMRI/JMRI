package jmri;


import org.openide.util.lookup.ServiceProvider;

/**
 * Standard permissions.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class StandardPermissions {

    public static final PermissionOwnerAdmin PERMISSION_OWNER_ADMIN =
            new PermissionOwnerAdmin();

    public static final PermissionAdmin PERMISSION_ADMIN =
            new PermissionAdmin();

    public static final PermissionEditPreferences PERMISSION_EDIT_PREFERENCES =
            new PermissionEditPreferences();


    @ServiceProvider(service = PermissionFactory.class)
    public static class Factory implements PermissionFactory {

        @Override
        public void register(PermissionManager manager) {
            manager.registerOwner(PERMISSION_OWNER_ADMIN);
            manager.registerPermission(PERMISSION_ADMIN);
            manager.registerPermission(PERMISSION_EDIT_PREFERENCES);
        }

    }


    public static class PermissionOwnerAdmin implements PermissionOwner {

        @Override
        public String getName() {
            return Bundle.getMessage("StandardPermissions_PermissionOwnerAdmin");
        }

    }

    public static class PermissionAdmin implements Permission {

        @Override
        public PermissionOwner getOwner() {
            return PERMISSION_OWNER_ADMIN;
        }

        @Override
        public String getName() {
            return Bundle.getMessage("StandardPermissions_PermissionAdmin");
        }

    }

    public static class PermissionEditPreferences implements Permission {

        @Override
        public PermissionOwner getOwner() {
            return PERMISSION_OWNER_ADMIN;
        }

        @Override
        public String getName() {
            return Bundle.getMessage("StandardPermissions_PermissionEditPreferences");
        }

    }

    // This class should never be instantiated.
    private StandardPermissions() {}


}
