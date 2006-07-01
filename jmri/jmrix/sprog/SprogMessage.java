// SprogMessage.java

package jmri.jmrix.sprog;

import jmri.Programmer;

/**
 * Description:		<describe the SprogMessage class here>
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Revision: 1.1 $
 */
public class SprogMessage {
    // is this logically an abstract class?

    // create a new one
    public  SprogMessage(int i) {
        if (i<1)
            log.error("invalid length in call to ctor");
        _nDataChars = i;
        _dataChars = new int[i];
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
    public void setElement(int n, int v) { _dataChars[n] = v&0x7F; }

    // display format
    public String toString() {
        String s = "";
        for (int i=0; i<_nDataChars; i++) {
            s+=(char)_dataChars[i];
        }
        return s;
    }

    // diagnose format
    public boolean isKillMain() {
      return getOpCode() == '-';
    }

    public boolean isEnableMain() {
        return getOpCode() == ' ';
    }


    // static methods to return a formatted message

    // [AC] 11/09/2002 SPROG doesn't have separate main and programming outputs
    // so send null message
    static public SprogMessage getEnableMain() {
        SprogMessage m = new SprogMessage(1);
        m.setOpCode(' ');
        return m;
    }

    // [AC] 11/09/2002 Just to be sure
    static public SprogMessage getKillMain() {
        SprogMessage m = new SprogMessage(1);
        m.setOpCode('-');
        return m;
    }

    // [AC] 11/09/2002 SPROG will normally be in prog mode but just in case
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

    /*
     * These need re-writing. added 'h' prefix but need to copy correct length string
     * not used at the moment
    private static String addIntAsTwoHex(int val, SprogMessage m, int offset) {
        String s = "h"+Integer.toHexString(val).toUpperCase();
        if (s.length() != 2) s = "0"+s;  // handle <10
        m.setElement(offset,s.charAt(0));
        m.setElement(offset+1,s.charAt(1));
        return s;
    }

    private static String addIntAsThreeHex(int val, SprogMessage m, int offset) {
        String s = "h"+Integer.toHexString(val).toUpperCase();
        if (s.length() != 3) s = "0"+s;  // handle <10
        if (s.length() != 3) s = "0"+s;  // handle <10
        m.setElement(offset,s.charAt(0));
        m.setElement(offset+1,s.charAt(1));
        m.setElement(offset+2,s.charAt(2));
        return s;
    }
    */

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogMessage.class.getName());

}


/* @(#)SprogMessage.java */
