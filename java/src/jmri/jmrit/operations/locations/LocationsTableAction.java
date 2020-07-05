package jmri.jmrit.operations.locations;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action to create and register a LocationTableFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 */
@API(status = MAINTAINED)
public class LocationsTableAction extends AbstractAction {

    public LocationsTableAction() {
        super(Bundle.getMessage("MenuLocations")); // NOI18N
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


