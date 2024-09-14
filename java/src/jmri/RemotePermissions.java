package jmri;

import org.openide.util.lookup.ServiceProvider;

/**
 * Standard permissions.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class RemotePermissions {

    public static final PermissionOwner PERMISSION_OWNER_REMOTE =
            new PermissionOwnerRemote();

    public static final Permission PERMISSION_VIEW_PANELS =
            new PermissionViewPanels();

    public static final Permission PERMISSION_VIEW_PANELS_ALLOW_OVERRIDE =
            new PermissionViewPanels_AllowOverride();

//    public static final Permission PERMISSION_LIST_NAMED_BEANS =
//            new PermissionListNamedBeans();

//    public static final Permission PERMISSION_CHANGE_STATE_NAMED_BEANS =
//            new PermissionChangeStateNamedBeans();


    @ServiceProvider(service = PermissionFactory.class)
    public static class Factory implements PermissionFactory {

        @Override
        public void register(PermissionManager manager) {
            manager.registerOwner(PERMISSION_OWNER_REMOTE);
            manager.registerPermission(PERMISSION_VIEW_PANELS);
            manager.registerPermission(PERMISSION_VIEW_PANELS_ALLOW_OVERRIDE);
//            manager.registerPermission(PERMISSION_LIST_NAMED_BEANS);
//            manager.registerPermission(PERMISSION_CHANGE_STATE_NAMED_BEANS);
        }

    }


    public static class PermissionOwnerRemote implements PermissionOwner {

        @Override
        public String getName() {
            return Bundle.getMessage("RemotePermissions_PermissionOwnerRemote");
        }

    }

    public static class PermissionViewPanels implements Permission {

        @Override
        public PermissionOwner getOwner() {
            return PERMISSION_OWNER_REMOTE;
        }

        @Override
        public String getName() {
            return Bundle.getMessage("RemotePermissions_PermissionViewPanels");
        }

        @Override
        public boolean getDefaultPermission(Role role) {
            return true;
        }

    }

    public static class PermissionViewPanels_AllowOverride implements Permission {

        @Override
        public Permission getParent() {
            return PERMISSION_VIEW_PANELS;
        }

        @Override
        public PermissionOwner getOwner() {
            return PERMISSION_OWNER_REMOTE;
        }

        @Override
        public String getName() {
            return Bundle.getMessage("PermissionViewPanels_AllowOverride");
        }

        @Override
        public boolean getDefaultPermission(Role role) {
            return true;
        }

    }

    public static class PermissionChangeStateNamedBeans implements Permission {

        @Override
        public PermissionOwner getOwner() {
            return PERMISSION_OWNER_REMOTE;
        }

        @Override
        public String getName() {
            return Bundle.getMessage("RemotePermissions_PermissionChangeStateNamedBeans");
        }

        @Override
        public boolean getDefaultPermission(Role role) {
            return true;
        }

    }

    public static class PermissionListNamedBeans implements Permission {

        @Override
        public PermissionOwner getOwner() {
            return PERMISSION_OWNER_REMOTE;
        }

        @Override
        public String getName() {
            return Bundle.getMessage("RemotePermissions_PermissionListNamedBeans");
        }

        @Override
        public boolean getDefaultPermission(Role role) {
            return true;
        }

    }

    // This class should never be instantiated.
    private RemotePermissions() {}


}
