// SdfByteBuffer.java

package jmri.jmrix.loconet.sdf;

import java.io.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide tools for reading, writing and accessing
 * Digitrax SPJ files
 *
 * @author		Bob Jacobsen  Copyright (C) 2007
 * @version             $Revision: 1.4 $
 */

public class SdfByteBuffer {

    // jmri.util.StringUtil.hexStringFromBytes
    
    public SdfByteBuffer(byte[] buffer) {
        this.buffer = buffer;
        loadArray();
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
        loadArray();
    }


    byte[] buffer;
    
    
    private int index;
    
    public void resetIndex() { index = 0; }
    public byte getAtIndex() { return buffer[index]; }
    public byte getAtIndexAndInc() { return buffer[index++]; }
    public boolean moreData() { return index<buffer.length; }
    
    public String toString() {
        String out ="";
        for (int i = 0; i<ops.size(); i++) {
            SdfMacro m = (SdfMacro)ops.get(i);

            out += m.toString();
        }
        return out;
    }
    
    public List getArray() { return ops; }
    
    void loadArray() {
        resetIndex();
        ops = new ArrayList();
        while (moreData()) {
            SdfMacro m = SdfMacro.decodeInstruction(this);
            ops.add(m);
        }
    }
    
    ArrayList ops;
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SdfByteBuffer.class.getName());

}

/* @(#)SdfByteBuffer.java */
