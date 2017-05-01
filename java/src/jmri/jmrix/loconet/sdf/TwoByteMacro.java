package jmri.jmrix.loconet.sdf;

/**
 * Implement generic two-byte macros from the Digitrax sound definition language
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class TwoByteMacro extends SdfMacro {

    public TwoByteMacro(int byte1, int byte2) {
        bytes[0] = (byte) (byte1 & 0xFF);
        bytes[1] = (byte) (byte2 & 0xFF);
    }

    @Override
    public String name() {
        return "Two Byte Macro"; // NOI18N
    }

    byte[] bytes = new byte[2];

    @Override
    public int length() {
        return 2;
    }

    static public SdfMacro match(SdfBuffer buff) {
        // always match
        return new TwoByteMacro(buff.getAtIndexAndInc(), buff.getAtIndexAndInc());
    }

    /**
     * Store into a buffer.
     */
    @Override
    public void loadByteArray(SdfBuffer buffer) {
        // data
        buffer.setAtIndexAndInc(bytes[0]);
        buffer.setAtIndexAndInc(bytes[1]);

        // store children
        super.loadByteArray(buffer);
    }

    @Override
    public String toString() {
        return name() + ' ' + jmri.util.StringUtil.hexStringFromBytes(bytes) + '\n';
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
