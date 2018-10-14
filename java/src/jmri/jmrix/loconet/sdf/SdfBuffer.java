package jmri.jmrix.loconet.sdf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide tools for reading, writing and accessing Digitrax SPJ files.
 * <p>
 * Maintains several representations:
 * <ul>
 *   <li>A byte array of the SDF contents after assembly. This is not complete, as
 *   it can't contain information like contents, labels, etc. Nor can it
 *   distinguish certain options with identical values (e.g. several constants
 *   that boil down to a zero value)
 *   <li>An array of nested SdfMacro objects. These contain more detailed
 *   representations of the macro source, in the limit containing the entire
 *   thing.
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2008, 2010
 */
public class SdfBuffer {

    public SdfBuffer(byte[] buffer) {
        this.buffer = Arrays.copyOf(buffer, buffer.length);
        loadMacroList();
    }

    public SdfBuffer(String name) throws IOException {
        File file = new File(name);
        int length = (int) file.length();

        InputStream s = new java.io.BufferedInputStream(new java.io.FileInputStream(file));

        try {
            // Assume we can get all this in memory
            buffer = new byte[length];

            for (int i = 0; i < length; i++) {
                buffer[i] = (byte) (s.read() & 0xFF);
            }
            loadMacroList();
        } catch (java.io.IOException e1) {
            log.error("error reading file", e1);
            throw e1;
        } finally {
            try {
                s.close();
            } catch (java.io.IOException e2) {
                log.error("Exception closing file", e2);
            }
        }
    }

    protected int index;

    public void resetIndex() {
        index = 0;
    }

    public int getAtIndex() {
        return buffer[index] & 0xFF;
    }

    public int getAtIndexAndInc() {
        return buffer[index++] & 0xFF;
    }

    public boolean moreData() {
        return index < buffer.length;
    }

    public void setAtIndex(int data) {
        buffer[index] = (byte) (data & 0xFF);
    }

    public void setAtIndexAndInc(int data) {
        buffer[index++] = (byte) (data & 0xFF);
    }

    /**
     * Reload the byte buffer from the contained instruction objects
     */
    public void loadByteArray() {
        // first get length of new array
        int length = 0;
        for (int i = 0; i < ops.size(); i++) {
            length += ops.get(i).totalLength();
        }
        buffer = new byte[length];
        log.debug("create buffer of length {}", length);
        resetIndex();
        // recurse to store bytes
        for (int i = 0; i < ops.size(); i++) {
            ops.get(i).loadByteArray(this);
        }
        if (index != length) {
            log.error("Lengths did not match: {} {}", index, length);
        }
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("");
        for (int i = 0; i < ops.size(); i++) {
            SdfMacro m = ops.get(i);

            out.append(m.allInstructionString("    "));
        }
        return out.toString();
    }

    public byte[] getByteArray() {
        return Arrays.copyOf(buffer, buffer.length);
    }

    public List<SdfMacro> getMacroList() {
        return ops;
    }

    void loadMacroList() {
        resetIndex();
        ops = new ArrayList<>();
        while (moreData()) {
            SdfMacro m = SdfMacro.decodeInstruction(this);
            ops.add(m);
        }
    }

    // List of contained instructions
    ArrayList<SdfMacro> ops;

    // byte[] representation
    byte[] buffer;

    private final static Logger log = LoggerFactory.getLogger(SdfBuffer.class);

}
