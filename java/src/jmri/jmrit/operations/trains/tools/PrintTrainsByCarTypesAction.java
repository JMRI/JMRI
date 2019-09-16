package jmri.jmrit.operations.trains.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractAction;
import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.davidflanagan.HardcopyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to print a summary of trains that service specific car types.
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2010
 */
public class PrintTrainsByCarTypesAction extends AbstractAction {

    static final String NEW_LINE = "\n"; // NOI18N
    static final String TAB = "\t"; // NOI18N

    public PrintTrainsByCarTypesAction(String actionName, boolean preview) {
        super(actionName);
        isPreview = preview;
    }

    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    HardcopyWriter writer;
    int max_name_length = Control.max_len_string_train_name + 1;

    @Override
    public void actionPerformed(ActionEvent e) {
        // obtain a HardcopyWriter
        try {
            writer = new HardcopyWriter(new Frame(), Bundle.getMessage("TitleTrainsByType"), Control.reportFontSize, .5, .5, .5, .5,
                    isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }

        // Loop through the car types showing which trains will service that car type
        String carTypes[] = InstanceManager.getDefault(CarTypes.class).getNames();
        List<Train> trains = InstanceManager.getDefault(TrainManager.class).getTrainsByNameList();

        try {
            // title line
            String s = Bundle.getMessage("Type") + TAB + TrainCommon.padString(Bundle.getMessage("Trains"),
                    max_name_length) + Bundle.getMessage("Description") + NEW_LINE;
            writer.write(s);
            
            for (String type : carTypes) {
                s = type + NEW_LINE;
                writer.write(s);
                
                for (Train train : trains) {
                    if (train.acceptsTypeName(type)) {
                        s = TAB + TrainCommon.padString(train.getName(), max_name_length) + train.getDescription() + NEW_LINE;
                        writer.write(s);
                    }
                }
            }
            // and force completion of the printing
            writer.close();
        } catch (IOException we) {
            log.error("Error printing PrintLocationAction: " + we);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PrintTrainsByCarTypesAction.class);
}
