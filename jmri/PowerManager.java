/** 
 * PowerManager.java
 *
 * Description:		Interface for controlling layout power
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 *
 *  The PowerManager handles two states:
 *
 *  On/Off           which controls electrical power to the track
 *  Started/Stopped  which controls the sending of packets to the layout
 *
 *  A layout may not have control over these, in which case attempts
 *  to change them should return an exception.  If the state cannot
 *  be sensed, that should also return an exception.
 */

package jmri;


public interface PowerManager {

	public void 	setPowerOff() 	throws JmriException;
	public void 	setPowerOn()  	throws JmriException;
	public boolean 	isPowerOn()  	throws JmriException;
	
	public void		setTrackStopped()  throws JmriException;
	public void		setTrackStarted()  throws JmriException;
	public boolean	isTrackStarted()   throws JmriException;

	// to free resources when no longer used
	public void dispose() throws JmriException;

}


/* @(#)PowerManager.java */
