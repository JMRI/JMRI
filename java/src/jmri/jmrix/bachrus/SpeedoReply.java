package jmri.jmrix.bachrus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Carries the reply to an SprogMessage
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Andrew Crosland Copyright (C) 2010
 */
public class SpeedoReply extends jmri.jmrix.AbstractMRReply {
 // This should be an extension af AbstractMRReply and needs re-factoring

    // create a new one
    public SpeedoReply() {
	super();
    }

    // copy one
    public SpeedoReply(SpeedoReply m) {
        this();
        if (m == null) {
            log.error("copy ctor of null message");
            return;
        }
        _nDataChars = m._nDataChars;
	if(m.isUnsolicited()) {
           setUnsolicited();
	}
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

    public int getCount() {
        // don't return 0 as it will cause an exception
        if (_nDataChars < 9) {
            return -1;
        }
        try {
            return Integer.parseInt(this.toString().substring(2, 8), 16);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public int getSeries() {
        if (_nDataChars < 7) {
            return 0;
        }
        try {
            return Integer.parseInt(this.toString().substring(1, 2));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    @Override
    public int match(String s) {
        // find a specific string in the reply
        String rep = new String(_dataChars, 0, _nDataChars);
        return rep.indexOf(s);
    }

    static public final int maxSize = 32;

    @Override
    public int maxSize() {
        return maxSize;
    }

    /**
     * skipPrefix is not used at this point in time, but is 
     * defined as abstract in AbstractMRReply
     */
    @Override
    protected int skipPrefix(int index) {
        return -1;
    }

    private final static Logger log = LoggerFactory.getLogger(SpeedoReply.class);

}
