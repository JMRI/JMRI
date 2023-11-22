package jmri.jmrix.nce.usbdriver;

/**
 * Updates for command station memory of the PH5.
 *
 * @author kcameron Copyright (C) 2023
 */
public class NceCmdStationMemory extends jmri.jmrix.nce.NceCmdStationMemory {

    public int NUM_CONSIST = 16;  // number of consists supported
    public int CS_CON_SIZE = 8;  // memory size per consist entry
    /*
     * give maximum consist number
     */
    @Override
    public int getConsistMin() {
        return 112;
    }
    
    public int CS_MAX_MACRO = 16;  // there are 16 possible macros
    public int CS_MACRO_SIZE = 16;  // 16 bytes per macro

    public int CS_PG_CONSIST = 13;  // Context Page for Consist Data
    public int CS_PG_MACRO = 14;  // Context Page for Macro Data
    
    /*
     * give max cab id
     */
    @Override
    public int getCabMin() {
        return 2;    // number of cab memory slots
    }
    /*
     * give max cab id
     */
    @Override
    public int getCabMax() {
        return 10;    // number of cab memory slots
    }

    /*
     * give cab index functions 13-20
     */
    @Override
    public int getCabIdxFunct13_20() {
        return 99; // Function keys 13 - 30
    }
    
    /*
     * give cab index functions 21-28
     */
    @Override
    public int getCabIdxFunct21_28() {
        return 100; // Function keys 21 - 28
    }
    
    /*
     * give cab index for cab status
     */
    @Override
    public int getCabIdxFlag1() {
        return 70;  // NCE flag 1
    }
}
