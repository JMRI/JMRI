/**
 * XNetConstants.java
 *
 * Description:		Constants to represent values seen in XpressNet traffic
 * @author		Paul Bender  
 * @version             $ Revision: 1.0 $
 *
 */

package jmri.jmrix.lenz;

public final class XNetConstants {

/* Commands send from the command station to the computer*/


/* Commands send from the computer to the command station */

/* Generic command station requests - used for*/
public final static int CS_REQUEST = 0x21;

/* sub operations for generic request  this is the second byte of
the message */
public final static int EMERGENCY_OFF = 0x80;
public final static int RESUME_OPS    = 0x81;
public final static int SERVICE_MODE_RESULT = 0x10;
public final static int CS_VERSION = 0x21;
public final static int CS_STATUS  = 0x24;

/* the following sets the Command station Power up mode, it's 
   used as the first two bytes of the command */
public final static int CS_SET_POWERMODE    = 0x22;

/* Emergency Stop */
public final static int ALL_ESTOP = 0x80;

/* this is for a single locomotive.  With version 3 of expressnet, this is
followed with a two byte address.  For version 2, this should be followed 
by a 1 byte address */
public final static int EMERGENCY_STOP = 0x91;

/* Program mode read requests */
public final static int PROG_READ_REQUEST = 0x22;

/* programing modes to be used with PROG_READ_REQUEST
public final static int PROG_READ_MODE_REGISTER = 0x11;
public final static int PROG_READ_MODE_CV       = 0x15;
public final static int PROG_READ_MODE_PAGED    = 0x14;

/* Program mode read requests */
public final static int PROG_WRITE_REQUEST = 0x23;

/* programing modes to be used with PROG_WRITE_REQUEST
public final static int PROG_WRITE_MODE_REGISTER = 0x12;
public final static int PROG_WRITE_MODE_CV       = 0x15;
public final static int PROG_WRITE_MODE_PAGED    = 0x17;

/* Accessory Decoder Info Request */
public final static int ACC_INFO_REQ = 0x42;

/* Accessory Decoder Operation Request */
public final static int ACC_OPER_REQ = 0x52;

/* Locomotive Information Request */
public final static int LOCO_INFO_REQ_V1 = 0xA1; /* for version 1 of XNet 
                                                 Follow imediatly with address*/

public final static int LOCO_INFO_REQ_V2 = 0xA2; /* for version 1 or 2 of 
                                                 XNet. 
                                                 folow with address and 
                                                 Mode Selection bytes */
/* V3 Status requests */
public final static int LOCO_STATUS_REQ = 0xE3; 

/* Status request subcodes for V3 */
public final static int LOCO_INFO_REQ_V3 = 0x00; /* for XNet V3, follow 
					         with 2 byte address */
public final static int LOCO_INFO_REQ_FUNC = 0x07; /* momentary/constatant
                                                  function status request */
public final static int LOCO_STACK_SEARCH_FWD = 0x05; /* search forward in 
		    		the command station stack for this unit */
public final static int LOCO_STACK_SEARCH_BKWD = 0x06; /* search backward in 
		    		the command station stack for this unit */

/* Locomotive Operations for XNet  (see XNet docs for more info */
public final static int LOCO_OPER_REQ_V1 = 0xB3;  /* for XNet V1 */
public final static int LOCO_OPER_REQ_V2 = 0xB4;  /* for XNet V2 */

/* Locomotive Operations for XNet V3 */
public final static int LOCO_OPER_REQ    = 0xE4;

/* XNet V3 operations subcommands */
public final static int LOCO_SPEED_14    = 0x10; /* speed and direction 14 
						speed steps */
public final static int LOCO_SPEED_27    = 0x11; /* speed and direction 27 
						speed steps */
public final static int LOCO_SPEED_28    = 0x12; /* speed and direction 28 
						speed steps */
public final static int LOCO_SPEED_127   = 0x13; /* speed and direction 128 
						speed steps */

public final static int LOCO_SET_FUNC_GROUP1 = 0x20; /* set functions F0-F4*/
public final static int LOCO_SET_FUNC_GROUP2 = 0x21; /* set functions F5-F8*/
public final static int LOCO_SET_FUNC_GROUP3 = 0x22; /* set functions F9-F12*/

/* these set momentary status for functions with 0xE4 as the opcode*/
public final static int LOCO_SET_FUNC_Group1 = 0x24; /* set functions F0-F4*/
public final static int LOCO_SET_FUNC_Group2 = 0x25; /* set functions F5-F8*/
public final static int LOCO_SET_FUNC_Group3 = 0x26; /* set functions F9-F12*/

/* add a unit to a multi-unit set opcode requires addition of the 
direction relative to the lead unit as the least significant bit 
This is followed by the 2 byte unit address of the unit, and the 1 
byte consist address */
public final static int LOCO_ADD_MULTI_UNIT_REQ = 0x40;

/* remove a unit from a multi-unit set This is followed by the 2 
byte unit address of the unit, and the 1 byte consist address */
public final static int LOCO_REM_MULTI_UNIT_REQ = 0x42;

/* find out if a unit is part of a specifc multi-unit set These are 
followed by the 1 byte consist address, and the 2 byte consist 
address (Forward and Backward refer to search direction */
public final static int LOCO_IN_MULTI_UNIT_REQ_FORWARD = 0x01;
public final static int LOCO_IN_MULTI_UNIT_REQ_BACKWARD = 0x02;


/* double headers for XNet V1 and V2 */
public final static int LOCO_DOUBLEHEAD_V1 = 0xC3;
/*subcommands for establishing/Disolving DH in V1 and V2 */
public final static int LOCO_ESTABLISH_DH_V1 = 0x05;
public final static int LOCO_DISOLVE_DH_V1   = 0x04;

/* double headers for XNet V3.  In V3, there is only one command.
The third and 4th bytes are the locomotive address of the lead unit,
the locomotive to add to the consist is the 5th and 6th byte.  To 
Disolve the double header, use 00 for both bytes of the second address */
public final static int LOCO_DOUBLEHEAD = 0xE5;
public final static int LOCO_DOUBLEHEAD_BYTE2 = 0x43;

/* Operations mode programing */
public final static int OPS_MODE_PROG_REQ = 0xE6;

/* Write requests (second byte for above) */
public final static int OPS_MODE_PROG_WRITE_REQ = 0x30;

/* Address inquiry Multi Unit Request */
public final static int CS_MULTI_UNIT_REQ = 0xE2;

/* Address inquiry Multi Unit Request directions (second byte for above) */
public final static int CS_MULTI_UNIT_REQ_FWD = 0x03;
public final static int CS_MULTI_UNIT_REQ_BKWD = 0x04;


}


/* @(#)XNetConstants.java */
