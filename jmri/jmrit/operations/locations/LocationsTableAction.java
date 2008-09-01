// LocationsTableAction.java

package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register a
 * PowerPanelFrame object.
 *
 * @author	    Bob Jacobsen    Copyright (C) 2001
 * @author 	Daniel Boudreau Copyright (C) 2008
 * @version         $Revision: 1.1 $
 */
public class LocationsTableAction extends AbstractAction {
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");

    public LocationsTableAction(String s) {
	super(s);
    }

    public LocationsTableAction() {
        this(rb.getString("TitleLocationsTable"));
    }

    LocationsTableFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a table frame
    	if (f == null || !f.isVisible()){
    		f = new LocationsTableFrame();
    		f.setVisible(true);
    	}
    	f.setExtendedState(f.NORMAL);
    }
}

/* @(#)LocationsTableAction.java */
