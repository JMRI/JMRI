package jmri.jmrix.marklin.mcan.protocol;

import jmri.jmrix.marklin.mcan.classify.CdBClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.CdBFrameType;

import java.util.Optional;

/**
 * Helpers for the CdB factory-fresh commissioning handshake.
 * <p>
 * A module in factory-reset state responds to the class broadcast ping with a DLC=6
 * PING_REPLY carrying module ID=0 and two challenge bytes at data positions 1 and 2
 * (frame bytes[6] and [7]).  These bytes must be echoed in the factory-select frame.
 * <p>
 * Commissioned modules (module ID &gt; 0) reply with DLC=3; their bytes[9..12] carry
 * a hardware fingerprint  -  that is handled separately in {@link jmri.jmrix.marklin.mcan.device.CdBDevice}.
 */
public final class CdBCommissioning {

    private CdBCommissioning() {}

    /**
     * Returns true when the frame is a factory-fresh ping reply:
     * {@link CdBFrameType#PING_REPLY}, module ID = 0, DLC = 6.
     *
     * @param frame second-pass classified CdB frame
     */
    public static boolean isFactoryFreshPingReply(CdBClassifiedFrame frame) {
        return frame.frameType() == CdBFrameType.PING_REPLY
            && frame.moduleId() == 0
            && frame.parent().raw().dlc() == 6;
    }

    /**
     * Extracts the two challenge bytes from a factory-fresh ping reply.
     * The bytes are at data positions 1 and 2 (frame bytes[6] and [7]).
     *
     * @param frame second-pass classified CdB frame
     * @return two-element array [challenge0, challenge1] or empty if not a factory-fresh reply
     */
    public static Optional<int[]> extractChallengeBytes(CdBClassifiedFrame frame) {
        if (!isFactoryFreshPingReply(frame)) {
            return Optional.empty();
        }
        int[] raw = frame.parent().raw().toArray();
        return Optional.of(new int[]{raw[6] & 0xFF, raw[7] & 0xFF});
    }

    /**
     * Builds a write-new-module-ID frame, echoing the challenge bytes from the factory ping reply.
     * Register 2, DLC=6: {0x00, 0x02, newIdHi, newIdLo, challenge0, challenge1}.
     *
     * @param canClass   CdB device class ID
     * @param newId      new module ID to assign (1–127)
     * @param challenge0 first challenge byte from the factory ping reply
     * @param challenge1 second challenge byte from the factory ping reply
     * @return 13-byte CAN frame
     */
    public static int[] buildWriteNewModuleId(int canClass, int newId, int challenge0, int challenge1) {
        return CdBFrameEncoder.buildWriteFrame(canClass, 0, 6, new int[]{
            0x00, 0x02, (newId >> 8) & 0xFF, newId & 0xFF, challenge0 & 0xFF, challenge1 & 0xFF
        });
    }
}
