// TimebaseRateException.java

package jmri;


/**
 * Thrown to indicate that a Timebase cant handle a particular
 * rate setting thats been requested.
 *
 * @author			Bob Jacobsen Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */
public class TimebaseRateException extends JmriException {
	public TimebaseRateException(String s) { super(s); }
	public TimebaseRateException() {}

}


/* @(#)TimebaseRateException.java */
