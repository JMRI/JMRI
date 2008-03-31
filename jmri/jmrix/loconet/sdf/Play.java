// Play.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the PLAY macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.8 $
 */

public class Play extends SdfMacro {

    public Play(int byte1, int byte2) {
        this.byte1 = byte1;
        this.byte2 = byte2;
        this.handle = byte2&0x3F;
        this.wavebrkFlags = byte1&0x18;
        this.brk = ((byte1&0x7)<<2)+((byte2&0xC0)>>6) ;
        this.byte1 = byte1;
        this.byte2 = byte2;
    }
    
    public String name() {
        return "PLAY";
    }
    
    int handle;
    int brk;  // "break" is a reserved word
    int wavebrkFlags;
    
    int byte1, byte2;
    
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
    
    static public SdfMacro match(SdfBuffer buff) {
        // course match
        if ( (buff.getAtIndex()&0xC0) != 0x40) return null;
        int byte1 = buff.getAtIndexAndInc();
        int byte2 = buff.getAtIndexAndInc();
        return new Play(byte1, byte2);
    }
    
    /**
     * Store into a buffer.
     */
    public void loadByteArray(SdfBuffer buffer){
        // data
        buffer.setAtIndexAndInc(byte1);
        buffer.setAtIndexAndInc(byte2);

        // store children
        super.loadByteArray(buffer);
    }

    public String toString() {
        return "Play Fragment "+handleVal()+'\n';
    }
    public String oneInstructionString() {
        return name()+' '+handleVal()+','+brkVal()+','+wavebrkFlagsVal()+'\n';
    }
    public String allInstructionString(String indent) {
        return indent+oneInstructionString();
    }
}

/* @(#)Play.java */
