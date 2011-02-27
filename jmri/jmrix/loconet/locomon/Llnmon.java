// Llnmon.java

package jmri.jmrix.loconet.locomon;

import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.StringUtil;

/**
 * A utility class for formatting LocoNet packets into human-readable text.
 * <P>
 * Much of this file is a Java-recoding of the display.c file from the llnmon
 * package of John Jabour. Some of the conversions involve explicit decoding of
 * structs defined in loconet.h in that same package. Those parts are (C)
 * Copyright 2001 Ron W. Auld. Use of these parts is by direct permission of the
 * author.
 * <P>
 * Most major comment blocks here are quotes from the Digitrax Loconet(r) OPCODE
 * SUMMARY: found in the Loconet(r) Personal Edition 1.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 * <P>
 * Note that the formatted strings end in a \n, and may contain more than one
 * line separated by \n. Someday this should be converted to proper Java line
 * handling, but for now it has to be handled in locomon, the sole user of this.
 * (It could be handled by moving the code from locomon into the display member
 * here)
 * <P>
 * Reverse engineering of OPC_MULTI_SENSE was provided by Al Silverstein, used
 * with permission.
 * <P>
 * Reverse engineering of the Duplex Group/Password/Channel management was
 * provided by Leo Bicknell with help from B. Milhaupt, used with permission.
 * 
 * @author Bob Jacobsen Copyright 2001, 2002, 2003
 * @version $Revision: 1.53 $
 */
public class Llnmon {

    /**
     * Flag that determines if we print loconet opcodes
     */
    private boolean showOpCode = false;

    /**
     * Flag that determines if we show track status on every slot read
     */
    private boolean showTrackStatus = true;

    /**
     * Most recent track status value
     */
    private int trackStatus = -1;

    /**
     * Global flag to indicate the message was not fully parsed, so the hex
     * should be included.
     */
    protected boolean forceHex = false;
 
    /**
     * Convert bytes from LocoNet packet into a locomotive address.
     * 
     * @param a1
     *            Byte containing the upper bits.
     * @param a2
     *            Byte containting the lower bits.
     * @return 1-4096 address
     */
    static private int LOCO_ADR(int a1, int a2) {
        return (((a1 & 0x7f) * 128) + (a2 & 0x7f));
    } // end of static private int LOCO_ADR(int a1, int a2)

    /**
     * Convert bytes from LocoNet packet into a 1-based address for a sensor or
     * turnout.
     * 
     * @param a1
     *            Byte containing the upper bits
     * @param a2
     *            Byte containing the lower bits
     * @return 1-4096 address
     */
    static private int SENSOR_ADR(int a1, int a2) {
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f)) + 1;
    } // end of static private int SENSOR_ADR(int a1, int a2)

    /*
     * Take an int and convert it to a dotted version number
     * as used by the LocoIO protocol
     * Example:  123 => 1.2.3
     */
    
    /**
     * Take the LocoIO version number and convert to human friendly format.
     * 
     * @param val
     *            The LocoIO version.
     * @return String with human readable format.
     */
    private String dotme(int val) {
        int dit;
        int x = val;
        StringBuffer ret = new StringBuffer();
        if (val == 0)
            return "0";
        while (x != 0) {
            dit = x % 10;
            ret.insert(0, Integer.toString(dit));
            x = x / 10;
            if (x != 0)
                ret.insert(0, ".");
        }
        return ret.toString();
    } // end of private String dotme(int val)
    
    /**
     * Convert throttle ID to a human friendly format.
     * 
     * @param id1
     *            Byte #1 of the ID.
     * @param id2
     *            Byte #2 of the ID.
     * @return String with human friendly format.
     */
    private String idString(int id1, int id2) {
        return "0x" + Integer.toHexString(id2 & 0x7F) + " 0x"
               + Integer.toHexString(id1 & 0x7F) + " ("
               + ((id2 & 0x7F) * 128 + (id1 & 0x7F)) + ")";
    } // end of private String idString(int id1, int id2)

    /**
     * This function creates a string representation of the loco address in
     * addressLow & addressHigh in a form appropriate for the type of address (2
     * or 4 digit) using the Digitrax 'mixed mode' if necessary.
     * 
     * @param addressLow
     * @param addressHigh
     * @return
     */
    private static String convertToMixed(int addressLow, int addressHigh) {

        // if we have a 2 digit decoder address and proceed accordingly
        if (addressHigh == 0) {
            if (addressLow >= 120)
                return "c" + String.valueOf(addressLow - 120) + " ("
                       + String.valueOf(addressLow) + ")";
            else if (addressLow >= 110)
                return "b" + String.valueOf(addressLow - 110) + " ("
                       + String.valueOf(addressLow) + ")";
            else if (addressLow >= 100)
                return "a" + String.valueOf(addressLow - 100) + " ("
                       + String.valueOf(addressLow) + ")";
            else
                return String.valueOf(addressLow & 0x7f);
        } else {
            // return the full 4 digit address
            return String.valueOf(LOCO_ADR(addressHigh, addressLow));
        }
    } // end of private static String convertToMixed(int addressLow, int addressHigh)

    /**
     * Format the message into a text string.  If forceHex is set
     * upon return, the message was not fully parsed.
     * @param l Message to parse
     * @return String representation
     */
    protected String format(LocoNetMessage l) {

        boolean showStatus = false; /* show track status in this message? */

        int minutes; // temporary time values
        int hours;
        int frac_mins;

        switch (l.getOpCode()) {
        /*
         * 2 Byte MESSAGE OPCODES * ; FORMAT = <OPC>,<CKSUM> * ; *
         */
     
            /*
             * OPC_IDLE 0x85 ;FORCE IDLE state, Broadcast emergency STOP
             * 
             * Page 8 of LocoNet Personal Edition v1.0.
             */
        case LnConstants.OPC_IDLE: {
            return "Force Idle, Broadcast Emergency STOP.\n";
        } // case LnConstants.OPC_IDLE

            /*
             * OPC_GPON 0x83 ;GLOBAL power ON request
             * 
             * Page 8 of LocoNet Personal Edition v1.0.
             */
       case LnConstants.OPC_GPON: {
            return "Global Power ON.\n";
       } // case LnConstants.OPC_GPON
            
            /*
             * OPC_GPOFF 0x82 ;GLOBAL power OFF request
             * 
             * Page 8 of LocoNet Personal Edition v1.0.
             */
        case LnConstants.OPC_GPOFF: {
            return "Global Power OFF.\n";
        } // case LnConstants.OPC_GPOFF
            
            /*
             * OPC_GPBUSY 0x81 ;MASTER busy code, NULL
             * 
             * Page 8 of LocoNet Personal Edition v1.0.
             */
        case LnConstants.OPC_GPBUSY: {
            return "Master is busy.\n";
        } // case LnConstants.OPC_GPBUSY

        /*
         * ; 4 byte MESSAGE OPCODES
         * ; FORMAT = <OPC>,<ARG1>,<ARG2>,<CKSUM>
         * :
         *  CODES 0xA8 to 0xAF have responses
         *  CODES 0xB8 to 0xBF have responses
         */

            /*
             * OPC_LOCO_ADR     0xBF   ; REQ loco ADR
             *                         ; Follow on message: <E7>SLOT READ
             *                         ; <0xBF>,<0>,<ADR>,<CHK> REQ loco ADR
             *                         ; DATA return <E7>, is SLOT#, DATA that ADR was
             *                         : found in.
             *                         ; IF ADR not found, MASTER puts ADR in FREE slot
             *                         ; and sends DATA/STATUS return <E7>......
             *                         ; IF no FREE slot, Fail LACK,0 is returned
             *                         ; [<B4>,<3F>,<0>,<CHK>]
             *                         
             * Page 8 of LocoNet Personal Edition v1.0.
             */
        case LnConstants.OPC_LOCO_ADR: {
            int adrHi = l.getElement(1); // Hi address listed as zero above
            int adrLo = l.getElement(2); // ADR above, the low part
            return "Request slot for loco address "
                   + convertToMixed(adrLo, adrHi) + ".\n";
        } // case LnConstants.OPC_LOCO_ADR

            /*
             * OPC_SW_ACK       0xBD   ; REQ SWITCH WITH acknowledge function (not DT200)
             *                         ; Follow on message: LACK
             *                         ; <0xBD>,<SW1>,<SW2>,<CHK> REQ SWITCH function
             *                         ;       <SW1> =<0,A6,A5,A4- A3,A2,A1,A0>
             *                         ;               7 ls adr bits.
             *                         ;               A1,A0 select 1 of 4 input pairs
             *                         ;               in a DS54
             *                         ;       <SW2> =<0,0,DIR,ON- A10,A9,A8,A7>
             *                         ;               Control bits and 4 MS adr bits.
             *                         ;               DIR=1 for Closed/GREEN
             *                         ;                  =0 for Thrown/RED
             *                         ;               ON=1 for Output ON
             *                         ;                 =0 FOR output OFF
             *                         ; response is:
             *                         ; <0xB4><3D><00> if DCS100 FIFO is full, rejected.
             *                         ; <0xB4><3D><7F> if DCS100 accepted
             *
             * Page 8 of LocoNet Personal Edition v1.0.
             */
       case LnConstants.OPC_SW_ACK: {
            int sw1 = l.getElement(1);
            int sw2 = l.getElement(2);
            return "Request switch "
                   + String.valueOf(SENSOR_ADR(sw1, sw2))
                   + ((sw2 & LnConstants.OPC_SW_ACK_CLOSED) != 0 ? " Closed/Green"
                           : " Thrown/Red")
                   + ((sw2 & LnConstants.OPC_SW_ACK_OUTPUT) != 0 ? " (Output On)"
                           : " (Output Off)") + " with acknowledgement.\n";
       } // case LnConstants.OPC_SW_ACK
            
           /*
            * OPC_SW_STATE     0xBC   ; REQ state of SWITCH
            *                         ; Follow on message: LACK
            *                         ; <0xBC>,<SW1>,<SW2>,<CHK> REQ state of SWITCH
            *
            * Page 8 of LocoNet Personal Edition v1.0.
            */
        case LnConstants.OPC_SW_STATE: {
            int sw1 = l.getElement(1);
            int sw2 = l.getElement(2);
            return "Request status of switch "
                   + String.valueOf(SENSOR_ADR(sw1, sw2)) + ".\n";
        } // case LnConstants.OPC_SW_STATE


            /*
             * OPC_RQ_SL_DATA   0xBB   ; Request SLOT DATA/status block
             *                         ; Follow on message: <E7>SLOT READ
             *                         ; <0xBB>,<SLOT>,<0>,<CHK> Request SLOT DATA/status block.
             *
             * Page 8 of LocoNet Personal Edition v1.0.
             */
        case LnConstants.OPC_RQ_SL_DATA: {
            int slot = l.getElement(1);

            switch (slot) {
            // Slots > 120 are all special, but these are the only ones we know to decode.
            case LnConstants.FC_SLOT:
                return "Request Fast Clock information.\n";
            case LnConstants.CFG_SLOT:
                return "Request Command Station Ops Switches.\n";
            case LnConstants.PRG_SLOT:
                return "Request Programming Track information.\n";
            default:
                return "Request data/status for slot " + slot + ".\n";
            }
        } // case LnConstants.OPC_RQ_SL_DATA
           
            /*
             * OPC_MOVE_SLOTS   0xBA   ; MOVE slot SRC to DEST
             *                         ; Follow on message: <E7>SLOT READ
             *                         ; <0xBA>,<SRC>,<DEST>,<CHK> Move SRC to DEST if
             *                         ; SRC or LACK etc is NOT IN_USE, clr SRC
             *                         ; SPECIAL CASES:
             *                         ; If SRC=0 ( DISPATCH GET) , DEST=dont care,
             *                         ;    Return SLOT READ DATA of DISPATCH Slot
             *                         ; IF SRC=DEST (NULL move) then SRC=DEST is set to
             *                         ;    IN_USE , if legal move.
             *                         ; If DEST=0, is DISPATCH Put, mark SLOT as DISPATCH
             *                         ;    RETURN slot status <0xE7> of DESTINATION slot
             *                         ;       DEST if move legal
             *                         ;    RETURN Fail LACK code if illegal move
             *                         ;       <B4>,<3A>,<0>,<chk>, illegal to move to/from
             *                         ;       slots 120/127
             *                         
             * Page 8 of LocoNet Personal Edition v1.0.
             */
        case LnConstants.OPC_MOVE_SLOTS: {
            int src = l.getElement(1);
            int dest = l.getElement(2);

            /* check special cases */
            if (src == 0) { /* DISPATCH GET */
                return "Get most recently dispatched slot.\n";
            } else if (src == dest) { /* IN USE */
                return "Set status of slot " + src + " to IN_USE.\n";
            } else if (dest == 0) { /* DISPATCH PUT */
                return "Mark slot " + src + " as DISPATCHED.\n";
            } else { /* general move */
                return "Move data in slot " + src + " to slot " + dest + ".\n";
            }
        } // case LnConstants.OPC_MOVE_SLOTS

            /*
             * OPC_LINK_SLOTS   0xB9   ; LINK slot ARG1 to slot ARG2=
             *                         ; Follow on message: <E7>SLOT READ=
             *                         ; <0xB9>,<SL1>,<SL2>,<CHK> SLAVE slot SL1 to slot SL2
             *                         ; Master LINKER sets the SL_CONUP/DN flags
             *                         ; appropriately. Reply is return of SLOT Status
             *                         ; <0xE7>. Inspect to see result of Link, invalid
             *                         ; Link will return Long Ack Fail <B4>,<39>,<0>,<CHK>
             *
             * Page 9 of LocoNet Personal Edition v1.0.
             */
        case LnConstants.OPC_LINK_SLOTS: {
            int src = l.getElement(1);
            int dest = l.getElement(2);
            return "Consist loco in slot " + src + " to loco in slot " + dest + ".\n";
        } // case LnConstants.OPC_LINK_SLOTS

            /*
             * OPC_UNLINK_SLOTS 0xB8   ;UNLINK slot ARG1 from slot ARG2
             *                         ; Follow on message: <E7>SLOT READ
             *                         ; <0xB8>,<SL1>,<SL2>,<CHK> UNLINK slot SL1 from SL2
             *                         ; UNLINKER executes unlink STRATEGY and returns new SLOT#
             *                         ; DATA/STATUS of unlinked LOCO . Inspect data to evaluate UNLINK
             *
             * Page 9 of LocoNet Personal Edition v1.0.
             */
        case LnConstants.OPC_UNLINK_SLOTS: {
            int src = l.getElement(1);
            int dest = l.getElement(2);
            return "Remove loco in slot " + src + " from consist with loco in slot " + dest + ".\n";
        } // case LnConstants.OPC_UNLINK_SLOTS

            /*
             * OPC_CONSIST_FUNC 0xB6   ; SET FUNC bits in a CONSIST uplink element
             *                         ; <0xB6>,<SLOT>,<DIRF>,<CHK> UP consist FUNC bits
             *                         ; NOTE this SLOT adr is considered in UPLINKED slot space.
             *
             * Page 9 of LocoNet Personal Edition v1.0.
             */
        case LnConstants.OPC_CONSIST_FUNC: {
            int slot = l.getElement(1);
            int dirf = l.getElement(2);
            return "Set consist in slot " + slot + " direction to "
                   + ((dirf & LnConstants.DIRF_DIR) != 0 ? "REV" : "FWD")
                   + "F0="
                   + ((dirf & LnConstants.DIRF_F0) != 0 ? "On, " : "Off,")
                   + "F1="
                   + ((dirf & LnConstants.DIRF_F1) != 0 ? "On, " : "Off,")
                   + "F2="
                   + ((dirf & LnConstants.DIRF_F2) != 0 ? "On, " : "Off,")
                   + "F3="
                   + ((dirf & LnConstants.DIRF_F3) != 0 ? "On, " : "Off,")
                   + "F4=" 
                   + ((dirf & LnConstants.DIRF_F4) != 0 ? "On" : "Off")
                   + ".\n";
        } // case LnConstants.OPC_CONSIST_FUNC
        
            /*
             * OPC_SLOT_STAT1   0xB5   ; WRITE slot stat1
             *                         ; <0xB5>,<SLOT>,<STAT1>,<CHK> WRITE stat1
             *
             * Page 9 of LocoNet Personal Edition v1.0.
             */
        case LnConstants.OPC_SLOT_STAT1: {
            int slot = l.getElement(1);
            int stat = l.getElement(2);
            return "Write slot " + slot + " with status value " + stat 
                   + " (0x"+ Integer.toHexString(stat) + ") - Loco is " 
                   + LnConstants.CONSIST_STAT(stat) + ", " + LnConstants.LOCO_STAT(stat) 
                   + "\n\tand operating in " + LnConstants.DEC_MODE(stat) + " speed step mode.\n";
        } // case LnConstants.OPC_SLOT_STAT1
        
            /*
             * OPC_LONG_ACK     0xB4   ; Long acknowledge
             *                         ; <0xB4>,<LOPC>,<ACK1>,<CHK> Long acknowledge
             *                         ; <LOPC> is COPY of OPCODE responding to (msb=0).
             *                         ; LOPC=0 (unused OPC) is also VALID fail code
             *                         ; <ACK1> is appropriate response code for the OPCode
             *
             * Page 9 of LocoNet Personal Edition v1.0.
             */
       case LnConstants.OPC_LONG_ACK: {
            int opcode = l.getElement(1);
            int ack1 = l.getElement(2);

            switch (opcode | 0x80) {
            case (LnConstants.OPC_LOCO_ADR):
                // response for OPC_LOCO_ADR
                return "LONG_ACK: NO FREE SLOTS!\n";
            
            case (LnConstants.OPC_LINK_SLOTS):
                // response for OPC_LINK_SLOTS
                return "LONG_ACK: Invalid consist, unable to link.\n";
            
            case (LnConstants.OPC_SW_ACK):
                // response for OPC_SW_ACK
                if (ack1 == 0) {
                    return "LONG_ACK: The Command Station FIFO is full, the switch command was rejected.\n";
                } else if (ack1 == 0x7f) {
                    return "LONG_ACK: The Command Station accepted the switch command.\n";
                } else {
                    forceHex = true;
                    return "LONG_ACK: Unknown response to 'Request Switch with ACK' command, value 0x"
                           + Integer.toHexString(ack1) + ".\n";
                }

            case (LnConstants.OPC_SW_REQ):
                // response for OPC_SW_REQ
                return "LONG_ACK: Switch request Failed!\n";

            case (LnConstants.OPC_WR_SL_DATA):
                // response for OPC_WR_SL_DATA
                if (ack1 == 0) {
                    return "LONG_ACK: The Slot Write command was rejected.\n";
                } else if (ack1 == 0x01) {
                    return "LONG_ACK: The Slot Write command was accepted.\n";
                } else if (ack1 == 0x23 || ack1 == 0x2b || ack1 == 0x6B) {
                    return "LONG_ACK: DCS51 programming reply, thought to mean OK.\n";
                } else if (ack1 == 0x40) {
                    return "LONG_ACK: The Slot Write command was accepted blind (no response will be sent).\n";
                } else if (ack1 == 0x7f) {
                    return "LONG_ACK: Function not implemented, no reply will follow.\n";
                } else {
                    forceHex = true;
                    return "LONG_ACK: Unknown response to Write Slot Data message value 0x"
                           + Integer.toHexString(ack1) + ".\n";
                }

            case (LnConstants.OPC_SW_STATE):
                // response for OPC_SW_STATE
                return "LONG_ACK: Command station response to switch state request 0x"
                       + Integer.toHexString(ack1)
                       + (((ack1 & 0x20) != 0) ? " (Closed)" : " (Thrown)")
                       + ".\n";

            case (LnConstants.OPC_MOVE_SLOTS):
                // response for OPC_MOVE_SLOTS
                if (ack1 == 0) {
                    return "LONG_ACK: The Move Slots command was rejected.\n";
                } else if (ack1 == 0x7f) {
                    return "LONG_ACK: The Move Slots command was accepted.\n";
                } else {
                    forceHex = true;
                    return "LONG_ACK: unknown reponse to Move Slots message 0x"
                           + Integer.toHexString(ack1) + ".\n";
                }

            case LnConstants.OPC_IMM_PACKET: 
                // response for OPC_IMM_PACKET
                if (ack1 == 0) {
                    return "LONG_ACK: the Send IMM Packet command was rejected, the buffer is full/busy.\n";
                } else if (ack1 == 0x7f) {
                    return "LONG_ACK: the Send IMM Packet command was accepted.\n";
                } else {
                    forceHex = true;
                    return "LONG_ACK: Unknown reponse to Send IMM Packet value 0x"
                           + Integer.toHexString(ack1) + ".\n";
                }

            case LnConstants.OPC_IMM_PACKET_2:
                // response for OPC_IMM_PACKET
                return "LONG_ACK: the Lim Master responded to the Send IMM Packet command with "
                       + ack1 + " (0x" + Integer.toHexString(ack1) + ").\n";

            default:
                // forceHex = TRUE;
                return "LONG_ACK: Response " + ack1 
                       + " (0x" + Integer.toHexString(ack1) + ") to opcode 0x"
                       + Integer.toHexString(opcode) + " not decoded.\n";
            } // switch (opcode | 0x80)
       } // case LnConstants.OPC_LONG_ACK

           /*
            * OPC_INPUT_REP    0xB2   ; General SENSOR Input codes
            *                         ; <0xB2>, <IN1>, <IN2>, <CHK>
            *                         ;   <IN1> =<0,A6,A5,A4- A3,A2,A1,A0>,
            *                         ;           7 ls adr bits.
            *                         ;           A1,A0 select 1 of 4 inputs pairs in a DS54.
            *                         ;   <IN2> =<0,X,I,L- A10,A9,A8,A7>,
            *                         ;           Report/status bits and 4 MS adr bits.
            *                         ;           "I"=0 for DS54 "aux" inputs
            *                         ;              =1 for "switch" inputs mapped to 4K SENSOR space.
            *                         ;
            *                         ;           (This is effectively a least significant adr bit when
            *                         ;            using DS54 input configuration)
            *                         ;
            *                         ;           "L"=0 for input SENSOR now 0V (LO),
            *                         ;              =1 for Input sensor >=+6V (HI)
            *                         ;           "X"=1, control bit,
            *                         ;              =0 is RESERVED for future!
            *
            * Page 9 of LocoNet Personal Edition v1.0.
            */
        case LnConstants.OPC_INPUT_REP: {
            int in1 = l.getElement(1);
            int in2 = l.getElement(2);
            int sensor = (SENSOR_ADR(in1, in2) - 1) * 2
                         + ((in2 & LnConstants.OPC_INPUT_REP_SW) != 0 ? 2 : 1);

            int bdlid = ((sensor - 1) / 16) + 1;
            int bdlin = ((sensor - 1) % 16) + 1;
            String bdl = "BDL16 #" + bdlid + ", DS" + bdlin;

            int boardid = ((sensor - 1) / 8) + 1;
            int boardindex = ((sensor - 1) % 8);
            String ds54sensors[] = { "AuxA", "SwiA", "AuxB", "SwiB", "AuxC", "SwiC", "AuxD", "SwiD" };
            String ds64sensors[] = { "A1", "S1", "A2", "S2", "A3", "S3", "A4", "S4" };
            String se8csensors[] = { "DS01", "DS02", "DS03", "DS04", "DS05", "DS06", "DS07", "DS08" };

            // There is no way to tell what kind of a board sent the message.
            // To be user friendly, we just print all the known combos.
            return "Sensor " + sensor + " is "
                   + ((in2 & LnConstants.OPC_INPUT_REP_HI) != 0 ? "Hi" : "Lo")
                   + ".  (" + bdl + "; DS54/64"
                   + (sensor < 289 ? "/SE8c #" : " #") 
                   + boardid + ", "
                   + ds54sensors[boardindex] + "/" + ds64sensors[boardindex]
                   + ((sensor < 289) ? "/" + se8csensors[boardindex] : "")
                   + ")\n";
        } // case LnConstants.OPC_INPUT_REP

            /*
             * OPC_SW_REP       0xB1   ; Turnout SENSOR state REPORT
             *                         ; <0xB1>,<SN1>,<SN2>,<CHK> SENSOR state REPORT
             *                         ;   <SN1> =<0,A6,A5,A4- A3,A2,A1,A0>,
             *                         ;           7 ls adr bits.
             *                         ;           A1,A0 select 1 of 4 input pairs in a DS54
             *                         ;   <SN2> =<0,1,I,L- A10,A9,A8,A7>
             *                         ;           Report/status bits and 4 MS adr bits.
             *                         ;           this <B1> opcode encodes input levels
             *                         ;           for turnout feedback
             *                         ;           "I" =0 for "aux" inputs (normally not feedback),
             *                         ;               =1 for "switch" input used for
             *                         ;                  turnout feedback for DS54
             *                         ;                  ouput/turnout # encoded by A0-A10
             *                         ;           "L" =0 for this input 0V (LO),
             *                         ;               =1 this input > +6V (HI)
             *                         ;
             *                         ;   alternately;
             *                         ;
             *                         ;   <SN2> =<0,0,C,T- A10,A9,A8,A7>
             *                         ;           Report/status bits and 4 MS adr bits.
             *                         ;           this <B1> opcode encodes current OUTPUT levels
             *                         ;           "C" =0 if "Closed" ouput line is OFF,
             *                         ;               =1 "closed" output line is ON
             *                         ;                  (sink current)
             *                         ;           "T" =0 if "Thrown" output line is OFF,
             *                         ;               =1 "thrown" output line is ON
             *                         ;                  (sink I)
             *
             * Page 9 of LocoNet Personal Edition v1.0.
             */
         case LnConstants.OPC_SW_REP: {
            int sn1 = l.getElement(1);
            int sn2 = l.getElement(2);

            if ((sn2 & LnConstants.OPC_SW_REP_INPUTS) != 0) {
                return "Turnout "
                       + SENSOR_ADR(sn1, sn2)
                       + ((sn2 & LnConstants.OPC_SW_REP_SW) != 0 ? " Switch input" : " Aux input")
                       + " is "
                       + (((sn2 & LnConstants.OPC_SW_REP_HI) != 0) ? "Closed (input off)"
                               : "Thrown (input on)") + ".\n";
            } else { // OPC_SW_REP_INPUTS is 0
                return "Turnout " + SENSOR_ADR(sn1, sn2) + " output state: Closed output is "
                       + ((sn2 & LnConstants.OPC_SW_REP_CLOSED) != 0 ? "ON (sink)" : "OFF (open)")
                       + ", Thrown output is "
                       + ((sn2 & LnConstants.OPC_SW_REP_THROWN) != 0 ? "ON (sink)" : "OFF (open)")
                       + ".\n";
            }
        } // case LnConstants.OPC_SW_REP

             /*
              * OPC_SW_REQ       0xB0   ; REQ SWITCH function
              *                         ; <0xB0>,<SW1>,<SW2>,<CHK> REQ SWITCH function
              *                         ;   <SW1> =<0,A6,A5,A4- A3,A2,A1,A0>,
              *                         ;           7 ls adr bits.
              *                         ;           A1,A0 select 1 of 4 input pairs in a DS54
              *                         ;   <SW2> =<0,0,DIR,ON- A10,A9,A8,A7>
              *                         ;           Control bits and 4 MS adr bits.
              *                         ;   DIR  =1 for Closed,/GREEN,
              *                         ;        =0 for Thrown/RED
              *                         ;   ON   =1 for Output ON,
              *                         ;        =0 FOR output OFF
              *                         ;
              *                         ;   Note-Immediate response of <0xB4><30><00> if command failed,
              *                         ;        otherwise no response "A" CLASS codes
              *
              * Page 9 of LocoNet Personal Edition v1.0.
              * Page 12 special form Broadcast.
              * Page 13 special form LocoNet interrogate.
              */
        case LnConstants.OPC_SW_REQ: {
            int sw1 = l.getElement(1);
            int sw2 = l.getElement(2);

            String retVal;

            /*
             * This is probably poor code structure. The decodes of bits a/c/b
             * and the resulting list of addresses are used by 2 of the three
             * cases below. We construct them every time even though one case
             * may not use them.
             */
            int a = (sw2 & 0x20) >> 5;
            int c = (sw1 & 0x02) >> 1;
            int b = (sw1 & 0x01);

            /*
             * All this blob does is loop through the ranges indicated by the
             * a/c/b bits, they are mask bits in the midde of the range. The
             * idea is to get 8 sensors at a time, since that is generally what
             * units have, and to query units 1, 9, 17... then 2, 10, 18... and
             * so on such that if they are all in a row they don't get hit at
             * the same time.
             * 
             * This is also part of the poor code structure, as it is only used
             * by 2 of the three cases.
             */
           int topbits = 0;
            int midbits = (a << 2) + (c << 1) + b;
            int count = 0;
            String addrList = "";
            for (topbits = 0; topbits < 32; topbits++) {
                // The extra "+1" adjusts for the fact that we show 1-2048,
                // rather than 0-2047 on the wire.
                int lval = (topbits << 6) + (midbits << 3) + 1;
                int hval = lval + 7;

                if ((count % 8) > 0) {
                    addrList = addrList + ", ";
                } else {
                    if (count == 0) {
                        addrList = addrList + "\t";
                    } else {
                        addrList = addrList + ",\n\t";
                    }
                }
                addrList = addrList + lval + "-" + hval;
                count++;
            }
            addrList = addrList + "\n";

            if (((sw2 & 0xCF) == 0x0F) && ((sw1 & 0xFC) == 0x78)) {
                // broadcast address LPU V1.0 page 12
                retVal = "Interrogate Stationary Decoders with bits a/c/b of " + a + "/" + c + "/"
                         + b + "; turnouts...\n" + addrList;
            } else if (((sw2 & 0xCF) == 0x07) && ((sw1 & 0xFC) == 0x78)) {
                // broadcast address LPU V1.0 page 13
                retVal = "Interrogate LocoNet Turnouts/Sensors with bits a/c/b of " + a + "/" + c
                         + "/" + b + "; addresses...\n" + addrList;
            } else {
                // ordinary form, LPU V1.0 page 9
                retVal = "Requesting Switch at " + SENSOR_ADR(sw1, sw2) + " to "
                         + ((sw2 & LnConstants.OPC_SW_REQ_DIR) != 0 ? "Closed" : "Thrown")
                         + " (output " + ((sw2 & LnConstants.OPC_SW_REQ_OUT) != 0 ? "On" : "Off")
                         + ").\n";
            }

           return retVal;
        } // case LnConstants.OPC_SW_REQ

            /*
             * OPC_LOCO_SND     0xA2   ;SET SLOT sound functions
             *
             * Page 10 of LocoNet Personal Edition v1.0.
             */
        case LnConstants.OPC_LOCO_SND: {
            int slot = l.getElement(1);
            int snd = l.getElement(2);

            return "Set loco in slot " + slot + " Sound1/F5="
                   + ((snd & LnConstants.SND_F5) != 0 ? "On" : "Off")
                   + ", Sound2/F6="
                   + ((snd & LnConstants.SND_F6) != 0 ? "On" : "Off")
                   + ", Sound3/F7="
                   + ((snd & LnConstants.SND_F7) != 0 ? "On" : "Off")
                   + ", Sound4/F8="
                   + ((snd & LnConstants.SND_F8) != 0 ? "On" : "Off") + ".\n";
        } // case LnConstants.OPC_LOCO_SND

            /*
             * OPC_LOCO_DIRF 0xA1 ;SET SLOT dir, F0-4 state
             *
             * Page 10 of LocoNet Personal Edition v1.0.
             */
        case LnConstants.OPC_LOCO_DIRF: {
            int slot = l.getElement(1);
            int dirf = l.getElement(2);

            return "Set loco in slot " + slot + " direction to "
                   + ((dirf & LnConstants.DIRF_DIR) != 0 ? "REV" : "FWD") 
                   + ", F0="
                   + ((dirf & LnConstants.DIRF_F0) != 0 ? "On, " : "Off,") 
                   + " F1="
                   + ((dirf & LnConstants.DIRF_F1) != 0 ? "On, " : "Off,") 
                   + " F2="
                   + ((dirf & LnConstants.DIRF_F2) != 0 ? "On, " : "Off,") 
                   + " F3="
                   + ((dirf & LnConstants.DIRF_F3) != 0 ? "On, " : "Off,") 
                   + " F4="
                   + ((dirf & LnConstants.DIRF_F4) != 0 ? "On" : "Off") 
                   + ".\n";
        } // case LnConstants.OPC_LOCO_DIRF

            /*
             * OPC_LOCO_SPD 0xA0 ;SET SLOT speed e.g. <0xA0><SLOT#><SPD><CHK>
             *
             * Page 10 of LocoNet Personal Edition v1.0.
             */
        case LnConstants.OPC_LOCO_SPD: {
            int slot = l.getElement(1);
            int spd = l.getElement(2);

            if (spd == LnConstants.OPC_LOCO_SPD_ESTOP) {
                return "Set speed of loco in slot " + slot + " to EMERGENCY STOP!\n";
            } else {
                return "Set speed of loco in slot " + slot + " to " + spd + ".\n";
            }
        } // case LnConstants.OPC_LOCO_SPD

        /*
         * ; 6 byte MESSAGE OPCODES
         * ; FORMAT = <OPC>,<ARG1>,<ARG2>,<ARG3>,<ARG4>,<CKSUM>
         * :
         *  CODES 0xC8 to 0xCF have responses
         *  CODES 0xD8 to 0xDF have responses
         */

            /*
             * OPC_PANEL_QUERY 0xDF messages used by throttles to discover
             * panels
             * 
             * This op code is not documented by Digitrax. Reverse engineering
             * performed by Leo Bicknell.
             */
        case LnConstants.OPC_PANEL_QUERY: {
            switch (l.getElement(1)) {
            case 0x00: {
                return "Query Panels.\n";
            }
            case 0x40: {
                if (l.getElement(2) == 0x1F) {
                    return "Set LocoNet ID to " + l.getElement(3) + " and Query Panels.\n";
                } else {
                    return "Unknown attempt to set the Loconet ID 0x" + Integer.toHexString(l.getElement(2))
                           + ".\n";
                }
            }
            default: {
                return "Unknown Query Panels Request 0x" + Integer.toHexString(l.getElement(1))
                       + ".\n";
            }
            }
        } // case LnConstants.OPC_PANEL_QUERY

            /*
             * OPC_PANEL_RESPONSE 0xD7 messages used by throttles to discover
             * panels
             * 
             * This op code is not documented by Digitrax. Reverse engineering
             * performed by Leo Bicknell.
             */
        case LnConstants.OPC_PANEL_RESPONSE: {
            switch (l.getElement(1)) {

            case 0x12: {
                // Bit 3 (0x08 in hex) is set by every UR-92 we've ever captured.
            	// The hypothesis is this indicates duplex enabled, but this has
            	// not been confirmed with Digitrax.
                return "UR-92 Responding with LocoNet ID " + (l.getElement(3) & 0x07)
                       + ((l.getElement(3) & 0x08) == 0x08 ? ", duplex enabled.\n" : ".\n");
            }
            case 0x17: {
                return "UR-90 Responding with LocoNet ID " + l.getElement(3) + ".\n";
            }
            case 0x1F: {
                return "UR-91 Responding with LocoNet ID " + l.getElement(3) + ".\n";
            }
            default: {
                return "Unknown Panel of type 0x" + Integer.toHexString(l.getElement(1))
                       + " responding.\n";

            }
            }
        } // case LnConstants.OPC_PANEL_RESPONSE

            /*
             * OPC_MULTI_SENSE 0xD0 messages about power management and
             * transponding
             * 
             * If byte 1 high nibble is 0x20 or 0x00 this is a transponding
             * message
             * 
             * This op code is not documented by Digitrax. Reverse engineering
             * performed by Al Silverstein, and corrections added by Bob Milhaupt.
             */
        case LnConstants.OPC_MULTI_SENSE: {
            int type = l.getElement(1) & LnConstants.OPC_MULTI_SENSE_MSG;
            String m;

            String zone;
            switch (l.getElement(2) & 0x0F) {
            case 0x00:
                zone = "A";
                break;
            case 0x02:
                zone = "B";
                break;
            case 0x04:
                zone = "C";
                break;
            case 0x06:
                zone = "D";
                break;
            case 0x08:
                zone = "E";
                break;
            case 0x0A:
                zone = "F";
                break;
            case 0x0C:
                zone = "G";
                break;
            case 0x0E:
                zone = "H";
                break;
            default:
                zone = "<unknown " + (l.getElement(2) & 0x0F) + ">";
                break;
            }

            int section = (l.getElement(2) / 16) + (l.getElement(1) & 0x1F) * 8;

            switch (type) {
            case LnConstants.OPC_MULTI_SENSE_POWER:
                // This is a PM42 power event.
                int pCMD = (l.getElement(3) & 0xF0);

                if ((pCMD == 0x30) || (pCMD == 0x10)) {
                    // autoreverse
                    int cm1 = l.getElement(3);
                    int cm2 = l.getElement(4);
                    return "PM4 " + (l.getElement(2) + 1) + " ch1 "
                           + ((cm1 & 1) != 0 ? "AR " : "SC ") 
                           + ((cm2 & 1) != 0 ? "ACT;" : "OK;")
                           + " ch2 " 
                           + ((cm1 & 2) != 0 ? "AR " : "SC ")
                           + ((cm2 & 2) != 0 ? "ACT;" : "OK;") 
                           + " ch3 "
                           + ((cm1 & 4) != 0 ? "AR " : "SC ") 
                           + ((cm2 & 4) != 0 ? "ACT;" : "OK;")
                           + " ch4 " 
                           + ((cm1 & 8) != 0 ? "AR " : "SC ")
                           + ((cm2 & 8) != 0 ? "ACT;" : "OK;") 
                           + "\n";
                } else if (pCMD == 0x70) {
                    // programming
                    String device;
                    if ((l.getElement(3) & 0x7) == 0)
                        device = "PM ";
                    else if ((l.getElement(3) & 0x7) == 1)
                        device = "BD ";
                    else if ((l.getElement(3) & 0x7) == 2)
                        device = "SE ";
                    else
                        device = "(unknown type) ";

                    int bit = (l.getElement(4) & 0x0E) / 2;
                    int val = (l.getElement(4) & 0x01);
                    int wrd = (l.getElement(4) & 0x70) / 16;
                    int opsw = (l.getElement(4) & 0x7E) / 2 + 1;
                    int bdaddr = l.getElement(2) + 1;
                    if ((l.getElement(1) & 0x1) != 0)
                        bdaddr += 128;
                    return device
                           + bdaddr
                           + (((l.getElement(1) & 0x10) != 0) ? " write config bit "
                                   : " read config bit ") + wrd + "," + bit + " (opsw " + opsw
                           + ") val=" + val + (val == 1 ? " (closed) " : " (thrown) ") + "\n";
                } else { 
                    // beats me
                    forceHex = true;
                    return "OPC_MULTI_SENSE power message PM4 " + (l.getElement(2) + 1)
                           + " unknown CMD=0x" + Integer.toHexString(pCMD) + " ";
                }

            case LnConstants.OPC_MULTI_SENSE_PRESENT:
                // Transponding Event
                m = "Transponder present in section " + section + " zone " + zone
                    + " decoder address ";
                if (l.getElement(3) == 0x7D)
                    m += l.getElement(4) + " (short) ";
                else
                    m += l.getElement(3) * 128 + l.getElement(4) + " (long) ";
                return m + ".\n";
            case LnConstants.OPC_MULTI_SENSE_ABSENT:
                // Transponding Event
                m = "Transponder absent in section " + section + " zone " + zone
                    + " decoder address ";
                if (l.getElement(3) == 0x7D)
                    m += l.getElement(4) + " (short) ";
                else
                    m += l.getElement(3) * 128 + l.getElement(4) + " (long) ";
                return m + ".\n";
            default:
                forceHex = true;
                return "OPC_MULTI_SENSE unknown format.\n";
            }
        } //  case LnConstants.OPC_MULTI_SENSE

        /*
         * ; VARIABLE byte MESSAGE OPCODES
         * ; FORMAT = <OPC>,<COUNT>,<ARG2>,<ARG3>,...,<ARG(COUNT-3)>,<CKSUM>
         * :
         */

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
        case LnConstants.OPC_WR_SL_DATA: /* page 10 of Loconet PE */
        case LnConstants.OPC_SL_RD_DATA: { // Page 10 of LocoNet PE
            String mode;
            String locoAdrStr;
            String mixedAdrStr;
            String logString;

            // rwSlotData = (rwSlotDataMsg *) msgBuf;
            int command = l.getElement(0);
            // int mesg_size = l.getElement(1); // ummmmm, size of the message
            // in bytes?
            int slot = l.getElement(2); // slot number for this request
            int stat = l.getElement(3); // slot status
            int adr = l.getElement(4); // loco address
            int spd = l.getElement(5); // command speed
            int dirf = l.getElement(6); // direction and F0-F4 bits
            int trk = l.getElement(7); // track status
            int ss2 = l.getElement(8); // slot status 2 (tells how to use
                                       // ID1/ID2 & ADV Consist)
            int adr2 = l.getElement(9); // loco address high
            int snd = l.getElement(10); // Sound 1-4 / F5-F8
            int id1 = l.getElement(11); // ls 7 bits of ID code
            int id2 = l.getElement(12); // ms 7 bits of ID code

            /* build loco address string */
            mixedAdrStr = convertToMixed(adr, adr2);

            /*
             * figure out the alias condition, and create the loco address
             * string
             */
            if (adr2 == 0x7f) {
                if ((ss2 & LnConstants.STAT2_ALIAS_MASK) == LnConstants.STAT2_ID_IS_ALIAS) {
                    /* this is an aliased address and we have the alias */
                    locoAdrStr = "" + LOCO_ADR(id2, id1) + " (Alias for loco "
                                 + mixedAdrStr + ")";
                } else {
                    /* this is an aliased address and we don't have the alias */
                    locoAdrStr = mixedAdrStr + " (via Alias)";
                }
            } else {
                /* regular 4 digit address, 128 to 9983 */
                locoAdrStr = mixedAdrStr;
            }

            /*
             * These share a common data format with the only difference being
             * whether we are reading or writing the slot data.
             */

            if (command == LnConstants.OPC_WR_SL_DATA) {
                mode = "Request";
            } else {
                mode = "Response";
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
                int clk_rate = l.getElement(3); // 0 = Freeze clock, 1 = normal,
                                                // 10 = 10:1 etc. Max is 0x7f
                int frac_minsl = l.getElement(4); // fractional minutes. not for
                                                  // external use.
                int frac_minsh = l.getElement(5);
                int mins_60 = l.getElement(6); // 256 - minutes
                int track_stat = l.getElement(7); // track status
                int hours_24 = l.getElement(8); // 256 - hours
                int days = l.getElement(9); // clock rollovers
                int clk_cntrl = l.getElement(10); // bit 6 = 1; data is valid
                                                  // clock info
                // "  " 0; ignore this reply
                // id1/id2 is device id of last device to set the clock
                // "   " = zero shows not set has happened

                /* recover hours and minutes values */
                minutes = ((255 - mins_60) & 0x7f) % 60;
                hours = ((256 - hours_24) & 0x7f) % 24;
                hours = (24 - hours) % 24;
                minutes = (60 - minutes) % 60;
                frac_mins = 0x3FFF - (frac_minsl + (frac_minsh << 7));

                /* check track status value and display */
                if ((trackStatus != track_stat) || showTrackStatus) {
                    trackStatus = track_stat;
                    showStatus = true;
                }

                if (showStatus) {
                    logString = mode + " Fast Clock is "
                                + ((clk_cntrl & 0x20) != 0 ? "" : "Synchronized, ")
                                + (clk_rate != 0 ? "Running, " : "Frozen, ")
                                + "rate is " + clk_rate
                                + ":1. Day " + days + ", " + hours + ":" + minutes + "." + frac_mins
                                + ". Last set by ID "+ idString(id1, id2)
                                + ".\n\tMaster: "
                                + ((track_stat & LnConstants.GTRK_MLOK1) != 0 ? "LocoNet 1.1" : "DT-200")
                                + "; Track Status: "
                                + ((track_stat & LnConstants.GTRK_POWER) != 0 ? "On" : "Off")
                                + "/"
                                + ((track_stat & LnConstants.GTRK_IDLE) == 0 ? "Paused" : "Running")
                                + "; Programming Track: "
                                + ((track_stat & LnConstants.GTRK_PROG_BUSY) != 0 ? "Busy" : "Available") + "\n";
                } else {
                    logString = mode + " Fast Clock is "
                                + ((clk_cntrl & 0x20) != 0 ? "" : "Synchronized, ")
                                + (clk_rate != 0 ? "Running, " : "Frozen, ")
                                + "rate is " + clk_rate
                                + ":1. Day " + days + ", " + hours + ":" + minutes + "." + frac_mins
                                + ". Last set by ID "+ idString(id1, id2)
                                + ".\n";
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
                String operation;
                String progMode;
                int cvData;
                boolean opsMode = false;
                int cvNumber;

                // progTask = (progTaskMsg *) msgBuf;
                // slot - slot number for this request - slot 124 is programmer
                int pcmd = l.getElement(3); // programmer command
                int pstat = l.getElement(4); // programmer status error flags in
                                             // reply message
                int hopsa = l.getElement(5); // Ops mode - 7 high address bits
                                             // of loco to program
                int lopsa = l.getElement(6); // Ops mode - 7 low address bits of
                                             // loco to program
                /* trk - track status. Note: bit 3 shows if prog track is busy */
                int cvh = l.getElement(8); // hi 3 bits of CV# and msb of data7
                int cvl = l.getElement(9); // lo 7 bits of CV#
                int data7 = l.getElement(10); // 7 bits of data to program, msb
                                              // is in cvh above

                cvData = (((cvh & LnConstants.CVH_D7) << 6) | (data7 & 0x7f)); // was
                                                                               // PROG_DATA
                cvNumber = (((((cvh & LnConstants.CVH_CV8_CV9) >> 3) | (cvh & LnConstants.CVH_CV7)) * 128) + (cvl & 0x7f)) + 1; // was
                                                                                                                                // PROG_CV_NUM(progTask)

                /* generate loco address, mixed mode or true 4 digit */
                mixedAdrStr = convertToMixed(lopsa, hopsa);

                /* determine programming mode for printing */
                if ((pcmd & LnConstants.PCMD_MODE_MASK) == LnConstants.PAGED_ON_SRVC_TRK) {
                    progMode = "Byte in Paged Mode on Service Track";
                } else if ((pcmd & LnConstants.PCMD_MODE_MASK) == LnConstants.DIR_BYTE_ON_SRVC_TRK) {
                    progMode = "Byte in Direct Mode on Service Track";
                } else if ((pcmd & LnConstants.PCMD_MODE_MASK) == LnConstants.DIR_BIT_ON_SRVC_TRK) {
                    progMode = "Bits in Direct Mode on Service Track";
                } else if (((pcmd & ~LnConstants.PCMD_BYTE_MODE) & LnConstants.PCMD_MODE_MASK) == LnConstants.REG_BYTE_RW_ON_SRVC_TRK) {
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
                } else if (((pcmd & ~LnConstants.PCMD_BYTE_MODE) & LnConstants.PCMD_MODE_MASK) == LnConstants.SRVC_TRK_RESERVED) {
                    progMode = "SERVICE TRACK RESERVED MODE DETECTED!";
                } else {
                    progMode = "Unknown mode " + pcmd + " (0x" + Integer.toHexString(pcmd) + ")";
                    forceHex = true;
                }

                /* are we sending or receiving? */
                if ((pcmd & LnConstants.PCMD_RW) != 0) {
                    /* sending a command */
                    operation = "Programming " + mode + ": Write " + progMode;

                    /* printout based on whether we're doing Ops mode or not */
                    if (opsMode) {
                        logString = operation + " to CV" + cvNumber + " of Loco " + mixedAdrStr
                                    + " value " + cvData + " (0x" + Integer.toHexString(cvData)
                                    + ", " + Integer.toBinaryString(cvData) + ").\n";

                    } else {
                        logString = operation + " to CV" + cvNumber + " value " + cvData + " (0x"
                                    + Integer.toHexString(cvData) + ", "
                                    + Integer.toBinaryString(cvData) + ").\n";
                    }
                } else {
                    /* receiving a reply */
                    operation = "Programming Track " + mode + ": Read " + progMode + " ";

                    /* if we're reading the slot back, check the status */
                    /* this is supposed to be the Programming task final reply */
                    /* and will have the resulting status byte */

                    if (command == LnConstants.OPC_SL_RD_DATA) {
                        if (pstat != 0) {
                            if ((pstat & LnConstants.PSTAT_USER_ABORTED) != 0) {
                                operation += "Failed, User Aborted: ";
                            }

                            if ((pstat & LnConstants.PSTAT_READ_FAIL) != 0) {
                                operation += "Failed, Read Compare Acknowledge not detected: ";
                            }

                            if ((pstat & LnConstants.PSTAT_WRITE_FAIL) != 0) {
                                operation += "Failed, No Write Acknowledge from decoder: ";
                            }

                            if ((pstat & LnConstants.PSTAT_NO_DECODER) != 0) {
                                operation += "Failed, Service Mode programming track empty: ";
                            }
                            if ((pstat & 0xF0) != 0) {
                                operation += "Unable to decode response = 0x"
                                             + Integer.toHexString(pstat) + ": ";
                            }
                        } else {
                            operation += "Was Successful, set ";
                        }
                    } else {
                        operation += "variable ";
                    }
                    /* printout based on whether we're doing Ops mode or not */
                    if (opsMode) {
                        logString = operation + " CV" + cvNumber + " of Loco " + mixedAdrStr
                                    + " value " + cvData + " (0x" + Integer.toHexString(cvData)
                                    + ", " + Integer.toBinaryString(cvData) + ").\n";
                    } else {
                        logString = operation + " CV" + cvNumber + " value " + cvData + " (0x"
                                    + Integer.toHexString(cvData) + ", "
                                    + Integer.toBinaryString(cvData) + ").\n";
                    }
                }
                // end programming track block

            } else if (slot == LnConstants.CFG_SLOT) {
                /**************************************************
                 * Configuration slot, holding op switches
                 **************************************************/
                logString = mode
                            + " Comand Station OpSw that are Closed (non-default):\n"
                            + ((l.getElement(3) & 0x80) > 0 ? "\tOpSw1=c, reserved.\n" : "")
                            + ((l.getElement(3) & 0x40) > 0 ? "\tOpSw2=c, DCS100 booster only.\n" : "")
                            + ((l.getElement(3) & 0x20) > 0 ? "\tOpSw3=c, Booster Autoreversing.\n" : "")
                            + ((l.getElement(3) & 0x10) > 0 ? "\tOpSw4=c, reserved.\n" : "")
                            + ((l.getElement(3) & 0x08) > 0 ? "\tOpSw5=c, Master Mode.\n" : "")
                            + ((l.getElement(3) & 0x04) > 0 ? "\tOpSw6=c, reserved.\n" : "")
                            + ((l.getElement(3) & 0x02) > 0 ? "\tOpSw7=c, reserved.\n" : "")
                            + ((l.getElement(3) & 0x01) > 0 ? "\tOpSw8=c, reserved.\n" : "")
                            + ((l.getElement(4) & 0x80) > 0 ? "\tOpSw9=c, Allow Motorola trinary echo 1-256.\n" : "")
                            + ((l.getElement(4) & 0x40) > 0 ? "\tOpSw10=c, Expand trinary switch echo.\n" : "")
                            + ((l.getElement(4) & 0x20) > 0 ? "\tOpSw11=c, Make certian trinary switches long duration.\n" : "")
                            + ((l.getElement(4) & 0x10) > 0 ? "\tOpSw12=c, Trinary addresses 1-80 allowed.\n" : "")
                            + ((l.getElement(4) & 0x08) > 0 ? "\tOpSw13=c, Raise loco address purge time to 600 seconds.\n" : "")
                            + ((l.getElement(4) & 0x04) > 0 ? "\tOpSw14=c, Disable loco address purging.\n" : "")
                            + ((l.getElement(4) & 0x02) > 0 ? "\tOpSw15=c, Purge will force loco to zero speed.\n" : "")
                            + ((l.getElement(4) & 0x01) > 0 ? "\tOpSw16=c, reserved.\n" : "")
                            + ((l.getElement(5) & 0x80) > 0 ? "\tOpSw17=c, Automatic advanced consists are disabled.\n" : "")
                            + ((l.getElement(5) & 0x40) > 0 ? "\tOpSw18=c, Extend booster short shutdown to 1/2 second.\n" : "")
                            + ((l.getElement(5) & 0x20) > 0 ? "\tOpSw19=c, reserved.\n" : "")
                            + ((l.getElement(5) & 0x10) > 0 ? "\tOpSw20=c, Disable address 00 analog operation.\n" : "")
                            + ((l.getElement(5) & 0x08) > 0 ? "\tOpSw21=c, Global default for new loco is FX.\n" : "")
                            + ((l.getElement(5) & 0x04) > 0 ? "\tOpSw22=c, Global default for new loco is 28 step.\n" : "")
                            + ((l.getElement(5) & 0x02) > 0 ? "\tOpSw23=c, Global default for new loco is 14 step.\n" : "")
                            + ((l.getElement(5) & 0x01) > 0 ? "\tOpSw24=c, reserved.\n" : "")
                            + ((l.getElement(6) & 0x80) > 0 ? "\tOpSw25=c, Disable aliasing.\n" : "")
                            + ((l.getElement(6) & 0x40) > 0 ? "\tOpSw26=c, Enable routes.\n" : "")
                            + ((l.getElement(6) & 0x20) > 0 ? "\tOpSw27=c, Disable normal switch commands (Bushby bit).\n" : "")
                            + ((l.getElement(6) & 0x10) > 0 ? "\tOpSw28=c, Disable DS54/64/SE8C interrogate at power on.\n" : "")
                            + ((l.getElement(6) & 0x08) > 0 ? "\tOpSw29=c, reserved.\n" : "")
                            + ((l.getElement(6) & 0x04) > 0 ? "\tOpSw30=c, reserved.\n" : "")
                            + ((l.getElement(6) & 0x02) > 0 ? "\tOpSw31=c, Meter route/switch output when not in trinary.\n" : "")
                            + ((l.getElement(6) & 0x01) > 0 ? "\tOpSw32=c, reserved.\n" : "")
                            // 7 skipped intentionally
                            + ((l.getElement(8) & 0x80) > 0 ? "\tOpSw33=c, Restore track power to previous state at power on.\n" : "")
                            + ((l.getElement(8) & 0x40) > 0 ? "\tOpSw34=c, Allow track to power up to run state.\n" : "")
                            + ((l.getElement(8) & 0x20) > 0 ? "\tOpSw35=c, reserved.\n" : "")
                            + ((l.getElement(8) & 0x10) > 0 ? "\tOpSw36=c, Clear all moble decoder information and consists.\n" : "")
                            + ((l.getElement(8) & 0x08) > 0 ? "\tOpSw37=c, Clear all routes.\n" : "")
                            + ((l.getElement(8) & 0x04) > 0 ? "\tOpSw38=c, Clear loco roster.\n" : "")
                            + ((l.getElement(8) & 0x02) > 0 ? "\tOpSw39=c, Clear internal memory.\n" : "")
                            + ((l.getElement(8) & 0x01) > 0 ? "\tOpSw40=c, reserved.\n" : "")
                            + ((l.getElement(9) & 0x80) > 0 ? "\tOpSw41=c, Diagnostic click when LocoNet command is received.\n" : "")
                            + ((l.getElement(9) & 0x40) > 0 ? "\tOpSw42=c, Disable 3 beeps when loco address is purged.\n" : "")
                            + ((l.getElement(9) & 0x20) > 0 ? "\tOpSw43=c, Disable LocoNet update of track status.\n" : "")
                            + ((l.getElement(9) & 0x10) > 0 ? "\tOpSw44=c, Expand slots to 120.\n" : "")
                            + ((l.getElement(9) & 0x08) > 0 ? "\tOpSw45=c, Disable replay for switch state request.\n" : "")
                            + ((l.getElement(9) & 0x04) > 0 ? "\tOpSw46=c, reserved.\n" : "")
                            + ((l.getElement(9) & 0x02) > 0 ? "\tOpSw47=c, Programming track is break generator.\n" : "")
                            + ((l.getElement(9) & 0x01) > 0 ? "\tOpSw48=c, reserved.\n" : "")
                            + ((l.getElement(10) & 0x80) > 0 ? "\tOpSw49=c, reserved.\n" : "")
                            + ((l.getElement(10) & 0x40) > 0 ? "\tOpSw50=c, reserved.\n" : "")
                            + ((l.getElement(10) & 0x20) > 0 ? "\tOpSw51=c, reserved.\n" : "")
                            + ((l.getElement(10) & 0x10) > 0 ? "\tOpSw52=c, reserved.\n" : "")
                            + ((l.getElement(10) & 0x08) > 0 ? "\tOpSw53=c, reserved.\n" : "")
                            + ((l.getElement(10) & 0x04) > 0 ? "\tOpSw54=c, reserved.\n" : "")
                            + ((l.getElement(10) & 0x02) > 0 ? "\tOpSw55=c, reserved.\n" : "")
                            + ((l.getElement(10) & 0x01) > 0 ? "\tOpSw56=c, reserved.\n" : "")
                            + ((l.getElement(11) & 0x80) > 0 ? "\tOpSw57=c, reserved.\n" : "")
                            + ((l.getElement(11) & 0x40) > 0 ? "\tOpSw58=c, reserved.\n" : "")
                            + ((l.getElement(11) & 0x20) > 0 ? "\tOpSw59=c, reserved.\n" : "")
                            + ((l.getElement(11) & 0x10) > 0 ? "\tOpSw60=c, reserved.\n" : "")
                            + ((l.getElement(11) & 0x08) > 0 ? "\tOpSw61=c, reserved.\n" : "")
                            + ((l.getElement(11) & 0x04) > 0 ? "\tOpSw62=c, reserved.\n" : "")
                            + ((l.getElement(11) & 0x02) > 0 ? "\tOpSw63=c, reserved.\n" : "")
                            + ((l.getElement(11) & 0x01) > 0 ? "\tOpSw64=c, reserved.\n" : "");
            } else {
                /**************************************************
                 * normal slot read/write message - see info above *
                 **************************************************/

                if ((trackStatus != trk) || showTrackStatus) {
                    trackStatus = trk;
                    showStatus = true;
                }

                if (showStatus) {
                    logString = mode + " slot " + slot + " information:\n\tLoco " + locoAdrStr
                                + " is " + LnConstants.CONSIST_STAT(stat) + ", "
                                + LnConstants.LOCO_STAT(stat) + ", operating in "
                                + LnConstants.DEC_MODE(stat) + " SS mode, and is going "
                                + ((dirf & LnConstants.DIRF_DIR) != 0 ? "in Reverse" : "Foward")
                                + " at speed " + spd + ",\n" + "\tF0="
                                + ((dirf & LnConstants.DIRF_F0) != 0 ? "On, " : "Off,") 
                                + " F1="
                                + ((dirf & LnConstants.DIRF_F1) != 0 ? "On, " : "Off,") 
                                + " F2="
                                + ((dirf & LnConstants.DIRF_F2) != 0 ? "On, " : "Off,") 
                                + " F3="
                                + ((dirf & LnConstants.DIRF_F3) != 0 ? "On, " : "Off,") 
                                + " F4="
                                + ((dirf & LnConstants.DIRF_F4) != 0 ? "On, " : "Off,")
                                + " Sound1/F5="
                                + ((snd & LnConstants.SND_F5) != 0 ? "On, " : "Off,")
                                + " Sound2/F6="
                                + ((snd & LnConstants.SND_F6) != 0 ? "On, " : "Off,")
                                + " Sound3/F7="
                                + ((snd & LnConstants.SND_F7) != 0 ? "On, " : "Off,")
                                + " Sound4/F8=" + ((snd & LnConstants.SND_F8) != 0 ? "On" : "Off")
                                + "\n\tMaster: "
                                + ((trk & LnConstants.GTRK_MLOK1) != 0 ? "LocoNet 1.1" : "DT-200")
                                + "; Track: " 
                                + ((trk & LnConstants.GTRK_IDLE) != 0 ? "On" : "Off")
                                + "; Programming Track: "
                                + ((trk & LnConstants.GTRK_PROG_BUSY) != 0 ? "Busy" : "Available")
                                + "; SS2=0x" + Integer.toHexString(ss2) 
                                + ", ThrottleID=" + idString(id1, id2) + "\n";
                } else {
                    logString = mode + " slot " + slot + " information:\n\tLoco " + locoAdrStr
                                + " is " + LnConstants.CONSIST_STAT(stat) + ", "
                                + LnConstants.LOCO_STAT(stat) + ", operating in "
                                + LnConstants.DEC_MODE(stat) + " SS mode, and is going "
                                + ((dirf & LnConstants.DIRF_DIR) != 0 ? "in Reverse" : "Foward")
                                + " at speed " + spd + ",\n" + "\tF0="
                                + ((dirf & LnConstants.DIRF_F0) != 0 ? "On, " : "Off,") 
                                + " F1="
                                + ((dirf & LnConstants.DIRF_F1) != 0 ? "On, " : "Off,") 
                                + " F2="
                                + ((dirf & LnConstants.DIRF_F2) != 0 ? "On, " : "Off,") 
                                + " F3="
                                + ((dirf & LnConstants.DIRF_F3) != 0 ? "On, " : "Off,") 
                                + " F4="
                                + ((dirf & LnConstants.DIRF_F4) != 0 ? "On, " : "Off,")
                                + " Sound1/F5="
                                + ((snd & LnConstants.SND_F5) != 0 ? "On, " : "Off,")
                                + " Sound2/F6="
                                + ((snd & LnConstants.SND_F6) != 0 ? "On, " : "Off,")
                                + " Sound3/F7="
                                + ((snd & LnConstants.SND_F7) != 0 ? "On, " : "Off,")
                                + " Sound4/F8=" + ((snd & LnConstants.SND_F8) != 0 ? "On" : "Off")
                                + "\n\tSS2=0x" + Integer.toHexString(ss2) 
                                + ", ThrottleID =" + idString(id1, id2) + "\n";
                }
                // end normal slot read/write case
            }
            return logString;
        } // case LnConstants.OPC_SL_RD_DATA

        case LnConstants.OPC_ALM_WRITE:
        case LnConstants.OPC_ALM_READ: {
            String message;
            if (l.getElement(0) == LnConstants.OPC_ALM_WRITE) {
                message = "Write ALM msg ";
            } else {
                message = "Read ALM msg (Write reply) ";
            }

            if (l.getElement(1) == 0x10) {
                // ALM read and write messages
                message = message + l.getElement(2) + " ATASK=" + l.getElement(3);
                if (l.getElement(3) == 2) {
                    message = message + " (RD)";
                } else if (l.getElement(3) == 3) {
                    message = message + " (WR)";
                } else if (l.getElement(3) == 0) {
                    message = message + " (ID)";
                }
                return message + " BLKL=" + l.getElement(4) 
                               + " BLKH=" + l.getElement(5)
                               + " LOGIC=" + l.getElement(6) 
                               + "\n      "
                               + " ARG1L=0x" + Integer.toHexString(l.getElement(7))
                               + " ARG1H=0x" + Integer.toHexString(l.getElement(8))
                               + " ARG2L=0x" + Integer.toHexString(l.getElement(9))
                               + " ARG2H=0x" + Integer.toHexString(l.getElement(10)) 
                               + "\n      "
                               + " ARG3L=0x" + Integer.toHexString(l.getElement(11))
                               + " ARG3H=0x" + Integer.toHexString(l.getElement(12))
                               + " ARG4L=0x" + Integer.toHexString(l.getElement(13))
                               + " ARG4H=0x" + Integer.toHexString(l.getElement(14)) 
                               + "\n";
            } else if (l.getElement(1) == 0x15) {
                // write extended master message
                if (l.getElement(0) == 0xEE) {
                    message = "Write extended slot: ";
                } else {
                    message = "Read extended slot (Write reply): ";
                }
                return message + "slot " + l.getElement(3)
                               + " stat " + l.getElement(4)
                               + " addr " + (l.getElement(6) * 128 + l.getElement(5))
                               + " speed " + l.getElement(8)
                               + ".\n";
            } else {
                return message + " with unexpected length " + l.getElement(1) + ".\n";
            }
        } // case LnConstants.OPC_ALM_READ

            /*
             * OPC_PEER_XFER   0xE5    ; move 8 bytes PEER to PEER, SRC->DST   NO resp
             *                         ; <0xE5>,<10>,<SRC>,<DSTL><DSTH>,<PXCT1>,<D1>,<D2>,<D3>,<D4>, 
             *                         ; <PXCT2>,<D5>,<D6>,<D7>,<D8>,<CHK>
             *                         ; SRC/DST are 7 bit args. DSTL/H=0 is BROADCAST msg
             *                         ;     SRC=0 is MASTER
             *                         ;     SRC=0x70-0x7E are reserved
             *
             * Page 10 of LocoNet Personal Edition v1.0.
             * 
             * Duplex group management reverse engineered by Leo Bicknell, with input from
             * B. Milhaupt.
             */
        case LnConstants.OPC_PEER_XFER: {
            // The first byte seems to determine the type of message.
            switch (l.getElement(1)) {
            case 0x10: {
                /*
                 * SRC=7F is THROTTLE msg xfer
                 *  ; <DSTL><DSTH> encode ID#,
                 *  ; <0><0> is THROT B'CAST
                 *  ; <PXCT1>=<0,XC2,XC1,XC0 - D4.7,D3.7,D2.7,D1.7>
                 *  ; XC0-XC2=ADR type CODE-0=7 bit Peer
                 * TO Peer adrs * 
                 *  ; 1=<D1>is SRC HI,<D2>is DST HI
                 *  ; <PXCT2>=<0,XC5,XC4,XC3 - D8.7,D7.7,D6.7,D5.7>
                 *  ; XC3-XC5=data type CODE- 0=ANSI TEXT string,
                 *  ; balance RESERVED *
                 * ****************************************************
                 * SV programming format 1
                 * 
                 * This is the message format as implemented by the certain
                 * existing devices. New designs should not use this format. The
                 * message bytes are assigned as follows:
                 *   ; <0xE5> <0x10> <SRC> <DST> <0x01> <PXCT1> 
                 *   ; <D1> <D2> <D3> <D4> <PXCT2> 
                 *   ; <D5> <D6> <D7> <D8> <CHK> 
                 *   
                 * The upper nibble of PXCT1 must be 0,
                 * and the upper nibble of PXCT2 must be 1. The meanings of the
                 * remaining bytes are as defined in the LocoNet Personal
                 * Edition specification.
                 * *********************************************
                 * SV programming format 2
                 * 
                 * This is the recommended format for new designs.
                 * The message bytes as assigned as follows: * 
                 *  ; <0xE5> <0x10> <SRC> <SV_CMD> <SV_TYPE> <SVX1>
                 *  ; <DST_L> <DST_H> <SV_ADRL> <SV_ADRH> <SVX2>
                 *  ; <D1> <D2> <D3> <D4> <CHK>
                 * 
                 * The upper nibble of both SVX1 (PXCT1) and SVX2 (PXCT2) must be 1.
                 */

                int src = l.getElement(2); // source of transfer
                int dst_l = l.getElement(3); // ls 7 bits of destination
                int dst_h = l.getElement(4); // ms 7 bits of destination
                int pxct1 = l.getElement(5);
                int pxct2 = l.getElement(10);

                int d[] = l.getPeerXfrData();

                String generic = "Peer to Peer transfer: SRC=0x" 
                                 + Integer.toHexString(src)
                                 + ", DSTL=0x" 
                                 + Integer.toHexString(dst_l) 
                                 + ", DSTH=0x"
                                 + Integer.toHexString(dst_h) 
                                 + ", PXCT1=0x"
                                 + Integer.toHexString(pxct1) 
                                 + ", PXCT2=0x"
                                 + Integer.toHexString(pxct2);
                String data = "Data [0x" + Integer.toHexString(d[0]) 
                              + " 0x" + Integer.toHexString(d[1]) 
                              + " 0x" + Integer.toHexString(d[2])
                              + " 0x" + Integer.toHexString(d[3]) 
                              + ",0x" + Integer.toHexString(d[4]) 
                              + " 0x" + Integer.toHexString(d[5])
                              + " 0x" + Integer.toHexString(d[6]) 
                              + " 0x" + Integer.toHexString(d[7]) + "]\n";

                if ((src == 0x7F) && (dst_l == 0x7F) && (dst_h == 0x7F)
                    && ((pxct1 & 0x70) == 0x40)) {
                    // Download (firmware?) messages.
                    int sub = pxct2 & 0x70;
                    switch (sub) {
                    case 0x00: // setup
                        return "Download message, setup.\n";
                    case 0x10: // set address
                        return "Download message, set address "
                               + StringUtil.twoHexFromInt(d[0])
                               + StringUtil.twoHexFromInt(d[1])
                               + StringUtil.twoHexFromInt(d[2]) + ".\n";
                    case 0x20: // send data
                        return "Download message, send data "
                               + StringUtil.twoHexFromInt(d[0]) + " "
                               + StringUtil.twoHexFromInt(d[1]) + " "
                               + StringUtil.twoHexFromInt(d[2]) + " "
                               + StringUtil.twoHexFromInt(d[3]) + " "
                               + StringUtil.twoHexFromInt(d[4]) + " "
                               + StringUtil.twoHexFromInt(d[5]) + " "
                               + StringUtil.twoHexFromInt(d[6]) + " "
                               + StringUtil.twoHexFromInt(d[7]) + ".\n";
                    case 0x30: // verify
                        return "Download message, verify.\n";
                    case 0x40: // end op
                        return "Download message, end operation.\n";
                    default: // everything else isn't understood, go to default
                    }
                }
                if (src == 0x50) {
                    // Packets from the LocoBuffer
                    String dst_subaddrx = (dst_h != 0x01 ? "" : ((d[4] != 0) ? "/" + Integer.toHexString(d[4]) : ""));
                    if (dst_h == 0x01 && ((pxct1 & 0xF0) == 0x00)
                        && ((pxct2 & 0xF0) == 0x10)) {
                        // LocoBuffer to LocoIO
                        return "LocoBuffer => LocoIO@"
                               + ((dst_l == 0) ? "broadcast" : Integer.toHexString(dst_l) + dst_subaddrx)
                               + " "
                               + (d[0] == 2 ? "Read SV" + d[1] : "Write SV" + d[1] + "=0x" + Integer.toHexString(d[3]))
                               + ((d[2] != 0) ? " Firmware rev " + dotme(d[2]) : "") + ".\n";
                    }
                }
                if (dst_h == 0x01 && ((pxct1 & 0xF0) == 0x00)
                    && ((pxct2 & 0xF0) == 0x00)) {
                    // (Jabour/Deloof LocoIO), SV Programming messages format 1
                    String src_subaddrx = ((d[4] != 0) ? "/" + Integer.toHexString(d[4]) : "");
                    String dst_subaddrx = (dst_h != 0x01 ? "" : ((d[4] != 0) ? "/" + Integer.toHexString(d[4]) : ""));
                    
                    String src_dev = ((src == 0x50) ? "Locobuffer" : "LocoIO@" + "0x" + Integer.toHexString(src) + src_subaddrx);
                    String dst_dev = (((dst_h == 0x01) && (dst_l == 0x50)) ? "LocoBuffer "
                            		 : (((dst_h == 0x01) && (dst_l == 0x0)) ? "broadcast"
                                       : "LocoIO@0x" + Integer.toHexString(dst_l) + dst_subaddrx));

                    return src_dev + "=> " + dst_dev + " "
                           + ((dst_h == 0x01) ? ((d[0] == 2 ? "Read" : "Write") + " SV" + d[1]) : "")
                           + ((src == 0x50) ? (d[0] != 2 ? ("=0x" + Integer.toHexString(d[3])) : "")
                             : " = " + ((d[0] == 2) ? ((d[2] != 0) ? (d[5] < 10) ? "" + d[5]
                                       : d[5] + " (0x" + Integer.toHexString(d[5]) + ")"
                                       : (d[7] < 10) ? "" + d[7] 
                                       : d[7] + " (0x" + Integer.toHexString(d[7]) + ")")
                                       : (d[7] < 10) ? "" + d[7] 
                                       : d[7] + " (0x" + Integer.toHexString(d[7]) + ")"))
                           + ((d[2] != 0) ? " Firmware rev " + dotme(d[2]) : "") + ".\n";
                }
                // check for a specific type - SV Programming messages format 2
                // (New Designs)
                if (((pxct1 & 0xF0) == 0x10) && ((pxct2 & 0xF0) == 0x10)) {
                    // New Designs, SV Programming messages format 2
                    // We don't know what to do with them yet.
                    return "SV Programming Protocol v2: " + generic + "\n\t"
                           + data;
                }

                // no specific type, return generic format
                return generic + "\n\t" + data;
            } // case 0x10
            
            case 0x0A: {
                // throttle status
                int tcntrl = l.getElement(2);
                String stat;
                if (tcntrl == 0x40)
                    stat = " (OK) ";
                else if (tcntrl == 0x7F)
                    stat = " (no key, immed, ignored) ";
                else if (tcntrl == 0x43)
                    stat = " (+ key during msg) ";
                else if (tcntrl == 0x42)
                    stat = " (- key during msg) ";
                else if (tcntrl == 0x41)
                    stat = " (R/S key during msg, aborts) ";
                else
                    stat = " (unknown) ";

                return "Throttle status TCNTRL=" + Integer.toHexString(tcntrl)
                       + stat + " ID1,ID2="
                       + Integer.toHexString(l.getElement(3))
                       + Integer.toHexString(l.getElement(4)) + " SLA="
                       + Integer.toHexString(l.getElement(7)) + " SLB="
                       + Integer.toHexString(l.getElement(8)) + ".\n";
            }
            
            case 0x14: {
                // Duplex Radio Management

                switch (l.getElement(2)) {
                case 0x01: {
                	// Seems to be a query for just duplex devices.
                	switch (l.getElement(3)) {
                	case 0x08: {
                		return "Query Duplex Panels.\n";
                	}
                	case 0x10: {
                		return "Duplex Panel Response.\n";
                	}
                	default: {
                        forceHex = true;
                        return "Unknown Duplex Channel message.\n";
                    }
                	} // end of switch (l.getElement(3))
                }
                case 0x02: {
                    // Request Duplex Radio Channel
                    switch (l.getElement(3)) {
                    case 0x00: {
                        // The MSB is stuffed elsewhere again...
                        int channel = l.getElement(5) | ((l.getElement(4) & 0x01) << 7);

                    	return "Set Duplex Channel to " + Integer.toString(channel) + ".\n";
                    }
                    case 0x08: {
                        return "Query Duplex Channel.\n";
                    }
                    case 0x10: {
                        // The MSB is stuffed elsewhere again...
                        int channel = l.getElement(5) | ((l.getElement(4) & 0x01) << 7);

                        return "Duplex Channel is " + Integer.toString(channel) + ".\n";
                    }
                    default: {
                        forceHex = true;
                        return "Unknown Duplex Channel message.\n";
                    }
                    } // end of switch (l.getElement(3))
                }

                case 0x03: {
                    // Duplex Group Name
                	// Characters appear to be 8 bit values, but transmitted over a 7 bit
                	// encoding, so high order bits are stashed in element 4 and 9.
                	char[] groupNameArray = { (char) (l.getElement(5)  | ((l.getElement(4) & 0x01) << 7)),
                							  (char) (l.getElement(6)  | ((l.getElement(4) & 0x02) << 6)),
                							  (char) (l.getElement(7)  | ((l.getElement(4) & 0x04) << 5)),
                							  (char) (l.getElement(8)  | ((l.getElement(4) & 0x08) << 4)),
                							  (char) (l.getElement(10) | ((l.getElement(9) & 0x01) << 7)),
                							  (char) (l.getElement(11) | ((l.getElement(9) & 0x02) << 6)),
                							  (char) (l.getElement(12) | ((l.getElement(9) & 0x04) << 5)),
                							  (char) (l.getElement(13) | ((l.getElement(9) & 0x08) << 4)) };
                	String groupName = new String(groupNameArray);
                	
                	// The pass code is stuffed in here, each digit in 4 bits.  But again, it's a
                	// 7 bit encoding, so the MSB of the "upper" half is stuffed into byte 14.
                	int p1 = ((l.getElement(14) & 0x01) << 3) | ((l.getElement(15) & 0x70) >> 4);
                    int p2 = l.getElement(15) & 0x0F;
                	int p3 = ((l.getElement(14) & 0x02) << 2) | ((l.getElement(16) & 0x70) >> 4);
                    int p4 = l.getElement(16) & 0x0F;
                    
                    // It's not clear you can set A-F from throttles or Digitrax's tools, but
                    // they do take and get returned if you send them on the wire...
                    String passcode = Integer.toHexString(p1) + Integer.toHexString(p2)
                    				+ Integer.toHexString(p3) + Integer.toHexString(p4);

                    // The MSB is stuffed elsewhere again...
                    int channel = l.getElement(17) | ((l.getElement(14) & 0x04) << 5);

                    // The MSB is stuffed elsewhere one last time.
                    int id = l.getElement(18) | ((l.getElement(14) & 0x08) << 4);

                    switch (l.getElement(3)) {
                    case 0x00: {
                    	return "Set Duplex Group Name to '" + groupName + ".\n";
                    }
                    case 0x08: {
                        return "Query Duplex Group Information.\n";
                    }
                    case 0x10: {
                         return "Duplex Group Name is '" + groupName 
                         	+ "', Password " + passcode
                         	+ ", Channel " + Integer.toString(channel)
                         	+ ", ID " + Integer.toString(id)
                         	+ ".\n";
                    }
                    default: {
                        forceHex = true;
                        return "Unknown Duplex Group Name message.\n";
                    }
                    } // end of switch (l.getElement(3))
                }
                case 0x04: {
                	// Duplex Group ID
                	
                    // The MSB is stuffed elsewhere again...
                    int id = l.getElement(5) | ((l.getElement(4) & 0x01) << 7);

                	switch (l.getElement(3)) {
                    case 0x00: {
                    	return "Set Duplex Group ID to '" + Integer.toString(id) + ".\n";
                    }
                    case 0x08: {
                        return "Query Duplex Group ID.\n";
                    }
                    case 0x10: {
                         return "Duplex Group ID is " + Integer.toString(id) + ".\n";
                    }
                    default: {
                        forceHex = true;
                        return "Unknown Duplex Group ID message.\n";
                    }
                    } // end of switch (l.getElement(3))
                }
                case 0x07: {
                    // Duplex Group Password
                    char[] groupPasswordArray = { (char) l.getElement(5),
                    							  (char) l.getElement(6),
                    							  (char) l.getElement(7),
                    							  (char) l.getElement(8)};
                    String groupPassword = new String(groupPasswordArray);

                    switch (l.getElement(3)) {
                    case 0x00: {
                        return "Set Duplex Group Password is '" + groupPassword + "'.\n";
                    }
                    case 0x08: {
                        return "Query Duplex Group Password.\n";
                    }
                    case 0x10: {
                        return "Duplex Group Password is '" + groupPassword + "'.\n";
                    }
                    default: {
                        forceHex = true;
                        return "Unknown Duplex Group Password message.\n";
                    }
                    } // end of switch (l.getElement(3))
                }
                case 0x10: {
                	// Radio Channel Noise/Activity
                    switch (l.getElement(3)) {
                    case 0x08: {
                        return "Query Duplex Channel " + Integer.toString(l.getElement(5)) + " noise/activity report.\n";
                    }
                    case 0x10: {
                    	// High order bit stashed in another element again.
                    	int level = (l.getElement(6) & 0x7F) | ((l.getElement(4) & 0x02) << 6);
                    	
                        return "Duplex Channel " + Integer.toString(l.getElement(5)) + " reports noise/activity level "
                               + Integer.toString(level) + "/255.\n";
                    }
                    default: {
                        forceHex = true;
                        return "Unknown Duplex Channel Activity message.\n";
                    }
                    } // end of switch (l.getElement(3))

                }
                default: {
                    // 0xE5 message of unknown format
                    forceHex = true;
                    return "Message with opcode 0xE5 and unknown format.";

                }
                } // switch (l.getElement(2))
            }
            default: {
                // 0xE5 message of unknown format
                forceHex = true;
                return "Message with opcode 0xE5 and unknown format.";

            }
            } 
        } // case LnConstants.OPC_PEER_XFER

            /*
             * OPC_LISSY_UPDATE   0xE5
             *
             * LISSY is an automatic train detection system made by Uhlenbrock.
             * All documentation appears to be in German.
             * 
             */
        case LnConstants.OPC_LISSY_UPDATE: {
            switch (l.getElement(1)) {
            case 0x08: // Format LISSY message
                int unit = (l.getElement(4) & 0x7F);
                int address = (l.getElement(6) & 0x7F) + 128 * (l.getElement(5) & 0x7F);
                return "Lissy " + unit + ": Loco " + address + " moving "
                       + ((l.getElement(3) & 0x20) == 0 ? "north" : "south") + ".\n";

            case 0x0A: // Format special message
                int element = l.getElement(2) * 128 + l.getElement(3);
                int stat1 = l.getElement(5);
                int stat2 = l.getElement(6);
                String status;
                if ((stat1 & 0x10) != 0)
                    if ((stat1 & 0x20) != 0)
                        status = " AX, XA reserved; ";
                    else
                        status = " AX reserved; ";
                else if ((stat1 & 0x20) != 0)
                    status = " XA reserved; ";
                else
                    status = " no reservation; ";
                if ((stat2 & 0x01) != 0)
                    status += "Turnout thrown; ";
                else
                    status += "Turnout closed; ";
                if ((stat1 & 0x01) != 0)
                    status += "Occupied";
                else
                    status += "Not occupied";
                return "SE" + (element + 1) + " (" + element + ") reports AX:" + l.getElement(7)
                       + " XA:" + l.getElement(8) + status + "\n";

            default:
                forceHex = true;
                return "Unrecognized OPC_LISSY_UPDATE command varient.\n";
            }
        } // case LnConstants.OPC_LISSY_UPDATE
        
            /*
             * OPC_IMM_PACKET   0xED   ;SEND n-byte packet immediate LACK
             *                         ; Follow on message: LACK
             *                         ; <0xED>,<0B>,<7F>,<REPS>,<DHI>,<IM1>,<IM2>,
             *                         ;        <IM3>,<IM4>,<IM5>,<CHK>
             *                         ;   <DHI>=<0,0,1,IM5.7-IM4.7,IM3.7,IM2.7,IM1.7>
             *                         ;   <REPS>  D4,5,6=#IM bytes,
             *                         ;           D3=0(reserved);
             *                         ;           D2,1,0=repeat CNT
             *                         ; IF Not limited MASTER then
             *                         ;   LACK=<B4>,<7D>,<7F>,<chk> if CMD ok
             *                         ; IF limited MASTER then Lim Masters respond
             *                         ;   with <B4>,<7E>,<lim adr>,<chk>
             *                         ; IF internal buffer BUSY/full respond
             *                         ;   with <B4>,<7D>,<0>,<chk>
             *                         ;   (NOT IMPLEMENTED IN DT200)
			 *
			 * This sends a raw NMRA packet across the LocoNet.
			 * 
			 * Page 11 of LocoNet Personal Edition v1.0.
			 * 
			 * Decodes for the F9-F28 functions taken from the NMRA standards and
			 * coded by Leo Bicknell.
             */
        case LnConstants.OPC_IMM_PACKET: {
            // sendPkt = (sendPktMsg *) msgBuf;
            int val7f = l.getElement(2); /* fixed value of 0x7f */
            int reps = l.getElement(3); /* repeat count */
            int dhi = l.getElement(4); /* high bits of data bytes */
            int im1 = l.getElement(5);
            int im2 = l.getElement(6);
            int im3 = l.getElement(7);
            int im4 = l.getElement(8);
            int im5 = l.getElement(9);

            // see if it really is a 'Send Packet' as defined in Loconet PE
            if (val7f == 0x7f) {
                int len = ((reps & 0x70) >> 4);
                byte[] packet = new byte[len];
                packet[0] = (byte) (im1 + ((dhi & 0x01) != 0 ? 0x80 : 0));
                if (len >= 2)
                    packet[1] = (byte) (im2 + ((dhi & 0x02) != 0 ? 0x80 : 0));
                if (len >= 3)
                    packet[2] = (byte) (im3 + ((dhi & 0x04) != 0 ? 0x80 : 0));
                if (len >= 4)
                    packet[3] = (byte) (im4 + ((dhi & 0x08) != 0 ? 0x80 : 0));
                if (len >= 5)
                    packet[4] = (byte) (im5 + ((dhi & 0x10) != 0 ? 0x80 : 0));

                int address;

                /*
                 * We use this two places below, so we generate it once here.
                 * That seems wrong, but what we really need is to be able to
                 * decode any NMRA packet here, which is a lot more work!
                 */
                String generic = "Send packet immediate: "
                                 + ((reps & 0x70) >> 4)
                                 + " bytes, repeat count " + (reps & 0x07)
                                 + "(" + reps + ")" + "\n\tDHI=0x"
                                 + Integer.toHexString(dhi) + ", IM1=0x"
                                 + Integer.toHexString(im1) + ", IM2=0x"
                                 + Integer.toHexString(im2) + ", IM3=0x"
                                 + Integer.toHexString(im3) + ", IM4=0x"
                                 + Integer.toHexString(im4) + ", IM5=0x"
                                 + Integer.toHexString(im5) + "\n\tpacket: ";

                // F9-F28 w/a long address.
                if ((packet[0] & 0xC0) == 0xC0) {
                    address = ((packet[0] & 0x3F) << 8) + packet[1];

                    if ((packet[2] & 0xFF) == 0xDF) {
                        // Functions 21-28
                        return "Send packet immediate: Locomotive " + address
                               + " set" + " F21="
                               + ((packet[3] & 0x01) > 0 ? "On" : "Off")
                               + ", F22="
                               + ((packet[3] & 0x02) > 0 ? "On" : "Off")
                               + ", F23="
                               + ((packet[3] & 0x04) > 0 ? "On" : "Off")
                               + ", F24="
                               + ((packet[3] & 0x08) > 0 ? "On" : "Off")
                               + ", F25="
                               + ((packet[3] & 0x10) > 0 ? "On" : "Off")
                               + ", F26="
                               + ((packet[3] & 0x20) > 0 ? "On" : "Off")
                               + ", F27="
                               + ((packet[3] & 0x40) > 0 ? "On" : "Off")
                               + ", F28="
                               + ((packet[3] & 0x80) > 0 ? "On" : "Off") + "\n";
                    } else if ((packet[2] & 0xFF) == 0xDE) {
                        // Functions 13-20
                        return "Send packet immediate: Locomotive " + address
                               + " set" + " F13="
                               + ((packet[3] & 0x01) > 0 ? "On" : "Off")
                               + ", F14="
                               + ((packet[3] & 0x02) > 0 ? "On" : "Off")
                               + ", F15="
                               + ((packet[3] & 0x04) > 0 ? "On" : "Off")
                               + ", F16="
                               + ((packet[3] & 0x08) > 0 ? "On" : "Off")
                               + ", F17="
                               + ((packet[3] & 0x10) > 0 ? "On" : "Off")
                               + ", F18="
                               + ((packet[3] & 0x20) > 0 ? "On" : "Off")
                               + ", F19="
                               + ((packet[3] & 0x40) > 0 ? "On" : "Off")
                               + ", F20="
                               + ((packet[3] & 0x80) > 0 ? "On" : "Off") + "\n";
                    } else if ((packet[2] & 0xF0) == 0xA0) {
                        // Functions 8-12
                        return "Send packet immediate: Locomotive " + address
                               + " set" + ", F09="
                               + ((packet[2] & 0x01) > 0 ? "On" : "Off")
                               + ", F10="
                               + ((packet[2] & 0x02) > 0 ? "On" : "Off")
                               + ", F11="
                               + ((packet[2] & 0x04) > 0 ? "On" : "Off")
                               + ", F12="
                               + ((packet[2] & 0x08) > 0 ? "On" : "Off") + "\n";
                    } else {
                        // Unknown
                        return generic + jmri.NmraPacket.format(packet) + "\n";
                    }
                } else { // F9-F28 w/a short address.
                    address = packet[0];
                    if ((packet[1] & 0xFF) == 0xDF) {
                        // Functions 21-28
                        return "Send packet immediate: Locomotive " + address
                               + " set" + " F21="
                               + ((packet[2] & 0x01) > 0 ? "On" : "Off")
                               + ", F22="
                               + ((packet[2] & 0x02) > 0 ? "On" : "Off")
                               + ", F23="
                               + ((packet[2] & 0x04) > 0 ? "On" : "Off")
                               + ", F24="
                               + ((packet[2] & 0x08) > 0 ? "On" : "Off")
                               + ", F25="
                               + ((packet[2] & 0x10) > 0 ? "On" : "Off")
                               + ", F26="
                               + ((packet[2] & 0x20) > 0 ? "On" : "Off")
                               + ", F27="
                               + ((packet[2] & 0x40) > 0 ? "On" : "Off")
                               + ", F28="
                               + ((packet[2] & 0x80) > 0 ? "On" : "Off") + "\n";
                    } else if ((packet[1] & 0xFF) == 0xDE) {
                        // Functions 13-20
                        return "Send packet immediate: Locomotive " + address
                               + " set" + " F13="
                               + ((packet[2] & 0x01) > 0 ? "On" : "Off")
                               + ", F14="
                               + ((packet[2] & 0x02) > 0 ? "On" : "Off")
                               + ", F15="
                               + ((packet[2] & 0x04) > 0 ? "On" : "Off")
                               + ", F16="
                               + ((packet[2] & 0x08) > 0 ? "On" : "Off")
                               + ", F17="
                               + ((packet[2] & 0x10) > 0 ? "On" : "Off")
                               + ", F18="
                               + ((packet[2] & 0x20) > 0 ? "On" : "Off")
                               + ", F19="
                               + ((packet[2] & 0x40) > 0 ? "On" : "Off")
                               + ", F20="
                               + ((packet[2] & 0x80) > 0 ? "On" : "Off") + "\n";
                    } else if ((packet[1] & 0xF0) == 0xA0) {
                        // Functions 8-12
                        return "Send packet immediate: Locomotive " + address
                               + " set" + " F09="
                               + ((packet[1] & 0x01) > 0 ? "On" : "Off")
                               + ", F10="
                               + ((packet[1] & 0x02) > 0 ? "On" : "Off")
                               + ", F11="
                               + ((packet[1] & 0x04) > 0 ? "On" : "Off")
                               + ", F12="
                               + ((packet[1] & 0x08) > 0 ? "On" : "Off") + "\n";
                    } else {
                        // Unknown
                        return generic + jmri.NmraPacket.format(packet) + "\n";
                    }
                }
            } else {
                /* Hmmmm... */
                forceHex = true;
                return "Undefined Send Packet Immediate, 3rd byte id 0x"
                       + Integer.toHexString(val7f) + " not 0x7f.\n";
            }
        } // case LnConstants.OPC_IMM_PACKET

        default:
            forceHex = true;
            return "Unable to parse command.\n";

        } // end switch over opcode type - default handles unrecognized cases,
          // so can't reach here
    } // end of protected String format(LocoNetMessage l)
    
    /**
     * This function creates a string representation of a LocoNet buffer. The
     * string may be more than one line, and is terminated with a newline.
     * 
     * @return The created string representation.
     */
    public String displayMessage(LocoNetMessage l) {

        forceHex = false;
        String s = format(l);
        if (forceHex)
            s += "contents: " + l.toString() + "\n";
        if (showOpCode)
            s = LnConstants.OPC_NAME(l.getOpCode()) + ": " + s;
        return s;
    } // end of public String displayMessage(LocoNetMessage l)
}  // end of public class Llnmon

/* @(#)Llnmon.java */
