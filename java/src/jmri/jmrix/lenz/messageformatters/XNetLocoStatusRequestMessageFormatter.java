package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Message Formatter for XPressNet Locomotive information request messages
 * @author Paul Bender Copyright (C) 2024
 */

public class XNetLocoStatusRequestMessageFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetMessage &&
               m.getElement(0) == XNetConstants.LOCO_STATUS_REQ ;
    }

    @Override
    public String formatMessage(Message m) {
        switch (m.getElement(1)) {
            case XNetConstants.LOCO_INFO_REQ_FUNC:
                return Bundle.getMessage("XNetMessageRequestLocoFunctionMomStatus", LenzCommandStation.calcLocoAddress(m.getElement(2), m.getElement(3)));
            case XNetConstants.LOCO_INFO_REQ_FUNC_HI_ON:
                return Bundle.getMessage("XNetMessageRequestLocoFunctionHighStatus", LenzCommandStation.calcLocoAddress(m.getElement(2), m.getElement(3)));
            case XNetConstants.LOCO_INFO_REQ_FUNC_HI_MOM:
                return Bundle.getMessage("XNetMessageRequestLocoFunctionHighMomStatus", LenzCommandStation.calcLocoAddress(m.getElement(2), m.getElement(3)));
            case XNetConstants.LOCO_INFO_REQ_V3:
                return Bundle.getMessage("XNetMessageRequestLocoInfo", LenzCommandStation.calcLocoAddress(m.getElement(2), m.getElement(3)));
            case XNetConstants.LOCO_STACK_SEARCH_FWD:
                return Bundle.getMessage("XNetMessageSearchCSStackForward", LenzCommandStation.calcLocoAddress(m.getElement(2), m.getElement(3)));
            case XNetConstants.LOCO_STACK_SEARCH_BKWD:
                return Bundle.getMessage("XNetMessageSearchCSStackBackward", LenzCommandStation.calcLocoAddress(m.getElement(2), m.getElement(3)));
            case XNetConstants.LOCO_STACK_DELETE:
                return Bundle.getMessage("XNetMessageDeleteAddressOnStack", LenzCommandStation.calcLocoAddress(m.getElement(2), m.getElement(3)));
            default:
                throw new IllegalArgumentException("Unknown Locomotive Status Request Message");
        }
    }

}
