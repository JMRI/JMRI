// SkipOnTrigger.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the SKIP_ON_TRIGGER macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.1 $
 */

class SkipOnTrigger extends SdfMacro {

    public SkipOnTrigger(byte byte1, byte byte2) {
        bytes[0] = byte1;
        bytes[1] = byte2;        
    }
    
    public String name() {
        return "SKIP_ON_TRIGGER";
    }
    
    byte[] bytes = new byte[2];
    
    public int length() { return 2;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        if ( (buff.getAtIndex()&0xFC) != 0x04) return null;
        return new SkipOnTrigger(buff.getAtIndexAndInc(), buff.getAtIndexAndInc());
    }
    
    public String toString() {
        return name()+' '+jmri.util.StringUtil.hexStringFromBytes(bytes)+'\n';
    }
}

/* @(#)SkipOnTrigger.java */
