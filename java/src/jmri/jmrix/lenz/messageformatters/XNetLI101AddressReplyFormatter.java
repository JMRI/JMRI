package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Format replies for XPressNet LI101 Address.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLI101AddressReplyFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&
                m.getElement(0) == XNetConstants.LI101_REQUEST &&
                m.getElement(1) == XNetConstants.LI101_REQUEST_ADDRESS;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not supported");
        }
        return Bundle.getMessage("XNetReplyLIAddress", m.getElement(2));
    }

}
