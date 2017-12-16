package jmri.jmrix.dccpp;

/**
 * DCCppConstants.java
 *
 * Description: Constants to represent values seen in DCC++ traffic
 *
 * @author Paul Bender Copyright (C) 2003-2009
 * @author Mark Underwood Copyright (C) 2015
 *
 * Variable prefix abreviation keys: ACC_ is for accessory messages BC_ is for
 * broadcast messages CS_ is for command station messages PROG_ is for
 * programing related messages LOCO_ is for locomotive related commands
 * OPS_MODE_ is for operations mode programing commands LI_ is for commands that
 * are for messages to and from the computer interface LI101_ is for commands
 * specific to the LI101
 *
 * A few variables don't have a prefix. The name should be self explanitory, but
 * a prefix may be added later.
 */
public final class DCCppConstants {

    public final static int MAX_MESSAGE_SIZE = 30;
    public final static int MAX_REPLY_SIZE = 256;
    public final static int MAX_MAIN_REGISTERS = 12;
    public final static int REGISTER_UNALLOCATED = -1;
    public final static int NO_REGISTER_FREE = -1; // TODO: Should this be a unique value?
    
    // DCC++ over TCP Port Number
    public final static int DCCPP_OVER_TCP_PORT = 1235;
    
    // Communications Port Info
    public final static int COMM_TYPE_SERIAL = 0;
    public final static int COMM_TYPE_ENET = 1;
    public final static int COMM_TYPE_ENETV2 = 2;

    // Command Station Types
    public final static int DCCPP_UNO_1_0 = 1;
    public final static int DCCPP_ARDUINO_1_1 = 2;
    public final static String CommandStationNames[] = {
 "DCCPP Arduino Uno v1.0",
 "DCCPP Arduino V1.1",
    };

    // DCC++ Command OpCodes

    public final static char THROTTLE_CMD           = 't'; // Throttle command <t reg cab speed dir>
    public final static char FUNCTION_CMD           = 'f'; // F0-F28 <f cab byte1 [byte2]>
    public final static char ACCESSORY_CMD          = 'a'; // Stationary accessory decoder <a addr subaddr activate>
    public final static char TURNOUT_CMD            = 'T'; // Turnout command <T id throw> -- NEW versions V1.1
    public final static char SENSOR_CMD             = 'S'; // Sensor command -- NEW V1.1
    public final static char OUTPUT_CMD             = 'Z'; // Output command -- NEW V1.2?
    public final static char OPS_WRITE_CV_BYTE      = 'w'; // Write CV byte on ops track
    public final static char OPS_WRITE_CV_BIT       = 'b'; // Set/Clear a single CV bit on ops track
    public final static char PROG_WRITE_CV_BYTE     = 'W'; // Write CV byte on program track
    public final static char PROG_WRITE_CV_BIT      = 'B'; // Set/Clear a single CV bit on ops track
    public final static char PROG_READ_CV           = 'R'; // Read CV byte on program track
    public final static char TRACK_POWER_ON         = '1'; // Track power ON
    public final static char TRACK_POWER_OFF        = '0'; // Track power OFF
    public final static char READ_TRACK_CURRENT     = 'c'; // Read current draw on ops track
    public final static char READ_CS_STATUS         = 's'; // Read status from command station
//    public final static char QUERY_SENSOR_STATE     = 'q'; // Query state of sensor
    public final static char WRITE_TO_EEPROM_CMD    = 'E'; // Store settings to eeprom  -- NEW V1.1
    public final static char CLEAR_EEPROM_CMD       = 'e'; // Clear EEPROM settings     -- NEW V1.1
    public final static char QUERY_SENSOR_STATES_CMD = 'Q'; // Show all sensors -- NEW V1.2?

    // Special Commands not for normal use.  Diagnostic and Test Use Only
    public final static char WRITE_DCC_PACKET_MAIN  = 'M';
    public final static char WRITE_DCC_PACKET_PROG  = 'P';
    public final static char GET_FREE_MEMORY        = 'F';
    public final static char LIST_REGISTER_CONTENTS = 'L';
    public final static char ENTER_DIAG_MODE_CMD    = 'D'; // Enter Diagnostics mode -- NEW V1.2?
 
    // Message Replies
    public final static char THROTTLE_REPLY   = 'T'; // <T reg speed dir>
    public final static char TURNOUT_REPLY    = 'H'; // <H id throw> or <X>
    public final static char PROGRAM_REPLY    = 'r';
    public final static char STATUS_REPLY    = 'i';
    public final static char POWER_REPLY      = 'p';
    public final static char CURRENT_REPLY    = 'a';
    public final static char MEMORY_REPLY     = 'f';
//    public final static char LISTPACKET_REPLY = 'M';
    public final static char SENSOR_REPLY     = 'Q';
    public final static char SENSOR_REPLY_H   = 'q';
    public final static char SENSOR_REPLY_L   = 'Q';
    public final static char OUTPUT_REPLY     = 'Y';
    public final static char WRITE_EEPROM_REPLY = 'e';
    public final static char MADC_FAIL_REPLY  = 'X';
    public final static char MADC_SUCCESS_REPLY = 'O';
    public final static char COMM_TYPE_REPLY = 'N';

    // Message / Reply Regexes
    public final static String THROTTLE_CMD_REGEX = "t\\s*(\\d+)\\s+(\\d+)\\s+([-]*\\d+)\\s+([1,0])\\s*"; // <t REG CAB SPEED DIR>
    public final static String FUNCTION_CMD_REGEX = "f\\s(\\d+)\\s(\\d+)\\s*(\\d+)?"; // <f ADDR BYTE1 (BYTE2)>
    public final static String ACCESSORY_CMD_REGEX = "a\\s(\\d+)\\s(\\d+)\\s([1,0])"; // <a ADDR SUBADDR ACTIVATE>
    public final static String TURNOUT_CMD_REGEX = "T\\s(\\d+)\\s([1,0])"; // <T ID THROW>
    public final static String TURNOUT_ADD_REGEX = "T\\s(\\d+)\\s(\\d+)\\s(\\d+)"; // <T ID ADDR SUBADDR>
    public final static String TURNOUT_DELETE_REGEX = "T\\s*(\\d+)"; // <T ID>
    public final static String TURNOUT_LIST_REGEX = "T"; // <T>
    public final static String SENSOR_ADD_REGEX = "S\\s(\\d+)\\s(\\d+)\\s([1,0])";
    public final static String SENSOR_DELETE_REGEX = "S\\s(\\d+)";
    public final static String SENSOR_LIST_REGEX = "S";
    //public final static String OUTPUT_CMD_REGEX = "\\s*Z\\s*(\\d+)\\s+(\\d+)\\s*"; // <Z ID STATE>
    public final static String OUTPUT_CMD_REGEX = "Z\\s(\\d+)\\s([1,0])"; // <Z ID STATE>
    public final static String OUTPUT_ADD_REGEX = "\\s*Z\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*"; // <Z ID PIN IFLAG>
    public final static String OUTPUT_DELETE_REGEX = "\\s*Z\\s*(\\d+)\\s*"; // <Z ID>
    public final static String OUTPUT_LIST_REGEX = "\\s*Z\\s*"; // <Z>
    public final static String QUERY_SENSOR_STATES_REGEX = "\\s*Q\\s*"; // <Q>

    public final static String WRITE_TO_EEPROM_REGEX = "E";
    public final static String CLEAR_EEPROM_REGEX = "e";

    public final static String OPS_WRITE_BYTE_REGEX = "\\s*w\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*"; // TODO
    public final static String OPS_WRITE_BIT_REGEX = "\\s*b\\s*(\\d+)\\s+(\\d+)\\s+([0-7])\\s+([01])\\s*"; // TODO
    
    public final static String PROG_WRITE_BYTE_REGEX = "W\\s*(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)";
    public final static String PROG_WRITE_BIT_REGEX = "B\\s*(\\d+)\\s([0-7])\\s([1,0])\\s(\\d+)\\s(\\d+)";
    public final static String PROG_READ_REGEX = "R\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)";
    public final static String TRACK_POWER_REGEX = "\\s*[0,1]\\s*"; // <1> or <0>
    public final static String READ_TRACK_CURRENT_REGEX = "\\s*c\\s*"; // <c>
    public final static String READ_CS_STATUS_REGEX = "\\s*s\\s*";// <s>
    public final static String QUERY_SENSOR_REGEX = "\\s*[Q,q]\\s*(\\d+)\\s*";
    public final static String WRITE_DCC_PACKET_MAIN_REGEX = "M\\s+(\\d+)(\\s+[0-9a-fA-F]{1,2})(\\s+[0-9a-fA-F]{1,2})(\\s+[0-9a-fA-F]{1,2})(\\s+[0-9a-fA-F]{1,2})(\\s+[0-9a-fA-F]{1,2})\\s*"; // M REG pktbyte1 pktbyte2 pktbyte3 ?pktbyte4 ?pktbyte5
    public final static String WRITE_DCC_PACKET_PROG_REGEX = "P\\s+(\\d+)(\\s+[0-9a-fA-F]{1,2})(\\s+[0-9a-fA-F]{1,2})(\\s+[0-9a-fA-F]{1,2})(\\s+[0-9a-fA-F]{1,2})(\\s+[0-9a-fA-F]{1,2})\\s*"; // P REG pktbyte1 pktbyte2 pktbyte3 ?pktbyte4 ?pktbyte5
    public final static String GET_FREE_MEMORY_REGEX = "\\s*f\\s*";
    public final static String LIST_REGISTER_CONTENTS_REGEX = "\\s*L\\s*";
    public final static String ENTER_DIAG_MODE_REGEX = "\\s*D\\s*";

    // Reply Regexes
    public final static String THROTTLE_REPLY_REGEX = "\\s*T\\s*(\\d+)\\s+([-]*\\d+)\\s+([1,0])\\s*";
    public final static String TURNOUT_REPLY_REGEX = "\\s*H\\s*(\\d+)\\s+([1,0])\\s*";
    public final static String TURNOUT_DEF_REPLY_REGEX = "\\s*H\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+([0|1])\\s*";
    public final static String LIST_TURNOUTS_REPLY_REGEX = "\\s*H\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+([1,0])\\s*";
    public final static String LIST_SENSORS_REPLY_REGEX = "\\s*Q\\s*(\\d+)\\s+(\\d+)\\s+([0,1])\\s*";
    public final static String PROGRAM_REPLY_REGEX = "\\s*r\\s*(\\d+)\\|(\\d+)\\|(\\d+)\\s+([-]*\\d+)\\s*";
    public final static String PROGRAM_BIT_REPLY_REGEX = "\\s*r\\s*(\\d+)\\|(\\d+)\\|(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*";
    public final static String CURRENT_REPLY_REGEX = "\\s*a\\s*(\\d+)";
    public final static String CURRENT_REPLY_NAMED_REGEX = "\\s*a\\s*(\\w+)\\s*(\\d+)";
    public final static String TRACK_POWER_REPLY_REGEX = "\\s*p\\s*([0,1])\\s*";
    public final static String TRACK_POWER_REPLY_NAMED_REGEX = "\\s*p\\s*(\\d+)\\s+(\\w+)\\s*";
    public final static String SENSOR_REPLY_REGEX = "\\s*[Qq]\\s*(\\d+)\\s*";
    public final static String SENSOR_DEF_REPLY_REGEX = "\\s*Q\\s*(\\d+)\\s+(\\d+)\\s+([0|1])\\s*";
    public final static String SENSOR_ACTIVE_REPLY_REGEX = "\\s*Q\\s*(\\d+)\\s*";
    public final static String SENSOR_INACTIVE_REPLY_REGEX = "\\s*q\\s*(\\d+)\\s*";
    public final static String BROKEN_SENSOR_REPLY_REGEX = "\\s*(\\d+)\\s*";
    public final static String OUTPUT_REPLY_REGEX = "\\s*Y\\s*(\\d+)\\s+(\\d+)\\s*"; // <Y ID STATE>
    public final static String OUTPUT_LIST_REPLY_REGEX = "\\s*Y\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*"; // <Y ID PIN IFLAG STATE>
    public final static String MADC_FAIL_REPLY_REGEX = "\\s*X\\s*";
    public final static String MADC_SUCCESS_REPLY_REGEX = "\\s*O\\s*";
//    public final static String STATUS_REPLY_REGEX = "i(DCC\\+\\+.*): BUILD (.*)"; // V1.0
//    public final static String STATUS_REPLY_REGEX = "i(DCC\\+\\+[^:]*): BUILD (.*)"; // V1.0 / V1.1
    public final static String STATUS_REPLY_REGEX = "i(DCC\\+\\+[^:]*):(?:\\sBUILD)? (.*)"; // V1.0 / V1.1 / V1.2
    public final static String STATUS_REPLY_ESP32_REGEX = "iDCC\\+\\+.*ESP32.*: V-([\\d\\.]+)\\s+/\\s+(.*)"; // V1.0
    //public final static String STATUS_REPLY_REGEX = "i(DCC\\+\\+\\s?.*):\\s?(?:BUILD)? (.*)"; // V1.0 / V1.1 / V1.2
    public final static String FREE_MEMORY_REPLY_REGEX = "\\s*f\\s*(\\d+)\\s*";
    public final static String WRITE_EEPROM_REPLY_REGEX = "\\s*e\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*";
    public final static String COMM_TYPE_REPLY_REGEX = "\\s*N\\s*(\\d+):\\s+((SERIAL)|(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}))\\s*";

    // Misc standard values
    public final static char WHITESPACE = ' ';
    public final static int MAX_SPEED = 126;
    public final static char FORWARD_DIR = '1';
    public final static char REVERSE_DIR = '0';

    public final static int REGISTER_1 = '1';

    public final static int FUNCTION_GROUP4_BYTE1 = 222;
    public final static int FUNCTION_GROUP5_BYTE1 = 223;

    public final static String TURNOUT_THROWN      = "1";
    public final static String TURNOUT_CLOSED      = "0";
    public final static String THROTTLE_FORWARD    = "1";
    public final static String THROTTLE_REVERSE    = "0";
    public final static String ACCESSORY_ON        = "1";
    public final static String ACCESSORY_OFF       = "0";
    public final static String POWER_ON            = "1";
    public final static String POWER_OFF           = "0";
    public final static String SENSOR_ON           = "1";
    public final static String SENSOR_OFF          = "0";
    public final static String SENSOR_FALLING_EDGE = "Q";
    public final static String SENSOR_RISING_EDGE  = "q";

    // Various min/max values for messages
    public final static int MAX_SENSOR_ID = 32767;
    public final static int MAX_SENSOR_NUMBER = 2048; // TODO: Check this
    public final static int MAX_ACC_DECODER_ADDRESS = 511;
    public final static int MAX_ACC_DECODER_SUBADDR = 3;
    // Max JMRI addr = ((MAX_ADDRESS - 1) * (MAX_SUBADDR+1)) + (MAX_SUBADDR) + 1
    public final static int MAX_ACC_DECODER_JMRI_ADDR = 2044;
    public final static int MAX_TURNOUT_ADDRESS = 32767;
    public final static int MAX_DIRECT_CV = 1024;
    public final static int MAX_DIRECT_CV_VAL = 255;
    public final static int MAX_CALLBACK_NUM = 32767;
    public final static int MAX_CALLBACK_SUB = 32767;
    public final static int MAX_LOCO_ADDRESS = 10293;
    public final static int MAX_CURRENT = 1024;
    public final static int METER_INTERVAL_MS = 1000;

}
