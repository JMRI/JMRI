package jmri.jmrix.loconet.sdf;

/**
 * Implement the MASK_COMPARE macro from the Digitrax sound definition language
 *
 * @author Bob Jacobsen Copyright (C) 2007
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

    @Override
    public String name() {
        return "MASK_COMPARE"; // NOI18N
    }

    int src;
    int immed;
    int targ;
    int mask;
    int skip;

    byte[] bytes = new byte[4];

    @Override
    public int length() {
        return 4;
    }

    String srcVal() {
        return "(src = " + src + ")"; // NOI18N
    }

    String immedVal() {
        if (immed != 0) {
            return "IMMED_DATA"; // NOI18N
        } else {
            return "TARGET_DATA"; // NOI18N
        }
    }

    String targVal() {
        return "(target = " + targ + ")"; // NOI18N
    }

    String maskVal() {
        return "(mask = " + mask + ")"; // NOI18N
    }

    String skipVal() {
        return "(skip = " + skip + ")"; // NOI18N
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
    @Override
    public void loadByteArray(SdfBuffer buffer) {
        // data
        buffer.setAtIndexAndInc(bytes[0]);
        buffer.setAtIndexAndInc(bytes[1]);
        buffer.setAtIndexAndInc(bytes[2]);
        buffer.setAtIndexAndInc(bytes[3]);

        // store children
        super.loadByteArray(buffer);
    }

    @Override
    public String toString() {
        return "Check Mask\n"; // NOI18N
    }

    @Override
    public String oneInstructionString() {
        return name() + ' ' + jmri.util.StringUtil.hexStringFromBytes(bytes) + '\n';
    }

    @Override
    public String allInstructionString(String indent) {
        return indent + oneInstructionString();
    }
}
