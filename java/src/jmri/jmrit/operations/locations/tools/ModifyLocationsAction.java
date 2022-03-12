package jmri.jmrit.operations.locations.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.locations.Location;

/**
 * Swing action to create and register a LocationsByCarTypeFrame object.
 *
 * @author Daniel Boudreau Copyright (C) 2009
 */
public class ModifyLocationsAction extends AbstractAction {

    public ModifyLocationsAction(Location location) {
        super(Bundle.getMessage("TitleModifyLocations"));
        _location = location;
    }

    public ModifyLocationsAction() {
        super(Bundle.getMessage("TitleModifyLocations"));
    }

    Location _location;
    LocationsByCarTypeFrame f;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a frame
        if (f == null || !f.isVisible()) {
            f = new LocationsByCarTypeFrame();
            f.initComponents(_location);
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}


