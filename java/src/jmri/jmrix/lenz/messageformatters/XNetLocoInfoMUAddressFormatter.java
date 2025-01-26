package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;
/**
 * Format replies for XPressNet Command Station reply for Loco Info MU Address.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLocoInfoMUAddressFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&
                m.getElement(0) == XNetConstants.LOCO_INFO_MU_ADDRESS;
    }

    @Override
    public String formatMessage(Message m) {
        if (!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not supported");
        }
        return Bundle.getMessage("XNetReplyLocoMUBaseLabel") + "," +
            XNetLocoInfoReplyUtilities.parseSpeedAndDirection(m.getElement(1), m.getElement(2)) + " ";

    }

}
