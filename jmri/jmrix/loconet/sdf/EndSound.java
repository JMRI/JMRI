// EndSound.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the END_SOUND macro from the Digitrax sound definition language.
 *
 * This carries no additional information.
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.4 $
 */

class EndSound extends SdfMacro {

    public EndSound() {
    }
    
    public String name() {
        return "END_SOUND";
    }
    
    public int length() { return 2;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        if ( (buff.getAtIndex()&0xFF) != 0x00) return null;
        buff.getAtIndexAndInc();
        buff.getAtIndexAndInc(); // skip bytes
        return new EndSound();
    }
    
    public String toString() {
        linestart = "      ";
        return linestart+name()+'\n';
    }
}

/* @(#)EndSound.java */
