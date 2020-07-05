package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarsTableFrame;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action to create and register a CarsTableFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2009
 */
@API(status = MAINTAINED)
public class ShowCarsByLocationAction extends AbstractAction {

    public ShowCarsByLocationAction(boolean showAllCars, Location location, Track track) {
        super(Bundle.getMessage("MenuItemShowCars"));
        this.showAllCars = showAllCars;
        if (location != null) {
            this.locationName = location.getName();
        }
        if (track != null) {
            this.trackName = track.getName();
        }

    }

    boolean showAllCars = true;
    String locationName = null;
    String trackName = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a car table frame
        new CarsTableFrame(showAllCars, locationName, trackName);
    }
}
