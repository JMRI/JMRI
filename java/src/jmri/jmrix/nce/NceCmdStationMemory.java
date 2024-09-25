package jmri.jmrix.nce;

/**
 * Contains the map for the command station and memory parts
 * The default values are for the older serial command station.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Ken Cameron Copyright (C) 2013, 2023
 * 
 */
public class NceCmdStationMemory {
    
    /*
     * give addr for cab data base
     */
    public int getCabAddr() {
        return 0x8800;
    }
    
    /*
     * give cab data size
     */
    public int getCabSize() {
        return 0x100;
    }
    /*
     * give min cab id
     */
    public int getCabMin() {
        return 1;    // min number for cab memory slot
    }
    
    /*
     * give max cab id
     */
    public int getCabMax() {
        return 63;    // max number for cab memory slot
    }
    
    /*
     * give addr computer cab
     */
    public int getCompCabAddr() {
        return 0xED00; // start of computer cab context page, PowerPro/CS2
    }
    
    /*
     * give addr consist head
     */
    public int getConsistHeadAddr() {
        return 0xF500;  // start of NCE CS Consist Head memory
    }

    /*
     * give addr consist tail
     */
    public int getConsistTailAddr() {
        return 0xF600;  // start of NCE CS Consist Tail memory
    }
    
    /*
     * give addr consist middle
     */
    public int getConsistMidAddr() {
        return 0xF700;  // start of NCE CS Consist memory
    }
    
    /*
     * give size of consist mid table
     */
    public int getConsistMidSize() {
        return 0x0600;  // size of NCE CS Consist memory
    }
    
    /*
     * give minimum consist number
     */
    public int getConsistMin() {
        return 1;
    }
    
    /*
     * give maximum consist number
     */
    public int getConsistMax() {
        return 127;
    }

    /*
     * give consist num lines
     */
    public int getConsistNumLines() {
        return 96;   // number of lines in the file
    }

    /*
     * give addr macro table
     */
    public int getMacroAddr() {
        return 0xC800; // start of NCE CS Macro memory 
    }
    
    /*
     * give number of macro limit
     */
    public int getMacroLimit() {
        return 256; // there are 256 possible macros
    }
    
    /*
     * give size of macro entry
     */
    public int getMacroSize() {
        return 20;  // 20 bytes per macro
    }

    /*
     * give accessory memory address (AIU status)
     */
    public int getAccyMemAddr() {
        return 0xEC00;
    }

    /*
     * give accessory memory size (AIU status)
     */
    public int getAccyMemSize() {
        return 0x100;
    }
    
    /*
     * give the AIU flag address
     */
    public int getAiuFlagAddr() {
        return 0xDC15;
    }
    
    /*
     * give base addr for clock operations
     */
    public int getClockAddr() {
        return 0xDC00;
    }

    public static int CAB_LINE_1 = 0;  // start of first line for cab display
    public static int CAB_LINE_2 = 16;  // start of second line for cab display
    //public static int CAB_SIZE = 256;  // Each cab has 256 bytes
    public static int CAB_CURR_SPEED = 32; // NCE cab speed
    public static int CAB_ADDR_H = 33;   // loco address, high byte
    public static int CAB_ADDR_L = 34;   // loco address, low byte
    public static int CAB_FLAGS = 35;  // FLAGS
    public static int CAB_FUNC_L = 36;  // Function keys low
    public static int CAB_FUNC_H = 37;  // Function keys high
    public static int CAB_ALIAS = 38;  // Consist address
    
    /*
     * give cab index functions 13-20
     */
    public int getCabIdxFunct13_20() {
        return 82; // Function keys 13 - 30
    }
    
    /*
     * give cab index functions 21-28
     */
    public int getCabIdxFunct21_28() {
        return 83; // Function keys 21 - 28
    }
    
    /*
     * give cab index for cab status
     */
    public int getCabIdxFlag1() {
        return 101;  // NCE flag 1
    }

    public static int FLAGS_MASK_CONSIST_REAR = 0x80;      // bit 7 set if CAB_ADDR_x is rear loco
    public static int FLAGS1_CABTYPE_DISPLAY = 0x00; // bit 0=0, bit 7=0;
    public static int FLAGS1_CABTYPE_NODISP = 0x01; // bit 0=1, bit 7=0;
    public static int FLAGS1_CABTYPE_USB = 0x80;  // bit 0=0, bit 7=1;
    public static int FLAGS1_CABTYPE_AIU = 0x81;  // bit 0=1, bit 7=1;
    public static int FLAGS1_CABISACTIVE = 0x02;          // if cab is active
    public static int FLAGS1_MASK_CABTYPE = 0x81;         // Only bits 0 and 7.
    public static int FLAGS1_MASK_CABISACTIVE = 0x02; // if cab is active

    public static final int FUNC_L_F0 = 0x10;  // F0 or headlight
    public static final int FUNC_L_F1 = 0x01;  // F1
    public static final int FUNC_L_F2 = 0x02;  // F2
    public static final int FUNC_L_F3 = 0x04;  // F3
    public static final int FUNC_L_F4 = 0x08;  // F4

    public static final int FUNC_H_F5 = 0x01;  // F5
    public static final int FUNC_H_F6 = 0x02;  // F6
    public static final int FUNC_H_F7 = 0x04;  // F7
    public static final int FUNC_H_F8 = 0x08;  // F8
    public static final int FUNC_H_F9 = 0x10;  // F9
    public static final int FUNC_H_F10 = 0x20;  // F10
    public static final int FUNC_H_F11 = 0x40;  // F11
    public static final int FUNC_H_F12 = 0x80;  // F12

    public static final int FUNC_H_F13 = 0x01;  // F13
    public static final int FUNC_H_F14 = 0x02;  // F14
    public static final int FUNC_H_F15 = 0x04;  // F15
    public static final int FUNC_H_F16 = 0x08;  // F16
    public static final int FUNC_H_F17 = 0x10;  // F17
    public static final int FUNC_H_F18 = 0x20;  // F18
    public static final int FUNC_H_F19 = 0x40;  // F10
    public static final int FUNC_H_F20 = 0x80;  // F20

    public static final int FUNC_H_F21 = 0x01;  // F21
    public static final int FUNC_H_F22 = 0x02;  // F22
    public static final int FUNC_H_F23 = 0x04;  // F23
    public static final int FUNC_H_F24 = 0x08;  // F24
    public static final int FUNC_H_F25 = 0x10;  // F25
    public static final int FUNC_H_F26 = 0x20;  // F26
    public static final int FUNC_H_F27 = 0x40;  // F27
    public static final int FUNC_H_F28 = 0x80;  // F28

    public static final int FUNC_H_F29 = 0x01;  // F29
    public static final int FUNC_H_F30 = 0x02;  // F30
    public static final int FUNC_H_F31 = 0x04;  // F31
    public static final int FUNC_H_F32 = 0x08;  // F32
    public static final int FUNC_H_F33 = 0x10;  // F33
    public static final int FUNC_H_F34 = 0x20;  // F34
    public static final int FUNC_H_F35 = 0x40;  // F35
    public static final int FUNC_H_F36 = 0x80;  // F36

    public static final int FUNC_H_F37 = 0x01;  // F37
    public static final int FUNC_H_F38 = 0x02;  // F38
    public static final int FUNC_H_F39 = 0x04;  // F39
    public static final int FUNC_H_F40 = 0x08;  // F40
    public static final int FUNC_H_F41 = 0x10;  // F41
    public static final int FUNC_H_F42 = 0x20;  // F42
    public static final int FUNC_H_F43 = 0x40;  // F43
    public static final int FUNC_H_F44 = 0x80;  // F44

    public static final int FUNC_H_F45 = 0x01;  // F45
    public static final int FUNC_H_F46 = 0x02;  // F46
    public static final int FUNC_H_F47 = 0x04;  // F47
    public static final int FUNC_H_F48 = 0x08;  // F48
    public static final int FUNC_H_F49 = 0x10;  // F49
    public static final int FUNC_H_F50 = 0x20;  // F50
    public static final int FUNC_H_F51 = 0x40;  // F51
    public static final int FUNC_H_F52 = 0x80;  // F52

    public static final int FUNC_H_F53 = 0x01;  // F53
    public static final int FUNC_H_F54 = 0x02;  // F54
    public static final int FUNC_H_F55 = 0x04;  // F55
    public static final int FUNC_H_F56 = 0x08;  // F56
    public static final int FUNC_H_F57 = 0x10;  // F57
    public static final int FUNC_H_F58 = 0x20;  // F58
    public static final int FUNC_H_F59 = 0x40;  // F59
    public static final int FUNC_H_F60 = 0x80;  // F60

    public static final int FUNC_H_F61 = 0x01;  // F61
    public static final int FUNC_H_F62 = 0x02;  // F62
    public static final int FUNC_H_F63 = 0x04;  // F63
    public static final int FUNC_H_F64 = 0x08;  // F64
    public static final int FUNC_H_F65 = 0x10;  // F65
    public static final int FUNC_H_F66 = 0x20;  // F66
    public static final int FUNC_H_F67 = 0x40;  // F67
    public static final int FUNC_H_F68 = 0x80;  // F68
}


