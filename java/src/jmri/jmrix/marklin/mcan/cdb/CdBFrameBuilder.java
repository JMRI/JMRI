package jmri.jmrix.marklin.mcan.cdb;

/**
 * Low-level CdB CAN frame encoder. Package-private  -  use the command classes.
 * <p>
 * CAN ID formula (NEW-protocol):
 * <pre>
 *   pingId      = ((classId &gt;&gt; 4) &lt;&lt; 21) | ((classId &amp; 0xF) &lt;&lt; 12) | 0x0A80
 *                 | ((moduleId + 0x80) &amp; 0xFF)
 *   pingReplyId = pingId + 0x10000
 *   readId      = pingId + 0x40000
 *   readReplyId = pingId + 0x50000
 *   writeId     = pingId + 0x60000
 * </pre>
 */
final class CdBFrameBuilder {

    static final int HASH_BASE      = 0x0A80;
    static final int DELTA_REPLY    = 0x10000;
    static final int DELTA_READ     = 0x40000;
    static final int DELTA_READ_REP = 0x50000;
    static final int DELTA_WRITE    = 0x60000;

    static final int REG_SELECT        = 0xF7;
    static final int REG_DESELECT      = 0xFE;
    static final int REG_FACTORY_RESET = 0xFF;

    private CdBFrameBuilder() {}

    /**
     * Computes the 29-bit ping (base) CAN ID for a given class and module slot.
     *
     * @param classId  8-bit CdB device class
     * @param moduleId module slot (0 = broadcast, 1–127 = addressed)
     * @return 29-bit CAN ID
     */
    static int pingCanId(int classId, int moduleId) {
        return ((classId >> 4) << 21) | ((classId & 0xF) << 12)
             | HASH_BASE | ((moduleId + 0x80) & 0xFF);
    }

    static void encodeCanId(int[] frame, int canId) {
        frame[0] = (canId >> 24) & 0x1F;
        frame[1] = (canId >> 16) & 0xFF;
        frame[2] = (canId >>  8) & 0xFF;
        frame[3] =  canId        & 0xFF;
    }

    static int[] buildWriteRaw(int classId, int moduleId, int dlc, int[] data) {
        int[] frame = new int[13];
        encodeCanId(frame, pingCanId(classId, moduleId) + DELTA_WRITE);
        frame[4] = dlc & 0xFF;
        for (int i = 0; i < data.length && i < 8; i++) {
            frame[5 + i] = data[i] & 0xFF;
        }
        return frame;
    }
}
