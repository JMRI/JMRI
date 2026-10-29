package jmri.jmrix.loconet.sdf;

/**
 * An SdfMacro for carrying a comment.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class CommentMacro extends SdfMacro {

    public CommentMacro(String comment) {
        this.comment = comment;
    }

    @Override
    public String name() {
        return "";
    }

    String comment;

    @Override
    public int length() {
        return 0;
    }

    public static SdfMacro match(SdfBuffer buff) {
        // never match, because this doesn't occur in byte stream
        return null;
    }

    @Override
    public String toString() {
        return ";" + comment + '\n';
    }

    @Override
    public String oneInstructionString() {
        return ";" + comment + '\n';
    }

    @Override
    public String allInstructionString(String indent) {
        return indent + oneInstructionString();
    }
}
