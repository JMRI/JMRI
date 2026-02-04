package jmri;

import org.openide.util.lookup.ServiceProvider;

/**
 * Permissions for the Programmers.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class PermissionsProgrammer {

    public static final PermissionOwner PERMISSION_OWNER_PROGRAMMING =
            new PermissionOwnerProgramming();

    public static final Permission PERMISSION_PROGRAMMING_TRACK =
            new PermissionProgrammingTrack();

    public static final Permission PERMISSION_PROGRAMMING_ON_MAIN =
            new PermissionProgrammingOnMain();


    @ServiceProvider(service = PermissionFactory.class)
    public static class Factory implements PermissionFactory {

        @Override
        public void register(PermissionManager manager) {
            manager.registerOwner(PERMISSION_OWNER_PROGRAMMING);
            manager.registerPermission(PERMISSION_PROGRAMMING_TRACK);
            manager.registerPermission(PERMISSION_PROGRAMMING_ON_MAIN);
        }

    }


    public static class PermissionOwnerProgramming implements PermissionOwner {

        @Override
        public String getName() {
            return Bundle.getMessage("PermissionOwnerProgramming_PermissionOwnerProgramming");
        }

    }

    public static class PermissionProgrammingTrack implements BooleanPermission {

        @Override
        public PermissionOwner getOwner() {
            return PERMISSION_OWNER_PROGRAMMING;
        }

        @Override
        public String getName() {
            return Bundle.getMessage("PermissionOwnerProgramming_PermissionProgrammingTrack");
        }

        @Override
        public BooleanValue getDefaultPermission(Role role) {
            return BooleanValue.get(role.isAdminRole());
        }

    }

    public static class PermissionProgrammingOnMain implements BooleanPermission {

        @Override
        public PermissionOwner getOwner() {
            return PERMISSION_OWNER_PROGRAMMING;
        }

        @Override
        public String getName() {
            return Bundle.getMessage("PermissionOwnerProgramming_PermissionProgrammingOnMain");
        }

        @Override
        public BooleanValue getDefaultPermission(Role role) {
            return BooleanValue.get(role.isAdminRole());
        }

    }

    // This class should never be instantiated.
    private PermissionsProgrammer() {}

}
