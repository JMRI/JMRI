package jmri;

/**
 * Factory class for Permission classes.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public interface PermissionFactory {

    /**
     * Register the permissions that this factory provides.
     * Note that the owner of the permissions must be registered before
     * the permissions are registered.
     * @param manager the permission manager
     */
    void register(PermissionManager manager);

}
