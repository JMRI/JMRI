package jmri.jmrit.operations.trains.tools;

import java.awt.Frame;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.*;
import jmri.util.davidflanagan.HardcopyWriter;

/**
 * Prints a summary of a train or trains. The trains list is controlled by the
 * "Show All" checkbox and the "Build" checkboxes in the TrainsTableFrame.
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2009, 2023
 */
public class PrintTrains {

    static final String NEW_LINE = "\n"; // NOI18N
    static final String TAB = "\t"; // NOI18N
    static final char FORM_FEED = '\f'; // NOI18N

    public static final int MAX_NAME_LENGTH = Control.max_len_string_train_name - 10;

    public PrintTrains(boolean isPreview, Train train) {
        printTrain(isPreview, train);
    }

    public PrintTrains(boolean isPreview, TrainsTableFrame trainsTableFrame) {
        printTrains(isPreview, trainsTableFrame);
    }

    private void printTrains(boolean isPreview, TrainsTableFrame trainsTableFrame) {
        // obtain a HardcopyWriter to do this
        try (HardcopyWriter writer = new HardcopyWriter(new Frame(), Bundle.getMessage("TitleTrainsTable"),
                Control.reportFontSize, .5, .5, .5, .5, isPreview)) {

            List<Train> trains = trainsTableFrame.getSortByList();
            printSummaryTrains(writer, trains, trainsTableFrame);
            writer.write(FORM_FEED); // new page

            // now do the details for each train
            for (Train train : trains) {
                if ((train.isBuildEnabled() || trainsTableFrame.showAllBox.isSelected()) && train.getRoute() != null) {
                    List<RouteLocation> route = train.getRoute().getLocationsBySequenceList();
                    // determine if another detailed summary can fit on the same page
                    if (writer.getLinesPerPage() - writer.getCurrentLineNumber() < route.size() +
                            getNumberOfLines(train.getComment()) +
                            NUMBER_OF_HEADER_LINES) {
                        writer.write(FORM_FEED);
                    } else if (writer.getCurrentLineNumber() > 0) {
                        writer.write(NEW_LINE);
                    }
                    printTrain(writer, train);
                }
            }
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
        } catch (IOException e1) {
            log.error("Exception in print train details");
        }
    }

    private void printSummaryTrains(HardcopyWriter writer, List<Train> trains, TrainsTableFrame trainsTableFrame)
            throws IOException {
        String s = Bundle.getMessage("Name") +
                TAB +
                TAB +
                Bundle.getMessage("Description") +
                TAB +
                Bundle.getMessage("Route") +
                TAB +
                TAB +
                Bundle.getMessage("Departs") +
                TAB +
                TAB +
                Bundle.getMessage("Time") +
                "  " +
                Bundle.getMessage("Terminates") +
                TAB +
                NEW_LINE;
        writer.write(s);
        for (Train train : trains) {
            if (train.isBuildEnabled() || trainsTableFrame.showAllBox.isSelected()) {
                String name = truncate(train.getName());
                String desc = truncate(train.getDescription());
                String route = truncate(train.getTrainRouteName());
                String departs = truncate(train.getTrainDepartsName());
                String terminates = truncate(train.getTrainTerminatesName());

                s = name +
                        " " +
                        desc +
                        " " +
                        route +
                        " " +
                        departs +
                        " " +
                        train.getDepartureTime() +
                        " " +
                        terminates +
                        NEW_LINE;
                writer.write(s);
            }
        }
    }

    private String truncate(String string) {
        return TrainCommon.padAndTruncate(string, MAX_NAME_LENGTH);
    }

    private void printTrain(boolean isPreview, Train train) {
        if (train == null) {
            return;
        }
        // obtain a HardcopyWriter to do this
        try (HardcopyWriter writer = new HardcopyWriter(new Frame(), Bundle.getMessage("TitleTrain", train.getName()),
                Control.reportFontSize, .5, .5, .5, .5, isPreview)) {

            printTrain(writer, train);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
        } catch (IOException ex) {
            log.error("Exception in print train");
        }
    }

    // 7 lines of header plus NEW_LINE at start
    private static final int NUMBER_OF_HEADER_LINES = 8;

    private void printTrain(HardcopyWriter writer, Train train) throws IOException {
        String s = Bundle.getMessage("Name") + ": " + train.getName() + NEW_LINE;
        writer.write(s);
        s = Bundle.getMessage("Description") + ": " + train.getDescription() + NEW_LINE;
        writer.write(s);
        s = Bundle.getMessage("Departs") + ": " + train.getTrainDepartsName() + NEW_LINE;
        writer.write(s);
        s = Bundle.getMessage("DepartTime") + ": " + train.getDepartureTime() + NEW_LINE;
        writer.write(s);
        s = Bundle.getMessage("Terminates") + ": " + train.getTrainTerminatesName() + NEW_LINE;
        writer.write(s);

        writer.write(NEW_LINE);

        s = Bundle.getMessage("Route") + ": " + train.getTrainRouteName() + NEW_LINE;
        writer.write(s);
        Route route = train.getRoute();
        if (route != null) {
            for (RouteLocation rl : route.getLocationsBySequenceList()) {
                s = TAB + rl.getName() + NEW_LINE;
                writer.write(s);
            }
        }
        if (!train.getComment().equals(Train.NONE)) {
            s = Bundle.getMessage("Comment") + ": " + train.getComment() + NEW_LINE;
            writer.write(s);
        }
    }

    private int getNumberOfLines(String string) {
        String[] lines = string.split(NEW_LINE);
        return lines.length;
    }

    private final static Logger log = LoggerFactory.getLogger(PrintTrains.class);
}
