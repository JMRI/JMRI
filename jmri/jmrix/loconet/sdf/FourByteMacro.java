// FourByteMacro.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement generic four-byte macros from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.2 $
 */

class FourByteMacro extends SdfMacro {

    public FourByteMacro(byte byte1, byte byte2, byte byte3, byte byte4) {
        bytes[0] = byte1;
        bytes[1] = byte2;        
        bytes[2] = byte3;        
        bytes[3] = byte4;        
    }
    
    public String name() {
        return "Four Byte Macro";
    }
    
    byte[] bytes = new byte[4];
    
    public int length() { return 4;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        // course match
        if ( (buff.getAtIndex()&0xFF) < 0xE0) return null;
        return new FourByteMacro(buff.getAtIndexAndInc(), 
                                buff.getAtIndexAndInc(),
                                buff.getAtIndexAndInc(),
                                buff.getAtIndexAndInc());
    }
    
    public String toString() {
        return linestart+name()+' '+jmri.util.StringUtil.hexStringFromBytes(bytes)+'\n';
    }
}

/* @(#)FourByteMacro.java */
