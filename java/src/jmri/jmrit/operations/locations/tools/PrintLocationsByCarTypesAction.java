package jmri.jmrit.operations.locations.tools;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractAction;
import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.util.davidflanagan.HardcopyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to print a summary of locations and tracks that service specific car
 * types.
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2010
 */
public class PrintLocationsByCarTypesAction extends AbstractAction {

    static final String NEW_LINE = "\n"; // NOI18N
    static final String TAB = "\t"; // NOI18N

    LocationManager locManager = InstanceManager.getDefault(LocationManager.class);

    public PrintLocationsByCarTypesAction(String actionName, Frame frame, boolean preview,
            Component pWho) {
        super(actionName);
        mFrame = frame;
        isPreview = preview;
    }

    /**
     * Frame hosting the printing
     */
    Frame mFrame;
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    HardcopyWriter writer;

    @Override
    public void actionPerformed(ActionEvent e) {
        // obtain a HardcopyWriter
        try {
            writer = new HardcopyWriter(mFrame, Bundle.getMessage("TitleLocationsByType"), Control.reportFontSize, .5,
                    .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }

        // Loop through the car types showing which locations and tracks will
        // service that car type
        String carTypes[] = InstanceManager.getDefault(CarTypes.class).getNames();

        List<Location> locations = locManager.getLocationsByNameList();

        try {
            // title line
            String s = Bundle.getMessage("Type") + TAB + Bundle.getMessage("Location") + TAB
                    + Bundle.getMessage("Track") + NEW_LINE;
            writer.write(s);
            // car types
            for (String type : carTypes) {
                s = type + NEW_LINE;
                writer.write(s);
                // locations
                for (Location location : locations) {
                    if (location.acceptsTypeName(type)) {
                        s = TAB + location.getName() + NEW_LINE;
                        writer.write(s);
                        // tracks
                        List<Track> tracks = location.getTrackByNameList(null);
                        for (Track track : tracks) {
                            if (track.acceptsTypeName(type)) {
                                s = TAB + TAB + TAB + track.getName() + NEW_LINE;
                                writer.write(s);
                            }
                        }
                    }
                }
            }
            // and force completion of the printing
            writer.close();
        } catch (IOException we) {
            log.error("Error printing PrintLocationAction: " + we);
        }
    }

    private final static Logger log = LoggerFactory
            .getLogger(PrintLocationsByCarTypesAction.class);
}
