package jmri.jmrix.marklin.mcan.device;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable model of a single CdB device on the MCAN bus.
 * <p>
 * Two identity fragments are kept separate because they come from different
 * protocol exchanges:
 * <ul>
 *   <li>{@link #hwFingerprint()}  -  bytes 9–12 of a DLC=3 commissioned-module ping
 *       reply.  Present only after the device has been commissioned.</li>
 *   <li>{@link #factoryChallengeBytes()}  -  bytes 6–7 of a DLC=6 factory-fresh ping
 *       reply (module ID = 0).  Present only during factory-reset discovery.</li>
 * </ul>
 * Both fields are optional and must not be conflated.
 * <p>
 * Device lifecycle/mode is not stored here; it is observable from classified frames
 * via {@link jmri.jmrix.marklin.mcan.cdb.CdBFrameParser.PingReplyFrame#isFactoryFresh()} and tracked
 * in the JMRI layer.
 */
public final class CdBDevice {

    private final DeviceIdentity.CdBDeviceIdentity identity;
    private final CdBDeviceClass                   deviceClass;

    /**
     * Hardware fingerprint from a commissioned-module ping reply (bytes 9–12, DLC=3).
     * Null when not yet known.
     */
    private final int[] hwFingerprint;          // length 4 or null

    /**
     * Challenge bytes from a factory-fresh ping reply (bytes 6–7, DLC=6, module ID=0).
     * Null when not in factory-reset state.
     */
    private final int[] factoryChallengeBytes;  // length 2 or null

    public CdBDevice(DeviceIdentity.CdBDeviceIdentity identity,
                     CdBDeviceClass deviceClass,
                     int[] hwFingerprint,
                     int[] factoryChallengeBytes) {
        this.identity              = Objects.requireNonNull(identity, "identity");
        this.deviceClass           = Objects.requireNonNull(deviceClass, "deviceClass");
        this.hwFingerprint         = hwFingerprint         != null ? hwFingerprint.clone()         : null;
        this.factoryChallengeBytes = factoryChallengeBytes != null ? factoryChallengeBytes.clone() : null;
    }

    public DeviceIdentity.CdBDeviceIdentity identity()    { return identity; }
    public CdBDeviceClass                   deviceClass() { return deviceClass; }

    /**
     * Hardware fingerprint (bytes 9–12, DLC=3 ping reply) as a defensive copy.
     * Empty when the device has not yet sent a commissioned-module ping.
     */
    public Optional<int[]> hwFingerprint() {
        return Optional.ofNullable(hwFingerprint).map(int[]::clone);
    }

    /**
     * Challenge bytes (bytes 6–7, DLC=6 factory-fresh ping reply) as a defensive copy.
     * Empty unless the device was discovered in factory-reset state.
     */
    public Optional<int[]> factoryChallengeBytes() {
        return Optional.ofNullable(factoryChallengeBytes).map(int[]::clone);
    }

    /** Returns a new {@code CdBDevice} with the hardware fingerprint set. */
    public CdBDevice withHwFingerprint(int[] fingerprint) {
        Objects.requireNonNull(fingerprint, "fingerprint");
        if (fingerprint.length != 4) {
            throw new IllegalArgumentException("hwFingerprint must be 4 bytes, got " + fingerprint.length);
        }
        return new CdBDevice(identity, deviceClass, fingerprint, factoryChallengeBytes);
    }

    /** Returns a new {@code CdBDevice} with the factory challenge bytes set. */
    public CdBDevice withFactoryChallengeBytes(int[] challengeBytes) {
        Objects.requireNonNull(challengeBytes, "challengeBytes");
        if (challengeBytes.length != 2) {
            throw new IllegalArgumentException("factoryChallengeBytes must be 2 bytes, got " + challengeBytes.length);
        }
        return new CdBDevice(identity, deviceClass, hwFingerprint, challengeBytes);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CdBDevice)) return false;
        CdBDevice that = (CdBDevice) o;
        return identity.equals(that.identity)
            && deviceClass == that.deviceClass
            && Arrays.equals(hwFingerprint,        that.hwFingerprint)
            && Arrays.equals(factoryChallengeBytes, that.factoryChallengeBytes);
    }

    @Override public int hashCode() {
        int result = Objects.hash(identity, deviceClass);
        result = 31 * result + Arrays.hashCode(hwFingerprint);
        result = 31 * result + Arrays.hashCode(factoryChallengeBytes);
        return result;
    }

    @Override public String toString() {
        return "CdBDevice{" + identity.idFragment() + ", " + deviceClass + "}";
    }
}
