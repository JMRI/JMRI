package jmri.jmrix.bachrus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Carries the reply to an SprogMessage
 *
 * The format of a KPF-Zeller message is <code>*0000;V3.0%\n</code>
 * but because we terminate on ";", it comes across as
 * <code>V3.0%\n*0000;</code>
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
        if (m.isUnsolicited()) {
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
        log.debug("getCount of n= {} '{}'", _nDataChars, this);
        // KPF-Zeller formatting
        if (_nDataChars == 12) {
            try {
                log.trace("selected out '{}'", this.toString().substring(7, 11));
                return Integer.parseInt(this.toString().substring(7, 11), 10);
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
        // bachrus formatting
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

    /**
     * Series numbers define the actual hardware, i.e. wheel circumference.
     * <dl>
     * <dt>0</dt><dd>none, ignore</dd>
     * <dt>4</dt><dd>Reader 40</dd>
     * <dt>5</dt><dd>Reader 50</dd>
     * <dt>6</dt><dd>Reader 60</dd>
     * <dt>103</dt><dd>KPR-Zeller</dd>
     * </dl>
     */
    public int getSeries() {
        log.debug("getSeries of n= {} '{}'", _nDataChars, this);
        // KPF-Zeller formatting
        if (_nDataChars == 12) {
            return 103;
        }
        // bachrus formatting
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
