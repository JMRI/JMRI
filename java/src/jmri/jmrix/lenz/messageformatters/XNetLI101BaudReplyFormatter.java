package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Format replies for XPressNet LI101F reply for Baud Rate.
 *
 * @author Paul Bender Copyright (C) 205
 */
public class XNetLI101BaudReplyFormatter implements XPressNetMessageFormatter {
    private static final String X_NET_REPLY_LI_BAUD = "XNetReplyLIBaud";

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply && 
                m.getElement(0) == XNetConstants.LI101_REQUEST && 
                m.getElement(1) == XNetConstants.LI101_REQUEST_BAUD;
    }

    @Override
    public String formatMessage(Message m) {
        if (!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not supported");
        }
        switch (m.getElement(2)) {
            case 1:
                return Bundle.getMessage(X_NET_REPLY_LI_BAUD, Bundle.getMessage("LIBaud19200"));
            case 2:
                return Bundle.getMessage(X_NET_REPLY_LI_BAUD, Bundle.getMessage("Baud38400"));
            case 3:
                return Bundle.getMessage(X_NET_REPLY_LI_BAUD, Bundle.getMessage("Baud57600"));
            case 4:
                return Bundle.getMessage(X_NET_REPLY_LI_BAUD, Bundle.getMessage("Baud115200"));
            default:
                return Bundle.getMessage(X_NET_REPLY_LI_BAUD, Bundle.getMessage("BaudOther"));
        }
    }

}
