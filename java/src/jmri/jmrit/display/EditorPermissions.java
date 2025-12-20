package jmri.jmrit.display;

import jmri.*;

import org.openide.util.lookup.ServiceProvider;

/**
 * Permissions for panels.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class EditorPermissions {

    public static final PermissionOwner EDITOR_PERMISSION_OWNER =
            new EditorPermissionOwner();

    public static final Permission EDITOR_PERMISSION =
            new EditorPermission();


    @ServiceProvider(service = PermissionFactory.class)
    public static class Factory implements PermissionFactory {

        @Override
        public void register(PermissionManager manager) {
            manager.registerOwner(EDITOR_PERMISSION_OWNER);
            manager.registerPermission(EDITOR_PERMISSION);
        }

    }


    public static class EditorPermissionOwner implements PermissionOwner {

        @Override
        public String getName() {
            return Bundle.getMessage("EditorPermissions_EditorPermissionOwner");
        }

    }


    public static class EditorPermission implements EnumPermission<EditorPermissionEnum> {

        @Override
        public EditorPermissionEnum[] getValues() {
            return EditorPermissionEnum.values();
        }

        @Override
        public PermissionOwner getOwner() {
            return EDITOR_PERMISSION_OWNER;
        }

        @Override
        public String getName() {
            return Bundle.getMessage("EditorPermissions_EditorPermission");
        }

        @Override
        public String getValue(PermissionValue value) {
            if (!(value instanceof EditorPermissionEnum)) {
                throw new IllegalArgumentException("value is not a EditorPermissionEnum: "
                        + (value != null ? value.getClass().getName() : "null"));
            }
            return ((EditorPermissionEnum)value).name();
        }

        @Override
        public PermissionValue valueOf(String value) {
            // Temporary fix due to change during 5.9.5 development.
            // Can be removed once 5.9.5 is merged.
            if ("Read".equals(value)) value = "View";
            if ("ReadWrite".equals(value)) value = "ViewControl";
            if ("ReadWriteEdit".equals(value)) value = "ViewControlEdit";
            // Temporary fix due to change during 5.9.5 development.
            // Can be removed once 5.9.5 is merged.

            return EditorPermissionEnum.valueOf(value);
        }

        @Override
        public PermissionValue getDefaultPermission() {
            return EditorPermissionEnum.None;
        }

        @Override
        public PermissionValue getDefaultPermission(Role role) {
            return EditorPermissionEnum.ViewControlEdit;
        }

        @Override
        public int compare(PermissionValue o1, PermissionValue o2) {
            if (o1 instanceof EditorPermissionEnum && o2 instanceof EditorPermissionEnum) {
                EditorPermissionEnum ep1 = (EditorPermissionEnum) o1;
                EditorPermissionEnum ep2 = (EditorPermissionEnum) o2;
                return Integer.compare(ep1.ordinal(), ep2.ordinal());
            } else {
                throw new IllegalArgumentException("Cannot compare o1 and o2 since one or both is not an EditorPermissionEnum");
            }
        }

    }

    public static enum EditorPermissionEnum implements PermissionValue {
        Default(true, Bundle.getMessage("EditorPermissions_EditorPermission_Default")),
        None(false, Bundle.getMessage("EditorPermissions_EditorPermission_None")),
        View(false, Bundle.getMessage("EditorPermissions_EditorPermission_View")),
        ViewControl(false, Bundle.getMessage("EditorPermissions_EditorPermission_ViewControl")),
        ViewControlEdit(false, Bundle.getMessage("EditorPermissions_EditorPermission_ViewControlEdit"));

        final boolean _isDefault;
        final String _text;

        private EditorPermissionEnum(boolean isDefault, String text) {
            this._isDefault = isDefault;
            this._text = text;
        }

        @Override
        public boolean isDefault() {
            return _isDefault;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

}
