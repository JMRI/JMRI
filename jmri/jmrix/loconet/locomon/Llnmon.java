// Llnmon.java

package jmri.jmrix.loconet.locomon;

import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;

 /**
 * A utility class for formatting LocoNet packets
 *              into human-readable text.
 * <P>
 * Much of this file is a Java-recoding of the display.c file from the
 * llnmon package of John Jabour.  Some of the conversions involve explicit
 * decoding of structs defined in loconet.h in that same package.  Those
 * parts are (C) Copyright 2001 Ron W. Auld.  Use of these parts is by
 * direct permission of the author.
 * <P>
 * Most major comment blocks here are quotes from the Digitrax Loconet(r)
 * OPCODE SUMMARY: found in the Loconet(r) Personal Edition 1.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 * <P>
 * Note that the formatted strings end in a \n, and may contain more than
 * one line separated by \n.  Someday this should be converted to
 * proper Java line handling, but for now it has to be handled in locomon,
 * the sole user of this. (It could be handled by moving the code from
 * locomon into the display member here)
 * <P>
 * Reverse engineering of OPC_MULTI_SENSE was provided by Al Silverstein,
 * used with permission.
 *
 * @author			Bob Jacobsen  Copyright 2001, 2002, 2003
 * @version			$Revision: 1.29 $
 */
public class Llnmon {

    static private int LOCO_ADR(int a1, int a2)   { return (((a1 & 0x7f) * 128) + (a2 & 0x7f)); }

    /**
     * Convert bytes from LocoNet packet into a 1-based address for
     * a sensor or turnout.
     * @param a1 Byte containing the upper bits
     * @param a2 Byte containing the lower bits
     * @return 1-4096 address
     */
    static private int SENSOR_ADR(int a1, int a2) { return (((a2 & 0x0f) * 128) + (a1 & 0x7f)) + 1; }


    // control data
    private boolean  rawLogMode      = false;  /* logging mode - 0=normal 1=raw (data only)        */
    private int      msgNumber       = 1;      /* message number                                   */
    private boolean  showDecimal     = false;  /* flag that determines if we print decimal values  */
    private boolean  showHex         = false;  /* flag that determines if we print hex values      */
    private boolean  showDiscards    = false;  /* TRUE if the user wants to display discarded data */
    private boolean  showTrackStatus = true;   /* if TRUE, show track status on every slot read    */
    private int      trackStatus     = -1;     /* most recent track status value                   */


    /**
     * This function creates a string representation of the loco address in
     *     addressLow & addressHigh in a form appropriate for the type of address
     *     (2 or 4 digit) using the Digitrax 'mixed mode' if necessary.
     */

    public static String convertToMixed(
                                        int addressLow,
                                        int addressHigh)
    {

        /* if we have a 2 digit decoder address and proceed accordingly */
        if (addressHigh == 0) {
            if (addressLow >= 120)
        	return "c"+String.valueOf(addressLow-120)+" ("+String.valueOf(addressLow)+")";
            else if (addressLow >= 110)
        	return "b"+String.valueOf(addressLow-110)+" ("+String.valueOf(addressLow)+")";
            else if (addressLow >= 100)
        	return "a"+String.valueOf(addressLow-100)+" ("+String.valueOf(addressLow)+")";
            else
        	return String.valueOf(addressLow & 0x7f);
        } else {
            /* return the full 4 digit address */
	    return String.valueOf(LOCO_ADR(addressHigh, addressLow));
	}
    }

    /**
     * Global flag to indicate the message was not fully parsed,
     * so the hex should be included.
     */
    protected boolean forceHex = false;

    /****************************************************************************
     * This function creates a string representation of a LocoNet buffer.
     *     The string may be more than one line, and is terminated with a newline.
     *
     *  @return The created string representation.
     */

    public String displayMessage(LocoNetMessage l) {

        forceHex = false;
        String s = format(l);
        if (forceHex) s += "contents: "+l.toString()+"\n";
        return s;
    }
    /**
     * Format the message into a text string.  If forceHex is set
     * upon return, the message was not fully parsed.
     * @param l Message to parse
     * @return String representation
     */
    protected String format(LocoNetMessage l) {

        boolean showStatus = false;   /* show track status in this message? */

	int minutes;  // temporary time values
	int hours;
        int frac_mins;


        switch (l.getOpCode()) {
            /***************************
             * ; 2 Byte MESSAGE OPCODES *
             * ; FORMAT = <OPC>,<CKSUM> *
             * ;                        *
             ***************************/

            /*************************************************
             * OPC_BUSY         0x81   ;MASTER busy code, NUL *
             *************************************************/
        case LnConstants.OPC_GPBUSY:                /* page 8 of Loconet PE */
            return "Master is busy\n";

            /****************************************************
             * OPC_GPOFF        0x82   ;GLOBAL power OFF request *
             ****************************************************/
        case LnConstants.OPC_GPOFF:                 /* page 8 of Loconet PE */
            return "Global Power OFF\n";

            /***************************************************
             * OPC_GPON         0x83   ;GLOBAL power ON request *
             ***************************************************/
        case LnConstants.OPC_GPON:                  /* page 8 of Loconet PE */
            return "Global Power ON\n";

            /**********************************************************************
             * OPC_IDLE         0x85   ;FORCE IDLE state, Broadcast emergency STOP *
             **********************************************************************/
        case LnConstants.OPC_IDLE:                  /* page 8 of Loconet PE */
            return "Force Idle, Emergency STOP\n";

            /*****************************************
             * ; 4 byte MESSAGE OPCODES               *
             * ; FORMAT = <OPC>,<ARG1>,<ARG2>,<CKSUM> *
             * :                                      *
             *  CODES 0xA8 to 0xAF have responses     *
             *  CODES 0xB8 to 0xBF have responses     *
             *****************************************/

            /***************************************************************************
             * OPC_LOCO_ADR     0xBF   ; REQ loco ADR                                   *
             *                         ; Follow on message: <E7>SLOT READ               *
             *                         ; <0xBF>,<0>,<ADR>,<CHK> REQ loco ADR            *
             *                         ; DATA return <E7>, is SLOT#, DATA that ADR was  *
             *                         : found in.                                      *
             *                         ; IF ADR not found, MASTER puts ADR in FREE slot *
             *                         ; and sends DATA/STATUS return <E7>......        *
             *                         ; IF no FREE slot, Fail LACK,0 is returned       *
             *                         ; [<B4>,<3F>,<0>,<CHK>]                          *
             ***************************************************************************/
        case LnConstants.OPC_LOCO_ADR: {             /* page 8 of Loconet PE */
            int adrHi = l.getElement(1);  // Hi address listed as zero above
            int adrLo = l.getElement(2);  // ADR above, the low part
            return "Request slot information for loco address "
                +convertToMixed(adrLo, adrHi)+"\n";
        }

            /*****************************************************************************
             * OPC_SW_ACK       0xBD   ; REQ SWITCH WITH acknowledge function (not DT200) *
             *                         ; Follow on message: LACK                          *
             *                         ; <0xBD>,<SW1>,<SW2>,<CHK> REQ SWITCH function     *
             *                         ;       <SW1> =<0,A6,A5,A4- A3,A2,A1,A0>           *
             *                         ;               7 ls adr bits.                     *
             *                         ;               A1,A0 select 1 of 4 input pairs    *
             *                         ;               in a DS54                          *
             *                         ;       <SW2> =<0,0,DIR,ON- A10,A9,A8,A7>          *
             *                         ;               Control bits and 4 MS adr bits.    *
             *                         ;               DIR=1 for Closed/GREEN             *
             *                         ;                  =0 for Thrown/RED               *
             *                         ;               ON=1 for Output ON                 *
             *                         ;                 =0 FOR output OFF                *
             *                         ; response is:                                     *
             *                         ; <0xB4><3D><00> if DCS100 FIFO is full, rejected. *
             *                         ; <0xB4><3D><7F> if DCS100 accepted                *
             *****************************************************************************/
        case LnConstants.OPC_SW_ACK: {               /* page 8 of Loconet PE */
            int sw1 = l.getElement(1);
            int sw2 = l.getElement(2);
            return "Request switch "+
                String.valueOf(SENSOR_ADR(sw1, sw2))+
                ((sw2 & LnConstants.OPC_SW_ACK_CLOSED)!=0 ? " Closed/Green" : " Thrown/Red")+
                ((sw2 & LnConstants.OPC_SW_ACK_OUTPUT)!=0 ? " (Output On)" : " (Output Off)")+
                " with Acknowledge\n";
        }

            /*************************************************************************
             * OPC_SW_STATE     0xBC   ; REQ state of SWITCH                          *
             *                         ; Follow on message: LACK                      *
             *                         ; <0xBC>,<SW1>,<SW2>,<CHK> REQ state of SWITCH *
             *************************************************************************/
        case LnConstants.OPC_SW_STATE: {             /* page 8 of Loconet PE */
            int sw1 = l.getElement(1);
            int sw2 = l.getElement(2);
            return "Request state of switch "+
                String.valueOf(SENSOR_ADR(sw1, sw2))+"\n";
        }

            /************************************************************************************
             * OPC_RQ_SL_DATA   0xBB   ; Request SLOT DATA/status block                          *
             *                         ; Follow on message: <E7>SLOT READ                        *
             *                         ; <0xBB>,<SLOT>,<0>,<CHK> Request SLOT DATA/status block. *
             ************************************************************************************/
        case LnConstants.OPC_RQ_SL_DATA: {           /* page 8 of Loconet PE */
            int slot = l.getElement(1);
            return "Request data/status for slot "+slot+"\n";
        }

            /*******************************************************************************
             * OPC_MOVE_SLOTS   0xBA   ; MOVE slot SRC to DEST                              *
             *                         ; Follow on message: <E7>SLOT READ                   *
             *                         ; <0xBA>,<SRC>,<DEST>,<CHK> Move SRC to DEST if      *
             *                         ; SRC or LACK etc is NOT IN_USE, clr SRC             *
             *                         ; SPECIAL CASES:                                     *
             *                         ; If SRC=0 ( DISPATCH GET) , DEST=dont care,         *
             *                         ;    Return SLOT READ DATA of DISPATCH Slot          *
             *                         ; IF SRC=DEST (NULL move) then SRC=DEST is set to    *
             *                         ;    IN_USE , if legal move.                         *
             *                         ; If DEST=0, is DISPATCH Put, mark SLOT as DISPATCH  *
             *                         ;    RETURN slot status <0xE7> of DESTINATION slot   *
             *                         ;       DEST if move legal                           *
             *                         ;    RETURN Fail LACK code if illegal move           *
             *                         ;       <B4>,<3A>,<0>,<chk>, illegal to move to/from *
             *                         ;       slots 120/127                                *
             *******************************************************************************/
        case LnConstants.OPC_MOVE_SLOTS: {           /* page 8 of Loconet PE */
            int src = l.getElement(1);
            int dest = l.getElement(2);

            /* check special cases */
            if (src == 0) {                  						/* DISPATCH GET */
                return "Get most recently dispatched slot\n";
            } else if (src == dest) {  								/* IN USE       */
                return "Set status of slot "+src+" to IN_USE\n";
            } else if (dest == 0) {              					/* DISPATCH PUT */
                return "Mark slot "+src+" as DISPATCHED to slot "+dest+"\n";
            } else {                                        		/* general move */
                return "Move data in slot "+src+" to slot "+dest+"\n";
            }
        }

            /********************************************************************************
             * OPC_LINK_SLOTS   0xB9   ; LINK slot ARG1 to slot ARG2                         *
             *                         ; Follow on message: <E7>SLOT READ                    *
             *                         ; <0xB9>,<SL1>,<SL2>,<CHK> SLAVE slot SL1 to slot SL2 *
             *                         ; Master LINKER sets the SL_CONUP/DN flags            *
             *                         ; appropriately. Reply is return of SLOT Status       *
             *                         ; <0xE7>. Inspect to see result of Link, invalid      *
             *                         ; Link will return Long Ack Fail <B4>,<39>,<0>,<CHK>  *
             ********************************************************************************/
        case LnConstants.OPC_LINK_SLOTS: {           /* page 9 of Loconet PE */
            int src = l.getElement(1);
            int dest = l.getElement(2);
            return "Consist loco in slot "+src+" to loco in slot "+dest+"\n";
        }

            /*******************************************************************************************
             * OPC_UNLINK_SLOTS 0xB8   ;UNLINK slot ARG1 from slot ARG2                                 *
             *                         ; Follow on message: <E7>SLOT READ                               *
             *                         ; <0xB8>,<SL1>,<SL2>,<CHK> UNLINK slot SL1 from SL2              *
             *                         ; UNLINKER executes unlink STRATEGY and returns new SLOT#        *
             *                         ; DATA/STATUS of unlinked LOCO . Inspect data to evaluate UNLINK *
             *******************************************************************************************/
        case LnConstants.OPC_UNLINK_SLOTS: {         /* page 9 of Loconet PE */
            int src = l.getElement(1);
            int dest = l.getElement(2);
            return "Remove loco in slot "+src+" from consist with loco in slot "+dest+"\n";
        }

            /*************************************************************************************
             * OPC_CONSIST_FUNC 0xB6   ; SET FUNC bits in a CONSIST uplink element                *
             *                         ; <0xB6>,<SLOT>,<DIRF>,<CHK> UP consist FUNC bits          *
             *                         ; NOTE this SLOT adr is considered in UPLINKED slot space. *
             *************************************************************************************/
        case LnConstants.OPC_CONSIST_FUNC:  {        /* page 9 of Loconet PE */
            int slot = l.getElement(1);
            int dirf = l.getElement(2);
            return "Set consist in slot "+slot
            	+" direction to "+((dirf & LnConstants.DIRF_DIR)!=0 ? "REV"   : "FWD")
                +"F0="+((dirf & LnConstants.DIRF_F0)!=0  ? "On, "  : "Off,")
                +"F1="+((dirf & LnConstants.DIRF_F1)!=0  ? "On, "  : "Off,")
                +"F2="+((dirf & LnConstants.DIRF_F2)!=0  ? "On, "  : "Off,")
                +"F3="+((dirf & LnConstants.DIRF_F3)!=0  ? "On, "  : "Off,")
                +"F4="+((dirf & LnConstants.DIRF_F4)!=0  ? "On"    : "Off")+"\n";
        }


            /********************************************************************
             * OPC_SLOT_STAT1   0xB5   ; WRITE slot stat1                        *
             *                         ; <0xB5>,<SLOT>,<STAT1>,<CHK> WRITE stat1 *
             ********************************************************************/
        case LnConstants.OPC_SLOT_STAT1:  {          /* page 9 of Loconet PE */
            int slot = l.getElement(1);
            int stat = l.getElement(2);
            return "Write slot "+slot
                +" with status value "+stat
                +" (0x"+Integer.toHexString(stat)
                +") - Loco is "+LnConstants.CONSIST_STAT(stat)+", "+LnConstants.LOCO_STAT(stat)
                +"\n\tand operating in "+LnConstants.DEC_MODE(stat)
                +" speed step mode\n";
        }

            /*******************************************************************************
             * OPC_LONG_ACK     0xB4   ; Long acknowledge                                   *
             *                         ; <0xB4>,<LOPC>,<ACK1>,<CHK> Long acknowledge        *
             *                         ; <LOPC> is COPY of OPCODE responding to (msb=0).    *
             *                         ; LOPC=0 (unused OPC) is also VALID fail code        *
             *                         ; <ACK1> is appropriate response code for the OPCode *
             *******************************************************************************/
        case LnConstants.OPC_LONG_ACK:              /* page 9 of Loconet PE */
            int opcode = l.getElement(1);
            int ack1 = l.getElement(2);

            switch (opcode | 0x80) {
            case (LnConstants.OPC_LOCO_ADR):             /* response for OPC_LOCO_ADR */
                return "LONG_ACK: No free slot\n";

            case (LnConstants.OPC_LINK_SLOTS):           /* response for OPC_LINK_SLOTS */
                return "LONG_ACK: Invalid consist\n";

            case (LnConstants.OPC_SW_ACK):               /* response for OPC_SW_ACK   */
                if (ack1 == 0) {
                    return "LONG_ACK: The DCS-100 FIFO is full, the switch command was rejected\n";
                } else if (ack1 == 0x7f) {
                    return "LONG_ACK: The DCS-100 accepted the switch command\n";
                } else {
                    forceHex = true;
                    return "LONG_ACK: Unknown response to 'Request Switch with ACK' command, 0x"+Integer.toHexString(ack1)+"\n";
                }

            case (LnConstants.OPC_SW_REQ):               /* response for OPC_SW_REQ */
                return "LONG_ACK: Switch request Failed!\n";

            case (LnConstants.OPC_WR_SL_DATA):
                if (ack1 == 0) {
                    return "LONG_ACK: The Slot Write command was rejected\n";
                } else if (ack1 == 0x01) {
                    return "LONG_ACK: The Slot Write command was accepted\n";
                } else if (ack1 == 0x40) {
                    return "LONG_ACK: The Slot Write command was accepted blind (no response will be sent)\n";
                } else if (ack1 == 0x7f) {
                    return "LONG_ACK: Function not implemented, no reply will follow\n";
                } else {
                    forceHex = true;
                    return "LONG_ACK: Unknown response to Write Slot Data message 0x"+Integer.toHexString(ack1)+"\n";
                }

            case (LnConstants.OPC_SW_STATE):
                return "LONG_ACK: Command station response to switch state request 0x"+Integer.toHexString(ack1)
                		+( ((ack1&0x20)!=0) ? " (Closed)" : " (Thrown)")+"\n";

            case (LnConstants.OPC_MOVE_SLOTS):
                if (ack1 == 0) {
                    return "LONG_ACK: The Move Slots command was rejected\n";
                } else if (ack1 == 0x7f) {
                    return "LONG_ACK: The Move Slots command was accepted\n";
                } else {
                    forceHex = true;
                    return "LONG_ACK: unknown reponse to Move Slots message 0x"+Integer.toHexString(ack1)+"\n";
                }

            case LnConstants.OPC_IMM_PACKET:      /* special response to OPC_IMM_PACKET */
                if (ack1 == 0) {
                    return "LONG_ACK: the Send IMM Packet command was rejected, the buffer is full/busy\n";
                } else if (ack1 == 0x7f) {
                    return "LONG_ACK: the Send IMM Packet command was accepted\n";
                } else {
                    forceHex = true;
                    return "Unknown reponse to Send IMM Packet message 0x"+Integer.toHexString(ack1)+"\n";
                }

            case LnConstants.OPC_IMM_PACKET_2:    /* special response to OPC_IMM_PACKET */
                return "LONG_ACK: the Lim Master responded to the Send IMM Packet command with "+ack1+" (0x"+Integer.toHexString(ack1)+")\n";


            default:
                // forceHex = TRUE;
                return "LONG_ACK: Response "+ack1+" (0x"+Integer.toHexString(ack1)
                    +") to opcode 0x"+Integer.toHexString(opcode)+" not decoded\n";
            }

            /********************************************************************************************
             * OPC_INPUT_REP    0xB2   ; General SENSOR Input codes                                      *
             *                         ; <0xB2>, <IN1>, <IN2>, <CHK>                                     *
             *                         ;   <IN1> =<0,A6,A5,A4- A3,A2,A1,A0>,                             *
             *                         ;           7 ls adr bits.                                        *
             *                         ;           A1,A0 select 1 of 4 inputs pairs in a DS54.           *
             *                         ;   <IN2> =<0,X,I,L- A10,A9,A8,A7>,                               *
             *                         ;           Report/status bits and 4 MS adr bits.                 *
             *                         ;           "I"=0 for DS54 "aux" inputs                           *
             *                         ;              =1 for "switch" inputs mapped to 4K SENSOR space.  *
             *                         ;                                                                 *
             *                         ;           (This is effectively a least significant adr bit when *
             *                         ;            using DS54 input configuration)                      *
             *                         ;                                                                 *
             *                         ;           "L"=0 for input SENSOR now 0V (LO),                   *
             *                         ;              =1 for Input sensor >=+6V (HI)                     *
             *                         ;           "X"=1, control bit,                                   *
             *                         ;              =0 is RESERVED for future!                         *
             ********************************************************************************************/
        case LnConstants.OPC_INPUT_REP:             /* page 9 of Loconet PE */
            int in1 = l.getElement(1);
            int in2 = l.getElement(2);

            String bdl = " (BDL16 "+((in1+(in2&0xF)*128)/8+1)+",";
            int ch = 0;
            if ( ((in1/2) & 3) == 0 ) ch = 0;
            else if ( ((in1/2) & 3) == 1 ) ch = 4;
            else if ( ((in1/2) & 3) == 2 ) ch = 8;
            else ch = 12;
            if ( ((in1 & 1) !=0) && ((in2 & LnConstants.OPC_INPUT_REP_SW)!=0) ) ch+=4;
            else if ( ((in1 & 1) !=0) && ((in2 & LnConstants.OPC_INPUT_REP_SW)==0) ) ch+=3;
            else if ( ((in1 & 1) ==0) && ((in2 & LnConstants.OPC_INPUT_REP_SW)!=0) ) ch+=2;
            else ch+=1;
            bdl = bdl+ch+")";

            String ds = " (DS54 switch "+SENSOR_ADR(in1,in2)
                +((in2 & LnConstants.OPC_INPUT_REP_SW)!=0 ? " Sw  input)" : " Aux input)");

            return "General sensor input report: contact "+
                ((SENSOR_ADR(in1, in2)-1)*2+((in2 & LnConstants.OPC_INPUT_REP_SW)!=0?2:1))
                +ds+
                bdl+
                " is "+
                ((in2 & LnConstants.OPC_INPUT_REP_HI)!=0 ? "Hi" : "Lo")+" "+
                ((in2 & LnConstants.OPC_INPUT_REP_CB)==0 ? "\n\t(Unexpected 0 value of reserved control bit)" : "")+
                "\n";


            /***************************************************************************************
             * OPC_SW_REP       0xB1   ; Turnout SENSOR state REPORT                                *
             *                         ; <0xB1>,<SN1>,<SN2>,<CHK> SENSOR state REPORT               *
             *                         ;   <SN1> =<0,A6,A5,A4- A3,A2,A1,A0>,                        *
             *                         ;           7 ls adr bits.                                   *
             *                         ;           A1,A0 select 1 of 4 input pairs in a DS54        *
             *                         ;   <SN2> =<0,1,I,L- A10,A9,A8,A7>                           *
             *                         ;           Report/status bits and 4 MS adr bits.            *
             *                         ;           this <B1> opcode encodes input levels            *
             *                         ;           for turnout feedback                             *
             *                         ;           "I" =0 for "aux" inputs (normally not feedback), *
             *                         ;               =1 for "switch" input used for               *
             *                         ;                  turnout feedback for DS54                 *
             *                         ;                  ouput/turnout # encoded by A0-A10         *
             *                         ;           "L" =0 for this input 0V (LO),                   *
             *                         ;               =1 this input > +6V (HI)                     *
             *                         ;                                                            *
             *                         ;   alternately;                                             *
             *                         ;                                                            *
             *                         ;   <SN2> =<0,0,C,T- A10,A9,A8,A7>                           *
             *                         ;           Report/status bits and 4 MS adr bits.            *
             *                         ;           this <B1> opcode encodes current OUTPUT levels   *
             *                         ;           "C" =0 if "Closed" ouput line is OFF,            *
             *                         ;               =1 "closed" output line is ON                *
             *                         ;                  (sink current)                            *
             *                         ;           "T" =0 if "Thrown" output line is OFF,           *
             *                         ;               =1 "thrown" output line is ON                *
             *                         ;                  (sink I)                                  *
             ***************************************************************************************/
        case LnConstants.OPC_SW_REP:                /* page 9 of Loconet PE */
            int sn1 = l.getElement(1);
            int sn2 = l.getElement(2);

            if ((sn2 & LnConstants.OPC_SW_REP_INPUTS)!=0) {
                return "Turnout "+
                    SENSOR_ADR(sn1, sn2)+
                    ((sn2 & LnConstants.OPC_SW_REP_SW) !=0 ? " Switch input" : " Aux input")+
                    " is "+
                    (((sn2 & LnConstants.OPC_SW_REP_HI)!=0) ? "Closed (input off)" : "Thrown (input on)")+"\n";
            } else {  // OPC_SW_REP_INPUTS is 0
                return "Turnout "+
                    SENSOR_ADR(sn1, sn2)+
                    " output state: Closed output is "+
                    ((sn2 & LnConstants.OPC_SW_REP_CLOSED)!=0 ? "ON (sink)" : "OFF (open)")+
                    ", Thrown output is "+
                    ((sn2 & LnConstants.OPC_SW_REP_THROWN)!=0 ? "ON (sink)" : "OFF (open)")+"\n";
            }


            /*******************************************************************************************
             * OPC_SW_REQ       0xB0   ; REQ SWITCH function                                            *
             *                         ; <0xB0>,<SW1>,<SW2>,<CHK> REQ SWITCH function                   *
             *                         ;   <SW1> =<0,A6,A5,A4- A3,A2,A1,A0>,                            *
             *                         ;           7 ls adr bits.                                       *
             *                         ;           A1,A0 select 1 of 4 input pairs in a DS54            *
             *                         ;   <SW2> =<0,0,DIR,ON- A10,A9,A8,A7>                            *
             *                         ;           Control bits and 4 MS adr bits.                      *
             *                         ;   DIR  =1 for Closed,/GREEN,                                   *
             *                         ;        =0 for Thrown/RED                                       *
             *                         ;   ON   =1 for Output ON,                                       *
             *                         ;        =0 FOR output OFF                                       *
             *                         ;                                                                *
             *                         ;   Note-Immediate response of <0xB4><30><00> if command failed, *
             *                         ;        otherwise no response "A" CLASS codes                   *
             *                         ;      														   *
             *                         ;   Special form:  broadcast (PE page 12)  					   *
             *                         ;      														   *
             *                         ;      														   *
             *                         ;   Special form:  LocoNet interrogate (PE page 13)  			   *
             *                         ;      														   *
             *                         ;      														   *
             *                         ;      														   *
             *                         ;      														   *
             *                         ;      														   *
             *                         ;      														   *
             *                         ;      														   *
             *                         ;      														   *

            *******************************************************************************************/
        case LnConstants.OPC_SW_REQ:                /* page 9 of Loconet PE */
            int sw1 = l.getElement(1);
            int sw2 = l.getElement(2);

            String retVal;

            // check for special forms first
            if ( ((sw2 & 0xCF) == 0x0F)  && ((sw1 & 0xFC) == 0x78) ) { // broadcast address LPU V1.0 page 12
                retVal = "Request Switch to broadcast address with bits "+
                    "a="+ ((sw2&0x20)>>5)+((sw2 & LnConstants.OPC_SW_REQ_DIR)!=0 ? " (Closed)" : " (Thrown)")+
                    " c="+ ((sw1 & 0x02)>>1) +
                    " b="+ ((sw1 & 0x01)) +
                    "\n\tOutput "+
                    ((sw2 & LnConstants.OPC_SW_REQ_OUT)!=0 ? "On"     : "Off")+"\n";

            } else if ( ((sw2 & 0xCF) == 0x07)  && ((sw1 & 0xFC) == 0x78) ) { // broadcast address LPU V1.0 page 13
                retVal = "Request switch command is Interrogate LocoNet with bits "+
                    "a="+ ((sw2 & 0x20)>>5) +
                    " c="+ ((sw1&0x02)>>1) +
                    " b="+ ((sw1&0x01)) +
                    "\n\tOutput "+
                    ((sw2 & LnConstants.OPC_SW_REQ_OUT)!=0 ? "On"     : "Off")+"\n"+
                    ( ( (sw2&0x10) == 0 ) ? "" : "\tNote 0x10 bit in sw2 is unexpectedly 0\n");

            } else { // normal command
                retVal = "Requesting Switch at "+
                    SENSOR_ADR(sw1, sw2)+
                    " to "+
                    ((sw2 & LnConstants.OPC_SW_REQ_DIR)!=0 ? "Closed" : "Thrown")+
                    " (output "+
                    ((sw2 & LnConstants.OPC_SW_REQ_OUT)!=0 ? "On"     : "Off")+")\n";
            }

            return retVal;


            /****************************************************
             * OPC_LOCO_SND     0xA2   ;SET SLOT sound functions *
             ****************************************************/
        case LnConstants.OPC_LOCO_SND:    {          /* page 10 of Loconet PE */
            int slot = l.getElement(1);
            int snd  = l.getElement(2);

            return "Set loco in slot "
                +slot
                +" Sound1/F5="
                +((snd & LnConstants.SND_F5) != 0  ? "On"  : "Off")
                +", Sound2/F6="
                +((snd & LnConstants.SND_F6) != 0  ? "On"  : "Off")
                +", Sound3/F7="
                +((snd & LnConstants.SND_F7) != 0  ? "On"  : "Off")
                +", Sound4/F8="
                +((snd & LnConstants.SND_F8) != 0  ? "On"  : "Off")
                +"\n";
      	}

            /****************************************************
             * OPC_LOCO_DIRF    0xA1   ;SET SLOT dir, F0-4 state *
             ****************************************************/
        case LnConstants.OPC_LOCO_DIRF:             /* page 10 of Loconet PE */
            {
                int slot = l.getElement(1);
                int dirf  = l.getElement(2);

                return "Set loco in slot "
                    +slot
                    +" direction to "
                    +((dirf & LnConstants.DIRF_DIR) != 0 ? "REV"   : "FWD")
                    +", F0="
                    +((dirf & LnConstants.DIRF_F0) != 0  ? "On, "  : "Off,")
                    +" F1="
                    +((dirf & LnConstants.DIRF_F1) != 0  ? "On, "  : "Off,")
                    +" F2="
                    +((dirf & LnConstants.DIRF_F2) != 0  ? "On, "  : "Off,")
                    +" F3="
                    +((dirf & LnConstants.DIRF_F3) != 0  ? "On, "  : "Off,")
                    +" F4="
                    +((dirf & LnConstants.DIRF_F4) != 0  ? "On"    : "Off")
                    +"\n";
            }


            /***********************************************************************
             * OPC_LOCO_SPD     0xA0   ;SET SLOT speed e.g. <0xA0><SLOT#><SPD><CHK> *
             ***********************************************************************/
        case LnConstants.OPC_LOCO_SPD:     {         /* page 10 of Loconet PE */
            int slot = l.getElement(1);
            int spd  = l.getElement(2);

            if (spd == LnConstants.OPC_LOCO_SPD_ESTOP) { /* emergency stop */
                return "Set speed of loco in slot "+slot+" to EMERGENCY STOP!\n";
            } else {
                return "Set speed of loco in slot "+slot+" to "+spd+"\n";
            }
        }

            /*******************************************************
             * ; 6 byte MESSAGE OPCODES                             *
             * ; FORMAT = <OPC>,<ARG1>,<ARG2>,<ARG3>,<ARG4>,<CKSUM> *
             * :                                                    *
             *  CODES 0xC8 to 0xCF have responses                   *
             *  CODES 0xD8 to 0xDF have responses                   *
             ********************************************************/

            /************************************************************************
             * OPC_MULTI_SENSE     0xD0 messages about power management              *
             *                          and transponding                             *
             *                                                                       *
             *  If byte 1 high nibble is 0x20 or 0x00 this is a transponding message *
             *************************************************************************/
        case LnConstants.OPC_MULTI_SENSE:     {         // definition courtesy Al Silverstein
            int type = l.getElement(1)&LnConstants.OPC_MULTI_SENSE_MSG;
            String m;

            String zone;
            if      ((l.getElement(2)&0x0F) == 0x00) zone = "A";
            else if ((l.getElement(2)&0x0F) == 0x02) zone = "B";
            else if ((l.getElement(2)&0x0F) == 0x04) zone = "C";
            else if ((l.getElement(2)&0x0F) == 0x06) zone = "D";
            else zone="<unknown "+(l.getElement(2)&0x0F)+">";
			int section = (l.getElement(2)/16)+(l.getElement(1)&0x1F)*16;

            switch (type) {
            case LnConstants.OPC_MULTI_SENSE_POWER:
                return powerMultiSenseMessage(l);
            case LnConstants.OPC_MULTI_SENSE_PRESENT:  // from transponding app note
                m =  "Transponder present in section "+section
                    +" zone "+zone+" decoder address ";
                if (l.getElement(3)==0x7D)
                    m+=l.getElement(4)+" (short) ";
                else
                    m+=l.getElement(3)*128+l.getElement(4)+" (long) ";
                return m+"\n";
            case LnConstants.OPC_MULTI_SENSE_ABSENT:
                m =  "Transponder absent in section "+section
                	+" zone "+zone+" decoder address ";
                if (l.getElement(3)==0x7D)
                    m+=l.getElement(4)+" (short) ";
                else
                    m+=l.getElement(3)*128+l.getElement(4)+" (long) ";
                return m+"\n";
            default:
                forceHex = true;
                return "OPC_MULTI_SENSE unknown format\n";
            }
        }

            /********************************************************************
             * ; VARIABLE Byte MESSAGE OPCODES                                   *
             * ; FORMAT = <OPC>,<COUNT>,<ARG2>,<ARG3>,...,<ARG(COUNT-3)>,<CKSUM> *
             ********************************************************************/

            /**********************************************************************************************
             * OPC_WR_SL_DATA   0xEF   ; WRITE SLOT DATA, 10 bytes                                         *
             *                         ; Follow on message: LACK                                           *
             *                         ; <0xEF>,<0E>,<SLOT#>,<STAT>,<ADR>,<SPD>,<DIRF>,                    *
             *                         ;        <TRK>,<SS2>,<ADR2>,<SND>,<ID1>,<ID2>,<CHK>                 *
             *                         ; SLOT DATA WRITE, 10 bytes data /14 byte MSG                       *
             ***********************************************************************************************
             * OPC_SL_RD_DATA   0xE7   ; SLOT DATA return, 10 bytes                                        *
             *                         ; <0xE7>,<0E>,<SLOT#>,<STAT>,<ADR>,<SPD>,<DIRF>,                    *
             *                         ;        <TRK>,<SS2>,<ADR2>,<SND>,<ID1>,<ID2>,<CHK>                 *
             *                         ; SLOT DATA READ, 10 bytes data /14 byte MSG                        *
             *                         ;                                                                   *
             *                         ; NOTE; If STAT2.2=0 EX1/EX2 encodes an ID#,                        *
             *                         ;       [if STAT2.2=1 the STAT.3=0 means EX1/EX2                    *
             *                         ;        are ALIAS]                                                 *
             *                         ;                                                                   *
             *                         ; ID1/ID2 are two 7 bit values encoding a 14 bit                    *
             *                         ;         unique DEVICE usage ID.                                   *
             *                         ;                                                                   *
             *                         ;   00/00 - means NO ID being used                                  *
             *                         ;                                                                   *
             *                         ;   01/00 - ID shows PC usage.                                      *
             *                         ;    to         Lo nibble is TYP PC#                                *
             *                         ;   7F/01       (PC can use hi values)                              *
             *                         ;                                                                   *
             *                         ;   00/02 -SYSTEM reserved                                          *
             *                         ;    to                                                             *
             *                         ;   7F/03                                                           *
             *                         ;                                                                   *
             *                         ;   00/04 -NORMAL throttle RANGE                                    *
             *                         ;    to                                                             *
             *                         ;   7F/7E                                                           *
             ***********************************************************************************************
             * Notes:                                                                                      *
             * The SLOT DATA bytes are, in order of TRANSMISSION for <E7> READ or <EF> WRITE.              *
             * NOTE SLOT 0 <E7> read will return MASTER config information bytes.                          *
             *                                                                                             *
             * 0) SLOT NUMBER:                                                                             *
             *                                                                                             *
             * ; 0-7FH, 0 is special SLOT,                                                                 *
             *                     ; 070H-07FH DIGITRAX reserved:                                          *
             *                                                                                             *
             * 1) SLOT STATUS1:                                                                            *
             *                                                                                             *
             *     D7-SL_SPURGE    ; 1=SLOT purge en,                                                      *
             *                     ; ALSO adrSEL (INTERNAL use only) (not seen on NET!)                    *
             *                                                                                             *
             *     D6-SL_CONUP     ; CONDN/CONUP: bit encoding-Control double linked Consist List          *
             *                     ;    11=LOGICAL MID CONSIST , Linked up AND down                        *
             *                     ;    10=LOGICAL CONSIST TOP, Only linked downwards                      *
             *                     ;    01=LOGICAL CONSIST SUB-MEMBER, Only linked upwards                 *
             *                     ;    00=FREE locomotive, no CONSIST indirection/linking                 *
             *                     ; ALLOWS "CONSISTS of CONSISTS". Uplinked means that                    *
             *                     ; Slot SPD number is now SLOT adr of SPD/DIR and STATUS                 *
             *                     ; of consist. i.e. is ;an Indirect pointer. This Slot                   *
             *                     ; has same BUSY/ACTIVE bits as TOP of Consist. TOP is                   *
             *                     ; loco with SPD/DIR for whole consist. (top of list).                   *
             *                     ; BUSY/ACTIVE: bit encoding for SLOT activity                           *
             *                                                                                             *
             *     D5-SL_BUSY      ; 11=IN_USE loco adr in SLOT -REFRESHED                                 *
             *                                                                                             *
             *     D4-SL_ACTIVE    ; 10=IDLE loco adr in SLOT -NOT refreshed                               *
             *                     ; 01=COMMON loco adr IN SLOT -refreshed                                 *
             *                     ; 00=FREE SLOT, no valid DATA -not refreshed                            *
             *                                                                                             *
             *     D3-SL_CONDN     ; shows other SLOT Consist linked INTO this slot, see SL_CONUP          *
             *                                                                                             *
             *     D2-SL_SPDEX     ; 3 BITS for Decoder TYPE encoding for this SLOT                        *
             *                                                                                             *
             *     D1-SL_SPD14     ; 011=send 128 speed mode packets                                       *
             *                                                                                             *
             *     D0-SL_SPD28     ; 010=14 step MODE                                                      *
             *                     ; 001=28 step. Generate Trinary packets for this                        *
             *                     ;              Mobile ADR                                               *
             *                     ; 000=28 step. 3 BYTE PKT regular mode                                  *
             *                     ; 111=128 Step decoder, Allow Advanced DCC consisting                   *
             *                     ; 100=28 Step decoder ,Allow Advanced DCC consisting                    *
             *                                                                                             *
             * 2) SLOT LOCO ADR:                                                                           *
             *                                                                                             *
             *     LOCO adr Low 7 bits (byte sent as ARG2 in ADR req opcode <0xBF>)                        *
             *                                                                                             *
             * 3) SLOT SPEED:                                                                              *
             *     0x00=SPEED 0 ,STOP inertially                                                           *
             *     0x01=SPEED 0 EMERGENCY stop                                                             *
             *     0x02->0x7F increasing SPEED,0x7F=MAX speed                                              *
             *     (byte also sent as ARG2 in SPD opcode <0xA0> )                                          *
             *                                                                                             *
             * 4) SLOT DIRF byte: (byte also sent as ARG2 in DIRF opcode <0xA1>)                           *
             *                                                                                             *
             *     D7-0        ; always 0                                                                  *
             *     D6-SL_XCNT  ; reserved , set 0                                                          *
             *     D5-SL_DIR   ; 1=loco direction FORWARD                                                  *
             *     D4-SL_F0    ; 1=Directional lighting ON                                                 *
             *     D3-SL_F4    ; 1=F4 ON                                                                   *
             *     D2-SL_F3    ; 1=F3 ON                                                                   *
             *     D1-SL_F2    ; 1=F2 ON                                                                   *
             *     D0-SL_F1    ; 1=F1 ON                                                                   *
             *                                                                                             *
             *                                                                                             *
             *                                                                                             *
             *                                                                                             *
             * 5) TRK byte: (GLOBAL system /track status)                                                  *
             *                                                                                             *
             *     D7-D4       Reserved                                                                    *
             *     D3          GTRK_PROG_BUSY 1=Programming TRACK in this Master is BUSY.                  *
             *     D2          GTRK_MLOK1     1=This Master IMPLEMENTS LocoNet 1.1 capability,             *
             *                                0=Master is DT200                                            *
             *     D1          GTRK_IDLE      0=TRACK is PAUSED, B'cast EMERG STOP.                        *
             *     D0          GTRK_POWER     1=DCC packets are ON in MASTER, Global POWER up              *
             *                                                                                             *
             * 6) SLOT STATUS:                                                                             *
             *                                                                                             *
             *     D3          1=expansion IN ID1/2, 0=ENCODED alias                                       *
             *     D2          1=Expansion ID1/2 is NOT ID usage                                           *
             *     D0          1=this slot has SUPPRESSED ADV consist-7)                                   *
             *                                                                                             *
             * 7) SLOT LOCO ADR HIGH:                                                                      *
             *                                                                                             *
             * Locomotive address high 7 bits. If this is 0 then Low address is normal 7 bit NMRA SHORT    *
             * address. If this is not zero then the most significant 6 bits of this address are used in   *
             * the first LONG address byte ( matching CV17). The second DCC LONG address byte matches CV18 *
             * and includes the Adr Low 7 bit value with the LS bit of ADR high in the MS postion of this  *
             * track adr byte.                                                                             *
             *                                                                                             *
             * Note a DT200 MASTER will always interpret this as 0.                                        *
             *                                                                                             *
             * 8) SLOT SOUND:                                                                              *
             *                                                                                             *
             *     Slot sound/ Accesory Function mode II packets. F5-F8                                    *
             *     (byte also sent as ARG2 in SND opcode)                                                  *
             *                                                                                             *
             *     D7-D4           reserved                                                                *
             *     D3-SL_SND4/F8                                                                           *
             *     D2-SL_SND3/F7                                                                           *
             *     D1-SL_SND2/F6                                                                           *
             *     D0-SL_SND1/F5   1= SLOT Sound 1 function 1active (accessory 2)                          *
             *                                                                                             *
             * 9) EXPANSION RESERVED ID1:                                                                  *
             *                                                                                             *
             *     7 bit ls ID code written by THROTTLE/PC when STAT2.4=1                                  *
             *                                                                                             *
             * 10) EXPANSION RESERVED ID2:                                                                 *
             *                                                                                             *
             *     7 bit ms ID code written by THROTTLE/PC when STAT2.4=1                                  *
             **********************************************************************************************/
        case LnConstants.OPC_WR_SL_DATA:            /* page 10 of Loconet PE */
        case LnConstants.OPC_SL_RD_DATA:            /* page 10 of Loconet PE */
            {
                String mode;
                String locoAdrStr;
                String mixedAdrStr;
                String logString;

                // rwSlotData = (rwSlotDataMsg *) msgBuf;
                int command =   l.getElement(0);
                int mesg_size = l.getElement(1);     // ummmmm, size of the message in bytes?
                int slot = 		l.getElement(2);     // slot number for this request
                int stat = 		l.getElement(3);     // slot status
                int adr = 		l.getElement(4);     // loco address
                int spd = 		l.getElement(5);     // command speed
                int dirf = 		l.getElement(6);     // direction and F0-F4 bits
                int trk = 		l.getElement(7);     // track status
                int ss2 = 		l.getElement(8);     // slot status 2 (tells how to use ID1/ID2 & ADV Consist)
                int adr2 = 		l.getElement(9);     // loco address high
                int snd = 		l.getElement(10);    // Sound 1-4 / F5-F8
                int id1 = 		l.getElement(11);    // ls 7 bits of ID code
                int id2 = 		l.getElement(12);    // ms 7 bits of ID code

                /* build loco address string */
                mixedAdrStr = convertToMixed(adr, adr2);

                /* figure out the alias condition, and create the loco address string */
                if (adr2 == 0x7f) {
                    if ((ss2 & LnConstants.STAT2_ALIAS_MASK) == LnConstants.STAT2_ID_IS_ALIAS) {
                        /* this is an aliased address and we have the alias*/
                        locoAdrStr = ""
                            +LOCO_ADR(id2, id1)
                            +" (Alias for loco "
                            +mixedAdrStr
                            +")";
                    } else {
                        /* this is an aliased address and we don't have the alias */
                        locoAdrStr = mixedAdrStr
                            +" (via Alias)";
                    }
                } else {
                    /* regular 4 digit address, 128 to 9983 */
                    locoAdrStr = mixedAdrStr;
                }

                /*
                 *  These share a common data format with the only
                 *  difference being whether we are reading or writing
                 *  the slot data.
                 */

                if (command == LnConstants.OPC_WR_SL_DATA) {
                    mode = "Write";
                } else {
                    mode = "Read";
                }

                if (slot == LnConstants.FC_SLOT) {
                    /**********************************************************************************************
                     * FAST Clock:                                                                                 *
                     * ===========                                                                                 *
                     * The system FAST clock and parameters are implemented in Slot#123 <7B>.                      *
                     *                                                                                             *
                     * Use <EF> to write new clock information, Slot read of 0x7B,<BB><7B>.., will return current  *
                     * System clock information, and other throttles will update to this SYNC. Note that all       *
                     * attached display devices keep a current clock calculation based on this SYNC read value,    *
                     * i.e. devices MUST not continuously poll the clock SLOT to generate time, but use this       *
                     * merely to restore SYNC and follow current RATE etc. This clock slot is typically "pinged"   *
                     * or read SYNC'd every 70 to 100 seconds , by a single user, so all attached devices can      *
                     * synchronise any phase drifts. Upon seeing a SYNC read, all devices should reset their local *
                     * sub-minute phase counter and invalidate the SYNC update ping generator.                     *
                     *                                                                                             *
                     * Clock Slot Format:                                                                          *
                     *                                                                                             *
                     * <0xEF>,<0E>,<7B>,<CLK_RATE>,<FRAC_MINSL>,<FRAC_MINSH>,<256-MINS_60>,                        *
                     * <TRK><256-HRS_24>,<DAYS>,<CLK_CNTRL>,<ID1>,<1D2>,<CHK>                                      *
                     *                                                                                             *
                     *     <CLK_RATE>      0=Freeze clock,                                                         *
                     *                     1=normal 1:1 rate,                                                      *
                     *                     10=10:1 etc, max VALUE is 7F/128 to 1                                   *
                     *     <FRAC_MINSL>    FRAC mins hi/lo are a sub-minute counter , depending                    *
                     *                         on the CLOCK generator                                              *
                     *     <FRAC_MINSH>    Not for ext. usage. This counter is reset when valid                    *
                     *                         <E6><7B> SYNC msg seen                                              *
                     *     <256-MINS_60>   This is FAST clock MINUTES subtracted from 256. Modulo 0-59             *
                     *     <256-HRS_24>    This is FAST clock HOURS subtracted from 256. Modulo 0-23               *
                     *     <DAYS>          number of 24 Hr clock rolls, positive count                             *
                     *     <CLK_CNTRL>     Clock Control Byte                                                      *
                     *                         D6- 1=This is valid Clock information,                              *
                     *                             0=ignore this <E6><7B>, SYNC reply                              *
                     *     <ID1>,<1D2>     This is device ID last setting the clock.                               *
                     *                         <00><00> shows no set has happened                                  *
                     *     <7F><7x>        are reserved for PC access                                              *
                     **********************************************************************************************/

                    /* make message easier to deal with internally */
                    // fastClock = (fastClockMsg *)msgBuf;
                    int clk_rate 	= l.getElement(3);   // 0 = Freeze clock, 1 = normal, 10 = 10:1 etc. Max is 0x7f
                    int frac_minsl 	= l.getElement(4);   // fractional minutes. not for external use.
                    int frac_minsh 	= l.getElement(5);
                    int mins_60 	= l.getElement(6);   // 256 - minutes
                    int track_stat 	= l.getElement(7);   // track status
                    int hours_24 	= l.getElement(8);   // 256 - hours
                    int days 		= l.getElement(9);   // clock rollovers
                    int clk_cntrl 	= l.getElement(10);  // bit 6 = 1; data is valid clock info
                    //  "  "   0; ignore this reply
                    // id1/id2 is device id of last device to set the clock
                    //  "   "  = zero shows not set has happened

                    /* recover hours and minutes values */
                    minutes = ((255 - mins_60) & 0x7f) % 60;
                    hours   = ((256 - hours_24)& 0x7f) % 24;
                    hours   = (24 - hours) % 24;
                    minutes = (60 - minutes) % 60;
                    frac_mins = 0x3FFF - ( frac_minsl + ( frac_minsh << 7 ) ) ;

                    /* check track status value and display */
                    if ((trackStatus != track_stat) || showTrackStatus) {
                        trackStatus = track_stat;
                        showStatus  = true;
                    }

                    if (showStatus) {
                        logString = mode
                            +" Fast Clock: (Data is "
                            +((clk_cntrl & 0x20) != 0 ? "Valid" : "Invalid - ignore")
                            +")\n\t"
                            +(clk_rate != 0 ? "Running" : "Frozen")
                            +", rate is "+clk_rate
                            +":1. Day "+days+", "+hours+":"+minutes+"."+frac_mins
                            +". Last set by ID "+idString(id1, id2)
                            +"\n\tMaster controller "
                            +((track_stat & LnConstants.GTRK_MLOK1)!=0 ? "implements LocoNet 1.1" : "is a DT-200")
                            +",\n\tTrack Status is "
                            +((track_stat & LnConstants.GTRK_POWER)!=0  ? " On," : " Off,")
                            +((track_stat & LnConstants.GTRK_IDLE)!=0  ? " Paused " : " Running ")
                            +",\n\tProgramming Track is "
                            +((track_stat & LnConstants.GTRK_PROG_BUSY)!=0 ? "Busy" : "Available")
                            +"\n";
                    } else {
                        logString = mode
                            +" Fast Clock: (Data is "
                            +((clk_cntrl & 0x20) != 0 ? "Valid" : "Invalid - ignore")
                            +")\n\t"
                            +(clk_rate != 0 ? "Frozen" : "Running")
                            +", rate is "+clk_rate
                            +":1. Day "+days+", "+hours+":"+minutes
                            +". Last set by ID "+idString(id1, id2)+"\n";
                    }
                    // end fast clock block

                } else if (slot == LnConstants.PRG_SLOT) {


                    /**********************************************************************************************
                     * Programmer track:                                                                           *
                     * =================                                                                           *
                     * The programmer track is accessed as Special slot #124 ( $7C, 0x7C). It is a full            *
                     * asynchronous shared system resource.                                                        *
                     *                                                                                             *
                     * To start Programmer task, write to slot 124. There will be an immediate LACK acknowledge    *
                     * that indicates what programming will be allowed. If a valid programming task is started,    *
                     * then at the final (asynchronous) programming completion, a Slot read <E7> from slot 124     *
                     * will be sent. This is the final task status reply.                                          *
                     *                                                                                             *
                     * Programmer Task Start:                                                                      *
                     * ----------------------                                                                      *
                     * <0xEF>,<0E>,<7C>,<PCMD>,<0>,<HOPSA>,<LOPSA>,<TRK>;<CVH>,<CVL>,                              *
                     *        <DATA7>,<0>,<0>,<CHK>                                                                *
                     *                                                                                             *
                     * This OPC leads to immediate LACK codes:                                                     *
                     *     <B4>,<7F>,<7F>,<chk>    Function NOT implemented, no reply.                             *
                     *     <B4>,<7F>,<0>,<chk>     Programmer BUSY , task aborted, no reply.                       *
                     *     <B4>,<7F>,<1>,<chk>     Task accepted , <E7> reply at completion.                       *
                     *     <B4>,<7F>,<0x40>,<chk>  Task accepted blind NO <E7> reply at completion.                *
                     *                                                                                             *
                     * Note that the <7F> code will occur in Operations Mode Read requests if the System is not    *
                     * configured for and has no Advanced Acknowlegement detection installed.. Operations Mode     *
                     * requests can be made and executed whilst a current Service Mode programming task is keeping *
                     * the Programming track BUSY. If a Programming request is rejected, delay and resend the      *
                     * complete request later. Some readback operations can keep the Programming track busy for up *
                     * to a minute. Multiple devices, throttles/PC's etc, can share and sequentially use the       *
                     * Programming track as long as they correctly interpret the response messages. Any Slot RD    *
                     * from the master will also contain the Programmer Busy status in bit 3 of the <TRK> byte.    *
                     *                                                                                             *
                     * A <PCMD> value of <00> will abort current SERVICE mode programming task and will echo with  *
                     * an <E6> RD the command string that was aborted.                                             *
                     *                                                                                             *
                     * <PCMD> Programmer Command:                                                                  *
                     * --------------------------                                                                  *
                     * Defined as                                                                                  *
                     *     D7 -0                                                                                   *
                     *     D6 -Write/Read  1= Write,                                                               *
                     *                     0=Read                                                                  *
                     *     D5 -Byte Mode   1= Byte operation,                                                      *
                     *                     0=Bit operation (if possible)                                           *
                     *     D4 -TY1 Programming Type select bit                                                     *
                     *     D3 -TY0 Prog type select bit                                                            *
                     *     D2 -Ops Mode    1=Ops Mode on Mainlines,                                                *
                     *                     0=Service Mode on Programming Track                                     *
                     *     D1 -0 reserved                                                                          *
                     *     D0 -0-reserved                                                                          *
                     *                                                                                             *
                     * Type codes:                                                                                 *
                     * -----------                                                                                 *
                     *     Byte Mode   Ops Mode   TY1   TY0   Meaning                                              *
                     *        1           0        0     0    Paged mode byte Read/Write on Service Track          *
                     *        1           0        0     0    Paged mode byte Read/Write on Service Track          *
                     *        1           0        0     1    Direct mode byteRead/Write on Service Track          *
                     *        0           0        0     1    Direct mode bit Read/Write on Service Track          *
                     *        x           0        1     0    Physical Register byte Read/Write on Service Track   *
                     *        x           0        1     1    Service Track- reserved function                     *
                     *        1           1        0     0    Ops mode Byte program, no feedback                   *
                     *        1           1        0     1    Ops mode Byte program, feedback                      *
                     *        0           1        0     0    Ops mode Bit program, no feedback                    *
                     *        0           1        0     1    Ops mode Bit program, feedback                       *
                     *                                                                                             *
                     *     <HOPSA>Operations Mode Programming                                                      *
                     *         7 High address bits of Loco to program, 0 if Service Mode                           *
                     *     <LOPSA>Operations Mode Programming                                                      *
                     *         7 Low address bits of Loco to program, 0 if Service Mode                            *
                     *     <TRK> Normal Global Track status for this Master,                                       *
                     *         Bit 3 also is 1 WHEN Service Mode track is BUSY                                     *
                     *     <CVH> High 3 BITS of CV#, and ms bit of DATA.7                                          *
                     *         <0,0,CV9,CV8 - 0,0, D7,CV7>                                                         *
                     *     <CVL> Low 7 bits of 10 bit CV address.                                                  *
                     *         <0,CV6,CV5,CV4-CV3,CV2,CV1,CV0>                                                     *
                     *     <DATA7>Low 7 BITS OF data to WR or RD COMPARE                                           *
                     *         <0,D6,D5,D4 - D3,D2,D1,D0>                                                          *
                     *         ms bit is at CVH bit 1 position.                                                    *
                     *                                                                                             *
                     * Programmer Task Final Reply:                                                                *
                     * ----------------------------                                                                *
                     * (if saw LACK <B4>,<7F>,<1>,<chk> code reply at task start)                                  *
                     *                                                                                             *
                     * <0xE7>,<0E>,<7C>,<PCMD>,<PSTAT>,<HOPSA>,<LOPSA>,<TRK>;<CVH>,<CVL>,                          *
                     * <DATA7>,<0>,<0>,<CHK>                                                                       *
                     *                                                                                             *
                     *     <PSTAT> Programmer Status error flags. Reply codes resulting from                       *
                     *             completed task in PCMD                                                          *
                     *         D7-D4 -reserved                                                                     *
                     *         D3    -1= User Aborted this command                                                 *
                     *         D2    -1= Failed to detect READ Compare acknowledge response                        *
                     *                   from decoder                                                              *
                     *         D1    -1= No Write acknowledge response from decoder                                *
                     *         D0    -1= Service Mode programming track empty- No decoder detected                 *
                     *                                                                                             *
                     * This <E7> response is issued whenever a Programming task is completed. It echos most of the *
                     * request information and returns the PSTAT status code to indicate how the task completed.   *
                     * If a READ was requested <DATA7> and <CVH> contain the returned data, if the PSTAT indicates *
                     * a successful readback (typically =0). Note that if a Paged Read fails to detect a           *
                     * successful Page write acknowledge when first setting the Page register, the read will be    *
                     * aborted, showing no Write acknowledge flag D1=1.                                            *
                     **********************************************************************************************/
                    String 	operation;
                    String 	progMode;
                    int 	cvData;
                    boolean opsMode = false;
                    int  	cvNumber;

                    // progTask   = (progTaskMsg *) msgBuf;
                    // slot - slot number for this request - slot 124 is programmer
                    int pcmd 	= l.getElement(3);  // programmer command
                    int pstat	= l.getElement(4);  // programmer status error flags in reply message
                    int hopsa	= l.getElement(5);  // Ops mode - 7 high address bits of loco to program
                    int lopsa	= l.getElement(6);  // Ops mode - 7 low  address bits of loco to program
                    /* trk - track status. Note: bit 3 shows if prog track is busy */
                    int cvh		= l.getElement(8);  // hi 3 bits of CV# and msb of data7
                    int cvl		= l.getElement(9);  // lo 7 bits of CV#
                    int data7	= l.getElement(10); // 7 bits of data to program, msb is in cvh above

                    cvData     =  (((cvh & LnConstants.CVH_D7) << 6) | (data7 & 0x7f));  // was PROG_DATA
                    cvNumber   = (((((cvh & LnConstants.CVH_CV8_CV9) >> 3) | (cvh & LnConstants.CVH_CV7)) * 128)
                                  + (cvl & 0x7f))+1;   // was PROG_CV_NUM(progTask)

                    /* generate loco address, mixed mode or true 4 digit */
                    mixedAdrStr = convertToMixed(lopsa, hopsa);

                    /* determine programming mode for printing */
                    if ((pcmd & LnConstants.PCMD_MODE_MASK) == LnConstants.PAGED_ON_SRVC_TRK) {
                        progMode = "Byte in Paged Mode on Service Track";
                    } else if ((pcmd & LnConstants.PCMD_MODE_MASK) == LnConstants.DIR_BYTE_ON_SRVC_TRK) {
                        progMode = "Byte in Direct Mode on Service Track";
                    } else if ((pcmd & LnConstants.PCMD_MODE_MASK) == LnConstants.DIR_BIT_ON_SRVC_TRK) {
                        progMode = "Bits in Direct Mode on Service Track";
                    } else if (((pcmd & ~LnConstants.PCMD_BYTE_MODE) & LnConstants.PCMD_MODE_MASK)
                               == LnConstants.REG_BYTE_RW_ON_SRVC_TRK) {
                        progMode = "Byte in Physical Register R/W Mode on Service Track";
                    } else if ((pcmd & LnConstants.PCMD_MODE_MASK) == LnConstants.OPS_BYTE_NO_FEEDBACK) {
                        progMode = "Byte in OP's Mode (NO feedback)";
                        opsMode = true;
                    } else if ((pcmd & LnConstants.PCMD_MODE_MASK) == LnConstants.OPS_BYTE_FEEDBACK) {
                        progMode = "Byte in OP's Mode";
                        opsMode = true;
                    } else if ((pcmd & LnConstants.PCMD_MODE_MASK) == LnConstants.OPS_BIT_NO_FEEDBACK) {
                        progMode = "Bits in OP's Mode (NO feedback)";
                        opsMode = true;
                    } else if ((pcmd & LnConstants.PCMD_MODE_MASK) == LnConstants.OPS_BIT_FEEDBACK) {
                        progMode = "Bits in OP's Mode";
                        opsMode = true;
                    } else if (((pcmd & ~LnConstants.PCMD_BYTE_MODE) & LnConstants.PCMD_MODE_MASK)
                               == LnConstants.SRVC_TRK_RESERVED) {
                        progMode = "SERVICE TRACK RESERVED MODE DETECTED!";
                    } else {
                        progMode = "Unknown mode "+pcmd+" (0x"+Integer.toHexString(pcmd)+")";
                        forceHex = true;
                    }

                    /* are we sending or receiving? */
                    if ((pcmd & LnConstants.PCMD_RW) != 0) {
                        /* sending a command */
                        operation = "Programming Track: Write";

                        /* printout based on whether we're doing Ops mode or not */
                        if (opsMode) {
                            logString = mode+" "
                                +operation+" "
                                +progMode+"\n"
                                +"\tSetting CV"+cvNumber
                                +" of Loco "+mixedAdrStr+" to "+cvData
                                +" (0x"+Integer.toHexString(cvData)+")\n";
                        } else {
                            logString = mode+" "
                                +operation+" "
                                +progMode+"\n"
                                +"\tSetting CV"+cvNumber
                                +" to "+cvData
                                +" (0x"+Integer.toHexString(cvData)+")\n";
                        }
                    } else {
                        /* receiving a reply */
                        operation = "Programming Track: Read";

                        /* printout based on whether we're doing Ops mode or not */
                        if (opsMode) {
                            logString = mode+" "
                                +operation+" "
                                +progMode+"\n"
                                +"\tSetting CV"+cvNumber
                                +" of Loco "+mixedAdrStr+" to "+cvData
                                +" (0x"+Integer.toHexString(cvData)+")\n";
                        } else {
                            logString = mode+" "
                                +operation+" "
                                +progMode+"\n"
                                +"\tSetting CV"+cvNumber
                                +" to "+cvData
                                +" (0x"+Integer.toHexString(cvData)+")\n";
                        }

                        /* if we're reading the slot back, check the status        */
                        /* this is supposed to be the Programming task final reply */
                        /* and will have the resulting status byte                 */

                        if (command == LnConstants.OPC_SL_RD_DATA) {
                            if (pstat != 0) {
                                if ((pstat & LnConstants.PSTAT_USER_ABORTED) != 0) {
                                    logString += "\tStatus = Failed, User Aborted\n";
                                }

                                if ((pstat & LnConstants.PSTAT_READ_FAIL) != 0) {
                                    logString += "\tStatus = Failed, Read Compare Acknowledge not detected\n";
                                }

                                if ((pstat & LnConstants.PSTAT_WRITE_FAIL) != 0 ) {
                                    logString += "\tStatus = Failed, No Write Acknowledge from decoder\n";
                                }

                                if ((pstat & LnConstants.PSTAT_NO_DECODER) != 0 ) {
                                    logString += "\tStatus = Failed, Service Mode programming track empty\n";
                                }
                                if ((pstat & 0xF0) != 0) {
                                    logString += "Warning: reserved bit set. Message may be invalid. PSTAT = 0x"
                                        +Integer.toHexString(pstat);
                                }
                            } else {
                                logString += "\tStatus = Success\n";
                            }
                        }
                    }
                    // end programming track block

                } else {
                    /**************************************************
                     * normal slot read/write message - see info above *
                     **************************************************/

                    if ((trackStatus != trk) || showTrackStatus) {
                        trackStatus = trk;
                        showStatus  = true;
                    }

                    if (showStatus) {
                        logString = mode
                            +" slot "+slot
                            +":\n\tLoco "+locoAdrStr
                            +" is "+LnConstants.CONSIST_STAT(stat)
                            +", "+LnConstants.LOCO_STAT(stat)
                            +", operating in "+LnConstants.DEC_MODE(stat)+" SS mode, and is going "
                            +((dirf & LnConstants.DIRF_DIR)!=0 ? "in Reverse" : "Foward")
                            +" at speed "+spd+",\n"
                            +"\tF0="+((dirf & LnConstants.DIRF_F0) != 0 ? "On, "  : "Off,")
                            +" F1="+((dirf & LnConstants.DIRF_F1) != 0 ? "On, "  : "Off,")
                            +" F2="+((dirf & LnConstants.DIRF_F2) != 0 ? "On, "  : "Off,")
                            +" F3="+((dirf & LnConstants.DIRF_F3) != 0 ? "On, "  : "Off,")
                            +" F4="+((dirf & LnConstants.DIRF_F4) != 0 ? "On, "  : "Off,")
                            +" Sound1/F5="+((snd  & LnConstants.SND_F5) != 0 ? "On, "  : "Off,")
                            +" Sound2/F6="+((snd  & LnConstants.SND_F6) != 0 ? "On, "  : "Off,")
                            +" Sound3/F7="+((snd  & LnConstants.SND_F7) != 0 ? "On, "  : "Off,")
                            +" Sound4/F8="+((snd  & LnConstants.SND_F8) != 0 ? "On"    : "Off")
                            +"\n\tMaster controller "+((trk  & LnConstants.GTRK_MLOK1) !=0 ? "implements LocoNet 1.1" : "is a DT-200")
                            +",\n\tTrack Status is "+((trk  & LnConstants.GTRK_IDLE) != 0  ? "On" : "Off")
                            +",\n\tProgramming Track is "+((trk  & LnConstants.GTRK_PROG_BUSY) != 0 ? "Busy" : "Available")
                            +"\n\tSS2=0x"+Integer.toHexString(ss2)
                            +", ID="+idString(id1, id2)+"\n";
                    } else {
                        logString = mode
                            +" slot "+slot
                            +":\n\tLoco "+locoAdrStr
                            +" is "+LnConstants.CONSIST_STAT(stat)
                            +", "+LnConstants.LOCO_STAT(stat)
                            +", operating in "+LnConstants.DEC_MODE(stat)+" SS mode, and is going "
                            +((dirf & LnConstants.DIRF_DIR)!=0 ? "in Reverse" : "Foward")
                            +" at speed "+spd+",\n"
                            +"\tF0="+((dirf & LnConstants.DIRF_F0) != 0 ? "On, "  : "Off,")
                            +" F1="+((dirf & LnConstants.DIRF_F1) != 0 ? "On, "  : "Off,")
                            +" F2="+((dirf & LnConstants.DIRF_F2) != 0 ? "On, "  : "Off,")
                            +" F3="+((dirf & LnConstants.DIRF_F3) != 0 ? "On, "  : "Off,")
                            +" F4="+((dirf & LnConstants.DIRF_F4) != 0 ? "On, "  : "Off,")
                            +" Sound1/F5="+((snd  & LnConstants.SND_F5) != 0 ? "On, "  : "Off,")
                            +" Sound2/F6="+((snd  & LnConstants.SND_F6) != 0 ? "On, "  : "Off,")
                            +" Sound3/F7="+((snd  & LnConstants.SND_F7) != 0 ? "On, "  : "Off,")
                            +" Sound4/F8="+((snd  & LnConstants.SND_F8) != 0 ? "On"    : "Off")
                            +"\n\tSS2=0x"+Integer.toHexString(ss2)
                            +", ID ="+idString(id1, id2)+"\n";
                    }
                    // end normal slot read/write case
                }

                // end of OPC_WR_SL_DATA, OPC_SL_RD_DATA case
                return logString;
            }

        case 0xEE:
        case 0xE6:
            // ALM read and write messages
            {
                if (l.getElement(1)!=0x10) return "ALM message with unexpected length "+l.getElement(1)+"\n";
                String message;
                if (l.getElement(0)==0xEE) message = "Write ALM ";
                else message = "Read ALM ";
                message = message+l.getElement(2)+" ATASK="+l.getElement(3);
                if (l.getElement(3) == 2) message=message+" (RD)";
                if (l.getElement(3) == 3) message=message+" (WR)";
                message = message+" BLKL="+l.getElement(4)+" BLKH="+l.getElement(5);
                message = message+" LOGIC="+l.getElement(6)+"\n      ";
                message = message+" ARG1L=0x"+Integer.toHexString(l.getElement(7))
                                 +" ARG1H=0x"+Integer.toHexString(l.getElement(8));
                message = message+" ARG2L=0x"+Integer.toHexString(l.getElement(9))
                                 +" ARG2H=0x"+Integer.toHexString(l.getElement(10))
                         +"\n      ";
                message = message+" ARG3L=0x"+Integer.toHexString(l.getElement(11))
                                 +" ARG3H=0x"+Integer.toHexString(l.getElement(12));
                message = message+" ARG4L=0x"+Integer.toHexString(l.getElement(13))
                                 +" ARG4H=0x"+Integer.toHexString(l.getElement(14))+"\n";

                return message;
            }
        case 0xE5:
            // there are several different formats for 0xE5 messages, with
            // the length apparently the distinquishing item.
            switch (l.getElement(1)) {
            case 0x10: {
                /***********************************************************************************
                 * OPC_PEER_XFER    0xE5   ; move 8 bytes PEER to PEER, SRC->DST                    *
                 *                         ; Message has response                                   *
                 *                         ; <0xE5>,<10>,<SRC>,<DSTL><DSTH>,<PXCT1>,<D1>,           *
                 *                         ;        <D2>,<D3>,<D4>,<PXCT2>,<D5>,<D6>,<D7>,          *
                 *                         ;        <D8>,<CHK>                                      *
                 *                         ;   SRC/DST are 7 bit args. DSTL/H=0 is BROADCAST msg    *
                 *                         ;   SRC=0 is MASTER                                      *
                 *                         ;   SRC=0x70-0x7E are reserved                           *
                 *                         ;   SRC=7F is THROTTLE msg xfer,                         *
                 *                         ;        <DSTL><DSTH> encode ID#,                        *
                 *                         ;        <0><0> is THROT B'CAST                          *
                 *                         ;   <PXCT1>=<0,XC2,XC1,XC0 - D4.7,D3.7,D2.7,D1.7>        *
                 *                         ;        XC0-XC2=ADR type CODE-0=7 bit Peer TO Peer adrs *
                 *                         ;           1=<D1>is SRC HI,<D2>is DST HI                *
                 *                         ;   <PXCT2>=<0,XC5,XC4,XC3 - D8.7,D7.7,D6.7,D5.7>        *
                 *                         ;        XC3-XC5=data type CODE- 0=ANSI TEXT string,     *
                 *                         ;           balance RESERVED                             *
                 ***********************************************************************************/

                int src 	= l.getElement(2);            	// source of transfer
                int dst_l 	= l.getElement(3);          	// ls 7 bits of destination
                int dst_h 	= l.getElement(4);          	// ms 7 bits of destination
                int pxct1 	= l.getElement(5);
                int pxct2	= l.getElement(10);

                int d[] = l.getPeerXfrData();

                return "Peer to Peer transfer: SRC=0x"+Integer.toHexString(src)
                    +", DSTL=0x"+Integer.toHexString(dst_l)
                    +", DSTH=0x"+Integer.toHexString(dst_h)
                    +", PXCT1=0x"+Integer.toHexString(pxct1)
                    +", PXCT2=0x"+Integer.toHexString(pxct2)+"\n"
                    +"\tD1=0x"+Integer.toHexString(d[0])
                    +", D2=0x"+Integer.toHexString(d[1])
                    +", D3=0x"+Integer.toHexString(d[2])
                    +", D4=0x"+Integer.toHexString(d[3])
                    +", D5=0x"+Integer.toHexString(d[4])
                    +", D6=0x"+Integer.toHexString(d[5])
                    +", D7=0x"+Integer.toHexString(d[6])
                    +", D8=0x"+Integer.toHexString(d[7])
                    +"\n";
            }
            case 0x0A: {
                // throttle status
                int tcntrl = l.getElement(2);
                String stat;
                if (tcntrl==0x40) stat = " (OK) ";
                else if (tcntrl==0x7F) stat = " (no key, immed, ignored) ";
                else if (tcntrl==0x43) stat = " (+ key during msg) ";
                else if (tcntrl==0x42) stat = " (- key during msg) ";
                else if (tcntrl==0x41) stat = " (R/S key during msg, aborts) ";
                else stat=" (unknown) ";

                return "Throttle status TCNTRL="+Integer.toHexString(tcntrl)
                    +stat
                    +" ID1,ID2="+Integer.toHexString(l.getElement(3))
                    +Integer.toHexString(l.getElement(4))
                    +" SLA="+Integer.toHexString(l.getElement(7))
                    +" SLB="+Integer.toHexString(l.getElement(8))
                    +"\n";            }
            default: {
                // 0xE5 message of unknown format
                forceHex = true;
                return "Message with opcode 0xE5 and unknown format";

            }
            } // end of 0xE5 switch statement


            /***********************************************************************************
             *                  0xE4   ;                                                        *
             *                         ;                                                        *
             *                         ; <0xE4>,<0x09>,...                                      *
             ***********************************************************************************/
        case 0XE4:
            if (l.getElement(1)!=0x0A) {
                forceHex = true;
                return "Unrecognized command varient\n";
            }

            // OK, format
            int element = l.getElement(2)*128+l.getElement(3);
            int stat1 = l.getElement(5);
            int stat2 = l.getElement(6);
            String status;
            if ( (stat1&0x10) !=0 )
                if ( (stat1&0x20) !=0 )
                    status = " AX, XA reserved; ";
                else
                    status = " AX reserved; ";
            else
                if ( (stat1&0x20) !=0 )
                    status = " XA reserved; ";
                else
                    status = " no reservation; ";
            if ( (stat2&0x01) !=0 ) status+="Turnout thrown; ";
            else status+="Turnout closed; ";
            if ( (stat1&0x01) !=0 ) status+="Occupied";
            else status+="Not occupied";
            return "SE"+(element+1)+" ("+element+") reports AX:"+l.getElement(7)
                +" XA:"+l.getElement(8)
                +status+"\n";

            /**************************************************************************
             * OPC_IMM_PACKET   0xED   ;SEND n-byte packet immediate LACK              *
             *                         ; Follow on message: LACK                       *
             *                         ; <0xED>,<0B>,<7F>,<REPS>,<DHI>,<IM1>,<IM2>,    *
             *                         ;        <IM3>,<IM4>,<IM5>,<CHK>                *
             *                         ;   <DHI>=<0,0,1,IM5.7-IM4.7,IM3.7,IM2.7,IM1.7> *
             *                         ;   <REPS>  D4,5,6=#IM bytes,                   *
             *                         ;           D3=0(reserved);                     *
             *                         ;           D2,1,0=repeat CNT                   *
             *                         ; IF Not limited MASTER then                    *
             *                         ;   LACK=<B4>,<7D>,<7F>,<chk> if CMD ok         *
             *                         ; IF limited MASTER then Lim Masters respond    *
             *                         ;   with <B4>,<7E>,<lim adr>,<chk>              *
             *                         ; IF internal buffer BUSY/full respond          *
             *                         ;   with <B4>,<7D>,<0>,<chk>                    *
             *                         ;   (NOT IMPLEMENTED IN DT200)                  *
             **************************************************************************/
        case LnConstants.OPC_IMM_PACKET:            /* page 11 of Loconet PE */
            //sendPkt = (sendPktMsg *) msgBuf;
            int val7f 	= l.getElement(2);         /* fixed value of 0x7f                                  */
            int reps 	= l.getElement(3);         /* repeat count                                         */
            int dhi 	= l.getElement(4);         /* high bits of data bytes                              */
            int im1 	= l.getElement(5);
            int im2 	= l.getElement(6);
            int im3 	= l.getElement(7);
            int im4 	= l.getElement(8);
            int im5 	= l.getElement(9);

            /* see if it really is a 'Send Packet' as defined in Loconet PE */
            if (val7f == 0x7f) {
                /* it is */
                return "Send packet immediate: "+((reps & 0x70) >> 4)
                    +" bytes, repeat count "+(reps & 0x07)
                    +", DHI=0x"+Integer.toHexString(dhi)
                    +",\n\tIM1=0x"+Integer.toHexString(im1)
                    +", IM2=0x"+Integer.toHexString(im2)
                    +", IM3=0x"+Integer.toHexString(im3)
                    +", IM4=0x"+Integer.toHexString(im4)
                    +", IM5=0x"+Integer.toHexString(im5)
                    +"\n";
            } else {
                /* Hmmmm... */
                forceHex = true;
                return "Weird Send Packet Immediate, 3rd byte id 0x"+Integer.toHexString(val7f)
                    +" not 0x7f\n";
            }

        default:
            forceHex = true;
            return "Command is not defined in Loconet Personal Use Edition 1.0\n";

        }  // end switch over opcode type - default handles unrecognized cases, so can't reach here

    }  // end of format() member function


    public String crcCheck(LocoNetMessage m) {
	// set the error correcting code byte
	int len = m.getNumDataElements();
	int chksum = 0xff;  /* the seed */
   	int loop;

        for(loop = 0; loop < len; loop++) {  // calculate contents for data part
            chksum ^= m.getElement(loop);
        }

	// if zero, the checksum in the stored message is correct
	if (chksum != 0)
            return "Warning - checksum invalid in message:\n";
	else
            return "";
    }

    /**
     * Factor out the PM power messages
     * @param l
     * @return human readable string, no \n on end
     */
    String powerMultiSenseMessage(LocoNetMessage l) {
        int pCMD = (l.getElement(3) & 0xF0);

        if ( (pCMD == 0x30)|| (pCMD == 0x10)) {
            // autoreverse
            int cm1 = l.getElement(3);
            int cm2 = l.getElement(4);
            return "PM4 "+(l.getElement(2)+1)
                +" ch1 "+((cm1&1)!=0 ? "AR " : "SC ")+((cm2&1)!=0 ? "ACT;" : "OK;")
                +" ch2 "+((cm1&2)!=0 ? "AR " : "SC ")+((cm2&2)!=0 ? "ACT;" : "OK;")
                +" ch3 "+((cm1&4)!=0 ? "AR " : "SC ")+((cm2&4)!=0 ? "ACT;" : "OK;")
                +" ch4 "+((cm1&8)!=0 ? "AR " : "SC ")+((cm2&8)!=0 ? "ACT;" : "OK;")
                +"\n";
        } else if (pCMD == 0x70) {
            // programming
            String device;
            if ( (l.getElement(3)&0x7) == 0) device = "PM ";
            else if ( (l.getElement(3)&0x7) == 1) device = "BD ";
            else if ( (l.getElement(3)&0x7) == 2) device = "SE ";
            else device = "(unknown type) ";

            int bit = (l.getElement(4)&0x0E)/2;
            int val = (l.getElement(4)&0x01);
            int wrd = (l.getElement(4)&0x70)/16;
            int opsw = (l.getElement(4)&0x7E)/2+1;
            return device+(l.getElement(2)+1)+
                ( ((l.getElement(1)&0x10)!=0) ? " write config bit ":" read config bit ")
                +wrd+","+bit+" (opsw "+opsw+") val="+val
                +(val==1 ? " (closed) ":" (thrown) ")+"\n";
        } else  // beats me
            forceHex = true;
            return "OPC_MULTI_SENSE power message PM4 "
                    +(l.getElement(2)+1)+" unknown CMD=0x"+Integer.toHexString(pCMD)+" ";
    }

    String idString(int id1, int id2) {
        return "0x"+Integer.toHexString( (id2&0x7F)*128+(id1&0x7F))
            +" ("+((id2/4&0)&0x3f)+")";
    }

}  // end of class


/* @(#)Llnmon.java */
