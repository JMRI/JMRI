package jmri.jmrix.marklin;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import jmri.jmrix.marklin.mcan.cdb.CdBFrameParser;
import jmri.jmrix.marklin.mcan.McanFrame;
import jmri.jmrix.marklin.mcan.classify.ProtocolClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatches OEM CAN-digital-Bahn (CdB) device configuration frames to typed
 * {@link CdBConfigListener}s.
 * <p>
 * CdB config frames share command code space with MCAN but are distinguished by
 * the hash field (bytes 2–3) matching pattern {@code 0x0A80} (mask {@code 0x0F80}).
 * Operational CdB frames (turnout, sensor, occupancy) use standard MCAN encoding
 * and pass through unfiltered.
 * <p>
 * The traffic controller calls {@link #offer(MarklinReply)} once per received
 * frame. The frame still reaches the standard listener chain afterwards.
 */
public class CdBClassFilter {

    // ---- listener registry ----------------------------------------------

    private final Set<CdBConfigListener> listeners = new CopyOnWriteArraySet<>();

    public void addCdBConfigListener(CdBConfigListener l) {
        listeners.add(l);
    }

    public void removeCdBConfigListener(CdBConfigListener l) {
        listeners.remove(l);
    }

    // ---- fast-path identification ---------------------------------------

    /**
     * Returns {@code true} if bytes 2–3 match the CdB hash pattern.
     *
     * @param r incoming 13-byte frame
     * @return true for a CdB configuration frame
     * @deprecated since 5.17.5; use {@link ProtocolClassifier#classify(int[])} and test for
     *             {@link jmri.jmrix.marklin.mcan.cdb.CdBFrameParser.CdBFrame}, or {@link #offer(MarklinReply)}.
     */
    @Deprecated(since = "5.17.5", forRemoval = true)
    public static boolean isCdBConfigFrame(MarklinReply r) {
        return ProtocolClassifier.isCdBConfigPattern(r.toIntArray());
    }

    // ---- dispatch entry point ------------------------------------------

    /**
     * Offers a frame to the CdB dispatcher.
     *
     * @param r incoming 13-byte frame
     * @return {@code true} if the frame is CdB config traffic and was notified to
     *         {@link CdBConfigListener}s; {@code false} otherwise
     */
    public boolean offer(MarklinReply r) {
        McanFrame cf = r.getFrame();
        if (!(cf instanceof CdBFrameParser.CdBFrame)) {
            return false;
        }

        CdBFrameParser.CdBFrame frame = (CdBFrameParser.CdBFrame) cf;

        if (log.isDebugEnabled()) {
            log.debug("CdB config frame: frameType={} canClass=0x{} module={} raw=[{}]",
                    frame.frameType(), Integer.toHexString(frame.classId()), frame.moduleId(), frame.raw());
        }

        for (CdBConfigListener l : listeners) {
            l.cdBConfigReply(frame, r);
        }
        return true;
    }

    private static final Logger log = LoggerFactory.getLogger(CdBClassFilter.class);
}
