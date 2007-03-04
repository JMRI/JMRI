// LoadModifier.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the LOAD_MODIFIER macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.2 $
 */

class LoadModifier extends SdfMacro {

    public LoadModifier(byte byte1, byte byte2, byte byte3, byte byte4) {
        bytes[0] = byte1;
        bytes[1] = byte2;        
        bytes[2] = byte3;        
        bytes[3] = byte4;        
    }
    
    public String name() {
        return "LOAD_MODIFIER";
    }
    
    byte[] bytes = new byte[4];
    
    public int length() { return 4;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        // course match
        if ( (buff.getAtIndex()&0xF0) != 0xE0) return null;
        return new LoadModifier(buff.getAtIndexAndInc(), 
                                buff.getAtIndexAndInc(),
                                buff.getAtIndexAndInc(),
                                buff.getAtIndexAndInc());
    }
    
    public String toString() {
        return linestart+name()+' '+jmri.util.StringUtil.hexStringFromBytes(bytes)+'\n';
    }
}

/* @(#)LoadModifier.java */
