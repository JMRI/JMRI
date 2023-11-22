package jmri.jmrix.nce.usbdriver;

/**
 * Updates for command station memory of the PH5.
 *
 * @author kcameron Copyright (C) 2023
 */
public class NceCmdStationMemory extends jmri.jmrix.nce.NceCmdStationMemory {

    public int CAB_NUM_CONSIST = 13;  // usb cab number for consist memory
    public int NUM_CONSIST = 16;  // number of consists supported
    public int CS_CON_SIZE = 8;  // memory size per consist entry
    public int CS_CON_MAX = 127;
    public int CS_CON_MIN = 112;
    public int CAB_NUM_MACRO = 14;  // usb cab number for macros
    public int CS_MAX_MACRO = 16;  // there are 16 possible macros
    public int CS_MACRO_SIZE = 16;  // 16 bytes per macro
    public int CS_MIN_CAB = 2;    // min number of cab memory slots
    public int CS_MAX_CAB = 10;    // max number of cab memory slots

    public int CS_PG_CONSIST = 13;  // Context Page for Consist Data
    public int CS_PG_MACRO = 14;  // Context Page for Macro Data

    public int CAB_LINE_1 = 0;  // start of first line for cab display
    public int CAB_LINE_2 = 16;  // start of second line for cab display
    public int CAB_SIZE = 256;  // Each cab has 256 bytes
    public int CAB_CURR_SPEED = 32; // NCE cab speed
    public int CAB_ADDR_H = 33;   // loco address, high byte
    public int CAB_ADDR_L = 34;   // loco address, low byte
    public int CAB_FLAGS = 35;  // FLAGS
    public int CAB_FUNC_L = 36;  // Function keys low
    public int CAB_FUNC_H = 37;  // Function keys high
    public int CAB_ALIAS = 38;  // Consist address
    public int CAB_FUNC_13_20 = 99; // Function keys 13 - 30
    public int CAB_FUNC_21_28 = 100; // Function keys 21 - 28
    public int CAB_FLAGS1 = 70;  // NCE flag 1
}
