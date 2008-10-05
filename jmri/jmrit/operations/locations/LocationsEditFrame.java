// LocationsEditFrame.java

package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.setup.OperationsXml;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.cars.CarLengths;
import jmri.jmrit.operations.cars.CarTypes;
import jmri.jmrit.operations.cars.CarManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
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
 * Frame for user edit of location
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.3 $
 */

public class LocationsEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	YardTableModel yardModel = new YardTableModel();
	javax.swing.JTable yardTable = new javax.swing.JTable(yardModel);
	JScrollPane yardPane;
	SidingTableModel sidingModel = new SidingTableModel();
	javax.swing.JTable sidingTable = new javax.swing.JTable(sidingModel);
	JScrollPane sidingPane;
	InterchangeTableModel interchangeModel = new InterchangeTableModel();
	javax.swing.JTable interchangeTable = new javax.swing.JTable(interchangeModel);
	JScrollPane interchangePane;
	StagingTableModel stagingModel = new StagingTableModel();
	javax.swing.JTable stagingTable = new javax.swing.JTable(stagingModel);
	JScrollPane stagingPane;
	
	LocationManager manager;
	LocationManagerXml managerXml;

	Location _location = null;
	ArrayList checkBoxes = new ArrayList();
	JPanel panelCheckBoxes = new JPanel();
	JScrollPane typePane;
	JPanel directionPanel = new JPanel();

	// labels
	javax.swing.JLabel textName = new javax.swing.JLabel();
	javax.swing.JLabel textLength = new javax.swing.JLabel();
	javax.swing.JLabel textTrain = new javax.swing.JLabel();
	javax.swing.JLabel textLoc = new javax.swing.JLabel();
	javax.swing.JLabel textType = new javax.swing.JLabel();
	javax.swing.JLabel textOptional = new javax.swing.JLabel();
	javax.swing.JLabel textComment = new javax.swing.JLabel();

	// major buttons
	javax.swing.JButton clearButton = new javax.swing.JButton();
	javax.swing.JButton setButton = new javax.swing.JButton();
	javax.swing.JButton saveLocationButton = new javax.swing.JButton();
	javax.swing.JButton deleteLocationButton = new javax.swing.JButton();
	javax.swing.JButton addLocationButton = new javax.swing.JButton();
	javax.swing.JButton addYardButton = new javax.swing.JButton();
	javax.swing.JButton addSidingButton = new javax.swing.JButton();
	javax.swing.JButton addInterchangeButton = new javax.swing.JButton();
	javax.swing.JButton addStagingButton = new javax.swing.JButton();
	

	// check boxes
	javax.swing.JCheckBox checkBox;
	javax.swing.JCheckBox northCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox southCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox eastCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox westCheckBox = new javax.swing.JCheckBox();
	
	
	// radio buttons
    javax.swing.JRadioButton stageRadioButton = new javax.swing.JRadioButton(rb.getString("Staging"));
    javax.swing.JRadioButton interchangeRadioButton = new javax.swing.JRadioButton(rb.getString("Interchange"));
    javax.swing.JRadioButton normalRadioButton = new javax.swing.JRadioButton(rb.getString("Yards&Sidings"));
        
	// text field
	javax.swing.JTextField locationNameTextField = new javax.swing.JTextField(20);
	javax.swing.JTextField commentTextField = new javax.swing.JTextField(35);

	// for padding out panel
	javax.swing.JLabel space1 = new javax.swing.JLabel();
	javax.swing.JLabel space2 = new javax.swing.JLabel();
	javax.swing.JLabel space3 = new javax.swing.JLabel();
	
	// combo boxes

	public static final String NAME = rb.getString("Name");
	public static final int MAX_NAME_LENGTH = 25;
	public static final String LENGTH = rb.getString("Length");
	public static final String DISPOSE = "dispose" ;

	public LocationsEditFrame() {
		super();
	}

	public void initComponents(Location location) {
		_location = location;

		// load managers
		manager = LocationManager.instance();
		managerXml = LocationManagerXml.instance();
		OperationsXml.instance();					// force settings to load 

		// the following code sets the frame's initial state
		
		textName.setText(rb.getString("Name"));
		textName.setVisible(true);
		textTrain.setText(rb.getString("Train"));
		textTrain.setVisible(true);
		northCheckBox.setText(rb.getString("North"));
		southCheckBox.setText(rb.getString("South"));
		eastCheckBox.setText(rb.getString("East"));
		westCheckBox.setText(rb.getString("West"));

		textLength.setText(rb.getString("Length"));
		textLength.setVisible(true);
		textLoc.setText(rb.getString("Ops"));
		textType.setText(rb.getString("Types"));
		textType.setVisible(true);
		
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

		deleteLocationButton.setText(rb.getString("DeleteLocation"));
		deleteLocationButton.setVisible(true);
		addLocationButton.setText(rb.getString("AddLocation"));
		addLocationButton.setVisible(true);
		saveLocationButton.setText(rb.getString("SaveLocation"));
		saveLocationButton.setVisible(true);
		addYardButton.setText(rb.getString("AddYard"));
		addYardButton.setVisible(true);
		addSidingButton.setText(rb.getString("AddSiding"));
		addSidingButton.setVisible(true);
		addInterchangeButton.setText(rb.getString("AddInterchange"));
		addInterchangeButton.setVisible(true);
		addStagingButton.setText(rb.getString("AddStaging"));
		addStagingButton.setVisible(true);
		
	   	// Set up the jtable in a Scroll Pane..
    	typePane = new JScrollPane(panelCheckBoxes);
    	typePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    	Dimension minimumSize = new Dimension (typePane.getWidth(), 160);
    	typePane.setMinimumSize(minimumSize);
    	
    	yardPane = new JScrollPane(yardTable);
    	yardPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        	
    	sidingPane = new JScrollPane(sidingTable);
    	sidingPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 	    
    	interchangePane = new JScrollPane(interchangeTable);
    	interchangePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 
    	stagingPane = new JScrollPane(stagingTable);
    	stagingPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		
		if (_location != null){
			enableButtons(true);
			locationNameTextField.setText(_location.getName());
			commentTextField.setText(_location.getComment());
	      	yardModel.initTable(yardTable, location);
	      	sidingModel.initTable(sidingTable, location);
	      	interchangeModel.initTable(interchangeTable, location);
	      	stagingModel.initTable(stagingTable, location);
			if (_location.getLocationOps() == _location.NORMAL)
				normalRadioButton.setSelected(true);
			else
				stageRadioButton.setSelected(true);
				
			setTrainDirectionBoxes();
		} else {
			enableButtons(false);
			normalRadioButton.setSelected(true);
		}
		
		setVisibleLocations();
	
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

	    //      Set up the panels
    	JPanel p1 = new JPanel();
    	p1.setLayout(new GridBagLayout());
				
		// Layout the panel by rows
		// row 1
		addItem(p1, textName, 0, 1);
		addItemWidth(p1, locationNameTextField, 3, 1, 1);

		// row 2
    	directionPanel.setLayout(new GridBagLayout());
		addItem(directionPanel, textTrain, 0, 1);
		addItemLeft(directionPanel, northCheckBox, 1, 1);
		addItemLeft(directionPanel, southCheckBox, 2, 1);
		addItemLeft(directionPanel, eastCheckBox, 3, 1);
		addItemLeft(directionPanel, westCheckBox, 4, 1);
		Border border = BorderFactory.createEtchedBorder();
		directionPanel.setBorder(border);
		// row 3

		
		// row 4
		

		// row 5
	   	panelCheckBoxes.setLayout(new GridBagLayout());
		updateCheckboxes();
		
	   	// row 6
		JPanel p2 = new JPanel();
    	p2.setLayout(new GridBagLayout());
    	addItem(p2, clearButton, 0, 0);
    	addItem(p2, setButton, 1, 0);

		// row 8
		
    	JPanel p3 = new JPanel();
    	p3.setLayout(new GridBagLayout());
    	int y=0;
		
		// row 9
		JPanel p = new JPanel();
		ButtonGroup opsGroup = new ButtonGroup();
		opsGroup.add(normalRadioButton);
		opsGroup.add(interchangeRadioButton);
		opsGroup.add(stageRadioButton);
		p.add(textLoc);
		p.add(normalRadioButton);
		p.add(interchangeRadioButton);
		p.add(stageRadioButton);
		addItemWidth(p3, p, 3, 0, ++y);
		
    	JPanel p4 = new JPanel();
    	p4.setLayout(new GridBagLayout());
		
		// row 10
		addItem (p4, space1, 0, ++y);
    	
		// row 11
		addItem(p4, textComment, 0, ++y);
		addItemWidth(p4, commentTextField, 3, 1, y);
				
		// row 12
		addItem(p4, space2, 0, ++y);
		// row 13
		addItem(p4, deleteLocationButton, 0, ++y);
		addItem(p4, addLocationButton, 1, y);
//		addItem(p4, copyButton, 2, y);
		addItem(p4, saveLocationButton, 3, y);
		
		getContentPane().add(p1);
		getContentPane().add(directionPanel);
		getContentPane().add(typePane);
		getContentPane().add(p2);
		getContentPane().add(p3);
       	getContentPane().add(yardPane);
       	getContentPane().add(addYardButton);
       	getContentPane().add(sidingPane);
       	getContentPane().add(addSidingButton);
       	getContentPane().add(interchangePane);
       	getContentPane().add(addInterchangeButton);
       	getContentPane().add(stagingPane);
       	getContentPane().add(addStagingButton);
       	getContentPane().add(p4);
		
		// setup buttons
		addButtonAction(setButton);
		addButtonAction(clearButton);
		addButtonAction(deleteLocationButton);
		addButtonAction(addLocationButton);
		addButtonAction(saveLocationButton);
		addButtonAction(addYardButton);
		addButtonAction(addSidingButton);
		addButtonAction(addInterchangeButton);
		addButtonAction(addStagingButton);
		
		addRadioButtonAction(normalRadioButton);
		addRadioButtonAction(interchangeRadioButton);
		addRadioButtonAction(stageRadioButton);
		
		addCheckBoxTrainAction(northCheckBox);
		addCheckBoxTrainAction(southCheckBox);
		addCheckBoxTrainAction(eastCheckBox);
		addCheckBoxTrainAction(westCheckBox);

		// add property listeners
		CarTypes.instance().addPropertyChangeListener(this);
       	

		// build menu
		JMenuBar menuBar = new JMenuBar();
//		JMenu toolMenu = new JMenu("Tools");
//		menuBar.add(toolMenu);
//		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Locations", true);

		//	 get notified if combo box gets modified
		
		// set frame size and location for display
		pack();
		if((getWidth()<600)) setSize(600, getHeight());
		setSize(getWidth(), 700);
//		setLocation(500, 300);
//		setAlwaysOnTop(true);
		setVisible(true);
	}
	
	private void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
	}
	
	
	YardEditFrame yef = null;
	SidingEditFrame sef = null;
	InterchangeEditFrame ief = null;
	StagingEditFrame stef = null;
	
	// Save, Delete, Add 
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == addYardButton){
// need to reinitialize each time, so for now make new
//			if (yef == null){
				yef = new YardEditFrame();
				yef.initComponents(_location, null);
				yef.setTitle(rb.getString("AddYard"));
//			}
		}
		if (ae.getSource() == addSidingButton){
			sef = new SidingEditFrame();
			sef.initComponents(_location, null);
			sef.setTitle(rb.getString("AddSiding"));
		}
		if (ae.getSource() == addInterchangeButton){
			ief = new InterchangeEditFrame();
			ief.initComponents(_location, null);
			ief.setTitle(rb.getString("AddInterchange"));
		}
		if (ae.getSource() == addStagingButton){
			stef = new StagingEditFrame();
			stef.initComponents(_location, null);
			stef.setTitle(rb.getString("AddStaging"));
		}

		if (ae.getSource() == saveLocationButton){
			log.debug("location save button actived");
			Location l = manager.getLocationByName(locationNameTextField.getText());
			if (_location == null && l == null){
				saveNewLocation();
			} else {
				if (l != null && l != _location){
					reportLocationExists("save");
					return;
				}
				saveLocation();
			}
		}
		if (ae.getSource() == deleteLocationButton){
			log.debug("location delete button actived");
			Location l = manager.getLocationByName(locationNameTextField.getText());
			if (l == null)
				return;
			int cars = l.getNumberCars();
			if (cars > 0){
				if (JOptionPane.showConfirmDialog(this,
						"There are " + cars + " cars at this location, delete?", "Delete location?",
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
					return;
				}
			}
			
			manager.deregister(l);
			_location = null;
			selectCheckboxes(false);
			enableCheckboxes(false);
			enableButtons(false);
			// save location file
			managerXml.writeOperationsLocationFile();
			// save car file in case location had cars
			CarManagerXml.instance().writeOperationsCarFile();
		}
		if (ae.getSource() == addLocationButton){
			Location l = manager.getLocationByName(locationNameTextField.getText());
			if (l != null){
				reportLocationExists("add");
				return;
			}
			saveNewLocation();
		}
		if (ae.getSource() == setButton){
			selectCheckboxes(true);
		}
		if (ae.getSource() == clearButton){
			selectCheckboxes(false);
		}
	}
	
	private void saveNewLocation(){
		if (!checkName())
			return;
		Location location = manager.newLocation(locationNameTextField.getText());
		yardModel.initTable(yardTable, location);
      	sidingModel.initTable(sidingTable, location);
      	interchangeModel.initTable(interchangeTable, location);
      	stagingModel.initTable(stagingTable, location);
		_location = location;
		// enable checkboxes
		selectCheckboxes(true);
		enableCheckboxes(true);
		enableButtons(true);
		setTrainDirectionBoxes();
		saveLocation();
	}
	
	private void saveLocation (){
		if (!checkName())
			return;
		_location.setName(locationNameTextField.getText());
		_location.setComment(commentTextField.getText());

		if (normalRadioButton.isSelected() || interchangeRadioButton.isSelected()){
			_location.setLocationOps(_location.NORMAL);
		}
		if (stageRadioButton.isSelected()){
			_location.setLocationOps(_location.STAGING);
		}

		// save location file
		managerXml.writeOperationsLocationFile();
		// save car file in case location name changed
		CarManagerXml.instance().writeOperationsCarFile();
		// save route file in case location name changed
		RouteManagerXml.instance().writeOperationsRouteFile();
	}
	

	/**
	 * 
	 * @return true if name is less than 26 characters
	 */
	private boolean checkName(){
		if (locationNameTextField.getText().length() > MAX_NAME_LENGTH){
			log.error("Location name must be less than "+ Integer.toString(MAX_NAME_LENGTH+1) +" characters");
			JOptionPane.showMessageDialog(this,
					"Location name must be less than "+ Integer.toString(MAX_NAME_LENGTH+1) +" characters", "Can not add location!",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private void reportLocationExists(String s){
		log.info("Can not " + s + ", location already exists");
		JOptionPane.showMessageDialog(this,
				"Location with this name already exists", "Can not " + s + " location!",
				JOptionPane.ERROR_MESSAGE);
	}
	
	private void enableButtons(boolean enabled){
		northCheckBox.setEnabled(enabled);
		southCheckBox.setEnabled(enabled);
		eastCheckBox.setEnabled(enabled);
		westCheckBox.setEnabled(enabled);
		clearButton.setEnabled(enabled);
		setButton.setEnabled(enabled);
		addYardButton.setEnabled(enabled);
		addSidingButton.setEnabled(enabled);
		addInterchangeButton.setEnabled(enabled);
		addStagingButton.setEnabled(enabled);
		saveLocationButton.setEnabled(enabled);
		deleteLocationButton.setEnabled(enabled);
		// the inverse!
		addLocationButton.setEnabled(!enabled);
		// enable radio buttons
		normalRadioButton.setEnabled(enabled);
		interchangeRadioButton.setEnabled(enabled);
		stageRadioButton.setEnabled(enabled);
		//
		yardTable.setEnabled(enabled);
	}
	
	private void addRadioButtonAction(JRadioButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonRadioActionPerformed(e);
			}
		});
	}
	
	public void buttonRadioActionPerformed(java.awt.event.ActionEvent ae) {
		setVisibleLocations();
	}
	
	private void setVisibleLocations(){
		setEnabledLocations();
		interchangePane.setVisible(interchangeRadioButton.isSelected());
		addInterchangeButton.setVisible(interchangeRadioButton.isSelected());
		stagingPane.setVisible(stageRadioButton.isSelected());
		addStagingButton.setVisible(stageRadioButton.isSelected());
		yardPane.setVisible(normalRadioButton.isSelected());
		addYardButton.setVisible(normalRadioButton.isSelected());
		sidingPane.setVisible(normalRadioButton.isSelected());
		addSidingButton.setVisible(normalRadioButton.isSelected());
	}
	
	private void setEnabledLocations(){
		log.debug("set radio button");
		if (yardModel.getRowCount()>0 || sidingModel.getRowCount()>0 || interchangeModel.getRowCount()>0){
			stageRadioButton.setEnabled(false);
			if(stageRadioButton.isSelected())
				normalRadioButton.setSelected(true);
		}
		else if (stagingModel.getRowCount()>0){
			stageRadioButton.setSelected(true);
			normalRadioButton.setEnabled(false);
			interchangeRadioButton.setEnabled(false);
		} 
		else {
			normalRadioButton.setEnabled(true);
			interchangeRadioButton.setEnabled(true);
			stageRadioButton.setEnabled(true);
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
			if(_location != null){
				if (enable)
					_location.addTypeName(checkBox.getText());
				else
					_location.deleteTypeName(checkBox.getText());
			}
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
			JCheckBox checkBox = new javax.swing.JCheckBox();
			checkBoxes.add(checkBox);
			checkBox.setText(carTypes[i]);
			addCheckBoxAction(checkBox);
			addItemLeft(panelCheckBoxes, checkBox, x++, y);
			if (_location != null){
				if(_location.acceptsTypeName(carTypes[i]))
					checkBox.setSelected(true);
			} else {
				checkBox.setEnabled(false);
			}
			if (x > 5){
				y++;
				x = 0;
			}
		}
//		addItem (panelCheckBoxes, clearButton, 1, ++y);
//		addItem (panelCheckBoxes, setButton, 4, y);

		Border border = BorderFactory.createEtchedBorder();
		panelCheckBoxes.setBorder(border);
		panelCheckBoxes.revalidate();

//		pack();
		repaint();
	}
	
		
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
			_location.addTypeName(b.getText());
		}else{
			_location.deleteTypeName(b.getText());
		}
	}
	
	
	private void addCheckBoxTrainAction(JCheckBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				checkBoxActionTrainPerformed(e);
			}
		});
	}
	
	private void checkBoxActionTrainPerformed(java.awt.event.ActionEvent ae) {
		// save train directions serviced by this location
		if (_location == null)
			return;
		int direction = 0;
		if (northCheckBox.isSelected()){
			direction += _location.NORTH;
		}
		if (southCheckBox.isSelected()){
			direction += _location.SOUTH;
		}
		if (eastCheckBox.isSelected()){
			direction += _location.EAST;
		}
		if (westCheckBox.isSelected()){
			direction += _location.WEST;
		}
		_location.setTrainDirections(direction);
		
	}
	
	private void setTrainDirectionBoxes(){
		northCheckBox.setVisible((Setup.getTrainDirection() & Setup.NORTH)>0);
		southCheckBox.setVisible((Setup.getTrainDirection() & Setup.SOUTH)>0);
		eastCheckBox.setVisible((Setup.getTrainDirection() & Setup.EAST)>0);
		westCheckBox.setVisible((Setup.getTrainDirection() & Setup.WEST)>0);
		
		northCheckBox.setSelected((_location.getTrainDirections() & _location.NORTH)>0);
		southCheckBox.setSelected((_location.getTrainDirections() & _location.SOUTH)>0);
		eastCheckBox.setSelected((_location.getTrainDirections() & _location.EAST)>0);
		westCheckBox.setSelected((_location.getTrainDirections() & _location.WEST)>0);
	}
	
	public void dispose() {
		CarTypes.instance().removePropertyChangeListener(this);
		yardModel.dispose();
		sidingModel.dispose();
		interchangeModel.dispose();
		stagingModel.dispose();
		super.dispose();
	}
	
 	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled()) 
			log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(CarTypes.CARTYPES)){
			updateCheckboxes();
		}
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(LocationsEditFrame.class.getName());
}
