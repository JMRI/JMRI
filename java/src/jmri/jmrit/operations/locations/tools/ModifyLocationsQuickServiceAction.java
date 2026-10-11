package jmri.jmrit.operations.locations.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.locations.Location;

/**
 * Action to create the modify locations by quick service.
 *
 * @author Daniel Boudreau Copyright (C) 2026
 */
public class ModifyLocationsQuickServiceAction extends AbstractAction {

    public ModifyLocationsQuickServiceAction(Location location) {
        super(Bundle.getMessage("TitleModifyLocQuickService"));
        _location = location;
    }

    public ModifyLocationsQuickServiceAction() {
        super(Bundle.getMessage("TitleModifyLocsQuickService"));
    }

    Location _location;
    LocationsByQuickServiceFrame f;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a frame
        if (f == null || !f.isVisible()) {
            f = new LocationsByQuickServiceFrame();
            f.initComponents(_location);
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}


