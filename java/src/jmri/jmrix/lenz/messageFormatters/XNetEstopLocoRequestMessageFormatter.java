package jmri.jmrix.lenz.messageFormatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetConstants;

/**
 * Format Emergency Stop Loco Request messages for display
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetEstopLocoRequestMessageFormatter {
    public Boolean handlesMessage(Message m) {
        return m instanceof XNetMessage &&
                m.getElement(0) == XNetConstants.EMERGENCY_STOP;
    }

    public String formatMessage(Message m) {
        return Bundle.getMessage("XNetMessageAddressedEmergencyStopRequest",
                LenzCommandStation.calcLocoAddress(m.getElement(1), m.getElement(2)));
    }
}
