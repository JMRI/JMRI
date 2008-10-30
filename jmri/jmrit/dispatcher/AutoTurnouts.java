// AutoTurnouts.java

package jmri.jmrit.dispatcher;

import jmri.jmrit.display.LayoutEditor;

import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Handles automatic setting of turnouts when Dispatcher allocates a section
 *    in a specific direction.
 * <p>
 * This functionality is still under development!!
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is open source software; you can redistribute it and/or modify it 
 * under the terms of version 2 of the GNU General Public License as 
 * published by the Free Software Foundation. See the "COPYING" file for 
 * a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author			Dave Duchamp    Copyright (C) 2008
 * @version			$Revision: 1.1 $
 */

public class AutoTurnouts {

	static final ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.dispatcher.DispatcherBundle");

	public AutoTurnouts (DispatcherFrame d) {
		_dispatcher = d;
	}
	
	// operational variables
	protected DispatcherFrame _dispatcher = null;
	boolean userInformed = false;
	
	protected void setTurnouts (AllocatedSection as, jmri.util.JmriJFrame frame) {
// djd debugging
// remove this message and the userInformed variable after autoTurnouts are implemented.
		if (!userInformed) {
			javax.swing.JOptionPane.showMessageDialog(frame, rb
				.getString("NoAutoTurnoutsMessage"), rb.getString("InformationTitle"),
						javax.swing.JOptionPane.INFORMATION_MESSAGE);
			userInformed = true;
		}	
	}
   
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AutoTurnouts.class.getName());
}

/* @(#)AutoTurnouts.java */
