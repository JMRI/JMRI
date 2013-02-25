// PrintTrainsByCarTypesAction.java

package jmri.jmrit.operations.trains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;

import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import java.util.List;


/**
 * Action to print a summary of trains that service specific
 * car types.
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @author  Dennis Miller  Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2010
 * @version     $Revision$
 */
public class PrintTrainsByCarTypesAction  extends AbstractAction {

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
			writer = new HardcopyWriter(mFrame, Bundle.getMessage("TitleTrainsByType"), 10, .5, .5, .5, .5,
					isPreview);
		} catch (HardcopyWriter.PrintCanceledException ex) {
			log.debug("Print cancelled");
			return;
		}

		// Loop through the car types showing which locations and tracks will
		// service that car type
		String carTypes[] = CarTypes.instance().getNames();

		List<String> trains = trainManager.getTrainsByNameList();
		
		try {
			// title line
			String s = Bundle.getMessage("Type") + TAB + Bundle.getMessage("Trains")
					+ TAB + TAB +TAB + Bundle.getMessage("Description") + NEW_LINE;
			writer.write(s);
			// car types
			for (int t = 0; t < carTypes.length; t++) {
				s = carTypes[t] + NEW_LINE;
				writer.write(s);
				// trains
				for (int i = 0; i < trains.size(); i++) {
					Train train = trainManager.getTrainById(trains.get(i));
					if (train.acceptsTypeName(carTypes[t])) {
						StringBuilder sb = new StringBuilder();
						String name = train.getName();
						sb.append(TAB + name + " ");
						int j = MAX_NAME_LENGTH - name.length();
						while (j>0){
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
 
    static Logger log = LoggerFactory.getLogger(PrintTrainsByCarTypesAction.class.getName());
}
