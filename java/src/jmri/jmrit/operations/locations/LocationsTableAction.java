// LocationsTableAction.java

package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import java.awt.Frame;

import javax.swing.AbstractAction;


/**
 * Swing action to create and register a LocationTableFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class LocationsTableAction extends AbstractAction {

    public LocationsTableAction(String s) {
    	super(s);
    }

    public LocationsTableAction() {
    	this(Bundle.getMessage("MenuLocations"));
    }

    static LocationsTableFrame f = null;
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public void actionPerformed(ActionEvent e) {
        // create a location table frame
    	if (f == null || !f.isVisible()){
    		f = new LocationsTableFrame();
     	}
    	f.setExtendedState(Frame.NORMAL);
   		f.setVisible(true);
    }
}

/* @(#)LocationsTableAction.java */
