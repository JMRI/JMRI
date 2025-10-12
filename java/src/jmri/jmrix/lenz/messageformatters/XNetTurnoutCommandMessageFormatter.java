package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Format Turnout Operations Request messages for display
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetTurnoutCommandMessageFormatter implements XPressNetMessageFormatter {
    public XNetTurnoutCommandMessageFormatter() {
    }

    @Override
    public boolean handlesMessage(jmri.jmrix.Message m) {
        return m instanceof jmri.jmrix.lenz.XNetMessage && ((XNetMessage) m).getElement(0)==XNetConstants.ACC_OPER_REQ;
    }

    @Override
    public String formatMessage(jmri.jmrix.Message m) {
        String messageKey =(((m.getElement(2) & 0x08) == 0x08) ? "XNetMessageAccessoryDecoderOnRequest" : "XNetMessageAccessoryDecoderOffRequest");
        int baseaddress = m.getElement(1);
        int subaddress = ((m.getElement(2) & 0x06) >> 1);
        int address = (baseaddress * 4) + subaddress + 1;
        int output = (m.getElement(2) & 0x01);
        return Bundle.getMessage(messageKey,address, baseaddress,subaddress,output);
    }
}
