// Timebase.java

package jmri;

import java.util.Date;

/**
 * Provide access to clock capabilities in hardware or software.
 * <P>
 * The Rate parameter determines how much faster than real time
 * this timebase runs.  E.g. a value of 2.0 means that the value
 * returned by getTime will advance an hour for every half-hour of
 * wall-clock time elapsed.
 * <P>
 * The Rate and Run parameters are bound, so you can listen for them
 * changing.  The Time parameters is not bound, because it changes
 * continuously.  Ask for its value when needed, or add a 
 * a listener for the changes in the "minute" value using {@link #addMinuteChangeListener}
 *
 * @author			Bob Jacobsen Copyright (C) 2004
 * @version			$Revision: 1.2 $
 */
public interface Timebase {

    // methods for getting the current time
    public Date getTime();
    public void setTime(Date d);

    public void setRun(boolean y);
    public boolean getRun();

    public void setRate(double factor) throws TimebaseRateException;
    public double getRate();

    /**
     * Request a call-back when the bound Rate or Run property changes.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l);

    /**
     * Remove a request for a call-back when a bound property changes.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l);

    /**
     * Request a call-back when the minutes place of the time changes.
     */
    public void addMinuteChangeListener(java.beans.PropertyChangeListener l);

    /**
     * Remove a request for call-back when the minutes place of the time changes.
     */
    public void removeMinuteChangeListener(java.beans.PropertyChangeListener l);

    /**
     * Remove references to and from this object, so that it can
     * eventually be garbage-collected.
     */
    public void dispose();  // remove _all_ connections!


}

/* @(#)Timebase.java */
