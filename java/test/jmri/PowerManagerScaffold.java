// PowerManagerScaffold.java

package jmri;

import java.beans.PropertyChangeListener;

 /**
 * Dummy implementation of PowerManager for testing purposes.
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
 * @author			Bob Jacobsen Copyright (C) 2008
 * @version			$Revision$
 */
public class PowerManagerScaffold implements PowerManager {

	public void 	setPower(int v) {}
	public int	 	getPower()  { return 0; }
	public String   getUserName() { return "none"; }

	public void dispose() {}

	public void addPropertyChangeListener(PropertyChangeListener p) {}
	public void removePropertyChangeListener(PropertyChangeListener p) {}

}


/* @(#)PowerManagerScaffold.java */
