// TrainSwitchListEditFrame.java

package jmri.jmrit.operations.trains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;

import java.beans.PropertyChangeEvent;
import java.io.File;

/**
 * Frame for user selection of switch lists
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2012, 2013
 * @version $Revision$
 */

public class TrainSwitchListEditFrame extends OperationsFrame implements
		java.beans.PropertyChangeListener {

	JScrollPane switchPane;

	// load managers
	LocationManager locationManager = LocationManager.instance();
	List<JCheckBox> locationCheckBoxes = new ArrayList<JCheckBox>();
	List<JComboBox> locationComboBoxes = new ArrayList<JComboBox>();
	JPanel locationPanelCheckBoxes = new JPanel();

	// checkboxes
	JCheckBox switchListRealTimeCheckBox = new JCheckBox(Bundle.getMessage("SwitchListRealTime"));
	JCheckBox switchListAllTrainsCheckBox = new JCheckBox(Bundle.getMessage("SwitchListAllTrains"));
	JCheckBox switchListPageCheckBox = new JCheckBox(Bundle.getMessage("SwitchListPage"));

	// major buttons
	JButton clearButton = new JButton(Bundle.getMessage("Clear"));
	JButton setButton = new JButton(Bundle.getMessage("Select"));
	JButton printButton = new JButton(Bundle.getMessage("PrintSwitchLists"));
	JButton previewButton = new JButton(Bundle.getMessage("PreviewSwitchLists"));
	JButton changeButton = new JButton(Bundle.getMessage("PrintChanges"));
	JButton runButton = new JButton(Bundle.getMessage("RunFile"));
	JButton csvGenerateButton = new JButton(Bundle.getMessage("CsvGenerate"));
	JButton csvChangeButton = new JButton(Bundle.getMessage("CsvChanges"));
	JButton updateButton = new JButton(Bundle.getMessage("Update"));
	JButton resetButton = new JButton(Bundle.getMessage("ResetSwitchLists"));
	JButton saveButton = new JButton(Bundle.getMessage("Save"));

	// text field

	// combo boxes

	public TrainSwitchListEditFrame() {
		super(Bundle.getMessage("TitleSwitchLists"));
	}

	public void initComponents() {
		// listen for any changes in the number of locations
		locationManager.addPropertyChangeListener(this);

		// the following code sets the frame's initial state
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// tool tips
		switchListRealTimeCheckBox.setToolTipText(Bundle.getMessage("RealTimeTip"));
		switchListAllTrainsCheckBox.setToolTipText(Bundle.getMessage("AllTrainsTip"));
		switchListPageCheckBox.setToolTipText(Bundle.getMessage("PageTrainTip"));
		csvChangeButton.setToolTipText(Bundle.getMessage("CsvChangesTip"));
		changeButton.setToolTipText(Bundle.getMessage("PrintChangesTip"));
		resetButton.setToolTipText(Bundle.getMessage("ResetSwitchListTip"));

		switchPane = new JScrollPane(locationPanelCheckBoxes);
		switchPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		switchPane.setBorder(BorderFactory.createTitledBorder(""));

		// Layout the panel by rows
		locationPanelCheckBoxes.setLayout(new GridBagLayout());
		updateLocationCheckboxes();
		enableChangeButtons();

		// Clear and set buttons
		JPanel pButtons = new JPanel();
		pButtons.setLayout(new GridBagLayout());
		pButtons.setBorder(BorderFactory.createTitledBorder(""));
		addItem(pButtons, clearButton, 0, 1);
		addItem(pButtons, setButton, 1, 1);

		// options
		JPanel pSwitchListOptions = new JPanel();
		pSwitchListOptions.setLayout(new GridBagLayout());
		pSwitchListOptions.setBorder(BorderFactory
				.createTitledBorder(Bundle.getMessage("BorderLayoutSwitchListOptions")));
		addItem(pSwitchListOptions, switchListAllTrainsCheckBox, 1, 0);
		addItem(pSwitchListOptions, switchListPageCheckBox, 2, 0);
		addItem(pSwitchListOptions, switchListRealTimeCheckBox, 3, 0);
		addItem(pSwitchListOptions, saveButton, 4, 0);

		// buttons
		JPanel controlpanel = new JPanel();
		controlpanel.setLayout(new GridBagLayout());

		// row 3
		addItem(controlpanel, previewButton, 0, 2);
		addItem(controlpanel, printButton, 1, 2);
		addItem(controlpanel, changeButton, 2, 2);
		// row 4
		addItem(controlpanel, updateButton, 0, 3);
		addItem(controlpanel, resetButton, 2, 3);
		// row 5
		if (Setup.isGenerateCsvSwitchListEnabled()) {
			addItem(controlpanel, runButton, 0, 4);
			addItem(controlpanel, csvGenerateButton, 1, 4);
			addItem(controlpanel, csvChangeButton, 2, 4);
		}

		getContentPane().add(switchPane);
		getContentPane().add(pButtons);
		getContentPane().add(pSwitchListOptions);
		getContentPane().add(controlpanel);

		// Set the state
		switchListRealTimeCheckBox.setSelected(Setup.isSwitchListRealTime());
		switchListAllTrainsCheckBox.setSelected(Setup.isSwitchListAllTrainsEnabled());
		switchListPageCheckBox.setSelected(Setup.isSwitchListPagePerTrainEnabled());

		updateButton.setVisible(!switchListRealTimeCheckBox.isSelected());
		resetButton.setVisible(!switchListRealTimeCheckBox.isSelected());
		saveButton.setEnabled(false);

		// setup buttons
		addButtonAction(clearButton);
		addButtonAction(setButton);
		addButtonAction(printButton);
		addButtonAction(previewButton);
		addButtonAction(changeButton);
		addButtonAction(runButton);
		addButtonAction(csvGenerateButton);
		addButtonAction(csvChangeButton);
		addButtonAction(updateButton);
		addButtonAction(resetButton);
		addButtonAction(saveButton);

		// setup checkbox
		addCheckBoxAction(switchListRealTimeCheckBox);
		addCheckBoxAction(switchListAllTrainsCheckBox);
		addCheckBoxAction(switchListPageCheckBox);

		// add help menu to window
		addHelpMenu("package.jmri.jmrit.operations.Operations_SwitchList", true); // NOI18N
		// set frame size and train for display
		initMinimumSize(new Dimension(Control.panelWidth, Control.panelHeight));
	}

	// Buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == clearButton) {
			selectCheckboxes(false);
		}
		if (ae.getSource() == setButton) {
			selectCheckboxes(true);
		}
		if (ae.getSource() == previewButton) {
			buildSwitchList(true, false, false, false);
		}
		if (ae.getSource() == printButton) {
			buildSwitchList(false, false, false, false);
		}
		if (ae.getSource() == changeButton) {
			buildSwitchList(false, true, false, false);
		}
		if (ae.getSource() == csvGenerateButton) {
			buildSwitchList(false, false, true, false);
		}
		if (ae.getSource() == csvChangeButton) {
			buildSwitchList(false, true, true, false);
		}
		if (ae.getSource() == updateButton) {
			buildSwitchList(true, false, false, true);
		}
		if (ae.getSource() == runButton) {
			runCustomSwitchLists();
		}
		if (ae.getSource() == resetButton) {
			reset();
		}
		if (ae.getSource() == saveButton) {
			save();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}

	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == switchListRealTimeCheckBox) {
			updateButton.setVisible(!switchListRealTimeCheckBox.isSelected());
			resetButton.setVisible(!switchListRealTimeCheckBox.isSelected());
		}
		// enable the save button whenever a checkbox is changed
		enableSaveButton(true);
	}
	
	// Remove all terminated or reset trains from the switch lists for selected locations
	private void reset() {
		for (int i = 0; i < locationCheckBoxes.size(); i++) {
			String locationName = locationCheckBoxes.get(i).getName();
			Location location = locationManager.getLocationByName(locationName);
			if (location.isSwitchListEnabled()) {
				// new switch lists will now be created for the location
				location.setSwitchListState(Location.SW_CREATE);
				location.setStatus(Location.MODIFIED);
			}
		}
		// set trains switch lists unknown, any built trains should remain on the switch lists
		TrainManager.instance().setTrainsSwitchListStatus(Train.UNKNOWN);
	}

	// save printer selection
	private void save() {
		for (int i = 0; i < locationCheckBoxes.size(); i++) {
			String locationName = locationCheckBoxes.get(i).getName();
			Location l = locationManager.getLocationByName(locationName);
			JComboBox comboBox = locationComboBoxes.get(i);
			String printerName = (String) comboBox.getSelectedItem();
			if (printerName.equals(TrainPrintUtilities.getDefaultPrinterName())) {
				l.setDefaultPrinterName("");
			} else {
				log.debug("Location " + l.getName() + " has selected printer " + printerName);
				l.setDefaultPrinterName(printerName);
			}
		}
		Setup.setSwitchListRealTime(switchListRealTimeCheckBox.isSelected());
		Setup.setSwitchListAllTrainsEnabled(switchListAllTrainsCheckBox.isSelected());
		Setup.setSwitchListPagePerTrainEnabled(switchListPageCheckBox.isSelected());
		// save setup file
		OperationsSetupXml.instance().setDirty(true);
		// save location file
		OperationsXml.save();
		enableSaveButton(false);
		if (Setup.isCloseWindowOnSaveEnabled())
			dispose();
	}

	/**
	 * Print = all false;
	 * @param isPreview true if print preview
	 * @param isChanged true if print changes was requested
	 * @param isCsv true if building a CSV switch list files
	 * @param isUpdate true if only updating switch lists
	 */
	private void buildSwitchList(boolean isPreview, boolean isChanged, boolean isCsv, boolean isUpdate) {
		TrainSwitchLists ts = new TrainSwitchLists();
		for (int i = 0; i < locationCheckBoxes.size(); i++) {
			String locationName = locationCheckBoxes.get(i).getName();
			Location location = locationManager.getLocationByName(locationName);
			if (location.isSwitchListEnabled()) {
				if (!isCsv) {
					ts.buildSwitchList(location);
					// print or print changes
					if (!isUpdate && !isChanged
							|| (!isUpdate && isChanged && !location.getStatus().equals(Location.PRINTED)))
						ts.printSwitchList(location, isPreview);
				} else if (Setup.isGenerateCsvSwitchListEnabled()) {
					TrainCsvSwitchLists tCSVs = new TrainCsvSwitchLists();
					tCSVs.buildSwitchList(location);
					location.setStatus(Location.CSV_GENERATED);
				}
			}
		}
		// set trains switch lists printed
		TrainManager.instance().setTrainsSwitchListStatus(Train.PRINTED);
	}

	private void selectCheckboxes(boolean enable) {
		for (int i = 0; i < locationCheckBoxes.size(); i++) {
			String locationName = locationCheckBoxes.get(i).getName();
			Location l = locationManager.getLocationByName(locationName);
			l.setSwitchListEnabled(enable);
		}
		// enable the save button whenever a checkbox is changed
		saveButton.setEnabled(true);
	}

	// name change or number of locations has changed
	private void updateLocationCheckboxes() {
		List<Location> locations = locationManager.getLocationsByNameList();
		synchronized (this) {
			for (int i = 0; i < locations.size(); i++) {
				locations.get(i).removePropertyChangeListener(this);
			}
		}

		locationCheckBoxes.clear();
		locationComboBoxes.clear(); // remove printer selection
		locationPanelCheckBoxes.removeAll();

		// create header
		addItem(locationPanelCheckBoxes, new JLabel(Bundle.getMessage("Location")), 0, 0);
		addItem(locationPanelCheckBoxes, new JLabel("        "), 1, 0);
		addItem(locationPanelCheckBoxes, new JLabel(Bundle.getMessage("Status")), 2, 0);
		addItem(locationPanelCheckBoxes, new JLabel("        "), 3, 0);
		addItem(locationPanelCheckBoxes, new JLabel(Bundle.getMessage("Comment")), 4, 0);
		addItem(locationPanelCheckBoxes, new JLabel("        "), 5, 0);
		addItem(locationPanelCheckBoxes, new JLabel(Bundle.getMessage("Printer")), 6, 0);

		int y = 1; // vertical position in panel

		Location previousLocation = null;

		for (int i = 0; i < locations.size(); i++) {
			Location l = locations.get(i);
			if (l.getStatus().equals(Location.MODIFIED) && l.isSwitchListEnabled()) {
				changeButton.setEnabled(true);
				csvChangeButton.setEnabled(true);
			}
			String name = TrainCommon.splitString(l.getName());
			if (previousLocation != null
					&& TrainCommon.splitString(previousLocation.getName()).equals(name)) {
				l.setSwitchListEnabled(previousLocation.isSwitchListEnabled());
				if (previousLocation.isSwitchListEnabled()
						&& l.getStatus().equals(Location.MODIFIED)) {
					previousLocation.setStatusModified(); // we need to update the primary location
					l.setStatus(Location.UPDATED); // and clear the secondaries
				}
				continue;
			}
			previousLocation = l;

			JCheckBox checkBox = new JCheckBox();
			locationCheckBoxes.add(checkBox);
			checkBox.setSelected(l.isSwitchListEnabled());
			checkBox.setText(name);
			checkBox.setName(l.getName());
			addLocationCheckBoxAction(checkBox);
			addItemLeft(locationPanelCheckBoxes, checkBox, 0, y);

			JLabel status = new JLabel(l.getStatus());
			addItem(locationPanelCheckBoxes, status, 2, y);

			JButton button = new JButton(Bundle.getMessage("Add"));
			if (!l.getSwitchListComment().equals(""))
				button.setText(Bundle.getMessage("Edit"));
			button.setName(l.getName());
			addCommentButtonAction(button);
			addItem(locationPanelCheckBoxes, button, 4, y);

			JComboBox comboBox = TrainPrintUtilities.getPrinterJComboBox();
			locationComboBoxes.add(comboBox);
			comboBox.setSelectedItem(l.getDefaultPrinterName());
			addComboBoxAction(comboBox);
			addItem(locationPanelCheckBoxes, comboBox, 6, y++);

		}

		// restore listeners
		synchronized (this) {
			for (int i = 0; i < locations.size(); i++) {
				locations.get(i).addPropertyChangeListener(this);
			}
		}

		locationPanelCheckBoxes.revalidate();
		pack();
		repaint();
	}
	
	private void runCustomSwitchLists() {
		if (!Setup.isGenerateCsvSwitchListEnabled())
			return;
		log.debug("run custom switch lists");
		TrainCsvSwitchLists tCSVs = new TrainCsvSwitchLists();
		for (int i = 0; i < locationCheckBoxes.size(); i++) {
			String locationName = locationCheckBoxes.get(i).getName();
			Location location = locationManager.getLocationByName(locationName);
			if (location.isSwitchListEnabled()) {
				File csvFile = tCSVs.buildSwitchList(location);
				location.setStatus(Location.CSV_GENERATED);
				if (csvFile == null || !csvFile.exists()) {
					log.warn("CSV switch list file was not created for location " + locationName);
					return;
				}
				if (TrainCustomSwitchList.manifestCreatorFileExists())
					TrainCustomSwitchList.addCVSFile(csvFile);
			}
		}
		// Processes the CSV Manifest files using an external custom program.
		if (!TrainCustomSwitchList.manifestCreatorFileExists()) {
			log.warn("Manifest creator file not found!, directory name: " + TrainCustomSwitchList.getDirectoryName()
					+ ", file name: " + TrainCustomSwitchList.getFileName()); // NOI18N
			JOptionPane.showMessageDialog(this, MessageFormat.format(
					Bundle.getMessage("LoadDirectoryNameFileName"), new Object[] {
						TrainCustomSwitchList.getDirectoryName(), TrainCustomSwitchList.getFileName() }), Bundle
					.getMessage("ManifestCreatorNotFound"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		// Now run the user specified custom Switch List processor program
		TrainCustomSwitchList.process();
	}

	private void enableSaveButton(boolean enable) {
		saveButton.setEnabled(enable);
		// these get the inverse
		previewButton.setEnabled(!enable);
		printButton.setEnabled(!enable);
		updateButton.setEnabled(!enable);
		resetButton.setEnabled(!enable);
	}

	private void enableChangeButtons() {
		changeButton.setEnabled(false);
		csvChangeButton.setEnabled(false);
		List<Location> locations = locationManager.getLocationsByNameList();
		for (int i = 0; i < locations.size(); i++) {
			Location l = locations.get(i);
			if (l.getStatus().equals(Location.MODIFIED) && l.isSwitchListEnabled()) {
				changeButton.setEnabled(true);
				csvChangeButton.setEnabled(true);
			}
		}
	}

	// The print switch list for a location has changed
	private void changeLocationCheckboxes(PropertyChangeEvent e) {
		Location l = (Location) e.getSource();
		for (int i = 0; i < locationCheckBoxes.size(); i++) {
			JCheckBox checkBox = locationCheckBoxes.get(i);
			if (checkBox.getName().equals(l.getName())) {
				checkBox.setSelected(l.isSwitchListEnabled());
				break;
			}
		}
	}

	private void addLocationCheckBoxAction(JCheckBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				locationCheckBoxActionPerformed(e);
			}
		});
	}

	public void locationCheckBoxActionPerformed(ActionEvent ae) {
		JCheckBox b = (JCheckBox) ae.getSource();
		log.debug("checkbox change " + b.getName());
		Location l = locationManager.getLocationByName(b.getName());
		l.setSwitchListEnabled(b.isSelected());
		// enable the save button whenever a checkbox is changed
		saveButton.setEnabled(true);
	}

	private void addCommentButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				commentButtonActionPerformed(e);
			}
		});
	}

	public void commentButtonActionPerformed(ActionEvent ae) {
		JButton b = (JButton) ae.getSource();
		log.debug("button action " + b.getName());
		Location l = locationManager.getLocationByName(b.getName());
		new TrainSwitchListCommentFrame(l);
	}
	
	protected void comboBoxActionPerformed(ActionEvent ae) {
		log.debug("combo box action");
		saveButton.setEnabled(true);
	}

	public void dispose() {
		locationManager.removePropertyChangeListener(this);
		List<Location> locations = locationManager.getLocationsByNameList();
		for (int i = 0; i < locations.size(); i++) {
			locations.get(i).removePropertyChangeListener(this);
		}
		super.dispose();
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue()
					+ " new: " + e.getNewValue()); // NOI18N
		if (e.getPropertyName().equals(Location.SWITCHLIST_CHANGED_PROPERTY)) {
			changeLocationCheckboxes(e);
			enableChangeButtons();
		}
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Location.STATUS_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Location.SWITCHLIST_COMMENT_CHANGED_PROPERTY)) {
			updateLocationCheckboxes();
			enableChangeButtons();
		}
	}

	private static class TrainSwitchListCommentFrame extends OperationsFrame {

		// text area
		JTextArea commentTextArea = new JTextArea(10, 90);
		JScrollPane commentScroller = new JScrollPane(commentTextArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Dimension minScrollerDim = new Dimension(1200, 500);
		JButton saveButton = new JButton(Bundle.getMessage("Save"));

		Location _location;

		private TrainSwitchListCommentFrame(Location location) {
			super();
			initComponents(location);
		}

		private void initComponents(Location location) {
			_location = location;
			// the following code sets the frame's initial state
			getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

			JPanel pC = new JPanel();
			pC.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
			pC.setLayout(new GridBagLayout());
			commentScroller.setMinimumSize(minScrollerDim);
			addItem(pC, commentScroller, 1, 0);

			commentTextArea.setText(location.getSwitchListComment());

			JPanel pB = new JPanel();
			pB.setLayout(new GridBagLayout());
			addItem(pB, saveButton, 0, 0);

			getContentPane().add(pC);
			getContentPane().add(pB);

			addButtonAction(saveButton);

			pack();
			setTitle(location.getName());
			setVisible(true);
		}

		// Buttons
		public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
			if (ae.getSource() == saveButton) {
				_location.setSwitchListComment(commentTextArea.getText());
				// save location file
				OperationsXml.save();
				if (Setup.isCloseWindowOnSaveEnabled())
					super.dispose();
			}
		}
	}

	static Logger log = LoggerFactory
			.getLogger(TrainSwitchListEditFrame.class.getName());
}
