package jmri.jmrix.marklin.mcan.protocol;

import java.util.Objects;

/**
 * Immutable value object carrying the information extracted from a CS2 native ping reply.
 * <p>
 * Ping reply layout (DLC=8, command=0x18, response=true):
 * <pre>
 *   bytes 0–3: device UID (32-bit, big-endian)
 *   bytes 4–5: software version (16-bit)
 *   bytes 6–7: device type code (16-bit, big-endian)
 * </pre>
 * The hash in the CAN header is derived from the UID via:
 * {@code hash = (uid >> 16) ^ (uid & 0xFFFF)}, then CS2 fingerprint bits are OR'd in.
 */
public final class NativeDeviceInfo {

    /** Device UIDs with reserved meanings. */
    public static final long UID_BROADCAST    = 0x00000000L;
    public static final long UID_UNINITIALIZED = 0xFFFFFFFFL;

    // Known device type codes (CS2 CAN protocol 2.0)
    public static final int TYPE_GFP          = 0x0000; // A Track Format processor (either a booster or a CS2/3 station's builtin but not a TrackBox / GleisBox)
    public static final int TYPE_GLEISBOX     = 0x0010; // A Marklin or Trix Track Signal box
    public static final int TYPE_CONNECT_6021 = 0x0020; // Connect 6021 interface module
    public static final int TYPE_MS2          = 0x0030; // MobileStation 2
    public static final int TYPE_CS3          = 0x0050; // Central Station 3
    public static final int TYPE_CC_SCHNITTE  = 0x1300; // CC-Schnitte versions below 3.1 do not emit this
    public static final int TYPE_WIRELESS     = 0xFFE0; // MobileStation Wireless
    public static final int TYPE_CS2_GUI      = 0xFFFF; // CS2 GUI (Master)

    private final long uid;
    private final int  deviceType;
    private final int  softwareVersion;
    private final int  hash;

    public NativeDeviceInfo(long uid, int deviceType, int softwareVersion, int hash) {
        this.uid             = uid;
        this.deviceType      = deviceType;
        this.softwareVersion = softwareVersion;
        this.hash            = hash;
    }

    public long uid()             { return uid; }
    public int  deviceType()      { return deviceType; }
    public int  softwareVersion() { return softwareVersion; }
    public int  hash()            { return hash; }

    /** True when UID is the broadcast address (0x00000000). */
    public boolean isBroadcast()    { return uid == UID_BROADCAST; }

    /** True when UID is the uninitialized sentinel (0xFFFFFFFF). */
    public boolean isUninitialized() { return uid == UID_UNINITIALIZED; }

    /**
     * True when the hash in this reply matches the hash derivable from the UID.
     * A mismatch indicates either a hash collision or a non-standard device.
     */
    public boolean isHashValid() {
        int expected = calculateHash(uid);
        // The stored hash has the CS2 fingerprint bits set; mask them out before comparing
        // (bits 9:7 of the hash are always 0b110 in a valid CS2 frame)
        return (hash & ~0x0380) == (expected & ~0x0380);
    }

    /**
     * Computes the hash from a UID per the CS2 spec:
     * {@code hash = (uid >> 16) ^ (uid & 0xFFFF)}.
     * The caller must OR in the CS2 fingerprint bits (0x0300) before use on the bus.
     */
    public static int calculateHash(long uid) {
        return (int)((uid >> 16) & 0xFFFF) ^ (int)(uid & 0xFFFF);
    }

    /**
     * Returns a human-readable name for a device type code.
     * Unknown types return a formatted hex string.
     */
    public static String deviceTypeName(int deviceType) {
        switch (deviceType) {
            case TYPE_GFP:          return "GFP (Gleis Format Processor)";
            case TYPE_GLEISBOX:     return "Gleisbox";
            case TYPE_CONNECT_6021: return "Connect 6021";
            case TYPE_MS2:          return "MS2";
            case TYPE_CS3:          return "CS3";
            case TYPE_CC_SCHNITTE:  return "CC-Schnitte";
            case TYPE_WIRELESS:     return "Wireless Device";
            case TYPE_CS2_GUI:      return "CS2-GUI (Master)";
            default:
                if (deviceType >= 0x0030 && deviceType <= 0x0039) {
                    return String.format("MS2 (variant 0x%04X)", deviceType);
                }
                if (deviceType >= 0x0010 && deviceType <= 0x0019) {
                    return String.format("Gleisbox (variant 0x%04X)", deviceType);
                }
                return String.format("MCAN Device (unknown type 0x%04X)", deviceType);
        }
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NativeDeviceInfo)) return false;
        NativeDeviceInfo that = (NativeDeviceInfo) o;
        return uid == that.uid && deviceType == that.deviceType
            && softwareVersion == that.softwareVersion && hash == that.hash;
    }

    @Override public int hashCode() {
        return Objects.hash(uid, deviceType, softwareVersion, hash);
    }

    @Override public String toString() {
        return String.format("NativeDeviceInfo{uid=0x%08X, type=%s, sw=0x%04X, hash=0x%04X}",
                uid, deviceTypeName(deviceType), softwareVersion, hash);
    }
}
