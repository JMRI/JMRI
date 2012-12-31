// TrackEditFrame.java

package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

import java.awt.*;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Frame for user edit of tracks
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2012
 * @version $Revision$
 */

public class TrackEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	private static boolean loadAndType = false;

	// Managers
	LocationManagerXml managerXml = LocationManagerXml.instance();
	TrainManager trainManager = TrainManager.instance();
	RouteManager routeManager = RouteManager.instance();

	Location _location = null;
	Track _track = null;
	String _type = "";
	JMenu _toolMenu = null;

	List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();

	// panels
	JPanel panelCheckBoxes = new JPanel();
	JScrollPane paneCheckBoxes = new JScrollPane(panelCheckBoxes);
	JPanel panelTrainDir = new JPanel();
	JPanel panelRoadNames = new JPanel();
	JScrollPane paneRoadNames = new JScrollPane(panelRoadNames);
	JPanel panelLoadNames = new JPanel();
	JScrollPane paneLoadNames = new JScrollPane(panelLoadNames);
	JPanel panelOrder = new JPanel();

	// major buttons
	JButton clearButton = new JButton(Bundle.getString("Clear"));
	JButton setButton = new JButton(Bundle.getString("Select"));
	JButton saveTrackButton = new JButton(Bundle.getString("SaveTrack"));
	JButton deleteTrackButton = new JButton(Bundle.getString("DeleteTrack"));
	JButton addTrackButton = new JButton(Bundle.getString("AddTrack"));
	JButton deleteRoadButton = new JButton(Bundle.getString("DeleteRoad"));
	JButton addRoadButton = new JButton(Bundle.getString("AddRoad"));
	JButton addLoadButton = new JButton(Bundle.getString("AddLoad"));
	JButton deleteLoadButton = new JButton(Bundle.getString("DeleteLoad"));
	JButton deleteAllLoadsButton = new JButton(Bundle.getString("DeleteAllLoads"));

	JButton deleteDropButton = new JButton(Bundle.getString("Delete"));
	JButton addDropButton = new JButton(Bundle.getString("Add"));
	JButton deletePickupButton = new JButton(Bundle.getString("Delete"));
	JButton addPickupButton = new JButton(Bundle.getString("Add"));

	// check boxes
	JCheckBox northCheckBox = new JCheckBox(Bundle.getString("North"));
	JCheckBox southCheckBox = new JCheckBox(Bundle.getString("South"));
	JCheckBox eastCheckBox = new JCheckBox(Bundle.getString("East"));
	JCheckBox westCheckBox = new JCheckBox(Bundle.getString("West"));
	JCheckBox loadAndTypeCheckBox = new JCheckBox(Bundle.getString("TypeAndLoad"));

	// radio buttons
	JRadioButton roadNameAll = new JRadioButton(Bundle.getString("AcceptAll"));
	JRadioButton roadNameInclude = new JRadioButton(Bundle.getString("AcceptOnly"));
	JRadioButton roadNameExclude = new JRadioButton(Bundle.getString("Exclude"));

	JRadioButton loadNameAll = new JRadioButton(Bundle.getString("AcceptAll"));
	JRadioButton loadNameInclude = new JRadioButton(Bundle.getString("AcceptOnly"));
	JRadioButton loadNameExclude = new JRadioButton(Bundle.getString("Exclude"));

	// car pick up order controls
	JRadioButton orderNormal = new JRadioButton(Bundle.getString("Normal"));
	JRadioButton orderFIFO = new JRadioButton(Bundle.getString("DescriptiveFIFO"));
	JRadioButton orderLIFO = new JRadioButton(Bundle.getString("DescriptiveLIFO"));

	JRadioButton anyDrops = new JRadioButton(Bundle.getString("Any"));
	JRadioButton trainDrop = new JRadioButton(Bundle.getString("Trains"));
	JRadioButton routeDrop = new JRadioButton(Bundle.getString("Routes"));
	JRadioButton excludeTrainDrop = new JRadioButton(Bundle.getString("ExcludeTrains"));
	JRadioButton excludeRouteDrop = new JRadioButton(Bundle.getString("ExcludeRoutes"));

	JRadioButton anyPickups = new JRadioButton(Bundle.getString("Any"));
	JRadioButton trainPickup = new JRadioButton(Bundle.getString("Trains"));
	JRadioButton routePickup = new JRadioButton(Bundle.getString("Routes"));
	JRadioButton excludeTrainPickup = new JRadioButton(Bundle.getString("ExcludeTrains"));
	JRadioButton excludeRoutePickup = new JRadioButton(Bundle.getString("ExcludeRoutes"));

	JComboBox comboBoxDropTrains = trainManager.getComboBox();
	JComboBox comboBoxDropRoutes = routeManager.getComboBox();
	JComboBox comboBoxPickupTrains = trainManager.getComboBox();
	JComboBox comboBoxPickupRoutes = routeManager.getComboBox();

	// text field
	JTextField trackNameTextField = new JTextField(20);
	JTextField trackLengthTextField = new JTextField(5);

	// text area
	JTextArea commentTextArea = new JTextArea(2, 60);
	JScrollPane commentScroller = new JScrollPane(commentTextArea,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	Dimension minScrollerDim = new Dimension(500, 42);

	// combo box
	JComboBox comboBoxRoads = CarRoads.instance().getComboBox();
	JComboBox comboBoxLoads = CarLoads.instance().getComboBox(null);
	JComboBox comboBoxTypes = CarTypes.instance().getComboBox();

	// optional panel for spurs, staging, and interchanges
	JPanel dropPanel = new JPanel();
	JPanel pickupPanel = new JPanel();
	JPanel panelOpt3 = new JPanel(); // not currently used
	JPanel panelOpt4 = new JPanel();

	public static final String DISPOSE = "dispose"; // NOI18N
	public static final int MAX_NAME_LENGTH = Control.max_len_string_track_name;

	public TrackEditFrame() {
		super();
	}

	public void initComponents(Location location, Track track) {
		_location = location;
		_track = track;

		// property changes
		_location.addPropertyChangeListener(this);
		// listen for car road name and type changes
		CarRoads.instance().addPropertyChangeListener(this);
		CarLoads.instance().addPropertyChangeListener(this);
		CarTypes.instance().addPropertyChangeListener(this);
		trainManager.addPropertyChangeListener(this);
		routeManager.addPropertyChangeListener(this);

		// the following code sets the frame's initial state
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Set up the panels
		// Layout the panel by rows
		// row 1
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
		JScrollPane p1Pane = new JScrollPane(p1);
		p1Pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		p1Pane.setMinimumSize(new Dimension(300, 3 * trackNameTextField.getPreferredSize().height));
		p1Pane.setBorder(BorderFactory.createTitledBorder(""));

		// row 1a
		JPanel pName = new JPanel();
		pName.setLayout(new GridBagLayout());
		pName.setBorder(BorderFactory.createTitledBorder(Bundle.getString("Name")));
		addItem(pName, trackNameTextField, 0, 0);

		// row 1b
		JPanel pLength = new JPanel();
		pLength.setLayout(new GridBagLayout());
		pLength.setBorder(BorderFactory.createTitledBorder(Bundle.getString("Length")));
		pLength.setMinimumSize(new Dimension(60, 1));
		addItem(pLength, trackLengthTextField, 0, 0);

		// row 1c
		panelTrainDir.setLayout(new GridBagLayout());
		panelTrainDir.setBorder(BorderFactory.createTitledBorder(Bundle.getString("TrainTrack")));
		panelTrainDir.setPreferredSize(new Dimension(200, 10));
		addItem(panelTrainDir, northCheckBox, 1, 1);
		addItem(panelTrainDir, southCheckBox, 2, 1);
		addItem(panelTrainDir, eastCheckBox, 3, 1);
		addItem(panelTrainDir, westCheckBox, 4, 1);

		p1.add(pName);
		p1.add(pLength);
		p1.add(panelTrainDir);

		// row 4
		panelCheckBoxes.setLayout(new GridBagLayout());

		// row 5
		panelRoadNames.setLayout(new GridBagLayout());
		paneRoadNames.setBorder(BorderFactory.createTitledBorder(Bundle.getString("RoadsTrack")));
		ButtonGroup roadGroup = new ButtonGroup();
		roadGroup.add(roadNameAll);
		roadGroup.add(roadNameInclude);
		roadGroup.add(roadNameExclude);

		// row 8
		panelLoadNames.setLayout(new GridBagLayout());
		paneLoadNames.setBorder(BorderFactory.createTitledBorder(Bundle.getString("LoadsTrack")));
		ButtonGroup loadGroup = new ButtonGroup();
		loadGroup.add(loadNameAll);
		loadGroup.add(loadNameInclude);
		loadGroup.add(loadNameExclude);

		// row 10
		// order panel
		panelOrder.setLayout(new GridBagLayout());
		panelOrder.setBorder(BorderFactory.createTitledBorder(Bundle.getString("PickupOrder")));
		panelOrder.add(orderNormal);
		panelOrder.add(orderFIFO);
		panelOrder.add(orderLIFO);

		ButtonGroup orderGroup = new ButtonGroup();
		orderGroup.add(orderNormal);
		orderGroup.add(orderFIFO);
		orderGroup.add(orderLIFO);

		// drop panel
		dropPanel.setLayout(new GridBagLayout());
		dropPanel.setBorder(BorderFactory.createTitledBorder(Bundle
				.getString("TrainsOrRoutesDrops")));

		// pickup panel
		pickupPanel.setLayout(new GridBagLayout());
		pickupPanel.setBorder(BorderFactory.createTitledBorder(Bundle
				.getString("TrainsOrRoutesPickups")));

		// row 11
		JPanel panelComment = new JPanel();
		panelComment.setLayout(new GridBagLayout());
		panelComment.setBorder(BorderFactory.createTitledBorder(Bundle.getString("Comment")));
		commentScroller.setMinimumSize(minScrollerDim);
		addItem(panelComment, commentScroller, 0, 0);

		// row 12
		JPanel panelButtons = new JPanel();
		panelButtons.setLayout(new GridBagLayout());

		// row 13
		addItem(panelButtons, deleteTrackButton, 0, 0);
		addItem(panelButtons, addTrackButton, 1, 0);
		addItem(panelButtons, saveTrackButton, 2, 0);

		paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(Bundle.getString("TypesTrack")));

		getContentPane().add(p1Pane);
		getContentPane().add(paneCheckBoxes);
		getContentPane().add(paneRoadNames);
		getContentPane().add(paneLoadNames);
		getContentPane().add(paneLoadNames);
		getContentPane().add(panelOrder);
		getContentPane().add(dropPanel);
		getContentPane().add(pickupPanel);

		// add optional panels
		getContentPane().add(panelOpt3);
		getContentPane().add(panelOpt4);

		getContentPane().add(panelComment);
		getContentPane().add(panelButtons);

		// setup buttons
		addButtonAction(setButton);
		addButtonAction(clearButton);

		addButtonAction(deleteTrackButton);
		addButtonAction(addTrackButton);
		addButtonAction(saveTrackButton);
		addButtonAction(deleteRoadButton);

		addButtonAction(addRoadButton);
		addButtonAction(deleteLoadButton);
		addButtonAction(deleteAllLoadsButton);
		addButtonAction(addLoadButton);

		addButtonAction(deleteDropButton);
		addButtonAction(addDropButton);
		addButtonAction(deletePickupButton);
		addButtonAction(addPickupButton);

		addRadioButtonAction(roadNameAll);
		addRadioButtonAction(roadNameInclude);
		addRadioButtonAction(roadNameExclude);

		addRadioButtonAction(loadNameAll);
		addRadioButtonAction(loadNameInclude);
		addRadioButtonAction(loadNameExclude);

		addRadioButtonAction(orderNormal);
		addRadioButtonAction(orderFIFO);
		addRadioButtonAction(orderLIFO);

		addRadioButtonAction(anyDrops);
		addRadioButtonAction(trainDrop);
		addRadioButtonAction(routeDrop);
		addRadioButtonAction(excludeTrainDrop);
		addRadioButtonAction(excludeRouteDrop);

		addRadioButtonAction(anyPickups);
		addRadioButtonAction(trainPickup);
		addRadioButtonAction(routePickup);
		addRadioButtonAction(excludeTrainPickup);
		addRadioButtonAction(excludeRoutePickup);

		addComboBoxAction(comboBoxTypes);

		// track name for tools menu
		String trackName = null;

		// load fields and enable buttons
		if (_track != null) {
			_track.addPropertyChangeListener(this);
			trackNameTextField.setText(_track.getName());
			commentTextArea.setText(_track.getComment());
			trackLengthTextField.setText(Integer.toString(_track.getLength()));
			enableButtons(true);
			trackName = _track.getName();
		} else {
			enableButtons(false);
		}

		// build menu
		JMenuBar menuBar = new JMenuBar();
		_toolMenu = new JMenu(Bundle.getString("Tools"));
		_toolMenu.add(new ShowCarsByLocationAction(false, location.getName(), trackName));
		_toolMenu.add(new PoolTrackAction(this));
		menuBar.add(_toolMenu);
		setJMenuBar(menuBar);

		// load
		updateCheckboxes();
		updateRoadNames();
		updateTypeComboBoxes();
		updateLoadComboBoxes();
		updateLoadNames();
		updateTrainDir();
		updateCarOrder();
		updateDropOptions();
		updatePickupOptions();

		loadAndTypeCheckBox.setSelected(loadAndType);
	}

	// Save, Delete, Add
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveTrackButton) {
			log.debug("track save button activated");
			if (_track != null) {
				if (!checkUserInputs(_track))
					return;
				saveTrack(_track);
			} else {
				addNewTrack();
			}
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
		if (ae.getSource() == deleteTrackButton) {
			log.debug("track delete button activated");
			if (_track != null) {
				int rs = _track.getNumberRS();
				if (rs > 0) {
					if (JOptionPane.showConfirmDialog(this,
							MessageFormat.format(Bundle.getString("ThereAreCars"),
									new Object[] { Integer.toString(rs) }), Bundle
									.getString("deleteTrack?"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
						return;
					}
				}
				selectCheckboxes(false);
				_location.deleteTrack(_track);
				_track = null;
				enableButtons(false);
				// save location file
				OperationsXml.save();
			}
		}
		if (ae.getSource() == addTrackButton) {
			addNewTrack();
		}
		if (_track == null)
			return;
		if (ae.getSource() == addRoadButton) {
			_track.addRoadName((String) comboBoxRoads.getSelectedItem());
			updateRoadNames();
			selectNextItemComboBox(comboBoxRoads);
		}
		if (ae.getSource() == deleteRoadButton) {
			_track.deleteRoadName((String) comboBoxRoads.getSelectedItem());
			updateRoadNames();
			selectNextItemComboBox(comboBoxRoads);
		}
		if (ae.getSource() == addLoadButton) {
			String loadName = (String) comboBoxLoads.getSelectedItem();
			if (loadAndTypeCheckBox.isSelected())
				loadName = comboBoxTypes.getSelectedItem() + CarLoad.SPLIT_CHAR + loadName;
			if (_track.addLoadName(loadName))
				updateLoadNames();
			selectNextItemComboBox(comboBoxLoads);
		}
		if (ae.getSource() == deleteLoadButton) {
			String loadName = (String) comboBoxLoads.getSelectedItem();
			if (loadAndTypeCheckBox.isSelected())
				loadName = comboBoxTypes.getSelectedItem() + CarLoad.SPLIT_CHAR + loadName;
			if (_track.deleteLoadName(loadName))
				updateLoadNames();
			selectNextItemComboBox(comboBoxLoads);
		}
		if (ae.getSource() == deleteAllLoadsButton) {
			deleteAllLoads();
		}
		if (ae.getSource() == setButton) {
			selectCheckboxes(true);
		}
		if (ae.getSource() == clearButton) {
			selectCheckboxes(false);
		}
		if (ae.getSource() == addDropButton) {
			String id = "";
			if (trainDrop.isSelected() || excludeTrainDrop.isSelected()) {
				if (comboBoxDropTrains.getSelectedItem().equals(""))
					return;
				Train train = ((Train) comboBoxDropTrains.getSelectedItem());
				Route route = train.getRoute();
				id = train.getId();
				if (!checkRoute(route)) {
					JOptionPane.showMessageDialog(this, MessageFormat.format(
							Bundle.getString("TrackNotByTrain"), new Object[] { train.getName() }),
							Bundle.getString("Error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
				selectNextItemComboBox(comboBoxDropTrains);
			} else {
				if (comboBoxDropRoutes.getSelectedItem().equals(""))
					return;
				Route route = ((Route) comboBoxDropRoutes.getSelectedItem());
				id = route.getId();
				if (!checkRoute(route)) {
					JOptionPane.showMessageDialog(this, MessageFormat.format(
							Bundle.getString("TrackNotByRoute"), new Object[] { route.getName() }),
							Bundle.getString("Error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
				selectNextItemComboBox(comboBoxDropRoutes);
			}
			_track.addDropId(id);
			updateDropOptions();
		}
		if (ae.getSource() == deleteDropButton) {
			String id = "";
			if (trainDrop.isSelected() || excludeTrainDrop.isSelected()) {
				if (comboBoxDropTrains.getSelectedItem().equals(""))
					return;
				id = ((Train) comboBoxDropTrains.getSelectedItem()).getId();
				selectNextItemComboBox(comboBoxDropTrains);
			} else {
				if (comboBoxDropRoutes.getSelectedItem().equals(""))
					return;
				id = ((Route) comboBoxDropRoutes.getSelectedItem()).getId();
				selectNextItemComboBox(comboBoxDropRoutes);
			}
			_track.deleteDropId(id);
			updateDropOptions();
		}
		if (ae.getSource() == addPickupButton) {
			String id = "";
			if (trainPickup.isSelected() || excludeTrainPickup.isSelected()) {
				if (comboBoxPickupTrains.getSelectedItem().equals(""))
					return;
				Train train = ((Train) comboBoxPickupTrains.getSelectedItem());
				Route route = train.getRoute();
				id = train.getId();
				if (!checkRoute(route)) {
					JOptionPane.showMessageDialog(this, MessageFormat.format(
							Bundle.getString("TrackNotByTrain"), new Object[] { train.getName() }),
							Bundle.getString("Error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
				selectNextItemComboBox(comboBoxPickupTrains);
			} else {
				if (comboBoxPickupRoutes.getSelectedItem().equals(""))
					return;
				Route route = ((Route) comboBoxPickupRoutes.getSelectedItem());
				id = route.getId();
				if (!checkRoute(route)) {
					JOptionPane.showMessageDialog(this, MessageFormat.format(
							Bundle.getString("TrackNotByRoute"), new Object[] { route.getName() }),
							Bundle.getString("Error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
				selectNextItemComboBox(comboBoxPickupRoutes);
			}
			_track.addPickupId(id);
			updatePickupOptions();
		}
		if (ae.getSource() == deletePickupButton) {
			String id = "";
			if (trainPickup.isSelected() || excludeTrainPickup.isSelected()) {
				if (comboBoxPickupTrains.getSelectedItem().equals(""))
					return;
				id = ((Train) comboBoxPickupTrains.getSelectedItem()).getId();
				selectNextItemComboBox(comboBoxPickupTrains);
			} else {
				if (comboBoxPickupRoutes.getSelectedItem().equals(""))
					return;
				id = ((Route) comboBoxPickupRoutes.getSelectedItem()).getId();
				selectNextItemComboBox(comboBoxPickupRoutes);
			}
			_track.deletePickupId(id);
			updatePickupOptions();
		}
	}

	protected void addNewTrack() {
		// check that track name is valid
		if (!checkName(Bundle.getString("add")))
			return;
		// check to see if track already exists
		Track check = _location.getTrackByName(trackNameTextField.getText(), null);
		if (check != null) {
			reportTrackExists(Bundle.getString("add"));
			return;
		}
		// add track to this location
		_track = _location.addTrack(trackNameTextField.getText(), _type);
		// check track length
		checkLength(_track);

		// reset all of the track's attributes
		updateTrainDir();
		updateCheckboxes();
		updateRoadNames();
		updateLoadNames();
		updateDropOptions();
		updatePickupOptions();

		_track.addPropertyChangeListener(this);

		// setup check boxes
		selectCheckboxes(true);
		// store comment
		_track.setComment(commentTextArea.getText());
		// enable
		enableButtons(true);
		// save location file
		OperationsXml.save();
	}

	// check to see if the route services this location
	private boolean checkRoute(Route route) {
		if (route == null)
			return false;
		RouteLocation rl = null;
		rl = route.getLastLocationByName(_location.getName());
		if (rl == null)
			return false;
		return true;
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
	protected void saveTrack(Track track) {
		// save train directions serviced by this location
		int direction = 0;
		if (northCheckBox.isSelected()) {
			direction += Track.NORTH;
		}
		if (southCheckBox.isSelected()) {
			direction += Track.SOUTH;
		}
		if (eastCheckBox.isSelected()) {
			direction += Track.EAST;
		}
		if (westCheckBox.isSelected()) {
			direction += Track.WEST;
		}
		track.setTrainDirections(direction);
		track.setName(trackNameTextField.getText());

		track.setComment(commentTextArea.getText());

		// save the last state of the "Use car type and load" checkbox
		loadAndType = loadAndTypeCheckBox.isSelected();

		// enable
		enableButtons(true);
		// save location file
		OperationsXml.save();
	}

	private boolean checkUserInputs(Track track) {
		// check that track name is valid
		if (!checkName(Bundle.getString("save")))
			return false;
		// check to see if track already exists
		Track check = _location.getTrackByName(trackNameTextField.getText(), null);
		if (check != null && check != track) {
			reportTrackExists(Bundle.getString("save"));
			return false;
		}
		// check track length
		if (!checkLength(track))
			return false;
		// check trains and route option
		if (!checkService(track))
			return false;

		return true;
	}

	/**
	 * 
	 * @return true if name is less than 26 characters
	 */
	private boolean checkName(String s) {
		if (trackNameTextField.getText().trim().equals("")) {
			log.debug("Must enter a track name");
			JOptionPane.showMessageDialog(this, Bundle.getString("MustEnterName"),
					MessageFormat.format(Bundle.getString("CanNotTrack"), new Object[] { s }),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (trackNameTextField.getText().length() > MAX_NAME_LENGTH) {
			log.error("Track name must be less than " + Integer.toString(MAX_NAME_LENGTH + 1)
					+ " charaters");
			JOptionPane
					.showMessageDialog(this, MessageFormat.format(
							Bundle.getString("TrackNameLengthMax"),
							new Object[] { Integer.toString(MAX_NAME_LENGTH + 1) }), MessageFormat
							.format(Bundle.getString("CanNotTrack"), new Object[] { s }),
							JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private boolean checkLength(Track track) {
		// convert track length if in inches
		String length = trackLengthTextField.getText();
		if (length.endsWith("\"")) { // NOI18N
			length = length.substring(0, length.length() - 1);
			try {
				double inches = Double.parseDouble(length);
				int feet = (int) (inches * Setup.getScaleRatio() / 12);
				length = Integer.toString(feet);
			} catch (NumberFormatException e) {
				log.error("Can not convert from inches to feet");
				JOptionPane.showMessageDialog(this, Bundle.getString("CanNotConvertFeet"),
						Bundle.getString("ErrorTrackLength"), JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		if (length.endsWith("cm")) { // NOI18N
			length = length.substring(0, length.length() - 2);
			try {
				double cm = Double.parseDouble(length);
				int meter = (int) (cm * Setup.getScaleRatio() / 100);
				length = Integer.toString(meter);
			} catch (NumberFormatException e) {
				log.error("Can not convert from cm to meters");
				JOptionPane.showMessageDialog(this, Bundle.getString("CanNotConvertMeter"),
						Bundle.getString("ErrorTrackLength"), JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		// confirm that length is a number and less than 10000 feet
		int trackLength = 0;
		try {
			trackLength = Integer.parseInt(length);
			if (length.length() > Control.max_len_string_track_length_name) {
				JOptionPane.showMessageDialog(this, MessageFormat.format(
						Bundle.getString("TrackMustBeLessThan"),
						new Object[] { Math.pow(10, Control.max_len_string_track_length_name) }),
						Bundle.getString("ErrorTrackLength"), JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} catch (NumberFormatException e) {
			log.error("Track length not an integer");
			JOptionPane.showMessageDialog(this, Bundle.getString("TrackMustBeNumber"),
					Bundle.getString("ErrorTrackLength"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// track length can not be less than than the sum of used and reserved length
		if (trackLength != track.getLength()
				&& trackLength < track.getUsedLength() + track.getReserved()) {
			log.error("Track length can not be less than used and reserved");
			JOptionPane.showMessageDialog(this, Bundle.getString("TrackMustBeGreater"),
					Bundle.getString("ErrorTrackLength"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// if everything is okay, save length
		track.setLength(trackLength);
		return true;
	}

	private boolean checkService(Track track) {
		// check train and route restrictions
		if ((trainDrop.isSelected() || routeDrop.isSelected()) && track.getDropIds().length == 0) {
			log.debug("Must enter trains or routes for this track");
			JOptionPane.showMessageDialog(this, Bundle.getString("UseAddTrainsOrRoutes"),
					Bundle.getString("SetOutDisabled"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if ((trainPickup.isSelected() || routePickup.isSelected())
				&& track.getPickupIds().length == 0) {
			log.debug("Must enter trains or routes for this track");
			JOptionPane.showMessageDialog(this, Bundle.getString("UseAddTrainsOrRoutes"),
					Bundle.getString("PickUpsDisabled"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private void reportTrackExists(String s) {
		log.info("Can not " + s + ", track already exists");
		JOptionPane.showMessageDialog(this, Bundle.getString("TrackAlreadyExists"),
				MessageFormat.format(Bundle.getString("CanNotTrack"), new Object[] { s }),
				JOptionPane.ERROR_MESSAGE);
	}

	protected void enableButtons(boolean enabled) {
		northCheckBox.setEnabled(enabled);
		southCheckBox.setEnabled(enabled);
		eastCheckBox.setEnabled(enabled);
		westCheckBox.setEnabled(enabled);
		clearButton.setEnabled(enabled);
		setButton.setEnabled(enabled);
		deleteTrackButton.setEnabled(enabled);
		saveTrackButton.setEnabled(enabled);
		roadNameAll.setEnabled(enabled);
		roadNameInclude.setEnabled(enabled);
		roadNameExclude.setEnabled(enabled);
		loadNameAll.setEnabled(enabled);
		loadNameInclude.setEnabled(enabled);
		loadNameExclude.setEnabled(enabled);
		loadAndTypeCheckBox.setEnabled(enabled);
		anyDrops.setEnabled(enabled);
		trainDrop.setEnabled(enabled);
		routeDrop.setEnabled(enabled);
		excludeTrainDrop.setEnabled(enabled);
		excludeRouteDrop.setEnabled(enabled);
		anyPickups.setEnabled(enabled);
		trainPickup.setEnabled(enabled);
		routePickup.setEnabled(enabled);
		excludeTrainPickup.setEnabled(enabled);
		excludeRoutePickup.setEnabled(enabled);
		orderNormal.setEnabled(enabled);
		orderFIFO.setEnabled(enabled);
		orderLIFO.setEnabled(enabled);
		enableCheckboxes(enabled);
	}

	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button activated");
		if (ae.getSource() == roadNameAll) {
			_track.setRoadOption(Track.ALLROADS);
			updateRoadNames();
		}
		if (ae.getSource() == roadNameInclude) {
			_track.setRoadOption(Track.INCLUDEROADS);
			updateRoadNames();
		}
		if (ae.getSource() == roadNameExclude) {
			_track.setRoadOption(Track.EXCLUDEROADS);
			updateRoadNames();
		}
		if (ae.getSource() == loadNameAll) {
			_track.setLoadOption(Track.ALLLOADS);
			updateLoadNames();
		}
		if (ae.getSource() == loadNameInclude) {
			_track.setLoadOption(Track.INCLUDELOADS);
			updateLoadNames();
		}
		if (ae.getSource() == loadNameExclude) {
			_track.setLoadOption(Track.EXCLUDELOADS);
			updateLoadNames();
		}
		if (ae.getSource() == orderNormal) {
			_track.setServiceOrder(Track.NORMAL);
		}
		if (ae.getSource() == orderFIFO) {
			_track.setServiceOrder(Track.FIFO);
		}
		if (ae.getSource() == orderLIFO) {
			_track.setServiceOrder(Track.LIFO);
		}
		if (ae.getSource() == anyDrops) {
			_track.setDropOption(Track.ANY);
			updateDropOptions();
		}
		if (ae.getSource() == trainDrop) {
			_track.setDropOption(Track.TRAINS);
			updateDropOptions();
		}
		if (ae.getSource() == routeDrop) {
			_track.setDropOption(Track.ROUTES);
			updateDropOptions();
		}
		if (ae.getSource() == excludeTrainDrop) {
			_track.setDropOption(Track.EXCLUDE_TRAINS);
			updateDropOptions();
		}
		if (ae.getSource() == excludeRouteDrop) {
			_track.setDropOption(Track.EXCLUDE_ROUTES);
			updateDropOptions();
		}
		if (ae.getSource() == anyPickups) {
			_track.setPickupOption(Track.ANY);
			updatePickupOptions();
		}
		if (ae.getSource() == trainPickup) {
			_track.setPickupOption(Track.TRAINS);
			updatePickupOptions();
		}
		if (ae.getSource() == routePickup) {
			_track.setPickupOption(Track.ROUTES);
			updatePickupOptions();
		}
		if (ae.getSource() == excludeTrainPickup) {
			_track.setPickupOption(Track.EXCLUDE_TRAINS);
			updatePickupOptions();
		}
		if (ae.getSource() == excludeRoutePickup) {
			_track.setPickupOption(Track.EXCLUDE_ROUTES);
			updatePickupOptions();
		}
	}

	// TODO only update comboBox when train or route list changes.
	private void updateDropOptions() {
		dropPanel.removeAll();
		int numberOfCheckboxes = getNumberOfCheckboxes();

		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		p.add(anyDrops, 0);
		p.add(trainDrop, 1);
		p.add(routeDrop, 2);
		p.add(excludeTrainDrop);
		p.add(excludeRouteDrop);
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridwidth = numberOfCheckboxes + 1;
		dropPanel.add(p, gc);

		int y = 1; // vertical position in panel

		if (_track != null) {
			// set radio button
			anyDrops.setSelected(_track.getDropOption().equals(Track.ANY));
			trainDrop.setSelected(_track.getDropOption().equals(Track.TRAINS));
			routeDrop.setSelected(_track.getDropOption().equals(Track.ROUTES));
			excludeTrainDrop.setSelected(_track.getDropOption().equals(Track.EXCLUDE_TRAINS));
			excludeRouteDrop.setSelected(_track.getDropOption().equals(Track.EXCLUDE_ROUTES));

			if (!anyDrops.isSelected()) {
				p = new JPanel();
				p.setLayout(new FlowLayout());
				if (trainDrop.isSelected() || excludeTrainDrop.isSelected()) {
					p.add(comboBoxDropTrains);
				} else {
					p.add(comboBoxDropRoutes);
				}
				p.add(addDropButton);
				p.add(deleteDropButton);
				gc.gridy = y++;
				dropPanel.add(p, gc);
				y++;

				String[] dropIds = _track.getDropIds();
				int x = 0;
				for (int i = 0; i < dropIds.length; i++) {
					JLabel names = new JLabel();
					String name = "<deleted>"; // NOI18N
					if (trainDrop.isSelected() || excludeTrainDrop.isSelected()) {
						Train train = trainManager.getTrainById(dropIds[i]);
						if (train != null)
							name = train.getName();
					} else {
						Route route = routeManager.getRouteById(dropIds[i]);
						if (route != null)
							name = route.getName();
					}
					if (name.equals("<deleted>")) // NOI18N
						_track.deleteDropId(dropIds[i]);
					names.setText(name);
					addItem(dropPanel, names, x++, y);
					if (x > numberOfCheckboxes) {
						y++;
						x = 0;
					}
				}
			}
		} else {
			anyDrops.setSelected(true);
		}
		dropPanel.revalidate();
		dropPanel.repaint();
		packFrame();
	}

	private void updatePickupOptions() {
		log.debug("update pick up options");
		pickupPanel.removeAll();
		int numberOfCheckboxes = getNumberOfCheckboxes();

		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		p.add(anyPickups, 0);
		p.add(trainPickup, 1);
		p.add(routePickup, 2);
		p.add(excludeTrainPickup);
		p.add(excludeRoutePickup);
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridwidth = numberOfCheckboxes + 1;
		pickupPanel.add(p, gc);

		int y = 1; // vertical position in panel

		if (_track != null) {
			// set radio button
			anyPickups.setSelected(_track.getPickupOption().equals(Track.ANY));
			trainPickup.setSelected(_track.getPickupOption().equals(Track.TRAINS));
			routePickup.setSelected(_track.getPickupOption().equals(Track.ROUTES));
			excludeTrainPickup.setSelected(_track.getPickupOption().equals(Track.EXCLUDE_TRAINS));
			excludeRoutePickup.setSelected(_track.getPickupOption().equals(Track.EXCLUDE_ROUTES));

			if (!anyPickups.isSelected()) {
				p = new JPanel();
				p.setLayout(new FlowLayout());
				if (trainPickup.isSelected() || excludeTrainPickup.isSelected()) {
					p.add(comboBoxPickupTrains);
				} else {
					p.add(comboBoxPickupRoutes);
				}
				p.add(addPickupButton);
				p.add(deletePickupButton);
				gc.gridy = y++;
				pickupPanel.add(p, gc);
				y++;

				String[] pickupIds = _track.getPickupIds();
				int x = 0;
				for (int i = 0; i < pickupIds.length; i++) {
					JLabel names = new JLabel();
					String name = "<deleted>"; // NOI18N
					if (trainPickup.isSelected() || excludeTrainPickup.isSelected()) {
						Train train = trainManager.getTrainById(pickupIds[i]);
						if (train != null)
							name = train.getName();
					} else {
						Route route = routeManager.getRouteById(pickupIds[i]);
						if (route != null)
							name = route.getName();
					}
					if (name.equals("<deleted>")) // NOI18N
						_track.deletePickupId(pickupIds[i]);
					names.setText(name);
					addItem(pickupPanel, names, x++, y);
					if (x > numberOfCheckboxes) {
						y++;
						x = 0;
					}
				}
			}
		} else {
			anyPickups.setSelected(true);
		}
		pickupPanel.revalidate();
		pickupPanel.repaint();
		packFrame();
	}

	private void updateTrainComboBox() {
		trainManager.updateComboBox(comboBoxPickupTrains);
		trainManager.updateComboBox(comboBoxDropTrains);
	}

	private void updateRouteComboBox() {
		routeManager.updateComboBox(comboBoxPickupRoutes);
		routeManager.updateComboBox(comboBoxDropRoutes);
	}

	// Car type combo box has been changed, show loads associated with this car type
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		updateLoadComboBoxes();
	}

	private void enableCheckboxes(boolean enable) {
		for (int i = 0; i < checkBoxes.size(); i++) {
			JCheckBox checkBox = checkBoxes.get(i);
			checkBox.setEnabled(enable);
		}
	}

	private void selectCheckboxes(boolean enable) {
		for (int i = 0; i < checkBoxes.size(); i++) {
			JCheckBox checkBox = checkBoxes.get(i);
			checkBox.setSelected(enable);
			if (_track != null) {
				// _track.removePropertyChangeListener(this);
				if (enable)
					_track.addTypeName(checkBox.getText());
				else
					_track.deleteTypeName(checkBox.getText());
				// _track.addPropertyChangeListener(this);
			}
		}
	}

	private void updateLoadComboBoxes() {
		String carType = (String) comboBoxTypes.getSelectedItem();
		CarLoads.instance().updateComboBox(carType, comboBoxLoads);
	}

	// car and loco types
	private void updateCheckboxes() {
		checkBoxes.clear();
		panelCheckBoxes.removeAll();
		x = 0;
		y = 0; // vertical position in panel
		loadTypes(CarTypes.instance().getNames());
		loadTypes(EngineTypes.instance().getNames());
		enableCheckboxes(_track != null);
		addItem(panelCheckBoxes, clearButton, 1, ++y);
		addItem(panelCheckBoxes, setButton, 4, y);
		panelCheckBoxes.revalidate();
		packFrame();
	}

	int x = 0;
	int y = 0; // vertical position in panel

	private void loadTypes(String[] types) {
		int numberOfCheckboxes = getNumberOfCheckboxes();
		for (int i = 0; i < types.length; i++) {
			if (_location.acceptsTypeName(types[i])) {
				JCheckBox checkBox = new JCheckBox();
				checkBoxes.add(checkBox);
				checkBox.setText(types[i]);
				addCheckBoxAction(checkBox);
				addItemLeft(panelCheckBoxes, checkBox, x++, y);
				if (_track != null && _track.acceptsTypeName(types[i]))
					checkBox.setSelected(true);
			}
			if (x > numberOfCheckboxes) {
				y++;
				x = 0;
			}
		}
	}

	private void updateRoadNames() {
		panelRoadNames.removeAll();
		int numberOfCheckboxes = getNumberOfCheckboxes();

		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		p.add(roadNameAll, 0);
		p.add(roadNameInclude, 1);
		p.add(roadNameExclude, 2);
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridwidth = numberOfCheckboxes + 1;
		panelRoadNames.add(p, gc);

		int y = 1; // vertical position in panel

		if (_track != null) {
			// set radio button
			roadNameAll.setSelected(_track.getRoadOption().equals(Track.ALLROADS));
			roadNameInclude.setSelected(_track.getRoadOption().equals(Track.INCLUDEROADS));
			roadNameExclude.setSelected(_track.getRoadOption().equals(Track.EXCLUDEROADS));

			if (!roadNameAll.isSelected()) {
				p = new JPanel();
				p.setLayout(new FlowLayout());
				p.add(comboBoxRoads);
				p.add(addRoadButton);
				p.add(deleteRoadButton);
				gc.gridy = y++;
				panelRoadNames.add(p, gc);
				y++;

				String[] carRoads = _track.getRoadNames();
				int x = 0;
				for (int i = 0; i < carRoads.length; i++) {
					JLabel road = new JLabel();
					road.setText(carRoads[i]);
					addItem(panelRoadNames, road, x++, y);
					if (x > numberOfCheckboxes) {
						y++;
						x = 0;
					}
				}
			}
		} else {
			roadNameAll.setSelected(true);
		}
		panelRoadNames.repaint();
		panelRoadNames.validate();
		packFrame();
	}

	private void updateLoadNames() {
		panelLoadNames.removeAll();

		int numberOfCheckboxes = getNumberOfCheckboxes();
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		p.add(loadNameAll, 0);
		p.add(loadNameInclude, 1);
		p.add(loadNameExclude, 2);
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridwidth = numberOfCheckboxes + 1;
		panelLoadNames.add(p, gc);

		int y = 1; // vertical position in panel

		if (_track != null) {
			// set radio button
			loadNameAll.setSelected(_track.getLoadOption().equals(Track.ALLLOADS));
			loadNameInclude.setSelected(_track.getLoadOption().equals(Track.INCLUDEROADS));
			loadNameExclude.setSelected(_track.getLoadOption().equals(Track.EXCLUDEROADS));

			if (!loadNameAll.isSelected()) {
				p = new JPanel();
				p.setLayout(new FlowLayout());
				p.add(comboBoxTypes);
				p.add(comboBoxLoads);
				p.add(addLoadButton);
				p.add(deleteLoadButton);
				p.add(deleteAllLoadsButton);
				p.add(loadAndTypeCheckBox);
				gc.gridy = y++;
				panelLoadNames.add(p, gc);

				String[] carLoads = _track.getLoadNames();
				int x = 0;
				for (int i = 0; i < carLoads.length; i++) {
					JLabel load = new JLabel();
					load.setText(carLoads[i]);
					addItem(panelLoadNames, load, x++, y);
					if (x > numberOfCheckboxes) {
						y++;
						x = 0;
					}
				}
			}
		} else {
			loadNameAll.setSelected(true);
		}
		panelLoadNames.repaint();
		panelLoadNames.validate();
		packFrame();
	}

	private void deleteAllLoads() {
		if (_track != null) {
			String[] trackLoads = _track.getLoadNames();
			for (int i = 0; i < trackLoads.length; i++) {
				_track.deleteLoadName(trackLoads[i]);
			}
		}
		updateLoadNames();
	}

	private void updateTrainDir() {
		northCheckBox.setVisible(((Setup.getTrainDirection() & Setup.NORTH) & (_location
				.getTrainDirections() & Location.NORTH)) > 0);
		southCheckBox.setVisible(((Setup.getTrainDirection() & Setup.SOUTH) & (_location
				.getTrainDirections() & Location.SOUTH)) > 0);
		eastCheckBox.setVisible(((Setup.getTrainDirection() & Setup.EAST) & (_location
				.getTrainDirections() & Location.EAST)) > 0);
		westCheckBox.setVisible(((Setup.getTrainDirection() & Setup.WEST) & (_location
				.getTrainDirections() & Location.WEST)) > 0);

		if (_track != null) {
			northCheckBox.setSelected((_track.getTrainDirections() & Track.NORTH) > 0);
			southCheckBox.setSelected((_track.getTrainDirections() & Track.SOUTH) > 0);
			eastCheckBox.setSelected((_track.getTrainDirections() & Track.EAST) > 0);
			westCheckBox.setSelected((_track.getTrainDirections() & Track.WEST) > 0);
		}
		panelTrainDir.revalidate();
		packFrame();
	}

	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		JCheckBox b = (JCheckBox) ae.getSource();
		log.debug("checkbox change " + b.getText());
		if (_location == null)
			return;
		if (b.isSelected()) {
			_track.addTypeName(b.getText());
		} else {
			_track.deleteTypeName(b.getText());
		}
	}

	private void updateTypeComboBoxes() {
		CarTypes.instance().updateComboBox(comboBoxTypes);
		// remove car types not serviced by this location and track
		for (int i = comboBoxTypes.getItemCount() - 1; i >= 0; i--) {
			String type = (String) comboBoxTypes.getItemAt(i);
			if (_track != null && !_track.acceptsTypeName(type)) {
				comboBoxTypes.removeItem(type);
			}
		}
	}

	private void updateRoadComboBox() {
		CarRoads.instance().updateComboBox(comboBoxRoads);
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue()
					+ " new: " + e.getNewValue());
		if (e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY)) {
			updateCheckboxes();
			updateTypeComboBoxes();
		}
		if (e.getPropertyName().equals(Location.TRAINDIRECTION_CHANGED_PROPERTY)) {
			updateTrainDir();
		}
		if (e.getPropertyName().equals(CarRoads.CARROADS_LENGTH_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Train.STATUS_CHANGED_PROPERTY)) {
			updateRoadComboBox();
			updateRoadNames();
		}
		if (e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY)) {
			updateTypeComboBoxes();
		}
		if (e.getPropertyName().equals(CarLoads.LOAD_NAME_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY)) {
			updateLoadComboBoxes();
			updateLoadNames();
		}
		if (e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY)) {
			updateTrainComboBox();
			updateDropOptions();
			updatePickupOptions();
		}
		if (e.getPropertyName().equals(RouteManager.LISTLENGTH_CHANGED_PROPERTY)) {
			updateRouteComboBox();
			updateDropOptions();
			updatePickupOptions();
		}
	}

	// set the service order
	private void updateCarOrder() {
		orderNormal.setSelected(true);
		if (_track != null) {
			if (_track.getServiceOrder().equals(Track.FIFO))
				orderFIFO.setSelected(true);
			if (_track.getServiceOrder().equals(Track.LIFO))
				orderLIFO.setSelected(true);
		}
	}

	public void dispose() {
		if (_track != null)
			_track.removePropertyChangeListener(this);
		_location.removePropertyChangeListener(this);
		CarRoads.instance().removePropertyChangeListener(this);
		CarLoads.instance().removePropertyChangeListener(this);
		CarTypes.instance().removePropertyChangeListener(this);
		ScheduleManager.instance().removePropertyChangeListener(this);
		trainManager.removePropertyChangeListener(this);
		routeManager.removePropertyChangeListener(this);
		super.dispose();
	}

	protected void packFrame() {
		validate();
		pack();
		// make some room so rolling stock type scroll window doesn't always appear
		if (getWidth() < 750)
			setSize(750, getHeight());
		if (getHeight() < Control.panelHeight)
			setSize(getWidth(), Control.panelHeight);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrackEditFrame.class
			.getName());
}
