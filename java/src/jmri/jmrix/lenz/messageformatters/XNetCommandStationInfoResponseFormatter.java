package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Formatter for XNet CS Info Response messages.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetCommandStationInfoResponseFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply && ((XNetReply) m).getElement(0) == XNetConstants.CS_INFO;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)){
            throw new IllegalArgumentException("Message is not a XNetReply or is not a CS Info message");
        }
        //XNetReply r = (XNetReply) m;
        switch (m.getElement(1)) {
            case XNetConstants.BC_NORMAL_OPERATIONS:
                return Bundle.getMessage("XNetReplyBCNormalOpsResumed");
            case XNetConstants.BC_EVERYTHING_OFF:
                return Bundle.getMessage("XNetReplyBCEverythingOff");
            case XNetConstants.BC_SERVICE_MODE_ENTRY:
                return Bundle.getMessage("XNetReplyBCServiceEntry");
            case XNetConstants.PROG_SHORT_CIRCUIT:
                return Bundle.getMessage("XNetReplyServiceModeShort");
            case XNetConstants.PROG_BYTE_NOT_FOUND:
                return Bundle.getMessage("XNetReplyServiceModeDataByteNotFound");
            case XNetConstants.PROG_CS_BUSY:
                return Bundle.getMessage("XNetReplyServiceModeCSBusy");
            case XNetConstants.PROG_CS_READY:
                return Bundle.getMessage("XNetReplyServiceModeCSReady");
            case XNetConstants.CS_BUSY:
                return Bundle.getMessage("XNetReplyCSBusy");
            case XNetConstants.CS_NOT_SUPPORTED:
                return Bundle.getMessage("XNetReplyCSNotSupported");
            case XNetConstants.CS_TRANSFER_ERROR:
                return Bundle.getMessage("XNetReplyCSTransferError");
            /* The remaining cases are for a Double Header or MU Error */
            case XNetConstants.CS_DH_ERROR_NON_OP:
                return Bundle.getMessage("XNetReplyV1DHErrorNotOperated");
            case XNetConstants.CS_DH_ERROR_IN_USE:
                return Bundle.getMessage("XNetReplyV1DHErrorInUse");
            case XNetConstants.CS_DH_ERROR_ALREADY_DH:
                return Bundle.getMessage("XNetReplyV1DHErrorAlreadyDH");
            case XNetConstants.CS_DH_ERROR_NONZERO_SPD:
                return Bundle.getMessage("XNetReplyV1DHErrorNonZeroSpeed");
            default:
                return m.toString();
        }
    }

}
