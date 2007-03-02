// SdfMacro.java

package jmri.jmrix.loconet.sdf;

/**
 * Common base for all the SDF macros defined by Digitrax
 * for their sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.2 $
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
        TRIG_DIRNOW_CHNG, TRIG_MOVING, TRIG_SND_ACTV11
};

static String[] triggerNames = new String[]{
        "TRIG_NEVER", "TRIG_MODE_CHNG", "TRIG_MATH", "TRIG_DISTANCE",
        "TRIG_SPD_INC", "TRIG_SPD_DEC", "TRIG_CAM", "TRIG_NOT_TRIG",
        "TRIG_SF0", "TRIG_SF1", "TRIG_SF2", "TRIG_SF3", "TRIG_SF4", 
         
        "TRIG_SF5", "TRIG_SF6", "TRIG_SF7", "TRIG_SF8",
        "TRIG_SF9", "TRIG_SF10", "TRIG_SF11", "TRIG_SF12",
        "TRIG_SF13", "TRIG_SF14", "TRIG_SF15", "TRIG_SF16",
        "TRIG_SF17", "TRIG_SF18", "TRIG_SF19", "TRIG_SF20",
        "TRIG_DIRNOW_CHNG", "TRIG_MOVING", "TRIG_SND_ACTV11"
};

}
/* @(#)SdfMacro.java */
