package jmri.jmrix.lenz;

/**
 * Constants to represent values seen in XpressNet traffic.
 * <p>
 * Variable prefix abreviation keys: ACC_ is for accessory messages BC_ is for
 * broadcast messages CS_ is for command station messages PROG_ is for
 * programming related messages LOCO_ is for locomotive related commands
 * OPS_MODE_ is for operations mode programming commands LI_ is for commands that
 * are for messages to and from the computer interface LI101_ is for commands
 * specific to the LI101.
 * <p>
 * A few variables don't have a prefix. The name should be self explanitory, but
 * a prefix may be added later.
 *
 * @author Paul Bender Copyright (C) 2003-2009
 */
public final class XNetConstants {

    private XNetConstants(){
        // final class of static methods.
    }

    /* Commands send from the command station to the computer*/

    /* Generic Information Messages */
    public static final int CS_INFO = 0x61;

    /* byte 2 commands for Information messages */

    /* broadcast messages */
    public static final int BC_NORMAL_OPERATIONS = 0x01; /* broadcast -normal
     operations resumed */

    public static final int BC_EVERYTHING_OFF = 0x00; /* broadcast emergency
     off (short circuit) */

    public static final int BC_SERVICE_MODE_ENTRY = 0x02;/* enter service mode */

    /* program mode messages */
    public static final int PROG_SHORT_CIRCUIT = 0x12; /* program mode short */

    public static final int PROG_BYTE_NOT_FOUND = 0x13; /* requested data
     byte not found */

    public static final int PROG_CS_BUSY = 0x1f; /* command station busy */

    public static final int PROG_CS_READY = 0x11; /* command station ready */

    /* standard responses */
    public static final int CS_TRANSFER_ERROR = 0x80; /* transfer error */

    public static final int CS_BUSY = 0x81; /* command station busy */

    public static final int CS_NOT_SUPPORTED = 0x82; /* command not supported */

    /* double header errors. These are for Xnet V1 and V2 */
    public static final int CS_DH_ERROR_NON_OP = 0x83; /* unit not operated by
     controler */

    public static final int CS_DH_ERROR_IN_USE = 0x84; /* one unit in DH
     controled by another device */

    public static final int CS_DH_ERROR_ALREADY_DH = 0x85; /* One locomotive in
     Double header is already
     in a Double Header */

    public static final int CS_DH_ERROR_NONZERO_SPD = 0x86; /* One or both units
     has a non-zero 
     speed setting */

    /* Service mode and Informational responses from the command station */
    public static final int CS_SERVICE_MODE_RESPONSE = 0x63;

    /* service mode sub commands for byte 2 */
    public static final int CS_SERVICE_DIRECT_RESPONSE = 0x14;  /* direct mode
     response */

    public static final int CS_SERVICE_REG_PAGE_RESPONSE = 0x10; /* Register and
     paged mode response */

    public static final int CS_SOFTWARE_VERSION = 0x21; /*software version
     is included with 
     the 0x63 group.*/

    /* informational request response */
    public static final int CS_REQUEST_RESPONSE = 0x62;
    /* information request response sub messages for byte 2 */
    /* CS_SOFTWARE_VERSION (0x21) is a valid byte 2 command for 0x62 */
    public static final int CS_STATUS_RESPONSE = 0x22; /* command station status */

    /* Different Command Station Type values */
    public static final int CS_TYPE_LZ100 = 0x00;  // Lenz LZ100/LZV100
    public static final int CS_TYPE_LH200 = 0x01;  // Lenz LH200
    public static final int CS_TYPE_COMPACT = 0x02; // Lenz Compact/Atlas Commander
    public static final int CS_TYPE_LOKMAUSII = 0x04; // Roco LokMaus II
    public static final int CS_TYPE_MULTIMAUS = 0x10; // Roco multiMAUS
    public static final int CS_TYPE_Z21 = 0x12; // Roco z21

    /* Emergency Stop */
    public static final int BC_EMERGENCY_STOP = 0x81;
    /* byte 2 commands for Emergecy Stop messages */
    public static final int BC_EVERYTHING_STOP = 0x00;   /* broadcast of
     emergency stop */

    /* Feedback - this is basically a bitmask.  The second nibble Indicates 
     how many address byte/data byte pairs follow the command */
    public static final int BC_FEEDBACK = 0x40;

    /* Accessory information response 
     * NOTE: This is identical to the feedback {@link BC_FEEDBACK} when
     * there is only one address byte/data byte pair
     */
    public static final int ACC_INFO_RESPONSE = 0x42;

    /* Locomotive Information for V1 & V2 */
    public static final int LOCO_AVAILABLE_V1 = 0x83; /* for XNet V1 */

    public static final int LOCO_NOT_AVAILABLE_V1 = 0xA3; /* for XNet V1 */

    public static final int LOCO_AVAILABLE_V2 = 0x84; /* for XNet V2 */

    public static final int LOCO_NOT_AVAILABLE_V2 = 0xA4; /* for XNet V2 */

    /* Locomotive Information for V3 
     NOTE: for version 3.6, 0xE4 and 0xE3 are used to send information
     about functions 13-28 */
    public static final int LOCO_INFO_NORMAL_UNIT = 0xE4;
    public static final int LOCO_INFO_MUED_UNIT = 0xE5;
    public static final int LOCO_INFO_MU_ADDRESS = 0xE2;
    public static final int LOCO_INFO_DH_UNIT = 0xE6;
    public static final int LOCO_INFO_RESPONSE = 0xE3;

    /* response types for LOCO_INFO_RESPONSE (byte two commands */
    public static final int LOCO_NOT_AVAILABLE = 0x40;
    public static final int LOCO_FUNCTION_STATUS = 0x50;
    public static final int LOCO_FUNCTION_STATUS_HIGH = 0x52; // for F13-F28
    public static final int LOCO_FUNCTION_STATUS_HIGH_MOM = 0x51; // for F13-F28

    /* responses for stack/database searches */
    public static final int LOCO_SEARCH_RESPONSE_N = 0x30; /*Normal Loco */

    public static final int LOCO_SEARCH_RESPONSE_DH = 0x31; /* in DH */

    public static final int LOCO_SEARCH_RESPONSE_MU_BASE = 0x32; /*MU base address */

    public static final int LOCO_SEARCH_RESPONSE_MU = 0x33; /* MUED Loco */

    public static final int LOCO_SEARCH_NO_RESULT = 0x34; /* No address found */

    /* Double Header Info for XNet V1 and V2 */
    public static final int LOCO_DH_INFO_V1 = 0xC5; /* Byte 1 for XNET V1 */

    public static final int LOCO_DH_INFO_V2 = 0xC6; /* Byte 1 for XNET V2 */

    /* byte 2 DH information for either V1 or V2 */
    public static final int LOCO_DH_AVAILABLE = 0x04;
    public static final int LOCO_DH_NOT_AVAILABLE = 0x05;

    /* XpressNet MU or DH Error Message */
    public static final int LOCO_MU_DH_ERROR = 0xE1;

    /* Commands send from the computer to the command station */

    /* Generic command station requests - used for*/
    public static final int CS_REQUEST = 0x21;

    /* sub operations for generic request  this is the second byte of
     the message */
    public static final int EMERGENCY_OFF = 0x80;
    public static final int RESUME_OPS = 0x81;
    public static final int SERVICE_MODE_CSRESULT = 0x10;
    public static final int CS_VERSION = 0x21;
    public static final int CS_STATUS = 0x24;

    /* the following sets the Command station Power up mode, it's 
     used as the first two bytes of the command */
    public static final int CS_SET_POWERMODE = 0x22;

    /* the third byte of the Power up mode is set for either Auto or Manual 
     startup */
    public static final int CS_POWERMODE_AUTO = 0x04;
    public static final int CS_POWERMODE_MANUAL = 0x00;


    /* Emergency Stop */
    public static final int ALL_ESTOP = 0x80;

    /* this is for a single locomotive.  With version 3 of expressnet, this is
     followed with a two byte address.  For version 2, this should be followed 
     by a 1 byte address */
    public static final int EMERGENCY_STOP_XNETV1V2 = 0x91;
    public static final int EMERGENCY_STOP = 0x92;

    /* Program mode read requests */
    public static final int PROG_READ_REQUEST = 0x22;

    /* programming modes to be used with PROG_READ_REQUEST */
    public static final int PROG_READ_MODE_REGISTER = 0x11;
    public static final int PROG_READ_MODE_CV = 0x15;
    public static final int PROG_READ_MODE_PAGED = 0x14;
    public static final int PROG_READ_MODE_CV_V36 = 0x18; // version 3.6 read

    /* Program mode read requests */
    public static final int PROG_WRITE_REQUEST = 0x23;

    /* programming modes to be used with PROG_WRITE_REQUEST */
    public static final int PROG_WRITE_MODE_REGISTER = 0x12;
    public static final int PROG_WRITE_MODE_CV = 0x16;
    public static final int PROG_WRITE_MODE_PAGED = 0x17;
    public static final int PROG_WRITE_MODE_CV_V36 = 0x1c; // version 3.6 write

    /* Accessory Decoder Info Request */
    public static final int ACC_INFO_REQ = 0x42;

    /* Accessory Decoder Operation Request */
    public static final int ACC_OPER_REQ = 0x52;

    /* Locomotive Information Request */
    public static final int LOCO_INFO_REQ_V1 = 0xA1; /* for version 1 of XNet
     Follow imediatly with address*/

    public static final int LOCO_INFO_REQ_V2 = 0xA2; /* for version 1 or 2 of
     XNet. 
     folow with address and 
     Mode Selection bytes */
    /* V3 Status requests */

    public static final int LOCO_STATUS_REQ = 0xE3;

    /* Status request subcodes for V3 */
    public static final int LOCO_INFO_REQ_V3 = 0x00; /* for XNet V3, follow
     with 2 byte address */

    public static final int LOCO_INFO_REQ_FUNC = 0x07; /* momentary/constatant
     function status request */

    public static final int LOCO_INFO_REQ_FUNC_HI_ON = 0x09; /* ON/OFF
     function status request 
     for Functions 13-28*/

    public static final int LOCO_INFO_REQ_FUNC_HI_MOM = 0x08; /* momentary/constatant
     function status request 
     for Functions 13-28*/

    public static final int LOCO_STACK_SEARCH_FWD = 0x05; /* search forward in
     the command station stack for this unit */

    public static final int LOCO_STACK_SEARCH_BKWD = 0x06; /* search backward in
     the command station stack for this unit */

    public static final int LOCO_STACK_DELETE = 0x44; /* Delete a unit from
     the command station stack */

    /* Locomotive Operations for XNet  (see XNet docs for more info */
    public static final int LOCO_OPER_REQ_V1 = 0xB3;  /* for XNet V1 */

    public static final int LOCO_OPER_REQ_V2 = 0xB4;  /* for XNet V2 */

    /* Locomotive Operations for XNet V3 */
    public static final int LOCO_OPER_REQ = 0xE4;

    /* XNet V3 operations subcommands */
    public static final int LOCO_SPEED_14 = 0x10; /* speed and direction 14
     speed steps */

    public static final int LOCO_SPEED_27 = 0x11; /* speed and direction 27
     speed steps */

    public static final int LOCO_SPEED_28 = 0x12; /* speed and direction 28
     speed steps */

    public static final int LOCO_SPEED_128 = 0x13; /* speed and direction 128
     speed steps */

    public static final int LOCO_SET_FUNC_GROUP1 = 0x20; /* set functions F0-F4*/

    public static final int LOCO_SET_FUNC_GROUP2 = 0x21; /* set functions F5-F8*/

    public static final int LOCO_SET_FUNC_GROUP3 = 0x22; /* set functions F9-F12*/

    public static final int LOCO_SET_FUNC_GROUP4 = 0x23; /* set functions F13-F20*/

    public static final int LOCO_SET_FUNC_GROUP5 = 0x28; /* set functions F21-F28*/

    /* these set momentary status for functions with 0xE4 as the opcode*/
    public static final int LOCO_SET_FUNC_Group1 = 0x24; /* set functions F0-F4*/

    public static final int LOCO_SET_FUNC_Group2 = 0x25; /* set functions F5-F8*/

    public static final int LOCO_SET_FUNC_Group3 = 0x26; /* set functions F9-F12*/

    public static final int LOCO_SET_FUNC_Group4 = 0x27; /* set functions F13-F20*/

    public static final int LOCO_SET_FUNC_Group5 = 0x2C; /* set functions F21-F28*/

    /* add a unit to a multi-unit set opcode requires addition of the 
     direction relative to the lead unit as the least significant bit 
     This is followed by the 2 byte unit address of the unit, and the 1 
     byte consist address */
    public static final int LOCO_ADD_MULTI_UNIT_REQ = 0x40;

    /* remove a unit from a multi-unit set This is followed by the 2 
     byte unit address of the unit, and the 1 byte consist address */
    public static final int LOCO_REM_MULTI_UNIT_REQ = 0x42;

    /* find out if a unit is part of a specifc multi-unit set These are 
     followed by the 1 byte consist address, and the 2 byte consist 
     address (Forward and Backward refer to search direction) */
    public static final int LOCO_IN_MULTI_UNIT_SEARCH_REQ = 0xE4; //This is the opcode

    /* These are byte 2 of the message */
    public static final int LOCO_IN_MULTI_UNIT_REQ_FORWARD = 0x01;
    public static final int LOCO_IN_MULTI_UNIT_REQ_BACKWARD = 0x02;

    /* double headers for XNet V1 and V2 */
    public static final int LOCO_DOUBLEHEAD_V1 = 0xC3;
    /*subcommands for establishing/Disolving DH in V1 and V2 */
    public static final int LOCO_ESTABLISH_DH_V1 = 0x05;
    public static final int LOCO_DISOLVE_DH_V1 = 0x04;

    /* double headers for XNet V3.  In V3, there is only one command.
     The third and 4th bytes are the locomotive address of the lead unit,
     the locomotive to add to the consist is the 5th and 6th byte.  To 
     Disolve the double header, use 00 for both bytes of the second address */
    public static final int LOCO_DOUBLEHEAD = 0xE5;
    public static final int LOCO_DOUBLEHEAD_BYTE2 = 0x43;

    /* Operations mode programming */
    public static final int OPS_MODE_PROG_REQ = 0xE6;

    /* Write requests (second byte for above) */
    public static final int OPS_MODE_PROG_WRITE_REQ = 0x30;

    /* Address inquiry Multi Unit Request 
     this is used to find the next Multi Unit address known to the 
     command station.  FWD and BKWD refer to search direction */
    public static final int CS_MULTI_UNIT_REQ = 0xE2;  // This is the OpCode
/* These are byte 2 of the message */
    public static final int CS_MULTI_UNIT_REQ_FWD = 0x03;
    public static final int CS_MULTI_UNIT_REQ_BKWD = 0x04;

    /* The following are for information requests from the LI100/LI100F/LI101*/

    /* LI10x responses for general messages */
    public static final int LI_MESSAGE_RESPONSE_HEADER = 0x01;
    /* First, we have an error for timeouts between the PC and the LI10x */
    public static final int LI_MESSAGE_RESPONSE_PC_DATA_ERROR = 0x01;
    /* Second, we have an error for timeouts between the LI10x and the C.S.*/
    public static final int LI_MESSAGE_RESPONSE_CS_DATA_ERROR = 0x02;
    /* Next is an unknown communications error */
    public static final int LI_MESSAGE_RESPONSE_UNKNOWN_DATA_ERROR = 0x03;
    /* Now, we have a response indicating what was sent was OK */
    public static final int LI_MESSAGE_RESPONSE_SEND_SUCCESS = 0x04;
    /* and a message indicating the LI10x doesn't have a timeslot on the 
     XpressNet (possibly too many devices connected) */
    public static final int LI_MESSAGE_RESPONSE_TIMESLOT_ERROR = 0x05;
    /* Last, there is an error for an LI10x buffer overflow */
    public static final int LI_MESSAGE_RESPONSE_BUFFER_OVERFLOW = 0x06;

    /* LI100/LI100F/LI101 information (version) request */
    public static final int LI_VERSION_REQUEST = 0xF0;
    /* The response to the above */
    public static final int LI_VERSION_RESPONSE = 0x02;

    /* request to the LI101 */
    public static final int LI101_REQUEST = 0xF2;
    /* The following are the two possible values for the second byte of a 
     request to the LI101 */
    public static final int LI101_REQUEST_ADDRESS = 0x01;
    public static final int LI101_REQUEST_BAUD = 0x02;

    /* The following are error messages sent by the LIUSB (version 3.6) */
    public static final int LIUSB_TIMESLOT_RESTORED = 0x07;
    public static final int LIUSB_REQUEST_SENT_WHILE_NO_TIMESLOT = 0x08;
    public static final int LIUSB_BAD_DATA_IN_REQUEST = 0x09;
    public static final int LIUSB_RETRANSMIT_REQUEST = 0x0A;

}
