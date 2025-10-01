package jmri.jmrix.can.cbus.swing.bootloader;

import java.io.*;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to encapsulate an intel format hex file for a CBUS PIC.
 *
 * Assumes hex record addresses are 8-byte aligned and that addresses increase
 * monotonically.
 * 
 * @author Andrew Crosland Copyright (C) 2020, 2022
 */
public class HexFile {

    protected String name;
    protected File file;
    protected FileInputStream in;
    protected BufferedInputStream buffIn;
    // Number of hex records
    protected static final int MAX_HEX_SIZE = 10000;

    protected int address = 0;
    protected boolean read;
    protected int lineNo = 0;
    private int progStart = 99999999;
    private int progEnd = 0;

    // Storage for raw program data
    protected HexRecord [] hexRecords;
    protected int readIndex = 0;
    protected int endRecord = 0;


    /**
     * Create a new HexFile object and initialize data to unprogrammed state.
     *
     * @param fileName file name to use for the hex file
     */
    public HexFile(String fileName) {
        name = fileName;
        file = new File(fileName);

        hexRecords = new HexRecord[MAX_HEX_SIZE];
    }


    /**
     * @return name of the open file
     */
    public String getName() {
        return name;
    }


    /**
     * Open hex file for reading.
     *
     * @throws FileNotFoundException if pre-defined file can't be opened
     */
    public void openRd() throws FileNotFoundException {
        read = true;
        // Create an input reader based on the file, so we can read its data.
        in = new FileInputStream(file);
        buffIn = new BufferedInputStream(in);
        address = 0;
    }


    /**
     * Close the currently open file.
     */
    public void close() {
        try {
            if (read) {
                buffIn.close();
                in.close();
            }
            name = null;
        } catch (IOException e) {
            log.warn("Exception closing hex file", e);
            name = null;
        }
    }


    /**
     * DProcess record if required
     * @param r hex record
     */
    protected void checkRecord(HexRecord r) {
        
    }
    
    /**
     * Read one hex record from the file
     * 
     * @return the hex record
     * @throws java.io.IOException on read error.
     */
    protected HexRecord readOneRecord() throws IOException {
        HexRecord r;
        try {
            r = new HexRecord(this);
        } catch (IOException e) {
            log.error("Exception reading hex record", e);
            throw new IOException(e);
        }
        
        checkRecord(r);
        
        return r;
    }
    
    /**
     * Read a hex file.
     * <p>
     * Read hex records and store TYPE_DATA records in the array.
     * 
     * @throws IOException on read error
     */
    public void read() throws IOException {
        HexRecord r;
        
        lineNo = 0;
        endRecord = 0;
        
        do {
            r = readOneRecord();
            r.setLineNo(lineNo);
            hexRecords[lineNo] = r;
            if (r.type == HexRecord.EXT_ADDR) {
                // Extended address record so update the record address
                address = (r.data[0]& 0xff) * 256 * 65536 + (r.data[1] & 0xff) * 65536;
//                hexRecords[lineNo].address = address;
                log.debug("Found extended adress record for address {}", Integer.toHexString(address));
                continue;
            }
            if (r.type == HexRecord.TYPE_DATA) {
                address = (address & 0xffff0000) + r.getAddress();
                hexRecords[lineNo].address = address;
                log.debug("Hex record for address {}", Integer.toHexString(hexRecords[lineNo].address));
                // Keep track of start and end addresses that have been read
                if (address < progStart) {
                    progStart = address;
                }
                if ((address + r.len) > progEnd) {
                    progEnd = address + r.len;
                }
            }
            if (r.type == HexRecord.END) {
                endRecord = lineNo;
            }
            lineNo++;
        } while (r.type != HexRecord.END);
        log.debug("Done reading hex file");
    }


    /**
     * Read a character from the hex file
     * @return the character
     * @throws IOException from the underlying read operation
     */
    public int readChar() throws IOException {
            return buffIn.read();
    }


    /**
     * Read a hex byte.
     *
     * @return the byte
     * @throws IOException from the underlying read operation
     */
    public int rdHexByte() throws IOException {
        int hi = rdHexDigit();
        int lo = rdHexDigit();
        return hi * 16 + lo;
    }


    /**
     * Read a single hex digit.
     *
     * @return int representing a single hex digit 0 - f.
     * @throws IOException  from the underlying read operation or if there's an invalid hex digit
     */
    private int rdHexDigit() throws IOException {
        int b = buffIn.read();
        if ((b >= '0') && (b <= '9')) {
            b = b - '0';
        } else if ((b >= 'A') && (b <= 'F')) {
            b = b - 'A' + 10;
        } else if ((b >= 'a') && (b <= 'f')) {
            b = b - 'a' + 10;
        } else {
            log.error("Invalid hex digit {}", b);
            throw new IOException(Bundle.getMessage("HexInvalidDigit"));
        }
        return (byte) b;
    }


    /**
     * Get current address.
     *
     * @return int the current address
     */
    public int getAddress() {
        return address;
    }


    /**
     * Get current file line number
     *
     * @return the file number
     */
    public int getLineNo() {
        return lineNo;
    }

    
    /**
     * Return the next TYPE_DATA record from the file
     * 
     * @return the next hex record
     * @throws ArrayIndexOutOfBoundsException when needed
     */
    public HexRecord getNextRecord() throws ArrayIndexOutOfBoundsException {
        return hexRecords[readIndex++];
    }

    
    /**
     * Get the hex record for a given address
     * 
     * Expected that the address will be the start address of a record but will
     * return the first record that encompasses the address and increment the
     * index to point at the next record.
     *
     * @param addr The address
     * @return the hex record
     * @throws ArrayIndexOutOfBoundsException when needed
     */
    public Optional<HexRecord> getRecordForAddress(int addr) throws ArrayIndexOutOfBoundsException {
        HexRecord r;
        readIndex = 0;
        
        while (true) { 
            try {
                r = hexRecords[readIndex++];
                if ((r.type == HexRecord.TYPE_DATA)
                        && (addr >= r.address) && (addr < (r.address + r.len))) {
                    return Optional.of(r);
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                return Optional.empty();
            }
        }
    }
    
    
    /**
     * Get the file parameters
     * 
     * Create an invalid parameter set of necessary. Override in hardware specific
     * implementations.
     * 
     * @return CBUS parameters from the file
     */
    public CbusParameters getParams() {
        return new CbusParameters();
    }
    

    /**
     * Return the lowest address read from the hex file
     *
     * @return the highest address
     */
    public int getProgStart() {
        return progStart;
    }


    /**
     * Return the highest address read from the hex file
     *
     * @return the highest address
     */
    public int getProgEnd() {
        return progEnd;
    }


    /**
     * close the open file
     */
    public void dispose() {
        close();
    }


    private final static Logger log = LoggerFactory.getLogger(HexFile.class);

}
