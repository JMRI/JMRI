package jmri.jmrit.trainrecord;

import jmri.BooleanPermission;
import jmri.Permission;
import jmri.PermissionFactory;
import jmri.PermissionManager;
import jmri.PermissionOwner;
import jmri.Role;

import org.openide.util.lookup.ServiceProvider;

/**
 * Train tracking's permissions, for JMRI's permission manager. Correcting
 * where trains are is allowed to everyone unless the permissions say
 * otherwise.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public final class PermissionsTrainTracking {

    /** The owner of train tracking's permissions. */
    public static final PermissionOwner PERMISSION_OWNER_TRAIN_TRACKING = new PermissionOwnerTrainTracking();

    /** Permission to tell train tracking where trains are, or that a Block is empty. */
    public static final Permission PERMISSION_CORRECT_TRAINS = new PermissionCorrectTrains();

    private PermissionsTrainTracking() {
    }

    /**
     * Registers the permissions.
     */
    @ServiceProvider(service = PermissionFactory.class)
    public static class Factory implements PermissionFactory {

        @Override
        public void register(PermissionManager manager) {
            manager.registerOwner(PERMISSION_OWNER_TRAIN_TRACKING);
            manager.registerPermission(PERMISSION_CORRECT_TRAINS);
        }
    }

    /**
     * The owner: train tracking.
     */
    public static class PermissionOwnerTrainTracking implements PermissionOwner {

        @Override
        public String getName() {
            return Bundle.getMessage("PermissionOwnerTrainTracking");
        }
    }

    /**
     * Correcting where trains are.
     */
    public static class PermissionCorrectTrains implements BooleanPermission {

        @Override
        public PermissionOwner getOwner() {
            return PERMISSION_OWNER_TRAIN_TRACKING;
        }

        @Override
        public String getName() {
            return Bundle.getMessage("PermissionCorrectTrains");
        }

        @Override
        public BooleanValue getDefaultPermission(Role role) {
            return BooleanValue.TRUE;
        }
    }

    /**
     * @return true if the current user may correct where trains are
     */
    public static boolean mayCorrect() {
        return jmri.InstanceManager.getDefault(PermissionManager.class)
                .hasAtLeastPermission(PERMISSION_CORRECT_TRAINS, BooleanPermission.BooleanValue.TRUE);
    }
}
