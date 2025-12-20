package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Formatter for XPressNet Broadcast Model Time messages.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetBCModelTimeFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&  m.getElement(0) == 0x63 && m.getElement(1) == 0x03;
    }

    @Override
    public String formatMessage(Message m) {
        if (!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not supported");
        }
        int day = ((m.getElement(2) & 0xE0) >>5);
        int hour = ((m.getElement(2) & 0x1F));
        int minute = m.getElement(3) & 0x3F;
        boolean stopped = (m.getElement(3) & 0x10000000) != 0;
        return Bundle.getMessage("XNetBCTimeFormat", day,hour,minute, Bundle.getMessage(stopped ? "ClockStopped" : "ClockRunning"));
    }

}
