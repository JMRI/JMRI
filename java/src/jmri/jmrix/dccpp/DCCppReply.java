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
public class DCCppReply extends jmri.jmrix.AbstractMRReply {
    
    private ArrayList valueList = new ArrayList();
    private String myRegex;

    // Create a new reply.
    public DCCppReply() {
        super();
        setBinary(false);
    }

    // Create a new reply from an existing reply
    public DCCppReply(DCCppReply reply) {
        super(reply);
        setBinary(false);
        valueList = new ArrayList(reply.valueList);
        myRegex = reply.myRegex;
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
        
    protected DCCppReply(char c, String regex) {
        super();
        setBinary(false);
        myRegex = regex;
        valueList.add(c);
        _nDataChars = this.toString().length();
    }


    public String toString() {
        String s = "";
        for (int i = 0; i < valueList.size(); i++) {
            if (valueList.get(i).getClass().equals(Boolean.class)) {
                s += ((boolean)valueList.get(i) ? "1" : "0");
            } else {
                s += valueList.get(i).toString();
            }
            if (i < valueList.size() -1) {
                s += " ";
            }
        }
        return(s);
    }
    
    public void parseReply(String s) {
        DCCppReply r = DCCppReply.parseDCCppReply(s);
        if (r != null) {
            this.valueList = r.valueList;
            this.myRegex = r.myRegex;
            this._nDataChars = r._nDataChars;
        }
    }
    
    /**
     * Parses a string and generates a DCCppReply from the string contents
     * 
     * @param s String to be parsed
     * @return DCCppReply or null if not a valid formatted string
     */
    public static DCCppReply parseDCCppReply(String s) {
        Matcher m;
        DCCppReply r = new DCCppReply(s.charAt(0), null);
        switch(s.charAt(0)) {
            case DCCppConstants.THROTTLE_REPLY:
                if ((m = match(s, DCCppConstants.THROTTLE_REPLY_REGEX, "ctor")) != null) {
                    r.valueList.add(Integer.parseInt(m.group(1))); // register
                    r.valueList.add(Float.parseFloat(m.group(2))); // speed
                    r.valueList.add(!m.group(3).equals("0")); // direction -- TODO: Make this BOOL?
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                r.myRegex = DCCppConstants.THROTTLE_REPLY_REGEX;
                return(r);  
            case DCCppConstants.TURNOUT_REPLY:
                if ((m = match(s, DCCppConstants.TURNOUT_REPLY_REGEX, "ctor")) != null) {
                    r.valueList.add(Integer.parseInt(m.group(1))); // id
                    r.valueList.add(!m.group(2).equals("0")); // direction -- TODO: Make this BOOL?
                    r.myRegex = DCCppConstants.TURNOUT_REPLY_REGEX;
                } else if ((m = match(s, DCCppConstants.TURNOUT_DEF_REPLY_REGEX, "ctor"))!= null) {
                    r.valueList.add(Integer.parseInt(m.group(1))); // id
                    r.valueList.add(Integer.parseInt(m.group(2))); // address                    
                    r.valueList.add(Integer.parseInt(m.group(3))); // subaddress                    
                    r.myRegex = DCCppConstants.TURNOUT_DEF_REPLY_REGEX;

                } else if ((m = match(s, DCCppConstants.MADC_FAIL_REPLY_REGEX, "ctor")) != null) {
                    r.myRegex = DCCppConstants.MADC_FAIL_REPLY_REGEX;
                    // Do nothing.  Constructor has already done the work
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                log.debug("Parsed Reply: {} length {}", r.toString(), r._nDataChars);
                return(r);  
            case DCCppConstants.OUTPUT_REPLY:
                if ((m = match(s, DCCppConstants.OUTPUT_REPLY_REGEX, "ctor")) != null) {
                    r.valueList.add(Integer.parseInt(m.group(1))); // id
                    r.valueList.add(!m.group(2).equals("0")); // state
                    r.myRegex = DCCppConstants.OUTPUT_REPLY_REGEX;
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                log.debug("Parsed Reply: {} length {}", r.toString(), r._nDataChars);
                return(r);
            case DCCppConstants.PROGRAM_REPLY:
                if ((m = match(s, DCCppConstants.PROGRAM_REPLY_REGEX, "ctor")) != null) {
                    r.valueList.add(Integer.parseInt(m.group(1))); // callback
                    r.valueList.add(Integer.parseInt(m.group(2))); // callbacksub
                    r.valueList.add(Integer.parseInt(m.group(3))); // cv
                    r.valueList.add(Integer.parseInt(m.group(4))); // value
                    r.myRegex = DCCppConstants.PROGRAM_REPLY_REGEX;
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                return(r);  
            case DCCppConstants.VERSION_REPLY:
                if ((m = match(s, DCCppConstants.STATUS_REPLY_REGEX, "ctor")) != null) {
                    r.valueList.add(m.group(1)); // Base Station Type
                    r.valueList.add(": BUILD ");
                    r.valueList.add(m.group(2)); // Build
                    r.myRegex = DCCppConstants.STATUS_REPLY_REGEX;
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                return(r);  
            case DCCppConstants.POWER_REPLY:
                if ((m = match(s, DCCppConstants.TRACK_POWER_REPLY_REGEX, "ctor")) != null) {
                    r.valueList.add(Integer.parseInt(m.group(1))); // on/off
                    r.myRegex = DCCppConstants.TRACK_POWER_REPLY_REGEX;
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                return(r);  
            case DCCppConstants.CURRENT_REPLY:
                if ((m = match(s, DCCppConstants.CURRENT_REPLY_REGEX, "ctor")) != null) {
                    r.valueList.add(Integer.parseInt(m.group(1))); // current
                    r.myRegex = DCCppConstants.CURRENT_REPLY_REGEX;
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                return(r);  
            case DCCppConstants.WRITE_EEPROM_REPLY:
                if ((m = match(s, DCCppConstants.WRITE_EEPROM_REPLY_REGEX, "ctor")) != null) {
                    r.valueList.add(Integer.parseInt(m.group(1))); // num Turnouts
                    r.valueList.add(Integer.parseInt(m.group(2))); // num Sensors
                    r.myRegex = DCCppConstants.WRITE_EEPROM_REPLY_REGEX;
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                return(r);  
            case DCCppConstants.MEMORY_REPLY:
                if ((m = match(s, DCCppConstants.FREE_MEMORY_REPLY_REGEX, "ctor")) != null) {
                    r.valueList.add(Integer.parseInt(m.group(1))); // memory
                    r.myRegex = DCCppConstants.FREE_MEMORY_REPLY_REGEX;
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                return(r);  
//            case DCCppConstants.LISTPACKET_REPLY:
            case DCCppConstants.SENSOR_REPLY_H:
                if ((m = match(s, DCCppConstants.SENSOR_INACTIVE_REPLY_REGEX, "ctor")) != null) {
                    r.valueList.add(Integer.parseInt(m.group(1))); // id
                    r.myRegex = DCCppConstants.SENSOR_INACTIVE_REPLY_REGEX;
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                return(r);  
            case DCCppConstants.SENSOR_REPLY_L:
                if ((m = match(s, DCCppConstants.SENSOR_ACTIVE_REPLY_REGEX, "ctor")) != null) {
                    r.valueList.add(Integer.parseInt(m.group(1))); // id
                    r.myRegex = DCCppConstants.SENSOR_ACTIVE_REPLY_REGEX;
                } else if ((m = match(s, DCCppConstants.SENSOR_DEF_REPLY_REGEX, "ctor")) != null) {
                    r.valueList.add(Integer.parseInt(m.group(1))); // id
                    r.valueList.add(Integer.parseInt(m.group(2))); // pin
                    r.valueList.add(!(m.group(3) == "0")); // pullup
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                return(r);  
            case DCCppConstants.MADC_FAIL_REPLY:
                r.myRegex = DCCppConstants.MADC_FAIL_REPLY_REGEX;
                return(r);
            case DCCppConstants.MADC_SUCCESS_REPLY:
                r.myRegex = DCCppConstants.MADC_SUCCESS_REPLY_REGEX;
                return(r);
            case DCCppConstants.COMM_TYPE_REPLY:
                if ((m = match(s, DCCppConstants.COMM_TYPE_REPLY_REGEX, "ctor")) != null) {
                    r.valueList.add(Integer.parseInt(m.group(1))); // comm type
                    r.valueList.add(m.group(2)); // SERIAL or IP address as string
                    r.myRegex = DCCppConstants.COMM_TYPE_REPLY_REGEX;
                } else {
                    return(null);
                }
                r._nDataChars = r.toString().length();
                return(r);
            default:
                return(null);
        }
    }
    
    public int getOpCode() {
        return((int)(valueList.get(0)) & 0x00FF);
//	return((getElement(0) & 0x00FF));
    }

    /* Get the opcode as a one character string */
    public char getOpCodeChar() {
        return((char)valueList.get(0));
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
        String s = this.toString();
        if(n < 0 || n >= s.length()) {
            return(' ');
        } else {
            return(this.toString().charAt(n));
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
        if (idx < valueList.size()) {
            return((boolean)valueList.get(idx));
        } else {
            log.error("DCCppMessage value index too big. idx = {} msg = {}", idx, this.toString());
            return(false);
        }
    }

    public String getValueString(int idx) {
        if (idx < valueList.size()) {
            return(valueList.get(idx).toString());
        } else {
            log.error("DCCppMessage value index too big. idx = {} msg = {}", idx, this.toString());
            return("");
        }
    }
    
    public int getValueInt(int idx) {
        // TODO: Error-safe this.
        if (valueList.get(idx).getClass() == Boolean.class) {
            return((boolean)valueList.get(idx) ? 1 : 0);
        } else if (valueList.get(idx).getClass() == Integer.class) {
            return(Integer.parseInt(this.getValueString(idx)));
        } else {
            // Hail Mary. Hope the string version can be parsed as an Int.
            return(Integer.parseInt(this.getValueString(idx)));
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

    private static Matcher match(String s, String pat, String name) {
	try {
	    Pattern p = Pattern.compile(pat);
	    Matcher m = p.matcher(s);
	    if (!m.matches()) {
		log.error("Malformed {} Command: {} pattern {}",name, s, pat);
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
	    log.error("Status Parser called on non-Status message type {}", this.getOpCodeChar());
	    return("Version Unknown");
        }
    }

    public String getStatusBuildDateString() {
        if (this.isStatusReply()) {
            return(this.getValueString(3));
        } else {
	    log.error("Status Parser called on non-Status message type {}", this.getOpCodeChar());
	    return("Build Date Unknown");
            
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
	return(this.getValueBool(2));
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
            if (this.getValueString(2).equals(Character.toString(DCCppConstants.PROG_WRITE_CV_BIT))) {
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
	if (this.isOutputAddReply() || this.isOutputCmdReply()) {
            return(this.getValueString(1));
	} else {
	    log.error("OutputAddReply Parser called on non-OutputAddReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getOutputNumInt() {
	return(Integer.parseInt(this.getOutputNumString()));
    }

    public String getOutputAddPinString() {
	if (this.isOutputAddReply()) {
            return(this.getValueString(2));
	} else {
	    log.error("OutputAddReply Parser called on non-OutputAddReply message type {}", this.getOpCodeChar());
	    return("0");
        }
    }

    public int getOutputAddPinInt() {
	return(Integer.parseInt(this.getOutputAddPinString()));
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
    public boolean isSensorDefReply() { return((this.getOpCodeChar() == DCCppConstants.SENSOR_REPLY) && (this.matches(DCCppConstants.SENSOR_DEF_REPLY_REGEX))); }
    public boolean isTurnoutDefReply() { return(this.matches(DCCppConstants.TURNOUT_DEF_REPLY_REGEX)); }
    public boolean isMADCFailReply() { return(this.getOpCodeChar() == DCCppConstants.MADC_FAIL_REPLY); }
    public boolean isMADCSuccessReply() { return(this.getOpCodeChar() == DCCppConstants.MADC_SUCCESS_REPLY); }
    public boolean isStatusReply() { return(this.getOpCodeChar() == DCCppConstants.VERSION_REPLY); }
    public boolean isFreeMemoryReply() { return(this.matches(DCCppConstants.FREE_MEMORY_REPLY_REGEX)); }
    public boolean isOutputAddReply() { return(this.matches(DCCppConstants.OUTPUT_REPLY_REGEX)); }
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
