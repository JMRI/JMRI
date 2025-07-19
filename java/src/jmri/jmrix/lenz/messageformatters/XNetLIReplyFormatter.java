package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XPressNetMessageFormatter;

/**
 * Format replies for XPressNet replies from the Computer Interface.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLIReplyFormatter implements XPressNetMessageFormatter {

    @Override
    public boolean handlesMessage(Message m) {
        return m instanceof XNetReply && m.getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER;
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not supported");
        }
        switch(m.getElement(1)) {
            case XNetConstants.LI_MESSAGE_RESPONSE_PC_DATA_ERROR:
                return Bundle.getMessage("XNetReplyErrorPCtoLI");
            case XNetConstants.LI_MESSAGE_RESPONSE_CS_DATA_ERROR:
                return Bundle.getMessage("XNetReplyErrorLItoCS");
            case XNetConstants.LI_MESSAGE_RESPONSE_UNKNOWN_DATA_ERROR:
                return Bundle.getMessage("XNetReplyErrorUnknown");
            case XNetConstants.LI_MESSAGE_RESPONSE_SEND_SUCCESS:
                return Bundle.getMessage("XNetReplyOkMessage");
            case XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR:
                return Bundle.getMessage("XNetReplyErrorNoTimeSlot");
            case XNetConstants.LI_MESSAGE_RESPONSE_BUFFER_OVERFLOW:
                return Bundle.getMessage("XNetReplyErrorBufferOverflow");
            case XNetConstants.LIUSB_TIMESLOT_RESTORED:
                return Bundle.getMessage("XNetReplyTimeSlotRestored");
            case XNetConstants.LIUSB_REQUEST_SENT_WHILE_NO_TIMESLOT:
                return Bundle.getMessage("XNetReplyRequestSentWhileNoTimeslot");
            case XNetConstants.LIUSB_BAD_DATA_IN_REQUEST:
                return Bundle.getMessage("XNetReplyBadDataInRequest");
            case XNetConstants.LIUSB_RETRANSMIT_REQUEST:
                return Bundle.getMessage("XNetReplyRetransmitRequest");
            default:
                return m.toString();
        }
    }

}
