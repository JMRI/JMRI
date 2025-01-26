package jmri.jmrix.lenz.messageformatters;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * XPressNet message formatter for throttle taken over reply.
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetThrottleTakenOverReplyFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&  ((XNetReply) m).getElement(0) == XNetConstants.LOCO_INFO_RESPONSE
                && ((XNetReply) m).getElement(1) == XNetConstants.LOCO_NOT_AVAILABLE;
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "cast is checked in handlesMessage")
    @Override
    public String formatMessage(Message m) {
        if (handlesMessage(m)) {
            XNetReply reply = (XNetReply) m;
            int address = reply.getThrottleMsgAddr();
            return Bundle.getMessage("XNetReplyLocoLabel") + " " +
                    Bundle.getMessage("rsType") + " " + address + " " +
                    Bundle.getMessage("XNetReplyLocoOperated");
        }
        throw new IllegalArgumentException("Message is not supported");
    }

}
