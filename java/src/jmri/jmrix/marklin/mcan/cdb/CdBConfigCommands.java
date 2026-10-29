package jmri.jmrix.marklin.mcan.cdb;

/**
 * Builders for CdB configuration-bus protocol primitives (NEW-protocol).
 * <p>
 * Covers session management (select/deselect/factory), raw register read/write,
 * module ID commissioning, module name writing and reply frame construction.
 * Device-family-specific register operations are in the device command classes.
 *
 * @see CdBWeichenChefCommands
 * @see CdBReporterCommands
 * @see CdBLampenChefCommands
 */
public final class CdBConfigCommands {

    private CdBConfigCommands() {}

    // =================================================================
    // Discovery / session management
    // =================================================================

    /**
     * Builds a NEW-protocol broadcast discovery ping (DLC=0).
     * Addresses all modules of the given class simultaneously.
     *
     * @param classId 8-bit CdB device class
     * @return 13-byte CAN frame
     */
    public static int[] buildPing(int classId) {
        int[] frame = new int[13];
        CdBFrameBuilder.encodeCanId(frame, CdBFrameBuilder.pingCanId(classId, 0));
        frame[4] = 0;
        return frame;
    }

    /**
     * Builds a module select frame (DLC=4).
     *
     * @param classId  8-bit CdB device class
     * @param moduleId module slot (1–127)
     * @return 13-byte CAN frame
     */
    public static int[] buildSelect(int classId, int moduleId) {
        return CdBFrameBuilder.buildWriteRaw(classId, moduleId, 4,
                new int[]{0x00, CdBFrameBuilder.REG_SELECT, 0x00, 0x01});
    }

    /**
     * Builds a factory-fresh module select frame (DLC=6), echoing the challenge bytes
     * from the device's factory ping reply.
     *
     * @param classId    8-bit CdB device class
     * @param challenge0 first challenge byte
     * @param challenge1 second challenge byte
     * @return 13-byte CAN frame
     */
    public static int[] buildFactorySelect(int classId, int challenge0, int challenge1) {
        return CdBFrameBuilder.buildWriteRaw(classId, 0, 6,
                new int[]{0x00, CdBFrameBuilder.REG_SELECT, 0x00, 0x01,
                          challenge0 & 0xFF, challenge1 & 0xFF});
    }

    /**
     * Builds a module deselect frame (DLC=2).
     *
     * @param classId  8-bit CdB device class
     * @param moduleId module slot (1–127)
     * @return 13-byte CAN frame
     */
    public static int[] buildDeselect(int classId, int moduleId) {
        return CdBFrameBuilder.buildWriteRaw(classId, moduleId, 2,
                new int[]{0x00, CdBFrameBuilder.REG_DESELECT});
    }

    /**
     * Builds a factory-reset frame (DLC=2). Module must already be selected.
     *
     * @param classId  8-bit CdB device class
     * @param moduleId module slot (1–127)
     * @return 13-byte CAN frame
     */
    public static int[] buildFactoryReset(int classId, int moduleId) {
        return CdBFrameBuilder.buildWriteRaw(classId, moduleId, 2,
                new int[]{0x00, CdBFrameBuilder.REG_FACTORY_RESET});
    }

    /**
     * Builds a write-new-module-ID commissioning frame (DLC=6).
     *
     * @param classId    8-bit CdB device class
     * @param newId      new module ID to assign (1–127)
     * @param challenge0 first challenge byte from the factory ping reply
     * @param challenge1 second challenge byte from the factory ping reply
     * @return 13-byte CAN frame
     */
    public static int[] buildWriteNewModuleId(int classId, int newId, int challenge0, int challenge1) {
        return CdBFrameBuilder.buildWriteRaw(classId, 0, 6, new int[]{
                0x00, 0x02, (newId >> 8) & 0xFF, newId & 0xFF,
                challenge0 & 0xFF, challenge1 & 0xFF});
    }

    // =================================================================
    // Raw register access
    // =================================================================

    /**
     * Builds a register read request frame (DLC=2).
     *
     * @param classId  8-bit CdB device class
     * @param moduleId module slot (1–127)
     * @param slot     sub-register slot (0 = global)
     * @param reg      register number within slot
     * @return 13-byte CAN frame
     */
    public static int[] buildRead(int classId, int moduleId, int slot, int reg) {
        int[] frame = new int[13];
        CdBFrameBuilder.encodeCanId(frame,
                CdBFrameBuilder.pingCanId(classId, moduleId) + CdBFrameBuilder.DELTA_READ);
        frame[4] = 2;
        frame[5] = slot & 0xFF;
        frame[6] = reg  & 0xFF;
        return frame;
    }

    /**
     * Builds a register write command frame (DLC=4).
     *
     * @param classId  8-bit CdB device class
     * @param moduleId module slot (1–127)
     * @param slot     sub-register slot
     * @param reg      register number within slot
     * @param value    16-bit value to write
     * @return 13-byte CAN frame
     */
    public static int[] buildWrite(int classId, int moduleId, int slot, int reg, int value) {
        int[] frame = new int[13];
        CdBFrameBuilder.encodeCanId(frame,
                CdBFrameBuilder.pingCanId(classId, moduleId) + CdBFrameBuilder.DELTA_WRITE);
        frame[4] = 4;
        frame[5] = slot         & 0xFF;
        frame[6] = reg          & 0xFF;
        frame[7] = (value >> 8) & 0xFF;
        frame[8] = value        & 0xFF;
        return frame;
    }

    /**
     * Builds a register write command frame (DLC=4) with little-endian value encoding.
     * Some WeichenChef registers (reg=4 address, reg=5 RMK) use [7]=low, [8]=high.
     *
     * @param classId  8-bit CdB device class
     * @param moduleId module slot (1–127)
     * @param slot     sub-register slot
     * @param reg      register number within slot
     * @param value    16-bit value to write
     * @return 13-byte CAN frame
     */
    public static int[] buildWriteLE(int classId, int moduleId, int slot, int reg, int value) {
        int[] frame = new int[13];
        CdBFrameBuilder.encodeCanId(frame,
                CdBFrameBuilder.pingCanId(classId, moduleId) + CdBFrameBuilder.DELTA_WRITE);
        frame[4] = 4;
        frame[5] = slot        & 0xFF;
        frame[6] = reg         & 0xFF;
        frame[7] = value       & 0xFF;
        frame[8] = (value >> 8) & 0xFF;
        return frame;
    }

    // =================================================================
    // Module name
    // =================================================================

    /**
     * Writes one 6-character packet of the module name (reg=250, DLC=8).
     *
     * @param classId     8-bit CdB device class
     * @param moduleId    module slot
     * @param packetIndex packet index (0-based, 6 chars per packet)
     * @param chars       up to 6 ASCII byte values (zero-padded if shorter)
     * @return 13-byte CAN frame
     */
    public static int[] buildWriteModulnamePacket(int classId, int moduleId,
                                                   int packetIndex, int[] chars) {
        int[] data = new int[8];
        data[0] = packetIndex & 0xFF;
        data[1] = 250;
        for (int i = 0; i < 6; i++) {
            data[2 + i] = (i < chars.length) ? (chars[i] & 0xFF) : 0;
        }
        return CdBFrameBuilder.buildWriteRaw(classId, moduleId, 8, data);
    }

    // =================================================================
    // Reply frame builders (for testing / simulation)
    // =================================================================

    /**
     * Builds an operational ping reply frame (DLC=3).
     *
     * @param classId  8-bit CdB device class
     * @param moduleId assigned module slot (1–127)
     * @param b0       data byte 0
     * @param b1       data byte 1
     * @param b2       data byte 2 (hardware fingerprint byte)
     * @return 13-byte CAN frame
     */
    public static int[] buildPingReply(int classId, int moduleId, int b0, int b1, int b2) {
        int[] frame = new int[13];
        CdBFrameBuilder.encodeCanId(frame,
                CdBFrameBuilder.pingCanId(classId, moduleId) + CdBFrameBuilder.DELTA_REPLY);
        frame[4] = 3;
        frame[5] = b0 & 0xFF;
        frame[6] = b1 & 0xFF;
        frame[7] = b2 & 0xFF;
        return frame;
    }

    /**
     * Builds a factory-fresh ping reply frame (DLC=6, moduleId=0).
     *
     * @param classId    8-bit CdB device class
     * @param challenge0 first challenge byte
     * @param challenge1 second challenge byte
     * @return 13-byte CAN frame
     */
    public static int[] buildFactoryPingReply(int classId, int challenge0, int challenge1) {
        int[] frame = new int[13];
        CdBFrameBuilder.encodeCanId(frame,
                CdBFrameBuilder.pingCanId(classId, 0) + CdBFrameBuilder.DELTA_REPLY);
        frame[4] = 6;
        frame[5] = 0x00;
        frame[6] = challenge0 & 0xFF;
        frame[7] = challenge1 & 0xFF;
        return frame;
    }

    /**
     * Builds a read reply frame (DLC=4).
     *
     * @param classId  8-bit CdB device class
     * @param moduleId module slot (1–127)
     * @param slot     sub-register slot echoed from the request
     * @param reg      register number echoed from the request
     * @param value    16-bit register value
     * @return 13-byte CAN frame
     */
    public static int[] buildReadReply(int classId, int moduleId, int slot, int reg, int value) {
        int[] frame = new int[13];
        CdBFrameBuilder.encodeCanId(frame,
                CdBFrameBuilder.pingCanId(classId, moduleId) + CdBFrameBuilder.DELTA_READ_REP);
        frame[4] = 4;
        frame[5] = slot         & 0xFF;
        frame[6] = reg          & 0xFF;
        frame[7] = (value >> 8) & 0xFF;
        frame[8] = value        & 0xFF;
        return frame;
    }

    // =================================================================
    // CAN ID helper (public for callers that need to compute IDs directly)
    // =================================================================

    /**
     * Computes the 29-bit ping (base) CAN ID for a given class and module slot.
     *
     * @param classId  8-bit CdB device class
     * @param moduleId module slot (0 = broadcast, 1–127 = addressed)
     * @return 29-bit CAN ID
     */
    public static int pingCanId(int classId, int moduleId) {
        return CdBFrameBuilder.pingCanId(classId, moduleId);
    }
}
