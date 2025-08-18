package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Formatter for XPressNet Direct Model Time messages.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetDirectModelTimeFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&  m.getElement(0) == XNetConstants.CS_ADVANCED_INFO_RESPONSE && m.getElement(1) == XNetConstants.MODEL_TIME;
    }

    @Override
    public String formatMessage(Message m) {
        if (!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not supported");
        }
        int day = ((m.getElement(2) & 0xE0) >>5);
        int hour = (m.getElement(2) & 0x1F);
        int minute = m.getElement(3) & 0xFF;
        int factor = (m.getElement(4)) &0xFF;
        return Bundle.getMessage("XNetTimeFormat", day,hour,minute,factor);
    }

}
