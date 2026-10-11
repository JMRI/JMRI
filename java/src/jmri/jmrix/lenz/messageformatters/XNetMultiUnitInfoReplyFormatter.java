package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Formatter for XNet Multi Unit Info Reply messages.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetMultiUnitInfoReplyFormatter implements XPressNetMessageFormatter{
    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&
                ((XNetReply) m).getElement(0) == XNetConstants.LOCO_INFO_MUED_UNIT &&
                ((XNetReply) m).getElement(1) != 0xF8 &&  // Exclude the two Hornby Elite specific messages
                ((XNetReply) m).getElement(1) != 0xF9;
    }

    @Override
    public String formatMessage(Message m) {
        if (handlesMessage(m)) {

            return Bundle.getMessage("XNetReplyLocoMULabel") + "," +
                   XNetLocoInfoReplyUtilities.parseSpeedAndDirection(m.getElement(1), m.getElement(2)) +
                   XNetLocoInfoReplyUtilities.parseFunctionStatus(m.getElement(3), m.getElement(4));
        }
        else throw new IllegalArgumentException("Message is not supported");
    }

}
