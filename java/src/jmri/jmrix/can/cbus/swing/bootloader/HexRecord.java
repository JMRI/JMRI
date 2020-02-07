package jmri.jmrix.can.cbus.swing.bootloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to encapsulate a hex record as used by Microchip tools
 * 
 * @author Andrew Crosland Copyright (C) 2020
 */
public class HexRecord {

    // Record types
    static final byte EXT_ADDR = 4;
    static final byte DATA = 0;
    static final byte END = 1;
    // Maximum data length
    private static final int MAX_LEN = 16;
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
     * Create an empty record with invalid status
     */
    public HexRecord() {
        len = 0;
        addrh = 0;
        addrl = 0;
        type = END;
        checksum = 0;
        valid = false;
        data = new byte[MAX_LEN];
        lineNo = -1;
    }
    
    
    /**
     * Read a new record from a file
     * 
     * @param f
     */
    public HexRecord(HexFile f) {
        this();
        readRecord(f);
    }
    
    
    /**
     * Set the line number where the record was found in the file
     * 
     * @param l 
     */
    protected void setLineNo(int l) {
        lineNo = l;
    }
    
    
    /**
     * Get the data array from a hex record
     * 
     * @return the data
     */
    protected byte [] getData() {
        return data;
    }
    
    
    /**
     * Get a data element from a hex record
     * 
     * @return the data
     */
    protected byte getData(int i) {
        return data[i];
    }
    
    
    /**
     * Get current address from a hex record
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
     * Read a record from a hex file and verify the checksum.
     *
     */
    private void readRecord(HexFile f) {
        int c;
        valid = true;
    
        // Make space for the maximum size record to be read
        checksum = 0;
        // Read ":"
        while (((c = f.readChar()) == 0xd)
                || (c == 0xa)) {
            // skip
        }
        if (c != ':') {
            valid = false;
            log.error("No colon at start of line {0}", f.getLineNo());
        }
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
        if (type != END) {
            for (int i = 0; i < len; i++) {
                data[i] = f.rdHexByte();
                checksum += data[i];
            }
        }
        int fileCheck = f.rdHexByte();
        if (((checksum + fileCheck) & 0xff) != 0) {
            log.error("Bad checksum at {0}", address);
            valid = false;
        }
    }

    
    private final static Logger log = LoggerFactory.getLogger(HexRecord.class);
    
}
