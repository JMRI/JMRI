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

    /*
     * give addr for cab data base
     */
    @Override
    public int getCabAddr() {
        return 0x0000; // start of NCE CS cab context page for cab 0
    }

    /*
     * give max cab id
     */
    @Override
    public int getCabMax() {
        return 59;    // number of cab memory slots
    }
    
    /*
     * give addr computer cab
     */
    @Override
    public int giveCompCabAddr() {
        return 0x3C00; // start of computer cab context page, PowerPro/CS2
    }
    
    /*
     * give addr consist head
     */
    @Override
    public int getConsistHeadAddr() {
        return 0x4e00;  // start of NCE CS Consist Head memory
    }

    /*
     * give addr consist tail
     */
    @Override
    public int getConsistTailAddr() {
        return 0x4F00;  // start of NCE CS Consist Tail memory
    }
    
    /*
     * give addr consist middle
     */
    @Override
    public int getConsistMidAddr() {
        return 0x5000;  // start of NCE CS Consist memory
    }

    /*
     * give addr macro table
     */
    @Override
    public int getMacroAddr() {
        return 0x6000; // start of NCE CS Macro memory 
    }
    
    @Override
    public int getAccyMemAddr() {
        return 0x5400;
    }

    @Override
    public int getAccyMemSize() {
        return 0x100;
    }
    
    /*
     * give base addr for clock operations
     */
    @Override
    public int getClockAddr() {
        return 0x3E00; // base for clock values
    }

    /*
     * give the AIU flag address
     */
    @Override
    public int getAiuFlagAddr() {
        return 0x3E15;
    }
    
    public int CS_MIN_CAB = 0;    // min number of cab memory slots
    public int CS_MAX_CAB = 59;    // number of cab memory slots

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
