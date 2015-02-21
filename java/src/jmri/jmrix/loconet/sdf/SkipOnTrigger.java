// SkipOnTrigger.java
package jmri.jmrix.loconet.sdf;

/**
 * Implement the SKIP_ON_TRIGGER macro from the Digitrax sound definition
 * language
 *
 * @author	Bob Jacobsen Copyright (C) 2007, 2008
 * @version $Revision$
 */
public class SkipOnTrigger extends SdfMacro {

    public SkipOnTrigger(int byte1, int byte2) {
        this.byte1 = byte1;
        this.byte2 = byte2;
        this.logic = byte1 & 0x03;
        this.trigger = byte2;
    }

    public String name() {
        return "SKIP_ON_TRIGGER";
    }

    int byte1, byte2;

    int logic;
    int trigger;

    public int length() {
        return 2;
    }

    String logicVal() {
        return decodeFlags(logic, trigLogicCodes, trigLogicMasks, trigLogicNames);
    }

    String triggerVal() {
        String trigName = jmri.util.StringUtil.getNameFromState(trigger, triggerCodes, triggerNames);
        if (trigName != null) {
            return trigName;
        }
        return "(trigger = 0x" + jmri.util.StringUtil.twoHexFromInt(trigger) + ")";
    }

    static public SdfMacro match(SdfBuffer buff) {
        if ((buff.getAtIndex() & 0xFC) != 0x04) {
            return null;
        }
        int byte1 = buff.getAtIndexAndInc();
        int byte2 = buff.getAtIndexAndInc();
        return new SkipOnTrigger(byte1, byte2);
    }

    /**
     * Store into a buffer.
     */
    public void loadByteArray(SdfBuffer buffer) {
        // data
        buffer.setAtIndexAndInc(byte1);
        buffer.setAtIndexAndInc(byte2);

        // store children
        super.loadByteArray(buffer);
    }

    public String toString() {
        return "Skip on Trigger\n";
    }

    public String oneInstructionString() {
        return name() + ' ' + logicVal() + ", " + triggerVal() + '\n';
    }

    public String allInstructionString(String indent) {
        return indent + oneInstructionString();
    }
}

/* @(#)SkipOnTrigger.java */
