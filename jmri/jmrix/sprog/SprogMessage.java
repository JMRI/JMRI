// SprogMessage.java

package jmri.jmrix.sprog;

import jmri.Programmer;

/**
 * Encodes a message to an SPROG command station.
 * <P>
 * The {@link SprogReply}
 * class handles the response from the command station.
 *
 * @author	Bob Jacobsen  Copyright (C) 2001
 * @version	$Revision: 1.3 $
 */
public class SprogMessage {
    // is this logically an abstract class?

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
    public static int MAXSIZE = 515;

    // create a new one
    public  SprogMessage(int i) {
        if (i<1)
            log.error("invalid length in call to ctor");
        _nDataChars = i;
        _dataChars = new int[i];
    }

    // from String
    public SprogMessage(String s) {
            _nDataChars = s.length();
            _dataChars = new int[_nDataChars];
            for (int i = 0; i<_nDataChars; i++)
                    _dataChars[i] = s.charAt(i);
    }

    // copy one
    public  SprogMessage(SprogMessage m) {
        if (m == null)
            log.error("copy ctor of null message");
        _nDataChars = m._nDataChars;
        _dataChars = new int[_nDataChars];
        for (int i = 0; i<_nDataChars; i++) _dataChars[i] = m._dataChars[i];
    }

    public void setOpCode(int i) { _dataChars[0]=i;}
    public int getOpCode() {return _dataChars[0];}
    public String getOpCodeHex() { return "0x"+Integer.toHexString(getOpCode()); }

    // accessors to the bulk data
    public int getNumDataElements() {return _nDataChars;}
    public int getElement(int n) {return _dataChars[n];}
    public void setElement(int n, int v) {
      if (!SprogTrafficController.instance().isSIIBootMode()) {v &= 0x7f;}
      _dataChars[n] = v;
    }

    public void setLength(int i) { _dataChars[1]=i;}

    public void setV4Length(int i) {
        _dataChars[0] = hexDigit((i&0xf0)>>4);
        _dataChars[1] = hexDigit(i&0xf);
    }

    public void setAddress(int i) {
        _dataChars[2] = i&0xff;
        _dataChars[3] = (i>>8)&0xff;
        _dataChars[4] = i>>16;
    }

    public void setV4Address(int i) {
        _dataChars[2] = hexDigit((i&0xf000)>>12);
        _dataChars[3] = hexDigit((i&0xf00)>>8);
        _dataChars[4] = hexDigit((i&0xf0)>>4);
        _dataChars[5] = hexDigit(i&0xf);
    }

    public void setV4RecType(int i) {
        _dataChars[6] = hexDigit((i&0xf0)>>4);
        _dataChars[7] = hexDigit(i&0xf);
    }

    public void setData(int [] d) {
        for (int i = 0; i < d.length; i++) {
            _dataChars[5+i] = d[i];
        }
    }

    public void setV4Data(int [] d) {
        int j = 8;
        for (int i = 0; i < d.length; i++) {
            _dataChars[j++] = hexDigit((d[i]&0xf0)>>4);
            _dataChars[j++] = hexDigit(d[i]&0xf);
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

    public void setV4Checksum(int length, int addr, int type, int [] data) {
      int checksum = length + ((addr&0xff00)>>8) + (addr&0xff) + type;
      for (int i = 0; i < data.length; i++) {
        checksum += data[i];
      }
      checksum = checksum & 0xff;
      if (checksum > 0) {
        checksum = 256 - checksum;
      }
      _dataChars[_nDataChars - 2] = hexDigit((checksum&0xf0)>>4);
      _dataChars[_nDataChars - 1] = hexDigit(checksum&0x0f);
    }

    private int hexDigit(int b) {
      if (b > 9) {
        return (b - 9 + 0x40);
      }
      else {
        return (b + 0x30);
      }
    }

    public SprogMessage frame() {
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

    public SprogMessage v4frame() {
      int i=0;
      // Create new message to hold the framed one
      SprogMessage f = new SprogMessage(MAXSIZE);
      f.setElement(0, (int)':');
      // copy existing message adding CRLF
      for (i = 1; i <= _nDataChars; i++) {
        f.setElement(i, _dataChars[i-1]);
      }
      f.setElement(i++, CR);
      f.setElement(i++, LF);
      f._nDataChars = i;
      // return new message
      return f;
    }

    // display format
    public String toString() {
        String s = "";
        if (!SprogTrafficController.instance().isSIIBootMode()) {
            for (int i=0; i<_nDataChars; i++) {
                s+=(char)_dataChars[i];
            }
        } else {
            for (int i=0; i<_nDataChars; i++) {
                s+="<"+(int)_dataChars[i]+">";
            }
        }
        return s;
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

    static public SprogMessage getProgMode() {
        SprogMessage m = new SprogMessage(1);
        m.setOpCode('P');
        return m;
    }

    // [AC] 11/09/2002 Leave SPROG in programmer mode. Don't want to go
    // to booster mode as this would power up the track.
    static public SprogMessage getExitProgMode() {
        SprogMessage m = new SprogMessage(1);
        m.setOpCode(' ');
        return m;
    }

    /*
     * SPROG uses same commands for reading and writing, with the number of
     * parameters determining the action. Currently supports page mode and
     * bit direct modes. A single parameter is taken as the CV address to read.
     * Two parametes are taken as the CV address and data to be written.
    */
    static public SprogMessage getReadCV(int cv, int mode) {
        SprogMessage m = new SprogMessage(5);
        if (mode == Programmer.PAGEMODE) {
          m.setOpCode('V');
        } else { // Bit direct mode
          m.setOpCode('C');
        }
        addSpace(m, 1);
        addIntAsThree(cv, m, 2);
        return m;
    }

    static public SprogMessage getWriteCV(int cv, int val, int mode) {
        SprogMessage m = new SprogMessage(9);
        if (mode == Programmer.PAGEMODE) {
          m.setOpCode('V');
        } else { // Bit direct mode
          m.setOpCode('C');
        }
        addSpace(m, 1);
        addIntAsThree(cv, m, 2);
        addSpace(m, 5);
        addIntAsThree(val, m, 6);
        return m;
    }

    // [AC] 11/09/2002 SPROG doesn't currently support registered mode
    static public SprogMessage getReadRegister(int reg) { //Vx
//        if (reg>8) log.error("register number too large: "+reg);
//        SprogMessage m = new SprogMessage(2);
//        m.setOpCode('V');
//        String s = ""+reg;
//        m.setElement(1, s.charAt(s.length()-1));
//        return m;
      SprogMessage m = new SprogMessage(1);
      m.setOpCode(' ');
      return m;
    }

    static public SprogMessage getWriteRegister(int reg, int val) { //Sx xx
//        if (reg>8) log.error("register number too large: "+reg);
//        SprogMessage m = new SprogMessage(4);
//        m.setOpCode('S');
//        String s = ""+reg;
//        m.setElement(1, s.charAt(s.length()-1));
//        addIntAsTwoHex(val, m, 2);
//        return m;
        SprogMessage m = new SprogMessage(1);
        m.setOpCode(' ');
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

    static public SprogMessage getWriteFlash(int addr, int [] data) {
      int l = data.length;
      // Writes are rounded up to multiples of 8 bytes
      if (l%8 != 0) { l = l + (8 - l%8);}
      // and data padded with erased condition
      int padded[] = new int[l];
      for (int i = 0; i < l; i++) {
        if (i < data.length) {
          padded[i] = data[i];
        } else {
          padded[i] = 0xff;
        }
      }
      SprogMessage m = new SprogMessage(6 + l);
      m.setOpCode(WT_FLASH);
      // length is number of 8 byte blocks
      m.setLength(l/8);
      m.setAddress(addr);
      m.setData(padded);
      m.setChecksum();
      return m.frame();
    }

    static public SprogMessage getV4WriteFlash(int addr, int [] data, int type) {
      // Create a v4 bootloader message which is same format as a record
      // in the hex file
      int l = (data.length + 5)*2;
      SprogMessage m = new SprogMessage(l);
      m.setV4Length(data.length);
      m.setV4Address(addr);
      m.setV4RecType(type);
      m.setV4Data(data);
      m.setV4Checksum(data.length, addr, type, data);
      return m.v4frame();
    }

    static public SprogMessage getV4EndOfFile() {
      // Create a v4 bootloader end of file message
      int l = 10;
      SprogMessage m = new SprogMessage(l);
      m.setV4Length(0);
      m.setV4Address(0);
      m.setV4RecType(1);
      m.setV4Checksum(0, 0, 1, new int [0]);
      return m.v4frame();
    }

    static public SprogMessage getv4ExtAddr() {
      // Create a v4 bootloader extended address message
      int l = 14;
      int [] data = {0, 0};
      SprogMessage m = new SprogMessage(l);
      m.setV4Length(2);
      m.setV4Address(0);
      m.setV4RecType(4);
      m.setV4Data(data);
      m.setV4Checksum(0, 0, 4, data);
      return m.v4frame();
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

    static public SprogMessage getWriteEE(int addr, int [] data) {
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

    // contents (private)
    private int _nDataChars = 0;
    private int _dataChars[] = null;

    // [AC] 11/09/2002
    private static String addSpace(SprogMessage m, int offset) {
      String s = " ";
      m.setElement(offset, ' ');
      return s;
    }

    // [AC] 11/09/2002
    private static String addIntAsTwo(int val, SprogMessage m, int offset) {
        String s = ""+val;
        if (s.length() != 2) s = "0"+s;  // handle <10
        m.setElement(offset,s.charAt(0));
        m.setElement(offset+1,s.charAt(1));
        return s;
    }

    private static String addIntAsThree(int val, SprogMessage m, int offset) {
        String s = ""+val;
        if (s.length() != 3) s = "0"+s;  // handle <10
        if (s.length() != 3) s = "0"+s;  // handle <100
        m.setElement(offset,s.charAt(0));
        m.setElement(offset+1,s.charAt(1));
        m.setElement(offset+2,s.charAt(2));
        return s;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogMessage.class.getName());

}

/* @(#)SprogMessage.java */
