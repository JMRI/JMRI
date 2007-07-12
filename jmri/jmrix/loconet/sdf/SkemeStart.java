// SkemeStart.java

package jmri.jmrix.loconet.sdf;

import java.util.ArrayList;

/**
 * Implement the SKEME_START macro from the Digitrax sound definition language.
 *<P>
 * This nests until the next SKEME_START.
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.6 $
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
        
        // gather leaves underneath
        SdfMacro next;
        while (buff.moreData()) {
            // look ahead at next instruction
            int peek = buff.getAtIndex()&0xFF;
            
            // if SKEME_START, done
            if (peek == 0xF1) {
                break;
            }
            
            // next is leaf, keep it
            next=decodeInstruction(buff);
            if (result.children==null) result.children = new ArrayList(); // make sure it's initialized
            result.children.add(next);
        }
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
