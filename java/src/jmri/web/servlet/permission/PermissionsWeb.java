package jmri.web.servlet.permission;


import jmri.*;

import org.openide.util.lookup.ServiceProvider;

/**
 * Standard permissions.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class PermissionsWeb {

    public static final PermissionOwner PERMISSION_OWNER_WEB =
            new PermissionOwnerWeb();

    public static final Permission PERMISSION_LIST_NAMED_BEANS =
            new PermissionListNamedBeans();

    public static final Permission PERMISSION_CHANGE_STATE_NAMED_BEANS =
            new PermissionChangeStateNamedBeans();


    @ServiceProvider(service = PermissionFactory.class)
    public static class Factory implements PermissionFactory {

        @Override
        public void register(PermissionManager manager) {
            manager.registerOwner(PERMISSION_OWNER_WEB);
            manager.registerPermission(PERMISSION_LIST_NAMED_BEANS);
            manager.registerPermission(PERMISSION_CHANGE_STATE_NAMED_BEANS);
        }

    }


    public static class PermissionOwnerWeb implements PermissionOwner {

        @Override
        public String getName() {
            return Bundle.getMessage("PermissionOwnerWeb_PermissionOwnerWeb");
        }

    }

    public static class PermissionChangeStateNamedBeans implements Permission {

        @Override
        public PermissionOwner getOwner() {
            return PERMISSION_OWNER_WEB;
        }

        @Override
        public String getName() {
            return Bundle.getMessage("PermissionOwnerWeb_PermissionChangeStateNamedBeans");
        }

        @Override
        public boolean getDefaultPermission(Role role) {
            return role.isAdminRole();
        }

    }

    public static class PermissionListNamedBeans implements Permission {

        @Override
        public PermissionOwner getOwner() {
            return PERMISSION_OWNER_WEB;
        }

        @Override
        public String getName() {
            return Bundle.getMessage("PermissionOwnerWeb_PermissionListNamedBeans");
        }

        @Override
        public boolean getDefaultPermission(Role role) {
            return role.isAdminRole();
        }

    }

    // This class should never be instantiated.
    private PermissionsWeb() {}


}
