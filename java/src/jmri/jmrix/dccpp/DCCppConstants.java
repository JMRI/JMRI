package jmri.jmrix.dccpp;

/**
 * DCCppConstants.java
 *
 * Description: Constants to represent values seen in DCC++ traffic
 *
 * @author Paul Bender Copyright (C) 2003-2009
 * @author Mark Underwood Copyright (C) 2015
 * @author Harald Barth Copyright (C) 2019
 *
 * Variable prefix abreviation keys: ACC_ is for accessory messages BC_ is for
 * broadcast messages CS_ is for command station messages PROG_ is for
 * programming related messages LOCO_ is for locomotive related commands
 * OPS_MODE_ is for operations mode programming commands LI_ is for commands that
 * are for messages to and from the computer interface LI101_ is for commands
 * specific to the LI101
 *
 * A few variables don't have a prefix. The name should be self explanitory, but
 * a prefix may be added later.
 */
public final class DCCppConstants {

    private DCCppConstants() {
        // final class of static values, no values to construct.
    }

    public static final int MAX_MESSAGE_SIZE = 30;
    public static final int MAX_REPLY_SIZE = 256;
    public static final int MAX_MAIN_REGISTERS = 12;
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
    public static final String[] CommandStationNames = {
     "DCCPP Arduino Uno v1.0",
     "DCCPP Arduino V1.1",
    };

    // DCC++ Command OpCodes

    public static final char THROTTLE_CMD           = 't'; // Throttle command <t reg cab speed dir>
    public static final char FUNCTION_CMD           = 'f'; // F0-F28 <f cab byte1 [byte2]>
    public static final char ACCESSORY_CMD          = 'a'; // Stationary accessory decoder <a addr subaddr activate>
    public static final char TURNOUT_CMD            = 'T'; // Turnout command <T id throw> -- NEW versions V1.1
    public static final char SENSOR_CMD             = 'S'; // Sensor command -- NEW V1.1
    public static final char OUTPUT_CMD             = 'Z'; // Output command -- NEW V1.2?
    public static final char OPS_WRITE_CV_BYTE      = 'w'; // Write CV byte on ops track
    public static final char OPS_WRITE_CV_BIT       = 'b'; // Set/Clear a single CV bit on ops track
    public static final char PROG_WRITE_CV_BYTE     = 'W'; // Write CV byte on program track
    public static final char PROG_WRITE_CV_BIT      = 'B'; // Set/Clear a single CV bit on ops track
    public static final char PROG_READ_CV           = 'R'; // Read CV byte on program track
    public static final char TRACK_POWER_ON         = '1'; // Track power ON
    public static final char TRACK_POWER_OFF        = '0'; // Track power OFF
    public static final char READ_TRACK_CURRENT     = 'c'; // Read current draw on ops track
    public static final char READ_CS_STATUS         = 's'; // Read status from command station
    public static final char READ_CS_MAXNUMSLOTS    = '#'; // Read max number of slots supported by CS
//    public static final char QUERY_SENSOR_STATE     = 'q'; // Query state of sensor
    public static final char WRITE_TO_EEPROM_CMD    = 'E'; // Store settings to eeprom  -- NEW V1.1
    public static final char CLEAR_EEPROM_CMD       = 'e'; // Clear EEPROM settings     -- NEW V1.1
    public static final char QUERY_SENSOR_STATES_CMD = 'Q'; // Show all sensors -- NEW V1.2?

    // Special Commands not for normal use.  Diagnostic and Test Use Only
    public static final char WRITE_DCC_PACKET_MAIN  = 'M';
    public static final char WRITE_DCC_PACKET_PROG  = 'P';
    public static final char GET_FREE_MEMORY        = 'F';
    public static final char LIST_REGISTER_CONTENTS = 'L';
    public static final char ENTER_DIAG_MODE_CMD    = 'D'; // Enter Diagnostics mode -- NEW V1.2?
 
    // Message Replies
    public static final char THROTTLE_REPLY   = 'T'; // <T reg speed dir>
    public static final char TURNOUT_REPLY    = 'H'; // <H id throw> or <X>
    public static final char PROGRAM_REPLY    = 'r';
    public static final char STATUS_REPLY    = 'i';
    public static final char MAXNUMSLOTS_REPLY = '#';
    public static final char POWER_REPLY      = 'p';
    public static final char CURRENT_REPLY    = 'a';
    public static final char MEMORY_REPLY     = 'f';
//    public static final char LISTPACKET_REPLY = 'M';
    public static final char SENSOR_REPLY     = 'Q';
    public static final char SENSOR_REPLY_H   = 'q';
    public static final char SENSOR_REPLY_L   = 'Q';
    public static final char OUTPUT_REPLY     = 'Y';
    public static final char WRITE_EEPROM_REPLY = 'e';
    public static final char MADC_FAIL_REPLY  = 'X';
    public static final char MADC_SUCCESS_REPLY = 'O';
    public static final char COMM_TYPE_REPLY = 'N';

    // Message / Reply Regexes
    public static final String THROTTLE_CMD_REGEX = "t\\s*(\\d+)\\s+(\\d+)\\s+([-]*\\d+)\\s+([1,0])\\s*"; // <t REG CAB SPEED DIR>
    public static final String FUNCTION_CMD_REGEX = "f\\s(\\d+)\\s(\\d+)\\s*(\\d+)?"; // <f ADDR BYTE1 (BYTE2)>
    public static final String ACCESSORY_CMD_REGEX = "a\\s(\\d+)\\s(\\d+)\\s([1,0])"; // <a ADDR SUBADDR ACTIVATE>
    public static final String TURNOUT_CMD_REGEX = "T\\s(\\d+)\\s([1,0])"; // <T ID THROW>
    public static final String TURNOUT_ADD_REGEX = "T\\s(\\d+)\\s(\\d+)\\s(\\d+)"; // <T ID ADDR SUBADDR>
    public static final String TURNOUT_DELETE_REGEX = "T\\s*(\\d+)"; // <T ID>
    public static final String TURNOUT_LIST_REGEX = "T"; // <T>
    public static final String SENSOR_ADD_REGEX = "S\\s(\\d+)\\s(\\d+)\\s([1,0])";
    public static final String SENSOR_DELETE_REGEX = "S\\s(\\d+)";
    public static final String SENSOR_LIST_REGEX = "S";
    //public static final String OUTPUT_CMD_REGEX = "\\s*Z\\s*(\\d+)\\s+(\\d+)\\s*"; // <Z ID STATE>
    public static final String OUTPUT_CMD_REGEX = "Z\\s(\\d+)\\s([1,0])"; // <Z ID STATE>
    public static final String OUTPUT_ADD_REGEX = "\\s*Z\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*"; // <Z ID PIN IFLAG>
    public static final String OUTPUT_DELETE_REGEX = "\\s*Z\\s*(\\d+)\\s*"; // <Z ID>
    public static final String OUTPUT_LIST_REGEX = "\\s*Z\\s*"; // <Z>
    public static final String QUERY_SENSOR_STATES_REGEX = "\\s*Q\\s*"; // <Q>

    public static final String WRITE_TO_EEPROM_REGEX = "E";
    public static final String CLEAR_EEPROM_REGEX = "e";

    public static final String OPS_WRITE_BYTE_REGEX = "\\s*w\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*"; // TODO
    public static final String OPS_WRITE_BIT_REGEX = "\\s*b\\s*(\\d+)\\s+(\\d+)\\s+([0-7])\\s+([01])\\s*"; // TODO
    
    public static final String PROG_WRITE_BYTE_REGEX = "W\\s*(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)";
    public static final String PROG_WRITE_BIT_REGEX = "B\\s*(\\d+)\\s([0-7])\\s([1,0])\\s(\\d+)\\s(\\d+)";
    public static final String PROG_READ_REGEX = "R\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)";
    public static final String TRACK_POWER_REGEX = "\\s*[0,1]\\s*"; // <1> or <0>
    public static final String READ_TRACK_CURRENT_REGEX = "\\s*c\\s*"; // <c>
    public static final String READ_CS_STATUS_REGEX = "\\s*s\\s*";// <s>
    public static final String QUERY_SENSOR_REGEX = "\\s*[Q,q]\\s*(\\d+)\\s*";
    public static final String WRITE_DCC_PACKET_MAIN_REGEX = "M\\s+(\\d+)((\\s+[0-9a-fA-F]{1,2}){2,5})\\s*"; // M REG pktbyte1 pktbyte2 pktbyte3 ?pktbyte4 ?pktbyte5
    public static final String WRITE_DCC_PACKET_PROG_REGEX = "P\\s+(\\d+)((\\s+[0-9a-fA-F]{1,2}){2,5})\\s*"; // P REG pktbyte1 pktbyte2 pktbyte3 ?pktbyte4 ?pktbyte5
    public static final String GET_FREE_MEMORY_REGEX = "\\s*f\\s*";
    public static final String LIST_REGISTER_CONTENTS_REGEX = "\\s*L\\s*";
    public static final String ENTER_DIAG_MODE_REGEX = "\\s*D\\s*";
    public static final String READ_CS_MAXNUMSLOTS_REGEX = "\\s*#\\s*";

    // Reply Regexes
    public static final String THROTTLE_REPLY_REGEX = "\\s*T\\s*(\\d+)\\s+([-]*\\d+)\\s+([1,0])\\s*";
    public static final String TURNOUT_REPLY_REGEX = "\\s*H\\s*(\\d+)\\s+([1,0])\\s*";
    public static final String TURNOUT_DEF_REPLY_REGEX = "\\s*H\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+([0|1])\\s*";
    public static final String LIST_TURNOUTS_REPLY_REGEX = "\\s*H\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+([1,0])\\s*";
    public static final String LIST_SENSORS_REPLY_REGEX = "\\s*Q\\s*(\\d+)\\s+(\\d+)\\s+([0,1])\\s*";
    public static final String PROGRAM_REPLY_REGEX = "\\s*r\\s*(\\d+)\\|(\\d+)\\|(\\d+)\\s+([-]*\\d+)\\s*";
    public static final String PROGRAM_BIT_REPLY_REGEX = "\\s*r\\s*(\\d+)\\|(\\d+)\\|(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*";
    public static final String MAXNUMSLOTS_REPLY_REGEX = "\\s*#\\s*(\\d+)\\s*";
    public static final String CURRENT_REPLY_REGEX = "\\s*a\\s*(\\d+)\\s*";
    public static final String CURRENT_REPLY_NAMED_REGEX = "\\s*a\\s*(\\w*?[a-zA-Z])\\s*(\\d+)\\s*";
    public static final String TRACK_POWER_REPLY_REGEX = "\\s*p\\s*([0,1])\\s*";
    public static final String TRACK_POWER_REPLY_NAMED_REGEX = "\\s*p\\s*(\\d+)\\s+(\\w+)\\s*";
    public static final String SENSOR_REPLY_REGEX = "\\s*[Qq]\\s*(\\d+)\\s*";
    public static final String SENSOR_DEF_REPLY_REGEX = "\\s*Q\\s*(\\d+)\\s+(\\d+)\\s+([0|1])\\s*";
    public static final String SENSOR_ACTIVE_REPLY_REGEX = "\\s*Q\\s*(\\d+)\\s*";
    public static final String SENSOR_INACTIVE_REPLY_REGEX = "\\s*q\\s*(\\d+)\\s*";
    public static final String BROKEN_SENSOR_REPLY_REGEX = "\\s*(\\d+)\\s*";
    public static final String OUTPUT_REPLY_REGEX = "\\s*Y\\s*(\\d+)\\s+(\\d+)\\s*"; // <Y ID STATE>
    public static final String OUTPUT_LIST_REPLY_REGEX = "\\s*Y\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*"; // <Y ID PIN IFLAG STATE>
    public static final String MADC_FAIL_REPLY_REGEX = "\\s*X\\s*";
    public static final String MADC_SUCCESS_REPLY_REGEX = "\\s*O\\s*";
//    public static final String STATUS_REPLY_REGEX = "i(DCC\\+\\+.*): BUILD (.*)"; // V1.0
//    public static final String STATUS_REPLY_REGEX = "i(DCC\\+\\+[^:]*): BUILD (.*)"; // V1.0 / V1.1
    public static final String STATUS_REPLY_REGEX = "i(DCC\\+\\+[^:]*):(?:\\sBUILD)? (.*)"; // V1.0 / V1.1 / V1.2
    public static final String STATUS_REPLY_ESP32_REGEX = "iDCC\\+\\+.*ESP32.*: V-([\\d\\.]+)\\s+/\\s+(.*)"; // V1.0
    //public static final String STATUS_REPLY_REGEX = "i(DCC\\+\\+\\s?.*):\\s?(?:BUILD)? (.*)"; // V1.0 / V1.1 / V1.2
    public static final String FREE_MEMORY_REPLY_REGEX = "\\s*f\\s*(\\d+)\\s*";
    public static final String WRITE_EEPROM_REPLY_REGEX = "\\s*e\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*";
    public static final String COMM_TYPE_REPLY_REGEX = "\\s*N\\s*(\\d+):\\s+((SERIAL)|(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}))\\s*";

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

}
