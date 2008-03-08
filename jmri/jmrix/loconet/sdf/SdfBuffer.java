// SdfBuffer.java

package jmri.jmrix.loconet.sdf;

import java.io.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide tools for reading, writing and accessing
 * Digitrax SPJ files.
 *<P>
 * Maintains several representations:
 *<UL>
 *<LI>A byte array of the SDF contents after assembly.  This is not
 * complete, as it can't contain information like contents, labels,
 * etc.  Nor can it distinguish certain options with identical values
 * (e.g. several constants that boil down to a zero value)
 *<LI>An array of nested SdfMacro objects.  These contain
 * more detailed representations of the macro source, in the 
 * limit containing the entire thing.
 *</UL>
 *
 * @author		Bob Jacobsen  Copyright (C) 2007, 2008
 * @version             $Revision: 1.3 $
 */

public class SdfBuffer {

    // jmri.util.StringUtil.hexStringFromBytes
    
    public SdfBuffer(byte[] buffer) {
        this.buffer = buffer;
        loadMacroList();
    }
    
    public SdfBuffer(String name) throws IOException {
        File file = new File(name);
        int length = (int)file.length();
        
        InputStream s = new java.io.BufferedInputStream(new java.io.FileInputStream(file));
        
        // Assume we can get all this in memory
        buffer = new byte[length];
        
        for (int i=0; i<length; i++) {
            buffer[i] = (byte)(s.read()&0xFF);
        }
        loadMacroList();
    }


    
    protected int index;
    
    public void resetIndex() { index = 0; }
    public int getAtIndex() { return buffer[index]&0xFF; }
    public int getAtIndexAndInc() { return buffer[index++]&0xFF; }
    public boolean moreData() { return index<buffer.length; }
    public void setAtIndex(int data) { buffer[index] = (byte)(data&0xFF); }
    public void setAtIndexAndInc(int data) { buffer[index++] = (byte)(data&0xFF); }
    
    /**
     * Reload the byte buffer from the contained
     * instruction objects
     */
    public void loadByteArray() {
        // first get length of new array
        int length = 0;
        for (int i = 0; i<ops.size(); i++) {
            length += ((SdfMacro)ops.get(i)).totalLength();
        }
        buffer = new byte[length];
        log.debug("create buffer of length "+length);
        resetIndex();
        // recurse to store bytes
        for (int i = 0; i<ops.size(); i++) {
            ((SdfMacro)ops.get(i)).loadByteArray(this);
        }
        if (index!=length) log.error("Lengths did not match: "+index+" "+length);
    }
    
    public String toString() {
        String out ="";
        for (int i = 0; i<ops.size(); i++) {
            SdfMacro m = (SdfMacro)ops.get(i);

            out += m.allInstructionString("    ");
        }
        return out;
    }
    
    public byte[] getByteArray() { return buffer; }
    public List getMacroList() { return ops; }
    
    void loadMacroList() {
        resetIndex();
        ops = new ArrayList();
        while (moreData()) {
            SdfMacro m = SdfMacro.decodeInstruction(this);
            ops.add(m);
        }
    }
    
    // List of contained instructions
    ArrayList ops;

    // byte[] representation
    byte[] buffer;
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SdfBuffer.class.getName());

}

/* @(#)SdfBuffer.java */
