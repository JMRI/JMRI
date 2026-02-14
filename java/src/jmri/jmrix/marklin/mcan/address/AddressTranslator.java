package jmri.jmrix.marklin.mcan.address;

import java.util.Optional;

/**
 * Translates between user-visible accessory/locomotive numbers and the
 * 29-bit CAN ID range offsets used in Märklin CS2 / CdB MCAN frames.
 * <p>
 * All translation is stateless and purely functional.
 * <p>
 * Range base offsets are derived from the CS2 CAN protocol documentation.
 * DCC accessory addresses are 1-based in user space and 0-based on the bus.
 */
public final class AddressTranslator {

    // CAN range base offsets (29-bit CAN ID Local-ID sub-range).
    // Source: CS2 CAN protocol documentation, section "Adressverteilungsschema".
    public static final int BASE_MM_LOCO            = 0x0000;
    public static final int BASE_SX1_LOCO           = 0x0800;
    public static final int BASE_MM_FUNCTION_DECODER= 0x1000;
    public static final int BASE_CLUB_RANGE         = 0x1800;
    public static final int BASE_VENDOR_RANGE       = 0x1C00;
    public static final int BASE_MM_LOCO_20KHZ      = 0x2000;
    public static final int BASE_SX1_ACCESSORY      = 0x2800;
    public static final int BASE_MM_ACCESSORY       = 0x3000;  // 0x3000–0x33FF
    public static final int BASE_MARKLIN_ACCESSORY  = 0x3000;  // same base as MM
    public static final int BASE_DCC_ACCESSORY      = 0x3800;  // 0x3800–0x3FFF
    public static final int BASE_MFX_LOCO           = 0x4000;
    public static final int BASE_SX2_LOCO           = 0x8000;
    public static final int BASE_DCC_LOCO           = 0xC000;

    // Number of addresses in each range per the spec.
    static final int MAX_MM_LOCO             = 0x0100;  // 256 addresses
    static final int MAX_SX1_LOCO            = 0x0400;  // 1024 addresses
    static final int MAX_MM_FUNCTION_DECODER = 0x0400;  // 1024 entries
    static final int MAX_CLUB_RANGE          = 0x0400;
    static final int MAX_VENDOR_RANGE        = 0x0400;
    static final int MAX_MM_LOCO_20KHZ       = 0x0400;  // 1024 addresses
    static final int MAX_SX1_ACCESSORY       = 0x0400;  // 1024 entries
    static final int MAX_MM_ACCESSORY        = 0x0400;  // 0x3000–0x33FF = 1024 entries
    static final int MAX_DCC_ACCESSORY       = 0x0800;  // 0x3800–0x3FFF = 2048 entries
    static final int MAX_MFX_LOCO            = 0x4000;  // 16384 addresses
    static final int MAX_SX2_LOCO            = 0x4000;  // 16384 addresses
    static final int MAX_DCC_LOCO            = 0x4000;  // 16384 addresses

    private AddressTranslator() {}

    /**
     * Converts a locomotive number and protocol to its CS2 CAN address.
     * <p>
     * The {@code locoNumber} is the raw address used by the decoder (0-based for MM/DCC,
     * consistent with how CS2 stores it).
     *
     * @param space     address space ({@link AddressSpace#MM_LOCO}, {@link AddressSpace#DCC_LOCO},
     *                  {@link AddressSpace#MFX_LOCO} or {@link AddressSpace#SX2_LOCO})
     * @param locoNumber raw decoder address (0-based)
     * @return CAN address (29-bit Local-ID) or -1 if out of range or space not a loco space
     */
    public static int locoToCanAddress(AddressSpace space, int locoNumber) {
        switch (space) {
            case MM_LOCO:
                if (locoNumber < 0 || locoNumber >= MAX_MM_LOCO) return -1;
                return BASE_MM_LOCO + locoNumber;
            case MFX_LOCO:
                if (locoNumber < 0 || locoNumber >= MAX_MFX_LOCO) return -1;
                return BASE_MFX_LOCO + locoNumber;
            case SX2_LOCO:
                if (locoNumber < 0 || locoNumber >= MAX_SX2_LOCO) return -1;
                return BASE_SX2_LOCO + locoNumber;
            case DCC_LOCO:
                if (locoNumber < 0 || locoNumber >= MAX_DCC_LOCO) return -1;
                return BASE_DCC_LOCO + locoNumber;
            default:
                return -1;
        }
    }

    /**
     * Converts a CS2 CAN address back to an {@link AddressSpace} and raw loco number.
     *
     * @param canAddress CS2 CAN loco address
     * @return optional pair of space + number, empty if not a recognised loco address
     */
    public static Optional<int[]> canAddressToLoco(int canAddress) {
        if (canAddress >= BASE_DCC_LOCO && canAddress < BASE_DCC_LOCO + MAX_DCC_LOCO) {
            return Optional.of(new int[]{AddressSpace.DCC_LOCO.ordinal(), canAddress - BASE_DCC_LOCO});
        }
        if (canAddress >= BASE_SX2_LOCO && canAddress < BASE_SX2_LOCO + MAX_SX2_LOCO) {
            return Optional.of(new int[]{AddressSpace.SX2_LOCO.ordinal(), canAddress - BASE_SX2_LOCO});
        }
        if (canAddress >= BASE_MFX_LOCO && canAddress < BASE_MFX_LOCO + MAX_MFX_LOCO) {
            return Optional.of(new int[]{AddressSpace.MFX_LOCO.ordinal(), canAddress - BASE_MFX_LOCO});
        }
        if (canAddress >= BASE_MM_LOCO && canAddress < BASE_MM_LOCO + MAX_MM_LOCO) {
            return Optional.of(new int[]{AddressSpace.MM_LOCO.ordinal(), canAddress - BASE_MM_LOCO});
        }
        return Optional.empty();
    }


    /**
     * Converts a user-facing DCC accessory address (1-based) to the CAN bus offset.
     *
     * @param userAddress 1-based DCC accessory address
     * @return CAN range offset or {@link Optional#empty()} if out of range
     */
    public static Optional<Integer> dccAccessoryToCanOffset(int userAddress) {
        if (userAddress < 1 || userAddress > MAX_DCC_ACCESSORY) {
            return Optional.empty();
        }
        return Optional.of(BASE_DCC_ACCESSORY + (userAddress - 1));
    }

    /**
     * Converts a CAN bus offset back to a user-facing DCC accessory address (1-based).
     *
     * @param canOffset raw CAN range offset
     * @return 1-based DCC accessory address or {@link Optional#empty()} if outside DCC range
     */
    public static Optional<Integer> canOffsetToDccAccessory(int canOffset) {
        int relative = canOffset - BASE_DCC_ACCESSORY;
        if (relative < 0 || relative >= MAX_DCC_ACCESSORY) {
            return Optional.empty();
        }
        return Optional.of(relative + 1);
    }

    /**
     * Converts a user-facing Motorola accessory address (1-based) to the CAN bus offset.
     *
     * @param userAddress 1-based Motorola accessory address
     * @return CAN range offset or {@link Optional#empty()} if out of range
     */
    public static Optional<Integer> mmAccessoryToCanOffset(int userAddress) {
        if (userAddress < 1 || userAddress > MAX_MM_ACCESSORY) {
            return Optional.empty();
        }
        return Optional.of(BASE_MM_ACCESSORY + (userAddress - 1));
    }

    /**
     * Converts a CAN bus offset back to a user-facing Motorola accessory address (1-based).
     *
     * @param canOffset raw CAN range offset
     * @return 1-based Motorola accessory address or {@link Optional#empty()} if outside MM range
     */
    public static Optional<Integer> canOffsetToMmAccessory(int canOffset) {
        int relative = canOffset - BASE_MM_ACCESSORY;
        if (relative < 0 || relative >= MAX_MM_ACCESSORY) {
            return Optional.empty();
        }
        return Optional.of(relative + 1);
    }

    /**
     * Infers the {@link AddressSpace} for a given CAN offset.
     * <p>
     * Covers all CS2 address ranges including loco (MM, SX1, MM 20kHz, MFX, SX2, DCC),
     * accessory (SX1, MM, DCC), function decoders, club and vendor ranges.
     * Note: MM and Märklin accessory ranges overlap at 0x3000; this method returns
     * {@link AddressSpace#MM_ACCESSORY} for the shared range.
     *
     * @param canOffset raw CAN range offset
     * @return detected address space or {@link Optional#empty()} if unrecognised
     */
    public static Optional<AddressSpace> detectAddressSpace(int canOffset) {
        // Checked in address order so ranges don't shadow each other.
        if (canOffset >= BASE_MM_LOCO && canOffset < BASE_MM_LOCO + MAX_MM_LOCO) {
            return Optional.of(AddressSpace.MM_LOCO);
        }
        if (canOffset >= BASE_SX1_LOCO && canOffset < BASE_SX1_LOCO + MAX_SX1_LOCO) {
            return Optional.of(AddressSpace.SX1_LOCO);
        }
        if (canOffset >= BASE_MM_FUNCTION_DECODER && canOffset < BASE_MM_FUNCTION_DECODER + MAX_MM_FUNCTION_DECODER) {
            return Optional.of(AddressSpace.MM_FUNCTION_DECODER);
        }
        if (canOffset >= BASE_CLUB_RANGE && canOffset < BASE_CLUB_RANGE + MAX_CLUB_RANGE) {
            return Optional.of(AddressSpace.CLUB_RANGE);
        }
        if (canOffset >= BASE_VENDOR_RANGE && canOffset < BASE_VENDOR_RANGE + MAX_VENDOR_RANGE) {
            return Optional.of(AddressSpace.VENDOR_RANGE);
        }
        if (canOffset >= BASE_MM_LOCO_20KHZ && canOffset < BASE_MM_LOCO_20KHZ + MAX_MM_LOCO_20KHZ) {
            return Optional.of(AddressSpace.MM_LOCO_20KHZ);
        }
        if (canOffset >= BASE_SX1_ACCESSORY && canOffset < BASE_SX1_ACCESSORY + MAX_SX1_ACCESSORY) {
            return Optional.of(AddressSpace.SX1_ACCESSORY);
        }
        if (canOffset >= BASE_MM_ACCESSORY && canOffset < BASE_MM_ACCESSORY + MAX_MM_ACCESSORY) {
            return Optional.of(AddressSpace.MM_ACCESSORY);
        }
        if (canOffset >= BASE_DCC_ACCESSORY && canOffset < BASE_DCC_ACCESSORY + MAX_DCC_ACCESSORY) {
            return Optional.of(AddressSpace.DCC_ACCESSORY);
        }
        if (canOffset >= BASE_MFX_LOCO && canOffset < BASE_MFX_LOCO + MAX_MFX_LOCO) {
            return Optional.of(AddressSpace.MFX_LOCO);
        }
        if (canOffset >= BASE_SX2_LOCO && canOffset < BASE_SX2_LOCO + MAX_SX2_LOCO) {
            return Optional.of(AddressSpace.SX2_LOCO);
        }
        if (canOffset >= BASE_DCC_LOCO && canOffset < BASE_DCC_LOCO + MAX_DCC_LOCO) {
            return Optional.of(AddressSpace.DCC_LOCO);
        }
        return Optional.empty();
    }
}
