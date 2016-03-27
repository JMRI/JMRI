package jmri;

/**
 * Thrown to indicate that a Timebase can't handle a particular rate setting
 * that's been requested.
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 * @version	$Revision$
 */
public class TimebaseRateException extends JmriException {

    /**
     *
     */
    private static final long serialVersionUID = 2898280102429813235L;

    public TimebaseRateException(String s) {
        super(s);
    }

    public TimebaseRateException() {
    }

}
