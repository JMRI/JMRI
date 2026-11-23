package jmri.jmrix.marklin;

import java.util.Objects;
import jmri.jmrix.marklin.mcan.McanUid;
import jmri.jmrix.marklin.mcan.cdb.CdBDeviceClass;
import jmri.jmrix.marklin.mcan.cs2.NativeDeviceInfo;

/**
 * Immutable identity representation for Märklin CAN (MCAN) devices.
 * <p>
 * Supports both Native MCAN devices (identified by a 32-bit UID)
 * and CAN-digital-bahn (CdB) modules (identified by CAN class and module ID).
 * <p>
 * Designed with zero JMRI runtime dependencies for standalone reusability.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public abstract class McanDeviceIdentity {

    protected McanDeviceIdentity() {
    }

    /**
     * Get the unique system ID fragment used for JMRI system naming.
     * <p>
     * Examples:
     * <ul>
     * <li>Native: <code>uid-00112233</code></li>
     * <li>CdB: <code>cdb-22-03</code></li>
     * </ul>
     *
     * @return system ID fragment
     */
    public abstract String getSystemIdFragment();

    /**
     * Get the full system name using the provided system prefix.
     *
     * @param systemPrefix system prefix (e.g., "M")
     * @return system name like <code>M:dev:uid-00112233</code>
     */
    public String getSystemName(String systemPrefix) {
        String prefix = (systemPrefix != null && !systemPrefix.isEmpty()) ? systemPrefix : "M";
        return prefix + ":dev:" + getSystemIdFragment();
    }

    /**
     * Get a human-readable display name for this device identity.
     *
     * @return display name
     */
    public abstract String getDisplayName();

    /**
     * Check if this identity represents a native MCAN device.
     *
     * @return true if native MCAN device
     */
    public abstract boolean isNative();

    /**
     * Check if this identity represents a CdB module.
     *
     * @return true if CdB module
     */
    public abstract boolean isCdB();

    /**
     * Create identity for a Native MCAN device.
     *
     * @param uid device UID
     * @param deviceType 16-bit device type code
     * @return Native device identity
     */
    public static Native forNative(McanUid uid, int deviceType) {
        return new Native(uid, deviceType);
    }

    /**
     * Create identity for a CdB (CAN-digital-bahn) module.
     *
     * @param canClass CdB CAN class ID
     * @param moduleId CdB module ID
     * @return CdB device identity
     */
    public static CdB forCdB(int canClass, int moduleId) {
        return new CdB(canClass, moduleId);
    }

    /**
     * Identity representation for native MCAN devices.
     */
    public static final class Native extends McanDeviceIdentity {
        private final McanUid uid;
        private final int deviceType;

        public Native(McanUid uid, int deviceType) {
            this.uid = Objects.requireNonNull(uid, "uid");
            this.deviceType = deviceType & 0xFFFF;
        }

        public McanUid getUid() {
            return uid;
        }

        public int getDeviceType() {
            return deviceType;
        }

        @Override
        public String getSystemIdFragment() {
            return String.format("uid-%08x", uid.toLong());
        }

        @Override
        public String getDisplayName() {
            String typeName = NativeDeviceInfo.deviceTypeName(deviceType);
            return String.format("%s (UID=%s)", typeName, uid.toHexString());
        }

        @Override
        public boolean isNative() {
            return true;
        }

        @Override
        public boolean isCdB() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Native aNative = (Native) o;
            return uid.equals(aNative.uid) && deviceType == aNative.deviceType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(uid, deviceType);
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
    }

    /**
     * Identity representation for CAN-digital-bahn (CdB) modules.
     */
    public static final class CdB extends McanDeviceIdentity {
        private final int canClass;
        private final int moduleId;

        public CdB(int canClass, int moduleId) {
            this.canClass = canClass & 0xFF;
            this.moduleId = moduleId & 0xFF;
        }

        public int getCanClass() {
            return canClass;
        }

        public int getModuleId() {
            return moduleId;
        }

        @Override
        public String getSystemIdFragment() {
            return String.format("cdb-%02x-%02x", canClass, moduleId);
        }

        @Override
        public String getDisplayName() {
            CdBDeviceClass cls = CdBDeviceClass.forClassId(canClass).orElse(null);
            if (cls != null) {
                return "CdB " + cls.getDisplayName() + ", Module " + moduleId;
            }
            return String.format("CdB Module (Class 0x%02X, Module %d)", canClass, moduleId);
        }

        @Override
        public boolean isNative() {
            return false;
        }

        @Override
        public boolean isCdB() {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CdB cdB = (CdB) o;
            return canClass == cdB.canClass && moduleId == cdB.moduleId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(canClass, moduleId);
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
    }
}
