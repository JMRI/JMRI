// DCCppMessage.java
package jmri.jmrix.dccpp;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single command or response on the DCC++.
 * <P>
 * Content is represented with ints to avoid the problems with sign-extension
 * that bytes have, and because a Java char is actually a variable number of
 * bytes in Unicode.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @author	Paul Bender Copyright (C) 2003-2010
 * @author	Mark Underwood Copyright (C) 2015
 * @version	$Revision$
 *
 * Based on XNetMessage by Bob Jacobsen and Paul Bender
 */
public class DCCppMessage extends jmri.jmrix.AbstractMRMessage implements Serializable {

    static private int _nRetries = 5;

    /* According to the specification, DCC++ has a maximum timing 
     interval of 500 milliseconds durring normal communications */
    // TODO: Note this timing interval is actually an XpressNet thing...
    // Need to find out what DCC++'s equivalent is.
    static protected final int DCCppProgrammingTimeout = 10000;  // TODO: Appropriate value for DCC++?
    static private int DCCppMessageTimeout = 5000;  // TODO: Appropriate value for DCC++?

    /**
     * Create a new object, representing a specific-length message.
     *
     * @param len Total bytes in message, including opcode and error-detection
     *            byte.
     */
    public DCCppMessage(int len) {
        super(len);
        setBinary(false);
        setRetries(_nRetries);
        setTimeout(DCCppMessageTimeout);
        if (len > DCCppConstants.MAX_MESSAGE_SIZE || len < 0) {
            log.error("Invalid length in ctor: " + len);
        }
        _nDataChars = len;
    }

    /**
     * Create a new object, that is a copy of an existing message.
     *
     * @param message existing message.
     */
    public DCCppMessage(DCCppMessage message) {
        super(message);
        setBinary(false);
        setRetries(_nRetries);
        setTimeout(DCCppMessageTimeout);
    }

    /**
     * Create an DCCppMessage from an DCCppReply.
     */
    public DCCppMessage(DCCppReply message) {
        super(message.getNumDataElements());
        setBinary(false);
        setRetries(_nRetries);
        setTimeout(DCCppMessageTimeout);
        for (int i = 0; i < message.getNumDataElements(); i++) {
            setElement(i, message.getElement(i));
        }
    }

    /**
     * Create a DCCppMessage from a String containing bytes.
     * Since DCCppMessages are text, there is no Hex-to-byte conversion
     */
    public DCCppMessage(String s) {
        setBinary(false);
        setRetries(_nRetries);
        setTimeout(DCCppMessageTimeout);
        // gather bytes in result
	byte b[] = s.getBytes();
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

    // For DCC++, the opcode is the first character in the
    // command (after the < ).

    // note that the opcode is part of the message, so we treat it
    // directly
    // WARNING: use this only with opcodes that have a variable number 
    // of arguments following included. Otherwise, just use setElement
    public void setOpCode(int i) {
        if (i > 0xFF || i < 0) {
            log.error("Opcode invalid: " + i);
        }
        setElement(0, i & 0xFF);
    }

    public int getOpCode() {
        return (getElement(0) & 0xFF);
    }

    public char getOpCodeChar() {
	return ((char) getElement(0));
    }

    public String getOpCodeString() {
	return(Character.toString((char)(getElement(0))));
    }

    /**
     * check whether the message has a valid parity
     *
     * Not used for DCC++ No checksum.
     */
    public boolean checkParity() {
	return(true);
    }

    public void setParity() {
	return;
    }

    /**
     * return the message length
     */
    public int length() {
        return _nDataChars;
    }

    /**
     * changing the default number of retries for an DCC++ message
     *
     * @param t number of retries to attempt.
     */
    static public void setDCCppMessageRetries(int t) {
        _nRetries = t;
    }

    /**
     * changing the default timeout for an DCC++ message
     *
     * @param t Timeout in milliseconds
     */
    static public void setDCCppMessageTimeout(int t) {
        DCCppMessageTimeout = t;
    }


    /*
     * Most messages are sent with a reply expected, but
     * we have a few that we treat as though the reply is always
     * a broadcast message, because the reply usually comes to us 
     * that way.
     */
    // TODO: Not sure this is useful in DCC++
    @Override
    public boolean replyExpected() {
        return !broadcastReply;
    }

    private boolean broadcastReply = false;

    // Tell the traffic controller we expect this
    // message to have a broadcast reply (or not).
    void setBroadcastReply(boolean v) {
        broadcastReply = v;
    }

    // decode messages of a particular form
    // create messages of a particular form

    /* 
     * The next group of routines are used by Feedback and/or turnout 
     * control code.  These are used in multiple places within the code, 
     * so they appear here. 
     */

    public static DCCppMessage getStationaryDecoderMsg(int address, int subaddress, boolean activate) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Stationary Decoder Command
	msg.setElement(i++, DCCppConstants.STATIONARY_DECODER_CMD);
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Sanity check inputs
	if (address < 0 || address > DCCppConstants.MAX_ACC_DECODER_ADDRESS)
	    return(null);
	if (subaddress < 0 || subaddress > DCCppConstants.MAX_ACC_DECODER_SUBADDR)
	    return(null);
	
	// Send the Address
	String ad = Integer.toString(address);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Send the Subaddress
	ad = Integer.toString(subaddress);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Send the activate/deactrivate
	msg.setElement(i++, (activate ? '1': '0'));

	return(msg);
    }

    public static DCCppMessage getTurnoutCommandMsg(int id, boolean thrown) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Turnout Command
	msg.setElement(i++, DCCppConstants.TURNOUT_CMD);
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Sanity check inputs
	if (id < 0 || id > DCCppConstants.MAX_TURNOUT_ADDRESS) return(null);
	// Need to also validate whether turnout is predefined?  Where to store the IDs?

	// Set the ID
	String ad = Integer.toString(id);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set the state.
	msg.setElement(i++, (thrown ? '1' : '0'));

	return(msg);
    }

    public static DCCppMessage getWriteDirectCVMsg(int cv, int val) {
	return(getWriteDirectCVMsg(cv, val, 0, 0));
    }

    public static DCCppMessage getWriteDirectCVMsg(int cv, int val, int callbacknum, int callbacksub) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Sanity check inputs
	if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) return(null);
	if (val < 0 || val > DCCppConstants.MAX_DIRECT_CV_VAL) return(null);
	if (callbacknum < 0 || callbacknum > DCCppConstants.MAX_CALLBACK_NUM)
	    return(null);
	if (callbacksub < 0 || callbacksub > DCCppConstants.MAX_CALLBACK_SUB)
	    return(null);

	// Write CV to Programming Track Command
	msg.setElement(i++, DCCppConstants.PROG_WRITE_CV_BYTE);
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set CV
	String ad = Integer.toString(cv);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set Value
	ad = Integer.toString(val);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set Callback Number
	ad = Integer.toString(callbacknum);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set Callback Sub
	ad = Integer.toString(callbacksub);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}

	return(msg);
    }

    public static DCCppMessage getBitWriteDirectCVMsg(int cv, int bit, boolean val) {
	return(getBitWriteDirectCVMsg(cv, bit, val, 0, 0));
    }

    public static DCCppMessage getBitWriteDirectCVMsg(int cv, int bit, boolean val, int callbacknum, int callbacksub) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Sanity Check Inputs
	if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) return(null);
	if (bit < 0 || bit > 7) return(null);
	if (callbacknum < 0 || callbacknum > DCCppConstants.MAX_CALLBACK_NUM)
	    return(null);
	if (callbacksub < 0 || callbacksub > DCCppConstants.MAX_CALLBACK_SUB)
	    return(null);

	// Write Bit to CV on Programming Track
	msg.setElement(i++, DCCppConstants.PROG_WRITE_CV_BYTE);
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set CV
	String ad = Integer.toString(cv);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set Bit
	ad = Integer.toString(bit);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set Value
	msg.setElement(i, (val ? '1' : '0'));	
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set Callback Number
	ad = Integer.toString(callbacknum);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set Callback Sub
	ad = Integer.toString(callbacksub);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}

	return(msg);
    }

    public static DCCppMessage getReadDirectCVMsg(int cv) {
	return(getReadDirectCVMsg(cv, 0, 0));
    }

    public static DCCppMessage getReadDirectCVMsg(int cv, int callbacknum, int callbacksub) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Sanity check inputs
	if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) return(null);
	if (callbacknum < 0 || callbacknum > DCCppConstants.MAX_CALLBACK_NUM)
	    return(null);
	if (callbacksub < 0 || callbacksub > DCCppConstants.MAX_CALLBACK_SUB)
	    return(null);

	// Read CV from Program Track
	msg.setElement(i++, DCCppConstants.PROG_READ_CV);
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set CV
	String ad = Integer.toString(cv);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set Callback Number
	ad = Integer.toString(callbacknum);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set Callback Sub
	ad = Integer.toString(callbacksub);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}

	return(msg);
    }

    
    public static DCCppMessage getWriteOpsModeCVMsg(int address, int cv, int val) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Sanity check inputs
	if (address < 0 || address > DCCppConstants.MAX_LOCO_ADDRESS)
	    return(null);
	if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) return(null);
	if (val < 0 || val > DCCppConstants.MAX_DIRECT_CV_VAL) return(null);
	
	// Write CV in Ops Mode
	msg.setElement(i++, DCCppConstants.OPS_WRITE_CV_BYTE);
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set address
	String ad = Integer.toString(address);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set CV
	ad = Integer.toString(cv);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set Value
	ad = Integer.toString(val);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}

	return(msg);
    }

    public static DCCppMessage getBitWriteOpsModeCVMsg(int address, int cv, int bit, boolean val) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Sanity Check Inputs
	if (address < 0 || address > DCCppConstants.MAX_LOCO_ADDRESS)
	    return(null);
	if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) return(null);
	if (bit < 0 || bit > 7) return(null);
	
	// Write Bit in Ops Mode
	msg.setElement(i++, DCCppConstants.OPS_WRITE_CV_BIT);
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set Address
	String ad = Integer.toString(address);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set CV
	ad = Integer.toString(cv);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set Bit
	ad = Integer.toString(bit);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Set Value
	msg.setElement(i, val ? '1' : '0');

	return(msg);
    }

    public static DCCppMessage getSetTrackPowerMsg(boolean on) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	msg.setElement(0, (on ? DCCppConstants.TRACK_POWER_ON : DCCppConstants.TRACK_POWER_OFF));
	return(msg);
    }

    public static DCCppMessage getTrackPowerOnMsg() {
	return(getSetTrackPowerMsg(true));
    }

    public static DCCppMessage getTrackPowerOffMsg() {
	return(getSetTrackPowerMsg(false));
    }


    public static DCCppMessage getReadTrackCurrentMsg() {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	msg.setElement(0, DCCppConstants.READ_TRACK_CURRENT);
	return(msg);
    }

    public static DCCppMessage getCSStatusMsg() {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	msg.setElement(0, DCCppConstants.READ_CS_STATUS);
	return(msg);
    }


    /*
     * Generate an emergency stop for the specified address
     * @param address is the locomotive address
     *
     * Note: This just sends a THROTTLE command with speed = -1
     */
    public static DCCppMessage getAddressedEmergencyStop(int register, int address) {
	int i = 0;
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);

	if (address < 0 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);

	// Byte 1 is the command
	msg.setElement(i++, DCCppConstants.THROTTLE_CMD);
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Field 2 is the Register (WTH?)
	msg.setElement(i++, register);
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Field 3 is the DCC address
	String ad = Integer.toString(address);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Field 4 is the speed ( -1 for emergency stop)
	String sp = "-1";
	for (int j = 0; i < sp.length(); j++, i++) {
	    msg.setElement(i, sp.charAt(j)); // TODO: Is this right?  Does it properly convert the string char to an int?
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Field 5 is direction.
	msg.setElement(i, DCCppConstants.FORWARD_DIR);

	return(msg);

    }

    /*
     * Generate a Speed and Direction Request message
     * @param address is the locomotive address
     * @param speedStepMode is the speedstep mode see @jmri.DccThrottle 
     *                       for possible values.
     * @param speed a normalized speed value (a floating point number between 0 
     *              and 1).  A negative value indicates emergency stop.
     * @param isForward true for forward, false for reverse.
     */
    public static DCCppMessage getSpeedAndDirectionMsg(int register, int address, float speed, boolean isForward) {
	int i = 0;
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);

	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	// Byte 1 is the command
	msg.setElement(i++, DCCppConstants.THROTTLE_CMD);
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Field 2 is the Register (WTH?)
	msg.setElement(i++, register);
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Field 3 is the DCC address
	String ad = Integer.toString(address);
	for (int j = 0; j < ad.length(); j++, i++) {
	    msg.setElement(i, ad.charAt(j));
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Field 4 is the speed (128 steps, 0-126 or -1 for emergency stop)
	// Emergency Stop is a different JMRI-level command, though.
	int speedVal = java.lang.Math.round(speed * 126);
	speedVal = ((speedVal > DCCppConstants.MAX_SPEED) ? DCCppConstants.MAX_SPEED : speedVal);
	String sp = Integer.toString(speedVal);
	for (int j = 0; i < sp.length(); j++, i++) {
	    msg.setElement(i, sp.charAt(j)); // TODO: Is this right?  Does it properly convert the string char to an int?
	}
	msg.setElement(i++, DCCppConstants.WHITESPACE);

	// Field 5 is direction.
	msg.setElement(i, (isForward ? DCCppConstants.FORWARD_DIR : DCCppConstants.REVERSE_DIR));

	return(msg);
    }

    /*
     * Generate a Function Group One Operation Request message
     * @param address is the locomotive address
     * @param f0 is true if f0 is on, false if f0 is off
     * @param f1 is true if f1 is on, false if f1 is off
     * @param f2 is true if f2 is on, false if f2 is off
     * @param f3 is true if f3 is on, false if f3 is off
     * @param f4 is true if f4 is on, false if f4 is off
     */

    public static DCCppMessage getFunctionGroup1OpsMsg(int address,
            boolean f0,
            boolean f1,
            boolean f2,
            boolean f3,
            boolean f4) 
    { 
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	msg.setElement(i++, DCCppConstants.FUNCTION_CMD);
	msg.setElement(i++, DCCppConstants.WHITESPACE);
	int byte1 = 128 + (f0 ? 16 : 0);
	byte1 += (f1 ? 1 : 0);
	byte1 += (f2 ? 2 : 0);
	byte1 += (f3 ? 4 : 0);
	byte1 += (f4 ? 8 : 0);
	String sp = Integer.toString(byte1);
	for (int j = 0; i < sp.length(); j++, i++) {
	    msg.setElement(i, sp.charAt(j)); // TODO: Is this right?  Does it properly convert the string char to an int?
	}
	return(msg);
    }

    /*
     * Generate a Function Group One Set Momentary Functions message
     * @param address is the locomotive address
     * @param f0 is true if f0 is momentary
     * @param f1 is true if f1 is momentary
     * @param f2 is true if f2 is momentary
     * @param f3 is true if f3 is momentary
     * @param f4 is true if f4 is momentary
     */
    public static DCCppMessage getFunctionGroup1SetMomMsg(int address,
            boolean f0,
            boolean f1,
            boolean f2,
            boolean f3,
            boolean f4) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	msg.setElement(i++, DCCppConstants.FUNCTION_CMD);
	msg.setElement(i++, DCCppConstants.WHITESPACE);
	int byte1 = 128 + (f0 ? 16 : 0);
	byte1 += (f1 ? 1 : 0);
	byte1 += (f2 ? 2 : 0);
	byte1 += (f3 ? 4 : 0);
	byte1 += (f4 ? 8 : 0);
	String sp = Integer.toString(byte1);
	for (int j = 0; i < sp.length(); j++, i++) {
	    msg.setElement(i, sp.charAt(j)); // TODO: Is this right?  Does it properly convert the string char to an int?
	}
	return(msg);
    }


    /*
     * Generate a Function Group Two Operation Request message
     * @param address is the locomotive address
     * @param f5 is true if f5 is on, false if f5 is off
     * @param f6 is true if f6 is on, false if f6 is off
     * @param f7 is true if f7 is on, false if f7 is off
     * @param f8 is true if f8 is on, false if f8 is off
     */
    public static DCCppMessage getFunctionGroup2OpsMsg(int address,
            boolean f5,
            boolean f6,
            boolean f7,
            boolean f8) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	msg.setElement(i++, DCCppConstants.FUNCTION_CMD);
	msg.setElement(i++, DCCppConstants.WHITESPACE);
	int byte1 = 176;
	byte1 += (f5 ? 1 : 0);
	byte1 += (f6 ? 2 : 0);
	byte1 += (f7 ? 4 : 0);
	byte1 += (f8 ? 8 : 0);
	String sp = Integer.toString(byte1);
	for (int j = 0; i < sp.length(); j++, i++) {
	    msg.setElement(i, sp.charAt(j)); // TODO: Is this right?  Does it properly convert the string char to an int?
	}
	return(msg);
    }

    /*
     * Generate a Function Group Two Set Momentary Functions message
     * @param address is the locomotive address
     * @param f5 is true if f5 is momentary
     * @param f6 is true if f6 is momentary
     * @param f7 is true if f7 is momentary
     * @param f8 is true if f8 is momentary
     */
    public static DCCppMessage getFunctionGroup2SetMomMsg(int address,
            boolean f5,
            boolean f6,
            boolean f7,
            boolean f8) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	msg.setElement(i++, DCCppConstants.FUNCTION_CMD);
	msg.setElement(i++, DCCppConstants.WHITESPACE);
	int byte1 = 176;
	byte1 += (f5 ? 1 : 0);
	byte1 += (f6 ? 2 : 0);
	byte1 += (f7 ? 4 : 0);
	byte1 += (f8 ? 8 : 0);
	String sp = Integer.toString(byte1);
	for (int j = 0; i < sp.length(); j++, i++) {
	    msg.setElement(i, sp.charAt(j)); // TODO: Is this right?  Does it properly convert the string char to an int?
	}
	return(msg);
    }


    /*
     * Generate a Function Group Three Operation Request message
     * @param address is the locomotive address
     * @param f9 is true if f9 is on, false if f9 is off
     * @param f10 is true if f10 is on, false if f10 is off
     * @param f11 is true if f11 is on, false if f11 is off
     * @param f12 is true if f12 is on, false if f12 is off
     */
    public static DCCppMessage getFunctionGroup3OpsMsg(int address,
            boolean f9,
            boolean f10,
            boolean f11,
            boolean f12) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	msg.setElement(i++, DCCppConstants.FUNCTION_CMD);
	msg.setElement(i++, DCCppConstants.WHITESPACE);
	int byte1 = 160;
	byte1 += (f9 ? 1 : 0);
	byte1 += (f10 ? 2 : 0);
	byte1 += (f11 ? 4 : 0);
	byte1 += (f12 ? 8 : 0);
	String sp = Integer.toString(byte1);
	for (int j = 0; i < sp.length(); j++, i++) {
	    msg.setElement(i, sp.charAt(j)); // TODO: Is this right?  Does it properly convert the string char to an int?
	}
	return(msg);
    }

    /*
     * Generate a Function Group Three Set Momentary Functions message
     * @param address is the locomotive address
     * @param f9 is true if f9 is momentary
     * @param f10 is true if f10 is momentary
     * @param f11 is true if f11 is momentary
     * @param f12 is true if f12 is momentary
     */
    public static DCCppMessage getFunctionGroup3SetMomMsg(int address,
            boolean f9,
            boolean f10,
            boolean f11,
            boolean f12) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	msg.setElement(i++, DCCppConstants.FUNCTION_CMD);
	msg.setElement(i++, DCCppConstants.WHITESPACE);
	int byte1 = 160;
	byte1 += (f9 ? 1 : 0);
	byte1 += (f10 ? 2 : 0);
	byte1 += (f11 ? 4 : 0);
	byte1 += (f12 ? 8 : 0);
	String sp = Integer.toString(byte1);
	for (int j = 0; i < sp.length(); j++, i++) {
	    msg.setElement(i, sp.charAt(j)); // TODO: Is this right?  Does it properly convert the string char to an int?
	}
	return(msg);
    }

    /*
     * Generate a Function Group Four Operation Request message
     * @param address is the locomotive address
     * @param f13 is true if f13 is on, false if f13 is off
     * @param f14 is true if f14 is on, false if f14 is off
     * @param f15 is true if f15 is on, false if f15 is off
     * @param f16 is true if f18 is on, false if f16 is off
     * @param f17 is true if f17 is on, false if f17 is off
     * @param f18 is true if f18 is on, false if f18 is off
     * @param f19 is true if f19 is on, false if f19 is off
     * @param f20 is true if f20 is on, false if f20 is off
     */
    public static DCCppMessage getFunctionGroup4OpsMsg(int address,
            boolean f13,
            boolean f14,
            boolean f15,
            boolean f16,
            boolean f17,
            boolean f18,
            boolean f19,
            boolean f20) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	msg.setElement(i++, DCCppConstants.FUNCTION_CMD);
	msg.setElement(i++, DCCppConstants.WHITESPACE);
	msg.setElement(i++, DCCppConstants.FUNCTION_GROUP4_BYTE1);
	int byte2 = 0;
	byte2 += (f13 ? 1 : 0);
	byte2 += (f14 ? 2 : 0);
	byte2 += (f15 ? 4 : 0);
	byte2 += (f16 ? 8 : 0);
	byte2 += (f17 ? 16 : 0);
	byte2 += (f18 ? 32 : 0);
	byte2 += (f19 ? 64 : 0);
	byte2 += (f20 ? 128 : 0);
	String sp = Integer.toString(byte2);
	for (int j = 0; i < sp.length(); j++, i++) {
	    msg.setElement(i, sp.charAt(j)); // TODO: Is this right?  Does it properly convert the string char to an int?
	}
	return(msg);
    }

    /*
     * Generate a Function Group Four Set Momentary Function message
     * @param address is the locomotive address
     * @param f13 is true if f13 is Momentary
     * @param f14 is true if f14 is Momentary
     * @param f15 is true if f15 is Momentary
     * @param f16 is true if f18 is Momentary
     * @param f17 is true if f17 is Momentary
     * @param f18 is true if f18 is Momentary
     * @param f19 is true if f19 is Momentary
     * @param f20 is true if f20 is Momentary
     */
    public static DCCppMessage getFunctionGroup4SetMomMsg(int address,
            boolean f13,
            boolean f14,
            boolean f15,
            boolean f16,
            boolean f17,
            boolean f18,
            boolean f19,
            boolean f20) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	msg.setElement(i++, DCCppConstants.FUNCTION_CMD);
	msg.setElement(i++, DCCppConstants.WHITESPACE);
	msg.setElement(i++, DCCppConstants.FUNCTION_GROUP4_BYTE1);
	int byte2 = 0;
	byte2 += (f13 ? 1 : 0);
	byte2 += (f14 ? 2 : 0);
	byte2 += (f15 ? 4 : 0);
	byte2 += (f16 ? 8 : 0);
	byte2 += (f17 ? 16 : 0);
	byte2 += (f18 ? 32 : 0);
	byte2 += (f19 ? 64 : 0);
	byte2 += (f20 ? 128 : 0);
	String sp = Integer.toString(byte2);
	for (int j = 0; i < sp.length(); j++, i++) {
	    msg.setElement(i, sp.charAt(j)); // TODO: Is this right?  Does it properly convert the string char to an int?
	}
	return(msg);
    }

    /*
     * Generate a Function Group Five Operation Request message
     * @param address is the locomotive address
     * @param f21 is true if f21 is on, false if f21 is off
     * @param f22 is true if f22 is on, false if f22 is off
     * @param f23 is true if f23 is on, false if f23 is off
     * @param f24 is true if f24 is on, false if f24 is off
     * @param f25 is true if f25 is on, false if f25 is off
     * @param f26 is true if f26 is on, false if f26 is off
     * @param f27 is true if f27 is on, false if f27 is off
     * @param f28 is true if f28 is on, false if f28 is off
     */
    public static DCCppMessage getFunctionGroup5OpsMsg(int address,
            boolean f21,
            boolean f22,
            boolean f23,
            boolean f24,
            boolean f25,
            boolean f26,
            boolean f27,
            boolean f28) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	msg.setElement(i++, DCCppConstants.FUNCTION_CMD);
	msg.setElement(i++, DCCppConstants.WHITESPACE);
	msg.setElement(i++, DCCppConstants.FUNCTION_GROUP4_BYTE1);
	int byte2 = 0;
	byte2 += (f21 ? 1 : 0);
	byte2 += (f22 ? 2 : 0);
	byte2 += (f23 ? 4 : 0);
	byte2 += (f24 ? 8 : 0);
	byte2 += (f25 ? 16 : 0);
	byte2 += (f26 ? 32 : 0);
	byte2 += (f27 ? 64 : 0);
	byte2 += (f28 ? 128 : 0);
	String sp = Integer.toString(byte2);
	for (int j = 0; i < sp.length(); j++, i++) {
	    msg.setElement(i, sp.charAt(j)); // TODO: Is this right?  Does it properly convert the string char to an int?
	}
	return(msg);
    }

    /*
     * Generate a Function Group Five Set Momentary Function message
     * @param address is the locomotive address
     * @param f21 is true if f21 is momentary
     * @param f22 is true if f22 is momentary
     * @param f23 is true if f23 is momentary
     * @param f24 is true if f24 is momentary
     * @param f25 is true if f25 is momentary
     * @param f26 is true if f26 is momentary
     * @param f27 is true if f27 is momentary
     * @param f28 is true if f28 is momentary
     */
    public static DCCppMessage getFunctionGroup5SetMomMsg(int address,
            boolean f21,
            boolean f22,
            boolean f23,
            boolean f24,
            boolean f25,
            boolean f26,
            boolean f27,
            boolean f28) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	msg.setElement(i++, DCCppConstants.FUNCTION_CMD);
	msg.setElement(i++, DCCppConstants.WHITESPACE);
	msg.setElement(i++, DCCppConstants.FUNCTION_GROUP4_BYTE1);
	int byte2 = 0;
	byte2 += (f21 ? 1 : 0);
	byte2 += (f22 ? 2 : 0);
	byte2 += (f23 ? 4 : 0);
	byte2 += (f24 ? 8 : 0);
	byte2 += (f25 ? 16 : 0);
	byte2 += (f26 ? 32 : 0);
	byte2 += (f27 ? 64 : 0);
	byte2 += (f28 ? 128 : 0);
	String sp = Integer.toString(byte2);
	for (int j = 0; i < sp.length(); j++, i++) {
	    msg.setElement(i, sp.charAt(j)); // TODO: Is this right?  Does it properly convert the string char to an int?
	}
	return(msg);
    }

    /*
     * Build an Emergency Off Message
     */
    

    /**
     * Test Code Functions... not for normal use
     */

    /** Write DCC Packet to a specified Register on the Main*/
    public static DCCppMessage getWriteDCCPacketMainMsg( int register, int num_bytes, byte[] bytes) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Sanity Check Inputs
	if (register < 0 || register > DCCppConstants.MAX_MAIN_REGISTERS) return(null);
	if (num_bytes < 2 || num_bytes > 5) return(null);
	for (int j = 0; j < num_bytes; j++) { if (bytes[j] < 0 || bytes[j] > 255) return(null); }
	
	// Write DCC Packet to the track
	msg.setElement(i++, DCCppConstants.WRITE_DCC_PACKET_MAIN);
	msg.setElement(i++, DCCppConstants.WHITESPACE);
	
	// Set Bytes
	for (int k = 0; k < num_bytes; k++) {
	    String ad = Integer.toString(bytes[k]);
	    for (int j = 0; j < ad.length(); j++, i++) {
		msg.setElement(i, ad.charAt(j));
	    }
	    msg.setElement(i++, DCCppConstants.WHITESPACE);
	}
	
	return(msg);	
    }
	
    /** Write DCC Packet to a specified Register on the Programming Track*/
    public static DCCppMessage getWriteDCCPacketProgMsg( int register, int num_bytes, int bytes[]) {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Sanity Check Inputs
	if (register < 0 || register > DCCppConstants.MAX_MAIN_REGISTERS) return(null);
	if (num_bytes < 2 || num_bytes > 5) return(null);
	for (int j = 0; j < num_bytes; j++) { if (bytes[j] < 0 || bytes[j] > 255) return(null); }
	
	// Write DCC Packet to the track
	msg.setElement(i++, DCCppConstants.WRITE_DCC_PACKET_PROG);
	msg.setElement(i++, DCCppConstants.WHITESPACE);
	
	// Set Bytes
	for (int k = 0; k < num_bytes; k++) {
	    String ad = Integer.toString(bytes[k]);
	    for (int j = 0; j < ad.length(); j++, i++) {
		msg.setElement(i, ad.charAt(j));
	    }
	    msg.setElement(i++, DCCppConstants.WHITESPACE);
	}
	
	return(msg);	
    }

    public static DCCppMessage getCheckFreeMemMsg() {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Write DCC Packet to the track
	msg.setElement(i++, DCCppConstants.GET_FREE_MEMORY);
	msg.setElement(i++, DCCppConstants.WHITESPACE);	
	return(msg);
    }

    public static DCCppMessage getListRegisterContentsMsg() {
	DCCppMessage msg = new DCCppMessage(DCCppConstants.MESSAGE_SIZE);
	int i = 0;

	// Write DCC Packet to the track
	msg.setElement(i++, DCCppConstants.LIST_REGISTER_CONTENTS);
	msg.setElement(i++, DCCppConstants.WHITESPACE);	
	return(msg);
    }

    // initialize logging    
    static Logger log = LoggerFactory.getLogger(DCCppMessage.class.getName());

}

/* @(#)DCCppMessage.java */
