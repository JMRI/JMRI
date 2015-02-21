// DelaySound.java
package jmri.jmrix.loconet.sdf;

/**
 * Implement the DELAY_SOUND macro from the Digitrax sound definition language
 *
 * @author	Bob Jacobsen Copyright (C) 2007, 2008
 * @version $Revision$
 */
public class DelaySound extends SdfMacro {

    public DelaySound(int byte1, int byte2) {
        this.mode = byte2 & 0x80;
        this.value = byte2 & 0x7F;
        this.glbl = byte1 & 0x01;
        this.byte1 = byte1;
        this.byte2 = byte2;
    }

    public String name() {
        return "DELAY_SOUND";
    }

    int mode;
    int value;
    int glbl;
    int byte1, byte2;

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
    public void loadByteArray(SdfBuffer buffer) {
        // data
        buffer.setAtIndexAndInc(byte1);
        buffer.setAtIndexAndInc(byte2);

        // store children
        super.loadByteArray(buffer);
    }

    public String toString() {
        return "Delay Sound\n";
    }

    public String oneInstructionString() {
        String modeVal = (DELAY_THIS == mode) ? "DELAY_THIS" : "DELAY_CV";
        String valueVal = (DELAY_THIS == mode) ? "" + value : "CV=" + value;
        String glblVal = (glbl == 1) ? "DELAY_GLOBAL" : "0";  // what should 0 case be?
        return name() + ' ' + modeVal + "," + valueVal + "," + glblVal + '\n';
    }

    public String allInstructionString(String indent) {
        return indent + oneInstructionString();
    }
}

/* @(#)DelaySound.java */
