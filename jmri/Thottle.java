/** 
 * Thottle.java
 *
 * Description:		<describe the Throttle interface here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri;


public interface Throttle {

	// speed - expressed as a value 0.0 -> 1.0
	
	// direction
	
	// functions - note that we use the naming for DCC, though that's not the implication;
	// see also DccThrottle interface
	
	// loco number - read only
	
	// disconnect when finished.  After this, further usage of
	// this throttle will result in a JmriException.
	
	public void disconnect() throws JmriException;
	
	// register for notification if any of the properties change
}


/* @(#)Thottle.java */
