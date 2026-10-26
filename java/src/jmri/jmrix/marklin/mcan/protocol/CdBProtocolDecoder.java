package jmri.jmrix.marklin.mcan.protocol;

import jmri.jmrix.marklin.mcan.classify.CdBClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.CdBFrameType;
import jmri.jmrix.marklin.mcan.classify.ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.FrameProtocol;

/**
 * Second-pass decoder for CdB configuration area frames.
 * <p>
 * Input: a {@link ClassifiedFrame} whose {@link FrameProtocol} is
 * {@link FrameProtocol#CDB_CONFIG}.  Decodes the CAN ID structure to extract
 * the device class, module ID and frame direction (ping / read / write / reply).
 * <p>
 * CAN ID layout for NEW-protocol CdB frames (29-bit):
 * <pre>{@code
 *   pingId  = ((class >> 4) << 21) | ((class & 0xF) << 12) | 0x0A80
 *   readId  = pingId + 0x40000
 *   writeId = pingId + 0x60000
 *
 *   Module ID is encoded in the low byte of the 16-bit hash as (moduleId + 0x80).
 *   A broadcast ping has hash low byte = 0x80 (module ID = 0).
 * }</pre>
 */
public final class CdBProtocolDecoder {

    private CdBProtocolDecoder() {}

    /**
     * Perform second-pass decoding on a CDB_CONFIG frame.
     *
     * @param frame first-pass result; must have {@code protocol() == CDB_CONFIG}
     * @return decoded result; frameType is UNKNOWN for unrecognised CAN ID offsets
     * @throws IllegalArgumentException if {@code frame} is not a CDB_CONFIG frame
     */
    public static CdBClassifiedFrame decode(ClassifiedFrame frame) {
        if (frame.protocol() != FrameProtocol.CDB_CONFIG) {
            throw new IllegalArgumentException(
                "CdBProtocolDecoder requires a CDB_CONFIG frame, got " + frame.protocol());
        }
        int[] raw = frame.raw().toArray();

        // Reconstruct 29-bit CAN ID from bytes 0–3 (big-endian, top 3 bits unused)
        int canId = ((raw[0] & 0x1F) << 24) | ((raw[1] & 0xFF) << 16)
                  | ((raw[2] & 0xFF) <<  8) |  (raw[3] & 0xFF);

        // Hash = lower 16 bits of the CAN ID
        int hash16 = canId & 0xFFFF;

        // Module: encoded as (moduleId + 0x80) in hash low byte
        int moduleRaw = hash16 & 0xFF;
        int moduleId  = moduleRaw - 0x80;  // 0 = broadcast, 1–127 = addressed

        // Class: high nibble in CAN ID bits 24:21, low nibble in hash bits 15:12
        int classHigh = (canId >> 21) & 0x0F;
        int classLow  = (hash16 >> 8) & 0x0F;
        int canClass  = (classHigh << 4) | classLow;

        // Reconstruct the expected pingId for this class and module, then
        // compare to determine direction from the delta.
        int pingId = CdBFrameEncoder.buildPingCanId(canClass, moduleId);
        int delta  = canId - pingId;

        CdBFrameType frameType;
        switch (delta) {
            case 0x00000: frameType = CdBFrameType.PING;       break;
            case 0x10000: frameType = CdBFrameType.PING_REPLY; break;
            case 0x40000: frameType = CdBFrameType.READ;       break;
            case 0x50000: frameType = CdBFrameType.READ_REPLY; break;
            case 0x60000: frameType = CdBFrameType.WRITE;      break;
            default:
                frameType = CdBFrameType.UNKNOWN;  // future or unrecognised offsets
        }

        return new CdBClassifiedFrame(frameType, canClass, moduleId, frame);
    }
}
