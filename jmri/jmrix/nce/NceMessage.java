// NceMessage.java

package jmri.jmrix.nce;

/**
 * Encodes a message to an NCE command station.  The NceReply
 * class handles the response from the command station.
 * <P>
 * There's some rudimentary support for binary commands, but it's
 * not to the point of being usable.
 *
 * @author	        Bob Jacobsen  Copyright (C) 2001
 * @version             $Revision: 1.5 $
 */
public class NceMessage {
    // is this logically an abstract class?

    public NceMessage() {
        setBinary(false);
    }

    // create a new one
    public  NceMessage(int i) {
        this();
        if (i<1)
            log.error("invalid length in call to ctor");
        _nDataChars = i;
        _dataChars = new int[i];
    }

    // copy one
    public  NceMessage(NceMessage m) {
        this();
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
    public void setElement(int n, int v) { _dataChars[n] = v; }

    // mode accessors
    boolean _isBinary;
    public boolean isBinary() { return _isBinary; }
    public void setBinary(boolean b) { _isBinary = b; }

    // display format
    public String toString() {
        String s = "";
        for (int i=0; i<_nDataChars; i++) {
            if (_isBinary) {
                if (i!=0) s+=" ";
                if (_dataChars[i] < 16) s+="0";
                s+=Integer.toHexString(_dataChars[i]);
            } else {
                s+=(char)_dataChars[i];
            }
        }
        return s;
    }

    // diagnose format
    public boolean isKillMain() {
        return getOpCode() == 'K';
    }

    public boolean isEnableMain() {
        return getOpCode() == 'E';
    }


    // static methods to return a formatted message
    static public NceMessage getEnableMain() {
        NceMessage m = new NceMessage(1);
        m.setBinary(false);
        m.setOpCode('E');
        return m;
    }

    static public NceMessage getKillMain() {
        NceMessage m = new NceMessage(1);
        m.setBinary(false);
        m.setOpCode('K');
        return m;
    }

    static public NceMessage getProgMode() {
        NceMessage m = new NceMessage(1);
        m.setBinary(false);
        m.setOpCode('M');
        return m;
    }

    static public NceMessage getExitProgMode() {
        NceMessage m = new NceMessage(1);
        m.setBinary(false);
        m.setOpCode('X');
        return m;
    }

    static public NceMessage getReadPagedCV(int cv) { //Rxxx
        NceMessage m = new NceMessage(4);
        m.setBinary(false);
        m.setOpCode('R');
        addIntAsThree(cv, m, 1);
        return m;
    }

    static public NceMessage getWritePagedCV(int cv, int val) { //Pxxx xxx
        NceMessage m = new NceMessage(8);
        m.setBinary(false);
        m.setOpCode('P');
        addIntAsThree(cv, m, 1);
        m.setElement(4,' ');
        addIntAsThree(val, m, 5);
        return m;
    }

    static public NceMessage getReadRegister(int reg) { //Vx
        if (reg>8) log.error("register number too large: "+reg);
        NceMessage m = new NceMessage(2);
        m.setBinary(false);
        m.setOpCode('V');
        String s = ""+reg;
        m.setElement(1, s.charAt(s.length()-1));
        return m;
    }

    static public NceMessage getWriteRegister(int reg, int val) { //Sx xxx
        if (reg>8) log.error("register number too large: "+reg);
        NceMessage m = new NceMessage(6);
        m.setBinary(false);
        m.setOpCode('S');
        String s = ""+reg;
        m.setElement(1, s.charAt(s.length()-1));
        m.setElement(2,' ');
        addIntAsThree(val, m, 3);
        return m;
    }

    // contents (private)
    private int _nDataChars = 0;
    private int _dataChars[] = null;

    static String addIntAsThree(int val, NceMessage m, int offset) {
        String s = ""+val;
        if (s.length() != 3) s = "0"+s;  // handle <10
        if (s.length() != 3) s = "0"+s;  // handle <100
        m.setElement(offset,s.charAt(0));
        m.setElement(offset+1,s.charAt(1));
        m.setElement(offset+2,s.charAt(2));
        return s;
    }

    static String addIntAsTwoHex(int val, NceMessage m, int offset) {
        String s = (""+Integer.toHexString(val)).toUpperCase();
        if (s.length() < 2) s = "0"+s;  // handle one digit
        if (s.length() > 2) log.error("can't add as two hex digits: "+s);
        m.setElement(offset,s.charAt(0));
        m.setElement(offset+1,s.charAt(1));
        return s;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceMessage.class.getName());

}


/* @(#)NceMessage.java */
