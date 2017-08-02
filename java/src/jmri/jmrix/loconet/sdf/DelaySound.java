package jmri.jmrix.loconet.sdf;

/**
 * Implement the DELAY_SOUND macro from the Digitrax sound definition language
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2008
 */
public class DelaySound extends SdfMacro {

    public DelaySound(int byte1, int byte2) {
        this.mode = byte2 & 0x80;
        this.value = byte2 & 0x7F;
        this.glbl = byte1 & 0x01;
        this.byte1 = byte1;
        this.byte2 = byte2;
    }

    @Override
    public String name() {
        return "DELAY_SOUND"; // NOI18N
    }

    int mode;
    int value;
    int glbl;
    int byte1, byte2;

    @Override
    public int length() {
        return 2;
    }

    static public SdfMacro match(SdfBuffer buff) {
        if ((buff.getAtIndex() & 0xFE) != 0xB4) {
            return null;
        }
        int byte1 = buff.getAtIndexAndInc();
        int byte2 = buff.getAtIndexAndInc();
        return new DelaySound(byte1, byte2);
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
        return "Delay Sound\n"; // NOI18N
    }

    @Override
    public String oneInstructionString() {
        String modeVal = (DELAY_THIS == mode) ? "DELAY_THIS" : "DELAY_CV"; // NOI18N
        String valueVal = (DELAY_THIS == mode) ? "" + value : "CV=" + value; // NOI18N
        String glblVal = (glbl == 1) ? "DELAY_GLOBAL" : "0";  // what should 0 case be? // NOI18N
        return name() + ' ' + modeVal + "," + valueVal + "," + glblVal + '\n'; // NOI18N
    }

    @Override
    public String allInstructionString(String indent) {
        return indent + oneInstructionString();
    }
}
