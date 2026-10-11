package jmri.jmrix.lenz;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
     * XPressNet Ops Mode Reply message handling routines
     */

    boolean isOpsModeResultMessage() {
        return (getElement(0) == XNetConstants.CS_ADVANCED_INFO_RESPONSE
                && getElement(1) == XNetConstants.POM_RESULTS);
    }

    int getOpsModeResultAddress() {
        if (isOpsModeResultMessage()) {
            return LenzCommandStation.calcLocoAddress(getElement(2), getElement(3));
        }
        throw new IllegalArgumentException("Message is not an Ops Mode Result message");
    }

    int getOpsModeResultValue() {
        if (isOpsModeResultMessage()) {
            return getElement(4) & 0xFF;
        }
        throw new IllegalArgumentException("Message is not an Ops Mode Result message");
    }

    private static final List<XPressNetMessageFormatter> formatterList = new ArrayList<>();
    /**
     * @return a string representation of the reply suitable for display in the
     * XpressNet monitor.
     */
    @Override
    public String toMonitorString() {

        if (formatterList.isEmpty()) {
            try {
                Reflections reflections = new Reflections("jmri.jmrix");
                Set<Class<? extends XPressNetMessageFormatter>> f = reflections.getSubTypesOf(XPressNetMessageFormatter.class);
                for (Class<?> c : f) {
                    log.debug("Found formatter: {}", f.getClass().getName());
                    Constructor<?> ctor = c.getConstructor();
                    formatterList.add((XPressNetMessageFormatter) ctor.newInstance());
                }
            } catch (NoSuchMethodException | SecurityException | InstantiationException |
                     IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                log.error("Error instantiating formatter", e);
            }
        }

        return formatterList.stream().filter(f -> f.handlesMessage(this)).findFirst().map(f -> f.formatMessage(this)).orElse(this.toString());
    }

    // initialize logging
    private static final Logger log = LoggerFactory.getLogger(XNetReply.class);

}
