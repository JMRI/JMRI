package jmri;

import java.util.Comparator;

import javax.annotation.Nonnull;

/**
 * Defines a permission.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public interface Permission {

    /**
     * Get the parent permission, if any.
     * @return the parent permission or null if no parent
     */
    default public Permission getParent() {
        return null;
    }

    /**
     * Get the owner
     * @return the owner
     */
    @Nonnull
    PermissionOwner getOwner();

    /**
     * Get the name of the permission
     * @return the name
     */
    @Nonnull
    String getName();

    /**
     * Get the default permission for a role.
     * @param role the role
     * @return the default
     */
    boolean getDefaultPermission(Role role);


    public static class PermissionComparator implements Comparator<Permission> {
        @Override
        public int compare(Permission a, Permission b) {
            Permission ap = a.getParent();
            Permission bp = b.getParent();
            if (ap != null && bp != null) {
                return ap.getName().compareTo(bp.getName());
            } else if (ap != null) {
                if (ap == b) return 1;
                return ap.getName().compareTo(b.getName());
            } else if (bp != null) {
                if (a == bp) return -1;
                return a.getName().compareTo(bp.getName());
            } else {
                return a.getName().compareTo(b.getName());
            }
        }
    }

}
