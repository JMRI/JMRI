package jmri.jmrix.dccpp;

import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.configurexml.AbstractXmlAdapter;

/**
 * Represents a single response from the DCC++ system.
 *
 * @author Paul Bender Copyright (C) 2004
 * @author Mark Underwood Copyright (C) 2015
 * @author Harald Barth Copyright (C) 2019
 *
 * Based on XNetReply
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
        log.trace("DCCppReply.toString(): msg '{}'", myReply);
        return myReply.toString();
    }

   /**
    * Generate text translations of replies for use in the DCCpp monitor.
    *
    * @return representation of the DCCppReply as a string.
    **/
    @Override
    public String toMonitorString(){
        // Beautify and display
        String text;

        switch (getOpCodeChar()) {
            case DCCppConstants.THROTTLE_REPLY:
                text = "Throttle Reply: ";
                text += "Register: " + getRegisterString() + ", ";
                text += "Speed: " + getSpeedString() + ", ";
                text += "Direction: " + getDirectionString();
                break;
            case DCCppConstants.TURNOUT_REPLY:
                if (isTurnoutCmdReply()) {
                    text = "Turnout Reply: ";
                    text += "ID: " + getTOIDString() + ", ";
                    text += "Dir: " + getTOStateString();
                } else if (isTurnoutDefReply()) {
                    text = "Turnout Def Reply: ";
                    text += "ID:" + getTOIDString() + ", ";
                    text += "Address:" + getTOAddressString() + ", ";
                    text += "Index:" + getTOAddressIndexString() + ", ";
                    // if we are able to parse the address and index we can convert it
                    // to a standard DCC address for display.
                    if (getTOAddressInt() != -1 && getTOAddressIndexInt() != -1) {
                        int boardAddr = getTOAddressInt();
                        int boardIndex = getTOAddressIndexInt();
                        int dccAddress = (((boardAddr - 1) * 4) + boardIndex) + 1;
                        text += "DCC Address: " + dccAddress + ", ";
                    }
                    text += "Dir: " + getTOStateString();
                } else if (isTurnoutDefDCCReply()) {
                    text = "Turnout Def DCC Reply: ";
                    text += "ID:" + getTOIDString() + ", ";
                    text += "Address:" + getTOAddressString() + ", ";
                    text += "Index:" + getTOAddressIndexString() + ", ";
                    // if we are able to parse the address and index we can convert it
                    // to a standard DCC address for display.
                    if (getTOAddressInt() != -1 && getTOAddressIndexInt() != -1) {
                        int boardAddr = getTOAddressInt();
                        int boardIndex = getTOAddressIndexInt();
                        int dccAddress = (((boardAddr - 1) * 4) + boardIndex) + 1;
                        text += "DCC Address:" + dccAddress + ", ";
                    }
                    text += "Dir:" + getTOStateString();
                } else if (isTurnoutDefServoReply()) {
                    text = "Turnout Def SERVO Reply: ";
                    text += "ID:" + getTOIDString() + ", ";
                    text += "Pin:" + getTOPinInt() + ", ";
                    text += "ThrownPos:" + getTOThrownPositionInt() + ", ";
                    text += "ClosedPos:" + getTOClosedPositionInt() + ", ";
                    text += "Profile:" + getTOProfileInt() + ", ";
                    text += "Dir:" + getTOStateString();
                } else if (isTurnoutDefVpinReply()) {
                    text = "Turnout Def VPIN Reply: ";
                    text += "ID:" + getTOIDString() + ", ";
                    text += "Pin:" + getTOPinInt() + ", ";
                    text += "Dir:" + getTOStateString();
                } else if (isTurnoutDefLCNReply()) {
                    text = "Turnout Def LCN Reply: ";
                    text += "ID:" + getTOIDString() + ", ";
                    text += "Dir:" + getTOStateString();
                } else {
                    text = "Unknown Turnout Reply Format: ";
                    text += toString();
                }
                break;
            case DCCppConstants.SENSOR_REPLY_H:
                text = "Sensor Reply (Inactive): ";
                text += "Number: " + getSensorNumString() + ", ";
                text += "State: INACTIVE";
                break;
            case DCCppConstants.SENSOR_REPLY_L:
                // Also covers the V1.0 version SENSOR_REPLY
                if (isSensorDefReply()) {
                    text = "Sensor Def Reply: ";
                    text += "Number: " + getSensorDefNumString() + ", ";
                    text += "Pin: " + getSensorDefPinString() + ", ";
                    text += "Pullup: " + getSensorDefPullupString();
                } else {
                    text = "Sensor Reply (Active): ";
                    text += "Number: " + getSensorNumString() + ", ";
                    text += "State: ACTIVE";
                }
                break;
            case DCCppConstants.OUTPUT_REPLY:
                if (isOutputCmdReply()) {
                    text = "Output Command Reply: ";
                    text += "Number: " + getOutputNumString() + ", ";
                    text += "State: " + getOutputCmdStateString();
                } else if (isOutputDefReply()) {
                    text = "Output Command Reply: ";
                    text += "Number: " + getOutputNumString() + ", ";
                    text += "Pin: " + getOutputListPinString() + ", ";
                    text += "Flags: " + getOutputListIFlagString() + ", ";
                    text += "State: " + getOutputListStateString();
                } else {
                    text = "Invalid Output Reply Format: ";
                    text += toString();
                }
                break;
            case DCCppConstants.PROGRAM_REPLY:
                if (isProgramBitReply()) {
                    text = "Program Bit Reply: ";
                    text += "Callback Num: " + getCallbackNumString() + ", ";
                    text += "Callback Sub: " + getCallbackSubString() + ", ";
                    text += "CV: " + getCVString() + ", ";
                    text += "CV Bit: " + getProgramBitString() + ", ";
                } else {
                    text = "Program Reply: ";
                    text += "Callback Num: " + getCallbackNumString() + ", ";
                    text += "Callback Sub: " + getCallbackSubString() + ", ";
                    text += "CV: " + getCVString() + ", ";
                }
                text += "Value: " + getReadValueString();
                break;
            case DCCppConstants.VERIFY_REPLY:
                text = "Prog Verify Reply: ";
                text += "CV: " + getCVString() + ", ";
                text += "Value: " + getReadValueString();
                break;
            case DCCppConstants.STATUS_REPLY:
                text = "Status:";
                text += "Station: " + getStationType();
                text += ", Build: " + getBuildString();
                text += ", Version: " + getVersion();
                break;
            case DCCppConstants.POWER_REPLY:
                if(isNamedPowerReply()) {
                    text = "Power Status: ";
                    text += "Name:" + getPowerDistrictName();
                    text += " Status:" + getPowerDistrictStatus();
                } else {
                    text = "Power Status: ";
                    text += (getPowerBool() ? "ON" : "OFF");
                }
                break;
            case DCCppConstants.CURRENT_REPLY:
                text = "Current: " + getCurrentString() + " / 1024";
                break;
            case DCCppConstants.METER_REPLY:
                text = String.format("Meter reply: name %s, value %.2f, type %s, unit %s, min %.2f, max %.2f, resolution %.2f, warn %.2f",
                        getMeterName(), getMeterValue(), getMeterType(),
                        getMeterUnit(), getMeterMinValue(), getMeterMaxValue(),
                        getMeterResolution(), getMeterWarnValue());
                break;
            // case DCCppConstants.LISTPACKET_REPLY:
            //     // TODO: Implement this fully
            //     text = "List Packet Reply...\n";
            //     break;
            case DCCppConstants.WRITE_EEPROM_REPLY:
                text = "Write EEPROM Reply... ";
                // TODO: Don't use getProgValueString()
                text += "Turnouts: " + getValueString(1) + ", ";
                text += "Sensors: " + getValueString(2) + ", ";
                text += "Outputs: " + getValueString(3);
                break;
            case DCCppConstants.COMM_TYPE_REPLY:
                text = "Comm Type Reply ";
                text += "Type: " + getCommTypeInt();
                text += " Port: " + getCommTypeValueString();
                break;
            case DCCppConstants.MADC_FAIL_REPLY:
                text = "No Sensor/Turnout/Output Reply ";
                break;
            case DCCppConstants.MADC_SUCCESS_REPLY:
                text = "Sensor/Turnout/Output MADC Success Reply ";
                break;
            case DCCppConstants.MAXNUMSLOTS_REPLY:
                text = "Number of slots reply: " + getValueString(1);
                break;
            case DCCppConstants.DIAG_REPLY:
                text = "DIAG: " + getValueString(1);
                break;
            default:
                text = "Unrecognized reply: '" + toString() + "'";
        }

        return text;
    }

    /**
     * Generate properties list for certain replies
     *
     * @return list of all properties as a string
     **/
    public String getPropertiesAsString(){
        StringBuilder text = new StringBuilder();
        StringBuilder comma = new StringBuilder();
        switch (getOpCodeChar()) {
            case DCCppConstants.TURNOUT_REPLY:
            case DCCppConstants.SENSOR_REPLY:
            case DCCppConstants.OUTPUT_REPLY:
                //write out properties in comment
                getProperties().forEach((key, value) -> {
                    text.append(comma).append(key).append(":").append(value);
                    comma.setLength(0);
                    comma.append(",");
                 });

                break;
            default:
                break;
        }
        return text.toString();
    }

    /**
     * build a propertylist from reply values
     *
     * @return properties hashmap
     **/
    public LinkedHashMap<String, Object> getProperties(){
        LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>();
        switch (getOpCodeChar()) {
            case DCCppConstants.TURNOUT_REPLY:
                if (isTurnoutDefDCCReply()) {
                    properties.put(DCCppConstants.PROP_TYPE, DCCppConstants.TURNOUT_TYPE_DCC);
                    properties.put(DCCppConstants.PROP_ID,      getTOIDInt());
                    properties.put(DCCppConstants.PROP_ADDRESS, getTOAddressInt());
                    properties.put(DCCppConstants.PROP_INDEX,   getTOAddressIndexInt());
                    // if we are able to parse the address and index we can convert it
                    // to a standard DCC address for display.
                    if (getTOAddressInt() != -1 && getTOAddressIndexInt() != -1) {
                        int boardAddr = getTOAddressInt();
                        int boardIndex = getTOAddressIndexInt();
                        int dccAddress = (((boardAddr - 1) * 4) + boardIndex) + 1;
                        properties.put(DCCppConstants.PROP_DCCADDRESS, dccAddress);
                    }
                } else if (isTurnoutDefServoReply()) {
                    properties.put(DCCppConstants.PROP_TYPE, DCCppConstants.TURNOUT_TYPE_SERVO);
                    properties.put(DCCppConstants.PROP_ID,        getTOIDInt());
                    properties.put(DCCppConstants.PROP_PIN,       getTOPinInt());
                    properties.put(DCCppConstants.PROP_THROWNPOS, getTOThrownPositionInt());
                    properties.put(DCCppConstants.PROP_CLOSEDPOS, getTOClosedPositionInt());
                    properties.put(DCCppConstants.PROP_PROFILE,   getTOProfileInt());
                } else if (isTurnoutDefVpinReply()) {
                    properties.put(DCCppConstants.PROP_TYPE, DCCppConstants.TURNOUT_TYPE_VPIN);
                    properties.put(DCCppConstants.PROP_ID,  getTOIDInt());
                    properties.put(DCCppConstants.PROP_PIN, getTOPinInt());
                } else if (isTurnoutDefLCNReply()) {
                    properties.put(DCCppConstants.PROP_TYPE, DCCppConstants.TURNOUT_TYPE_LCN);
                    properties.put(DCCppConstants.PROP_ID,  getTOIDInt());
                }
                break;
            case DCCppConstants.SENSOR_REPLY:
                if (isSensorDefReply()) {
                    properties.put(DCCppConstants.PROP_TYPE, DCCppConstants.SENSOR_TYPE);
                    properties.put(DCCppConstants.PROP_ID,   getSensorDefNumInt());
                    properties.put(DCCppConstants.PROP_PIN,  getSensorDefPinInt());
                    properties.put(DCCppConstants.PROP_PULLUP,getSensorDefPullupBool());
                }
                break;
            case DCCppConstants.OUTPUT_REPLY:
                if (isOutputDefReply()) {
                    properties.put(DCCppConstants.PROP_TYPE, DCCppConstants.OUTPUT_TYPE);
                    properties.put(DCCppConstants.PROP_ID,   getOutputNumInt());
                    properties.put(DCCppConstants.PROP_PIN,  getOutputListPinInt());
                    properties.put(DCCppConstants.PROP_IFLAG,getOutputListIFlagInt());
                }
                break;
            default:
                break;
        }
        return properties;
    }

    public void parseReply(String s) {
        DCCppReply r = DCCppReply.parseDCCppReply(s);
        log.debug("in parseReply() string: {}", s);
        if (r != null) {
            this.myRegex = r.myRegex;
            this.myReply = r.myReply;
            this._nDataChars = r._nDataChars;
            log.trace("copied: this: {}", this);
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
     * @return DCCppReply or empty string if not a valid formatted string
     */
    public static DCCppReply parseDCCppReply(String s) {

        if (log.isTraceEnabled()) {
            log.trace("Parse charAt(0): {}", s.charAt(0));
        }
        DCCppReply r = new DCCppReply(s);
        switch (s.charAt(0)) {
            case DCCppConstants.STATUS_REPLY:
                if (s.matches(DCCppConstants.STATUS_REPLY_BSC_REGEX)) {
                    log.debug("BSC Status Reply: '{}'", r);
                    r.myRegex = DCCppConstants.STATUS_REPLY_BSC_REGEX;
                } else if (s.matches(DCCppConstants.STATUS_REPLY_ESP32_REGEX)) {
                    log.debug("ESP32 Status Reply: '{}'", r);
                    r.myRegex = DCCppConstants.STATUS_REPLY_ESP32_REGEX;
                } else if (s.matches(DCCppConstants.STATUS_REPLY_REGEX)) {
                    log.debug("Original Status Reply: '{}'", r);
                    r.myRegex = DCCppConstants.STATUS_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.STATUS_REPLY_DCCEX_REGEX)) {
                    log.debug("DCC-EX Status Reply: '{}'", r);
                    r.myRegex = DCCppConstants.STATUS_REPLY_DCCEX_REGEX;
                }
                return(r);
            case DCCppConstants.THROTTLE_REPLY:
                if (s.matches(DCCppConstants.THROTTLE_REPLY_REGEX)) {
                   log.debug("Throttle Reply: '{}'", r);
                   r.myRegex = DCCppConstants.THROTTLE_REPLY_REGEX;
                }
                return(r);
            case DCCppConstants.TURNOUT_REPLY:
                // the order of checking the reply here is critical as both the TURNOUT_DEF_REPLY
                // and TURNOUT_REPLY regex strings start with the same strings but have different
                // meanings.
                if (s.matches(DCCppConstants.TURNOUT_DEF_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.TURNOUT_DEF_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.TURNOUT_DEF_DCC_REPLY_REGEX)) {
                    r.myRegex =      DCCppConstants.TURNOUT_DEF_DCC_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.TURNOUT_DEF_SERVO_REPLY_REGEX)) {
                    r.myRegex =      DCCppConstants.TURNOUT_DEF_SERVO_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.TURNOUT_DEF_VPIN_REPLY_REGEX)) {
                    r.myRegex =      DCCppConstants.TURNOUT_DEF_VPIN_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.TURNOUT_DEF_LCN_REPLY_REGEX)) {
                    r.myRegex =      DCCppConstants.TURNOUT_DEF_LCN_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.TURNOUT_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.TURNOUT_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.MADC_FAIL_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.MADC_FAIL_REPLY_REGEX;
                }
                log.debug("Parsed Reply: '{}' length {}", r.toString(), r._nDataChars);
                return(r);
            case DCCppConstants.OUTPUT_REPLY:
                if (s.matches(DCCppConstants.OUTPUT_DEF_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.OUTPUT_DEF_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.OUTPUT_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.OUTPUT_REPLY_REGEX;
                }
                log.debug("Parsed Reply: '{}' length {}", r, r._nDataChars);
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
            case DCCppConstants.VERIFY_REPLY:
                if (s.matches(DCCppConstants.PROGRAM_VERIFY_REGEX)) {
                    log.debug("Matches VerifyReply");
                    r.myRegex = DCCppConstants.PROGRAM_VERIFY_REGEX;
                } else {
                    log.debug("Does not match VerifyReply Regex");
                }
                return(r);
            case DCCppConstants.POWER_REPLY:
                if (s.matches(DCCppConstants.TRACK_POWER_REPLY_NAMED_REGEX)) {
                    r.myRegex = DCCppConstants.TRACK_POWER_REPLY_NAMED_REGEX;
                } else if (s.matches(DCCppConstants.TRACK_POWER_REPLY_REGEX)) {
                        r.myRegex = DCCppConstants.TRACK_POWER_REPLY_REGEX;
                }
                return(r);
            case DCCppConstants.CURRENT_REPLY:
                if (s.matches(DCCppConstants.CURRENT_REPLY_NAMED_REGEX)) {
                    r.myRegex = DCCppConstants.CURRENT_REPLY_NAMED_REGEX;
                } else if (s.matches(DCCppConstants.CURRENT_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.CURRENT_REPLY_REGEX;
                }
                return(r);
            case DCCppConstants.METER_REPLY:
                if (s.matches(DCCppConstants.METER_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.METER_REPLY_REGEX;
                }
                return(r);
            case DCCppConstants.MAXNUMSLOTS_REPLY:
                if (s.matches(DCCppConstants.MAXNUMSLOTS_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.MAXNUMSLOTS_REPLY_REGEX;
                }
                return(r);
            case DCCppConstants.DIAG_REPLY:
                if (s.matches(DCCppConstants.DIAG_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.DIAG_REPLY_REGEX;
                }
                return(r);
            case DCCppConstants.WRITE_EEPROM_REPLY:
                if (s.matches(DCCppConstants.WRITE_EEPROM_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.WRITE_EEPROM_REPLY_REGEX;
                }
                return(r);
            case DCCppConstants.SENSOR_REPLY_H:
                if (s.matches(DCCppConstants.SENSOR_INACTIVE_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.SENSOR_INACTIVE_REPLY_REGEX;
                }
                return(r);
            case DCCppConstants.SENSOR_REPLY_L:
                if (s.matches(DCCppConstants.SENSOR_DEF_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.SENSOR_DEF_REPLY_REGEX;
                } else if (s.matches(DCCppConstants.SENSOR_ACTIVE_REPLY_REGEX)) {
                    r.myRegex = DCCppConstants.SENSOR_ACTIVE_REPLY_REGEX;
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

    public boolean getValueBool(int idx) {
        Matcher m = DCCppReply.match(myReply.toString(), myRegex, "gvb");
        if (m == null) {
            log.error("DCCppReply '{}' not matched by '{}'", this.toString(), myRegex);
            return(false);
        } else if (idx <= m.groupCount()) {
            return(!m.group(idx).equals("0"));
        } else {
            log.error("DCCppReply bool value index too big. idx = {} msg = {}", idx, this.toString());
            return(false);
        }
    }

    public String getValueString(int idx) {
        Matcher m = DCCppReply.match(myReply.toString(), myRegex, "gvs");
        if (m == null) {
            log.error("DCCppReply '{}' not matched by '{}'", this.toString(), myRegex);
            return("");
        } else if (idx <= m.groupCount()) {
            return(m.group(idx));
        } else {
            log.error("DCCppReply string value index too big. idx = {} msg = {}", idx, this.toString());
            return("");
        }
    }

    //is there a match at idx?
    public boolean valueExists(int idx) {
        Matcher m = DCCppReply.match(myReply.toString(), myRegex, "gvs");
        return (m != null) && (idx <= m.groupCount());
    }

    public int getValueInt(int idx) {
        Matcher m = DCCppReply.match(myReply.toString(), myRegex, "gvi");
        if (m == null) {
            log.error("DCCppReply '{}' not matched by '{}'", this.toString(), myRegex);
            return(0);
        } else if (idx <= m.groupCount()) {
            return(Integer.parseInt(m.group(idx)));
        } else {
            log.error("DCCppReply int value index too big. idx = {} msg = {}", idx, this.toString());
            return(0);
        }
    }

    public double getValueDouble(int idx) {
        Matcher m = DCCppReply.match(myReply.toString(), myRegex, "gvd");
        if (m == null) {
            log.error("DCCppReply '{}' not matched by '{}'", this.toString(), myRegex);
            return(0.0);
        } else if (idx <= m.groupCount()) {
            return(Double.parseDouble(m.group(idx)));
        } else {
            log.error("DCCppReply double value index too big. idx = {} msg = {}", idx, this.toString());
            return(0.0);
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
     *     <T ...>Throttle responses from all registers
     *     <iDCC++ ... > Base station version and build date
     *     <H ID ADDR INDEX THROW> All turnout states.
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
                log.trace("No Match {} Command: {} pattern {}",name, s, pat);
                return(null);
            }
            return(m);

        } catch (PatternSyntaxException e) {
            log.error("Malformed DCC++ reply syntax! s = {}", pat);
            return(null);
        } catch (IllegalStateException e) {
            log.error("Group called before match operation executed string = {}", s);
            return(null);
        } catch (IndexOutOfBoundsException e) {
            log.error("Index out of bounds string = {}", s);
            return(null);
        }
    }

    public String getStationType() {
        if (this.isStatusReply()) {
            return(this.getValueString(1)); //1st match in all versions
        } else {
            return("Unknown");
        }
    }

    //build value is 3rd match in v3+, 2nd in previous
    public String getBuildString() {
        if (this.isStatusReply()) {
            if (this.valueExists(3)) {
                return(this.getValueString(3));
            } else {
                return(this.getValueString(2));
            }
        } else {
            return("Unknown");
        }
    }

    //look for canonical version in 2nd match
    public String getVersion() {
        if (this.isStatusReply()) {
            String s = this.getValueString(2);
            if (jmri.Version.isCanonicalVersion(s)) {
                return s;
            } else {
                return("0.0.0");
            }
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

    public String getTOAddressString() {
        if (this.isTurnoutDefReply() || this.isTurnoutDefDCCReply()) {
            return(this.getValueString(2));
        } else {
            return("-1");
        }
    }

    public int getTOAddressInt() {
        if (this.isTurnoutDefReply() || this.isTurnoutDefDCCReply()) {
            return(this.getValueInt(2));
        } else {
            return(-1);
        }
    }

    public String getTOAddressIndexString() {
        if (this.isTurnoutDefReply() || this.isTurnoutDefDCCReply()) {
            return(this.getValueString(3));
        } else {
            return("-1");
        }
    }

    public int getTOAddressIndexInt() {
        if (this.isTurnoutDefReply() || this.isTurnoutDefDCCReply()) {
            return(this.getValueInt(3));
        } else {
            return(-1);
        }
    }

    public int getTOPinInt() {
        if (this.isTurnoutDefServoReply() || this.isTurnoutDefVpinReply()) {
            return(this.getValueInt(2));
        } else {
            return(-1);
        }
    }

    public int getTOThrownPositionInt() {
        if (this.isTurnoutDefServoReply()) {
            return(this.getValueInt(3));
        } else {
            return(-1);
        }
    }

    public int getTOClosedPositionInt() {
        if (this.isTurnoutDefServoReply()) {
            return(this.getValueInt(4));
        } else {
            return(-1);
        }
    }

    public int getTOProfileInt() {
        if (this.isTurnoutDefServoReply()) {
            return(this.getValueInt(5));
        } else {
            return(-1);
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
        if (this.isTurnoutDefReply() || this.isTurnoutDefDCCReply()) { // turnout list response
            return(this.getValueInt(4));
        } else if (this.isTurnoutDefServoReply()) { // servo turnout
            return(this.getValueInt(6));
        } else if (this.isTurnoutDefVpinReply()) { // vpin turnout
            return(this.getValueInt(3));
        } else if (this.isTurnoutDefLCNReply()) { // LCN turnout
            return(this.getValueInt(2));
        } else if (this.isTurnoutReply()) { // single turnout response
            return(this.getValueInt(2));
        } else {
            log.error("TurnoutReply Parser called on non-TurnoutReply message type {}", this.getOpCodeChar());
            return(0);
        }
    }

    public boolean getTOIsThrown() {
        return(this.getTOStateInt() == 1);
    }

    public boolean getTOIsClosed() {
        return(!this.getTOIsThrown());
    }


    //------------------------------------------------------
    // Helper methods for Program Replies

    public String getCallbackNumString() {
        if (this.isProgramReply() || isProgramBitReply() ) {
            return(this.getValueString(1));
        } else {
            log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getOpCodeChar());
            return("0");
        }
    }

    public int getCallbackNumInt() {
        if (this.isProgramReply() || isProgramBitReply() ) {
            return(this.getValueInt(1));
        } else {
            log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getOpCodeChar());
            return(0);
        }
    }

    public String getCallbackSubString() {
        if (this.isProgramReply() || isProgramBitReply() ) {
            return(this.getValueString(2));
        } else {
            log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getOpCodeChar());
            return("0");
        }
    }

    public int getCallbackSubInt() {
        if (this.isProgramReply() || isProgramBitReply() ) {
            return(this.getValueInt(2));
        } else {
            log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getOpCodeChar());
            return(0);
        }
    }

    public String getCVString() {
        if (this.isProgramReply() || isProgramBitReply() ) {
            return(this.getValueString(3));
        } else if (this.isVerifyReply() ) {
            return(this.getValueString(1));
        } else {
            log.error("ProgramReply Parser called on non-ProgramReply message type {}", this.getOpCodeChar());
            return("0");
        }
    }

    public int getCVInt() {
        if (this.isProgramReply() || isProgramBitReply() ) {
            return(this.getValueInt(3));
        } else if (this.isVerifyReply() ) {
            return(this.getValueInt(1));
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
        if (this.isProgramReply() || isProgramBitReply() ) {
            if (this.matches(DCCppConstants.PROGRAM_BIT_REPLY_REGEX)) {
                return(this.getValueString(5));
            } else {
                return(this.getValueString(4));
            }
        } else if (this.isVerifyReply() ) {
            return(this.getValueString(2));
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
            log.error("NamedPowerReply Parser called on non-NamedPowerReply message type '{}' message '{}'", this.getOpCodeChar(), this.toString());
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

    public String getMeterName() {
        if (this.isMeterReply()) {
            return(this.getValueString(1));
        } else {
            log.error("MeterReply Parser called on non-MeterReply message type '{}' message '{}'", this.getOpCodeChar(), this.toString());
            return("");
        }
    }
    public double getMeterValue() {
        if (this.isMeterReply()) {
            return(this.getValueDouble(2));
        } else {
            log.error("MeterReply Parser called on non-MeterReply message type '{}' message '{}'", this.getOpCodeChar(), this.toString());
            return(0.0);
        }
    }
    public String getMeterType() {
        if (this.isMeterReply()) {
            String t = getValueString(3);
            if (t.equals(DCCppConstants.VOLTAGE) || t.equals(DCCppConstants.CURRENT)) {
                return(t);
            } else {
                log.warn("Meter Type '{}' is not valid type in message '{}'", t, this.toString());
                return("");
            }
        } else {
            log.error("MeterReply Parser called on non-MeterReply message type '{}' message '{}'", this.getOpCodeChar(), this.toString());
            return("");
        }
    }
    public jmri.Meter.Unit getMeterUnit() {
        if (this.isMeterReply()) {
            String us = this.getValueString(4);
            AbstractXmlAdapter.EnumIO<jmri.Meter.Unit> map = new AbstractXmlAdapter.EnumIoNames<>(jmri.Meter.Unit.class);
            return(map.inputFromString(us));
        } else {
            log.error("MeterReply Parser called on non-MeterReply message type '{}' message '{}'", this.getOpCodeChar(), this.toString());
            return(jmri.Meter.Unit.NoPrefix);
        }
    }
    public double getMeterMinValue() {
        if (this.isMeterReply()) {
            return(this.getValueDouble(5));
        } else {
            log.error("MeterReply Parser called on non-MeterReply message type '{}' message '{}'", this.getOpCodeChar(), this.toString());
            return(0.0);
        }
    }
    public double getMeterMaxValue() {
        if (this.isMeterReply()) {
            return(this.getValueDouble(6));
        } else {
            log.error("MeterReply Parser called on non-MeterReply message type '{}' message '{}'", this.getOpCodeChar(), this.toString());
            return(0.0);
        }
    }
    public double getMeterResolution() {
        if (this.isMeterReply()) {
            return(this.getValueDouble(7));
        } else {
            log.error("MeterReply Parser called on non-MeterReply message type '{}' message '{}'", this.getOpCodeChar(), this.toString());
            return(0.0);
        }
    }
    public double getMeterWarnValue() {
        if (this.isMeterReply()) {
            return(this.getValueDouble(8));
        } else {
            log.error("MeterReply Parser called on non-MeterReply message type '{}' message '{}'", this.getOpCodeChar(), this.toString());
            return(0.0);
        }
    }
    public boolean isMeterTypeVolt() {
        if (this.isMeterReply()) {
            return(this.getMeterType().equals(DCCppConstants.VOLTAGE));
        } else {
            log.error("MeterReply Parser called on non-MeterReply message type '{}' message '{}'", this.getOpCodeChar(), this.toString());
            return(false);
        }
    }
    public boolean isMeterTypeCurrent() {
        if (this.isMeterReply()) {
            return(this.getMeterType().equals(DCCppConstants.CURRENT));
        } else {
            log.error("MeterReply Parser called on non-MeterReply message type '{}' message '{}'", this.getOpCodeChar(), this.toString());
            return(false);
        }
    }

    public boolean getPowerBool() {
        if (this.isPowerReply()) {
            return(this.getValueString(1).equals(DCCppConstants.POWER_ON));
        } else {
            log.error("PowerReply Parser called on non-PowerReply message type {} message {}", this.getOpCodeChar(), this.toString());
            return(false);
        }
    }

    public String getTurnoutDefNumString() {
        if (this.isTurnoutDefReply() || this.isTurnoutDefDCCReply()) {
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
        if (this.isTurnoutDefReply() || this.isTurnoutDefDCCReply()) {
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
        if (this.isTurnoutDefReply() || this.isTurnoutDefDCCReply()) {
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
        if (this.isOutputDefReply() || this.isOutputCmdReply()) {
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
        if (this.isOutputDefReply()) {
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
        if (this.isOutputDefReply()) {
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
        if (this.isOutputDefReply()) {
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
    public boolean isTurnoutCmdReply() { return (this.matches(DCCppConstants.TURNOUT_REPLY_REGEX)); }
    public boolean isProgramReply() { return (this.matches(DCCppConstants.PROGRAM_REPLY_REGEX)); }
    public boolean isVerifyReply()  { return (this.matches(DCCppConstants.PROGRAM_VERIFY_REGEX)); }
    public boolean isProgramBitReply() { return (this.matches(DCCppConstants.PROGRAM_BIT_REPLY_REGEX)); }
    public boolean isPowerReply() { return (this.getOpCodeChar() == DCCppConstants.POWER_REPLY); }
    public boolean isNamedPowerReply() { return(this.matches(DCCppConstants.TRACK_POWER_REPLY_NAMED_REGEX)); }
    public boolean isMaxNumSlotsReply() { return(this.matches(DCCppConstants.MAXNUMSLOTS_REPLY_REGEX)); }
    public boolean isDiagReply() { return(this.matches(DCCppConstants.DIAG_REPLY_REGEX)); }
    public boolean isCurrentReply() { return (this.getOpCodeChar() == DCCppConstants.CURRENT_REPLY); }
    public boolean isNamedCurrentReply() { return(this.matches(DCCppConstants.CURRENT_REPLY_NAMED_REGEX)); }
    public boolean isMeterReply()   { return (this.matches(DCCppConstants.METER_REPLY_REGEX)); }
    public boolean isSensorReply() { return((this.getOpCodeChar() == DCCppConstants.SENSOR_REPLY) ||
         (this.getOpCodeChar() == DCCppConstants.SENSOR_REPLY_H) ||
         (this.getOpCodeChar() == DCCppConstants.SENSOR_REPLY_L)); }
    public boolean isSensorDefReply() { return(this.matches(DCCppConstants.SENSOR_DEF_REPLY_REGEX)); }
    public boolean isTurnoutDefReply() { return(this.matches(DCCppConstants.TURNOUT_DEF_REPLY_REGEX)); }
    public boolean isTurnoutDefDCCReply()   { return(this.matches(DCCppConstants.TURNOUT_DEF_DCC_REPLY_REGEX)); }
    public boolean isTurnoutDefServoReply() { return(this.matches(DCCppConstants.TURNOUT_DEF_SERVO_REPLY_REGEX)); }
    public boolean isTurnoutDefVpinReply()  { return(this.matches(DCCppConstants.TURNOUT_DEF_VPIN_REPLY_REGEX)); }
    public boolean isTurnoutDefLCNReply()   { return(this.matches(DCCppConstants.TURNOUT_DEF_LCN_REPLY_REGEX)); }
    public boolean isMADCFailReply() { return(this.getOpCodeChar() == DCCppConstants.MADC_FAIL_REPLY); }
    public boolean isMADCSuccessReply() { return(this.getOpCodeChar() == DCCppConstants.MADC_SUCCESS_REPLY); }
    public boolean isStatusReply() { return(this.getOpCodeChar() == DCCppConstants.STATUS_REPLY); }
    public boolean isOutputReply() { return (this.getOpCodeChar() == DCCppConstants.OUTPUT_REPLY); }
    public boolean isOutputDefReply() { return(this.matches(DCCppConstants.OUTPUT_DEF_REPLY_REGEX)); }
    public boolean isOutputCmdReply() { return(this.matches(DCCppConstants.OUTPUT_REPLY_REGEX)); }
    public boolean isCommTypeReply() { return(this.matches(DCCppConstants.COMM_TYPE_REPLY_REGEX)); }
    public boolean isWriteEepromReply() { return(this.matches(DCCppConstants.WRITE_EEPROM_REPLY_REGEX)); }

    public boolean isValidReplyFormat() {
        if ((this.matches(DCCppConstants.THROTTLE_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.TURNOUT_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.PROGRAM_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.PROGRAM_VERIFY_REGEX)) ||
            (this.matches(DCCppConstants.TRACK_POWER_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.TRACK_POWER_REPLY_NAMED_REGEX)) ||
            (this.matches(DCCppConstants.CURRENT_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.CURRENT_REPLY_NAMED_REGEX)) ||
            (this.matches(DCCppConstants.METER_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.SENSOR_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.SENSOR_DEF_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.SENSOR_INACTIVE_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.SENSOR_ACTIVE_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.OUTPUT_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.MADC_FAIL_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.MADC_SUCCESS_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.STATUS_REPLY_REGEX)) ||
            (this.matches(DCCppConstants.STATUS_REPLY_BSC_REGEX)) ||
            (this.matches(DCCppConstants.STATUS_REPLY_ESP32_REGEX)) ||
            (this.matches(DCCppConstants.STATUS_REPLY_DCCEX_REGEX))
        ) {
            return(true);
        } else {
            return(false);
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(DCCppReply.class);

}
