package jmri.jmrix.dccpp;

/**
 * DCCppConstants.java
 *
 * Constants to represent values seen in DCC++ traffic
 *
 * @author Paul Bender Copyright (C) 2003-2009
 * @author Mark Underwood Copyright (C) 2015
 * @author Harald Barth Copyright (C) 2019
 *
 * Variable prefix abbreviation keys: ACC_ is for accessory messages BC_ is for
 * broadcast messages CS_ is for command station messages PROG_ is for
 * programming related messages LOCO_ is for locomotive related commands
 * OPS_MODE_ is for operations mode programming commands
 *
 * A few variables don't have a prefix. The name should be self explanatory, but
 * a prefix may be added later.
 */
public final class DCCppConstants {

    private DCCppConstants() {
        // final class of static values, no values to construct.
    }

    public static final int MAX_MESSAGE_SIZE = 30;
    public static final int MAX_REPLY_SIZE = 2048; //max size of DCC++EX wifi send buffer
    public static final int MAX_MAIN_REGISTERS = 12;
    public static final int MAX_FUNCTION_NUMBER = 68; //
    public static final int REGISTER_UNALLOCATED = -1;
    public static final int NO_REGISTER_FREE = -1; // TODO: Should this be a unique value?

    // DCC++ over TCP Port Number
    public static final int DCCPP_OVER_TCP_PORT = 1235;

    // Communications Port Info
    public static final int COMM_TYPE_SERIAL = 0;
    public static final int COMM_TYPE_ENET = 1;
    public static final int COMM_TYPE_ENETV2 = 2;

    // Command Station Types
    public static final int DCCPP_UNO_1_0 = 1;
    public static final int DCCPP_ARDUINO_1_1 = 2;
    static final String[] CommandStationNames = {
     "DCCPP Arduino Uno v1.0",
     "DCCPP Arduino V1.1",
    };
    // Meter Type codes and descriptions
    public static final String VOLTAGE = "V";
    public static final String CURRENT = "C";

    // DCC++ Command OpCodes
    public static final char THROTTLE_CMD           = 't'; // Throttle command <t reg cab speed dir>
    public static final char FUNCTION_CMD           = 'f'; // F0-F28 <f cab byte1 [byte2]>
    public static final char FUNCTION_V4_CMD        = 'F'; // F0-F68 <F CAB FUNC 1|0>
    public static final char FORGET_CAB_CMD         = '-'; // Forgets one or all locos. <- [CAB]>
    public static final char ACCESSORY_CMD          = 'a'; // Stationary accessory decoder <a addr subaddr activate>
    public static final char TURNOUT_CMD            = 'T'; // Turnout command <T id throw> -- NEW versions V1.1
    public static final char SENSOR_CMD             = 'S'; // Sensor command -- NEW V1.1
    public static final char OUTPUT_CMD             = 'Z'; // Output command -- NEW V1.2?
    public static final char OPS_WRITE_CV_BYTE      = 'w'; // Write CV byte on ops track
    public static final char OPS_WRITE_CV_BIT       = 'b'; // Set/Clear a single CV bit on ops track
    public static final char PROG_WRITE_CV_BYTE     = 'W'; // Write CV byte on program track
    public static final char PROG_WRITE_CV_BIT      = 'B'; // Set/Clear a single CV bit on ops track
    public static final char PROG_READ_CV           = 'R'; // 3 different messages
    public static final char PROG_VERIFY_CV         = 'V'; // Verify CV byte on program track
    public static final char TRACK_POWER_ON         = '1'; // Track power ON
    public static final char TRACK_POWER_OFF        = '0'; // Track power OFF
    public static final char READ_TRACK_CURRENT     = 'c'; // Request current and voltage readings
    public static final char READ_CS_STATUS         = 's'; // Read status from command station
    public static final char READ_MAXNUMSLOTS       = '#'; // Read max number of slots supported by CS
    public static final char WRITE_TO_EEPROM_CMD    = 'E'; // Store settings to eeprom  -- NEW V1.1
    public static final char CLEAR_EEPROM_CMD       = 'e'; // Clear EEPROM settings     -- NEW V1.1
    public static final char QUERY_SENSOR_STATES_CMD= 'Q'; // Show all sensors -- NEW V1.2?
    public static final char ESTOP_ALL_CMD          = '!'; // Stops all locos on the track but leaves power on.

    // Special Commands not for normal use.  Diagnostic and Test Use Only
    public static final char WRITE_DCC_PACKET_MAIN  = 'M';
    public static final char WRITE_DCC_PACKET_PROG  = 'P';
    public static final char LIST_REGISTER_CONTENTS = 'L';
    public static final char DIAG_CMD               = 'D'; // Send various diagnostics commands
    public static final char CONTROL_CMD            = '/'; // Send various control commands (e.g. </START 1224 4>), replies via DIAG_REPLY

    // Message Replies
    public static final char THROTTLE_REPLY   = 'T'; // <T reg speed dir>
    public static final char TURNOUT_REPLY    = 'H'; // <H id throw> or <X>
    public static final char PROGRAM_REPLY    = 'r';
    public static final char VERIFY_REPLY     = 'v';
    public static final char STATUS_REPLY    = 'i';
    public static final char MAXNUMSLOTS_REPLY = '#';
    public static final char POWER_REPLY      = 'p';
    public static final char CURRENT_REPLY    = 'a';
    public static final char METER_REPLY      = 'c';
    public static final char SENSOR_REPLY     = 'Q';
    public static final char SENSOR_REPLY_H   = 'q';
    public static final char SENSOR_REPLY_L   = 'Q';
    public static final char OUTPUT_REPLY     = 'Y';
    public static final char WRITE_EEPROM_REPLY = 'e';
    public static final char MADC_FAIL_REPLY  = 'X';
    public static final char MADC_SUCCESS_REPLY = 'O';
    public static final char COMM_TYPE_REPLY = 'N';
    public static final char DIAG_REPLY      = '*';
    public static final char LOCO_STATE_REPLY= 'l';

    // Message / Reply Regexes
    public static final String THROTTLE_CMD_REGEX    = "t\\s*(\\d+)\\s+(\\d+)\\s+([-]*\\d+)\\s+([1,0])\\s*"; // <t REG CAB SPEED DIR>
    public static final String THROTTLE_V3_CMD_REGEX = "t\\s*(\\d+)\\s+([-]*\\d+)\\s+([01])\\s*"; // <t CAB SPEED DIR>
    public static final String FUNCTION_CMD_REGEX = "f\\s(\\d+)\\s(\\d+)\\s*(\\d+)?"; // <f ADDR BYTE1 (BYTE2)>
    public static final String FUNCTION_V4_CMD_REGEX="F\\s*([0-9]{1,4})\\s+([0-9]{1,2})\\s+([01])\\s*"; // <F CAB FUNC STATE>
    public static final String FORGET_CAB_CMD_REGEX ="-\\s*([0-9]{0,4})\\s*"; // <- [CAB]>
    public static final String ACCESSORY_CMD_REGEX = "a\\s(\\d+)\\s(\\d+)\\s([1,0])"; // <a ADDR SUBADDR ACTIVATE>
    public static final String TURNOUT_CMD_REGEX = "T\\s(\\d+)\\s([1,0])"; // <T ID THROW>
    public static final String TURNOUT_ADD_REGEX =     "T\\s(\\d+)\\s(\\d+)\\s(\\d+)";                          // <T id addr subaddr> (deprecated at 3.1.7, use DCC)
    public static final String TURNOUT_ADD_DCC_REGEX = "T\\s(\\d+)\\sDCC\\s(\\d+)\\s(\\d+)";                    // <T id DCC addr subaddr>
    public static final String TURNOUT_ADD_SERVO_REGEX="T\\s(\\d+)\\sSERVO\\s(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)";// <T id SERVO pin thrownposition closedposition profile>
    public static final String TURNOUT_ADD_VPIN_REGEX ="T\\s(\\d+)\\sVPIN\\s(\\d+)";                            // <T id VPIN pin>
    public static final String TURNOUT_DELETE_REGEX = "T\\s*(\\d+)"; // <T ID>
    public static final String TURNOUT_LIST_REGEX = "T"; // <T>
    public static final String SENSOR_ADD_REGEX = "S\\s(\\d+)\\s(\\d+)\\s([1,0])";
    public static final String SENSOR_DELETE_REGEX = "S\\s(\\d+)";
    public static final String SENSOR_LIST_REGEX = "S";
    public static final String OUTPUT_CMD_REGEX = "Z\\s(\\d+)\\s([1,0])"; // <Z ID STATE>
    public static final String OUTPUT_ADD_REGEX = "\\s*Z\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*"; // <Z ID PIN IFLAG>
    public static final String OUTPUT_DELETE_REGEX = "\\s*Z\\s*(\\d+)\\s*"; // <Z ID>
    public static final String OUTPUT_LIST_REGEX = "\\s*Z\\s*"; // <Z>
    public static final String QUERY_SENSOR_STATES_REGEX = "\\s*Q\\s*"; // <Q>
    public static final String LOCO_STATE_REGEX = "\\s*l\\s*(\\d+)\\s*([-]*\\d+)\\s*(\\d+)\\s*(\\d+)\\s*"; // <l loco slot speedByte functions>

    public static final String WRITE_TO_EEPROM_REGEX = "E";
    public static final String CLEAR_EEPROM_REGEX = "e";

    public static final String OPS_WRITE_BYTE_REGEX = "\\s*w\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*"; // TODO
    public static final String OPS_WRITE_BIT_REGEX = "\\s*b\\s*(\\d+)\\s+(\\d+)\\s+([0-7])\\s+([01])\\s*"; // TODO

    public static final String PROG_WRITE_BYTE_REGEX = "W\\s*(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)"; // <W cv value callbacknum callbacksub>
    public static final String PROG_WRITE_BYTE_V4_REGEX =  "W\\s*(\\d+)\\s(\\d+)"; // <W cv value>
    public static final String PROG_WRITE_BIT_REGEX = "B\\s*(\\d+)\\s([0-7])\\s([1,0])\\s(\\d+)\\s(\\d+)"; // <B cv bit value callbacknum callbacksub>
    public static final String PROG_WRITE_BIT_V4_REGEX =   "B\\s*(\\d+)\\s([0-7])\\s([01])"; // <B cv bit value> 
    public static final String PROG_READ_CV_REGEX = "R\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)"; // <R cv callbacknum callbacksub> 
    public static final String PROG_READ_CV_V4_REGEX = "R\\s*(\\d+)"; // <R cv> - use <V cv guess> instead
    public static final String PROG_READ_LOCOID_REGEX =  "R"; // <R>
    public static final String PROG_VERIFY_REGEX = "V\\s*(\\d+)\\s+(\\d+)\\s*"; //<V cv bytevalue>
    public static final String TRACK_POWER_REGEX = "\\s*[0,1]\\s*"; // <1> or <0>
    public static final String READ_TRACK_CURRENT_REGEX = "\\s*c\\s*"; // <c>
    public static final String READ_CS_STATUS_REGEX = "\\s*s\\s*";// <s>
    public static final String QUERY_SENSOR_REGEX = "\\s*[Q,q]\\s*(\\d+)\\s*";
    public static final String WRITE_DCC_PACKET_MAIN_REGEX = "M\\s+(\\d+)((\\s+[0-9a-fA-F]{1,2}){2,5})\\s*"; // M REG pktbyte1 pktbyte2 pktbyte3 ?pktbyte4 ?pktbyte5
    public static final String WRITE_DCC_PACKET_PROG_REGEX = "P\\s+(\\d+)((\\s+[0-9a-fA-F]{1,2}){2,5})\\s*"; // P REG pktbyte1 pktbyte2 pktbyte3 ?pktbyte4 ?pktbyte5
    public static final String LIST_REGISTER_CONTENTS_REGEX = "\\s*L\\s*";
    public static final String READ_MAXNUMSLOTS_REGEX = "\\s*#\\s*";
    public static final String DIAG_CMD_REGEX         = "\\s*D\\s.*"; //D alone or followed by various commands
    public static final String CONTROL_CMD_REGEX      = "\\s*/\\s.*"; // slash followed by various commands
    public static final String ESTOP_ALL_REGEX        = "\\s*!";

    // Reply Regexes
    public static final String THROTTLE_REPLY_REGEX =      "\\s*T\\s*(\\d+)\\s+([-]*\\d+)\\s+([1,0]).*";
    public static final String TURNOUT_REPLY_REGEX =       "\\s*H\\s*(\\d+)\\s+([1,0])\\s*";
    public static final String TURNOUT_DEF_REPLY_REGEX =   "\\s*H\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+([0|1]).*"; // <T id addr subaddr thrown> (deprecated at 3.1.7, use DCC)
    public static final String TURNOUT_DEF_DCC_REPLY_REGEX = "\\s*H\\s(\\d+)\\sDCC\\s(\\d+)\\s(\\d+)\\s+([0|1]).*";                    // <H id DCC addr subaddr thrown>
    public static final String TURNOUT_DEF_SERVO_REPLY_REGEX="\\s*H\\s(\\d+)\\sSERVO\\s(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)\\s+([0|1]).*";// <H id SERVO pin thrownposition closedposition profile thrown>
    public static final String TURNOUT_DEF_VPIN_REPLY_REGEX ="\\s*H\\s(\\d+)\\sVPIN\\s(\\d+)\\s+([0|1]).*";                            // <H id VPIN pin thrown>
    public static final String TURNOUT_DEF_LCN_REPLY_REGEX = "\\s*H\\s(\\d+)\\sLCN\\s+([0|1]).*";                                      // <H id LCN thrown>
    public static final String PROGRAM_REPLY_REGEX =       "\\s*r\\s*(\\d+)\\|(\\d+)\\|(\\d+)\\s+([-]*\\d+)\\s*"; //<r CALLBACKNUM|CALLBACKSUB|CV Value> deprecated
    public static final String PROGRAM_REPLY_V4_REGEX =    "\\s*r\\s*(\\d+)\\s+([-]*\\d+)\\s*"; // <r cv value> 
    public static final String PROGRAM_LOCOID_REPLY_REGEX = "\\s*r\\s+([-]*\\d+)\\s*"; //<r locoid> 
    public static final String PROGRAM_VERIFY_REPLY_REGEX = "\\s*v\\s*(\\d+)\\s*([-]*\\d+)\\s*"; //<v cv bytevalue>
    public static final String PROGRAM_BIT_REPLY_REGEX =   "\\s*r\\s*(\\d+)\\|(\\d+)\\|(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*";
    public static final String PROGRAM_BIT_REPLY_V4_REGEX ="\\s*r\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*";
    public static final String MAXNUMSLOTS_REPLY_REGEX =   "\\s*#\\s*(\\d+).*"; //<# 50>
    public static final String DIAG_REPLY_REGEX        =   "^\\*\\s*([\\S\\s]*)\\*$"; //matches anything between leading and trailing asterisks, left-trimmed
    public static final String CURRENT_REPLY_REGEX =       "\\s*a\\s*(\\d+).*";
    public static final String CURRENT_REPLY_NAMED_REGEX = "\\s*a\\s*(\\w*?[a-zA-Z])\\s*(\\d+).*";
    public static final String METER_REPLY_REGEX = " *c *(.+) +([-]*[\\d\\.]+) +([A-Z]) +(\\w+) +([\\d\\.]+) +([-]*[\\d\\.]+) +([\\d\\.]+) +([-]*[\\d\\.]+).*";

    public static final String TRACK_POWER_REPLY_REGEX =       "\\s*p\\s*([0,1])\\s*";
    public static final String TRACK_POWER_REPLY_NAMED_REGEX = "\\s*p\\s*(\\d+)\\s+(\\w+).*";
    public static final String SENSOR_REPLY_REGEX =          "\\s*[Qq]\\s*(\\d+)\\s*";
    public static final String SENSOR_DEF_REPLY_REGEX =      "\\s*Q\\s*(\\d+)\\s+(\\d+)\\s+([0|1]).*";
    public static final String SENSOR_ACTIVE_REPLY_REGEX =   "\\s*Q\\s*(\\d+)\\s*";
    public static final String SENSOR_INACTIVE_REPLY_REGEX = "\\s*q\\s*(\\d+)\\s*";
    public static final String OUTPUT_REPLY_REGEX =       "\\s*Y\\s*(\\d+)\\s+(\\d+)\\s*"; // <Y ID STATE>
    public static final String OUTPUT_DEF_REPLY_REGEX =   "\\s*Y\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+).*"; // <Y ID PIN IFLAG STATE>
    public static final String MADC_FAIL_REPLY_REGEX =    "\\s*X.*";
    public static final String MADC_SUCCESS_REPLY_REGEX = "\\s*O.*";
    public static final String STATUS_REPLY_REGEX =       "i(DCC\\+\\+[^:]*):(?:\\sBUILD)? (.*)"; // V1.0 / V1.1 / V1.2
    public static final String STATUS_REPLY_BSC_REGEX =   "i(DCC\\+\\+.*): V-(.*)\\+\\s\\/\\s(.*)"; // BaseStation Classic
    public static final String STATUS_REPLY_ESP32_REGEX = "iDCC\\+\\+.*ESP32.*: V-([\\d\\.]+)\\s+/\\s+(.*)"; // V1.0
    public static final String STATUS_REPLY_DCCEX_REGEX = "i(DCC-EX) V-([\\d\\.]*).*G-(.*)";
    public static final String WRITE_EEPROM_REPLY_REGEX = "\\s*e\\s*(\\d+)\\s+(\\d+)\\s+(\\d+).*";
    public static final String COMM_TYPE_REPLY_REGEX =    "\\s*N\\s*(\\d+):\\s+((SERIAL)|(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})).*";

    // Misc standard values
    public static final char WHITESPACE = ' ';
    public static final int MAX_SPEED = 126;
    public static final char FORWARD_DIR = '1';
    public static final char REVERSE_DIR = '0';

    public static final int REGISTER_1 = '1';

    public static final int FUNCTION_GROUP4_BYTE1 = 222;
    public static final int FUNCTION_GROUP5_BYTE1 = 223;

    public static final String TURNOUT_THROWN      = "1";
    public static final String TURNOUT_CLOSED      = "0";
    public static final String THROTTLE_FORWARD    = "1";
    public static final String THROTTLE_REVERSE    = "0";
    public static final String ACCESSORY_ON        = "1";
    public static final String ACCESSORY_OFF       = "0";
    public static final String POWER_ON            = "1";
    public static final String POWER_OFF           = "0";
    public static final String SENSOR_ON           = "1";
    public static final String SENSOR_OFF          = "0";
    public static final String SENSOR_FALLING_EDGE = "Q";
    public static final String SENSOR_RISING_EDGE  = "q";

    // Various min/max values for messages
    public static final int MAX_SENSOR_ID = 32767;
    public static final int MAX_SENSOR_NUMBER = 2048; // TODO: Check this
    public static final int MAX_ACC_DECODER_ADDRESS = 511;
    public static final int MAX_ACC_DECODER_SUBADDR = 3;
    // Max JMRI addr = ((MAX_ADDRESS - 1) * (MAX_SUBADDR+1)) + (MAX_SUBADDR) + 1
    public static final int MAX_ACC_DECODER_JMRI_ADDR = 2044;
    public static final int MAX_TURNOUT_ADDRESS = 32767;
    public static final int MAX_DIRECT_CV = 1024;
    public static final int MAX_DIRECT_CV_VAL = 255;
    public static final int MAX_CALLBACK_NUM = 32767;
    public static final int MAX_CALLBACK_SUB = 32767;
    public static final int MAX_LOCO_ADDRESS = 10293;
    public static final int MAX_CURRENT = 1024;
    public static final int METER_INTERVAL_MS = 1000;

    //Turnout types (added in DCC-EX 3.1.7)
    public static final String TURNOUT_TYPE_DCC = "DCC";
    public static final String TURNOUT_TYPE_SERVO="SERVO";
    public static final String TURNOUT_TYPE_VPIN ="VPIN";
    public static final String TURNOUT_TYPE_LCN = "LCN";

    public static final String OUTPUT_TYPE = "OUTPUT";
    public static final String SENSOR_TYPE = "SENSOR";

    //Property Keys
    public static final String PROP_TYPE =     "Type";
    public static final String PROP_ID   =     "ID";
    public static final String PROP_ADDRESS =  "Address";
    public static final String PROP_INDEX =    "Index";
    public static final String PROP_DCCADDRESS="DCC Address";
    public static final String PROP_PIN   =    "Pin";
    public static final String PROP_THROWNPOS= "ThrownPos";
    public static final String PROP_CLOSEDPOS= "ClosedPos";
    public static final String PROP_PROFILE  = "Profile";
    public static final String PROP_IFLAG  =   "IFlag";
    public static final String PROP_PULLUP =   "Pullup";

    //Referred to as Throttle commands for some reason
    public static final char THROTTLE_COMMANDS         = 'J'; // First char of Jx two-letter commands
    public static final char THROTTLE_COMMANDS_REPLY   = 'j'; // First char of jx two-letter responses
    public static final String TURNOUT_IDS             = "J T"; //Request turnout ID list
    public static final String TURNOUT_IDS_REGEX       = "^J\\s*T$"; // <J T> or <JT>
    public static final String TURNOUT_ID_REGEX        = "^J\\s*T\\s*(\\d+)$"; // <J T 123>
    public static final String TURNOUT_IDS_REPLY_REGEX = "^j\\s*T\\s*((?:\\s*\\d+)*)$"; // <j T 123 456 789>
    public static final String TURNOUT_ID_REPLY_REGEX  = "^j\\s*T\\s+(\\d+)\\s([C|T])\\s\\\"(.*)\\\""; // <jT 123 C "description">   
    public static final String TURNOUT_IMPL_REGEX      = "^T\\s+(\\d+)\\s+X$"; // <T 123 X> Note: may be dropped from DCC-EX
    public static final String ROSTER_IDS             = "J R"; //Request Roster ID list
    public static final String ROSTER_IDS_REGEX       = "^J\\s*R$"; // <J R> or <JR>
    public static final String ROSTER_ID_REGEX        = "^J\\s*R\\s*(\\d+)$"; // <J R 123>
    public static final String ROSTER_IDS_REPLY_REGEX = "^j\\s*R\\s*((?:\\s*\\d+)*)$"; // <j R 123 456 789>
    public static final String ROSTER_ID_REPLY_REGEX  = "^j\\s*R\\s+(\\d+)\\s\\\"(.*)\\\"\\s\\\"(.*)\\\""; // <jR 123 "description" "functionkeystring">   
    public static final String AUTOMATION_IDS             = "J A"; //Request Automation ID list
    public static final String AUTOMATION_IDS_REGEX       = "^J\\s*A$"; // <J A> or <JA>
    public static final String AUTOMATION_ID_REGEX        = "^J\\s*A\\s*(\\d+)$"; // <J A 123>
    public static final String AUTOMATION_IDS_REPLY_REGEX = "^j\\s*A\\s*((?:\\s*\\d+)*)$"; // <j A 123 456 789>
    public static final String AUTOMATION_ID_REPLY_REGEX  = "^j\\s*A\\s+(\\d+)\\s([A|R])\\s\\\"(.*)\\\""; // <jA 123 R "description">   
    public static final String CURRENT_MAXES              = "J G"; //Request list of current maximums (always mA)
    public static final String CURRENT_MAXES_REGEX        = "^J\\s*G$"; // <J G> or <JG>
    public static final String CURRENT_MAXES_REPLY_REGEX  = "^j\\s*G\\s*((?:\\s*\\d+)*)$"; // <j A 123 456 789>
    public static final String CURRENT_VALUES             = "J I"; //Request list of current values (always mA)
    public static final String CURRENT_VALUES_REGEX       = "^J\\s*I$"; // <J I> or <JI>
    public static final String CURRENT_VALUES_REPLY_REGEX = "^j\\s*I\\s*((?:\\s*[-]?\\d+)*)$"; // <j I 123 456 789> or <jI -1 123>
    public static final String CLOCK_REQUEST_TIME      = "J C"; //<J C> Request current time from DCC-EX
    public static final String CLOCK_REQUEST_TIME_REGEX= "^J\\s*C$"; // <J C> or <JC>
    public static final String CLOCK_SET_REGEX         = "^J\\s*C\\s+(\\d+)\\s*(\\d*)$"; // <J C 123 4> or <j C 124> outgoing set to 123min past midnight, rate 4
    public static final String CLOCK_REPLY_REGEX       = "^j\\s*C\\s+(\\d+)\\s*(\\d*)$"; // <j C 123 4> or <j C 124> incoming version of above 
    
    //Track Manager (in 5.x)
    public static final char   TRACKMANAGER_CMD        = '=';
    public static final String TRACKMANAGER_CMD_REGEX  = "=";
    public static final String TRACKMANAGER_REPLY_REGEX= "\\s*=\\s*([A-H])\\s+([A-Z]+)\\s*(\\d*)"; //<= A PROG> or <= B DC 1234>  

    //LCD message
    public static final char   LCD_TEXT_CMD         = '@'; //request that LCD messages be sent to this instance of JMRI
    public static final String LCD_TEXT_CMD_REGEX   = "@";
    public static final char   LCD_TEXT_REPLY       = '@';
    public static final String LCD_TEXT_REPLY_REGEX = "^\\s*@\\s*(\\d+)\\s+(\\d+)\\s+\\\"(.*)\\\"$"; //<@ 0 3 "message text"> where 0 is display# and 3 is line#  

}
