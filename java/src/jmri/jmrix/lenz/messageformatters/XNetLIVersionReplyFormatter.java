package jmri.jmrix.lenz.messageformatters;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;
/**
 * Format XPressNet reply for LI version.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLIVersionReplyFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply && m.getElement(0) == XNetConstants.LI_VERSION_RESPONSE;
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "cast is checked in handlesMessage")
    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not supported");
        }
        XNetReply r = (XNetReply) m;
        return Bundle.getMessage("XNetReplyLIVersion", (r.getElementBCD(1).floatValue()) / 10, (r.getElementBCD(2).floatValue()) / 10);

    }

}
