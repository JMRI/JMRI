package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;
/** Class that translates @Ref{XNetReply} objects into strings
 *
 * NOTE: This is a placeholder until individual message classes are completed.
 *
 * @author Paul Bender Copyright (C) 2024
 */
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
