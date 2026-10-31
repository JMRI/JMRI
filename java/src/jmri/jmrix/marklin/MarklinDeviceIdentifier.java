package jmri.jmrix.marklin;

/**
 * Utility class for identifying Märklin CAN devices from protocol messages.
 * <p>
 * Supports passive device identification from:
 * <ul>
 * <li>PING responses (command 0x18) containing full device information</li>
 * <li>Regular CAN traffic via hash bytes that contain device UID fingerprint</li>
 * </ul>
 * <p>
 * Device UID Structure (32-bit):
 * <ul>
 * <li>Contains device type code and unique serial number</li>
 * <li>0x00000000 = Broadcast address</li>
 * <li>0xFFFFFFFF = Uninitialized/invalid UID</li>
 * </ul>
 * <p>
 * Hash Calculation: Hash = (UID[31:16]) XOR (UID[15:0])
 * <p>
 * The 16-bit hash serves for collision avoidance - each device monitors
 * for its hash appearing from another sender and must select a new one
 * if collision is detected.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de) Copyright (C) 2026
 * @see <a href="https://streaming.maerklin.de/public-media/cs2/cs2CAN-Protokoll-2_0.pdf">CS2 CAN Protocol 2.0</a>
 */
public class MarklinDeviceIdentifier {

    /**
     * Device information extracted from PING responses or traffic.
     */
    public static class DeviceInfo {
        private final long uid;
        private final int deviceType;
        private final int softwareVersion;
        private final int hash;

        public DeviceInfo(long uid, int deviceType, int softwareVersion, int hash) {
            this.uid = uid;
            this.deviceType = deviceType;
            this.softwareVersion = softwareVersion;
            this.hash = hash;
        }

        /**
         * Get the 32-bit device UID.
         * @return device UID or 0xFFFFFFFF if uninitialized
         */
        public long getUid() {
            return uid;
        }

        /**
         * Get the 16-bit device type code.
         * @return device type code (e.g., 0x0010 for Gleisbox)
         */
        public int getDeviceType() {
            return deviceType;
        }

        /**
         * Get the software version from PING response.
         * @return software version or -1 if not from PING
         */
        public int getSoftwareVersion() {
            return softwareVersion;
        }

        /**
         * Get the 16-bit hash value.
         * @return hash value
         */
        public int getHash() {
            return hash;
        }

        /**
         * Get human-readable device type name.
         * @return device type name
         */
        public String getDeviceTypeName() {
            return MarklinDeviceIdentifier.getDeviceTypeName(deviceType);
        }

        /**
         * Check if this is a broadcast UID.
         * @return true if UID is 0x00000000
         */
        public boolean isBroadcast() {
            return uid == 0x00000000L;
        }

        /**
         * Check if this is an uninitialized UID.
         * @return true if UID is 0xFFFFFFFF
         */
        public boolean isUninitialized() {
            return uid == 0xFFFFFFFFL;
        }

        /**
         * Verify hash consistency with UID.
         * @return true if hash matches calculated value from UID
         */
        public boolean isHashValid() {
            return hash == calculateHash(uid);
        }

        @Override
        public String toString() {
            if (isBroadcast()) {
                return "Broadcast";
            }
            if (isUninitialized()) {
                return "Uninitialized";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(getDeviceTypeName());
            sb.append(String.format(" (UID=0x%08X", uid));
            if (softwareVersion >= 0) {
                sb.append(String.format(", SW=v%d.%d",
                    (softwareVersion >> 8) & 0xFF,
                    softwareVersion & 0xFF));
            }
            sb.append(String.format(", Hash=0x%04X", hash));
            if (!isHashValid()) {
                sb.append(" [HASH MISMATCH]");
            }
            sb.append(")");
            return sb.toString();
        }
    }

    // Device type constants from CS2 CAN Protocol 2.0
    public static final int DEVICE_TYPE_GFP = 0x0000;          // Gleis Format Processor 60213/60214
    public static final int DEVICE_TYPE_GLEISBOX = 0x0010;     // Gleisbox 60112/60113
    public static final int DEVICE_TYPE_CONNECT_6021 = 0x0020; // Connect 6021 (60128)
    public static final int DEVICE_TYPE_MS2 = 0x0030;          // MS 2 (60653)
    public static final int DEVICE_TYPE_CS3 = 0x0050;          // CS3 (estimated, not in v2.0 spec)
    public static final int DEVICE_TYPE_CC_SCHNITTE = 0x1300;  // CC-Schnitte CAN/USB interface
    public static final int DEVICE_TYPE_WIRELESS = 0xFFE0;     // Wireless Devices
    public static final int DEVICE_TYPE_CS2_GUI = 0xFFFF;      // CS2-GUI (Master)

    // Broadcast and uninitialized UIDs
    public static final long UID_BROADCAST = 0x00000000L;
    public static final long UID_UNINITIALIZED = 0xFFFFFFFFL;

    /**
     * Calculate hash from UID according to spec: Hash = (UID[31:16]) XOR (UID[15:0])
     *
     * @param uid 32-bit device UID
     * @return 16-bit hash value
     */
    public static int calculateHash(long uid) {
        int upper = (int) ((uid >> 16) & 0xFFFF);
        int lower = (int) (uid & 0xFFFF);
        return upper ^ lower;
    }

    /**
     * Extract device information from a PING response (command 0x18).
     * <p>
     * PING response format (DLC=8):
     * <ul>
     * <li>Bytes 0-3: Device UID (32-bit, Big-Endian)</li>
     * <li>Bytes 4-5: Software version (16-bit)</li>
     * <li>Bytes 6-7: Device type code (16-bit, Big-Endian)</li>
     * </ul>
     *
     * @param decoded decoded MCAN message
     * @return DeviceInfo if this is a PING response, null otherwise
     */
    public static DeviceInfo fromPingResponse(MarklinCanCodec.DecodedMessage decoded) {
        // Check if this is a PING response
        if (decoded.getCommand() != MarklinConstants.CMDPING || !decoded.isResponse()) {
            return null;
        }

        // PING response must have 8 data bytes
        if (decoded.getDataLength() != 8) {
            return null;
        }

        // Extract UID (bytes 0-3, Big-Endian)
        long uid = ((long) decoded.getDataByte(0) << 24)
                 | ((long) decoded.getDataByte(1) << 16)
                 | ((long) decoded.getDataByte(2) << 8)
                 | decoded.getDataByte(3);

        // Extract software version (bytes 4-5)
        int softwareVersion = (decoded.getDataByte(4) << 8) | decoded.getDataByte(5);

        // Extract device type (bytes 6-7, Big-Endian)
        int deviceType = (decoded.getDataByte(6) << 8) | decoded.getDataByte(7);

        // Extract hash from message
        int hash = (decoded.getHash()[0] << 8) | decoded.getHash()[1];

        return new DeviceInfo(uid, deviceType, softwareVersion, hash);
    }

    /**
     * Extract partial device information from hash bytes in any CAN message.
     * <p>
     * This provides a "fingerprint" of the sending device based on its hash,
     * but does not include device type or software version (only available
     * from PING responses).
     *
     * @param decoded decoded MCAN message
     * @return DeviceInfo with UID estimation from hash or null if broadcast
     */
    public static DeviceInfo fromHash(MarklinCanCodec.DecodedMessage decoded) {
        int hash = (decoded.getHash()[0] << 8) | decoded.getHash()[1];

        // Cannot determine UID from hash alone (hash is not reversible)
        // But we can provide the hash for tracking message sources
        // UID is set to UNINITIALIZED to indicate it's estimated
        return new DeviceInfo(UID_UNINITIALIZED, -1, -1, hash);
    }

    /**
     * Get human-readable device type name.
     *
     * @param deviceType 16-bit device type code
     * @return device type name
     */
    public static String getDeviceTypeName(int deviceType) {
        switch (deviceType) {
            case DEVICE_TYPE_GFP:
                return "GFP (Gleis Format Processor)";
            case DEVICE_TYPE_GLEISBOX:
                return "Gleisbox";
            case DEVICE_TYPE_CONNECT_6021:
                return "Connect 6021";
            case DEVICE_TYPE_MS2:
                return "MS2";
            case DEVICE_TYPE_CS3:
                return "CS3";
            case DEVICE_TYPE_CC_SCHNITTE:
                return "CC-Schnitte";
            case DEVICE_TYPE_WIRELESS:
                return "Wireless Device";
            case DEVICE_TYPE_CS2_GUI:
                return "CS2-GUI (Master)";
            default:
                // Check for undocumented MS2 variants
                if (deviceType >= 0x0030 && deviceType <= 0x0039) {
                    return String.format("MS2 (variant 0x%04X)", deviceType);
                }
                // Check for undocumented Gleisbox variants
                if (deviceType >= 0x0010 && deviceType <= 0x0019) {
                    return String.format("Gleisbox (variant 0x%04X)", deviceType);
                }
                return String.format("MCAN Device (unknown type 0x%04X)", deviceType);
        }
    }

    /**
     * Check if a message is a PING request (command 0x18, not response, DLC=0).
     *
     * @param decoded decoded MCAN message
     * @return true if this is a PING request
     */
    public static boolean isPingRequest(MarklinCanCodec.DecodedMessage decoded) {
        return decoded.getCommand() == MarklinConstants.CMDPING
            && !decoded.isResponse()
            && decoded.getDataLength() == 0;
    }

    /**
     * Check if a message is a PING response.
     *
     * @param decoded decoded MCAN message
     * @return true if this is a PING response
     */
    public static boolean isPingResponse(MarklinCanCodec.DecodedMessage decoded) {
        return decoded.getCommand() == MarklinConstants.CMDPING
            && decoded.isResponse()
            && decoded.getDataLength() == 8;
    }

    /**
     * Format device information as a compact string for logging.
     *
     * @param info device information
     * @return formatted string like "[MS2:0x12345678]" or "[Hash:0xABCD]"
     */
    public static String formatCompact(DeviceInfo info) {
        if (info == null) {
            return "[Unknown]";
        }
        if (info.isBroadcast()) {
            return "[Broadcast]";
        }
        if (info.isUninitialized()) {
            // Hash-only identification (not from PING)
            return String.format("[Hash:0x%04X]", info.getHash());
        }

        // Full device identification from PING
        String type = getDeviceTypeName(info.getDeviceType());
        // Simplify common names
        if (type.contains("(")) {
            type = type.substring(0, type.indexOf("(")).trim();
        }
        return String.format("[%s:0x%08X]", type, info.getUid());
    }
}
