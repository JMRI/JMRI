// LabelMacro.java
package jmri.jmrix.loconet.sdf;

/**
 * An SdfMacro for carrying a comment
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version $Revision$
 */
public class LabelMacro extends SdfMacro {

    public LabelMacro(String label) {
        this.label = label;
    }

    public String name() {
        return "";
    }

    String label;

    public int length() {
        return 0;
    }

    static public SdfMacro match(SdfBuffer buff) {
        // never match, because this doesn't occur in byte stream
        return null;
    }

    /**
     * Store into a buffer.
     */
    public void loadByteArray(SdfBuffer buffer) {
        // store children
        super.loadByteArray(buffer);
    }

    public String toString() {
        return label + '\n';
    }

    public String oneInstructionString() {
        return label + '\n';
    }

    public String allInstructionString(String indent) {
        // not indented
        return oneInstructionString();
    }
}

/* @(#)LabelMacro.java */
