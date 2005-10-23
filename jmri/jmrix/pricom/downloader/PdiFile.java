// PdiFile.java

package jmri.jmrix.pricom.downloader;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

/**
 * Support for reading PRICOM ".pdi" files
 * <P>
 * The PRICOM format documentation is Copyright 2003, 2005, PRICOM Corp. 
 * They have kindly given permission for this use.
 * @author		Bob Jacobsen   Copyright (C) 2005
 * @version             $Revision: 1.1 $
 */
public class PdiFile {

    public PdiFile(File file) {
        this.file = file;       
    }

    File file;
    private FileInputStream in;
    private BufferedInputStream buffIn;
    boolean open = false;

    String comment = "";
    int commentLength;
    
    int remainingDataLength;
    int address;
    
    int fileLength;
        
    public void open() throws IOException {
        in = new FileInputStream(file);
        buffIn = new BufferedInputStream(in);
        open = true;
        
        // get comment length, comment
        byte high= (byte) (buffIn.read()&0xFF);
        byte low = (byte) (buffIn.read()&0xFF);
        commentLength = high*256+low;
        
        StringBuffer buffer = new StringBuffer();
        
        for (int i = 0; i< (commentLength); i++) {
            int next = buffIn.read();
            if (next == 0x0d) buffer.append("\n");
            else if (next != 0x0a) buffer.append((char)next);
        }
        
        comment = new String(buffer);
    
        // get data base address
        high= (byte) (buffIn.read()&0xFF);
        low = (byte) (buffIn.read()&0xFF);
        address = high*256+low;
        
        // get data length
        high= (byte) (buffIn.read()&0xFF);
        low = (byte) (buffIn.read()&0xFF);
        remainingDataLength = high*256+low;

        fileLength = (int)file.length()-6-commentLength;
    }
    
    /**
     * Return the comment embedded at the front of the file
     */
    public String getComment() {
        return comment;
    }
    
    int length() {
        return fileLength;
    }
    
    /**
     * Get the next n bytes for transmission to the device
     * @param n number of data bytes to include
     * @returns byte buffer, starting with address info and containing data, but not CRC
     */
    public byte[] getNext(int n) {
        byte[] buffer = new byte[n+3+2];
        
        // load header
        if (n == 128) buffer[0] = 60;
        else buffer[0] = 59;
        
        buffer[1] = (byte) ((address>>8)&0xFF);
        buffer[2] = (byte) (address & 0xFF);
        address = address+n;
        
        try {
            // fill data
            for (int i = 0; i<n; i++) {
                buffer[2+i] = (byte) (buffIn.read()&0xFF);
            }
        } catch (IOException e) {
            log.error("IO exception reading file: "+e);
        }
        return buffer;
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PdiFile.class.getName());
}
