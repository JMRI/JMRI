// SdfMacro.java

package jmri.jmrix.loconet.sdf;

/**
 * Common base for all the SDF macros defined by Digitrax
 * for their sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.4 $
 */

abstract class SdfMacro {

    /**
     * Name used by the macro in the SDF definition
     */
    abstract public String name();
    
    /**
     * Provide number of bytes defined by this macro
     */
    abstract public int length();
    
    /**
     * Provide a single-line representation,
     * including the trailing newline
     */
    abstract public String toString();


    /**
     * Service method to unpack various bit-coded values
     * for display
     */
    String decodeFlags(int input, int[] values, int[] masks, String[] labels) {
        String[] names = jmri.util.StringUtil.getNamesFromStateMasked(input, values, masks, labels);
        if (names == null) return "<ERROR>"; // unexpected case, internal error, should also log?
        else if (names.length == 0) return labels[labels.length-1];  // last name is non-of-above special case
        else if (names.length == 1) return names[0];
        String output = names[0];
        for (int i=1; i<names.length; i++)
            output+="+"+names[i];
        return output;
    }
     
    /**
     * Insert this as the spacing at the front of ecah line in toString.
     * Used to do nested indention.
     */
    static String linestart = "    ";
    
/* **********************
* Constants
** **********************/

//-------------------
static int TRUE     =     1;
static int FALSE    =     0;
static int[] trueFalseCodes = new int[]{TRUE, FALSE};
static String[] trueFalseNames = new String[]{"TRUE", "FALSE"};

// TRIGGER values
// sound TRIGGER PREMPT control bits

// SPECIAL trigs:	POL,0,0,0-0,s,s,s 	0-7 are special trigs
// FN trigger format:	POL,0,0,B-B,b,b,b   	BB 1 of 3 bytes 01,10,11 (24 fns), bbb is bit adr#


static int NORMAL		        =	0;	  // (complete .WAV frag/preempt higher/trigger true)

static int ZAP		            =	0x01; // value for ZAP on PREMPT
static int RUN_WHILE_TRIG	    =	0x02; // mask/binary values
static int NO_PREEMPT_TRIG	    =	0x04;
static int NOT_TRIG	            =	0x80; // 12/13/04, in TRIG argument now

// 3 ls bits of INITIATE CMD 1ST byte
static int CMD_ZAP_BIT		    =	0; // 1=END immed if PREMPTED, 0=finish current .wav frag before END
static int RUN_WHILE_TRIG_BIT	=	1; // 1=persistent while TRIGGER is valid, 0=run once
static int NO_PREEMPT_TRIG_BIT	=	2;

static int NOT_TRIG_BIT		    =	7; // POL bit in TRIG byte 1=reverse POL

static int[] premptCodes = new int[]{ZAP,RUN_WHILE_TRIG,NO_PREEMPT_TRIG, NOT_TRIG}; // NORMAL matches all (is zero), so left off
static int[] premptMasks = premptCodes;             // just check 1 bits
static String[] premptNames = new String[]{"ZAP","RUN_WHILE_TRIG","NO_PREEMPT_TRIG", "NOT_TRIG","NORMAL"};

//----------
// TRIGGER initiate CODES 
    
static int TRIG_NEVER	    =	0x00; // 0 is INACTIVE entry (NULL,not a valid INITIATE coding)
static int TRIG_MODE_CHNG	=	0x01;
static int TRIG_MATH	    =	0x02; // math result trigger
static int TRIG_DISTANCE	=	0x03; // have new increment of distance, use for FUEL low alarm, maintenance ETC

static int TRIG_SPD_INC	    =	0x04; // have SPD increase
static int TRIG_SPD_DEC	    =	0x05; // have SPD decrease
static int TRIG_CAM	        =	0x06; // TRIG on CAM event, either H/W input or AUTO-chuff,
static int TRIG_NOT_TRIG	=	0x07; // brk/loop SPECIAL, a.k.a. "loop_till_init_TRIG"

// -------
// F1-F28 and F0 map into 3 BYTES of static trig state/fn state bits

// these triggers preCODED for fast TRIG bit adr   POL,0,BBB,bbb:   BB=byte,bbb=bit adr
// Mapped to internal allocations

static int MOVE_BIT        =     6;    // moving BIT
static int MVFN_MASK       =     0x1F; // direct FN bits


static int TRIG_SF1        =     0x08; // F1 change event
static int TRIG_SF2        =     0x09; // byte 1, bit1
static int TRIG_SF3        =     0x0A;
static int TRIG_SF4        =     0x0B;

static int TRIG_SF0        =     0x0C; // headlight/F0 state
static int TRIG_DIRNOW_CHNG =    0x0D; // have DIR_NOW changed
static int TRIG_MOVING     =     0x0E; // SPD = Non-Zero
static int TRIG_SND_ACTV11 =     0x0F; // DECODER is SPD addressed within CV11 time

// ----------
static int TRIG_SF5        =     0x10; // byte 2,bit0
static int TRIG_SF6        =     0x11;
static int TRIG_SF7        =     0x12;
static int TRIG_SF8        =     0x13;

static int TRIG_SF13       =     0x14; // BYTE 2, bit 4, 
static int TRIG_SF14       =     0x15;
static int TRIG_SF15       =     0x16;
static int TRIG_SF16       =     0x17;

// ---------
static int TRIG_SF9        =     0x18; // byte 3 bit 0
static int TRIG_SF10       =     0x19;
static int TRIG_SF11       =     0x1A;
static int TRIG_SF12       =     0x1B;

static int TRIG_SF17       =     0x1C; // byte 3, bit 4
static int TRIG_SF18       =     0x1D;
static int TRIG_SF19       =     0x1E;
static int TRIG_SF20       =     0x1F;

//

static int TRIG_SF21       =     0x20; // expanded FUNCTIONS
static int TRIG_SF22       =     0x21;
static int TRIG_SF23       =     0x22;
static int TRIG_SF24       =     0x23;

static int TRIG_SF25       =     0x24;
static int TRIG_SF26       =     0x25;
static int TRIG_SF27       =     0x26;
static int TRIG_SF28       =     0x27;

//
//----------------FIRST 8 bsc regs reserved
static int TRIG_BSC0       =     0x28; // expanded FUNCTIONS
static int TRIG_BSC1       =     0x29;
static int TRIG_BSC2       =     0x2A;
static int TRIG_BSC3       =     0x2B;

static int TRIG_BSC4       =     0x2C;
static int TRIG_BSC5       =     0x2D;
static int TRIG_BSC6       =     0x2E;
static int TRIG_BSC7       =     0x2F;

//------------------
//these trig lines both REPORT the external inputs to decoder, and CAN also be SET/RESET by SDF trigger commands
// and can be read back by TRANSPONDING as external ALARMS etc

static int TRIG_IN_BASE    =     0x50;

static int TRIG_IN_0       =     TRIG_IN_BASE+0; // input 0 trig, CAM input if not assigned to STEAM chuf
static int TRIG_IN_1       =     TRIG_IN_BASE+1;
static int TRIG_IN_2       =     TRIG_IN_BASE+2;
static int TRIG_IN_3       =     TRIG_IN_BASE+3;

static int TRIG_IN_4       =     TRIG_IN_BASE+4;
static int TRIG_IN_5       =     TRIG_IN_BASE+5;
static int TRIG_IN_6       =     TRIG_IN_BASE+6;
static int TRIG_IN_7       =     TRIG_IN_BASE+7;

static int TRIG_NOTCH_CHNG =     TRIG_IN_7+1;   // when notch changes
static int TRIG_TIME_16PPS =     TRIG_IN_7+2;   // 16 per sec/64mS rate
static int TRIG_FACTORY_CVRESET =  TRIG_IN_7+3; // have CV8=8/9 request, USER definable CV values
static int TRIG_OPSPROGWR_CV    =  TRIG_IN_7+4; // have OPSPROG action

//--------------- SCATTER TRIGGER codes, ON/OFF phase per SCATTER task
static int SCAT_TRIG_BASE  =     0x60;

static int TRIG_SCAT0      =     SCAT_TRIG_BASE+0; // scatter CHNL0, phase A sel NOT_TRIG_BIT
static int TRIG_SCAT1      =     SCAT_TRIG_BASE+1;
static int TRIG_SCAT2      =     SCAT_TRIG_BASE+2;
static int TRIG_SCAT3      =     SCAT_TRIG_BASE+3;
static int TRIG_SCAT4      =     SCAT_TRIG_BASE+4; // has visible WORK register
static int TRIG_SCAT5      =     SCAT_TRIG_BASE+5; // has visible WORK register
static int TRIG_SCAT6      =     SCAT_TRIG_BASE+6; // has visible WORK register
static int TRIG_SCAT7      =     SCAT_TRIG_BASE+7; // has visible WORK register

// ===============================
//  encoded TRIGGER values related to SPEED_STATE code

static int T_SS_BASE       =     0x70; // 0x20 ;base TRIG code for this STATE logic
static int TSPD            =     T_SS_BASE;  // base TRIG code for this STATE logic

static int T_SPD_MUTE      =     TSPD+0;
static int T_SPD_TURNON    =     TSPD+1;
static int T_SPD_IDLE      =     TSPD+2;
static int T_SPD_ACCEL1    =     TSPD+3;

static int T_SPD_ACC_CHNG  =     TSPD+4;  // 1st GEN parallel/series chng
static int T_SPD_ACCEL2    =     TSPD+5;
static int T_SPD_IDLEXIT   =     TSPD+6;  // leaving IDLE state
static int T_SPD_RUN       =     TSPD+7;

static int T_SPD_DECEL1    =     TSPD+8;
static int T_SPD_DEC_CHNG  =     TSPD+9;
static int T_SPD_DECEL2    =     TSPD+10;
// T_SPD_DIR_CHNG =     TSPD+11   ;       (Commented in original Digitrax file)

static int T_SPD_DEC_IDLE  =     TSPD+11;
static int T_SPD_TURNOFF   =     TSPD+12;
static int T_SPD_DEC_SP1   =     TSPD+13;
static int T_SPD_DEC_SP2   =     TSPD+14;
static int T_SPD_DIR_CHNG  =     TSPD+15;

static int[] triggerCodes    = new int[]{
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

static String[] triggerNames = new String[]{
        "TRIG_NEVER", "TRIG_MODE_CHNG", "TRIG_MATH", "TRIG_DISTANCE",
        "TRIG_SPD_INC", "TRIG_SPD_DEC", "TRIG_CAM", "TRIG_NOT_TRIG",
        "TRIG_SF0", "TRIG_SF1", "TRIG_SF2", "TRIG_SF3", "TRIG_SF4", 
         
        "TRIG_SF5", "TRIG_SF6", "TRIG_SF7", "TRIG_SF8",
        "TRIG_SF9", "TRIG_SF10", "TRIG_SF11", "TRIG_SF12",
        "TRIG_SF13", "TRIG_SF14", "TRIG_SF15", "TRIG_SF16",
        "TRIG_SF17", "TRIG_SF18", "TRIG_SF19", "TRIG_SF20",
        "TRIG_DIRNOW_CHNG", "TRIG_MOVING", "TRIG_SND_ACTV11",

        "TRIG_SF21", "TRIG_SF22", "TRIG_SF23", "TRIG_SF24",
        "TRIG_SF25", "TRIG_SF26", "TRIG_SF27", "TRIG_SF28",

        "TRIG_BSC0", "TRIG_BSC1", "TRIG_BSC2", "TRIG_BSC3",

        "TRIG_BSC4", "TRIG_BSC5", "TRIG_BSC6", "TRIG_BSC7",

        "TRIG_IN_0", "TRIG_IN_1", "TRIG_IN_2", "TRIG_IN_3", 
        "TRIG_IN_4", "TRIG_IN_5", "TRIG_IN_6", "TRIG_IN_7", 
        "TRIG_NOTCH_CHNG", "TRIG_TIME_16PPS", "TRIG_FACTORY_CVRESET", "TRIG_OPSPROGWR_CV",
        "TRIG_SCAT0", "TRIG_SCAT1", "TRIG_SCAT2", "TRIG_SCAT3",
        "TRIG_SCAT4", "TRIG_SCAT5", "TRIG_SCAT6", "TRIG_SCAT7",

        "T_SPD_MUTE", "T_SPD_TURNON", "T_SPD_IDLE", "T_SPD_ACCEL1",
        "T_SPD_ACC_CHNG", "T_SPD_ACCEL2", "T_SPD_IDLEXIT", "T_SPD_RUN",
        "T_SPD_DECEL1", "T_SPD_DEC_CHNG", "T_SPD_DECEL2",
        "T_SPD_DEC_IDLE", "T_SPD_TURNOFF", 
        "T_SPD_DEC_SP1", "T_SPD_DEC_SP2", "T_SPD_DIR_CHNG"

};

///============================BREAK/LOOP logic during a .WAV playback==================

// WAVBRK modify flags

static int loop_STD	        = 0x00; // default is NO loop logic modification
static int loop_GLOBAL	    = 0x10; // assert to loop and UP 1 level...
static int loop_INVERT	    = 0x08; // invert/FALSE POL of loop to generate WAV BREAK condition

static int[] wavebrkCodes    = new int[]{
    loop_GLOBAL, loop_INVERT
};
static int[] wavebrkMasks    = wavebrkCodes;

static String[] wavebrkNames    = new String[]{
    "loop_GLOBAL", "loop_INVERT", "loop_STD"
};

static int loop_GLOBAL_BIT  = 4; // bit # assert to loop and UP 1 level...
static int loop_INVERT_BIT	= 3; // bit # invert (NOT/FALSE) Polarity of loop condition/evaluation

// --------- 32 loop event codes
static int loop_MASK	    = 0x1F; // mask for VALID BRK rng-5bits

// disable loop- just run/playback .WAV file from SOUND flash or other source
static int no_loop		    = 0; // RUN to completeion of WAV segmenT

//--------------
// codes that imply LOOPING of current HANDLE WAV seg till loop WAV BREAK CONDITON met


static int loop_till_NEVER	    = TRIG_NEVER; // same as "no_loop", RUN to completion of WAV segmenT


static int loop_till_cam		= TRIG_CAM; // loop on CAM event, either H/W input or AUTO-chuff,
static int loop_till_init_TRIG	= TRIG_NOT_TRIG; // loop until INITIATE TRIGGER condition NOT met

// FUNCTION change events
 
static int loop_till_F1	        = TRIG_SF1; // F1 change event
static int loop_till_F2	        = TRIG_SF2;
static int loop_till_F3	        = TRIG_SF3;
static int loop_till_F4	        = TRIG_SF4;

static int loop_till_F0		    = TRIG_SF0; // headlight
static int loop_till_DIRNOW_CHNG 	= TRIG_DIRNOW_CHNG; // have DIR_NOW changed
static int loop_till_MOVING	    = TRIG_MOVING; // SPD = Non-Zero
static int loop_till_SND_ACTV11 = TRIG_SND_ACTV11; // DECODER is addressed within CV11 time


static int loop_till_F5	        = TRIG_SF5;
static int loop_till_F6	        = TRIG_SF6;
static int loop_till_F7	        = TRIG_SF7;
static int loop_till_F8	        = TRIG_SF8;

static int loop_till_F9	        = TRIG_SF9;
static int loop_till_F10	    = TRIG_SF10;
static int loop_till_F11	    = TRIG_SF11;
static int loop_till_F12	    = TRIG_SF12;

// ONLY scatter CHNLS 4-7 [also WORK visibles] can be used for TIMED loop break conditions
static int loop_till_SCAT4		= 0x1C; // scatter CHNL4, phase A/B selected by loop_INVERT_BIT
static int loop_till_SCAT5 	    = 0x1D;		
static int loop_till_SCAT6 	    = 0x1E;
static int loop_till_SCAT7 	    = 0x1F;

static int[] loopCodes    = new int[]{
    loop_till_cam, loop_till_init_TRIG,   // loop_till_NEVER is same as no_loop
    loop_till_DIRNOW_CHNG, loop_till_MOVING, loop_till_SND_ACTV11, 
    loop_till_F0,
    loop_till_F1,
    loop_till_F2, loop_till_F3, loop_till_F4, loop_till_F5,
    loop_till_F6, loop_till_F7, loop_till_F8, loop_till_F9,
    loop_till_F10, loop_till_F11, loop_till_F12, 
    loop_till_SCAT4, loop_till_SCAT5, loop_till_SCAT6, loop_till_SCAT7
};
static int[] loopMasks    = loopCodes;

static String[] loopNames    = new String[]{
    "loop_till_cam", "loop_till_init_TRIG", 
    "loop_till_DIRNOW_CHNG", "loop_till_MOVING", "loop_till_SND_ACTV11", 
    "loop_till_F0",
    "loop_till_F1",
    "loop_till_F2", "loop_till_F3", "loop_till_F4", "loop_till_F5",
    "loop_till_F6", "loop_till_F7", "loop_till_F8", "loop_till_F9",
    "loop_till_F10", "loop_till_F11", "loop_till_F12", 
    "loop_till_SCAT4", "loop_till_SCAT5", "loop_till_SCAT6", "loop_till_SCAT7",
    "no_loop"
};

//----------------
static int DELAY_GLOBAL	=	0x01;
static int DELAY_CV	    =	0x80; // delay fron SND_CV range
static int DELAY_THIS	    =	0x00; // immed data

//----------
// for TEST trigger OPC
static int TRIG_TRUE	    =	0x01;
static int TRIG_FALSE	    =	0x00;
static int ABOVE		    =	0x02; // this bit forces eval above on SPD state trigs

static int[] trigLogicCodes    = new int[]{
    TRIG_TRUE
};
static int[] trigLogicMasks    = loopCodes;

static String[] trigLogicNames    = new String[]{
    "TRIG_TRUE", "TRIG_FALSE"
};


static int SK_SENS_BIT	    =	0;
static int ABOVE_BIT	    =	1;

//=====================================================================
// load MODIFY COMMAND arg/VALUES- allows USER maximal configurability/customization in SDF files
// 4byte CMD 	1110-mmmm, ARG1,ARG2,ARG3

//---------------------
// modifier control REG command types
// mmmm target embedded in LOAD MODIFY CMD to direct what following 3 ARG bytes will be loaded TO

static int MTYPE_TIME	=	0; // MODIFY timer
static int MTYPE_GAIN	=	1; // setup a GAIN modify task for current SOUND chain
static int MTYPE_PITCH	=	2; // setup a PITCH modify task for current SOUND chain
static int MTYPE_BLEND	=	3; // setup BLEND logic task

// undefined NOW
static int MTYPE_SCATTER	=	4; // preset a SCATTER channel/task
static int MTYPE_SNDCV	    =	5; // modify under MASK SNDCV,ARG1=SNDCV#,ARG2=DATA,ARG3=1bit=chng mask
static int MTYPE_WORK_IMMED  =	6; // modify WORK reg immed data
static int MTYPE_WORK_INDIRECT = 7; // modify WORK reg indirect from SCV [selectable sound CV] or other WORK reg

static int[] modControlCodes = new int[]{
    MTYPE_TIME, MTYPE_GAIN, MTYPE_PITCH, MTYPE_BLEND,
    MTYPE_SCATTER, MTYPE_SNDCV, MTYPE_WORK_IMMED, MTYPE_WORK_INDIRECT
};
static String[] modControlNames = new String[]{
    "MTYPE_TIME", "MTYPE_GAIN", "MTYPE_PITCH", "MTYPE_BLEND",
    "MTYPE_SCATTER", "MTYPE_SNDCV", "MTYPE_WORK_IMMED", "MTYPE_WORK_INDIRECT"
};

// ---------------
// values for MATH functions on WORK regs

static int FMATH_LODE	=	0x00; // load DATA,,MATH_FLAG SET shows whole WORK is ZERO- DEFAULT task
static int FMATH_AND	=	0x20; // use to CLR bit(s),MATH_FLAG SET shows whole WORK is ZERO
static int FMATH_OR	    =	0x40; // use to SET  bit(S), MATH FLAG SET shows whole WORK is 1's
static int FMATH_XOR	=	0x60; // use to flip bit(s), MATH FLAG SET if all flipped bits are now 0 

//;FMATH_ADD	EQU	0x80	;add signed value  (commented out in original Digitrax file)
static int FMATH_INTEGRATE	=	0xA0; // add signed value to WORK,MATH_FLAG SET if over/underflo,or clamp LIMIT 		
static int FMATH_TEST_ZERO	=	0xC0; // see if WORK under MASK is ZERO, if ZERO set MATH bit

// -----1111----------------
// FIRST modify ARG- COMMAND
static int GCMD_NONE	= 0x00; // NO gain MODIFY task RUN
static int GCMD_MASK	= 0xE0; // 3 ms bits 1st CMD decode

// 32 analog ARGS 3/21
// TYPE 8 GAIN
static int IMMED_GAIN_MODIFY   =		0x80; // set new GAIN to [CV# 6 bit offset in ARG1] *CV58 , CV120 base
static int ANALOG_GAIN_MODIFY  =		0xA0; // modify INIT GAIN by analog chnl in 5 ls bits
						// AUX1=		AUX2=dither coding

static int FCMD_NONE	=		0x00; // NO PITCH MODIFY task RUN
static int FCMD_MASK	=		0xE0; // 3 ms bits 1st CMD decode

// type 8 PITCH
static int CV_PITCH_MODIFY	   =		0x80; // set new PITCH to [CV# 6 bit offset in ARG1] , CV120 base
static int ANALOG_PITCH_MODIFY =		0xA0; // modify INIT PITCH by analog chnl in 5 ls bits
						// AUX1=		AUX2=dither coding

//==================
// 32 predefined user visible WORK/STATE regs, use these defined values

// first 16 WORK visible reg address codes.

static int WORK_SPEED	=	0x00; // SOUND target PWM/SPD 
static int WORK_NOTCH	=	0x01; // CHNL# for NOTCH Modified spd info, MODUL0 8
static int WORK_SERVO	=	0x02;
static int WORK_MVOLTS	=	0x03;

static int WORK_USER_LINES	=	0x05;
static int WORK_TIMEBASE	=	0x06; // 64mS cntr, ROLL=16 SECS
static int WORK_STATUS_BITS =	0x07; // PRIMARY decoder STATUS bits

static int WORK_GLBL_GAIN	=	0x08; // FULL gain authority
static int WORK_GAIN_TRIM	=	0x09; // signed (bit7) gain trim +/- 25%
static int WORK_PITCH_TRIM =	0x0A; // signed (bit7) pitch trim +/- 25%
static int WORK_SPEED_DELTA =	0x0B; // unsigned SPEED change

// SECOND 16 WORK  visible reg address codes

static int WORK_SCATTER4	=	0x10; // VISIBLE working SCATTER reg
static int WORK_SCATTER5	=	0x11; // VISIBLE working SCATTER reg
static int WORK_SCATTER6	=	0x12; // VISIBLE working SCATTER reg
static int WORK_SCATTER7	=	0x13; // VISIBLE working SCATTER reg

static int WORK_ACHNL_7F	=	0x14; // DT400 VARIABLE whistle CHNL
static int WORK_ACHNL_7E	=	0x15;
static int WORK_SKAT_FAST	=	0x16;
static int WORK_SKAT_SLOW	=	0x17;

static int WORK_DISTANCE	=	0x18;
static int WORK_PEAK_SPD	=	0x19;
static int WORK_USER_0	    =	0x1A; // user  REG
static int WORK_USER_1	    =	0x1B; // user  REG

static int WORK_USER_2	    =	0x1C; // user  REG
static int WORK_USER_3	    =	0x1D; // user  REG
static int WORK_USER_4	    =	0x1E; // user  REG
static int WORK_USER_5	    =	0x1F; // user  REG


// ---------------------
// WORK_STATUS_BITS register bit definitions. data is VOLATILE

static int WKSB_ANALOG_BIT	= 0; // 1=is in ANALOG mode [STATUS mirror]
static int WKSB_ANALOG_MASK = 0x01; // bit involved

static int WKSB_DIRNOW_BIT	= 1; // 1= rev direction ?[STATUS mirror]
static int WKSB_DIRNOW_MASK = 0x02; // bit involved

static int WKSB_RUN_BIT     = 2; // 0= stop motor PWM, non-primemover sounds RUN
static int WKSB_RUN_MASK    = 0x04; // bit involved
	
static int WKSB_ACEL_BIT	= 3; // 1= is ACCEL state
static int WKSB_ACEL_MASK   = 0x08; // bit involved

static int WKSB_SPDDELTA_BIT  =	4; // 1= change SPD by unsigned SPD_DELTA work REG only
static int WKSB_SPDDELTA_MASK =	0x10; // bit involved


static int WKSB_MATH_BIT	= 7; // result from last MODIFY math action (can test with
static int WKSB_MATH_MASK	= 0x80; // bit involved

//========================
// SCATTER commands as arg bytes in MTYPE_SCATTER

// is MODIFY SCTR command  -  1110-0100, cccc-XAAA, aaaa-aaaa, bbbb-bbbb, AAA is 1 of 8 scat tasks/chnls

// is SCTR_PERIOD command  -  1110-0100, 0001-PAAA, Srrr-rrrr, IIIw-wwww, 

// AAA is 1 of 8 scat tasks/chnls,  P=WORK scatter POLARITY, 
// S=1 is SOUNDCV src  [0x80=CV141 etc], S=0 rrr-rrrr is RATE in approx 1 sec counts, rate=0 is CNTR hold...
// III=scatter intensity- 000=no SCATTER, w-wwww is a WORK reg# as SCATTER vary data src

// cccc is 16 SCATTER command modes, dddd and eeee are ARG Regs for run modes

static int SCAT_CMD_PERIOD		    = 0x20; // command for PERIODIC event, SCALABLE scatter speedup on WORK reg INC
static int SCAT_CMD_PERIOD_REV	    = 0x28; // command for PERIODIC event, with SCALABLE scatter slowdn on WORK reg INC

static int SCAT_CMD_SAWTOOTH	    = 0x30;
static int SCAT_CMD_SAWTOOTH_REV	= 0x38;

static int SCAT_PERIOD_POLARITY_BIT =	3; // 1= reverse WORK influence
static int SKATTER_INCREMENT	    =	8; // phase INCREMENT
	

static int SCAT_CHNL0		=	0x00; // SCATTER task0 
static int SCAT_CHNL1		=	0x01; // SCATTER task1 
static int SCAT_CHNL2		=	0x02; // SCATTER task2 
static int SCAT_CHNL3		=	0x03; // SCATTER task3 
static int SCAT_CHNL4		=	0x04; // SCATTER task4- visible WORK reg
static int SCAT_CHNL5		=	0x05; // SCATTER task5- visible WORK reg
static int SCAT_CHNL6		=	0x06; // SCATTER task6- visible WORK reg
static int SCAT_CHNL7		=	0x07; // SCATTER task7- visible WORK reg


static int SINTEN_IMMED		=	0xE0; // use WORK# as immediate data
static int SINTEN_HIGH		=	0xC0;
static int SINTEN_MID		=	0x60; // mid scatter intensity 
static int SINTEN_LOW		=	0x40;
static int SINTEN_MIN		=	0x20;
static int SINTEN_OFF		=	0x00;

// ======================
static int DEFAULT_GLBL_GAIN	=	0xC0;
static int MERGE_ALL_MASK		=	0; // any 0 bit is involved

// ----2222--------------
// 2ND modify ARG

static int SNDCV_SRC	    =	0x80; // this bit set chngs from IMMED to SND_CV to control EFFECT span
static int SNDCV_SRC_BIT	=	7; // 1=ls 7 bits SCV#, 0=ls 7 bits immed arg data

// ls 7 bit ARGS
static int MAXG_NONE	=	0x00; // value of 0 means no scaling, SRC is 100% GAIN
static int MAXP_NONE	=	0x00; // value of 0 means no scaling, SRC is 100% PITCH


// ----3333--------------
// 3RD modify ARG
//;MPUSH		EQU	0x80		;this bit forces LEVEL OVERIDE

static int DITHER		=	0x80; // value to change to DITHER mode
static int SUMG		=	0x40;

static int MIN_DITH	=	0x00;
static int LOW_DITH	=	0x20;
static int MID_DITH	=	0x40;
static int MAX_DITH	=	0x60;

// ;MPUSH_BIT	EQU	7  (commendted out in the original Digitrax file)

static int DITHER_BIT	=	7;
static int SUM_BIT		=	6; // 1=add ARG1/2 variation,0=multiply
static int MG1_BIT		=	5; // these code meaning of LO nibble
static int MG0_BIT		=	4;

// for CMDS 8/9 ls nibble ARG3 is scaling factor to set CV/IMMED data control SPAN
static int SCALE_F		=	0x0F;
static int SCALE_C		=	0x0C;
static int SCALE_8		=	0x08;
static int SCALE_6		=	0x06;
static int SCALE_5		=	0x05;
static int SCALE_4		=	0x04;
static int SCALE_2		=	0x02;

// DITHER ????

static int DITHERG_WHISTLE	=	0x00;
static int DITHERP_WHISTLE	=	0x00;

static int DITHERG_DIESEL	=	0x00;
static int DITHERP_DIESEL	=	0x00;

static int DITHERG_NONE	    =	0x00;
static int DITHERP_NONE	    =	0x00;

// BLEND logic commnds
// 1110-BLEND, BLEND_CMDS, BLEND_GAIN_ARG, BLEND_FASE_ARG

// BLEND cmds bit encoding
static int BLEND_TGTMASK	=	0xC0; // 2 ms bits control Blend target
static int BLEND_FASEMASK	=	0x38; // 3  bits FASE_BLEND type, 000=inactive
static int BLEND_GAINMASK	=	0x07; // 3 ls bits GAIN_BLEND type, 000=inactive

// MODE0 with ARG=0 is INACTIVE BLEND

static int BLEND_CURRENT_CHNL	=	0x00 & BLEND_TGTMASK;
static int BLEND_ALL		=	0x40 & BLEND_TGTMASK;

static int BLEND_GAIN0		=	0x00 & BLEND_GAINMASK; // STD logic 0 blend
static int BLEND_GAIN1		=	0x01 & BLEND_GAINMASK; // alt logic 1 blend

static int BLEND_FASE0		=	0x00 & BLEND_FASEMASK; // STD logic 0 blend
static int BLEND_FASE1		=	0x08 & BLEND_FASEMASK; // alt logic 1 blend


static int BLENDG_DSL_ACCEL0	=	0x04; // GAIN rate for loaded DIESEL
static int BLENDF_DSL_ACCEL0	=	0x02; // FASE rate for loaded DIESEL

static int BLENDG_DSL_ACCEL1	=	0x06; // GAIN rate for loaded DIESEL
static int BLENDF_DSL_ACCEL1	=	0x05; // GAIN rate for unloading DIESEL

static int BLENDG_DSL_DECEL0	=	0x09; // GAIN rate for unloading DIESEL
static int BLENDF_DSL_DECEL0	=	0x07; // FASE rate for unloading DIESEL


// COMPARE CMD control bits

static int TARGET_DATA	=	0x00; // second COMPARE ARG is WORK reg or SCV
static int IMMED_DATA	=	0x04; // second COMPARE ARG is IMMEDIATE 8 bit DATA

static int SKIP_SAME	=	0x00;
static int SKIP_RSVD	=	0x01; //reserved CONDITION code
static int SKIP_LESS	=	0x02;
static int SKIP_GRTR	=	0x03;

static int COMP_ALL	=	0x00; // 1 bits= do not include
static int COMP_7LSB	=	0X80;

// define fixed CV locations

static int SNDCV_CONFIGA    = 0x80; // CV129= config byte
static int SNDCV_CONFIGB    = 0x81; //CV130= config byte
static int SCV_DCONFIG      = 0x82; // CV131= diesel config
static int SCV_NOTCH        = 0x83; // CV132= typ DIESEL NOTCH rate byte
static int SNDCV_STEAM      = 0x84; // CV133= typ steam CAM config byte, x80=EXT or 1-127=DRIVER dia in inches
static int SCV_STGEAR       = 0x85; // CV134= steam gear ratio trim
static int SCV_MUTE_VOL     = 0x86; // CV135= vol level when MUTE action is triggered, e.g. F8=ON
static int SCV_MAIN_PITCH   = 0x87; // CV136
static int SCV_137          = 0x88;
static int SCV_138          = 0x89; // SCV138= 
static int SCV_DISTANCE_RATE = 0x8A; // SCV139= mask for controlling DISTANCE rate event/trigger
static int SCV_FREEFORM     = 0x8B; // SCV_140, here the SCV's are SDF defined
	
// CV133, SNDCV_STEAM /DRIVER size bit7=1 means EXTERNAL INPUT_0 generates a special CAM code as well

static int STEAM_CAM_BIT = 7;

// =======end of FIXED DATA assignments==============

// =================
// USER choices
// SCAT_AIRCOMP_RATE	EQU	25		;AIR compressor about 2 mins, above 128 is SNDCV foR RATE ARG
// SCAT_DRIER_RATE		EQU	10		;about 10 secs for DRIER
// SCAT_WATERPUMP_RATE	EQU	100
// SCAT_COAL_RATE		EQU	120


// specific DFLT values to scale MAX ranges
static int MAXG_WHISTLE	=	0x6A;
static int MAXP_WHISTLE	=	0x07;
static int MAXG_DIESEL	=	0x1C;
static int MAXP_DIESEL	=	0x3E;
static int MAXG_STEAM	=	0x1C;
static int MAXP_STEAM	=	0x7E; // was 3E- make more explosive!



}
/* @(#)SdfMacro.java */
