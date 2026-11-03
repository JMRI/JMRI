package jmri.jmrix.marklin.mcan.cs2;

/**
 * Low-level CS2 CAN frame byte-layout builder shared by all family encoder classes.
 * <p>
 * Encodes the 29-bit CAN ID (priority/command/response/hash) into bytes 0–3,
 * DLC into byte 4 and up to 8 data bytes into bytes 5–12.
 * <p>
 * CAN ID layout (big-endian, top 3 bits of byte[0] unused):
 * <pre>
 *   bits 28–25: priority (4 bits)
 *   bits 24–17: command  (8 bits)
 *   bit  16:    response flag
 *   bits 15–0:  hash     (16 bits)
 * </pre>
 */
final class Cs2FrameBuilder {

    private Cs2FrameBuilder() {}

    /**
     * Builds a 13-byte CS2 CAN frame.
     *
     * @param priority  4-bit priority (0–15)
     * @param command   8-bit command code
     * @param response  true for reply frames
     * @param hash      16-bit hash carrying the CS2 fingerprint
     * @param dlc       data length (0–8)
     * @param data      up to 8 data bytes; shorter arrays are zero-padded
     * @return 13-byte frame
     */
    static int[] buildFrame(int priority, int command, boolean response,
                             int hash, int dlc, int[] data) {
        long canId = ((long)(priority & 0x0F) << 25)
                   | ((long)(command  & 0xFF) << 17)
                   | ((long)(response ? 1 : 0) << 16)
                   | (hash     & 0xFFFF);

        int[] frame = new int[13];
        frame[0] = (int)((canId >> 24) & 0x1F);
        frame[1] = (int)((canId >> 16) & 0xFF);
        frame[2] = (int)((canId >>  8) & 0xFF);
        frame[3] = (int)( canId        & 0xFF);
        frame[4] = dlc & 0xFF;
        if (data != null) {
            for (int i = 0; i < data.length && i < 8; i++) {
                frame[5 + i] = data[i] & 0xFF;
            }
        }
        return frame;
    }
}
