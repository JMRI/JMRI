// BranchTo.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the BRANCH_TO macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.4 $
 */

class BranchTo extends SdfMacro {

    public BranchTo(int addr, int skemebase) {
        this.addr = addr;
        this.skemebase = skemebase;
    }
    
    public String name() {
        return "BRANCH_TO";
    }
    
    public int length() { return 2;}
    
    int addr;
    int skemebase;
    
    static public SdfMacro match(SdfByteBuffer buff) {
        if ( (buff.getAtIndex()&0xF0) != 0xC0) return null;
        int taddr = (buff.getAtIndexAndInc()&0x0F)*256+(buff.getAtIndexAndInc()&0xFF);
        int toffset =0;
        return new BranchTo(taddr, toffset);
    }
    
    public String toString() {
        return "Branch\n";
    }
    public String oneInstructionString() {
        return name()+' '+addr+"; from base of "+skemebase+'\n';
    }
    public String allInstructionString(String indent) {
        return indent+oneInstructionString();
    }
}

/* @(#)BranchTo.java */
