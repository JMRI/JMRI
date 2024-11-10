package jmri.jmrit.operations.locations.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.util.davidflanagan.HardcopyWriter;

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

    public PrintLocationsByCarTypesAction(boolean preview) {
        super(preview ? Bundle.getMessage("MenuItemPreviewByType") : Bundle.getMessage("MenuItemPrintByType"));
        isPreview = preview;
    }

    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;

    @Override
    public void actionPerformed(ActionEvent e) {
        print();
    }

    private void print() {
        // obtain a HardcopyWriter
        try ( HardcopyWriter writer = new HardcopyWriter(new Frame(), Bundle.getMessage("TitleLocationsByType"),
            Control.reportFontSize, .5, .5, .5, .5, isPreview); ) {
            
            // Loop through the car types showing which locations and tracks will
            // service that car type
            String carTypes[] = InstanceManager.getDefault(CarTypes.class).getNames();
            
            List<Location> locations = InstanceManager.getDefault(LocationManager.class).getLocationsByNameList();

            // title line
            String s = Bundle.getMessage(
                    "Type") + TAB + Bundle.getMessage("Location") + TAB + Bundle.getMessage("Track") + NEW_LINE;
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
                        List<Track> tracks = location.getTracksByNameList(null);
                        for (Track track : tracks) {
                            if (track.isTypeNameAccepted(type)) {
                                s = TAB + TAB + TAB + track.getName() + NEW_LINE;
                                writer.write(s);
                            }
                        }
                    }
                }
            }
            // and force completion of the printing
//            writer.close(); not needed when using try / catch
        } catch (HardcopyWriter.PrintCanceledException we) {
            log.debug("Print cancelled");
        } catch (IOException we) {
            log.error("Error printing PrintLocationAction: {}", we.getLocalizedMessage());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PrintLocationsByCarTypesAction.class);
}
