package jmri.jmrix.bachrus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SpeedoReply.java
 *
 * Description:	Carries the reply to an SprogMessage
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @author	Andrew Crosland Copyright (C) 2010
 */
public class SpeedoReply {
	// This should be an extension af AbstractMRReply and needs re-factoring

    // create a new one
    public SpeedoReply() {
        unsolicited = false;
    }

    // copy one
    @SuppressWarnings("null")
    public SpeedoReply(SpeedoReply m) {
        this();
        if (m == null) {
            log.error("copy ctor of null message");
            return;
        }
        _nDataChars = m._nDataChars;
        unsolicited = m.unsolicited;
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = m._dataChars[i];
        }
    }

    // from String
    public SpeedoReply(String s) {
        this();
        _nDataChars = s.length();
        for (int i = 0; i < _nDataChars; i++) {
            _dataChars[i] = s.charAt(i);
        }
    }

    public void setOpCode(int i) {
        _dataChars[0] = (char) i;
    }

    public int getOpCode() {
        return _dataChars[0];
    }

    public final void setUnsolicited() {
        unsolicited = true;
    }

    public boolean isUnsolicited() {
        return unsolicited;
    }

    // accessors to the bulk data
    public int getNumDataElements() {
        return _nDataChars;
    }

    public int getElement(int n) {
        return _dataChars[n];
    }

    public void setElement(int n, int v) {
        _dataChars[n] = (char) v;
        _nDataChars = Math.max(_nDataChars, n + 1);
    }

    public int getCount() {
        // don't return 0 as it will cause an exception
        if (_nDataChars < 9) {
            return -1;
        }
        try {
            return Integer.valueOf(this.toString().substring(2, 8), 16);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public int getSeries() {
        if (_nDataChars < 7) {
            return 0;
        }
        try {
            return Integer.valueOf(this.toString().substring(1, 2));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    // display format
    public String toString() {
//        String s = "";
//        for (int i = 0; i < _nDataChars; i++) {
//            s += _dataChars[i];
//        }
//        return s;
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < _nDataChars; i++) {
            buf.append(_dataChars[i]);
        }
        return buf.toString();
    }

    int match(String s) {
        // find a specific string in the reply
        String rep = new String(_dataChars, 0, _nDataChars);
        return rep.indexOf(s);
    }

    int skipWhiteSpace(int index) {
        // start at index, passing any whitespace & control characters at the start of the buffer
        while (index < getNumDataElements() - 1
                && ((char) getElement(index) <= ' ')) {
            index++;
        }
        return index;
    }

    static public final int maxSize = 32;

    // contents (private)
    private int _nDataChars;
    private char _dataChars[] = new char[maxSize];
    private boolean unsolicited;
    private final static Logger log = LoggerFactory.getLogger(SpeedoReply.class.getName());
}
