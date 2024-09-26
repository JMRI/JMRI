package jmri.jmrix.lenz.messageFormatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

public class XNetReplyFormatter implements XPressNetMessageFormatter {
    @Override
    public Boolean handlesMessage(Message m) {
        return m instanceof XNetReply;
    }

    @Override
    public String formatMessage(Message m) {
        return m.toMonitorString();
    }
}
