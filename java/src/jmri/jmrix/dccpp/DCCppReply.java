// DCCppReply.java
package jmri.jmrix.dccpp;

import java.util.ArrayList;
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

/*
 * A few notes on implementation
 * 
 * DCCppReply objects are (usually) created by parsing a String that is the result
 * of an incoming reply message from the Base Station.
 * The information is stored as a String, along with a Regex string that allows
 * the individual data elements to be extracted when needed.
 *
 * Listeners and other higher level code should first check to make sure the
 * DCCppReply is of the correct type by calling the relevant isMessageType() method.
 * Then, call the various getThisDataElement() method to retrieve the data of 
 * interest.
 * 
 * For example, to get the Speed from a Throttle Reply, first check that it
 * /is/ a ThrottleReply by calling message.isThrottleReply(), and then
 * get the speed by calling message.getSpeedInt() or getSpeedString().
 *
 * The reason for all of this misdirection is to make sure that the upper layer
 * JMRI code is isolated/insulated from any changes in the actual Base Station
 * message format.  For example, there is no need for the listener code to know
 * that the speed is the second number after the "T" in the reply (nor that a
 * Throttle reply starts with a "T"). 
 *
 */

public class DCCppReply extends jmri.jmrix.AbstractMRReply {
    
    private ArrayList<Integer> valueList = new ArrayList<>();
    protected String myRegex;
    protected StringBuilder myReply;
    protected char opcode;

    // Create a new reply.
    public DCCppReply() {
        super();
        setBinary(false);
        myRegex = "";
        myReply = new StringBuilder("");
        opcode = 0x00;
    }

    // Create a new reply from an existing reply
    public DCCppReply(DCCppReply reply) {
        super(reply);
        setBinary(false);
        valueList = new ArrayList(reply.valueList);
        myRegex = reply.myRegex;
        myReply = reply.myReply;
    }

    /**
     * Create a reply from an DCCppMessage.
     */
    // NOTE: Not Used
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
    @Deprecated
    public DCCppReply(String message) {
        super();
        setBinary(false);
        myReply = new StringBuilder(message);
        // gather bytes in result
        _nDataChars = myReply.length();
        _dataChars = new int[_nDataChars];
    }
        
    protected DCCppReply(char c, String regex) {
        super();
        setBinary(false);
        myRegex = regex;
        opcode = c;
        myReply = new StringBuilder(Character.toString(opcode));
        _nDataChars = myReply.length();
        log.debug("DCCppReply() opcode = {} ({})", opcode, Character.toString(opcode));
    }


    @Override
    public String toString() {
        log.debug("DCCppReply.toString(): char {} {} msg {}", opcode, Character.toString(opcode), myReply.toString());
        return(myReply.toString());
    }
    
    public void parseReply(String s) {
        DCCppReply r = DCCppReplyParser.parseReply(s);
        log.debug("in parseReply() string: {}", s);
        if (r != null) {
            this.opcode = r.opcode;
            this.valueList = r.valueList;
            this.myRegex = r.myRegex;
            this.myReply = r.myReply;
            this._nDataChars = r._nDataChars;
            log.debug("copied: this: {} opcode {}", this.toString(), opcode);
        }
    }
    
    ///
    ///
    /// TODO: Stopped Refactoring to StringBuilder here 12/12/15
    ///
    ///
    
    /**
     * Parses a string and generates a DCCppReply from the string contents
     * 
     * @param s String to be parsed
     * @return DCCppReply or null if not a valid formatted string
     */
    public static DCCppReply parseDCCppReply(String s) {
        
        log.debug("Parse charAt(0): {} ({})", s.charAt(0), Character.toString(s.charAt(0)));
        DCCppReply r = new DCCppReply(s.charAt(0), null);
        switch(s.charAt(0)) {
            case DCCppConstants.VERSION_REPLY:
                if (s.matches(DCCppConstants.STATUS_REPLY_REGEX)) {
                    r.myReply = new StringBuilder(s);
                    log.debug("Status Reply: {}", r.toString());
                    r._nDataChars = r.toString().length();
                    r.myRegex = DCCppConstants.STATUS_REPLY_REGEX;
                    return(r);
                } else {
                    return(null);
                }
                
            case DCCppConstants.THROTTLE_REPLY:
                if (s.matches(DCCppConstants.THROTTLE_REPLY_REGEX)) {
                    r.myReply = new StringBuilder(s);
                } else {
                    return(null);
                }
                log.debug("Throttle Reply: {}", r.toString());
                r._nDataChars = r.toString().length();
                r.myRegex = DCCppConstants.THROTTLE_REPLY_REGEX;
                return(r);  
            case DCCppConstants.TURNOUT_REPLY:
                if (s.matches(DCCppConstants.TURNOUT_REPLY_REGEX)) {
                    r.myReply = new StringBuilder(s);
                    r.myRegex = DCCppConstants.TURNOUT_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.TURNOUT_DEF_REPLY_REGEX)) {
                    r.myReply = new StringBuilder(s);
                    r.myRegex = DCCppConstants.TURNOUT_DEF_REPLY_REGEX;

                } else if (s.matches(DCCppConstants.MADC_FAIL_REPLY_REGEX)) {
                    r.myReply = new StringBuilder(s);
                    r.myRegex = DCCppConstants.MADC_FAIL_REPLY_REGEX;
                    // Do nothing.  Constructor has already done the work
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                log.debug("Parsed Reply: {} length {}", r.toString(), r._nDataChars);
                return(r);  
            case DCCppConstants.OUTPUT_REPLY:
                if (s.matches(DCCppConstants.OUTPUT_REPLY_REGEX)) {
                    r.myReply = new StringBuilder(s);
                    r.myRegex = DCCppConstants.OUTPUT_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.OUTPUT_LIST_REPLY_REGEX)) {
                    r.myReply = new StringBuilder(s);
                    r.myRegex = DCCppConstants.OUTPUT_LIST_REPLY_REGEX;
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                log.debug("Parsed Reply: {} length {}", r.toString(), r._nDataChars);
                return(r);
            case DCCppConstants.PROGRAM_REPLY:
                if (s.matches(DCCppConstants.PROGRAM_REPLY_REGEX)) {
                    r.myReply = new StringBuilder(s);
                    r.myRegex = DCCppConstants.PROGRAM_REPLY_REGEX;
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                return(r);  
            case DCCppConstants.POWER_REPLY:
                if (s.matches(DCCppConstants.TRACK_POWER_REPLY_REGEX)) {
                    r.myReply = new StringBuilder(s);
                    r.myRegex = DCCppConstants.TRACK_POWER_REPLY_REGEX;
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                return(r);  
            case DCCppConstants.CURRENT_REPLY:
                if (s.matches(DCCppConstants.CURRENT_REPLY_REGEX)) {
                    r.myReply = new StringBuilder(s);
                    r.myRegex = DCCppConstants.CURRENT_REPLY_REGEX;
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                return(r);  
            case DCCppConstants.WRITE_EEPROM_REPLY:
                if (s.matches(DCCppConstants.WRITE_EEPROM_REPLY_REGEX)) {
                    r.myReply = new StringBuilder(s);
                    r.myRegex = DCCppConstants.WRITE_EEPROM_REPLY_REGEX;
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                return(r);  
            case DCCppConstants.MEMORY_REPLY:
                if (s.matches(DCCppConstants.FREE_MEMORY_REPLY_REGEX)) {
                    r.myReply = new StringBuilder(s);
                    r.myRegex = DCCppConstants.FREE_MEMORY_REPLY_REGEX;
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                return(r);  
//            case DCCppConstants.LISTPACKET_REPLY:
            case DCCppConstants.SENSOR_REPLY_H:
                if (s.matches(DCCppConstants.SENSOR_INACTIVE_REPLY_REGEX)) {
                    r.myReply = new StringBuilder(s);
                    r.myRegex = DCCppConstants.SENSOR_INACTIVE_REPLY_REGEX;
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                return(r);  
            case DCCppConstants.SENSOR_REPLY_L:
                if (s.matches(DCCppConstants.SENSOR_ACTIVE_REPLY_REGEX)) {
                    r.myReply = new StringBuilder(s);
                    r.myRegex = DCCppConstants.SENSOR_ACTIVE_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.SENSOR_DEF_REPLY_REGEX)) {
                    r.myReply = new StringBuilder(s);
                    r.myRegex = DCCppConstants.SENSOR_DEF_REPLY_REGEX;
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                return(r);  
            case DCCppConstants.MADC_FAIL_REPLY:
                r.myReply = new StringBuilder(s);
                r.myRegex = DCCppConstants.MADC_FAIL_REPLY_REGEX;
                return(r);
            case DCCppConstants.MADC_SUCCESS_REPLY:
                r.myReply = new StringBuilder(s);
                r.myRegex = DCCppConstants.MADC_SUCCESS_REPLY_REGEX;
                return(r);
            default:
                return(null);
        }
    }
    
    public int getOpCode() {
        return((int)(opcode) & 0x00FF);
//	return((getElement(0) & 0x00FF));
    }

    /* Get the opcode as a one character string */
    public char getOpCodeChar() {
        return(opcode);
//	return ((char)(getElement(0) & 0x00FF));
    }

    /* Get the opcode as a string in hex format */
    // Not sure how this is used or applies in DCC++
    @Deprecated
    public String getOpCodeHex() {
        return "0x" + Integer.toHexString(this.getOpCode());
    }

    @Override
    public int getElement(int n) {
        if ((n >= 0) && (n < myReply.length())) {
            return(myReply.charAt(n));
        } else {
            return(' ');
        }
    }
 
    public void setElement(int n, int v) {
        // We want the ASCII value, not the string interpretation of the int
        char c = (char)(v & 0xFF);
        if (myReply == null) {
            myReply = new StringBuilder(Character.toString(c));
        } else if (n >= myReply.length()) {
            myReply.append(c);
        } else if (n > 0) {
            myReply.setCharAt(n,c);
        }
    }
    /**
     * Get an integer representation of a BCD value
     *
     * @param n byte in message to convert
     * @return Integer value of BCD byte.
     */
    // Not sure how (or if) useful in DCC++
    @Deprecated
    public Integer getElementBCD(int n) {
        return Integer.decode(Integer.toHexString(getElement(n)));
    }

    public boolean getValueBool(int idx) {
        Matcher m = this.match(myReply.toString(), myRegex, "gvi");
        if ((m != null) && (idx <= m.groupCount())) {
            return(!m.group(idx).equals("0"));
        } else {
            log.error("DCCppReply value index too big. idx = {} msg = {}", idx, this.toString());
            return(false);
        }
    }

    public String getValueString(int idx) {
        Matcher m = this.match(myReply.toString(), myRegex, "gvs");
        if ((m != null) && (idx <= m.groupCount())) {
            return(m.group(idx));
        } else {
            log.error("DCCppReply value index too big. idx = {} msg = {}", idx, this.toString());
            return("");
        }
    }
    
    public int getValueInt(int idx) {
        Matcher m = this.match(myReply.toString(), myRegex, "gvi");
        if ((m != null) && (idx <= m.groupCount())) {
            return(Integer.parseInt(m.group(idx)));
        } else {
            log.error("DCCppReply value index too big. idx = {} msg = {}", idx, this.toString());
            return(0);
        }
    }

    /* 
     * skipPrefix is not used at this point in time, but is 
     *  defined as abstract in AbstractMRReply 
     */
    @Override
    protected int skipPrefix(int index) {
        return -1;
    }

    @Override
    public int maxSize() {
	return DCCppConstants.MAX_REPLY_SIZE;
    }

    public int getLength() {
        return(myReply.length());
	//return(_nDataChars);
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

    protected boolean matches(String pat) {
	return(match(this.toString(), pat, "Validator") != null);
    }

    protected static Matcher match(String s, String pat, String name) {
	try {
	    Pattern p = Pattern.compile(pat);
	    Matcher m = p.matcher(s);
	    if (!m.matches()) {
		//log.debug("No Match {} Command: {} pattern {}",name, s, pat);
		return(null);
	    }
	    return(m);

	} catch (PatternSyntaxException e) {
            log.error("Malformed DCC++ reply syntax! s = ", pat);
	    return(null);
        } catch (IllegalStateException e) {
            log.error("Group called before match operation executed string= " + s);
	    return(null);
        } catch (IndexOutOfBoundsException e) {
            log.error("Index out of bounds string= " + s);
	    return(null);
        }
    }



    public String getStatusVersionString() {
        if (this.isStatusReply()) {
            return(this.getValueString(1));
        } else {
            return("Unknown");
        }
    }

    public String getStatusBuildDateString() {
        if (this.isStatusReply()) {
            return(this.getValueString(2));
        } else {
            return("Unknown");
        }
    }

     //------------------------------------------------------
    // Helper methods for ThrottleReplies

    public String getRegisterString() {
	if (this.isThrottleReply()) {
            return(this.getValueString(1));
	} else {
	    log.error("ThrottleReply Parser called on non-Throttle message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getRegisterInt() {
        if (this.isThrottleReply()) {
            return(this.getValueInt(1));
        } else {
	    log.error("ThrottleReply Parser called on non-Throttle message type {}", this.getOpCodeChar());
	    return(0);
        }
    }

    public String getSpeedString() {
	if (this.isThrottleReply()) {
            return(this.getValueString(2));
	} else {
	    log.error("ThrottleReply Parser called on non-Throttle message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getSpeedInt() {
        if (this.isThrottleReply()) {
            return(this.getValueInt(2));
        } else {
	    log.error("ThrottleReply Parser called on non-Throttle message type {}", this.getOpCodeChar());
	    return(0);
        }
    }

    public String getDirectionString() {
        // Will return "Forward" (true) or "Reverse" (false)
	if (this.isThrottleReply()) {
	    return(this.getValueBool(3) ? "Forward" : "Reverse");
	} else {
	    log.error("ThrottleReply Parser called on non-ThrottleReply message type {}", this.getOpCodeChar());
	    return("Not a Throttle");
	}
    }

    public int getDirectionInt() {
        // Will return 1 (true) or 0 (false)
	if (this.isThrottleReply()) {
            return(this.getValueInt(3));
	} else {
	    log.error("ThrottleReply Parser called on non-ThrottleReply message type {}", this.getOpCodeChar());
	    return(0);
        }
    }
    
    public boolean getDirectionBool() {
        // Will return true or false
        if (this.isThrottleReply()) {
            return(this.getValueBool(3));
	} else {
	    log.error("ThrottleReply Parser called on non-ThrottleReply message type {}", this.getOpCodeChar());
	    return(false);
        }
    }

     //------------------------------------------------------
    // Helper methods for Turnout Replies

    public String getTOIDString() {
	if (this.isTurnoutReply()) {
            return(this.getValueString(1));
	} else {
	    log.error("TurnoutReply Parser called on non-TurnoutReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getTOIDInt() {
        if (this.isTurnoutReply()) {
            return(this.getValueInt(1));
        } else {
	    log.error("TurnoutReply Parser called on non-TurnoutReply message type {}", this.getOpCodeChar());
	    return(0);
        }
    }

    public String getTOStateString() {
        // Will return human readable state. To get string value for command, use
        // getTOStateInt().toString()
	if (this.isTurnoutReply()) {
	    return(this.getTOStateInt() == 1 ? "THROWN" : "CLOSED");
	} else {
	    log.error("TurnoutReply Parser called on non-TurnoutReply message type {}", this.getOpCodeChar());
	    return("Not a Turnout");
	}
    }

    public int getTOStateInt() {
        // Will return 1 (true - thrown) or 0 (false - closed)
	if (this.isTurnoutReply()) {
            return(this.getValueInt(2));
	} else {
	    log.error("TurnoutReply Parser called on non-TurnoutReply message type {}", this.getOpCodeChar());
	    return(0);
        }
    }

    public boolean getTOIsThrown() {
	return(this.getValueBool(1));
    }

    public boolean getTOIsClosed() {
	return(!this.getValueBool(2));
    }


     //------------------------------------------------------
    // Helper methods for Program Replies

    public String getCallbackNumString() {
	if (this.isProgramReply()) {
            return(this.getValueString(1));
	} else {
	    log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getCallbackNumInt() {
        if (this.isProgramReply()) {
            return(this.getValueInt(1));
        } else {
	    log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getOpCodeChar());
	    return(0);
        }
    }

    public String getCallbackSubString() {
	if (this.isProgramReply()) {
            return(this.getValueString(2));
	} else {
	    log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getCallbackSubInt() {
        if (this.isProgramReply()) {
            return(this.getValueInt(2));
        } else {
	    log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getOpCodeChar());
	    return(0);
        }
    }

    public String getCVString() {
	if (this.isProgramReply()) {
            return(this.getValueString(3));
	} else {
	    log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getCVInt() {
        if (this.isProgramReply()) {
            return(this.getValueInt(3));
        } else {
	    log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getOpCodeChar());
	    return(0);
        }
    }

    public String getReadValueString() {
	if (this.isProgramReply()) {
            if (this.opcode == DCCppConstants.PROG_WRITE_CV_BIT) {
                return(this.getValueString(6));
            } else {
                return(this.getValueString(4));
            }
	} else {
	    log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getReadValueInt() {
	return(Integer.parseInt(this.getReadValueString()));
    }

    public String getCurrentString() {
	if (this.isCurrentReply()) {
            return(this.getValueString(1));
	} else {
	    log.error("CurrentReply Parser called on non-CurrentReply message type {} message {}", this.getOpCodeChar(), this.toString());
	    return("0");
        }
    }

    public int getCurrentInt() {
	return(Integer.parseInt(this.getCurrentString()));
    }

    public boolean getPowerBool() {
	if (this.isPowerReply()) {
            return(this.getValueString(1).equals(DCCppConstants.POWER_ON));
	} else {
	    log.error("CurrentReply Parser called on non-CurrentReply message type {} message {}", this.getOpCodeChar(), this.toString());
	    return(false);
        }
    }

    public String getTurnoutDefNumString() {
	if (this.isTurnoutDefReply()) {
            return(this.getValueString(1));
	} else {
	    log.error("TurnoutDefReply Parser called on non-TurnoutDefReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getTurnoutDefNumInt() {
	return(Integer.parseInt(this.getTurnoutDefNumString()));
    }

    public String getTurnoutDefAddrString() {
	if (this.isTurnoutDefReply()) {
            return(this.getValueString(2));
	} else {
	    log.error("TurnoutDefReply Parser called on non-TurnoutDefReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getTurnoutDefAddrInt() {
	return(Integer.parseInt(this.getTurnoutDefAddrString()));
    }

    public String getTurnoutDefSubAddrString() {
	if (this.isTurnoutDefReply()) {
            return(this.getValueString(3));
	} else {
	    log.error("TurnoutDefReply Parser called on non-TurnoutDefReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getTurnoutDefSubAddrInt() {
	return(Integer.parseInt(this.getTurnoutDefSubAddrString()));
    }


    public String getOutputNumString() {
	if (this.isOutputListReply() || this.isOutputCmdReply()) {
            return(this.getValueString(1));
	} else {
	    log.error("OutputAddReply Parser called on non-OutputAddReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getOutputNumInt() {
	return(Integer.parseInt(this.getOutputNumString()));
    }

    public String getOutputListPinString() {
	if (this.isOutputListReply()) {
            return(this.getValueString(2));
	} else {
	    log.error("OutputAddReply Parser called on non-OutputAddReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getOutputListPinInt() {
	return(Integer.parseInt(this.getOutputListPinString()));
    }

    public String getOutputListIFlagString() {
	if (this.isOutputListReply()) {
            return(this.getValueString(3));
	} else {
	    log.error("OutputListReply Parser called on non-OutputListReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getOutputListIFlagInt() {
	return(Integer.parseInt(this.getOutputListIFlagString()));
    }

    public String getOutputListStateString() {
	if (this.isOutputListReply()) {
            return(this.getValueString(4));
	} else {
	    log.error("OutputListReply Parser called on non-OutputListReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getOutputListStateInt() {
	return(Integer.parseInt(this.getOutputListStateString()));
    }

    public boolean getOutputCmdStateBool() {
	if (this.isOutputCmdReply()) {
            return((this.getValueBool(2)));
	} else {
	    log.error("OutputCmdReply Parser called on non-OutputCmdReply message type {}", this.getOpCodeChar());
	    return(false);
        }
    }
    
    public String getOutputCmdStateString() {
	if (this.isOutputCmdReply()) {
            return(this.getValueBool(2) ? "THROWN" : "CLOSED");
	} else {
	    log.error("OutputCmdReply Parser called on non-OutputCmdReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getOutputCmdStateInt() {
	return(this.getOutputCmdStateBool() ? 1 : 0);
    }

    public boolean getOutputIsHigh() {
        return(this.getOutputCmdStateBool());
    }
    
    public boolean getOutputIsLow() {
        return(!this.getOutputCmdStateBool());
    }
    
    public String getSensorDefNumString() {
	if (this.isSensorDefReply()) {
            return(this.getValueString(1));
	} else {
	    log.error("SensorDefReply Parser called on non-SensorDefReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getSensorDefNumInt() {
	return(Integer.parseInt(this.getSensorDefNumString()));
    }

    public String getSensorDefPinString() {
	if (this.isSensorDefReply()) {
            return(this.getValueString(2));
	} else {
	    log.error("SensorDefReply Parser called on non-SensorDefReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getSensorDefPinInt() {
	return(Integer.parseInt(this.getSensorDefPinString()));
    }

    public String getSensorDefPullupString() {
	if (this.isSensorDefReply()) {
	    return(this.getSensorDefPullupBool() ? "Pullup" : "NoPullup");
	} else {
	    log.error("SensorDefReply Parser called on non-SensorDefReply message type {}", this.getOpCodeChar());
	    return("Not a Sensor");
	}
    }

    public int getSensorDefPullupInt() {
	if (this.isSensorDefReply()) {
            return(this.getValueInt(3));
	} else {
	    log.error("SensorDefReply Parser called on non-SensorDefReply message type {}", this.getOpCodeChar());
	    return(0);
        }
    }
    public boolean getSensorDefPullupBool() {
	if (this.isSensorDefReply()) {
            return(this.getValueBool(3));
	} else {
	    log.error("SensorDefReply Parser called on non-SensorDefReply message type {}", this.getOpCodeChar());
	    return(false);
        }
    }
    
    public String getSensorNumString() {
	if (this.isSensorReply()) {
            return(this.getValueString(1));
	} else {
	    log.error("SensorReply Parser called on non-SensorReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getSensorNumInt() {
	return(Integer.parseInt(this.getSensorNumString()));
    }

    public String getSensorStateString() {
	if (this.isSensorReply()) {
            return(this.myRegex.equals(DCCppConstants.SENSOR_ACTIVE_REPLY_REGEX) ? "Active" : "Inactive");
        } else {
	    log.error("SensorReply Parser called on non-SensorReply message type {}", this.getOpCodeChar());
	    return("Not a Sensor");
	}
    }

    public int getSensorStateInt() {
	if (this.isSensorReply()) {
            return(this.myRegex.equals(DCCppConstants.SENSOR_ACTIVE_REPLY_REGEX) ? 1 : 0);
	} else 
	    log.error("SensorReply Parser called on non-SensorReply message type {}", this.getOpCodeChar());
	    return(0);
    }

    public boolean getSensorIsActive() {
        return(this.myRegex.equals(DCCppConstants.SENSOR_ACTIVE_REPLY_REGEX));
    }

    public boolean getSensorIsInactive() {
        return(this.myRegex.equals(DCCppConstants.SENSOR_INACTIVE_REPLY_REGEX));
    }
    
    public String getFreeMemoryString() {
 	if (this.isFreeMemoryReply()) {
            return(this.getValueString(1));
	} else {
	    log.error("FreeMemoryReply Parser called on non-FreeMemoryReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getFreeMemoryInt() {
	return(Integer.parseInt(this.getFreeMemoryString()));
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
//    public boolean isListPacketRegsReply() { return (this.getOpCodeChar() == DCCppConstants.LISTPACKET_REPLY); }
    public boolean isSensorReply() { return((this.getOpCodeChar() == DCCppConstants.SENSOR_REPLY) ||
					    (this.getOpCodeChar() == DCCppConstants.SENSOR_REPLY_H) ||
					    (this.getOpCodeChar() == DCCppConstants.SENSOR_REPLY_L)); }
    public boolean isSensorDefReply() { return(this.matches(DCCppConstants.SENSOR_DEF_REPLY_REGEX)); }
    public boolean isTurnoutDefReply() { return(this.matches(DCCppConstants.TURNOUT_DEF_REPLY_REGEX)); }
    public boolean isMADCFailReply() { return(this.getOpCodeChar() == DCCppConstants.MADC_FAIL_REPLY); }
    public boolean isMADCSuccessReply() { return(this.getOpCodeChar() == DCCppConstants.MADC_SUCCESS_REPLY); }
    public boolean isStatusReply() { return(this.getOpCodeChar() == DCCppConstants.VERSION_REPLY); }
    public boolean isFreeMemoryReply() { return(this.matches(DCCppConstants.FREE_MEMORY_REPLY_REGEX)); }
    public boolean isOutputListReply() { return(this.matches(DCCppConstants.OUTPUT_LIST_REPLY_REGEX)); }
    public boolean isOutputCmdReply() { return(this.matches(DCCppConstants.OUTPUT_REPLY_REGEX)); }

    public boolean isValidReplyFormat() {
	// NOTE: Does not (yet) handle STATUS replies
	if ((this.matches(DCCppConstants.THROTTLE_REPLY_REGEX)) ||
	    (this.matches(DCCppConstants.TURNOUT_REPLY_REGEX)) ||
	    (this.matches(DCCppConstants.LIST_TURNOUTS_REPLY_REGEX)) ||
	    (this.matches(DCCppConstants.LIST_SENSORS_REPLY_REGEX)) ||
	    (this.matches(DCCppConstants.PROGRAM_REPLY_REGEX)) ||
	    (this.matches(DCCppConstants.TRACK_POWER_REPLY_REGEX)) ||
	    (this.matches(DCCppConstants.CURRENT_REPLY_REGEX)) ||
	    (this.matches(DCCppConstants.SENSOR_REPLY_REGEX)) ||
	    (this.matches(DCCppConstants.BROKEN_SENSOR_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.SENSOR_DEF_REPLY_REGEX)) ||    
	    (this.matches(DCCppConstants.SENSOR_INACTIVE_REPLY_REGEX)) ||
	    (this.matches(DCCppConstants.SENSOR_ACTIVE_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.OUTPUT_REPLY_REGEX)) ||    
	    (this.matches(DCCppConstants.MADC_FAIL_REPLY_REGEX)) ||
	    (this.matches(DCCppConstants.MADC_SUCCESS_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.STATUS_REPLY_REGEX)) ||
	    (this.isVersionReply())
	    ) {
	    return(true);
	} else {
	return(false);
	}
    }

    // decode messages of a particular form 
    /* 
     * The next group of routines are used by Feedback and/or turnout 
     * control code.  These are used in multiple places within the code, 
     * so they appear here. 
     */

    // NOTE: Methods below here are holdovers from XPressNet implementation
    // They should be removed when/if possible.


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
     * @param startByte The address byte for this address byte data byte pair.
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
    private final static Logger log = LoggerFactory.getLogger(DCCppReply.class.getName());

}

/* @(#)DCCppReply.java */
