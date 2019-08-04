package jmri.jmrix.lenz;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.SpeedStepMode;

/**
 * Represents a single command or response on the XpressNet.
 * <p>
 * Content is represented with ints to avoid the problems with sign-extension
 * that bytes have, and because a Java char is actually a variable number of
 * bytes in Unicode.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2003-2010
  *
 */
public class XNetMessage extends jmri.jmrix.AbstractMRMessage implements Serializable {

    static private int _nRetries = 5;

    /* According to the specification, XpressNet has a maximum timing
     interval of 500 milliseconds during normal communications */
    static protected final int XNetProgrammingTimeout = 10000;
    static private int XNetMessageTimeout = 5000;

    /**
     * Create a new object, representing a specific-length message.
     *
     * @param len Total bytes in message, including opcode and error-detection
     *            byte.  Valid values are 0 to 15 (0x0 to 0xF).
     */
    public XNetMessage(int len) {
        super(len);
        if (len > 15 ) {  // only check upper bound. Lower bound checked in
                          // super call.
            log.error("Invalid length in ctor: {}", len);
            throw new IllegalArgumentException("Invalid length in ctor: " + len);
        }
        setBinary(true);
        setRetries(_nRetries);
        setTimeout(XNetMessageTimeout);
        _nDataChars = len;
    }

    /**
     * Create a new object, that is a copy of an existing message.
     *
     * @param message an existing XpressNet message
     */
    public XNetMessage(XNetMessage message) {
        super(message);
        setBinary(true);
        setRetries(_nRetries);
        setTimeout(XNetMessageTimeout);
    }

    /**
     * Create an XNetMessage from an XNetReply.
     */
    public XNetMessage(XNetReply message) {
        super(message.getNumDataElements());
        setBinary(true);
        setRetries(_nRetries);
        setTimeout(XNetMessageTimeout);
        for (int i = 0; i < message.getNumDataElements(); i++) {
            setElement(i, message.getElement(i));
        }
    }

    /**
     * Create an XNetMessage from a String containing bytes.
     */
    public XNetMessage(String s) {
        setBinary(true);
        setRetries(_nRetries);
        setTimeout(XNetMessageTimeout);
        // gather bytes in result
        byte b[] = jmri.util.StringUtil.bytesFromHexString(s);
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

    // note that the opcode is part of the message, so we treat it
    // directly
    // WARNING: use this only with opcodes that have a variable number 
    // of arguments following included. Otherwise, just use setElement
    @Override
    public void setOpCode(int i) {
        if (i > 0xF || i < 0) {
            log.error("Opcode invalid: {}", i);
        }
        setElement(0, ((i * 16) & 0xF0) | ((getNumDataElements() - 2) & 0xF));
    }

    @Override
    public int getOpCode() {
        return (getElement(0) / 16) & 0xF;
    }

    /**
     * Get a String representation of the op code in hex.
     */
    @Override
    public String getOpCodeHex() {
        return "0x" + Integer.toHexString(getOpCode());
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
     */
    public Integer getElementBCD(int n) {
        return Integer.decode(Integer.toHexString(getElement(n)));
    }

    /**
     * Get the message length.
     */
    public int length() {
        return _nDataChars;
    }

    /**
     * Set the default number of retries for an XpressNet message.
     *
     * @param t number of retries to attempt
     */
    static public void setXNetMessageRetries(int t) {
        _nRetries = t;
    }

    /**
     * Set the default timeout for an XpressNet message.
     *
     * @param t Timeout in milliseconds
     */
    static public void setXNetMessageTimeout(int t) {
        XNetMessageTimeout = t;
    }

    /**
     * Most messages are sent with a reply expected, but
     * we have a few that we treat as though the reply is always
     * a broadcast message, because the reply usually comes to us 
     * that way.
     */
    @Override
    public boolean replyExpected() {
        return !broadcastReply;
    }

    private boolean broadcastReply = false;

    /**
     * Tell the traffic controller we expect this
     * message to have a broadcast reply.
     */
    public void setBroadcastReply() {
        broadcastReply = true;
    }

    // decode messages of a particular form
    // create messages of a particular form

    /**
     * Encapsulate an NMRA DCC packet in an XpressNet message.
     * <p>
     * On Current (v3.5) Lenz command stations, the Operations Mode
     *     Programming Request is implemented by sending a packet directly
     *     to the rails.  This packet is not checked by the XpressNet
     *     protocol, and is just the track packet with an added header
     *     byte.
     *     <p>
     *     NOTE: Lenz does not say this will work for anything but 5
     *     byte packets.
     */
    public static XNetMessage getNMRAXNetMsg(byte[] packet) {
        XNetMessage msg = new XNetMessage(packet.length + 2);
        msg.setOpCode((XNetConstants.OPS_MODE_PROG_REQ & 0xF0) >> 4);
        msg.setElement(1, 0x30);
        for (int i = 0; i < packet.length; i++) {
            msg.setElement((i + 2), packet[i] & 0xff);
        }
        msg.setParity();
        return (msg);
    }

    /* 
     * The next group of routines are used by Feedback and/or turnout 
     * control code.  These are used in multiple places within the code, 
     * so they appear here. 
     */

    /**
     * Generate a message to change turnout state.
     */
    public static XNetMessage getTurnoutCommandMsg(int pNumber, boolean pClose,
            boolean pThrow, boolean pOn) {
        XNetMessage l = new XNetMessage(4);
        l.setElement(0, XNetConstants.ACC_OPER_REQ);

        // compute address byte fields
        int hiadr = (pNumber - 1) / 4;
        int loadr = ((pNumber - 1) - hiadr * 4) * 2;
        // The MSB of the upper nibble is required to be set on
        // The rest of the upper nibble should be zeros.
        // The MSB of the lower nibble says weather or not the
        // accessory line should be "on" or "off"
        if (!pOn) {
            loadr |= 0x80;
        } else {
            loadr |= 0x88;
        }
        // If we are sending a "throw" command, we set the LSB of the 
        // lower nibble on, otherwise, we leave it "off".
        if (pThrow) {
            loadr |= 0x01;
        }

        // we don't know how to command both states right now!
        if (pClose & pThrow) {
            log.error("XpressNet turnout logic can't handle both THROWN and CLOSED yet");
        }
        // store and send
        l.setElement(1, hiadr);
        l.setElement(2, loadr);
        l.setParity(); // Set the parity bit

        return l;
    }

    /**
     * Generate a message to receive the feedback information for an upper or
     * lower nibble of the feedback address in question.
     */
    public static XNetMessage getFeedbackRequestMsg(int pNumber,
            boolean pLowerNibble) {
        XNetMessage l = new XNetMessage(4);
        l.setBroadcastReply();  // we the message reply as a broadcast message.
        l.setElement(0, XNetConstants.ACC_INFO_REQ);

        // compute address byte field
        l.setElement(1, (pNumber - 1) / 4);
        // The MSB of the upper nibble is required to be set on
        // The rest of the upper nibble should be zeros.
        // The LSB of the lower nibble says weather or not the
        // information request is for the upper or lower nibble.
        if (pLowerNibble) {
            l.setElement(2, 0x80);
        } else {
            l.setElement(2, 0x81);
        }
        l.setParity(); // Set the parity bit
        return l;
    }

    /* 
     * Next, we have some messages related to sending programming commands.
     */

    public static XNetMessage getServiceModeResultsMsg() {
        XNetMessage m = new XNetMessage(3);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(XNetProgrammingTimeout);
        m.setElement(0, XNetConstants.CS_REQUEST);
        m.setElement(1, XNetConstants.SERVICE_MODE_CSRESULT);
        m.setParity(); // Set the parity bit
        return m;
    }

    public static XNetMessage getExitProgModeMsg() {
        XNetMessage m = new XNetMessage(3);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setElement(0, XNetConstants.CS_REQUEST);
        m.setElement(1, XNetConstants.RESUME_OPS);
        m.setParity();
        return m;
    }

    public static XNetMessage getReadPagedCVMsg(int cv) {
        XNetMessage m = new XNetMessage(4);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(XNetProgrammingTimeout);
        m.setElement(0, XNetConstants.PROG_READ_REQUEST);
        m.setElement(1, XNetConstants.PROG_READ_MODE_PAGED);
        m.setElement(2, (0xff & cv));
        m.setParity(); // Set the parity bit
        return m;
    }

    public static XNetMessage getReadDirectCVMsg(int cv) {
        XNetMessage m = new XNetMessage(4);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(XNetProgrammingTimeout);
        m.setElement(0, XNetConstants.PROG_READ_REQUEST);
        if (cv < 0x0100) /* Use the version 3.5 command for CVs <= 256 */ {
            m.setElement(1, XNetConstants.PROG_READ_MODE_CV);
        } else if (cv == 0x0400) /* For CV1024, we need to send the version 3.6
         command for CVs 1 to 256, sending a 0 for the
         CV */ {
            m.setElement(1, XNetConstants.PROG_READ_MODE_CV_V36);
        } else /* and the version 3.6 command for CVs > 256 */ {
            m.setElement(1, XNetConstants.PROG_READ_MODE_CV_V36 | ((cv & 0x0300) >> 8));
        }
        m.setElement(2, (0xff & cv));
        m.setParity(); // Set the parity bit
        return m;
    }

    public static XNetMessage getWritePagedCVMsg(int cv, int val) {
        XNetMessage m = new XNetMessage(5);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(XNetProgrammingTimeout);
        m.setElement(0, XNetConstants.PROG_WRITE_REQUEST);
        m.setElement(1, XNetConstants.PROG_WRITE_MODE_PAGED);
        m.setElement(2, (0xff & cv));
        m.setElement(3, val);
        m.setParity(); // Set the parity bit
        return m;
    }

    public static XNetMessage getWriteDirectCVMsg(int cv, int val) {
        XNetMessage m = new XNetMessage(5);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(XNetProgrammingTimeout);
        m.setElement(0, XNetConstants.PROG_WRITE_REQUEST);
        if (cv < 0x0100) /* Use the version 3.5 command for CVs <= 256 */ {
            m.setElement(1, XNetConstants.PROG_WRITE_MODE_CV);
        } else if (cv == 0x0400) /* For CV1024, we need to send the version 3.6
         command for CVs 1 to 256, sending a 0 for the
         CV */ {
            m.setElement(1, XNetConstants.PROG_WRITE_MODE_CV_V36);
        } else /* and the version 3.6 command for CVs > 256 */ {
            m.setElement(1, XNetConstants.PROG_WRITE_MODE_CV_V36 | ((cv & 0x0300) >> 8));
        }
        m.setElement(2, (0xff & cv));
        m.setElement(3, val);
        m.setParity(); // Set the parity bit
        return m;
    }

    public static XNetMessage getReadRegisterMsg(int reg) {
        if (reg > 8) {
            log.error("register number too large: " + reg);
        }
        XNetMessage m = new XNetMessage(4);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(XNetProgrammingTimeout);
        m.setElement(0, XNetConstants.PROG_READ_REQUEST);
        m.setElement(1, XNetConstants.PROG_READ_MODE_REGISTER);
        m.setElement(2, (0x0f & reg));
        m.setParity(); // Set the parity bit
        return m;
    }

    public static XNetMessage getWriteRegisterMsg(int reg, int val) {
        if (reg > 8) {
            log.error("register number too large: " + reg);
        }
        XNetMessage m = new XNetMessage(5);
        m.setNeededMode(jmri.jmrix.AbstractMRTrafficController.PROGRAMINGMODE);
        m.setTimeout(XNetProgrammingTimeout);
        m.setElement(0, XNetConstants.PROG_WRITE_REQUEST);
        m.setElement(1, XNetConstants.PROG_WRITE_MODE_REGISTER);
        m.setElement(2, (0x0f & reg));
        m.setElement(3, val);
        m.setParity(); // Set the parity bit
        return m;
    }

    public static XNetMessage getWriteOpsModeCVMsg(int AH, int AL, int cv, int val) {
        XNetMessage m = new XNetMessage(8);
        m.setElement(0, XNetConstants.OPS_MODE_PROG_REQ);
        m.setElement(1, XNetConstants.OPS_MODE_PROG_WRITE_REQ);
        m.setElement(2, AH);
        m.setElement(3, AL);
        /* Element 4 is 0xEC + the upper two  bits of the 10 bit CV address.
         NOTE: This is the track packet CV, not the human readable CV, so 
         its value actually is one less than what we normally think of it as.*/
        int temp = (cv - 1) & 0x0300;
        temp = temp / 0x00FF;
        m.setElement(4, 0xEC + temp);
        /* Element 5 is the lower 8 bits of the cv */
        m.setElement(5, ((0x00ff & cv) - 1));
        m.setElement(6, val);
        m.setParity(); // Set the parity bit
        return m;
    }

    public static XNetMessage getVerifyOpsModeCVMsg(int AH, int AL, int cv, int val) {
        XNetMessage m = new XNetMessage(8);
        m.setElement(0, XNetConstants.OPS_MODE_PROG_REQ);
        m.setElement(1, XNetConstants.OPS_MODE_PROG_WRITE_REQ);
        m.setElement(2, AH);
        m.setElement(3, AL);
        /* Element 4 is 0xE4 + the upper two  bits of the 10 bit CV address.
         NOTE: This is the track packet CV, not the human readable CV, so 
         its value actually is one less than what we normally think of it as.*/
        int temp = (cv - 1) & 0x0300;
        temp = temp / 0x00FF;
        m.setElement(4, 0xE4 + temp);
        /* Element 5 is the lower 8 bits of the cv */
        m.setElement(5, ((0x00ff & cv) - 1));
        m.setElement(6, val);
        m.setParity(); // Set the parity bit
        return m;
    }

    public static XNetMessage getBitWriteOpsModeCVMsg(int AH, int AL, int cv, int bit, boolean value) {
        XNetMessage m = new XNetMessage(8);
        m.setElement(0, XNetConstants.OPS_MODE_PROG_REQ);
        m.setElement(1, XNetConstants.OPS_MODE_PROG_WRITE_REQ);
        m.setElement(2, AH);
        m.setElement(3, AL);
        /* Element 4 is 0xE8 + the upper two  bits of the 10 bit CV address.
         NOTE: This is the track packet CV, not the human readable CV, so 
         its value actually is one less than what we normally think of it as.*/
        int temp = (cv - 1) & 0x0300;
        temp = temp / 0x00FF;
        m.setElement(4, 0xE8 + temp);
        /* Element 5 is the lower 8 bits of the cv */
        m.setElement(5, ((0x00ff & cv) - 1));
        /* Since this is a bit write, Element 6 is:
         0xE0 +
         bit 3 is the value to write
         bit's 0-2 are the location of the bit we are changing */
        if (value == true) {
            m.setElement(6, ((0xe8) | (bit & 0xff)));
        } else // value == false
        {
            m.setElement(6, ((0xe0) | (bit & 0xff)));
        }
        m.setParity(); // Set the parity bit
        return m;
    }

    public static XNetMessage getBitVerifyOpsModeCVMsg(int AH, int AL, int cv, int bit, boolean value) {
        XNetMessage m = new XNetMessage(8);
        m.setElement(0, XNetConstants.OPS_MODE_PROG_REQ);
        m.setElement(1, XNetConstants.OPS_MODE_PROG_WRITE_REQ);
        m.setElement(2, AH);
        m.setElement(3, AL);
        /* Element 4 is 0xE8 + the upper two  bits of the 10 bit CV address.
         NOTE: This is the track packet CV, not the human readable CV, so 
         its value actually is one less than what we normally think of it as.*/
        int temp = (cv - 1) & 0x0300;
        temp = temp / 0x00FF;
        m.setElement(4, 0xE8 + temp);
        /* Element 5 is the lower 8 bits of the cv */
        m.setElement(5, ((0x00ff & cv) - 1));
        /* Since this is a bit verify, Element 6 is:
         0xF0 +
         bit 3 is the value to write
         bit's 0-2 are the location of the bit we are changing */
        if (value == true) {
            m.setElement(6, ((0xf8) | (bit & 0xff)));
        } else // value == false
        {
            m.setElement(6, ((0xf0) | (bit & 0xff)));
        }
        m.setParity(); // Set the parity bit
        return m;
    }

    /*
     * Next, we have routines to generate XpressNet Messages for building
     * and tearing down a consist or a double header.
     */

    /**
     * Build a Double Header.
     *
     * @param address1 the first address in the consist
     * @param address2 the second address in the consist.
     */
    public static XNetMessage getBuildDoubleHeaderMsg(int address1, int address2) {
        XNetMessage msg = new XNetMessage(7);
        msg.setElement(0, XNetConstants.LOCO_DOUBLEHEAD);
        msg.setElement(1, XNetConstants.LOCO_DOUBLEHEAD_BYTE2);
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address1));
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address1));
        msg.setElement(4, LenzCommandStation.getDCCAddressHigh(address2));
        msg.setElement(5, LenzCommandStation.getDCCAddressLow(address2));
        msg.setParity();
        return (msg);
    }

    /**
     * Dissolve a Double Header.
     *
     * @param address one of the two addresses in the Double Header
     */
    public static XNetMessage getDisolveDoubleHeaderMsg(int address) {
        // All we have to do is call getBuildDoubleHeaderMsg with the 
        // second address as a zero
        return (getBuildDoubleHeaderMsg(address, 0));
    }

    /**
     * Add a Single address to a specified Advanced consist.
     *
     * @param consist the consist address (1-99)
     * @param address the locomotive address to add.
     * @param isNormalDir tells us if the locomotive is going forward when 
     * the consist is going forward.
     */
    public static XNetMessage getAddLocoToConsistMsg(int consist, int address,
            boolean isNormalDir) {
        XNetMessage msg = new XNetMessage(6);
        msg.setElement(0, XNetConstants.LOCO_OPER_REQ);
        if (isNormalDir) {
            msg.setElement(1, XNetConstants.LOCO_ADD_MULTI_UNIT_REQ);
        } else {
            msg.setElement(1, XNetConstants.LOCO_ADD_MULTI_UNIT_REQ | 0x01);
        }
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        msg.setElement(4, consist);
        msg.setParity();
        return (msg);
    }

    /**
     * Remove a Single address to a specified Advanced consist.
     *
     * @param consist the consist address (1-99)
     * @param address the locomotive address to remove
     */
    public static XNetMessage getRemoveLocoFromConsistMsg(int consist, int address) {
        XNetMessage msg = new XNetMessage(6);
        msg.setElement(0, XNetConstants.LOCO_OPER_REQ);
        msg.setElement(1, XNetConstants.LOCO_REM_MULTI_UNIT_REQ);
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        msg.setElement(4, consist);
        msg.setParity();
        return (msg);
    }


    /*
     * Next, we have routines to generate XpressNet Messages for search
     * and manipulation of the Command Station Database
     */

    /**
     * Given a locomotive address, search the database for the next 
     * member. (if the Address is zero start at the begining of the 
     * database).
     *
     * @param address is the locomotive address
     * @param searchForward indicates to search the database Forward if 
     * true, or backwards if False 
     */
    public static XNetMessage getNextAddressOnStackMsg(int address, boolean searchForward) {
        XNetMessage msg = new XNetMessage(5);
        msg.setElement(0, XNetConstants.LOCO_STATUS_REQ);
        if (searchForward) {
            msg.setElement(1, XNetConstants.LOCO_STACK_SEARCH_FWD);
        } else {
            msg.setElement(1, XNetConstants.LOCO_STACK_SEARCH_BKWD);
        }
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        msg.setParity();
        return (msg);
    }

    /**
     * Given a consist address, search the database for the next Consist 
     * address.
     *
     * @param address is the consist address (in the range 1-99).
     * If the Address is zero start at the beginning of the database.
     * @param searchForward indicates to search the database Forward if 
     * true, or backwards if false
     */
    public static XNetMessage getDBSearchMsgConsistAddress(int address, boolean searchForward) {
        XNetMessage msg = new XNetMessage(4);
        msg.setElement(0, XNetConstants.CS_MULTI_UNIT_REQ);
        if (searchForward) {
            msg.setElement(1, XNetConstants.CS_MULTI_UNIT_REQ_FWD);
        } else {
            msg.setElement(1, XNetConstants.CS_MULTI_UNIT_REQ_BKWD);
        }
        msg.setElement(2, address);
        msg.setParity();
        return (msg);
    }

    /**
     * Given a consist and a locomotive address, search the database for 
     * the next Locomotive in the consist.
     *
     * @param consist the consist address (1-99).
     * If the Consist Address is zero start at the begining of the database
     * @param address the locomotive address.
     * If the Address is zero start at the begining of the consist
     * @param searchForward indicates to search the database Forward if 
     * true, or backwards if False 
     */
    public static XNetMessage getDBSearchMsgNextMULoco(int consist, int address, boolean searchForward) {
        XNetMessage msg = new XNetMessage(6);
        msg.setElement(0, XNetConstants.LOCO_IN_MULTI_UNIT_SEARCH_REQ);
        if (searchForward) {
            msg.setElement(1, XNetConstants.LOCO_IN_MULTI_UNIT_REQ_FORWARD);
        } else {
            msg.setElement(1, XNetConstants.LOCO_IN_MULTI_UNIT_REQ_BACKWARD);
        }
        msg.setElement(2, consist);
        msg.setElement(3, LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(4, LenzCommandStation.getDCCAddressLow(address));
        msg.setParity();
        return (msg);
    }

    /**
     * Given a locomotive address, delete it from the database .
     *
     * @param address the locomotive address
     */
    public static XNetMessage getDeleteAddressOnStackMsg(int address) {
        XNetMessage msg = new XNetMessage(5);
        msg.setElement(0, XNetConstants.LOCO_STATUS_REQ);
        msg.setElement(1, XNetConstants.LOCO_STACK_DELETE);
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        msg.setParity();
        return (msg);
    }

    /**
     * Given a locomotive address, request its status .
     *
     * @param address the locomotive address
     */
    public static XNetMessage getLocomotiveInfoRequestMsg(int address) {
        XNetMessage msg = new XNetMessage(5);
        msg.setElement(0, XNetConstants.LOCO_STATUS_REQ);
        msg.setElement(1, XNetConstants.LOCO_INFO_REQ_V3);
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        msg.setParity();
        return (msg);
    }

    /**
     * Given a locomotive address, request the function state (momentary status).
     *
     * @param address the locomotive address
     */
    public static XNetMessage getLocomotiveFunctionStatusMsg(int address) {
        XNetMessage msg = new XNetMessage(5);
        msg.setElement(0, XNetConstants.LOCO_STATUS_REQ);
        msg.setElement(1, XNetConstants.LOCO_INFO_REQ_FUNC);
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        msg.setParity();
        return (msg);
    }

    /**
     * Given a locomotive address, request the function on/off state 
     * for functions 13-28
     *
     * @param address the locomotive address
     */
    public static XNetMessage getLocomotiveFunctionHighOnStatusMsg(int address) {
        XNetMessage msg = new XNetMessage(5);
        msg.setElement(0, XNetConstants.LOCO_STATUS_REQ);
        msg.setElement(1, XNetConstants.LOCO_INFO_REQ_FUNC_HI_ON);
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        msg.setParity();
        return (msg);
    }

    /**
     * Given a locomotive address, request the function state (momentary status)
     * for high functions (functions 13-28).
     *
     * @param address the locomotive address
     */
    public static XNetMessage getLocomotiveFunctionHighMomStatusMsg(int address) {
        XNetMessage msg = new XNetMessage(5);
        msg.setElement(0, XNetConstants.LOCO_STATUS_REQ);
        msg.setElement(1, XNetConstants.LOCO_INFO_REQ_FUNC_HI_MOM);
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        msg.setParity();
        return (msg);
    }

    /*
     * Generate an emergency stop for the specified address.
     *
     * @param address the locomotive address
     */
    public static XNetMessage getAddressedEmergencyStop(int address) {
        XNetMessage msg = new XNetMessage(4);
        msg.setElement(0, XNetConstants.EMERGENCY_STOP);
        msg.setElement(1, LenzCommandStation.getDCCAddressHigh(address));
        // set to the upper
        // byte of the  DCC address
        msg.setElement(2, LenzCommandStation.getDCCAddressLow(address));
        // set to the lower byte
        //of the DCC address
        msg.setParity(); // Set the parity bit
        return msg;
    }

    /**
     * Generate a Speed and Direction Request message.
     *
     * @param address the locomotive address
     * @param speedStepMode the speedstep mode see @jmri.DccThrottle
     *                       for possible values.
     * @param speed a normalized speed value (a floating point number between 0 
     *              and 1).  A negative value indicates emergency stop.
     * @param isForward true for forward, false for reverse.
     */
    public static XNetMessage getSpeedAndDirectionMsg(int address,
            SpeedStepMode speedStepMode,
            float speed,
            boolean isForward) {
        XNetMessage msg = new XNetMessage(6);
        msg.setElement(0, XNetConstants.LOCO_OPER_REQ);
        int element4value = 0;   /* this is for holding the speed and
         direction setting */

        if (speedStepMode == SpeedStepMode.NMRA_DCC_128) {
            // We're in 128 speed step mode
            msg.setElement(1, XNetConstants.LOCO_SPEED_128);
            // Now, we need to figure out what to send in element 4
            // Remember, the speed steps are identified as 0-127 (in
            // 128 step mode), not 1-128.
            int speedVal = java.lang.Math.round(speed * 126);
            // speed step 1 is reserved to indicate emergency stop,
            // so we need to step over speed step 1
            if (speedVal >= 1) {
                element4value = speedVal + 1;
            }
        } else if (speedStepMode == SpeedStepMode.NMRA_DCC_28) {
            // We're in 28 speed step mode
            msg.setElement(1, XNetConstants.LOCO_SPEED_28);
            // Now, we need to figure out what to send in element 4
            int speedVal = java.lang.Math.round(speed * 28);
            // The first speed step used is actually at 4 for 28
            // speed step mode.
            if (speedVal >= 1) {
                speedVal += 3;
            }
            // We have to re-arange the bits, since bit 4 is the LSB,
            // but other bits are in order from 0-3
            element4value = ((speedVal & 0x1e) >> 1)
                    + ((speedVal & 0x01) << 4);
        } else if (speedStepMode == SpeedStepMode.NMRA_DCC_27) {
            // We're in 27 speed step mode
            msg.setElement(1, XNetConstants.LOCO_SPEED_27);
            // Now, we need to figure out what to send in element 4
            int speedVal = java.lang.Math.round(speed * 27);
            // The first speed step used is actually at 4 for 27
            // speed step mode.
            if (speedVal >= 1) {
                speedVal += 3;
            }
            // We have to re-arange the bits, since bit 4 is the LSB,
            // but other bits are in order from 0-3
            element4value = ((speedVal & 0x1e) >> 1)
                    + ((speedVal & 0x01) << 4);
        } else {
            // We're in 14 speed step mode
            msg.setElement(1, XNetConstants.LOCO_SPEED_14);
            // Now, we need to figure out what to send in element 4
            element4value = (int) (speed * 14);
            int speedVal = java.lang.Math.round(speed * 14);
            // The first speed step used is actually at 2 for 14
            // speed step mode.
            if (speedVal >= 1) {
                element4value += 1;
            }
        }
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        // set to the upper byte of the  DCC address
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        // set to the lower byte
        //of the DCC address
        if (isForward) {
            /* the direction bit is always the most significant bit */
            element4value += 128;
        }
        msg.setElement(4, element4value);
        msg.setParity(); // Set the parity bit
        return msg;
    }

    /**
     * Generate a Function Group One Operation Request message.
     *
     * @param address the locomotive address
     * @param f0 is true if f0 is on, false if f0 is off
     * @param f1 is true if f1 is on, false if f1 is off
     * @param f2 is true if f2 is on, false if f2 is off
     * @param f3 is true if f3 is on, false if f3 is off
     * @param f4 is true if f4 is on, false if f4 is off
     */
    public static XNetMessage getFunctionGroup1OpsMsg(int address,
            boolean f0,
            boolean f1,
            boolean f2,
            boolean f3,
            boolean f4) {
        XNetMessage msg = new XNetMessage(6);
        msg.setElement(0, XNetConstants.LOCO_OPER_REQ);
        msg.setElement(1, XNetConstants.LOCO_SET_FUNC_GROUP1);
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        // set to the upper byte of the  DCC address
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        // set to the lower byte of the DCC address
        // Now, we need to figure out what to send in element 3
        int element4value = 0;
        if (f0) {
            element4value += 16;
        }
        if (f1) {
            element4value += 1;
        }
        if (f2) {
            element4value += 2;
        }
        if (f3) {
            element4value += 4;
        }
        if (f4) {
            element4value += 8;
        }
        msg.setElement(4, element4value);
        msg.setParity(); // Set the parity bit
        return msg;
    }

    /**
     * Generate a Function Group One Set Momentary Functions message.
     *
     * @param address the locomotive address
     * @param f0 is true if f0 is momentary
     * @param f1 is true if f1 is momentary
     * @param f2 is true if f2 is momentary
     * @param f3 is true if f3 is momentary
     * @param f4 is true if f4 is momentary
     */
    public static XNetMessage getFunctionGroup1SetMomMsg(int address,
            boolean f0,
            boolean f1,
            boolean f2,
            boolean f3,
            boolean f4) {
        XNetMessage msg = new XNetMessage(6);
        msg.setElement(0, XNetConstants.LOCO_OPER_REQ);
        msg.setElement(1, XNetConstants.LOCO_SET_FUNC_Group1);
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        // set to the upper byte of the  DCC address
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        // set to the lower byte of the DCC address
        // Now, we need to figure out what to send in element 3
        int element4value = 0;
        if (f0) {
            element4value += 16;
        }
        if (f1) {
            element4value += 1;
        }
        if (f2) {
            element4value += 2;
        }
        if (f3) {
            element4value += 4;
        }
        if (f4) {
            element4value += 8;
        }
        msg.setElement(4, element4value);
        msg.setParity(); // Set the parity bit
        return msg;
    }

    /**
     * Generate a Function Group Two Operation Request message.
     *
     * @param address the locomotive address
     * @param f5 is true if f5 is on, false if f5 is off
     * @param f6 is true if f6 is on, false if f6 is off
     * @param f7 is true if f7 is on, false if f7 is off
     * @param f8 is true if f8 is on, false if f8 is off
     */
    public static XNetMessage getFunctionGroup2OpsMsg(int address,
            boolean f5,
            boolean f6,
            boolean f7,
            boolean f8) {
        XNetMessage msg = new XNetMessage(6);
        msg.setElement(0, XNetConstants.LOCO_OPER_REQ);
        msg.setElement(1, XNetConstants.LOCO_SET_FUNC_GROUP2);
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        // set to the upper byte of the DCC address
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        // set to the lower byte of the DCC address
        // Now, we need to figure out what to send in element 3
        int element4value = 0;
        if (f5) {
            element4value += 1;
        }
        if (f6) {
            element4value += 2;
        }
        if (f7) {
            element4value += 4;
        }
        if (f8) {
            element4value += 8;
        }
        msg.setElement(4, element4value);
        msg.setParity(); // Set the parity bit
        return msg;
    }

    /**
     * Generate a Function Group Two Set Momentary Functions message.
     *
     * @param address the locomotive address
     * @param f5 is true if f5 is momentary
     * @param f6 is true if f6 is momentary
     * @param f7 is true if f7 is momentary
     * @param f8 is true if f8 is momentary
     */
    public static XNetMessage getFunctionGroup2SetMomMsg(int address,
            boolean f5,
            boolean f6,
            boolean f7,
            boolean f8) {
        XNetMessage msg = new XNetMessage(6);
        msg.setElement(0, XNetConstants.LOCO_OPER_REQ);
        msg.setElement(1, XNetConstants.LOCO_SET_FUNC_Group2);
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        // set to the upper byte of the  DCC address
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        // set to the lower byte of the DCC address
        // Now, we need to figure out what to send in element 3
        int element4value = 0;
        if (f5) {
            element4value += 1;
        }
        if (f6) {
            element4value += 2;
        }
        if (f7) {
            element4value += 4;
        }
        if (f8) {
            element4value += 8;
        }
        msg.setElement(4, element4value);
        msg.setParity(); // Set the parity bit
        return msg;
    }

    /**
     * Generate a Function Group Three Operation Request message.
     *
     * @param address the locomotive address
     * @param f9 is true if f9 is on, false if f9 is off
     * @param f10 is true if f10 is on, false if f10 is off
     * @param f11 is true if f11 is on, false if f11 is off
     * @param f12 is true if f12 is on, false if f12 is off
     */
    public static XNetMessage getFunctionGroup3OpsMsg(int address,
            boolean f9,
            boolean f10,
            boolean f11,
            boolean f12) {
        XNetMessage msg = new XNetMessage(6);
        msg.setElement(0, XNetConstants.LOCO_OPER_REQ);
        msg.setElement(1, XNetConstants.LOCO_SET_FUNC_GROUP3);
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        // set to the upper byte of the  DCC address
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        // set to the lower byte of the DCC address
        // Now, we need to figure out what to send in element 3
        int element4value = 0;
        if (f9) {
            element4value += 1;
        }
        if (f10) {
            element4value += 2;
        }
        if (f11) {
            element4value += 4;
        }
        if (f12) {
            element4value += 8;
        }
        msg.setElement(4, element4value);
        msg.setParity(); // Set the parity bit
        return msg;
    }

    /**
     * Generate a Function Group Three Set Momentary Functions message.
     *
     * @param address the locomotive address
     * @param f9 is true if f9 is momentary
     * @param f10 is true if f10 is momentary
     * @param f11 is true if f11 is momentary
     * @param f12 is true if f12 is momentary
     */
    public static XNetMessage getFunctionGroup3SetMomMsg(int address,
            boolean f9,
            boolean f10,
            boolean f11,
            boolean f12) {
        XNetMessage msg = new XNetMessage(6);
        msg.setElement(0, XNetConstants.LOCO_OPER_REQ);
        msg.setElement(1, XNetConstants.LOCO_SET_FUNC_Group3);
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        // set to the upper byte of the  DCC address
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        // set to the lower byte of the DCC address
        // Now, we need to figure out what to send in element 3
        int element4value = 0;
        if (f9) {
            element4value += 1;
        }
        if (f10) {
            element4value += 2;
        }
        if (f11) {
            element4value += 4;
        }
        if (f12) {
            element4value += 8;
        }
        msg.setElement(4, element4value);
        msg.setParity(); // Set the parity bit
        return msg;
    }

    /**
     * Generate a Function Group Four Operation Request message.
     *
     * @param address the locomotive address
     * @param f13 is true if f13 is on, false if f13 is off
     * @param f14 is true if f14 is on, false if f14 is off
     * @param f15 is true if f15 is on, false if f15 is off
     * @param f16 is true if f18 is on, false if f16 is off
     * @param f17 is true if f17 is on, false if f17 is off
     * @param f18 is true if f18 is on, false if f18 is off
     * @param f19 is true if f19 is on, false if f19 is off
     * @param f20 is true if f20 is on, false if f20 is off
     */
    public static XNetMessage getFunctionGroup4OpsMsg(int address,
            boolean f13,
            boolean f14,
            boolean f15,
            boolean f16,
            boolean f17,
            boolean f18,
            boolean f19,
            boolean f20) {
        XNetMessage msg = new XNetMessage(6);
        msg.setElement(0, XNetConstants.LOCO_OPER_REQ);
        msg.setElement(1, XNetConstants.LOCO_SET_FUNC_GROUP4);
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        // set to the upper byte of the  DCC address
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        // set to the lower byte of the DCC address
        // Now, we need to figure out what to send in element 3
        int element4value = 0;
        if (f13) {
            element4value += 1;
        }
        if (f14) {
            element4value += 2;
        }
        if (f15) {
            element4value += 4;
        }
        if (f16) {
            element4value += 8;
        }
        if (f17) {
            element4value += 16;
        }
        if (f18) {
            element4value += 32;
        }
        if (f19) {
            element4value += 64;
        }
        if (f20) {
            element4value += 128;
        }
        msg.setElement(4, element4value);
        msg.setParity(); // Set the parity bit
        return msg;
    }

    /**
     * Generate a Function Group Four Set Momentary Function message.
     *
     * @param address the locomotive address
     * @param f13 is true if f13 is Momentary
     * @param f14 is true if f14 is Momentary
     * @param f15 is true if f15 is Momentary
     * @param f16 is true if f18 is Momentary
     * @param f17 is true if f17 is Momentary
     * @param f18 is true if f18 is Momentary
     * @param f19 is true if f19 is Momentary
     * @param f20 is true if f20 is Momentary
     */
    public static XNetMessage getFunctionGroup4SetMomMsg(int address,
            boolean f13,
            boolean f14,
            boolean f15,
            boolean f16,
            boolean f17,
            boolean f18,
            boolean f19,
            boolean f20) {
        XNetMessage msg = new XNetMessage(6);
        msg.setElement(0, XNetConstants.LOCO_OPER_REQ);
        msg.setElement(1, XNetConstants.LOCO_SET_FUNC_Group4);
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        // set to the upper byte of the  DCC address
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        // set to the lower byte of the DCC address
        // Now, we need to figure out what to send in element 3
        int element4value = 0;
        if (f13) {
            element4value += 1;
        }
        if (f14) {
            element4value += 2;
        }
        if (f15) {
            element4value += 4;
        }
        if (f16) {
            element4value += 8;
        }
        if (f17) {
            element4value += 16;
        }
        if (f18) {
            element4value += 32;
        }
        if (f19) {
            element4value += 64;
        }
        if (f20) {
            element4value += 128;
        }
        msg.setElement(4, element4value);
        msg.setParity(); // Set the parity bit
        return msg;
    }

    /**
     * Generate a Function Group Five Operation Request message.
     *
     * @param address the locomotive address
     * @param f21 is true if f21 is on, false if f21 is off
     * @param f22 is true if f22 is on, false if f22 is off
     * @param f23 is true if f23 is on, false if f23 is off
     * @param f24 is true if f24 is on, false if f24 is off
     * @param f25 is true if f25 is on, false if f25 is off
     * @param f26 is true if f26 is on, false if f26 is off
     * @param f27 is true if f27 is on, false if f27 is off
     * @param f28 is true if f28 is on, false if f28 is off
     */
    public static XNetMessage getFunctionGroup5OpsMsg(int address,
            boolean f21,
            boolean f22,
            boolean f23,
            boolean f24,
            boolean f25,
            boolean f26,
            boolean f27,
            boolean f28) {
        XNetMessage msg = new XNetMessage(6);
        msg.setElement(0, XNetConstants.LOCO_OPER_REQ);
        msg.setElement(1, XNetConstants.LOCO_SET_FUNC_GROUP5);
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        // set to the upper byte of the  DCC address
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        // set to the lower byte of the DCC address
        // Now, we need to figure out what to send in element 3
        int element4value = 0;
        if (f21) {
            element4value += 1;
        }
        if (f22) {
            element4value += 2;
        }
        if (f23) {
            element4value += 4;
        }
        if (f24) {
            element4value += 8;
        }
        if (f25) {
            element4value += 16;
        }
        if (f26) {
            element4value += 32;
        }
        if (f27) {
            element4value += 64;
        }
        if (f28) {
            element4value += 128;
        }
        msg.setElement(4, element4value);
        msg.setParity(); // Set the parity bit
        return msg;
    }

    /**
     * Generate a Function Group Five Set Momentary Function message.
     *
     * @param address the locomotive address
     * @param f21 is true if f21 is momentary
     * @param f22 is true if f22 is momentary
     * @param f23 is true if f23 is momentary
     * @param f24 is true if f24 is momentary
     * @param f25 is true if f25 is momentary
     * @param f26 is true if f26 is momentary
     * @param f27 is true if f27 is momentary
     * @param f28 is true if f28 is momentary
     */
    public static XNetMessage getFunctionGroup5SetMomMsg(int address,
            boolean f21,
            boolean f22,
            boolean f23,
            boolean f24,
            boolean f25,
            boolean f26,
            boolean f27,
            boolean f28) {
        XNetMessage msg = new XNetMessage(6);
        msg.setElement(0, XNetConstants.LOCO_OPER_REQ);
        msg.setElement(1, XNetConstants.LOCO_SET_FUNC_Group5);
        msg.setElement(2, LenzCommandStation.getDCCAddressHigh(address));
        // set to the upper byte of the  DCC address
        msg.setElement(3, LenzCommandStation.getDCCAddressLow(address));
        // set to the lower byte of the DCC address
        // Now, we need to figure out what to send in element 3
        int element4value = 0;
        if (f21) {
            element4value += 1;
        }
        if (f22) {
            element4value += 2;
        }
        if (f23) {
            element4value += 4;
        }
        if (f24) {
            element4value += 8;
        }
        if (f25) {
            element4value += 16;
        }
        if (f26) {
            element4value += 32;
        }
        if (f27) {
            element4value += 64;
        }
        if (f28) {
            element4value += 128;
        }
        msg.setElement(4, element4value);
        msg.setParity(); // Set the parity bit
        return msg;
    }

    /**
     * Build a Resume operations Message.
     */
    public static XNetMessage getResumeOperationsMsg() {
        XNetMessage msg = new XNetMessage(3);
        msg.setElement(0, XNetConstants.CS_REQUEST);
        msg.setElement(1, XNetConstants.RESUME_OPS);
        msg.setParity();
        return (msg);
    }

    /**
     * Build an EmergencyOff Message.
     */
    public static XNetMessage getEmergencyOffMsg() {
        XNetMessage msg = new XNetMessage(3);
        msg.setElement(0, XNetConstants.CS_REQUEST);
        msg.setElement(1, XNetConstants.EMERGENCY_OFF);
        msg.setParity();
        return (msg);
    }

    /**
     * Build an EmergencyStop Message.
     */
    public static XNetMessage getEmergencyStopMsg() {
        XNetMessage msg = new XNetMessage(2);
        msg.setElement(0, XNetConstants.ALL_ESTOP);
        msg.setParity();
        return (msg);
    }

    /**
     * Generate the message to request the Command Station Hardware/Software
     * Version.
     */
    public static XNetMessage getCSVersionRequestMessage() {
        XNetMessage msg = new XNetMessage(3);
        msg.setElement(0, XNetConstants.CS_REQUEST);
        msg.setElement(1, XNetConstants.CS_VERSION);
        msg.setParity(); // Set the parity bit
        return msg;
    }

    /**
     * Generate the message to request the Command Station Status.
     */
    public static XNetMessage getCSStatusRequestMessage() {
        XNetMessage msg = new XNetMessage(3);
        msg.setElement(0, XNetConstants.CS_REQUEST);
        msg.setElement(1, XNetConstants.CS_STATUS);
        msg.setParity(); // Set the parity bit
        return msg;
    }

    /**
     * Generate the message to set the Command Station to Auto or Manual restart
     * mode.
     */
    public static XNetMessage getCSAutoStartMessage(boolean autoMode) {
        XNetMessage msg = new XNetMessage(4);
        msg.setElement(0, XNetConstants.CS_SET_POWERMODE);
        msg.setElement(1, XNetConstants.CS_SET_POWERMODE);
        if (autoMode) {
            msg.setElement(2, XNetConstants.CS_POWERMODE_AUTO);
        } else {
            msg.setElement(2, XNetConstants.CS_POWERMODE_MANUAL);
        }
        msg.setParity(); // Set the parity bit
        return msg;
    }

    /**
     * Generate the message to request the Computer Interface Hardware/Software
     * Version.
     */
    public static XNetMessage getLIVersionRequestMessage() {
        XNetMessage msg = new XNetMessage(2);
        msg.setElement(0, XNetConstants.LI_VERSION_REQUEST);
        msg.setParity(); // Set the parity bit
        return msg;
    }

    /**
     * Generate the message to set or request the Computer Interface Address.
     *
     * @param address Interface address (0-31). Send invalid address to request
     *                the address (32-255).
     */
    public static XNetMessage getLIAddressRequestMsg(int address) {
        XNetMessage msg = new XNetMessage(4);
        msg.setElement(0, XNetConstants.LI101_REQUEST);
        msg.setElement(1, XNetConstants.LI101_REQUEST_ADDRESS);
        msg.setElement(2, address);
        msg.setParity(); // Set the parity bit
        return msg;
    }

    /**
     * Generate the message to set or request the Computer Interface speed.
     *
     * @param speed 1 is 19,200bps, 2 is 38,400bps, 3 is 57,600bps, 4 is
     *              115,200bps. Send invalid speed to request the current
     *              setting.
     */
    public static XNetMessage getLISpeedRequestMsg(int speed) {
        XNetMessage msg = new XNetMessage(4);
        msg.setElement(0, XNetConstants.LI101_REQUEST);
        msg.setElement(1, XNetConstants.LI101_REQUEST_BAUD);
        msg.setElement(2, speed);
        msg.setParity(); // Set the parity bit
        return msg;
    }

   /**
    * Generate text translations of messages for use in the XpressNet monitor.
    *
    * @return representation of the XNetMessage as a string.
    */
    @Override
   public String toMonitorString(){
        String text;
        /* Start decoding messages sent by the computer */
        /* Start with LI101F requests */
        if (getElement(0) == XNetConstants.LI_VERSION_REQUEST) {
            text = Bundle.getMessage("XNetMessageRequestLIVersion");
        } else if (getElement(0) == XNetConstants.LI101_REQUEST) {
            switch (getElement(1)) {
                case XNetConstants.LI101_REQUEST_ADDRESS:
                    text = Bundle.getMessage("XNetMessageRequestLIAddress", getElement(2));
                    break;
                case XNetConstants.LI101_REQUEST_BAUD:
                    switch (getElement(2)) {
                        case 1:
                            text = Bundle.getMessage("XNetMessageRequestLIBaud",
                                   Bundle.getMessage("LIBaud19200"));
                            break;
                        case 2:
                            text = Bundle.getMessage("XNetMessageRequestLIBaud",
                                   Bundle.getMessage("Baud38400"));
                            break;
                        case 3:
                            text = Bundle.getMessage("XNetMessageRequestLIBaud",
                                   Bundle.getMessage("Baud57600"));
                            break;
                        case 4:
                            text = Bundle.getMessage("XNetMessageRequestLIBaud",
                                   Bundle.getMessage("Baud115200"));
                            break;
                        default:
                            text = Bundle.getMessage("XNetMessageRequestLIBaud",
                                   Bundle.getMessage("BaudOther"));
                    }
                    break;
                default:
                    text = toString();
            }
            /* Next, we have generic requests */
        } else if (getElement(0) == XNetConstants.CS_REQUEST) {
            switch (getElement(1)) {
                case XNetConstants.EMERGENCY_OFF:
                    text = Bundle.getMessage("XNetMessageRequestEmergencyOff");
                    break;
                case XNetConstants.RESUME_OPS:
                    text = Bundle.getMessage("XNetMessageRequestNormalOps");
                    break;
                case XNetConstants.SERVICE_MODE_CSRESULT:
                    text = Bundle.getMessage("XNetMessageRequestServiceModeResult");
                    break;
                case XNetConstants.CS_VERSION:
                    text = Bundle.getMessage("XNetMessageRequestCSVersion");
                    break;
                case XNetConstants.CS_STATUS:
                    text = Bundle.getMessage("XNetMessageRequestCSStatus");
                    break;
                default:
                    text = toString();
            }
        } else if (getElement(0) == XNetConstants.CS_SET_POWERMODE
                && getElement(1) == XNetConstants.CS_SET_POWERMODE
                && getElement(2) == XNetConstants.CS_POWERMODE_AUTO) {
            text = Bundle.getMessage("XNetMessageRequestCSPowerModeAuto");
        } else if (getElement(0) == XNetConstants.CS_SET_POWERMODE
                && getElement(1) == XNetConstants.CS_SET_POWERMODE
                && getElement(2) == XNetConstants.CS_POWERMODE_MANUAL) {
            text = Bundle.getMessage("XNetMessageRequestCSPowerModeManual");
            /* Next, we have Programming Requests */
        } else if (getElement(0) == XNetConstants.PROG_READ_REQUEST) {
            switch (getElement(1)) {
                case XNetConstants.PROG_READ_MODE_REGISTER:
                    text = Bundle.getMessage("XNetMessageRequestServiceModeReadRegister",getElement(2));
                    break;
                case XNetConstants.PROG_READ_MODE_CV:
                    text = Bundle.getMessage("XNetMessageRequestServiceModeReadDirect",getElement(2));
                    break;
                case XNetConstants.PROG_READ_MODE_PAGED:
                    text = Bundle.getMessage("XNetMessageRequestServiceModeReadPaged",getElement(2));
                    break;
                case XNetConstants.PROG_READ_MODE_CV_V36:
                    text = Bundle.getMessage("XNetMessageRequestServiceModeReadDirectV36",(getElement(2)== 0 ? 1024 : getElement(2)));
                    break;
                case XNetConstants.PROG_READ_MODE_CV_V36 + 1:
                    text = Bundle.getMessage("XNetMessageRequestServiceModeReadDirectV36",(256 + getElement(2)));
                    break;
                case XNetConstants.PROG_READ_MODE_CV_V36 + 2:
                    text = Bundle.getMessage("XNetMessageRequestServiceModeReadDirectV36",(512 + getElement(2)));
                    break;
                case XNetConstants.PROG_READ_MODE_CV_V36 + 3:
                    text = Bundle.getMessage("XNetMessageRequestServiceModeReadDirectV36",(768 + getElement(2)));
                    break;
                default:
                    text = toString();
            }
        } else if (getElement(0) == XNetConstants.PROG_WRITE_REQUEST) {
            switch (getElement(1)) {
                case XNetConstants.PROG_WRITE_MODE_REGISTER:
                    text = Bundle.getMessage("XNetMessageRequestServiceModeWriteRegister",getElement(2),getElement(3));
                    break;
                case XNetConstants.PROG_WRITE_MODE_CV:
                    text = Bundle.getMessage("XNetMessageRequestServiceModeWriteDirect",getElement(2),getElement(3));
                    break;
                case XNetConstants.PROG_WRITE_MODE_PAGED:
                    text = Bundle.getMessage("XNetMessageRequestServiceModeWritePaged",getElement(2),getElement(3));
                    break;
                case XNetConstants.PROG_WRITE_MODE_CV_V36:
                    text = Bundle.getMessage("XNetMessageRequestServiceModeWriteDirectV36",(getElement(2)== 0 ? 1024 : getElement(2)),getElement(3));
                    break;
                case (XNetConstants.PROG_WRITE_MODE_CV_V36 + 1):
                    text = Bundle.getMessage("XNetMessageRequestServiceModeWriteDirectV36",(256 + getElement(2)),getElement(3));
                    break;
                case (XNetConstants.PROG_WRITE_MODE_CV_V36 + 2):
                    text = Bundle.getMessage("XNetMessageRequestServiceModeWriteDirectV36",(512 + getElement(2)),getElement(3));
                    break;
                case (XNetConstants.PROG_WRITE_MODE_CV_V36 + 3):
                    text = Bundle.getMessage("XNetMessageRequestServiceModeWriteDirectV36",(768 + getElement(2)),getElement(3));
                    break;
                default:
                    text = toString();
            }
        } else if (getElement(0) == XNetConstants.OPS_MODE_PROG_REQ) {
            switch (getElement(1)) {
                case XNetConstants.OPS_MODE_PROG_WRITE_REQ:
                    if ((getElement(4) & 0xEC) == 0xEC
                            || (getElement(4) & 0xE4) == 0xE4) {
                        String message = "";
                        if ((getElement(4) & 0xEC) == 0xEC) {
                            message ="XNetMessageOpsModeByteWrite";
                        } else if ((getElement(4) & 0xE4) == 0xE4) {
                            message ="XNetMessageOpsModeByteVerify";
                        }
                        text = Bundle.getMessage(message,
                               getElement(6),
                               (1 + getElement(5) + ((getElement(4) & 0x03) << 8)),
                               LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)));
                        break;
                    } else if ((getElement(4) & 0xE8) == 0xE8) {
                        String message = "";
                        if ((getElement(6) & 0x10) == 0x10) {
                            message ="XNetMessageOpsModeBitVerify";
                        } else {
                            message ="XNetMessageOpsModeBitWrite";
                        }
                        text = Bundle.getMessage(message,
                                ((getElement(6) & 0x08) >> 3),
                                (1 + getElement(5) + ((getElement(4) & 0x03) << 8)),
                                (getElement(6) & 0x07),
                                LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)));
                        break;
                    }
                    //$FALL-THROUGH$
                default:
                    text = toString();
            }
            // Next, decode the locomotive operation requests
        } else if (getElement(0) == XNetConstants.LOCO_OPER_REQ) {
            text = "Mobile Decoder Operations Request: ";
            int speed;
            String direction;
            switch (getElement(1)) {
                case XNetConstants.LOCO_SPEED_14:
                    if ((getElement(4) & 0x80) != 0) {
                        direction = Bundle.getMessage("Forward");
                    } else {
                        direction = Bundle.getMessage("Reverse");
                    }
                    text = text
                            + Bundle.getMessage("XNetMessageSetSpeed",
                            LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)))
                            + " " + (getElement(4) & 0x0f)
                            + " " + Bundle.getMessage("XNetMessageSetDirection", direction);
                    text += " " + Bundle.getMessage("SpeedStepModeX", 14) + ".";
                    break;
                case XNetConstants.LOCO_SPEED_27:
                    text = text
                            + Bundle.getMessage("XNetMessageSetSpeed",
                            LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)))
                            + " ";
                    log.debug("" + LenzCommandStation.calcLocoAddress(getElement(2), getElement(3))); // address printed as: "1234" = OK
                    log.debug(text); // address printed as: "1,234" = WRONG
                    speed
                            = (((getElement(4) & 0x10) >> 4)
                            + ((getElement(4) & 0x0F) << 1));
                    if (speed >= 3) {
                        speed -= 3;
                    }
                    text += speed;
                    if ((getElement(4) & 0x80) != 0) {
                        text += " " + Bundle.getMessage("XNetMessageSetDirection", Bundle.getMessage("Forward"));
                    } else {
                        text += " " + Bundle.getMessage("XNetMessageSetDirection", Bundle.getMessage("Reverse"));
                    }
                    text += " " + Bundle.getMessage("SpeedStepModeX", 27) + ".";
                    break;
                case XNetConstants.LOCO_SPEED_28:
                    text = text
                            + Bundle.getMessage("XNetMessageSetSpeed",
                            LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)))
                            + " ";
                    speed
                            = (((getElement(4) & 0x10) >> 4)
                            + ((getElement(4) & 0x0F) << 1));
                    if (speed >= 3) {
                        speed -= 3;
                    }
                    text += speed;
                    if ((getElement(4) & 0x80) != 0) {
                        text += " " + Bundle.getMessage("XNetMessageSetDirection", Bundle.getMessage("Forward"));
                    } else {
                        text += " " + Bundle.getMessage("XNetMessageSetDirection", Bundle.getMessage("Reverse"));
                    }
                    text += " " + Bundle.getMessage("SpeedStepModeX", 28) + ".";
                    break;
                case XNetConstants.LOCO_SPEED_128:
                    if ((getElement(4) & 0x80) != 0) {
                        direction = Bundle.getMessage("Forward");
                    } else {
                        direction = Bundle.getMessage("Reverse");
                    }
                    text = text
                            + Bundle.getMessage("XNetMessageSetSpeed",
                            LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)))
                            + " "
                            + (getElement(4) & 0x7F) + " " + Bundle.getMessage("XNetMessageSetDirection", direction);
                    text += " " + Bundle.getMessage("SpeedStepModeX", 128) + ".";
                    break;
                case XNetConstants.LOCO_SET_FUNC_GROUP1: {
                    text = text
                            + Bundle.getMessage("XNetMessageSetFunctionGroupX", 1) + " "
                            + LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)) + " ";
                    int element4 = getElement(4);
                    if ((element4 & 0x10) != 0) {
                        text += "F0 " + Bundle.getMessage("PowerStateOn") + "; ";
                    } else {
                        text += "F0 " + Bundle.getMessage("PowerStateOff") + "; ";
                    }
                    if ((element4 & 0x01) != 0) {
                        text += "F1 " + Bundle.getMessage("PowerStateOn") + "; ";
                    } else {
                        text += "F1 " + Bundle.getMessage("PowerStateOff") + "; ";
                    }
                    if ((element4 & 0x02) != 0) {
                        text += "F2 " + Bundle.getMessage("PowerStateOn") + "; ";
                    } else {
                        text += "F2 " + Bundle.getMessage("PowerStateOff") + "; ";
                    }
                    if ((element4 & 0x04) != 0) {
                        text += "F3 " + Bundle.getMessage("PowerStateOn") + "; ";
                    } else {
                        text += "F3 " + Bundle.getMessage("PowerStateOff") + "; ";
                    }
                    if ((element4 & 0x08) != 0) {
                        text += "F4 " + Bundle.getMessage("PowerStateOn") + "; ";
                    } else {
                        text += "F4 " + Bundle.getMessage("PowerStateOff") + "; ";
                    }
                    break;
                }
                case XNetConstants.LOCO_SET_FUNC_GROUP2: {
                    text = text
                            + Bundle.getMessage("XNetMessageSetFunctionGroupX", 2) + " "
                            + LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)) + " ";
                    int element4 = getElement(4);
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
                    break;
                }
                case XNetConstants.LOCO_SET_FUNC_GROUP3: {
                    text = text
                            + Bundle.getMessage("XNetMessageSetFunctionGroupX", 3) + " "
                            + LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)) + " ";
                    int element4 = getElement(4);
                    if ((element4 & 0x01) != 0) {
                        text += "F9 " + Bundle.getMessage("PowerStateOn") + "; ";
                    } else {
                        text += "F9 " + Bundle.getMessage("PowerStateOff") + "; ";
                    }
                    if ((element4 & 0x02) != 0) {
                        text += "F10 " + Bundle.getMessage("PowerStateOn") + "; ";
                    } else {
                        text += "F10 " + Bundle.getMessage("PowerStateOff") + "; ";
                    }
                    if ((element4 & 0x04) != 0) {
                        text += "F11 " + Bundle.getMessage("PowerStateOn") + "; ";
                    } else {
                        text += "F11 " + Bundle.getMessage("PowerStateOff") + "; ";
                    }
                    if ((element4 & 0x08) != 0) {
                        text += "F12 " + Bundle.getMessage("PowerStateOn") + "; ";
                    } else {
                        text += "F12 " + Bundle.getMessage("PowerStateOff") + "; ";
                    }
                    break;
                }
                case XNetConstants.LOCO_SET_FUNC_GROUP4: {
                    text = text
                            + Bundle.getMessage("XNetMessageSetFunctionGroupX", 4) + " "
                            + LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)) + " ";
                    int element4 = getElement(4);
                    if ((element4 & 0x01) != 0) {
                        text += "F13 " + Bundle.getMessage("PowerStateOn") + "; ";
                    } else {
                        text += "F13 " + Bundle.getMessage("PowerStateOff") + "; ";
                    }
                    if ((element4 & 0x02) != 0) {
                        text += "F14 " + Bundle.getMessage("PowerStateOn") + "; ";
                    } else {
                        text += "F14 " + Bundle.getMessage("PowerStateOff") + "; ";
                    }
                    if ((element4 & 0x04) != 0) {
                        text += "F15 " + Bundle.getMessage("PowerStateOn") + "; ";
                    } else {
                        text += "F15 " + Bundle.getMessage("PowerStateOff") + "; ";
                    }
                    if ((element4 & 0x08) != 0) {
                        text += "F16 " + Bundle.getMessage("PowerStateOn") + "; ";
                    } else {
                        text += "F16 " + Bundle.getMessage("PowerStateOff") + "; ";
                    }
                    if ((element4 & 0x10) != 0) {
                        text += "F17 " + Bundle.getMessage("PowerStateOn") + "; ";
                    } else {
                        text += "F17 " + Bundle.getMessage("PowerStateOff") + "; ";
                    }
                    if ((element4 & 0x20) != 0) {
                        text += "F18 " + Bundle.getMessage("PowerStateOn") + "; ";
                    } else {
                        text += "F18 " + Bundle.getMessage("PowerStateOff") + "; ";
                    }
                    if ((element4 & 0x40) != 0) {
                        text += "F19 " + Bundle.getMessage("PowerStateOn") + "; ";
                    } else {
                        text += "F19 " + Bundle.getMessage("PowerStateOff") + "; ";
                    }
                    if ((element4 & 0x80) != 0) {
                        text += "F20 " + Bundle.getMessage("PowerStateOn") + "; ";
                    } else {
                        text += "F20 " + Bundle.getMessage("PowerStateOff") + "; ";
                    }
                    break;
                }
                case XNetConstants.LOCO_SET_FUNC_GROUP5: {
                    text = text
                            + Bundle.getMessage("XNetMessageSetFunctionGroupX", 5) + " "
                            + LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)) + " ";
                    int element4 = getElement(4);
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
                    break;
                }
                case XNetConstants.LOCO_SET_FUNC_Group1: {
                    text = text
                            + Bundle.getMessage("XNetMessageSetFunctionGroupXMomentary", 1) + " "
                            + LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)) + " ";
                    int element4 = getElement(4);
                    if ((element4 & 0x10) == 0) {
                        text += "F0 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F0 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x01) == 0) {
                        text += "F1 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F1 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x02) == 0) {
                        text += "F2 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F2 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x04) == 0) {
                        text += "F3 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F3 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x08) == 0) {
                        text += "F4 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F4 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    break;
                }
                case XNetConstants.LOCO_SET_FUNC_Group2: {
                    text = text
                            + Bundle.getMessage("XNetMessageSetFunctionGroupXMomentary", 2) + " "
                            + LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)) + " ";
                    int element4 = getElement(4);
                    if ((element4 & 0x01) == 0) {
                        text += "F5 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F5 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x02) == 0) {
                        text += "F6 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F6 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x04) == 0) {
                        text += "F7 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F7 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x08) == 0) {
                        text += "F8 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F8 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    break;
                }
                case XNetConstants.LOCO_SET_FUNC_Group3: {
                    text = text
                            + Bundle.getMessage("XNetMessageSetFunctionGroupXMomentary", 3) + " "
                            + LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)) + " ";
                    int element4 = getElement(4);
                    if ((element4 & 0x01) == 0) {
                        text += "F9 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F9 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x02) == 0) {
                        text += "F10 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F10 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x04) == 0) {
                        text += "F11 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F11 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x08) == 0) {
                        text += "F12 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F12 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    break;
                }
                case XNetConstants.LOCO_SET_FUNC_Group4: {
                    text = text
                            + Bundle.getMessage("XNetMessageSetFunctionGroupXMomentary", 4) + " "
                            + LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)) + " ";
                    int element4 = getElement(4);
                    if ((element4 & 0x01) == 0) {
                        text += "F13 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F13 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x02) == 0) {
                        text += "F14 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F14 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x04) == 0) {
                        text += "F15 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F15 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x08) == 0) {
                        text += "F16 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F16 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x10) == 0) {
                        text += "F17 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F17 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x20) == 0) {
                        text += "F18 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F18 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x40) == 0) {
                        text += "F19 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F19 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x80) == 0) {
                        text += "F20 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F20 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    break;
                }
                case XNetConstants.LOCO_SET_FUNC_Group5: {
                    text = text
                            + Bundle.getMessage("XNetMessageSetFunctionGroupXMomentary", 5) + " "
                            + LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)) + " ";
                    int element4 = getElement(4);
                    if ((element4 & 0x01) == 0) {
                        text += "F21 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F21 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x02) == 0) {
                        text += "F22 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F22 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x04) == 0) {
                        text += "F23 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F23 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x08) == 0) {
                        text += "F24 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F24 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x10) == 0) {
                        text += "F25 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F25 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x20) == 0) {
                        text += "F26 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F26 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x40) == 0) {
                        text += "F27 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F27 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    if ((element4 & 0x80) == 0) {
                        text += "F28 " + Bundle.getMessage("FunctionContinuous") + "; ";
                    } else {
                        text += "F28 " + Bundle.getMessage("FunctionMomentary") + "; ";
                    }
                    break;
                }
                case XNetConstants.LOCO_ADD_MULTI_UNIT_REQ:
                    text = Bundle.getMessage("XNetMessageAddToConsistDirNormalRequest",
                           LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)),
                           getElement(4));
                    break;
                case (XNetConstants.LOCO_ADD_MULTI_UNIT_REQ | 0x01):
                    text = Bundle.getMessage("XNetMessageAddToConsistDirReverseRequest",
                           LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)),
                           getElement(4));
                    break;
                case (XNetConstants.LOCO_REM_MULTI_UNIT_REQ):
                    text = Bundle.getMessage("XNetMessageRemoveFromConsistRequest",
                           LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)),
                           getElement(4));
                    break;
                case (XNetConstants.LOCO_IN_MULTI_UNIT_REQ_FORWARD):
                    text = Bundle.getMessage("XNetMessageSearchCSStackForwardNextMULoco",
                           getElement(2),
                           LenzCommandStation.calcLocoAddress(getElement(3), getElement(4)));
                    break;
                case (XNetConstants.LOCO_IN_MULTI_UNIT_REQ_BACKWARD):
                    text = Bundle.getMessage("XNetMessageSearchCSStackBackwardNextMULoco",
                           getElement(2),
                           LenzCommandStation.calcLocoAddress(getElement(3), getElement(4)));
                    break;
                default:
                    text = toString();
            }
            // Emergency Stop a locomotive
        } else if (getElement(0) == XNetConstants.EMERGENCY_STOP) {
            text = Bundle.getMessage("XNetMessageAddressedEmergencyStopRequest",
              LenzCommandStation.calcLocoAddress(getElement(1), getElement(2)));
            // Disolve or Establish a Double Header
        } else if (getElement(0) == XNetConstants.LOCO_DOUBLEHEAD
                && getElement(1) == XNetConstants.LOCO_DOUBLEHEAD_BYTE2) {
            int loco1 = LenzCommandStation.calcLocoAddress(getElement(2), getElement(3));
            int loco2 = LenzCommandStation.calcLocoAddress(getElement(4), getElement(5));
            if (loco2 == 0) {
                text = Bundle.getMessage("XNetMessageDisolveDoubleHeaderRequest",loco1);
            } else {
                text = Bundle.getMessage("XNetMessageBuildDoubleHeaderRequest",loco1,loco2);
            }
            // Locomotive Status Request messages
        } else if (getElement(0) == XNetConstants.LOCO_STATUS_REQ) {
            switch (getElement(1)) {
                case XNetConstants.LOCO_INFO_REQ_FUNC:
                    text = Bundle.getMessage("XNetMessageRequestLocoFunctionMomStatus",
                            LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)));
                    break;
                case XNetConstants.LOCO_INFO_REQ_FUNC_HI_ON:
                    text = Bundle.getMessage("XNetMessageRequestLocoFunctionHighStatus",
                            LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)));
                    break;
                case XNetConstants.LOCO_INFO_REQ_FUNC_HI_MOM:
                    text = Bundle.getMessage("XNetMessageRequestLocoFunctionHighMomStatus",
                            LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)));
                    break;
                case XNetConstants.LOCO_INFO_REQ_V3:
                    text = Bundle.getMessage("XNetMessageRequestLocoInfo",
                            LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)));
                    break;
                case XNetConstants.LOCO_STACK_SEARCH_FWD:
                    text = Bundle.getMessage("XNetMessageSearchCSStackForward",
                           LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)));
                    break;
                case XNetConstants.LOCO_STACK_SEARCH_BKWD:
                    text = Bundle.getMessage("XNetMessageSearchCSStackBackward",
                           LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)));
                    break;
                case XNetConstants.LOCO_STACK_DELETE:
                    text = Bundle.getMessage("XNetMessageDeleteAddressOnStack",
                            LenzCommandStation.calcLocoAddress(getElement(2), getElement(3)));
                    break;
                default:
                    text = toString();
            }
        } else if(getElement(0) == XNetConstants.CS_MULTI_UNIT_REQ) {
            if (getElement(1) == XNetConstants.CS_MULTI_UNIT_REQ_FWD){
                text = Bundle.getMessage("XNetMessageSearchCSStackForwardConsistAddress",
                           getElement(2));
            } else if(getElement(1) == XNetConstants.CS_MULTI_UNIT_REQ_BKWD){
                text = Bundle.getMessage("XNetMessageSearchCSStackBackwardConsistAddress",
                           getElement(2));
            } else {
                    text = toString();
            }
            // Accessory Info Request message
        } else if (getElement(0) == XNetConstants.ACC_INFO_REQ) {
            String nibblekey=(((getElement(2) & 0x01) == 0x01) ? "FeedbackEncoderUpperNibble" : "FeedbackEncoderLowerNibble");
            text = Bundle.getMessage("XNetMessageFeedbackRequest",
                       getElement(1),
                       Bundle.getMessage(nibblekey));
        } else if (getElement(0) == XNetConstants.ACC_OPER_REQ) {
            String messageKey =(((getElement(2) & 0x08) == 0x08) ? "XNetMessageAccessoryDecoderOnRequest" : "XNetMessageAccessoryDecoderOffRequest");
            int baseaddress = getElement(1);
            int subaddress = ((getElement(2) & 0x06) >> 1);
            int address = (baseaddress * 4) + subaddress + 1;
            int output = (getElement(2) & 0x01);
            text = Bundle.getMessage(messageKey,address, baseaddress,subaddress,output);
        } else if (getElement(0) == XNetConstants.ALL_ESTOP) {
            text = Bundle.getMessage("XNetMessageRequestEmergencyStop");
        } else {
            text = toString();
        }
        return text;
   }

    // initialize logging    
    private final static Logger log = LoggerFactory.getLogger(XNetMessage.class);

}
