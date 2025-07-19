package jmri.jmrix.nce.ph5driver;

/**
 * Updates for command station memory of the PH5.
 *
 * @author Ken Cameron Copyright (C) 2023
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
    public int getCompCabAddr() {
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

    /*
     * give cab index functions 13-20
     */
    @Override
    public int getCabIdxFunct13_20() {
        return 39; // Function keys 13 - 30
    }
    
    /*
     * give cab index functions 21-28
     */
    @Override
    public int getCabIdxFunct21_28() {
        return 40; // Function keys 21 - 28
    }
        
    /*
     * give cab index functions 29-36
     */
    public int getCabIdxFunct29_36() {
        return 41; // Function keys 29 - 36
    }
    
    /*
     * give cab index functions 37-44
     */
    public int getCabIdxFunct37_44() {
        return 42; // Function keys 37 - 44
    }
    
    /*
     * give cab index functions 45-52
     */
    public int getCabIdxFunct45_52() {
        return 43; // Function keys 45 - 52
    }
    
    /*
     * give cab index functions 53-60
     */
    public int getCabIdxFunct53_60() {
        return 44; // Function keys 53 - 60
    }
    
    /*
     * give cab index functions 53-60
     */
    public int getCabIdxFunct61_68() {
        return 45; // Function keys 61 - 68
    }

}
