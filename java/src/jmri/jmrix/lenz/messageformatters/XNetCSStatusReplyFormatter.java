package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Format replies for XPressNet Command Station reply for Status.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetCSStatusReplyFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&
                m.getElement(0) == XNetConstants.CS_REQUEST_RESPONSE &&
                m.getElement(1) == XNetConstants.CS_STATUS_RESPONSE; 
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) { 
            throw new IllegalArgumentException("Message is not supported");
        }
        StringBuilder text = new StringBuilder(Bundle.getMessage("XNetReplyCSStatus") + " ");
        int statusByte = m.getElement(2);
        if ((statusByte & 0x01) == 0x01) {
            // Command station is in Emergency Off Mode
            text.append(Bundle.getMessage("XNetCSStatusEmergencyOff")).append("; ");
        }
            if ((statusByte & 0x02) == 0x02) {
                // Command station is in Emergency Stop Mode
                text.append(Bundle.getMessage("XNetCSStatusEmergencyStop")).append("; ");
            }
            if ((statusByte & 0x08) == 0x08) {
                // Command station is in Service Mode
                text.append(Bundle.getMessage("XNetCSStatusServiceMode")).append("; ");
            }
            if ((statusByte & 0x40) == 0x40) {
                // Command station is in Power Up Mode
                text.append(Bundle.getMessage("XNetCSStatusPoweringUp")).append("; ");
            }
            if ((statusByte & 0x04) == 0x04) {
                text.append(Bundle.getMessage("XNetCSStatusPowerModeAuto")).append("; ");
            } else {
                text.append(Bundle.getMessage("XNetCSStatusPowerModeManual")).append("; ");
            }
            if ((statusByte & 0x80) == 0x80) {
                // Command station has a experienced a ram check error
                text.append(Bundle.getMessage("XNetCSStatusRamCheck"));
            }
            return text.toString();
    }

}
