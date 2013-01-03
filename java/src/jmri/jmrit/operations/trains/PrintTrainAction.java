// PrintTrainAction.java

package jmri.jmrit.operations.trains;

import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.*;

import java.util.List;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;

/**
 * Action to print a summary of a train
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in Macintosh MRJ
 * 
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2009
 * @version $Revision$
 */
public class PrintTrainAction extends AbstractAction {

	String newLine = "\n";
	public static final int MAX_NAME_LENGTH = 15;

	public PrintTrainAction(String actionName, Frame mFrame, boolean isPreview, Frame frame) {
		super(actionName);
		this.mFrame = mFrame;
		this.isPreview = isPreview;
		this.frame = frame;
	}

	/**
	 * Frame hosting the printing
	 */
	Frame mFrame;
	Frame frame; // TrainEditFrame
	/**
	 * Variable to set whether this is to be printed or previewed
	 */
	boolean isPreview;

	public void actionPerformed(ActionEvent e) {
		TrainEditFrame f = (TrainEditFrame) frame;
		Train train = f._train;
		if (train == null)
			return;

		// obtain a HardcopyWriter to do this
		HardcopyWriter writer = null;
		try {
			writer = new HardcopyWriter(mFrame, MessageFormat.format(Bundle.getMessage("TitleTrain"),
					new Object[] { train.getName() }), 10, .5, .5, .5, .5, isPreview);
		} catch (HardcopyWriter.PrintCanceledException ex) {
			log.debug("Print cancelled");
			return;
		}

		try {
			String s = Bundle.getMessage("Name") + ": " + train.getName() + newLine;
			writer.write(s, 0, s.length());
			s = Bundle.getMessage("Description") + ": " + train.getDescription() + newLine;
			writer.write(s, 0, s.length());
			s = Bundle.getMessage("Departs") + ": " + train.getTrainDepartsName() + newLine;
			writer.write(s, 0, s.length());
			s = Bundle.getMessage("DepartTime") + ": " + train.getDepartureTime() + newLine;
			writer.write(s, 0, s.length());
			s = Bundle.getMessage("Terminates") + ": " + train.getTrainTerminatesName() + newLine;
			writer.write(s, 0, s.length());
			s = newLine;
			writer.write(s, 0, s.length());
			s = Bundle.getMessage("Route") + ": " + train.getTrainRouteName() + newLine;
			writer.write(s, 0, s.length());
			Route route = train.getRoute();
			if (route != null) {
				List<String> locations = route.getLocationsBySequenceList();
				for (int i = 0; i < locations.size(); i++) {
					RouteLocation rl = route.getLocationById(locations.get(i));
					s = "\t" + rl.getName() + newLine;
					writer.write(s, 0, s.length());
				}
			}
			if (!train.getComment().equals("")) {
				s = Bundle.getMessage("Comment") + ": " + train.getComment() + newLine;
				writer.write(s);
			}

			// and force completion of the printing
			writer.close();
		} catch (IOException we) {
			log.error("Error printing ConsistRosterEntry: " + e);
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PrintTrainAction.class
			.getName());
}
