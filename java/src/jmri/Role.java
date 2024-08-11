package jmri;

/**
 * A role in the permission system.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public interface Role {

    String getName();

    boolean hasPermission(Permission permission);

    void setPermission(Permission permission, boolean enable);

}
