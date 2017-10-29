package jmri.jmrit.operations.locations;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a LocationTableFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 */
public class LocationsTableAction extends AbstractAction {

    public LocationsTableAction(String s) {
        super(s);
    }

    public LocationsTableAction() {
        this(Bundle.getMessage("MenuLocations")); // NOI18N
    }

    private static LocationsTableFrame locationTableFrame = null;

    @Override
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "Show only one LocationsTableFrame")
    public void actionPerformed(ActionEvent e) {
        // create a location table frame
        if (locationTableFrame == null || !locationTableFrame.isVisible()) {
            locationTableFrame = new LocationsTableFrame();
        }
        locationTableFrame.setExtendedState(Frame.NORMAL);
        locationTableFrame.setVisible(true); // this also brings the frame into focus
    }
}


