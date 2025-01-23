package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Formatter for XPressNet V1 software version replies.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetV1SoftwareVersionReplyFormatter implements XPressNetMessageFormatter {

    @Override
    public Boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&
                ((XNetReply) m).getElement(0) == XNetConstants.CS_REQUEST_RESPONSE &&
                ((XNetReply) m).getElement(1) == XNetConstants.CS_SOFTWARE_VERSION;
    }

    @Override
    public String formatMessage(Message m) {
        if (!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not supported");
        }
        XNetReply r = (XNetReply) m;
        return Bundle.getMessage("XNetReplyCSVersionV1", (r.getElementBCD(2).floatValue()) / 10);
    }

}
