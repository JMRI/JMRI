package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Formatter for XPressNet XNet Double Header and Multiple Unit Error replies.
 *
 * @author Paul Bender Copyright (C) 2025
 */

public class XNetDHandMUErrorMessageFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply && ((XNetReply) m).getElement(0) == XNetConstants.LOCO_MU_DH_ERROR;
    }

    @Override
    public String formatMessage(Message m) {
        if (!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not supported");
        }
        switch (m.getElement(1)) {
            case 0x81:
                return Bundle.getMessage("XNetReplyDHErrorNotOperated");
            case 0x82:
                return Bundle.getMessage("XNetReplyDHErrorInUse");
            case 0x83:
                return Bundle.getMessage("XNetReplyDHErrorAlreadyDH");
            case 0x84:
                return Bundle.getMessage("XNetReplyDHErrorNonZeroSpeed");
            case 0x85:
                return Bundle.getMessage("XNetReplyDHErrorLocoNotMU");
            case 0x86:
                return Bundle.getMessage("XNetReplyDHErrorLocoNotMUBase");
            case 0x87:
                return Bundle.getMessage("XNetReplyDHErrorCanNotDelete");
            case 0x88:
                return Bundle.getMessage("XNetReplyDHErrorStackFull");
            default:
                return Bundle.getMessage("XNetReplyDHErrorOther", (m.getElement(1) - 0x80));
        }
    }

}
