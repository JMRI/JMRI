// SkipOnTrigger.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the SKIP_ON_TRIGGER macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.4 $
 */

class SkipOnTrigger extends SdfMacro {

    public SkipOnTrigger(int logic, int trigger) {
        this.logic = logic;
        this.trigger = trigger;
    }
    
    public String name() {
        return "SKIP_ON_TRIGGER";
    }
        
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
        int byte1 = buff.getAtIndexAndInc();
        int byte2 = buff.getAtIndexAndInc();
        return new SkipOnTrigger(byte1&0x03, byte2);
    }
    
    public String toString() {
        return name()+' '+logicVal()+", "+triggerVal()+'\n';
    }
    public String oneInstructionString() {
        return name()+' '+logicVal()+", "+triggerVal()+'\n';
    }
    public String allInstructionString(String indent) {
        return indent+oneInstructionString();
    }
}

/* @(#)SkipOnTrigger.java */
