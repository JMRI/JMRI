// MaskCompare.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the MASK_COMPARE macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.2 $
 */

class MaskCompare extends SdfMacro {

    public MaskCompare(byte byte1, byte byte2, byte byte3, byte byte4) {
        bytes[0] = byte1;
        bytes[1] = byte2;
        bytes[2] = byte3;
        bytes[3] = byte4;
        
        src = byte2;
        immed = byte1 & 0x04; 
        targ = byte3;
        mask = byte4;
        skip = byte1 & 0x03;
    }
    
    public String name() {
        return "MASK_COMPARE";
    }
    
    int src;
    int immed;
    int targ;
    int mask;
    int skip;
    
    byte[] bytes = new byte[4];
    
    public int length() { return 4;}
    
    String srcVal() {
        return "(src = "+src+")";
    }
    
    String immedVal() {
        if (immed != 0) return "IMMED_DATA";
        else return "TARGET_DATA";
    }
    
    String targVal() {
        return "(target = "+targ+")";
    }
    
    String maskVal() {
        return "(mask = "+mask+")";
    }
    
    String skipVal() {
        return "(skip = "+skip+")";
    }
    
    static public SdfMacro match(SdfByteBuffer buff) {
        // course match
        if ( (buff.getAtIndex()&0xF8) != 0xF8) return null;
        return new MaskCompare(buff.getAtIndexAndInc(), 
                                buff.getAtIndexAndInc(),
                                buff.getAtIndexAndInc(),
                                buff.getAtIndexAndInc());
    }
    
    public String toString() {
        return linestart+name()+' '+jmri.util.StringUtil.hexStringFromBytes(bytes)+'\n';
    }
}

/* @(#)TwoByteMacro.java */
