/** 
 * DccThottle.java
 *
 * Description:		<describe the DccThrottle interface here>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Id: DccThrottle.java,v 1.2 2002-02-04 07:36:36 jacobsen Exp $
 */

package jmri;


public interface DccThrottle extends Throttle {

	// extends Throttle to allow asking about DCC items
	
	public int dccAddress();
	
	// to handle quantized speed. Note this can change! Valued returned is
	// always positive.
	public float speedIncrement();
	
	// information on consisting  (how do we set consisting?)
	
	// register for notification
	
	
}


/* @(#)DccThottle.java */
