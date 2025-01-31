package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Format Ops Mode Request messages for display
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetOpsModeRequestMessageFormatter implements XPressNetMessageFormatter{
    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetMessage &&
                m.getElement(0)  == XNetConstants.OPS_MODE_PROG_REQ;
    }

    @Override
    public String formatMessage(Message m) {
            if(m.getElement(1)==   XNetConstants.OPS_MODE_PROG_WRITE_REQ){
                if ((m.getElement(4) & 0xEC) == 0xEC || (m.getElement(4) & 0xE4) == 0xE4) {
                    return getWriteString(m);
                } else if ((m.getElement(4) & 0xE8) == 0xE8) {
                    return getVerifyString(m);
                }
            }
        throw new IllegalArgumentException("Unknown Ops Mode Request Type");
    }

    private static String getVerifyString(Message m) {
        String message;
        if ((m.getElement(6) & 0x10) == 0x10) {
            message = "XNetMessageOpsModeBitVerify";
        } else {
            message = "XNetMessageOpsModeBitWrite";
        }
        return Bundle.getMessage(message, ((m.getElement(6) & 0x08) >> 3), (1 + m.getElement(5) + ((m.getElement(4) & 0x03) << 8)), (m.getElement(6) & 0x07), LenzCommandStation.calcLocoAddress(m.getElement(2), m.getElement(3)));
    }

    private static String getWriteString(Message m) {
        String message = "";
        if ((m.getElement(4) & 0xEC) == 0xEC) {
            message = "XNetMessageOpsModeByteWrite";
        } else if ((m.getElement(4) & 0xE4) == 0xE4) {
            message = "XNetMessageOpsModeByteVerify";
        }
        return Bundle.getMessage(message, m.getElement(6), (1 + m.getElement(5) + ((m.getElement(4) & 0x03) << 8)), LenzCommandStation.calcLocoAddress(m.getElement(2), m.getElement(3)));
    }

}
