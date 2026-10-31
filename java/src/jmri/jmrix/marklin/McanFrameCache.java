package jmri.jmrix.marklin;

import java.util.Arrays;

import jmri.jmrix.marklin.mcan.McanFrame;
import jmri.jmrix.marklin.mcan.classify.ProtocolClassifier;

/**
 * Holds the classified {@link McanFrame} for one {@link MarklinReply} or
 * {@link MarklinMessage}.
 * <p>
 * The owning message can still be changed through inherited setters such as
 * {@code setElement}, which offer no hook. The cache therefore compares the
 * current 13 bytes against the classified snapshot and reclassifies on change.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
final class McanFrameCache {

    private int[] snapshot;
    private McanFrame frame;

    /**
     * @param current the owner's current 13 bytes; not retained if unchanged
     * @return the classified frame for {@code current}
     * @throws IllegalArgumentException if {@code current} is not 13 bytes long
     */
    synchronized McanFrame get(int[] current) {
        if (frame == null || !Arrays.equals(snapshot, current)) {
            frame = ProtocolClassifier.classify(current);
            snapshot = current;
        }
        return frame;
    }
}
