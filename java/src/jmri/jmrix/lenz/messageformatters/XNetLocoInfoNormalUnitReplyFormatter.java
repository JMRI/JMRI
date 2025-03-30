package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * XPressNet message formatter for Loco Info Normal Unit Reply.
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetLocoInfoNormalUnitReplyFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&
                ((XNetReply) m).getElement(0) == XNetConstants.LOCO_INFO_NORMAL_UNIT
                && ((XNetReply) m).getElement(1) != XNetConstants.LOCO_FUNCTION_STATUS_HIGH;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)){
            throw new IllegalArgumentException("Message is not supported");
        }
        // message byte 4, contains F0,F1,F2,F3,F4
        int element3 = m.getElement(3);
        // message byte 5, contains F12,F11,F10,F9,F8,F7,F6,F5
        int element4 = m.getElement(4);
        return Bundle.getMessage("XNetReplyLocoNormalLabel") + ","+
                XNetLocoInfoReplyUtilities.parseSpeedAndDirection(m.getElement(1), m.getElement(2)) + " " +
                XNetLocoInfoReplyUtilities.parseFunctionStatus(element3, element4);
    }





}

