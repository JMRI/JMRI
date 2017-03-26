package jmri.jmrix.lenz;


/**
 * Represents a single response from the XpressNet.
 * <P>
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

    /* Get the opcode as a string in hex format */
    public String getOpCodeHex() {
        return "0x" + Integer.toHexString(this.getOpCode());
    }

    /**
     * check whether the message has a valid parity
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
     * Get an integer representation of a BCD value
     *
     * @param n byte in message to convert
     * @return Integer value of BCD byte.
     */
    public Integer getElementBCD(int n) {
        return Integer.decode(Integer.toHexString(getElement(n)));
    }

    /* 
     * skipPrefix is not used at this point in time, but is 
     *  defined as abstract in AbstractMRReply 
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
     * <p>
     * If this is a feedback response message for a turnout, return the address.
     * Otherwise return -1.
     * </p>
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
     * <p>
     * If this is a feedback broadcast message and the specified startbyte is
     * the address byte of an addres byte data byte pair for a turnout, return
     * the address. Otherwise return -1.
     * </p>
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
     * <p>
     * Parse the feedback message for a turnout, and return the status for the
     * even or odd half of the nibble (upper or lower part)
     * </p>
     *
     * @param turnout <ul>
     * <li>0 for the even turnout associated with the pair. This is the upper
     * half of the data nibble asociated with the pair </li>
     * <li>1 for the odd turnout associated with the pair. This is the lower
     * half of the data nibble asociated with the pair </li>
     * </ul>
     * @return THROWN/CLOSED as defined in {@link jmri.Turnout}
     *
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
     * <p>
     * Parse the specified address byte/data byte pair in a feedback broadcast
     * message and see if it is for a turnout. If it is, return the status for
     * the even or odd half of the nibble (upper or lower part)
     * </p>
     *
     * @param startByte address byte of the address byte/data byte pair.
     * @param turnout   <ul>
     * <li>0 for the even turnout associated with the pair. This is the upper
     * half of the data nibble asociated with the pair </li>
     * <li>1 for the odd turnout associated with the pair. This is the lower
     * half of the data nibble asociated with the pair </li>
     * </ul>
     * @return THROWN/CLOSED as defined in {@link jmri.Turnout}
     *
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
     * <p>
     * If this is a feedback broadcast message and the specified startByte is
     * the address byte of an address byte/data byte pair for a feedback
     * encoder, return the address. Otherwise return -1.
     * </p>
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
     * <p>
     * Extract the feedback message type from a feedback message this is the
     * middle two bits of the upper byte of the second data byte.
     * </p>
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
     * <p>
     * Extract the feedback message type from the data byte of associated with
     * the specified address byte specified by startByte.
     * </p>
     * <p>
     * The return value is the middle two bits of the upper byte of the data
     * byte of an address byte/data byte pair.
     * </p>
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

    /* 
     * In the interest of code reuse, The following function checks to see 
     * if an XPressNet Message is the OK message (01 04 05)
     */
    public boolean isOkMessage() {
        return (this.getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER
                && this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_SEND_SUCCESS);
    }

    /* 
     * In the interest of code reuse, The following function checks to see 
     * if an XPressNet Message is the timeslot restored message (01 07 06)
     */
    public boolean isTimeSlotRestored() {
        return (this.getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER
                && this.getElement(1) == XNetConstants.LIUSB_TIMESLOT_RESTORED);
    }

    /* 
     * In the interest of code reuse, The following function checks to see 
     * if an XPressNet Message is the command staiton no longer provideing a
     * timeslot message (01 05 04)
     */
    public boolean isTimeSlotRevoked() {
        return (this.getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER
                && this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR);
    }

    /* 
     * In the interest of code reuse, The following function checks to see 
     * if an XPressNet Message is the Command Station Busy message (61 81 e3)
     */
    public boolean isCSBusyMessage() {
        return (this.getElement(0) == XNetConstants.CS_INFO
                && this.getElement(1) == XNetConstants.CS_BUSY);
    }


    /* 
     * In the interest of code reuse, The following function checks to see 
     * if an XPressNet Message is the Command Station Transfer Error 
     * message (61 80 e1)
     */
    public boolean isCSTransferError() {
        return (this.getElement(0) == XNetConstants.CS_INFO
                && this.getElement(1) == XNetConstants.CS_TRANSFER_ERROR);
    }

    /* 
     * In the interest of code reuse, The following function checks to see 
     * if an XPressNet Message is a communications error message.
     * the errors handeled are:
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

    /* 
     * In the interest of code reuse, The following function checks to see 
     * if an XPressNet Message is a communications error message.
     * the errors handeled are:
     *  01 05 04  -- Timeslot Error
     *      01 07 06  -- Timeslot Restored 
     *      01 08 09  -- Data sent while there is no Timeslot
     */
    public boolean isTimeSlotErrorMessage() {
        return (this.getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER
                && ((this.getElement(1) == XNetConstants.LIUSB_REQUEST_SENT_WHILE_NO_TIMESLOT 
                || this.getElement(1) == XNetConstants.LIUSB_TIMESLOT_RESTORED
                || this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR)));
    }


    /*
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

    /*
     * Is this message a register or paged mode programming response?
     */
    public boolean isPagedModeResponse() {
        return (getElement(0) == XNetConstants.CS_SERVICE_MODE_RESPONSE
                && getElement(1) == XNetConstants.CS_SERVICE_REG_PAGE_RESPONSE);
    }

    /*
     * Is this message a direct CV mode programming response?
     */
    public boolean isDirectModeResponse() {
        return (getElement(0) == XNetConstants.CS_SERVICE_MODE_RESPONSE
                && (getElement(1) == XNetConstants.CS_SERVICE_DIRECT_RESPONSE
                || getElement(1) == (XNetConstants.CS_SERVICE_DIRECT_RESPONSE + 1)
                || getElement(1) == (XNetConstants.CS_SERVICE_DIRECT_RESPONSE + 2)
                || getElement(1) == (XNetConstants.CS_SERVICE_DIRECT_RESPONSE + 3)));
    }

    /*
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

    /*
     * @return the value returned by the DCC system associated with a 
     * service mode reply
     * return -1 if not a service mode message.
     */
    public int getServiceModeCVValue() {
        int value = -1;
        if (isServiceModeResponse()) {
            value = getElement(3);
        }
        return (value);
    }

    /*
     * Return True if the message is an error message indicating 
     * we should retransmit.
     */
    @Override
    public boolean isRetransmittableErrorMsg() {
        return (this.isCSBusyMessage()
                || this.isCommErrorMessage()
                || this.isCSTransferError());
    }

    /*
     * Return true of the message is an unsolicited message
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
     * XPressNet monitor.
     */
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
                                   Bundle.getMessage("LIBaud38400"));
                            break;
                        case 3:
                            text = Bundle.getMessage("XNetReplyLIBaud",
                                   Bundle.getMessage("LIBaud57600"));
                            break;
                        case 4:
                            text = Bundle.getMessage("XNetReplyLIBaud",
                                   Bundle.getMessage("LIBaud115200"));
                            break;
                        default:
                            text = Bundle.getMessage("XNetReplyLIBaud",
                                   Bundle.getMessage("LIBaudOther"));
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
            //}
 /* We want to look at responses to specific requests made to the Command Station */
        } else if (getElement(0) == XNetConstants.CS_REQUEST_RESPONSE) {
            if (getElement(1) == XNetConstants.CS_STATUS_RESPONSE) {
                text = Bundle.getMessage("XNetReplyCSStatus");
                int statusByte = getElement(2);
                if ((statusByte & 0x01) == 0x01) {
                    // Command station is in Emergency Off Mode
                    text += Bundle.getMessage("XNetCSStatusEmergencyOff");
                }
                if ((statusByte & 0x02) == 0x02) {
                    // Command station is in Emergency Stop Mode
                    text += Bundle.getMessage("XNetCSStatusEmergencyStop");
                }
                if ((statusByte & 0x08) == 0x08) {
                    // Command station is in Service Mode
                    text += Bundle.getMessage("XNetCSStatusServiceMode");
                }
                if ((statusByte & 0x40) == 0x40) {
                    // Command station is in Power Up Mode
                    text += Bundle.getMessage("XNetCSStatusPoweringUp");
                }
                if ((statusByte & 0x04) == 0x04) {
                    text += Bundle.getMessage("XNetCSStatusPowerModeAuto");
                } else {
                    text += Bundle.getMessage("XNetCSStatusPowerModeManual");
                }
                if ((statusByte & 0x80) == 0x80) {
                    // Command station has a experienced a ram check error
                    text += Bundle.getMessage("XNetCSStatusRamCheck");
                }
            } else if (getElement(1) == XNetConstants.CS_SOFTWARE_VERSION) {
                /* This is a Software version response for XPressNet
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
                text = "Locomotive F13-F28 Momentary Status: ";
                // message byte 3, contains F20,F19,F18,F17,F16,F15,F14,F13
                int element3 = getElement(2);
                // message byte 4, contains F28,F27,F26,F25,F24,F23,F22,F21
                int element4 = getElement(3);
                text += parseFunctionHighMomentaryStatus(element3, element4);
            } else {
                text = "Locomotive Information Response: Normal Unit ";
                text
                        += parseSpeedandDirection(getElement(1),
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
                text = "Elite Speed/Direction Information: Locomotive ";
                text += LenzCommandStation.calcLocoAddress(getElement(2), getElement(3));
                text
                        += parseSpeedandDirection(getElement(4),
                                getElement(5)) + " ";
            } else if (getElement(1) == 0xF9) {
                // This message is a Hornby addition to the protocol
                // indicating the function on/off status of a locomoitve
                // controlled by the elite's built in throttles
                text = "Elite Function Information: Locomotive ";
                text += LenzCommandStation.calcLocoAddress(getElement(2), getElement(3));
                // message byte 5, contains F0,F1,F2,F3,F4
                int element4 = getElement(4);
                // message byte 5, contains F12,F11,F10,F9,F8,F7,F6,F5
                int element5 = getElement(5);
                text += parseFunctionStatus(element4, element5);
            } else {
                text
                        = "Locomotive Information Response: Locomotive in Multiple Unit ";
                text
                        += parseSpeedandDirection(getElement(1),
                                getElement(2)) + " ";
                // message byte 4, contains F0,F1,F2,F3,F4
                int element3 = getElement(3);
                // message byte 5, contains F12,F11,F10,F9,F8,F7,F6,F5
                int element4 = getElement(4);
                text += parseFunctionStatus(element3, element4);
            }
        } else if (getElement(0) == XNetConstants.LOCO_INFO_MU_ADDRESS) {
            text = "Locomotive Information Response: Multi Unit Base Address";
            text
                    += parseSpeedandDirection(getElement(1), getElement(2)) + " ";
        } else if (getElement(0) == XNetConstants.LOCO_INFO_DH_UNIT) {
            text
                    = "Locomotive Information Response: Locomotive in Double Header ";
            text
                    += parseSpeedandDirection(getElement(1), getElement(2)) + " ";
            // message byte 4, contains F0,F1,F2,F3,F4
            int element3 = getElement(3);
            // message byte 5, contains F12,F11,F10,F9,F8,F7,F6,F5
            int element4 = getElement(4);
            text += parseFunctionStatus(element3, element4);
            text += " Second Locomotive in Double Header is: ";
            text += LenzCommandStation.calcLocoAddress(getElement(5), getElement(6));
        } else if (getElement(0) == XNetConstants.LOCO_INFO_RESPONSE) {
            text = "Locomotive Information Response: ";
            switch (getElement(1)) {
                case XNetConstants.LOCO_SEARCH_RESPONSE_N:
                    text += "Search Response, Normal Locomotive: ";
                    text += getThrottleMsgAddr();
                    break;
                case XNetConstants.LOCO_SEARCH_RESPONSE_DH:
                    text += "Search Response, Loco in Double Header: ";
                    text += getThrottleMsgAddr();
                    break;
                case XNetConstants.LOCO_SEARCH_RESPONSE_MU_BASE:
                    text += "Search Response, MU Base Address: ";
                    text += getThrottleMsgAddr();
                    break;
                case XNetConstants.LOCO_SEARCH_RESPONSE_MU:
                    text += "Search Response, Loco in MU: ";
                    text += getThrottleMsgAddr();
                    break;
                case XNetConstants.LOCO_SEARCH_NO_RESULT:
                    text += "Search Response, Search failed for: ";
                    text += getThrottleMsgAddr();
                    break;
                case XNetConstants.LOCO_NOT_AVAILABLE:
                    text += "Locomotive ";
                    text += getThrottleMsgAddr();
                    text += " is being operated by another device.";
                    break;
                case XNetConstants.LOCO_FUNCTION_STATUS: {
                    text += "Locomotive ";
                    text += " Function Status: ";
                    // message byte 3, contains F0,F1,F2,F3,F4
                    int element3 = getElement(2);
                    // message byte 4, contains F12,F11,F10,F9,F8,F7,F6,F5
                    int element4 = getElement(3);
                    text += parseFunctionMomentaryStatus(element3, element4);
                    break;
                }
                case XNetConstants.LOCO_FUNCTION_STATUS_HIGH: {
                    text += "Locomotive ";
                    text += "F13-F28 Status: ";
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
            text = "Feedback Response:";
            int numDataBytes = getElement(0) & 0x0f;
            for (int i = 1; i < numDataBytes; i += 2) {
                switch (getFeedbackMessageType(i)) {
                    case 0:
                        text = text + "Turnout with out Feedback "
                                + " Turnout: " + getTurnoutMsgAddr(i) + " State: ";
                        if ((getElement(i + 1) & 0x03) == 0x00) {
                            text = text + "Not Operated";
                        } else if ((getElement(i + 1) & 0x03) == 0x01) {
                            text = text + "Thrown Left";
                        } else if ((getElement(i + 1) & 0x03) == 0x02) {
                            text = text + "Thrown Right";
                        } else {
                            text = text + "<Invalid>";
                        }
                        text = text + "; Turnout: " + (getTurnoutMsgAddr(i) + 1)
                                + " State: ";
                        if ((getElement(i + 1) & 0x0C) == 0x00) {
                            text = text + "Not Operated";
                        } else if ((getElement(i + 1) & 0x0C) == 0x04) {
                            text = text + "Thrown Left";
                        } else if ((getElement(i + 1) & 0x0C) == 0x08) {
                            text = text + "Thrown Right";
                        } else {
                            text = text + "<Invalid>";
                        }
                        break;
                    case 1:
                        text = text + "Turnout with Feedback "
                                + " Turnout: " + getTurnoutMsgAddr(i) + " State: ";
                        if ((getElement(i + 1) & 0x03) == 0x00) {
                            text = text + "Not Operated";
                        } else if ((getElement(i + 1) & 0x03) == 0x01) {
                            text = text + "Thrown Left";
                        } else if ((getElement(i + 1) & 0x03) == 0x02) {
                            text = text + "Thrown Right";
                        } else {
                            text = text + "<Invalid>";
                        }
                        text = text + "; Turnout: " + (getTurnoutMsgAddr() + 1)
                                + " State: ";
                        if ((getElement(i + 1) & 0x0C) == 0x00) {
                            text = text + "Not Operated";
                        } else if ((getElement(i + 1) & 0x0C) == 0x04) {
                            text = text + "Thrown Left";
                        } else if ((getElement(i + 1) & 0x0C) == 0x08) {
                            text = text + "Thrown Right";
                        } else {
                            text = text + "<Invalid>";
                        }
                        break;
                    case 2:
                        text = text + "Feedback Encoder "
                                + "Base Address: " + (getFeedbackEncoderMsgAddr(i) + 1);
                        boolean highnibble = ((getElement(i + 1) & 0x10) == 0x10);
                        text = text + " Contact: " + (highnibble ? 5 : 1);
                        text
                                = text + " State: "
                                + (((getElement(i + 1) & 0x01) == 0x01) ? "On;" : "Off;");
                        text = text + " Contact: " + (highnibble ? 6 : 2);
                        text
                                = text + " State: "
                                + (((getElement(i + 1) & 0x02) == 0x02) ? "On;" : "Off;");
                        text = text + " Contact: " + (highnibble ? 7 : 3);
                        text
                                = text + " State: "
                                + (((getElement(i + 1) & 0x04) == 0x04) ? "On;" : "Off;");
                        text = text + " Contact: " + (highnibble ? 8 : 4);
                        text
                                = text + " State: "
                                + (((getElement(i + 1) & 0x08) == 0x08) ? "On;" : "Off;");
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

    /* parse the speed step and the direction information for a locomotive
     * element1 contains the speed step mode designation and 
     * availability information
     * element2 contains the data byte including the step mode and 
     * availability information 
     */
    private String parseSpeedandDirection(int element1, int element2) {
        String text = "";
        int speedVal = 0;
        if ((element2 & 0x80) == 0x80) {
            text += "Direction Forward,";
        } else {
            text += "Direction Reverse,";
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
            text += "128 Speed Step Mode,";
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
            text += "28 Speed Step Mode,";
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
            text += "27 Speed Step Mode,";
        } else {
            // Assume we're in 14 speed step mode.
            speedVal = (element2 & 0x0F);
            if (speedVal >= 1) {
                speedVal -= 1;
            } else {
                speedVal = 0;
            }
            text += "14 Speed Step Mode,";
        }

        text += "Speed Step " + speedVal + ". ";

        if ((element1 & 0x08) == 0x08) {
            text += " Address in use by another device.";
        } else {
            text += " Address is Free for Operation.";
        }
        return (text);
    }

    /* Parse the status of functions.
     * element3 contains the data byte including F0,F1,F2,F3,F4
     * element4 contains F12,F11,F10,F9,F8,F7,F6,F5
     */
    private String parseFunctionStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x10) != 0) {
            text += "F0 on ";
        } else {
            text += "F0 off ";
        }
        if ((element3 & 0x01) != 0) {
            text += "F1 on ";
        } else {
            text += "F1 off ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F2 on ";
        } else {
            text += "F2 off ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F3 on ";
        } else {
            text += "F3 off ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F4 on ";
        } else {
            text += "F4 off ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F5 on ";
        } else {
            text += "F5 off ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F6 on ";
        } else {
            text += "F6 off ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F7 on ";
        } else {
            text += "F7 off ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F8 on ";
        } else {
            text += "F8 off ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F9 on ";
        } else {
            text += "F9 off ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F10 on ";
        } else {
            text += "F10 off ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F11 on ";
        } else {
            text += "F11 off ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F12 on ";
        } else {
            text += "F12 off ";
        }
        return (text);
    }

    /* Parse the status of functions functions F13-F28.
     * element3 contains F20,F19,F18,F17,F16,F15,F14,F13
     * element4 contains F28,F27,F26,F25,F24,F23,F22,F21
     */
    private String parseFunctionHighStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x01) != 0) {
            text += "F13 on ";
        } else {
            text += "F13 off ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F14 on ";
        } else {
            text += "F14 off ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F15 on ";
        } else {
            text += "F15 off ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F16 on ";
        } else {
            text += "F16 off ";
        }
        if ((element3 & 0x10) != 0) {
            text += "F17 on ";
        } else {
            text += "F17 off ";
        }
        if ((element3 & 0x20) != 0) {
            text += "F18 on ";
        } else {
            text += "F18 off ";
        }
        if ((element3 & 0x40) != 0) {
            text += "F19 on ";
        } else {
            text += "F19 off ";
        }
        if ((element3 & 0x80) != 0) {
            text += "F20 on ";
        } else {
            text += "F20 off ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F21 on ";
        } else {
            text += "F21 off ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F22 on ";
        } else {
            text += "F22 off ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F23 on ";
        } else {
            text += "F23 off ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F24 on ";
        } else {
            text += "F24 off ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F25 on ";
        } else {
            text += "F25 off ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F26 on ";
        } else {
            text += "F26 off ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F27 on ";
        } else {
            text += "F27 off ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F28 on ";
        } else {
            text += "F28 off ";
        }
        return (text);
    }
    /* Parse the Momentary sytatus of functions.
     * element3 contains the data byte including F0,F1,F2,F3,F4
     * element4 contains F12,F11,F10,F9,F8,F7,F6,F5
     */

    private String parseFunctionMomentaryStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x10) != 0) {
            text += "F0 Momentary ";
        } else {
            text += "F0 Continuous ";
        }
        if ((element3 & 0x01) != 0) {
            text += "F1 Momentary ";
        } else {
            text += "F1 Continuous ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F2 Momentary ";
        } else {
            text += "F2 Continuous ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F3 Momentary ";
        } else {
            text += "F3 Continuous ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F4 Momentary ";
        } else {
            text += "F4 Continuous ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F5 Momentary ";
        } else {
            text += "F5 Continuous ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F6 Momentary ";
        } else {
            text += "F6 Continuous ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F7 Momentary ";
        } else {
            text += "F7 Continuous ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F8 Momentary ";
        } else {
            text += "F8 Continuous ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F9 Momentary ";
        } else {
            text += "F9 Continuous ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F10 Momentary ";
        } else {
            text += "F10 Continuous ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F11 Momentary ";
        } else {
            text += "F11 Continuous ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F12 Momentary ";
        } else {
            text += "F12 Continuous ";
        }
        return (text);
    }

    /* Parse the Momentary sytatus of functions F13-F28.
     * element3 contains F20,F19,F18,F17,F16,F15,F14,F13
     * element4 contains F28,F27,F26,F25,F24,F23,F22,F21
     */
    private String parseFunctionHighMomentaryStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x01) != 0) {
            text += "F13 Momentary ";
        } else {
            text += "F13 Continuous ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F14 Momentary ";
        } else {
            text += "F14 Continuous ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F15 Momentary ";
        } else {
            text += "F15 Continuous ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F16 Momentary ";
        } else {
            text += "F16 Continuous ";
        }
        if ((element3 & 0x10) != 0) {
            text += "F17 Momentary ";
        } else {
            text += "F17 Continuous ";
        }
        if ((element3 & 0x20) != 0) {
            text += "F18 Momentary ";
        } else {
            text += "F18 Continuous ";
        }
        if ((element3 & 0x40) != 0) {
            text += "F19 Momentary ";
        } else {
            text += "F19 Continuous ";
        }
        if ((element3 & 0x80) != 0) {
            text += "F20 Momentary ";
        } else {
            text += "F20 Continuous ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F21 Momentary ";
        } else {
            text += "F21 Continuous ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F22 Momentary ";
        } else {
            text += "F22 Continuous ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F23 Momentary ";
        } else {
            text += "F23 Continuous ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F24 Momentary ";
        } else {
            text += "F24 Continuous ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F25 Momentary ";
        } else {
            text += "F25 Continuous ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F26 Momentary ";
        } else {
            text += "F26 Continuous ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F27 Momentary ";
        } else {
            text += "F27 Continuous ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F28 Momentary ";
        } else {
            text += "F28 Continuous ";
        }
        return (text);
    }

}


