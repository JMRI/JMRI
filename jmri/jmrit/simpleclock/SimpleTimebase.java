// Timebase.java

package jmri.jmrit.simpleclock;

import java.util.Date;
import jmri.Timebase;

/**
 * Provide basic Timebase implementation from system clock.
 * <P>
 * The setTimeValue member is the fast time when the clock
 * started.  The startAtTime member is the wall-clock time
 * when the clock was started.  Together, those can be used
 * to calculate the current fast time.
 * <P>
 * The pauseTime member is used to indicate that the
 * timebase was paused. If non-null, it indicates the
 * current fast time when the clock was paused.
 *
 * @author			Bob Jacobsen Copyright (C) 2004
 * @version			$Revision: 1.3 $
 */
public class SimpleTimebase implements Timebase {

	public SimpleTimebase() {
		// set to start counting from now
		setTime(new Date());
		pauseTime = null;
	}
	
    // methods for getting the current time
    public Date getTime() {
    	// is clock stopped?
    	if (pauseTime!=null) return new Date(pauseTime.getTime()); // to ensure not modified outside
    	// clock running
    	long elapsedMSec = (new Date()).getTime() - startAtTime.getTime();
    	long nowMSec = setTimeValue.getTime()+(long)(mFactor*(double)elapsedMSec);
    	return new Date(nowMSec);
    }
    	
    public void setTime(Date d) {
    	startAtTime = new Date();	// set now
    	setTimeValue = new Date(d.getTime());   // to ensure not modified from outside
    	if (pauseTime != null)
    		pauseTime = setTimeValue;  // if stopped, continue stopped at new time
    }

    public void setRun(boolean run) {
    	if (run && pauseTime!=null) {
    		// starting of stopped clock
    		setTime(pauseTime);
			pauseTime = null;
    		
    	} else if (!run && pauseTime == null) {
    		// stopping of running clock:
    		// Store time it was stopped, and stop it
    		pauseTime = getTime();
    	}
    	firePropertyChange("run", new Boolean(run), new Boolean(!run));
    }
    
    public boolean getRun() { return pauseTime == null; }

    public void setRate(double factor) {
    	double oldFactor = mFactor;
    	mFactor = factor;
    	firePropertyChange("rate", new Double(factor), new Double(oldFactor));
    }
    public double getRate() { return mFactor; }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    protected void firePropertyChange(String p, Object old, Object n) { 
    	pcs.firePropertyChange(p,old,n);
    }

    /**
     * Request a call-back when the bound Rate or Run property changes.
     * <P>
     * Not yet implemented.
     */
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    /**
     * Remove a request for a call-back when a bound property changes.
     * <P>
     * Not yet implemented.
     */
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * Remove references to and from this object, so that it can
     * eventually be garbage-collected.
     * 
     */
    public void dispose() {}

	double mFactor = 1.0;
	Date startAtTime;
	Date setTimeValue;
	Date pauseTime;   // null value indicates clock is running
	
}

/* @(#)Timebase.java */
