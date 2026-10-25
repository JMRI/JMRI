package jmri.jmrix.marklin;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import jmri.jmrix.marklin.mcan.cdb.CdBFrameParser;
import jmri.jmrix.marklin.mcan.classify.ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.FrameProtocol;
import jmri.jmrix.marklin.mcan.classify.ProtocolClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Intercepts OEM CAN-digital-Bahn (CdB) device configuration frames before they
 * reach the standard Marklin CAN (MCAN) listener chain.
 * <p>
 * CdB config frames share command code space with MCAN but are distinguished by
 * the hash field (bytes 2–3) matching pattern {@code 0x0A80} (mask {@code 0x0F80}).
 * Operational CdB frames (turnout, sensor, occupancy) use standard MCAN encoding
 * and pass through unfiltered.
 * <p>
 * Call {@link #offer(MarklinReply)} from the traffic controller before forwarding
 * to the standard listener chain.
 */
public class CdBClassFilter {

    /** Mask and value identifying a CdB config frame in bytes 2–3. */
    static final int CDB_HASH_MASK  = 0x0F80;
    static final int CDB_HASH_VALUE = 0x0A80;

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
     * Cheap enough to call on every incoming frame.
     */
    public static boolean isCdBConfigFrame(MarklinReply r) {
        int hash16 = ((r.getElement(2) & 0xFF) << 8) | (r.getElement(3) & 0xFF);
        return (hash16 & CDB_HASH_MASK) == CDB_HASH_VALUE;
    }

    // ---- dispatch entry point ------------------------------------------

    /**
     * Offers a frame to the CdB pre-filter.
     * Call this before the standard Märklin listener chain.
     *
     * @param r incoming 13-byte frame
     * @return {@code true} if the frame is CdB config traffic (notified to
     *         {@link CdBConfigListener}s  -  must not be forwarded further);
     *         {@code false} if it is normal MCAN traffic
     */
    public boolean offer(MarklinReply r) {
        int[] raw13 = new int[13];
        for (int i = 0; i < 13; i++) raw13[i] = r.getElement(i) & 0xFF;

        ClassifiedFrame cf = ProtocolClassifier.classify(raw13);
        if (cf.protocol() != FrameProtocol.CDB_CONFIG) {
            return false;
        }

        CdBFrameParser.CdBFrame frame = CdBFrameParser.parse(cf);

        if (log.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 13; i++) {
                if (i > 0) sb.append(' ');
                sb.append(String.format("%02X", raw13[i]));
            }
            log.info("CdB frame intercepted: frameType={} canClass=0x{} module={} raw=[{}]",
                    frame.frameType(), Integer.toHexString(frame.classId()), frame.moduleId(), sb);
        }

        for (CdBConfigListener l : listeners) {
            l.cdBConfigReply(frame, r);
        }
        return true;
    }

    private static final Logger log = LoggerFactory.getLogger(CdBClassFilter.class);
}
