package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Format Command Station Request messages for display
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetCommandStationRequestFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(jmri.jmrix.Message m) {
        return m instanceof jmri.jmrix.lenz.XNetMessage &&
                ((jmri.jmrix.lenz.XNetMessage) m).getElement(0) == XNetConstants.CS_REQUEST;
    }

    @Override
    public String formatMessage(jmri.jmrix.Message m) {
        switch (m.getElement(1)) {
            case XNetConstants.EMERGENCY_OFF:
                return Bundle.getMessage("XNetMessageRequestEmergencyOff");
            case XNetConstants.RESUME_OPS:
                return Bundle.getMessage("XNetMessageRequestNormalOps");
            case XNetConstants.SERVICE_MODE_CSRESULT:
                return Bundle.getMessage("XNetMessageRequestServiceModeResult");
            case XNetConstants.OPS_MODE_CSRESULT:
                return Bundle.getMessage("XNetMessageRequestOpsModeResult");
            case XNetConstants.CS_VERSION:
                return Bundle.getMessage("XNetMessageRequestCSVersion");
            case XNetConstants.CS_STATUS:
                return Bundle.getMessage("XNetMessageRequestCSStatus");
            default:
                throw new IllegalArgumentException("Unknown Command Station Request: " + m.getElement(1));
        }
    }
}
