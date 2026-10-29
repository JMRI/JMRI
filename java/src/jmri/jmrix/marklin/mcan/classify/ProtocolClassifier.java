package jmri.jmrix.marklin.mcan.classify;

/**
 * Protocol classifier for 13-byte Märklin CAN frames.
 * <p>
 * Determines the protocol family ({@link FrameProtocol}) and delegates full decoding to
 * {@link jmri.jmrix.marklin.mcan.cdb.CdBFrameParser#parse} for CDB_CONFIG frames or
 * {@link jmri.jmrix.marklin.mcan.cs2.Cs2FrameParser} for CS2_NATIVE frames.
 * <p>
 * Contract: {@link #classify(int[])} never throws and never returns null.
 * {@link FrameProtocol#UNKNOWN} is a valid, expected result for any frame
 * the library has not yet modelled (MS2 config streams, new module types, etc.).
 */
public final class ProtocolClassifier {

    private ProtocolClassifier() {}

    /**
     * Classify a raw 13-byte CAN frame.
     *
     * <ol>
     *   <li>If the CS2 fingerprint is present → {@link FrameProtocol#CS2_NATIVE}</li>
     *   <li>Else if the CdB hash pattern is present → {@link FrameProtocol#CDB_CONFIG}</li>
     *   <li>Else → {@link FrameProtocol#UNKNOWN}</li>
     * </ol>
     *
     * @param raw13 13-byte CAN frame. null or wrong length is classified as {@link FrameProtocol#UNKNOWN}
     * @return classification result, never null
     */
    public static ClassifiedFrame classify(int[] raw13) {
        if (raw13 == null || raw13.length != 13) {
            return unknown(new RawFrame(new int[13]));
        }
        RawFrame frame = new RawFrame(raw13);

        // CS2 native frames have a hash fingerprint and a valid DLC.
        Cs2Header cs2 = Cs2Header.tryParse(raw13);
        if (cs2 != null) {
            return new ClassifiedFrame(FrameProtocol.CS2_NATIVE, frame, cs2);
        }

        // CdB config frames use the hash pattern (hash16 & 0x0F80) == 0x0A80.
        if (isCdBConfigPattern(raw13)) {
            return new ClassifiedFrame(FrameProtocol.CDB_CONFIG, frame, null);
        }
        /**
         * TODO: Add CS1 / MS1 / Ecos protocol support.
         */

        return unknown(frame);
    }

    /**
     * True when bytes 2–3 of the frame carry the CdB config area hash pattern.
     * The pattern {@code 0x0A80} occupies bits 11–7 of the 16-bit hash field,
     * which is mutually exclusive with the CS2 fingerprint.
     */
    static boolean isCdBConfigPattern(int[] raw13) {
        int hash16 = ((raw13[2] & 0xFF) << 8) | (raw13[3] & 0xFF);
        return (hash16 & 0x0F80) == 0x0A80;
    }

    private static ClassifiedFrame unknown(RawFrame frame) {
        return new ClassifiedFrame(FrameProtocol.UNKNOWN, frame, null);
    }
}
