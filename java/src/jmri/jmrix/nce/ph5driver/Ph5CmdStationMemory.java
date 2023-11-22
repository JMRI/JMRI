package jmri.jmrix.nce.ph5driver;

/**
 * Updates for command station memory of the PH5.
 *
 * @author kcameron Copyright (C) 2023
 */
public class Ph5CmdStationMemory extends jmri.jmrix.nce.NceCmdStationMemory {

    public Ph5CmdStationMemory() {
        super();
    }
    
    public int CS_CAB_MEM_PRO = 0x0000; // start of NCE CS cab context page for cab 0, PowerPro/CS2
    public int CS_COMP_CAB_MEM_PRO = 0x3C00; // start of computer cab context page, PowerPro/CS2
    public int CS_CONSIST_MEM = 0x4e00;  // start of NCE CS Consist memory
    public int CS_CON_MEM_REAR = 0x4F00;  // start of rear consist locos
    public int CS_CON_MEM_MID = 0x5000;  // start of mid consist locos
    public int CS_CONSIST_SIZE = 0x0600;  // size of NCE CS Consist memory
    public int CS_CON_MIN = 1;
    public int CS_CON_MAX = 127;
    public int CS_MACRO_MEM = 0x6000; // start of NCE CS Macro memory 
    public int CS_MAX_MACRO = 255;  // there are 256 possible macros
    public int CS_MACRO_SIZE = 20;  // 20 bytes per macro
    
    @Override
    public int getAccyMemAddr() {
        return 0x5400;
    }

    @Override
    public int getAccyMemSize() {
        return 0x100;
    }
    public int CS_CLOCK_MEM_ADDR = 0x3E00; // base for clock values

    /*
     * give the AIU flag address
     */
    @Override
    public int getAiuFlagAddr() {
        return 0x3E15;
    }
    
    public int CS_MIN_CAB = 0;    // min number of cab memory slots
    public int CS_MAX_CAB = 59;    // number of cab memory slots

    public int NUM_CONSIST = 96;   // number of lines in the file

    public static int CAB_LINE_1 = 0;  // start of first line for cab display
    public static int CAB_LINE_2 = 16;  // start of second line for cab display
    public static int CAB_SIZE = 256;  // Each cab has 256 bytes
    public static int CAB_CURR_SPEED = 32; // NCE cab speed
    public static int CAB_ADDR_H = 33;   // loco address, high byte
    public static int CAB_ADDR_L = 34;   // loco address, low byte
    public static int CAB_FLAGS = 35;  // FLAGS
    public static int CAB_FUNC_L = 36;  // Function keys low
    public static int CAB_FUNC_H = 37;  // Function keys high
    public static int CAB_ALIAS = 38;  // Consist address
    public static int CAB_FUNC_13_20 = 39; // Function keys 13 - 30
    public static int CAB_FUNC_21_28 = 40; // Function keys 21 - 28
    public static int CAB_FUNC_29_36 = 41; // Function keys 29 - 36
    public static int CAB_FUNC_37_44 = 42; // Function keys 37 - 44
    public static int CAB_FUNC_45_52 = 43; // Function keys 45 - 52
    public static int CAB_FUNC_53_60 = 44; // Function keys 53 - 60
    public static int CAB_FUNC_61_68 = 45; // Function keys 61 - 68
    public static int CAB_FLAGS1 = 101;  // NCE flag 1

}
