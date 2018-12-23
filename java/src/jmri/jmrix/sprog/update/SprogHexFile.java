package jmri.jmrix.sprog.update;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to encapsulate an intel format hex file and methods to manipulate it.
 * Intended use is as an input format for new program code to be sent to a
 * hardware device via some bootloading process.
 *
 * @author	Andrew Crosland Copyright (C) 2010
 */
public class SprogHexFile extends jmri.util.JmriJFrame {

    private File file;
    private FileInputStream in;
    private BufferedInputStream buffIn;
    private FileOutputStream out;
    private BufferedOutputStream buffOut;
    private int address = 0;
    private int type;
    private int len;
    private int data[];
    private boolean read;
    private int lineNo = 0;
    private int charIn;
    private String name;

    // Hex file record types
    static final byte EXT_ADDR = 4;
    static final byte DATA = 0;
    static final byte END = 1;
    // Maximum record length
    private static final int MAX_LEN = (255 + 1 + 2 + 1 + 1) * 2;
    // Offsets of fields within the record
    private static final int LEN = 0;
    private static final int ADDRH = 1;
    private static final int ADDRL = 2;
    private static final int TYPE = 3;

    public SprogHexFile(String fileName) {
        name = fileName;
        file = new File(fileName);
    }

    /**
     * @return name of the open file
     */
    @Override
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
            address = 999999;
            //line = new StringBuffer("");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Open file for writing.
     *
     * @return boolean true if successful
     */
    public boolean openWr() {
        read = false;
        try {
            // Create an output writer based on the file, so we can write.
            out = new FileOutputStream(file);
            buffOut = new BufferedOutputStream(out);
            return true;
        } catch (IOException e) {
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
            } else {
                buffOut.flush();
                buffOut.close();
                out.close();
            }
            name = null;
        } catch (IOException e) {

        }
    }

    /**
     * Read a record (line) from the hex file.
     * <p>
     * If it's an extended address record then update the address
     * and read the next line. Returns the data length.
     *
     * @return int the data length of the record, or 0 if no data
     */
    @SuppressFBWarnings(value = "DLS_DEAD_LOCAL_STORE")
    // False positive
    public int read() {
        // Make space for the maximum size record to be read
        int record[] = new int[MAX_LEN];
        do {
            record = readLine();
            if (type == EXT_ADDR) {
                // Get new extended address and read next line
                address = address & 0xffff
                        + record[4] * 256 * 65536 + record[5] * 65536;
                record = readLine();
            }
        } while ((type != DATA) && (type != END));
        if (type == END) {
            return 0;
        }
        data = new int[len];
        for (int i = 0; i < len; i++) {
            data[i] = record[TYPE + 1 + i];
        }
        return len;
    }

    /**
     * Read a line from the hex file and verify the checksum.
     *
     * @return int[] the array of bytes read from the file
     */
    public int[] readLine() {
        // Make space for the maximum size record to be read
        int record[] = new int[MAX_LEN];
        int checksum = 0;
        // Read ":"
        try {
            while (((charIn = buffIn.read()) == 0xd)
                    || (charIn == 0xa)) {
                // skip
            }
            if (charIn != ':') {
                if (log.isDebugEnabled()) {
                    log.debug("HexFile.readLine no colon at start of line " + lineNo);
                }
                return new int[]{-1};
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("IoErrorReadingHexFile"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            if (log.isDebugEnabled()) {
                log.debug("I/O Error reading hex file!" + e.toString());
            }
        }
        // length of data
        record[LEN] = rdHexByte();
        checksum += record[LEN];
        // High address
        record[ADDRH] = rdHexByte();
        checksum += record[ADDRH];
        // Low address
        record[ADDRL] = rdHexByte();
        checksum += record[ADDRL];
        // record type
        record[TYPE] = rdHexByte();
        checksum += record[TYPE];
        // update address
        address = (address & 0xffff0000) + record[ADDRH] * 256 + record[ADDRL];
        type = record[TYPE];
        if (type != END) {
            len = record[LEN];
            for (int i = 1; i <= len; i++) {
                record[TYPE + i] = rdHexByte();
                checksum += record[TYPE + i];
            }
        }
        int fileCheck = rdHexByte();
        if (((checksum + fileCheck) & 0xff) != 0) {
            log.error("HexFile.readLine bad checksum at line " + lineNo);
        }
        lineNo++;
        return record;
    }

    /**
     * Read a hex byte.
     *
     * @return byte the byte that was read
     */
    private int rdHexByte() {
        int hi = rdHexDigit();
        int lo = rdHexDigit();
        if ((hi < 16) && (lo < 16)) {
            return (hi * 16 + lo);
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
                JOptionPane.showMessageDialog(this, Bundle.getMessage("InvalidHexDigitAtLine", lineNo),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                log.error("Format Error! Invalid hex digit at line ()", lineNo);
                b = 16;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("IoErrorReadingHexFile"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            log.error("I/O Error reading hex file!"+ e.toString());
        }
        return (byte) b;
    }

    /**
     * Write a line to the hex file.
     *
     * @param addr int the starting address of the data
     * @param type byte the type of data record being written
     * @param data byte[] the array of bytes to be written
     */
    public void write(int addr, byte type, byte[] data) {
        // Make space for the record to be written
        byte record[] = new byte[data.length + 1 + 2 + 1];
        if (addr / 0x10000 != address / 0x10000) {
            // write an extended address record
            byte[] extAddr = {
                2, 0, 0, EXT_ADDR, 0, (byte) (addr / 0x10000)};
            writeLine(extAddr);
        }
        // update current address
        address = addr;
        // save length, address and record type
        record[LEN] = (byte) (data.length);
        record[ADDRH] = (byte) (address / 0x100);
        record[ADDRL] = (byte) (address & 0xff);
        record[TYPE] = type;
        // copy the data
        for (int i = 0; i < data.length; i++) {
            record[TYPE + 1 + i] = data[i];
        }
        // write the record
        writeLine(record);
    }

    /**
     * Write an extended address record.
     *
     * @param addr the extended address
     */
    public void wrExtAddr(int addr) {
        write(0, EXT_ADDR, new byte[]{(byte) (addr / 256), (byte) (addr & 0xff)});
    }

    /**
     * Write an end of file record.
     *
     */
    public void wrEof() {
        writeLine(new byte[]{0, 0, 0, END});
    }

    /**
     * Get the type of the last record read from the hex file.
     *
     * @return byte the record type
     */
    public int getRecordType() {
        return type;
    }

    /**
     * Get the length of the last record read from the hex file.
     *
     * @return byte the length
     */
    public int getLen() {
        return len;
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
     * Get lower byte of current address.
     *
     * @return byte the lower byte of the address
     */
    public byte getAddressL() {
        return (byte) (address & 0xff);
    }

    /**
     * Get high (middle) byte of current address.
     *
     * @return byte the high (middle) byte of the address
     */
    public byte getAddressH() {
        return (byte) ((address / 0x100) & 0xff);
    }

    /**
     * Get upper byte of current address.
     *
     * @return byte the upper byte of the address
     */
    public byte getAddressU() {
        return (byte) (address / 0x10000);
    }

    /**
     * Get data from last record read.
     *
     * @return byte[] array of data bytes
     */
    public int[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    /**
     * Write a byte array to the hex file, prepending ":" and appending checksum
     * and carriage return.
     *
     * @param data byte[] array of data bytes top be written
     */
    private void writeLine(byte[] data) {
        int checksum = 0;
        try {
            buffOut.write(':');
            for (int i = 0; i < data.length; i++) {
                writeHexByte(data[i]);
                checksum += data[i];
            }
            checksum = checksum & 0xff;
            if (checksum > 0) {
                checksum = 256 - checksum;
            }
            writeHexByte((byte) checksum);
            buffOut.write('\n');
        } catch (IOException e) {

        }
    }

    /**
     * Write a byte as two hex characters.
     *
     * @param b byte the byte to be written
     */
    private void writeHexByte(byte b) {
        int i = b;
        // correct for byte being -128 to +127
        if (b < 0) {
            i = 256 + b;
        }
        writeHexDigit((byte) (i / 16));
        writeHexDigit((byte) (i & 0xf));
    }

    /**
     * Write a single hex digit.
     *
     * @param b byte low nibble contains the hex digit to be written
     */
    private void writeHexDigit(byte b) {
        try {
            if (b > 9) {
                buffOut.write(b - 9 + 0x40);
            } else {
                buffOut.write(b + 0x30);
            }
        } catch (IOException e) {
        }
    }

    private final static Logger log = LoggerFactory
            .getLogger(SprogHexFile.class);

}
