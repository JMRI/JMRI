// MemoryContents.java

package signalpro.mangler;

import java.io.*;

/**
 * Model (and provide utility functions for) PIC memory
 * as expressed in hex files
 *
 * @author	    Bob Jacobsen    Copyright (C) 2005
 * @version         $Revision: 1.1.1.1 $
 */
public class MemoryContents {

    int[] largeArray;

    public MemoryContents(int maxmem) {
        largeArray = new int[maxmem];
        for (int i = 0; i<maxmem; i++) largeArray[0] = 0;
    }
    
    void read(File file) throws FileNotFoundException {
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
                    if (type != 0) continue; // not record we need to handle
                    int count = Integer.valueOf(s.substring(1,3),16).intValue();
                    int address = Integer.valueOf(s.substring(3,7),16).intValue();

                    for (int i=9; i<9+count*2; i+=2) {
                        // parse as hex into integer, then convert to byte
                        ival = Integer.valueOf(s.substring(i,i+2),16).intValue();
                        largeArray[address++] = ival;
                    }
                } else {
                    log.error("Unknown line type: "+s);
                }
            }
        } catch (Exception e) { log.error("Exception reading file: "+e);}

    }
    
    void write(Writer w) throws IOException {
        int blocksize = 16;
        
        for (int i = 0; i<largeArray.length; i+=blocksize) {
            // see if need to write this block
            boolean write = false;
            for (int j = i; j<i+blocksize; j++) {
                if (largeArray[j] != 0) {
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
                int val = largeArray[j];
                checksum += val;
                output = jmri.util.StringUtil.appendTwoHexFromInt(val, output);
            }
            output = jmri.util.StringUtil.appendTwoHexFromInt(((~checksum)+1)&0xFF, output)+"\n";
            w.write(output);
        }
        w.flush();

    }

    void setLocation(int location, int value) { largeArray[location] = value; }
    int getLocation(int location) { return largeArray[location]; }
    
    // calculated via XOR on locations
    int checksum(int minmem, int maxmem) {
        int sum = 0;
        for (int i = 0; i<largeArray.length; i++) sum ^= largeArray[i];
        return sum;       
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MemoryContents.class.getName());
}

/* @(#)MemoryContents.java */
