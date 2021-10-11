package jmri.jmrix.dccpp;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Represents a single command or response on the DCC++.
 * <p>
 * Content is represented with ints to avoid the problems with sign-extension
 * that bytes have, and because a Java char is actually a variable number of
 * bytes in Unicode.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2003-2010
 * @author Mark Underwood Copyright (C) 2015
 * @author Costin Grigoras Copyright (C) 2018
 * @author Harald Barth Copyright (C) 2019
 *
 * Based on XNetMessage by Bob Jacobsen and Paul Bender
 */

/*
 * A few words on implementation:
 *
 * DCCppMessage objects are (usually) created by calling one of the static makeMessageType()
 * methods, and are then consumed by the TrafficController/Packetizer by being converted to
 * a String and sent out the port.
 * <p>
 * Internally the DCCppMessage is actually stored as a String, and alongside that is kept
 * a Regex for easy extraction of the values where needed in the code.
 * <p>
 * The various getParameter() type functions are mainly for convenience in places such as the
 * port monitor where we want to be able to extract the /meaning/ of the DCCppMessage and
 * present it in a human readable form.  Using the getParameterType() methods insulates
 * the higher level code from needing to know what order/format the actual message is
 * in.
 */
public class DCCppMessage extends jmri.jmrix.AbstractMRMessage implements Delayed {

    private static int _nRetries = 3;

    /* According to the specification, DCC++ has a maximum timing
     interval of 500 milliseconds during normal communications */
    protected static final int DCCppProgrammingTimeout = 10000;  // TODO: Appropriate value for DCC++?
    private static int DCCppMessageTimeout = 5000;  // TODO: Appropriate value for DCC++?

    //private ArrayList<Integer> valueList = new ArrayList<>();
    private StringBuilder myMessage;
    private String myRegex;
    private char opcode;

    /**
     * Create a new object, representing a specific-length message.
     *
     * @param len Total bytes in message, including opcode and error-detection
     *            byte.
     */
    //NOTE: Not used anywhere useful... consider removing.
    public DCCppMessage(int len) {
        super(len);
        setBinary(false);
        setRetries(_nRetries);
        setTimeout(DCCppMessageTimeout);
        if (len > DCCppConstants.MAX_MESSAGE_SIZE || len < 0) {
            log.error("Invalid length in ctor: {}", len);
        }
        _nDataChars = len;
        myRegex = "";
        myMessage = new StringBuilder(len);
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
        myRegex = message.myRegex;
        myMessage = message.myMessage;
        toStringCache = message.toStringCache;
    }

    /**
     * Create an DCCppMessage from an DCCppReply.
     * Not used.  Really, not even possible.  Consider removing.
     * @param message existing reply to replicate.
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
     * <p>
     * Since DCCppMessages are text, there is no Hex-to-byte conversion.
     * <p>
     * NOTE 15-Feb-17: un-Deprecating this function so that it can be used in
     * the DCCppOverTCP server/client interface. 
     * Messages shouldn't be parsed, they are already in DCC++ format,
     * so we need the string constructor to generate a DCCppMessage from 
     * the incoming byte stream.
     * @param s message in string form.
     */
    public DCCppMessage(String s) {
        setBinary(false);
        setRetries(_nRetries);
        setTimeout(DCCppMessageTimeout);
        myMessage = new StringBuilder(s); // yes, copy... or... maybe not.
        toStringCache = s;
        // gather bytes in result
        setRegex();
        _nDataChars = myMessage.length();
        _dataChars = new int[_nDataChars];
    }

    // Partial constructor used in the static getMessageType() calls below.
    protected DCCppMessage(char c) {
        setBinary(false);
        setRetries(_nRetries);
        setTimeout(DCCppMessageTimeout);
        opcode = c;
        myMessage = new StringBuilder(Character.toString(c));
        _nDataChars = myMessage.length();
    }

    protected DCCppMessage(char c, String regex) {
        setBinary(false);
        setRetries(_nRetries);
        setTimeout(DCCppMessageTimeout);
        opcode = c;
        myRegex = regex;
        myMessage = new StringBuilder(Character.toString(c));
        _nDataChars = myMessage.length();
    }

    private void setRegex() {
        switch (myMessage.charAt(0)) {
            case DCCppConstants.THROTTLE_CMD:
                myRegex = DCCppConstants.THROTTLE_CMD_REGEX;
                break;
            case DCCppConstants.FUNCTION_CMD:
                myRegex = DCCppConstants.FUNCTION_CMD_REGEX;
                break;
            case DCCppConstants.FUNCTION_V2_CMD:
                myRegex = DCCppConstants.FUNCTION_V2_CMD_REGEX;
                break;
            case DCCppConstants.FORGET_CAB_CMD:
                myRegex = DCCppConstants.FORGET_CAB_CMD_REGEX;
                break;
            case DCCppConstants.ACCESSORY_CMD:
                myRegex = DCCppConstants.ACCESSORY_CMD_REGEX;
                break;
            case DCCppConstants.TURNOUT_CMD:
                if ((match(toString(), DCCppConstants.TURNOUT_ADD_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.TURNOUT_ADD_REGEX;
                } else if ((match(toString(), DCCppConstants.TURNOUT_ADD_DCC_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.TURNOUT_ADD_DCC_REGEX;
                } else if ((match(toString(), DCCppConstants.TURNOUT_ADD_SERVO_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.TURNOUT_ADD_SERVO_REGEX;
                } else if ((match(toString(), DCCppConstants.TURNOUT_ADD_VPIN_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.TURNOUT_ADD_VPIN_REGEX;
                } else if ((match(toString(), DCCppConstants.TURNOUT_DELETE_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.TURNOUT_DELETE_REGEX;
                } else if ((match(toString(), DCCppConstants.TURNOUT_LIST_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.TURNOUT_LIST_REGEX;
                } else if ((match(toString(), DCCppConstants.TURNOUT_CMD_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.TURNOUT_CMD_REGEX;
                } else {
                    myRegex = "";
                }
                break;
            case DCCppConstants.SENSOR_CMD:
                if ((match(toString(), DCCppConstants.SENSOR_ADD_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.SENSOR_ADD_REGEX;
                } else if ((match(toString(), DCCppConstants.SENSOR_DELETE_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.SENSOR_DELETE_REGEX;
                } else if ((match(toString(), DCCppConstants.SENSOR_LIST_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.SENSOR_LIST_REGEX;
                } else {
                    myRegex = "";
                }
                break;
            case DCCppConstants.OUTPUT_CMD:
                if ((match(toString(), DCCppConstants.OUTPUT_ADD_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.OUTPUT_ADD_REGEX;
                } else if ((match(toString(), DCCppConstants.OUTPUT_DELETE_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.OUTPUT_DELETE_REGEX;
                } else if ((match(toString(), DCCppConstants.OUTPUT_LIST_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.OUTPUT_LIST_REGEX;
                } else if ((match(toString(), DCCppConstants.OUTPUT_CMD_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.OUTPUT_CMD_REGEX;
                } else {
                    myRegex = "";
                }
                break;
            case DCCppConstants.OPS_WRITE_CV_BYTE:
                myRegex = DCCppConstants.OPS_WRITE_BYTE_REGEX;
                break;
            case DCCppConstants.OPS_WRITE_CV_BIT:
                myRegex = DCCppConstants.OPS_WRITE_BIT_REGEX;
                break;
            case DCCppConstants.PROG_WRITE_CV_BYTE:
                myRegex = DCCppConstants.PROG_WRITE_BYTE_REGEX;
                break;
            case DCCppConstants.PROG_WRITE_CV_BIT:
                myRegex = DCCppConstants.PROG_WRITE_BIT_REGEX;
                break;
            case DCCppConstants.PROG_READ_CV:
                myRegex = DCCppConstants.PROG_READ_REGEX;
                break;
            case DCCppConstants.PROG_VERIFY_CV:
                myRegex = DCCppConstants.PROG_VERIFY_REGEX;
                break;
            case DCCppConstants.TRACK_POWER_ON:
            case DCCppConstants.TRACK_POWER_OFF:
                myRegex = DCCppConstants.TRACK_POWER_REGEX;
                break;
            case DCCppConstants.READ_TRACK_CURRENT:
                myRegex = DCCppConstants.READ_TRACK_CURRENT_REGEX;
                break;
            case DCCppConstants.READ_CS_STATUS:
                myRegex = DCCppConstants.READ_CS_STATUS_REGEX;
                break;
            case DCCppConstants.READ_MAXNUMSLOTS:
                myRegex = DCCppConstants.READ_MAXNUMSLOTS_REGEX;
                break;
            case DCCppConstants.WRITE_TO_EEPROM_CMD:
                myRegex = DCCppConstants.WRITE_TO_EEPROM_REGEX;
                break;
            case DCCppConstants.CLEAR_EEPROM_CMD:
                myRegex = DCCppConstants.CLEAR_EEPROM_REGEX;
                break;
            case DCCppConstants.QUERY_SENSOR_STATES_CMD:
                myRegex = DCCppConstants.QUERY_SENSOR_STATES_REGEX;
                break;
            case DCCppConstants.WRITE_DCC_PACKET_MAIN:
                myRegex = DCCppConstants.WRITE_DCC_PACKET_MAIN_REGEX;
                break;
            case DCCppConstants.WRITE_DCC_PACKET_PROG:
                myRegex = DCCppConstants.WRITE_DCC_PACKET_PROG_REGEX;
                break;
            case DCCppConstants.LIST_REGISTER_CONTENTS:
                myRegex = DCCppConstants.LIST_REGISTER_CONTENTS_REGEX;
                break;
            case DCCppConstants.DIAG_CMD:
                myRegex = DCCppConstants.DIAG_CMD_REGEX;                
                break;
            default:
                myRegex = "";
        }
    }

    private String toStringCache = null;

    /**
     * Converts DCCppMessage to String format (without the {@code <>} brackets)
     *
     * @return String form of message.
     */
    @Override
    public String toString() {
        if (toStringCache == null) {
            toStringCache = myMessage.toString();
        }

        return toStringCache;
        /*
        String s = Character.toString(opcode);
        for (int i = 0; i < valueList.size(); i++) {
            s += " ";
            s += valueList.get(i).toString();
        }
        return(s);
         */
    }

    /**
     * Generate text translations of messages for use in the DCCpp monitor.
     *
     * @return representation of the DCCpp as a string.
     */
    @Override
    public String toMonitorString() {
        // Beautify and display
        String text;

        switch (getOpCodeChar()) {
            case DCCppConstants.THROTTLE_CMD:
                text = "Throttle Cmd: ";
                text += "Register: " + getRegisterString();
                text += ", Address: " + getAddressString();
                text += ", Speed: " + getSpeedString();
                text += ", Direction: " + getDirectionString();
                break;
            case DCCppConstants.FUNCTION_CMD:
                text = "Function Cmd: ";
                text += "Address: " + getFuncAddressString();
                text += ", Byte 1: " + getFuncByte1String();
                text += ", Byte 2: " + getFuncByte2String();
                text += ", (No Reply Expected)";
                break;
            case DCCppConstants.FUNCTION_V2_CMD:
                text = "Function Cmd: ";
                if (isFunctionV2Message()) {
                    text += "CAB: " + getFuncV2CabString();
                    text += ", FUNC: " + getFuncV2FuncString();
                    text += ", State: " + getFuncV2StateString();
                    text += ", (No Reply Expected)";
                } else {
                    text += "Invalid syntax: '" + toString() + "'";                                        
                }
                break;
            case DCCppConstants.FORGET_CAB_CMD:
                text = "Forget Cab: ";
                if (isForgetCabMessage()) {
                    text += "CAB: " + (getForgetCabString().equals("")?"[ALL]":getForgetCabString());
                    text += ", (No Reply Expected)";
                } else {
                    text += "Invalid syntax: '" + toString() + "'";                    
                }
                break;
            case DCCppConstants.ACCESSORY_CMD:
                text = "Accessory Decoder Cmd: ";
                text += "Address: " + getAccessoryAddrString();
                text += ", Subaddr: " + getAccessorySubString();
                text += ", State: " + getAccessoryStateString();
                break;
            case DCCppConstants.TURNOUT_CMD:
                if (isTurnoutAddMessage()) {
                    text = "Add Turnout: ";
                    text += "ID: " + getTOIDString();
                    text += ", Address: " + getTOAddressString();
                    text += ", Subaddr: " + getTOSubAddressString();
                } else if (isTurnoutAddDCCMessage()) {
                    text = "Add Turnout DCC: ";
                    text += "ID:" + getTOIDString();
                    text += ", Address:" + getTOAddressString();
                    text += ", Subaddr:" + getTOSubAddressString();
                } else if (isTurnoutAddServoMessage()) {
                    text = "Add Turnout Servo: ";
                    text += "ID:" + getTOIDString();
                    text += ", Pin:" + getTOPinInt();
                    text += ", ThrownPos:" + getTOThrownPositionInt();
                    text += ", ClosedPos:" + getTOClosedPositionInt();
                    text += ", Profile:" + getTOProfileInt();
                } else if (isTurnoutAddVpinMessage()) {
                    text = "Add Turnout Vpin: ";
                    text += "ID:" + getTOIDString();
                    text += ", Pin:" + getTOPinInt();
                } else if (isTurnoutDeleteMessage()) {
                    text = "Delete Turnout: ";
                    text += "ID: " + getTOIDString();
                } else if (isListTurnoutsMessage()) {
                    text = "List Turnouts...";
                } else if (isTurnoutCmdMessage()) {
                    text = "Turnout Cmd: ";
                    text += "ID: " + getTOIDString();
                    text += ", State: " + getTOStateString();
                } else {
                    text = "Unmatched Turnout Cmd: " + toString();                    
                }
                break;
            case DCCppConstants.OUTPUT_CMD:
                if (isOutputCmdMessage()) {
                    text = "Output Cmd: ";
                    text += "ID: " + getOutputIDString();
                    text += ", State: " + getOutputStateString();
                } else if (isOutputAddMessage()) {
                    text = "Add Output: ";
                    text += "ID: " + getOutputIDString();
                    text += ", Pin: " + getOutputPinString();
                    text += ", IFlag: " + getOutputIFlagString();
                } else if (isOutputDeleteMessage()) {
                    text = "Delete Output: ";
                    text += "ID: " + getOutputIDString();
                } else if (isListOutputsMessage()) {
                    text = "List Outputs...";
                } else {
                    text = "Invalid Output Command: " + toString();
                }
                break;
            case DCCppConstants.SENSOR_CMD:
                if (isSensorAddMessage()) {
                    text = "Add Sensor: ";
                    text += "ID: " + getSensorIDString();
                    text += ", Pin: " + getSensorPinString();
                    text += ", Pullup: " + getSensorPullupString();
                } else if (isSensorDeleteMessage()) {
                    text = "Delete Sensor: ";
                    text += "ID: " + getSensorIDString();
                } else if (isListSensorsMessage()) {
                    text = "List Sensors...";
                } else {
                    text = "Unknown Sensor Cmd...";
                }
                break;
            case DCCppConstants.OPS_WRITE_CV_BYTE:
                text = "Ops Write Byte Cmd: "; // <w cab cv val>
                text += "Address: " + getOpsWriteAddrString() + ", ";
                text += "CV: " + getOpsWriteCVString() + ", ";
                text += "Value: " + getOpsWriteValueString();
                break;
            case DCCppConstants.OPS_WRITE_CV_BIT: // <b cab cv bit val>
                text = "Ops Write Bit Cmd: ";
                text += "Address: " + getOpsWriteAddrString() + ", ";
                text += "CV: " + getOpsWriteCVString() + ", ";
                text += "Bit: " + getOpsWriteBitString() + ", ";
                text += "Value: " + getOpsWriteValueString();
                break;
            case DCCppConstants.PROG_WRITE_CV_BYTE:
                text = "Prog Write Byte Cmd: ";
                text += "CV : " + getCVString();
                text += ", Value: " + getProgValueString();
                text += ", Callback Num: " + getCallbackNumString();
                text += ", Callback Sub: " + getCallbackSubString();
                break;

            case DCCppConstants.PROG_WRITE_CV_BIT:
                text = "Prog Write Bit Cmd: ";
                text += "CV : " + getCVString();
                text += ", Bit : " + getBitString();
                text += ", Value: " + getProgValueString();
                text += ", Callback Num: " + getCallbackNumString();
                text += ", Callback Sub: " + getCallbackSubString();
                break;
            case DCCppConstants.PROG_READ_CV:
                text = "Prog Read Cmd: ";
                text += "CV: " + getCVString();
                text += ", Callback Num: " + getCallbackNumString();
                text += ", Callback Sub: " + getCallbackSubString();
                break;
            case DCCppConstants.PROG_VERIFY_CV:
                text = "Prog Verify Cmd:  ";
                text += "CV: " + getCVString();
                text += ", startVal: " + getProgValueString();
                break;
            case DCCppConstants.TRACK_POWER_ON:
                text = "Track Power ON Cmd ";
                break;
            case DCCppConstants.TRACK_POWER_OFF:
                text = "Track Power OFF Cmd ";
                break;
            case DCCppConstants.READ_TRACK_CURRENT:
                text = "Read Track Current Cmd ";
                break;
            case DCCppConstants.READ_CS_STATUS:
                text = "Status Cmd ";
                break;
            case DCCppConstants.READ_MAXNUMSLOTS:
                text = "Get MaxNumSlots Cmd ";
                break;
            case DCCppConstants.WRITE_DCC_PACKET_MAIN:
                text = "Write DCC Packet Main Cmd: ";
                text += "Register: " + getRegisterString();
                text += ", Packet:" + getPacketString();
                break;
            case DCCppConstants.WRITE_DCC_PACKET_PROG:
                text = "Write DCC Packet Prog Cmd: ";
                text += "Register: " + getRegisterString();
                text += ", Packet:" + getPacketString();
                break;
            case DCCppConstants.LIST_REGISTER_CONTENTS:
                text = "List Register Contents Cmd: ";
                text += toString();
                break;
            case DCCppConstants.WRITE_TO_EEPROM_CMD:
                text = "Write to EEPROM Cmd: ";
                text += toString();
                break;
            case DCCppConstants.CLEAR_EEPROM_CMD:
                text = "Clear EEPROM Cmd: ";
                text += toString();
                break;
            case DCCppConstants.QUERY_SENSOR_STATES_CMD:
                text = "Query Sensor States Cmd: '" + toString() + "'";
                break;               
            case DCCppConstants.DIAG_CMD:
                text = "Diag Cmd: '" + toString() + "'";
                break;               
            case DCCppConstants.ESTOP_ALL_CMD:
                text = "eStop All Locos Cmd: '" + toString() + "'";
                break;               
            default:
                text = "Unknown Message: '" + toString() + "'";
        }

        return text;
    }

    @Override
    public int getNumDataElements() {
        return (myMessage.length());
        // return(_nDataChars);
    }

    @Override
    public int getElement(int n) {
        return (this.myMessage.charAt(n));
    }

    @Override
    public void setElement(int n, int v) {
        // We want the ASCII value, not the string interpretation of the int
        char c = (char) (v & 0xFF);
        if (n >= myMessage.length()) {
            myMessage.append(c);
        } else if (n > 0) {
            myMessage.setCharAt(n, c);
        }
        toStringCache = null;
    }
    // For DCC++, the opcode is the first character in the
    // command (after the < ).

    // note that the opcode is part of the message, so we treat it
    // directly
    // WARNING: use this only with opcodes that have a variable number
    // of arguments following included. Otherwise, just use setElement
    @Override
    public void setOpCode(int i) {
        if (i > 0xFF || i < 0) {
            log.error("Opcode invalid: {}", i);
        }
        opcode = (char) (i & 0xFF);
        myMessage.setCharAt(0, opcode);
        toStringCache = null;
    }

    @Override
    public int getOpCode() {
        return (opcode & 0xFF);
    }

    public char getOpCodeChar() {
        //return(opcode);
        return (myMessage.charAt(0));
    }

    @Deprecated
    public String getOpCodeString() {
        return (Character.toString(opcode));
    }

    private int getGroupCount() {
        Matcher m = match(toString(), myRegex, "gvs");
        assert m != null;
        return m.groupCount();
    }

    public String getValueString(int idx) {
        Matcher m = match(toString(), myRegex, "gvs");
        if (m == null) {
            log.error("DCCppMessage '{}' not matched by '{}'", this.toString(), myRegex);
            return ("");
        } else if (idx <= m.groupCount()) {
            return (m.group(idx));
        } else {
            log.error("DCCppMessage value index too big. idx = {} msg = {}", idx, this);
            return ("");
        }
    }

    public int getValueInt(int idx) {
        Matcher m = match(toString(), myRegex, "gvi");
        if (m == null) {
            log.error("DCCppMessage '{}' not matched by '{}'", this.toString(), myRegex);
            return (0);
        } else if (idx <= m.groupCount()) {
            return (Integer.parseInt(m.group(idx)));
        } else {
            log.error("DCCppMessage value index too big. idx = {} msg = {}", idx, this);
            return (0);
        }
    }

    public boolean getValueBool(int idx) {
        log.debug("msg = {}, regex = {}", this, myRegex);
        Matcher m = match(toString(), myRegex, "gvb");

        if (m == null) {
            log.error("DCCppMessage '{}' not matched by '{}'", this.toString(), myRegex);
            return (false);
        } else if (idx <= m.groupCount()) {
            return (!m.group(idx).equals("0"));
        } else {
            log.error("DCCppMessage value index too big. idx = {} msg = {}", idx, this);
            return (false);
        }
    }

    /**
     * @return the message length
     */
    public int length() {
        return (myMessage.length());
    }

    /**
     * Change the default number of retries for an DCC++ message.
     *
     * @param t number of retries to attempt
     */
    public static void setDCCppMessageRetries(int t) {
        _nRetries = t;
    }

    /**
     * Change the default timeout for a DCC++ message.
     *
     * @param t Timeout in milliseconds
     */
    public static void setDCCppMessageTimeout(int t) {
        DCCppMessageTimeout = t;
    }

    //------------------------------------------------------
    // Message Helper Functions
    // Core methods
    /**
     * Returns true if this DCCppMessage is properly formatted (or will generate
     * a properly formatted command when converted to String).
     *
     * @return boolean true/false
     */
    public boolean isValidMessageFormat() {
        return this.match(this.myRegex) != null;
    }

    /**
     * Matches this DCCppMessage against the given regex 'pat'
     *
     * @param pat Regex
     * @return Matcher or null if no match.
     */
    private Matcher match(String pat) {
        return (match(this.toString(), pat, "Validator"));
    }

    /**
     * matches the given string against the given Regex pattern.
     *
     * @param s    string to be matched
     * @param pat  Regex string to match against
     * @param name Text name to use in debug messages.
     * @return Matcher or null if no match
     */
    @CheckForNull
    private static Matcher match(String s, String pat, String name) {
        try {
            Pattern p = Pattern.compile(pat);
            Matcher m = p.matcher(s);
            if (!m.matches()) {
                log.trace("No Match {} Command: '{}' Pattern: '{}'", name, s, pat);
                return null;
            }
            return m;

        } catch (PatternSyntaxException e) {
            log.error("Malformed DCC++ message syntax! s = {}", pat);
            return (null);
        } catch (IllegalStateException e) {
            log.error("Group called before match operation executed string= {}", s);
            return (null);
        } catch (IndexOutOfBoundsException e) {
            log.error("Index out of bounds string= {}", s);
            return (null);
        }
    }

    // Identity Methods
    public boolean isThrottleMessage() {
        return (this.getOpCodeChar() == DCCppConstants.THROTTLE_CMD);
    }

    public boolean isAccessoryMessage() {
        return (this.getOpCodeChar() == DCCppConstants.ACCESSORY_CMD);
    }

    public boolean isFunctionMessage() {
        return (this.getOpCodeChar() == DCCppConstants.FUNCTION_CMD);
    }

    public boolean isFunctionV2Message() {
        return (this.match(DCCppConstants.FUNCTION_V2_CMD_REGEX) != null);
    }

    public boolean isForgetCabMessage() {
        return (this.match(DCCppConstants.FORGET_CAB_CMD_REGEX) != null);
    }

    public boolean isTurnoutMessage() {
        return (this.getOpCodeChar() == DCCppConstants.TURNOUT_CMD);
    }

    public boolean isSensorMessage() {
        return (this.getOpCodeChar() == DCCppConstants.SENSOR_CMD);
    }

    public boolean isEEPROMWriteMessage() {
        return (this.getOpCodeChar() == DCCppConstants.WRITE_TO_EEPROM_CMD);
    }

    public boolean isEEPROMClearMessage() {
        return (this.getOpCodeChar() == DCCppConstants.CLEAR_EEPROM_CMD);
    }

    public boolean isOpsWriteByteMessage() {
        return (this.getOpCodeChar() == DCCppConstants.OPS_WRITE_CV_BYTE);
    }

    public boolean isOpsWriteBitMessage() {
        return (this.getOpCodeChar() == DCCppConstants.OPS_WRITE_CV_BIT);
    }

    public boolean isProgWriteByteMessage() {
        return (this.getOpCodeChar() == DCCppConstants.PROG_WRITE_CV_BYTE);
    }

    public boolean isProgWriteBitMessage() {
        return (this.getOpCodeChar() == DCCppConstants.PROG_WRITE_CV_BIT);
    }

    public boolean isProgReadMessage() {
        return (this.getOpCodeChar() == DCCppConstants.PROG_READ_CV);
    }

    public boolean isProgVerifyMessage() {
        return (this.getOpCodeChar() == DCCppConstants.PROG_VERIFY_CV);
    }

    public boolean isTurnoutCmdMessage() {
        return (this.match(DCCppConstants.TURNOUT_CMD_REGEX) != null);
    }

    public boolean isTurnoutAddMessage() {
        return (this.match(DCCppConstants.TURNOUT_ADD_REGEX) != null);
    }

    public boolean isTurnoutAddDCCMessage() {
        return (this.match(DCCppConstants.TURNOUT_ADD_DCC_REGEX) != null);
    }

    public boolean isTurnoutAddServoMessage() {
        return (this.match(DCCppConstants.TURNOUT_ADD_SERVO_REGEX) != null);
    }

    public boolean isTurnoutAddVpinMessage() {
        return (this.match(DCCppConstants.TURNOUT_ADD_VPIN_REGEX) != null);
    }

    public boolean isTurnoutDeleteMessage() {
        return (this.match(DCCppConstants.TURNOUT_DELETE_REGEX) != null);
    }

    public boolean isListTurnoutsMessage() {
        return (this.match(DCCppConstants.TURNOUT_LIST_REGEX) != null);
    }

    public boolean isSensorAddMessage() {
        return (this.match(DCCppConstants.SENSOR_ADD_REGEX) != null);
    }

    public boolean isSensorDeleteMessage() {
        return (this.match(DCCppConstants.SENSOR_DELETE_REGEX) != null);
    }

    public boolean isListSensorsMessage() {
        return (this.match(DCCppConstants.SENSOR_LIST_REGEX) != null);
    }

    //public boolean isOutputCmdMessage() { return(this.getOpCodeChar() == DCCppConstants.OUTPUT_CMD); }
    public boolean isOutputCmdMessage() {
        return (this.match(DCCppConstants.OUTPUT_CMD_REGEX) != null);
    }

    public boolean isOutputAddMessage() {
        return (this.match(DCCppConstants.OUTPUT_ADD_REGEX) != null);
    }

    public boolean isOutputDeleteMessage() {
        return (this.match(DCCppConstants.OUTPUT_DELETE_REGEX) != null);
    }

    public boolean isListOutputsMessage() {
        return (this.match(DCCppConstants.OUTPUT_LIST_REGEX) != null);
    }

    public boolean isQuerySensorStatesMessage() {
        return (this.match(DCCppConstants.QUERY_SENSOR_STATES_REGEX) != null);
    }

    public boolean isWriteDccPacketMessage() {
        return ((this.getOpCodeChar() == DCCppConstants.WRITE_DCC_PACKET_MAIN) || (this.getOpCodeChar() == DCCppConstants.WRITE_DCC_PACKET_PROG));
    }

    //------------------------------------------------------
    // Helper methods for Sensor Query Commands
    public String getOutputIDString() {
        if (this.isOutputAddMessage() || this.isOutputDeleteMessage() || this.isOutputCmdMessage()) {
            return getValueString(1);
        } else {
            log.error("Output Parser called on non-Output message type {}", this.getOpCodeChar());
            return ("0");
        }
    }

    public int getOutputIDInt() {
        if (this.isOutputAddMessage() || this.isOutputDeleteMessage() || this.isOutputCmdMessage()) {
            return (getValueInt(1)); // assumes stored as an int!
        } else {
            log.error("Output Parser called on non-Output message type {}", this.getOpCodeChar());
            return (0);
        }
    }

    public String getOutputPinString() {
        if (this.isOutputAddMessage()) {
            return (getValueString(2));
        } else {
            log.error("Output Parser called on non-Output message type {}", this.getOpCodeChar());
            return ("0");
        }
    }

    public int getOutputPinInt() {
        if (this.isOutputAddMessage()) {
            return (getValueInt(2));
        } else {
            log.error("Output Parser called on non-Output message type {}", this.getOpCodeChar());
            return (0);
        }
    }

    public String getOutputIFlagString() {
        if (this.isOutputAddMessage()) {
            return (getValueString(3));
        } else {
            log.error("Output Parser called on non-Output message type {}", this.getOpCodeChar());
            return ("0");
        }
    }

    public int getOutputIFlagInt() {
        if (this.isOutputAddMessage()) {
            return (getValueInt(3));
        } else {
            log.error("Output Parser called on non-Output message type {}", this.getOpCodeChar());
            return (0);
        }
    }

    public String getOutputStateString() {
        if (isOutputCmdMessage()) {
            return (this.getOutputStateInt() == 1 ? "HIGH" : "LOW");
        } else {
            return ("Not a Turnout");
        }
    }

    public int getOutputStateInt() {
        if (isOutputCmdMessage()) {
            return (getValueInt(2));
        } else {
            log.error("Output Parser called on non-Output message type {}", this.getOpCodeChar());
            return (0);
        }
    }

    public boolean getOutputStateBool() {
        if (this.isOutputCmdMessage()) {
            return (getValueInt(2) != 0);
        } else {
            log.error("Output Parser called on non-Output message type {} message {}", this.getOpCodeChar(), this);
            return (false);
        }
    }

    public String getSensorIDString() {
        if (this.isSensorAddMessage()) {
            return getValueString(1);
        } else {
            log.error("Sensor Parser called on non-Sensor message type {}", this.getOpCodeChar());
            return ("0");
        }
    }

    public int getSensorIDInt() {
        if (this.isSensorAddMessage()) {
            return (getValueInt(1)); // assumes stored as an int!
        } else {
            log.error("Sensor Parser called on non-Sensor message type {}", this.getOpCodeChar());
            return (0);
        }
    }

    public String getSensorPinString() {
        if (this.isSensorAddMessage()) {
            return (getValueString(2));
        } else {
            log.error("Sensor Parser called on non-Sensor message type {}", this.getOpCodeChar());
            return ("0");
        }
    }

    public int getSensorPinInt() {
        if (this.isSensorAddMessage()) {
            return (getValueInt(2));
        } else {
            log.error("Sensor Parser called on non-Sensor message type {}", this.getOpCodeChar());
            return (0);
        }
    }

    public String getSensorPullupString() {
        if (isSensorAddMessage()) {
            return (getValueBool(3) ? "PULLUP" : "NO PULLUP");
        } else {
            return ("Not a Sensor");
        }
    }

    public int getSensorPullupInt() {
        if (this.isSensorAddMessage()) {
            return (getValueInt(3));
        } else {
            log.error("Sensor Parser called on non-Sensor message type {} message {}", this.getOpCodeChar(), this);
            return (0);
        }
    }

    public boolean getSensorPullupBool() {
        if (this.isSensorAddMessage()) {
            return (getValueBool(3));
        } else {
            log.error("Sensor Parser called on non-Sensor message type {} message {}", this.getOpCodeChar(), this);
            return (false);
        }
    }

    // Helper methods for Accessory Decoder Commands
    public String getAccessoryAddrString() {
        if (this.isAccessoryMessage()) {
            return (getValueString(1));
        } else {
            log.error("Accessory Parser called on non-Accessory message type {}", this.getOpCodeChar());
            return ("0");
        }
    }

    public int getAccessoryAddrInt() {
        if (this.isAccessoryMessage()) {
            return (getValueInt(1));
        } else {
            log.error("Accessory Parser called on non-Accessory message type {}", this.getOpCodeChar());
            return (0);
        }
        //return(Integer.parseInt(this.getAccessoryAddrString()));
    }

    public String getAccessorySubString() {
        if (this.isAccessoryMessage()) {
            return (getValueString(2));
        } else {
            log.error("Accessory Parser called on non-Accessory message type {} message {}", this.getOpCodeChar(), this);
            return ("0");
        }
    }

    public int getAccessorySubInt() {
        if (this.isAccessoryMessage()) {
            return (getValueInt(2));
        } else {
            log.error("Accessory Parser called on non-Accessory message type {} message {}", this.getOpCodeChar(), this);
            return (0);
        }
    }

    public String getAccessoryStateString() {
        if (isAccessoryMessage()) {
            return (this.getAccessoryStateInt() == 1 ? "ON" : "OFF");
        } else {
            return ("Not an Accessory Decoder");
        }
    }

    public int getAccessoryStateInt() {
        if (this.isAccessoryMessage()) {
            return (getValueInt(3));
        } else {
            log.error("Accessory Parser called on non-Accessory message type {} message {}", this.getOpCodeChar(), this);
            return (0);
        }
    }

    //------------------------------------------------------
    // Helper methods for Throttle Commands
    public String getRegisterString() {
        if (this.isThrottleMessage() || this.isWriteDccPacketMessage()) {
            return (getValueString(1));
        } else {
            log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
            return ("0");
        }
    }

    public int getRegisterInt() {
        if (this.isThrottleMessage()) {
            return (getValueInt(1));
        } else {
            log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
            return (0);
        }
    }

    public String getAddressString() {
        if (this.isThrottleMessage()) {
            return (getValueString(2));
        } else {
            log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
            return ("0");
        }
    }

    public int getAddressInt() {
        if (this.isThrottleMessage()) {
            return (getValueInt(2));
        } else {
            log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
            return (0);
        }
    }

    public String getSpeedString() {
        if (this.isThrottleMessage()) {
            return (getValueString(3));
        } else {
            log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
            return ("0");
        }
    }

    public int getSpeedInt() {
        if (this.isThrottleMessage()) {
            return (getValueInt(3));
        } else {
            log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
            return (0);
        }
    }

    public String getDirectionString() {
        if (this.isThrottleMessage()) {
            return (this.getDirectionInt() == 1 ? "Forward" : "Reverse");
        } else {
            log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
            return ("Not a Throttle");
        }
    }

    public int getDirectionInt() {
        if (this.isThrottleMessage()) {
            return (getValueInt(4));
        } else {
            log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
            return (0);
        }
    }

    //------------------------------------------------------
    // Helper methods for Function Commands
    public String getFuncAddressString() {
        if (this.isFunctionMessage()) {
            return (getValueString(1));
        } else {
            log.error("Function Parser called on non-Function message type {}", this.getOpCodeChar());
            return ("0");
        }
    }

    public int getFuncAddressInt() {
        if (this.isFunctionMessage()) {
            return (getValueInt(1));
        } else {
            log.error("Function Parser called on non-Function message type {}", this.getOpCodeChar());
            return (0);
        }
    }

    public String getFuncByte1String() {
        if (this.isFunctionMessage()) {
            return (getValueString(2));
        } else {
            log.error("Function Parser called on non-Function message type {}", this.getOpCodeChar());
            return ("0");
        }
    }

    public int getFuncByte1Int() {
        if (this.isFunctionMessage()) {
            return (getValueInt(2));
        } else {
            log.error("Function Parser called on non-Function message type {}", this.getOpCodeChar());
            return (0);
        }
    }

    public String getFuncByte2String() {
        if (this.isFunctionMessage()) {
            return (getValueString(3));
        } else {
            log.error("Function Parser called on non-Function message type {}", this.getOpCodeChar());
            return ("0");
        }
    }

    public int getFuncByte2Int() {
        if (this.isFunctionMessage()) {
            return (getValueInt(3));
        } else {
            log.error("Function Parser called on non-Function message type {}", this.getOpCodeChar());
            return (0);
        }
    }

    public String getFuncV2CabString() {
        if (this.isFunctionV2Message()) {
            return (getValueString(1));
        } else {
            log.error("Function Parser called on non-Function V2 message type {}", this.getOpCodeChar());
            return ("0");
        }
    }

    public String getFuncV2FuncString() {
        if (this.isFunctionV2Message()) {
            return (getValueString(2));
        } else {
            log.error("Function Parser called on non-Function V2 message type {}", this.getOpCodeChar());
            return ("0");
        }
    }

    public String getFuncV2StateString() {
        if (this.isFunctionV2Message()) {
            return (getValueString(3));
        } else {
            log.error("Function Parser called on non-Function V2 message type {}", this.getOpCodeChar());
            return ("0");
        }
    }

    public String getForgetCabString() {
        if (this.isForgetCabMessage()) {
            return (getValueString(1));
        } else {
            log.error("Function Parser called on non-Forget Cab message type {}", this.getOpCodeChar());
            return ("0");
        }
    }

    //------------------------------------------------------
    // Helper methods for Turnout Commands
    public String getTOIDString() {
        if (this.isTurnoutMessage()) {
            return (getValueString(1));
        } else {
            log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this);
            return ("0");
        }
    }

    public int getTOIDInt() {
        if (this.isTurnoutMessage()) {
            return (getValueInt(1));
        } else {
            log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this);
            return (0);
        }
    }

    public String getTOStateString() {
        if (isTurnoutMessage()) {
            return (this.getTOStateInt() == 1 ? "THROWN" : "CLOSED");
        } else {
            return ("Not a Turnout");
        }
    }

    public int getTOStateInt() {
        if (this.isTurnoutMessage()) {
            return (getValueInt(2));
        } else {
            log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this);
            return (0);
        }
    }

    public String getTOAddressString() {
        if (this.isTurnoutAddMessage() || this.isTurnoutAddDCCMessage()) {
            return (getValueString(2));
        } else {
            log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this);
            return ("0");
        }
    }

    public int getTOAddressInt() {
        if (this.isTurnoutAddMessage() || this.isTurnoutAddDCCMessage()) {
            return (getValueInt(2));
        } else {
            log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this);
            return (0);
        }
    }

    public String getTOSubAddressString() {
        if (this.isTurnoutAddMessage() || this.isTurnoutAddDCCMessage()) {
            return (getValueString(3));
        } else {
            log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this);
            return ("0");
        }
    }

    public int getTOSubAddressInt() {
        if (this.isTurnoutAddMessage() || this.isTurnoutAddDCCMessage()) {
            return (getValueInt(3));
        } else {
            log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this);
            return (0);
        }
    }

    public int getTOThrownPositionInt() {
        if (this.isTurnoutAddServoMessage()) {
            return (getValueInt(3));
        } else {
            log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this);
            return (0);
        }
    }

    public int getTOClosedPositionInt() {
        if (this.isTurnoutAddServoMessage()) {
            return (getValueInt(4));
        } else {
            log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this);
            return (0);
        }
    }

    public int getTOProfileInt() {
        if (this.isTurnoutAddServoMessage()) {
            return (getValueInt(5));
        } else {
            log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this);
            return (0);
        }
    }

    public int getTOPinInt() {
        if (this.isTurnoutAddServoMessage() || this.isTurnoutAddVpinMessage()) {
            return (getValueInt(2));
        } else {
            log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this);
            return (0);
        }
    }

    //------------------------------------------------------
    // Helper methods for Ops Write Byte Commands
    public String getOpsWriteAddrString() {
        if (this.isOpsWriteByteMessage() || this.isOpsWriteBitMessage()) {
            return (getValueString(1));
        } else {
            return ("0");
        }
    }

    public int getOpsWriteAddrInt() {
        if (this.isOpsWriteByteMessage() || this.isOpsWriteBitMessage()) {
            return (getValueInt(1));
        } else {
            return (0);
        }
    }

    public String getOpsWriteCVString() {
        if (this.isOpsWriteByteMessage() || this.isOpsWriteBitMessage()) {
            return (getValueString(2));
        } else {
            return ("0");
        }
    }

    public int getOpsWriteCVInt() {
        if (this.isOpsWriteByteMessage() || this.isOpsWriteBitMessage()) {
            return (getValueInt(2));
        } else {
            return (0);
        }
    }

    public String getOpsWriteBitString() {
        if (this.isOpsWriteBitMessage()) {
            return (getValueString(3));
        } else {
            return ("0");
        }
    }

    public int getOpsWriteBitInt() {
        if (this.isOpsWriteBitMessage()) {
            return (getValueInt(3));
        } else {
            return (0);
        }
    }

    public String getOpsWriteValueString() {
        if (this.isOpsWriteByteMessage()) {
            return (getValueString(3));
        } else if (this.isOpsWriteBitMessage()) {
            return (getValueString(4));
        } else {
            log.error("Ops Program Parser called on non-OpsProgram message type {}", this.getOpCodeChar());
            return ("0");
        }
    }

    public int getOpsWriteValueInt() {
        if (this.isOpsWriteByteMessage()) {
            return (getValueInt(3));
        } else if (this.isOpsWriteBitMessage()) {
            return (getValueInt(4));
        } else {
            return (0);
        }
    }

    //------------------------------------------------------
    // Helper methods for Prog Write and Read Byte Commands
    public String getCVString() {
        if (this.isProgWriteByteMessage() || this.isProgWriteBitMessage() || this.isProgReadMessage() || this.isProgVerifyMessage()) {
            return (getValueString(1));
        } else {
            return ("0");
        }
    }

    public int getCVInt() {
        if (this.isProgWriteByteMessage() || this.isProgWriteBitMessage() || this.isProgReadMessage() || this.isProgVerifyMessage()) {
            return (getValueInt(1));
        } else {
            return (0);
        }
    }

    public String getCallbackNumString() {
        int idx;
        if (this.isProgWriteByteMessage()) {
            idx = 3;
        } else if (this.isProgWriteBitMessage()) {
            idx = 4;
        } else if (this.isProgReadMessage()) {
            idx = 2;
        } else {
            return ("0");
        }
        return (getValueString(idx));
    }

    public int getCallbackNumInt() {
        int idx;
        if (this.isProgWriteByteMessage()) {
            idx = 3;
        } else if (this.isProgWriteBitMessage()) {
            idx = 4;
        } else if (this.isProgReadMessage()) {
            idx = 2;
        } else {
            return (0);
        }
        return (getValueInt(idx));
    }

    public String getCallbackSubString() {
        int idx;
        if (this.isProgWriteByteMessage()) {
            idx = 4;
        } else if (this.isProgWriteBitMessage()) {
            idx = 5;
        } else if (this.isProgReadMessage()) {
            idx = 3;
        } else {
            return ("0");
        }
        return (getValueString(idx));
    }

    public int getCallbackSubInt() {
        int idx;
        if (this.isProgWriteByteMessage()) {
            idx = 4;
        } else if (this.isProgWriteBitMessage()) {
            idx = 5;
        } else if (this.isProgReadMessage()) {
            idx = 3;
        } else {
            return (0);
        }
        return (getValueInt(idx));
    }

    public String getProgValueString() {
        int idx;
        if (this.isProgWriteByteMessage() || this.isProgVerifyMessage()) {
            idx = 2;
        } else if (this.isProgWriteBitMessage()) {
            idx = 3;
        } else {
            return ("0");
        }
        return (getValueString(idx));
    }

    public int getProgValueInt() {
        int idx;
        if (this.isProgWriteByteMessage() || this.isProgVerifyMessage()) {
            idx = 2;
        } else if (this.isProgWriteBitMessage()) {
            idx = 3;
        } else {
            return (0);
        }
        return (getValueInt(idx));
    }

    //------------------------------------------------------
    // Helper methods for Prog Write Bit Commands
    public String getBitString() {
        if (this.isProgWriteBitMessage()) {
            return (getValueString(2));
        } else {
            log.error("PWBit Parser called on non-PWBit message type {}", this.getOpCodeChar());
            return ("0");
        }
    }

    public int getBitInt() {
        if (this.isProgWriteBitMessage()) {
            return (getValueInt(2));
        } else {
            return (0);
        }
    }

    public String getPacketString() {
        if (this.isWriteDccPacketMessage()) {
            StringBuilder b = new StringBuilder();
            for (int i = 2; i <= getGroupCount() - 1; i++) {
                b.append(this.getValueString(i));
            }
            return (b.toString());
        } else {
            log.error("Write Dcc Packet parser called on non-Dcc Packet message type {}", this.getOpCodeChar());
            return ("0");
        }
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
        boolean retv;
        switch (this.getOpCodeChar()) {
            case DCCppConstants.THROTTLE_CMD:
            case DCCppConstants.TURNOUT_CMD:
            case DCCppConstants.SENSOR_CMD:
            case DCCppConstants.PROG_WRITE_CV_BYTE:
            case DCCppConstants.PROG_WRITE_CV_BIT:
            case DCCppConstants.PROG_READ_CV:
            case DCCppConstants.PROG_VERIFY_CV:
            case DCCppConstants.TRACK_POWER_ON:
            case DCCppConstants.TRACK_POWER_OFF:
            case DCCppConstants.READ_TRACK_CURRENT:
            case DCCppConstants.READ_CS_STATUS:
            case DCCppConstants.READ_MAXNUMSLOTS:
            case DCCppConstants.OUTPUT_CMD:
            case DCCppConstants.LIST_REGISTER_CONTENTS:
                retv = true;
                break;
            default:
                retv = false;
        }
        return (retv);
    }

    // decode messages of a particular form
    // create messages of a particular form

    /*
     * The next group of routines are used by Feedback and/or turnout
     * control code.  These are used in multiple places within the code,
     * so they appear here.
     */
    
    /**
     * Stationary Decoder Message.
     * <p>
     * Note that many decoders and controllers combine the ADDRESS and
     * SUBADDRESS into a single number, N, from 1 through a max of 2044, where
     * <p>
     * {@code N = (ADDRESS - 1) * 4 + SUBADDRESS + 1, for all ADDRESS>0}
     * <p>
     * OR
     * <p>
     * {@code ADDRESS = INT((N - 1) / 4) + 1}
     *    {@code SUBADDRESS = (N - 1) % 4}
     * <p>
     * @param address the primary address of the decoder (0-511).
     * @param subaddress the subaddress of the decoder (0-3).
     * @param activate true on, false off.
     * @return accessory decoder message.
     */
    public static DCCppMessage makeAccessoryDecoderMsg(int address, int subaddress, boolean activate) {
        // Sanity check inputs
        if (address < 0 || address > DCCppConstants.MAX_ACC_DECODER_ADDRESS) {
            return (null);
        }
        if (subaddress < 0 || subaddress > DCCppConstants.MAX_ACC_DECODER_SUBADDR) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.ACCESSORY_CMD);

        m.myMessage.append(" ").append(address);
        m.myMessage.append(" ").append(subaddress);
        m.myMessage.append(" ").append(activate ? "1" : "0");
        m.myRegex = DCCppConstants.ACCESSORY_CMD_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    public static DCCppMessage makeAccessoryDecoderMsg(int address, boolean activate) {
        // Convert the single address to an address/subaddress pair:
        // address = (address - 1) * 4 + subaddress + 1 for address>0;
        int addr, subaddr;
        if (address > 0) {
            addr = ((address - 1) / (DCCppConstants.MAX_ACC_DECODER_SUBADDR + 1)) + 1;
            subaddr = (address - 1) % (DCCppConstants.MAX_ACC_DECODER_SUBADDR + 1);
        } else {
            addr = subaddr = 0;
        }
        log.debug("makeAccessoryDecoderMsg address {}, addr {}, subaddr {}, activate {}", address, addr, subaddr, activate);
        return (makeAccessoryDecoderMsg(addr, subaddr, activate));
    }

    /**
     * Predefined Turnout Control Message.
     * <p>
     * @param id the numeric ID (0-32767) of the turnout to control.
     * @param thrown true thrown, false closed.
     * @return message to set turnout.
     */
    public static DCCppMessage makeTurnoutCommandMsg(int id, boolean thrown) {
        // Sanity check inputs
        if (id < 0 || id > DCCppConstants.MAX_TURNOUT_ADDRESS) {
            return (null);
        }
        // Need to also validate whether turnout is predefined?  Where to store the IDs?
        // Turnout Command

        DCCppMessage m = new DCCppMessage(DCCppConstants.TURNOUT_CMD);
        m.myMessage.append(" ").append(id);
        m.myMessage.append((thrown ? " 1" : " 0"));
        m.myRegex = DCCppConstants.TURNOUT_CMD_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    public static DCCppMessage makeOutputCmdMsg(int id, boolean state) {
        // Sanity check inputs
        if (id < 0 || id > DCCppConstants.MAX_TURNOUT_ADDRESS) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.OUTPUT_CMD);
        m.myMessage.append(" ").append(id);
        m.myMessage.append(" ").append(state ? "1" : "0");
        m.myRegex = DCCppConstants.OUTPUT_CMD_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    public static DCCppMessage makeOutputAddMsg(int id, int pin, int iflag) {
        // Sanity check inputs
        if (id < 0 || id > DCCppConstants.MAX_TURNOUT_ADDRESS) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.OUTPUT_CMD);
        m.myMessage.append(" ").append(id);
        m.myMessage.append(" ").append(pin);
        m.myMessage.append(" ").append(iflag);
        m.myRegex = DCCppConstants.OUTPUT_ADD_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    public static DCCppMessage makeOutputDeleteMsg(int id) {
        // Sanity check inputs
        if (id < 0 || id > DCCppConstants.MAX_TURNOUT_ADDRESS) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.OUTPUT_CMD);
        m.myMessage.append(" ").append(id);
        m.myRegex = DCCppConstants.OUTPUT_DELETE_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    public static DCCppMessage makeOutputListMsg() {
        return (new DCCppMessage(DCCppConstants.OUTPUT_CMD, DCCppConstants.OUTPUT_LIST_REGEX));
    }

    public static DCCppMessage makeTurnoutAddMsg(int id, int addr, int subaddr) {
        // Sanity check inputs
        if (id < 0 || id > DCCppConstants.MAX_TURNOUT_ADDRESS) {
            log.error("turnout Id {} must be between {} and {}", id, 0, DCCppConstants.MAX_TURNOUT_ADDRESS);
            return (null);
        }
        if (addr < 0 || addr > DCCppConstants.MAX_ACC_DECODER_ADDRESS) {
            log.error("turnout address {} must be between {} and {}", id, 0, DCCppConstants.MAX_ACC_DECODER_ADDRESS);
            return (null);
        }
        if (subaddr < 0 || subaddr > DCCppConstants.MAX_ACC_DECODER_SUBADDR) {
            log.error("turnout subaddress {} must be between {} and {}", id, 0, DCCppConstants.MAX_ACC_DECODER_SUBADDR);
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.TURNOUT_CMD);
        m.myMessage.append(" ").append(id);
        m.myMessage.append(" ").append(addr);
        m.myMessage.append(" ").append(subaddr);
        m.myRegex = DCCppConstants.TURNOUT_ADD_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    public static DCCppMessage makeTurnoutDeleteMsg(int id) {
        // Sanity check inputs
        if (id < 0 || id > DCCppConstants.MAX_TURNOUT_ADDRESS) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.TURNOUT_CMD);
        m.myMessage.append(" ").append(id);
        m.myRegex = DCCppConstants.TURNOUT_DELETE_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    public static DCCppMessage makeTurnoutListMsg() {
        return (new DCCppMessage(DCCppConstants.TURNOUT_CMD, DCCppConstants.TURNOUT_LIST_REGEX));
    }

    public static DCCppMessage makeMessage(String msg) {
        return (new DCCppMessage(msg));
    }

    /**
     * Create/Delete/Query Sensor.
     * <p>
     * sensor, or {@code <X>} if no sensors defined.
     * @param id pin pullup (0-32767).
     * @param pin Arduino pin index of sensor.
     * @param pullup true if use internal pullup for PIN, false if not.
     * @return message to create the sensor.
     */
    public static DCCppMessage makeSensorAddMsg(int id, int pin, int pullup) {
        // Sanity check inputs
        // TODO: Optional sanity check pin number vs. Arduino model.
        if (id < 0 || id > DCCppConstants.MAX_SENSOR_ID) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.SENSOR_CMD);
        m.myMessage.append(" ").append(id);
        m.myMessage.append(" ").append(pin);
        m.myMessage.append(" ").append(pullup);
        m.myRegex = DCCppConstants.SENSOR_ADD_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    public static DCCppMessage makeSensorDeleteMsg(int id) {
        // Sanity check inputs
        if (id < 0 || id > DCCppConstants.MAX_SENSOR_ID) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.SENSOR_CMD);
        m.myMessage.append(" ").append(id);
        m.myRegex = DCCppConstants.SENSOR_DELETE_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    public static DCCppMessage makeSensorListMsg() {
        return (new DCCppMessage(DCCppConstants.SENSOR_CMD, DCCppConstants.SENSOR_LIST_REGEX));
    }

    /**
     * Query All Sensors States.
     * <p>
     * @return message to query all sensor states.
     */
    public static DCCppMessage makeQuerySensorStatesMsg() {
        return (new DCCppMessage(DCCppConstants.QUERY_SENSOR_STATES_CMD, DCCppConstants.QUERY_SENSOR_STATES_REGEX));
    }

    /**
     * Write Direct CV Byte to Programming Track
     * <p>
     * Format: {@code <W CV VALUE CALLBACKNUM CALLBACKSUB>}
     * <p>
     * ID: the numeric ID (0-32767) of the turnout to control THROW: 0
     * (unthrown) or 1 (thrown) CV: the number of the Configuration Variable
     * memory location in the decoder to write to (1-1024) VALUE: the value to
     * be written to the Configuration Variable memory location (0-255)
     * CALLBACKNUM: an arbitrary integer (0-32767) that is ignored by the Base
     * Station and is simply echoed back in the output - useful for external
     * programs that call this function CALLBACKSUB: a second arbitrary integer
     * (0-32767) that is ignored by the Base Station and is simply echoed back
     * in the output - useful for external programs (e.g. DCC++ Interface) that
     * call this function
     * <p>
     * Note: The two-argument form embeds the opcode in CALLBACKSUB to aid in
     * decoding the responses.
     * <p>
     * returns: {@code <r CALLBACKNUM|CALLBACKSUB|CV Value)} where VALUE is a
     * number from 0-255 as read from the requested CV, or -1 if verification
     * read fails
     * @param cv CV index, 1-1024.
     * @param val new CV value, 0-255.
     * @return message to write Direct CV.
     */
    public static DCCppMessage makeWriteDirectCVMsg(int cv, int val) {
        return (makeWriteDirectCVMsg(cv, val, 0, DCCppConstants.PROG_WRITE_CV_BYTE));
    }

    public static DCCppMessage makeWriteDirectCVMsg(int cv, int val, int callbacknum, int callbacksub) {
        // Sanity check inputs
        if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) {
            return (null);
        }
        if (val < 0 || val > DCCppConstants.MAX_DIRECT_CV_VAL) {
            return (null);
        }
        if (callbacknum < 0 || callbacknum > DCCppConstants.MAX_CALLBACK_NUM) {
            return (null);
        }
        if (callbacksub < 0 || callbacksub > DCCppConstants.MAX_CALLBACK_SUB) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.PROG_WRITE_CV_BYTE);
        m.myMessage.append(" ").append(cv);
        m.myMessage.append(" ").append(val);
        m.myMessage.append(" ").append(callbacknum);
        m.myMessage.append(" ").append(callbacksub);
        m.myRegex = DCCppConstants.PROG_WRITE_BYTE_REGEX;

        m._nDataChars = m.toString().length();
        m.setTimeout(DCCppProgrammingTimeout);
        return (m);
    }

    /**
     * Write Direct CV Bit to Programming Track.
     * <p>
     * Format: {@code <B CV BIT VALUE CALLBACKNUM CALLBACKSUB>}
     * <p>
     * writes, and then verifies, a single bit within a Configuration Variable
     * to the decoder of an engine on the programming track
     * <p>
     * CV: the number of the Configuration Variable memory location in the
     * decoder to write to (1-1024) BIT: the bit number of the Configurarion
     * Variable memory location to write (0-7) VALUE: the value of the bit to be
     * written (0-1) CALLBACKNUM: an arbitrary integer (0-32767) that is ignored
     * by the Base Station and is simply echoed back in the output - useful for
     * external programs that call this function CALLBACKSUB: a second arbitrary
     * integer (0-32767) that is ignored by the Base Station and is simply
     * echoed back in the output - useful for external programs (e.g. DCC++
     * Interface) that call this function
     * <p>
     * Note: The two-argument form embeds the opcode in CALLBACKSUB to aid in
     * decoding the responses.
     * <p>
     * returns: {@code <r CALLBACKNUM|CALLBACKSUB|CV BIT VALUE)} where VALUE is
     * a number from 0-1 as read from the requested CV bit, or -1 if
     * verification read fails
     * @param cv CV index, 1-1024.
     * @param bit bit index, 0-7
     * @param val bit value, 0-1.
     * @return message to write direct CV bit.
     */
    public static DCCppMessage makeBitWriteDirectCVMsg(int cv, int bit, int val) {
        return (makeBitWriteDirectCVMsg(cv, bit, val, 0, DCCppConstants.PROG_WRITE_CV_BIT));
    }

    public static DCCppMessage makeBitWriteDirectCVMsg(int cv, int bit, int val, int callbacknum, int callbacksub) {

        // Sanity Check Inputs
        if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) {
            return (null);
        }
        if (bit < 0 || bit > 7) {
            return (null);
        }
        if (callbacknum < 0 || callbacknum > DCCppConstants.MAX_CALLBACK_NUM) {
            return (null);
        }
        if (callbacksub < 0 || callbacksub > DCCppConstants.MAX_CALLBACK_SUB) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.PROG_WRITE_CV_BIT);
        m.myMessage.append(" ").append(cv);
        m.myMessage.append(" ").append(bit);
        m.myMessage.append(" ").append(val == 0 ? "0" : "1");
        m.myMessage.append(" ").append(callbacknum);
        m.myMessage.append(" ").append(callbacksub);
        m.myRegex = DCCppConstants.PROG_WRITE_BIT_REGEX;

        m._nDataChars = m.toString().length();
        m.setTimeout(DCCppProgrammingTimeout);
        return (m);
    }

    /**
     * Read Direct CV Byte from Programming Track.
     * <p>
     * Format: {@code <R CV CALLBACKNUM CALLBACKSUB>}
     * <p>
     * reads a Configuration Variable from the decoder of an engine on the
     * programming track
     * <p>
     * CV: the number of the Configuration Variable memory location in the
     * decoder to read from (1-1024) CALLBACKNUM: an arbitrary integer (0-32767)
     * that is ignored by the Base Station and is simply echoed back in the
     * output - useful for external programs that call this function
     * CALLBACKSUB: a second arbitrary integer (0-32767) that is ignored by the
     * Base Station and is simply echoed back in the output - useful for
     * external programs (e.g. DCC++ Interface) that call this function
     * <p>
     * Note: The two-argument form embeds the opcode in CALLBACKSUB to aid in
     * decoding the responses.
     * <p>
     * returns: {@code <r CALLBACKNUM|CALLBACKSUB|CV VALUE>} where VALUE is a
     * number from 0-255 as read from the requested CV, or -1 if read could not
     * be verified
     * @param cv CV index.
     * @return message to send read direct CV.
     */
    public static DCCppMessage makeReadDirectCVMsg(int cv) {
        return (makeReadDirectCVMsg(cv, 0, DCCppConstants.PROG_READ_CV));
    }

    public static DCCppMessage makeReadDirectCVMsg(int cv, int callbacknum, int callbacksub) {
        // Sanity check inputs
        if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) {
            return (null);
        }
        if (callbacknum < 0 || callbacknum > DCCppConstants.MAX_CALLBACK_NUM) {
            return (null);
        }
        if (callbacksub < 0 || callbacksub > DCCppConstants.MAX_CALLBACK_SUB) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.PROG_READ_CV);
        m.myMessage.append(" ").append(cv);
        m.myMessage.append(" ").append(callbacknum);
        m.myMessage.append(" ").append(callbacksub);
        m.myRegex = DCCppConstants.PROG_READ_REGEX;

        m._nDataChars = m.toString().length();
        m.setTimeout(DCCppProgrammingTimeout);
        return (m);
    }

    /**
     * Verify Direct CV Byte from Programming Track.
     * <p>
     * Format: {@code <V CV STARTVAL>}
     * <p>
     * Verifies a Configuration Variable from the decoder of an engine on the
     * programming track. Returns the current value of that CV.
     * Used as faster replacement for 'R'eadCV command
     * <p>
     * CV: the number of the Configuration Variable memory location in the
     * decoder to read from (1-1024) STARTVAL: a "guess" as to the current
     * value of the CV. DCC-EX will try this value first, then read and return
     * the current value if different
     * <p>
     * returns: {@code <v CV VALUE>} where VALUE is a
     * number from 0-255 as read from the requested CV, -1 if read could not
     * be performed
     * @param cv CV index.
     * @param startVal "guess" as to current value
     * @return message to send verify direct CV.
     */
    public static DCCppMessage makeVerifyCVMsg(int cv, int startVal) {
        // Sanity check inputs
        if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) {
            return (null);
        }
        DCCppMessage m = new DCCppMessage(DCCppConstants.PROG_VERIFY_CV);
        m.myMessage.append(" ").append(cv);
        m.myMessage.append(" ").append(startVal);
        m.myRegex = DCCppConstants.PROG_VERIFY_REGEX;

        m._nDataChars = m.toString().length();
        m.setTimeout(DCCppProgrammingTimeout);
        return (m);
    }

    /**
     * Write Direct CV Byte to Main Track
     * <p>
     * Format: {@code <w CAB CV VALUE>}
     * <p>
     * Writes, without any verification, a Configuration Variable to the decoder
     * of an engine on the main operations track.
     * <p>
     * @param address the short (1-127) or long (128-10293) address of the 
     *                  engine decoder.
     * @param cv the number of the Configuration Variable memory location in the
     *                  decoder to write to (1-1024).
     * @param val the value to be written to the
     *                  Configuration Variable memory location (0-255).
     * @return message to Write CV in Ops Mode.
     */
    @CheckForNull
    public static DCCppMessage makeWriteOpsModeCVMsg(int address, int cv, int val) {
        // Sanity check inputs
        if (address < 0 || address > DCCppConstants.MAX_LOCO_ADDRESS) {
            return (null);
        }
        if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) {
            return (null);
        }
        if (val < 0 || val > DCCppConstants.MAX_DIRECT_CV_VAL) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.OPS_WRITE_CV_BYTE);
        m.myMessage.append(" ").append(address);
        m.myMessage.append(" ").append(cv);
        m.myMessage.append(" ").append(val);
        m.myRegex = DCCppConstants.OPS_WRITE_BYTE_REGEX;

        m._nDataChars = m.toString().length();
        m.setTimeout(DCCppProgrammingTimeout);
        return (m);
    }

    /**
     * Write Direct CV Bit to Main Track.
     * <p>
     * Format: {@code <b CAB CV BIT VALUE>}
     * <p>
     * writes, without any verification, a single bit within a Configuration
     * Variable to the decoder of an engine on the main operations track
     * <p>
     * CAB: the short (1-127) or long (128-10293) address of the engine decoder
     * CV: the number of the Configuration Variable memory location in the
     * decoder to write to (1-1024) BIT: the bit number of the Configuration
     * Variable register to write (0-7) VALUE: the value of the bit to be
     * written (0-1)
     * <p>
     * returns: NONE
     * @param address loco cab address.
     * @param cv CV index, 1-1024.
     * @param bit bit index, 0-7.
     * @param val bit value, 0 or 1.
     * @return message to write direct CV bit to main track.
     */
    public static DCCppMessage makeBitWriteOpsModeCVMsg(int address, int cv, int bit, int val) {
        // Sanity Check Inputs
        if (address < 0 || address > DCCppConstants.MAX_LOCO_ADDRESS) {
            return (null);
        }
        if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) {
            return (null);
        }
        if (bit < 0 || bit > 7) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.OPS_WRITE_CV_BIT);
        m.myMessage.append(" ").append(address);
        m.myMessage.append(" ").append(cv);
        m.myMessage.append(" ").append(bit);
        m.myMessage.append(" ").append(val == 0 ? "0" : "1");

        m.myRegex = DCCppConstants.OPS_WRITE_BIT_REGEX;

        m._nDataChars = m.toString().length();
        m.setTimeout(DCCppProgrammingTimeout);
        return (m);
    }

    /**
     * Set Track Power ON or OFF.
     * <p>
     * Format: {@code <1> (ON) or <0> (OFF)}
     * <p>
     * @return message to send track power on or off.
     * @param on true on, false off.
     */
    public static DCCppMessage makeSetTrackPowerMsg(boolean on) {
        return (new DCCppMessage((on ? DCCppConstants.TRACK_POWER_ON : DCCppConstants.TRACK_POWER_OFF),
                DCCppConstants.TRACK_POWER_REGEX));
    }

    public static DCCppMessage makeTrackPowerOnMsg() {
        return (makeSetTrackPowerMsg(true));
    }

    public static DCCppMessage makeTrackPowerOffMsg() {
        return (makeSetTrackPowerMsg(false));
    }

    /**
     * Read main operations track current
     * <p>
     * Format: {@code <c>}
     * <p>
     * reads current being drawn on main operations track
     * <p>
     * @return (for DCC-EX), 1 or more of  {@code <c MeterName value C/V unit min max res warn>}
     * where name and settings are used to define arbitrary meters on the DCC-EX side
     * AND {@code <a CURRENT>} where CURRENT = 0-1024, based on 
     * exponentially-smoothed weighting scheme
     * 
     */
    public static DCCppMessage makeReadTrackCurrentMsg() {
        return (new DCCppMessage(DCCppConstants.READ_TRACK_CURRENT, DCCppConstants.READ_TRACK_CURRENT_REGEX));
    }

    /**
     * Read DCC++ Base Station Status
     * <p>
     * Format: {@code <s>}
     * <p>
     * returns status messages containing track power status, throttle status,
     * turn-out status, and a version number NOTE: this is very useful as a
     * first command for an interface to send to this sketch in order to verify
     * connectivity and update any GUI to reflect actual throttle and turn-out
     * settings
     * <p>
     * @return series of status messages that can be read by an interface to
     * determine status of DCC++ Base Station and important settings
     */
    public static DCCppMessage makeCSStatusMsg() {
        return (new DCCppMessage(DCCppConstants.READ_CS_STATUS, DCCppConstants.READ_CS_STATUS_REGEX));
    }

    /**
     * Get number of supported slots for this DCC++ Base Station Status
     * <p>
     * Format: {@code <N>}
     * <p>
     * returns number of slots NOTE: this is not implemented in older versions
     * which then do not return anything at all
     * <p>
     * @return status message with to get number of slots.
     */
    public static DCCppMessage makeCSMaxNumSlotsMsg() {
        return (new DCCppMessage(DCCppConstants.READ_MAXNUMSLOTS, DCCppConstants.READ_MAXNUMSLOTS_REGEX));
    }
    /**
     * Generate a function message using the V2 'F' syntax supported by DCC-EX
     * <p>
     * @param cab cab address to send function to
     * @param func function number to set
     * @param state new state of function 0/1
     * @return function V2 message
     */
    public static DCCppMessage makeFunctionV2Message(int cab, int func, int state) {
        // Sanity check inputs
        if (cab < 0 || cab > DCCppConstants.MAX_LOCO_ADDRESS) {
            return (null);
        }
        if (func < 0 || func > DCCppConstants.MAX_FUNCTION_NUMBER) {
            return (null);
        }
        if (state < 0 || state > 1) {
            return (null);
        }
        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_V2_CMD);
        m.myMessage.append(" ").append(cab);
        m.myMessage.append(" ").append(func);
        m.myMessage.append(" ").append(state);
        m.myRegex = DCCppConstants.FUNCTION_V2_CMD_REGEX;
        m._nDataChars = m.toString().length();
        return (m);
    }

    /**
     * Generate a "Forget Cab" message '-'
     * <p>
     * @param cab cab address to send function to (or 0 for all)
     * @return forget message to be sent
     */
    public static DCCppMessage makeForgetCabMessage(int cab) {
        // Sanity check inputs
        if (cab < 0 || cab > DCCppConstants.MAX_LOCO_ADDRESS) {
            return (null);
        }
        DCCppMessage m = new DCCppMessage(DCCppConstants.FORGET_CAB_CMD);
        if (cab > 0) {
            m.myMessage.append(" ").append(cab);
        }
        m.myRegex = DCCppConstants.FORGET_CAB_CMD_REGEX;
        m._nDataChars = m.toString().length();
        return (m);
    }

    /**
     * Generate an emergency stop for the specified address.
     * <p>
     * Note: This just sends a THROTTLE command with speed = -1
     * 
     * @param register Register Number for the loco assigned address.
     * @param address is the locomotive address.
     * @return message to send e stop to the specified address.
     */
    public static DCCppMessage makeAddressedEmergencyStop(int register, int address) {
        // Sanity check inputs
        if (address < 0 || address > DCCppConstants.MAX_LOCO_ADDRESS) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.THROTTLE_CMD);
        m.myMessage.append(" ").append(register);
        m.myMessage.append(" ").append(address);
        m.myMessage.append(" -1 1");
        m.myRegex = DCCppConstants.THROTTLE_CMD_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    /**
     * Generate an emergency stop for all locos in reminder table.
     * @return message to send e stop for all locos
     */
    public static DCCppMessage makeEmergencyStopAllMsg() {
        DCCppMessage m = new DCCppMessage(DCCppConstants.ESTOP_ALL_CMD);
        m.myRegex = DCCppConstants.ESTOP_ALL_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    /**
     * Generate a Speed and Direction Request message
     *
     * @param register  is the DCC++ base station register assigned.
     * @param address   is the locomotive address
     * @param speed     a normalized speed value (a floating point number
     *                  between 0 and 1). A negative value indicates emergency
     *                  stop.
     * @param isForward true for forward, false for reverse.
     *
     * Format: {@code <t REGISTER CAB SPEED DIRECTION>}
     *
     * sets the throttle for a given register/cab combination
     *
     * REGISTER: an internal register number, from 1 through MAX_MAIN_REGISTERS
     * (inclusive), to store the DCC packet used to control this throttle
     * setting CAB: the short (1-127) or long (128-10293) address of the engine
     * decoder SPEED: throttle speed from 0-126, or -1 for emergency stop
     * (resets SPEED to 0) DIRECTION: 1=forward, 0=reverse. Setting direction
     * when speed=0 or speed=-1 only effects directionality of cab lighting for
     * a stopped train
     *
     * @return {@code <T REGISTER SPEED DIRECTION>}
     *
     */
    public static DCCppMessage makeSpeedAndDirectionMsg(int register, int address, float speed, boolean isForward) {
        // Sanity check inputs
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.THROTTLE_CMD);
        m.myMessage.append(" ").append(register);
        m.myMessage.append(" ").append(address);
        if (speed < 0.0) {
            m.myMessage.append(" -1");
        } else {
            int speedVal = java.lang.Math.round(speed * 126);
            speedVal = Math.min(speedVal, DCCppConstants.MAX_SPEED);
            m.myMessage.append(" ").append(speedVal);
        }
        m.myMessage.append(" ").append(isForward ? "1" : "0");

        m.myRegex = DCCppConstants.THROTTLE_CMD_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    /*
     * Function Group Messages (common serial format)
     * <p>
     * Format: {@code <f CAB BYTE1 [BYTE2]>}
     * <p>
     * turns on and off engine decoder functions F0-F28 (F0 is sometimes called
     * FL) NOTE: setting requests transmitted directly to mobile engine decoder
     * --- current state of engine functions is not stored by this program
     * <p>
     * CAB: the short (1-127) or long (128-10293) address of the engine decoder
     * <p>
     * To set functions F0-F4 on (=1) or off (=0):
     * <p>
     * BYTE1: 128 + F1*1 + F2*2 + F3*4 + F4*8 + F0*16 BYTE2: omitted
     * <p>
     * To set functions F5-F8 on (=1) or off (=0):
     * <p>
     * BYTE1: 176 + F5*1 + F6*2 + F7*4 + F8*8 BYTE2: omitted
     * <p>
     * To set functions F9-F12 on (=1) or off (=0):
     * <p>
     * BYTE1: 160 + F9*1 +F10*2 + F11*4 + F12*8 BYTE2: omitted
     * <p>
     * To set functions F13-F20 on (=1) or off (=0):
     * <p>
     * BYTE1: 222 BYTE2: F13*1 + F14*2 + F15*4 + F16*8 + F17*16 + F18*32 +
     * F19*64 + F20*128
     * <p>
     * To set functions F21-F28 on (=1) of off (=0):
     * <p>
     * BYTE1: 223 BYTE2: F21*1 + F22*2 + F23*4 + F24*8 + F25*16 + F26*32 +
     * F27*64 + F28*128
     * <p>
     * returns: NONE
     * <p>
     */
    /**
     * Generate a Function Group One Operation Request message.
     *
     * @param address is the locomotive address
     * @param f0      is true if f0 is on, false if f0 is off
     * @param f1      is true if f1 is on, false if f1 is off
     * @param f2      is true if f2 is on, false if f2 is off
     * @param f3      is true if f3 is on, false if f3 is off
     * @param f4      is true if f4 is on, false if f4 is off
     * @return message to set function group 1.
     */
    public static DCCppMessage makeFunctionGroup1OpsMsg(int address,
            boolean f0,
            boolean f1,
            boolean f2,
            boolean f3,
            boolean f4) {
        // Sanity check inputs
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" ").append(address);

        int byte1 = 128 + (f0 ? 16 : 0);
        byte1 += (f1 ? 1 : 0);
        byte1 += (f2 ? 2 : 0);
        byte1 += (f3 ? 4 : 0);
        byte1 += (f4 ? 8 : 0);
        m.myMessage.append(" ").append(byte1);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    /**
     * Generate a Function Group One Set Momentary Functions message.
     *
     * @param address is the locomotive address
     * @param f0      is true if f0 is momentary
     * @param f1      is true if f1 is momentary
     * @param f2      is true if f2 is momentary
     * @param f3      is true if f3 is momentary
     * @param f4      is true if f4 is momentary
     * @return message to set momentary function group 1.
     */
    public static DCCppMessage makeFunctionGroup1SetMomMsg(int address,
            boolean f0,
            boolean f1,
            boolean f2,
            boolean f3,
            boolean f4) {

        // Sanity check inputs
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" ").append(address);

        int byte1 = 128 + (f0 ? 16 : 0);
        byte1 += (f1 ? 1 : 0);
        byte1 += (f2 ? 2 : 0);
        byte1 += (f3 ? 4 : 0);
        byte1 += (f4 ? 8 : 0);

        m.myMessage.append(" ").append(byte1);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    /**
     * Generate a Function Group Two Operation Request message.
     *
     * @param address is the locomotive address
     * @param f5      is true if f5 is on, false if f5 is off
     * @param f6      is true if f6 is on, false if f6 is off
     * @param f7      is true if f7 is on, false if f7 is off
     * @param f8      is true if f8 is on, false if f8 is off
     * @return message to set function group 2.
     */
    public static DCCppMessage makeFunctionGroup2OpsMsg(int address,
            boolean f5,
            boolean f6,
            boolean f7,
            boolean f8) {

        // Sanity check inputs
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" ").append(address);

        int byte1 = 176;
        byte1 += (f5 ? 1 : 0);
        byte1 += (f6 ? 2 : 0);
        byte1 += (f7 ? 4 : 0);
        byte1 += (f8 ? 8 : 0);

        m.myMessage.append(" ").append(byte1);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    /**
     * Generate a Function Group Two Set Momentary Functions message.
     *
     * @param address is the locomotive address
     * @param f5      is true if f5 is momentary
     * @param f6      is true if f6 is momentary
     * @param f7      is true if f7 is momentary
     * @param f8      is true if f8 is momentary
     * @return message to set momentary function group 2.
     */
    public static DCCppMessage makeFunctionGroup2SetMomMsg(int address,
            boolean f5,
            boolean f6,
            boolean f7,
            boolean f8) {

        // Sanity check inputs
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" ").append(address);

        int byte1 = 176;
        byte1 += (f5 ? 1 : 0);
        byte1 += (f6 ? 2 : 0);
        byte1 += (f7 ? 4 : 0);
        byte1 += (f8 ? 8 : 0);
        m.myMessage.append(" ").append(byte1);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    /**
     * Generate a Function Group Three Operation Request message.
     *
     * @param address is the locomotive address
     * @param f9      is true if f9 is on, false if f9 is off
     * @param f10     is true if f10 is on, false if f10 is off
     * @param f11     is true if f11 is on, false if f11 is off
     * @param f12     is true if f12 is on, false if f12 is off
     * @return message to set function group 3.
     */
    public static DCCppMessage makeFunctionGroup3OpsMsg(int address,
            boolean f9,
            boolean f10,
            boolean f11,
            boolean f12) {

        // Sanity check inputs
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" ").append(address);

        int byte1 = 160;
        byte1 += (f9 ? 1 : 0);
        byte1 += (f10 ? 2 : 0);
        byte1 += (f11 ? 4 : 0);
        byte1 += (f12 ? 8 : 0);
        m.myMessage.append(" ").append(byte1);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    /**
     * Generate a Function Group Three Set Momentary Functions message.
     *
     * @param address is the locomotive address
     * @param f9      is true if f9 is momentary
     * @param f10     is true if f10 is momentary
     * @param f11     is true if f11 is momentary
     * @param f12     is true if f12 is momentary
     * @return message to set momentary function group 3.
     */
    public static DCCppMessage makeFunctionGroup3SetMomMsg(int address,
            boolean f9,
            boolean f10,
            boolean f11,
            boolean f12) {

        // Sanity check inputs
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" ").append(address);

        int byte1 = 160;
        byte1 += (f9 ? 1 : 0);
        byte1 += (f10 ? 2 : 0);
        byte1 += (f11 ? 4 : 0);
        byte1 += (f12 ? 8 : 0);
        m.myMessage.append(" ").append(byte1);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    /**
     * Generate a Function Group Four Operation Request message.
     *
     * @param address is the locomotive address
     * @param f13     is true if f13 is on, false if f13 is off
     * @param f14     is true if f14 is on, false if f14 is off
     * @param f15     is true if f15 is on, false if f15 is off
     * @param f16     is true if f18 is on, false if f16 is off
     * @param f17     is true if f17 is on, false if f17 is off
     * @param f18     is true if f18 is on, false if f18 is off
     * @param f19     is true if f19 is on, false if f19 is off
     * @param f20     is true if f20 is on, false if f20 is off
     * @return message to set function group 4.
     */
    public static DCCppMessage makeFunctionGroup4OpsMsg(int address,
            boolean f13,
            boolean f14,
            boolean f15,
            boolean f16,
            boolean f17,
            boolean f18,
            boolean f19,
            boolean f20) {

        // Sanity check inputs
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" ").append(address);

        int byte2 = 0;
        byte2 += (f13 ? 1 : 0);
        byte2 += (f14 ? 2 : 0);
        byte2 += (f15 ? 4 : 0);
        byte2 += (f16 ? 8 : 0);
        byte2 += (f17 ? 16 : 0);
        byte2 += (f18 ? 32 : 0);
        byte2 += (f19 ? 64 : 0);
        byte2 += (f20 ? 128 : 0);
        m.myMessage.append(" ").append(DCCppConstants.FUNCTION_GROUP4_BYTE1);
        m.myMessage.append(" ").append(byte2);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    /**
     * Generate a Function Group Four Set Momentary Function message.
     *
     * @param address is the locomotive address
     * @param f13     is true if f13 is Momentary
     * @param f14     is true if f14 is Momentary
     * @param f15     is true if f15 is Momentary
     * @param f16     is true if f18 is Momentary
     * @param f17     is true if f17 is Momentary
     * @param f18     is true if f18 is Momentary
     * @param f19     is true if f19 is Momentary
     * @param f20     is true if f20 is Momentary
     * @return message to set momentary function group 4.
     */
    public static DCCppMessage makeFunctionGroup4SetMomMsg(int address,
            boolean f13,
            boolean f14,
            boolean f15,
            boolean f16,
            boolean f17,
            boolean f18,
            boolean f19,
            boolean f20) {

        // Sanity check inputs
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" ").append(address);

        int byte2 = 0;
        byte2 += (f13 ? 1 : 0);
        byte2 += (f14 ? 2 : 0);
        byte2 += (f15 ? 4 : 0);
        byte2 += (f16 ? 8 : 0);
        byte2 += (f17 ? 16 : 0);
        byte2 += (f18 ? 32 : 0);
        byte2 += (f19 ? 64 : 0);
        byte2 += (f20 ? 128 : 0);

        m.myMessage.append(" ").append(DCCppConstants.FUNCTION_GROUP4_BYTE1);
        m.myMessage.append(" ").append(byte2);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    /**
     * Generate a Function Group Five Operation Request message.
     *
     * @param address is the locomotive address
     * @param f21     is true if f21 is on, false if f21 is off
     * @param f22     is true if f22 is on, false if f22 is off
     * @param f23     is true if f23 is on, false if f23 is off
     * @param f24     is true if f24 is on, false if f24 is off
     * @param f25     is true if f25 is on, false if f25 is off
     * @param f26     is true if f26 is on, false if f26 is off
     * @param f27     is true if f27 is on, false if f27 is off
     * @param f28     is true if f28 is on, false if f28 is off
     * @return message to set function group 5.
     */
    public static DCCppMessage makeFunctionGroup5OpsMsg(int address,
            boolean f21,
            boolean f22,
            boolean f23,
            boolean f24,
            boolean f25,
            boolean f26,
            boolean f27,
            boolean f28) {
        // Sanity check inputs
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" ").append(address);

        int byte2 = 0;
        byte2 += (f21 ? 1 : 0);
        byte2 += (f22 ? 2 : 0);
        byte2 += (f23 ? 4 : 0);
        byte2 += (f24 ? 8 : 0);
        byte2 += (f25 ? 16 : 0);
        byte2 += (f26 ? 32 : 0);
        byte2 += (f27 ? 64 : 0);
        byte2 += (f28 ? 128 : 0);
        log.debug("DCCppMessage: Byte2 = {}", byte2);

        m.myMessage.append(" ").append(DCCppConstants.FUNCTION_GROUP5_BYTE1);
        m.myMessage.append(" ").append(byte2);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    /**
     * Generate a Function Group Five Set Momentary Function message.
     *
     * @param address is the locomotive address
     * @param f21     is true if f21 is momentary
     * @param f22     is true if f22 is momentary
     * @param f23     is true if f23 is momentary
     * @param f24     is true if f24 is momentary
     * @param f25     is true if f25 is momentary
     * @param f26     is true if f26 is momentary
     * @param f27     is true if f27 is momentary
     * @param f28     is true if f28 is momentary
     * @return message to set momentary function group 5.
     */
    public static DCCppMessage makeFunctionGroup5SetMomMsg(int address,
            boolean f21,
            boolean f22,
            boolean f23,
            boolean f24,
            boolean f25,
            boolean f26,
            boolean f27,
            boolean f28) {

        // Sanity check inputs
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" ").append(address);

        int byte2 = 0;
        byte2 += (f21 ? 1 : 0);
        byte2 += (f22 ? 2 : 0);
        byte2 += (f23 ? 4 : 0);
        byte2 += (f24 ? 8 : 0);
        byte2 += (f25 ? 16 : 0);
        byte2 += (f26 ? 32 : 0);
        byte2 += (f27 ? 64 : 0);
        byte2 += (f28 ? 128 : 0);

        m.myMessage.append(" ").append(DCCppConstants.FUNCTION_GROUP5_BYTE1);
        m.myMessage.append(" ").append(byte2);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;

        m._nDataChars = m.toString().length();
        return (m);
    }

    /*
     * Build an Emergency Off Message
     */

    /*
     * Test Code Functions... not for normal use
     */

    /**
     * Write DCC Packet to a specified Register on the Main.
     * <br>
     * DCC++ BaseStation code appends its own error-correction byte so we must
     * not provide one.
     *
     * @param register the DCC++ BaseStation main register number to use
     * @param numBytes the number of bytes in the packet
     * @param bytes    byte array representing the packet. The first
     *                 {@code num_bytes} are used.
     * @return the formatted message to send
     */
    public static DCCppMessage makeWriteDCCPacketMainMsg(int register, int numBytes, byte[] bytes) {
        // Sanity Check Inputs
        if (register < 0 || register > DCCppConstants.MAX_MAIN_REGISTERS || numBytes < 2 || numBytes > 5) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.WRITE_DCC_PACKET_MAIN);
        m.myMessage.append(" ").append(register);
        for (int k = 0; k < numBytes; k++) {
            m.myMessage.append(" ").append(jmri.util.StringUtil.twoHexFromInt(bytes[k]));
        }
        m.myRegex = DCCppConstants.WRITE_DCC_PACKET_MAIN_REGEX;
        return (m);

    }

    /**
     * Write DCC Packet to a specified Register on the Programming Track.
     * <br><br>
     * DCC++ BaseStation code appends its own error-correction byte so we must
     * not provide one.
     *
     * @param register the DCC++ BaseStation main register number to use
     * @param numBytes the number of bytes in the packet
     * @param bytes    byte array representing the packet. The first
     *                 {@code num_bytes} are used.
     * @return the formatted message to send
     */
    public static DCCppMessage makeWriteDCCPacketProgMsg(int register, int numBytes, byte[] bytes) {
        // Sanity Check Inputs
        if (register < 0 || register > DCCppConstants.MAX_MAIN_REGISTERS || numBytes < 2 || numBytes > 5) {
            return (null);
        }

        DCCppMessage m = new DCCppMessage(DCCppConstants.WRITE_DCC_PACKET_PROG);
        m.myMessage.append(" ").append(register);
        for (int k = 0; k < numBytes; k++) {
            m.myMessage.append(" ").append(jmri.util.StringUtil.twoHexFromInt(bytes[k]));
        }
        m.myRegex = DCCppConstants.WRITE_DCC_PACKET_PROG_REGEX;
        return (m);

    }

//    public static DCCppMessage makeCheckFreeMemMsg() {
//        return (new DCCppMessage(DCCppConstants.GET_FREE_MEMORY, DCCppConstants.GET_FREE_MEMORY_REGEX));
//    }
//
    public static DCCppMessage makeListRegisterContentsMsg() {
        return (new DCCppMessage(DCCppConstants.LIST_REGISTER_CONTENTS,
                DCCppConstants.LIST_REGISTER_CONTENTS_REGEX));
    }

    /**
     * This implementation of equals is targeted to the background function
     * refreshing in SerialDCCppPacketizer. To keep only one function group in
     * the refresh queue the logic is as follows. Two messages are equal if they
     * are:
     * <ul>
     * <li>actually identical, or</li>
     * <li>a function call to the same address and same function group</li>
     * </ul>
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof DCCppMessage)) {
            return false;
        }

        final DCCppMessage other = (DCCppMessage) obj;

        final String myCmd = this.toString();
        final String otherCmd = other.toString();

        if (myCmd.equals(otherCmd)) {
            return true;
        }

        if (!(myCmd.charAt(0) == DCCppConstants.FUNCTION_CMD) || !(otherCmd.charAt(0) == DCCppConstants.FUNCTION_CMD)) {
            return false;
        }

        final int mySpace1 = myCmd.indexOf(' ', 2);
        final int otherSpace1 = otherCmd.indexOf(' ', 2);

        if (mySpace1 != otherSpace1) {
            return false;
        }

        if (!myCmd.subSequence(2, mySpace1).equals(otherCmd.subSequence(2, otherSpace1))) {
            return false;
        }

        int mySpace2 = myCmd.indexOf(' ', mySpace1 + 1);
        if (mySpace2 < 0) {
            mySpace2 = myCmd.length();
        }

        int otherSpace2 = otherCmd.indexOf(' ', otherSpace1 + 1);
        if (otherSpace2 < 0) {
            otherSpace2 = otherCmd.length();
        }

        final int myBaseFunction = Integer.parseInt(myCmd.substring(mySpace1 + 1, mySpace2));
        final int otherBaseFunction = Integer.parseInt(otherCmd.substring(otherSpace1 + 1, otherSpace2));

        if (myBaseFunction == otherBaseFunction) {
            return true;
        }

        return getFuncBaseByte1(myBaseFunction) == getFuncBaseByte1(otherBaseFunction);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Get the function group from the first byte of the function setting call.
     *
     * @param byte1 first byte (mixed in with function bits for groups 1 to 3,
     *              or standalone value for groups 4 and 5)
     * @return the base group
     */
    private static int getFuncBaseByte1(final int byte1) {
        if (byte1 == DCCppConstants.FUNCTION_GROUP4_BYTE1 || byte1 == DCCppConstants.FUNCTION_GROUP5_BYTE1) {
            return byte1;
        }

        if (byte1 < 160) {
            return 128;
        }

        if (byte1 < 176) {
            return 160;
        }

        return 176;
    }

    /**
     * When is this message supposed to be resent?
     */
    private long expireTime;

    /**
     * Before adding the message to the delay queue call this method to set when
     * the message should be repeated. The only time guarantee is that it will
     * be repeated after <u>at least</u> this much time, but it can be
     * significantly longer until it is repeated, function of the message queue
     * length.
     *
     * @param millis milliseconds in the future
     */
    public void delayFor(final long millis) {
        expireTime = System.currentTimeMillis() + millis;
    }

    /**
     * Comparing two queued message for refreshing the function calls, based on
     * their expected execution time.
     */
    @Override
    public int compareTo(@Nonnull final Delayed o) {
        final long diff = this.expireTime - ((DCCppMessage) o).expireTime;

        if (diff < 0) {
            return -1;
        }

        if (diff > 0) {
            return 1;
        }

        return 0;
    }

    /**
     * From the {@link Delayed} interface, how long this message still has until
     * it should be executed.
     */
    @Override
    public long getDelay(final TimeUnit unit) {
        return unit.convert(expireTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    // initialize logging
    private static final Logger log = LoggerFactory.getLogger(DCCppMessage.class);

}
