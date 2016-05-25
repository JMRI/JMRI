// FourByteMacro.java
package jmri.jmrix.loconet.sdf;

/**
 * Implement generic four-byte macros from the Digitrax sound definition
 * language
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version $Revision$
 */
public class FourByteMacro extends SdfMacro {

    public FourByteMacro(int byte1, int byte2, int byte3, int byte4) {
        bytes[0] = (byte) (byte1 & 0xFF);
        bytes[1] = (byte) (byte2 & 0xFF);
        bytes[2] = (byte) (byte3 & 0xFF);
        bytes[3] = (byte) (byte4 & 0xFF);
    }

    public String name() {
        return "Four Byte Macro";
    }

    byte[] bytes = new byte[4];

    public int length() {
        return 4;
    }

    static public SdfMacro match(SdfBuffer buff) {
        // course match
        if ((buff.getAtIndex() & 0xFF) < 0xE0) {
            return null;
        }
        return new FourByteMacro(buff.getAtIndexAndInc(),
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
        return name() + ' ' + jmri.util.StringUtil.hexStringFromBytes(bytes) + '\n';
    }

    public String oneInstructionString() {
        return name() + ' ' + jmri.util.StringUtil.hexStringFromBytes(bytes) + '\n';
    }

    public String allInstructionString(String indent) {
        return indent + oneInstructionString();
    }
}

/* @(#)FourByteMacro.java */
