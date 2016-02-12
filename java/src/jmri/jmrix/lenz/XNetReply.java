// XNetReply.java
package jmri.jmrix.lenz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single response from the XpressNet.
 * <P>
 *
 * @author	Paul Bender Copyright (C) 2004
 * @version	$Revision$
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
            setElement(i, b[i]);
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
                && this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_SEND_SUCCESS);
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
     *		01 01 00  -- Error between interface and the PC
     *		01 02 03  -- Error between interface and the Command Station
     *		01 03 02  -- Unknown Communications Error
     *          01 06 07  -- LI10x Buffer Overflow
     *          01 0A 0B  -- LIUSB only. Request resend of data.
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
     *		01 05 04  -- Timeslot Error
     *          01 07 06  -- Timeslot Restored 
     *          01 08 09  -- Timeslot Restored 
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

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(XNetReply.class.getName());

}

/* @(#)XNetMessage.java */
