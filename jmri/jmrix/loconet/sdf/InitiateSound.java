// InitiateSound.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the INITIATE_SOUND macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.3 $
 */

class InitiateSound extends SdfMacro {

    public InitiateSound(byte byte1, byte byte2) {
        bytes[0] = byte1;
        bytes[1] = byte2;      
        trigger = byte2&0x7f;
        prempt = (byte1&0x7) + (byte2&0x80);
    }
    
    public String name() {
        return "INITIATE_SOUND";
    }
    
    byte[] bytes = new byte[2];
    
    int prempt;
    int trigger;
    
    public int length() { return 2;}
    
    String premptVal() {
        return decodeFlags(prempt, premptCodes, premptMasks, premptNames);
    }
    
    String triggerVal() {
        String trigName = jmri.util.StringUtil.getNameFromState(trigger, triggerCodes, triggerNames);
        if (trigName!=null) return trigName;
        return "(trigger = 0x"+jmri.util.StringUtil.twoHexFromInt(trigger)+")";
    }
    
    static public SdfMacro match(SdfByteBuffer buff) {
        if ( (buff.getAtIndex()&0xF8) != 0x90) return null;
        return new InitiateSound(buff.getAtIndexAndInc(), buff.getAtIndexAndInc());
    }
    
    public String toString() {
        return linestart+name()+" "+triggerVal()+","+premptVal()+'\n';
    }
}

/* @(#)InitiateSound.java */
