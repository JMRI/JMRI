package jmri;

/**
 * Thrown to indicate that a Timebase can't handle a particular rate setting
 * that's been requested.
 *
 * @author Bob Jacobsen Copyright (C) 2004
 */
public class TimebaseRateException extends JmriException {

    public TimebaseRateException(String s) {
        super(s);
    }

    public TimebaseRateException() {
    }

}
