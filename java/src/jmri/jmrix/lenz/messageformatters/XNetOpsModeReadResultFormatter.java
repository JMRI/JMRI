package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Formatter for Ops Mode Read Result messages.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetOpsModeReadResultFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&  m.getElement(0) == XNetConstants.CS_ADVANCED_INFO_RESPONSE && m.getElement(1) == XNetConstants.POM_RESULTS;
    }

    @Override
    public String formatMessage(Message m) {
        if (!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not supported");
        }
        return Bundle.getMessage("XNetReplyOpsModeResultResponse",
                LenzCommandStation.calcLocoAddress(m.getElement(2), m.getElement(3)),
                m.getElement(4) &0xFF);
    }

}
