package jmri.jmrix.loconet;

/**
 * Constants to represent values seen in LocoNet traffic.
 * <p>
 * Some, but not all, of the names have a convention where the
 * first "word" gives the type of the constant:
 *  <ul>
 *  <li>CONSIST - Consist type codes
 *  <li>DEC - Decoder type codes
 *  <li>OPC - LocoNet op code
 *  <li>PCMD - Programming command
 *  <li>RE - Re-engineered, not from the LocoNet documentations
 *  <li>STAT1 - bits in status byte 1
 *  <li>STAT2 - bits in status byte 2
 *  </ul>
 * <p>
 * Slot Status byte definitions and macros
 * <ul>
 * <li>D7-SL_SPURGE : 1=SLOT purge en, ALSO adrSEL (INTERNAL use only) (not seen on NET!)
 *
 * <li>D6-SL_CONUP : CONDN/CONUP: bit encoding-Control double
 * linked Consist List
 *    <ul>
 *    <li> 11=LOGICAL MID CONSIST , Linked up AND down
 *    <li> 10=LOGICAL CONSIST TOP, Only linked downwards
 *    <li> 01=LOGICAL CONSIST SUB-MEMBER, Only linked upwards
 *    <li> 00=FREE locomotive, no CONSIST indirection/linking
 *    </ul>
 * ALLOWS "CONSISTS of CONSISTS". Uplinked means that Slot SPD number is
 * now SLOT adr of SPD/DIR and STATUS of
 * consist. i.e. is an Indirect pointer. This Slot
 * has same BUSY/ACTIVE bits as TOP of Consist. TOP is
 * loco with SPD/DIR for whole consist. (top of list).
 *
 * <li> D5-SL_BUSY: BUSY/ACTIVE: bit encoding for SLOT activity
 *      <ul>
 *      <li> 11=IN_USE loco adr in SLOT -REFRESHED
 *      </ul>
 *
 * <li> D4-SL_ACTIVE ;
 *      <ul>
 *      <li>10=IDLE loco adr in SLOT -NOT refreshed
 *      <li>01=COMMON loco adr IN SLOT-refreshed
 *      <li>00=FREE SLOT, no valid DATA -not refreshed
 *      </ul>
 *
 * <li>D3-SL_CONDN : shows other SLOT Consist linked INTO this slot, see SL_CONUP
 *
 * <li>D2-SL_SPDEX ; 3 BITS for Decoder TYPE encoding for this SLOT
 *
 * <li>D1-SL_SPD14
 *      <ul>
 *      <li>011=send 128 speed mode packets
 *      </ul>
 *
 * <li>D0-SL_SPD28
 *      <ul>
 *      <li>010=14 step MODE
 *      <li>001=28 step. Generate Trinary packets for this Mobile ADR
 *      <li>000=28 step. 3 BYTE PKT regular mode
 *      <li>111=128 Step decoder, Allow Advanced DCC consisting
 *      <li>100=28 Step decoder ,Allow Advanced DCC consisting
 *      </ul>
 * </ul>
 * Note that the values in this class have been taken from the llnmom C program
 * of Ron W. Auld, which included some work of John Kabat. The symbol names are
 * copied from the loconet.h file, CVS revision 1.1.1.1, program release 0.3.0
 * Those parts are (C) Copyright 2001 Ron W. Auld, and are used with direct
 * permission of the copyright holder.
 * <p>
 * Most major comment blocks here are quotes from the Digitrax LocoNet(r) OPCODE
 * SUMMARY found in the LocoNet(r) Personal Edition 1.
 * <p>
 * Al Silverstein provided the reverse-engineering effort for the
 * OPC_MULTI_SENSE message.
 * <p>
 * Alain Le Marchand completed the list of constants for Uhlenbrock Intellibox-I
 * and -II, from observations of Intellibox traffic.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2015
 * @author Ron W. Auld
 * @author John Kabat
 * @author Alain Le Marchand
 * @author B. Milhaupt Copyright (C) 2018
 */
public final class LnConstants {

    /* various bit masks */
    public final static int DIRF_DIR = 0x20;  /* direction bit    */

    public final static int DIRF_F0 = 0x10;  /* Function 0 bit   */

    public final static int DIRF_F4 = 0x08;  /* Function 1 bit   */

    public final static int DIRF_F3 = 0x04;  /* Function 2 bit   */

    public final static int DIRF_F2 = 0x02;  /* Function 3 bit   */

    public final static int DIRF_F1 = 0x01;  /* Function 4 bit   */

    public final static int SND_F8 = 0x08;  /* Sound 4/Function 8 bit */

    public final static int SND_F7 = 0x04;  /* Sound 3/Function 7 bit */

    public final static int SND_F6 = 0x02;  /* Sound 2/Function 6 bit */

    public final static int SND_F5 = 0x01;  /* Sound 1/Function 5 bit */

    public final static int OPC_SW_ACK_CLOSED = 0x20;  /* command switch closed/open bit   */

    public final static int OPC_SW_ACK_OUTPUT = 0x10;  /* command switch output on/off bit */

    public final static int OPC_INPUT_REP_CB = 0x40;  /* control bit, reserved otherwise      */

    public final static int OPC_INPUT_REP_SW = 0x20;  /* input is switch input, aux otherwise */

    public final static int OPC_INPUT_REP_HI = 0x10;  /* input is HI, LO otherwise            */

    public final static int OPC_SW_REP_SW = 0x20;  /* switch input, aux input otherwise    */

    public final static int OPC_SW_REP_HI = 0x10;  /* input is HI, LO otherwise            */

    public final static int OPC_SW_REP_CLOSED = 0x20;  /* 'Closed' line is ON, OFF otherwise   */

    public final static int OPC_SW_REP_THROWN = 0x10;  /* 'Thrown' line is ON, OFF otherwise   */

    public final static int OPC_SW_REP_INPUTS = 0x40;  /* sensor inputs, outputs otherwise     */

    public final static int OPC_SW_REQ_DIR = 0x20;  /* switch direction - closed/thrown     */

    public final static int OPC_SW_REQ_OUT = 0x10;  /* output On/Off                        */

    public final static int OPC_LOCO_SPD_ESTOP = 0x01; /* emergency stop command               */

    public final static int OPC_MULTI_SENSE_MSG = 0x60; // byte 1
    public final static int OPC_MULTI_SENSE_PRESENT = 0x20; // MSG field: transponder seen
    public final static int OPC_MULTI_SENSE_ABSENT = 0x00; // MSG field: transponder lost
    public final static int OPC_MULTI_SENSE_POWER = 0x60; // MSG field: Power message

    public final static int STAT1_SL_SPURGE = 0x80;  /* internal use only, not seen on net */

    /** consist status                     */
    public final static int STAT1_SL_CONUP = 0x40;

    /** Used with STAT1_SL_ACTIVE         */
    public final static int STAT1_SL_BUSY = 0x20;

    public final static int STAT1_SL_ACTIVE = 0x10;

    public final static int STAT1_SL_CONDN = 0x08;

    public final static int STAT1_SL_SPDEX = 0x04;

    public final static int STAT1_SL_SPD14 = 0x02;

    public final static int STAT1_SL_SPD28 = 0x01;

    /** 1 = Adv. Consisting supressed      */
    public final static int STAT2_SL_SUPPRESS = 0x01;

    /** 1 = ID1/ID2 is not ID usage        */
    public final static int STAT2_SL_NOT_ID = 0x04;

    /** 1 = ID1/ID2 is not encoded alias   */
    public final static int STAT2_SL_NOTENCOD = 0x08;

    public final static int STAT2_ALIAS_MASK = STAT2_SL_NOTENCOD | STAT2_SL_NOT_ID;
    public final static int STAT2_ID_IS_ALIAS = STAT2_SL_NOT_ID;

    /* mask and values for consist determination */
    public final static int CONSIST_MASK = STAT1_SL_CONDN | STAT1_SL_CONUP;
    public final static int CONSIST_MID = STAT1_SL_CONDN | STAT1_SL_CONUP;
    public final static int CONSIST_TOP = STAT1_SL_CONDN;
    public final static int CONSIST_SUB = STAT1_SL_CONUP;
    public final static int CONSIST_NO = 0;

    /**
     * Encode consisting status as a string
     *
     * @param s  consist status bits
     * @return string contaning a description of the consisting state
     */
    public final static String CONSIST_STAT(int s) {
        return ((s & CONSIST_MASK) == CONSIST_MID) ? "Mid Consist" // NOI18N
                : (((s & CONSIST_MASK) == CONSIST_TOP) ? "Consist TOP" // NOI18N
                        : (((s & CONSIST_MASK) == CONSIST_SUB) ? "Sub Consist" // NOI18N
                                : "Not Consisted")); // NOI18N
    }

   /** Mask for locomotive use determination.
    * Compare value to {@link #LOCO_IN_USE},  {@link #LOCO_IDLE},
    * {@link #LOCO_COMMON},  {@link #LOCO_FREE}
    */
    public final static int LOCOSTAT_MASK = STAT1_SL_BUSY | STAT1_SL_ACTIVE;
    /** Value for locomotive use determination */
    public final static int LOCO_IN_USE = STAT1_SL_BUSY | STAT1_SL_ACTIVE;
    /** Value for locomotive use determination */
    public final static int LOCO_IDLE = STAT1_SL_BUSY;
    /** Value for locomotive use determination */
    public final static int LOCO_COMMON = STAT1_SL_ACTIVE;
    /** Value for locomotive use determination */
    public final static int LOCO_FREE = 0;

    /**
     * Encode loco status as a string
     *
     * @param s  integer containing loco "status"
     * @return string containing a description of the loco "status"
     */
    public final static String LOCO_STAT(int s) {
        return ((s & LOCOSTAT_MASK) == LOCO_IN_USE) ? "In-Use" // NOI18N
                : (((s & LOCOSTAT_MASK) == LOCO_IDLE) ? "Idle" // NOI18N
                        : (((s & LOCOSTAT_MASK) == LOCO_COMMON) ? "Common" // NOI18N
                                : "Free")); // NOI18N
    }

   /** Mask for decoder type encoding for this slot.
    * Compare value to
    * {@link #DEC_MODE_128A}, {@link #DEC_MODE_28A}, {@link #DEC_MODE_128},
    * {@link #DEC_MODE_14}, {@link #DEC_MODE_28TRI}, {@link #DEC_MODE_28}
    */
    public final static int DEC_MODE_MASK = STAT1_SL_SPDEX | STAT1_SL_SPD14 | STAT1_SL_SPD28;
    /** Decoder Type: 128 step, Advanced Consisting allowed */
    public final static int DEC_MODE_128A = STAT1_SL_SPDEX | STAT1_SL_SPD14 | STAT1_SL_SPD28;
    /** Decoder Type: 28 step, Advanced Consisting allowed */
    public final static int DEC_MODE_28A = STAT1_SL_SPDEX;
    /** Decoder Type:  128 step */
    public final static int DEC_MODE_128 = STAT1_SL_SPD14 | STAT1_SL_SPD28;
    /** Decoder Type:  14 step */
    public final static int DEC_MODE_14 = STAT1_SL_SPD14;
    /** Decoder Type:  28 step, send Motorola Trinary */
    public final static int DEC_MODE_28TRI = STAT1_SL_SPD28;
    /** Decoder Type:  28 step */
    public final static int DEC_MODE_28 = 0;

    public final static String DEC_MODE(int s) { // encode decoder type as a string
        return ((s & DEC_MODE_MASK) == DEC_MODE_128A) ? "128 (Allow Adv. consisting)" // NOI18N
                : (((s & DEC_MODE_MASK) == DEC_MODE_28A) ? "28 (Allow Adv. consisting)" // NOI18N
                        : (((s & DEC_MODE_MASK) == DEC_MODE_128) ? "128" // NOI18N
                                : (((s & DEC_MODE_MASK) == DEC_MODE_14) ? "14" // NOI18N
                                        : (((s & DEC_MODE_MASK) == DEC_MODE_28TRI) ? "28 (Motorola)" // NOI18N
                                                : "28")))); // NOI18N
    }

    /* values for track status encoding for this slot */
    public final static int GTRK_PROG_BUSY = 0x08; /* 1 = programming track in this master is Busy         */

    public final static int GTRK_MLOK1 = 0x04;     /* 0 = Master is DT200, 1=Master implements LocoNet 1.1 */

    public final static int GTRK_IDLE = 0x02;      /* 0=TRACK is PAUSED, B'cast EMERG STOP.                */

    public final static int GTRK_POWER = 0x01;     /* 1=DCC packets are ON in MASTER, Global POWER up      */

    /** Fast clock is in this slot                           */
    public final static int FC_SLOT = 0x7b;

    /** Fast CLock valid **/
    public final static int FC_VALID = 0X40;

     /** This slot communicates with the programming track   */
    public final static int PRG_SLOT = 0x7c;

    /** This slot holds extended configuration bits for some command stations */
    public final static int CFG_EXT_SLOT = 0x7e;

    /** This slot holds configuration bits                   */
    public final static int CFG_SLOT = 0x7f;

    /** Values and macros to decode programming messages */
    public final static int PCMD_RW = 0x40;        /* 1 = write, 0 = read                                  */

    public final static int PCMD_BYTE_MODE = 0x20; /* 1 = byte operation, 0 = bit operation (if possible)  */

    public final static int PCMD_TY1 = 0x10;       /* TY1 Programming type select bit                      */

    public final static int PCMD_TY0 = 0x08;       /* TY0 Programming type select bit                      */

    public final static int PCMD_OPS_MODE = 0x04;  /* 1 = Ops Mode, 0 = Service Mode                       */

    public final static int PCMD_RSVRD1 = 0x02;    /* reserved                                             */

    public final static int PCMD_RSVRD0 = 0x01;    /* reserved                                             */

    /** Programming mode mask */
    public final static int PCMD_MODE_MASK = PCMD_BYTE_MODE | PCMD_OPS_MODE | PCMD_TY1 | PCMD_TY0;

    /** Programming modes: Paged mode byte R/W on Service Track */
    public final static int PAGED_ON_SRVC_TRK = PCMD_BYTE_MODE;

    /** Programming modes: Direct mode byte R/W on Service Track */
    public final static int DIR_BYTE_ON_SRVC_TRK = PCMD_BYTE_MODE | PCMD_TY0;

    /** Programming modes: Direct mode bit  R/W on Service Track */
    public final static int DIR_BIT_ON_SRVC_TRK = PCMD_TY0;

    /** Programming modes: Physical Register byte R/W on Service Track */
    public final static int REG_BYTE_RW_ON_SRVC_TRK = PCMD_TY1;

    /** Programming modes: Service Track Reserved function */
    public final static int SRVC_TRK_RESERVED = PCMD_TY1 | PCMD_TY0;

    /** Programming modes: Ops mode byte program - no feedback */
    public final static int OPS_BYTE_NO_FEEDBACK = PCMD_BYTE_MODE | PCMD_OPS_MODE;

    /** Programming modes: Ops mode byte program - feedback */
    public final static int OPS_BYTE_FEEDBACK = OPS_BYTE_NO_FEEDBACK | PCMD_TY0;

    /** Programming modes: Ops mode bit program - no feedback */
    public final static int OPS_BIT_NO_FEEDBACK = PCMD_OPS_MODE;

    /** Programming modes: Ops mode bit program - feedback */
    public final static int OPS_BIT_FEEDBACK = OPS_BIT_NO_FEEDBACK | PCMD_TY0;

    /** Programmer status error flags: User aborted this command */
    public final static int PSTAT_USER_ABORTED = 0x08;

    /** Programmer status error flags: Failed to detect Read Compare Acknowledge from decoder */
    public final static int PSTAT_READ_FAIL = 0x04;

    /** Programmer status error flags: No Write acknowledge from decoder */
    public final static int PSTAT_WRITE_FAIL = 0x02;

    /** Programmer status error flags: Service mode programming track empty  */
    public final static int PSTAT_NO_DECODER = 0x01;

    /* bit masks for CVH */
    public final static int CVH_CV8_CV9 = 0x30; /* mask for CV# bits 8 and 9 */

    public final static int CVH_CV7 = 0x01;     /* mask for CV# bit 7        */

    public final static int CVH_D7 = 0x02;      /* MSbit for data value      */

    /* LocoNet opcodes */
    public final static int OPC_GPBUSY = 0x81;
    public final static int OPC_GPOFF = 0x82;
    public final static int OPC_GPON = 0x83;
    public final static int OPC_IDLE = 0x85;
    public final static int OPC_RE_LOCORESET_BUTTON = 0x8A; // Undocumented name
    public final static int OPC_LOCO_SPD = 0xa0;
    public final static int OPC_LOCO_DIRF = 0xa1;
    public final static int OPC_LOCO_SND = 0xa2;
    public final static int OPC_SW_REQ = 0xb0;
    public final static int OPC_SW_REP = 0xb1;
    public final static int OPC_INPUT_REP = 0xb2;
    public final static int OPC_UNKNOWN = 0xb3;
    public final static int OPC_LONG_ACK = 0xb4;
    public final static int OPC_SLOT_STAT1 = 0xb5;
    public final static int OPC_CONSIST_FUNC = 0xb6;
    public final static int OPC_UNLINK_SLOTS = 0xb8;
    public final static int OPC_LINK_SLOTS = 0xb9;
    public final static int OPC_MOVE_SLOTS = 0xba;
    public final static int OPC_RQ_SL_DATA = 0xbb;
    public final static int OPC_SW_STATE = 0xbc;
    public final static int OPC_SW_ACK = 0xbd;
    public final static int OPC_LOCO_ADR = 0xbf;
    public final static int OPC_MULTI_SENSE = 0xd0; // Undocumented name
    public final static int OPC_PANEL_RESPONSE = 0xd7; // Undocumented name
    public final static int OPC_PANEL_QUERY = 0xdf; // Undocumented name
    public final static int OPC_LISSY_UPDATE = 0xe4; // Undocumented name
    public final static int OPC_PEER_XFER = 0xe5;
    public final static int OPC_ALM_READ = 0xe6; // Undocumented name
    public final static int OPC_SL_RD_DATA = 0xe7;
    public final static int OPC_IMM_PACKET = 0xed;
    //TODO Conflicts with OPC_EXP_WR_SL_DATA - or maybe length?
    public final static int OPC_IMM_PACKET_2 = 0xee;
    public final static int OPC_WR_SL_DATA = 0xef;
    //TODO Conflicts with OPC_EXP_WR_SL_DATA - or maybe length?
    public final static int OPC_ALM_WRITE = 0xee; // Undocumented name
    public final static int OPC_MASK = 0x7f;  /* mask for acknowledge opcodes */

    /* protocol level */

    /** The protocol has not been established */
    public final static int LOCONETPROTOCOL_UNKNOWN = 0;
    /** Supports loconet 1.1 */
    public final static int LOCONETPROTOCOL_ONE = 1;
    /** Supports the protocol introduced to DCS240, DCS210 */
    public final static int LOCONETPROTOCOL_TWO = 2;

    /* Expanded slot codes */
    public final static int OPC_EXP_REQ_SLOT = 0xbe;
    public final static int OPC_EXP_SLOT_MOVE = 0xd4;
    public final static int OPC_EXP_RD_SL_DATA = 0xe6;
    public final static int OPC_EXP_WR_SL_DATA = 0xee;
    public final static int OPC_EXP_SEND_SUB_CODE_MASK_SPEED = 0b11110000;
    public final static int OPC_EXP_SEND_SUB_CODE_MASK_FUNCTION = 0b11111000;
    public final static int OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR = 0xd5;
    public final static int OPC_EXP_SEND_SPEED_AND_DIR_MASK = 0b00010000;
    public final static int OPC_EXP_SEND_FUNCTION_GROUP_F0F6_MASK = 0b00010000;
    public final static int OPC_EXP_SEND_FUNCTION_GROUP_F7F13_MASK = 0b00011000;
    public final static int OPC_EXP_SEND_FUNCTION_GROUP_F14F20_MASK = 0b00100000;
    public final static int OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF_MASK = 0b00101000;
    public final static int OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28ON_MASK =  0b00110000;

    /**
     * Encode LocoNet Opcode as a string
     *
     * @param opcode  a LocoNet opcode value
     * @return string containing the opcode "name"
     */
    public final static String OPC_NAME(int opcode) {
        switch (opcode) {
            case OPC_GPBUSY     : return "OPC_GPBUSY"; // NOI18N
            case OPC_GPOFF      : return "OPC_GPOFF"; // NOI18N
            case OPC_GPON       : return "OPC_GPON"; // NOI18N
            case OPC_IDLE       : return "OPC_IDLE"; // NOI18N
            case OPC_RE_LOCORESET_BUTTON:   return "OPC_RE_LOCORESET_BUTTON"; // NOI18N
            case OPC_LOCO_SPD   : return "OPC_LOCO_SPD"; // NOI18N
            case OPC_LOCO_DIRF  : return "OPC_LOCO_DIRF"; // NOI18N
            case OPC_LOCO_SND   : return "OPC_LOCO_SND"; // NOI18N
            case OPC_SW_REQ     : return "OPC_SW_REQ"; // NOI18N
            case OPC_SW_REP     : return "OPC_SW_REP"; // NOI18N
            case OPC_INPUT_REP  : return "OPC_INPUT_REP"; // NOI18N
            case OPC_UNKNOWN    : return "OPC_UNKNOWN"; // NOI18N
            case OPC_LONG_ACK   : return "OPC_LONG_ACK"; // NOI18N
            case OPC_SLOT_STAT1 : return "OPC_SLOT_STAT1"; // NOI18N
            case OPC_CONSIST_FUNC: return "OPC_CONSIST_FUNC"; // NOI18N
            case OPC_UNLINK_SLOTS: return "OPC_UNLINK_SLOTS"; // NOI18N
            case OPC_LINK_SLOTS : return "OPC_LINK_SLOTS"; // NOI18N
            case OPC_MOVE_SLOTS : return "OPC_MOVE_SLOTS"; // NOI18N
            case OPC_RQ_SL_DATA : return "OPC_RQ_SL_DATA"; // NOI18N
            case OPC_SW_STATE   : return "OPC_SW_STATE"; // NOI18N
            case OPC_SW_ACK     : return "OPC_SW_ACK"; // NOI18N
            case OPC_LOCO_ADR   : return "OPC_LOCO_ADR"; // NOI18N
            case OPC_MULTI_SENSE: return "OPC_MULTI_SENSE"; // NOI18N
            case OPC_PANEL_QUERY: return "OPC_PANEL_QUERY"; // NOI18N
            case OPC_PANEL_RESPONSE: return "OPC_PANEL_RESPONSE"; // NOI18N
            case OPC_LISSY_UPDATE: return "OPC_LISSY_UPDATE"; // NOI18N
            case OPC_PEER_XFER  : return "OPC_PEER_XFER"; // NOI18N
            case OPC_ALM_READ   : return "OPC_ALM_READ"; // NOI18N
            case OPC_SL_RD_DATA : return "OPC_SL_RD_DATA"; // NOI18N
            case OPC_IMM_PACKET : return "OPC_IMM_PACKET"; // NOI18N
            case OPC_IMM_PACKET_2: return "OPC_IMM_PACKET_2"; // NOI18N
            case OPC_WR_SL_DATA : return "OPC_WR_SL_DATA"; // NOI18N
            // case OPC_WR_SL_DATA_EXP: return "OPC_WR_SL_DATA_EXP"; // duplicates 0xEE
            // case OPC_ALM_WRITE: return "OPC_ALM_WRITE"; // duplicates 0xEE
            default: return "<unknown>"; // NOI18N
        }
    }

// start of values not from llnmon.c

// Multimeter polling interval
    public final static int METER_INTERVAL_MS = 30000;

// Expanded slot index values
    public final static int EXP_MAST = 0;
    public final static int EXP_SLOT = 0x01;
    public final static int EXPD_LENGTH = 16;
//offsets into message
    public final static int EXPD_STAT = 0;
    public final static int EXPD_ADRL = 1;
    public final static int EXPD_ADRH = 2;
    public final static int EXPD_FLAGS = 3;
    public final static int EXPD_SPD = 4;
    public final static int EXPD_F28F20F12 = 5;
    public final static int EXPD_DIR_F0F4_F1 = 6;
    public final static int EXPD_F11_F5 = 7;
    public final static int EXPD_F19_F13 = 8;
    public final static int EXPD_F27_F21 = 9;

//  opcode keys used to express interest in various messages
//            note these are _not_ the LocoNet opcode values!
    public final static int KEY_GPBUSY = 1 << 0;
    public final static int KEY_GPOFF = 1 << 1;
    public final static int KEY_GPON = 1 << 2;
    public final static int KEY_IDLE = 1 << 3;

    public final static int KEY_LOCO_SPD = 1 << 4;
    public final static int KEY_LOCO_DIRF = 1 << 5;
    public final static int KEY_LOCO_SND = 1 << 6;
    public final static int KEY_SW_REQ = 1 << 7;

    public final static int KEY_SW_REP = 1 << 8;
    public final static int KEY_INPUT_REP = 1 << 9;
    public final static int KEY_LONG_ACK = 1 << 10;
    public final static int KEY_SLOT_STAT1 = 1 << 11;

    public final static int KEY_CONSIST_FUNC = 1 << 12;
    public final static int KEY_UNLINK_SLOTS = 1 << 13;
    public final static int KEY_LINK_SLOTS = 1 << 14;
    public final static int KEY_MOVE_SLOTS = 1 << 15;

    public final static int KEY_RQ_SL_DATA = 1 << 16;
    public final static int KEY_SW_STATE = 1 << 17;
    public final static int KEY_SW_ACK = 1 << 18;
    public final static int KEY_LOCO_ADR = 1 << 19;

    public final static int KEY_PEER_XFR = 1 << 20;
    public final static int KEY_IMM_PACKET = 1 << 21;
    public final static int KEY_WR_SL_DATA = 1 << 22;

// reverse-engineered constants
    public final static int RE_IPL_MFR_DIGITRAX = 0x00;
    public final static int RE_IPL_MFR_ALL = 0x00;
    public final static int RE_IPL_DIGITRAX_HOST_LNRP = 0x01;
    public final static int RE_IPL_DIGITRAX_HOST_UT4 = 0x04;
    public final static int RE_IPL_DIGITRAX_HOST_WTL12 = 0x0c;
    public final static int RE_IPL_DIGITRAX_HOST_DB210OPTO = 0x14;
    public final static int RE_IPL_DIGITRAX_HOST_DB210 = 0x15;
    public final static int RE_IPL_DIGITRAX_HOST_DB220 = 0x16;
    public final static int RE_IPL_DIGITRAX_HOST_DCS210 = 0x1b;
    public final static int RE_IPL_DIGITRAX_HOST_DCS240 = 0x1c;
    public final static int RE_IPL_DIGITRAX_HOST_DCS52 = 0x34;
    public final static int RE_IPL_DIGITRAX_HOST_PR3 = 0x23;
    public final static int RE_IPL_DIGITRAX_HOST_PR4 = 0x24;
    public final static int RE_IPL_DIGITRAX_HOST_DT402 = 0x2A;
    public final static int RE_IPL_DIGITRAX_HOST_DT500 = 0x32;
    public final static int RE_IPL_DIGITRAX_HOST_DCS51 = 0x33;
    public final static int RE_IPL_DIGITRAX_HOST_BXPA1 = 0x51;
    public final static int RE_IPL_DIGITRAX_HOST_UR92 = 0x5C;
    public final static int RE_IPL_DIGITRAX_HOST_BXP88 = 0x58;
    public final static int RE_IPL_DIGITRAX_HOST_LNWI = 0x63;
    public final static int RE_IPL_DIGITRAX_HOST_ALL = 0x00;
    public final static int RE_IPL_DIGITRAX_SLAVE_RF24 = 0x18;
    public final static int RE_IPL_DIGITRAX_SLAVE_ALL = 0x00;
    public final static int RE_IPL_PING_OPERATION = 0x08;
    public final static int RE_IPL_IDENTITY_OPERATION = 0x0f;
    public final static int RE_LACK_SPEC_CASE1 = 0x50; // special case LACK response for OpSw accesses
    public final static int RE_LACK_SPEC_CASE2 = 0x00; // special case LACK response for OpSw accesses
    public final static int RE_OPC_PR3_MODE = 0xD3;
    public final static int RE_MULTI_SENSE_DEV_TYPE_PM4X = 0x00;
    public final static int RE_MULTI_SENSE_DEV_TYPE_BDL16X = 0x01;
    public final static int RE_MULTI_SENSE_DEV_TYPE_SE8 = 0x02;
    public final static int RE_MULTI_SENSE_DEV_TYPE_DS64 = 0x03;

// Below data is assumed, based on firmware files available from RR-Cirkits web site
    public final static int RE_IPL_MFR_RR_CIRKITS = 87;
    public final static int RE_IPL_RRCIRKITS_HOST_TC64 = 11;
    public final static int RE_IPL_RRCIRKITS_HOST_LNCP = 12;
    public final static int RE_IPL_RRCIRKITS_HOST_SIGNALMAN = 21;
    public final static int RE_IPL_RRCIRKITS_HOST_TOWERMAN = 22;
    public final static int RE_IPL_RRCIRKITS_HOST_WATCHMAN = 23;
    public final static int RE_IPL_RRCIRKITS_HOST_TC64_MKII = 24;
    public final static int RE_IPL_RRCIRKITS_HOST_MOTORMAN = 25;
    public final static int RE_IPL_RRCIRKITS_HOST_MOTORMAN_II = 28;
    public final static int RE_IPL_RRCIRKITS_SLAVE_ALL = 00;

// Constants associated with OPC_PEER_XFR for Duplex operations
    public final static int RE_DPLX_OP_TYPE_WRITE = 0x00;
    public final static int RE_DPLX_OP_TYPE_QUERY = 0x08;
    public final static int RE_DPLX_OP_TYPE_REPORT = 0x10;
    public final static int RE_DPLX_OP_LEN = 0x14;
    public final static int RE_IPL_OP_LEN = 0x14;
    public final static int RE_IPL_OP_QUERY = 0x08;
    public final static int RE_IPL_OP_REPORT = 0x10;
    public final static int RE_IPL_OP_SLV_QUERY = 0x00;
    public final static int RE_IPL_OP_HFW_QUERY = 0x00;
    public final static int RE_IPL_OP_HSNM_QUERY = 0x00;
    public final static int RE_IPL_OP_SFW_QUERY = 0x00;
    public final static int RE_IPL_OP_HSN0_QUERY = 0x01;
    public final static int RE_IPL_OP_HSN1_QUERY = 0x00;
    public final static int RE_IPL_OP_HSN2_QUERY = 0x00;
    public final static int RE_IPL_OP_SSNM_QUERY = 0x00;
    public final static int RE_IPL__OP_SSN0_QUERY = 0x00;
    public final static int RE_IPL_OP_SSN1_QUERY = 0x00;
    public final static int RE_IPL_OP_SSN2_QUERY = 0x00;
    public final static int RE_IPL_OP_SSN3_QUERY = 0x00;
    public final static int RE_DPLX_GP_CHAN_TYPE = 2;
    public final static int RE_DPLX_GP_NAME_TYPE = 3;
    public final static int RE_DPLX_GP_ID_TYPE = 4;
    public final static int RE_DPLX_GP_PW_TYPE = 7;
    public final static int RE_DPLX_OPC_BAD = 0x80;
    public final static int RE_DPLX_MSB1_BIT = 1;
    public final static int RE_DPLX_MSB2_BIT = 2;
    public final static int RE_DPLX_MSB3_BIT = 4;
    public final static int RE_DPLX_MSB4_BIT = 8;
    public final static int RE_DPLX_BUMP_MSB1_BIT = 7;
    public final static int RE_DPLX_BUMP_MSB2_BIT = 6;
    public final static int RE_DPLX_BUMP_MSB3_BIT = 5;
    public final static int RE_DPLX_BUMP_MSB4_BIT = 4;
    public final static int RE_DPLX_7BITS_MAX = 127;
    public final static int RE_DPLX_MAX_NOT_OPC = 0x7F;
    public final static int RE_DPLX_ALT_CH_MSB_BIT = 0x4;
    public final static int RE_DPLX_ALT_CH_MSB_SHIFT = 0x5;
    public final static int RE_DPLX_ALT_ID_MSB_BIT = 0x8;
    public final static int RE_DPLX_ALT_ID_MSB_SHIFT = 0x4;
    public final static int RE_DPLX_ALT_PW1_MSB_BIT = 0x1;
    public final static int RE_DPLX_ALT_PW1_MSB_SHIFT = 0x3;
    public final static int RE_DPLX_ALT_PW3_MSB_BIT = 0x2;
    public final static int RE_DPLX_ALT_PW3_MSB_SHIFT = 0x2;

    public final static int RE_DPLX_DATA_LS_NIBBLE = 0x0F;
    public final static int RE_DPLX_DATA_MS_NIBBLE = 0x70;
    public final static int RE_DPLX_DATA_MS_NIBBLE_SHIFT = 4;

// Duplex Group Scan Operation Constants
    public final static int RE_DPLX_SCAN_OP_LEN = 0x14;
    public final static int RE_DPLX_SCAN_QUERY_B2 = 0x10;
    public final static int RE_DPLX_SCAN_QUERY_B3 = 0x08;
    public final static int RE_DPLX_SCAN_QUERY_B4 = 0x00;
    public final static int RE_DPLX_SCAN_REPORT_B2 = 0x10;
    public final static int RE_DPLX_SCAN_REPORT_B3 = 0x10;

    /* Intellibox-II mobile decoder function control beyond F8
     * also used for Intellibox-I ("one") with SW version 2.x for control of functions beyond F8
     * Intellibox-I version 2.x has two ways to control F0-F8:
     *    - with regular LocoNet OPC_LOCO_SND and OPC_LOCO_DIRF
     *    - with special Uhlenbrock RE_OPC_IB2_SPECIAL
     *
     * 4 byte MESSAGE with OPCODE = RE_OPC_IB2_F9_F12
     * Used by Intellibox-II only, for F9-F12
     * FORMAT = <OPC>,<SLOT>,<FUNC>,<CKSUM>
     * :
     *  <SLOT> = Slot number
     *  <FUNC> = functions F9-F12 mask
     */
    public final static int RE_OPC_IB2_F9_F12 = 0xA3;
    public final static int RE_IB2_F9_MASK = 0x01;
    public final static int RE_IB2_F10_MASK = 0x02;
    public final static int RE_IB2_F11_MASK = 0x04;
    public final static int RE_IB2_F12_MASK = 0x08;

    /* 6 byte MESSAGE with OPCODE = RE_OPC_IB2_SPECIAL
     * Used by Intellibox-I for F0-F28 and Intellibox-II for F13-F28
     * For Intellibox-I, for F0-F8:
     *      - F0-F8 triggers this message only when controlling function after pressing twice on lok# button
     *      - Direct control of functions through function buttons triggers the regular LocoNet function message
     * :
     * FORMAT = <OPC>,<SPE>,<SLOT>,<FTOK>,<FUNC>,<CKSUM>
     * :
     *  <SPE> = Specific value RE_IB2_SPECIAL_FUNCS_TOKEN
     *  <SLOT> = Slot number
     *  <FTOK> = functions token
     *  <FUNC> = functions mask
     */
// Common to Intellibox-I and -II :
    public final static int RE_OPC_IB2_SPECIAL = 0xD4; //For functions F13-F28 (IB-II) and by IB-I v2.x ("one") for F0-F28
    public final static int RE_IB2_SPECIAL_FUNCS_TOKEN = 0x20;
//Used only by Intellibox-I ("one") version 2.x
    public final static int RE_IB1_SPECIAL_F0_F4_TOKEN = 0x06; //Used by Intellibox-I ("one") version 2.x
    public final static int RE_IB1_F0_MASK = 0x10; //Used by Intellibox-I ("one") version 2.x only for F0
    public final static int RE_IB1_F1_MASK = 0x01; //Used by Intellibox-I ("one") version 2.x only for F1
    public final static int RE_IB1_F2_MASK = 0x02; //Used by Intellibox-I ("one") version 2.x only for F2
    public final static int RE_IB1_F3_MASK = 0x04; //Used by Intellibox-I ("one") version 2.x only for F3
    public final static int RE_IB1_F4_MASK = 0x08; //Used by Intellibox-I ("one") version 2.x only for F4
//Used only by Intellibox-I ("one") version 2.x
    public final static int RE_IB1_SPECIAL_F5_F11_TOKEN = 0x07; //Used by Intellibox-I ("one") version 2.x
    public final static int RE_IB1_F5_MASK = 0x01; //Used by Intellibox-I ("one") version 2.x only for F5
    public final static int RE_IB1_F6_MASK = 0x02; //Used by Intellibox-I ("one") version 2.x only for F6
    public final static int RE_IB1_F7_MASK = 0x04; //Used by Intellibox-I ("one") version 2.x only for F7
    public final static int RE_IB1_F8_MASK = 0x08; //Used by Intellibox-I ("one") version 2.x only for F8
    public final static int RE_IB1_F9_MASK = 0x10; //Used by Intellibox-I ("one") version 2.x only for F9
    public final static int RE_IB1_F10_MASK = 0x20; //Used by Intellibox-I ("one") version 2.x only for F10
    public final static int RE_IB1_F11_MASK = 0x40; //Used by Intellibox-I ("one") version 2.x only for F11
// Common to Intellibox-I and -II :
    public final static int RE_IB2_SPECIAL_F13_F19_TOKEN = 0x08;
    public final static int RE_IB2_F13_MASK = 0x01;
    public final static int RE_IB2_F14_MASK = 0x02;
    public final static int RE_IB2_F15_MASK = 0x04;
    public final static int RE_IB2_F16_MASK = 0x08;
    public final static int RE_IB2_F17_MASK = 0x10;
    public final static int RE_IB2_F18_MASK = 0x20;
    public final static int RE_IB2_F19_MASK = 0x40;
// Common to Intellibox-I and -II :
    public final static int RE_IB2_SPECIAL_F21_F27_TOKEN = 0x09;
    public final static int RE_IB2_F21_MASK = 0x01;
    public final static int RE_IB2_F22_MASK = 0x02;
    public final static int RE_IB2_F23_MASK = 0x04;
    public final static int RE_IB2_F24_MASK = 0x08;
    public final static int RE_IB2_F25_MASK = 0x10;
    public final static int RE_IB2_F26_MASK = 0x20;
    public final static int RE_IB2_F27_MASK = 0x40;
// Common to Intellibox-I and -II :
    public final static int RE_IB2_SPECIAL_F20_F28_TOKEN = 0x05; // Also applicable to F12
    public final static int RE_IB2_SPECIAL_F12_MASK = 0x10; //F12 is also controlled with the special F20-F28 command
    public final static int RE_IB2_SPECIAL_F20_MASK = 0x20;
    public final static int RE_IB2_SPECIAL_F28_MASK = 0x40;

}
