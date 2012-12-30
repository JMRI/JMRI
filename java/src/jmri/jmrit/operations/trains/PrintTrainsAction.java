// PrintTrainsAction.java

package jmri.jmrit.operations.trains;

import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import java.util.List;

/**
 * Action to print a summary of the Roster contents
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in Macintosh MRJ
 * 
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2009
 * @version $Revision$
 */
public class PrintTrainsAction extends AbstractAction {

	String newLine = "\n";
	TrainManager manager = TrainManager.instance();
	public static final int MAX_NAME_LENGTH = 15;

	public PrintTrainsAction(String actionName, Frame frame, boolean preview, Component pWho) {
		super(actionName);
		mFrame = frame;
		isPreview = preview;
		panel = (TrainsTableFrame) pWho;
	}

	/**
	 * Frame hosting the printing
	 */
	Frame mFrame;
	/**
	 * Variable to set whether this is to be printed or previewed
	 */
	boolean isPreview;
	TrainsTableFrame panel;

	public void actionPerformed(ActionEvent e) {

		// obtain a HardcopyWriter to do this
		HardcopyWriter writer = null;
		try {
			writer = new HardcopyWriter(mFrame, Bundle.getString("TitleTrainsTable"), 10, .5, .5, .5, .5,
					isPreview);
		} catch (HardcopyWriter.PrintCanceledException ex) {
			log.debug("Print cancelled");
			return;
		}

		// Loop through the Roster, printing as needed

		List<String> trains = panel.getSortByList();
		String tab = "\t";

		try {
			String s = Bundle.getString("Name") + tab + tab + Bundle.getString("Description") + tab
					+ Bundle.getString("Route") + tab + tab + Bundle.getString("Departs") + tab + tab
					+ Bundle.getString("Time") + "  " + Bundle.getString("Terminates") + tab + newLine;
			writer.write(s, 0, s.length());
			for (int i = 0; i < trains.size(); i++) {
				Train train = manager.getTrainById(trains.get(i));
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

				s = name + " " + desc + " " + route + " " + departs + " "
						+ train.getDepartureTime() + " " + terminates + newLine;
				writer.write(s, 0, s.length());
			}

			// and force completion of the printing
			writer.close();
		} catch (IOException we) {
			log.error("Error printing ConsistRosterEntry: " + e);
		}
	}

	private String truncate(String string) {
		string = string.trim();
		if (string.length() > MAX_NAME_LENGTH)
			string = string.substring(0, MAX_NAME_LENGTH);
		// pad out the string
		StringBuffer buf = new StringBuffer(string);
		for (int j = string.length(); j < MAX_NAME_LENGTH; j++) {
			buf.append(" ");
		}
		return buf.toString();
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PrintTrainsAction.class
			.getName());
}
