// MaskCompare.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the MASK_COMPARE macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.1 $
 */

class MaskCompare extends SdfMacro {

    public MaskCompare(byte byte1, byte byte2, byte byte3, byte byte4) {
        bytes[0] = byte1;
        bytes[1] = byte2;        
        bytes[2] = byte3;        
        bytes[3] = byte4;        
    }
    
    public String name() {
        return "MASK_COMPARE";
    }
    
    byte[] bytes = new byte[4];
    
    public int length() { return 4;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        // course match
        if ( (buff.getAtIndex()&0xF8) != 0xF8) return null;
        return new MaskCompare(buff.getAtIndexAndInc(), 
                                buff.getAtIndexAndInc(),
                                buff.getAtIndexAndInc(),
                                buff.getAtIndexAndInc());
    }
    
    public String toString() {
        return name()+' '+jmri.util.StringUtil.hexStringFromBytes(bytes)+'\n';
    }
}

/* @(#)TwoByteMacro.java */
