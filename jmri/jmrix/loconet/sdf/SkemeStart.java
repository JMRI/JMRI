// SkemeStart.java

package jmri.jmrix.loconet.sdf;

import java.util.ArrayList;

/**
 * Implement the SKEME_START macro from the Digitrax sound definition language.
 *<P>
 * This nests until the next SKEME_START.
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.5 $
 */

class SkemeStart extends SdfMacro {

    public SkemeStart(int number, int length) {
        this.number = number;
        this.length = length;     
    }
    
    int number;
    int length;
    
    public String name() {
        return "SKEME_START";
    }
    
    public int length() { return 4;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        // course match
        if ( (buff.getAtIndex()&0xFF) != 0xF1) return null;
        
        buff.getAtIndexAndInc(); // skip op code
        int byte2 = buff.getAtIndexAndInc();
        int byte3 = buff.getAtIndexAndInc();
        int byte4 = buff.getAtIndexAndInc();
        
        SkemeStart result = new SkemeStart( byte2, byte3*256+byte4);
        
        System.out.println("SK s");
        SdfMacro next;
        while (buff.moreData()) {
            // beware of recursion in this part of the code
            int i = buff.getIndex();
            next=decodeInstruction(buff);
            if (next.name().equals(result.name())) {
                // time to start the next one; 
                // decrement index to rescan this, and 
                // return via break
                buff.restoreIndex(i);
                return result;
            }
            if (result.children==null) result.children = new ArrayList(); // make sure it's initialized
            result.children.add(next);
        }
        System.out.println("SK e");
        return result;
    }
    
    public String toString() {
        String output;
        linestart = "    ";
        output = linestart+name()+' '+number+"; length="+length+'\n';
        if (children==null) return output;
        for (int i = 0; i<children.size(); i++) {
            output+= ((SdfMacro)children.get(i)).toString();
        }
        return output;
    }
}

/* @(#)SdfMacro.java */
