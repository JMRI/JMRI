//ShowCarsByLocation.java
package jmri.jmrit.operations.locations;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.rollingstock.cars.CarsTableFrame;

/**
 * Swing action to create and register a CarsTableFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2009
 * @version $Revision$
 */
public class ShowCarsByLocationAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -3383744212033518858L;

    public ShowCarsByLocationAction(String s) {
        super(s);
    }

    public ShowCarsByLocationAction(boolean showAllCars, String locationName, String trackName) {
        this(Bundle.getMessage("MenuItemShowCars"));
        this.showAllCars = showAllCars;
        this.locationName = locationName;
        this.trackName = trackName;
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

/* @(#)ShowCarsByLocation.java */
