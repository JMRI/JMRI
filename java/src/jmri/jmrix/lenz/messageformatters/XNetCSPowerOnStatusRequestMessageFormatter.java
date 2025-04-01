package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

public class XNetCSPowerOnStatusRequestMessageFormatter implements XPressNetMessageFormatter {
    @Override
    public boolean handlesMessage(jmri.jmrix.Message m) {
        return m instanceof jmri.jmrix.lenz.XNetMessage &&
                ((jmri.jmrix.lenz.XNetMessage) m).getElement(0) == XNetConstants.CS_SET_POWERMODE &&
                ((jmri.jmrix.lenz.XNetMessage) m).getElement(1) == XNetConstants.CS_SET_POWERMODE;
    }

    @Override
    public String formatMessage(jmri.jmrix.Message m) {
        if (m.getElement(0) == XNetConstants.CS_SET_POWERMODE
                && m.getElement(1) == XNetConstants.CS_SET_POWERMODE
                && m.getElement(2) == XNetConstants.CS_POWERMODE_AUTO) {
            return Bundle.getMessage("XNetMessageRequestCSPowerModeAuto");
        } else if (m.getElement(0) == XNetConstants.CS_SET_POWERMODE
                && m.getElement(1) == XNetConstants.CS_SET_POWERMODE
                && m.getElement(2) == XNetConstants.CS_POWERMODE_MANUAL) {
            return Bundle.getMessage("XNetMessageRequestCSPowerModeManual");
        }
        throw new IllegalArgumentException("Unknown CS Power Mode Request Type");
    }
}
