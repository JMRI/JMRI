package jmri.jmrix.marklin;

import jmri.jmrix.marklin.mcan.cdb.CdBFrameParser;

/**
 * Callback interface for CAN-digital-Bahn device configuration frames.
 * <p>
 * Implementations receive positively identified CdB config traffic (ping, indexed parameter
 * read/write, read replies) dispatched by {@link CdBClassFilter}.
 * <p>
 * Standard MCAN frames including CdB operational traffic are not delivered through this interface.
 *
 */
public interface CdBConfigListener {

    /**
     * Called when a CdB device-configuration frame has been received.
     *
     * @param frame  the typed CdB frame (ping, ping reply, read, read reply, write or unknown)
     * @param rawReply the original 13-byte reply, available for low-level access if needed
     */
    void cdBConfigReply(CdBFrameParser.CdBFrame frame, MarklinReply rawReply);
}
