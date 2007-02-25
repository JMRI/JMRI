// Play.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the PLAY macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.1 $
 */

class Play extends SdfMacro {

    public Play(byte byte1, byte byte2) {
        bytes[0] = byte1;
        bytes[1] = byte2;        
    }
    
    public String name() {
        return "PLAY";
    }
    
    byte[] bytes = new byte[2];
    
    public int length() { return 2;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        // course match
        if ( (buff.getAtIndex()&0xC0) != 0x40) return null;
        return new Play(buff.getAtIndexAndInc(), 
                                buff.getAtIndexAndInc());
    }
    
    public String toString() {
        return name()+' '+jmri.util.StringUtil.hexStringFromBytes(bytes)+'\n';
    }
}

/* @(#)Play.java */
