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

import java.beans.PropertyChangeListener;

public interface PowerManager {

	static final int UNKNOWN = 0;
	static final int ON      = 2;
	static final int OFF     = 4;
	
	public void 	setPower(int v) 	throws JmriException;
	public int	 	getPower()  	throws JmriException;
	
	// to free resources when no longer used
	public void dispose() throws JmriException;

	// to hear of changes
	public void addPropertyChangeListener(PropertyChangeListener p);
	public void removePropertyChangeListener(PropertyChangeListener p);

}


/* @(#)PowerManager.java */
