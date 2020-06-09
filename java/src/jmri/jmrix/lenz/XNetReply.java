package jmri.jmrix.lenz;

import java.util.Optional;
import java.util.function.Function;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Represents a single response from the XpressNet.
 *
 * @author Paul Bender Copyright (C) 2004
 *
 */
public class XNetReply extends jmri.jmrix.AbstractMRReply {

    private static final String RS_TYPE = "rsType";
    private static final String X_NET_REPLY_LI_BAUD = "XNetReplyLIBaud";
    private static final String COLUMN_STATE = "ColumnState";
    private static final String MAKE_LABEL = "MakeLabel";
    private static final String POWER_STATE_ON = "PowerStateOn";
    private static final String POWER_STATE_OFF = "PowerStateOff";
    private static final String FUNCTION_MOMENTARY = "FunctionMomentary";
    private static final String FUNCTION_CONTINUOUS = "FunctionContinuous";
    private static final String BEAN_NAME_TURNOUT = "BeanNameTurnout";
    private static final String X_NET_REPLY_NOT_OPERATED = "XNetReplyNotOperated";
    private static final String X_NET_REPLY_THROWN_LEFT = "XNetReplyThrownLeft";
    private static final String X_NET_REPLY_THROWN_RIGHT = "XNetReplyThrownRight";
    private static final String X_NET_REPLY_INVALID = "XNetReplyInvalid";
    private static final String X_NET_REPLY_CONTACT_LABEL = "XNetReplyContactLabel";
    private static final String SPEED_STEP_MODE_X = "SpeedStepModeX";
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
     * @param message existing message.
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
     * @param message hex string of message.
     */
    public XNetReply(String message) {
        super();
        setBinary(true);
        // gather bytes in result
        byte[]  b= jmri.util.StringUtil.bytesFromHexString(message);
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
     * @return 0x hex string of OpCode.
     */
    public String getOpCodeHex() {
        return "0x" + Integer.toHexString(this.getOpCode());
    }

    /**
     * Check whether the message has a valid parity.
     * @return true if parity valid, else false.
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
            return getTurnoutAddrFromData(
                    getElement(1),
                    getElement(2));
        }
        return -1;
    }
    
    private int getTurnoutAddrFromData(int a1, int a2) {
        if (getFeedbackMessageType() > 1) {
            return -1;
        }
        int address = (a1 & 0xff) * 4 + 1;
        if ((a2 & 0x10) != 0) {
            address += 2;
        }
        return address;
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
            return getTurnoutAddrFromData(a1, a2);
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
        if (this.isFeedbackMessage() && (turnout == 0 || turnout == 1)) {
            int a2 = this.getElement(2);
            // fake turnout id, used just internally. Just odd/even matters.
            return createFeedbackItem(turnout, a2).getTurnoutStatus();
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
        if (this.isFeedbackBroadcastMessage() && (turnout == 0 || turnout == 1)) {
            int a2 = this.getElement(startByte + 1);
            // fake turnout id, used just internally. Just odd/even matters.
            return createFeedbackItem(turnout, a2).getTurnoutStatus();
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
                return (a1 & 0xff);
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * Returns the number of feedback items in the messages.
     * For accessory info replies, always returns 1. For broadcast, it returns the
     * number of feedback pairs. Returns 0 for non-feedback messages.
     * 
     * @return number of feedback pair items.
     */
    public final int getFeedbackMessageItems() {
        if (isFeedbackMessage()) {
            return 1;
        } else if (isFeedbackBroadcastMessage()) {
            return (this.getElement(0) & 0x0F) / 2;
        }
        return 0;
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
                return (a1 & 0xff);
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * Is this a feedback response message?
     * @return true if a feedback response, else false.
     */
    public boolean isFeedbackMessage() {
        return (this.getElement(0) == XNetConstants.ACC_INFO_RESPONSE);
    }

    /**
     * Is this a feedback broadcast message?
     * @return true if a feedback broadcast message, else false.
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

    public boolean isFeedbackMotionComplete(int startByte) {
        int messageType = getFeedbackMessageType(startByte);
        if (messageType == 1) {
            int a2 = getElement(startByte + 1);
            return ((a2 & 0x80) != 0x80);
        }
        return false;
    }

    /*
     * Next we have a few throttle related messages
     */

    /**
     * If this is a throttle-type message, return address.
     * Otherwise return -1. 
     * <p>
     * Note we only identify the command now;
     * the response to a request for status is not yet seen here.
     * @return address if throttle-type message, else -1.
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
     * @return true if throttle message. else false.
     */
    public boolean isThrottleMessage() {
        int message = this.getElement(0);
        return (message == XNetConstants.LOCO_INFO_NORMAL_UNIT
                || message == XNetConstants.LOCO_INFO_RESPONSE
                || message == XNetConstants.LOCO_INFO_MUED_UNIT
                || message == XNetConstants.LOCO_INFO_MU_ADDRESS
                || message == XNetConstants.LOCO_INFO_DH_UNIT
                || message == XNetConstants.LOCO_AVAILABLE_V1
                || message == XNetConstants.LOCO_AVAILABLE_V2
                || message == XNetConstants.LOCO_NOT_AVAILABLE_V1
                || message == XNetConstants.LOCO_NOT_AVAILABLE_V2);
    }

    /**
     * Does this message indicate the locomotive has been taken over by another
     * device?
     * @return true if take over message, else false.
     */
    public boolean isThrottleTakenOverMessage() {
        return (this.getElement(0) == XNetConstants.LOCO_INFO_RESPONSE
                && this.getElement(1) == XNetConstants.LOCO_NOT_AVAILABLE);
    }

    /**
     * Is this a consist message?
     * @return true if consist message, else false.
     */
    public boolean isConsistMessage() {
        int message = this.getElement(0);
        return (message == XNetConstants.LOCO_MU_DH_ERROR
                || message == XNetConstants.LOCO_DH_INFO_V1
                || message == XNetConstants.LOCO_DH_INFO_V2);
    }

    /* 
     * Finally, we have some commonly used routines that are used for 
     * checking specific, generic, response messages.
     */

    /**
     * In the interest of code reuse, the following function checks to see
     * if an XpressNet Message is the OK message (01 04 05).
     * @return true if an OK message, else false.
     */
    public boolean isOkMessage() {
        return (this.getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER
                && this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_SEND_SUCCESS);
    }

    /**
     * In the interest of code reuse, the following function checks to see
     * if an XpressNet Message is the timeslot restored message (01 07 06).
     * @return true if a time-slot restored message.
     */
    public boolean isTimeSlotRestored() {
        return (this.getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER
                && this.getElement(1) == XNetConstants.LIUSB_TIMESLOT_RESTORED);
    }

    /**
     * In the interest of code reuse, the following function checks to see
     * if an XpressNet Message is the Command Station no longer providing a
     * timeslot message (01 05 04).
     * @return true if a time-slot revoked message, else false.
     */
    public boolean isTimeSlotRevoked() {
        return (this.getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER
                && this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR);
    }

    /**
     * In the interest of code reuse, the following function checks to see
     * if an XpressNet Message is the Command Station Busy message (61 81 e3).
     * @return true if is a CS Busy message, else false.
     */
    public boolean isCSBusyMessage() {
        return (this.getElement(0) == XNetConstants.CS_INFO
                && this.getElement(1) == XNetConstants.CS_BUSY);
    }


    /**
     * In the interest of code reuse, the following function checks to see
     * if an XpressNet Message is the Command Station Transfer Error
     * message (61 80 e1).
     * @return if CS Transfer error, else false.
     */
    public boolean isCSTransferError() {
        return (this.getElement(0) == XNetConstants.CS_INFO
                && this.getElement(1) == XNetConstants.CS_TRANSFER_ERROR);
    }

    /**
     * In the interest of code reuse, the following function checks to see
     * if an XpressNet Message is the not supported Error
     * message (61 82 e3).
     * @return true if unsupported error, else false.
     */
    public boolean isUnsupportedError() {
        return (this.getElement(0) == XNetConstants.CS_INFO
                && this.getElement(1) == XNetConstants.CS_NOT_SUPPORTED);
    }

    /**
     * In the interest of code reuse, the following function checks to see
     * if an XpressNet Message is a communications error message.
     * <p>
     * The errors handled are:
     *  01 01 00  -- Error between interface and the PC
     *  01 02 03  -- Error between interface and the Command Station
     *  01 03 02  -- Unknown Communications Error
     *  01 06 07  -- LI10x Buffer Overflow
     *  01 0A 0B  -- LIUSB only. Request resend of data.
     * @return true if comm error message, else false.
     */
    public boolean isCommErrorMessage() {
        return (this.getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER
                && (this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_UNKNOWN_DATA_ERROR
                || this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_CS_DATA_ERROR
                || this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_PC_DATA_ERROR
                || this.getElement(1) == XNetConstants.LIUSB_RETRANSMIT_REQUEST
                || this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_BUFFER_OVERFLOW)
                || this.isTimeSlotErrorMessage());
    }

    /**
     * In the interest of code reuse, the following function checks to see
     * if an XpressNet Message is a communications error message.
     * <p>
     * The errors handled are:
     *  01 05 04  -- Timeslot Error
     *  01 07 06  -- Timeslot Restored
     *  01 08 09  -- Data sent while there is no Timeslot
     * @return true if time slot error, else false.
     */
    public boolean isTimeSlotErrorMessage() {
        return (this.getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER
                && (this.getElement(1) == XNetConstants.LIUSB_REQUEST_SENT_WHILE_NO_TIMESLOT
                || this.getElement(1) == XNetConstants.LIUSB_TIMESLOT_RESTORED
                || this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR));
    }


    /**
     * Is this message a service mode response?
     * @return true if a service mode response, else false.
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
     * @return true if register or paged mode programming response, else false.
     */
    public boolean isPagedModeResponse() {
        return (getElement(0) == XNetConstants.CS_SERVICE_MODE_RESPONSE
                && getElement(1) == XNetConstants.CS_SERVICE_REG_PAGE_RESPONSE);
    }

    /**
     * Is this message a direct CV mode programming response?
     * @return true if direct CV mode programming response, else false.
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
     * @return true if the message is an unsolicited message
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
                || this.isThrottleTakenOverMessage());
    }

    /**
     * Resets the unsolicited feedback flag. If the reply was not a feedback,
     * or was received as a broadcast - unsolicited from the command station, 
     * this method  <b>will not cause</b> the {@link #isUnsolicited()} to 
     * return {@code false}.  
     * <p>
     * Messages sent as unsolicited by the command station can not be turned 
     * to solicited.
     * @deprecated since 4.21.1 without replacement
     */
    @Deprecated
    public final void resetUnsolicited() {
        // method deprecated
    }
    
    /**
     * Mask to identify a turnout feedback + correct nibble. Turnout types differ in
     * 6th bit, so it's left out (is variable).
     */
    private static final int FEEDBACK_TURNOUT_MASK = 0b0101_0000;
    
    /**
     * Mask to identify a feedback module + correct nibble. Turnout modules have
     * type exactly 2.
     */
    private static final int FEEDBACK_MODULE_MASK  = 0b0111_0000;
    
    /**
     * The value of "feedback module" type. 
     */
    private static final int FEEDBACK_TYPE_FBMODULE = 0b0100_0000;
    
    /**
     * Bit that indicates the higher nibble in module or turnout feedback
     */
    private static final int FEEDBACK_HIGH_NIBBLE = 0b0001_0000;
    
    private int findFeedbackData(int baseAddress, int selector, int mask) {
        if (isFeedbackMessage()) {
            // shorctcut for single-item msg
            int data = getElement(2);
            if (getElement(1) == baseAddress &&
                (data & mask) == selector) {
                return data;
            }
        } else {
            int start = 1;
            for (int cnt = getFeedbackMessageItems(); cnt > 0; cnt--, start += 2) {
                int data = getElement(start + 1);
                if (getElement(start) == baseAddress &&
                    (data & mask) == selector) {
                    return data;
                }
            }
        }
        return -1;
    }

    /**
     * Returns value of the given feedback module bit. Returns {@link Optional}
     * that is non-empty, if the feedback was present. The Optional's value indicates the
     * feedback state.
     * 
     * @param sensorNumber the sensor bit ID
     * @return optional sensor state.
     */
    @CheckForNull
    public Boolean selectModuleFeedback(int sensorNumber) {
        if (!isFeedbackBroadcastMessage() || sensorNumber == 0 || sensorNumber >= 1024) {
            return null;
        }
        // feedback address directly addresses 8-bit module, XpressNet spec 3.0:2.1.11.
        int s = sensorNumber - 1;
        int baseAddress = (s / 8);
        int selector2 = (s & 0x04) != 0 ? 
                FEEDBACK_TYPE_FBMODULE | FEEDBACK_HIGH_NIBBLE : 
                FEEDBACK_TYPE_FBMODULE;
        int res = findFeedbackData(baseAddress, selector2, FEEDBACK_MODULE_MASK);
        return res == -1 ? null : 
                (res & (1 << (s % 4))) > 0;
    }
    
    /**
     * Calls processor for turnout's feedback, returns the processor's outcome.
     * Searches for the turnout feedback for the given accessory. If found,
     * runs a processor on the feedback item, and returns its Boolean result.
     * <p>
     * Returns {@code false}, if matching feedback is not found.
     * @param accessoryNumber the turnout number
     * @param proc the processor
     * @return {@code false} if feedback was not found, or a result of {@code proc()}.
     */
    public boolean onTurnoutFeedback(int accessoryNumber, Function<FeedbackItem, Boolean> proc) {
        return selectTurnoutFeedback(accessoryNumber).map(proc).orElse(false);
    }

    /**
     * Selects a matching turnout feedback. Finds turnout feedback for the given {@code accessoryNumber}.
     * Returns an encapsulated feedback, that can be inspected. If no matching feedback is
     * present, returns empty {@link Optional}.
     * @param accessoryNumber the turnout number
     * @return optional feedback item.
     */
    @Nonnull
    public Optional<FeedbackItem> selectTurnoutFeedback(int accessoryNumber) {
        // shortcut for single-item messages.
        if (!isFeedbackBroadcastMessage() || accessoryNumber <= 0 || accessoryNumber >= 1024) {
            return Optional.empty();
        }
        int a = accessoryNumber - 1;
        int base = (a / 4);
        // the mask makes the turnout feedback type bit irrelevant
        int selector2 = (a & 0x02) != 0 ? FEEDBACK_HIGH_NIBBLE : 0;
        int r = findFeedbackData(base, selector2, FEEDBACK_TURNOUT_MASK);
        if (r == -1) {
            return Optional.empty();
        }
        FeedbackItem item = new FeedbackItem(this, accessoryNumber, r);
        return Optional.of(item);
    }
    
    protected final FeedbackItem createFeedbackItem(int n, int d) {
        return new FeedbackItem(this, n, d);
    }

    /**
     * @return a string representation of the reply suitable for display in the
     * XpressNet monitor.
     */
    @Override
    public String toMonitorString(){
        StringBuilder text;
        // First, Decode anything that is sent by the LI10x, and
        // not the command station
        
        if(getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER){
            switch(this.getElement(1)) {
              case XNetConstants.LI_MESSAGE_RESPONSE_PC_DATA_ERROR:
                 text = new StringBuilder(Bundle.getMessage("XNetReplyErrorPCtoLI"));
                 break;
              case XNetConstants.LI_MESSAGE_RESPONSE_CS_DATA_ERROR:
                 text = new StringBuilder(Bundle.getMessage("XNetReplyErrorLItoCS"));
                 break;
              case XNetConstants.LI_MESSAGE_RESPONSE_UNKNOWN_DATA_ERROR:
                 text = new StringBuilder(Bundle.getMessage("XNetReplyErrorUnknown"));
                 break;
              case XNetConstants.LI_MESSAGE_RESPONSE_SEND_SUCCESS:
                 text = new StringBuilder(Bundle.getMessage("XNetReplyOkMessage"));
                 break;
              case XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR:
                 text = new StringBuilder(Bundle.getMessage("XNetReplyErrorNoTimeSlot"));
                 break;
              case XNetConstants.LI_MESSAGE_RESPONSE_BUFFER_OVERFLOW:
                 text = new StringBuilder(Bundle.getMessage("XNetReplyErrorBufferOverflow"));
                 break;
              case XNetConstants.LIUSB_TIMESLOT_RESTORED:
                 text = new StringBuilder(Bundle.getMessage("XNetReplyTimeSlotRestored"));
                 break;
              case XNetConstants.LIUSB_REQUEST_SENT_WHILE_NO_TIMESLOT:
                 text = new StringBuilder(Bundle.getMessage("XNetReplyRequestSentWhileNoTimeslot"));
                 break;
              case XNetConstants.LIUSB_BAD_DATA_IN_REQUEST:
                 text = new StringBuilder(Bundle.getMessage("XNetReplyBadDataInRequest"));
                 break;
              case XNetConstants.LIUSB_RETRANSMIT_REQUEST:
                 text = new StringBuilder(Bundle.getMessage("XNetReplyRetransmitRequest"));
                 break;
              default:
                 text = new StringBuilder(toString());
           }
        } else if (getElement(0) == XNetConstants.LI_VERSION_RESPONSE) {
            text = new StringBuilder(Bundle.getMessage("XNetReplyLIVersion", (getElementBCD(1).floatValue()) / 10, (getElementBCD(2).floatValue()) / 10));
        } else if (getElement(0) == XNetConstants.LI101_REQUEST) {
            // The request and response for baud rate look the same,
            // so we need this for both incoming and outgoing directions
            switch (getElement(1)) {
                case XNetConstants.LI101_REQUEST_ADDRESS:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyLIAddress", getElement(2)));
                    break;
                case XNetConstants.LI101_REQUEST_BAUD:
                    switch (getElement(2)) {
                        case 1:
                            text = new StringBuilder(Bundle.getMessage(X_NET_REPLY_LI_BAUD, Bundle.getMessage("LIBaud19200")));
                            break;
                        case 2:
                            text = new StringBuilder(Bundle.getMessage(X_NET_REPLY_LI_BAUD, Bundle.getMessage("Baud38400")));
                            break;
                        case 3:
                            text = new StringBuilder(Bundle.getMessage(X_NET_REPLY_LI_BAUD, Bundle.getMessage("Baud57600")));
                            break;
                        case 4:
                            text = new StringBuilder(Bundle.getMessage(X_NET_REPLY_LI_BAUD, Bundle.getMessage("Baud115200")));
                            break;
                        default:
                            text = new StringBuilder(Bundle.getMessage(X_NET_REPLY_LI_BAUD, Bundle.getMessage("BaudOther")));
                    }
                    break;
                default:
                    text = new StringBuilder(toString());
            }
            /* Next, check the "CS Info" messages */
        } else if (getElement(0) == XNetConstants.CS_INFO) {
            switch (getElement(1)) {
                case XNetConstants.BC_NORMAL_OPERATIONS:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyBCNormalOpsResumed"));
                    break;
                case XNetConstants.BC_EVERYTHING_OFF:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyBCEverythingOff"));
                    break;
                case XNetConstants.BC_SERVICE_MODE_ENTRY:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyBCServiceEntry"));
                    break;
                case XNetConstants.PROG_SHORT_CIRCUIT:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyServiceModeShort"));
                    break;
                case XNetConstants.PROG_BYTE_NOT_FOUND:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyServiceModeDataByteNotFound"));
                    break;
                case XNetConstants.PROG_CS_BUSY:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyServiceModeCSBusy"));
                    break;
                case XNetConstants.PROG_CS_READY:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyServiceModeCSReady"));
                    break;
                case XNetConstants.CS_BUSY:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyCSBusy"));
                    break;
                case XNetConstants.CS_NOT_SUPPORTED:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyCSNotSupported"));
                    break;
                case XNetConstants.CS_TRANSFER_ERROR:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyCSTransferError"));
                    break;
                /* The remaining cases are for a Double Header or MU Error */
                case XNetConstants.CS_DH_ERROR_NON_OP:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyV1DHErrorNotOperated"));
                    break;
                case XNetConstants.CS_DH_ERROR_IN_USE:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyV1DHErrorInUse"));
                    break;
                case XNetConstants.CS_DH_ERROR_ALREADY_DH:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyV1DHErrorAlreadyDH"));
                    break;
                case XNetConstants.CS_DH_ERROR_NONZERO_SPD:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyV1DHErrorNonZeroSpeed"));
                    break;
                default:
                    text = new StringBuilder(toString());
            }
        } else if (getElement(0) == XNetConstants.BC_EMERGENCY_STOP
                && getElement(1) == XNetConstants.BC_EVERYTHING_STOP) {
            text = new StringBuilder(Bundle.getMessage("XNetReplyBCEverythingStop"));
            /* Followed by Service Mode responses */
        } else if (getElement(0) == XNetConstants.CS_SERVICE_MODE_RESPONSE) {
            if (isDirectModeResponse()) {
                text = new StringBuilder(Bundle.getMessage("XNetReplyServiceModeDirectResponse", getServiceModeCVNumber(), getServiceModeCVValue()));
            } else if (isPagedModeResponse()) {
                text = new StringBuilder(Bundle.getMessage("XNetReplyServiceModePagedResponse", getServiceModeCVNumber(), getServiceModeCVValue()));
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
                text = new StringBuilder(Bundle.getMessage("XNetReplyCSVersion", (getElementBCD(2).floatValue()) / 10, typeString));
            } else {
                text = new StringBuilder(toString());
            }
 /* We want to look at responses to specific requests made to the Command Station */
        } else if (getElement(0) == XNetConstants.CS_REQUEST_RESPONSE) {
            if (getElement(1) == XNetConstants.CS_STATUS_RESPONSE) {
                text = new StringBuilder(Bundle.getMessage("XNetReplyCSStatus") + " ");
                int statusByte = getElement(2);
                if ((statusByte & 0x01) == 0x01) {
                    // Command station is in Emergency Off Mode
                    text.append(Bundle.getMessage("XNetCSStatusEmergencyOff")).append("; ");
                }
                if ((statusByte & 0x02) == 0x02) {
                    // Command station is in Emergency Stop Mode
                    text.append(Bundle.getMessage("XNetCSStatusEmergencyStop")).append("; ");
                }
                if ((statusByte & 0x08) == 0x08) {
                    // Command station is in Service Mode
                    text.append(Bundle.getMessage("XNetCSStatusServiceMode")).append("; ");
                }
                if ((statusByte & 0x40) == 0x40) {
                    // Command station is in Power Up Mode
                    text.append(Bundle.getMessage("XNetCSStatusPoweringUp")).append("; ");
                }
                if ((statusByte & 0x04) == 0x04) {
                    text.append(Bundle.getMessage("XNetCSStatusPowerModeAuto")).append("; ");
                } else {
                    text.append(Bundle.getMessage("XNetCSStatusPowerModeManual")).append("; ");
                }
                if ((statusByte & 0x80) == 0x80) {
                    // Command station has a experienced a ram check error
                    text.append(Bundle.getMessage("XNetCSStatusRamCheck"));
                }
            } else if (getElement(1) == XNetConstants.CS_SOFTWARE_VERSION) {
                /* This is a Software version response for XpressNet
                 Version 1 or 2 */
                text = new StringBuilder(Bundle.getMessage("XNetReplyCSVersionV1", (getElementBCD(2).floatValue()) / 10));
            } else {
                text = new StringBuilder(toString());
            }

            // MU and Double Header Related Responses
        } else if (getElement(0) == XNetConstants.LOCO_MU_DH_ERROR) {
            switch (getElement(1)) {
                case 0x81:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyDHErrorNotOperated"));
                    break;
                case 0x82:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyDHErrorInUse"));
                    break;
                case 0x83:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyDHErrorAlreadyDH"));
                    break;
                case 0x84:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyDHErrorNonZeroSpeed"));
                    break;
                case 0x85:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyDHErrorLocoNotMU"));
                    break;
                case 0x86:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyDHErrorLocoNotMUBase"));
                    break;
                case 0x87:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyDHErrorCanNotDelete"));
                    break;
                case 0x88:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyDHErrorStackFull"));
                    break;
                default:
                    text = new StringBuilder(Bundle.getMessage("XNetReplyDHErrorOther", (getElement(1) - 0x80)));
            }
            // Loco Information Response Messages
        } else if (getElement(0) == XNetConstants.LOCO_INFO_NORMAL_UNIT) {
            if (getElement(1) == XNetConstants.LOCO_FUNCTION_STATUS_HIGH_MOM) {
                text = new StringBuilder(Bundle.getMessage("XNetReplyLocoStatus13Label") + " ");
                // message byte 3, contains F20,F19,F18,F17,F16,F15,F14,F13
                int element3 = getElement(2);
                // message byte 4, contains F28,F27,F26,F25,F24,F23,F22,F21
                int element4 = getElement(3);
                text.append(parseFunctionHighMomentaryStatus(element3, element4));
            } else {
                text = new StringBuilder(Bundle.getMessage("XNetReplyLocoNormalLabel") + ",");
                text.append(parseSpeedAndDirection(getElement(1), getElement(2))).append(" ");
                // message byte 4, contains F0,F1,F2,F3,F4
                int element3 = getElement(3);
                // message byte 5, contains F12,F11,F10,F9,F8,F7,F6,F5
                int element4 = getElement(4);
                text.append(parseFunctionStatus(element3, element4));
            }
        } else if (getElement(0) == XNetConstants.LOCO_INFO_MUED_UNIT) {
            if (getElement(1) == 0xF8) {
                // This message is a Hornby addition to the protocol
                // indicating the speed and direction of a locomoitve
                // controlled by the elite's built in throttles
                text = new StringBuilder(Bundle.getMessage("XNetReplyLocoEliteSLabel") + " ");
                text.append(LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)));
                text.append(",").append(parseSpeedAndDirection(getElement(4), getElement(5))).append(" ");
            } else if (getElement(1) == 0xF9) {
                // This message is a Hornby addition to the protocol
                // indicating the function on/off status of a locomoitve
                // controlled by the elite's built in throttles
                text = new StringBuilder(Bundle.getMessage("XNetReplyLocoEliteFLabel") + " ");
                text.append(LenzCommandStation.calcLocoAddress(getElement(2), getElement(3))).append(" ");
                // message byte 5, contains F0,F1,F2,F3,F4
                int element4 = getElement(4);
                // message byte 5, contains F12,F11,F10,F9,F8,F7,F6,F5
                int element5 = getElement(5);
                text.append(parseFunctionStatus(element4, element5));
            } else {
                text = new StringBuilder(Bundle.getMessage("XNetReplyLocoMULabel") + ",");
                text.append(parseSpeedAndDirection(getElement(1), getElement(2)));
                // message byte 4, contains F0,F1,F2,F3,F4
                int element3 = getElement(3);
                // message byte 5, contains F12,F11,F10,F9,F8,F7,F6,F5
                int element4 = getElement(4);
                text.append(parseFunctionStatus(element3, element4));
            }
        } else if (getElement(0) == XNetConstants.LOCO_INFO_MU_ADDRESS) {
            text = new StringBuilder(Bundle.getMessage("XNetReplyLocoMUBaseLabel") + ",");
            text.append(parseSpeedAndDirection(getElement(1), getElement(2))).append(" ");
        } else if (getElement(0) == XNetConstants.LOCO_INFO_DH_UNIT) {
            text = new StringBuilder(Bundle.getMessage("XNetReplyLocoDHLabel") + ",");
            text.append(parseSpeedAndDirection(getElement(1), getElement(2))).append(" ");
            // message byte 4, contains F0,F1,F2,F3,F4
            int element3 = getElement(3);
            // message byte 5, contains F12,F11,F10,F9,F8,F7,F6,F5
            int element4 = getElement(4);
            text.append(parseFunctionStatus(element3, element4));
            text.append(" ").append(Bundle.getMessage("XNetReplyLoco2DHLabel")).append(" ");
            text.append(LenzCommandStation.calcLocoAddress(getElement(5), getElement(6)));
        } else if (getElement(0) == XNetConstants.LOCO_INFO_RESPONSE) {
            text = new StringBuilder(Bundle.getMessage("XNetReplyLocoLabel") + " ");
            switch (getElement(1)) {
                case XNetConstants.LOCO_SEARCH_RESPONSE_N:
                    text.append(Bundle.getMessage("XNetReplySearchNormalLabel")).append(" ");
                    text.append(getThrottleMsgAddr());
                    break;
                case XNetConstants.LOCO_SEARCH_RESPONSE_DH:
                    text.append(Bundle.getMessage("XNetReplySearchDHLabel")).append(" ");
                    text.append(getThrottleMsgAddr());
                    break;
                case XNetConstants.LOCO_SEARCH_RESPONSE_MU_BASE:
                    text.append(Bundle.getMessage("XNetReplySearchMUBaseLabel")).append(" ");
                    text.append(getThrottleMsgAddr());
                    break;
                case XNetConstants.LOCO_SEARCH_RESPONSE_MU:
                    text.append(Bundle.getMessage("XNetReplySearchMULabel")).append(" ");
                    text.append(getThrottleMsgAddr());
                    break;
                case XNetConstants.LOCO_SEARCH_NO_RESULT:
                    text.append(Bundle.getMessage("XNetReplySearchFailedLabel")).append(" ");
                    text.append(getThrottleMsgAddr());
                    break;
                case XNetConstants.LOCO_NOT_AVAILABLE:
                    text.append(Bundle.getMessage(RS_TYPE)).append(" ");
                    text.append(getThrottleMsgAddr()).append(" ");
                    text.append(Bundle.getMessage("XNetReplyLocoOperated"));
                    break;
                case XNetConstants.LOCO_FUNCTION_STATUS:
                    locoFunctionStatusText(text);
                    break;
                case XNetConstants.LOCO_FUNCTION_STATUS_HIGH:
                    locoFunctionStatusHighText(text);
                    break;
                default:
                    text = new StringBuilder(toString());
            }
            // Feedback Response Messages
        } else if (isFeedbackBroadcastMessage()) {
            text = new StringBuilder().append(Bundle.getMessage("XNetReplyFeedbackLabel")).append(" ");
            int numDataBytes = getElement(0) & 0x0f;
            for (int i = 1; i < numDataBytes; i += 2) {
                switch (getFeedbackMessageType(i)) {
                    case 0:
                        text.append(getTurnoutReplyMonitorString(i, "TurnoutWoFeedback"));
                        break;
                    case 1:
                        text.append(getTurnoutReplyMonitorString(i, "TurnoutWFeedback"));
                        break;
                    case 2:
                        text.append(Bundle.getMessage("XNetReplyFeedbackEncoder")).append(" ").append(getFeedbackEncoderMsgAddr(i) + 1);
                        boolean highnibble = ((getElement(i + 1) & 0x10) == 0x10);
                        text.append(" ").append(Bundle.getMessage(X_NET_REPLY_CONTACT_LABEL)).append(" ").append(highnibble ? 5 : 1);

                        text.append(" ").append(Bundle.getMessage(MAKE_LABEL, Bundle.getMessage(COLUMN_STATE))).append(" ")
                                .append(((getElement(i + 1) & 0x01) == 0x01) ? Bundle.getMessage(POWER_STATE_ON) : Bundle.getMessage(POWER_STATE_OFF));
                        text.append("; ").append(Bundle.getMessage(X_NET_REPLY_CONTACT_LABEL)).append(" ").append(highnibble ? 6 : 2);

                        text.append(" ").append(Bundle.getMessage(MAKE_LABEL, Bundle.getMessage(COLUMN_STATE))).append(" ")
                                .append(((getElement(i + 1) & 0x02) == 0x02) ? Bundle.getMessage(POWER_STATE_ON) : Bundle.getMessage(POWER_STATE_OFF));
                        text.append("; ").append(Bundle.getMessage(X_NET_REPLY_CONTACT_LABEL)).append(" ").append(highnibble ? 7 : 3);

                        text.append(" ").append(Bundle.getMessage(MAKE_LABEL, Bundle.getMessage(COLUMN_STATE))).append(" ")
                                .append(((getElement(i + 1) & 0x04) == 0x04) ? Bundle.getMessage(POWER_STATE_ON) : Bundle.getMessage(POWER_STATE_OFF));
                        text.append("; ").append(Bundle.getMessage(X_NET_REPLY_CONTACT_LABEL)).append(" ").append(highnibble ? 8 : 4);

                        text.append(" ").append(Bundle.getMessage(MAKE_LABEL, Bundle.getMessage(COLUMN_STATE))).append(" ")
                                .append(((getElement(i + 1) & 0x08) == 0x08) ? Bundle.getMessage(POWER_STATE_ON) : Bundle.getMessage(POWER_STATE_OFF));
                        text.append("; ");
                        break;
                    default:
                        text.append(getElement(i)).append(" ").append(getElement(i + 1));
                }
            }
        } else {
            text = new StringBuilder(toString());
        }
        return text.toString();
    }

    private void locoFunctionStatusHighText(StringBuilder text) {
        text.append(Bundle.getMessage(RS_TYPE)).append(" ");
        text.append(Bundle.getMessage("XNetReplyF13StatusLabel")).append(" ");
        // message byte 3, contains F20,F19,F18,F17,F16,F15,F14,F13
        int element3 = getElement(2);
        // message byte 4, contains F28,F27,F26,F25,F24,F23,F22,F21
        int element4 = getElement(3);
        text.append(parseFunctionHighStatus(element3, element4));
    }

    private void locoFunctionStatusText(StringBuilder text) {
        text.append(Bundle.getMessage(RS_TYPE)).append(" "); // "Locomotive", key in NBBundle, shared with Operations
        text.append(Bundle.getMessage("XNetReplyFStatusLabel")).append(" ");
        // message byte 3, contains F0,F1,F2,F3,F4
        int element3 = getElement(2);
        // message byte 4, contains F12,F11,F10,F9,F8,F7,F6,F5
        int element4 = getElement(3);
        text.append(parseFunctionMomentaryStatus(element3, element4));
    }

    private String getTurnoutReplyMonitorString(int startByte, String typeBundleKey) {
        StringBuilder text = new StringBuilder();
        int turnoutMsgAddr = getTurnoutMsgAddr(startByte);
        Optional<FeedbackItem> feedBackOdd = selectTurnoutFeedback(turnoutMsgAddr);
        if(feedBackOdd.isPresent()){
            FeedbackItem feedbackItem = feedBackOdd.get();
            text.append(singleTurnoutMonitorMessage(Bundle.getMessage(typeBundleKey), turnoutMsgAddr, feedbackItem));
            text.append(";");
            FeedbackItem pairedItem = feedbackItem.pairedAccessoryItem();
            text.append(singleTurnoutMonitorMessage("", turnoutMsgAddr + 1, pairedItem));

        }
        return text.toString();
    }

    private String singleTurnoutMonitorMessage(String prefix, int turnoutMsgAddr, FeedbackItem feedbackItem) {
        StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append(prefix).append(" ")
                .append(Bundle.getMessage(MAKE_LABEL, Bundle.getMessage(BEAN_NAME_TURNOUT))).append(" ")
                .append(turnoutMsgAddr).append(" ").append(Bundle.getMessage(MAKE_LABEL, Bundle.getMessage(COLUMN_STATE))).append(" ");
        switch (feedbackItem.getAccessoryStatus()){
            case 0:
                outputBuilder.append(Bundle.getMessage(X_NET_REPLY_NOT_OPERATED)); // last items on line, no trailing space
               break;
            case 1:
                outputBuilder.append(Bundle.getMessage(X_NET_REPLY_THROWN_LEFT));
               break;
            case 2:
                outputBuilder.append(Bundle.getMessage(X_NET_REPLY_THROWN_RIGHT));
                break;
            default:
                outputBuilder.append(Bundle.getMessage(X_NET_REPLY_INVALID));
        }
        if(feedbackItem.getType()==1){
            outputBuilder.append(" ");
            if(feedbackItem.isMotionComplete()){
                outputBuilder.append(Bundle.getMessage("XNetReplyMotionComplete"));
            } else {
                outputBuilder.append(Bundle.getMessage("XNetReplyMotionIncomplete"));
            }
        }
        return outputBuilder.toString();
    }

    /**
     * Parse the speed step and the direction information for a locomotive.
     *
     * @param element1 contains the speed step mode designation and
     * availability information
     * @param element2 contains the data byte including the step mode and
     * availability information
     * @return readable version of message
     */
    protected String parseSpeedAndDirection(int element1, int element2) {
        String text = "";
        int speedVal;
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
            text += Bundle.getMessage(SPEED_STEP_MODE_X, 128) + ",";
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
            text += Bundle.getMessage(SPEED_STEP_MODE_X, 28) + ",";
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
            text += Bundle.getMessage(SPEED_STEP_MODE_X, 27) + ",";
        } else {
            // Assume we're in 14 speed step mode.
            speedVal = (element2 & 0x0F);
            if (speedVal >= 1) {
                speedVal -= 1;
            } else {
                speedVal = 0;
            }
            text += Bundle.getMessage(SPEED_STEP_MODE_X, 14) + ",";
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
     * @return readable version of message
     */
    protected String parseFunctionStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x10) != 0) {
            text += "F0 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F0 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x01) != 0) {
            text += "F1 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F1 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F2 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F2 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F3 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F3 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F4 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F4 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F5 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F5 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F6 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F6 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F7 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F7 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F8 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F8 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F9 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F9 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F10 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F10 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F11 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F11 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F12 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F12 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        return (text);
    }

    /**
     * Parse the status of functions F13-F28.
     *
     * @param element3 contains F20,F19,F18,F17,F16,F15,F14,F13
     * @param element4 contains F28,F27,F26,F25,F24,F23,F22,F21
     * @return readable version of message
     */
    protected String parseFunctionHighStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x01) != 0) {
            text += "F13 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F13 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F14 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F14 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F15 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F15 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F16 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F16 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x10) != 0) {
            text += "F17 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F17 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x20) != 0) {
            text += "F18 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F18 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x40) != 0) {
            text += "F19 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F19 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element3 & 0x80) != 0) {
            text += "F20 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F20 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F21 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F21 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F22 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F22 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F23 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F23 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F24 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F24 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F25 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F25 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F26 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F26 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F27 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F27 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F28 " + Bundle.getMessage(POWER_STATE_ON) + "; ";
        } else {
            text += "F28 " + Bundle.getMessage(POWER_STATE_OFF) + "; ";
        }
        return (text);
    }
    /**
     * Parse the Momentary status of functions.
     *
     * @param element3 contains the data byte including F0,F1,F2,F3,F4
     * @param element4 contains F12,F11,F10,F9,F8,F7,F6,F5
     * @return readable version of message
     */
    protected String parseFunctionMomentaryStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x10) != 0) {
            text += "F0 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F0 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x01) != 0) {
            text += "F1 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F1 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F2 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F2 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F3 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F3 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F4 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F4 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F5 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F5 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F6 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F6 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F7 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F7 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F8 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F8 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F9 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F9 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F10 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F10 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F11 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F11 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F12 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F12 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        return (text);
    }

    /**
     * Parse the Momentary sytatus of functions F13-F28.
     *
     * @param element3 contains F20,F19,F18,F17,F16,F15,F14,F13
     * @param element4 contains F28,F27,F26,F25,F24,F23,F22,F21
     * @return readable version of message
     */
    protected String parseFunctionHighMomentaryStatus(int element3, int element4) {
        String text = "";
        if ((element3 & 0x01) != 0) {
            text += "F13 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F13 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x02) != 0) {
            text += "F14 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F14 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x04) != 0) {
            text += "F15 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F15 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x08) != 0) {
            text += "F16 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F16 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x10) != 0) {
            text += "F17 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F17 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x20) != 0) {
            text += "F18 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F18 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x40) != 0) {
            text += "F19 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F19 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element3 & 0x80) != 0) {
            text += "F20 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F20 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x01) != 0) {
            text += "F21 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F21 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x02) != 0) {
            text += "F22 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F22 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x04) != 0) {
            text += "F23 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F23 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x08) != 0) {
            text += "F24 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F24 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x10) != 0) {
            text += "F25 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F25 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x20) != 0) {
            text += "F26 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F26 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x40) != 0) {
            text += "F27 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F27 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        if ((element4 & 0x80) != 0) {
            text += "F28 " + Bundle.getMessage(FUNCTION_MOMENTARY) + "; ";
        } else {
            text += "F28 " + Bundle.getMessage(FUNCTION_CONTINUOUS) + "; ";
        }
        return (text);
    }

}
