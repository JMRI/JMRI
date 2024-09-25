package jmri;

/**
 * Defines a permission based on a boolean.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public interface BooleanPermission extends Permission {

    public static class BooleanValue implements PermissionValue {

        public static final BooleanValue TRUE = new BooleanValue(true);
        public static final BooleanValue FALSE = new BooleanValue(false);

        private final boolean _value;

        public boolean get() {
            return _value;
        }

        public static BooleanValue get(boolean value) {
            return value ? TRUE : FALSE;
        }

        private BooleanValue(boolean value) {
            this._value = value;
        }

        @Override
        public boolean isDefault() {
            return false;
        }

        @Override
        public String toString() {
            return Boolean.toString(_value);
        }
    }

    @Override
    default String getValue(PermissionValue value) {
        if (!(value instanceof BooleanValue)) {
            throw new IllegalArgumentException("value is not a BooleanValue");
        }
        return ((BooleanValue)value).get() ? "yes" : "no";
    }

    @Override
    default PermissionValue valueOf(String value) {
        return BooleanValue.get("yes".equals(value));
    }

    /**
     * Get the default permission for a role.
     * @return the default
     */
    @Override
    default BooleanValue getDefaultPermission() {
        return BooleanValue.FALSE;
    }

    /**
     * Get the default permission for a role.
     * @param role the role
     * @return the default
     */
    @Override
    BooleanValue getDefaultPermission(Role role);

    @Override
    default int compare(PermissionValue o1, PermissionValue o2) {
        if (o1 instanceof BooleanValue && o2 instanceof BooleanValue) {
            boolean b1 = ((BooleanValue) o1).get();
            boolean b2 = ((BooleanValue) o2).get();
            return Boolean.compare(b1, b2);
        } else {
            throw new IllegalArgumentException("Cannot compare o1 and o2 since one or both is not a BooleanValue");
        }
    }

}
