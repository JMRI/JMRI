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
        XNetReply r = (XNetReply) m;
        return Bundle.getMessage("XNetReplyLocoDHLabel") + "," +
               XNetLocoInfoReplyUtilities.parseSpeedAndDirection(r.getElement(1), r.getElement(2)) + " " +
               XNetLocoInfoReplyUtilities.parseFunctionStatus(r.getElement(3), r.getElement(4)) +
               " " + Bundle.getMessage("XNetReplyLoco2DHLabel") + " " +
               LenzCommandStation.calcLocoAddress(r.getElement(5), r.getElement(6));

    }
}
