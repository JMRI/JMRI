package jmri.jmrix.can.cbus.swing.bootloader;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to encapsulate a hex record as used by Microchip tools.
 * 
 * @author Andrew Crosland Copyright (C) 2020
 */
public class HexRecord {

    // Record types
    static final byte EXT_ADDR = 4;
    static final byte DATA = 0;
    static final byte END = 1;
    // Maximum data length
    static final int MAX_LEN = 256;
    // Offsets of fields within the record

    // Record property members
    protected int len;
    protected int addrh;
    protected int addrl;
    protected int address;
    protected int type;
    protected int checksum;
    protected boolean valid;
    protected byte [] data;
    protected int lineNo;
    
    
    /**
     * Create an empty record with unprogrammed data and invalid status.
     */
    public HexRecord() {
        len = 0;
        addrh = 0;
        addrl = 0;
        type = END;
        checksum = 0;
        valid = false;
        data = new byte[MAX_LEN];
        Arrays.fill(data, (byte)0xFF);
        lineNo = -1;
    }
    
    
    /**
     * Read a new record from a file.
     * 
     * @param f hex file to read from
     * @throws IOException from underlying read operations
     */
    public HexRecord(HexFile f) throws IOException {
        this();
        readRecord(f);
    }
    
    
    /**
     * Set the line number where the record was found in the file.
     * 
     * @param l the line number
     */
    protected void setLineNo(int l) {
        lineNo = l;
    }
    
    
    /**
     * Get the data array from a hex record.
     * 
     * @return the data
     */
    protected byte [] getData() {
        return data;
    }
    
    
    /**
     * Get a data element from a hex record.
     * 
     * @param i index of the element to get
     * @return the data
     */
    protected byte getData(int i) {
        return data[i];
    }
    
    
    /**
     * Get current address from a hex record.
     * <p>
     * Returns 16 bit address from a normal hex record. Extended address records
     * are handled elsewhere.
     * 
     * @return the address
     */
    protected int getAddress() {
        return address;
    }
    
    
    /**
     * Look for the start of a new record.
     * 
     * @param f Input hex file
     */
    private void startRecord(HexFile f) throws IOException {
        int c;
        
        checksum = 0;
        // Skip newline and look for ':'
        while (((c = f.readChar()) == 0xd) || (c == 0xa)) {  }
        if (c != ':') {
            log.error("No colon at start of line {}", f.getLineNo());
            throw new IOException("No colon at start of line "+f.getLineNo());
        }
    }
    
    
    /**
     * Read hex record header.
     * 
     * @param f Input hex file
     * @throws IOException 
     */
    private void readHeader(HexFile f) throws IOException {
        // length of data
        len = f.rdHexByte();
        checksum += len;
        // High address
        addrh = f.rdHexByte();
        checksum += addrh;
        // Low address
        addrl = f.rdHexByte();
        checksum += addrl;
        // record type
        type = f.rdHexByte();
        checksum += type;
        if (type != EXT_ADDR) {
            // update address, extended address should be handled by caller
            address = addrh * 256 + addrl;
        }
    }
    
    
    /**
     * Read the data bytes.
     * 
     * @param f Input hex file
     * @throws IOException from underlying read operations
     */
    void readData(HexFile f) throws IOException {
        if (type != END) {
            for (int i = 0; i < len; i++) {
                data[i] = (byte)(f.rdHexByte() & 0xFF);
                checksum += data[i];
            }
        }
    }
    
    
    /**
     * Verify the record checksum.
     * 
     * @param f Input hex file
     * @throws IOException 
     */
    private void checkRecord(HexFile f) throws IOException {
        int fileCheck = f.rdHexByte();
        if (((checksum + fileCheck) & 0xff) != 0) {
            log.error("Bad checksum at {}", Integer.toHexString(address));
            throw new IOException("Bad checksum");
        }
    }
    
    
    /**
     * Read a record from a hex file and verify the checksum.
     * 
     * @param f the hex file
     * @throws IOException 
     */
    private void readRecord(HexFile f) throws IOException {
        try {
            startRecord(f);
            readHeader(f);
            readData(f);
            checkRecord(f);
        } catch (IOException e) {
            throw new IOException(e);
        }
        valid = true;
    }

    
    private static final Logger log = LoggerFactory.getLogger(HexRecord.class);
    
}
