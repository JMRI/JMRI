// PrintEngineRosterAction.java

package jmri.jmrit.operations.rollingstock.engines;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
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
 * @author Daniel Boudreau Copyright (C) 2008, 2011
 * @version $Revision$
 */
public class PrintEngineRosterAction extends AbstractAction {

	private static final int numberCharPerLine = 90;
	final int ownerMaxLen = 4; // Only show the first 4 characters of the owner's name

	EngineManager manager = EngineManager.instance();

	public PrintEngineRosterAction(String actionName, Frame frame, boolean preview, Component pWho) {
		super(actionName);
		mFrame = frame;
		isPreview = preview;
		panel = (EnginesTableFrame) pWho;
	}

	/**
	 * Frame hosting the printing
	 */
	Frame mFrame;
	/**
	 * Variable to set whether this is to be printed or previewed
	 */
	boolean isPreview;
	EnginesTableFrame panel;
	
	static final String NEW_LINE = "\n"; // NOI18N
	static final String TAB = "\t"; // NOI18N

	public void actionPerformed(ActionEvent e) {

		// obtain a HardcopyWriter to do this
		HardcopyWriter writer = null;
		try {
			writer = new HardcopyWriter(mFrame, Bundle.getString("TitleEngineRoster"), 10, .5, .5,
					.5, .5, isPreview);
		} catch (HardcopyWriter.PrintCanceledException ex) {
			log.debug("Print cancelled");
			return;
		}

		// Loop through the Roster, printing as needed
		String number;
		String road;
		String model;
		String type;
		String length;
		String owner = "";
		String consist = "";
		String built = "";
		String value = "";
		String rfid = "";
		String location;

		List<String> engines = panel.getSortByList();
		try {
			// header
			String s = Bundle.getString("Number")
					+ TAB
					+ Bundle.getString("Road")
					+ TAB
					+ Bundle.getString("Model")
					+ TAB
					+ "     "
					+ Bundle.getString("Type")
					+ "      "
					+ Bundle.getString("Length")
					+ " "
					+ (panel.sortByConsist.isSelected() ? Bundle.getString("Consist") + "     "
							: Bundle.getString("Owner"))
					+ (panel.sortByValue.isSelected() ? " "
							+ padAttribute(Setup.getValueLabel(), Control.max_len_string_attibute)
							: "")
					+ (panel.sortByRfid.isSelected() ? " "
							+ padAttribute(Setup.getRfidLabel(), Control.max_len_string_attibute)
							: "")
					+ ((!panel.sortByValue.isSelected() && !panel.sortByRfid.isSelected()) ? " "
							+ Bundle.getString("Built") : "") + " " + Bundle.getString("Location")
					+ NEW_LINE;
			writer.write(s);
			for (int i = 0; i < engines.size(); i++) {
				Engine engine = manager.getById(engines.get(i));

				// loco number
				number = padAttribute(engine.getNumber().trim(), 7);
				road = padAttribute(engine.getRoad().trim(), 7);
				model = padAttribute(engine.getModel().trim(), Control.max_len_string_attibute);
				type = padAttribute(engine.getType().trim(), Control.max_len_string_attibute);
				length = padAttribute(engine.getLength().trim(), Control.max_len_string_length_name);

				if (panel.sortByConsist.isSelected())
					consist = padAttribute(engine.getConsistName().trim(),
							Control.max_len_string_attibute);
				else
					owner = padAttribute(engine.getOwner().trim(), ownerMaxLen);

				if (panel.sortByValue.isSelected())
					value = padAttribute(engine.getValue().trim(), Control.max_len_string_attibute);
				else if (panel.sortByRfid.isSelected())
					rfid = padAttribute(engine.getRfid().trim(), Control.max_len_string_attibute);
				else
					built = padAttribute(engine.getBuilt().trim(),
							Control.max_len_string_built_name);

				location = "";
				if (!engine.getLocationName().equals("")) {
					location = engine.getLocationName() + " - " + engine.getTrackName();
				}

				s = number + road + model + type + length + owner + consist + value + rfid + built
						+ location;
				if (s.length() > numberCharPerLine)
					s = s.substring(0, numberCharPerLine);
				writer.write(s + NEW_LINE);
			}

			// and force completion of the printing
			writer.close();
		} catch (IOException we) {
			log.error("Error printing ConsistRosterEntry: " + e);
		}
	}

	private String padAttribute(String attribute, int length) {
		if (attribute.length() > length)
			attribute = attribute.substring(0, length);
		StringBuffer buf = new StringBuffer(attribute);
		for (int i = attribute.length(); i < length + 1; i++)
			buf.append(" ");
		return buf.toString();
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(PrintEngineRosterAction.class.getName());
}
