package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Format replies for XPressNet Command Station reply for Loco Info DH Unit.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLocoInfoDHUnitFormatter implements XPressNetMessageFormatter {
    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&
                m.getElement(0) == XNetConstants.LOCO_INFO_DH_UNIT;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not supported");
        }
        return Bundle.getMessage("XNetReplyLocoDHLabel") + "," +
               XNetLocoInfoReplyUtilities.parseSpeedAndDirection(m.getElement(1), m.getElement(2)) + " " +
               XNetLocoInfoReplyUtilities.parseFunctionStatus(m.getElement(3), m.getElement(4)) +
               " " + Bundle.getMessage("XNetReplyLoco2DHLabel") + " " +
               LenzCommandStation.calcLocoAddress(m.getElement(5), m.getElement(6));

    }
}
