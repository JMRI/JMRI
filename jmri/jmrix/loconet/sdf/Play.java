// Play.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the PLAY macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.5 $
 */

class Play extends SdfMacro {

    public Play(int handle, int wavebrkFlags, int brk) {
        this.handle = handle;
        this.wavebrkFlags = wavebrkFlags;
        this.brk = brk;
    }
    
    public String name() {
        return "PLAY";
    }
    
    int handle;
    int brk;  // "break" is a reserved word
    int wavebrkFlags;
    
    String handleVal() {
        return ""+handle;
    }
    
    String brkVal() {
        return decodeFlags(brk, loopCodes, loopMasks, loopNames);
    }
    
    String wavebrkFlagsVal() {
        return decodeFlags(wavebrkFlags, wavebrkCodes, wavebrkMasks, wavebrkNames);
    }
    
    public int length() { return 2;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        // course match
        if ( (buff.getAtIndex()&0xC0) != 0x40) return null;
        int byte1 = buff.getAtIndexAndInc();
        int byte2 = buff.getAtIndexAndInc();
        return new Play(byte2&0x3F, byte1&0x18, ((byte1&0x7)<<2)+((byte2&0xC0)>>6) );
    }
    
    public String toString() {
        return "Play fragment "+handleVal()+'\n';
    }
    public String oneInstructionString() {
        return name()+' '+handleVal()+','+brkVal()+','+wavebrkFlagsVal()+'\n';
    }
    public String allInstructionString(String indent) {
        return indent+oneInstructionString();
    }
}

/* @(#)Play.java */
