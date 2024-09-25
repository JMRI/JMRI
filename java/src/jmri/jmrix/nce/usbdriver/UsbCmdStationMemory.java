package jmri.jmrix.nce.usbdriver;

/**
 * Updates for command station memory of the PH5.
 *
 * @author Ken Cameron Copyright (C) 2023
 */
public class UsbCmdStationMemory extends jmri.jmrix.nce.NceCmdStationMemory {

    /*
     * give maximum consist number
     */
    @Override
    public int getConsistMin() {
        return 112;
    }
        
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
    
    /*
     * give cab addr macro table
     */
    @Override
    public int getMacroAddr() {
        return 0x0E; // start of NCE CS Macro memory 
    }

    /*
     * give size of macro entry
     */
    @Override
    public int getMacroSize() {
        return 16;  // 16 bytes per macro
    }
    
    /*
     * give number of macro limit
     */
    @Override
    public int getMacroLimit() {
        return 16;  // there are 16 possible macros
    }
}
