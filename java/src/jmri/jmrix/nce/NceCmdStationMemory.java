package jmri.jmrix.nce;


/**
 * Contains the map for the command station and memory parts
 *
 * @author Ken Cameron Copyright (C) 2013
 * 
 */
public class NceCmdStationMemory {

    /**
     * Memory offsets for cab info in a serial connected command station
     *
     * @author kcameron
     *
     */
    public static class CabMemorySerial {

        public final static int CS_CAB_MEM_PRO = 0x8800; // start of NCE CS cab context page for cab 0, PowerPro/CS2
        public final static int CS_COMP_CAB_MEM_PRO = 0xED00; // start of computer cab context page, PowerPro/CS2
        public final static int CS_CONSIST_MEM = 0xF500;  // start of NCE CS Consist memory
        public final static int CS_CON_MEM_REAR = 0xF600;  // start of rear consist locos
        public final static int CS_CON_MEM_MID = 0xF700;  // start of mid consist locos
        public final static int CS_CON_MIN = 1;
        public final static int CS_CON_MAX = 127;
        public final static int CS_MACRO_MEM = 0xC800; // start of NCE CS Macro memory 
        public final static int CS_MAX_MACRO = 255;  // there are 256 possible macros
        public final static int CS_MACRO_SIZE = 20;  // 20 bytes per macro

        public static final int NUM_CONSIST = 96;   // number of lines in the file

        public final static int CAB_LINE_1 = 0;  // start of first line for cab display
        public final static int CAB_LINE_2 = 16;  // start of second line for cab display
        public final static int CAB_SIZE = 256;  // Each cab has 256 bytes
        public final static int CAB_CURR_SPEED = 32; // NCE cab speed
        public final static int CAB_ADDR_H = 33;   // loco address, high byte
        public final static int CAB_ADDR_L = 34;   // loco address, low byte
        public final static int CAB_FLAGS = 35;  // FLAGS
        public final static int CAB_FUNC_L = 36;  // Function keys low
        public final static int CAB_FUNC_H = 37;  // Function keys high
        public final static int CAB_ALIAS = 38;  // Consist address
        public final static int CAB_FUNC_13_20 = 82; // Function keys 13 - 30
        public final static int CAB_FUNC_21_28 = 83; // Function keys 21 - 28
        public final static int CAB_FLAGS1 = 101;  // NCE flag 1
    }

    /**
     * Memory offsets for cab info in a usb connected command station
     *
     * @author kcameron
     *
     */
    public static class CabMemoryUsb {

        public static final int CAB_NUM_CONSIST = 13;  // usb cab number for consist memory
        public static final int NUM_CONSIST = 16;  // number of consists supported
        public static final int CS_CON_SIZE = 8;  // memory size per consist entry
        public static final int CS_CON_MAX = 127;
        public static final int CS_CON_MIN = 112;
        public static final int CAB_NUM_MACRO = 14;  // usb cab number for macros
        public static final int CS_MAX_MACRO = 16;  // there are 16 possible macros
        public final static int CS_MACRO_SIZE = 16;  // 16 bytes per macro

        public static final int CS_PG_CONSIST = 13;  // Context Page for Consist Data
        public static final int CS_PG_MACRO = 14;  // Context Page for Macro Data

        public final static int CAB_LINE_1 = 0;  // start of first line for cab display
        public final static int CAB_LINE_2 = 16;  // start of second line for cab display
        public final static int CAB_SIZE = 256;  // Each cab has 256 bytes
        public final static int CAB_CURR_SPEED = 32; // NCE cab speed
        public final static int CAB_ADDR_H = 33;   // loco address, high byte
        public final static int CAB_ADDR_L = 34;   // loco address, low byte
        public final static int CAB_FLAGS = 35;  // FLAGS
        public final static int CAB_FUNC_L = 36;  // Function keys low
        public final static int CAB_FUNC_H = 37;  // Function keys high
        public final static int CAB_ALIAS = 38;  // Consist address
        public final static int CAB_FUNC_13_20 = 99; // Function keys 13 - 30
        public final static int CAB_FUNC_21_28 = 100; // Function keys 21 - 28
        public final static int CAB_FLAGS1 = 70;  // NCE flag 1
    }

    public static final int FLAGS_MASK_CONSIST_REAR = 0x80;      // bit 7 set if CAB_ADDR_x is rear loco
    public static final int FLAGS1_CABTYPE_DISPLAY = 0x00; // bit 0=0, bit 7=0;
    public static final int FLAGS1_CABTYPE_NODISP = 0x01; // bit 0=1, bit 7=0;
    public static final int FLAGS1_CABTYPE_USB = 0x80;  // bit 0=0, bit 7=1;
    public static final int FLAGS1_CABTYPE_AIU = 0x81;  // bit 0=1, bit 7=1;
    public static final int FLAGS1_CABISACTIVE = 0x02;          // if cab is active
    public static final int FLAGS1_MASK_CABTYPE = 0x81;         // Only bits 0 and 7.
    public static final int FLAGS1_MASK_CABISACTIVE = 0x02; // if cab is active

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
}


