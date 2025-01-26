package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Formatter for XNet Search Loco in MU Request Messages
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetSearchMURequestMessageFormatter implements XPressNetMessageFormatter {
    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetMessage &&
                m.getElement(0) == XNetConstants.LOCO_IN_MULTI_UNIT_SEARCH_REQ &&
                (m.getElement(1) == XNetConstants.LOCO_IN_MULTI_UNIT_REQ_FORWARD ||
                m.getElement(1) == XNetConstants.LOCO_IN_MULTI_UNIT_REQ_BACKWARD);

    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m))
            throw new IllegalArgumentException("Message is not supported");
        String key = "XNetMessageSearchCSStackForwardNextMULoco";
        if(m.getElement(1) == XNetConstants.LOCO_IN_MULTI_UNIT_REQ_BACKWARD) {
            key = "XNetMessageSearchCSStackBackwardNextMULoco";
        }
        return Bundle.getMessage(key,
                           m.getElement(2),
                           LenzCommandStation.calcLocoAddress(m.getElement(3), m.getElement(4)));
    }
}
