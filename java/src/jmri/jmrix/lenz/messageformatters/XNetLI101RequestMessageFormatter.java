package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Formatter for XPressNet messages targeted at the Interface device.
 * @author Paul Bender copyright (C) 2024
 */
public class XNetLI101RequestMessageFormatter implements XPressNetMessageFormatter {

    private static final String X_NET_MESSAGE_REQUEST_LI_BAUD = "XNetMessageRequestLIBaud";

    @Override
    public boolean handlesMessage(jmri.jmrix.Message m) {
        return m instanceof jmri.jmrix.lenz.XNetMessage && ((jmri.jmrix.lenz.XNetMessage) m).getElement(0) == XNetConstants.LI101_REQUEST;
    }

    @Override
    public String formatMessage(jmri.jmrix.Message m) {
        if(m.getElement(1) == XNetConstants.LI101_REQUEST_ADDRESS) {
            return Bundle.getMessage("XNetMessageRequestLIAddress", m.getElement(2));
        } else if(m.getElement(1) == XNetConstants.LI101_REQUEST_BAUD) {
            switch (m.getElement(2)) {
                case 1:
                    return Bundle.getMessage(X_NET_MESSAGE_REQUEST_LI_BAUD, Bundle.getMessage("LIBaud19200"));
                case 2:
                    return Bundle.getMessage(X_NET_MESSAGE_REQUEST_LI_BAUD, Bundle.getMessage("Baud38400"));
                case 3:
                    return Bundle.getMessage(X_NET_MESSAGE_REQUEST_LI_BAUD, Bundle.getMessage("Baud57600"));
                case 4:
                    return Bundle.getMessage(X_NET_MESSAGE_REQUEST_LI_BAUD, Bundle.getMessage("Baud115200"));
                default:
                    return Bundle.getMessage(X_NET_MESSAGE_REQUEST_LI_BAUD, Bundle.getMessage("BaudOther"));
            }
        }
        throw new IllegalArgumentException("Unknown LI101 Request Type");
    }
}
