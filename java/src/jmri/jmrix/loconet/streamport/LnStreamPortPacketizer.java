package jmri.jmrix.loconet.streamport;

import java.io.DataInputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts Stream-based I/O to/from LocoNet messages. The "LocoNetInterface"
 * side sends/receives LocoNetMessage objects. The connection to a
 * LnPortController is via a pair of *Streams, which then carry sequences of
 * characters for transmission.
 * <P>
 * Messages come to this via the main GUI thread, and are forwarded back to
 * listeners in that same thread. Reception and transmission are handled in
 * dedicated threads by RcvHandler and XmtHandler objects. Those are internal
 * classes defined here. The thread priorities are:
 * <UL>
 * <LI> RcvHandler - at highest available priority
 * <LI> XmtHandler - down one, which is assumed to be above the GUI
 * <LI> (everything else)
 * </UL>
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 *
 */
public class LnStreamPortPacketizer extends jmri.jmrix.loconet.LnPacketizer {

    public LnStreamPortPacketizer() {
        super();
    }

    public LnStreamPortController streamController = null;

    @Override
    public boolean isXmtBusy() {
        if (streamController == null) {
            return false;
        }

        return true;
    }

    /**
     * Make connection to existing LnPortController object.
     *
     * @param p Port controller for connected. Save this for a later disconnect
     *          call
     */
    public void connectPort(LnStreamPortController p) {
        istream = p.getInputStream();
        ostream = p.getOutputStream();
        if (controller != null) {
            log.warn("connectPort: connect called while connected");
        }
        streamController = p;
    }

    /**
     * Break connection to existing LnPortController object. Once broken,
     * attempts to send via "message" member will fail.
     *
     * @param p previously connected port
     */
    public void disconnectPort(LnStreamPortController p) {
        istream = null;
        ostream = null;
        if (streamController != p) {
            log.warn("disconnectPort: disconnect called from non-connected LnPortController");
        }
        streamController = null;
    }

    private final static Logger log = LoggerFactory.getLogger(LnStreamPortPacketizer.class);
}
