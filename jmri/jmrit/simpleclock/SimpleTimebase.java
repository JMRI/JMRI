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
 * @version			$Revision: 1.1 $
 */
public class SimpleTimebase implements Timebase {

	public SimpleTimebase() {
		// set to start counting from now
		setTime(new Date());
	}
	
    // methods for getting the current time
    public Date getTime() {
    	long elapsedMSec = (new Date()).getTime() - startAtTime.getTime();
    	long nowMSec = startAtTime.getTime()+(long)(mFactor*(double)elapsedMSec);
    	return new Date(nowMSec);
    }
    	
    public void setTime(Date d) {
    	startAtTime = new Date();	// set now
    	setTimeValue = d;
    }

    public void setRun(boolean y) {}
    public boolean getRun() { return true; }

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
	
}

/* @(#)Timebase.java */
