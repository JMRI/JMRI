// MaskCompare.java
package jmri.jmrix.loconet.sdf;

/**
 * Implement the MASK_COMPARE macro from the Digitrax sound definition language
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version $Revision$
 */
public class MaskCompare extends SdfMacro {

    public MaskCompare(int byte1, int byte2, int byte3, int byte4) {
        bytes[0] = (byte) (byte1 & 0xFF);
        bytes[1] = (byte) (byte2 & 0xFF);
        bytes[2] = (byte) (byte3 & 0xFF);
        bytes[3] = (byte) (byte4 & 0xFF);

        src = byte2;
        immed = byte1 & 0x04;
        targ = byte3;
        mask = byte4;
        skip = byte1 & 0x03;
    }

    public String name() {
        return "MASK_COMPARE";
    }

    int src;
    int immed;
    int targ;
    int mask;
    int skip;

    byte[] bytes = new byte[4];

    public int length() {
        return 4;
    }

    String srcVal() {
        return "(src = " + src + ")";
    }

    String immedVal() {
        if (immed != 0) {
            return "IMMED_DATA";
        } else {
            return "TARGET_DATA";
        }
    }

    String targVal() {
        return "(target = " + targ + ")";
    }

    String maskVal() {
        return "(mask = " + mask + ")";
    }

    String skipVal() {
        return "(skip = " + skip + ")";
    }

    static public SdfMacro match(SdfBuffer buff) {
        // course match
        if ((buff.getAtIndex() & 0xF8) != 0xF8) {
            return null;
        }
        return new MaskCompare(buff.getAtIndexAndInc(),
                buff.getAtIndexAndInc(),
                buff.getAtIndexAndInc(),
                buff.getAtIndexAndInc());
    }

    /**
     * Store into a buffer.
     */
    public void loadByteArray(SdfBuffer buffer) {
        // data
        buffer.setAtIndexAndInc(bytes[0]);
        buffer.setAtIndexAndInc(bytes[1]);
        buffer.setAtIndexAndInc(bytes[2]);
        buffer.setAtIndexAndInc(bytes[3]);

        // store children
        super.loadByteArray(buffer);
    }

    public String toString() {
        return "Check Mask\n";
    }

    public String oneInstructionString() {
        return name() + ' ' + jmri.util.StringUtil.hexStringFromBytes(bytes) + '\n';
    }

    public String allInstructionString(String indent) {
        return indent + oneInstructionString();
    }
}

/* @(#)TwoByteMacro.java */
