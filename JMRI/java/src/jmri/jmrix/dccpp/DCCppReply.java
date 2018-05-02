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
 * @author Paul Bender Copyright (C) 2004
 * @author Mark Underwood Copyright (C) 2015
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

    protected String myRegex;
    protected StringBuilder myReply;

    // Create a new reply.
    public DCCppReply() {
        super();
        setBinary(false);
        myRegex = "";
        myReply = new StringBuilder();
    }

    // Create a new reply from an existing reply
    public DCCppReply(DCCppReply reply) {
        super(reply);
        setBinary(false);
        myRegex = reply.myRegex;
        myReply = reply.myReply;
    }

    // Create a new reply from a string
    public DCCppReply(String reply) {
        super();
        setBinary(false);
        myRegex = "";
        myReply = new StringBuilder(reply);
        _nDataChars = toString().length();
    }

    @Override
    public String toString() {
        log.trace("DCCppReply.toString(): msg {}", myReply.toString());
        return myReply.toString();
    }

   /**
    * Generate text translations of replies for use in the DCCpp monitor.
    *
    * @return representation of the DCCppReply as a string.
    */
   public String toMonitorString(){
        // Beautify and display
        String text;

        switch (getOpCodeChar()) {
            case DCCppConstants.THROTTLE_REPLY:
                text = "Throttle Reply: \n";
                text += "\tRegister: " + getRegisterString() + "\n";
                text += "\tSpeed: " + getSpeedString() + "\n";
                text += "\tDirection: " + getDirectionString();
                break;
            case DCCppConstants.TURNOUT_REPLY:
                text = "Turnout Reply: \n";
                text += "\tT/O Number: " + getTOIDString() + "\n";
                text += "\tDirection: " + getTOStateString();
                break;
            case DCCppConstants.SENSOR_REPLY_H:
                text = "Sensor Reply (Inactive): \n";
                text += "\tSensor Number: " + getSensorNumString() + "\n";
                text += "\tState: INACTIVE";
                break;
            case DCCppConstants.SENSOR_REPLY_L:
                // Also covers the V1.0 version SENSOR_REPLY
                if (isSensorDefReply()) {
                    text = "Sensor Def Reply: \n";
                    text += "\tSensor Number: " + getSensorDefNumString() + "\n";
                    text += "\tSensor Pin: " + getSensorDefPinString() + "\n";
                    text += "\tSensor Pullup: " + getSensorDefPullupString();
                } else {
                    text = "Sensor Reply (Active): \n";
                    text += "\tSensor Number: " + getSensorNumString() + "\n";
                    text += "\tState: ACTIVE";
                }
                break;
            case DCCppConstants.OUTPUT_REPLY:
                if (isOutputCmdReply()) {
                    text = "Output Command Reply: \n";
                    text += "\tOutput Number: " + getOutputNumString() + "\n";
                    text += "\tOutputState: " + getOutputCmdStateString();
                } else if (isOutputListReply()) {
                    text = "Output Command Reply: \n";
                    text += "\tOutput Number: " + getOutputNumString() + "\n";
                    text += "\tOutputState: " + getOutputListPinString() + "\n";
                    text += "\tOutputState: " + getOutputListIFlagString() + "\n";
                    text += "\tOutputState: " + getOutputListStateString();
                } else {
                    text = "Invalid Output Reply Format: \n";
                    text += "\t" + toString();
                }
                break;
            case DCCppConstants.PROGRAM_REPLY:
                if (isProgramBitReply()) {
                    text = "Program Bit Reply: \n";
                    text += "\tCallback Num: " + getCallbackNumString() + "\n";
                    text += "\tCallback Sub: " + getCallbackSubString() + "\n";
                    text += "\tCV: " + getCVString() + "\n";
                    text += "\tCV Bit: " + getProgramBitString() + "\n";
                    text += "\tValue: " + getReadValueString();
                } else {
                    text = "Program Reply: \n";
                    text += "\tCallback Num: " + getCallbackNumString() + "\n";
                    text += "\tCallback Sub: " + getCallbackSubString() + "\n";
                    text += "\tCV: " + getCVString() + "\n";
                    text += "\tValue: " + getReadValueString();
                }
                break;
            case DCCppConstants.STATUS_REPLY:
                text = "Base Station Status: \n";
                text += "\tVersion: " + getStatusVersionString() + "\n";
                text += "\tBuild: " + getStatusBuildDateString();
                break;
            case DCCppConstants.POWER_REPLY:
                if(isNamedPowerReply()) {
                    text = "Power Status: \n";
                    text += "\tName:" + getPowerDistrictName();
                    text += "\tStatus:" + getPowerDistrictStatus();
                } else {
                    text = "Power Status: ";
                    text += ((char) (getElement(1) & 0x00FF) == '1' ? "ON" : "OFF");
                }
                break;
            case DCCppConstants.CURRENT_REPLY:
                text = "Current: " + getCurrentString() + " / 1024";
                break;
            // case DCCppConstants.LISTPACKET_REPLY:
            //     // TODO: Implement this fully
            //     text = "List Packet Reply...\n";
            //     break;
            case DCCppConstants.WRITE_EEPROM_REPLY:
                text = "Write EEPROM Reply...\n";
                // TODO: Don't use getProgValueString()
                text += "\tTurnouts: " + getValueString(1) + "\n";
                text += "\tSensors: " + getValueString(2);
                text += "\tOutputs: " + getValueString(3);
                break;
            case DCCppConstants.MEMORY_REPLY:
                // TODO: Implement this fully
                text = "Memory Reply...\n";
                text += "\tFree Memory: " + getFreeMemoryString();
                break;
            case DCCppConstants.COMM_TYPE_REPLY:
                text = "Comm Type Reply ";
                text += "Type: " + Integer.toString(getCommTypeInt());
                text += " Port: " + getCommTypeValueString();
                break;
            case DCCppConstants.MADC_FAIL_REPLY:
                text = "No Sensor/Turnout/Output Reply ";
                break;
            case DCCppConstants.MADC_SUCCESS_REPLY:
                text = "Sensor/Turnout/Output MADC Success Reply ";
                break;
            default:
                text = "Unregonized reply: ";
                text += toString() + "\n\tvals: ";
                text += toString().replace("", " ").trim(); // inserts a space for every character
        }

        return text;
    }

    public void parseReply(String s) {
        DCCppReply r = DCCppReply.parseDCCppReply(s);
        log.debug("in parseReply() string: {}", s);
        if (r != null) {
            this.myRegex = r.myRegex;
            this.myReply = r.myReply;
            this._nDataChars = r._nDataChars;
            log.debug("copied: this: {}", this.toString());
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
        DCCppReply r = new DCCppReply(s);
        switch(s.charAt(0)) {
            case DCCppConstants.STATUS_REPLY:
                if (s.matches(DCCppConstants.STATUS_REPLY_ESP32_REGEX)) {
                    log.debug("Status Reply: {}", r.toString());
                    r.myRegex = DCCppConstants.STATUS_REPLY_ESP32_REGEX;
                } else if (s.matches(DCCppConstants.STATUS_REPLY_REGEX)) {
                    log.debug("Status Reply: {}", r.toString());
                    r.myRegex = DCCppConstants.STATUS_REPLY_REGEX;
                } 
                return(r);
            case DCCppConstants.THROTTLE_REPLY:
                if (s.matches(DCCppConstants.THROTTLE_REPLY_REGEX)) {
                   log.debug("Throttle Reply: {}", r.toString());
                   r.myRegex = DCCppConstants.THROTTLE_REPLY_REGEX;
                }
                return(r);
            case DCCppConstants.TURNOUT_REPLY:
                if (s.matches(DCCppConstants.TURNOUT_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.TURNOUT_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.TURNOUT_DEF_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.TURNOUT_DEF_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.MADC_FAIL_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.MADC_FAIL_REPLY_REGEX;
                }
                log.debug("Parsed Reply: {} length {}", r.toString(), r._nDataChars);
                return(r);
            case DCCppConstants.OUTPUT_REPLY:
                if (s.matches(DCCppConstants.OUTPUT_LIST_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.OUTPUT_LIST_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.OUTPUT_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.OUTPUT_REPLY_REGEX;
                }
                log.debug("Parsed Reply: {} length {}", r.toString(), r._nDataChars);
                return(r);
            case DCCppConstants.PROGRAM_REPLY:
                if (s.matches(DCCppConstants.PROGRAM_BIT_REPLY_REGEX)) {
                    log.debug("Matches ProgBitReply");
                    r.myRegex = DCCppConstants.PROGRAM_BIT_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.PROGRAM_REPLY_REGEX)) {
                    log.debug("Matches ProgReply");
                    r.myRegex = DCCppConstants.PROGRAM_REPLY_REGEX;
                } else {
                    log.debug("Does not match ProgReply Regex");
                }
                return(r);
            case DCCppConstants.POWER_REPLY:
                if (s.matches(DCCppConstants.TRACK_POWER_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.TRACK_POWER_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.TRACK_POWER_REPLY_NAMED_REGEX)) {
                    r.myRegex = DCCppConstants.TRACK_POWER_REPLY_NAMED_REGEX;
                }
                return(r);
            case DCCppConstants.CURRENT_REPLY:
                if (s.matches(DCCppConstants.CURRENT_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.CURRENT_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.CURRENT_REPLY_NAMED_REGEX)) {
                    r.myRegex = DCCppConstants.CURRENT_REPLY_NAMED_REGEX;
                }
                return(r);
            case DCCppConstants.WRITE_EEPROM_REPLY:
                if (s.matches(DCCppConstants.WRITE_EEPROM_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.WRITE_EEPROM_REPLY_REGEX;
                }
                return(r);
            case DCCppConstants.MEMORY_REPLY:
                if (s.matches(DCCppConstants.FREE_MEMORY_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.FREE_MEMORY_REPLY_REGEX;
                }
                return(r);
            case DCCppConstants.SENSOR_REPLY_H:
                if (s.matches(DCCppConstants.SENSOR_INACTIVE_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.SENSOR_INACTIVE_REPLY_REGEX;
                }
                return(r);
            case DCCppConstants.SENSOR_REPLY_L:
                if (s.matches(DCCppConstants.SENSOR_ACTIVE_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.SENSOR_ACTIVE_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.SENSOR_DEF_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.SENSOR_DEF_REPLY_REGEX;
                }
                return(r);
            case DCCppConstants.MADC_FAIL_REPLY:
                r.myRegex = DCCppConstants.MADC_FAIL_REPLY_REGEX;
                return(r);
            case DCCppConstants.MADC_SUCCESS_REPLY:
                r.myRegex = DCCppConstants.MADC_SUCCESS_REPLY_REGEX;
                return(r);
            case DCCppConstants.COMM_TYPE_REPLY:
                r.myRegex = DCCppConstants.COMM_TYPE_REPLY_REGEX;
                return(r);
            default:
                return(r);
        }
    }

    /**
     * 
     * Not really used inside of DCC++.  Just here
     * to play nicely with the inheritance.
     * 
     * TODO: If this is unused, can we just not override it
     * and (not) "use" the superclass version?
     * ANSWER: No, we can't because the superclass looks in
     * the _datachars element, which we don't use, and which
     * will contain garbage data.  Better to return something
     * meaningful.
     * @return first char of myReply as integer
     */
    @Override
    public int getOpCode() {
        if (myReply.length() > 0) {
            return(Character.getNumericValue(myReply.charAt(0)));
        } else {
            return(0);
        }
    }

    /** Get the opcode as a one character string.
     * @return first char of myReply
     */
    public char getOpCodeChar() {
        if (myReply.length() > 0) {
            return(myReply.charAt(0));
        } else {
            return(0);
        }
// return ((char)(getElement(0) & 0x00FF));
    }

    @Override
    public int getElement(int n) {
        if ((n >= 0) && (n < myReply.length())) {
            return(myReply.charAt(n));
        } else {
            return(' ');
        }
    }

    @Override
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
        Matcher m = DCCppReply.match(myReply.toString(), myRegex, "gvi");
        if ((m != null) && (idx <= m.groupCount())) {
            return(!m.group(idx).equals("0"));
        } else {
            log.error("DCCppReply value index too big. idx = {} msg = {}", idx, this.toString());
            return(false);
        }
    }

    public String getValueString(int idx) {
        Matcher m = DCCppReply.match(myReply.toString(), myRegex, "gvs");
        if ((m != null) && (idx <= m.groupCount())) {
            return(m.group(idx));
        } else {
            log.error("DCCppReply value index too big. idx = {} msg = {}", idx, this.toString());
            return("");
        }
    }

    public int getValueInt(int idx) {
        Matcher m = DCCppReply.match(myReply.toString(), myRegex, "gvi");
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

    public String getProgramBitString() {
        if (this.isProgramBitReply()) {
            return(this.getValueString(4));
        } else {
            log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getOpCodeChar());
            return("0");
        }
    }

    public int getProgramBitInt() {
        if (this.isProgramBitReply()) {
            return(this.getValueInt(4));
        } else {
            log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getOpCodeChar());
            return(0);
        }
    }

    public String getReadValueString() {
        if (this.isProgramReply()) {
            if (this.matches(DCCppConstants.PROGRAM_BIT_REPLY_REGEX)) {
                return(this.getValueString(5));
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

    public String getPowerDistrictName() {
        if (this.isNamedPowerReply()) {
            return(this.getValueString(2));
        } else {
            log.error("NamedPowerReply Parser called on non-NamedPowerReply message type {} message {}", this.getOpCodeChar(), this.toString());
            return("");
        }
    }

    public String getPowerDistrictStatus() {
        if (this.isNamedPowerReply()) {
           if(this.getValueString(1).equals(DCCppConstants.POWER_OFF)) {
               return("OFF");
           } else if(this.getValueString(1).equals(DCCppConstants.POWER_ON)) {
               return("ON");
           } else {
               return("OVERLOAD");
           }
        } else {
            log.error("NamedPowerReply Parser called on non-NamedPowerReply message type {} message {}", this.getOpCodeChar(), this.toString());
            return("");
        }
    }

    public String getCurrentString() {
        if (this.isCurrentReply()) {
            if(this.isNamedCurrentReply()) {
                return(this.getValueString(2));
            }
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
            return(this.getValueBool(2) ? "HIGH" : "LOW");
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
        } else {
            log.error("SensorReply Parser called on non-SensorReply message type {}", this.getOpCodeChar());
        return(0);
        }
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
    
    public int getCommTypeInt() {
        if (this.isCommTypeReply()) {
            return(this.getValueInt(1));
        } else {
            log.error("CommTypeReply Parser called on non-CommTypeReply message type {}", this.getOpCodeChar());
            return(0);
        }
    }
    
    public String getCommTypeValueString() {
        if (this.isCommTypeReply()) {
            return(this.getValueString(2));
        } else {
            log.error("CommTypeReply Parser called on non-CommTypeReply message type {}", this.getOpCodeChar());
            return("N/A");
        }
    }


    //-------------------------------------------------------------------


     // Message Identification functions
    public boolean isThrottleReply() { return (this.getOpCodeChar() == DCCppConstants.THROTTLE_REPLY); }
    public boolean isTurnoutReply() { return (this.getOpCodeChar() == DCCppConstants.TURNOUT_REPLY); }
    public boolean isProgramReply() { return (this.matches(DCCppConstants.PROGRAM_REPLY_REGEX)); }
    public boolean isProgramBitReply() { return (this.matches(DCCppConstants.PROGRAM_BIT_REPLY_REGEX)); }
    public boolean isPowerReply() { return (this.getOpCodeChar() == DCCppConstants.POWER_REPLY); }
    public boolean isNamedPowerReply() { return(this.matches(DCCppConstants.TRACK_POWER_REPLY_NAMED_REGEX)); }
    public boolean isCurrentReply() { return (this.getOpCodeChar() == DCCppConstants.CURRENT_REPLY); }
    public boolean isNamedCurrentReply() { return(this.matches(DCCppConstants.CURRENT_REPLY_NAMED_REGEX)); }
    public boolean isMemoryReply() { return (this.getOpCodeChar() == DCCppConstants.MEMORY_REPLY); }
    public boolean isVersionReply() { return (this.getOpCodeChar() == DCCppConstants.STATUS_REPLY); }
//    public boolean isListPacketRegsReply() { return (this.getOpCodeChar() == DCCppConstants.LISTPACKET_REPLY); }
    public boolean isSensorReply() { return((this.getOpCodeChar() == DCCppConstants.SENSOR_REPLY) ||
         (this.getOpCodeChar() == DCCppConstants.SENSOR_REPLY_H) ||
         (this.getOpCodeChar() == DCCppConstants.SENSOR_REPLY_L)); }
    public boolean isSensorDefReply() { return(this.matches(DCCppConstants.SENSOR_DEF_REPLY_REGEX)); }
    public boolean isTurnoutDefReply() { return(this.matches(DCCppConstants.TURNOUT_DEF_REPLY_REGEX)); }
    public boolean isMADCFailReply() { return(this.getOpCodeChar() == DCCppConstants.MADC_FAIL_REPLY); }
    public boolean isMADCSuccessReply() { return(this.getOpCodeChar() == DCCppConstants.MADC_SUCCESS_REPLY); }
    public boolean isStatusReply() { return(this.getOpCodeChar() == DCCppConstants.STATUS_REPLY); }
    public boolean isESPStatusReply() { return(this.matches(DCCppConstants.STATUS_REPLY_ESP32_REGEX)); }
    public boolean isFreeMemoryReply() { return(this.matches(DCCppConstants.FREE_MEMORY_REPLY_REGEX)); }
    public boolean isOutputListReply() { return(this.matches(DCCppConstants.OUTPUT_LIST_REPLY_REGEX)); }
    public boolean isOutputCmdReply() { return(this.matches(DCCppConstants.OUTPUT_REPLY_REGEX)); }
    public boolean isCommTypeReply() { return(this.matches(DCCppConstants.COMM_TYPE_REPLY_REGEX)); }

    public boolean isValidReplyFormat() {
        // NOTE: Does not (yet) handle STATUS replies
        if ((this.matches(DCCppConstants.THROTTLE_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.TURNOUT_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.LIST_TURNOUTS_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.LIST_SENSORS_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.PROGRAM_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.TRACK_POWER_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.TRACK_POWER_REPLY_NAMED_REGEX)) ||
            (this.matches(DCCppConstants.CURRENT_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.CURRENT_REPLY_NAMED_REGEX)) ||
            (this.matches(DCCppConstants.SENSOR_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.BROKEN_SENSOR_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.SENSOR_DEF_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.SENSOR_INACTIVE_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.SENSOR_ACTIVE_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.OUTPUT_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.MADC_FAIL_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.MADC_SUCCESS_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.STATUS_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.STATUS_REPLY_ESP32_REGEX)) ||
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

    // NOTE: Methods below here are holdovers from XpressNet implementation
    // They should be removed when/if possible.


    /**
     * Is this a feedback response message?
     * @return true for feedback response
     */
    public boolean isFeedbackMessage() {
        //return (this.getOpCodeChar() == XNetConstants.ACC_INFO_RESPONSE);
        return false;
    }

    /**
     * Is this a feedback broadcast message?
     * @return true for feedback broadcast
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
    private final static Logger log = LoggerFactory.getLogger(DCCppReply.class);

}
