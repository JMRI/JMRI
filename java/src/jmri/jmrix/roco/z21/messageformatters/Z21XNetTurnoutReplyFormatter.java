package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.lenz.XPressNetMessageFormatter;
import jmri.jmrix.roco.z21.Z21Constants;
import jmri.jmrix.roco.z21.Z21XNetReply;

/**
 * Format Z21XNet Turnout Replies for display.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21XNetTurnoutReplyFormatter implements XPressNetMessageFormatter {
    public Z21XNetTurnoutReplyFormatter() {
    }

    public Boolean handlesMessage(jmri.jmrix.Message m) {
        return (m instanceof jmri.jmrix.roco.z21.Z21XNetReply && (m.getElement(0) == Z21Constants.LAN_X_TURNOUT_INFO));
    }

    public String formatMessage(jmri.jmrix.Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException("Message not supported");
        }
        Z21XNetReply r = (Z21XNetReply) m;
        int address = (r.getElement(1) << 8 ) + r.getElement(2) +1;
        String state = "";
        switch(r.getElement(3)) {
            case 0x03:
                state += "inconsistent";
                break;
            case 0x02:
                state += "Thrown";
                break;
            case 0x01:
                state += "Closed";
                break;
            default:
                state += "Unknown";
        }
        return Bundle.getMessage("Z21LAN_X_TURNOUT_INFO",address, state);
    }
}
