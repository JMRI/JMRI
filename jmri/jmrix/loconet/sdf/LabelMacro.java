// LabelMacro.java

package jmri.jmrix.loconet.sdf;

/**
 * An SdfMacro for carrying a comment
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.2 $
 */

class LabelMacro extends SdfMacro {

    public LabelMacro(String label) {
        this.label = label;        
    }
    
    public String name() {
        return "";
    }
    
    String label;
    
    public int length() { return 0;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        // never match, because this doesn't occur in byte stream
        return null;
    }
    
    public String toString() {
        return label+'\n';
    }
    public String oneInstructionString() {
        return label+'\n';
    }
    public String allInstructionString(String indent) {
        // not indented
        return oneInstructionString();
    }
}

/* @(#)LabelMacro.java */
