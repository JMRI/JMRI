// InitiateSound.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the INITIATE_SOUND macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.2 $
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
        String[] names = jmri.util.StringUtil.getNamesFromStateMasked(prempt, premptCodes, premptMasks, premptNames);
        if (names == null) return "<ERROR>"; // unexpected case, internal error, should also log?
        else if (names.length == 0) return premptNames[premptNames.length-1];  // last name is non-of-above special case
        else if (names.length == 1) return names[0];
        String output = names[0];
        for (int i=1; i<names.length; i++)
            output+="+"+names[i];
        return output;
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
        return name()+" "+triggerVal()+","+premptVal()+'\n';
    }
}

/* @(#)InitiateSound.java */
