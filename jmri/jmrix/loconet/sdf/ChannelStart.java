// ChannelStart.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the CHANNEL_START macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.2 $
 */

class ChannelStart extends SdfMacro {

    public ChannelStart(int number) {
        this.number = number;      
    }
    
    public String name() {
        return "CHANNEL_START";
    }
    
    int number;
        
    public int length() { return 2;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        if ( (buff.getAtIndex()&0xFF) != 0x81) return null;
        buff.getAtIndexAndInc(); // drop opcode
        return new ChannelStart(buff.getAtIndexAndInc());
    }
    
    public String toString() {
        linestart = "    "; // shouldn't be here, needs to be stacked later
        String result = linestart+name()+' '+number+'\n';
        linestart = "      ";
        return result;
    }
}

/* @(#)ChannelStart.java */
