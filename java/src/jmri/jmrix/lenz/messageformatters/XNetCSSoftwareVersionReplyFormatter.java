package jmri.jmrix.lenz.messageformatters;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Format XPressNet V3 messages for CS Software Version Reply for display.
 *
 * @author Paul Bender Copyright (C) 2024
 */

public class XNetCSSoftwareVersionReplyFormatter implements XPressNetMessageFormatter {
    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&
                ((XNetReply) m).getElement(0)== XNetConstants.CS_SERVICE_MODE_RESPONSE &&
                ((XNetReply) m).getElement(1)== XNetConstants.CS_SOFTWARE_VERSION;
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "cast is checked in handlesMessage")
    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not a V3 CS Software Version Reply");
        }
        XNetReply r = (XNetReply) m;
        String typeString;
        switch (r.getElement(3)) {
            case 0x00:
                typeString = Bundle.getMessage("CSTypeLZ100");
                break;
            case 0x01:
                typeString = Bundle.getMessage("CSTypeLH200");
                break;
            case 0x02:
                typeString = Bundle.getMessage("CSTypeCompact");
                break;
            // GT 2007/11/6 - Added multiMaus
            case 0x10:
                typeString = Bundle.getMessage("CSTypeMultiMaus");
                break;
            default:
                typeString = "" + r.getElement(3);
        }
        return Bundle.getMessage("XNetReplyCSVersion", (r.getElementBCD(2).floatValue()) / 10, typeString);
    }
}
