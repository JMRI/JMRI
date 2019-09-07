package jmri.jmrix.pricom.downloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for reading PRICOM ".pdi" files
 * <p>
 * The PRICOM format documentation is Copyright 2003, 2005, PRICOM Corp. They
 * have kindly given permission for this use.
 *
 * @author	Bob Jacobsen Copyright (C) 2005
 */
public class PdiFile {

    public PdiFile(File file) {
        this.file = file;
    }

    File file;
    private InputStream buffIn;

    String comment = "";
    int commentLength;

    int lastAddress;
    int address;

    int fileLength;

    public void open() throws IOException {
        InputStream stream = new BufferedInputStream(new FileInputStream(file));
        open(stream);
    }

    public void open(InputStream stream) throws IOException {
        buffIn = stream;

        // get comment length, comment
        int high = (buffIn.read() & 0xFF);
        int low = (buffIn.read() & 0xFF);
        commentLength = high * 256 + low;

        StringBuffer buffer = new StringBuffer();

        // Note the count is decremented by two in the following.
        // Apparently, the comment length field includes it's own
        // two bytes in the count
        for (int i = 0; i < (commentLength - 2); i++) {
            int next = buffIn.read();
            if (next == 0x0d) {
                buffer.append("\n");
            } else if (next != 0x0a) {
                buffer.append((char) next);
            }
        }

        comment = buffer.toString();

        // get data base address
        high = (buffIn.read() & 0xFF);
        low = (buffIn.read() & 0xFF);
        address = high * 256 + low;
        if (log.isDebugEnabled()) {
            log.debug("address " + high + " " + low);
        }

        // get last address to write
        high = (buffIn.read() & 0xFF);
        low = (buffIn.read() & 0xFF);
        lastAddress = high * 256 + low;
        if (log.isDebugEnabled()) {
            log.debug("length " + high + " " + low);
        }

        fileLength = (int) file.length() - 6 - commentLength;

        if (log.isDebugEnabled()) {
            log.debug("lengths: file " + (int) file.length()
                    + ", comment " + commentLength
                    + ", data " + lastAddress);
        }
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
     *
     * @param n number of data bytes to include
     * @return byte buffer, starting with address info and containing data, but
     *         not CRC
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
        justification = "API defined by Pricom docs")
    public byte[] getNext(int n) {
        byte[] buffer = new byte[n + 3 + 2]; // 3 at front, 2 at back for CRC
        int rd;

        // load header
        if (n == 128) {
            buffer[0] = 60;
        } else {
            buffer[0] = 59;
        }

        buffer[1] = (byte) ((address >> 8) & 0xFF);
        buffer[2] = (byte) (address & 0xFF);
        address = address + n;

        for (int i = 0; i < n + 2; i++) {
            buffer[3 + i] = 0;  // clear data section
        }
        try {
            // fill data
            for (int i = 0; i < n; i++) {
                rd = buffIn.read();                         // read from file, -1=EOF
                if (rd == -1) {
                    return null;                  // return NULL pointer
                }
                buffer[3 + i] = (byte) (rd & 0xFF);             // tuck the byte
            }
        } catch (IOException e) {
            log.error("IO exception reading file: " + e);
        }
        return buffer;
    }

    private final static Logger log = LoggerFactory.getLogger(PdiFile.class);
}
