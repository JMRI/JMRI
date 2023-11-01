package jmri.jmrit.operations.locations.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.locations.Location;

/**
 * Swing action to create and register a LocationCopyFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2014
 */
public class LocationCopyAction extends AbstractAction {

    public LocationCopyAction(Location location) {
        super(Bundle.getMessage("MenuItemCopyLocation"));
        _location = location;
    }

    LocationCopyFrame f = null;
    Location _location;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a copy track frame
        if (f == null || !f.isVisible()) {
            f = new LocationCopyFrame(_location);
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}


