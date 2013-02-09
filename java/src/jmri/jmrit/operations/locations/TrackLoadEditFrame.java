// TrackLoadEditFrame.java

package jmri.jmrit.operations.locations;

import org.apache.log4j.Logger;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import java.awt.*;

import javax.swing.*;

/**
 * Frame for user edit of track loads
 * 
 * @author Dan Boudreau Copyright (C) 2013
 * @version $Revision: 22371 $
 */

public class TrackLoadEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	private static boolean loadAndType = false;
	private static boolean shipLoadAndType = false;


	Location _location = null;
	Track _track = null;
	String _type = "";
	JMenu _toolMenu = null;

	// panels
	JPanel pLoadControls = new JPanel();
	JPanel panelLoads = new JPanel();
	JScrollPane paneLoads = new JScrollPane(panelLoads);
	
	JPanel pShipLoadControls = new JPanel();
	JPanel panelShipLoads = new JPanel();
	JScrollPane paneShipLoadControls;
	JScrollPane paneShipLoads = new JScrollPane(panelShipLoads);

	// major buttons
	JButton saveTrackButton = new JButton(Bundle.getMessage("SaveTrack"));
	
	JButton addLoadButton = new JButton(Bundle.getMessage("AddLoad"));
	JButton deleteLoadButton = new JButton(Bundle.getMessage("DeleteLoad"));
	JButton deleteAllLoadsButton = new JButton(Bundle.getMessage("DeleteAll"));
	
	JButton addShipLoadButton = new JButton(Bundle.getMessage("AddLoad"));
	JButton deleteShipLoadButton = new JButton(Bundle.getMessage("DeleteLoad"));
	JButton deleteAllShipLoadsButton = new JButton(Bundle.getMessage("DeleteAll"));

	// check boxes
	JCheckBox loadAndTypeCheckBox = new JCheckBox(Bundle.getMessage("TypeAndLoad"));
	JCheckBox shipLoadAndTypeCheckBox = new JCheckBox(Bundle.getMessage("TypeAndLoad"));

	// radio buttons
	JRadioButton loadNameAll = new JRadioButton(Bundle.getMessage("AcceptAll"));
	JRadioButton loadNameInclude = new JRadioButton(Bundle.getMessage("AcceptOnly"));
	JRadioButton loadNameExclude = new JRadioButton(Bundle.getMessage("Exclude"));
	
	JRadioButton shipLoadNameAll = new JRadioButton(Bundle.getMessage("ShipAll"));
	JRadioButton shipLoadNameInclude = new JRadioButton(Bundle.getMessage("ShipOnly"));
	JRadioButton shipLoadNameExclude = new JRadioButton(Bundle.getMessage("Exclude"));

	// combo box
	JComboBox comboBoxLoads = CarLoads.instance().getComboBox(null);
	JComboBox comboBoxShipLoads = CarLoads.instance().getComboBox(null);
	JComboBox comboBoxTypes = CarTypes.instance().getComboBox();
	JComboBox comboBoxShipTypes = CarTypes.instance().getComboBox();
	
	// labels
	JLabel trackName = new JLabel();

	public static final String DISPOSE = "dispose"; // NOI18N
	public static final int MAX_NAME_LENGTH = Control.max_len_string_track_name;

	public TrackLoadEditFrame() {
		super(Bundle.getMessage("TitleEditTrackLoads"));
	}

	public void initComponents(Location location, Track track) {
		_location = location;
		_track = track;

		// property changes
		_location.addPropertyChangeListener(this);
		// listen for car load name and type changes
		CarLoads.instance().addPropertyChangeListener(this);
		CarTypes.instance().addPropertyChangeListener(this);

		// the following code sets the frame's initial state
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Set up the panels
		// Layout the panel by rows
		// row 1
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
		p1.setMaximumSize(new Dimension(2000, 250));

		// row 1a
		JPanel pTrackName = new JPanel();
		pTrackName.setLayout(new GridBagLayout());
		pTrackName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Track")));
		addItem(pTrackName, trackName, 0, 0);

		// row 1b
		JPanel pLocationName = new JPanel();
		pLocationName.setLayout(new GridBagLayout());
		pLocationName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));
		addItem(pLocationName, new JLabel(_location.getName()), 0, 0);

		p1.add(pTrackName);
		p1.add(pLocationName);
		
		// row 3
		JPanel p3 = new JPanel();
		p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
		JScrollPane pane3 = new JScrollPane(p3);
		pane3.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("LoadsTrack")));
		pane3.setMaximumSize(new Dimension(2000, 400));
		
		JPanel pLoadRadioButtons = new JPanel();
		pLoadRadioButtons.setLayout(new FlowLayout());
		
		pLoadRadioButtons.add(loadNameAll);
		pLoadRadioButtons.add(loadNameInclude);
		pLoadRadioButtons.add(loadNameExclude);
		pLoadRadioButtons.add(loadAndTypeCheckBox);
		
		pLoadControls.setLayout(new FlowLayout());
		
		pLoadControls.add(comboBoxTypes);
		pLoadControls.add(comboBoxLoads);
		pLoadControls.add(addLoadButton);
		pLoadControls.add(deleteLoadButton);
		pLoadControls.add(deleteAllLoadsButton);
		
		pLoadControls.setVisible(false);
		
		p3.add(pLoadRadioButtons);
		p3.add(pLoadControls);

		// row 4
		panelLoads.setLayout(new GridBagLayout());
		paneLoads.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Loads")));
		
		ButtonGroup loadGroup = new ButtonGroup();
		loadGroup.add(loadNameAll);
		loadGroup.add(loadNameInclude);
		loadGroup.add(loadNameExclude);
		
		// row 6
		JPanel p6 = new JPanel();
		p6.setLayout(new BoxLayout(p6, BoxLayout.Y_AXIS));
		paneShipLoadControls = new JScrollPane(p6);
		paneShipLoadControls.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ShipLoadsTrack")));
		paneShipLoadControls.setMaximumSize(new Dimension(2000, 400));
		
		JPanel pShipLoadRadioButtons = new JPanel();
		pShipLoadRadioButtons.setLayout(new FlowLayout());
		
		pShipLoadRadioButtons.add(shipLoadNameAll);
		pShipLoadRadioButtons.add(shipLoadNameInclude);
		pShipLoadRadioButtons.add(shipLoadNameExclude);
		pShipLoadRadioButtons.add(shipLoadAndTypeCheckBox);
		
		pShipLoadControls.setLayout(new FlowLayout());
		
		pShipLoadControls.add(comboBoxShipTypes);
		pShipLoadControls.add(comboBoxShipLoads);
		pShipLoadControls.add(addShipLoadButton);
		pShipLoadControls.add(deleteShipLoadButton);
		pShipLoadControls.add(deleteAllShipLoadsButton);
		
		pShipLoadControls.setVisible(false);
		
		p6.add(pShipLoadRadioButtons);
		p6.add(pShipLoadControls);

		// row 7
		panelShipLoads.setLayout(new GridBagLayout());
		paneShipLoads.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Loads")));
		
		ButtonGroup shipLoadGroup = new ButtonGroup();
		shipLoadGroup.add(shipLoadNameAll);
		shipLoadGroup.add(shipLoadNameInclude);
		shipLoadGroup.add(shipLoadNameExclude);

		// row 12
		JPanel panelButtons = new JPanel();
		panelButtons.setLayout(new GridBagLayout());
		panelButtons.setBorder(BorderFactory.createTitledBorder(""));
		panelButtons.setMaximumSize(new Dimension(2000, 200));
		
		// row 13
		addItem(panelButtons, saveTrackButton, 0, 0);

		getContentPane().add(p1);
		getContentPane().add(pane3);
		getContentPane().add(paneLoads);
		getContentPane().add(paneShipLoadControls);
		getContentPane().add(paneShipLoads);
		getContentPane().add(panelButtons);

		// setup buttons
		addButtonAction(saveTrackButton);

		addButtonAction(deleteLoadButton);
		addButtonAction(deleteAllLoadsButton);
		addButtonAction(addLoadButton);
		
		addButtonAction(deleteShipLoadButton);
		addButtonAction(deleteAllShipLoadsButton);
		addButtonAction(addShipLoadButton);

		addRadioButtonAction(loadNameAll);
		addRadioButtonAction(loadNameInclude);
		addRadioButtonAction(loadNameExclude);
		
		addRadioButtonAction(shipLoadNameAll);
		addRadioButtonAction(shipLoadNameInclude);
		addRadioButtonAction(shipLoadNameExclude);

		addComboBoxAction(comboBoxTypes);
		addComboBoxAction(comboBoxShipTypes);

		paneShipLoadControls.setVisible(false);
		paneShipLoads.setVisible(false);	
		
		// load fields and enable buttons
		if (_track != null) {
			_track.addPropertyChangeListener(this);
			trackName.setText(_track.getName());
			// only show ship loads for staging tracks
			paneShipLoadControls.setVisible(_track.getLocType().equals(Track.STAGING));
			paneShipLoads.setVisible(_track.getLocType().equals(Track.STAGING));				

			updateButtons(true);
		} else {
			updateButtons(false);
		}

		// build menu
//		JMenuBar menuBar = new JMenuBar();
//		_toolMenu = new JMenu(Bundle.getMessage("Tools"));
//		menuBar.add(_toolMenu);
//		setJMenuBar(menuBar);

		// load
		updateTypeComboBoxes();
		updateLoadComboBoxes();
		updateLoadNames();
		updateShipLoadNames();

		loadAndTypeCheckBox.setSelected(loadAndType);
		shipLoadAndTypeCheckBox.setSelected(shipLoadAndType);
		
		setMinimumSize(new Dimension(500, Control.panelHeight));
		pack();
		setVisible(true);
	}

	// Save, Delete, Add
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (_track == null)
			return;
		if (ae.getSource() == saveTrackButton) {
			log.debug("track save button activated");
			saveTrack(_track);
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
		if (ae.getSource() == addLoadButton) {
			String loadName = (String) comboBoxLoads.getSelectedItem();
			if (loadAndTypeCheckBox.isSelected())
				loadName = comboBoxTypes.getSelectedItem() + CarLoad.SPLIT_CHAR + loadName;
			_track.addLoadName(loadName);
			selectNextItemComboBox(comboBoxLoads);
		}
		if (ae.getSource() == deleteLoadButton) {
			String loadName = (String) comboBoxLoads.getSelectedItem();
			if (loadAndTypeCheckBox.isSelected())
				loadName = comboBoxTypes.getSelectedItem() + CarLoad.SPLIT_CHAR + loadName;
			_track.deleteLoadName(loadName);
			selectNextItemComboBox(comboBoxLoads);
		}
		if (ae.getSource() == deleteAllLoadsButton) {
			deleteAllLoads();
		}
		if (ae.getSource() == addShipLoadButton) {
			String loadName = (String) comboBoxShipLoads.getSelectedItem();
			if (shipLoadAndTypeCheckBox.isSelected())
				loadName = comboBoxShipTypes.getSelectedItem() + CarLoad.SPLIT_CHAR + loadName;
			_track.addShipLoadName(loadName);
			selectNextItemComboBox(comboBoxShipLoads);
		}
		if (ae.getSource() == deleteShipLoadButton) {
			String loadName = (String) comboBoxShipLoads.getSelectedItem();
			if (shipLoadAndTypeCheckBox.isSelected())
				loadName = comboBoxShipTypes.getSelectedItem() + CarLoad.SPLIT_CHAR + loadName;
			_track.deleteShipLoadName(loadName);
			selectNextItemComboBox(comboBoxShipLoads);
		}
		if (ae.getSource() == deleteAllShipLoadsButton) {
			deleteAllShipLoads();
		}
	}



	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
	protected void saveTrack(Track track) {
		// save the last state of the "Use car type and load" checkbox
		loadAndType = loadAndTypeCheckBox.isSelected();
		shipLoadAndType = shipLoadAndTypeCheckBox.isSelected();
		// save location file
		OperationsXml.save();
	}

	protected void updateButtons(boolean enabled) {
		saveTrackButton.setEnabled(enabled);
		
		loadNameAll.setEnabled(enabled);
		loadNameInclude.setEnabled(enabled);
		loadNameExclude.setEnabled(enabled);
		loadAndTypeCheckBox.setEnabled(enabled);
		
		boolean en = enabled && (_track.isAddCustomLoadsAnyStagingTrackEnabled()
				|| _track.isAddLoadsAnySidingEnabled()
				|| _track.isAddLoadsEnabled());
		
		shipLoadNameAll.setEnabled(en);
		shipLoadNameInclude.setEnabled(en);
		shipLoadNameExclude.setEnabled(en);
		shipLoadAndTypeCheckBox.setEnabled(en);
		
		addShipLoadButton.setEnabled(en);
		deleteShipLoadButton.setEnabled(en);
		deleteAllShipLoadsButton.setEnabled(en);
		
		comboBoxShipLoads.setEnabled(en);
		comboBoxShipTypes.setEnabled(en);
		
	}

	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button activated");
		if (ae.getSource() == loadNameAll) {
			_track.setLoadOption(Track.ALLLOADS);
		}
		if (ae.getSource() == loadNameInclude) {
			_track.setLoadOption(Track.INCLUDELOADS);
		}
		if (ae.getSource() == loadNameExclude) {
			_track.setLoadOption(Track.EXCLUDELOADS);
		}
		if (ae.getSource() == shipLoadNameAll) {
			_track.setShipLoadOption(Track.ALLLOADS);
		}
		if (ae.getSource() == shipLoadNameInclude) {
			_track.setShipLoadOption(Track.INCLUDELOADS);
		}
		if (ae.getSource() == shipLoadNameExclude) {
			_track.setShipLoadOption(Track.EXCLUDELOADS);
		}
	}

	// Car type combo box has been changed, show loads associated with this car type
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		updateLoadComboBoxes();
	}

	private void updateLoadComboBoxes() {
		String carType = (String) comboBoxTypes.getSelectedItem();
		CarLoads.instance().updateComboBox(carType, comboBoxLoads);
		carType = (String) comboBoxShipTypes.getSelectedItem();
		CarLoads.instance().updateComboBox(carType, comboBoxShipLoads);
	}

	private void updateLoadNames() {
		log.debug("Update load names");
		panelLoads.removeAll();
		if (_track != null) {
			// set radio button
			loadNameAll.setSelected(_track.getLoadOption().equals(Track.ALLLOADS));
			loadNameInclude.setSelected(_track.getLoadOption().equals(Track.INCLUDELOADS));
			loadNameExclude.setSelected(_track.getLoadOption().equals(Track.EXCLUDELOADS));
			
			pLoadControls.setVisible(!loadNameAll.isSelected());

			if (!loadNameAll.isSelected()) {
				int x = 0;
				int y = 0; // vertical position in panel

				int numberOfLoads = getNumberOfCheckboxes()/2 +1;
				String[] carLoads = _track.getLoadNames();
				for (int i = 0; i < carLoads.length; i++) {
					JLabel load = new JLabel();
					load.setText(carLoads[i]);
					addItemTop(panelLoads, load, x++, y);
					// limit the number of loads per line
					if (x > numberOfLoads) {
						y++;
						x = 0;
					}
				}
				validate();
			}
		} else {
			loadNameAll.setSelected(true);
		}
		panelLoads.repaint();
		panelLoads.validate();
	}
	
	private void updateShipLoadNames() {
		log.debug("Update ship load names");
		panelShipLoads.removeAll();
		if (_track != null) {
			// set radio button
			shipLoadNameAll.setSelected(_track.getShipLoadOption().equals(Track.ALLLOADS));
			shipLoadNameInclude.setSelected(_track.getShipLoadOption().equals(Track.INCLUDELOADS));
			shipLoadNameExclude.setSelected(_track.getShipLoadOption().equals(Track.EXCLUDELOADS));
			
			pShipLoadControls.setVisible(!shipLoadNameAll.isSelected());

			if (!shipLoadNameAll.isSelected()) {
				int x = 0;
				int y = 0; // vertical position in panel

				int numberOfLoads = getNumberOfCheckboxes()/2 +1;
				String[] carLoads = _track.getShipLoadNames();
				for (int i = 0; i < carLoads.length; i++) {
					JLabel load = new JLabel();
					load.setText(carLoads[i]);
					addItemTop(panelShipLoads, load, x++, y);
					// limit the number of loads per line
					if (x > numberOfLoads) {
						y++;
						x = 0;
					}
				}
				validate();
			}
		} else {
			shipLoadNameAll.setSelected(true);
		}
		panelShipLoads.repaint();
		panelShipLoads.validate();
	}

	private void deleteAllLoads() {
		if (_track != null) {
			String[] trackLoads = _track.getLoadNames();
			for (int i = 0; i < trackLoads.length; i++) {
				_track.deleteLoadName(trackLoads[i]);
			}
		}
	}
	
	private void deleteAllShipLoads() {
		if (_track != null) {
			String[] trackLoads = _track.getShipLoadNames();
			for (int i = 0; i < trackLoads.length; i++) {
				_track.deleteShipLoadName(trackLoads[i]);
			}
		}
	}

//	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
//		JCheckBox b = (JCheckBox) ae.getSource();
//		log.debug("checkbox change " + b.getText());
//		if (_location == null)
//			return;
//		if (b.isSelected()) {
//			_track.addTypeName(b.getText());
//		} else {
//			_track.deleteTypeName(b.getText());
//		}
//	}

	private void updateTypeComboBoxes() {
		CarTypes.instance().updateComboBox(comboBoxTypes);
		// remove car types not serviced by this location and track
		for (int i = comboBoxTypes.getItemCount() - 1; i >= 0; i--) {
			String type = (String) comboBoxTypes.getItemAt(i);
			if (_track != null && !_track.acceptsTypeName(type)) {
				comboBoxTypes.removeItem(type);
			}
		}
		CarTypes.instance().updateComboBox(comboBoxShipTypes);
		// remove car types not serviced by this location and track
		for (int i = comboBoxShipTypes.getItemCount() - 1; i >= 0; i--) {
			String type = (String) comboBoxShipTypes.getItemAt(i);
			if (_track != null && !_track.acceptsTypeName(type)) {
				comboBoxShipTypes.removeItem(type);
			}
		}
	}

	public void dispose() {
		if (_track != null)
			_track.removePropertyChangeListener(this);
		_location.removePropertyChangeListener(this);
		CarLoads.instance().removePropertyChangeListener(this);
		CarTypes.instance().removePropertyChangeListener(this);
		super.dispose();
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
//		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue()
					+ " new: " + e.getNewValue()); // NOI18N
		if (e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY)) {
			updateTypeComboBoxes();
		}
		if (e.getPropertyName().equals(CarLoads.LOAD_NAME_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY)) {
			updateLoadComboBoxes();
			updateLoadNames();
			updateShipLoadNames();
		}
		if (e.getPropertyName().equals(Track.LOADS_CHANGED_PROPERTY)) {
			updateLoadNames();
			updateShipLoadNames();
		}
		if (e.getPropertyName().equals(Track.LOAD_OPTIONS_CHANGED_PROPERTY)) {
			if (_track != null)
				updateButtons(true);			
		}
		
	}

	static Logger log = Logger.getLogger(TrackLoadEditFrame.class
			.getName());
}
