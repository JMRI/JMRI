// SdfByteBuffer.java

package jmri.jmrix.loconet.sdf;

import java.io.*;

/**
 * Provide tools for reading, writing and accessing
 * Digitrax SPJ files
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.2 $
 */

public class SdfByteBuffer {

    // jmri.util.StringUtil.hexStringFromBytes
    
    public SdfByteBuffer(byte[] buffer) {
        this.buffer = buffer;
    }
    
    public SdfByteBuffer(String name) throws IOException {
        File file = new File(name);
        int length = (int)file.length();
        
        InputStream s = new java.io.BufferedInputStream(new java.io.FileInputStream(file));
        
        // Assume we can get all this in memory
        buffer = new byte[length];
        
        for (int i=0; i<length; i++) {
            buffer[i] = (byte)(s.read()&0xFF);
        }
    }


    byte[] buffer;
    
    
    private int index;
    
    public void resetIndex() { index = 0; }
    public byte getAtIndex() { return buffer[index]; }
    public byte getAtIndexAndInc() { return buffer[index++]; }
    
    public String toString() {
        resetIndex();
        String out ="";
        while (index<buffer.length) {
            SdfMacro m;

            // full 1st byte decoder
            if ( (m=ChannelStart.match(this)) != null) ; else
            if ( (m=SdlVersion.match(this)) != null) ; else
            if ( (m=SkemeStart.match(this)) != null) ; else
            if ( (m=GenerateTrigger.match(this)) != null) ; else
            if ( (m=EndSound.match(this)) != null) ; else

            // 7 bit decode
            if ( (m=DelaySound.match(this)) != null) ; else
                            
            // 6 bit decode
            if ( (m=SkipOnTrigger.match(this)) != null) ; else
            
            // 5 bit decode
            if ( (m=InitiateSound.match(this)) != null) ; else
            if ( (m=MaskCompare.match(this)) != null) ; else
            
            // 4 bit decode
            if ( (m=LoadModifier.match(this)) != null) ; else
            if ( (m=BranchTo.match(this)) != null) ; else

            // 2 bit decode
            if ( (m=Play.match(this)) != null) ; else
            
            // generics
            if ( (m=FourByteMacro.match(this)) != null) ; else
            if ( (m=TwoByteMacro.match(this)) != null) ; else
            
            // bail
            if (m==null) System.out.println("PANIC");
            out += m.toString();
        }
        return out;
    }
        
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SdfByteBuffer.class.getName());

}

/* @(#)SdfByteBuffer.java */
