package jmri.jmrix.marklin.mcan.device;

import java.util.Optional;

/**
 * Known CdB hardware device classes on the Märklin/CdB MCAN bus.
 * <p>
 * Each entry carries the 8-bit class ID used in CAN ID construction, a human-readable
 * display name and the protocol generation that governs discovery frame format.
 * German product names are intentional  -  they are the vendor's own identifiers.
 * <p>
 * CAN ID formula for NEW-protocol frames:
 * <pre>
 *   pingId = ((classId &gt;&gt; 4) &lt;&lt; 21) | ((classId &amp; 0xF) &lt;&lt; 12) | 0x0A80
 * </pre>
 */
public enum CdBDeviceClass {

    GLEIS_REPORTER_JUMBO  (0x03, "GleisReporter Jumbo",           ProtocolGeneration.NEW, HardwareCapability.SINGLE_GEN),
    STROM_SNIFFER         (0x0B, "StromSniffer",                  ProtocolGeneration.NEW, HardwareCapability.SINGLE_GEN),
    GLEIS_REPORTER        (0x0D, "GleisReporter",                 ProtocolGeneration.NEW, HardwareCapability.SINGLE_GEN),
    REPORTER_CHEF_002     (0x21, "ReporterChef 002",              ProtocolGeneration.NEW, HardwareCapability.SINGLE_GEN),
    WEICHEN_CHEF_MAGNET   (0x22, "WeichenChef Magnet M / deLuxe", ProtocolGeneration.NEW, HardwareCapability.AMBIGUOUS),
    WEICHEN_CHEF_AC       (0x23, "WeichenChef AC",                ProtocolGeneration.NEW, HardwareCapability.SINGLE_GEN),
    WEICHEN_CHEF_BASIC    (0x26, "WeichenChef Basic",             ProtocolGeneration.NEW, HardwareCapability.SINGLE_GEN),
    WEICHEN_CHEF_KOMBI    (0x27, "WeichenChef Kombi",             ProtocolGeneration.NEW, HardwareCapability.SINGLE_GEN),
    WEICHEN_CHEF_MOTOR    (0x2A, "WeichenChef Motor",             ProtocolGeneration.NEW, HardwareCapability.SINGLE_GEN),
    LAMPEN_CHEF           (0x4A, "LampenChef",                    ProtocolGeneration.OLD, HardwareCapability.SINGLE_GEN);

    // -----------------------------------------------------------------
    // Protocol generation
    // -----------------------------------------------------------------

    public enum ProtocolGeneration {
        /** NEW-protocol: single class-level broadcast ping, module ID in hash. */
        NEW,
        /** OLD-protocol: 128 individually addressed pings per class. */
        OLD
    }

    // -----------------------------------------------------------------
    // Hardware capability
    // -----------------------------------------------------------------

    /**
     * Describes how many distinguishable hardware generations exist for a device class,
     * as observed on the wire at discovery time.
     * <p>
     * The CdB bus provides no generation field in the ping reply  -  devices of the same
     * class look identical regardless of their hardware revision. When a class is
     * {@link #SINGLE_GEN} the caller can proceed immediately after discovery.
     * When a class is {@link #AMBIGUOUS} the caller must issue a post-discovery
     * probe (e.g. read firmware version register) before selecting the correct command set.
     */
    public enum HardwareCapability {
        /**
         * Exactly one hardware generation exists for this device class.
         * The device's capabilities are fully determined by its class ID alone;
         * no post-discovery probe is required.
         */
        SINGLE_GEN,

        /**
         * Two or more hardware generations share this class ID and produce identical
         * ping replies, so they cannot be distinguished at discovery time.
         * <p>
         * The marklin layer must establish capabilities before selecting a
         * command profile. Firmware alone is not a proven discriminator.
         * <p>
         * Currently only {@link CdBDeviceClass#WEICHEN_CHEF_MAGNET} (0x22) is
         * {@code AMBIGUOUS}: Both profiles have four turnouts. Each turnout can have a single address for both directions or separate addresses for each direction. In separate addresses mode, old generation hardware shares the primary timing slots with both addresses. The newer hardware can use independent timing slots for each individual address.
         */
        AMBIGUOUS
    }

    // -----------------------------------------------------------------
    // Instance data
    // -----------------------------------------------------------------

    private final int classId;
    private final String displayName;
    private final ProtocolGeneration protocolGeneration;
    private final HardwareCapability hardwareCapability;

    CdBDeviceClass(int classId, String displayName, ProtocolGeneration protocolGeneration,
                   HardwareCapability hardwareCapability) {
        this.classId             = classId;
        this.displayName         = displayName;
        this.protocolGeneration  = protocolGeneration;
        this.hardwareCapability  = hardwareCapability;
    }

    /** The 8-bit class ID used in CAN ID construction. */
    public int getClassId() { return classId; }

    /** Human-readable vendor product name. */
    public String getDisplayName() { return displayName; }

    public ProtocolGeneration getProtocolGeneration() { return protocolGeneration; }

    /** Whether this class has a single known hardware generation or multiple ambiguous ones. */
    public HardwareCapability getHardwareCapability() { return hardwareCapability; }

    /**
     * The 29-bit base (ping) CAN ID for this device class with broadcast module slot.
     * Formula: {@code ((classId >> 4) << 21) | ((classId & 0xF) << 12) | 0x0A80 | 0x80}
     * (0x80 encodes moduleId=0 in the hash low byte).
     */
    public int getPingId() {
        return ((classId >> 4) << 21) | ((classId & 0xF) << 12) | 0x0A80 | 0x80;
    }

    /**
     * Looks up a {@code CdBDeviceClass} by its 8-bit class ID.
     *
     * @param classId raw class ID from the CAN frame
     * @return matching enum constant or {@link Optional#empty()} for unknown IDs
     */
    public static Optional<CdBDeviceClass> forClassId(int classId) {
        for (CdBDeviceClass dc : values()) {
            if (dc.classId == classId) {
                return Optional.of(dc);
            }
        }
        return Optional.empty();
    }
}
