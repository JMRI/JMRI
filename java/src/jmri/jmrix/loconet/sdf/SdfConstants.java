package jmri.jmrix.loconet.sdf;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2010
 */
@SuppressFBWarnings(value = "MS_OOI_PKGPROTECT") // Don't worry about malicious code changing constants
public interface SdfConstants {

    /* **********************
     * Constants
     ** **********************/
//-------------------
    int TRUE = 1;
    int FALSE = 0;
    int[] trueFalseCodes = new int[]{TRUE, FALSE};
    String[] trueFalseNames = new String[]{"TRUE", "FALSE"}; // NOI18N

// TRIGGER values
// sound TRIGGER PREMPT control bits
// SPECIAL trigs: POL,0,0,0-0,s,s,s  0-7 are special trigs
// FN trigger format: POL,0,0,B-B,b,b,b    BB 1 of 3 bytes 01,10,11 (24 fns), bbb is bit adr#
    int NORMAL = 0;   // (complete .WAV frag/preempt higher/trigger true)

    int ZAP = 0x01; // value for ZAP on PREMPT
    int RUN_WHILE_TRIG = 0x02; // mask/binary values
    int NO_PREEMPT_TRIG = 0x04;
    int NOT_TRIG = 0x80; // 12/13/04, in TRIG argument now

// 3 ls bits of INITIATE CMD 1ST byte
    int CMD_ZAP_BIT = 0; // 1=END immed if PREMPTED, 0=finish current .wav frag before END
    int RUN_WHILE_TRIG_BIT = 1; // 1=persistent while TRIGGER is valid, 0=run once
    int NO_PREEMPT_TRIG_BIT = 2;

    int NOT_TRIG_BIT = 7; // POL bit in TRIG byte 1=reverse POL

    int[] premptCodes = new int[]{ZAP, RUN_WHILE_TRIG, NO_PREEMPT_TRIG, NOT_TRIG}; // NORMAL matches all (is zero), so left off
    int[] premptMasks = premptCodes;             // just check 1 bits
    String[] premptNames = new String[]{"ZAP", "RUN_WHILE_TRIG", "NO_PREEMPT_TRIG", "NOT_TRIG", "NORMAL"}; // NOI18N

//----------
// TRIGGER initiate CODES
    int TRIG_NEVER = 0x00; // 0 is INACTIVE entry (NULL,not a valid INITIATE coding)
    int TRIG_MODE_CHNG = 0x01;
    int TRIG_MATH = 0x02; // math result trigger
    int TRIG_DISTANCE = 0x03; // have new increment of distance, use for FUEL low alarm, maintenance ETC

    int TRIG_SPD_INC = 0x04; // have SPD increase
    int TRIG_SPD_DEC = 0x05; // have SPD decrease
    int TRIG_CAM = 0x06; // TRIG on CAM event, either H/W input or AUTO-chuff,
    int TRIG_NOT_TRIG = 0x07; // brk/loop SPECIAL, a.k.a. "loop_till_init_TRIG"

// -------
// F1-F28 and F0 map into 3 BYTES of static trig state/fn state bits
// these triggers preCODED for fast TRIG bit adr   POL,0,BBB,bbb:   BB=byte,bbb=bit adr
// Mapped to internal allocations
    int MOVE_BIT = 6;    // moving BIT
    int MVFN_MASK = 0x1F; // direct FN bits

    int TRIG_SF1 = 0x08; // F1 change event
    int TRIG_SF2 = 0x09; // byte 1, bit1
    int TRIG_SF3 = 0x0A;
    int TRIG_SF4 = 0x0B;

    int TRIG_SF0 = 0x0C; // headlight/F0 state
    int TRIG_DIRNOW_CHNG = 0x0D; // have DIR_NOW changed
    int TRIG_MOVING = 0x0E; // SPD = Non-Zero
    int TRIG_SND_ACTV11 = 0x0F; // DECODER is SPD addressed within CV11 time

// ----------
    int TRIG_SF5 = 0x10; // byte 2,bit0
    int TRIG_SF6 = 0x11;
    int TRIG_SF7 = 0x12;
    int TRIG_SF8 = 0x13;

    int TRIG_SF13 = 0x14; // BYTE 2, bit 4,
    int TRIG_SF14 = 0x15;
    int TRIG_SF15 = 0x16;
    int TRIG_SF16 = 0x17;

// ---------
    int TRIG_SF9 = 0x18; // byte 3 bit 0
    int TRIG_SF10 = 0x19;
    int TRIG_SF11 = 0x1A;
    int TRIG_SF12 = 0x1B;

    int TRIG_SF17 = 0x1C; // byte 3, bit 4
    int TRIG_SF18 = 0x1D;
    int TRIG_SF19 = 0x1E;
    int TRIG_SF20 = 0x1F;

//
    int TRIG_SF21 = 0x20; // expanded FUNCTIONS
    int TRIG_SF22 = 0x21;
    int TRIG_SF23 = 0x22;
    int TRIG_SF24 = 0x23;

    int TRIG_SF25 = 0x24;
    int TRIG_SF26 = 0x25;
    int TRIG_SF27 = 0x26;
    int TRIG_SF28 = 0x27;

//
//----------------FIRST 8 bsc regs reserved
    int TRIG_BSC0 = 0x28; // expanded FUNCTIONS
    int TRIG_BSC1 = 0x29;
    int TRIG_BSC2 = 0x2A;
    int TRIG_BSC3 = 0x2B;

    int TRIG_BSC4 = 0x2C;
    int TRIG_BSC5 = 0x2D;
    int TRIG_BSC6 = 0x2E;
    int TRIG_BSC7 = 0x2F;

//------------------
//these trig lines both REPORT the external inputs to decoder, and CAN also be SET/RESET by SDF trigger commands
// and can be read back by TRANSPONDING as external ALARMS etc
    int TRIG_IN_BASE = 0x50;

    int TRIG_IN_0 = TRIG_IN_BASE + 0; // input 0 trig, CAM input if not assigned to STEAM chuf
    int TRIG_IN_1 = TRIG_IN_BASE + 1;
    int TRIG_IN_2 = TRIG_IN_BASE + 2;
    int TRIG_IN_3 = TRIG_IN_BASE + 3;

    int TRIG_IN_4 = TRIG_IN_BASE + 4;
    int TRIG_IN_5 = TRIG_IN_BASE + 5;
    int TRIG_IN_6 = TRIG_IN_BASE + 6;
    int TRIG_IN_7 = TRIG_IN_BASE + 7;

    int TRIG_NOTCH_CHNG = TRIG_IN_7 + 1;   // when notch changes
    int TRIG_TIME_16PPS = TRIG_IN_7 + 2;   // 16 per sec/64mS rate
    int TRIG_FACTORY_CVRESET = TRIG_IN_7 + 3; // have CV8=8/9 request, USER definable CV values
    int TRIG_OPSPROGWR_CV = TRIG_IN_7 + 4; // have OPSPROG action

//--------------- SCATTER TRIGGER codes, ON/OFF phase per SCATTER task
    int SCAT_TRIG_BASE = 0x60;

    int TRIG_SCAT0 = SCAT_TRIG_BASE + 0; // scatter CHNL0, phase A sel NOT_TRIG_BIT
    int TRIG_SCAT1 = SCAT_TRIG_BASE + 1;
    int TRIG_SCAT2 = SCAT_TRIG_BASE + 2;
    int TRIG_SCAT3 = SCAT_TRIG_BASE + 3;
    int TRIG_SCAT4 = SCAT_TRIG_BASE + 4; // has visible WORK register
    int TRIG_SCAT5 = SCAT_TRIG_BASE + 5; // has visible WORK register
    int TRIG_SCAT6 = SCAT_TRIG_BASE + 6; // has visible WORK register
    int TRIG_SCAT7 = SCAT_TRIG_BASE + 7; // has visible WORK register

// ===============================
//  encoded TRIGGER values related to SPEED_STATE code
    int T_SS_BASE = 0x70; // 0x20 ;base TRIG code for this STATE logic
    int TSPD = T_SS_BASE;  // base TRIG code for this STATE logic

    int T_SPD_MUTE = TSPD + 0;
    int T_SPD_TURNON = TSPD + 1;
    int T_SPD_IDLE = TSPD + 2;
    int T_SPD_ACCEL1 = TSPD + 3;

    int T_SPD_ACC_CHNG = TSPD + 4;  // 1st GEN parallel/series chng
    int T_SPD_ACCEL2 = TSPD + 5;
    int T_SPD_IDLEXIT = TSPD + 6;  // leaving IDLE state
    int T_SPD_RUN = TSPD + 7;

    int T_SPD_DECEL1 = TSPD + 8;
    int T_SPD_DEC_CHNG = TSPD + 9;
    int T_SPD_DECEL2 = TSPD + 10;
// T_SPD_DIR_CHNG =     TSPD+11   ;       (Commented in original Digitrax file)

    int T_SPD_DEC_IDLE = TSPD + 11;
    int T_SPD_TURNOFF = TSPD + 12;
    int T_SPD_DEC_SP1 = TSPD + 13;
    int T_SPD_DEC_SP2 = TSPD + 14;
    int T_SPD_DIR_CHNG = TSPD + 15;

    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY")  // known to be mutable, OK by convention
    int[] triggerCodes = new int[]{
        TRIG_NEVER, TRIG_MODE_CHNG, TRIG_MATH, TRIG_DISTANCE,
        TRIG_SPD_INC, TRIG_SPD_DEC, TRIG_CAM, TRIG_NOT_TRIG,
        TRIG_SF0, TRIG_SF1, TRIG_SF2, TRIG_SF3, TRIG_SF4,
        TRIG_SF5, TRIG_SF6, TRIG_SF7, TRIG_SF8,
        TRIG_SF9, TRIG_SF10, TRIG_SF11, TRIG_SF12,
        TRIG_SF13, TRIG_SF14, TRIG_SF15, TRIG_SF16,
        TRIG_SF17, TRIG_SF18, TRIG_SF19, TRIG_SF20,
        TRIG_DIRNOW_CHNG, TRIG_MOVING, TRIG_SND_ACTV11,
        TRIG_SF21, TRIG_SF22, TRIG_SF23, TRIG_SF24,
        TRIG_SF25, TRIG_SF26, TRIG_SF27, TRIG_SF28,
        TRIG_BSC0, TRIG_BSC1, TRIG_BSC2, TRIG_BSC3,
        TRIG_BSC4, TRIG_BSC5, TRIG_BSC6, TRIG_BSC7,
        TRIG_IN_0, TRIG_IN_1, TRIG_IN_2, TRIG_IN_3,
        TRIG_IN_4, TRIG_IN_5, TRIG_IN_6, TRIG_IN_7,
        TRIG_NOTCH_CHNG, TRIG_TIME_16PPS, TRIG_FACTORY_CVRESET, TRIG_OPSPROGWR_CV,
        TRIG_SCAT0, TRIG_SCAT1, TRIG_SCAT2, TRIG_SCAT3,
        TRIG_SCAT4, TRIG_SCAT5, TRIG_SCAT6, TRIG_SCAT7,
        T_SPD_MUTE, T_SPD_TURNON, T_SPD_IDLE, T_SPD_ACCEL1,
        T_SPD_ACC_CHNG, T_SPD_ACCEL2, T_SPD_IDLEXIT, T_SPD_RUN,
        T_SPD_DECEL1, T_SPD_DEC_CHNG, T_SPD_DECEL2,
        T_SPD_DEC_IDLE, T_SPD_TURNOFF, T_SPD_DEC_SP1, T_SPD_DEC_SP2, T_SPD_DIR_CHNG

    };

    String[] triggerNames = new String[]{
        "TRIG_NEVER", "TRIG_MODE_CHNG", "TRIG_MATH", "TRIG_DISTANCE", // NOI18N
        "TRIG_SPD_INC", "TRIG_SPD_DEC", "TRIG_CAM", "TRIG_NOT_TRIG", // NOI18N
        "TRIG_SF0", "TRIG_SF1", "TRIG_SF2", "TRIG_SF3", "TRIG_SF4", // NOI18N
        "TRIG_SF5", "TRIG_SF6", "TRIG_SF7", "TRIG_SF8", // NOI18N
        "TRIG_SF9", "TRIG_SF10", "TRIG_SF11", "TRIG_SF12", // NOI18N
        "TRIG_SF13", "TRIG_SF14", "TRIG_SF15", "TRIG_SF16", // NOI18N
        "TRIG_SF17", "TRIG_SF18", "TRIG_SF19", "TRIG_SF20", // NOI18N
        "TRIG_DIRNOW_CHNG", "TRIG_MOVING", "TRIG_SND_ACTV11", // NOI18N
        "TRIG_SF21", "TRIG_SF22", "TRIG_SF23", "TRIG_SF24", // NOI18N
        "TRIG_SF25", "TRIG_SF26", "TRIG_SF27", "TRIG_SF28", // NOI18N
        "TRIG_BSC0", "TRIG_BSC1", "TRIG_BSC2", "TRIG_BSC3", // NOI18N
        "TRIG_BSC4", "TRIG_BSC5", "TRIG_BSC6", "TRIG_BSC7", // NOI18N
        "TRIG_IN_0", "TRIG_IN_1", "TRIG_IN_2", "TRIG_IN_3", // NOI18N
        "TRIG_IN_4", "TRIG_IN_5", "TRIG_IN_6", "TRIG_IN_7", // NOI18N
        "TRIG_NOTCH_CHNG", "TRIG_TIME_16PPS", "TRIG_FACTORY_CVRESET", "TRIG_OPSPROGWR_CV", // NOI18N
        "TRIG_SCAT0", "TRIG_SCAT1", "TRIG_SCAT2", "TRIG_SCAT3", // NOI18N
        "TRIG_SCAT4", "TRIG_SCAT5", "TRIG_SCAT6", "TRIG_SCAT7", // NOI18N
        "T_SPD_MUTE", "T_SPD_TURNON", "T_SPD_IDLE", "T_SPD_ACCEL1", // NOI18N
        "T_SPD_ACC_CHNG", "T_SPD_ACCEL2", "T_SPD_IDLEXIT", "T_SPD_RUN", // NOI18N
        "T_SPD_DECEL1", "T_SPD_DEC_CHNG", "T_SPD_DECEL2", // NOI18N
        "T_SPD_DEC_IDLE", "T_SPD_TURNOFF", // NOI18N
        "T_SPD_DEC_SP1", "T_SPD_DEC_SP2", "T_SPD_DIR_CHNG" // NOI18N

    };

    /**
     * Human-readable form of trigger constants for use in SdfEditor
     */
    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY")  // known to be mutable, OK by convention
    String[] editorTriggerNames = new String[]{
        "TRIG_NEVER", "TRIG_MODE_CHNG", "TRIG_MATH", "TRIG_DISTANCE", // NOI18N
        "TRIG_SPD_INC", "TRIG_SPD_DEC", "TRIG_CAM", "TRIG_NOT_TRIG", // NOI18N
        "Function 0 On", "Function 1 On", "Function 2 On", "Function 3 On", // NOI18N
        "Function 4 On", "Function 5 On", "Function 6 On", "Function 7 On", // NOI18N
        "Function 8 On", "Function 9 On", "Function 10 On", "Function 11 On", // NOI18N
        "Function 12 On", "Function 13 On", "Function 14 On", "Function 15 On", // NOI18N
        "Function 16 On", "Function 17 On", "Function 18 On", "Function 19 On", // NOI18N
        "Function 20 On", // NOI18N
        "TRIG_DIRNOW_CHNG", "TRIG_MOVING", "TRIG_SND_ACTV11", // NOI18N
        "Function 21 On", "Function 22 On", "Function 23 On", "Function 24 On", // NOI18N
        "Function 25 On", "Function 26 On", "Function 27 On", "Function 28 On", // NOI18N
        "TRIG_BSC0", "TRIG_BSC1", "TRIG_BSC2", "TRIG_BSC3", // NOI18N
        "TRIG_BSC4", "TRIG_BSC5", "TRIG_BSC6", "TRIG_BSC7", // NOI18N
        "TRIG_IN_0", "TRIG_IN_1", "TRIG_IN_2", "TRIG_IN_3", // NOI18N
        "TRIG_IN_4", "TRIG_IN_5", "TRIG_IN_6", "TRIG_IN_7", // NOI18N
        "TRIG_NOTCH_CHNG", "TRIG_TIME_16PPS", "TRIG_FACTORY_CVRESET", "TRIG_OPSPROGWR_CV", // NOI18N
        "TRIG_SCAT0", "TRIG_SCAT1", "TRIG_SCAT2", "TRIG_SCAT3", // NOI18N
        "TRIG_SCAT4", "TRIG_SCAT5", "TRIG_SCAT6", "TRIG_SCAT7", // NOI18N
        "T_SPD_MUTE", "T_SPD_TURNON", "T_SPD_IDLE", "T_SPD_ACCEL1", // NOI18N
        "T_SPD_ACC_CHNG", "T_SPD_ACCEL2", "T_SPD_IDLEXIT", "T_SPD_RUN", // NOI18N
        "T_SPD_DECEL1", "T_SPD_DEC_CHNG", "T_SPD_DECEL2", // NOI18N
        "T_SPD_DEC_IDLE", "T_SPD_TURNOFF", // NOI18N
        "T_SPD_DEC_SP1", "T_SPD_DEC_SP2", "T_SPD_DIR_CHNG" // NOI18N

    };

///============================BREAK/LOOP logic during a .WAV playback==================
// WAVBRK modify flags
    int loop_STD = 0x00; // default is NO loop logic modification
    int loop_GLOBAL = 0x10; // assert to loop and UP 1 level...
    int loop_INVERT = 0x08; // invert/FALSE POL of loop to generate WAV BREAK condition

    int[] wavebrkCodes = new int[]{
        loop_GLOBAL, loop_INVERT
    };
    int[] wavebrkMasks = wavebrkCodes;

    String[] wavebrkNames = new String[]{
        "loop_GLOBAL", "loop_INVERT", "loop_STD" // NOI18N
    };

    int loop_GLOBAL_BIT = 4; // bit # assert to loop and UP 1 level...
    int loop_INVERT_BIT = 3; // bit # invert (NOT/FALSE) Polarity of loop condition/evaluation

// --------- 32 loop event codes
    int loop_MASK = 0x1F; // mask for VALID BRK rng-5bits

// disable loop- just run/playback .WAV file from SOUND flash or other source
    int no_loop = 0; // RUN to completeion of WAV segmenT

//--------------
// codes that imply LOOPING of current HANDLE WAV seg till loop WAV BREAK CONDITON met
    int loop_till_NEVER = TRIG_NEVER; // same as "no_loop", RUN to completion of WAV segmenT

    int loop_till_cam = TRIG_CAM; // loop on CAM event, either H/W input or AUTO-chuff,
    int loop_till_init_TRIG = TRIG_NOT_TRIG; // loop until INITIATE TRIGGER condition NOT met

// FUNCTION change events
    int loop_till_F1 = TRIG_SF1; // F1 change event
    int loop_till_F2 = TRIG_SF2;
    int loop_till_F3 = TRIG_SF3;
    int loop_till_F4 = TRIG_SF4;

    int loop_till_F0 = TRIG_SF0; // headlight
    int loop_till_DIRNOW_CHNG = TRIG_DIRNOW_CHNG; // have DIR_NOW changed
    int loop_till_MOVING = TRIG_MOVING; // SPD = Non-Zero
    int loop_till_SND_ACTV11 = TRIG_SND_ACTV11; // DECODER is addressed within CV11 time

    int loop_till_F5 = TRIG_SF5;
    int loop_till_F6 = TRIG_SF6;
    int loop_till_F7 = TRIG_SF7;
    int loop_till_F8 = TRIG_SF8;

    int loop_till_F9 = TRIG_SF9;
    int loop_till_F10 = TRIG_SF10;
    int loop_till_F11 = TRIG_SF11;
    int loop_till_F12 = TRIG_SF12;

// ONLY scatter CHNLS 4-7 [also WORK visibles] can be used for TIMED loop break conditions
    int loop_till_SCAT4 = 0x1C; // scatter CHNL4, phase A/B selected by loop_INVERT_BIT
    int loop_till_SCAT5 = 0x1D;
    int loop_till_SCAT6 = 0x1E;
    int loop_till_SCAT7 = 0x1F;

    int[] loopCodes = new int[]{
        loop_till_cam, loop_till_init_TRIG, // loop_till_NEVER is same as no_loop
        loop_till_DIRNOW_CHNG, loop_till_MOVING, loop_till_SND_ACTV11,
        loop_till_F0,
        loop_till_F1,
        loop_till_F2, loop_till_F3, loop_till_F4, loop_till_F5,
        loop_till_F6, loop_till_F7, loop_till_F8, loop_till_F9,
        loop_till_F10, loop_till_F11, loop_till_F12,
        loop_till_SCAT4, loop_till_SCAT5, loop_till_SCAT6, loop_till_SCAT7
    };
    int[] loopMasks = loopCodes;

    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY")  // known to be mutable, OK by convention
    String[] loopNames = new String[]{
        "loop_till_cam", "loop_till_init_TRIG", // NOI18N
        "loop_till_DIRNOW_CHNG", "loop_till_MOVING", "loop_till_SND_ACTV11", // NOI18N
        "loop_till_F0", // NOI18N
        "loop_till_F1", // NOI18N
        "loop_till_F2", "loop_till_F3", "loop_till_F4", "loop_till_F5", // NOI18N
        "loop_till_F6", "loop_till_F7", "loop_till_F8", "loop_till_F9", // NOI18N
        "loop_till_F10", "loop_till_F11", "loop_till_F12", // NOI18N
        "loop_till_SCAT4", "loop_till_SCAT5", "loop_till_SCAT6", "loop_till_SCAT7", // NOI18N
        "no_loop" // NOI18N
    };

//----------------
    int DELAY_GLOBAL = 0x01;
    int DELAY_CV = 0x80; // delay fron SND_CV range
    int DELAY_THIS = 0x00; // immed data

//----------
// for TEST trigger OPC
    int TRIG_TRUE = 0x01;
    int TRIG_FALSE = 0x00;
    int ABOVE = 0x02; // this bit forces eval above on SPD state trigs

    int[] trigLogicCodes = new int[]{
        TRIG_TRUE
    };
    int[] trigLogicMasks = loopCodes;

    String[] trigLogicNames = new String[]{
        "TRIG_TRUE", "TRIG_FALSE" // NOI18N
    };

    int SK_SENS_BIT = 0;
    int ABOVE_BIT = 1;

//=====================================================================
// load MODIFY COMMAND arg/VALUES- allows USER maximal configurability/customization in SDF files
// 4byte CMD  1110-mmmm, ARG1,ARG2,ARG3
//---------------------
// modifier control REG command types
// mmmm target embedded in LOAD MODIFY CMD to direct what following 3 ARG bytes will be loaded TO
    int MTYPE_TIME = 0; // MODIFY timer
    int MTYPE_GAIN = 1; // setup a GAIN modify task for current SOUND chain
    int MTYPE_PITCH = 2; // setup a PITCH modify task for current SOUND chain
    int MTYPE_BLEND = 3; // setup BLEND logic task

// undefined NOW
    int MTYPE_SCATTER = 4; // preset a SCATTER channel/task
    int MTYPE_SNDCV = 5; // modify under MASK SNDCV,ARG1=SNDCV#,ARG2=DATA,ARG3=1bit=chng mask
    int MTYPE_WORK_IMMED = 6; // modify WORK reg immed data
    int MTYPE_WORK_INDIRECT = 7; // modify WORK reg indirect from SCV [selectable sound CV] or other WORK reg

    int[] modControlCodes = new int[]{
        MTYPE_TIME, MTYPE_GAIN, MTYPE_PITCH, MTYPE_BLEND,
        MTYPE_SCATTER, MTYPE_SNDCV, MTYPE_WORK_IMMED, MTYPE_WORK_INDIRECT
    };
    String[] modControlNames = new String[]{
        "MTYPE_TIME", "MTYPE_GAIN", "MTYPE_PITCH", "MTYPE_BLEND", // NOI18N
        "MTYPE_SCATTER", "MTYPE_SNDCV", "MTYPE_WORK_IMMED", "MTYPE_WORK_INDIRECT" // NOI18N
    };

// ---------------
// values for MATH functions on WORK regs
    int FMATH_LODE = 0x00; // load DATA,,MATH_FLAG SET shows whole WORK is ZERO- DEFAULT task
    int FMATH_AND = 0x20; // use to CLR bit(s),MATH_FLAG SET shows whole WORK is ZERO
    int FMATH_OR = 0x40; // use to SET  bit(S), MATH FLAG SET shows whole WORK is 1's
    int FMATH_XOR = 0x60; // use to flip bit(s), MATH FLAG SET if all flipped bits are now 0

//;FMATH_ADD EQU 0x80 ;add signed value  (commented out in original Digitrax file)
    int FMATH_INTEGRATE = 0xA0; // add signed value to WORK,MATH_FLAG SET if over/underflo,or clamp LIMIT
    int FMATH_TEST_ZERO = 0xC0; // see if WORK under MASK is ZERO, if ZERO set MATH bit

// -----1111----------------
// FIRST modify ARG- COMMAND
    int GCMD_NONE = 0x00; // NO gain MODIFY task RUN
    int GCMD_MASK = 0xE0; // 3 ms bits 1st CMD decode

    static final int[] arg1ModCodes = new int[]{
        FMATH_LODE, FMATH_AND, FMATH_OR, FMATH_XOR, FMATH_INTEGRATE, FMATH_TEST_ZERO, GCMD_MASK
    };
    static final String[] arg1ModNames = new String[]{
        "FMATH_LODE", "FMATH_AND", "FMATH_OR", "FMATH_XOR", "FMATH_INTEGRATE", "FMATH_TEST_ZERO", "GCMD_MASK" // NOI18N
    };

// 32 analog ARGS 3/21
// TYPE 8 GAIN
    int IMMED_GAIN_MODIFY = 0x80; // set new GAIN to [CV# 6 bit offset in ARG1] *CV58 , CV120 base
    int ANALOG_GAIN_MODIFY = 0xA0; // modify INIT GAIN by analog chnl in 5 ls bits
    // AUX1=  AUX2=dither coding

    int FCMD_NONE = 0x00; // NO PITCH MODIFY task RUN
    int FCMD_MASK = 0xE0; // 3 ms bits 1st CMD decode

// type 8 PITCH
    int CV_PITCH_MODIFY = 0x80; // set new PITCH to [CV# 6 bit offset in ARG1] , CV120 base
    int ANALOG_PITCH_MODIFY = 0xA0; // modify INIT PITCH by analog chnl in 5 ls bits
    // AUX1=  AUX2=dither coding

//==================
// 32 predefined user visible WORK/STATE regs, use these defined values
// first 16 WORK visible reg address codes.
    int WORK_SPEED = 0x00; // SOUND target PWM/SPD
    int WORK_NOTCH = 0x01; // CHNL# for NOTCH Modified spd info, MODUL0 8
    int WORK_SERVO = 0x02;
    int WORK_MVOLTS = 0x03;

    int WORK_USER_LINES = 0x05;
    int WORK_TIMEBASE = 0x06; // 64mS cntr, ROLL=16 SECS
    int WORK_STATUS_BITS = 0x07; // PRIMARY decoder STATUS bits

    int WORK_GLBL_GAIN = 0x08; // FULL gain authority
    int WORK_GAIN_TRIM = 0x09; // signed (bit7) gain trim +/- 25%
    int WORK_PITCH_TRIM = 0x0A; // signed (bit7) pitch trim +/- 25%
    int WORK_SPEED_DELTA = 0x0B; // unsigned SPEED change

// SECOND 16 WORK  visible reg address codes
    int WORK_SCATTER4 = 0x10; // VISIBLE working SCATTER reg
    int WORK_SCATTER5 = 0x11; // VISIBLE working SCATTER reg
    int WORK_SCATTER6 = 0x12; // VISIBLE working SCATTER reg
    int WORK_SCATTER7 = 0x13; // VISIBLE working SCATTER reg

    int WORK_ACHNL_7F = 0x14; // DT400 VARIABLE whistle CHNL
    int WORK_ACHNL_7E = 0x15;
    int WORK_SKAT_FAST = 0x16;
    int WORK_SKAT_SLOW = 0x17;

    int WORK_DISTANCE = 0x18;
    int WORK_PEAK_SPD = 0x19;
    int WORK_USER_0 = 0x1A; // user  REG
    int WORK_USER_1 = 0x1B; // user  REG

    int WORK_USER_2 = 0x1C; // user  REG
    int WORK_USER_3 = 0x1D; // user  REG
    int WORK_USER_4 = 0x1E; // user  REG
    int WORK_USER_5 = 0x1F; // user  REG

    int[] workRegCodes = new int[]{
        WORK_SPEED, WORK_NOTCH, WORK_SERVO, WORK_MVOLTS,
        WORK_USER_LINES, WORK_TIMEBASE, WORK_STATUS_BITS,
        WORK_GLBL_GAIN, WORK_GAIN_TRIM, WORK_PITCH_TRIM, WORK_SPEED_DELTA,
        WORK_SCATTER4, WORK_SCATTER5, WORK_SCATTER6, WORK_SCATTER7,
        WORK_ACHNL_7F, WORK_ACHNL_7E, WORK_SKAT_FAST, WORK_SKAT_SLOW,
        WORK_DISTANCE, WORK_PEAK_SPD, WORK_USER_0, WORK_USER_1,
        WORK_USER_2, WORK_USER_3, WORK_USER_4, WORK_USER_5
    };

    String[] workRegNames = new String[]{
        "WORK_SPEED", "WORK_NOTCH", "WORK_SERVO", "WORK_MVOLTS", // NOI18N
        "WORK_USER_LINES", "WORK_TIMEBASE", "WORK_STATUS_BITS", // NOI18N
        "WORK_GLBL_GAIN", "WORK_GAIN_TRIM", "WORK_PITCH_TRIM", "WORK_SPEED_DELTA", // NOI18N
        "WORK_SCATTER4", "WORK_SCATTER5", "WORK_SCATTER6", "WORK_SCATTER7", // NOI18N
        "WORK_ACHNL_7F", "WORK_ACHNL_7E", "WORK_SKAT_FAST", "WORK_SKAT_SLOW", // NOI18N
        "WORK_DISTANCE", "WORK_PEAK_SPD", "WORK_USER_0", "WORK_USER_1", // NOI18N
        "WORK_USER_2", "WORK_USER_3", "WORK_USER_4", "WORK_USER_5" // NOI18N
    };

// ---------------------
// WORK_STATUS_BITS register bit definitions. data is VOLATILE
    int WKSB_ANALOG_BIT = 0; // 1=is in ANALOG mode [STATUS mirror]
    int WKSB_ANALOG_MASK = 0x01; // bit involved

    int WKSB_DIRNOW_BIT = 1; // 1= rev direction ?[STATUS mirror]
    int WKSB_DIRNOW_MASK = 0x02; // bit involved

    int WKSB_RUN_BIT = 2; // 0= stop motor PWM, non-primemover sounds RUN
    int WKSB_RUN_MASK = 0x04; // bit involved

    int WKSB_ACEL_BIT = 3; // 1= is ACCEL state
    int WKSB_ACEL_MASK = 0x08; // bit involved

    int WKSB_SPDDELTA_BIT = 4; // 1= change SPD by unsigned SPD_DELTA work REG only
    int WKSB_SPDDELTA_MASK = 0x10; // bit involved

    int WKSB_MATH_BIT = 7; // result from last MODIFY math action (can test with
    int WKSB_MATH_MASK = 0x80; // bit involved

    int[] workStatusBitCodes = new int[]{
        WKSB_ANALOG_MASK, WKSB_DIRNOW_MASK, WKSB_RUN_MASK, WKSB_ACEL_MASK, WKSB_SPDDELTA_MASK, WKSB_MATH_MASK
    };
    String[] workStatusBitNames = new String[]{
        "WKSB_ANALOG_MASK", "WKSB_DIRNOW_MASK", "WKSB_RUN_MASK", "WKSB_ACEL_MASK", "WKSB_SPDDELTA_MASK", "WKSB_MATH_MASK" // NOI18N
    };

//========================
// SCATTER commands as arg bytes in MTYPE_SCATTER
// is MODIFY SCTR command  -  1110-0100, cccc-XAAA, aaaa-aaaa, bbbb-bbbb, AAA is 1 of 8 scat tasks/chnls
// is SCTR_PERIOD command  -  1110-0100, 0001-PAAA, Srrr-rrrr, IIIw-wwww,
// AAA is 1 of 8 scat tasks/chnls,  P=WORK scatter POLARITY,
// S=1 is SOUNDCV src  [0x80=CV141 etc], S=0 rrr-rrrr is RATE in approx 1 sec counts, rate=0 is CNTR hold...
// III=scatter intensity- 000=no SCATTER, w-wwww is a WORK reg# as SCATTER vary data src
// cccc is 16 SCATTER command modes, dddd and eeee are ARG Regs for run modes
    int SCAT_CMD_PERIOD = 0x20; // command for PERIODIC event, SCALABLE scatter speedup on WORK reg INC
    int SCAT_CMD_PERIOD_REV = 0x28; // command for PERIODIC event, with SCALABLE scatter slowdn on WORK reg INC

    int SCAT_CMD_SAWTOOTH = 0x30;
    int SCAT_CMD_SAWTOOTH_REV = 0x38;

    int[] scatCommandCodes = new int[]{
        SCAT_CMD_PERIOD, SCAT_CMD_PERIOD_REV, SCAT_CMD_SAWTOOTH, SCAT_CMD_SAWTOOTH_REV};
    String[] scatCommandNames = new String[]{
        "SCAT_CMD_PERIOD", "SCAT_CMD_PERIOD_REV", "SCAT_CMD_SAWTOOTH", "SCAT_CMD_SAWTOOTH_REV"}; // NOI18N

    int SCAT_PERIOD_POLARITY_BIT = 3; // 1= reverse WORK influence
    int SKATTER_INCREMENT = 8; // phase INCREMENT

    int SCAT_CHNL0 = 0x00; // SCATTER task0
    int SCAT_CHNL1 = 0x01; // SCATTER task1
    int SCAT_CHNL2 = 0x02; // SCATTER task2
    int SCAT_CHNL3 = 0x03; // SCATTER task3
    int SCAT_CHNL4 = 0x04; // SCATTER task4- visible WORK reg
    int SCAT_CHNL5 = 0x05; // SCATTER task5- visible WORK reg
    int SCAT_CHNL6 = 0x06; // SCATTER task6- visible WORK reg
    int SCAT_CHNL7 = 0x07; // SCATTER task7- visible WORK reg

    int[] scatChannelCodes = new int[]{
        SCAT_CHNL0, SCAT_CHNL1, SCAT_CHNL2, SCAT_CHNL3, SCAT_CHNL4, SCAT_CHNL5, SCAT_CHNL6, SCAT_CHNL7};
    String[] scatChannelNames = new String[]{
        "SCAT_CHNL0", "SCAT_CHNL1", "SCAT_CHNL2", "SCAT_CHNL3", "SCAT_CHNL4", "SCAT_CHNL5", "SCAT_CHNL6", "SCAT_CHNL7"}; // NOI18N

    int SINTEN_IMMED = 0xE0; // use WORK# as immediate data
    int SINTEN_HIGH = 0xC0;
    int SINTEN_MID = 0x60; // mid scatter intensity
    int SINTEN_LOW = 0x40;
    int SINTEN_MIN = 0x20;
    int SINTEN_OFF = 0x00;

    int[] sintenCodes = new int[]{
        SINTEN_IMMED, SINTEN_HIGH, SINTEN_MID, SINTEN_LOW, SINTEN_MIN, SINTEN_OFF};
    String[] sintenNames = new String[]{
        "SINTEN_IMMED", "SINTEN_HIGH", "SINTEN_MID", "SINTEN_LOW", "SINTEN_MIN", "SINTEN_OFF"}; // NOI18N

// ======================
    int DEFAULT_GLBL_GAIN = 0xC0;
    int MERGE_ALL_MASK = 0; // any 0 bit is involved

// ----2222--------------
// 2ND modify ARG
    int SNDCV_SRC = 0x80; // this bit set chngs from IMMED to SND_CV to control EFFECT span
    int SNDCV_SRC_BIT = 7; // 1=ls 7 bits SCV#, 0=ls 7 bits immed arg data

// ls 7 bit ARGS
    int MAXG_NONE = 0x00; // value of 0 means no scaling, SRC is 100% GAIN
    int MAXP_NONE = 0x00; // value of 0 means no scaling, SRC is 100% PITCH

// ----3333--------------
// 3RD modify ARG
//;MPUSH  EQU 0x80  ;this bit forces LEVEL OVERIDE
    int DITHER = 0x80; // value to change to DITHER mode
    int SUMG = 0x40;

    int MIN_DITH = 0x00;
    int LOW_DITH = 0x20;
    int MID_DITH = 0x40;
    int MAX_DITH = 0x60;

// ;MPUSH_BIT EQU 7  (commendted out in the original Digitrax file)
    int DITHER_BIT = 7;
    int SUM_BIT = 6; // 1=add ARG1/2 variation,0=multiply
    int MG1_BIT = 5; // these code meaning of LO nibble
    int MG0_BIT = 4;

// for CMDS 8/9 ls nibble ARG3 is scaling factor to set CV/IMMED data control SPAN
    int SCALE_F = 0x0F;
    int SCALE_C = 0x0C;
    int SCALE_8 = 0x08;
    int SCALE_6 = 0x06;
    int SCALE_5 = 0x05;
    int SCALE_4 = 0x04;
    int SCALE_2 = 0x02;

    int[] arg3ModCodes = new int[]{
        DITHER,
        LOW_DITH, MID_DITH, MAX_DITH, // MIN_DITH is zero, a no-op
        SCALE_F, SCALE_C, SCALE_8, SCALE_6, SCALE_5, SCALE_4, SCALE_2
    };
    int[] arg3ModMasks = new int[]{
        DITHER,
        0x60, 0x60, 0x60,
        0x0F, 0x0F, 0x0F, 0x0F, 0x0F, 0x0F, 0x0F
    };
    String[] arg3ModNames = new String[]{
        "DITHER", // NOI18N
        "LOW_DITH", "MID_DITH", "MAX_DITH", // NOI18N
        "SCALE_F", "SCALE_C", "SCALE_8", "SCALE_6", "SCALE_5", "SCALE_4", "SCALE_2" // NOI18N
    };

// DITHER ????
    int DITHERG_WHISTLE = 0x00;
    int DITHERP_WHISTLE = 0x00;

    int DITHERG_DIESEL = 0x00;
    int DITHERP_DIESEL = 0x00;

    int DITHERG_NONE = 0x00;
    int DITHERP_NONE = 0x00;

// THE FOLLOWING ARE PRESENT, BUT WITH CURRENT VALUES ARE NOT PARTICULARLY USEFUL
    int[] ditherGCodes = new int[]{
        DITHERG_DIESEL, DITHERG_WHISTLE, DITHERG_NONE
    };
    String[] ditherGNames = new String[]{
        "DITHERG_DIESEL", "DITHERG_WHISTLE", "DITHERG_NONE" // NOI18N
    };
    int[] ditherPCodes = new int[]{
        DITHERP_DIESEL, DITHERP_WHISTLE, DITHERP_NONE
    };
    String[] ditherPNames = new String[]{
        "DITHERP_DIESEL", "DITHERP_WHISTLE", "DITHERP_NONE" // NOI18N
    };

// BLEND logic commnds
// 1110-BLEND, BLEND_CMDS, BLEND_GAIN_ARG, BLEND_FASE_ARG
// BLEND cmds bit encoding
    int BLEND_TGTMASK = 0xC0; // 2 ms bits control Blend target
    int BLEND_FASEMASK = 0x38; // 3  bits FASE_BLEND type, 000=inactive
    int BLEND_GAINMASK = 0x07; // 3 ls bits GAIN_BLEND type, 000=inactive

// MODE0 with ARG=0 is INACTIVE BLEND
    int BLEND_CURRENT_CHNL = 0x00 & BLEND_TGTMASK;
    int BLEND_ALL = 0x40 & BLEND_TGTMASK;

    int BLEND_GAIN0 = 0x00 & BLEND_GAINMASK; // STD logic 0 blend
    int BLEND_GAIN1 = 0x01 & BLEND_GAINMASK; // alt logic 1 blend

    int BLEND_FASE0 = 0x00 & BLEND_FASEMASK; // STD logic 0 blend
    int BLEND_FASE1 = 0x08 & BLEND_FASEMASK; // alt logic 1 blend

    int[] blendArg1Codes = new int[]{
        BLEND_CURRENT_CHNL, BLEND_ALL,
        BLEND_GAIN0, BLEND_GAIN1,
        BLEND_FASE0, BLEND_FASE1
    };
    int[] blendArg1Masks = new int[]{
        BLEND_TGTMASK, BLEND_TGTMASK,
        BLEND_GAINMASK, BLEND_GAINMASK,
        BLEND_FASEMASK, BLEND_FASEMASK
    };
    String[] blendArg1Names = new String[]{
        "BLEND_CURRENT_CHNL", "BLEND_ALL", // NOI18N
        "BLEND_GAIN0", "BLEND_GAIN1", // NOI18N
        "BLEND_FASE0", "BLEND_FASE1" // NOI18N
    };

    int BLENDG_DSL_ACCEL0 = 0x04; // GAIN rate for loaded DIESEL
    int BLENDF_DSL_ACCEL0 = 0x02; // FASE rate for loaded DIESEL

    int BLENDG_DSL_ACCEL1 = 0x06; // GAIN rate for loaded DIESEL
    int BLENDF_DSL_ACCEL1 = 0x05; // GAIN rate for unloading DIESEL

    int BLENDG_DSL_DECEL0 = 0x09; // GAIN rate for unloading DIESEL
    int BLENDF_DSL_DECEL0 = 0x07; // FASE rate for unloading DIESEL

    int[] blendArg2Codes = new int[]{
        BLENDG_DSL_ACCEL0, BLENDG_DSL_ACCEL1, BLENDG_DSL_ACCEL1};
    String[] blendArg2Names = new String[]{
        "BLENDG_DSL_ACCEL0", "BLENDG_DSL_ACCEL1", "BLENDG_DSL_ACCEL1"}; // NOI18N
    int[] blendArg3Codes = new int[]{
        BLENDF_DSL_ACCEL0, BLENDF_DSL_ACCEL1, BLENDF_DSL_ACCEL1};
    String[] blendArg3Names = new String[]{
        "BLENDF_DSL_ACCEL0", "BLENDF_DSL_ACCEL1", "BLENDF_DSL_ACCEL1"}; // NOI18N

// COMPARE CMD control bits
    int TARGET_DATA = 0x00; // second COMPARE ARG is WORK reg or SCV
    int IMMED_DATA = 0x04; // second COMPARE ARG is IMMEDIATE 8 bit DATA

    int SKIP_SAME = 0x00;
    int SKIP_RSVD = 0x01; //reserved CONDITION code
    int SKIP_LESS = 0x02;
    int SKIP_GRTR = 0x03;

    int COMP_ALL = 0x00; // 1 bits= do not include
    int COMP_7LSB = 0X80;

// define fixed CV locations
    int SNDCV_CONFIGA = 0x80; // CV129= config byte
    int SNDCV_CONFIGB = 0x81; //CV130= config byte
    int SCV_DCONFIG = 0x82; // CV131= diesel config
    int SCV_NOTCH = 0x83; // CV132= typ DIESEL NOTCH rate byte
    int SNDCV_STEAM = 0x84; // CV133= typ steam CAM config byte, x80=EXT or 1-127=DRIVER dia in inches
    int SCV_STGEAR = 0x85; // CV134= steam gear ratio trim
    int SCV_MUTE_VOL = 0x86; // CV135= vol level when MUTE action is triggered, e.g. F8=ON
    int SCV_MAIN_PITCH = 0x87; // CV136
    int SCV_137 = 0x88;
    int SCV_138 = 0x89; // SCV138=
    int SCV_DISTANCE_RATE = 0x8A; // SCV139= mask for controlling DISTANCE rate event/trigger
    int SCV_FREEFORM = 0x8B; // SCV_140, here the SCV's are SDF defined

    static final int[] fixedCVCodes = new int[]{
        SNDCV_CONFIGA, SNDCV_CONFIGB, SCV_DCONFIG, SCV_NOTCH,
        SNDCV_STEAM, SCV_STGEAR, SCV_MUTE_VOL, SCV_MAIN_PITCH,
        SCV_137, SCV_138, SCV_DISTANCE_RATE, SCV_FREEFORM
    };
    static final String[] fixedCVNames = new String[]{
        "SNDCV_CONFIGA", "SNDCV_CONFIGB", "SCV_DCONFIG", "SCV_NOTCH", // NOI18N
        "SNDCV_STEAM", "SCV_STGEAR", "SCV_MUTE_VOL", "SCV_MAIN_PITCH", // NOI18N
        "SCV_137", "SCV_138", "SCV_DISTANCE_RATE", "SCV_FREEFORM" // NOI18N
    };

// CV133, SNDCV_STEAM /DRIVER size bit7=1 means EXTERNAL INPUT_0 generates a special CAM code as well
    static int STEAM_CAM_BIT = 7;

// =======end of FIXED DATA assignments==============
// =================
// USER choices
// SCAT_AIRCOMP_RATE EQU 25  ;AIR compressor about 2 mins, above 128 is SNDCV foR RATE ARG
// SCAT_DRIER_RATE  EQU 10  ;about 10 secs for DRIER
// SCAT_WATERPUMP_RATE EQU 100
// SCAT_COAL_RATE  EQU 120
// specific DFLT values to scale MAX ranges
    int MAXG_WHISTLE = 0x6A;
    int MAXP_WHISTLE = 0x07;
    int MAXG_DIESEL = 0x1C;
    int MAXP_DIESEL = 0x3E;
    int MAXG_STEAM = 0x1C;
    int MAXP_STEAM = 0x7E; // was 3E- make more explosive!

// VALUES ARE SUCH THAT WE CAN'T TELL STEAM FROM DIESEL WHEN DISASSMEMLING
    int[] maxPCodes = new int[]{
        MAXP_WHISTLE, MAXP_DIESEL, MAXP_STEAM
    };
    String[] maxPNames = new String[]{
        "MAXP_WHISTLE", "MAXP_DIESEL", "MAXP_STEAM" // NOI18N
    };

    int[] maxGCodes = new int[]{
        MAXG_WHISTLE, MAXG_DIESEL, MAXG_STEAM
    };
    String[] maxGNames = new String[]{
        "MAXG_WHISTLE", "MAXG_DIESEL", "MAXG_STEAM" // NOI18N
    };

}
