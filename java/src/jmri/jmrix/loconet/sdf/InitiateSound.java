package jmri.jmrix.loconet.sdf;

import java.util.ArrayList;

/**
 * Implement the INITIATE_SOUND macro from the Digitrax sound definition
 * language
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class InitiateSound extends SdfMacro {

    public InitiateSound(int byte1, int byte2) {
        this.byte1 = byte1;
        this.byte2 = byte2;
        this.trigger = byte2 & 0x7f;
        this.prempt = (byte1 & 0x7) + (byte2 & 0x80);
    }

    @Override
    public String name() {
        return "INITIATE_SOUND"; // NOI18N
    }

    int prempt;
    int trigger;
    int byte1, byte2;

    public int getTrigger() {
        return trigger;
    }

    public void setTrigger(int t) {
        trigger = t & 0x7f;
        byte2 = (byte2 & 0x80) | (t & 0x7f);
    }

    public int getPrempt() {
        return prempt;
    }

    public void setPrempt(int prempt) {
        byte1 = (byte1 & 0xF8) | (prempt & 0x7);
        byte2 = (byte2 & 0x7F) | (prempt & 0x80);
        this.prempt = (byte1 & 0x7) + (byte2 & 0x80);
    }

    @Override
    public int length() {
        return 2;
    }

    String premptVal() {
        return decodeFlags(prempt, premptCodes, premptMasks, premptNames);
    }

    String triggerVal() {
        String trigName = jmri.util.StringUtil.getNameFromState(trigger, triggerCodes, triggerNames);
        if (trigName != null) {
            return trigName;
        }
        return "(trigger = 0x" + jmri.util.StringUtil.twoHexFromInt(trigger) + ")"; // NOI18N
    }

    static public SdfMacro match(SdfBuffer buff) {
        if ((buff.getAtIndex() & 0xF8) != 0x90) {
            return null;
        }
        int byte1 = buff.getAtIndexAndInc();
        int byte2 = buff.getAtIndexAndInc();
        InitiateSound result = new InitiateSound(byte1, byte2);

        // gather leaves underneath
        SdfMacro next;
        while (buff.moreData()) {
            // look ahead at next instruction
            int peek = buff.getAtIndex() & 0xFF;

            // if SKEME_START, CHANNEL_START, done
            // Note that INITIATE_SOUND can be nested, so isn't here
            if (peek == 0xF1
                    || peek == 0x81) {
                break;
            }

            // if this is a INITIATE_SOUND, process it _without_
            // allowing recursion
            if ((peek & 0xF8) == 0x90) {
                // manually create next
                byte1 = buff.getAtIndexAndInc();
                byte2 = buff.getAtIndexAndInc();
                next = new InitiateSound(byte1, byte2);
            } else {
                // next is leaf, keep it
                next = decodeInstruction(buff);
            }
            if (result.children == null) {
                result.children = new ArrayList<SdfMacro>(); // make sure it's initialized
            }
            result.children.add(next);

            // if this was an END_SOUND, we're done now that we've included it
            if (peek == 0x00) {
                break;
            }
        }
        return result;
    }

    /**
     * Store into a buffer.
     */
    @Override
    public void loadByteArray(SdfBuffer buffer) {
        // data
        buffer.setAtIndexAndInc(byte1);
        buffer.setAtIndexAndInc(byte2);

        // store children
        super.loadByteArray(buffer);
    }

    @Override
    public String toString() {
        return "Define Sequence " + triggerVal() + "," + premptVal() + '\n';
    }

    @Override
    public String oneInstructionString() {
        return name() + " " + triggerVal() + "," + premptVal() + '\n';
    }

    @Override
    public String allInstructionString(String indent) {
        String output = indent + oneInstructionString();
        if (children == null) {
            return output;
        }
        for (int i = 0; i < children.size(); i++) {
            output += children.get(i).allInstructionString(indent + "  ");
        }
        return output;
    }
}
