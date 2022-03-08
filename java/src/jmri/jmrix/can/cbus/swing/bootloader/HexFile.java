package jmri.jmrix.can.cbus.swing.bootloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to encapsulate an intel format hex file for a CBUS PIC.
 *
 * @author Andrew Crosland Copyright (C) 2020
 */
public class HexFile {

    private String name;
    private final File file;
    private FileInputStream in;
    private BufferedInputStream buffIn;
    // 4MB should be enough for most CBUS modules 
    protected static final int MAX_PROG_SIZE = 4*1024*1024;

    private int address = 0;
    private boolean read;
    private int lineNo = 0;
    private int progStart = MAX_PROG_SIZE;
    private int progEnd = 0;

    // Storage for raw program data
    private byte [] hexData;


    /**
     * Create a new HexFile object and initialize data to unprogrammed state.
     *
     * @param fileName file name to use for the hex file
     * @param eepromStart start address of EEPROM for the device
     */
    public HexFile(String fileName) {
        name = fileName;
        file = new File(fileName);

        hexData = new byte [MAX_PROG_SIZE];
        setDataToErasedState();
    }


    /**
     * Set data arrays to erased state, usually all FFs
     */
    private void setDataToErasedState() {
        Arrays.fill(hexData, (byte)-1);
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
     * Read a hex file.
     * <p>
     * Read hex records into the array of programming data. A second array is
     * used as a map to flag which bytes have actually been loaded from the hex
     * file.
     * 
     * @throws java.io.IOException on read error.
     */
    public void read() throws IOException {
        HexRecord r;
        try {
            do {
                r = new HexRecord(this);
                if (r.type == HexRecord.EXT_ADDR) {
                    // Extended address record so update the address and read
                    // next record. Cast data from byte to int
                    address = (r.data[0]& 0xff) * 256 * 65536 + (r.data[1] & 0xff) * 65536;
                    log.debug("Found extended adress record. Address is now {}", Integer.toHexString(address));
                    lineNo++;
                    r = new HexRecord(this);
                }
                if (r.type == HexRecord.DATA) {
                    lineNo++;
                    r.setLineNo(lineNo);
                    address = (address & 0xffff0000) + r.getAddress();
                    log.debug("Hex record for address {}", Integer.toHexString(address));

                    for (int i = 0; i < r.len; i++) {
                        hexData[address + i] = r.getData(i);
                    }
                    
                    // Keep track of start and end addresses that have been read
                    if (address < progStart) {
                        progStart = address;
                    }
                    if ((address + r.len) > progEnd) {
                        progEnd = address + r.len;
                    }
                }
            } while (r.type != HexRecord.END);
        } catch (IOException e) {
            log.error("Exception reading hex file", e);
            setDataToErasedState();
            throw new IOException(e);
        }
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
        int b = 0;
        b = buffIn.read();
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
     * Get program data bytes from a data array
     * 
     * @param offset    address of data to retrieve
     * @param len       number of bytes to retrieve
     * @param hexData   the array to get the data from
     * @return          array of bytes
     */
    private byte [] getBytes(int offset, int len) {
        byte [] d = new byte[len];
        Arrays.fill(d, (byte)-1);

        int end = offset + len;
        if ((offset + len) > MAX_PROG_SIZE) {
            end = MAX_PROG_SIZE;
        }

        try {
            System.arraycopy(hexData, offset, d, 0, end - offset);
        } catch (ArrayIndexOutOfBoundsException e) {
            log.error("Index out of bounds", e);
            d = new byte[len];
            Arrays.fill(d, (byte)-1);
        }
        return d;
    }


    /**
     * Get program data bytes from the data array
     *
     * @param offset address of data to retrieve
     * @param len   number of bytes to retrieve
     * @return      array of bytes
     */
    public byte [] getData(int offset, int len) {
        byte [] d;
        d = getBytes(offset, len);
        return d;
    }


    /**
     * Return the lowest address read from the hex file
     *
     * @return the highest addresss
     */
    public int getProgStart() {
        return progStart;
    }


    /**
     * Return the highest address read from the hex file
     *
     * @return the highest addresss
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
