// DelaySound.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the DELAY_SOUND macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.1 $
 */

class DelaySound extends SdfMacro {

    public DelaySound(byte byte1, byte byte2) {
        bytes[0] = byte1;
        bytes[1] = byte2;        
    }
    
    public String name() {
        return "DELAY_SOUND";
    }
    
    byte[] bytes = new byte[2];
    
    public int length() { return 2;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        if ( (buff.getAtIndex()&0xFE) != 0xB4) return null;
        return new DelaySound(buff.getAtIndexAndInc(), buff.getAtIndexAndInc());
    }
    
    public String toString() {
        return name()+' '+jmri.util.StringUtil.hexStringFromBytes(bytes)+'\n';
    }
}

/* @(#)DelaySound.java */
