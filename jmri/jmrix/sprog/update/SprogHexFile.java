package jmri.jmrix.sprog.update;

import java.io.*;
import javax.swing.*;

public class SprogHexFile extends JFrame {
  private File file;
  private FileInputStream in;
  private BufferedInputStream buffIn;
  private FileOutputStream out;
  private BufferedOutputStream buffOut;
  private int address = 0;
  private int type;
  private int len;
  private StringBuffer line;
  private int data[];
  private boolean read;
  private int lineNo = 0;
  private int charIn;
  private String name;

  static final byte EXT_ADDR = 4;
  static final byte DATA = 0;
  static final byte END = 1;
  private static final int MAX_LEN = (255 + 1 + 2 + 1 + 1)*2;
  private static final int LEN = 0;
  private static final int ADDRH = 1;
  private static final int ADDRL = 2;
  private static final int TYPE = 3;

  SprogAlertDialog ad;

  public SprogHexFile(String fileName) {
    name = fileName;
    file = new File(fileName);
  }

  public String getName() {
    return name;
  }

  /**
   * Open hex file for reading (writing to device)
   *
   * @return boolean
   */
  public boolean openRd() {
    read = true;
    try {
      // Create an input reader based on the file, so we can read its data.
      in = new FileInputStream(file);
      buffIn = new BufferedInputStream(in);
      address = 999999;
      line = new StringBuffer("");
      return true;
    }
    catch (IOException e) {
      return false;
    }
  }

  /**
   * Open hex file for writing (reading from device)
   *
   * @return boolean
   */
  public boolean openWr() {
    read = false;
    try {
      // Create an output writer based on the file, so we can write.
      out = new FileOutputStream(file, false);
      buffOut = new BufferedOutputStream(out);
      return true;
    }
    catch (IOException e) {
      return false;
    }
  }

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
    }
    catch (IOException e) {

    }
  }

  /**
   * Read a record (line) from the hex file. If it's an extended address record
   * then update the address and read the next line. Returns the data length
   *
   * @return int
   */
  public int read() {
    // Make space for the the maximum size record to be read
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
    for (int i=0; i < len; i++) {
      data[i] = record[TYPE + 1 + i];
    }
    return len;
  }

  /**
   * Read a line from the hex file and verify the checksum.
   *
   * @return int[]
   */
  public int[] readLine() {
    // Make space for the the maximum size record to be read
    int record[] = new int[MAX_LEN];
    int checksum = 0;
    // Read ":"
    try {
      while (((charIn = buffIn.read()) == 0xd)
             || (charIn == 0xa)) ;
      if (charIn != ':') {
//        ad = new SprogAlertDialog(this, "Hex File Format Error!", "No leading : at line " + lineNo);
        if (log.isDebugEnabled()) log.debug("HexFile.readLine no colon at start of line " + lineNo);
        return new int[] {-1};
      }
    }
    catch (IOException e) {
      ad = new SprogAlertDialog(this, "I/O Error reading hex file!", e.toString());
      if (log.isDebugEnabled()) log.debug("I/O Error reading hex file!" + e.toString());
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
//      ad = new SprogAlertDialog(this, "Checksum Error!", "Checksum Error reading hex file at line " + lineNo);
      log.error("HexFile.readLine bad checksum at line " + lineNo);
    }
    lineNo++;
    return record;
  }

  /**
   * Read a hex byte
   * @return byte
   */
  private int rdHexByte() {
    int hi = rdHexDigit();
    int lo = rdHexDigit();
    if ((hi < 16) && (lo < 16)) {
      return (hi*16 + lo);
    } else {
      return 0;
    }
  }

  /**
   * Read a single hex digit. returns 16 if digit is invalid
   *
   * @return byte
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
        ad = new SprogAlertDialog(this, "Format Error!", "Invalid hex digit at line " + lineNo);
        log.error("Format Error! Invalid hex digit at line " + lineNo);
        b = 16;
      }
    }
    catch (IOException e) {
      ad = new SprogAlertDialog(this, "I/O Error reading hex file!", e.toString());
      log.error("I/O Error reading hex file!" + e.toString());
    }
    return (byte)b;
  }

  /**
   * Write a line to the hex file
   *
   * @param address int
   * @param type byte
   * @param data byte[]
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

  public void wrExtAddr(int addr) {
    write(0, EXT_ADDR, new byte[] {(byte)(addr/256), (byte)(addr & 0xff)});
  }

  public void wrEof() {
    writeLine(new byte[] {0, 0, 0, END});
  }

  /**
   * Get the type of the last record read from the hex file
   *
   * @return byte
   */
  public int getType() {
    return type;
  }

  /**
   * Get the length of the last record read from the hex file
   *
   * @return byte
   */
  public int getLen() {
    return len;
  }

  /**
   * Get current address
   *
   * @return int
   */
  public int getAddress() {
    return address;
  }

  /**
   * Get bytes of current address
   *
   * @return byte
   */
  public byte getAddressL() {
    return (byte) (address & 0xff);
  }

  public byte getAddressH() {
    return (byte) ( (address / 0x100) & 0xff);
  }

  public byte getAddressU() {
    return (byte) (address / 0x10000);
  }

  /**
   * Get data from last record read
   *
   * @return byte[]
   */
  public int[] getData() {
    return data;
  }

  /**
   * Write a byte array to the hex file, prepending ":" and appending checksum
   * and carriage return
   *
   * @param data byte[]
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
      writeHexByte((byte)checksum);
      buffOut.write('\n');
    }
    catch (IOException e) {

    }
  }

  /**
   * Write a byte as two hex characters
   *
   * @param b byte
   */
  private void writeHexByte(byte b) {
    int i = b;
    // correct for byte being -128 to +127
    if (b < 0) {
      i = 256 + b;
    }
    writeHexDigit((byte)(i / 16));
    writeHexDigit((byte)(i & 0xf));
  }

  /**
   * Write a single hex digit
   *
   * @param b byte
   */
  private void writeHexDigit(byte b) {
    try {
      if (b > 9) {
        buffOut.write(b - 9 + 0x40);
      }
      else {
        buffOut.write(b + 0x30);
      }
    }
    catch (IOException e) {
    }
  }

  static org.apache.log4j.Category log = org.apache.log4j.Category.
      getInstance(SprogHexFile.class.getName());

}
