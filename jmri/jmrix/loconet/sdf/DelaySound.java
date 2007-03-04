// DelaySound.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the DELAY_SOUND macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.2 $
 */

class DelaySound extends SdfMacro {

    public DelaySound(int mode, int value, int glbl) {
        this.mode = mode;
        this.value = value;
        this.glbl = glbl;
    }
    
    public String name() {
        return "DELAY_SOUND";
    }
    
    int mode;
    int value;
    int glbl;
        
    public int length() { return 2;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        if ( (buff.getAtIndex()&0xFE) != 0xB4) return null;
        int byte1 = buff.getAtIndexAndInc();
        int byte2 = buff.getAtIndexAndInc();
        return new DelaySound(byte2&0x80, byte2&0x7F, byte1&0x01);
    }
    
    public String toString() {
        String modeVal = (DELAY_THIS == mode) ? "DELAY_THIS" : "DELAY_CV";
        String valueVal = (DELAY_THIS == mode) ? ""+value : "CV="+value;
        String glblVal = (glbl == 1) ? "DELAY_GLOBAL" : "0";  // what should 0 case be?
        return linestart+name()+' '+modeVal+","+valueVal+","+glblVal+'\n';
    }
}

/* @(#)DelaySound.java */
