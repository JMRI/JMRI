package jmri.jmrix.loconet.sdf;

import java.util.ArrayList;

/**
 * Implement the SKEME_START macro from the Digitrax sound definition language.
 * <p>
 * This nests until the next SKEME_START.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2008
 */
public class SkemeStart extends SdfMacro {

    public SkemeStart(int byte1, int byte2, int byte3, int byte4) {
        this.byte1 = byte1;
        this.byte2 = byte2;
        this.byte3 = byte3;
        this.byte4 = byte4;

        this.number = byte2;
        this.length = byte3 * 256 + byte4;
    }

    int byte1, byte2, byte3, byte4;

    int number;
    int length;

    public int getNumber() {
        return number;
    }

    public void setNumber(int num) {
        number = num;
        byte2 = num;
    }

    @Override
    public String name() {
        return "SKEME_START"; // NOI18N
    }

    @Override
    public int length() {
        return 4;
    }

    static public SdfMacro match(SdfBuffer buff) {
        // course match
        if ((buff.getAtIndex() & 0xFF) != 0xF1) {
            return null;
        }

        int byte1 = buff.getAtIndexAndInc(); // skip op code
        int byte2 = buff.getAtIndexAndInc();
        int byte3 = buff.getAtIndexAndInc();
        int byte4 = buff.getAtIndexAndInc();

        SkemeStart result = new SkemeStart(byte1, byte2, byte3, byte4);

        // gather leaves underneath
        SdfMacro next;
        while (buff.moreData()) {
            // look ahead at next instruction
            int peek = buff.getAtIndex() & 0xFF;

            // if SKEME_START, done
            if (peek == 0xF1) {
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
    @Override
    public void loadByteArray(SdfBuffer buffer) {
        // data
        buffer.setAtIndexAndInc(byte1);
        buffer.setAtIndexAndInc(byte2);
        buffer.setAtIndexAndInc(byte3);
        buffer.setAtIndexAndInc(byte4);

        // store children
        super.loadByteArray(buffer);
    }

    @Override
    public String toString() {
        return "Scheme " + number + "\n"; // NOI18N
    }

    @Override
    public String oneInstructionString() {
        return name() + ' ' + number + "; length=" + length + '\n'; // NOI18N
    }

    @Override
    public String allInstructionString(String indent) {
        String output;
        output = indent + oneInstructionString();

        if (children == null) {
            return output;
        }
        for (int i = 0; i < children.size(); i++) {
            output += children.get(i).allInstructionString(indent + "  ");
        }
        return output;
    }
}
