// InterchangeEditFrame.java

package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsXml;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.cars.CarTypes;
import jmri.jmrit.operations.cars.CarRoads;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.OperationsFrame;

import java.awt.*;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.Border;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Frame for user edit of location interchanges
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.4 $
 */

public class InterchangeEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	LocationManager manager;
	LocationManagerXml managerXml;

	Location _location = null;
	SecondaryLocation _interchange = null;
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
	javax.swing.JButton saveInterchangeButton = new javax.swing.JButton();
	javax.swing.JButton deleteInterchangeButton = new javax.swing.JButton();
	javax.swing.JButton addInterchangeButton = new javax.swing.JButton();
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
	javax.swing.JTextField interchangeNameTextField = new javax.swing.JTextField(20);
	javax.swing.JTextField interchangeLengthTextField = new javax.swing.JTextField(5);
	javax.swing.JTextField commentTextField = new javax.swing.JTextField(35);

	// for padding out panel
	javax.swing.JLabel space1 = new javax.swing.JLabel();
	javax.swing.JLabel space2 = new javax.swing.JLabel();
	javax.swing.JLabel space3 = new javax.swing.JLabel();
	
	// combo box
	javax.swing.JComboBox comboBox = CarRoads.instance().getComboBox();
	javax.swing.JComboBox comboBoxDropTrains = TrainManager.instance().getComboBox();
	javax.swing.JComboBox comboBoxDropRoutes = RouteManager.instance().getComboBox();
	javax.swing.JComboBox comboBoxPickupTrains = TrainManager.instance().getComboBox();
	javax.swing.JComboBox comboBoxPickupRoutes = RouteManager.instance().getComboBox();

	public static final String DISPOSE = "dispose" ;
	public static final int MAX_NAME_LENGTH = 25;

	public InterchangeEditFrame() {
		super();
	}

	public void initComponents(Location location, SecondaryLocation interchange) {
		_location = location;
		_location.addPropertyChangeListener(this);
		_interchange = interchange;

		// load managers
		managerXml = LocationManagerXml.instance();

		// the following code sets the frame's initial state
		
		textName.setText(rb.getString("Name"));
		textName.setVisible(true);

		textLength.setText(rb.getString("Length"));
		textLength.setVisible(true);
		textTrain.setText(rb.getString("TrainInterchange"));
		textTrain.setVisible(true);
		northCheckBox.setText(rb.getString("North"));
		southCheckBox.setText(rb.getString("South"));
		eastCheckBox.setText(rb.getString("East"));
		westCheckBox.setText(rb.getString("West"));
		textType.setText(rb.getString("TypesInterchange"));
		textType.setVisible(true);
		textRoad.setText(rb.getString("RoadsInterchange"));
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

		deleteInterchangeButton.setText(rb.getString("DeleteInterchange"));
		deleteInterchangeButton.setVisible(true);
		addInterchangeButton.setText(rb.getString("AddInterchange"));
		addInterchangeButton.setVisible(true);
		saveInterchangeButton.setText(rb.getString("SaveInterchange"));
		saveInterchangeButton.setVisible(true);
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
		addItemWidth(p1, interchangeNameTextField, 3, 1, 1);

		// row 2
		addItem(p1, textLength, 0, 2);
		addItemLeft(p1, interchangeLengthTextField, 1, 2);
		
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
		addItem(p4, deleteInterchangeButton, 0, ++y);
		addItem(p4, addInterchangeButton, 1, y);
		addItem(p4, saveInterchangeButton, 3, y);
		
		getContentPane().add(p1);
		getContentPane().add(panelTrainDir);
		getContentPane().add(panelCheckBoxes);
		getContentPane().add(panelRoadNames);
		getContentPane().add(panelDropOptions);
		getContentPane().add(panelPickupOptions);
       	getContentPane().add(p4);
		
		// setup buttons
		addButtonAction(setButton);
		addButtonAction(clearButton);
		addButtonAction(deleteInterchangeButton);
		addButtonAction(addInterchangeButton);
		addButtonAction(saveInterchangeButton);
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
		if (_interchange !=null){
			interchangeNameTextField.setText(_interchange.getName());
			commentTextField.setText(_interchange.getComment());
			interchangeLengthTextField.setText(Integer.toString(_interchange.getLength()));
			enableButtons(true);
		} else {
			enableButtons(false);
		}
 
		// build menu
		JMenuBar menuBar = new JMenuBar();
//		JMenu toolMenu = new JMenu("Tools");
//		menuBar.add(toolMenu);
//		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Interchange", true);

		//	 get notified if combo box gets modified
		
		// set frame size and location for display
		pack();
		setLocation(500, 300);
		setVisible(true);	
	}
	
	private void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
	}
	
	// Save, Delete, Add 
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveInterchangeButton){
			log.debug("interchange save button actived");
			if (_interchange != null){
				saveInterchange(_interchange);
			} else {
				addNewInterchange();
			}
		}
		if (ae.getSource() == deleteInterchangeButton){
			log.debug("interchange delete button actived");
//			SecondaryLocation y = _location.getSecondaryLocationByName(interchangeNameTextField.getText());
			if (_interchange != null){
				int cars = _interchange.getNumberCars();
				if (cars > 0){
					if (JOptionPane.showConfirmDialog(this,
							"There are " + cars + " cars at this location, delete?", "Delete location?",
							JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
						return;
					}
				}
				selectCheckboxes(false);
				_location.deleteSecondaryLocation(_interchange);
				_interchange = null;
				enableButtons(false);
				// save location file
				managerXml.writeOperationsLocationFile();
			}
		}
		if (ae.getSource() == addInterchangeButton){
			addNewInterchange();
		}
		if (ae.getSource() == addRoadButton){
			_interchange.addRoadName((String) comboBox.getSelectedItem());
			updateRoadNames();
		}
		if (ae.getSource() == deleteRoadButton){
			_interchange.deleteRoadName((String) comboBox.getSelectedItem());
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
							"This interchange track is not serviced by train ("+ train.getName() + ")", "Error!",
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
							"This interchange track is not serviced by route ("+ route.getName() + ")", "Error!",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			_interchange.addDropId(id);
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
			_interchange.deleteDropId(id);
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
							"This interchange track is not serviced by train ("+ train.getName() + ")", "Error!",
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
							"This interchange track is not serviced by route ("+ route.getName() + ")", "Error!",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			_interchange.addPickupId(id);
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
			_interchange.deletePickupId(id);
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

	private void addNewInterchange(){
		// check that interchange name is valid
		if (!checkName())
			return;
		// check to see if interchange already exsists
		SecondaryLocation check = _location.getSecondaryLocationByName(interchangeNameTextField.getText(), SecondaryLocation.INTERCHANGE);
		if (check != null){
			reportInterchangeExists("add");
			return;
		}
		// add interchange to this location
		_interchange =_location.addSecondaryLocation(interchangeNameTextField.getText(), SecondaryLocation.INTERCHANGE);
		// check interchange length
		checkLength(_interchange);
		// copy checkboxes
		copyCheckboxes();
		// store comment
		_interchange.setComment(commentTextField.getText());
		// enable 
		enableButtons(true);
		// setup checkboxes
		selectCheckboxes(true);
		updateTrainDir();
		// save location file
		managerXml.writeOperationsLocationFile();
	}

	private void saveInterchange (SecondaryLocation y){
		// check that interchange name is valid
		if (!checkName())
			return;
		// check to see if interchange already exsists
		SecondaryLocation check = _location.getSecondaryLocationByName(interchangeNameTextField.getText(), SecondaryLocation.INTERCHANGE);
		if (check != null && check != y){
			reportInterchangeExists("save");
			return;
		}
		// check interchange length
		if (!checkLength(y))
			return;
		// save train directions serviced by this location
		int direction = 0;
		if (northCheckBox.isSelected()){
			direction += y.NORTH;
		}
		if (southCheckBox.isSelected()){
			direction += y.SOUTH;
		}
		if (eastCheckBox.isSelected()){
			direction += y.EAST;
		}
		if (westCheckBox.isSelected()){
			direction += y.WEST;
		}
		y.setTrainDirections(direction);
		y.setName(interchangeNameTextField.getText());
		y.setComment(commentTextField.getText());
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
		if (interchangeNameTextField.getText().length() > MAX_NAME_LENGTH){
			log.error("Interchange name must be less than "+ Integer.toString(MAX_NAME_LENGTH+1) +" charaters");
			JOptionPane.showMessageDialog(this,
					"Interchange name must be less than "+ Integer.toString(MAX_NAME_LENGTH+1) +" charaters", "Can not add location!",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private boolean checkLength (SecondaryLocation y){	
		// convert interchange length if in inches
		String length = interchangeLengthTextField.getText();
		if (length.endsWith("\"")){
			length = length.substring(0, length.length()-1);
			try {
				double inches = Double.parseDouble(length);
				int feet = (int)(inches * Setup.getScaleRatio() / 12);
				length = Integer.toString(feet);
			} catch (NumberFormatException e){
				log.error("Can not convert from inches to feet");
				JOptionPane.showMessageDialog(this,
						"Can not convert from inches to feet", "Interchange length incorrect",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		// confirm that length is a number and less than 10000 feet
		int interchangeLength = 0;
		try {
			interchangeLength = Integer.parseInt(length);
			if (interchangeLength > 99999){
				log.error("Interchange length must be less than 100,000 feet");
				JOptionPane.showMessageDialog(this,
						"Interchange length must be less than 100,000 feet", "Interchange length incorrect",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} catch (NumberFormatException e){
			log.error("interchange length not an integer");
			JOptionPane.showMessageDialog(this,
					"Interchange length must be a number", "Interchange length incorrect",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// if everything is okay, save length
		y.setLength(interchangeLength);
		return true;
	}
	
	private void reportInterchangeExists(String s){
		log.info("Can not " + s + ", interchange already exists");
		JOptionPane.showMessageDialog(this,
				"Interchange with this name already exists", "Can not " + s + " interchange!",
				JOptionPane.ERROR_MESSAGE);
	}

	private void enableButtons(boolean enabled){
		northCheckBox.setEnabled(enabled);
		southCheckBox.setEnabled(enabled);
		eastCheckBox.setEnabled(enabled);
		westCheckBox.setEnabled(enabled);
		clearButton.setEnabled(enabled);
		setButton.setEnabled(enabled);
		deleteInterchangeButton.setEnabled(enabled);
		saveInterchangeButton.setEnabled(enabled);
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
	
	private void addRadioButtonAction(JRadioButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonRadioActionPerformed(e);
			}
		});
	}
	
	public void buttonRadioActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button activated");
		if (ae.getSource() == roadNameAll){
			_interchange.setRoadOption(_interchange.ALLROADS);
			updateRoadNames();
		}
		if (ae.getSource() == roadNameInclude){
			_interchange.setRoadOption(_interchange.INCLUDEROADS);
			updateRoadNames();
		}
		if (ae.getSource() == roadNameExclude){
			_interchange.setRoadOption(_interchange.EXCLUDEROADS);
			updateRoadNames();
		}
		if (ae.getSource() == anyDrops){
			_interchange.setDropOption(_interchange.ANY);
			updateDropOptions();
		}
		if (ae.getSource() == trainDrop){
			_interchange.setDropOption(_interchange.TRAINS);
			updateDropOptions();
		}
		if (ae.getSource() == routeDrop){
			_interchange.setDropOption(_interchange.ROUTES);
			updateDropOptions();
		}
		if (ae.getSource() == anyPickups){
			_interchange.setPickupOption(_interchange.ANY);
			updatePickupOptions();
		}
		if (ae.getSource() == trainPickup){
			_interchange.setPickupOption(_interchange.TRAINS);
			updatePickupOptions();
		}
		if (ae.getSource() == routePickup){
			_interchange.setPickupOption(_interchange.ROUTES);
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
			if(_interchange != null){
				if (enable)
					_interchange.addTypeName(checkBox.getText());
				else
					_interchange.deleteTypeName(checkBox.getText());
			}
		}
	}
	
	private void copyCheckboxes(){
		for (int i=0; i < checkBoxes.size(); i++){
			checkBox = (JCheckBox)checkBoxes.get(i);
			if (checkBox.isSelected() && _interchange != null)
				_interchange.addTypeName(checkBox.getText());
			else
				_interchange.deleteTypeName(checkBox.getText());
		}
	}
	
	private void updateCheckboxes(){
		checkBoxes.clear();
		panelCheckBoxes.removeAll();
		int y = 0;		// vertical position in panel
		addItemWidth(panelCheckBoxes, textType, 3, 1, y++);

		String[]carTypes = CarTypes.instance().getNames();
		int x = 0;
		for (int i =0; i<carTypes.length; i++){
			if(_location.acceptsTypeName(carTypes[i])){
				JCheckBox checkBox = new javax.swing.JCheckBox();
				checkBoxes.add(checkBox);
				checkBox.setText(carTypes[i]);
				addCheckBoxAction(checkBox);
				addItemLeft(panelCheckBoxes, checkBox, x++, y);
				if(_interchange != null && _interchange.acceptsTypeName(carTypes[i]))
					checkBox.setSelected(true);
				if (x > 5){
					y++;
					x = 0;
				}
			}
		}
		enableCheckboxes(_interchange != null);

		addItem (panelCheckBoxes, clearButton, 1, ++y);
		addItem (panelCheckBoxes, setButton, 4, y);

		Border border = BorderFactory.createEtchedBorder();
		panelCheckBoxes.setBorder(border);
		panelCheckBoxes.revalidate();

		pack();
		repaint();
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

		if(_interchange != null){
			// set radio button
			anyDrops.setSelected(_interchange.getDropOption().equals(_interchange.ANY));
			trainDrop.setSelected(_interchange.getDropOption().equals(_interchange.TRAINS));
			routeDrop.setSelected(_interchange.getDropOption().equals(_interchange.ROUTES));
			
			if (!anyDrops.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	if (trainDrop.isSelected())
		    		p.add(comboBoxDropTrains);
		    	else
		    		p.add(comboBoxDropRoutes);
		    	p.add(addDropButton);
		    	p.add(deleteDropButton);
				gc.gridy = y++;
				panelDropOptions.add(p, gc);
		    	y++;

		    	String[]dropIds = _interchange.getDropIds();
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

		if(_interchange != null){
			// set radio button
			anyPickups.setSelected(_interchange.getPickupOption().equals(_interchange.ANY));
			trainPickup.setSelected(_interchange.getPickupOption().equals(_interchange.TRAINS));
			routePickup.setSelected(_interchange.getPickupOption().equals(_interchange.ROUTES));
			
			if (!anyPickups.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	if (trainPickup.isSelected())
		    		p.add(comboBoxPickupTrains);
		    	else
		    		p.add(comboBoxPickupRoutes);
		    	p.add(addPickupButton);
		    	p.add(deletePickupButton);
				gc.gridy = y++;
				panelPickupOptions.add(p, gc);
		    	y++;

		    	String[]pickupIds = _interchange.getPickupIds();
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

		if(_interchange != null){
			// set radio button
			roadNameAll.setSelected(_interchange.getRoadOption().equals(_interchange.ALLROADS));
			roadNameInclude.setSelected(_interchange.getRoadOption().equals(_interchange.INCLUDEROADS));
			roadNameExclude.setSelected(_interchange.getRoadOption().equals(_interchange.EXCLUDEROADS));
			
			if (!roadNameAll.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	p.add(comboBox);
		    	p.add(addRoadButton);
		    	p.add(deleteRoadButton);
				gc.gridy = y++;
		    	panelRoadNames.add(p, gc);
		    	y++;

		    	String[]carRoads = _interchange.getRoadNames();
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
		
		if (_interchange != null){
			northCheckBox.setSelected((_interchange.getTrainDirections() & _interchange.NORTH) > 0);
			southCheckBox.setSelected((_interchange.getTrainDirections() & _interchange.SOUTH) > 0);
			eastCheckBox.setSelected((_interchange.getTrainDirections() & _interchange.EAST) > 0);
			westCheckBox.setSelected((_interchange.getTrainDirections() & _interchange.WEST) > 0);
		}
		
		panelTrainDir.revalidate();

		pack();
		repaint();
	}


	private boolean editActive = false;

	
	private void addCheckBoxAction(JCheckBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				checkBoxActionPerformed(e);
			}
		});
	}
	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		JCheckBox b =  (JCheckBox)ae.getSource();
		log.debug("checkbox change "+ b.getText());
		if (_location == null)
			return;
		if (b.isSelected()){
			_interchange.addTypeName(b.getText());
		}else{
			_interchange.deleteTypeName(b.getText());
		}
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled()) 
			log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(Location.CARTYPES)){
			updateCheckboxes();
		}
		if (e.getPropertyName().equals(Location.TRAINDIRECTION)){
			updateTrainDir();
		}
		
		if (e.getPropertyName().equals(DISPOSE)){
			editActive = false;
		}
	}
	
    public void dispose() {
    	_location.removePropertyChangeListener(this);
        super.dispose();
    }

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(InterchangeEditFrame.class.getName());
}
