// ChannelStart.java
package jmri.jmrix.loconet.sdf;

import java.util.ArrayList;

/**
 * Implement the CHANNEL_START macro from the Digitrax sound definition language
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version $Revision$
 */
public class ChannelStart extends SdfMacro {

    public ChannelStart(int number) {
        this.number = number;
    }

    public String name() {
        return "CHANNEL_START";
    }

    int number;

    public int length() {
        return 2;
    }

    static public SdfMacro match(SdfBuffer buff) {
        if ((buff.getAtIndex() & 0xFF) != 0x81) {
            return null;
        }
        buff.getAtIndexAndInc(); // drop opcode
        ChannelStart result = new ChannelStart(buff.getAtIndexAndInc());

        // gather leaves underneath
        SdfMacro next;
        while (buff.moreData()) {
            // look ahead at next instruction
            int peek = buff.getAtIndex() & 0xFF;

            // if SKEME_START or CHANNEL_START, done
            if (peek == 0xF1
                    || peek == 0x81) {
                break;
            }

            // next is leaf, keep it
            next = decodeInstruction(buff);
            if (result.children == null) {
                result.children = new ArrayList<SdfMacro>(); // make sure it's initialized
            }
            result.children.add(next);
        }
        return result;
    }

    /**
     * Store into a buffer.
     */
    public void loadByteArray(SdfBuffer buffer) {
        // data
        buffer.setAtIndexAndInc(0x81);
        buffer.setAtIndexAndInc(number);

        // store children
        super.loadByteArray(buffer);
    }

    public String toString() {
        return "Channel " + number + '\n';
    }

    public String oneInstructionString() {
        return name() + ' ' + number + '\n';
    }

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

/* @(#)ChannelStart.java */
