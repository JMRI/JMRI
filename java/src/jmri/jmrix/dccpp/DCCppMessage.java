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
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2003-2010
 * @author Mark Underwood Copyright (C) 2015
 *
 * Based on XNetMessage by Bob Jacobsen and Paul Bender
 */

/*
 * A few words on implementation
 *
 * DCCppMessage objects are (usually) created by calling one of the static makeMessageType()
 * methods, and are then consumed by the TrafficController/Packetizer by being converted to
 * a String and sent out the port.
 *
 * Internally the DCCppMessage is actually stored as a String, and alongside that is kept
 * a Regex for easy extraction of the values where needed in the code.
 *
 * The various getParameter() type functions are mainly for convenience in places such as the
 * port monitor where we want to be able to extract the /meaning/ of the DCCppMessage and
 * present it in a human readable form.  Using the getParameterType() methods insulates
 * the higher level code from needing to know what order/format the actual message is
 * in.
 */
public class DCCppMessage extends jmri.jmrix.AbstractMRMessage {

    static private int _nRetries = 5;

    /* According to the specification, DCC++ has a maximum timing 
     interval of 500 milliseconds durring normal communications */
    // TODO: Note this timing interval is actually an XpressNet thing...
    // Need to find out what DCC++'s equivalent is.
    static protected final int DCCppProgrammingTimeout = 10000;  // TODO: Appropriate value for DCC++?
    static private int DCCppMessageTimeout = 5000;  // TODO: Appropriate value for DCC++?
    
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
            log.error("Invalid length in ctor: " + len);
        }
        _nDataChars = len;
        myRegex = "";
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
    }

    /**
     * Create an DCCppMessage from an DCCppReply.
     */
    // NOTE: Not used.  Really, not even possible.  Consider removing.
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
     * 
     * NOTE 15-Feb-17: un-Deprecating this function so that it can be used
     * in the DCCppOverTCP server/client interface.  Messages shouldn't
     * be parsed, they are already in DCC++ format, so we need the string
     * constructor to generate a DCCppMessage from the incoming byte stream
     */
    //@Deprecated
    public DCCppMessage(String s) {
        setBinary(false);
        setRetries(_nRetries);
        setTimeout(DCCppMessageTimeout);
        myMessage = new StringBuilder(s); // yes, copy... or... maybe not.
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

    /**
     * Parse a string and generate/return a DCCppMessage object
     * 
     * @param s String of DCC++ message without the {@literal < >} brackets
     * @return DCCppMessage
     */
    
    public static DCCppMessage parseDCCppMessage(String s) {
        // Need to parse the string and construct a message from it.
        Matcher m;
        switch(s.charAt(0)) {
            case DCCppConstants.ACCESSORY_CMD:
                if ((m = match(s, DCCppConstants.ACCESSORY_CMD_REGEX, "ctor")) != null) {
                    int addr = Integer.parseInt(m.group(1));
                    int sub = Integer.parseInt(m.group(2));
                    int v = (m.group(3).equals("0") ? 0 : 1);
                    return(DCCppMessage.makeAccessoryDecoderMsg(addr, sub, (v == 1)));
                } else {
                    return(null);
                }
            case DCCppConstants.CLEAR_EEPROM_CMD:
                return(new DCCppMessage(DCCppConstants.CLEAR_EEPROM_CMD, DCCppConstants.CLEAR_EEPROM_REGEX));
            case DCCppConstants.FUNCTION_CMD:
                break;
            case DCCppConstants.GET_FREE_MEMORY:
                return(new DCCppMessage(DCCppConstants.GET_FREE_MEMORY, DCCppConstants.GET_FREE_MEMORY_REGEX));
            case DCCppConstants.LIST_REGISTER_CONTENTS:
                return(new DCCppMessage(DCCppConstants.LIST_REGISTER_CONTENTS, DCCppConstants.LIST_REGISTER_CONTENTS_REGEX));
            case DCCppConstants.OPS_WRITE_CV_BIT:
                if ((m = match(s, DCCppConstants.OPS_WRITE_BIT_REGEX, "ctor")) != null) {
                    int addr = Integer.parseInt(m.group(1));
                    int cv = Integer.parseInt(m.group(2));
                    int bit = Integer.parseInt(m.group(3));
                    int val = (m.group(4).equals("0") ? 0 : 1);
                    return(DCCppMessage.makeBitWriteOpsModeCVMsg(addr, cv, bit, val));
                } else {
                    return(null);
                }
            case DCCppConstants.OPS_WRITE_CV_BYTE:
                if ((m = match(s, DCCppConstants.OPS_WRITE_BYTE_REGEX, "ctor")) != null) {
                    int addr = Integer.parseInt(m.group(1));
                    int cv = Integer.parseInt(m.group(2));
                    int val = Integer.parseInt(m.group(3));
                    return(DCCppMessage.makeWriteOpsModeCVMsg(addr, cv, val));
                } else {
                    return(null);
                }
            case DCCppConstants.PROG_READ_CV:
                if ((m = match(s, DCCppConstants.PROG_READ_REGEX, "ctor")) != null) {
                    int cv = Integer.parseInt(m.group(1));
                    int cb = Integer.parseInt(m.group(2));
                    int cs = Integer.parseInt(m.group(3));
                    return(DCCppMessage.makeReadDirectCVMsg(cv, cb, cs));
                } else {
                    return(null);
                }
            case DCCppConstants.PROG_WRITE_CV_BIT:
                if ((m = match(s, DCCppConstants.PROG_WRITE_BIT_REGEX, "ctor")) != null) {
                    int cv = Integer.parseInt(m.group(1));
                    int bit = Integer.parseInt(m.group(2));
                    int val = (m.group(3).equals("0") ? 0 : 1);
                    int addr = Integer.parseInt(m.group(4));
                    int sub = Integer.parseInt(m.group(5));
                    return(DCCppMessage.makeBitWriteDirectCVMsg(cv, bit, val, addr, sub));
                } else {
                    return(null);
                }
            case DCCppConstants.PROG_WRITE_CV_BYTE:
                if ((m = match(s, DCCppConstants.PROG_WRITE_BYTE_REGEX, "ctor")) != null) {
                    int cv = Integer.parseInt(m.group(1));
                    int val = Integer.parseInt(m.group(2));
                    int addr = Integer.parseInt(m.group(3));
                    int sub = Integer.parseInt(m.group(4));
                    return(DCCppMessage.makeWriteDirectCVMsg(cv, val, addr, sub));
                } else {
                    return(null);
                }
            case DCCppConstants.READ_CS_STATUS:
                return(new DCCppMessage(DCCppConstants.READ_CS_STATUS, DCCppConstants.READ_CS_STATUS_REGEX));
            case DCCppConstants.READ_TRACK_CURRENT:
                return(DCCppMessage.makeReadTrackCurrentMsg());
            case DCCppConstants.SENSOR_CMD:
                if ((m = match(s, DCCppConstants.SENSOR_ADD_REGEX, "ctor")) != null) {
                    int id = Integer.parseInt(m.group(1));
                    int pin = Integer.parseInt(m.group(2));
                    int pullup = (m.group(4).equals("0") ? 0 : 1);
                    return(DCCppMessage.makeSensorAddMsg(id, pin, pullup));
                } else if ((m = match(s, DCCppConstants.SENSOR_DELETE_REGEX, "ctor")) != null) {
                    int id = Integer.parseInt(m.group(1));
                    return(DCCppMessage.makeSensorDeleteMsg(id));
                } else if ((match(s, DCCppConstants.SENSOR_LIST_REGEX, "ctor")) != null) {
                    return(new DCCppMessage(DCCppConstants.SENSOR_CMD, DCCppConstants.SENSOR_LIST_REGEX));
                } else {
                    return(null);
                }
            case DCCppConstants.THROTTLE_CMD:
                if ((m = match(s, DCCppConstants.THROTTLE_CMD_REGEX, "ctor")) != null) {
                    int reg = Integer.parseInt(m.group(1));
                    int addr = Integer.parseInt(m.group(2));
                    float speed = Float.parseFloat(m.group(3)); // Note: gets converted to int inside makeSpeedAndDirectionMsg()
                    int fwd = (m.group(4).equals("0") ? 0 : 1);
                    return(DCCppMessage.makeSpeedAndDirectionMsg(reg, addr, speed, (fwd == 1)));
                } else {
                    return(null);
                }
            case DCCppConstants.TRACK_POWER_OFF:
                return(DCCppMessage.makeTrackPowerOffMsg());
            case DCCppConstants.TRACK_POWER_ON:
                return(DCCppMessage.makeTrackPowerOnMsg());
            case DCCppConstants.TURNOUT_CMD:
                if ((m = match(s, DCCppConstants.TURNOUT_ADD_REGEX, "ctor")) != null) {
                    int id = Integer.parseInt(m.group(1));
                    int addr = Integer.parseInt(m.group(2));
                    int sub = Integer.parseInt(m.group(3));
                    return(DCCppMessage.makeTurnoutAddMsg(id, addr, sub));
                } else if ((m = match(s, DCCppConstants.TURNOUT_DELETE_REGEX, "ctor")) != null) {
                    int id = Integer.parseInt(m.group(1));
                    return(DCCppMessage.makeTurnoutDeleteMsg(id));
                } else if ((match(s, DCCppConstants.TURNOUT_LIST_REGEX, "ctor")) != null) {
                    return(new DCCppMessage(DCCppConstants.TURNOUT_CMD, DCCppConstants.TURNOUT_LIST_REGEX));
                } else if ((m = match(s, DCCppConstants.TURNOUT_CMD_REGEX, "ctor")) != null) {
                    int id = Integer.parseInt(m.group(1));
                    int thrown = m.group(2).equals("0") ? 0 : 1;
                    return(DCCppMessage.makeTurnoutCommandMsg(id, (thrown == 1)));
                }else {
                    return(null);
                }
            case DCCppConstants.OUTPUT_CMD:
                if ((m = match(s, DCCppConstants.OUTPUT_CMD_REGEX, "ctor")) != null) {
                    int id = Integer.parseInt(m.group(1));
                    int state = m.group(2).equals("0") ? 0 : 1;
                    return(DCCppMessage.makeOutputCmdMsg(id, (state == 1)));
                } else if ((m = match(s, DCCppConstants.OUTPUT_ADD_REGEX, "ctor")) != null) {
                    int id = Integer.parseInt(m.group(1));
                    int pin = Integer.parseInt(m.group(2));
                    int iflag = Integer.parseInt(m.group(3));
                    return(DCCppMessage.makeOutputAddMsg(id, pin, iflag));
                } else if ((m = match(s, DCCppConstants.OUTPUT_DELETE_REGEX, "ctor")) != null) {
                    int id = Integer.parseInt(m.group(1));
                    return(DCCppMessage.makeOutputDeleteMsg(id));
                } else if ((m = match(s, DCCppConstants.OUTPUT_LIST_REGEX, "ctor")) != null) {
                    return(DCCppMessage.makeOutputListMsg());
                } else {
                    return(null);
                }
            case DCCppConstants.WRITE_DCC_PACKET_MAIN:
                break;
            case DCCppConstants.WRITE_DCC_PACKET_PROG:
                break;
            case DCCppConstants.WRITE_TO_EEPROM_CMD:
                return(new DCCppMessage(DCCppConstants.WRITE_TO_EEPROM_CMD, DCCppConstants.WRITE_TO_EEPROM_REGEX));
            case DCCppConstants.QUERY_SENSOR_STATES_CMD:
                return(new DCCppMessage(DCCppConstants.QUERY_SENSOR_STATES_CMD, DCCppConstants.QUERY_SENSOR_STATES_REGEX));
            default:
                return(null);
        }
        return(null);
    }
    
    private void setRegex() {
        switch(myMessage.charAt(0)) {
            case DCCppConstants.THROTTLE_CMD:
                myRegex = DCCppConstants.THROTTLE_CMD_REGEX; break;
            case DCCppConstants.FUNCTION_CMD:
                myRegex = DCCppConstants.FUNCTION_CMD_REGEX; break;
            case DCCppConstants.ACCESSORY_CMD:
                myRegex = DCCppConstants.ACCESSORY_CMD_REGEX; break;
            case DCCppConstants.TURNOUT_CMD:
                if ((match(myMessage.toString(), DCCppConstants.TURNOUT_ADD_REGEX, "ctor")) != null) {
                myRegex = DCCppConstants.TURNOUT_ADD_REGEX;
                } else if ((match(myMessage.toString(), DCCppConstants.TURNOUT_DELETE_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.TURNOUT_DELETE_REGEX;
                } else if ((match(myMessage.toString(), DCCppConstants.TURNOUT_LIST_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.TURNOUT_LIST_REGEX;
                } else if ((match(myMessage.toString(), DCCppConstants.TURNOUT_CMD_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.TURNOUT_CMD_REGEX;
                } else {
                    myRegex = "";
                }
                break;
            case DCCppConstants.SENSOR_CMD:
                if ((match(myMessage.toString(), DCCppConstants.SENSOR_ADD_REGEX, "ctor")) != null) {
                myRegex = DCCppConstants.SENSOR_ADD_REGEX;
                } else if ((match(myMessage.toString(), DCCppConstants.SENSOR_DELETE_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.SENSOR_DELETE_REGEX;
                } else if ((match(myMessage.toString(), DCCppConstants.SENSOR_LIST_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.SENSOR_LIST_REGEX;
                } else {
                    myRegex = "";
                }
                break;
            case DCCppConstants.OUTPUT_CMD:
                if ((match(myMessage.toString(), DCCppConstants.OUTPUT_ADD_REGEX, "ctor")) != null) {
                myRegex = DCCppConstants.OUTPUT_ADD_REGEX;
                } else if ((match(myMessage.toString(), DCCppConstants.OUTPUT_DELETE_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.OUTPUT_DELETE_REGEX;
                } else if ((match(myMessage.toString(), DCCppConstants.OUTPUT_LIST_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.OUTPUT_LIST_REGEX;
                } else if ((match(myMessage.toString(), DCCppConstants.OUTPUT_CMD_REGEX, "ctor")) != null) {
                    myRegex = DCCppConstants.OUTPUT_CMD_REGEX;
                } else {
                    myRegex = "";
                }
                break;
            case DCCppConstants.OPS_WRITE_CV_BYTE:
                myRegex = DCCppConstants.OPS_WRITE_BYTE_REGEX; break;
            case DCCppConstants.OPS_WRITE_CV_BIT:
                myRegex = DCCppConstants.OPS_WRITE_BIT_REGEX; break;
            case DCCppConstants.PROG_WRITE_CV_BYTE:
                myRegex = DCCppConstants.PROG_WRITE_BYTE_REGEX; break;
            case DCCppConstants.PROG_WRITE_CV_BIT:
                myRegex = DCCppConstants.PROG_WRITE_BIT_REGEX; break;
            case DCCppConstants.PROG_READ_CV:
                myRegex = DCCppConstants.PROG_READ_REGEX; break;
            case DCCppConstants.TRACK_POWER_ON:
            case DCCppConstants.TRACK_POWER_OFF:
                myRegex = DCCppConstants.TRACK_POWER_REGEX; break;
            case DCCppConstants.READ_TRACK_CURRENT:
                myRegex = DCCppConstants.READ_TRACK_CURRENT_REGEX; break;
            case DCCppConstants.READ_CS_STATUS:
                myRegex = DCCppConstants.READ_CS_STATUS_REGEX; break;
            case DCCppConstants.WRITE_TO_EEPROM_CMD:
                myRegex = DCCppConstants.WRITE_TO_EEPROM_REGEX; break;
            case DCCppConstants.CLEAR_EEPROM_CMD:
                myRegex = DCCppConstants.CLEAR_EEPROM_REGEX; break;
            case DCCppConstants.QUERY_SENSOR_STATES_CMD:
                myRegex = DCCppConstants.QUERY_SENSOR_STATES_REGEX; break;
            case DCCppConstants.WRITE_DCC_PACKET_MAIN:
                myRegex = DCCppConstants.WRITE_DCC_PACKET_MAIN_REGEX; break;
            case DCCppConstants.WRITE_DCC_PACKET_PROG:
                myRegex = DCCppConstants.WRITE_DCC_PACKET_PROG_REGEX; break;
            case DCCppConstants.GET_FREE_MEMORY:
                myRegex = DCCppConstants.GET_FREE_MEMORY_REGEX; break;
            case DCCppConstants.LIST_REGISTER_CONTENTS:
                myRegex = DCCppConstants.LIST_REGISTER_CONTENTS_REGEX; break;
            case DCCppConstants.ENTER_DIAG_MODE_CMD:
                myRegex = DCCppConstants.ENTER_DIAG_MODE_REGEX; break;
            default:
                myRegex = "";
        }
    }
    
    /**
     * toString() converts DCCppMessage to String format
     * (without the {@code <>} brackets)
     * 
     * @return String form of message.
     */
    @Override
    public String toString() {
        return(myMessage.toString());
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
   public String toMonitorString(){
        // Beautify and display
        String text;

        switch (getOpCodeChar()) {
            case DCCppConstants.THROTTLE_CMD:
                text = "Throttle Cmd: ";
                text += "\n\tRegister: " + getRegisterString();
                text += "\n\tAddress: " + getAddressString();
                text += "\n\tSpeed: " + getSpeedString();
                text += "\n\t:Direction: " + getDirectionString();
                break;
            case DCCppConstants.FUNCTION_CMD:
                text = "Function Cmd: ";
                text += "\n\tAddress: " + getFuncAddressString();
                text += "\n\tByte 1: " + getFuncByte1String();
                text += "\n\tByte 2: " + getFuncByte2String();
                text += "\n\t(No Reply Expected)";
                break;
            case DCCppConstants.ACCESSORY_CMD:
                text = "Accessory Decoder Cmd: ";
                text += "\n\tAddress: " + getAccessoryAddrString();
                text += "\n\tSubaddr: " + getAccessorySubString();
                text += "\n\tState: " + getAccessoryStateString();
                break;
            case DCCppConstants.TURNOUT_CMD:
                if (isTurnoutAddMessage()) {
                    text = "Add Turnout: ";
                    text += "\n\tT/O ID: " + getTOIDString();
                    text += "\n\tAddress: " + getTOAddressString();
                    text += "\n\tSubaddr: " + getTOSubAddressString();
                } else if (isTurnoutDeleteMessage()) {
                    text = "Delete Turnout: ";
                    text += "\n\tT/O ID: " + getTOIDString();
                } else if (isListTurnoutsMessage()) {
                    text = "List Turnouts...";
                } else {
                    text = "Turnout Cmd: ";
                    text += "\n\tT/O ID: " + getTOIDString();
                    text += "\n\tState: " + getTOStateString();
                }
                break;
            case DCCppConstants.OUTPUT_CMD:
                if (isOutputCmdMessage()) {
                    text = "Output Cmd: ";
                    text += "\n\tOutput ID: " + getOutputIDString();
                    text += "\n\tState: " + getOutputStateString();
                } else if (isOutputAddMessage()) {
                    text = "Add Output: ";
                    text += "\n\tOutput ID: " + getOutputIDString();
                    text += "\n\tPin: " + getOutputPinString();
                    text += "\n\tIFlag: " + getOutputIFlagString();
                } else if (isOutputDeleteMessage()) {
                    text = "Delete Output: ";
                    text += "\n\tOutput ID: " + getOutputIDString();
                } else if (isListOutputsMessage()) {
                    text = "List Outputs...";
                } else {
                    text = "Invalid Output Command: " + toString();
                }
                break;
            case DCCppConstants.SENSOR_CMD:
                if (isSensorAddMessage()) {
                    text = "Add Sensor: ";
                    text += "\n\tSensor ID: " + getSensorIDString();
                    text += "\n\tPin: " + getSensorPinString();
                    text += "\n\tPullup: " + getSensorPullupString();
                } else if (isSensorDeleteMessage()) {
                    text = "Delete Sensor: ";
                    text += "\n\tSensor ID: " + getSensorIDString();
                } else if (isListSensorsMessage()) {
                    text = "List Sensors...";
                } else {
                    text = "Unknown Sensor Cmd...";
                }
                break;
            case DCCppConstants.OPS_WRITE_CV_BYTE:
                text = "Ops Write Byte Cmd: \n"; // <w cab cv val>
                text += "\tAddress: " + getOpsWriteAddrString() + "\n";
                text += "\tCV: " + getOpsWriteCVString() + "\n";
                text += "\tValue: " + getOpsWriteValueString();
                break;
            case DCCppConstants.OPS_WRITE_CV_BIT: // <b cab cv bit val>
                text = "Ops Write Bit Cmd: \n";
                text += "\tAddress: " + getOpsWriteAddrString() + "\n";
                text += "\tCV: " + getOpsWriteCVString() + "\n";
                text += "\tBit: " + getOpsWriteBitString() + "\n";
                text += "\tValue: " + getOpsWriteValueString();
                break;
            case DCCppConstants.PROG_WRITE_CV_BYTE:
                text = "Prog Write Byte Cmd: ";
                text += "\n\tCV : " + getCVString();
                text += "\n\tValue: " + getProgValueString();
                text += "\n\tCallback Num: " + getCallbackNumString();
                text += "\n\tCallback Sub: " + getCallbackSubString();
                break;

            case DCCppConstants.PROG_WRITE_CV_BIT:
                text = "Prog Write Bit Cmd: ";
                text += "\n\tCV : " + getCVString();
                text += "\n\tBit : " + getBitString();
                text += "\n\tValue: " + getProgValueString();
                text += "\n\tCallback Num: " + getCallbackNumString();
                text += "\n\tCallback Sub: " + getCallbackSubString();
                break;
            case DCCppConstants.PROG_READ_CV:
                text = "Prog Read Cmd: ";
                text += "\n\tCV: " + getCVString();
                text += "\n\tCallback Num: " + getCallbackNumString();
                text += "\n\tCallback Sub: " + getCallbackSubString();
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
            case DCCppConstants.WRITE_DCC_PACKET_MAIN:
                text = "Write DCC Packet Main Cmd: ";
                text += "\n\tRegister: " + getRegisterString();
                text += "\n\tPacket:" + getPacketString();
                break;
            case DCCppConstants.WRITE_DCC_PACKET_PROG:
                text = "Write DCC Packet Prog Cmd: ";
                text += "\n\tRegister: " + getRegisterString();
                text += "\n\tPacket:" + getPacketString();
                break;
            case DCCppConstants.GET_FREE_MEMORY:
                text = "Get Free Memory Cmd: ";
                text += toString();
                break;
            case DCCppConstants.LIST_REGISTER_CONTENTS:
                text = "List Register Contents Cmd: ";
                text += toString();
                break;
            default:
                text = "Unknown Message: " +toString();
        }

        return text;
   } 

 
    @Override
    public int getNumDataElements() {
        return(myMessage.length());
//        return(_nDataChars);
    }
    
    @Override
    public int getElement(int n) {
        return(this.myMessage.charAt(n));
    }
    
    @Override
    public void setElement(int n, int v) {
        // We want the ASCII value, not the string interpretation of the int
        char c = (char)(v & 0xFF);
        if (n >= myMessage.length()) {
            myMessage.append(c);
        } else if (n > 0) {
            myMessage.setCharAt(n,c);
        }
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
            log.error("Opcode invalid: " + i);
        }
        opcode = (char) (i & 0xFF);
        myMessage.setCharAt(0, opcode);
    }

    @Override
    public int getOpCode() {
        return(opcode & 0xFF);
    }

    public char getOpCodeChar() {
        //return(opcode);
        return(myMessage.charAt(0));
    }

    @Deprecated
    public String getOpCodeString() {
        return(Character.toString(opcode));
    }
   
    private int getGroupCount(){
        Matcher m = match(myMessage.toString(), myRegex, "gvs");
        return m.groupCount();
    }
 
    public String getValueString(int idx) {
        Matcher m = match(myMessage.toString(), myRegex, "gvs");
        if (m == null) {
            log.error("No match!");
            return("");
        } else if (idx <= m.groupCount()) {
            return(m.group(idx));
        } else {
            log.error("DCCppMessage value index too big. idx = {} msg = {}", idx, this.toString());
            return("");
        }
    }
    
    public int getValueInt(int idx) {
        Matcher m = match(myMessage.toString(), myRegex, "gvi");
        if (m == null) {
            log.error("No match!");
            return(0);
        } else if (idx <= m.groupCount()) {
            return(Integer.parseInt(m.group(idx)));
        } else {
            log.error("DCCppMessage value index too big. idx = {} msg = {}", idx, this.toString());
            return(0);
        }
    }
    
    public boolean getValueBool(int idx) {
        log.debug("msg = {}, regex = {}", myMessage.toString(), myRegex);
        Matcher m = match(myMessage.toString(), myRegex, "gvi");
        
        if (m == null) {
            log.error("No Match!");
            return(false);
        } else if (idx <= m.groupCount()) {
            return(!m.group(idx).equals("0"));
        } else {
            log.error("DCCppMessage value index too big. idx = {} msg = {}", idx, this.toString());
            return(false);
        }
    }

    /**
     * return the message length
     */
    public int length() {
        return(myMessage.length());
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

    /**
     * Returns true if this DCCppMessage is properly formatted
     * (or will generate a properly formatted command when
     * converted to String)
     * 
     * @return boolean true/false 
     */
    public boolean isValidMessageFormat() {
        if (this.match(this.myRegex) != null) {
     return(true);
 } else {
     return(false);
 }
    }

    /**
     * matches this DCCppMessage against the given regex 'pat'
     * 
     * @param pat Regex
     * @return Matcher or null if no match.
     */
    private Matcher match(String pat) {
 return(match(this.toString(), pat, "Validator"));
    }

    /**
     * matches the given string against the given Regex pattern.
     * 
     * @param s    string to be matched
     * @param pat  Regex string to match against
     * @param name Text name to use in debug messages.
     * @return Matcher or null if no match
     */
    private static Matcher match(String s, String pat, String name) {
 try {
     Pattern p = Pattern.compile(pat);
     Matcher m = p.matcher(s);
     if (!m.matches()) {
  //log.warn("No Match {} Command: {} Pattern: {}",name, s, pat);
  return(null);
     }
     return(m);

 } catch (PatternSyntaxException e) {
            log.error("Malformed DCC++ message syntax! s = ", pat);
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

    public boolean isSensorMessage() { return(this.getOpCodeChar() == DCCppConstants.SENSOR_CMD); }

    public boolean isEEPROMWriteMessage() { return(this.getOpCodeChar() == DCCppConstants.WRITE_TO_EEPROM_CMD); }

    public boolean isEEPROMClearMessage() { return(this.getOpCodeChar() == DCCppConstants.CLEAR_EEPROM_CMD); }

    public boolean isOpsWriteByteMessage() { return(this.getOpCodeChar() == DCCppConstants.OPS_WRITE_CV_BYTE); }

    public boolean isOpsWriteBitMessage() { return(this.getOpCodeChar() == DCCppConstants.OPS_WRITE_CV_BIT); }

    public boolean isProgWriteByteMessage() { return(this.getOpCodeChar() == DCCppConstants.PROG_WRITE_CV_BYTE); }

    public boolean isProgWriteBitMessage() { return(this.getOpCodeChar() == DCCppConstants.PROG_WRITE_CV_BIT); }

    public boolean isProgReadMessage() { return(this.getOpCodeChar() == DCCppConstants.PROG_READ_CV); }

    //public boolean isQuerySensorMessage() { return(this.getOpCodeChar() == DCCppConstants.QUERY_SENSOR_STATE); }

    public boolean isTurnoutCmdMessage() { return(this.match(DCCppConstants.TURNOUT_CMD_REGEX) != null); }

    public boolean isTurnoutAddMessage() { return(this.match(DCCppConstants.TURNOUT_ADD_REGEX) != null); }

    public boolean isTurnoutDeleteMessage() { return(this.match(DCCppConstants.TURNOUT_DELETE_REGEX) != null); }

    public boolean isListTurnoutsMessage() { return(this.match(DCCppConstants.TURNOUT_LIST_REGEX) != null); }

    public boolean isSensorAddMessage() { return(this.match(DCCppConstants.SENSOR_ADD_REGEX) != null); }

    public boolean isSensorDeleteMessage() { return(this.match(DCCppConstants.SENSOR_DELETE_REGEX) != null); }

    public boolean isListSensorsMessage() { return(this.match(DCCppConstants.SENSOR_LIST_REGEX) != null); }

    //public boolean isOutputCmdMessage() { return(this.getOpCodeChar() == DCCppConstants.OUTPUT_CMD); }

    public boolean isOutputCmdMessage() { return(this.match(DCCppConstants.OUTPUT_CMD_REGEX) != null); }

    public boolean isOutputAddMessage() { return(this.match(DCCppConstants.OUTPUT_ADD_REGEX) != null); }

    public boolean isOutputDeleteMessage() { return(this.match(DCCppConstants.OUTPUT_DELETE_REGEX) != null); }

    public boolean isListOutputsMessage() { return(this.match(DCCppConstants.OUTPUT_LIST_REGEX) != null); }

    public boolean isQuerySensorStatesMessage() { return(this.match(DCCppConstants.QUERY_SENSOR_STATES_REGEX) != null); }

    public boolean isWriteDccPacketMessage() { return ((this.getOpCodeChar() == DCCppConstants.WRITE_DCC_PACKET_MAIN) || (this.getOpCodeChar() == DCCppConstants.WRITE_DCC_PACKET_PROG)); }

    //------------------------------------------------------
    // Helper methods for Sensor Query Commands

    public String getOutputIDString() {
 if (this.isOutputAddMessage() || this.isOutputDeleteMessage() || this.isOutputCmdMessage()) {
            return getValueString(1);
 } else { 
     log.error("Output Parser called on non-Output message type {}", this.getOpCodeChar());
     return("0");
        }
    }

    public int getOutputIDInt() {
        if (this.isOutputAddMessage() || this.isOutputDeleteMessage() || this.isOutputCmdMessage()) {
        return(getValueInt(1)); // assumes stored as an int!
        } else {
     log.error("Output Parser called on non-Output message type {}", this.getOpCodeChar());
     return(0);
        }
    }

    public String getOutputPinString() {
 if (this.isOutputAddMessage()) {
            return(getValueString(2));
 } else {
     log.error("Output Parser called on non-Output message type {}", this.getOpCodeChar());
     return("0");
        }
    }

    public int getOutputPinInt() {
        if (this.isOutputAddMessage()) {
            return(getValueInt(2));
        } else {
     log.error("Output Parser called on non-Output message type {}", this.getOpCodeChar());
            return(0);
        }
    }

    public String getOutputIFlagString() {
 if (this.isOutputAddMessage()) {
            return(getValueString(3));
 } else {
     log.error("Output Parser called on non-Output message type {}", this.getOpCodeChar());
     return("0");
        }
    }

    public int getOutputIFlagInt() {
        if (this.isOutputAddMessage()) {
            return(getValueInt(3));
        } else {
     log.error("Output Parser called on non-Output message type {}", this.getOpCodeChar());
            return(0);
        }
    }

    public String getOutputStateString() {
 if (isOutputCmdMessage()) {
     return(this.getOutputStateInt() == 1 ? "HIGH" : "LOW");
 } else {
     return("Not a Turnout");
 }
    }

    public int getOutputStateInt() {
 if (isOutputCmdMessage()) {
            return(getValueInt(2));
 } else {
     log.error("Output Parser called on non-Output message type {}", this.getOpCodeChar());
     return(0);
 }
    }
    
    public boolean getOutputStateBool() {
 if (this.isOutputCmdMessage()) {
            return(getValueInt(2) != 0);
 } else {
     log.error("Output Parser called on non-Output message type {} message {}", this.getOpCodeChar(), this.toString());
     return(false);
        }
    }
    public String getSensorIDString() {
 if (this.isSensorAddMessage()) {
            return getValueString(1);
 } else { 
     log.error("Sensor Parser called on non-Sensor message type {}", this.getOpCodeChar());
     return("0");
        }
    }

    public int getSensorIDInt() {
        if (this.isSensorAddMessage()) {
        return(getValueInt(1)); // assumes stored as an int!
        } else {
     log.error("Sensor Parser called on non-Sensor message type {}", this.getOpCodeChar());
     return(0);
        }
    }

    public String getSensorPinString() {
 if (this.isSensorAddMessage()) {
            return(getValueString(2));
 } else {
     log.error("Sensor Parser called on non-Sensor message type {}", this.getOpCodeChar());
     return("0");
        }
    }

    public int getSensorPinInt() {
        if (this.isSensorAddMessage()) {
            return(getValueInt(2));
        } else {
     log.error("Sensor Parser called on non-Sensor message type {}", this.getOpCodeChar());
            return(0);
        }
    }

    public String getSensorPullupString() {
 if (isSensorAddMessage()) {
            return(getValueBool(3) ? "PULLUP" : "NO PULLUP");
 } else {
     return("Not a Sensor");
 }
    }

    public int getSensorPullupInt() {
 if (this.isSensorAddMessage()) {
            return(getValueInt(3));
 } else {
     log.error("Sensor Parser called on non-Sensor message type {} message {}", this.getOpCodeChar(), this.toString());
     return(0);
        }
    }
    
    public boolean getSensorPullupBool() {
 if (this.isSensorAddMessage()) {
            return(getValueBool(3));
 } else {
     log.error("Sensor Parser called on non-Sensor message type {} message {}", this.getOpCodeChar(), this.toString());
     return(false);
        }
    }



    // Helper methods for Accessory Decoder Commands

    public String getAccessoryAddrString() {
 if (this.isAccessoryMessage()) {
            return(getValueString(1));
 } else {
     log.error("Accessory Parser called on non-Accessory message type {}", this.getOpCodeChar());
     return("0");
        }
    }

    public int getAccessoryAddrInt() {
        if (this.isAccessoryMessage()) {
            return(getValueInt(1));
        } else {
     log.error("Accessory Parser called on non-Accessory message type {}", this.getOpCodeChar());
            return(0);
        }
 //return(Integer.parseInt(this.getAccessoryAddrString()));
    }

    public String getAccessorySubString() {
 if (this.isAccessoryMessage()) {
            return(getValueString(2));
 } else {
     log.error("Accessory Parser called on non-Accessory message type {} message {}", this.getOpCodeChar(), this.toString());
            return("0");
        }
    }

    public int getAccessorySubInt() {
        if (this.isAccessoryMessage()) {
            return(getValueInt(2));
 } else {
     log.error("Accessory Parser called on non-Accessory message type {} message {}", this.getOpCodeChar(), this.toString());
            return(0);
        }
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
            return(getValueInt(3));
        } else {
     log.error("Accessory Parser called on non-Accessory message type {} message {}", this.getOpCodeChar(), this.toString());
     return(0);
        }
    }



    //------------------------------------------------------
    // Helper methods for Throttle Commands

    public String getRegisterString() {
 if (this.isThrottleMessage() || this.isWriteDccPacketMessage() ) {
            return(getValueString(1));
 } else {
     log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
     return("0");
        }
    }

    public int getRegisterInt() {
        if (this.isThrottleMessage()) {
            return(getValueInt(1));
 } else {
     log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
     return(0);
        }
    }

    public String getAddressString() {
 if (this.isThrottleMessage()) {
            return(getValueString(2));
 } else {
     log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
     return("0");
        }
    }

    public int getAddressInt() {
        if (this.isThrottleMessage()) {
            return(getValueInt(2));
 } else {
     log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
     return(0);
        }
    }

    public String getSpeedString() {
 if (this.isThrottleMessage()) {
            return(getValueString(3));
 } else {
     log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
     return("0");
        }
    }

    public int getSpeedInt() {
        if (this.isThrottleMessage()) {
            return(getValueInt(3));
 } else {
     log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
     return(0);
        }
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
            return(getValueInt(4));
 } else {
     log.error("Throttle Parser called on non-Throttle message type {}", this.getOpCodeChar());
     return(0);
        }
    }

    //------------------------------------------------------
    // Helper methods for Function Commands

    public String getFuncAddressString() {
 if (this.isFunctionMessage()) {
            return(getValueString(1));
 } else {
     log.error("Function Parser called on non-Function message type {}", this.getOpCodeChar());
     return("0");
        }
    }

    public int getFuncAddressInt() {
        if (this.isFunctionMessage()) {
            return(getValueInt(1));
 } else {
     log.error("Function Parser called on non-Function message type {}", this.getOpCodeChar());
     return(0);
        }
    }

    public String getFuncByte1String() {
 if (this.isFunctionMessage()) {
            return(getValueString(2));
 } else {
     log.error("Function Parser called on non-Function message type {}", this.getOpCodeChar());
     return("0");
        }
    }

    public int getFuncByte1Int() {
        if (this.isFunctionMessage()) {
            return(getValueInt(2));
 } else {
     log.error("Function Parser called on non-Function message type {}", this.getOpCodeChar());
     return(0);
        }
    }

    public String getFuncByte2String() {
 if (this.isFunctionMessage()) {
            return(getValueString(3));
 } else {
     log.error("Function Parser called on non-Function message type {}", this.getOpCodeChar());
     return("0");
        }
    }

    public int getFuncByte2Int() {
        if (this.isFunctionMessage()) {
            return(getValueInt(3));
  } else {
     log.error("Function Parser called on non-Function message type {}", this.getOpCodeChar());
     return(0);
        }
    }

    //------------------------------------------------------
    // Helper methods for Turnout Commands

    public String getTOIDString() {
 if (this.isTurnoutMessage()) {
            return(getValueString(1));
 } else {
     log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this.toString());
     return("0");
        }
    }

    public int getTOIDInt() {
        if (this.isTurnoutMessage()) {
            return(getValueInt(1));
 } else {
     log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this.toString());
     return(0);
        }
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
            return(getValueInt(2));
 } else {
     log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this.toString());
     return(0);
        }
    }


    public String getTOAddressString() {
 if (this.isTurnoutAddMessage()) {
            return(getValueString(2));
 } else {
     log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this.toString());
     return("0");
        }
    }

    public int getTOAddressInt() {
        if (this.isTurnoutAddMessage()) {
            return(getValueInt(2));
 } else {
     log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this.toString());
     return(0);
        }
    }

    public String getTOSubAddressString() {
 if (this.isTurnoutAddMessage()) {
            return(getValueString(3));
 } else {
     log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this.toString());
     return("0");
        }
    }

    public int getTOSubAddressInt() {
        if (this.isTurnoutAddMessage()) {
            return(getValueInt(3));
 } else {
     log.error("Turnout Parser called on non-Turnout message type {} message {}", this.getOpCodeChar(), this.toString());
     return(0);
        }
    }

    //------------------------------------------------------
    // Helper methods for Ops Write Byte Commands

     public String getOpsWriteAddrString() {
        if (this.isOpsWriteByteMessage() || this.isOpsWriteBitMessage()) {
            return(getValueString(1));
        } else {
            return("0");
        }
    }

    public int getOpsWriteAddrInt() {
        if (this.isOpsWriteByteMessage() || this.isOpsWriteBitMessage()) {
            return(getValueInt(1));
        } else {
            return(0);
        }
    }

    public String getOpsWriteCVString() {
        if (this.isOpsWriteByteMessage() || this.isOpsWriteBitMessage()) {
            return(getValueString(2));
        } else {
            return("0");
        }
    }

    public int getOpsWriteCVInt() {
        if (this.isOpsWriteByteMessage() || this.isOpsWriteBitMessage()) {
            return(getValueInt(2));
        } else {
            return(0);
        }
    }
    
    public String getOpsWriteBitString() {
        if (this.isOpsWriteBitMessage()) {
            return(getValueString(3));
        } else {
            return("0");
        }
    }

    public int getOpsWriteBitInt() {
        if (this.isOpsWriteBitMessage()) {
            return(getValueInt(3));
        } else {
            return(0);
        }
    }
    
    public String getOpsWriteValueString() {
 if (this.isOpsWriteByteMessage()) {
            return(getValueString(3));
 } else if (this.isOpsWriteBitMessage()) {
            return(getValueString(4));
 } else {
     log.error("Ops Program Parser called on non-OpsProgram message type {}", this.getOpCodeChar());
     return("0");
 }
    }

    public int getOpsWriteValueInt() {
        if (this.isOpsWriteByteMessage()) {
            return(getValueInt(3));
        } else if (this.isOpsWriteBitMessage()) {
            return(getValueInt(4));
        } else {
            return(0);
        }
    }
    
    //------------------------------------------------------
    // Helper methods for Prog Write Byte Commands

    public String getCVString() {
        if (this.isProgWriteByteMessage() || this.isProgWriteBitMessage() || this.isProgReadMessage()) {
            return(getValueString(1));
        } else {
            return("0");
        }
    }

    public int getCVInt() {
        if (this.isProgWriteByteMessage() || this.isProgWriteBitMessage() || this.isProgReadMessage()) {
            return(getValueInt(1));
        } else {
            return(0);
        }
    }

    public String getCallbackNumString() {
        int idx = 2;
        if (this.isProgWriteByteMessage()) {
            idx = 3;
        } else if (this.isProgWriteBitMessage()) {
            idx = 4;
        } else if (this.isProgReadMessage()) {
            idx = 2;
        } else {
            return("0");
        }
        return(getValueString(idx));
    }

    public int getCallbackNumInt() {
        int idx = 2;
        if (this.isProgWriteByteMessage()) {
            idx = 3;
        } else if (this.isProgWriteBitMessage()) {
            idx = 4;
        } else if (this.isProgReadMessage()) {
            idx = 2;
        } else {
            return(0);
        }
        return(getValueInt(idx));
    }

    public String getCallbackSubString() {
        int idx = 3;
        if (this.isProgWriteByteMessage()) {
            idx = 4;
        } else if (this.isProgWriteBitMessage()) {
            idx = 5;
        } else if (this.isProgReadMessage()) {
            idx = 3;
        } else {
            return("0");
        }
        return(getValueString(idx));
    }

    public int getCallbackSubInt() {
        int idx = 3;
        if (this.isProgWriteByteMessage()) {
            idx = 4;
        } else if (this.isProgWriteBitMessage()) {
            idx = 5;
        } else if (this.isProgReadMessage()) {
            idx = 3;
        } else {
            return(0);
        }
        return(getValueInt(idx));
    }

    public String getProgValueString() {
        int idx = 2;
        if (this.isProgWriteByteMessage()) {
            idx = 2;
        } else if (this.isProgWriteBitMessage()) {
            idx = 3;
        } else {
            return("0");
        }
        return(getValueString(idx));
    }

    public int getProgValueInt() {
        int idx = 2;
        if (this.isProgWriteByteMessage()) {
            idx = 2;
        } else if (this.isProgWriteBitMessage()) {
            idx = 3;
        } else {
            return(0);
        }
        return(getValueInt(idx));
    }

    //------------------------------------------------------
    // Helper methods for Prog Write Bit Commands

    public String getBitString() {
 if (this.isProgWriteBitMessage()) {
            return(getValueString(2));
        } else {
     log.error("PWBit Parser called on non-PWBit message type {}", this.getOpCodeChar());
     return("0");
        }
    }

    public int getBitInt() {
        if (this.isProgWriteBitMessage()) {
            return(getValueInt(2));
        } else {
            return(0);
        }
    }

    public String getPacketString() {
       if ( this.isWriteDccPacketMessage() ) {
            StringBuffer b = new StringBuffer();
            for(int i = 2;i<=getGroupCount();i++){
                b.append(this.getValueString(i));
            }
            return(b.toString());
       } else {
            log.error("Write Dcc Packet parser called on non-Dcc Packet message type {}", this.getOpCodeChar());
            return("0");
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
        case DCCppConstants.OUTPUT_CMD:
 case DCCppConstants.LIST_REGISTER_CONTENTS:
     retv = true;
     break;
 default:
     retv = false;
 }
 return(retv);
    }

    //private boolean responseExpected = true;

    // Tell the traffic controller we expect this
    // message to have a broadcast reply (or not).
    //(Not really used)
    void setResponseExpected(boolean v) {
        //responseExpected = v;
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
     * Format: {@code <a ADDRESS SUBADDRESS ACTIVATE>}
     *
     *    ADDRESS:  the primary address of the decoder (0-511)
     *    SUBADDRESS: the subaddress of the decoder (0-3)
     *    ACTIVATE: 1=on (set), 0=off (clear)
     *
     *    Note that many decoders and controllers combine the ADDRESS and SUBADDRESS into a single number, N,
     *    from  1 through a max of 2044, where
     *    
     *    {@code N = (ADDRESS - 1) * 4 + SUBADDRESS + 1, for all ADDRESS>0}
     *    
     *    OR
     *    
     *    {@code ADDRESS = INT((N - 1) / 4) + 1}
     *    {@code SUBADDRESS = (N - 1) % 4}
     *    
     *    returns: NONE
    */
    public static DCCppMessage makeAccessoryDecoderMsg(int address, int subaddress, boolean activate) {
 // Sanity check inputs
 if (address < 0 || address > DCCppConstants.MAX_ACC_DECODER_ADDRESS)
     return(null);
 if (subaddress < 0 || subaddress > DCCppConstants.MAX_ACC_DECODER_SUBADDR)
     return(null);

        DCCppMessage m = new DCCppMessage(DCCppConstants.ACCESSORY_CMD);
        
        m.myMessage.append(" " + address);
        m.myMessage.append(" " + subaddress);
        m.myMessage.append(" " + (activate ? "1" : "0"));
        m.myRegex = DCCppConstants.ACCESSORY_CMD_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
    }
    
    public static DCCppMessage makeAccessoryDecoderMsg(int address, boolean activate) {
 // Convert the single address to an address/subaddress pair:
 // address = (address - 1) * 4 + subaddress + 1 for address>0;
        int addr, subaddr;
        if (address > 0) {
            addr = (address - 1) / (DCCppConstants.MAX_ACC_DECODER_SUBADDR + 1);
            subaddr = (address - 1) % (DCCppConstants.MAX_ACC_DECODER_SUBADDR + 1);
        } else {
            addr = subaddr = 0;
        }
        return(makeAccessoryDecoderMsg(addr, subaddr, activate));
    }


    /**
     * Predefined Turnout Control Message
     *
     * Format: {@code <T ID THROW>}
     *
     *   ID: the numeric ID (0-32767) of the turnout to control
     *   THROW: 0 (unthrown) or 1 (thrown)
     *   
     *   returns:{@code <H ID THROW>}
     *
     * ADD: {@code <T ID ADDRESS SUBADDRESS>}
     *    ID: the numeric ID (0-32767) of the turnout to control
     *    ADDRESS: Decoder address (0-511)
     *    SUBADDRESS: Decoder subaddress (0-3)
     *    RETURNS: {@code <O>} on success, {@code <X>} on failure
     *
     * DELETE: {@code <T ID>}
     *    ID: the numeric ID (0-32767) of the turnout to control
     *    RETURNS: {@code <O>} on success, {@code <X>} on failure
     *
     * LIST: {@code <T>}
     *    RETURNS: {@code <H ID ADDRESS SUBADDRESS THROW>} for each defined turnout or {@code <X>} if no turnouts defined.
     */
    public static DCCppMessage makeTurnoutCommandMsg(int id, boolean thrown) {
 // Sanity check inputs
 if (id < 0 || id > DCCppConstants.MAX_TURNOUT_ADDRESS) return(null);
 // Need to also validate whether turnout is predefined?  Where to store the IDs?
 // Turnout Command
        
        DCCppMessage m = new DCCppMessage(DCCppConstants.TURNOUT_CMD);
        m.myMessage.append(" " + id);
        m.myMessage.append((thrown ? " 1" : " 0"));
        m.myRegex = DCCppConstants.TURNOUT_CMD_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
    }

    public static DCCppMessage makeOutputCmdMsg(int id, boolean state) {
 // Sanity check inputs
 if (id < 0 || id > DCCppConstants.MAX_TURNOUT_ADDRESS) { return(null); }

        DCCppMessage m = new DCCppMessage(DCCppConstants.OUTPUT_CMD);
        m.myMessage.append(" " + id);
        m.myMessage.append(" " + (state ? "1" : "0"));
        m.myRegex = DCCppConstants.OUTPUT_CMD_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
    }

    public static DCCppMessage makeOutputAddMsg(int id, int pin, int iflag) {
 // Sanity check inputs
 if (id < 0 || id > DCCppConstants.MAX_TURNOUT_ADDRESS) { return(null); }

        DCCppMessage m = new DCCppMessage(DCCppConstants.OUTPUT_CMD);
        m.myMessage.append(" " + id);
        m.myMessage.append(" " + pin);
        m.myMessage.append(" " + iflag);
        m.myRegex = DCCppConstants.OUTPUT_ADD_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
    }

    public static DCCppMessage makeOutputDeleteMsg(int id) {
 // Sanity check inputs
 if (id < 0 || id > DCCppConstants.MAX_TURNOUT_ADDRESS) { return(null); }

        DCCppMessage m = new DCCppMessage(DCCppConstants.OUTPUT_CMD);
        m.myMessage.append(" " + id);
        m.myRegex = DCCppConstants.OUTPUT_DELETE_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
    }

    public static DCCppMessage makeOutputListMsg() {
        return(new DCCppMessage(DCCppConstants.OUTPUT_CMD, DCCppConstants.OUTPUT_LIST_REGEX));
    }

    public static DCCppMessage makeTurnoutAddMsg(int id, int addr, int subaddr) {
 // Sanity check inputs
 if (id < 0 || id > DCCppConstants.MAX_TURNOUT_ADDRESS) { return(null); }
 if (addr < 0 || addr > DCCppConstants.MAX_ACC_DECODER_ADDRESS) { return(null); }
 if (subaddr < 0 || subaddr > DCCppConstants.MAX_ACC_DECODER_SUBADDR) { return(null); }

        DCCppMessage m = new DCCppMessage(DCCppConstants.TURNOUT_CMD);
        m.myMessage.append(" " + id);
        m.myMessage.append(" " + addr);
        m.myMessage.append(" " + subaddr);
        m.myRegex = DCCppConstants.TURNOUT_ADD_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
    }

    public static DCCppMessage makeTurnoutDeleteMsg(int id) {
 // Sanity check inputs
 if (id < 0 || id > DCCppConstants.MAX_TURNOUT_ADDRESS) { return(null); }

        DCCppMessage m = new DCCppMessage(DCCppConstants.TURNOUT_CMD);
        m.myMessage.append(" " + id);
        m.myRegex = DCCppConstants.TURNOUT_DELETE_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
    }

    public static DCCppMessage makeTurnoutListMsg() {
        return(new DCCppMessage(DCCppConstants.TURNOUT_CMD, DCCppConstants.TURNOUT_LIST_REGEX));
    }

    /** Create/Delete/Query Sensor
     * 
     * ADD: {@code <S ID PIN PULLUP>}
     *    ID (0-32767)
     *    PIN: Arduino Pin # of sensor
     *    PULLUP: TRUE if use internal pullup for PIN, FALSE if don't.
     *    RETURNS: {@code <O>} on success, {@code <X>} on failure
     *
     * DELETE: {@code <S ID>}
     *    RETURNS: {@code <O>} on success, {@code <X>} on failure
     *
     * LIST: {@code <S>}
     *    RETURNS: {@code <Q ID PIN PULLUP>} for each defined sensor, or {@code <X>} if no sensors defined.
     */
    public static DCCppMessage makeSensorAddMsg(int id, int pin, int pullup) {
 // Sanity check inputs
 // TODO: Optional sanity check pin number vs. Arduino model.
 if (id < 0 || id > DCCppConstants.MAX_SENSOR_ID) { return(null); }

        DCCppMessage m = new DCCppMessage(DCCppConstants.SENSOR_CMD);
        m.myMessage.append(" " + id);
        m.myMessage.append(" " + pin);
        m.myMessage.append(" " + pullup);
        m.myRegex = DCCppConstants.SENSOR_ADD_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
    }

    public static DCCppMessage makeSensorDeleteMsg(int id) {
 // Sanity check inputs
 if (id < 0 || id > DCCppConstants.MAX_SENSOR_ID) { return(null); }

        DCCppMessage m = new DCCppMessage(DCCppConstants.SENSOR_CMD);
        m.myMessage.append(" " + id);
        m.myRegex = DCCppConstants.SENSOR_DELETE_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
    }

    public static DCCppMessage makeSensorListMsg() {
        return(new DCCppMessage(DCCppConstants.SENSOR_CMD, DCCppConstants.SENSOR_LIST_REGEX));
    }

     /**
     * Query All Sensors States
     *
     * Format: {@code <Q>}
     *
     *    returns status messages containing the status of each connected sensor.
     */
  public static DCCppMessage makeQuerySensorStatesMsg() {
 return(new DCCppMessage(DCCppConstants.QUERY_SENSOR_STATES_CMD, DCCppConstants.QUERY_SENSOR_STATES_REGEX));
    }

    /**
     * Write Direct CV Byte to Programming Track
     *
     * Format: {@code <W CV VALUE CALLBACKNUM CALLBACKSUB>}
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
     *    returns: {@code <r CALLBACKNUM|CALLBACKSUB|CV Value)}
     *    where VALUE is a number from 0-255 as read from the requested CV, or -1 if verificaiton read fails
     */
    public static DCCppMessage makeWriteDirectCVMsg(int cv, int val) {
 return(makeWriteDirectCVMsg(cv, val, 0, DCCppConstants.PROG_WRITE_CV_BYTE));
    }

    public static DCCppMessage makeWriteDirectCVMsg(int cv, int val, int callbacknum, int callbacksub) {
 // Sanity check inputs
 if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) return(null);
 if (val < 0 || val > DCCppConstants.MAX_DIRECT_CV_VAL) return(null);
 if (callbacknum < 0 || callbacknum > DCCppConstants.MAX_CALLBACK_NUM)
     return(null);
 if (callbacksub < 0 || callbacksub > DCCppConstants.MAX_CALLBACK_SUB)
     return(null);

        DCCppMessage m = new DCCppMessage(DCCppConstants.PROG_WRITE_CV_BYTE);
        m.myMessage.append(" " + cv);
        m.myMessage.append(" " + val);
        m.myMessage.append(" " + callbacknum);
        m.myMessage.append(" " + callbacksub);
        m.myRegex = DCCppConstants.PROG_WRITE_BYTE_REGEX;
        
        m._nDataChars = m.toString().length();
        m.setTimeout(DCCppProgrammingTimeout);
        return(m);
    }

    /**
     * Write Direct CV Bit to Programming Track
     *
     * Format: {@code <B CV BIT VALUE CALLBACKNUM CALLBACKSUB>}
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
     *    returns: {@code <r CALLBACKNUM|CALLBACKSUB|CV BIT VALUE)}
     *    where VALUE is a number from 0-1 as read from the requested CV bit, or -1 if verificaiton read fails
     */    
    public static DCCppMessage makeBitWriteDirectCVMsg(int cv, int bit, int val) {
 return(makeBitWriteDirectCVMsg(cv, bit, val, 0, DCCppConstants.PROG_WRITE_CV_BIT));
    }

    public static DCCppMessage makeBitWriteDirectCVMsg(int cv, int bit, int val, int callbacknum, int callbacksub) {

 // Sanity Check Inputs
 if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) return(null);
 if (bit < 0 || bit > 7) return(null);
 if (callbacknum < 0 || callbacknum > DCCppConstants.MAX_CALLBACK_NUM)
     return(null);
 if (callbacksub < 0 || callbacksub > DCCppConstants.MAX_CALLBACK_SUB)
     return(null);

        DCCppMessage m = new DCCppMessage(DCCppConstants.PROG_WRITE_CV_BIT);
        m.myMessage.append(" " + cv);
        m.myMessage.append(" " + bit);
        m.myMessage.append(" " + (val == 0 ? "0" : "1"));
        m.myMessage.append(" " + callbacknum);
        m.myMessage.append(" " + callbacksub);
        m.myRegex = DCCppConstants.PROG_WRITE_BIT_REGEX;
 
        m._nDataChars = m.toString().length();
        m.setTimeout(DCCppProgrammingTimeout);
        return(m);
    }

    /**
     * Read Direct CV Byte from Programming Track
     *
     * Format: {@code <R CV CALLBACKNUM CALLBACKSUB>}
     *
     *    reads a Configuration Variable from the decoder of an engine on the programming track
     *    
     *    CV: the number of the Configuration Variable memory location in the decoder to read from (1-1024)
     *    CALLBACKNUM: an arbitrary integer (0-32767) that is ignored by the Base Station and is simply echoed back in the output - useful for external programs that call this function
     *    CALLBACKSUB: a second arbitrary integer (0-32767) that is ignored by the Base Station and is simply echoed back in the output - useful for external programs (e.g. DCC++ Interface) that call this function
     *    
     * Note: The two-argument form embeds the opcode in CALLBACKSUB to aid in decoding the responses.
     *
     *    returns: {@code <r CALLBACKNUM|CALLBACKSUB|CV VALUE>}
     *    where VALUE is a number from 0-255 as read from the requested CV, or -1 if read could not be verified
     */    
    public static DCCppMessage makeReadDirectCVMsg(int cv) {
 return(makeReadDirectCVMsg(cv, 0, DCCppConstants.PROG_READ_CV));
    }

    public static DCCppMessage makeReadDirectCVMsg(int cv, int callbacknum, int callbacksub) {

 // Sanity check inputs
 if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) return(null);
 if (callbacknum < 0 || callbacknum > DCCppConstants.MAX_CALLBACK_NUM)
     return(null);
 if (callbacksub < 0 || callbacksub > DCCppConstants.MAX_CALLBACK_SUB)
     return(null);
        
        DCCppMessage m = new DCCppMessage(DCCppConstants.PROG_READ_CV);
        m.myMessage.append(" " + cv);
        m.myMessage.append(" " + callbacknum);
        m.myMessage.append(" " + callbacksub);
        m.myRegex = DCCppConstants.PROG_READ_REGEX;

        m._nDataChars = m.toString().length();
        m.setTimeout(DCCppProgrammingTimeout);
        return(m);
    }

    
    /**
     * Write Direct CV Byte to Main Track
     *
     * Format: {@code <w CAB CV VALUE>}
     *
     *    writes, without any verification, a Configuration Variable to the decoder of an engine on the main operations track
     *    
     *    CAB:  the short (1-127) or long (128-10293) address of the engine decoder 
     *    CV: the number of the Configuration Variable memory location in the decoder to write to (1-1024)
     *    VALUE: the value to be written to the Configuration Variable memory location (0-255)
     *    
     *    returns: NONE
     */    
    public static DCCppMessage makeWriteOpsModeCVMsg(int address, int cv, int val) {
 // Sanity check inputs
 if (address < 0 || address > DCCppConstants.MAX_LOCO_ADDRESS)
     return(null);
 if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) return(null);
 if (val < 0 || val > DCCppConstants.MAX_DIRECT_CV_VAL) return(null);
 
        DCCppMessage m = new DCCppMessage(DCCppConstants.OPS_WRITE_CV_BYTE);
        m.myMessage.append(" " + address);
        m.myMessage.append(" " + cv);
        m.myMessage.append(" " + val);
        m.myRegex = DCCppConstants.OPS_WRITE_BYTE_REGEX;
        
        m._nDataChars = m.toString().length();
        m.setTimeout(DCCppProgrammingTimeout);
        return(m);
    }

    /**
     * Write Direct CV Bit to Main Track
     *
     * Format: {@code <b CAB CV BIT VALUE>}
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
    public static DCCppMessage makeBitWriteOpsModeCVMsg(int address, int cv, int bit, int val) {

 // Sanity Check Inputs
 if (address < 0 || address > DCCppConstants.MAX_LOCO_ADDRESS)
     return(null);
 if (cv < 1 || cv > DCCppConstants.MAX_DIRECT_CV) return(null);
 if (bit < 0 || bit > 7) return(null);
 
        DCCppMessage m = new DCCppMessage(DCCppConstants.OPS_WRITE_CV_BIT);
        m.myMessage.append(" " + address);
        m.myMessage.append(" " + cv);
        m.myMessage.append(" " + bit);
        m.myMessage.append(" " + (val == 0 ? "0" : "1"));
       
        m.myRegex = DCCppConstants.OPS_WRITE_BIT_REGEX;
        
        m._nDataChars = m.toString().length();
        m.setTimeout(DCCppProgrammingTimeout);
        return(m);
    }

    /**
     * Set Track Power ON or OFFf
     *
     * Format: {@code <1> (ON) or <0> (OFF)}
     *
     * Returns {@code <p1> (ON) or <p0> (OFF)}
     */
    public static DCCppMessage makeSetTrackPowerMsg(boolean on) {
 //String s = new String(Character.toString((on ? DCCppConstants.TRACK_POWER_ON : DCCppConstants.TRACK_POWER_OFF)));
 //return(new DCCppMessage(s));
        return(new DCCppMessage((on ? DCCppConstants.TRACK_POWER_ON : DCCppConstants.TRACK_POWER_OFF),
                                DCCppConstants.TRACK_POWER_REGEX));
    }

    public static DCCppMessage makeTrackPowerOnMsg() {
 return(makeSetTrackPowerMsg(true));
    }

    public static DCCppMessage makeTrackPowerOffMsg() {
 return(makeSetTrackPowerMsg(false));
    }


    /**
     * Read main operations track current
     *
     * Format: {@code <c>}
     *
     *    reads current being drawn on main operations track
     *    
     *    returns: {@code <a CURRENT>}
     *    where CURRENT = 0-1024, based on exponentially-smoothed weighting scheme
     */
   public static DCCppMessage makeReadTrackCurrentMsg() {
 return(new DCCppMessage(DCCppConstants.READ_TRACK_CURRENT, DCCppConstants.READ_TRACK_CURRENT_REGEX));
    }

     /**
     * Read DCC++ Base Station Status
     *
     * Format: {@code <s>}
     *
     *    returns status messages containing track power status, throttle status, turn-out status, and a version number
     *    NOTE: this is very useful as a first command for an interface to send to this sketch in order to verify connectivity and update any GUI to reflect actual throttle and turn-out settings
     *    
     *    returns: series of status messages that can be read by an interface to determine status of DCC++ Base Station and important settings
     */
  public static DCCppMessage makeCSStatusMsg() {
 return(new DCCppMessage(DCCppConstants.READ_CS_STATUS, DCCppConstants.READ_CS_STATUS_REGEX));
    }


    /*
     * Generate an emergency stop for the specified address
     * @param address is the locomotive address
     *
     * Note: This just sends a THROTTLE command with speed = -1
     */
    public static DCCppMessage makeAddressedEmergencyStop(int register, int address) {
        // Sanity check inputs
        if (address < 0 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);

        DCCppMessage m = new DCCppMessage(DCCppConstants.THROTTLE_CMD);
        m.myMessage.append(" " + register);
        m.myMessage.append(" " + address);
        m.myMessage.append(" -1 1");
        m.myRegex = DCCppConstants.THROTTLE_CMD_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
    }
    
    /*
     * Generate a Speed and Direction Request message
     * @param register is the DCC++ base station register assigned.
     * @param address is the locomotive address
     * @param speed a normalized speed value (a floating point number between 0 
     *              and 1).  A negative value indicates emergency stop.
     * @param isForward true for forward, false for reverse.
     *
     * Format: {@code <t REGISTER CAB SPEED DIRECTION>}
     *
     *    sets the throttle for a given register/cab combination 
     *    
     *    REGISTER: an internal register number, from 1 through MAX_MAIN_REGISTERS (inclusive), to store the DCC packet used to control this throttle setting
     *    CAB:  the short (1-127) or long (128-10293) address of the engine decoder
     *    SPEED: throttle speed from 0-126, or -1 for emergency stop (resets SPEED to 0)
     *    DIRECTION: 1=forward, 0=reverse.  Setting direction when speed=0 or speed=-1 only effects directionality of cab lighting for a stopped train
     *    
     *    returns: {@code <T REGISTER SPEED DIRECTION>}
     *    
     */
    public static DCCppMessage makeSpeedAndDirectionMsg(int register, int address, float speed, boolean isForward) {
 // Sanity check inputs
 if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
 
        DCCppMessage m = new DCCppMessage(DCCppConstants.THROTTLE_CMD);
        m.myMessage.append(" " + register);
        m.myMessage.append(" " + address);
 if (speed < 0.0) {
            m.myMessage.append(" -1");
 } else {
     int speedVal = java.lang.Math.round(speed * 126);
     speedVal = ((speedVal > DCCppConstants.MAX_SPEED) ? DCCppConstants.MAX_SPEED : speedVal);
            m.myMessage.append(" " + speedVal);
        }
        m.myMessage.append(" " + (isForward ? "1" : "0"));
        
        m.myRegex = DCCppConstants.THROTTLE_CMD_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
    }

    /** 
     * Function Group Messages (common serial format)
     * 
     * Format: {@code <f CAB BYTE1 [BYTE2]>}
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

    public static DCCppMessage makeFunctionGroup1OpsMsg(int address,
            boolean f0,
            boolean f1,
            boolean f2,
            boolean f3,
            boolean f4) 
    { 

        // Sanity check inputs
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
        
        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" " + address);
        
        int byte1 = 128 + (f0 ? 16 : 0);
        byte1 += (f1 ? 1 : 0);
        byte1 += (f2 ? 2 : 0);
        byte1 += (f3 ? 4 : 0);
        byte1 += (f4 ? 8 : 0);
        m.myMessage.append(" " + byte1);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
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
    public static DCCppMessage makeFunctionGroup1SetMomMsg(int address,
            boolean f0,
            boolean f1,
            boolean f2,
            boolean f3,
            boolean f4) {

        // Sanity check inputs
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
 
        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" " + address);

        int byte1 = 128 + (f0 ? 16 : 0);
        byte1 += (f1 ? 1 : 0);
        byte1 += (f2 ? 2 : 0);
        byte1 += (f3 ? 4 : 0);
        byte1 += (f4 ? 8 : 0);

        m.myMessage.append(" " + byte1);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
    }


    /*
     * Generate a Function Group Two Operation Request message
     * @param address is the locomotive address
     * @param f5 is true if f5 is on, false if f5 is off
     * @param f6 is true if f6 is on, false if f6 is off
     * @param f7 is true if f7 is on, false if f7 is off
     * @param f8 is true if f8 is on, false if f8 is off
     */
    public static DCCppMessage makeFunctionGroup2OpsMsg(int address,
            boolean f5,
            boolean f6,
            boolean f7,
            boolean f8) {

        // Sanity check inputs
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
 
        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" " + address);

        int byte1 = 176;
        byte1 += (f5 ? 1 : 0);
        byte1 += (f6 ? 2 : 0);
        byte1 += (f7 ? 4 : 0);
        byte1 += (f8 ? 8 : 0);

        m.myMessage.append(" " + byte1);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;
        
       m._nDataChars = m.toString().length();
        return(m);
     }

    /*
     * Generate a Function Group Two Set Momentary Functions message
     * @param address is the locomotive address
     * @param f5 is true if f5 is momentary
     * @param f6 is true if f6 is momentary
     * @param f7 is true if f7 is momentary
     * @param f8 is true if f8 is momentary
     */
    public static DCCppMessage makeFunctionGroup2SetMomMsg(int address,
            boolean f5,
            boolean f6,
            boolean f7,
            boolean f8) {

        // Sanity check inputs
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
 
        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" " + address);

        int byte1 = 176;
        byte1 += (f5 ? 1 : 0);
        byte1 += (f6 ? 2 : 0);
        byte1 += (f7 ? 4 : 0);
        byte1 += (f8 ? 8 : 0);
        m.myMessage.append(" " + byte1);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
    }


    /*
     * Generate a Function Group Three Operation Request message
     * @param address is the locomotive address
     * @param f9 is true if f9 is on, false if f9 is off
     * @param f10 is true if f10 is on, false if f10 is off
     * @param f11 is true if f11 is on, false if f11 is off
     * @param f12 is true if f12 is on, false if f12 is off
     */
    public static DCCppMessage makeFunctionGroup3OpsMsg(int address,
            boolean f9,
            boolean f10,
            boolean f11,
            boolean f12) {

        // Sanity check inputs
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
 
        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" " + address);

        int byte1 = 160;
        byte1 += (f9 ? 1 : 0);
        byte1 += (f10 ? 2 : 0);
        byte1 += (f11 ? 4 : 0);
        byte1 += (f12 ? 8 : 0);
        m.myMessage.append(" " + byte1);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
    }

    /*
     * Generate a Function Group Three Set Momentary Functions message
     * @param address is the locomotive address
     * @param f9 is true if f9 is momentary
     * @param f10 is true if f10 is momentary
     * @param f11 is true if f11 is momentary
     * @param f12 is true if f12 is momentary
     */
    public static DCCppMessage makeFunctionGroup3SetMomMsg(int address,
            boolean f9,
            boolean f10,
            boolean f11,
            boolean f12) {

        // Sanity check inputs
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
 
        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" " + address);
 
        int byte1 = 160;
        byte1 += (f9 ? 1 : 0);
        byte1 += (f10 ? 2 : 0);
        byte1 += (f11 ? 4 : 0);
        byte1 += (f12 ? 8 : 0);
        m.myMessage.append(" " + byte1);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
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
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
 
        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" " + address);

        int byte2 = 0;
        byte2 += (f13 ? 1 : 0);
        byte2 += (f14 ? 2 : 0);
        byte2 += (f15 ? 4 : 0);
        byte2 += (f16 ? 8 : 0);
        byte2 += (f17 ? 16 : 0);
        byte2 += (f18 ? 32 : 0);
        byte2 += (f19 ? 64 : 0);
        byte2 += (f20 ? 128 : 0);
        m.myMessage.append(" " + DCCppConstants.FUNCTION_GROUP4_BYTE1);
        m.myMessage.append(" " + byte2);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
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
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
 
        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" " + address);

        int byte2 = 0;
        byte2 += (f13 ? 1 : 0);
        byte2 += (f14 ? 2 : 0);
        byte2 += (f15 ? 4 : 0);
        byte2 += (f16 ? 8 : 0);
        byte2 += (f17 ? 16 : 0);
        byte2 += (f18 ? 32 : 0);
        byte2 += (f19 ? 64 : 0);
        byte2 += (f20 ? 128 : 0);

        m.myMessage.append(" " + DCCppConstants.FUNCTION_GROUP4_BYTE1);
        m.myMessage.append(" " + byte2);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
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
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
 
        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" " + address);

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

        m.myMessage.append(" " + DCCppConstants.FUNCTION_GROUP5_BYTE1);
        m.myMessage.append(" " + byte2);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
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
        if (address < 1 || address > DCCppConstants.MAX_LOCO_ADDRESS) return(null);
 
        DCCppMessage m = new DCCppMessage(DCCppConstants.FUNCTION_CMD);
        m.myMessage.append(" " + address);

        int byte2 = 0;
        byte2 += (f21 ? 1 : 0);
        byte2 += (f22 ? 2 : 0);
        byte2 += (f23 ? 4 : 0);
        byte2 += (f24 ? 8 : 0);
        byte2 += (f25 ? 16 : 0);
        byte2 += (f26 ? 32 : 0);
        byte2 += (f27 ? 64 : 0);
        byte2 += (f28 ? 128 : 0);

        m.myMessage.append(" " + DCCppConstants.FUNCTION_GROUP5_BYTE1);
        m.myMessage.append(" " + byte2);
        m.myRegex = DCCppConstants.FUNCTION_CMD_REGEX;
        
        m._nDataChars = m.toString().length();
        return(m);
    }

    /*
     * Build an Emergency Off Message
     */
    

    /**
     * Test Code Functions... not for normal use
     */

    /** Write DCC Packet to a specified Register on the Main*/
    public static DCCppMessage makeWriteDCCPacketMainMsg( int register, int num_bytes, byte[] bytes) {
        // Sanity Check Inputs
        if (register < 0 || register > DCCppConstants.MAX_MAIN_REGISTERS) return(null);
        if (num_bytes < 2 || num_bytes > 5) return(null);
 
        DCCppMessage m = new DCCppMessage(DCCppConstants.WRITE_DCC_PACKET_MAIN);
        m.myMessage.append(" " + register);
        for (int k = 0; k < num_bytes; k++) {
            m.myMessage.append(" " + jmri.util.StringUtil.twoHexFromInt(bytes[k]));
        }
        m.myRegex = DCCppConstants.WRITE_DCC_PACKET_MAIN_REGEX;
        return(m);
        
    }
 
    /** Write DCC Packet to a specified Register on the Programming Track*/
    public static DCCppMessage makeWriteDCCPacketProgMsg( int register, int num_bytes, byte bytes[]) {
        // Sanity Check Inputs
        if (register < 0 || register > DCCppConstants.MAX_MAIN_REGISTERS) return(null);
        if (num_bytes < 2 || num_bytes > 5) return(null);
 
        DCCppMessage m = new DCCppMessage(DCCppConstants.WRITE_DCC_PACKET_PROG);
        m.myMessage.append(" " + register);
        for (int k = 0; k < num_bytes; k++) {
            m.myMessage.append(" " + jmri.util.StringUtil.twoHexFromInt(bytes[k]));
        }
        m.myRegex = DCCppConstants.WRITE_DCC_PACKET_PROG_REGEX;
        return(m);
        
    }

    public static DCCppMessage makeCheckFreeMemMsg() {
        return(new DCCppMessage(DCCppConstants.GET_FREE_MEMORY, DCCppConstants.GET_FREE_MEMORY_REGEX));
    }

    public static DCCppMessage makeListRegisterContentsMsg() {
        return(new DCCppMessage(DCCppConstants.LIST_REGISTER_CONTENTS, 
                   DCCppConstants.LIST_REGISTER_CONTENTS_REGEX));
    }

    // initialize logging    
    private final static Logger log = LoggerFactory.getLogger(DCCppMessage.class);

}
