// TrackEditFrame.java

package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.OperationsFrame;
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
import javax.swing.border.Border;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Frame for user edit of tracks
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.9 $
 */

public class TrackEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	LocationManager manager;
	LocationManagerXml managerXml;

	Location _location = null;
	Track _track = null;
	String _type = "";
	List checkBoxes = new ArrayList();
	JPanel panelCheckBoxes = new JPanel();
	JPanel panelTrainDir = new JPanel();
	JPanel panelRoadNames = new JPanel();
	JPanel panelDropOptions = new JPanel();
	JPanel panelPickupOptions = new JPanel();
	
	// labels
	javax.swing.JLabel textName = new javax.swing.JLabel();
	javax.swing.JLabel textLength = new javax.swing.JLabel();
	javax.swing.JLabel textTrain = new javax.swing.JLabel();
	javax.swing.JLabel textType = new javax.swing.JLabel();
	javax.swing.JLabel textRoad = new javax.swing.JLabel();
	javax.swing.JLabel textDrops = new javax.swing.JLabel();
	javax.swing.JLabel textPickups = new javax.swing.JLabel();
	javax.swing.JLabel textOptional = new javax.swing.JLabel();
	javax.swing.JLabel textComment = new javax.swing.JLabel();

	// major buttons
	javax.swing.JButton clearButton = new javax.swing.JButton();
	javax.swing.JButton setButton = new javax.swing.JButton();
	javax.swing.JButton saveTrackButton = new javax.swing.JButton();
	javax.swing.JButton deleteTrackButton = new javax.swing.JButton();
	javax.swing.JButton addTrackButton = new javax.swing.JButton();
	javax.swing.JButton deleteRoadButton = new javax.swing.JButton();
	javax.swing.JButton addRoadButton = new javax.swing.JButton();
	javax.swing.JButton deleteDropButton = new javax.swing.JButton();
	javax.swing.JButton addDropButton = new javax.swing.JButton();
	javax.swing.JButton deletePickupButton = new javax.swing.JButton();
	javax.swing.JButton addPickupButton = new javax.swing.JButton();
	
	// check boxes
	javax.swing.JCheckBox checkBox;
	javax.swing.JCheckBox northCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox southCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox eastCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox westCheckBox = new javax.swing.JCheckBox();
	
	// radio buttons
    javax.swing.JRadioButton roadNameAll = new javax.swing.JRadioButton("Accept all");
    javax.swing.JRadioButton roadNameInclude = new javax.swing.JRadioButton("Accept only");
    javax.swing.JRadioButton roadNameExclude = new javax.swing.JRadioButton("Exclude");
    ButtonGroup roadGroup = new ButtonGroup();
    javax.swing.JRadioButton anyDrops = new javax.swing.JRadioButton("Any");
    javax.swing.JRadioButton trainDrop = new javax.swing.JRadioButton("Trains");
    javax.swing.JRadioButton routeDrop = new javax.swing.JRadioButton("Routes");
    ButtonGroup dropGroup = new ButtonGroup();
    javax.swing.JRadioButton anyPickups = new javax.swing.JRadioButton("Any");
    javax.swing.JRadioButton trainPickup = new javax.swing.JRadioButton("Trains");
    javax.swing.JRadioButton routePickup = new javax.swing.JRadioButton("Routes");
    ButtonGroup pickupGroup = new ButtonGroup();
    
	// text field
	javax.swing.JTextField trackNameTextField = new javax.swing.JTextField(20);
	javax.swing.JTextField trackLengthTextField = new javax.swing.JTextField(5);
	javax.swing.JTextField commentTextField = new javax.swing.JTextField(35);

	// for padding out panel
	javax.swing.JLabel space1 = new javax.swing.JLabel();
	javax.swing.JLabel space2 = new javax.swing.JLabel();
	javax.swing.JLabel space3 = new javax.swing.JLabel();
	
	// combo box
	javax.swing.JComboBox comboBoxRoads = CarRoads.instance().getComboBox();
	javax.swing.JComboBox comboBoxDropTrains = TrainManager.instance().getComboBox();
	javax.swing.JComboBox comboBoxDropRoutes = RouteManager.instance().getComboBox();
	javax.swing.JComboBox comboBoxPickupTrains = TrainManager.instance().getComboBox();
	javax.swing.JComboBox comboBoxPickupRoutes = RouteManager.instance().getComboBox();

	public static final String DISPOSE = "dispose" ;
	public static final int MAX_NAME_LENGTH = 25;

	public TrackEditFrame() {
		super();
	}

	public void initComponents(Location location, Track track) {
		_location = location;
		_location.addPropertyChangeListener(this);
		_track = track;
		CarRoads.instance().addPropertyChangeListener(this);

		// load managers
		managerXml = LocationManagerXml.instance();

		// the following code sets the frame's initial state
		
		textName.setText(rb.getString("Name"));
		textName.setVisible(true);

		textLength.setText(rb.getString("Length"));
		textLength.setVisible(true);
		textTrain.setText(rb.getString("TrainTrack"));
		textTrain.setVisible(true);
		northCheckBox.setText(rb.getString("North"));
		southCheckBox.setText(rb.getString("South"));
		eastCheckBox.setText(rb.getString("East"));
		westCheckBox.setText(rb.getString("West"));
		textType.setText(rb.getString("TypesTrack"));
		textType.setVisible(true);
		textRoad.setText(rb.getString("RoadsTrack"));
		textRoad.setVisible(true);
		textDrops.setText(rb.getString("TrainsOrRoutesDrops"));
		textDrops.setVisible(true);
		textPickups.setText(rb.getString("TrainsOrRoutesPickups"));
		textPickups.setVisible(true);
		
		textOptional.setText("-------------------------------- Optional ------------------------------------");
		textOptional.setVisible(true);
		textComment.setText(rb.getString("Comment"));
		textComment.setVisible(true);
		space1.setText("     ");
		space1.setVisible(true);
		space2.setText("     ");
		space2.setVisible(true);

		clearButton.setText(rb.getString("Clear"));
		clearButton.setVisible(true);
		setButton.setText(rb.getString("Select"));
		setButton.setVisible(true);

		deleteTrackButton.setText(rb.getString("DeleteTrack"));
		deleteTrackButton.setVisible(true);
		addTrackButton.setText(rb.getString("AddTrack"));
		addTrackButton.setVisible(true);
		saveTrackButton.setText(rb.getString("SaveTrack"));
		saveTrackButton.setVisible(true);
		addRoadButton.setText(rb.getString("AddRoad"));
		addRoadButton.setVisible(true);
		deleteRoadButton.setText(rb.getString("DeleteRoad"));
		deleteRoadButton.setVisible(true);
		addDropButton.setText(rb.getString("Add"));
		addDropButton.setVisible(true);
		deleteDropButton.setText(rb.getString("Delete"));
		deleteDropButton.setVisible(true);
		addPickupButton.setText(rb.getString("Add"));
		addPickupButton.setVisible(true);
		deletePickupButton.setText(rb.getString("Delete"));
		deletePickupButton.setVisible(true);
		
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

	    //      Set up the panels
    	JPanel p1 = new JPanel();
    	p1.setLayout(new GridBagLayout());
				
		// Layout the panel by rows
		// row 1
		addItem(p1, textName, 0, 1);
		addItemWidth(p1, trackNameTextField, 3, 1, 1);

		// row 2
		addItem(p1, textLength, 0, 2);
		addItemLeft(p1, trackLengthTextField, 1, 2);
		
		// row 3
		panelTrainDir.setLayout(new GridBagLayout());
		updateTrainDir();
		
		// row 4
	   	panelCheckBoxes.setLayout(new GridBagLayout());
		updateCheckboxes();
		
		// row 5
		panelRoadNames.setLayout(new GridBagLayout());
		roadGroup.add(roadNameAll);
		roadGroup.add(roadNameInclude);
		roadGroup.add(roadNameExclude);
		updateRoadNames();
		
		// row 6
		panelDropOptions.setLayout(new GridBagLayout());
		dropGroup.add(anyDrops);
		dropGroup.add(trainDrop);
		dropGroup.add(routeDrop);
		updateDropOptions();
		
		// row 7
		panelPickupOptions.setLayout(new GridBagLayout());
		pickupGroup.add(anyPickups);
		pickupGroup.add(trainPickup);
		pickupGroup.add(routePickup);
		updatePickupOptions();
 		
    	JPanel p4 = new JPanel();
    	p4.setLayout(new GridBagLayout());
		int y = 0;
		
		// row 10
		addItem (p4, space1, 0, ++y);
    	
		// row 11
		addItem(p4, textComment, 0, ++y);
		addItemWidth(p4, commentTextField, 3, 1, y);
				
		// row 12
		addItem(p4, space2, 0, ++y);
		// row 13
		addItem(p4, deleteTrackButton, 0, ++y);
		addItem(p4, addTrackButton, 1, y);
		addItem(p4, saveTrackButton, 3, y);
		
		getContentPane().add(p1);
		getContentPane().add(panelTrainDir);
		getContentPane().add(panelCheckBoxes);
		getContentPane().add(panelRoadNames);
		// Only interchange tracks can control drops and pickups
		if (_type.equals(Track.INTERCHANGE)){
			getContentPane().add(panelDropOptions);
			getContentPane().add(panelPickupOptions);
		}
       	getContentPane().add(p4);
		
		// setup buttons
		addButtonAction(setButton);
		addButtonAction(clearButton);
		addButtonAction(deleteTrackButton);
		addButtonAction(addTrackButton);
		addButtonAction(saveTrackButton);
		addButtonAction(deleteRoadButton);
		addButtonAction(addRoadButton);
		addButtonAction(deleteDropButton);
		addButtonAction(addDropButton);
		addButtonAction(deletePickupButton);
		addButtonAction(addPickupButton);
		
		addRadioButtonAction(roadNameAll);
		addRadioButtonAction(roadNameInclude);
		addRadioButtonAction(roadNameExclude);
		addRadioButtonAction(anyDrops);
		addRadioButtonAction(trainDrop);
		addRadioButtonAction(routeDrop);
		addRadioButtonAction(anyPickups);
		addRadioButtonAction(trainPickup);
		addRadioButtonAction(routePickup);
		
		// load fields and enable buttons
		if (_track !=null){
			trackNameTextField.setText(_track.getName());
			commentTextField.setText(_track.getComment());
			trackLengthTextField.setText(Integer.toString(_track.getLength()));
			enableButtons(true);
		} else {
			enableButtons(false);
		}
		
		// set frame size and location for display
		pack();
		setSize(getWidth(), getHeight()+50); // add some room for menu
		setLocation(500, 200);
		setVisible(true);	
	}
	
	// Save, Delete, Add 
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveTrackButton){
			log.debug("track save button actived");
			if (_track != null){
				saveTrack(_track);
			} else {
				addNewTrack();
			}
		}
		if (ae.getSource() == deleteTrackButton){
			log.debug("track delete button actived");
//			Track y = _location.getTrackByName(trackNameTextField.getText());
			if (_track != null){
				int rs = _track.getNumberRS();
				if (rs > 0){
					if (JOptionPane.showConfirmDialog(this,
							MessageFormat.format(rb.getString("ThereAreCars"),new Object[]{Integer.toString(rs)}), rb.getString("deleteTrack?"),
							JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
						return;
					}
				}
				selectCheckboxes(false);
				_location.deleteTrack(_track);
				_track = null;
				enableButtons(false);
				// save location file
				managerXml.writeOperationsLocationFile();
			}
		}
		if (ae.getSource() == addTrackButton){
			addNewTrack();
		}
		if (ae.getSource() == addRoadButton){
			_track.addRoadName((String) comboBoxRoads.getSelectedItem());
			updateRoadNames();
		}
		if (ae.getSource() == deleteRoadButton){
			_track.deleteRoadName((String) comboBoxRoads.getSelectedItem());
			updateRoadNames();
		}
		if (ae.getSource() == setButton){
			selectCheckboxes(true);
		}
		if (ae.getSource() == clearButton){
			selectCheckboxes(false);
		}
		if (ae.getSource() == addDropButton){
			String id ="";
			if (trainDrop.isSelected()){
				if (comboBoxDropTrains.getSelectedItem().equals(""))
					return;
				Train train = ((Train) comboBoxDropTrains.getSelectedItem());
				Route route = train.getRoute();
				id = train.getId();
				if (!checkRoute(route)){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("TrackNotByTrain"),new Object[]{train.getName()}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			} else {
				if (comboBoxDropRoutes.getSelectedItem().equals(""))
					return;
				Route route = ((Route) comboBoxDropRoutes.getSelectedItem());
				id = route.getId();
				if (!checkRoute(route)){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("TrackNotByRoute"),new Object[]{route.getName()}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			_track.addDropId(id);
			updateDropOptions();
		}
		if (ae.getSource() == deleteDropButton){
			String id ="";
			if (trainDrop.isSelected()){
				if (comboBoxDropTrains.getSelectedItem().equals(""))
					return;
				id = ((Train) comboBoxDropTrains.getSelectedItem()).getId();
			} else{
				if (comboBoxDropRoutes.getSelectedItem().equals(""))
					return;
				id = ((Route) comboBoxDropRoutes.getSelectedItem()).getId();
			}
			_track.deleteDropId(id);
			updateDropOptions();
		}
		if (ae.getSource() == addPickupButton){
			String id ="";
			if (trainPickup.isSelected()){
				if (comboBoxPickupTrains.getSelectedItem().equals(""))
					return;
				Train train = ((Train) comboBoxPickupTrains.getSelectedItem());
				Route route = train.getRoute();
				id = train.getId();
				if (!checkRoute(route)){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("TrackNotByTrain"),new Object[]{train.getName()}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			} else{
				if (comboBoxPickupRoutes.getSelectedItem().equals(""))
					return;
				Route route = ((Route) comboBoxPickupRoutes.getSelectedItem());
				id = route.getId();
				if (!checkRoute(route)){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("TrackNotByRoute"),new Object[]{route.getName()}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			_track.addPickupId(id);
			updatePickupOptions();
		}
		if (ae.getSource() == deletePickupButton){
			String id ="";
			if (trainPickup.isSelected()){
				if (comboBoxPickupTrains.getSelectedItem().equals(""))
					return;
				id = ((Train) comboBoxPickupTrains.getSelectedItem()).getId();
			} else{
				if (comboBoxPickupRoutes.getSelectedItem().equals(""))
					return;
				id = ((Route) comboBoxPickupRoutes.getSelectedItem()).getId();
			}
			_track.deletePickupId(id);
			updatePickupOptions();
		}
	}
	
	// check to see if the route services this location
	private boolean checkRoute (Route route){
		if (route == null)
			return false;
		RouteLocation rl = null;
		rl = route.getLocationByName(_location.getName());
		if (rl == null)
			return false;
		return true;
	}

	private void addNewTrack(){
		// check that track name is valid
		if (!checkName())
			return;
		// check to see if track already exsists
		Track check = _location.getTrackByName(trackNameTextField.getText(), _type);
		if (check != null){
			reportTrackExists(rb.getString("add"));
			return;
		}
		// add track to this location
		_track =_location.addTrack(trackNameTextField.getText(), _type);
		// check track length
		checkLength(_track);
		// copy checkboxes
		copyCheckboxes();
		// store comment
		_track.setComment(commentTextField.getText());
		// enable 
		enableButtons(true);
		// setup checkboxes
		selectCheckboxes(true);
		updateTrainDir();
		// save location file
		managerXml.writeOperationsLocationFile();
	}

	private void saveTrack (Track track){
		// check that track name is valid
		if (!checkName())
			return;
		// check to see if track already exsists
		Track check = _location.getTrackByName(trackNameTextField.getText(), _type);
		if (check != null && check != track){
			reportTrackExists(rb.getString("save"));
			return;
		}
		// check track length
		if (!checkLength(track))
			return;
		// save train directions serviced by this location
		int direction = 0;
		if (northCheckBox.isSelected()){
			direction += track.NORTH;
		}
		if (southCheckBox.isSelected()){
			direction += track.SOUTH;
		}
		if (eastCheckBox.isSelected()){
			direction += track.EAST;
		}
		if (westCheckBox.isSelected()){
			direction += track.WEST;
		}
		track.setTrainDirections(direction);
		track.setName(trackNameTextField.getText());
		track.setComment(commentTextField.getText());
		// enable 
		enableButtons(true);
		// save location file
		managerXml.writeOperationsLocationFile();
	}


	/**
	 * 
	 * @return true if name is less than 26 characters
	 */
	private boolean checkName(){
		if (trackNameTextField.getText().length() > MAX_NAME_LENGTH){
			log.error("Track name must be less than "+ Integer.toString(MAX_NAME_LENGTH+1) +" charaters");
			JOptionPane.showMessageDialog(this,
					MessageFormat.format(rb.getString("TrackNameLengthMax"),new Object[]{Integer.toString(MAX_NAME_LENGTH+1)}),
					rb.getString("canNotAddTrack"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private boolean checkLength (Track track){	
		// convert track length if in inches
		String length = trackLengthTextField.getText();
		if (length.endsWith("\"")){
			length = length.substring(0, length.length()-1);
			try {
				double inches = Double.parseDouble(length);
				int feet = (int)(inches * Setup.getScaleRatio() / 12);
				length = Integer.toString(feet);
			} catch (NumberFormatException e){
				log.error("Can not convert from inches to feet");
				JOptionPane.showMessageDialog(this,
						rb.getString("CanNotConvertFeet"), rb.getString("ErrorTrackLength"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		if (length.endsWith("cm")){
			length = length.substring(0, length.length()-2);
			try {
				double cm = Double.parseDouble(length);
				int meter = (int)(cm * Setup.getScaleRatio() / 100);
				length = Integer.toString(meter);
			} catch (NumberFormatException e){
				log.error("Can not convert from cm to meters");
				JOptionPane.showMessageDialog(this,
						rb.getString("CanNotConvertMeter"), rb.getString("ErrorTrackLength"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		// confirm that length is a number and less than 10000 feet
		int trackLength = 0;
		try {
			trackLength = Integer.parseInt(length);
			if (trackLength > 99999){
				log.error("Track length must be less than 100,000 feet");
				JOptionPane.showMessageDialog(this,
						rb.getString("TrackMustBeLessThan"), rb.getString("ErrorTrackLength"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} catch (NumberFormatException e){
			log.error("Track length not an integer");
			JOptionPane.showMessageDialog(this,
					rb.getString("TrackMustBeNumber"), rb.getString("ErrorTrackLength"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// track length can not be less than than the sum of used and reserved length
		if (trackLength < track.getUsedLength() + track.getReserved()){
			log.error("Track length can not be less than used and reserved");
			JOptionPane.showMessageDialog(this,
					rb.getString("TrackMustBeGreater"), rb.getString("ErrorTrackLength"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// if everything is okay, save length
		track.setLength(trackLength);
		return true;
	}
	
	private void reportTrackExists(String s){
		log.info("Can not " + s + ", track already exists");
		JOptionPane.showMessageDialog(this,
				rb.getString("TrackAlreadyExists"), MessageFormat.format(rb.getString("CanNotTrack"),new Object[]{s }),
				JOptionPane.ERROR_MESSAGE);
	}

	private void enableButtons(boolean enabled){
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
		anyDrops.setEnabled(enabled);
		trainDrop.setEnabled(enabled);
		routeDrop.setEnabled(enabled);
		anyPickups.setEnabled(enabled);
		trainPickup.setEnabled(enabled);
		routePickup.setEnabled(enabled);
		enableCheckboxes(enabled);
	}
	
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button activated");
		if (ae.getSource() == roadNameAll){
			_track.setRoadOption(_track.ALLROADS);
			updateRoadNames();
		}
		if (ae.getSource() == roadNameInclude){
			_track.setRoadOption(_track.INCLUDEROADS);
			updateRoadNames();
		}
		if (ae.getSource() == roadNameExclude){
			_track.setRoadOption(_track.EXCLUDEROADS);
			updateRoadNames();
		}
		if (ae.getSource() == anyDrops){
			_track.setDropOption(_track.ANY);
			updateDropOptions();
		}
		if (ae.getSource() == trainDrop){
			_track.setDropOption(_track.TRAINS);
			updateDropOptions();
		}
		if (ae.getSource() == routeDrop){
			_track.setDropOption(_track.ROUTES);
			updateDropOptions();
		}
		if (ae.getSource() == anyPickups){
			_track.setPickupOption(_track.ANY);
			updatePickupOptions();
		}
		if (ae.getSource() == trainPickup){
			_track.setPickupOption(_track.TRAINS);
			updatePickupOptions();
		}
		if (ae.getSource() == routePickup){
			_track.setPickupOption(_track.ROUTES);
			updatePickupOptions();
		}
	}

	private void enableCheckboxes(boolean enable){
		for (int i=0; i < checkBoxes.size(); i++){
			checkBox = (JCheckBox)checkBoxes.get(i);
			checkBox.setEnabled(enable);
		}
	}
	
	private void selectCheckboxes(boolean enable){
		for (int i=0; i < checkBoxes.size(); i++){
			checkBox = (JCheckBox)checkBoxes.get(i);
			checkBox.setSelected(enable);
			if(_track != null){
				if (enable)
					_track.addTypeName(checkBox.getText());
				else
					_track.deleteTypeName(checkBox.getText());
			}
		}
	}
	
	private void copyCheckboxes(){
		for (int i=0; i < checkBoxes.size(); i++){
			checkBox = (JCheckBox)checkBoxes.get(i);
			if (checkBox.isSelected() && _track != null)
				_track.addTypeName(checkBox.getText());
			else
				_track.deleteTypeName(checkBox.getText());
		}
	}
	
	private void updateCheckboxes(){
		checkBoxes.clear();
		panelCheckBoxes.removeAll();
		x = 0;
		y = 0;		// vertical position in panel
		addItemWidth(panelCheckBoxes, textType, 3, 1, y++);
		loadTypes(CarTypes.instance().getNames());
		loadTypes(EngineTypes.instance().getNames());
		enableCheckboxes(_track != null);
		addItem (panelCheckBoxes, clearButton, 1, ++y);
		addItem (panelCheckBoxes, setButton, 4, y);
		Border border = BorderFactory.createEtchedBorder();
		panelCheckBoxes.setBorder(border);
		panelCheckBoxes.revalidate();
		pack();
		repaint();
	}
	
	int x = 0;
	int y = 0;	// vertical position in panel
	private void loadTypes(String[] types){
		for (int i =0; i<types.length; i++){
			if(_location.acceptsTypeName(types[i])){
				JCheckBox checkBox = new javax.swing.JCheckBox();
				checkBoxes.add(checkBox);
				checkBox.setText(types[i]);
				addCheckBoxAction(checkBox);
				addItemLeft(panelCheckBoxes, checkBox, x++, y);
				if(_track != null && _track.acceptsTypeName(types[i]))
					checkBox.setSelected(true);
			} 
			if (x > 5){
				y++;
				x = 0;
			}
		}
	}
	
	private void updateDropOptions(){
		panelDropOptions.removeAll();
		
    	JPanel p = new JPanel();
    	p.setLayout(new GridBagLayout());
    	p.add(textDrops, 0);
    	p.add(anyDrops, 1);
    	p.add(trainDrop, 2);
    	p.add(routeDrop, 3);
    	GridBagConstraints gc = new GridBagConstraints();
    	gc.gridwidth = 6;
    	panelDropOptions.add(p, gc);
		
		int y = 1;		// vertical position in panel

		if(_track != null){
			// set radio button
			anyDrops.setSelected(_track.getDropOption().equals(_track.ANY));
			trainDrop.setSelected(_track.getDropOption().equals(_track.TRAINS));
			routeDrop.setSelected(_track.getDropOption().equals(_track.ROUTES));
			
			if (!anyDrops.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	if (trainDrop.isSelected()){
		    		TrainManager.instance().updateComboBox(comboBoxDropTrains);
		    		p.add(comboBoxDropTrains);
		    	} else {
		    		RouteManager.instance().updateComboBox(comboBoxDropRoutes);
		    		p.add(comboBoxDropRoutes);
		    	}
		    	p.add(addDropButton);
		    	p.add(deleteDropButton);
				gc.gridy = y++;
				panelDropOptions.add(p, gc);
		    	y++;

		    	String[]dropIds = _track.getDropIds();
		    	int x = 0;
		    	String name;
		    	for (int i =0; i<dropIds.length; i++){
		    		JLabel names = new javax.swing.JLabel();
		    		name = "";
		    		if (trainDrop.isSelected()){
		    			Train train = TrainManager.instance().getTrainById(dropIds[i]);
		    			if(train != null)
		    				name = train.getName();
		    		} else {
		    			Route route = RouteManager.instance().getRouteById(dropIds[i]);
		    			if(route != null)
		    				name = route.getName();
		    		}
		    		names.setText(name);
		    		addItem(panelDropOptions, names, x++, y);
		    		if (x > 5){
		    			y++;
		    			x = 0;
		    		}
		    	}
			}
		} else {
			anyDrops.setSelected(true);
		}

		Border border = BorderFactory.createEtchedBorder();
		panelDropOptions.setBorder(border);
		panelDropOptions.revalidate();

		pack();
		repaint();
	}
	
	private void updatePickupOptions(){
		panelPickupOptions.removeAll();
		
    	JPanel p = new JPanel();
    	p.setLayout(new GridBagLayout());
    	p.add(textPickups, 0);
    	p.add(anyPickups, 1);
    	p.add(trainPickup, 2);
    	p.add(routePickup, 3);
    	GridBagConstraints gc = new GridBagConstraints();
    	gc.gridwidth = 6;
    	panelPickupOptions.add(p, gc);
		
		int y = 1;		// vertical position in panel

		if(_track != null){
			// set radio button
			anyPickups.setSelected(_track.getPickupOption().equals(_track.ANY));
			trainPickup.setSelected(_track.getPickupOption().equals(_track.TRAINS));
			routePickup.setSelected(_track.getPickupOption().equals(_track.ROUTES));
			
			if (!anyPickups.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	if (trainPickup.isSelected()){
		    		TrainManager.instance().updateComboBox(comboBoxPickupTrains);
		    		p.add(comboBoxPickupTrains);
		    	} else {
		    		RouteManager.instance().updateComboBox(comboBoxPickupRoutes);
		    		p.add(comboBoxPickupRoutes);
		    	}
		    	p.add(addPickupButton);
		    	p.add(deletePickupButton);
				gc.gridy = y++;
				panelPickupOptions.add(p, gc);
		    	y++;

		    	String[]pickupIds = _track.getPickupIds();
		    	int x = 0;
		    	String name;
		    	for (int i =0; i<pickupIds.length; i++){
		    		JLabel names = new javax.swing.JLabel();
		    		name = "";
		    		if (trainPickup.isSelected()){
		    			Train train = TrainManager.instance().getTrainById(pickupIds[i]);
		    			if(train != null)
		    				name = train.getName();
		    		} else {
		    			Route route = RouteManager.instance().getRouteById(pickupIds[i]);
		    			if(route != null)
		    				name = route.getName();
		    		}
		    		names.setText(name);
		    		addItem(panelPickupOptions, names, x++, y);
		    		if (x > 5){
		    			y++;
		    			x = 0;
		    		}
		    	}
			}
		} else {
			anyPickups.setSelected(true);
		}

		Border border = BorderFactory.createEtchedBorder();
		panelPickupOptions.setBorder(border);
		panelPickupOptions.revalidate();

		pack();
		repaint();
	}
	
	private void updateRoadNames(){
		panelRoadNames.removeAll();
		
    	JPanel p = new JPanel();
    	p.setLayout(new GridBagLayout());
    	p.add(textRoad, 0);
    	p.add(roadNameAll, 1);
    	p.add(roadNameInclude, 2);
    	p.add(roadNameExclude, 3);
    	GridBagConstraints gc = new GridBagConstraints();
    	gc.gridwidth = 6;
    	panelRoadNames.add(p, gc);
		
		int y = 1;		// vertical position in panel

		if(_track != null){
			// set radio button
			roadNameAll.setSelected(_track.getRoadOption().equals(_track.ALLROADS));
			roadNameInclude.setSelected(_track.getRoadOption().equals(_track.INCLUDEROADS));
			roadNameExclude.setSelected(_track.getRoadOption().equals(_track.EXCLUDEROADS));
			
			if (!roadNameAll.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	p.add(comboBoxRoads);
		    	p.add(addRoadButton);
		    	p.add(deleteRoadButton);
				gc.gridy = y++;
		    	panelRoadNames.add(p, gc);
		    	y++;

		    	String[]carRoads = _track.getRoadNames();
		    	int x = 0;
		    	for (int i =0; i<carRoads.length; i++){
		    		JLabel road = new javax.swing.JLabel();
		    		road.setText(carRoads[i]);
		    		addItem(panelRoadNames, road, x++, y);
		    		if (x > 5){
		    			y++;
		    			x = 0;
		    		}
		    	}
			}
		} else {
			roadNameAll.setSelected(true);
		}

		Border border = BorderFactory.createEtchedBorder();
		panelRoadNames.setBorder(border);
		panelRoadNames.revalidate();

		pack();
		repaint();
	}
	
	private void updateTrainDir(){
		panelTrainDir.removeAll();
		
		addItem(panelTrainDir, textTrain, 0, 1);
		addItemLeft(panelTrainDir, northCheckBox, 1, 1);
		addItemLeft(panelTrainDir, southCheckBox, 2, 1);
		addItemLeft(panelTrainDir, eastCheckBox, 3, 1);
		addItemLeft(panelTrainDir, westCheckBox, 4, 1);
		Border border = BorderFactory.createEtchedBorder();
		panelTrainDir.setBorder(border);
		
		northCheckBox.setVisible(((Setup.getTrainDirection() & Setup.NORTH) & (_location.getTrainDirections() & _location.NORTH))>0);
		southCheckBox.setVisible(((Setup.getTrainDirection() & Setup.SOUTH) & (_location.getTrainDirections() & _location.SOUTH))>0);
		eastCheckBox.setVisible(((Setup.getTrainDirection() & Setup.EAST) & (_location.getTrainDirections() & _location.EAST))>0);
		westCheckBox.setVisible(((Setup.getTrainDirection() & Setup.WEST) & (_location.getTrainDirections() & _location.WEST))>0);
		
		if (_track != null){
			northCheckBox.setSelected((_track.getTrainDirections() & _track.NORTH) > 0);
			southCheckBox.setSelected((_track.getTrainDirections() & _track.SOUTH) > 0);
			eastCheckBox.setSelected((_track.getTrainDirections() & _track.EAST) > 0);
			westCheckBox.setSelected((_track.getTrainDirections() & _track.WEST) > 0);
		}
		
		panelTrainDir.revalidate();

		pack();
		repaint();
	}

	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		JCheckBox b =  (JCheckBox)ae.getSource();
		log.debug("checkbox change "+ b.getText());
		if (_location == null)
			return;
		if (b.isSelected()){
			_track.addTypeName(b.getText());
		}else{
			_track.deleteTypeName(b.getText());
		}
	}
	
	private void updateRoadComboBox(){
		CarRoads.instance().updateComboBox(comboBoxRoads);
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled()) 
			log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY)){
			updateCheckboxes();
		}
		if (e.getPropertyName().equals(Location.TRAINDIRECTION_CHANGED_PROPERTY)){
			updateTrainDir();
		}
		if (e.getPropertyName().equals(CarRoads.CARROADS_CHANGED_PROPERTY) || e.getPropertyName().equals(Train.STATUS_CHANGED_PROPERTY)){
			updateRoadComboBox();
			updateRoadNames();
		}
	}
	
    public void dispose() {
    	_location.removePropertyChangeListener(this);
    	CarRoads.instance().removePropertyChangeListener(this);
        super.dispose();
    }

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(TrackEditFrame.class.getName());
}
