package jmri;

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

    public static final PermissionAddUser PERMISSION_ADD_USER =
            new PermissionAddUser();


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

    public static class PermissionAddUser implements Permission {

        @Override
        public PermissionOwner getOwner() {
            return PERMISSION_OWNER_ADMIN;
        }

        @Override
        public String getName() {
            return Bundle.getMessage("StandardPermissions_PermissionAddUser");
        }

    }

    // This class should never be instantiated.
    private StandardPermissions() {}


}
