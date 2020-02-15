package jmri.jmrix.can.cbus.swing.bootloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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
    protected static final int MAX_PROG_SIZE = 256*1024;
    protected static final int MAX_CONFIG_SIZE = 1024;
    protected static final int MAX_EEPROM_SIZE = 1024;
    protected static final int ID_START = 0x200000;
    protected static final int CONFIG_START = 0x300000;
    protected static final int EE_START = 0xf00000;

    private int address = 0;
    private boolean read;
    private int lineNo = 0;
    private int progEnd = 0;
    private int configEnd = 0;
    private int eeEnd = 0;

    // Storage for raw program data
    private byte [] hexDataProg;
    private byte [] hexDataConfig;
    private byte [] hexDataEeprom;

    
    /**
     * Create a new HexFile object and initialize data to unprogrammed state.
     * 
     * @param fileName file name to use for the hex file
     */
    public HexFile(String fileName) {
        name = fileName;
        file = new File(fileName);
        
        hexDataProg = new byte [MAX_PROG_SIZE];
        hexDataConfig = new byte [MAX_CONFIG_SIZE];
        hexDataEeprom = new byte [MAX_EEPROM_SIZE];
        Arrays.fill(hexDataProg, (byte)-1);
        Arrays.fill(hexDataConfig, (byte)-1);
        Arrays.fill(hexDataEeprom, (byte)-1);
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
     * @return boolean true if successful
     */
    public boolean openRd() {
        read = true;
        try {
            // Create an input reader based on the file, so we can read its data.
            in = new FileInputStream(file);
            buffIn = new BufferedInputStream(in);
            address = 0;
            //line = new StringBuffer("");
            return true;
        } catch (IOException e) {
            log.error("I/O exception opening hex file {}", e.toString());
            return false;
        }
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

        }
    }

    
    /**
     * Read a hex file
     * 
     * Read into the array of individual records.
     * Add the actual programming data to the data arrays.
     * Track the highest used addresses
     */
    public void read() {
        HexRecord r;
        
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

                if ((address >= EE_START) && (address < (EE_START  + MAX_EEPROM_SIZE))) {
                    for (int i = 0; i < r.len; i++) {
                        hexDataEeprom[address - EE_START + i] = r.getData(i);
                    }
                    if ((address + r.len) > eeEnd) {
                        eeEnd = address + r.len;
                    }
                } else if ((address >= CONFIG_START) && (address < (CONFIG_START  + MAX_CONFIG_SIZE))) {
                    for (int i = 0; i < r.len; i++) {
                        hexDataConfig[address - CONFIG_START + i] = r.getData(i);
                    } 
                    if ((address + r.len) > configEnd) {
                        configEnd = address + r.len;
                    }
                } else if (address < ID_START) {
                    for (int i = 0; i < r.len; i++) {
                        hexDataProg[address + i] = r.getData(i);
                    } 
                    if ((address + r.len) > progEnd) {
                        progEnd = address + r.len;
                    }
                }
            } 
        } while (r.type != HexRecord.END);
        log.debug("End addresses prog: {} config: {} EEPROM: {}", Integer.toHexString(progEnd), Integer.toHexString(configEnd), Integer.toHexString(eeEnd));
    }


    public int readChar() {
        try {
            return buffIn.read();
        } catch (IOException e) {
            log.error("I/O Error reading hex file!"+ e.toString());
            return 0;
        }
    }
    
    
    /**
     * Read a hex byte.
     *
     * @return int the byte that was read
     */
    public int rdHexByte() {
        int hi = rdHexDigit();
        int lo = rdHexDigit();
        if ((hi < 16) && (lo < 16)) {
            return hi * 16 + lo;
        } else {
            return 0;
        }
    }

    
    /**
     * Read a single hex digit.
     *
     * @return 16 if digit is invalid. byte low nibble contains the hex digit read.
     * high nibble set if error.
     */
    private int rdHexDigit() {
        int b = 0;
        try {
            b = buffIn.read();
            if ((b >= '0') && (b <= '9')) {
                b = b - '0';
            } else if ((b >= 'A') && (b <= 'F')) {
                b = b - 'A' + 10;
            } else if ((b >= 'a') && (b <= 'f')) {
                b = b - 'a' + 10;
            } else {
                throw new IOException(Bundle.getMessage("HexInvalidDigit" + lineNo));
            }
        } catch (IOException e) {
            log.error("I/O Error reading hex file!"+ e.toString());
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
     * <p>
     * Pad returned data at end with unprogrammed data (-1 or 0xFF)
     * 
     * 
     * @param offset    address of data to retrieve
     * @param len       number of bytes to retrieve
     * @param hexData   the array to get the data from
     * @return          array of bytes 
     */
    private byte [] getBytes(int offset, int len, byte [] hexData) {
        byte [] d = new byte[len];
        Arrays.fill(d, (byte)-1);
        
        int end = offset + len;
        if ((offset + len) > MAX_PROG_SIZE) {
            end = MAX_PROG_SIZE;
        }
        
        try {
            System.arraycopy(hexData, offset, d, 0, end - offset);
        } catch (ArrayIndexOutOfBoundsException e) {
            log.warn("Index out of bounds {}", e);
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
        d = getBytes(offset, len, hexDataProg);
        return d;
    }
    
    
    /**
     * Get CONFIG bytes from the data array
     * 
     * @param offset address of data to retrieve
     * @param len   number of bytes to retrieve
     * @return      array of bytes 
     */
    public byte [] getConfig(int offset, int len) {
        byte [] d;
        d = getBytes(offset, len, hexDataConfig);
        return d;
    }
    
    
    /**
     * Get EEPROM data bytes from the data array
     * 
     * @param offset offset of data to retrieve
     * @param len   number of bytes to retrieve
     * @return      array of bytes 
     */
    public byte [] getEeprom(int offset, int len) {
        byte [] d;
        d = getBytes(offset, len, hexDataEeprom);
        return d;
    }
    
    
    /**
     * Return the highest program memory address
     * 
     * @return the highest adress
     */
    public int getProgEnd() {
        return progEnd;
    }
    
    
    /**
     * Return the highest config memory address
     * 
     * @return the highest address
     */
    public int getConfigEnd() {
        return configEnd;
    }
    
    
    /**
     * Return the highest eeprom memory address
     * 
     * @return the highest address
     */
    public int getEeEnd() {
        return eeEnd;
    }
    
    
    /**
     * close the open file
     */
    public void dispose() {
        close();
    }

    
    private final static Logger log = LoggerFactory.getLogger(HexFile.class);

}
