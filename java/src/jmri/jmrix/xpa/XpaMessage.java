package jmri.jmrix.xpa;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encodes a message to an XpressNet command station via an XPA and a modem.
 *
 * @author	Paul Bender Copyright (C) 2004
 */
public class XpaMessage implements jmri.jmrix.Message {

    public final static int MAX_SIZE = 64;

    private int _nDataChars = 0;
    private byte _dataChars[] = null;

    // create a new one
    public XpaMessage(int i) {
        if (i < 1) {
            log.error("invalid length in call to ctor");
        }
        _nDataChars = i;
        _dataChars = new byte[i];
    }

    // create a new one, given a string containing the message.
    public XpaMessage(String S) {
        if (S.length() < 1) {
            log.error("zero length string in call to ctor");
        }
        _nDataChars = S.length();
        _dataChars = S.getBytes();
    }

    // create a new one with default MAX_SIZE
    public XpaMessage() {
        this(MAX_SIZE);
    }

    // copy one
    public XpaMessage(XpaMessage m) {
        if (m == null) {
            log.error("copy ctor of null message");
            return;
        }
        _nDataChars = m._nDataChars;
        _dataChars = new byte[_nDataChars];
        System.arraycopy(m._dataChars, 0, _dataChars, 0, _nDataChars);
    }

    // compare two XpaMessages.
    @Override
    public boolean equals(Object m) {
        if (m != null && m instanceof XpaMessage
                && ((XpaMessage) m).getNumDataElements() == this.getNumDataElements()) {
            return Arrays.equals(((XpaMessage) m)._dataChars, _dataChars);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + this._nDataChars;
        hash = 79 * hash + Arrays.hashCode(this._dataChars);
        return hash;
    }

    // accessors to the bulk data
    @Override
    public int getNumDataElements() {
        return _nDataChars;
    }

    @Override
    public int getElement(int n) {
        return _dataChars[n];
    }

    @Override
    public void setElement(int n, int v) {
        _dataChars[n] = (byte) (v & 0x7F);
    }

    // display format
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < _nDataChars; i++) {
            s.append((char) _dataChars[i]);
        }
        return s.toString();
    }

    // static methods to return a formatted message
    static XpaMessage getDefaultInitMsg() {
        XpaMessage m = new XpaMessage("ATX0E0;");
        return m;
    }


    /* Get a message which sends an Estop or Everything off command
     to the layout.  This will toggle the Estop commands.
     XPA settings can change the behavior of this command.  It may
     only work with a single locomotive, or it may kill the entire
     layout.
     */
    static XpaMessage getEStopMsg() {
        XpaMessage m = new XpaMessage("ATDT0;");
        return m;
    }

    // Locomotive Messages

    /*
     Get a message which sends an "Idle" (zero speed) command
     to a specific locomotive on the layout.
     */
    static XpaMessage getIdleMsg(int Address) {
        XpaMessage m = new XpaMessage("ATDT#" + Address + "*5;");
        return m;
    }

    /*
     Get a message for an "Increase Speed" command
     to a specific locomotive on the layout.  To make
     calculations easy, this uses a single speed step increase
     */
    static XpaMessage getIncSpeedMsg(int Address, int steps) {
        StringBuilder buf = new StringBuilder("ATDT#" + Address + "*");
        String Message;
        for (int i = 0; i < steps; i++) {
            buf.append("3");
        }
        Message = buf.toString() + ";";
        XpaMessage m = new XpaMessage(Message);
        return m;
    }

    /*
     Get a message for a "Decrease Speed" command
     to a specific locomotive on the layout.  To make
     calculations easy, this uses a single speed step increase
     */
    static XpaMessage getDecSpeedMsg(int Address, int steps) {
        StringBuilder buf = new StringBuilder("ATDT#" + Address + "*");
        String Message;
        for (int i = 0; i < steps; i++) {
            buf.append("1");
        }
        Message = buf.toString() + ";";
        XpaMessage m = new XpaMessage(Message);
        return m;
    }

    /*
     Get a message for a "Direction Forward" command
     to a specific locomotive on the layout.
     */
    static XpaMessage getDirForwardMsg(int Address) {
        XpaMessage m = new XpaMessage("ATDT#" + Address + "*52;");
        return m;
    }

    /*
     Get a message for a "Direction Reverse" command
     to a specific locomotive on the layout.
     */
    static XpaMessage getDirReverseMsg(int Address) {
        XpaMessage m = new XpaMessage("ATDT#" + Address + "*58;");
        return m;
    }

    /*
     Get a message which sends a "Toggle Function" command
     to a specific locomotive on the layout.
     */
    static XpaMessage getFunctionMsg(int Address, int Function) {
        XpaMessage m = new XpaMessage("ATDT#" + Address + "**" + Function + ";");
        return m;
    }

    // Switch Commands

    /*
     Get a message for a "Switch Possition Normal" command
     to a specific accessory decoder on the layout.
     */
    static XpaMessage getSwitchNormalMsg(int Address) {
        XpaMessage m = new XpaMessage("ATDT#" + Address + "#3;");
        return m;
    }

    /*
     Get a message for a "Switch Possition Reverse" command
     to a specific accessory decoder on the layout.
     */
    static XpaMessage getSwitchReverseMsg(int Address) {
        XpaMessage m = new XpaMessage("ATDT#" + Address + "#1;");
        return m;
    }

    // Xpa Device Settings
    /* Get a message for setting a Device value */
    public static XpaMessage getDeviceSettingMsg(int setting) {
        XpaMessage m = new XpaMessage("ATDT*" + setting + "*");
        return m;
    }

    private final static Logger log = LoggerFactory.getLogger(XpaMessage.class
            .getName());

}
