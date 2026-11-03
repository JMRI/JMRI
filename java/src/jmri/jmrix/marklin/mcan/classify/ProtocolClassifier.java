package jmri.jmrix.marklin.mcan.classify;

import jmri.jmrix.marklin.mcan.McanFrame;
import jmri.jmrix.marklin.mcan.RawFrame;
import jmri.jmrix.marklin.mcan.UnknownFrame;
import jmri.jmrix.marklin.mcan.cdb.CdBFrameParser;
import jmri.jmrix.marklin.mcan.cs2.Cs2FrameParser;
import jmri.jmrix.marklin.mcan.cs2.Cs2Header;

/**
 * Protocol classifier for 13-byte M&auml;rklin CAN frames.
 * <p>
 * Determines the protocol family and returns the fully parsed, typed frame:
 * a {@link jmri.jmrix.marklin.mcan.cs2.Cs2Frame} subtype, a
 * {@link jmri.jmrix.marklin.mcan.cdb.CdBFrameParser.CdBFrame} subtype or an {@link UnknownFrame}.
 * <p>
 * This package sits above {@code cs2} and {@code cdb}; neither may depend on it.
 */
public final class ProtocolClassifier {

    private ProtocolClassifier() {}

    /**
     * Classify and parse a raw 13-byte CAN frame.
     *
     * <ol>
     *   <li>If the CS2 fingerprint is present: CS2 native frame</li>
     *   <li>Else if the CdB hash pattern is present: CdB configuration frame</li>
     *   <li>Else: {@link UnknownFrame}</li>
     * </ol>
     * TODO: Add CS1 / MS1 / ECoS protocol support.
     *
     * @param raw13 13-byte CAN frame
     * @return typed frame, never null
     * @throws IllegalArgumentException if {@code raw13} is null or not 13 bytes long
     */
    public static McanFrame classify(int[] raw13) {
        RawFrame frame = new RawFrame(raw13);
        Cs2Header cs2 = Cs2Header.tryParse(raw13);
        if (cs2 != null) {
            return Cs2FrameParser.parse(frame, cs2);
        }
        if (isCdBConfigPattern(raw13)) {
            return CdBFrameParser.parse(frame);
        }
        return new UnknownFrame(frame);
    }

    /**
     * True when bytes 2–3 of the frame carry the CdB config area hash pattern.
     * The pattern {@code 0x0A80} occupies bits 11–7 of the 16-bit hash field,
     * which is mutually exclusive with the CS2 fingerprint.
     *
     * @param raw13 13-byte CAN frame
     * @return true for a CdB configuration frame
     */
    public static boolean isCdBConfigPattern(int[] raw13) {
        int hash16 = ((raw13[2] & 0xFF) << 8) | (raw13[3] & 0xFF);
        return (hash16 & 0x0F80) == 0x0A80;
    }
}
