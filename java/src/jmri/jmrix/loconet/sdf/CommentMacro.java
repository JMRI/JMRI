// CommentMacro.java
package jmri.jmrix.loconet.sdf;

/**
 * An SdfMacro for carrying a comment
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version $Revision$
 */
public class CommentMacro extends SdfMacro {

    public CommentMacro(String comment) {
        this.comment = comment;
    }

    public String name() {
        return "";
    }

    String comment;

    public int length() {
        return 0;
    }

    static public SdfMacro match(SdfBuffer buff) {
        // never match, because this doesn't occur in byte stream
        return null;
    }

    public String toString() {
        return ";" + comment + '\n';
    }

    public String oneInstructionString() {
        return ";" + comment + '\n';
    }

    public String allInstructionString(String indent) {
        return indent + oneInstructionString();
    }
}

/* @(#)CommentMacro.java */
