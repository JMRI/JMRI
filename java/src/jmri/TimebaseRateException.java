package jmri;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Thrown to indicate that a Timebase can't handle a particular rate setting
 * that's been requested.
 *
 * @author Bob Jacobsen Copyright (C) 2004
 */
@API(status = STABLE)
public class TimebaseRateException extends JmriException {

    public TimebaseRateException(String s) {
        super(s);
    }

    public TimebaseRateException() {
    }

}
