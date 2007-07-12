// InitiateSound.java

package jmri.jmrix.loconet.sdf;

import java.util.ArrayList;

/**
 * Implement the INITIATE_SOUND macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.6 $
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
    
    static SkemeStart dummySkemeStart = new SkemeStart(0,0); // to get name
    static ChannelStart dummyChannelStart = new ChannelStart(0); // to get name
    static EndSound dummyEndSound = new EndSound(); // to get name

    static public SdfMacro match(SdfByteBuffer buff) {
        if ( (buff.getAtIndex()&0xF8) != 0x90) return null;
        int byte1 = buff.getAtIndexAndInc();
        int byte2 =  buff.getAtIndexAndInc();
        InitiateSound result = new InitiateSound(byte2&0x7f, (byte1&0x7) + (byte2&0x80));

        SdfMacro next = null;
        while (buff.moreData()) {
            // beware of recursion in this part of the code
            int i = buff.getIndex();
            next=decodeInstruction(buff);

            // check for end of channel
            // Since multiple InitiateSounds can appear before a EndSound,
            // we don't check for those
            if (result.name().equals(dummyEndSound.name())
                || result.name().equals(dummySkemeStart.name())
                || result.name().equals(dummyChannelStart.name())) {
                // time to start the next one; 
                // decrement index to rescan this, and 
                // return via break
                buff.restoreIndex(i);
                break;
            }
            if (result.children==null) result.children = new ArrayList(); // make sure it's initialized
            result.children.add(next);
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
