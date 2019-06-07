package jmri.jmrix.qsi;

import jmri.ProgrammingMode;
import jmri.util.StringUtil;

/**
 * Encodes a message to an QSI command station.
 * <p>
 * The {@link QsiReply} class handles the response from the command station.
 *
 * @author	Bob Jacobsen Copyright (C) 2007, 2008
 */
public class QsiMessage extends jmri.jmrix.AbstractMessage {

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
    static final int MAXSIZE = 515;

    // create a new one
    public QsiMessage(int i) {
        super(i);
    }

    // from String
    public QsiMessage(String s) {
        super(s);
    }

    // copy one
    public QsiMessage(QsiMessage m) {
        super(m);
    }

    public void setOpCode(int i) {
        _dataChars[0] = i;
    }

    public int getOpCode() {
        return _dataChars[0];
    }

    public String getOpCodeHex() {
        return "0x" + Integer.toHexString(getOpCode());
    }

    public void setLength(int i) {
        _dataChars[1] = i;
    }

    public void setV4Length(int i) {
        _dataChars[0] = hexDigit((i & 0xf0) >> 4);
        _dataChars[1] = hexDigit(i & 0xf);
    }

    public void setAddress(int i) {
        _dataChars[2] = i & 0xff;
        _dataChars[3] = (i >> 8) & 0xff;
        _dataChars[4] = i >> 16;
    }

    public void setV4Address(int i) {
        _dataChars[2] = hexDigit((i & 0xf000) >> 12);
        _dataChars[3] = hexDigit((i & 0xf00) >> 8);
        _dataChars[4] = hexDigit((i & 0xf0) >> 4);
        _dataChars[5] = hexDigit(i & 0xf);
    }

    public void setV4RecType(int i) {
        _dataChars[6] = hexDigit((i & 0xf0) >> 4);
        _dataChars[7] = hexDigit(i & 0xf);
    }

    public void setData(int[] d) {
        System.arraycopy(d, 0, _dataChars, 5, d.length);
    }

    public void setV4Data(int[] d) {
        int j = 8;
        for (int i = 0; i < d.length; i++) {
            _dataChars[j++] = hexDigit((d[i] & 0xf0) >> 4);
            _dataChars[j++] = hexDigit(d[i] & 0xf);
        }
    }

    public void setChecksum() {
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

    public void setV4Checksum(int length, int addr, int type, int[] data) {
        int checksum = length + ((addr & 0xff00) >> 8) + (addr & 0xff) + type;
        for (int i = 0; i < data.length; i++) {
            checksum += data[i];
        }
        checksum = checksum & 0xff;
        if (checksum > 0) {
            checksum = 256 - checksum;
        }
        _dataChars[_nDataChars - 2] = hexDigit((checksum & 0xf0) >> 4);
        _dataChars[_nDataChars - 1] = hexDigit(checksum & 0x0f);
    }

    private int hexDigit(int b) {
        if (b > 9) {
            return (b - 9 + 0x40);
        } else {
            return (b + 0x30);
        }
    }

    public QsiMessage frame() {
        int j = 2;
        // Create new message to hold the framed one
        QsiMessage f = new QsiMessage(MAXSIZE);
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

    public QsiMessage v4frame() {
        int i;
        // Create new message to hold the framed one
        QsiMessage f = new QsiMessage(MAXSIZE);
        f.setElement(0, ':');
        // copy existing message adding CRLF
        for (i = 1; i <= _nDataChars; i++) {
            f.setElement(i, _dataChars[i - 1]);
        }
        f.setElement(i++, CR);
        f.setElement(i++, LF);
        f._nDataChars = i;
        // return new message
        return f;
    }

    @Override
    public String toString() {
        QsiSystemConnectionMemo memo = jmri.InstanceManager.getDefault(jmri.jmrix.qsi.QsiSystemConnectionMemo.class);
        return toString(memo.getQsiTrafficController());
    }

    public String toString(QsiTrafficController controller) {
        if (_dataChars == null) {
            return "<none>";
        }
        StringBuilder s = new StringBuilder("");
        if (controller == null || controller.isSIIBootMode()) {
            for (int i = 0; i < _nDataChars; i++) {
                s.append(StringUtil.twoHexFromInt(_dataChars[i])).append(" ");
            }
        } else {
            for (int i = 0; i < _nDataChars; i++) {
                s.append("<").append(_dataChars[i]).append(">");
            }
        }
        return s.toString();
    }

    // diagnose format
    public boolean isKillMain() {
        return getOpCode() == '-';
    }

    public boolean isEnableMain() {
        return getOpCode() == '+';
    }

    // static methods to return a formatted message
    static public QsiMessage getEnableMain() {
        QsiMessage m = new QsiMessage(1);
        m.setOpCode('+');
        return m;
    }

    static public QsiMessage getKillMain() {
        QsiMessage m = new QsiMessage(1);
        m.setOpCode('-');
        return m;
    }

    static public QsiMessage getProgMode() {
        QsiMessage m = new QsiMessage(1);
        m.setOpCode('P');
        return m;
    }

    // [AC] 11/09/2002 Leave QSI in programmer mode. Don't want to go
    // to booster mode as this would power up the track.
    static public QsiMessage getExitProgMode() {
        QsiMessage m = new QsiMessage(1);
        m.setOpCode(' ');
        return m;
    }

    static public QsiMessage getClearStatus() {
        // OP_REQ_CLEAR_ERROR_STATUS
        QsiMessage m = new QsiMessage(3);
        m.setElement(0, 17);
        m.setElement(1, 0);
        m.setElement(2, 0);
        return m;
    }

    static public QsiMessage getReadCV(int cv, ProgrammingMode mode) {
        // OP_REQ_READ_CV
        QsiMessage m = new QsiMessage(4);
        m.setElement(0, 9);
        m.setElement(1, 1);
        m.setElement(2, 0);
        m.setElement(3, cv);
        return m;
    }

    static public QsiMessage getWriteCV(int cv, int val, ProgrammingMode mode) {
        // OP_REQ_WRITE_CV
        QsiMessage m = new QsiMessage(5);
        m.setElement(0, 30);
        m.setElement(1, 2);
        m.setElement(2, 0);
        m.setElement(3, cv);
        m.setElement(4, val);
        return m;
    }

    // [AC] 11/09/2002 QSI doesn't currently support registered mode
    static public QsiMessage getReadRegister(int reg) { //Vx
        //        if (reg>8) log.error("register number too large: "+reg);
        //        QsiMessage m = new QsiMessage(2);
        //        m.setOpCode('V');
        //        String s = ""+reg;
        //        m.setElement(1, s.charAt(s.length()-1));
        //        return m;
        QsiMessage m = new QsiMessage(1);
        m.setOpCode(' ');
        return m;
    }

    static public QsiMessage getWriteRegister(int reg, int val) { //Sx xx
        //        if (reg>8) log.error("register number too large: "+reg);
        //        QsiMessage m = new QsiMessage(4);
        //        m.setOpCode('S');
        //        String s = ""+reg;
        //        m.setElement(1, s.charAt(s.length()-1));
        //        addIntAsTwoHex(val, m, 2);
        //        return m;
        QsiMessage m = new QsiMessage(1);
        m.setOpCode(' ');
        return m;
    }

    // Bootloader messages are initially created long enough for
    // the message and checksum. The message is then framed with control
    // characters before being returned
    static public QsiMessage getReadBootVersion() {
        QsiMessage m = new QsiMessage(3);
        m.setOpCode(RD_VER);
        m.setLength(2);
        m.setChecksum();
        return m.frame();
    }

    static public QsiMessage getWriteFlash(int addr, int[] data) {
        int l = data.length;
        // Writes are rounded up to multiples of 8 bytes
        if (l % 8 != 0) {
            l = l + (8 - l % 8);
        }
        // and data padded with erased condition
        int padded[] = new int[l];
        for (int i = 0; i < l; i++) {
            if (i < data.length) {
                padded[i] = data[i];
            } else {
                padded[i] = 0xff;
            }
        }
        QsiMessage m = new QsiMessage(6 + l);
        m.setOpCode(WT_FLASH);
        // length is number of 8 byte blocks
        m.setLength(l / 8);
        m.setAddress(addr);
        m.setData(padded);
        m.setChecksum();
        return m.frame();
    }

    static public QsiMessage getV4WriteFlash(int addr, int[] data, int type) {
        // Create a v4 bootloader message which is same format as a record
        // in the hex file
        int l = (data.length + 5) * 2;
        QsiMessage m = new QsiMessage(l);
        m.setV4Length(data.length);
        m.setV4Address(addr);
        m.setV4RecType(type);
        m.setV4Data(data);
        m.setV4Checksum(data.length, addr, type, data);
        return m.v4frame();
    }

    static public QsiMessage getV4EndOfFile() {
        // Create a v4 bootloader end of file message
        int l = 10;
        QsiMessage m = new QsiMessage(l);
        m.setV4Length(0);
        m.setV4Address(0);
        m.setV4RecType(1);
        m.setV4Checksum(0, 0, 1, new int[0]);
        return m.v4frame();
    }

    static public QsiMessage getv4ExtAddr() {
        // Create a v4 bootloader extended address message
        int l = 14;
        int[] data = {0, 0};
        QsiMessage m = new QsiMessage(l);
        m.setV4Length(2);
        m.setV4Address(0);
        m.setV4RecType(4);
        m.setV4Data(data);
        m.setV4Checksum(0, 0, 4, data);
        return m.v4frame();
    }

    static public QsiMessage getEraseFlash(int addr, int rows) {
        QsiMessage m = new QsiMessage(6);
        m.setOpCode(ER_FLASH);
        // Erase a number of 64 byte rows
        m.setLength(rows);
        m.setAddress(addr);
        m.setChecksum();
        return m.frame();
    }

    static public QsiMessage getWriteEE(int addr, int[] data) {
        QsiMessage m = new QsiMessage(6 + data.length);
        m.setOpCode(WT_EEDATA);
        m.setLength(data.length);
        m.setAddress(addr & 0xff);
        m.setData(data);
        m.setChecksum();
        return m.frame();
    }

    static public QsiMessage getReset() {
        QsiMessage m = new QsiMessage(3);
        m.setOpCode(0);
        m.setLength(0);
        m.setChecksum();
        return m.frame();
    }

}
