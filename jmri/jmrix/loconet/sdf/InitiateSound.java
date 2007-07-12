// InitiateSound.java

package jmri.jmrix.loconet.sdf;

import java.util.ArrayList;

/**
 * Implement the INITIATE_SOUND macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.7 $
 */

class InitiateSound extends SdfMacro {

    public InitiateSound(int trigger, int prempt) {
        this.trigger = trigger;
        this.prempt = prempt;
    }
    
    public String name() {
        return "INITIATE_SOUND";
    }
    
    int prempt;
    int trigger;
    
    public int length() { return 2;}
    
    String premptVal() {
        return decodeFlags(prempt, premptCodes, premptMasks, premptNames);
    }
    
    String triggerVal() {
        String trigName = jmri.util.StringUtil.getNameFromState(trigger, triggerCodes, triggerNames);
        if (trigName!=null) return trigName;
        return "(trigger = 0x"+jmri.util.StringUtil.twoHexFromInt(trigger)+")";
    }

    static public SdfMacro match(SdfByteBuffer buff) {
        if ( (buff.getAtIndex()&0xF8) != 0x90) return null;
        int byte1 = buff.getAtIndexAndInc();
        int byte2 =  buff.getAtIndexAndInc();
        InitiateSound result = new InitiateSound(byte2&0x7f, (byte1&0x7) + (byte2&0x80));

        // gather leaves underneath
        SdfMacro next;
        while (buff.moreData()) {
            // look ahead at next instruction
            int peek = buff.getAtIndex()&0xFF;
            
            // if SKEME_START, CHANNEL_START, done
            // Note that INITIATE_SOUND can be nested, so isn't here
            if (peek == 0xF1
                || peek == 0x81) {
                break;
            }
            
            // next is leaf, keep it
            next=decodeInstruction(buff);
            if (result.children==null) result.children = new ArrayList(); // make sure it's initialized
            result.children.add(next);
            
            // if this was an END_SOUND, we're done now that we've included it
            if (peek == 0x00) break;
        }
        return result;
    }
    
    public String toString() {
        String output = linestart+name()+" "+triggerVal()+","+premptVal()+'\n';
        linestart = "        ";
        if (children==null) return output;
        for (int i = 0; i<children.size(); i++) {
            output+= ((SdfMacro)children.get(i)).toString();
        }
        return output;
    }
}

/* @(#)InitiateSound.java */
