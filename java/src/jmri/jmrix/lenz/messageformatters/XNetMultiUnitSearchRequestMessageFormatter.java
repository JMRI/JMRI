package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Formatter for XPressNet Multi Unit Search requests
 * @author Paul Bender Copyright (C) 2024
 */

public class XNetMultiUnitSearchRequestMessageFormatter implements XPressNetMessageFormatter {
    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetMessage &&
                (m.getElement(0) == XNetConstants.CS_MULTI_UNIT_REQ);
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m))
            throw new IllegalArgumentException("Invalid Multi Unit Search Request");
        if (m.getElement(1) == XNetConstants.CS_MULTI_UNIT_REQ_FWD){
            return Bundle.getMessage("XNetMessageSearchCSStackForwardConsistAddress",
                    m.getElement(2));
        } else if(m.getElement(1) == XNetConstants.CS_MULTI_UNIT_REQ_BKWD) {
            return Bundle.getMessage("XNetMessageSearchCSStackBackwardConsistAddress", m.getElement(2));
        }
        throw new IllegalArgumentException("Invalid Multi Unit Search Request");
    }

}
