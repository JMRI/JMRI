// ChannelStart.java

package jmri.jmrix.loconet.sdf;

/**
 * Implement the CHANNEL_START macro from the Digitrax sound definition language
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.1 $
 */

class ChannelStart extends SdfMacro {

    public ChannelStart(byte byte1, byte byte2) {
        bytes[0] = byte1;
        bytes[1] = byte2;        
    }
    
    public String name() {
        return "CHANNEL_START";
    }
    
    byte[] bytes = new byte[2];
    
    public int length() { return 2;}
    
    static public SdfMacro match(SdfByteBuffer buff) {
        if ( (buff.getAtIndex()&0xFF) != 0x81) return null;
        return new ChannelStart(buff.getAtIndexAndInc(), buff.getAtIndexAndInc());
    }
    
    public String toString() {
        return name()+' '+jmri.util.StringUtil.hexStringFromBytes(bytes)+'\n';
    }
}

/* @(#)ChannelStart.java */
