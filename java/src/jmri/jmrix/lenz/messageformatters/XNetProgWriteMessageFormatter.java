package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XPressNetMessageFormatter;
import jmri.jmrix.lenz.XNetConstants;

/**
 * Message Formatter for XPressNet programming request messages
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetProgWriteMessageFormatter implements XPressNetMessageFormatter {
    private static final String X_NET_MESSAGE_REQUEST_SERVICE_MODE_WRITE_DIRECT_V_36 = "XNetMessageRequestServiceModeWriteDirectV36";

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetMessage &&
           m.getElement(0) == XNetConstants.PROG_WRITE_REQUEST;
    }

    @Override
    public String formatMessage(Message m) {
        switch (m.getElement(1)) {
            case XNetConstants.PROG_WRITE_MODE_REGISTER:
                return Bundle.getMessage("XNetMessageRequestServiceModeWriteRegister", m.getElement(2), m.getElement(3));
            case XNetConstants.PROG_WRITE_MODE_CV:
                return Bundle.getMessage("XNetMessageRequestServiceModeWriteDirect", m.getElement(2), m.getElement(3));
            case XNetConstants.PROG_WRITE_MODE_PAGED:
                return Bundle.getMessage("XNetMessageRequestServiceModeWritePaged", m.getElement(2), m.getElement(3));
            case XNetConstants.PROG_WRITE_MODE_CV_V36:
                return Bundle.getMessage(X_NET_MESSAGE_REQUEST_SERVICE_MODE_WRITE_DIRECT_V_36, (m.getElement(2) == 0 ? 1024 : m.getElement(2)), m.getElement(3));
            case (XNetConstants.PROG_WRITE_MODE_CV_V36 + 1):
                return Bundle.getMessage(X_NET_MESSAGE_REQUEST_SERVICE_MODE_WRITE_DIRECT_V_36, (256 + m.getElement(2)), m.getElement(3));
            case (XNetConstants.PROG_WRITE_MODE_CV_V36 + 2):
                return Bundle.getMessage(X_NET_MESSAGE_REQUEST_SERVICE_MODE_WRITE_DIRECT_V_36, (512 + m.getElement(2)), m.getElement(3));
            case (XNetConstants.PROG_WRITE_MODE_CV_V36 + 3):
                return Bundle.getMessage(X_NET_MESSAGE_REQUEST_SERVICE_MODE_WRITE_DIRECT_V_36, (768 + m.getElement(2)), m.getElement(3));
            default:
                throw new IllegalArgumentException("Unknown Programming Write Message");
        }
    }
}
