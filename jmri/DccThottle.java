/** 
 * DccThottle.java
 *
 * Description:		<describe the DccThrottle interface here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri;


public interface DccThrottle extends Throttle {

	// extends Throttle to allow asking about DCC items
	
	public int dccAddress();
	
	public int speedSteps();
	
	// information on consisting  (how do we set consisting?)
	
	// register for notification
	
	
}


/* @(#)DccThottle.java */
