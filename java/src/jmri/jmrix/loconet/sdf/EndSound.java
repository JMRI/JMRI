package jmri.jmrix.loconet.sdf;

/**
 * Implement the END_SOUND macro from the Digitrax sound definition language.
 *
 * This carries no additional information.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class EndSound extends SdfMacro {

    public EndSound(int byte1, int byte2) {
        this.byte1 = byte1;
        this.byte2 = byte2;
    }

    int byte1, byte2;

    @Override
    public String name() {
        return "END_SOUND"; // NOI18N
    }

    @Override
    public int length() {
        return 2;
    }

    static public SdfMacro match(SdfBuffer buff) {
        if ((buff.getAtIndex() & 0xFF) != 0x00) {
            return null;
        }
        int byte1 = buff.getAtIndexAndInc();
        int byte2 = buff.getAtIndexAndInc(); // skip bytes
        return new EndSound(byte1, byte2);
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
        return "End Sequence\n"; // NOI18N
    }

    @Override
    public String oneInstructionString() {
        return name() + '\n';
    }

    @Override
    public String allInstructionString(String indent) {
        return indent + oneInstructionString();
    }
}
