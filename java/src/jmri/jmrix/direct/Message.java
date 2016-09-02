package jmri.jmrix.direct;

import javax.annotation.Nonnull;
import jmri.ProgrammingMode;
import jmri.managers.DefaultProgrammerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encodes a message for direct DCC
 * <P>
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 */
public class Message extends jmri.jmrix.AbstractMRMessage {

    // create a new one
    public Message(int i) {
        if (i < 1) {
            log.error("invalid length in call to ctor");
        }
        _nDataChars = i;
        _dataChars = new int[i];
    }

    // copy one
    @SuppressWarnings("null")
    public Message(@Nonnull Message m) {
        _nDataChars = m._nDataChars;
        _dataChars = new int[_nDataChars];
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = m._dataChars[i];
        }
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

    // accessors to the bulk data
    public int getNumDataElements() {
        return _nDataChars;
    }

    public int getElement(int n) {
        return _dataChars[n];
    }

    public void setElement(int n, int v) {
        _dataChars[n] = v & 0x7F;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION")
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    public String toString() {
        String s = "";
        for (int i = 0; i < _nDataChars; i++) {
            s += (char) _dataChars[i];
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
    static public Message getEnableMain() {
        log.error("getEnableMain doesnt have a reasonable implementation yet");
        return null;
    }

    static public Message getKillMain() {
        log.error("getKillMain doesnt have a reasonable implementation yet");
        return null;
    }

    static public Message getProgMode() {
        log.error("getProgMode doesnt have a reasonable implementation yet");
        return null;
    }

    static public Message getExitProgMode() {
        log.error("getExitProgMode doesnt have a reasonable implementation yet");
        return null;
    }

    static public Message getReadCV(int cv, ProgrammingMode mode) {
        Message m = new Message(5);
        if (mode.equals(DefaultProgrammerManager.PAGEMODE)) {
            m.setOpCode('V');
        } else { // Bit direct mode
            m.setOpCode('C');
        }
        addSpace(m, 1);
        addIntAsThree(cv, m, 2);
        return m;
    }

    static public Message getWriteCV(int cv, int val, ProgrammingMode mode) {
        Message m = new Message(9);
        if (mode.equals(DefaultProgrammerManager.PAGEMODE)) {
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

    static public Message getReadRegister(int reg) { //Vx
        return null;
    }

    static public Message getWriteRegister(int reg, int val) { //Sx xx
        return null;
    }

    // contents (private)
    private int _nDataChars = 0;
    private int _dataChars[] = null;

    private static String addSpace(Message m, int offset) {
        String s = " ";
        m.setElement(offset, ' ');
        return s;
    }

    @SuppressWarnings("unused")
    private static String addIntAsTwo(int val, Message m, int offset) {
        String s = "" + val;
        if (s.length() != 2) {
            s = "0" + s;  // handle <10
        }
        m.setElement(offset, s.charAt(0));
        m.setElement(offset + 1, s.charAt(1));
        return s;
    }

    private static String addIntAsThree(int val, Message m, int offset) {
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

    private final static Logger log = LoggerFactory.getLogger(Message.class.getName());

}
