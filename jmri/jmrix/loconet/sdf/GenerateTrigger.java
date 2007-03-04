// GenerateTrigger.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the GENERATE_TRIGGER macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.2 $
 */

class GenerateTrigger extends SdfMacro {

    public GenerateTrigger(byte byte1, byte byte2) {
        bytes[0] = byte1;
        bytes[1] = byte2;        
    }
    
    public String name() {
        return "GENERATE_TRIGGER";
    }
    
    byte[] bytes = new byte[2];
    
    public int length() { return 2;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        if ( (buff.getAtIndex()&0xFF) != 0xB1) return null;
        return new GenerateTrigger(buff.getAtIndexAndInc(), buff.getAtIndexAndInc());
    }
    
    public String toString() {
        return linestart+name()+' '+jmri.util.StringUtil.hexStringFromBytes(bytes)+'\n';
    }
}

/* @(#)GenerateTrigger.java */
