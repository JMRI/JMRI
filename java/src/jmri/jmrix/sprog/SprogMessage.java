package jmri.jmrix.sprog;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.ProgrammingMode;
import jmri.jmrix.sprog.SprogConstants.SprogState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encode a message to an SPROG command station.
 * <p>
 * The {@link SprogReply} class handles the response from the command station.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class SprogMessage extends jmri.jmrix.AbstractMRMessage {

    // Special characters (NOTE: microchip bootloader does not use standard ASCII)
    public static final int STX = 15;
    public static final int DLE = 5;
    public static final int ETX = 4;
    public static final int CR = 0x0d;
    public static final int LF = 0x0a;

    // bootloader commands
    public static final int RD_VER = 0;
    public static final int WT_FLASH = 2;
    public static final int ER_FLASH = 3;
    public static final int WT_EEDATA = 5;

    // Longest boot message is 256bytes each preceded by DLE + 2xSTX + ETX
    public static final int MAXSIZE = 515;

    private static int msgId = 0;
    protected int _id = -1;
    
    /**
     * Get next message id
     * 
     * For modules that need to match their own message/reply pairs in strict sequence, e.g., 
     * SprogCommandStation, return a unique message id. The id wraps at a suitably large
     * value.
     * 
     * @return the message id
     */
    protected synchronized int newMsgId() {
        msgId = (msgId+1)%65536;
        return msgId;
    }
    
    public int getId() {
        return _id;
    }
    
    // create a new one
    public SprogMessage(int i) {
        if (i < 1) {
            log.error("invalid length in call to ctor");
        }
        _nDataChars = i;
        _dataChars = new int[i];
        _id = newMsgId();
    }

    /**
     * Create a new SprogMessage containing a byte array to represent a packet
     * to output.
     *
     * @param packet The contents of the packet
     */
    public SprogMessage(byte[] packet) {
        this(1 + (packet.length * 3));
        int i; // counter of byte in output message
        int j; // counter of byte in input packet

        i = 0;
        this.setElement(i++, 'O');  // "O " starts output packet

        // add each byte of the input message
        for (j = 0; j < packet.length; j++) {
            this.setElement(i++, ' ');
            String s = Integer.toHexString(packet[j] & 0xFF).toUpperCase();
            if (s.length() == 1) {
                this.setElement(i++, '0');
                this.setElement(i++, s.charAt(0));
            } else {
                this.setElement(i++, s.charAt(0));
                this.setElement(i++, s.charAt(1));
            }
        }
        _id = newMsgId();
    }

    // from String
    public SprogMessage(String s) {
        _nDataChars = s.length();
        _dataChars = new int[_nDataChars];
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = s.charAt(i);
        }
        _id = newMsgId();
    }

    // copy one
    public SprogMessage(SprogMessage m) {
        if (m == null) {
            log.error("copy ctor of null message");
            return;
        }
        _nDataChars = m._nDataChars;
        _dataChars = new int[_nDataChars];
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = m._dataChars[i];
        }
        // Copy has a unique id
        _id = newMsgId();
    }

    @Override
    public void setElement(int n, int v) {
        _dataChars[n] = v;
    }

    private void setLength(int i) {
        _dataChars[1] = i;
    }

    private void setAddress(int i) {
        _dataChars[2] = i & 0xff;
        _dataChars[3] = (i >> 8) & 0xff;
        _dataChars[4] = i >> 16;
    }

    private void setData(int[] d) {
        for (int i = 0; i < d.length; i++) {
            _dataChars[5 + i] = d[i];
        }
    }

    private void setChecksum() {
        int checksum = 0;
        for (int i = 0; i < _nDataChars - 1; i++) {
            checksum += _dataChars[i];
        }
        checksum = checksum & 0xff;
        if (checksum > 0) {
            checksum = 256 - checksum;
        }
        _dataChars[_nDataChars - 1] = checksum;
    }

    private SprogMessage frame() {
        int j = 2;
        // Create new message to hold the framed one
        SprogMessage f = new SprogMessage(MAXSIZE);
        f.setElement(0, STX);
        f.setElement(1, STX);
        // copy existing message adding DLE
        for (int i = 0; i < _nDataChars; i++) {
            if (_dataChars[i] == STX
                    || _dataChars[i] == ETX
                    || _dataChars[i] == DLE) {
                f.setElement(j++, DLE);
            }
            f.setElement(j++, _dataChars[i]);
        }
        f.setElement(j++, ETX);
        f._nDataChars = j;
        // return new message
        return f;
    }

    // display format
    @Override
    public String toString(){
       // default to not SIIBootMode being false.
       return this.toString(false);
    }

    public String toString(boolean isSIIBootMode) {
        StringBuffer buf = new StringBuffer();
        if (!isSIIBootMode) {
            for (int i = 0; i < _nDataChars; i++) {
                buf.append((char) _dataChars[i]);
            }
        } else {
            for (int i = 0; i < _nDataChars; i++) {
                //s+="<"+_dataChars[i]+">";
                buf.append("<");
                buf.append(_dataChars[i]);
                buf.append(">");
            }
        }
        return buf.toString();
    }

    /**
     * Get formatted message for direct output to stream - this is the final
     * format of the message as a byte array.
     *
     * @param sprogState a SprogState variable representing the current state of
     *                   the Sprog
     * @return the formatted message as a byte array
     */
    public byte[] getFormattedMessage(SprogState sprogState) {
        int len = this.getNumDataElements();

        // space for carriage return if required
        int cr = 0;
        if (sprogState != SprogState.SIIBOOTMODE) {
            cr = 1;
        }

        byte msg[] = new byte[len + cr];

        for (int i = 0; i < len; i++) {
            if (sprogState != SprogState.SIIBOOTMODE) {
               msg[i] = (byte) ( this.getElement(i) & 0x7f);
            } else {
               msg[i] = (byte) ( this.getElement(i));
            }
        }
        if (sprogState != SprogState.SIIBOOTMODE) {
            msg[len] = 0x0d;
        }
        return msg;
    }

    // diagnose format
    public boolean isKillMain() {
        return getOpCode() == '-';
    }

    public boolean isEnableMain() {
        return getOpCode() == '+';
    }

    // static methods to return a formatted message
    static public SprogMessage getEnableMain() {
        SprogMessage m = new SprogMessage(1);
        m.setOpCode('+');
        return m;
    }

    static public SprogMessage getKillMain() {
        SprogMessage m = new SprogMessage(1);
        m.setOpCode('-');
        return m;
    }

    static public SprogMessage getStatus() {
        SprogMessage m = new SprogMessage(1);
        m.setOpCode('S');
        return m;
    }

    /*
     * SPROG uses same commands for reading and writing, with the number of
     * parameters determining the action. Currently supports page mode and
     * bit direct modes. A single parameter is taken as the CV address to read.
     * Two parametes are taken as the CV address and data to be written.
     */
    static public SprogMessage getReadCV(int cv, ProgrammingMode mode) {
        SprogMessage m = new SprogMessage(6);
        if (mode == ProgrammingMode.PAGEMODE) {
            m.setOpCode('V');
        } else { // Bit direct mode
            m.setOpCode('C');
        }
        addSpace(m, 1);
        addIntAsFour(cv, m, 2);
        return m;
    }

    static public SprogMessage getWriteCV(int cv, int val, ProgrammingMode mode) {
        SprogMessage m = new SprogMessage(10);
        if (mode == ProgrammingMode.PAGEMODE) {
            m.setOpCode('V');
        } else { // Bit direct mode
            m.setOpCode('C');
        }
        addSpace(m, 1);
        addIntAsFour(cv, m, 2);
        addSpace(m, 6);
        addIntAsThree(val, m, 7);
        return m;
    }

    // [AC] 11/09/2002 SPROG doesn't currently support registered mode
    static public SprogMessage getReadRegister(int reg) { //Vx
        SprogMessage m = new SprogMessage(1);
        m.setOpCode(' ');
        return m;
    }

    static public SprogMessage getWriteRegister(int reg, int val) { //Sx xx
        SprogMessage m = new SprogMessage(1);
        m.setOpCode(' ');
        return m;
    }

    /**
     * Get a message containing a DCC packet.
     *
     * @param bytes byte[]
     * @return SprogMessage
     */
    static public SprogMessage getPacketMessage(byte[] bytes) {
        SprogMessage m = new SprogMessage(1 + 3 * bytes.length);
        int i = 0; // counter to make it easier to format the message

        m.setElement(i++, 'O');  // "O" Output DCC packet command
        for (int j = 0; j < bytes.length; j++) {
            m.setElement(i++, ' ');
            m.addIntAsTwoHex(bytes[j] & 0xFF, i);
            i = i + 2;
        }
        return m;
    }

    // Bootloader messages are initially created long enough for
    // the message and checksum. The message is then framed with control
    // characters before being returned
    static public SprogMessage getReadBootVersion() {
        SprogMessage m = new SprogMessage(3);
        m.setOpCode(RD_VER);
        m.setLength(2);
        m.setChecksum();
        return m.frame();
    }

    static public SprogMessage getWriteFlash(int addr, int[] data, int blockLen) {
        int l = data.length;
        int offset;
        // Writes are rounded up to multiples of blockLen
        if (l % blockLen != 0) {
            l = l + (blockLen - l % blockLen);
        }
        // and data padded with erased condition
        int padded[] = new int[l];
        for (int i = 0; i < l; i++) {
            padded[i] = 0xff;
        }
        // Address is masked to start on blockLen boundary
        if (blockLen == 16) {
            offset = addr & 0xF;
            addr = addr & 0xFFFFFFF0;
        } else {
            offset = addr & 0x7;
            addr = addr & 0xFFFFFFF8;
        }
        // Copy data into padded array at address offset
        for (int i = 0; i < data.length; i++) {
            padded[i + offset] = data[i];
        }
        SprogMessage m = new SprogMessage(6 + l);
        m.setOpCode(WT_FLASH);
        // length is number of blockLen blocks
        m.setLength(l / blockLen);
        m.setAddress(addr);
        m.setData(padded);
        m.setChecksum();
        return m.frame();
    }

    static public SprogMessage getEraseFlash(int addr, int rows) {
        SprogMessage m = new SprogMessage(6);
        m.setOpCode(ER_FLASH);
        // Erase a number of 64 byte rows
        m.setLength(rows);
        m.setAddress(addr);
        m.setChecksum();
        return m.frame();
    }

    static public SprogMessage getWriteEE(int addr, int[] data) {
        SprogMessage m = new SprogMessage(6 + data.length);
        m.setOpCode(WT_EEDATA);
        m.setLength(data.length);
        m.setAddress(addr & 0xff);
        m.setData(data);
        m.setChecksum();
        return m.frame();
    }

    static public SprogMessage getReset() {
        SprogMessage m = new SprogMessage(3);
        m.setOpCode(0);
        m.setLength(0);
        m.setChecksum();
        return m.frame();
    }

    // [AC] 11/09/2002
    private static String addSpace(SprogMessage m, int offset) {
        String s = " ";
        m.setElement(offset, ' ');
        return s;
    }

    // [AC] 11/09/2002
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification="was previously marked with @SuppressWarnings, reason unknown")
    private static String addIntAsTwo(int val, SprogMessage m, int offset) {
        String s = "" + val;
        if (s.length() != 2) {
            s = "0" + s;  // handle <10
        }
        m.setElement(offset, s.charAt(0));
        m.setElement(offset + 1, s.charAt(1));
        return s;
    }

    private static String addIntAsThree(int val, SprogMessage m, int offset) {
        String s = "" + val;
        if (s.length() != 3) {
            s = "0" + s;  // handle <10
        }
        if (s.length() != 3) {
            s = "0" + s;  // handle <100
        }
        m.setElement(offset, s.charAt(0));
        m.setElement(offset + 1, s.charAt(1));
        m.setElement(offset + 2, s.charAt(2));
        return s;
    }

    private static String addIntAsFour(int val, SprogMessage m, int offset) {
        String s = "" + val;
        if (s.length() != 4) {
            s = "0" + s;  // handle <10
        }
        if (s.length() != 4) {
            s = "0" + s;  // handle <100
        }
        if (s.length() != 4) {
            s = "0" + s;  // handle <1000
        }
        m.setElement(offset, s.charAt(0));
        m.setElement(offset + 1, s.charAt(1));
        m.setElement(offset + 2, s.charAt(2));
        m.setElement(offset + 3, s.charAt(3));
        return s;
    }

    private final static Logger log = LoggerFactory.getLogger(SprogMessage.class);

}
