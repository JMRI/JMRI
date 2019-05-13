package jmri.jmrit.operations.trains.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.MessageFormat;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainEditFrame;
import jmri.util.davidflanagan.HardcopyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to print a summary of a train
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2009
 */
public class PrintTrainAction extends AbstractAction {

    static final String NEW_LINE = "\n"; // NOI18N
    static final String TAB = "\t"; // NOI18N

    public PrintTrainAction(String actionName, boolean isPreview, TrainEditFrame frame) {
        super(actionName);
        this.isPreview = isPreview;
        this.trainEditFrame = frame;
    }
    
    public PrintTrainAction(String actionName, boolean isPreview) {
        super(actionName);
        this.isPreview = isPreview;
    }

    TrainEditFrame trainEditFrame;
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;

    @Override
    public void actionPerformed(ActionEvent e) {
        Train train = trainEditFrame._train;
        if (train == null) {
            return;
        }

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(new Frame(), MessageFormat.format(Bundle.getMessage("TitleTrain"),
                    new Object[]{train.getName()}), Control.reportFontSize, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }

        printTrain(writer, train);

        // and force completion of the printing
        writer.close();
    }

    // 7 lines of header and another 3 lines for possible comments
    protected static final int NUMBER_OF_HEADER_LINES = 10;

    protected void printTrain(HardcopyWriter writer, Train train) {
        try {
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
            s = NEW_LINE;
            writer.write(s);
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

        } catch (IOException we) {
            log.error("Error printing train report");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PrintTrainAction.class);
}
