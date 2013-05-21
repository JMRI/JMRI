// PrintCarRosterAction.java

package jmri.jmrit.operations.rollingstock.cars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.List;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Action to print a summary of the Roster contents
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in MacIntosh MRJ
 * 
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2008, 2010, 2011, 2012, 2013
 * @version $Revision$
 */
public class PrintCarRosterAction extends AbstractAction {

	CarManager manager = CarManager.instance();

	public PrintCarRosterAction(String actionName, Frame frame, boolean preview, Component pWho) {
		super(actionName);
		mFrame = frame;
		isPreview = preview;
		panel = (CarsTableFrame) pWho;
	}

	/**
	 * Frame hosting the printing
	 */
	Frame mFrame;
	/**
	 * Variable to set whether this is to be printed or previewed
	 */
	boolean isPreview;
	CarsTableFrame panel;
	CarPrintOptionFrame cpof = null;

	public void actionPerformed(ActionEvent e) {
		if (cpof == null)
			cpof = new CarPrintOptionFrame(this);
		else
			cpof.setVisible(true);
		cpof.initComponents();
	}

	int numberCharPerLine = 90;
	int ownerMaxLen = CarOwners.instance().getCurMaxNameLength();
	List<String> cars;

	private void printCars() {

		boolean landscape = false;
		if (manifestOrientationComboBox.getSelectedItem() != null
				&& manifestOrientationComboBox.getSelectedItem() == Setup.LANDSCAPE) {
			landscape = true;
			numberCharPerLine = 120;
		}

		// obtain a HardcopyWriter to do this
		HardcopyWriter writer = null;
		try {
			writer = new HardcopyWriter(mFrame, Bundle.getMessage("TitleCarRoster"), 10, .5, .5, .5, .5,
					isPreview, "", landscape, true);
		} catch (HardcopyWriter.PrintCanceledException ex) {
			log.debug("Print cancelled");
			return;
		}

		// Loop through the Roster, printing as needed

		String location = "";
		String number;
		String road;
		String type;
		String length = "";
		String weight = "";
		String color = "";
		String owner = "";
		String built = "";
		String load = "";
		String kernel = "";
		String train = "";
		String destination = "";
		String value = "";
		String rfid = "";
		String last = "";
		String wait = "";
		String comment = "";

		ownerMaxLen = CarOwners.instance().getCurMaxNameLength();
		if (printCarLoad.isSelected() || printCarKernel.isSelected() || printCarColor.isSelected())
			ownerMaxLen = 5;

		try {
			printTitleLine(writer);
			String previousLocation = "";
			for (int i = 0; i < cars.size(); i++) {
				Car car = manager.getById(cars.get(i));
				if (printCarsWithLocation.isSelected() && car.getLocation() == null)
					continue; // car doesn't have a location skip
				location = "";
				if (printCarLocation.isSelected() && car.getLocation() != null) {
					location = car.getLocationName().trim() + " - " + car.getTrackName().trim();
					// reduce location name by one half of the track name
					location = padAttribute(location, Control.max_len_string_location_name
							+ Control.max_len_string_track_name / 2);
				}
				// Page break between locations?
				if (!previousLocation.equals("") && !car.getLocationName().trim().equals(previousLocation)
						&& printPage.isSelected()) {
					writer.pageBreak();
					printTitleLine(writer);
				}
				// Add a line between locations?
				else if (!previousLocation.equals("")
						&& !car.getLocationName().trim().equals(previousLocation) && printSpace.isSelected()) {
					writer.write(NEW_LINE);
				}
				previousLocation = car.getLocationName().trim();

				// car number
				number = padAttribute(car.getNumber().trim(), 7);
				// car road
				road = padAttribute(car.getRoadName().trim(), CarRoads.instance().getCurMaxNameLength());
				// car type
				type = padAttribute(car.getTypeName().trim(), CarTypes.instance().getCurMaxNameLength());

				if (printCarLength.isSelected())
					length = padAttribute(car.getLength().trim(), Control.max_len_string_length_name);
				if (printCarWeight.isSelected())
					weight = padAttribute(car.getWeight().trim(), Control.max_len_string_weight_name);
				if (printCarColor.isSelected())
					color = padAttribute(car.getColor().trim(), CarColors.instance().getCurMaxNameLength());
				if (printCarLoad.isSelected())
					load = padAttribute(car.getLoadName().trim(), CarLoads.instance().getCurMaxNameLength());
				if (printCarKernel.isSelected())
					kernel = padAttribute(car.getKernelName().trim(), Control.max_len_string_attibute);
				if (printCarOwner.isSelected())
					owner = padAttribute(car.getOwner().trim(), ownerMaxLen);
				if (printCarBuilt.isSelected())
					built = padAttribute(car.getBuilt().trim(), Control.max_len_string_built_name);
				if (printCarLast.isSelected())
					last = padAttribute(car.getLastDate().split(" ")[0], 10);
				if (printCarWait.isSelected())
					wait = padAttribute(Integer.toString(car.getWait()), 4);
				if (printCarValue.isSelected())
					value = padAttribute(car.getValue().trim(), Control.max_len_string_attibute);
				if (printCarRfid.isSelected())
					rfid = padAttribute(car.getRfid().trim(), Control.max_len_string_attibute);
				if (printCarTrain.isSelected())
					// pad out train to half of its maximum
					train = padAttribute(car.getTrainName().trim(), Control.max_len_string_train_name / 2);
				if (printCarDestination.isSelected())
					destination = padAttribute(car.getDestinationName().trim(),
							Control.max_len_string_location_name);
				if (printCarComment.isSelected())
					comment = car.getComment().trim();

				String s = number + road + type + length + weight + color + load + kernel + owner + built
						+ last + wait + value + rfid + location + train + destination + comment;

				if (s.length() > numberCharPerLine)
					s = s.substring(0, numberCharPerLine);
				writer.write(s + NEW_LINE);
			}

			// and force completion of the printing
			writer.close();
		} catch (IOException we) {
			log.error("Error printing car roster");
		}
	}

	private void printTitleLine(HardcopyWriter writer) throws IOException {
		String s = Bundle.getMessage("Number")
				+ "  "
				+ padAttribute(Bundle.getMessage("Road"), CarRoads.instance().getCurMaxNameLength())
				+ padAttribute(Bundle.getMessage("Type"), CarTypes.instance().getCurMaxNameLength())
				+ (printCarLength.isSelected() ? Bundle.getMessage("Len") + "  " : "")
				+ (printCarWeight.isSelected() ? "     " : "")
				+ (printCarColor.isSelected() ? padAttribute(Bundle.getMessage("Color"), CarColors.instance()
						.getCurMaxNameLength()) : "")
				+ (printCarLoad.isSelected() ? padAttribute(Bundle.getMessage("Load"), CarLoads.instance()
						.getCurMaxNameLength()) : "")
				+ (printCarKernel.isSelected() ? padAttribute(("Kernel"), Control.max_len_string_attibute)
						: "")
				+ (printCarOwner.isSelected() ? padAttribute(Bundle.getMessage("Owner"), ownerMaxLen) : "")
				+ (printCarBuilt.isSelected() ? Bundle.getMessage("Built") + " " : "")
				+ (printCarLast.isSelected() ? Bundle.getMessage("LastMoved") + " " : "")
				+ (printCarWait.isSelected() ? Bundle.getMessage("Wait") + " " : "")
				+ (printCarValue.isSelected() ? Setup.getValueLabel() + "        " : "")
				+ (printCarRfid.isSelected() ? Setup.getRfidLabel() + "        " : "")
				+ (printCarLocation.isSelected() ? padAttribute(Bundle.getMessage("Location"),
						Control.max_len_string_location_name + Control.max_len_string_track_name / 2) : "")
				+ (printCarTrain.isSelected() ? padAttribute(Bundle.getMessage("Train"),
						Control.max_len_string_train_name / 2) : "")
				+ (printCarDestination.isSelected() ? padAttribute(Bundle.getMessage("Destination"),
						Control.max_len_string_location_name) : "")
				+ (printCarComment.isSelected() ? Bundle.getMessage("Comment") : "");
		if (s.length() > numberCharPerLine)
			s = s.substring(0, numberCharPerLine);
		writer.write(s + NEW_LINE);
	}

	private String padAttribute(String attribute, int length) {
		if (attribute.length() > length)
			attribute = attribute.substring(0, length);
		StringBuffer buf = new StringBuffer(attribute);
		for (int i = attribute.length(); i < length + 1; i++)
			buf.append(" ");
		return buf.toString();
	}

	JLabel sort = new JLabel(" ");

	JComboBox manifestOrientationComboBox = Setup.getOrientationComboBox();

	JCheckBox printCarsWithLocation = new JCheckBox(Bundle.getMessage("PrintCarsWithLocation"));
	JCheckBox printCarLength = new JCheckBox(Bundle.getMessage("PrintCarLength"));
	JCheckBox printCarWeight = new JCheckBox(Bundle.getMessage("PrintCarWeight"));
	JCheckBox printCarColor = new JCheckBox(Bundle.getMessage("PrintCarColor"));
	JCheckBox printCarOwner = new JCheckBox(Bundle.getMessage("PrintCarOwner"));
	JCheckBox printCarBuilt = new JCheckBox(Bundle.getMessage("PrintCarBuilt"));
	JCheckBox printCarLoad = new JCheckBox(Bundle.getMessage("PrintCarLoad"));
	JCheckBox printCarKernel = new JCheckBox(Bundle.getMessage("PrintKernel"));
	JCheckBox printCarValue = new JCheckBox(MessageFormat.format(Bundle.getMessage("PrintCar"),
			new Object[] { Setup.getValueLabel() }));
	JCheckBox printCarRfid = new JCheckBox(MessageFormat.format(Bundle.getMessage("PrintCar"),
			new Object[] { Setup.getRfidLabel() }));
	JCheckBox printCarLast = new JCheckBox(Bundle.getMessage("PrintCarLastMoved"));
	JCheckBox printCarWait = new JCheckBox(Bundle.getMessage("PrintCarWait"));
	JCheckBox printCarLocation = new JCheckBox(Bundle.getMessage("PrintCarLocation"));
	JCheckBox printCarTrain = new JCheckBox(Bundle.getMessage("PrintCarTrain"));
	JCheckBox printCarDestination = new JCheckBox(Bundle.getMessage("PrintCarDestination"));
	JCheckBox printCarComment = new JCheckBox(Bundle.getMessage("PrintCarComment"));
	JCheckBox printSpace = new JCheckBox(Bundle.getMessage("PrintSpace"));
	JCheckBox printPage = new JCheckBox(Bundle.getMessage("PrintPage"));

	JButton okayButton = new JButton(Bundle.getMessage("ButtonOkay"));

	static final String NEW_LINE = "\n"; // NOI18N

	public class CarPrintOptionFrame extends OperationsFrame {
		PrintCarRosterAction pcr;

		public CarPrintOptionFrame(PrintCarRosterAction pcr) {
			super();
			this.pcr = pcr;
			// create panel
			JPanel pSortBy = new JPanel();
			pSortBy.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SortBy")));
			pSortBy.add(sort);

			JPanel pOrientation = new JPanel();
			pOrientation.setBorder(BorderFactory.createTitledBorder(Bundle
					.getMessage("BorderLayoutOrientation")));
			pOrientation.add(manifestOrientationComboBox);

			JPanel pPanel = new JPanel();
			pPanel.setLayout(new GridBagLayout());
			pPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PrintOptions")));
			addItemLeft(pPanel, printCarsWithLocation, 0, 0);
			addItemLeft(pPanel, printCarLength, 0, 1);
			addItemLeft(pPanel, printCarWeight, 0, 2);
			addItemLeft(pPanel, printCarColor, 0, 3);
			addItemLeft(pPanel, printCarLoad, 0, 4);
			addItemLeft(pPanel, printCarKernel, 0, 5);
			addItemLeft(pPanel, printCarOwner, 0, 6);
			addItemLeft(pPanel, printCarBuilt, 0, 7);
			addItemLeft(pPanel, printCarLast, 0, 8);
			addItemLeft(pPanel, printCarWait, 0, 9);
			if (Setup.isValueEnabled())
				addItemLeft(pPanel, printCarValue, 0, 10);
			if (Setup.isRfidEnabled())
				addItemLeft(pPanel, printCarRfid, 0, 11);
			addItemLeft(pPanel, printCarLocation, 0, 12);
			addItemLeft(pPanel, printCarTrain, 0, 13);
			addItemLeft(pPanel, printCarDestination, 0, 14);
			addItemLeft(pPanel, printCarComment, 0, 15);
			addItemLeft(pPanel, printSpace, 0, 16);
			addItemLeft(pPanel, printPage, 0, 17);

			// set defaults
			printCarsWithLocation.setSelected(false);
			printCarLength.setSelected(true);
			printCarWeight.setSelected(false);
			printCarColor.setSelected(true);
			printCarLoad.setSelected(false);
			printCarKernel.setSelected(false);
			printCarOwner.setSelected(false);
			printCarBuilt.setSelected(false);
			printCarLast.setSelected(false);
			printCarWait.setSelected(false);
			printCarValue.setSelected(false);
			printCarRfid.setSelected(false);
			printCarLocation.setSelected(true);
			printCarTrain.setSelected(false);
			printCarDestination.setSelected(false);
			printCarComment.setSelected(false);
			printSpace.setSelected(false);
			printPage.setSelected(false);

			// add tool tips
			printSpace.setToolTipText(Bundle.getMessage("TipSelectSortByLoc"));
			printPage.setToolTipText(Bundle.getMessage("TipSelectSortByLoc"));

			JPanel pButtons = new JPanel();
			pButtons.setLayout(new GridBagLayout());
			pButtons.add(okayButton);
			pButtons.setBorder(BorderFactory.createTitledBorder(""));
			addButtonAction(okayButton);

			getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
			getContentPane().add(pSortBy);
			getContentPane().add(pOrientation);
			getContentPane().add(pPanel);
			getContentPane().add(pButtons);
			setPreferredSize(null);
			pack();
			setVisible(true);
		}

		public void initComponents() {
			if (isPreview)
				cpof.setTitle(Bundle.getMessage("MenuItemPreview"));
			else
				cpof.setTitle(Bundle.getMessage("MenuItemPrint"));
			sort.setText(panel.carsModel.getSortByName());
			cars = panel.getSortByList();
			printSpace.setEnabled(panel.sortByLocation.isSelected());
			printPage.setEnabled(panel.sortByLocation.isSelected());
			if (!panel.sortByLocation.isSelected()) {
				printSpace.setSelected(false);
				printPage.setSelected(false);
			}
		}

		public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
			setVisible(false);
			pcr.printCars();
		}
	}

	static Logger log = LoggerFactory.getLogger(PrintCarRosterAction.class.getName());
}
