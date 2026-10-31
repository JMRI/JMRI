package jmri.jmrix.marklin;

import jmri.jmrix.marklin.mcan.McanFrame;
import jmri.jmrix.marklin.mcan.cs2.Cs2Frame;

/**
 * Bridge between the Marklin JMRI bean layer and the mcan sub-package.
 * <p>
 * Provides a single entry-point to obtain the typed CS2 view of an incoming
 * {@link MarklinReply}.
 * <p>
 * Returns {@code null} for frames that are not CS2_NATIVE (e.g. CdB or
 * unknown protocols).
 */
public final class MarklinCanBridge {

    private MarklinCanBridge() {}

    /**
     * Parses a {@link MarklinReply} into a typed CS2 frame.
     *
     * @param reply the incoming reply frame
     * @return typed {@link Cs2Frame} or {@code null} if not CS2_NATIVE
     */
    public static Cs2Frame parse(MarklinReply reply) {
        McanFrame cf = reply.getFrame();
        return cf instanceof Cs2Frame ? (Cs2Frame) cf : null;
    }
}
