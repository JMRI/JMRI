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
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision$
 */
public interface PowerManager {

	static final int UNKNOWN = NamedBean.UNKNOWN;
	static final int ON      = 0x02;
	static final int OFF     = 0x04;

        static final String POWER = "Power"; // NOI18N
        
	public void 	setPower(int v) 	throws JmriException;
	public int	 	getPower()  	throws JmriException;

	// to free resources when no longer used
	public void dispose() throws JmriException;

	// to hear of changes
	public void addPropertyChangeListener(PropertyChangeListener p);
	public void removePropertyChangeListener(PropertyChangeListener p);

    public String getUserName();
}


/* @(#)PowerManager.java */
