package jmri.configurexml;

import jmri.*;

import org.openide.util.lookup.ServiceProvider;

/**
 * Owner of permissions for Load and Store classes.
 *
 * @author Daniel Bergqist (C) 2024
 */
public class LoadAndStorePermissionOwner implements PermissionOwner {

    public static final LoadAndStorePermissionOwner LOAD_AND_STORE_PERMISSION_OWNER =
            new LoadAndStorePermissionOwner();

    public static final LoadXmlFilePermission LOAD_XML_FILE_PERMISSION =
            new LoadXmlFilePermission(LOAD_AND_STORE_PERMISSION_OWNER);

    public static final StoreXmlFilePermission STORE_XML_FILE_PERMISSION =
            new StoreXmlFilePermission(LOAD_AND_STORE_PERMISSION_OWNER);


    @Override
    public String getName() {
        return Bundle.getMessage("LoadAndStorePermissionOwner_Name");
    }


    @ServiceProvider(service = PermissionFactory.class)
    public static class Factory implements PermissionFactory {

        @Override
        public void register(PermissionManager manager) {
            manager.registerOwner(LOAD_AND_STORE_PERMISSION_OWNER);
            manager.registerPermission(LOAD_XML_FILE_PERMISSION);
            manager.registerPermission(STORE_XML_FILE_PERMISSION);
        }

    }


    public static class LoadXmlFilePermission implements BooleanPermission {

        private final PermissionOwner _owner;

        private LoadXmlFilePermission(PermissionOwner owner) {
            _owner = owner;
        }

        @Override
        public PermissionOwner getOwner() {
            return _owner;
        }

        @Override
        public String getName() {
            return Bundle.getMessage("LoadAndStorePermission_Load");
        }

        @Override
        public BooleanValue getDefaultPermission(Role role) {
            return BooleanValue.get(role.isAdminRole());
        }

    }


    public static class StoreXmlFilePermission implements BooleanPermission {

        private final PermissionOwner _owner;

        private StoreXmlFilePermission(PermissionOwner owner) {
            _owner = owner;
        }

        @Override
        public PermissionOwner getOwner() {
            return _owner;
        }

        @Override
        public String getName() {
            return Bundle.getMessage("LoadAndStorePermission_Store");
        }

        @Override
        public BooleanValue getDefaultPermission(Role role) {
            return BooleanValue.get(role.isAdminRole());
        }

    }
}
