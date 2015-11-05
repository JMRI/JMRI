// DCCppReply.java
package jmri.jmrix.dccpp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single response from the DCC++ system.
 * <P>
 *
 * @author	Paul Bender Copyright (C) 2004
 * @author	Mark Underwood Copyright (C) 2015
 * @version	$Revision$
 *
 */
public class DCCppReply extends jmri.jmrix.AbstractMRReply {

    private boolean reallyUnsolicited = true;  // used to override automatic
    // unsolicited by message type.

    // Create a new reply.
    public DCCppReply() {
        super();
        setBinary(false);
    }

    // Create a new reply from an existing reply
    public DCCppReply(DCCppReply reply) {
        super(reply);
        setBinary(false);
    }

    /**
     * Create a reply from an DCCppMessage.
     */
    public DCCppReply(DCCppMessage message) {
        super();
        setBinary(false);
        for (int i = 0; i < message.getNumDataElements(); i++) {
            setElement(i, message.getElement(i));
        }
    }

    /**
     * Create a reply from a string of hex characters.
     * 
     * Not sure this one is needed.
     */
    public DCCppReply(String message) {
        super();
        setBinary(false);
        // gather bytes in result
        byte b[] = message.getBytes();
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

    /* Get the opcode as a one character string */
    public char getOpCodeChar() {
	return ((char)getOpCode());
    }

    /* Get the opcode as a string in hex format */
    // Not sure how this is used or applies in DCC++
    public String getOpCodeHex() {
        return "0x" + Integer.toHexString(this.getOpCode());
    }

    /**
     * Get an integer representation of a BCD value
     *
     * @param n byte in message to convert
     * @return Integer value of BCD byte.
     */
    // Not sure how (or if) useful in DCC++
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

    public int maxSize() {
	return DCCppConstants.MAX_MESSAGE_SIZE;
    }

    public int getLength() {
	return(_nDataChars);
    }

    /* Some notes on DCC++ messages and responses...
     *
     * Messages that have responses expected:
     * t : <T REGISTER SPEED DIRECTION>
     * f : (none)
     * a : (none)
     * T : <H ID THROW>
     * w : (none)
     * b : (none)
     * W : <r CALLBACKNUM CALLBACKSUB CV_Value>
     * B : <r CALLBACKNUM CALLBACKSUB CV_Bit_Value>
     * R : <r CALLBACKNUM CALLBACKSUB CV_Value>
     * 1 : <p1>
     * 0 : <p0>
     * c : <a CURRENT>
     * s : Series of status messages...
     *     <p[0,1]>  Power state
     *     <T ...>Throttle responses from all 12 registers
     *     <iDCC++ ... > Base station version and build date
     *     <H ...> All turnout states.
     *
     * Debug messages:
     * M : (none)
     * P : (none)
     * f : <f MEM>
     * L : <M ... data ... >
     */

    //-------------------------------------------------------------------
    // Message helper functions
    // Core methods
    private Matcher match(String s, String pat, String name) {
	try {
	    Pattern p = Pattern.compile(pat);
	    Matcher m = p.matcher(s);
	    if (!m.matches()) {
		log.error("Malformed {} Command: {}",name, s);
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

     //------------------------------------------------------
    // Helper methods for ThrottleReplies

    public String getRegisterString() {
	if (this.isThrottleReply()) {
	    Matcher m = match(this.toString(), DCCppConstants.THROTTLE_REPLY_REGEX, "ThrottleReply");
	    if (m != null) {
		return(m.group(1));
	    } else {
		return("");
	    }
	} else 
	    log.error("ThrottleReply Parser called on non-Throttle message type {}", this.getElement(0));
	    return("0");
    }

    public int getRegisterInt() {
	return(Integer.parseInt(this.getRegisterString()));
    }

    public String getSpeedString() {
	if (this.isThrottleReply()) {
	    Matcher m = match(this.toString(), DCCppConstants.THROTTLE_REPLY_REGEX, "ThrottleReply");
	    if (m != null) {
		return(m.group(2));
	    } else {
		return("");
	    }
	} else 
	    log.error("ThrottleReply Parser called on non-Throttle message type {}", this.getElement(0));
	    return("0");
    }

    public int getSpeedInt() {
	return(Integer.parseInt(this.getSpeedString()));
    }

    public String getDirectionString() {
	if (this.isThrottleReply()) {
	    return(this.getDirectionInt() == 1 ? "Forward" : "Reverse");
	} else {
	    log.error("ThrottleReply Parser called on non-ThrottleReply message type {}", this.getElement(0));
	    return("Not a Throttle");
	}
    }

    public int getDirectionInt() {
	if (this.isThrottleReply()) {
	    Matcher m = match(this.toString(), DCCppConstants.THROTTLE_REPLY_REGEX, "TurnoutReply");
	    if (m != null) {
		return(m.group(3).equals(DCCppConstants.THROTTLE_FORWARD) ? 1 : 0);
	    } else {
		return(0);
	    }
	} else 
	    log.error("ThrottleReply Parser called on non-ThrottleReply message type {}", this.getElement(0));
	    return(0);
    }

     //------------------------------------------------------
    // Helper methods for Turnout Replies

    public String getTOIDString() {
	if (this.isTurnoutReply()) {
	    Matcher m = match(this.toString(), DCCppConstants.TURNOUT_REPLY_REGEX, "TurnoutReply");
	    if (m != null) {
		return(m.group(1));
	    } else {
		return("");
	    }
	} else 
	    log.error("TurnoutReply Parser called on non-TurnoutReply message type {}", this.getElement(0));
	    return("0");
    }

    public int getTOIDInt() {
	return(Integer.parseInt(this.getTOIDString()));
    }

    public String getTOStateString() {
	if (this.isTurnoutReply()) {
	    return(this.getTOStateInt() == 1 ? "Thrown" : "Closed");
	} else {
	    log.error("TurnoutReply Parser called on non-TurnoutReply message type {}", this.getElement(0));
	    return("Not a Turnout");
	}
    }

    public int getTOStateInt() {
	if (this.isTurnoutReply()) {
	    Matcher m = match(this.toString(), DCCppConstants.TURNOUT_REPLY_REGEX, "TurnoutReply");
	    if (m != null) {
		return(m.group(2).equals(DCCppConstants.TURNOUT_THROWN) ? 1 : 0);
	    } else {
		return(0);
	    }
	} else 
	    log.error("TurnoutReply Parser called on non-TurnoutReply message type {}", this.getElement(0));
	    return(0);
    }


     //------------------------------------------------------
    // Helper methods for Program Replies

    public String getCallbackNumString() {
	if (this.isProgramReply()) {
	    Matcher m = match(this.toString(), DCCppConstants.PROGRAM_REPLY_REGEX, "ProgramReply");
	    if (m != null) {
		return(m.group(1));
	    } else {
		return("");
	    }
	} else 
	    log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getElement(0));
	    return("0");
    }

    public int getCallbackNumInt() {
	return(Integer.parseInt(this.getCallbackNumString()));
    }

    public String getCallbackSubString() {
	if (this.isProgramReply()) {
	    Matcher m = match(this.toString(), DCCppConstants.PROGRAM_REPLY_REGEX, "ProgramReply");
	    if (m != null) {
		return(m.group(2));
	    } else {
		return("0");
	    }
	} else 
	    log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getElement(0));
	    return("0");
    }

    public int getCallbackSubInt() {
	return(Integer.parseInt(this.getCallbackSubString()));
    }

    public String getCVString() {
	if (this.isProgramReply()) {
	    Matcher m = match(this.toString(), DCCppConstants.PROGRAM_REPLY_REGEX, "ProgramReply");
	    if (m != null) {
		return(m.group(3));
	    } else {
		return("");
	    }
	} else 
	    log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getElement(0));
	    return("0");
    }

    public int getCVInt() {
	return(Integer.parseInt(this.getCVString()));
    }

    //-------------------------------------------------------------------


     // Message Identification functions
    public boolean isThrottleReply() { return (this.getElement(0) == DCCppConstants.THROTTLE_REPLY); }
    public boolean isTurnoutReply() { return (this.getElement(0) == DCCppConstants.TURNOUT_REPLY); }
    public boolean isProgramReply() { return (this.getElement(0) == DCCppConstants.PROGRAM_REPLY); }
    public boolean isPowerReply() { return (this.getElement(0) == DCCppConstants.POWER_REPLY); }
    public boolean isCurrentReply() { return (this.getElement(0) == DCCppConstants.CURRENT_REPLY); }
    public boolean isMemoryReply() { return (this.getElement(0) == DCCppConstants.MEMORY_REPLY); }
    public boolean isListPacketRegsReply() { return (this.getElement(0) == DCCppConstants.LISTPACKET_REPLY); }

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
        //return (this.getElement(0) == XNetConstants.ACC_INFO_RESPONSE);
	return false;
    }

    /**
     * Is this a feedback broadcast message?
     */
    public boolean isFeedbackBroadcastMessage() {
	return false;
        //return ((this.getElement(0) & 0xF0) == XNetConstants.BC_FEEDBACK);
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
        if (message == DCCppConstants.THROTTLE_REPLY) {
            return true;
        }
        return false;
    }

    /**
     * Does this message indicate the locomotive has been taken over by another
     * device?
     */
    public boolean isThrottleTakenOverMessage() {
	return false;
	/*
        return (this.getElement(0) == XNetConstants.LOCO_INFO_RESPONSE
                && this.getElement(1) == XNetConstants.LOCO_NOT_AVAILABLE);
	*/
    }

    /**
     * Is this a consist message?
     */
    public boolean isConsistMessage() {
	/*
        int message = this.getElement(0);
        if (message == XNetConstants.LOCO_MU_DH_ERROR
                || message == XNetConstants.LOCO_DH_INFO_V1
                || message == XNetConstants.LOCO_DH_INFO_V2) {
            return true;
        }
	*/
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
	return(true);
	/*
        return (this.getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER
                && this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_SEND_SUCCESS);
	*/
    }

    /* 
     * In the interest of code reuse, The following function checks to see 
     * if an XPressNet Message is the Command Station Busy message (61 81 e3)
     */
    public boolean isCSBusyMessage() {
	return false;
	/*
        return (this.getElement(0) == XNetConstants.CS_INFO
                && this.getElement(1) == XNetConstants.CS_BUSY);
	*/
    }


    /* 
     * In the interest of code reuse, The following function checks to see 
     * if an XPressNet Message is the Command Station Transfer Error 
     * message (61 80 e1)
     */
    public boolean isCSTransferError() {
	return false;
	/*
        return (this.getElement(0) == XNetConstants.CS_INFO
                && this.getElement(1) == XNetConstants.CS_TRANSFER_ERROR);
	*/
    }

    /* 
     * In the interest of code reuse, The following function checks to see 
     * if an XPressNet Message is a communications error message.
     * the errors handeled are:
     *		01 01 00  -- Error between interface and the PC
     *		01 02 03  -- Error between interface and the Command Station
     *		01 03 02  -- Unknown Communications Error
     *		01 05 04  -- Timeslot Error
     *          01 06 07  -- LI10x Buffer Overflow
     */
    public boolean isCommErrorMessage() {
	return false;
	/*
        return (this.getElement(0) == XNetConstants.LI_MESSAGE_RESPONSE_HEADER
                && ((this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_UNKNOWN_DATA_ERROR
                || this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_CS_DATA_ERROR
                || this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_PC_DATA_ERROR
                || this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_BUFFER_OVERFLOW
                || this.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR)));
	*/
    }


    /*
     * Is this message a service mode response?
     */
    public boolean isServiceModeResponse() {
	return(getElement(0) == DCCppConstants.PROGRAM_REPLY);
    }

    /*
     * Is this a programming response
     */
    public boolean isProgrammingResponse() {
	return(getElement(0) == DCCppConstants.PROGRAM_REPLY);
    }

    /*
     * Is this message a register or paged mode programming response?
     */
    public boolean isPagedModeResponse() {
	return false;
	/*
        return (getElement(0) == XNetConstants.CS_SERVICE_MODE_RESPONSE
                && getElement(1) == XNetConstants.CS_SERVICE_REG_PAGE_RESPONSE);
	*/
    }

    /*
     * Is this message a direct CV mode programming response?
     */
    public boolean isDirectModeResponse() {
	return (getElement(0) == DCCppConstants.PROGRAM_REPLY);
    }

    /*
     * @return the CV value associated with a service mode reply
     * return -1 if not a service mode message.
     */
    public int getServiceModeCVNumber() {
        int cv = -1;
	/*
        if (isServiceModeResponse()) {
            if ((getElement(1) & XNetConstants.CS_SERVICE_DIRECT_RESPONSE) == XNetConstants.CS_SERVICE_DIRECT_RESPONSE) {
                cv = (getElement(1) - XNetConstants.CS_SERVICE_DIRECT_RESPONSE) * 256 + getElement(2);
            } else {
                cv = getElement(2);
            }
        }
	*/
        return (cv);
    }

    /*
     * @return the value returned by the DCC system associated with a 
     * service mode reply
     * return -1 if not a service mode message.
     */
    public int getServiceModeCVValue() {
        int value = -1;
	/*
        if (isServiceModeResponse()) {
            value = getElement(3);
        }
	*/
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
	return(false);
	/*
        return (super.isUnsolicited()
                || this.isThrottleTakenOverMessage()
                || (this.isFeedbackMessage() && reallyUnsolicited));
	*/
    }

    public final void resetUnsolicited() {
        reallyUnsolicited = false;
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(DCCppReply.class.getName());

}

/* @(#)DCCppReply.java */
