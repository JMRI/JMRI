// SkemeStart.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the SKEME_START macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.4 $
 */

class SkemeStart extends SdfMacro {

    public SkemeStart(int number, int length) {
        this.number = number;
        this.length = length;     
    }
    
    int number;
    int length;
    
    public String name() {
        return "SKEME_START";
    }
    
    public int length() { return 4;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        // course match
        if ( (buff.getAtIndex()&0xFF) != 0xF1) return null;
        
        buff.getAtIndexAndInc(); // skip op code
        int byte2 = buff.getAtIndexAndInc();
        int byte3 = buff.getAtIndexAndInc();
        int byte4 = buff.getAtIndexAndInc();
        
        return new SkemeStart( byte2, byte3*256+byte4);
    }
    
    public String toString() {
        linestart = "    ";
        return linestart+name()+' '+number+"; length="+length+'\n';
    }
}

/* @(#)SdfMacro.java */
