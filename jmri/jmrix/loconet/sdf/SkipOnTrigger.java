// SkipOnTrigger.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the SKIP_ON_TRIGGER macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.2 $
 */

class SkipOnTrigger extends SdfMacro {

    public SkipOnTrigger(byte byte1, byte byte2) {
        bytes[0] = byte1;
        bytes[1] = byte2;
        
        logic = byte1&0x03;
        trigger = byte2;     
    }
    
    public String name() {
        return "SKIP_ON_TRIGGER";
    }
    
    byte[] bytes = new byte[2];
    
    int logic;
    int trigger;
    
    public int length() { return 2;}
    
    String logicVal() {
        return decodeFlags(logic, trigLogicCodes, trigLogicMasks, trigLogicNames);
    }
    
    String triggerVal() {
        String trigName = jmri.util.StringUtil.getNameFromState(trigger, triggerCodes, triggerNames);
        if (trigName!=null) return trigName;
        return "(trigger = 0x"+jmri.util.StringUtil.twoHexFromInt(trigger)+")";
    }

    static public SdfMacro match(SdfByteBuffer buff) {
        if ( (buff.getAtIndex()&0xFC) != 0x04) return null;
        return new SkipOnTrigger(buff.getAtIndexAndInc(), buff.getAtIndexAndInc());
    }
    
    public String toString() {
        return linestart+name()+' '+logicVal()+", "+triggerVal()+'\n';
    }
}

/* @(#)SkipOnTrigger.java */
