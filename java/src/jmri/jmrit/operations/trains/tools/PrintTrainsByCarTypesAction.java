// PrintTrainsByCarTypesAction.java
package jmri.jmrit.operations.trains.tools;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.davidflanagan.HardcopyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to print a summary of trains that service specific car types.
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class PrintTrainsByCarTypesAction extends AbstractAction {

    static final String NEW_LINE = "\n";	// NOI18N
    static final String TAB = "\t"; // NOI18N
    TrainManager trainManager = TrainManager.instance();

    public PrintTrainsByCarTypesAction(String actionName, Frame frame, boolean preview, Component pWho) {
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
    public static final int MAX_NAME_LENGTH = 25;

    public void actionPerformed(ActionEvent e) {
        // obtain a HardcopyWriter
        try {
            writer = new HardcopyWriter(mFrame, Bundle.getMessage("TitleTrainsByType"), Control.reportFontSize, .5, .5, .5, .5,
                    isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }

        // Loop through the car types showing which locations and tracks will
        // service that car type
        String carTypes[] = CarTypes.instance().getNames();

        List<Train> trains = trainManager.getTrainsByNameList();

        try {
            // title line
            String s = Bundle.getMessage("Type") + TAB + Bundle.getMessage("Trains")
                    + TAB + TAB + TAB + Bundle.getMessage("Description") + NEW_LINE;
            writer.write(s);
            // car types
            for (String type : carTypes) {
                s = type + NEW_LINE;
                writer.write(s);
                // trains
                for (Train train : trains) {
                    if (train.acceptsTypeName(type)) {
                        StringBuilder sb = new StringBuilder();
                        String name = train.getName();
                        sb.append(TAB + name + " ");
                        int j = MAX_NAME_LENGTH - name.length();
                        while (j > 0) {
                            j--;
                            sb.append(" ");
                        }
                        sb.append(train.getDescription() + NEW_LINE);
                        writer.write(sb.toString());
                    }
                }
            }
            // and force completion of the printing
            writer.close();
        } catch (IOException we) {
            log.error("Error printing PrintLocationAction: " + we);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PrintTrainsByCarTypesAction.class.getName());
}
