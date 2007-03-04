// Play.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the PLAY macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.2 $
 */

class Play extends SdfMacro {

    public Play(byte byte1, byte byte2) {
        bytes[0] = byte1;
        bytes[1] = byte2;
        
        handle = byte2&0x3F;
        wavebrkFlags = byte1&0x18;
        brk = ((byte1&0x7)<<2)+((byte2&0xC0)>>6); 
    }
    
    public String name() {
        return "PLAY";
    }
    
    byte[] bytes = new byte[2];
    
    int handle;
    int brk;  // "break" is a reserved word
    int wavebrkFlags;
    
    String handleVal() {
        return "(handle = "+handle+")";
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
        return new Play(buff.getAtIndexAndInc(), 
                                buff.getAtIndexAndInc());
    }
    
    public String toString() {
        return linestart+name()+' '+handleVal()+','+brkVal()+','+wavebrkFlagsVal()+'\n';
    }
}

/* @(#)Play.java */
