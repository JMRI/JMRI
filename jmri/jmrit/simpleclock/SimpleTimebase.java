// Timebase.java

package jmri.jmrit.simpleclock;

import java.util.Date;
import jmri.Timebase;

/**
 * Provide basic Timebase implementation from system clock.
 * <P>
 * This clock cannot be stopped, so setRun doesnt do anything.
 *
 * @author			Bob Jacobsen Copyright (C) 2004
 * @version			$Revision: 1.2 $
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
    }
    
    public boolean getRun() { return pauseTime == null; }

    public void setRate(double factor) {
    	mFactor = factor;
    }
    public double getRate() { return mFactor; }

    /**
     * Request a call-back when the bound Rate or Run property changes.
     * <P>
     * Not yet implemented.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {}

    /**
     * Remove a request for a call-back when a bound property changes.
     * <P>
     * Not yet implemented.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {}

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
