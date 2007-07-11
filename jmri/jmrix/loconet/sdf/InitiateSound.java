// InitiateSound.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the INITIATE_SOUND macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.5 $
 */

class InitiateSound extends SdfMacro {

    public InitiateSound(int trigger, int prempt) {
        this.trigger = trigger;
        this.prempt = prempt;
    }
    
    public String name() {
        return "INITIATE_SOUND";
    }
    
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
        int byte1 = buff.getAtIndexAndInc();
        int byte2 =  buff.getAtIndexAndInc();
        return new InitiateSound(byte2&0x7f, (byte1&0x7) + (byte2&0x80));
    }
    
    public String toString() {
        String retval = linestart+name()+" "+triggerVal()+","+premptVal()+'\n';
        linestart = "        ";
        return retval;
    }
}

/* @(#)InitiateSound.java */
