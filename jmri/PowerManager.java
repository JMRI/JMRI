// PowerManager.java

package jmri;

import java.beans.PropertyChangeListener;

 /**
 * Provide controls for layout power.
 *<P>
 *  The PowerManager handles two states:
 *<UL>
 *<LI>On/Off           which controls electrical power to the track
 *<LI>Started/Stopped  which controls the sending of packets to the layout
 *</UL>
 *  A layout may not have control over these, in which case attempts
 *  to change them should return an exception.  If the state cannot
 *  be sensed, that should also return an exception.
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.6 $
 */
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
