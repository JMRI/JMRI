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
    final static int TRUE = 1;
    final static int FALSE = 0;
    final static int[] trueFalseCodes = new int[]{TRUE, FALSE};
    final static String[] trueFalseNames = new String[]{"TRUE", "FALSE"}; // NOI18N

// TRIGGER values
// sound TRIGGER PREMPT control bits
// SPECIAL trigs: POL,0,0,0-0,s,s,s  0-7 are special trigs
// FN trigger format: POL,0,0,B-B,b,b,b    BB 1 of 3 bytes 01,10,11 (24 fns), bbb is bit adr#
    final static int NORMAL = 0;   // (complete .WAV frag/preempt higher/trigger true)

    final static int ZAP = 0x01; // value for ZAP on PREMPT
    final static int RUN_WHILE_TRIG = 0x02; // mask/binary values
    final static int NO_PREEMPT_TRIG = 0x04;
    final static int NOT_TRIG = 0x80; // 12/13/04, in TRIG argument now

// 3 ls bits of INITIATE CMD 1ST byte
    final static int CMD_ZAP_BIT = 0; // 1=END immed if PREMPTED, 0=finish current .wav frag before END
    final static int RUN_WHILE_TRIG_BIT = 1; // 1=persistent while TRIGGER is valid, 0=run once
    final static int NO_PREEMPT_TRIG_BIT = 2;

    final static int NOT_TRIG_BIT = 7; // POL bit in TRIG byte 1=reverse POL

    final static int[] premptCodes = new int[]{ZAP, RUN_WHILE_TRIG, NO_PREEMPT_TRIG, NOT_TRIG}; // NORMAL matches all (is zero), so left off
    final static int[] premptMasks = premptCodes;             // just check 1 bits
    final static String[] premptNames = new String[]{"ZAP", "RUN_WHILE_TRIG", "NO_PREEMPT_TRIG", "NOT_TRIG", "NORMAL"}; // NOI18N

//----------
// TRIGGER initiate CODES 
    final static int TRIG_NEVER = 0x00; // 0 is INACTIVE entry (NULL,not a valid INITIATE coding)
    final static int TRIG_MODE_CHNG = 0x01;
    final static int TRIG_MATH = 0x02; // math result trigger
    final static int TRIG_DISTANCE = 0x03; // have new increment of distance, use for FUEL low alarm, maintenance ETC

    final static int TRIG_SPD_INC = 0x04; // have SPD increase
    final static int TRIG_SPD_DEC = 0x05; // have SPD decrease
    final static int TRIG_CAM = 0x06; // TRIG on CAM event, either H/W input or AUTO-chuff,
    final static int TRIG_NOT_TRIG = 0x07; // brk/loop SPECIAL, a.k.a. "loop_till_init_TRIG"

// -------
// F1-F28 and F0 map into 3 BYTES of static trig state/fn state bits
// these triggers preCODED for fast TRIG bit adr   POL,0,BBB,bbb:   BB=byte,bbb=bit adr
// Mapped to internal allocations
    final static int MOVE_BIT = 6;    // moving BIT
    final static int MVFN_MASK = 0x1F; // direct FN bits

    final static int TRIG_SF1 = 0x08; // F1 change event
    final static int TRIG_SF2 = 0x09; // byte 1, bit1
    final static int TRIG_SF3 = 0x0A;
    final static int TRIG_SF4 = 0x0B;

    final static int TRIG_SF0 = 0x0C; // headlight/F0 state
    final static int TRIG_DIRNOW_CHNG = 0x0D; // have DIR_NOW changed
    final static int TRIG_MOVING = 0x0E; // SPD = Non-Zero
    final static int TRIG_SND_ACTV11 = 0x0F; // DECODER is SPD addressed within CV11 time

// ----------
    final static int TRIG_SF5 = 0x10; // byte 2,bit0
    final static int TRIG_SF6 = 0x11;
    final static int TRIG_SF7 = 0x12;
    final static int TRIG_SF8 = 0x13;

    final static int TRIG_SF13 = 0x14; // BYTE 2, bit 4, 
    final static int TRIG_SF14 = 0x15;
    final static int TRIG_SF15 = 0x16;
    final static int TRIG_SF16 = 0x17;

// ---------
    final static int TRIG_SF9 = 0x18; // byte 3 bit 0
    final static int TRIG_SF10 = 0x19;
    final static int TRIG_SF11 = 0x1A;
    final static int TRIG_SF12 = 0x1B;

    final static int TRIG_SF17 = 0x1C; // byte 3, bit 4
    final static int TRIG_SF18 = 0x1D;
    final static int TRIG_SF19 = 0x1E;
    final static int TRIG_SF20 = 0x1F;

//
    final static int TRIG_SF21 = 0x20; // expanded FUNCTIONS
    final static int TRIG_SF22 = 0x21;
    final static int TRIG_SF23 = 0x22;
    final static int TRIG_SF24 = 0x23;

    final static int TRIG_SF25 = 0x24;
    final static int TRIG_SF26 = 0x25;
    final static int TRIG_SF27 = 0x26;
    final static int TRIG_SF28 = 0x27;

//
//----------------FIRST 8 bsc regs reserved
    final static int TRIG_BSC0 = 0x28; // expanded FUNCTIONS
    final static int TRIG_BSC1 = 0x29;
    final static int TRIG_BSC2 = 0x2A;
    final static int TRIG_BSC3 = 0x2B;

    final static int TRIG_BSC4 = 0x2C;
    final static int TRIG_BSC5 = 0x2D;
    final static int TRIG_BSC6 = 0x2E;
    final static int TRIG_BSC7 = 0x2F;

//------------------
//these trig lines both REPORT the external inputs to decoder, and CAN also be SET/RESET by SDF trigger commands
// and can be read back by TRANSPONDING as external ALARMS etc
    final static int TRIG_IN_BASE = 0x50;

    final static int TRIG_IN_0 = TRIG_IN_BASE + 0; // input 0 trig, CAM input if not assigned to STEAM chuf
    final static int TRIG_IN_1 = TRIG_IN_BASE + 1;
    final static int TRIG_IN_2 = TRIG_IN_BASE + 2;
    final static int TRIG_IN_3 = TRIG_IN_BASE + 3;

    final static int TRIG_IN_4 = TRIG_IN_BASE + 4;
    final static int TRIG_IN_5 = TRIG_IN_BASE + 5;
    final static int TRIG_IN_6 = TRIG_IN_BASE + 6;
    final static int TRIG_IN_7 = TRIG_IN_BASE + 7;

    final static int TRIG_NOTCH_CHNG = TRIG_IN_7 + 1;   // when notch changes
    final static int TRIG_TIME_16PPS = TRIG_IN_7 + 2;   // 16 per sec/64mS rate
    final static int TRIG_FACTORY_CVRESET = TRIG_IN_7 + 3; // have CV8=8/9 request, USER definable CV values
    final static int TRIG_OPSPROGWR_CV = TRIG_IN_7 + 4; // have OPSPROG action

//--------------- SCATTER TRIGGER codes, ON/OFF phase per SCATTER task
    final static int SCAT_TRIG_BASE = 0x60;

    final static int TRIG_SCAT0 = SCAT_TRIG_BASE + 0; // scatter CHNL0, phase A sel NOT_TRIG_BIT
    final static int TRIG_SCAT1 = SCAT_TRIG_BASE + 1;
    final static int TRIG_SCAT2 = SCAT_TRIG_BASE + 2;
    final static int TRIG_SCAT3 = SCAT_TRIG_BASE + 3;
    final static int TRIG_SCAT4 = SCAT_TRIG_BASE + 4; // has visible WORK register
    final static int TRIG_SCAT5 = SCAT_TRIG_BASE + 5; // has visible WORK register
    final static int TRIG_SCAT6 = SCAT_TRIG_BASE + 6; // has visible WORK register
    final static int TRIG_SCAT7 = SCAT_TRIG_BASE + 7; // has visible WORK register

// ===============================
//  encoded TRIGGER values related to SPEED_STATE code
    final static int T_SS_BASE = 0x70; // 0x20 ;base TRIG code for this STATE logic
    final static int TSPD = T_SS_BASE;  // base TRIG code for this STATE logic

    final static int T_SPD_MUTE = TSPD + 0;
    final static int T_SPD_TURNON = TSPD + 1;
    final static int T_SPD_IDLE = TSPD + 2;
    final static int T_SPD_ACCEL1 = TSPD + 3;

    final static int T_SPD_ACC_CHNG = TSPD + 4;  // 1st GEN parallel/series chng
    final static int T_SPD_ACCEL2 = TSPD + 5;
    final static int T_SPD_IDLEXIT = TSPD + 6;  // leaving IDLE state
    final static int T_SPD_RUN = TSPD + 7;

    final static int T_SPD_DECEL1 = TSPD + 8;
    final static int T_SPD_DEC_CHNG = TSPD + 9;
    final static int T_SPD_DECEL2 = TSPD + 10;
// T_SPD_DIR_CHNG =     TSPD+11   ;       (Commented in original Digitrax file)

    final static int T_SPD_DEC_IDLE = TSPD + 11;
    final static int T_SPD_TURNOFF = TSPD + 12;
    final static int T_SPD_DEC_SP1 = TSPD + 13;
    final static int T_SPD_DEC_SP2 = TSPD + 14;
    final static int T_SPD_DIR_CHNG = TSPD + 15;

    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY")  // known to be mutable, OK by convention
    final static int[] triggerCodes = new int[]{
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

    final static String[] triggerNames = new String[]{
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
    final static String[] editorTriggerNames = new String[]{
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
    final static int loop_STD = 0x00; // default is NO loop logic modification
    final static int loop_GLOBAL = 0x10; // assert to loop and UP 1 level...
    final static int loop_INVERT = 0x08; // invert/FALSE POL of loop to generate WAV BREAK condition

    final static int[] wavebrkCodes = new int[]{
        loop_GLOBAL, loop_INVERT
    };
    final static int[] wavebrkMasks = wavebrkCodes;

    final static String[] wavebrkNames = new String[]{
        "loop_GLOBAL", "loop_INVERT", "loop_STD" // NOI18N
    };

    final static int loop_GLOBAL_BIT = 4; // bit # assert to loop and UP 1 level...
    final static int loop_INVERT_BIT = 3; // bit # invert (NOT/FALSE) Polarity of loop condition/evaluation

// --------- 32 loop event codes
    final static int loop_MASK = 0x1F; // mask for VALID BRK rng-5bits

// disable loop- just run/playback .WAV file from SOUND flash or other source
    final static int no_loop = 0; // RUN to completeion of WAV segmenT

//--------------
// codes that imply LOOPING of current HANDLE WAV seg till loop WAV BREAK CONDITON met
    final static int loop_till_NEVER = TRIG_NEVER; // same as "no_loop", RUN to completion of WAV segmenT

    final static int loop_till_cam = TRIG_CAM; // loop on CAM event, either H/W input or AUTO-chuff,
    final static int loop_till_init_TRIG = TRIG_NOT_TRIG; // loop until INITIATE TRIGGER condition NOT met

// FUNCTION change events
    final static int loop_till_F1 = TRIG_SF1; // F1 change event
    final static int loop_till_F2 = TRIG_SF2;
    final static int loop_till_F3 = TRIG_SF3;
    final static int loop_till_F4 = TRIG_SF4;

    final static int loop_till_F0 = TRIG_SF0; // headlight
    final static int loop_till_DIRNOW_CHNG = TRIG_DIRNOW_CHNG; // have DIR_NOW changed
    final static int loop_till_MOVING = TRIG_MOVING; // SPD = Non-Zero
    final static int loop_till_SND_ACTV11 = TRIG_SND_ACTV11; // DECODER is addressed within CV11 time

    final static int loop_till_F5 = TRIG_SF5;
    final static int loop_till_F6 = TRIG_SF6;
    final static int loop_till_F7 = TRIG_SF7;
    final static int loop_till_F8 = TRIG_SF8;

    final static int loop_till_F9 = TRIG_SF9;
    final static int loop_till_F10 = TRIG_SF10;
    final static int loop_till_F11 = TRIG_SF11;
    final static int loop_till_F12 = TRIG_SF12;

// ONLY scatter CHNLS 4-7 [also WORK visibles] can be used for TIMED loop break conditions
    final static int loop_till_SCAT4 = 0x1C; // scatter CHNL4, phase A/B selected by loop_INVERT_BIT
    final static int loop_till_SCAT5 = 0x1D;
    final static int loop_till_SCAT6 = 0x1E;
    final static int loop_till_SCAT7 = 0x1F;

    final static int[] loopCodes = new int[]{
        loop_till_cam, loop_till_init_TRIG, // loop_till_NEVER is same as no_loop
        loop_till_DIRNOW_CHNG, loop_till_MOVING, loop_till_SND_ACTV11,
        loop_till_F0,
        loop_till_F1,
        loop_till_F2, loop_till_F3, loop_till_F4, loop_till_F5,
        loop_till_F6, loop_till_F7, loop_till_F8, loop_till_F9,
        loop_till_F10, loop_till_F11, loop_till_F12,
        loop_till_SCAT4, loop_till_SCAT5, loop_till_SCAT6, loop_till_SCAT7
    };
    final static int[] loopMasks = loopCodes;

    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY")  // known to be mutable, OK by convention
    final static String[] loopNames = new String[]{
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
    final static int DELAY_GLOBAL = 0x01;
    final static int DELAY_CV = 0x80; // delay fron SND_CV range
    final static int DELAY_THIS = 0x00; // immed data

//----------
// for TEST trigger OPC
    final static int TRIG_TRUE = 0x01;
    final static int TRIG_FALSE = 0x00;
    final static int ABOVE = 0x02; // this bit forces eval above on SPD state trigs

    final static int[] trigLogicCodes = new int[]{
        TRIG_TRUE
    };
    final static int[] trigLogicMasks = loopCodes;

    final static String[] trigLogicNames = new String[]{
        "TRIG_TRUE", "TRIG_FALSE" // NOI18N
    };

    final static int SK_SENS_BIT = 0;
    final static int ABOVE_BIT = 1;

//=====================================================================
// load MODIFY COMMAND arg/VALUES- allows USER maximal configurability/customization in SDF files
// 4byte CMD  1110-mmmm, ARG1,ARG2,ARG3
//---------------------
// modifier control REG command types
// mmmm target embedded in LOAD MODIFY CMD to direct what following 3 ARG bytes will be loaded TO
    final static int MTYPE_TIME = 0; // MODIFY timer
    final static int MTYPE_GAIN = 1; // setup a GAIN modify task for current SOUND chain
    final static int MTYPE_PITCH = 2; // setup a PITCH modify task for current SOUND chain
    final static int MTYPE_BLEND = 3; // setup BLEND logic task

// undefined NOW
    final static int MTYPE_SCATTER = 4; // preset a SCATTER channel/task
    final static int MTYPE_SNDCV = 5; // modify under MASK SNDCV,ARG1=SNDCV#,ARG2=DATA,ARG3=1bit=chng mask
    final static int MTYPE_WORK_IMMED = 6; // modify WORK reg immed data
    final static int MTYPE_WORK_INDIRECT = 7; // modify WORK reg indirect from SCV [selectable sound CV] or other WORK reg

    final static int[] modControlCodes = new int[]{
        MTYPE_TIME, MTYPE_GAIN, MTYPE_PITCH, MTYPE_BLEND,
        MTYPE_SCATTER, MTYPE_SNDCV, MTYPE_WORK_IMMED, MTYPE_WORK_INDIRECT
    };
    final static String[] modControlNames = new String[]{
        "MTYPE_TIME", "MTYPE_GAIN", "MTYPE_PITCH", "MTYPE_BLEND", // NOI18N
        "MTYPE_SCATTER", "MTYPE_SNDCV", "MTYPE_WORK_IMMED", "MTYPE_WORK_INDIRECT" // NOI18N
    };

// ---------------
// values for MATH functions on WORK regs
    final static int FMATH_LODE = 0x00; // load DATA,,MATH_FLAG SET shows whole WORK is ZERO- DEFAULT task
    final static int FMATH_AND = 0x20; // use to CLR bit(s),MATH_FLAG SET shows whole WORK is ZERO
    final static int FMATH_OR = 0x40; // use to SET  bit(S), MATH FLAG SET shows whole WORK is 1's
    final static int FMATH_XOR = 0x60; // use to flip bit(s), MATH FLAG SET if all flipped bits are now 0 

//;FMATH_ADD EQU 0x80 ;add signed value  (commented out in original Digitrax file)
    final static int FMATH_INTEGRATE = 0xA0; // add signed value to WORK,MATH_FLAG SET if over/underflo,or clamp LIMIT   
    final static int FMATH_TEST_ZERO = 0xC0; // see if WORK under MASK is ZERO, if ZERO set MATH bit

// -----1111----------------
// FIRST modify ARG- COMMAND
    final static int GCMD_NONE = 0x00; // NO gain MODIFY task RUN
    final static int GCMD_MASK = 0xE0; // 3 ms bits 1st CMD decode

    static final int[] arg1ModCodes = new int[]{
        FMATH_LODE, FMATH_AND, FMATH_OR, FMATH_XOR, FMATH_INTEGRATE, FMATH_TEST_ZERO, GCMD_MASK
    };
    static final String[] arg1ModNames = new String[]{
        "FMATH_LODE", "FMATH_AND", "FMATH_OR", "FMATH_XOR", "FMATH_INTEGRATE", "FMATH_TEST_ZERO", "GCMD_MASK" // NOI18N
    };

// 32 analog ARGS 3/21
// TYPE 8 GAIN
    final static int IMMED_GAIN_MODIFY = 0x80; // set new GAIN to [CV# 6 bit offset in ARG1] *CV58 , CV120 base
    final static int ANALOG_GAIN_MODIFY = 0xA0; // modify INIT GAIN by analog chnl in 5 ls bits
    // AUX1=  AUX2=dither coding

    final static int FCMD_NONE = 0x00; // NO PITCH MODIFY task RUN
    final static int FCMD_MASK = 0xE0; // 3 ms bits 1st CMD decode

// type 8 PITCH
    final static int CV_PITCH_MODIFY = 0x80; // set new PITCH to [CV# 6 bit offset in ARG1] , CV120 base
    final static int ANALOG_PITCH_MODIFY = 0xA0; // modify INIT PITCH by analog chnl in 5 ls bits
    // AUX1=  AUX2=dither coding

//==================
// 32 predefined user visible WORK/STATE regs, use these defined values
// first 16 WORK visible reg address codes.
    final static int WORK_SPEED = 0x00; // SOUND target PWM/SPD 
    final static int WORK_NOTCH = 0x01; // CHNL# for NOTCH Modified spd info, MODUL0 8
    final static int WORK_SERVO = 0x02;
    final static int WORK_MVOLTS = 0x03;

    final static int WORK_USER_LINES = 0x05;
    final static int WORK_TIMEBASE = 0x06; // 64mS cntr, ROLL=16 SECS
    final static int WORK_STATUS_BITS = 0x07; // PRIMARY decoder STATUS bits

    final static int WORK_GLBL_GAIN = 0x08; // FULL gain authority
    final static int WORK_GAIN_TRIM = 0x09; // signed (bit7) gain trim +/- 25%
    final static int WORK_PITCH_TRIM = 0x0A; // signed (bit7) pitch trim +/- 25%
    final static int WORK_SPEED_DELTA = 0x0B; // unsigned SPEED change

// SECOND 16 WORK  visible reg address codes
    final static int WORK_SCATTER4 = 0x10; // VISIBLE working SCATTER reg
    final static int WORK_SCATTER5 = 0x11; // VISIBLE working SCATTER reg
    final static int WORK_SCATTER6 = 0x12; // VISIBLE working SCATTER reg
    final static int WORK_SCATTER7 = 0x13; // VISIBLE working SCATTER reg

    final static int WORK_ACHNL_7F = 0x14; // DT400 VARIABLE whistle CHNL
    final static int WORK_ACHNL_7E = 0x15;
    final static int WORK_SKAT_FAST = 0x16;
    final static int WORK_SKAT_SLOW = 0x17;

    final static int WORK_DISTANCE = 0x18;
    final static int WORK_PEAK_SPD = 0x19;
    final static int WORK_USER_0 = 0x1A; // user  REG
    final static int WORK_USER_1 = 0x1B; // user  REG

    final static int WORK_USER_2 = 0x1C; // user  REG
    final static int WORK_USER_3 = 0x1D; // user  REG
    final static int WORK_USER_4 = 0x1E; // user  REG
    final static int WORK_USER_5 = 0x1F; // user  REG

    final static int[] workRegCodes = new int[]{
        WORK_SPEED, WORK_NOTCH, WORK_SERVO, WORK_MVOLTS,
        WORK_USER_LINES, WORK_TIMEBASE, WORK_STATUS_BITS,
        WORK_GLBL_GAIN, WORK_GAIN_TRIM, WORK_PITCH_TRIM, WORK_SPEED_DELTA,
        WORK_SCATTER4, WORK_SCATTER5, WORK_SCATTER6, WORK_SCATTER7,
        WORK_ACHNL_7F, WORK_ACHNL_7E, WORK_SKAT_FAST, WORK_SKAT_SLOW,
        WORK_DISTANCE, WORK_PEAK_SPD, WORK_USER_0, WORK_USER_1,
        WORK_USER_2, WORK_USER_3, WORK_USER_4, WORK_USER_5
    };

    final static String[] workRegNames = new String[]{
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
    final static int WKSB_ANALOG_BIT = 0; // 1=is in ANALOG mode [STATUS mirror]
    final static int WKSB_ANALOG_MASK = 0x01; // bit involved

    final static int WKSB_DIRNOW_BIT = 1; // 1= rev direction ?[STATUS mirror]
    final static int WKSB_DIRNOW_MASK = 0x02; // bit involved

    final static int WKSB_RUN_BIT = 2; // 0= stop motor PWM, non-primemover sounds RUN
    final static int WKSB_RUN_MASK = 0x04; // bit involved

    final static int WKSB_ACEL_BIT = 3; // 1= is ACCEL state
    final static int WKSB_ACEL_MASK = 0x08; // bit involved

    final static int WKSB_SPDDELTA_BIT = 4; // 1= change SPD by unsigned SPD_DELTA work REG only
    final static int WKSB_SPDDELTA_MASK = 0x10; // bit involved

    final static int WKSB_MATH_BIT = 7; // result from last MODIFY math action (can test with
    final static int WKSB_MATH_MASK = 0x80; // bit involved

    final static int[] workStatusBitCodes = new int[]{
        WKSB_ANALOG_MASK, WKSB_DIRNOW_MASK, WKSB_RUN_MASK, WKSB_ACEL_MASK, WKSB_SPDDELTA_MASK, WKSB_MATH_MASK
    };
    final static String[] workStatusBitNames = new String[]{
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
    final static int SCAT_CMD_PERIOD = 0x20; // command for PERIODIC event, SCALABLE scatter speedup on WORK reg INC
    final static int SCAT_CMD_PERIOD_REV = 0x28; // command for PERIODIC event, with SCALABLE scatter slowdn on WORK reg INC

    final static int SCAT_CMD_SAWTOOTH = 0x30;
    final static int SCAT_CMD_SAWTOOTH_REV = 0x38;

    final static int[] scatCommandCodes = new int[]{
        SCAT_CMD_PERIOD, SCAT_CMD_PERIOD_REV, SCAT_CMD_SAWTOOTH, SCAT_CMD_SAWTOOTH_REV};
    final static String[] scatCommandNames = new String[]{
        "SCAT_CMD_PERIOD", "SCAT_CMD_PERIOD_REV", "SCAT_CMD_SAWTOOTH", "SCAT_CMD_SAWTOOTH_REV"}; // NOI18N

    final static int SCAT_PERIOD_POLARITY_BIT = 3; // 1= reverse WORK influence
    final static int SKATTER_INCREMENT = 8; // phase INCREMENT

    final static int SCAT_CHNL0 = 0x00; // SCATTER task0 
    final static int SCAT_CHNL1 = 0x01; // SCATTER task1 
    final static int SCAT_CHNL2 = 0x02; // SCATTER task2 
    final static int SCAT_CHNL3 = 0x03; // SCATTER task3 
    final static int SCAT_CHNL4 = 0x04; // SCATTER task4- visible WORK reg
    final static int SCAT_CHNL5 = 0x05; // SCATTER task5- visible WORK reg
    final static int SCAT_CHNL6 = 0x06; // SCATTER task6- visible WORK reg
    final static int SCAT_CHNL7 = 0x07; // SCATTER task7- visible WORK reg

    final static int[] scatChannelCodes = new int[]{
        SCAT_CHNL0, SCAT_CHNL1, SCAT_CHNL2, SCAT_CHNL3, SCAT_CHNL4, SCAT_CHNL5, SCAT_CHNL6, SCAT_CHNL7};
    final static String[] scatChannelNames = new String[]{
        "SCAT_CHNL0", "SCAT_CHNL1", "SCAT_CHNL2", "SCAT_CHNL3", "SCAT_CHNL4", "SCAT_CHNL5", "SCAT_CHNL6", "SCAT_CHNL7"}; // NOI18N

    final static int SINTEN_IMMED = 0xE0; // use WORK# as immediate data
    final static int SINTEN_HIGH = 0xC0;
    final static int SINTEN_MID = 0x60; // mid scatter intensity 
    final static int SINTEN_LOW = 0x40;
    final static int SINTEN_MIN = 0x20;
    final static int SINTEN_OFF = 0x00;

    final static int[] sintenCodes = new int[]{
        SINTEN_IMMED, SINTEN_HIGH, SINTEN_MID, SINTEN_LOW, SINTEN_MIN, SINTEN_OFF};
    final static String[] sintenNames = new String[]{
        "SINTEN_IMMED", "SINTEN_HIGH", "SINTEN_MID", "SINTEN_LOW", "SINTEN_MIN", "SINTEN_OFF"}; // NOI18N

// ======================
    final static int DEFAULT_GLBL_GAIN = 0xC0;
    final static int MERGE_ALL_MASK = 0; // any 0 bit is involved

// ----2222--------------
// 2ND modify ARG
    final static int SNDCV_SRC = 0x80; // this bit set chngs from IMMED to SND_CV to control EFFECT span
    final static int SNDCV_SRC_BIT = 7; // 1=ls 7 bits SCV#, 0=ls 7 bits immed arg data

// ls 7 bit ARGS
    final static int MAXG_NONE = 0x00; // value of 0 means no scaling, SRC is 100% GAIN
    final static int MAXP_NONE = 0x00; // value of 0 means no scaling, SRC is 100% PITCH

// ----3333--------------
// 3RD modify ARG
//;MPUSH  EQU 0x80  ;this bit forces LEVEL OVERIDE
    final static int DITHER = 0x80; // value to change to DITHER mode
    final static int SUMG = 0x40;

    final static int MIN_DITH = 0x00;
    final static int LOW_DITH = 0x20;
    final static int MID_DITH = 0x40;
    final static int MAX_DITH = 0x60;

// ;MPUSH_BIT EQU 7  (commendted out in the original Digitrax file)
    final static int DITHER_BIT = 7;
    final static int SUM_BIT = 6; // 1=add ARG1/2 variation,0=multiply
    final static int MG1_BIT = 5; // these code meaning of LO nibble
    final static int MG0_BIT = 4;

// for CMDS 8/9 ls nibble ARG3 is scaling factor to set CV/IMMED data control SPAN
    final static int SCALE_F = 0x0F;
    final static int SCALE_C = 0x0C;
    final static int SCALE_8 = 0x08;
    final static int SCALE_6 = 0x06;
    final static int SCALE_5 = 0x05;
    final static int SCALE_4 = 0x04;
    final static int SCALE_2 = 0x02;

    final static int[] arg3ModCodes = new int[]{
        DITHER,
        LOW_DITH, MID_DITH, MAX_DITH, // MIN_DITH is zero, a no-op
        SCALE_F, SCALE_C, SCALE_8, SCALE_6, SCALE_5, SCALE_4, SCALE_2
    };
    final static int[] arg3ModMasks = new int[]{
        DITHER,
        0x60, 0x60, 0x60,
        0x0F, 0x0F, 0x0F, 0x0F, 0x0F, 0x0F, 0x0F
    };
    final static String[] arg3ModNames = new String[]{
        "DITHER", // NOI18N
        "LOW_DITH", "MID_DITH", "MAX_DITH", // NOI18N
        "SCALE_F", "SCALE_C", "SCALE_8", "SCALE_6", "SCALE_5", "SCALE_4", "SCALE_2" // NOI18N
    };

// DITHER ????
    final static int DITHERG_WHISTLE = 0x00;
    final static int DITHERP_WHISTLE = 0x00;

    final static int DITHERG_DIESEL = 0x00;
    final static int DITHERP_DIESEL = 0x00;

    final static int DITHERG_NONE = 0x00;
    final static int DITHERP_NONE = 0x00;

// THE FOLLOWING ARE PRESENT, BUT WITH CURRENT VALUES ARE NOT PARTICULARLY USEFUL
    final static int[] ditherGCodes = new int[]{
        DITHERG_DIESEL, DITHERG_WHISTLE, DITHERG_NONE
    };
    final static String[] ditherGNames = new String[]{
        "DITHERG_DIESEL", "DITHERG_WHISTLE", "DITHERG_NONE" // NOI18N
    };
    final static int[] ditherPCodes = new int[]{
        DITHERP_DIESEL, DITHERP_WHISTLE, DITHERP_NONE
    };
    final static String[] ditherPNames = new String[]{
        "DITHERP_DIESEL", "DITHERP_WHISTLE", "DITHERP_NONE" // NOI18N
    };

// BLEND logic commnds
// 1110-BLEND, BLEND_CMDS, BLEND_GAIN_ARG, BLEND_FASE_ARG
// BLEND cmds bit encoding
    final static int BLEND_TGTMASK = 0xC0; // 2 ms bits control Blend target
    final static int BLEND_FASEMASK = 0x38; // 3  bits FASE_BLEND type, 000=inactive
    final static int BLEND_GAINMASK = 0x07; // 3 ls bits GAIN_BLEND type, 000=inactive

// MODE0 with ARG=0 is INACTIVE BLEND
    final static int BLEND_CURRENT_CHNL = 0x00 & BLEND_TGTMASK;
    final static int BLEND_ALL = 0x40 & BLEND_TGTMASK;

    final static int BLEND_GAIN0 = 0x00 & BLEND_GAINMASK; // STD logic 0 blend
    final static int BLEND_GAIN1 = 0x01 & BLEND_GAINMASK; // alt logic 1 blend

    final static int BLEND_FASE0 = 0x00 & BLEND_FASEMASK; // STD logic 0 blend
    final static int BLEND_FASE1 = 0x08 & BLEND_FASEMASK; // alt logic 1 blend

    final static int[] blendArg1Codes = new int[]{
        BLEND_CURRENT_CHNL, BLEND_ALL,
        BLEND_GAIN0, BLEND_GAIN1,
        BLEND_FASE0, BLEND_FASE1
    };
    final static int[] blendArg1Masks = new int[]{
        BLEND_TGTMASK, BLEND_TGTMASK,
        BLEND_GAINMASK, BLEND_GAINMASK,
        BLEND_FASEMASK, BLEND_FASEMASK
    };
    final static String[] blendArg1Names = new String[]{
        "BLEND_CURRENT_CHNL", "BLEND_ALL", // NOI18N
        "BLEND_GAIN0", "BLEND_GAIN1", // NOI18N
        "BLEND_FASE0", "BLEND_FASE1" // NOI18N
    };

    final static int BLENDG_DSL_ACCEL0 = 0x04; // GAIN rate for loaded DIESEL
    final static int BLENDF_DSL_ACCEL0 = 0x02; // FASE rate for loaded DIESEL

    final static int BLENDG_DSL_ACCEL1 = 0x06; // GAIN rate for loaded DIESEL
    final static int BLENDF_DSL_ACCEL1 = 0x05; // GAIN rate for unloading DIESEL

    final static int BLENDG_DSL_DECEL0 = 0x09; // GAIN rate for unloading DIESEL
    final static int BLENDF_DSL_DECEL0 = 0x07; // FASE rate for unloading DIESEL

    final static int[] blendArg2Codes = new int[]{
        BLENDG_DSL_ACCEL0, BLENDG_DSL_ACCEL1, BLENDG_DSL_ACCEL1};
    final static String[] blendArg2Names = new String[]{
        "BLENDG_DSL_ACCEL0", "BLENDG_DSL_ACCEL1", "BLENDG_DSL_ACCEL1"}; // NOI18N
    final static int[] blendArg3Codes = new int[]{
        BLENDF_DSL_ACCEL0, BLENDF_DSL_ACCEL1, BLENDF_DSL_ACCEL1};
    final static String[] blendArg3Names = new String[]{
        "BLENDF_DSL_ACCEL0", "BLENDF_DSL_ACCEL1", "BLENDF_DSL_ACCEL1"}; // NOI18N

// COMPARE CMD control bits
    final static int TARGET_DATA = 0x00; // second COMPARE ARG is WORK reg or SCV
    final static int IMMED_DATA = 0x04; // second COMPARE ARG is IMMEDIATE 8 bit DATA

    final static int SKIP_SAME = 0x00;
    final static int SKIP_RSVD = 0x01; //reserved CONDITION code
    final static int SKIP_LESS = 0x02;
    final static int SKIP_GRTR = 0x03;

    final static int COMP_ALL = 0x00; // 1 bits= do not include
    final static int COMP_7LSB = 0X80;

// define fixed CV locations
    final static int SNDCV_CONFIGA = 0x80; // CV129= config byte
    final static int SNDCV_CONFIGB = 0x81; //CV130= config byte
    final static int SCV_DCONFIG = 0x82; // CV131= diesel config
    final static int SCV_NOTCH = 0x83; // CV132= typ DIESEL NOTCH rate byte
    final static int SNDCV_STEAM = 0x84; // CV133= typ steam CAM config byte, x80=EXT or 1-127=DRIVER dia in inches
    final static int SCV_STGEAR = 0x85; // CV134= steam gear ratio trim
    final static int SCV_MUTE_VOL = 0x86; // CV135= vol level when MUTE action is triggered, e.g. F8=ON
    final static int SCV_MAIN_PITCH = 0x87; // CV136
    final static int SCV_137 = 0x88;
    final static int SCV_138 = 0x89; // SCV138= 
    final static int SCV_DISTANCE_RATE = 0x8A; // SCV139= mask for controlling DISTANCE rate event/trigger
    final static int SCV_FREEFORM = 0x8B; // SCV_140, here the SCV's are SDF defined

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
    final static int MAXG_WHISTLE = 0x6A;
    final static int MAXP_WHISTLE = 0x07;
    final static int MAXG_DIESEL = 0x1C;
    final static int MAXP_DIESEL = 0x3E;
    final static int MAXG_STEAM = 0x1C;
    final static int MAXP_STEAM = 0x7E; // was 3E- make more explosive!

// VALUES ARE SUCH THAT WE CAN'T TELL STEAM FROM DIESEL WHEN DISASSMEMLING
    final static int[] maxPCodes = new int[]{
        MAXP_WHISTLE, MAXP_DIESEL, MAXP_STEAM
    };
    final static String[] maxPNames = new String[]{
        "MAXP_WHISTLE", "MAXP_DIESEL", "MAXP_STEAM" // NOI18N
    };

    final static int[] maxGCodes = new int[]{
        MAXG_WHISTLE, MAXG_DIESEL, MAXG_STEAM
    };
    final static String[] maxGNames = new String[]{
        "MAXG_WHISTLE", "MAXG_DIESEL", "MAXG_STEAM" // NOI18N
    };

}
