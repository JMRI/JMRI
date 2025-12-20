package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Formatter for XPressNet XNet Broadcast Emergency Stop replies.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetBroadcastEmergencyStopFormatter implements XPressNetMessageFormatter {
    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&
                ((XNetReply) m).getElement(0) == XNetConstants.BC_EMERGENCY_STOP &&
                ((XNetReply) m).getElement(1) == XNetConstants.BC_EVERYTHING_STOP;
    }

    @Override
    public String formatMessage(Message message) {
        if(!handlesMessage(message)){
            throw new IllegalArgumentException("Message is not supported");
        }
        return Bundle.getMessage("XNetReplyBCEverythingStop");
    }
}
