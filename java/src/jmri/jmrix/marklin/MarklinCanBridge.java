package jmri.jmrix.marklin;

import jmri.jmrix.marklin.mcan.classify.ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.Cs2ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.FrameProtocol;
import jmri.jmrix.marklin.mcan.classify.ProtocolClassifier;
import jmri.jmrix.marklin.mcan.cs2.Cs2FrameParser;

/**
 * Bridge between the Marklin JMRI bean layer and the mcan sub-package.
 * <p>
 * Provides a single entry-point to parse an incoming {@link MarklinReply}
 * through the two-pass mcan pipeline:
 * {@link ProtocolClassifier#classify(int[])} → {@link Cs2FrameParser#parse}.
 * <p>
 * Returns {@code null} for frames that are not CS2_NATIVE (e.g. CdB or
 * unknown protocols), allowing callers to fall through gracefully.
 */
public final class MarklinCanBridge {

    private MarklinCanBridge() {}

    /**
     * Parses a {@link MarklinReply} through the mcan two-pass pipeline.
     *
     * @param reply the incoming reply frame
     * @return typed {@link Cs2ClassifiedFrame} or {@code null} if not CS2_NATIVE
     */
    public static Cs2ClassifiedFrame parse(MarklinReply reply) {
        ClassifiedFrame cf = ProtocolClassifier.classify(reply.toIntArray());
        if (cf.protocol() != FrameProtocol.CS2_NATIVE) {
            return null;
        }
        return Cs2FrameParser.parse(cf);
    }
}
