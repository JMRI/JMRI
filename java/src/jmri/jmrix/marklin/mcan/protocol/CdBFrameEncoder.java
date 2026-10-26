package jmri.jmrix.marklin.mcan.protocol;

import java.util.Optional;

/**
 * Constructs 13-byte CAN frames for NEW-protocol CdB configuration commands.
 * <p>
 * CAN ID layout:
 * <pre>
 *   pingId  = ((class >> 4) << 21) | ((class & 0xF) << 12) | 0x0A80
 *   readId  = pingId + 0x40000
 *   writeId = pingId + 0x60000
 *
 *   Module ID encoded in hash low byte as (moduleId + 0x80).
 *   DLC is written into byte[4].  Data bytes follow from byte[5].
 * </pre>
 */
public final class CdBFrameEncoder {

    private CdBFrameEncoder() {}

    /**
     * Builds the 29-bit base (ping) CAN ID for the given class and module.
     *
     * @param canClass  8-bit device class
     * @param moduleId  module slot (0 = broadcast, 1–127 = addressed)
     * @return 29-bit CAN ID
     */
    public static int buildPingCanId(int canClass, int moduleId) {
        int base = ((canClass >> 4) << 21) | ((canClass & 0xF) << 12) | 0x0A80;
        return base | ((moduleId + 0x80) & 0xFF);
    }

    /**
     * Encodes a 29-bit CAN ID into bytes 0–3 of a 13-byte frame array
     * (big-endian, bits 28:0).
     */
    static void encodeCanId(int[] frame, int canId) {
        frame[0] = (canId >> 24) & 0x1F;
        frame[1] = (canId >> 16) & 0xFF;
        frame[2] = (canId >>  8) & 0xFF;
        frame[3] =  canId        & 0xFF;
    }

    /**
     * Builds a class-level broadcast ping frame (DLC=0) to discover all modules of a class.
     *
     * @param canClass 8-bit device class
     * @return 13-byte frame
     */
    public static int[] buildPing(int canClass) {
        int[] frame = new int[13];
        encodeCanId(frame, buildPingCanId(canClass, 0));
        frame[4] = 0;  // DLC=0 for broadcast ping
        return frame;
    }

    /**
     * Builds a register read request frame (DLC=2: slot, register).
     *
     * @param canClass 8-bit device class
     * @param moduleId module slot (1–127)
     * @param slot     sub-register slot (0 = global)
     * @param reg      register number within slot
     * @return 13-byte frame
     */
    public static int[] buildReadRegister(int canClass, int moduleId, int slot, int reg) {
        int[] frame = new int[13];
        int readId  = buildPingCanId(canClass, moduleId) + 0x40000;
        encodeCanId(frame, readId);
        frame[4] = 2;
        frame[5] = slot & 0xFF;
        frame[6] = reg  & 0xFF;
        return frame;
    }

    /**
     * Builds a register write command frame (DLC=4: slot, register, value hi, value lo).
     *
     * @param canClass 8-bit device class
     * @param moduleId module slot (1–127)
     * @param slot     sub-register slot (0 = global)
     * @param reg      register number within slot
     * @param value    16-bit value to write
     * @return 13-byte frame
     */
    public static int[] buildWriteRegister(int canClass, int moduleId, int slot, int reg, int value) {
        int[] frame = new int[13];
        int writeId = buildPingCanId(canClass, moduleId) + 0x60000;
        encodeCanId(frame, writeId);
        frame[4] = 4;
        frame[5] = slot         & 0xFF;
        frame[6] = reg          & 0xFF;
        frame[7] = (value >> 8) & 0xFF;
        frame[8] = value        & 0xFF;
        return frame;
    }

    /**
     * Builds an OLD-protocol addressed ping frame (DLC=0).
     * Frame layout: byte[0]={@code 0x00}; byte[1]={@code 0x40}; byte[2]=classId; byte[3]=0x80|addr.
     * This is the flat 4-byte header format used by LampenChef (0x4A) and any other
     * OLD-protocol class: no data bytes, DLC=0.
     *
     * @param classId 8-bit device class
     * @param addr    module address (0–127)
     * @return 13-byte frame
     */
    static int[] buildOldProtocolPing(int classId, int addr) {
        int[] frame = new int[13];
        frame[0] = 0x00;
        frame[1] = 0x40;
        frame[2] = classId & 0xFF;
        frame[3] = 0x80 | (addr & 0x7F);
        frame[4] = 0;  // DLC=0
        return frame;
    }

    /**
     * Builds a write frame with arbitrary data bytes starting at byte[5].
     * Use this for protocol commands that don't follow the slot/reg/value layout
     * (e.g. select, deselect, factory-reset).
     *
     * @param canClass  8-bit device class
     * @param moduleId  module slot (0 = broadcast, 1–127 = addressed)
     * @param dlc       data length code (0–8)
     * @param data      up to 8 data bytes written from byte[5] onward
     * @return 13-byte frame
     */
    static int[] buildWriteFrame(int canClass, int moduleId, int dlc, int[] data) {
        int[] frame = new int[13];
        int writeId = buildPingCanId(canClass, moduleId) + 0x60000;
        encodeCanId(frame, writeId);
        frame[4] = dlc & 0xFF;
        for (int i = 0; i < data.length && i < 8; i++) {
            frame[5 + i] = data[i] & 0xFF;
        }
        return frame;
    }

    /**
     * Extracts the 16-bit register value from a read-reply frame (bytes 7–8).
     *
     * @param frame 13-byte frame
     * @return the 16-bit value or {@link Optional#empty()} if DLC &lt; 4
     */
    public static Optional<Integer> extractReadReplyValue(int[] frame) {
        if (frame == null || frame.length != 13) return Optional.empty();
        int dlc = frame[4] & 0x0F;
        if (dlc < 4) return Optional.empty();
        return Optional.of(((frame[7] & 0xFF) << 8) | (frame[8] & 0xFF));
    }
}
