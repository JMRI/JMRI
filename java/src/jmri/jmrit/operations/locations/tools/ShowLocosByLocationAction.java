package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.engines.gui.EnginesTableFrame;

/**
 * Swing action to create and register a CarsTableFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class ShowLocosByLocationAction extends AbstractAction {

    public ShowLocosByLocationAction(boolean showAllLocos, Location location, Track track) {
        super(Bundle.getMessage("MenuItemShowLocos"));
        this.showAllLocos = showAllLocos;
        if (location != null) {
            this.locationName = location.getName();
        }
        if (track != null) {
            this.trackName = track.getName();
        }

    }

    boolean showAllLocos = true;
    String locationName = null;
    String trackName = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a car table frame
        new EnginesTableFrame(showAllLocos, locationName, trackName);
    }
}
