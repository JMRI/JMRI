// ModifyLocationsAction.java

package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import java.awt.Frame;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a LocationsByCarTypeFrame object.
 * 
 * @author Daniel Boudreau Copyright (C) 2009
 * @version $Revision$
 */
public class ModifyLocationsAction extends AbstractAction {

	public ModifyLocationsAction(String s, Location location) {
		super(s);
		l = location;
	}

	public ModifyLocationsAction(String s) {
		super(s);
	}

	public ModifyLocationsAction() {
		super(Bundle.getMessage("TitleModifyLocations"));
	}

	Location l;

	LocationsByCarTypeFrame f = null;

	public void actionPerformed(ActionEvent e) {
		// create a frame
		if (f == null || !f.isVisible()) {
			f = new LocationsByCarTypeFrame();
			f.initComponents(l);
		}
		f.setExtendedState(Frame.NORMAL);
		f.setVisible(true);
	}
}

/* @(#)ModifyLocationsAction.java */
