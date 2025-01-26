package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Format XPressNet messages for Service Mode Response for display.
 *
 * @author Paul Bender Copyright (C) 2024
 */

public class XNetServiceModeResponseFormatter implements XPressNetMessageFormatter {
    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&
                ((XNetReply) m).isServiceModeResponse() &&
                ((XNetReply) m).getElement(1) != XNetConstants.CS_SOFTWARE_VERSION;
    }

    @Override
    public String formatMessage(Message m) {
        if(!(m instanceof XNetReply) && ((XNetReply) m).isServiceModeResponse()) {
            throw new IllegalArgumentException("Message is not a Service Mode Response");
        }
        XNetReply r = (XNetReply) m;
        if (r.isDirectModeResponse()) {
            return Bundle.getMessage("XNetReplyServiceModeDirectResponse", r.getServiceModeCVNumber(), r.getServiceModeCVValue());
        } else if (r.isPagedModeResponse()) {
            return Bundle.getMessage("XNetReplyServiceModePagedResponse", r.getServiceModeCVNumber(), r.getServiceModeCVValue());
        } else {
            return r.toString();
        }
    }

}
