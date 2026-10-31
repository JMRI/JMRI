package jmri.jmrix.marklin.mcan.address;

import java.util.List;
import java.util.Optional;

/**
 * Enumeration of the address spaces carried in the Märklin CS2 and CdB MCAN protocols.
 * <p>
 * Each constant carries its CAN range base offset, size and display label.
 * Callers can compute and reverse CAN addresses.
 */
public enum AddressSpace {

    /** Motorola (MM1/MM2) locomotive addresses. CAN range: 0x0000–0x00FF (256 addresses) */
    MM_LOCO             (0x0000, 0x0100, "MM Loco"),

    /** Selectrix 1 (SX1) locomotive addresses. CAN range: 0x0800–0x0BFF (1024 addresses) */
    SX1_LOCO            (0x0800, 0x0400, "SX1 Loco"),

    /** MM function decoder addresses (m83/m84). CAN range: 0x1000–0x13FF (1024 entries) */
    MM_FUNCTION_DECODER (0x1000, 0x0400, "MM Function Decoder"),

    /** Club-reserved address range. CAN range: 0x1800–0x1BFF */
    CLUB_RANGE          (0x1800, 0x0400, "Club Range"),

    /** Vendor-reserved address range. CAN range: 0x1C00–0x1FFF */
    VENDOR_RANGE        (0x1C00, 0x0400, "Vendor Range"),

    /** Motorola (MM1/MM2) locomotive addresses at 20 kHz. CAN range: 0x2000–0x23FF (1024 addresses) */
    MM_LOCO_20KHZ       (0x2000, 0x0400, "MM Loco (20kHz)"),

    /** Selectrix 1 (SX1) accessory addresses. CAN range: 0x2800–0x2BFF (1024 entries) */
    SX1_ACCESSORY       (0x2800, 0x0400, "SX1 Accessory"),

    /** Motorola (MM1/MM2) accessory decoder addresses. CAN range: 0x3000–0x33FF (1024 entries) */
    MM_ACCESSORY        (0x3000, 0x0400, "MM Accessory"),

    /**
     * Märklin m84/m83 accessory addresses (Motorola protocol variant).
     * Shares base 0x3000 with MM_ACCESSORY; kept separate for display purposes.
     */
    MARKLIN_ACCESSORY   (0x3000, 0x0400, "MM Accessory"),

    /** DCC accessory decoder addresses. CAN range: 0x3800–0x3FFF (2048 entries) */
    DCC_ACCESSORY       (0x3800, 0x0800, "DCC Accessory"),

    /** MFX locomotive addresses. CAN range: 0x4000–0x7FFF (16384 addresses) */
    MFX_LOCO            (0x4000, 0x4000, "MFX"),

    /** Selectrix 2 (SX2) locomotive addresses. CAN range: 0x8000–0xBFFF (16384 addresses) */
    SX2_LOCO            (0x8000, 0x4000, "SX2 Loco"),

    /** DCC locomotive addresses (14/28/128 speed step). CAN range: 0xC000–0xFFFF (16384 addresses) */
    DCC_LOCO            (0xC000, 0x4000, "DCC Loco"),

    /**
     * Raw CAN address. The full 16-bit Local-ID space (0x0000–0xFFFF).
     * <p>
     * Used as a last-resort answer when a Local-ID does not fall within any named
     * protocol space in a given context.
     *
     * In an accessory context a WeichenChef turnout decoder channel configured as "CAN N" sends Local-ID N verbatim.
     * If N does not land in MM_ACCESSORY or DCC_ACCESSORY it's meant to be CAN.
     * If a user configures CAN12290 he lands in MM territory.
     * A UI may accept that but any decoding from message would - correctly - assume it's a single-digit MM address.
     *
     * Note that not all accessories can send or process "CAN" addressed messages.
     * <p>
     * Never returned by {@link #forCanOffset(int)} but an available option in {@link #forCanOffset(int, List)} when CAN is included in the candidate list.
     */
    CAN                 (0x0000, 0x10000, "CAN");

    private final int base;
    private final int size;
    private final String label;

    AddressSpace(int base, int size, String label) {
        this.base  = base;
        this.size  = size;
        this.label = label;
    }

    /** CAN ID Local-ID base offset for this address space. */
    public int base() { return base; }

    /** Number of addresses in this space. */
    public int size() { return size; }

    /** Human-readable display label. */
    public String label() { return label; }

    /** Returns true if {@code canOffset} falls within this address space's range. */
    public boolean containsOffset(int canOffset) {
        return canOffset >= base && canOffset < base + size;
    }

    /**
     * Converts a 0-based address number to its CAN offset.
     *
     * @param number 0-based address within this space
     * @return CAN offset or -1 if out of range
     */
    public int toCanOffset(int number) {
        if (number < 0 || number >= size) return -1;
        return base + number;
    }

    /**
     * Converts a 1-based user address to a CAN offset for this address space.
     * <p>
     * User addresses are 1-based (address 1 = CAN offset {@code base}).
     * CAN offsets are 0-based from the space base.
     *
     * @param userAddress 1-based user address
     * @return CAN offset or empty if out of range
     */
    public Optional<Integer> fromUserAddress(int userAddress) {
        if (userAddress < 1 || userAddress > size) return Optional.empty();
        return Optional.of(base + (userAddress - 1));
    }

    /**
     * Converts a CAN offset back to a 1-based user address for this address space.
     *
     * @param canOffset raw CAN ID Local-ID value
     * @return 1-based user address or empty if outside this space's range
     */
    public Optional<Integer> toUserAddress(int canOffset) {
        int relative = canOffset - base;
        if (relative < 0 || relative >= size) return Optional.empty();
        return Optional.of(relative + 1);
    }

    /**
     * Returns the named protocol space that contains the given CAN offset, if any.
     * <p>
     * This is a protocol-level lookup: it identifies which CS2 address space a
     * Local-ID belongs to (MM loco, DCC accessory, etc.). It is context-independent
     * and does not distinguish between uses of the same Local-ID in different command
     * types (loco vs. accessory).
     * <p>
     * Two spaces are always skipped:
     * <ul>
     *   <li>{@link #MARKLIN_ACCESSORY} as it's an alias of {@link #MM_ACCESSORY}
     *       MM_ACCESSORY is returned for the shared range.</li>
     *   <li>{@link #CAN} as it covers the full 0x0000–0xFFFF span and would match every
     *       address. Use {@link #forCanOffset(int, List)} with CAN as the last entry in the candidate
     *       list to get it as a last-resort answer.</li>
     * </ul>
     *
     * @param canOffset raw CAN ID Local-ID value
     * @return matching named address space, or empty if none of the named spaces match
     */
    public static Optional<AddressSpace> forCanOffset(int canOffset) {
        for (AddressSpace space : values()) {
            if (space == MARKLIN_ACCESSORY) continue;
            // Is this even necessary if CAN is the last enum entry?
            if (space == CAN) continue;
            if (space.containsOffset(canOffset)) return Optional.of(space);
        }
        return Optional.empty();
    }

    /**
     * Returns the first space in {@code candidates} that contains the given CAN offset.
     * <p>
     * Spaces are tested in the order supplied. Including {@link #CAN} last makes it a
     * catch-all for any Local-ID not covered by the preceding candidates:
     * <pre>
     *   // accessory context: MM and DCC first, raw CAN as last resort
     *   AddressSpace.forCanOffset(addr,
     *       List.of(MM_ACCESSORY, DCC_ACCESSORY, CAN));
     * </pre>
     *
     * @param canOffset  raw CAN ID Local-ID value
     * @param candidates ordered list of spaces to consider
     * @return first matching space, or empty if none match
     */
    public static Optional<AddressSpace> forCanOffset(int canOffset, List<AddressSpace> candidates) {
        for (AddressSpace space : candidates) {
            if (space.containsOffset(canOffset)) return Optional.of(space);
        }
        return Optional.empty();
    }
}
