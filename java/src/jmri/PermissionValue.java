package jmri;

/**
 * A value of a permission.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public interface PermissionValue {

    /**
     * Is this the default value of this permission?
     * @return true if it's the default, false otherwise
     */
    boolean isDefault();

}
