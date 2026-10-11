package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XPressNetMessageFormatter;
import jmri.jmrix.lenz.XNetConstants;

/**
 * Format Emergency Stop All Request messages for display
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetEstopAllRequestMessageFormatter implements XPressNetMessageFormatter {
    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetMessage &&
                m.getElement(0) == XNetConstants.ALL_ESTOP;
    }

    @Override
    public String formatMessage(Message m) {
        return Bundle.getMessage("XNetMessageRequestEmergencyStop");
    }
}
