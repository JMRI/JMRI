// SkemeStart.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the SKEME_START macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.2 $
 */

class SkemeStart extends SdfMacro {

    public SkemeStart(byte byte1, byte byte2, byte byte3) {
        bytes[0] = byte1;
        bytes[1] = byte2;        
        bytes[2] = byte3;   
        number = byte1;
        length = byte2*256+byte3;     
    }
    
    int number;
    int length;
    
    public String name() {
        return "SKEME_START";
    }
    
    byte[] bytes = new byte[3];
    
    public int length() { return 4;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        // course match
        if ( (buff.getAtIndex()&0xFF) != 0xF1) return null;
        
        buff.getAtIndexAndInc(); // skip op code
        return new SkemeStart( buff.getAtIndexAndInc(),
                               buff.getAtIndexAndInc(),
                               buff.getAtIndexAndInc());
    }
    
    public String toString() {
        return name()+' '+number+"; length="+length+'\n';
    }
}

/* @(#)SdfMacro.java */
