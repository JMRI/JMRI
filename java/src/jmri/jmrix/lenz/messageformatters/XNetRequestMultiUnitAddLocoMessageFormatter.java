package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Format requests to add a locomotive to a MulitUnit consist
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetRequestMultiUnitAddLocoMessageFormatter implements XPressNetMessageFormatter {
    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetMessage &&
                m.getElement(0) == XNetConstants.LOCO_OPER_REQ &&
                (m.getElement(1) == XNetConstants.LOCO_ADD_MULTI_UNIT_REQ ||
                        m.getElement(1) == (XNetConstants.LOCO_ADD_MULTI_UNIT_REQ | 0x01) );
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException( "XNetRequestMultiUnitAddLocoMessageFormatter: message type not supported");
        }
        return Bundle.getMessage(m.getElement(1) == XNetConstants.LOCO_ADD_MULTI_UNIT_REQ ? "XNetMessageAddToConsistDirNormalRequest":"XNetMessageAddToConsistDirReverseRequest",
                           LenzCommandStation.calcLocoAddress(m.getElement(2), m.getElement(3)),
                           m.getElement(4));
    }
}
