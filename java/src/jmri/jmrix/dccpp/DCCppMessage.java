// DCCppMessage.java
package jmri.jmrix.dccpp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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
public class DCCppMessage extends jmri.jmrix.AbstractMRMessage {

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
	return ((char) (getElement(0) & 0x00FF));
    }

    public String getOpCodeString() {
	return(Character.toString((char)(getElement(0))));
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

    //------------------------------------------------------
    // Message Helper Functions

    // Core methods
    private Matcher match(String s, String pat, String name) {
	try {
	    Pattern p = Pattern.compile(pat);
	    Matcher m = p.matcher(s);
	    if (!m.matches()) {
		log.error("Malformed {} Command: {} Pattern: {}",name, s, p.toString());
		return(null);
	    }
	    return(m);

	} catch (PatternSyntaxException e) {
            log.error("Malformed DCC++ message syntax! ");
	    return(null);
        } catch (IllegalStateException e) {
            log.error("Group called before match operation executed string= " + s);
	    return(null);
        } catch (IndexOutOfBoundsException e) {
            log.error("Index out of bounds string= " + s);
	    return(null);
        }
    }
    
    // Identity Methods
    public boolean isThrottleMessage() { return(this.getOpCodeChar() == DCCppConstants.THROTTLE_CMD); }
    public boolean isAccessoryMessage() { return(this.getOpCodeChar() == DCCppConstants.ACCESSORY_CMD); }
    public boolean isFunctionMessage() { return(this.getOpCodeChar() == DCCppConstants.FUNCTION_CMD); }
    public boolean isTurnoutMessage() { return(this.getOpCodeChar() == DCCppConstants.TURNOUT_CMD); }
    public boolean isProgWriteByteMessage() { return(this.getOpCodeChar() == DCCppConstants.PROG_WRITE_CV_BYTE); }
    public boolean isProgWriteBitMessage() { return(this.getOpCodeChar() == DCCppConstants.PROG_WRITE_CV_BIT); }
    public boolean isProgReadMessage() { return(this.getOpCodeChar() == DCCppConstants.PROG_READ_CV); }


    //------------------------------------------------------
    // Helper methods for Accessory Decoder Commands

    public String getAccessoryAddrString() {
	if (this.isAccessoryMessage()) {
	    Matcher m = match(this.toString(), DCCppConstants.ACCESSORY_CMD_REGEX, "Accessory");
	    if (m != null) {
		return(m.group(1));
	    } else {
		return("0");
	    }
	} else 
	    log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
	    return("0");
    }

    public int getAccessoryAddrInt() {
	return(Integer.parseInt(this.getAccessoryAddrString()));
    }

    public String getAccessorySubString() {
	if (this.isAccessoryMessage()) {
	    Matcher m = match(this.toString(), DCCppConstants.ACCESSORY_CMD_REGEX, "Accessory");
	    if (m != null) {
		return(m.group(2));
	    } else {
		return("0");
	    }
	} else 
	    log.error("Accessory Parser called on non-Accessory message type {} message {}", this.getOpCodeChar(), this.toString());
	return("0");
    }

    public int getAccessorySubInt() {
	return(Integer.parseInt(this.getAccessorySubString()));
    }

    public String getAccessoryStateString() {
	if (isAccessoryMessage()) {
	    return(this.getAccessoryStateInt() == 1 ? "ON" : "OFF");
	} else {
	    return("Not an Accessory Decoder");
	}
    }

    public int getAccessoryStateInt() {
	if (this.isAccessoryMessage()) {
	    Matcher m = match(this.toString(), DCCppConstants.ACCESSORY_CMD_REGEX, "Accessory");
	    if (m != null) {
		return(m.group(3).equals(DCCppConstants.ACCESSORY_ON) ? 1 : 0);
	    } else {
		return(0);
	    }
	} else 
	    log.error("Accessory Parser called on non-Accessory message type {} message {}", this.getOpCodeChar(), this.toString());
	    return(0);
    }



    //------------------------------------------------------
    // Helper methods for Throttle Commands

    public String getRegisterString() {
	if (this.isThrottleMessage()) {
	    Matcher m = match(this.toString(), DCCppConstants.THROTTLE_CMD_REGEX, "Throttle");
	    if (m != null) {
		return(m.group(1));
	    } else {
		return("0");
	    }
	} else 
	    log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
	    return("0");
    }

    public int getRegisterInt() {
	return(Integer.parseInt(this.getRegisterString()));
    }

    public String getAddressString() {
	if (this.isThrottleMessage()) {
	    Matcher m = match(this.toString(), DCCppConstants.THROTTLE_CMD_REGEX, "Throttle");
	    if (m != null) {
		return(m.group(2));
	    } else {
		return("0");
	    }
	} else 
	    log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
	    return("0");
    }

    public int getAddressInt() {
	return(Integer.parseInt(this.getAddressString()));
    }

    public String getSpeedString() {
	if (this.isThrottleMessage()) {
	    Matcher m = match(this.toString(), DCCppConstants.THROTTLE_CMD_REGEX, "Throttle");
	    if (m != null) {
		return(m.group(3));
	    } else {
		return("0");
	    }
	} else 
	    log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
	    return("0");
    }

    public int getSpeedInt() {
	return(Integer.parseInt(this.getSpeedString()));
    }

    public String getDirectionString() {
	if (this.isThrottleMessage()) {
	    return(this.getDirectionInt() == 1 ? "Forward" : "Reverse");
	} else {
	    log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
	    return("Not a Throttle");
	}
    }

    public int getDirectionInt() {
	if (this.isThrottleMessage()) {
	    Matcher m = match(this.toString(), DCCppConstants.THROTTLE_CMD_REGEX, "Throttle");
	    if (m != null) {
		return(m.group(4).equals(DCCppConstants.THROTTLE_FORWARD) ? 1 : 0);
	    } else {
		return(0);
	    }
	} else 
	    log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
	    return(0);
    }

    //------------------------------------------------------
    // Helper methods for Function Commands

    public String getFuncAddressString() {
	if (this.isFunctionMessage()) {
	    Matcher m = match(this.toString(), DCCppConstants.FUNCTION_CMD_REGEX, "Function");
	    if (m != null) {
		return(m.group(1));
	    } else {
		return("0");
	    }
	} else 
	    log.error("Function Parser called on non-Function message type {}", this.getOpCodeChar());
	    return("0");
    }

    public int getFuncAddressInt() {
	return(Integer.parseInt(this.getFuncAddressString()));
    }

    public String getFuncByte1String() {
	if (this.isFunctionMessage()) {
	    Matcher m = match(this.toString(), DCCppConstants.FUNCTION_CMD_REGEX, "Function");
	    if (m != null) {
		return(m.group(2));
	    } else {
		return("0");
	    }
	} else 
	    log.error("Function Parser called on non-Function message type {}", this.getOpCodeChar());
	    return("0");
    }

    public int getFuncByte1Int() {
	return(Integer.parseInt(this.getFuncByte1String()));
    }

    public String getFuncByte2String() {
	if (this.isFunctionMessage()) {
	    Matcher m = match(this.toString(), DCCppConstants.FUNCTION_CMD_REGEX, "Function");
	    if ((m != null) && (m.groupCount() > 2)){
		return(m.group(3));
	    } else {
		return("");
	    }
	} else 
	    log.error("Function Parser called on non-Function message type {}", this.getOpCodeChar());
	    return("0");
    }

    public int getFuncByte2Int() {
	if (this.getFuncByte2String() != "") {
	    return(Integer.parseInt(this.getFuncByte2String()));
	} else {
	    return(0);
	}
    }

    //------------------------------------------------------
    // Helper methods for Turnout Commands

    public String getTOIDString() {
	if (this.isTurnoutMessage()) {
	    Matcher m = match(this.toString(), DCCppConstants.TURNOUT_CMD_REGEX, "Turnout");
	    if (m != null) {
		return(m.group(1));
	    } else {
		return("0");
	    }
	} else 
	    log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this.toString());
	    return("0");
    }

    public int getTOIDInt() {
	return(Integer.parseInt(this.getTOIDString()));
    }

    public String getTOStateString() {
	if (isTurnoutMessage()) {
	    return(this.getTOStateInt() == 1 ? "THROWN" : "CLOSED");
	} else {
	    return("Not a Turnout");
	}
    }

    public int getTOStateInt() {
	if (this.isTurnoutMessage()) {
	    Matcher m = match(this.toString(), DCCppConstants.TURNOUT_CMD_REGEX, "Turnout");
	    if (m != null) {
		return(m.group(2).equals(DCCppConstants.TURNOUT_THROWN) ? 1 : 0);
	    } else {
		return(0);
	    }
	} else 
	    log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this.toString());
	    return(0);
    }


    //------------------------------------------------------
    // Helper methods for Prog Write Byte Commands

    public String getCVString() {
	Matcher m;
	int idx = 1;
	if (this.isProgWriteByteMessage()) {
	    idx = 1;
	    m = match(this.toString(), DCCppConstants.PROG_WRITE_BYTE_REGEX, "ProgWriteByte");
	} else if (this.isProgWriteBitMessage()) {
	    idx = 1;
	    m = match(this.toString(), DCCppConstants.PROG_WRITE_BYTE_REGEX, "ProgWriteByte");
	} else if (this.isProgReadMessage()) {
	    idx = 1;
	   m = match(this.toString(), DCCppConstants.PROG_READ_REGEX, "ProgRead");
	} else {
	    log.error("Program Parser called on non-Program message type {}", this.getOpCodeChar());
	    return("0");
	}
	if (m != null) {
	    return(m.group(idx));
	} else {
	    return("0");
	}
    }

    public int getCVInt() {
	return(Integer.parseInt(this.getCVString()));
	
    }

    public String getCallbackNumString() {
	Matcher m;
	int idx = 1;
	if (this.isProgWriteByteMessage()) {
	    idx = 3;
	    m = match(this.toString(), DCCppConstants.PROG_WRITE_BYTE_REGEX, "ProgWriteByte");
	} else if (this.isProgWriteBitMessage()) {
	    idx = 4;
	    m = match(this.toString(), DCCppConstants.PROG_WRITE_BIT_REGEX, "ProgWriteBit");
	} else if (this.isProgReadMessage()) {
	    idx = 2;
	    m = match(this.toString(), DCCppConstants.PROG_READ_REGEX, "ProgRead");
	} else {
	    log.error("Program Parser called on non-Program message type {}", this.getOpCodeChar());
	    return("0");
	}
	if (m != null) {
	    return(m.group(idx));
	} else {
	    return("0");
	}
    }

    public int getCallbackNumInt() {
	return(Integer.parseInt(this.getCallbackNumString()));
    }

    public String getCallbackSubString() {
	Matcher m;
	int idx = 1;
	if (this.isProgWriteByteMessage()) {
	    idx = 4;
	    m = match(this.toString(), DCCppConstants.PROG_WRITE_BYTE_REGEX, "ProgWriteByte");
	} else if (this.isProgWriteBitMessage()) {
	    idx = 5;
	    m = match(this.toString(), DCCppConstants.PROG_WRITE_BIT_REGEX, "ProgWriteBit");
	} else if (this.isProgReadMessage()) {
	    idx = 3;
	    m = match(this.toString(), DCCppConstants.PROG_READ_REGEX, "ProgRead");
	} else {
	    log.error("Program Parser called on non-Program message type {}", this.getOpCodeChar());
	    return("0");
	}
	if (m != null) {
	    return(m.group(idx));
	} else {
	    return("0");
	}
    }

    public int getCallbackSubInt() {
	return(Integer.parseInt(this.getCallbackSubString()));
    }

    public String getValueString() {
	Matcher m;
	int idx = 1;
	if (this.isProgWriteByteMessage()) {
	    idx = 2;
	    m = match(this.toString(), DCCppConstants.PROG_WRITE_BYTE_REGEX, "ProgWriteByte");
	} else if (this.isProgWriteBitMessage()) {
	    idx = 3;
	    m = match(this.toString(), DCCppConstants.PROG_WRITE_BIT_REGEX, "ProgWriteBit");
	} else {
	    log.error("Program Parser called on non-Program message type {}", this.getOpCodeChar());
	    return("0");
	}
	if (m != null) {
	    return(m.group(idx));
	} else {
	    return("0");
	}
    }

    public int getValueInt() {
	return(Integer.parseInt(this.getValueString()));
    }

    //------------------------------------------------------
    // Helper methods for Prog Write Bit Commands

    public String getBitString() {
	if (this.isProgWriteBitMessage()) {
	    Matcher m = match(this.toString(), DCCppConstants.PROG_WRITE_BIT_REGEX, "ProgWriteBit");
	    if (m != null) {
		return(m.group(2));
	    } else {
		return("0");
	    }
	} else 
	    log.error("PWBit Parser called on non-PWBit message type {}", this.getOpCodeChar());
	    return("0");
    }

    public int getBitInt() {
	return(Integer.parseInt(this.getBitString()));
    }

    //------------------------------------------------------

    /*
     * Most messages are sent with a reply expected, but
     * we have a few that we treat as though the reply is always
     * a broadcast message, because the reply usually comes to us 
     * that way.
     */
    // TODO: Not sure this is useful in DCC++
    @Override
    public boolean replyExpected() {
	boolean retv = false;
	switch(this.getOpCodeChar()) {
	case DCCppConstants.THROTTLE_CMD:
	case DCCppConstants.TURNOUT_CMD:
	case DCCppConstants.PROG_WRITE_CV_BYTE:
	case DCCppConstants.PROG_WRITE_CV_BIT:
	case DCCppConstants.PROG_READ_CV:
	case DCCppConstants.TRACK_POWER_ON:
	case DCCppConstants.TRACK_POWER_OFF:
	case DCCppConstants.READ_TRACK_CURRENT:
	case DCCppConstants.READ_CS_STATUS:
	case DCCppConstants.GET_FREE_MEMORY:
	case DCCppConstants.LIST_REGISTER_CONTENTS:
	    retv = true;
	default:
	    retv = false;
	}
	return(retv);
    }

    private boolean responseExpected = true;

    // Tell the traffic controller we expect this
    // message to have a broadcast reply (or not).
    void setResponseExpected(boolean v) {
        responseExpected = v;
    }

    // decode messages of a particular form
    // create messages of a particular form

    /* 
     * The next group of routines are used by Feedback and/or turnout 
     * control code.  These are used in multiple places within the code, 
     * so they appear here. 
     */

    /**
     * Stationary Decoder Message
     *
     * Format: <a ADDRESS SUBADDRESS ACTIVATE>
     *
     *    ADDRESS:  the primary address of the decoder (0-511)
     *    SUBADDRESS: the subaddress of the decoder (0-3)
     *    ACTIVATE: 1=on (set), 0=off (clear)
     *
     *    Note that many decoders and controllers combine the ADDRESS and SUBADDRESS into a single number, N,
     *    from  1 through a max of 2044, where
     *    
     *    N = (ADDRESS - 1) * 4 + SUBADDRESS + 1, for all ADDRESS>0
     *    
     *    OR
     *    
     *    ADDRESS = INT((N - 1) / 4) + 1
     *    SUBADDRESS = (N - 1) % 4
     *    
     *    returns: NONE
    */
    public static DCCppMessage getAccessoryDecoderMsg(int address, int subaddress, boolean activate) {
	// Sanity check inputs
	if (address < 0 || address > DCCppConstants.MAX_ACC_DECODER_ADDRESS)
	    return(null);
	if (subaddress < 0 || subaddress > DCCppConstants.MAX_ACC_DECODER_SUBADDR)
	    return(null);
	
	// Stationary Decoder Command
	String s = new String(Character.toString(DCCppConstants.ACCESSORY_CMD));
	s += Character.toString(DCCppConstants.WHITESPACE);
	
	// Add the Address
	s += Integer.toString(address);
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Add the Subaddress
	s += Integer.toString(subaddress);
	s += Character.toString(DCCppConstants.WHITESPACE);
	
	// Add the activate / deactivate
	s += (activate ? "1" : "0");

	return(new DCCppMessage(s));
    }

    /**
     * Predefined Turnout Control Message
     *
     * Format: <T ID THROW>
     *
     *   ID: the numeric ID (0-32767) of the turnout to control
     *   THROW: 0 (unthrown) or 1 (thrown)
     *   
     *   returns: <H ID THROW>
     */
    public static DCCppMessage getTurnoutCommandMsg(int id, boolean thrown) {
	// Sanity check inputs
	if (id < 0 || id > DCCppConstants.MAX_TURNOUT_ADDRESS) return(null);
	// Need to also validate whether turnout is predefined?  Where to store the IDs?
	// Turnout Command
	String s = new String(Character.toString(DCCppConstants.TURNOUT_CMD));
	s += Character.toString(DCCppConstants.WHITESPACE);


	// Set the ID
	s += Integer.toString(id);
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set the state.
	s += (thrown ? "1" : "0");

	return(new DCCppMessage(s));
    }

    /**
     * Write Direct CV Byte to Programming Track
     *
     * Format: <W CV VALUE CALLBACKNUM CALLBACKSUB>
     *
     *   ID: the numeric ID (0-32767) of the turnout to control
     *   THROW: 0 (unthrown) or 1 (thrown)
     *    CV: the number of the Configuration Variable memory location in the decoder to write to (1-1024)
     *    VALUE: the value to be written to the Configuration Variable memory location (0-255) 
     *    CALLBACKNUM: an arbitrary integer (0-32767) that is ignored by the Base Station and is simply echoed back in the output - useful for external programs that call this function
     *    CALLBACKSUB: a second arbitrary integer (0-32767) that is ignored by the Base Station and is simply echoed back in the output - useful for external programs (e.g. DCC++ Interface) that call this function
     *    
     * Note: The two-argument form embeds the opcode in CALLBACKSUB to aid in decoding the responses.
     *
     *    returns: <r CALLBACKNUM|CALLBACKSUB|CV Value)
     *    where VALUE is a number from 0-255 as read from the requested CV, or -1 if verificaiton read fails
     */
    public static DCCppMessage getWriteDirectCVMsg(int cv, int val) {
	return(getWriteDirectCVMsg(cv, val, 0, DCCppConstants.PROG_WRITE_CV_BYTE));
    }

    public static DCCppMessage getWriteDirectCVMsg(int cv, int val, int callbacknum, int callbacksub) {
	// Sanity check inputs
	if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) return(null);
	if (val < 0 || val > DCCppConstants.MAX_DIRECT_CV_VAL) return(null);
	if (callbacknum < 0 || callbacknum > DCCppConstants.MAX_CALLBACK_NUM)
	    return(null);
	if (callbacksub < 0 || callbacksub > DCCppConstants.MAX_CALLBACK_SUB)
	    return(null);

	// Write CV to Programming Track Command
	String s = new String(Character.toString(DCCppConstants.PROG_WRITE_CV_BYTE));
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set CV
	s += Integer.toString(cv);
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set Value
	s += Integer.toString(val);
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set Callback Number
	s += Integer.toString(callbacknum);
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set Callback Sub
	s += Integer.toString(callbacksub);

	return(new DCCppMessage(s));
    }

    /**
     * Write Direct CV Bit to Programming Track
     *
     * Format: <B CV BIT VALUE CALLBACKNUM CALLBACKSUB>
     *
     *    writes, and then verifies, a single bit within a Configuration Variable to the decoder of an engine on the programming track
     *    
     *    CV: the number of the Configuration Variable memory location in the decoder to write to (1-1024)
     *    BIT: the bit number of the Configurarion Variable memory location to write (0-7)
     *    VALUE: the value of the bit to be written (0-1)
     *    CALLBACKNUM: an arbitrary integer (0-32767) that is ignored by the Base Station and is simply echoed back in the output - useful for external programs that call this function
     *    CALLBACKSUB: a second arbitrary integer (0-32767) that is ignored by the Base Station and is simply echoed back in the output - useful for external programs (e.g. DCC++ Interface) that call this function
     *    
     * Note: The two-argument form embeds the opcode in CALLBACKSUB to aid in decoding the responses.
     *
     *    returns: <r CALLBACKNUM|CALLBACKSUB|CV BIT VALUE)
     *    where VALUE is a number from 0-1 as read from the requested CV bit, or -1 if verificaiton read fails
     */    
    public static DCCppMessage getBitWriteDirectCVMsg(int cv, int bit, boolean val) {
	return(getBitWriteDirectCVMsg(cv, bit, val, 0, DCCppConstants.PROG_WRITE_CV_BIT));
    }

    public static DCCppMessage getBitWriteDirectCVMsg(int cv, int bit, boolean val, int callbacknum, int callbacksub) {

	// Sanity Check Inputs
	if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) return(null);
	if (bit < 0 || bit > 7) return(null);
	if (callbacknum < 0 || callbacknum > DCCppConstants.MAX_CALLBACK_NUM)
	    return(null);
	if (callbacksub < 0 || callbacksub > DCCppConstants.MAX_CALLBACK_SUB)
	    return(null);

	// Write Bit to CV on Programming Track
	String s = new String(Character.toString(DCCppConstants.PROG_WRITE_CV_BIT));
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set CV
	s += Integer.toString(cv);
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set Bit
	s += Integer.toString(bit);
	s += Character.toString(DCCppConstants.WHITESPACE);
	// Set Value
	s += (val ? "1" : "0");
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set Callback Number
	s += Integer.toString(callbacknum);
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set Callback Sub
	s += Integer.toString(callbacksub);

	return(new DCCppMessage(s));
    }

    /**
     * Read Direct CV Byte from Programming Track
     *
     * Format: <R CV CALLBACKNUM CALLBACKSUB>
     *
     *    reads a Configuration Variable from the decoder of an engine on the programming track
     *    
     *    CV: the number of the Configuration Variable memory location in the decoder to read from (1-1024)
     *    CALLBACKNUM: an arbitrary integer (0-32767) that is ignored by the Base Station and is simply echoed back in the output - useful for external programs that call this function
     *    CALLBACKSUB: a second arbitrary integer (0-32767) that is ignored by the Base Station and is simply echoed back in the output - useful for external programs (e.g. DCC++ Interface) that call this function
     *    
     * Note: The two-argument form embeds the opcode in CALLBACKSUB to aid in decoding the responses.
     *
     *    returns: <r CALLBACKNUM|CALLBACKSUB|CV VALUE)
     *    where VALUE is a number from 0-255 as read from the requested CV, or -1 if read could not be verified
     */    
    public static DCCppMessage getReadDirectCVMsg(int cv) {
	return(getReadDirectCVMsg(cv, 0, DCCppConstants.PROG_READ_CV));
    }

    public static DCCppMessage getReadDirectCVMsg(int cv, int callbacknum, int callbacksub) {

	// Sanity check inputs
	if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) return(null);
	if (callbacknum < 0 || callbacknum > DCCppConstants.MAX_CALLBACK_NUM)
	    return(null);
	if (callbacksub < 0 || callbacksub > DCCppConstants.MAX_CALLBACK_SUB)
	    return(null);

	// Read CV from Program Track
	String s = new String(Character.toString(DCCppConstants.PROG_READ_CV));
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set CV
	s += Integer.toString(cv);
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set Callback Number
	s += Integer.toString(callbacknum);
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set Callback Sub
	s += Integer.toString(callbacksub);

	return(new DCCppMessage(s));
    }

    
    /**
     * Write Direct CV Byte to Main Track
     *
     * Format: <w CAB CV VALUE>
     *
     *    writes, without any verification, a Configuration Variable to the decoder of an engine on the main operations track
     *    
     *    CAB:  the short (1-127) or long (128-10293) address of the engine decoder 
     *    CV: the number of the Configuration Variable memory location in the decoder to write to (1-1024)
     *    VALUE: the value to be written to the Configuration Variable memory location (0-255)
     *    
     *    returns: NONE
     */    
    public static DCCppMessage getWriteOpsModeCVMsg(int address, int cv, int val) {
	// Sanity check inputs
	if (address < 0 || address > DCCppConstants.MAX_LOCO_ADDRESS)
	    return(null);
	if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) return(null);
	if (val < 0 || val > DCCppConstants.MAX_DIRECT_CV_VAL) return(null);
	
	// Write CV in Ops Mode
	String s = new String(Character.toString(DCCppConstants.OPS_WRITE_CV_BYTE));
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set address
	s += Integer.toString(address);
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set CV
	s += Integer.toString(cv);
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set Value
	s += Integer.toString(val);

	return(new DCCppMessage(s));
    }

    /**
     * Write Direct CV Bit to Main Track
     *
     * Format: <b CAB CV BIT VALUE>
     *
     *    writes, without any verification, a single bit within a Configuration Variable to the decoder of an engine on the main operations track
     *    
     *    CAB:  the short (1-127) or long (128-10293) address of the engine decoder 
     *    CV: the number of the Configuration Variable memory location in the decoder to write to (1-1024)
     *    BIT: the bit number of the Configurarion Variable regsiter to write (0-7)
     *    VALUE: the value of the bit to be written (0-1)
     *    
     *    returns: NONE
     */        
    public static DCCppMessage getBitWriteOpsModeCVMsg(int address, int cv, int bit, boolean val) {

	// Sanity Check Inputs
	if (address < 0 || address > DCCppConstants.MAX_LOCO_ADDRESS)
	    return(null);
	if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) return(null);
	if (bit < 0 || bit > 7) return(null);
	
	// Write Bit in Ops Mode
	String s = new String(Character.toString(DCCppConstants.OPS_WRITE_CV_BIT));
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set Address
	s += Integer.toString(address);
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set CV
	s += Integer.toString(cv);
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set Bit
	s += Integer.toString(bit);
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Set Value
	s += (val ? "1" : "0");

	return(new DCCppMessage(s));
    }

    /**
     * Set Track Power ON or OFFf
     *
     * Format: <1> (ON) or <0> (OFF)
     *
     * Returns <p1> (ON) or <p0> (OFF)
     */
    public static DCCppMessage getSetTrackPowerMsg(boolean on) {
	String s = new String(Character.toString((on ? DCCppConstants.TRACK_POWER_ON : DCCppConstants.TRACK_POWER_OFF)));
	return(new DCCppMessage(s));
    }

    public static DCCppMessage getTrackPowerOnMsg() {
	return(getSetTrackPowerMsg(true));
    }

    public static DCCppMessage getTrackPowerOffMsg() {
	return(getSetTrackPowerMsg(false));
    }


    /**
     * Read main operations track current
     *
     * Format: <c>
     *
     *    reads current being drawn on main operations track
     *    
     *    returns: <a CURRENT> 
     *    where CURRENT = 0-1024, based on exponentially-smoothed weighting scheme
     */
   public static DCCppMessage getReadTrackCurrentMsg() {
	String s = new String(Character.toString(DCCppConstants.READ_TRACK_CURRENT));
	return(new DCCppMessage(s));
    }

     /**
     * Read DCC++ Base Station Status
     *
     * Format: <s>
     *
     *    returns status messages containing track power status, throttle status, turn-out status, and a version number
     *    NOTE: this is very useful as a first command for an interface to send to this sketch in order to verify connectivity and update any GUI to reflect actual throttle and turn-out settings
     *    
     *    returns: series of status messages that can be read by an interface to determine status of DCC++ Base Station and important settings
     */
  public static DCCppMessage getCSStatusMsg() {
	String s = new String(Character.toString(DCCppConstants.READ_CS_STATUS));
	return(new DCCppMessage(s));
    }


    /*
     * Generate an emergency stop for the specified address
     * @param address is the locomotive address
     *
     * Note: This just sends a THROTTLE command with speed = -1
     */
    public static DCCppMessage getAddressedEmergencyStop(int register, int address) {
	// Sanity check inputs
	if (address < 0 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);

	// Byte 1 is the command
	String s = new String(Character.toString(DCCppConstants.THROTTLE_CMD));
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Field 2 is the Register (WTH?)
	s += Integer.toString(register);
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Field 3 is the DCC address
	s += Integer.toString(address);
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Field 4 is the speed ( -1 for emergency stop)
	s += "-1";
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Field 5 is direction.
	s += Character.toString(DCCppConstants.FORWARD_DIR);

	return(new DCCppMessage(s));
    }
    
    /*
     * Generate a Speed and Direction Request message
     * @param register is the DCC++ base station register assigned.
     * @param address is the locomotive address
     * @param speed a normalized speed value (a floating point number between 0 
     *              and 1).  A negative value indicates emergency stop.
     * @param isForward true for forward, false for reverse.
     *
     * Format: <t REGISTER CAB SPEED DIRECTION>
     *
     *    sets the throttle for a given register/cab combination 
     *    
     *    REGISTER: an internal register number, from 1 through MAX_MAIN_REGISTERS (inclusive), to store the DCC packet used to control this throttle setting
     *    CAB:  the short (1-127) or long (128-10293) address of the engine decoder
     *    SPEED: throttle speed from 0-126, or -1 for emergency stop (resets SPEED to 0)
     *    DIRECTION: 1=forward, 0=reverse.  Setting direction when speed=0 or speed=-1 only effects directionality of cab lighting for a stopped train
     *    
     *    returns: <T REGISTER SPEED DIRECTION>
     *    
     */
    public static DCCppMessage getSpeedAndDirectionMsg(int register, int address, float speed, boolean isForward) {
	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	// Byte 1 is the command
	String s = new String(Character.toString(DCCppConstants.THROTTLE_CMD));
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Field 2 is the Register (WTH?)
	s += Integer.toString(register);
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Field 3 is the DCC address
	s += Integer.toString(address);
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Field 4 is the speed (128 steps, 0-126 or -1 for emergency stop)
	// Emergency Stop is a different JMRI-level command, though.
	if (speed < 0.0) {
	    s += "-1";
	} else {
	    int speedVal = java.lang.Math.round(speed * 126);
	    speedVal = ((speedVal > DCCppConstants.MAX_SPEED) ? DCCppConstants.MAX_SPEED : speedVal);
	    s += Integer.toString(speedVal);
	}
	s += Character.toString(DCCppConstants.WHITESPACE);

	// Field 5 is direction.
	s += Character.toString(isForward ? DCCppConstants.FORWARD_DIR : DCCppConstants.REVERSE_DIR);

	return(new DCCppMessage(s));
    }

    /** 
     * Function Group Messages (common serial format)
     * 
     * Format: <f CAB BYTE1 [BYTE2]>
     *
     *    turns on and off engine decoder functions F0-F28 (F0 is sometimes called FL)  
     *    NOTE: setting requests transmitted directly to mobile engine decoder --- current state of engine functions is not stored by this program
     *    
     *    CAB:  the short (1-127) or long (128-10293) address of the engine decoder
     *    
     *    To set functions F0-F4 on (=1) or off (=0):
     *      
     *    BYTE1:  128 + F1*1 + F2*2 + F3*4 + F4*8 + F0*16
     *    BYTE2:  omitted
     *   
     *    To set functions F5-F8 on (=1) or off (=0):
     *   
     *    BYTE1:  176 + F5*1 + F6*2 + F7*4 + F8*8
     *    BYTE2:  omitted
     *   
     *    To set functions F9-F12 on (=1) or off (=0):
     *   
     *    BYTE1:  160 + F9*1 +F10*2 + F11*4 + F12*8
     *    BYTE2:  omitted
     *   
     *    To set functions F13-F20 on (=1) or off (=0):
     *   
     *    BYTE1: 222 
     *    BYTE2: F13*1 + F14*2 + F15*4 + F16*8 + F17*16 + F18*32 + F19*64 + F20*128
     *   
     *    To set functions F21-F28 on (=1) of off (=0):
     *   
     *    BYTE1: 223
     *    BYTE2: F21*1 + F22*2 + F23*4 + F24*8 + F25*16 + F26*32 + F27*64 + F28*128
     *   
     *    returns: NONE
     * 
     */

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
	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	String s = new String(Character.toString(DCCppConstants.FUNCTION_CMD));
	s += Character.toString(DCCppConstants.WHITESPACE);

	s += Integer.toString(address);
	s += Character.toString(DCCppConstants.WHITESPACE);

	int byte1 = 128 + (f0 ? 16 : 0);
	byte1 += (f1 ? 1 : 0);
	byte1 += (f2 ? 2 : 0);
	byte1 += (f3 ? 4 : 0);
	byte1 += (f4 ? 8 : 0);
	s += Integer.toString(byte1);

	return(new DCCppMessage(s));
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

	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	String s = new String(Character.toString(DCCppConstants.FUNCTION_CMD));
	s += Character.toString(DCCppConstants.WHITESPACE);

	s += Integer.toString(address);
	s += Character.toString(DCCppConstants.WHITESPACE);

	int byte1 = 128 + (f0 ? 16 : 0);
	byte1 += (f1 ? 1 : 0);
	byte1 += (f2 ? 2 : 0);
	byte1 += (f3 ? 4 : 0);
	byte1 += (f4 ? 8 : 0);
	s += Integer.toString(byte1);

	return(new DCCppMessage(s));
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

	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	String s = new String(Character.toString(DCCppConstants.FUNCTION_CMD));
	s += Character.toString(DCCppConstants.WHITESPACE);

	s += Integer.toString(address);
	s += Character.toString(DCCppConstants.WHITESPACE);

	int byte1 = 176;
	byte1 += (f5 ? 1 : 0);
	byte1 += (f6 ? 2 : 0);
	byte1 += (f7 ? 4 : 0);
	byte1 += (f8 ? 8 : 0);
	s += Integer.toString(byte1);

	return(new DCCppMessage(s));
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
	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	String s = new String(Character.toString(DCCppConstants.FUNCTION_CMD));
	s += Character.toString(DCCppConstants.WHITESPACE);

	s += Integer.toString(address);
	s += Character.toString(DCCppConstants.WHITESPACE);

	int byte1 = 176;
	byte1 += (f5 ? 1 : 0);
	byte1 += (f6 ? 2 : 0);
	byte1 += (f7 ? 4 : 0);
	byte1 += (f8 ? 8 : 0);
	s += Integer.toString(byte1);
	return(new DCCppMessage(s));
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

	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	String s = new String(Character.toString(DCCppConstants.FUNCTION_CMD));
	s += Character.toString(DCCppConstants.WHITESPACE);

	s += Integer.toString(address);
	s += Character.toString(DCCppConstants.WHITESPACE);

	int byte1 = 160;
	byte1 += (f9 ? 1 : 0);
	byte1 += (f10 ? 2 : 0);
	byte1 += (f11 ? 4 : 0);
	byte1 += (f12 ? 8 : 0);
	s += Integer.toString(byte1);
	return(new DCCppMessage(s));
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
	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	String s = new String(Character.toString(DCCppConstants.FUNCTION_CMD));
	s += Character.toString(DCCppConstants.WHITESPACE);

	s += Integer.toString(address);
	s += Character.toString(DCCppConstants.WHITESPACE);

	int byte1 = 160;
	byte1 += (f9 ? 1 : 0);
	byte1 += (f10 ? 2 : 0);
	byte1 += (f11 ? 4 : 0);
	byte1 += (f12 ? 8 : 0);
	s += Integer.toString(byte1);
	return(new DCCppMessage(s));
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
	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	String s = new String(Character.toString(DCCppConstants.FUNCTION_CMD));
	s += Character.toString(DCCppConstants.WHITESPACE);

	s += Integer.toString(address);
	s += Character.toString(DCCppConstants.WHITESPACE);

	s += Integer.toString(DCCppConstants.FUNCTION_GROUP4_BYTE1);
	s += Character.toString(DCCppConstants.WHITESPACE);
	int byte2 = 0;
	byte2 += (f13 ? 1 : 0);
	byte2 += (f14 ? 2 : 0);
	byte2 += (f15 ? 4 : 0);
	byte2 += (f16 ? 8 : 0);
	byte2 += (f17 ? 16 : 0);
	byte2 += (f18 ? 32 : 0);
	byte2 += (f19 ? 64 : 0);
	byte2 += (f20 ? 128 : 0);
	s += Integer.toString(byte2);

	return(new DCCppMessage(s));
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
	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	String s = new String(Character.toString(DCCppConstants.FUNCTION_CMD));
	s += Character.toString(DCCppConstants.WHITESPACE);

	s += Integer.toString(address);
	s += Character.toString(DCCppConstants.WHITESPACE);

	s += Integer.toString(DCCppConstants.FUNCTION_GROUP4_BYTE1);
	s += Character.toString(DCCppConstants.WHITESPACE);

	int byte2 = 0;
	byte2 += (f13 ? 1 : 0);
	byte2 += (f14 ? 2 : 0);
	byte2 += (f15 ? 4 : 0);
	byte2 += (f16 ? 8 : 0);
	byte2 += (f17 ? 16 : 0);
	byte2 += (f18 ? 32 : 0);
	byte2 += (f19 ? 64 : 0);
	byte2 += (f20 ? 128 : 0);
	s += Integer.toString(byte2);

	return(new DCCppMessage(s));
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
	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	String s = new String(Character.toString(DCCppConstants.FUNCTION_CMD));
	s += Character.toString(DCCppConstants.WHITESPACE);

	s += Integer.toString(address);
	s += Character.toString(DCCppConstants.WHITESPACE);

	s += Integer.toString(DCCppConstants.FUNCTION_GROUP5_BYTE1);
	s += Character.toString(DCCppConstants.WHITESPACE);
	int byte2 = 0;
	byte2 += (f21 ? 1 : 0);
	byte2 += (f22 ? 2 : 0);
	byte2 += (f23 ? 4 : 0);
	byte2 += (f24 ? 8 : 0);
	byte2 += (f25 ? 16 : 0);
	byte2 += (f26 ? 32 : 0);
	byte2 += (f27 ? 64 : 0);
	byte2 += (f28 ? 128 : 0);
	s += Integer.toString((byte2 & 0x00FF));
	log.debug("DCCppMessage: Byte2 = {} string = {}", byte2, s);

	return(new DCCppMessage(s));
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
	// Sanity check inputs
	if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
	
	String s = new String(Character.toString(DCCppConstants.FUNCTION_CMD));
	s += Character.toString(DCCppConstants.WHITESPACE);

	s += Integer.toString(address);
	s += Character.toString(DCCppConstants.WHITESPACE);

	s += Integer.toString(DCCppConstants.FUNCTION_GROUP5_BYTE1);
	s += Character.toString(DCCppConstants.WHITESPACE);

	int byte2 = 0;
	byte2 += (f21 ? 1 : 0);
	byte2 += (f22 ? 2 : 0);
	byte2 += (f23 ? 4 : 0);
	byte2 += (f24 ? 8 : 0);
	byte2 += (f25 ? 16 : 0);
	byte2 += (f26 ? 32 : 0);
	byte2 += (f27 ? 64 : 0);
	byte2 += (f28 ? 128 : 0);
	s += Integer.toString(byte2);

	return(new DCCppMessage(s));
    }

    /*
     * Build an Emergency Off Message
     */
    

    /**
     * Test Code Functions... not for normal use
     */

    /** Write DCC Packet to a specified Register on the Main*/
    public static DCCppMessage getWriteDCCPacketMainMsg( int register, int num_bytes, byte[] bytes) {
	// Sanity Check Inputs
	if (register < 0 || register > DCCppConstants.MAX_MAIN_REGISTERS) return(null);
	if (num_bytes < 2 || num_bytes > 5) return(null);
	for (int j = 0; j < num_bytes; j++) { if (bytes[j] < 0 || bytes[j] > 255) return(null); }
	
	// Write DCC Packet to the track
	String s = new String(Character.toString(DCCppConstants.WRITE_DCC_PACKET_MAIN));
	s += Character.toString(DCCppConstants.WHITESPACE);
	
	// Set Bytes
	for (int k = 0; k < num_bytes; k++) {
	    s += Integer.toString(bytes[k]);
	    s += Character.toString(DCCppConstants.WHITESPACE);
	}
	
	return(new DCCppMessage(s));	
    }
	
    /** Write DCC Packet to a specified Register on the Programming Track*/
    public static DCCppMessage getWriteDCCPacketProgMsg( int register, int num_bytes, int bytes[]) {
	// Sanity Check Inputs
	if (register < 0 || register > DCCppConstants.MAX_MAIN_REGISTERS) return(null);
	if (num_bytes < 2 || num_bytes > 5) return(null);
	for (int j = 0; j < num_bytes; j++) { if (bytes[j] < 0 || bytes[j] > 255) return(null); }
	
	// Write DCC Packet to the track
	String s = new String(Character.toString(DCCppConstants.WRITE_DCC_PACKET_PROG));
	s += Character.toString(DCCppConstants.WHITESPACE);
	
	// Set Bytes
	for (int k = 0; k < num_bytes; k++) {
	    s += Integer.toString(bytes[k]);
	    s += Character.toString(DCCppConstants.WHITESPACE);
	}
	
	return(new DCCppMessage(s));	
    }

    public static DCCppMessage getCheckFreeMemMsg() {
	// Write DCC Packet to the track
	String s = new String(Character.toString(DCCppConstants.GET_FREE_MEMORY));
	s += Character.toString(DCCppConstants.WHITESPACE);
	return(new DCCppMessage(s));
    }

    public static DCCppMessage getListRegisterContentsMsg() {
	// Write DCC Packet to the track
	String s = new String(Character.toString(DCCppConstants.LIST_REGISTER_CONTENTS));
	s += Character.toString(DCCppConstants.WHITESPACE);
	return(new DCCppMessage(s));
    }

    // initialize logging    
    static Logger log = LoggerFactory.getLogger(DCCppMessage.class.getName());

}

/* @(#)DCCppMessage.java */
