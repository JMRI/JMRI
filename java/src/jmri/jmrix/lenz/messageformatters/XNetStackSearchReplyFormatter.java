package jmri.jmrix.lenz.messageformatters;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Format replies for XPressNet Command Station Stack Search Results.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetStackSearchReplyFormatter implements XPressNetMessageFormatter {


    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply &&
                m.getElement(0) == XNetConstants.LOCO_INFO_RESPONSE &&
                (m.getElement(1) == XNetConstants.LOCO_SEARCH_NO_RESULT ||
                        m.getElement(1) == XNetConstants.LOCO_SEARCH_RESPONSE_DH ||
                        m.getElement(1) == XNetConstants.LOCO_SEARCH_RESPONSE_MU ||
                        m.getElement(1) == XNetConstants.LOCO_SEARCH_RESPONSE_MU_BASE ||
                        m.getElement(1) == XNetConstants.LOCO_SEARCH_RESPONSE_N);
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "cast is checked in handlesMessage")
    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)){
            throw new IllegalArgumentException("Message not supported by this formatter");
        }
        XNetReply r = (XNetReply) m;
        StringBuilder text = new StringBuilder(Bundle.getMessage("XNetReplyLocoLabel") + " ");
        switch (r.getElement(1)) {
            case XNetConstants.LOCO_SEARCH_RESPONSE_N:
                text.append(Bundle.getMessage("XNetReplySearchNormalLabel")).append(" ");
                text.append(r.getThrottleMsgAddr());
                break;
            case XNetConstants.LOCO_SEARCH_RESPONSE_DH:
                text.append(Bundle.getMessage("XNetReplySearchDHLabel")).append(" ");
                text.append(r.getThrottleMsgAddr());
                break;
            case XNetConstants.LOCO_SEARCH_RESPONSE_MU_BASE:
                text.append(Bundle.getMessage("XNetReplySearchMUBaseLabel")).append(" ");
                text.append(r.getThrottleMsgAddr());
                break;
            case XNetConstants.LOCO_SEARCH_RESPONSE_MU:
                text.append(Bundle.getMessage("XNetReplySearchMULabel")).append(" ");
                text.append(r.getThrottleMsgAddr());
                break;
            case XNetConstants.LOCO_SEARCH_NO_RESULT:
                text.append(Bundle.getMessage("XNetReplySearchFailedLabel")).append(" ");
                text.append(r.getThrottleMsgAddr());
                break;
            default:
                // Should never happen
                throw new IllegalArgumentException("Message not supported by this formatter");
        }
        return text.toString();
    }

}
