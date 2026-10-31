package jmri.jmrix.marklin.mcan.device;

/**
 * Typed immutable device identity on the Märklin CAN bus.
 * <p>
 * Two concrete variants exist: {@link NativeDeviceIdentity} for CS2/MS2 native devices
 * (identified by a 32-bit UID) and {@link CdBDeviceIdentity} for CdB configured modules
 * (identified by class ID + module slot).
 * <p>
 * Note: {@code getSystemName(prefix)} is intentionally absent  -  that string format
 * belongs in the JMRI layer, not here.
 */
public abstract class DeviceIdentity {

    /** Short, stable fragment usable as a key  -  e.g. "uid-00112233" or "cdb-27-03". */
    public abstract String idFragment();

    /** Human-readable display name. */
    public abstract String displayName();

    public abstract boolean isNative();
    public abstract boolean isCdB();

    /** Factory: native CS2/MS2 device identified by 32-bit UID and device type. */
    public static NativeDeviceIdentity forNative(long uid, int deviceType) {
        return new NativeDeviceIdentity(uid, deviceType);
    }

    /** Factory: CdB configured module identified by class ID and module slot. */
    public static CdBDeviceIdentity forCdB(int canClass, int moduleId) {
        return new CdBDeviceIdentity(canClass, moduleId);
    }

    // -----------------------------------------------------------------
    // Concrete inner types
    // -----------------------------------------------------------------

    public static final class NativeDeviceIdentity extends DeviceIdentity {

        private final long uid;
        private final int  deviceType;

        NativeDeviceIdentity(long uid, int deviceType) {
            this.uid        = uid;
            this.deviceType = deviceType;
        }

        public long uid()        { return uid; }
        public int  deviceType() { return deviceType; }

        @Override public String  idFragment()   { return String.format("uid-%08x", uid); }
        @Override public String  displayName()  { return String.format("Native 0x%08X (type 0x%04X)", uid, deviceType); }
        @Override public boolean isNative()     { return true; }
        @Override public boolean isCdB()        { return false; }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NativeDeviceIdentity)) return false;
            NativeDeviceIdentity that = (NativeDeviceIdentity) o;
            return uid == that.uid && deviceType == that.deviceType;
        }

        @Override public int hashCode() {
            return Long.hashCode(uid) * 31 + deviceType;
        }
    }

    public static final class CdBDeviceIdentity extends DeviceIdentity {

        private final int canClass;
        private final int moduleId;

        CdBDeviceIdentity(int canClass, int moduleId) {
            this.canClass = canClass;
            this.moduleId = moduleId;
        }

        public int canClass()  { return canClass; }
        public int moduleId()  { return moduleId; }

        @Override public String  idFragment()  { return String.format("cdb-%02x-%02x", canClass, moduleId); }
        @Override public String  displayName() { return String.format("CdB 0x%02X module %d", canClass, moduleId); }
        @Override public boolean isNative()    { return false; }
        @Override public boolean isCdB()       { return true; }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CdBDeviceIdentity)) return false;
            CdBDeviceIdentity that = (CdBDeviceIdentity) o;
            return canClass == that.canClass && moduleId == that.moduleId;
        }

        @Override public int hashCode() {
            return canClass * 31 + moduleId;
        }
    }
}
