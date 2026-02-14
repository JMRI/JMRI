package jmri.jmrix.marklin.mcan.address;

/**
 * Enumeration of the address spaces carried in the Märklin CS2 and CdB MCAN protocols.
 * <p>
 * Each entry documents the 29-bit CAN ID range used for that address type so that
 * {@link AddressTranslator} can convert between user-facing accessory numbers and
 * the offsets encoded in CAN IDs.
 */
public enum AddressSpace {

    /**
     * Motorola (MM1/MM2) locomotive addresses.
     * CS2 CAN range offset: 0x0000 – 0x00FF (256 addresses)
     */
    MM_LOCO,

    /**
     * Selectrix 1 (SX1) locomotive addresses.
     * CS2 CAN range offset: 0x0800 – 0x0BFF (1024 addresses)
     */
    SX1_LOCO,

    /**
     * MM function decoder addresses (m83/m84 in function-decoder mode).
     * CS2 CAN range offset: 0x1000 – 0x13FF (1024 entries)
     */
    MM_FUNCTION_DECODER,

    /**
     * Club-reserved address range.
     * CS2 CAN range offset: 0x1800 – 0x1BFF
     */
    CLUB_RANGE,

    /**
     * Vendor-reserved address range.
     * CS2 CAN range offset: 0x1C00 – 0x1FFF
     */
    VENDOR_RANGE,

    /**
     * Motorola (MM1/MM2) locomotive addresses at 20 kHz carrier frequency.
     * CS2 CAN range offset: 0x2000 – 0x23FF (1024 addresses)
     */
    MM_LOCO_20KHZ,

    /**
     * Selectrix 1 (SX1) accessory addresses.
     * CS2 CAN range offset: 0x2800 – 0x2BFF (1024 entries)
     */
    SX1_ACCESSORY,

    /**
     * Motorola (MM1/MM2) accessory decoder addresses.
     * CS2 CAN Local-ID range: 0x3000 – 0x33FF (1024 entries).
     */
    MM_ACCESSORY,

    /**
     * Märklin m84/m83 accessory addresses (Motorola protocol variant).
     * Shares base 0x3000 with MM_ACCESSORY; kept separate for display purposes.
     */
    MARKLIN_ACCESSORY,

    /**
     * DCC accessory decoder addresses (turnouts, signals).
     * CS2 CAN Local-ID range: 0x3800 – 0x3FFF (2048 entries).
     */
    DCC_ACCESSORY,

    /**
     * MFX locomotive addresses.
     * CS2 CAN range offset: 0x4000 – 0x7FFF
     */
    MFX_LOCO,

    /**
     * Selectrix 2 (SX2) locomotive addresses.
     * CS2 CAN range offset: 0x8000 – 0xBFFF
     */
    SX2_LOCO,

    /**
     * DCC locomotive addresses (14/28/128 speed step).
     * CS2 CAN range offset: 0xC000 – 0xFFFF
     */
    DCC_LOCO
}
