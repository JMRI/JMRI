package jmri.jmrix.lenz;

/**
 * Represents a single response from the XpressNet.
 *
 * @author Paul Bender Copyright (C) 2004
 *
 */
public class XNetReply extends jmri.jmrix.AbstractMRReply {

    private boolean reallyUnsolicited = true;  // used to override automatic
    // unsolicited by message type.

    // Create a new reply.
    public XNetReply() {
        super();
        setBinary(true);
    }

    // Create a new reply from an existing reply
    public XNetReply(XNetReply reply) {
        super(reply);
        setBinary(true);
    }

    /**
     * Create a reply from an XNetMessage.
     */
    public XNetReply(XNetMessage message) {
        super();
        setBinary(true);
        for (int i = 0; i < message.getNumDataElements(); i++) {
            setElement(i, message.getElement(i));
        }
    }

    /**
     * Create a reply from a string of hex characters.
     */
    public XNetReply(String message) {
        super();
        setBinary(true);
        // gather bytes in result
        byte b[] = jmri.util.StringUtil.bytesFromHexString(message);
        if (b.length == 0) {
            // no such thing as a zero-length message
            _nDataChars = 0;
            _dataChars = null;
            return;
        }
        _nDataChars = b.length;
        _dataChars = new int[_nDataChars];
        for (int i = 0; i < b.length; i++) {
            setElement(i, ( b[i] & 0xff) );
        }
    }

    /**
     * Get the opcode as a string in hex format.
     */
    public String getOpCodeHex() {
        return "0x" + Integer.toHexString(this.getOpCode());
    }

    /**
     * Check whether the message has a valid parity.
     */
    public boolean checkParity() {
        int len = getNumDataElements();
        int chksum = 0x00;  /* the seed */

        int loop;

        for (loop = 0; loop < len - 1; loop++) {  // calculate contents for data part
            chksum ^= getElement(loop);
        }
        return ((chksum & 0xFF) == getElement(len - 1));
    }

    public void setParity() {
        int len = getNumDataElements();
        int chksum = 0x00;  /* the seed */

        int loop;

        for (loop = 0; loop < len - 1; loop++) {  // calculate contents for data part
            chksum ^= getElement(loop);
        }
        setElement(len - 1, chksum & 0xFF);
    }

    /**
     * Get an integer representation of a BCD value.
     *
     * @param n byte in message to convert
     * @return Integer value of BCD byte.
     */
    public Integer getElementBCD(int n) {
        return Integer.decode(Integer.toHexString(getElement(n)));
    }

    /**
     * skipPrefix is not used at this point in time, but is 
     * defined as abstract in AbstractMRReply
     */
    @Override
    protected int skipPrefix(int index) {
        return -1;
    }

    // decode messages of a particular form

    /* 
     * The next group of routines are used by Feedback and/or turnout 
     * control code.  These are used in multiple places within the code, 
     * so they appear here. 
     */

    /**
     * If this is a feedback response message for a turnout, return the address.
     * Otherwise return -1.
     *
     * @return the integer address or -1 if not a turnout message
     */
    public int getTurnoutMsgAddr() {
        if (this.isFeedbackMessage()) {
            int a1 = this.getElement(1);
            int a2 = this.getElement(2);
            int messagetype = this.getFeedbackMessageType();
            if (messagetype == 0 || messagetype == 1) {
                // This is a turnout message
                int address = (a1 & 0xff) * 4;
                if (((a2 & 0x13) == 0x01) || ((a2 & 0x13) == 0x02)) {
                    // This is the first address in the group*/
                    return (address + 1);
                } else if (((a2 & 0x1c) == 0x04) || ((a2 & 0x1c) == 0x08)) {
                    // This is the second address in the group
                    // return the odd address associated with this turnout
                    return (address + 1);
                } else if (((a2 & 0x13) == 0x11) || ((a2 & 0x13) == 0x12)) {
                    // This is the third address in the group
                    return (address + 3);
                } else if (((a2 & 0x1c) == 0x14) || ((a2 & 0x1c) == 0x18)) {
                    // This is the fourth address in the group
                    // return the odd address associated with this turnout
                    return (address + 3);
                } else if ((a2 & 0x1f) == 0x10) {
                    // This is an address in the upper nibble, but neither 
                    // address has been operated.
                    return (address + 3);
                } else {
                    // This is an address in the lower nibble, but neither 
                    // address has been operated
                    return (address + 1);
                }
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * If this is a feedback broadcast message and the specified startbyte is
     * the address byte of an addres byte data byte pair for a turnout, return
     * the address. Otherwise return -1.
     *
     * @param startByte address byte of the address byte/data byte pair.
     * @return the integer address or -1 if not a turnout message
     */
    public int getTurnoutMsgAddr(int startByte) {
        if (this.isFeedbackBroadcastMessage()) {
            int a1 = this.getElement(startByte);
            int a2 = this.getElement(startByte + 1);
            int messagetype = this.getFeedbackMessageType();
            if (messagetype == 0 || messagetype == 1) {
                // This is a turnout message
                int address = (a1 & 0xff) * 4;
                if (((a2 & 0x13) == 0x01) || ((a2 & 0x13) == 0x02)) {
                    // This is the first address in the group*/
                    return (address + 1);
                } else if (((a2 & 0x1c) == 0x04) || ((a2 & 0x1c) == 0x08)) {
                    // This is the second address in the group
                    // return the odd address associated with this turnout
                    return (address + 1);
                } else if (((a2 & 0x13) == 0x11) || ((a2 & 0x13) == 0x12)) {
                    // This is the third address in the group
                    return (address + 3);
                } else if (((a2 & 0x1c) == 0x14) || ((a2 & 0x1c) == 0x18)) {
                    // This is the fourth address in the group
                    // return the odd address associated with this turnout
                    return (address + 3);
                } else if ((a2 & 0x1f) == 0x10) {
                    // This is an address in the upper nibble, but neither 
                    // address has been operated.
                    return (address + 3);
                } else {
                    // This is an address in the lower nibble, but neither 
                    // address has been operated
                    return (address + 1);
                }
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * Parse the feedback message for a turnout, and return the status for the
     * even or odd half of the nibble (upper or lower part).
     *
     * @param turnout <ul>
     * <li>0 for the even turnout associated with the pair. This is the upper
     * half of the data nibble asociated with the pair </li>
     * <li>1 for the odd turnout associated with the pair. This is the lower
     * half of the data nibble asociated with the pair </li>
     * </ul>
     * @return THROWN/CLOSED as defined in {@link jmri.Turnout}
     */
    public int getTurnoutStatus(int turnout) {
        if (this.isFeedbackMessage()) {
            //int a1 = this.getElement(1);
            int a2 = this.getElement(2);
            int messagetype = this.getFeedbackMessageType();
            if (messagetype == 0 || messagetype == 1) {
                if (turnout == 1) {
                    // we want the lower half of the nibble
                    if ((a2 & 0x03) != 0) {
                        /* this is for the First turnout in the nibble */
                        int state = this.getElement(2) & 0x03;
                        if (state == 0x01) {
                            return (jmri.Turnout.CLOSED);
                        } else if (state == 0x02) {
                            return (jmri.Turnout.THROWN);
                        } else {
                            return -1; /* the state is invalid */

                        }
                    }
                } else if (turnout == 0) {
                    /* we want the upper half of the nibble */
                    if ((a2 & 0x0C) != 0) {
                        /* this is for the upper half of the nibble */
                        int state = this.getElement(2) & 0x0C;
                        if (state == 0x04) {
                            return (jmri.Turnout.CLOSED);
                        } else if (state == 0x08) {
                            return (jmri.Turnout.THROWN);
                        } else {
                            return -1; /* the state is invalid */

                        }
                    }
                }
            }
        }
        return (-1);
    }

    /**
     * Parse the specified address byte/data byte pair in a feedback broadcast
     * message and see if it is for a turnout. If it is, return the status for
     * the even or odd half of the nibble (upper or lower part)
     *
     * @param startByte address byte of the address byte/data byte pair.
     * @param turnout   <ul>
     * <li>0 for the even turnout associated with the pair. This is the upper
     * half of the data nibble asociated with the pair </li>
     * <li>1 for the odd turnout associated with the pair. This is the lower
     * half of the data nibble asociated with the pair </li>
     * </ul>
     * @return THROWN/CLOSED as defined in {@link jmri.Turnout}
     */
    public int getTurnoutStatus(int startByte, int turnout) {
        if (this.isFeedbackBroadcastMessage()) {
            //int a1 = this.getElement(startByte);
            int a2 = this.getElement(startByte + 1);
            int messagetype = this.getFeedbackMessageType();
            if (messagetype == 0 || messagetype == 1) {
                if (turnout == 1) {
                    // we want the lower half of the nibble
                    if ((a2 & 0x03) != 0) {
                        /* this is for the First turnout in the nibble */
                        int state = this.getElement(2) & 0x03;
                        if (state == 0x01) {
                            return (jmri.Turnout.CLOSED);
                        } else if (state == 0x02) {
                            return (jmri.Turnout.THROWN);
                        } else {
                            return -1; /* the state is invalid */

                        }
                    }
                } else if (turnout == 0) {
                    /* we want the upper half of the nibble */
                    if ((a2 & 0x0C) != 0) {
                        /* this is for the upper half of the nibble */
                        int state = this.getElement(2) & 0x0C;
                        if (state == 0x04) {
                            return (jmri.Turnout.CLOSED);
                        } else if (state == 0x08) {
                            return (jmri.Turnout.THROWN);
                        } else {
                            return -1; /* the state is invalid */

                        }
                    }
                }
            }
        }
        return (-1);
    }

    /**
     * If this is a feedback response message for a feedback encoder, return the
     * address. Otherwise return -1.
     *
     * @return the integer address or -1 if not a feedback message
     */
    public int getFeedbackEncoderMsgAddr() {
        if (this.isFeedbackMessage()) {
            int a1 = this.getElement(1);
            int messagetype = this.getFeedbackMessageType();
            if (messagetype == 2) {
                // This is a feedback encoder message
                int address = (a1 & 0xff);
                return (address);
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * If this is a feedback broadcast message and the specified startByte is
     * the address byte of an address byte/data byte pair for a feedback
     * encoder, return the address. Otherwise return -1.
     *
     * @param startByte address byte of the address byte data byte pair.
     * @return the integer address or -1 if not a feedback message
     */
    public int getFeedbackEncoderMsgAddr(int startByte) {
        if (this.isFeedbackBroadcastMessage()) {
            int a1 = this.getElement(startByte);
            int messagetype = this.getFeedbackMessageType(startByte);
            if (messagetype == 2) {
                // This is a feedback encoder message
                int address = (a1 & 0xff);
                return (address);
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * Is this a feedback response message?
     */
    public boolean isFeedbackMessage() {
        return (this.getElement(0) == XNetConstants.ACC_INFO_RESPONSE);
    }

    /**
     * Is this a feedback broadcast message?
     */
    public boolean isFeedbackBroadcastMessage() {
        return ((this.getElement(0) & 0xF0) == XNetConstants.BC_FEEDBACK);
    }

    /**
     * Extract the feedback message type from a feedback message this is the
     * middle two bits of the upper byte of the second data byte.
     *
     * @return message type, values are:
     * <ul>
     * <li>0 for a turnout with no feedback</li>
     * <li>1 for a turnout with feedback</li>
     * <li>2 for a feedback encoder</li>
     * <li>3 is reserved by Lenz for future use.</li>
     * </ul>
     */
    public int getFeedbackMessageType() {
        if (this.isFeedbackMessage()) {
            int a2 = this.getElement(2);
            return ((a2 & 0x60) / 32);
        } else {
            return -1;
        }
    }

    /**
     * Extract the feedback message type from the data byte of associated with
     * the specified address byte specified by startByte.
     * <p>
     * The return value is the middle two bits of the upper byte of the data
     * byte of an address byte/data byte pair.
     *
     * @param startByte The address byte for this addres byte data byte pair.
     * @return message type, values are:
     * <ul>
     * <li>0 for a turnout with no feedback</li>
     * <li>1 for a turnout with feedback</li>
     * <li>2 for a feedback encoder</li>
     * <li>3 is reserved by Lenz for future use.</li>
     * </ul>
     */
    public int getFeedbackMessageType(int startByte) {
        if (this.isFeedbackBroadcastMessage()) {
            int a2 = this.getElement(startByte + 1);
            return ((a2 & 0x60) / 32);
        } else {
            return -1;
        }
    }

    /* 
     * Next we have a few throttle related messages
     */

    /**
     * If this is a throttle-type message, return address. Otherwise return -1.
     * Note we only identify the command now; the reponse to a request for
     * status is not yet seen here.
     */
    public int getThrottleMsgAddr() {
        if (this.isThrottleMessage()) {
            int a1 = this.getElement(2);
            int a2 = this.getElement(3);
            if (a1 == 0) {
                return (a2);
            } else {
                return (((a1 * 256) & 0xFF00) + (a2 & 0xFF) - 0xC000);
            }
        } else {
            return -1;
        }
    }

    /**
     * Is this a throttle message?
     */
    public boolean isThrottleMessage() {
        int message = this.getElement(0);
        if (message == XNetConstants.LOCO_INFO_NORMAL_UNIT
                || message == XNetConstants.LOCO_INFO_RESPONSE
                || message == XNetConstants.LOCO_INFO_MUED_UNIT
                || message == XNetConstants.LOCO_INFO_MU_ADDRESS
                || message == XNetConstants.LOCO_INFO_DH_UNIT
                || message == XNetConstants.LOCO_AVAILABLE_V1
                || message == XNetConstants.LOCO_AVAILABLE_V2
                || message == XNetConstants.LOCO_NOT_AVAILABLE_V1
                || message == XNetConstants.LOCO_NOT_AVAILABLE_V2) {
            return true;
        }
        return false;
    }

    /**
     * Does this message indicate the locomotive has been taken over by another
     * device?
     */
    public boolean isThrottleTakenOverMessage() {
        return (this.getElement(0) == XNetConstants.LOCO_INFO_RESPONSE
                && this.getElement(1) == XNetConstants.LOCO_NOT_AVAILABLE);
    }

    /**
     * Is this a consist message?
     */
    public boolean isConsistMessage() {
        int message = this.getElement(0);
        if (message == XNetConstants.LOCO_MU_DH_ERROR
                || message == XNetConstants.LOCO_DH_INFO_V1
                || message == XNetConstants.LOCO_DH_INFO_V2) {
            return true;
        }
        return false;
    }

    /* 
     * Finally, we have some commonly used routines that are used for 
     * checking specific, generic, response messages.
     */

    /**
     * In the interest of code reuse, the following function checks to see
     * if an XpressNet Message is the OK message (01 04 05).
     */
    public boolean isOkMessage() {
        return (this.getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER
                && this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_SEND_SUCCESS);
    }

    /**
     * In the interest of code reuse, the following function checks to see
     * if an XpressNet Message is the timeslot restored message (01 07 06).
     */
    public boolean isTimeSlotRestored() {
        return (this.getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER
                && this.getElement(1) == XNetConstants.LIUSB_TIMESLOT_RESTORED);
    }

    /**
     * In the interest of code reuse, the following function checks to see
     * if an XpressNet Message is the Command Station no longer provideing a
     * timeslot message (01 05 04).
     */
    public boolean isTimeSlotRevoked() {
        return (this.getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER
                && this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR);
    }

    /**
     * In the interest of code reuse, the following function checks to see
     * if an XpressNet Message is the Command Station Busy message (61 81 e3).
     */
    public boolean isCSBusyMessage() {
        return (this.getElement(0) == XNetConstants.CS_INFO
                && this.getElement(1) == XNetConstants.CS_BUSY);
    }


    /**
     * In the interest of code reuse, the following function checks to see
     * if an XpressNet Message is the Command Station Transfer Error
     * message (61 80 e1).
     */
    public boolean isCSTransferError() {
        return (this.getElement(0) == XNetConstants.CS_INFO
                && this.getElement(1) == XNetConstants.CS_TRANSFER_ERROR);
    }

    /**
     * In the interest of code reuse, the following function checks to see
     * if an XpressNet Message is the not supported Error
     * message (61 82 e3).
     */
    public boolean isUnsupportedError() {
        return (this.getElement(0) == XNetConstants.CS_INFO
                && this.getElement(1) == XNetConstants.CS_NOT_SUPPORTED);
    }

    /**
     * In the interest of code reuse, the following function checks to see
     * if an XpressNet Message is a communications error message.
     * The errors handled are:
     *  01 01 00  -- Error between interface and the PC
     *  01 02 03  -- Error between interface and the Command Station
     *  01 03 02  -- Unknown Communications Error
     *      01 06 07  -- LI10x Buffer Overflow
     *      01 0A 0B  -- LIUSB only. Request resend of data.
     */
    public boolean isCommErrorMessage() {
        return (this.getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER
                && ((this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_UNKNOWN_DATA_ERROR
                || this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_CS_DATA_ERROR
                || this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_PC_DATA_ERROR
                || this.getElement(1) == XNetConstants.LIUSB_RETRANSMIT_REQUEST
                || this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_BUFFER_OVERFLOW)) 
                || this.isTimeSlotErrorMessage());
    }

    /**
     * In the interest of code reuse, the following function checks to see
     * if an XpressNet Message is a communications error message.
     * The errors handled are:
     *  01 05 04  -- Timeslot Error
     *  01 07 06  -- Timeslot Restored
     *  01 08 09  -- Data sent while there is no Timeslot
     */
    public boolean isTimeSlotErrorMessage() {
        return (this.getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER
                && ((this.getElement(1) == XNetConstants.LIUSB_REQUEST_SENT_WHILE_NO_TIMESLOT 
                || this.getElement(1) == XNetConstants.LIUSB_TIMESLOT_RESTORED
                || this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR)));
    }


    /**
     * Is this message a service mode response?
     */
    public boolean isServiceModeResponse() {
        return (getElement(0) == XNetConstants.CS_SERVICE_MODE_RESPONSE
                && (getElement(1) == XNetConstants.CS_SERVICE_DIRECT_RESPONSE
                || getElement(1) == (XNetConstants.CS_SERVICE_DIRECT_RESPONSE + 1)
                || getElement(1) == (XNetConstants.CS_SERVICE_DIRECT_RESPONSE + 2)
                || getElement(1) == (XNetConstants.CS_SERVICE_DIRECT_RESPONSE + 3)
                || getElement(1) == XNetConstants.CS_SERVICE_REG_PAGE_RESPONSE));
    }

    /**
     * Is this message a register or paged mode programming response?
     */
    public boolean isPagedModeResponse() {
        return (getElement(0) == XNetConstants.CS_SERVICE_MODE_RESPONSE
                && getElement(1) == XNetConstants.CS_SERVICE_REG_PAGE_RESPONSE);
    }

    /**
     * Is this message a direct CV mode programming response?
     */
    public boolean isDirectModeResponse() {
        return (getElement(0) == XNetConstants.CS_SERVICE_MODE_RESPONSE
                && (getElement(1) == XNetConstants.CS_SERVICE_DIRECT_RESPONSE
                || getElement(1) == (XNetConstants.CS_SERVICE_DIRECT_RESPONSE + 1)
                || getElement(1) == (XNetConstants.CS_SERVICE_DIRECT_RESPONSE + 2)
                || getElement(1) == (XNetConstants.CS_SERVICE_DIRECT_RESPONSE + 3)));
    }

    /**
     * @return the CV value associated with a service mode reply
     * return -1 if not a service mode message.
     */
    public int getServiceModeCVNumber() {
        int cv = -1;
        if (isServiceModeResponse()) {
            if ((getElement(1) & XNetConstants.CS_SERVICE_DIRECT_RESPONSE) == XNetConstants.CS_SERVICE_DIRECT_RESPONSE) {
                cv = (getElement(1) - XNetConstants.CS_SERVICE_DIRECT_RESPONSE) * 256 + getElement(2);
            } else {
                cv = getElement(2);
            }
        }
        return (cv);
    }

    /**
     * @return the value returned by the DCC system associated with a 
     * service mode reply.
     * return -1 if not a service mode message.
     */
    public int getServiceModeCVValue() {
        int value = -1;
        if (isServiceModeResponse()) {
            value = getElement(3);
        }
        return (value);
    }

    /**
     * @return true if the message is an error message indicating
     * we should retransmit.
     */
    @Override
    public boolean isRetransmittableErrorMsg() {
        return (this.isCSBusyMessage()
                || this.isCommErrorMessage()
                || this.isCSTransferError());
    }

    /**
     * @return true if the message is an unsollicited message
     */
    @Override
    public boolean isUnsolicited() {
        // The message may be set as an unsolicited message else where
        // or it may be classified as unsolicited based on the type of 
        // message received.
        // NOTE: The feedback messages may be received in either solicited
        // or unsolicited form.  requesting code can mark the reply as solicited
        // by calling the resetUnsolicited function.
        return (super.isUnsolicited()
                || this.isThrottleTakenOverMessage()
                || (this.isFeedbackMessage() && reallyUnsolicited));
    }

    public final void resetUnsolicited() {
        reallyUnsolicited = false;
    }

    /**
     * @return a string representation of the reply suitable for display in the
     * XpressNet monitor.
     */
    @Override
    public String toMonitorString(){
        String text;
        // First, Decode anything that is sent by the LI10x, and
        // not the command station
        
        if(getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER){
            switch(this.getElement(1)) {
              case XNetConstants.LI_MESSAGE_RESPONSE_PC_DATA_ERROR:
                 text = Bundle.getMessage("XNetReplyErrorPCtoLI");
                 break;
              case XNetConstants.LI_MESSAGE_RESPONSE_CS_DATA_ERROR:
                 text = Bundle.getMessage("XNetReplyErrorLItoCS");
                 break;
              case XNetConstants.LI_MESSAGE_RESPONSE_UNKNOWN_DATA_ERROR:
                 text = Bundle.getMessage("XNetReplyErrorUnknown");
                 break;
              case XNetConstants.LI_MESSAGE_RESPONSE_SEND_SUCCESS:
                 text = Bundle.getMessage("XNetReplyOkMessage");
                 break;
              case XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR:
                 text = Bundle.getMessage("XNetReplyErrorNoTimeSlot");
                 break;
              case XNetConstants.LI_MESSAGE_RESPONSE_BUFFER_OVERFLOW:
                 text = Bundle.getMessage("XNetReplyErrorBufferOverflow");
                 break;
              case XNetConstants.LIUSB_TIMESLOT_RESTORED:
                 text = Bundle.getMessage("XNetReplyTimeSlotRestored");
                 break;
              case XNetConstants.LIUSB_REQUEST_SENT_WHILE_NO_TIMESLOT:
                 text = Bundle.getMessage("XNetReplyRequestSentWhileNoTimeslot");
                 break;
              case XNetConstants.LIUSB_BAD_DATA_IN_REQUEST:
                 text = Bundle.getMessage("XNetReplyBadDataInRequest");
                 break;
              case XNetConstants.LIUSB_RETRANSMIT_REQUEST:
                 text = Bundle.getMessage("XNetReplyRetransmitRequest");
                 break;
              default:
                 text = toString();
           }
        } else if (getElement(0) == XNetConstants.LI_VERSION_RESPONSE) {
            text = Bundle.getMessage("XNetReplyLIVersion",
                        (getElementBCD(1).floatValue())/10,
                        (getElementBCD(2).floatValue())/10);
        } else if (getElement(0) == XNetConstants.LI101_REQUEST) {
            // The request and response for baud rate look the same,
            // so we need this for both incoming and outgoing directions
            switch (getElement(1)) {
                case XNetConstants.LI101_REQUEST_ADDRESS:
                    text = Bundle.getMessage("XNetReplyLIAddress",
                                   getElement(2));
                    break;
                case XNetConstants.LI101_REQUEST_BAUD:
                    switch (getElement(2)) {
                        case 1:
                            text = Bundle.getMessage("XNetReplyLIBaud",
                                   Bundle.getMessage("LIBaud19200"));
                            break;
                        case 2:
                            text = Bundle.getMessage("XNetReplyLIBaud",
                                   Bundle.getMessage("Baud38400"));
                            break;
                        case 3:
                            text = Bundle.getMessage("XNetReplyLIBaud",
                                   Bundle.getMessage("Baud57600"));
                            break;
                        case 4:
                            text = Bundle.getMessage("XNetReplyLIBaud",
                                   Bundle.getMessage("Baud115200"));
                            break;
                        default:
                            text = Bundle.getMessage("XNetReplyLIBaud",
                                   Bundle.getMessage("BaudOther"));
                    }
                    break;
                default:
                    text = toString();
            }
            /* Next, check the "CS Info" messages */
        } else if (getElement(0) == XNetConstants.CS_INFO) {
            switch (getElement(1)) {
                case XNetConstants.BC_NORMAL_OPERATIONS:
                    text = Bundle.getMessage("XNetReplyBCNormalOpsResumed");  
                    break;
                case XNetConstants.BC_EVERYTHING_OFF:
                    text = Bundle.getMessage("XNetReplyBCEverythingOff");
                    break;
                case XNetConstants.BC_SERVICE_MODE_ENTRY:
                    text = Bundle.getMessage("XNetReplyBCServiceEntry");
                    break;
                case XNetConstants.PROG_SHORT_CIRCUIT:
                    text = Bundle.getMessage("XNetReplyServiceModeShort");
                    break;
                case XNetConstants.PROG_BYTE_NOT_FOUND:
                    text = Bundle.getMessage("XNetReplyServiceModeDataByteNotFound");
                    break;
                case XNetConstants.PROG_CS_BUSY:
                    text = Bundle.getMessage("XNetReplyServiceModeCSBusy");
                    break;
                case XNetConstants.PROG_CS_READY:
                    text = Bundle.getMessage("XNetReplyServiceModeCSReady");
                    break;
                case XNetConstants.CS_BUSY:
                    text = Bundle.getMessage("XNetReplyCSBusy");
                    break;
                case XNetConstants.CS_NOT_SUPPORTED:
                    text = Bundle.getMessage("XNetReplyCSNotSupported");
                    break;
                case XNetConstants.CS_TRANSFER_ERROR:
                    text = Bundle.getMessage("XNetReplyCSTransferError");
                    break;
                /* The remaining cases are for a Double Header or MU Error */
                case XNetConstants.CS_DH_ERROR_NON_OP:
                    text = Bundle.getMessage("XNetReplyV1DHErrorNotOperated");
                    break;
                case XNetConstants.CS_DH_ERROR_IN_USE:
                    text = Bundle.getMessage("XNetReplyV1DHErrorInUse");
                    break;
                case XNetConstants.CS_DH_ERROR_ALREADY_DH:
                    text = Bundle.getMessage("XNetReplyV1DHErrorAlreadyDH");
                    break;
                case XNetConstants.CS_DH_ERROR_NONZERO_SPD:
                    text = Bundle.getMessage("XNetReplyV1DHErrorNonZeroSpeed");
                    break;
                default:
                    text = toString();
            }
        } else if (getElement(0) == XNetConstants.BC_EMERGENCY_STOP
                && getElement(1) == XNetConstants.BC_EVERYTHING_STOP) {
            text = Bundle.getMessage("XNetReplyBCEverythingStop");
            /* Followed by Service Mode responses */
        } else if (getElement(0) == XNetConstants.CS_SERVICE_MODE_RESPONSE) {
            if (isDirectModeResponse()) {
                text = Bundle.getMessage("XNetReplyServiceModeDirectResponse",
                        getServiceModeCVNumber(),
                        getServiceModeCVValue());
            } else if (isPagedModeResponse()) {
                text = Bundle.getMessage("XNetReplyServiceModePagedResponse",
                        getServiceModeCVNumber(),
                        getServiceModeCVValue());
            } else if (getElement(1) == XNetConstants.CS_SOFTWARE_VERSION) {
                String typeString; 
                switch (getElement(3)) {
                    case 0x00:
                        typeString = Bundle.getMessage("CSTypeLZ100");
                        break;
                    case 0x01:
                        typeString = Bundle.getMessage("CSTypeLH200");
                        break;
                    case 0x02:
                        typeString = Bundle.getMessage("CSTypeCompact");
                        break;
                    // GT 2007/11/6 - Added multiMaus
                    case 0x10:
                        typeString = Bundle.getMessage("CSTypeMultiMaus");
                        break;
                    default:
                        typeString = "" + getElement(3);
                }
                text = Bundle.getMessage("XNetReplyCSVersion",
                        (getElementBCD(2).floatValue()) / 10,
                        typeString);
            } else {
                text = toString();
            }
 /* We want to look at responses to specific requests made to the Command Station */
        } else if (getElement(0) == XNetConstants.CS_REQUEST_RESPONSE) {
            if (getElement(1) == XNetConstants.CS_STATUS_RESPONSE) {
                text = Bundle.getMessage("XNetReplyCSStatus") + " ";
                int statusByte = getElement(2);
                if ((statusByte & 0x01) == 0x01) {
                    // Command station is in Emergency Off Mode
                    text += Bundle.getMessage("XNetCSStatusEmergencyOff") + "; ";
                }
                if ((statusByte & 0x02) == 0x02) {
                    // Command station is in Emergency Stop Mode
                    text += Bundle.getMessage("XNetCSStatusEmergencyStop") + "; ";
                }
                if ((statusByte & 0x08) == 0x08) {
                    // Command station is in Service Mode
                    text += Bundle.getMessage("XNetCSStatusServiceMode") + "; ";
                }
                if ((statusByte & 0x40) == 0x40) {
                    // Command station is in Power Up Mode
                    text += Bundle.getMessage("XNetCSStatusPoweringUp") + "; ";
                }
                if ((statusByte & 0x04) == 0x04) {
                    text += Bundle.getMessage("XNetCSStatusPowerModeAuto") + "; ";
                } else {
                    text += Bundle.getMessage("XNetCSStatusPowerModeManual") + "; ";
                }
                if ((statusByte & 0x80) == 0x80) {
                    // Command station has a experienced a ram check error
                    text += Bundle.getMessage("XNetCSStatusRamCheck") + "";
                }
            } else if (getElement(1) == XNetConstants.CS_SOFTWARE_VERSION) {
                /* This is a Software version response for XpressNet
                 Version 1 or 2 */
                text = Bundle.getMessage("XNetReplyCSVersionV1",
                        (getElementBCD(2).floatValue()) / 10);
            } else {
                text = toString();
            }

            // MU and Double Header Related Responses
        } else if (getElement(0) == XNetConstants.LOCO_MU_DH_ERROR) {
            switch (getElement(1)) {
                case 0x81:
                    text = Bundle.getMessage("XNetReplyDHErrorNotOperated");
                    break;
                case 0x82:
                    text = Bundle.getMessage("XNetReplyDHErrorInUse");
                    break;
                case 0x83:
                    text = Bundle.getMessage("XNetReplyDHErrorAlreadyDH");
                    break;
                case 0x84:
                    text = Bundle.getMessage("XNetReplyDHErrorNonZeroSpeed");
                    break;
                case 0x85:
                    text = Bundle.getMessage("XNetReplyDHErrorLocoNotMU");
                    break;
                case 0x86:
                    text = Bundle.getMessage("XNetReplyDHErrorLocoNotMUBase");
                    break;
                case 0x87:
                    text = Bundle.getMessage("XNetReplyDHErrorCanNotDelete");
                    break;
                case 0x88:
                    text = Bundle.getMessage("XNetReplyDHErrorStackFull");
                    break;
                default:
                    text = Bundle.getMessage("XNetReplyDHErrorOther",(getElement(1)-0x80));
            }
            // Loco Information Response Messages
        } else if (getElement(0) == XNetConstants.LOCO_INFO_NORMAL_UNIT) {
            if (getElement(1) == XNetConstants.LOCO_FUNCTION_STATUS_HIGH_MOM) {
                text = Bundle.getMessage("XNetReplyLocoStatus13Label") + " ";
                // message byte 3, contains F20,F19,F18,F17,F16,F15,F14,F13
                int element3 = getElement(2);
                // message byte 4, contains F28,F27,F26,F25,F24,F23,F22,F21
                int element4 = getElement(3);
                text += parseFunctionHighMomentaryStatus(element3, element4);
            } else {
                text = Bundle.getMessage("XNetReplyLocoNormalLabel") + ",";
                text
                        += parseSpeedAndDirection(getElement(1),
                                getElement(2)) + " ";
                // message byte 4, contains F0,F1,F2,F3,F4
                int element3 = getElement(3);
                // message byte 5, contains F12,F11,F10,F9,F8,F7,F6,F5
                int element4 = getElement(4);
                text += parseFunctionStatus(element3, element4);
            }
        } else if (getElement(0) == XNetConstants.LOCO_INFO_MUED_UNIT) {
            if (getElement(1) == 0xF8) {
                // This message is a Hornby addition to the protocol
                // indicating the speed and direction of a locomoitve
                // controlled by the elite's built in throttles
                text = Bundle.getMessage("XNetReplyLocoEliteSLabel") + " ";
                text += LenzCommandStation.calcLocoAddress(getElement(2), getElement(3));
                text
                        += "," + parseSpeedAndDirection(getElement(4),
                                getElement(5)) + " ";
            } else if (getElement(1) == 0xF9) {
                // This message is a Hornby addition to the protocol
                // indicating the function on/off status of a locomoitve
                // controlled by the elite's built in throttles
                text = Bundle.getMessage("XNetReplyLocoEliteFLabel") + " ";
                text += LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)) + " ";
                // message byte 5, contains F0,F1,F2,F3,F4
                int element4 = getElement(4);
                // message byte 5, contains F12,F11,F10,F9,F8,F7,F6,F5
                int element5 = getElement(5);
                text += parseFunctionStatus(element4, element5);
            } else {
                text
                        = Bundle.getMessage("XNetReplyLocoMULabel") + ",";
                text
                        += parseSpeedAndDirection(getElement(1),
                                getElement(2)) + "";
                // message byte 4, contains F0,F1,F2,F3,F4
                int element3 = getElement(3);
                // message byte 5, contains F12,F11,F10,F9,F8,F7,F6,F5
                int element4 = getElement(4);
                text += parseFunctionStatus(element3, element4);
            }
        } else if (getElement(0) == XNetConstants.LOCO_INFO_MU_ADDRESS) {
            text = Bundle.getMessage("XNetReplyLocoMUBaseLabel") + ",";
            text
                    += parseSpeedAndDirection(getElement(1), getElement(2)) + " ";
        } else if (getElement(0) == XNetConstants.LOCO_INFO_DH_UNIT) {
            text = Bundle.getMessage("XNetReplyLocoDHLabel") + ",";
            text += parseSpeedAndDirection(getElement(1), getElement(2)) + " ";
            // message byte 4, contains F0,F1,F2,F3,F4
            int element3 = getElement(3);
            // message byte 5, contains F12,F11,F10,F9,F8,F7,F6,F5
            int element4 = getElement(4);
            text += parseFunctionStatus(element3, element4);
            text += " " + Bundle.getMessage("XNetReplyLoco2DHLabel") + " ";
            text += LenzCommandStation.calcLocoAddress(getElement(5), getElement(6));
        } else if (getElement(0) == XNetConstants.LOCO_INFO_RESPONSE) {
            text = Bundle.getMessage("XNetReplyLocoLabel") + " ";
            switch (getElement(1)) {
                case XNetConstants.LOCO_SEARCH_RESPONSE_N:
                    text += Bundle.getMessage("XNetReplySearchNormalLabel") + " ";
                    text += getThrottleMsgAddr();
                    break;
                case XNetConstants.LOCO_SEARCH_RESPONSE_DH:
                    text += Bundle.getMessage("XNetReplySearchDHLabel") + " ";
                    text += getThrottleMsgAddr();
                    break;
                case XNetConstants.LOCO_SEARCH_RESPONSE_MU_BASE:
                    text += Bundle.getMessage("XNetReplySearchMUBaseLabel") + " ";
                    text += getThrottleMsgAddr();
                    break;
                case XNetConstants.LOCO_SEARCH_RESPONSE_MU:
                    text += Bundle.getMessage("XNetReplySearchMULabel") + " ";
                    text += getThrottleMsgAddr();
                    break;
                case XNetConstants.LOCO_SEARCH_NO_RESULT:
                    text += Bundle.getMessage("XNetReplySearchFailedLabel") + " ";
                    text += getThrottleMsgAddr();
                    break;
                case XNetConstants.LOCO_NOT_AVAILABLE:
                    text += Bundle.getMessage("rsType") + " ";
                    text += getThrottleMsgAddr() + " ";
                    text += Bundle.getMessage("XNetReplyLocoOperated");
                    break;
                case XNetConstants.LOCO_FUNCTION_STATUS: {
                    text += Bundle.getMessage("rsType") + " "; // "Locomotive", key in NBBundle, shared with Operations
                    text += Bundle.getMessage("XNetReplyFStatusLabel") + " ";
                    // message byte 3, contains F0,F1,F2,F3,F4
                    int element3 = getElement(2);
                    // message byte 4, contains F12,F11,F10,F9,F8,F7,F6,F5
                    int element4 = getElement(3);
                    text += parseFunctionMomentaryStatus(element3, element4);
                    break;
                }
                case XNetConstants.LOCO_FUNCTION_STATUS_HIGH: {
                    text += Bundle.getMessage("rsType") + " ";
                    text += Bundle.getMessage("XNetReplyF13StatusLabel") + " ";
                    // message byte 3, contains F20,F19,F18,F17,F16,F15,F14,F13
                    int element3 = getElement(2);
                    // message byte 4, contains F28,F27,F26,F25,F24,F23,F22,F21
                    int element4 = getElement(3);
                    text += parseFunctionHighStatus(element3, element4);
                    break;
                }
                default:
                    text = toString();
            }
            // Feedback Response Messages
        } else if (isFeedbackBroadcastMessage()) {
            text = Bundle.getMessage("XNetReplyFeedbackLabel") + " ";
            int numDataBytes = getElement(0) & 0x0f;
            for (int i = 1; i < numDataBytes; i += 2) {
                switch (getFeedbackMessageType(i)) {
                    case 0:
                        text = text + Bundle.getMessage("TurnoutWoFeedback")
                                + " " + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
                                + " " + getTurnoutMsgAddr(i) + " "
                                + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "; // "State: "
                        if ((getElement(i + 1) & 0x03) == 0x00) {
                            text = text + Bundle.getMessage("XNetReplyNotOperated"); // last items on line, no trailing space
                        } else if ((getElement(i + 1) & 0x03) == 0x01) {
                            text = text + Bundle.getMessage("XNetReplyThrownLeft");
                        } else if ((getElement(i + 1) & 0x03) == 0x02) {
                            text = text + Bundle.getMessage("XNetReplyThrownRight");
                        } else {
                            text = text + Bundle.getMessage("XNetReplyInvalid");
                        }
                        text = text + "; " + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
                                + " " + (getTurnoutMsgAddr(i) + 1) + " "
                                + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "; // "State: "
                        if ((getElement(i + 1) & 0x0C) == 0x00) {
                            text = text + Bundle.getMessage("XNetReplyNotOperated"); // last items on line, no trailing space
                        } else if ((getElement(i + 1) & 0x0C) == 0x04) {
                            text = text + Bundle.getMessage("XNetReplyThrownLeft");
                        } else if ((getElement(i + 1) & 0x0C) == 0x08) {
                            text = text + Bundle.getMessage("XNetReplyThrownRight");
                        } else {
                            text = text + Bundle.getMessage("XNetReplyInvalid");
                        }
                        break;
                    case 1:
                        text = text + Bundle.getMessage("TurnoutWFeedback")
                                + " " + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
                                + " " + getTurnoutMsgAddr(i) + " "
                                + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " ";
                        if ((getElement(i + 1) & 0x03) == 0x00) {
                            text = text + Bundle.getMessage("XNetReplyNotOperated"); // last items on line, no trailing space
                        } else if ((getElement(i + 1) & 0x03) == 0x01) {
                            text = text + Bundle.getMessage("XNetReplyThrownLeft");
                        } else if ((getElement(i + 1) & 0x03) == 0x02) {
                            text = text + Bundle.getMessage("XNetReplyThrownRight");
                        } else {
                            text = text + Bundle.getMessage("XNetReplyInvalid");
                        }
                        text = text + "; " + Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"))
                                + " " + (getTurnoutMsgAddr() + 1) + " "
                                + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " ";
                        if ((getElement(i + 1) & 0x0C) == 0x00) {
                            text = text + Bundle.getMessage("XNetReplyNotOperated"); // last items on line, no trailing space
                        } else if ((getElement(i + 1) & 0x0C) == 0x04) {
                            text = text + Bundle.getMessage("XNetReplyThrownLeft");
                        } else if ((getElement(i + 1) & 0x0C) == 0x08) {
                            text = text + Bundle.getMessage("XNetReplyThrownRight");
                        } else {
                            text = text + Bundle.getMessage("XNetReplyInvalid");
                        }
                        break;
                    case 2:
                        text = text + Bundle.getMessage("XNetReplyFeedbackEncoder") + " "
                                + (getFeedbackEncoderMsgAddr(i) + 1);
                        boolean highnibble = ((getElement(i + 1) & 0x10) == 0x10);
                        text = text + " " + Bundle.getMessage("XNetReplyContactLabel") + " " + (highnibble ? 5 : 1);

                        text = text + " " + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
                                + (((getElement(i + 1) & 0x01) == 0x01) ? Bundle.getMessage("PowerStateOn") : Bundle.getMessage("PowerStateOff"));
                        text = text + "; " + Bundle.getMessage("XNetReplyContactLabel") + " " + (highnibble ? 6 : 2);

                        text = text + " " + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
                                + (((getElement(i + 1) & 0x02) == 0x02) ? Bundle.getMessage("PowerStateOn") : Bundle.getMessage("PowerStateOff"));
                        text = text + "; " + Bundle.getMessage("XNetReplyContactLabel") + " " + (highnibble ? 7 : 3);

                        text = text + " " + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
                                + (((getElement(i + 1) & 0x04) == 0x04) ? Bundle.getMessage("PowerStateOn") : Bundle.getMessage("PowerStateOff"));
                        text = text + "; " + Bundle.getMessage("XNetReplyContactLabel") + " " + (highnibble ? 8 : 4);

                        text = text + " " + Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")) + " "
                                + (((getElement(i + 1) & 0x08) == 0x08) ? Bundle.getMessage("PowerStateOn") : Bundle.getMessage("PowerStateOff"));
                        text = text + "; ";
                        break;
                    default:
                        text = text + getElement(i) + " " + getElement(i + 1);
                }
            }
        } else {
            text = toString();
        }
        return text;
    }

    /**
     * Parse the speed step and the direction information for a locomotive.
     *
     * @param element1 contains the speed step mode designation and
     * availability information
     * @param element2 contains the data byte including the step mode and
     * availability information 
     */
    protected String parseSpeedAndDirection(int element1, int element2) {
        String text = "";
        int speedVal = 0;
        if ((element2 & 0x80) == 0x80) {
            text += Bundle.getMessage("Forward") + ",";
        } else {
            text += Bundle.getMessage("Reverse") + ",";
        }

        if ((element1 & 0x04) == 0x04) {
            // We're in 128 speed step mode
            speedVal = element2 & 0x7f;
            // The first speed step used is actually at 2 for 128
            // speed step mode.
            if (speedVal >= 1) {
                speedVal -= 1;
            } else {
                speedVal = 0;
            }
            text += Bundle.getMessage("SpeedStepModeX", 128) + ",";
        } else if ((element1 & 0x02) == 0x02) {
            // We're in 28 speed step mode
            // We have to re-arange the bits, since bit 4 is the LSB,
            // but other bits are in order from 0-3
            speedVal = ((element2 & 0x0F) << 1) + ((element2 & 0x10) >> 4);
            // The first speed step used is actually at 4 for 28  
            // speed step mode.
            if (speedVal >= 3) {
                speedVal -= 3;
            } else {
                speedVal = 0;
            }
            text += Bundle.getMessage("SpeedStepModeX", 28) + ",";
        } else if ((element1 & 0x01) == 0x01) {
            // We're in 27 speed step mode
            // We have to re-arange the bits, since bit 4 is the LSB,
            // but other bits are in order from 0-3
            speedVal = ((element2 & 0x0F) << 1) + ((element2 & 0x10) >> 4);
            // The first speed step used is actually at 4 for 27
            // speed step mode.
            if (speedVal >= 3) {
                speedVal -= 3;
            } else {
                speedVal = 0;
            }
            text += Bundle.getMessage("SpeedStepModeX", 27) + ",";
        } else {
            // Assume we're in 14 speed step mode.
            speedVal = (element2 & 0x0F);
            if (speedVal >= 1) {
                speedVal -= 1;
            } else {
                speedVal = 0;
            }
            text += Bundle.getMessage("SpeedStepModeX", 14) + ",";
        }

        text += Bundle.getMessage("SpeedStepLabel") + " " + speedVal + ". ";

        if ((element1 & 0x08) == 0x08) {
            text += "" + Bundle.getMessage("XNetReplyAddressInUse");
        } else {
            text += "" + Bundle.getMessage("XNetReplyAddressFree");
        }
        return (text);
    }

    /**
     * Parse the status of functions F0-F12.
     *
     * @param element3 contains the data byte including F0,F1,F2,F3,F4
     * @param element4 contains F12,F11,F10,F9,F8,F7,F6,F5
     */
    protected String parseFunctionStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x10) != 0) {
            text += "F0 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F0 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element3 & 0x01) != 0) {
            text += "F1 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F1 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F2 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F2 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F3 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F3 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F4 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F4 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F5 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F5 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F6 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F6 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F7 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F7 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F8 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F8 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F9 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F9 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F10 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F10 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F11 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F11 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F12 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F12 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        return (text);
    }

    /**
     * Parse the status of functions F13-F28.
     *
     * @param element3 contains F20,F19,F18,F17,F16,F15,F14,F13
     * @param element4 contains F28,F27,F26,F25,F24,F23,F22,F21
     */
    protected String parseFunctionHighStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x01) != 0) {
            text += "F13 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F13 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F14 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F14 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F15 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F15 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F16 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F16 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element3 & 0x10) != 0) {
            text += "F17 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F17 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element3 & 0x20) != 0) {
            text += "F18 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F18 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element3 & 0x40) != 0) {
            text += "F19 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F19 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element3 & 0x80) != 0) {
            text += "F20 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F20 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F21 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F21 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F22 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F22 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F23 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F23 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F24 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F24 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F25 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F25 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F26 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F26 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F27 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F27 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F28 " + Bundle.getMessage("PowerStateOn") + "; ";
        } else {
            text += "F28 " + Bundle.getMessage("PowerStateOff") + "; ";
        }
        return (text);
    }
    /**
     * Parse the Momentary status of functions.
     *
     * @param element3 contains the data byte including F0,F1,F2,F3,F4
     * @param element4 contains F12,F11,F10,F9,F8,F7,F6,F5
     */
    protected String parseFunctionMomentaryStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x10) != 0) {
            text += "F0 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F0 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element3 & 0x01) != 0) {
            text += "F1 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F1 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F2 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F2 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F3 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F3 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F4 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F4 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F5 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F5 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F6 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F6 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F7 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F7 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F8 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F8 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F9 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F9 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F10 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F10 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F11 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F11 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F12 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F12 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        return (text);
    }

    /**
     * Parse the Momentary sytatus of functions F13-F28.
     *
     * @param element3 contains F20,F19,F18,F17,F16,F15,F14,F13
     * @param element4 contains F28,F27,F26,F25,F24,F23,F22,F21
     */
    protected String parseFunctionHighMomentaryStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x01) != 0) {
            text += "F13 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F13 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F14 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F14 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F15 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F15 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F16 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F16 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element3 & 0x10) != 0) {
            text += "F17 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F17 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element3 & 0x20) != 0) {
            text += "F18 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F18 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element3 & 0x40) != 0) {
            text += "F19 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F19 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element3 & 0x80) != 0) {
            text += "F20 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F20 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F21 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F21 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F22 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F22 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F23 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F23 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F24 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F24 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F25 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F25 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F26 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F26 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F27 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F27 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F28 " + Bundle.getMessage("FunctionMomentary") + "; ";
        } else {
            text += "F28 " + Bundle.getMessage("FunctionContinuous") + "; ";
        }
        return (text);
    }

}
