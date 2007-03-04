// EndSound.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the CHANNEL_START macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.2 $
 */

class EndSound extends SdfMacro {

    public EndSound(byte byte1, byte byte2) {
        bytes[0] = byte1;
        bytes[1] = byte2;        
    }
    
    public String name() {
        return "END_SOUND";
    }
    
    byte[] bytes = new byte[2];
    
    public int length() { return 2;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        if ( (buff.getAtIndex()&0xFF) != 0x00) return null;
        return new EndSound(buff.getAtIndexAndInc(), buff.getAtIndexAndInc());
    }
    
    public String toString() {
        return linestart+name()+'\n';
    }
}

/* @(#)EndSound.java */
