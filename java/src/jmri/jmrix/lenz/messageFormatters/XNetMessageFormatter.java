package jmri.jmrix.lenz.messageFormatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

public class XNetMessageFormatter implements XPressNetMessageFormatter {
    @Override
    public Boolean handlesMessage(Message m) {
        return m instanceof XNetMessage;
    }

    @Override
    public String formatMessage(Message m) {
        return m.toMonitorString();
    }
}
