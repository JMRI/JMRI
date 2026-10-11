package jmri.jmrix.lenz.hornbyelite.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;
import jmri.jmrix.lenz.messageformatters.XNetLocoInfoReplyUtilities;

/**
 * Formatter for Hornby Elite specific Multiple Unit Info Reply messages.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class HornbyEliteMultiUnitInfoReplyFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&
                ((XNetReply) m).getElement(0) == XNetConstants.LOCO_INFO_MUED_UNIT && (
                ((XNetReply) m).getElement(1) == 0xF8 ||  // only handle the two Hornby Elite specific messages
                ((XNetReply) m).getElement(1) == 0xF9 );
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)){
            throw new IllegalArgumentException("Message is not supported");
        }
        StringBuilder text = new StringBuilder();
        if (m.getElement(1) == 0xF8) {
            // This message is a Hornby addition to the protocol
            // indicating the speed and direction of a locomoitve
            // controlled by the elite's built in throttles
            text = new StringBuilder(Bundle.getMessage("XNetReplyLocoEliteSLabel") + " ");
            text.append(LenzCommandStation.calcLocoAddress(m.getElement(2), m.getElement(3)));
            text.append(",").append(XNetLocoInfoReplyUtilities.parseSpeedAndDirection(m.getElement(4), m.getElement(5))).append(" ");
        } else if (m.getElement(1) == 0xF9) {
            // This message is a Hornby addition to the protocol
            // indicating the function on/off status of a locomoitve
            // controlled by the elite's built in throttles
            text = new StringBuilder(Bundle.getMessage("XNetReplyLocoEliteFLabel") + " ");
            text.append(LenzCommandStation.calcLocoAddress(m.getElement(2), m.getElement(3))).append(" ");
            // message byte 5, contains F0,F1,F2,F3,F4
            int element4 = m.getElement(4);
            // message byte 5, contains F12,F11,F10,F9,F8,F7,F6,F5
            int element5 = m.getElement(5);
            text.append(XNetLocoInfoReplyUtilities.parseFunctionStatus(element4, element5));
        }
        return text.toString();
    }

}
