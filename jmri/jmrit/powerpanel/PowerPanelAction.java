/**
 * PowerPanelAction.java
 *
 * Description:		Swing action to create and register a
 *       			PowerPanel object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version
 */

package jmri.jmrit.powerpanel;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class PowerPanelAction 			extends AbstractAction {

	public PowerPanelAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {

		// create a PowerPanelFrame
		PowerPanelFrame f = new PowerPanelFrame();
		f.show();

	}
}


/* @(#)PowerPanelAction.java */
