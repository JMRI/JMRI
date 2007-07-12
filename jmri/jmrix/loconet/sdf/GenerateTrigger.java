// GenerateTrigger.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the GENERATE_TRIGGER macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.4 $
 */

class GenerateTrigger extends SdfMacro {

    public GenerateTrigger(int trigger) {
        this.trigger = trigger;
    }
    
    public String name() {
        return "GENERATE_TRIGGER";
    }
    
    int trigger;
        
    public int length() { return 2;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        if ( (buff.getAtIndex()&0xFF) != 0xB1) return null;
        buff.getAtIndexAndInc(); // skip op ocde
        return new GenerateTrigger(buff.getAtIndexAndInc());
    }
    
    String triggerVal() {
        String trigName = jmri.util.StringUtil.getNameFromState(trigger, triggerCodes, triggerNames);
        if (trigName!=null) return trigName;
        return "(trigger = 0x"+jmri.util.StringUtil.twoHexFromInt(trigger)+")";
    }

    public String toString() {
        return name()+' '+triggerVal()+'\n';
    }
    public String oneInstructionString() {
        return name()+' '+triggerVal()+'\n';
    }
    public String allInstructionString(String indent) {
        return indent+oneInstructionString();
    }
}

/* @(#)GenerateTrigger.java */
