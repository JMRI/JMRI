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
 * Based on XNetReply
 *
 */
public class DCCppReply extends jmri.jmrix.AbstractMRReply {

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

    public int getOpCode() {
	return((getElement(0) & 0x00FF));
    }

    /* Get the opcode as a one character string */
    public char getOpCodeChar() {
	return ((char)(getElement(0) & 0x00FF));
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
	return DCCppConstants.MAX_REPLY_SIZE;
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
     * W : <r CALLBACKNUM|CALLBACKSUB|CV CV_Value>
     * B : <r CALLBACKNUM CALLBACKSUB|CV|Bit CV_Bit_Value>
     * R : <r CALLBACKNUM|CALLBACKSUB|CV CV_Value>
     * 1 : <p1>
     * 0 : <p0>
     * c : <a CURRENT>
     * s : Series of status messages...
     *     <p[0,1]>  Power state
     *     <T ...>Throttle responses from all 12 registers
     *     <iDCC++ ... > Base station version and build date
     *     <H ...> All turnout states.
     *
     * Unsolicited Replies
     *   | <Q snum [0,1]> Sensor reply.
     * Debug messages:
     * M : (none)
     * P : (none)
     * f : <f MEM>
     * L : <M ... data ... >
     */

    //-------------------------------------------------------------------
    // Message helper functions
    // Core methods

    private boolean matches(String pat) {
	return(match(this.toString(), pat, "Validator") != null);
    }

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
		log.debug("register: {}", m.group(1));
		return(m.group(1));
	    } else {
		return("0");
	    }
	} else 
	    log.error("ThrottleReply Parser called on non-Throttle message type {}", this.getOpCodeChar());
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
		return("0");
	    }
	} else 
	    log.error("ThrottleReply Parser called on non-Throttle message type {}", this.getOpCodeChar());
	    return("0");
    }

    public int getSpeedInt() {
	return(Integer.parseInt(this.getSpeedString()));
    }

    public String getDirectionString() {
	if (this.isThrottleReply()) {
	    return(this.getDirectionInt() == 1 ? "Forward" : "Reverse");
	} else {
	    log.error("ThrottleReply Parser called on non-ThrottleReply message type {}", this.getOpCodeChar());
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
	    log.error("ThrottleReply Parser called on non-ThrottleReply message type {}", this.getOpCodeChar());
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
		return("0");
	    }
	} else 
	    log.error("TurnoutReply Parser called on non-TurnoutReply message type {}", this.getOpCodeChar());
	    return("0");
    }

    public int getTOIDInt() {
	return(Integer.parseInt(this.getTOIDString()));
    }

    public String getTOStateString() {
	if (this.isTurnoutReply()) {
	    return(this.getTOStateInt() == 1 ? DCCppConstants.TURNOUT_THROWN : DCCppConstants.TURNOUT_CLOSED);
	} else {
	    log.error("TurnoutReply Parser called on non-TurnoutReply message type {}", this.getOpCodeChar());
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
	    log.error("TurnoutReply Parser called on non-TurnoutReply message type {}", this.getOpCodeChar());
	    return(0);
    }

    public boolean getTOIsThrown() {
	return(this.getTOStateString().equals(DCCppConstants.TURNOUT_THROWN));
    }

    public boolean getTOIsClosed() {
	return(this.getTOStateString().equals(DCCppConstants.TURNOUT_CLOSED));
    }


     //------------------------------------------------------
    // Helper methods for Program Replies

    public String getCallbackNumString() {
	if (this.isProgramReply()) {
	    Matcher m = match(this.toString(), DCCppConstants.PROGRAM_REPLY_REGEX, "ProgramReply");
	    if (m != null) {
		return(m.group(1));
	    } else {
		return("0");
	    }
	} else 
	    log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getOpCodeChar());
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
	    log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getOpCodeChar());
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
		return("0");
	    }
	} else 
	    log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getOpCodeChar());
	    return("0");
    }

    public int getCVInt() {
	return(Integer.parseInt(this.getCVString()));
    }

    public String getReadValueString() {
	if (this.isProgramReply()) {
	    Matcher m = match(this.toString(), DCCppConstants.PROGRAM_REPLY_REGEX, "ProgramReply");
	    if (m != null) {
		if (m.group(2).equals(Integer.toString((int)DCCppConstants.PROG_WRITE_CV_BIT)))
		    return(m.group(6));
		else
		    return(m.group(4));
	    } else {
		return("0");
	    }
	} else 
	    log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getOpCodeChar());
	    return("0");
    }

    public int getReadValueInt() {
	return(Integer.parseInt(this.getReadValueString()));
    }

    public String getCurrentString() {
	if (this.isCurrentReply()) {
	    Matcher m = match(this.toString(), DCCppConstants.CURRENT_REPLY_REGEX, "ProgramReply");
	    if (m != null) {
		return(m.group(1));
	    } else {
		return("0");
	    }
	} else 
	    log.error("CurrentReply Parser called on non-CurrentReply message type {} message {}", this.getOpCodeChar(), this.toString());
	    return("0");
	
    }

    public int getCurrentInt() {
	return(Integer.parseInt(this.getCurrentString()));
    }

    public boolean getPowerBool() {
	if (this.isPowerReply()) {
	    Matcher m = match(this.toString(), DCCppConstants.TRACK_POWER_REPLY_REGEX, "PowerReply");
	    if (m != null) {
		return(m.group(1).equals(DCCppConstants.POWER_ON));
	    } else {
		return(false);
	    }
	} else 
	    log.error("CurrentReply Parser called on non-CurrentReply message type {} message {}", this.getOpCodeChar(), this.toString());
	    return(false);

    }


    public String getSensorNumString() {
	if (this.isSensorReply()) {
	    Matcher m = match(this.toString(), DCCppConstants.SENSOR_REPLY_REGEX, "SensorReply");
	    if (m != null) {
		return(m.group(1));
	    } else {
		return("0");
	    }
	} else 
	    log.error("SensorReply Parser called on non-SensorReply message type {}", this.getOpCodeChar());
	    return("0");
    }

    public int getSensorNumInt() {
	return(Integer.parseInt(this.getSensorNumString()));
    }

    public String getSensorStateString() {
	if (this.isSensorReply()) {
	    return(this.getSensorStateInt() == 1 ? "Active" : "Inactive");
	} else {
	    log.error("SensorReply Parser called on non-SensorReply message type {}", this.getOpCodeChar());
	    return("Not a Sensor");
	}
    }

    public int getSensorStateInt() {
	if (this.isSensorReply()) {
	    Matcher m = match(this.toString(), DCCppConstants.SENSOR_REPLY_REGEX, "SensorReply");
	    if (m != null) {
		return(m.group(2).equals(DCCppConstants.SENSOR_ON) ? 1 : 0);
	    } else {
		return(0);
	    }
	} else 
	    log.error("SensorReply Parser called on non-SensorReply message type {}", this.getOpCodeChar());
	    return(0);
    }

    public boolean getSensorIsActive() {
	return(this.getSensorStateString().equals(DCCppConstants.SENSOR_ON));
    }

    public boolean getSensorIsInactive() {
	return(this.getSensorStateString().equals(DCCppConstants.SENSOR_OFF));
    }

    //-------------------------------------------------------------------


     // Message Identification functions
    public boolean isThrottleReply() { return (this.getOpCodeChar() == DCCppConstants.THROTTLE_REPLY); }
    public boolean isTurnoutReply() { return (this.getOpCodeChar() == DCCppConstants.TURNOUT_REPLY); }
    public boolean isProgramReply() { return (this.getOpCodeChar() == DCCppConstants.PROGRAM_REPLY); }
    public boolean isPowerReply() { return (this.getOpCodeChar() == DCCppConstants.POWER_REPLY); }
    public boolean isCurrentReply() { return (this.getOpCodeChar() == DCCppConstants.CURRENT_REPLY); }
    public boolean isMemoryReply() { return (this.getOpCodeChar() == DCCppConstants.MEMORY_REPLY); }
    public boolean isVersionReply() { return (this.getOpCodeChar() == DCCppConstants.VERSION_REPLY); }
    public boolean isListPacketRegsReply() { return (this.getOpCodeChar() == DCCppConstants.LISTPACKET_REPLY); }
    public boolean isSensorReply() { return(this.getOpCodeChar() == DCCppConstants.SENSOR_REPLY); }

    public boolean isValidReplyFormat() {
	// NOTE: Does not (yet) handle STATUS replies
	if (this.matches(DCCppConstants.THROTTLE_REPLY_REGEX))
	    return(true);
	if (this.matches(DCCppConstants.TURNOUT_REPLY_REGEX))
	    return(true);
	if (this.matches(DCCppConstants.PROGRAM_REPLY_REGEX))
	    return(true);
	if (this.matches(DCCppConstants.TRACK_POWER_REPLY_REGEX))
	    return(true);
	if (this.matches(DCCppConstants.CURRENT_REPLY_REGEX))
	    return(true);
	if (this.matches(DCCppConstants.SENSOR_REPLY_REGEX))
	    return(true);
	if (this.matches(DCCppConstants.BROKEN_SENSOR_REPLY_REGEX))
	    return(true);
	if (this.isVersionReply())
	    return(true);

	return(false);
    }

    // decode messages of a particular form 
    /* 
     * The next group of routines are used by Feedback and/or turnout 
     * control code.  These are used in multiple places within the code, 
     * so they appear here. 
     */

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
        //return (this.getOpCodeChar() == XNetConstants.ACC_INFO_RESPONSE);
	return false;
    }

    /**
     * Is this a feedback broadcast message?
     */
    public boolean isFeedbackBroadcastMessage() {
	return(this.isTurnoutReply());
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

    // initialize logging
    static Logger log = LoggerFactory.getLogger(DCCppReply.class.getName());

}

/* @(#)DCCppReply.java */
