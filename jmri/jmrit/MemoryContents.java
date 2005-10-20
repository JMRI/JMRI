// MemoryContents.java

package jmri.jmrit;

import java.io.*;
import com.sun.java.util.collections.ArrayList;


/**
 * Model (and provide utility functions for) board memory
 * as expressed in .hex files.
 * <P>
 * This will eventually have to be extended to 24-bit addressing,
 * in which case a sparse implementation (e.g. 16 bit pages) will be needed.
 *
 * @author	    Bob Jacobsen    Copyright (C) 2005
 * @version         $Revision: 1.1 $
 */
public class MemoryContents {

    static final int PAGES = 256;
    static final int PAGESIZE = 0x10000;
    static final int DEFAULT = -1;
    
    int[][] pageArray;
    
    public MemoryContents() {
        pageArray = new int[PAGES][];
        
        initPage(0);    
    }
    
    void initPage(int page) {
        if (pageArray[page]!=null) {
            log.warn("initPage too late: "+page);
            return;
        }
        
        int[] largeArray = new int[PAGESIZE];
        for (int i = 0; i<PAGESIZE; i++) largeArray[i] = DEFAULT;  // default contents
        
        pageArray[page] = largeArray;
    }
    
    int currentPage = 0;
    
    public void readHex(File file) throws FileNotFoundException {
        DataInputStream fileStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        // start reading the file
        try {
            byte bval;
            int ival;
            while (fileStream.available() > 3) {
                // this loop reads one line per turn
                String s = fileStream.readLine();
                
                // decode line type
                int len = s.length();
                if (len<1) return;
                if (s.charAt(0)=='#') {
                    // human comment
                } else if (s.charAt(0)=='!') {
                    // machine comment
                } else if (s.charAt(0)==':') {
                    // hex file
                    int type = Integer.valueOf(s.substring(7,9),16).intValue();
                    if (type == 0) {
                        // record type 0 is data
                        int count = Integer.valueOf(s.substring(1,3),16).intValue();
                        int address = Integer.valueOf(s.substring(3,7),16).intValue();

                        for (int i=9; i<9+count*2; i+=2) {
                            // parse as hex into integer, then convert to byte
                            ival = Integer.valueOf(s.substring(i,i+2),16).intValue();
                            pageArray[currentPage][address++] = ival;
                        } 
                    } else if (type == 4) {
                        // set segment
                        currentPage = Integer.valueOf(s.substring(9,13),16).intValue();
                        log.debug("New page "+currentPage+" "+s);
                        initPage(currentPage);
                    } else if (type == 1) {
                        continue; // not record we need to handle
                    } else {
                        log.warn("Unknown hex record type "+type);
                        continue;
                    }
                    // end parsing hex file record
                } else {
                    log.error("Unknown line type: "+s);
                }
            }
        } catch (Exception e) { log.error("Exception reading file: "+e);}

    }
    
    ArrayList lines = new ArrayList(10);
        
    public void writeHex(Writer w) throws IOException {
        int blocksize = 16;
        
        for (int i = 0; i<pageArray[currentPage].length-blocksize+1; i+=blocksize) {
            // see if need to write this block
            boolean write = false;
            for (int j = i; j<i+blocksize; j++) {
                if (pageArray[currentPage][j] != 0) {
                    write = true;
                    break;
                }
            }
            if (!write) continue; // no, we don't
            // here we do need to write it
            String output = ":10";  // always write 16 bytes
            String address = jmri.util.StringUtil.twoHexFromInt(i/256)
                            +jmri.util.StringUtil.twoHexFromInt(i&0xFF);
            output += (address + "00");
             
            // loop to append the data
            int checksum = 16+(i/256)+(i&0xFF)+0;
            for (int j = i; j<i+blocksize; j++) {
                int val = pageArray[currentPage][j];
                checksum += val;
                output = jmri.util.StringUtil.appendTwoHexFromInt(val, output);
            }
            output = jmri.util.StringUtil.appendTwoHexFromInt(((~checksum)+1)&0xFF, output)+"\n";
            w.write(output);
        }
        // write last line & close
        w.write(":00000001FF\n"); 
        w.flush();

    }


    /**
     * Return the address of the next location containing data, including
     * the location in the argument
     */
    public int nextContent(int location) {
        currentPage = location/PAGESIZE;
        int offset = location % PAGESIZE;
        for (; currentPage < PAGES; currentPage++) {
            if (pageArray[currentPage] != null)
                for (; offset<pageArray[currentPage].length; offset++)
                    if (pageArray[currentPage][offset] != DEFAULT ) return offset+currentPage*PAGESIZE;
            offset = 0;
        }
        return -1;
    }
    
    public void setLocation(int location, int value) { 
        currentPage = location / PAGESIZE;
        
        pageArray[currentPage][location % PAGESIZE] = value;
    }
    
    public boolean locationInUse(int location) {
        currentPage = location/PAGESIZE;
        if (pageArray[currentPage]==null) {
            return false;
        }
        try {
        return pageArray[currentPage][location % PAGESIZE] != DEFAULT; 
        } catch (Exception e) { log.error("error in locationInUse "+currentPage+" "+location+" "+e);return false;}
        
    }
    
    public int getLocation(int location) { 
        currentPage = location/PAGESIZE;
        if (pageArray[currentPage]==null) {
            log.error("getLocation("+location+") when no data at that location");
            return DEFAULT;
        }
        try {
        return pageArray[currentPage][location % PAGESIZE]; 
        } catch (Exception e) { log.error("error in getLocation "+currentPage+" "+location+" "+e);return 0;}
    }
        
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MemoryContents.class.getName());
}

/* @(#)MemoryContents.java */
