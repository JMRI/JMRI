// PrintTrainsAction.java
package jmri.jmrit.operations.trains.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainsTableFrame;
import jmri.util.davidflanagan.HardcopyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to print a summary of each train in operations.
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2009, 2014
 * @version $Revision$
 */
public class PrintTrainsAction extends PrintTrainAction {

    static final String NEW_LINE = "\n"; // NOI18N
    static final String TAB = "\t"; // NOI18N
    static final char FORM_FEED = '\f'; // NOI18N

    TrainManager trainManager = TrainManager.instance();
    TrainsTableFrame trainsTableFrame;

    public static final int MAX_NAME_LENGTH = Control.max_len_string_train_name - 10;

    public PrintTrainsAction(String actionName, Frame mframe, boolean preview, TrainsTableFrame frame) {
        super(actionName, mframe, preview);
        trainsTableFrame = frame;
    }

    public void actionPerformed(ActionEvent e) {

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, Bundle.getMessage("TitleTrainsTable"), Control.reportFontSize, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
        
        List<Train> trains = trainsTableFrame.getSortByList();

        printSummaryTrains(writer, trains);

        try {

            writer.write(FORM_FEED); // new page
            int numberOfLines = writer.getLinesPerPage();

            // now do the details for each train
            for (Train train : trains) {
                if ((train.isBuildEnabled() || trainsTableFrame.showAllBox.isSelected()) && train.getRoute() != null) {
                    List<RouteLocation> route = train.getRoute().getLocationsBySequenceList();
                    // determine if another detailed summary can fit on the same page
                    if (numberOfLines - writer.getCurrentLineNumber() < route.size() + NUMBER_OF_HEADER_LINES) {
                        writer.write(FORM_FEED);
                    } else if (writer.getCurrentLineNumber() > 0) {
                        writer.write(NEW_LINE);
                    }
                    printTrain(writer, train);
                }
            }
        } catch (IOException e1) {
            log.error("Exception in print train details");
        }

        // and force completion of the printing
        writer.close();
    }

    protected void printSummaryTrains(HardcopyWriter writer, List<Train> trains) {
        try {
            String s = Bundle.getMessage("Name") + TAB + TAB + Bundle.getMessage("Description") + TAB
                    + Bundle.getMessage("Route") + TAB + TAB + Bundle.getMessage("Departs") + TAB + TAB
                    + Bundle.getMessage("Time") + "  " + Bundle.getMessage("Terminates") + TAB + NEW_LINE;
            writer.write(s, 0, s.length());
            for (Train train : trains) {
                if (train.isBuildEnabled() || trainsTableFrame.showAllBox.isSelected()) {
                    String name = train.getName();
                    name = truncate(name);
                    String desc = train.getDescription();
                    desc = truncate(desc);
                    String route = train.getTrainRouteName();
                    route = truncate(route);
                    String departs = train.getTrainDepartsName();
                    departs = truncate(departs);
                    String terminates = train.getTrainTerminatesName();
                    terminates = truncate(terminates);

                    s = name + " " + desc + " " + route + " " + departs + " " + train.getDepartureTime() + " "
                            + terminates + NEW_LINE;
                    writer.write(s, 0, s.length());
                }
            }

        } catch (IOException we) {
            log.error("Error printing trains summary");
        }
    }

    private String truncate(String string) {
        string = string.trim();
        if (string.length() > MAX_NAME_LENGTH) {
            string = string.substring(0, MAX_NAME_LENGTH);
        }
        // pad out the string
        StringBuffer buf = new StringBuffer(string);
        for (int j = string.length(); j < MAX_NAME_LENGTH; j++) {
            buf.append(" ");
        }
        return buf.toString();
    }

    private final static Logger log = LoggerFactory.getLogger(PrintTrainsAction.class.getName());
}
