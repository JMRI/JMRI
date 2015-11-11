/**
 * DCCppConstants.java
 *
 * Description:	Constants to represent values seen in DCC++ traffic
 *
 * @author	Paul Bender Copyright (C) 2003-2009
 * @author	Mark Underwood Copyright (C) 2015
 * @version $ Revision: $
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
package jmri.jmrix.dccpp;

public final class DCCppConstants {

    public final static int MAX_MESSAGE_SIZE = 30;
    public final static int MAX_REPLY_SIZE = 256;
    public final static int MAX_MAIN_REGISTERS = 12;
    public final static int REGISTER_UNALLOCATED = -1;
    public final static int NO_REGISTER_FREE = -1; // TODO: Should this be a unique value?

    // DCC++ Command OpCodes

    public final static char THROTTLE_CMD           = 't'; // Throttle command
    public final static char FUNCTION_CMD           = 'f'; // F0-F28
    public final static char ACCESSORY_CMD          = 'a'; // Stationary accessory decoder
    public final static char TURNOUT_CMD            = 'T'; // Turnout command
    public final static char OPS_WRITE_CV_BYTE      = 'w'; // Write CV byte on ops track
    public final static char OPS_WRITE_CV_BIT       = 'b'; // Set/Clear a single CV bit on ops track
    public final static char PROG_WRITE_CV_BYTE     = 'W'; // Write CV byte on program track
    public final static char PROG_WRITE_CV_BIT      = 'B'; // Set/Clear a single CV bit on ops track
    public final static char PROG_READ_CV           = 'R'; // Read CV byte on program track
    public final static char TRACK_POWER_ON         = '1'; // Track power ON
    public final static char TRACK_POWER_OFF        = '0'; // Track power OFF
    public final static char READ_TRACK_CURRENT     = 'c'; // Read current draw on ops track
    public final static char READ_CS_STATUS         = 's'; // Read status from command station

    // Special Commands not for normal use.  Diagnostic and Test Use Only
    public final static char WRITE_DCC_PACKET_MAIN  = 'M';
    public final static char WRITE_DCC_PACKET_PROG  = 'P';
    public final static char GET_FREE_MEMORY        = 'F';
    public final static char LIST_REGISTER_CONTENTS = 'L';
	
    // Message Replies
    public final static char THROTTLE_REPLY   = 'T';
    public final static char TURNOUT_REPLY    = 'H';
    public final static char PROGRAM_REPLY    = 'r';
    public final static char VERSION_REPLY    = 'i';
    public final static char POWER_REPLY      = 'p';
    public final static char CURRENT_REPLY    = 'a';
    public final static char MEMORY_REPLY     = 'f';
    public final static char LISTPACKET_REPLY = 'L';

    // Message / Reply Regexes
    public final static String THROTTLE_CMD_REGEX = "t\\s(\\d+)\\s(\\d+)\\s([-]*\\d+)\\s([1,0])";
    public final static String FUNCTION_CMD_REGEX = "f\\s(\\d+)\\s(\\d+)\\s*(\\d+)?";
    public final static String ACCESSORY_CMD_REGEX = "a\\s(\\d+)\\s(\\d+)\\s([1,0])";
    public final static String TURNOUT_CMD_REGEX = "T\\s(\\d+)\\s([1,0])";
    public final static String OPS_WRITE_BYTE_REGEX = " "; // TODO
    public final static String OPS_WRITE_BIT_REGEX = " "; // TODO
    
    public final static String PROG_WRITE_BYTE_REGEX = "W\\s*(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)";
    public final static String PROG_WRITE_BIT_REGEX = "B\\s*(\\d+)\\s([0-7])\\s([1,0])\\s(\\d+)\\s(\\d+)";
    public final static String PROG_READ_REGEX = "R\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)";

    public final static String THROTTLE_REPLY_REGEX = "\\s*T\\s*(\\d+)\\s+([-]*\\d+)\\s+([1,0])\\s*";
    public final static String TURNOUT_REPLY_REGEX = "\\s*H\\s*(\\d+)\\s+([1,0])\\s*";
    public final static String PROGRAM_REPLY_REGEX = "\\s*r\\s*(\\d+)\\|(\\d+)\\|(\\d+)\\s+(\\d+)(\\s+(\\d+))?\\s*";
    public final static String CURRENT_REPLY_REGEX = "\\s*a\\s*(\\d+)";

    // Misc standard values
    public final static char WHITESPACE = ' ';
    public final static int MAX_SPEED = 126;
    public final static char FORWARD_DIR = '1';
    public final static char REVERSE_DIR = '0';

    public final static int REGISTER_1 = '1';

    public final static int FUNCTION_GROUP4_BYTE1 = 222;
    public final static int FUNCTION_GROUP5_BYTE1 = 223;

    public final static String TURNOUT_THROWN   = "1";
    public final static String TURNOUT_CLOSED   = "0";
    public final static String THROTTLE_FORWARD = "1";
    public final static String THROTTLE_REVERSE = "0";
    public final static String ACCESSORY_ON     = "1";
    public final static String ACCESSORY_OFF    = "0";

    // Various min/max values for messages
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


/* @(#)DCCppConstants.java */
