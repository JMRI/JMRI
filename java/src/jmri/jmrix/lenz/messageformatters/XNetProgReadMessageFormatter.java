package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Message Formatter for XPressNet programming read request messages
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetProgReadMessageFormatter implements XPressNetMessageFormatter {

    private static final String X_NET_MESSAGE_REQUEST_SERVICE_MODE_READ_DIRECT_V_36 = "XNetMessageRequestServiceModeReadDirectV36";


    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetMessage &&
            m.getElement(0) == XNetConstants.PROG_READ_REQUEST &&
                (m.getElement(1) == XNetConstants.PROG_READ_MODE_REGISTER ||
             m.getElement(1) == XNetConstants.PROG_READ_MODE_CV ||
             m.getElement(1) == XNetConstants.PROG_READ_MODE_PAGED ||
                        (m.getElement(1) >= XNetConstants.PROG_READ_MODE_CV_V36
                        && m.getElement(1) <= XNetConstants.PROG_READ_MODE_CV_V36 + 3));
    }

    @Override
    public String formatMessage(Message m) {
        switch (m.getElement(1)) {
            case XNetConstants.PROG_READ_MODE_REGISTER:
                return Bundle.getMessage("XNetMessageRequestServiceModeReadRegister", m.getElement(2));
            case XNetConstants.PROG_READ_MODE_CV:
                return Bundle.getMessage("XNetMessageRequestServiceModeReadDirect", m.getElement(2));
            case XNetConstants.PROG_READ_MODE_PAGED:
                return Bundle.getMessage("XNetMessageRequestServiceModeReadPaged", m.getElement(2));
            case XNetConstants.PROG_READ_MODE_CV_V36:
                return Bundle.getMessage(X_NET_MESSAGE_REQUEST_SERVICE_MODE_READ_DIRECT_V_36, (m.getElement(2) == 0 ? 1024 : m.getElement(2)));
            case XNetConstants.PROG_READ_MODE_CV_V36 + 1:
                return Bundle.getMessage(X_NET_MESSAGE_REQUEST_SERVICE_MODE_READ_DIRECT_V_36, (256 + m.getElement(2)));
            case XNetConstants.PROG_READ_MODE_CV_V36 + 2:
                return Bundle.getMessage(X_NET_MESSAGE_REQUEST_SERVICE_MODE_READ_DIRECT_V_36, (512 + m.getElement(2)));
            case XNetConstants.PROG_READ_MODE_CV_V36 + 3:
                return Bundle.getMessage(X_NET_MESSAGE_REQUEST_SERVICE_MODE_READ_DIRECT_V_36, (768 + m.getElement(2)));
            default:
                throw new IllegalArgumentException("Unknown Programming Read Message");
        }
    }
}
