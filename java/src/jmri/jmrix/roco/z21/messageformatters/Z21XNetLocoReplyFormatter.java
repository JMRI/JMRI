package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.lenz.XPressNetMessageFormatter;
import jmri.jmrix.lenz.messageformatters.XNetLocoInfoReplyUtilities;
import jmri.jmrix.roco.z21.Z21XNetReply;

/**
 * Format Z21XNetReply containg Z21 Specific Loco results for display in the XpressNet monitor.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21XNetLocoReplyFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(jmri.jmrix.Message m) {
        return m instanceof Z21XNetReply &&
        (m.getElement(0)&0xE0)==0xE0 && ((m.getElement(0)&0x0f) >= 7 && (m.getElement(0)&0x0f) <=15 );
    }

    @Override
    public String formatMessage(jmri.jmrix.Message m) {
        if (!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not a Z21XNetReply");
        }
        //Data Byte 0 and 1 contain the locomotive address
        int messageaddress = ((m.getElement(1) & 0x3F) << 8) + (m.getElement(2) & 0xff);
        String text = "Z21 Mobile decoder info reply for address " + messageaddress + ":";
        //The message is for this throttle.
        int b2 = m.getElement(3) & 0xff;
        int b3 = m.getElement(4) & 0xff;
        int b4 = m.getElement(5) & 0xff;
        int b5 = m.getElement(6) & 0xff;
        int b6 = m.getElement(7) & 0xff;
        int b7 = m.getElement(8) & 0xff;
        // byte 2 contains the speed step mode and availability
        // information.
        // byte 3 contains the direction and the speed information
        text += " " + XNetLocoInfoReplyUtilities.parseSpeedAndDirection(b2, b3);
        // byte 4 contains flags for whether or not the locomotive
        // is in a double header and for smart search.  These aren't used
        // here.

        // byte 4 and 5 contain function information for F0-F12
        text += " " + XNetLocoInfoReplyUtilities.parseFunctionStatus(b4, b5);
        // byte 6 and 7 contain function information for F13-F28
        text += " " + XNetLocoInfoReplyUtilities.parseFunctionHighStatus(b6, b7);
        return text;
    }
}
