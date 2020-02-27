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
    static final int MAX_LEN = 16;
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
     * @param f hex file to read from
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
     * @param i index of the element to get
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
     * Look for the start of a new record
     * 
     * @param f Input hex file
     */
    private void startRecord(HexFile f) {
        int c;
        
        checksum = 0;
        // Read ":"
        while (((c = f.readChar()) == 0xd)
                || (c == 0xa)) {
            // skip
        }
        if (c != ':') {
            valid = false;
            log.error("No colon at start of line {}", f.getLineNo());
        }
    }
    
    
    /**
     * Read hex record header
     * 
     * @param f Input hex file
     */
    private void readHeader(HexFile f) {
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
     * Read the data bytes
     * 
     * @param f Input hex file
     */
    void readData(HexFile f) {
        if (type != END) {
            for (int i = 0; i < len; i++) {
                data[i] = (byte)(f.rdHexByte() & 0xFF);
                checksum += data[i];
            }
        }
    }
    
    
    /**
     * Verify the record checksum
     * 
     * @param f Input hex file
     */
    private void checkRecord(HexFile f) {
        valid = true;
        
        int fileCheck = f.rdHexByte();
        if (((checksum + fileCheck) & 0xff) != 0) {
            log.error("Bad checksum at {}", Integer.toHexString(address));
            valid = false;
        }
    }
    
    
    /**
     * Read a record from a hex file and verify the checksum.
     *
     */
    private void readRecord(HexFile f) {
        startRecord(f);
        readHeader(f);
        readData(f);
        checkRecord(f);
    }

    
    private final static Logger log = LoggerFactory.getLogger(HexRecord.class);
    
}
