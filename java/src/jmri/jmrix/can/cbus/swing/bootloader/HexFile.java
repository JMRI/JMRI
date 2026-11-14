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
     * <p>
     * Safe to call if the file was never opened, or has already been closed.
     * The records already read remain available in memory.
     */
    public void close() {
        try {
            if (buffIn != null) {
                buffIn.close();
            } else if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            log.warn("Exception closing hex file", e);
        }
        buffIn = null;
        in = null;
        read = false;
        name = null;
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

        return r;
    }

    /**
     * Read a hex file.
     * <p>
     * Read hex records and store TYPE_DATA records in the array.
     * <p>
     * Only data, end of file and extended linear address records are
     * understood. Any other record type causes the file to be rejected.
     *
     * @throws IOException on read error, or if the file contains an
     *                     unsupported record type, or too many records
     */
    public void read() throws IOException {
        HexRecord r;

        lineNo = 0;
        endRecord = 0;

        do {
            if (lineNo >= hexRecords.length) {
                log.error("Too many records in hex file, maximum is {}", hexRecords.length);
                throw new IOException("Too many records in hex file, maximum is " + hexRecords.length);
            }
            r = readOneRecord();
            if ((r.type != HexRecord.TYPE_DATA) && (r.type != HexRecord.END) && (r.type != HexRecord.EXT_ADDR)) {
                // Segment address, start address or unknown record types are not supported
                log.error("Unsupported hex record type {} at line {}", r.type, lineNo);
                throw new IOException("Unsupported hex record type " + r.type + " at line " + lineNo);
            }
            r.setLineNo(lineNo);
            hexRecords[lineNo] = r;
            if (r.type == HexRecord.EXT_ADDR) {
                // Extended address record so update the record address.
                // Not kept in the array (lineNo is not incremented) so the
                // next record overwrites it, only data and end records are stored.
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
            // Process the record now that its full address is known
            checkRecord(r);
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
     * <p>
     * Once the records read from the file are exhausted, an END record is
     * returned so that callers stop cleanly.
     *
     * @return the next hex record
     */
    public HexRecord getNextRecord() {
        if ((readIndex >= 0) && (readIndex <= endRecord) && (readIndex < hexRecords.length)
                && (hexRecords[readIndex] != null)) {
            return hexRecords[readIndex++];
        }
        log.warn("No more hex records to read at index {}", readIndex);
        readIndex = endRecord + 1;
        return new HexRecord();
    }


    /**
     * Get the hex record for a given address
     *
     * Expected that the address will be the start address of a record but will
     * return the first record that encompasses the address and increment the
     * index to point at the next record.
     * <p>
     * Only the records actually read from the file are searched.
     *
     * @param addr The address
     * @return the hex record, or empty if no data record contains the address
     */
    public Optional<HexRecord> getRecordForAddress(int addr) {
        readIndex = 0;

        for (int i = 0; (i <= endRecord) && (i < hexRecords.length); i++) {
            HexRecord r = hexRecords[i];
            if (r == null) {
                break;
            }
            if ((r.type == HexRecord.TYPE_DATA)
                    && (addr >= r.address) && (addr < (r.address + r.len))) {
                readIndex = i + 1;
                return Optional.of(r);
            }
        }
        // Not found, leave the index pointing at the END record
        readIndex = endRecord;
        return Optional.empty();
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


    private static final Logger log = LoggerFactory.getLogger(HexFile.class);

}
