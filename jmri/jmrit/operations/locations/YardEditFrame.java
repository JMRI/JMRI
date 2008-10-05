// YardEditFrame.java

package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsXml;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.cars.CarTypes;
import jmri.jmrit.operations.cars.CarRoads;
import java.awt.*;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.Border;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Frame for user edit of location yards
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.3 $
 */

public class YardEditFrame extends jmri.util.JmriJFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	LocationManager manager;
	LocationManagerXml managerXml;

	Location _location = null;
	SecondaryLocation _yard = null;
	List checkBoxes = new ArrayList();
	JPanel panelCheckBoxes = new JPanel();
	JPanel panelTrainDir = new JPanel();
	JPanel panelRoadNames = new JPanel();
	
	// labels
	javax.swing.JLabel textName = new javax.swing.JLabel();
	javax.swing.JLabel textLength = new javax.swing.JLabel();
	javax.swing.JLabel textTrain = new javax.swing.JLabel();
	javax.swing.JLabel textType = new javax.swing.JLabel();
	javax.swing.JLabel textRoad = new javax.swing.JLabel();
	javax.swing.JLabel textOptional = new javax.swing.JLabel();
	javax.swing.JLabel textComment = new javax.swing.JLabel();

	// major buttons
	javax.swing.JButton clearButton = new javax.swing.JButton();
	javax.swing.JButton setButton = new javax.swing.JButton();
	javax.swing.JButton saveYardButton = new javax.swing.JButton();
	javax.swing.JButton deleteYardButton = new javax.swing.JButton();
	javax.swing.JButton addYardButton = new javax.swing.JButton();
	javax.swing.JButton deleteRoadButton = new javax.swing.JButton();
	javax.swing.JButton addRoadButton = new javax.swing.JButton();
	
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
	
	// text field
	javax.swing.JTextField yardNameTextField = new javax.swing.JTextField(20);
	javax.swing.JTextField yardLengthTextField = new javax.swing.JTextField(5);
	javax.swing.JTextField commentTextField = new javax.swing.JTextField(35);

	// for padding out panel
	javax.swing.JLabel space1 = new javax.swing.JLabel();
	javax.swing.JLabel space2 = new javax.swing.JLabel();
	javax.swing.JLabel space3 = new javax.swing.JLabel();
	
	// combo box
	javax.swing.JComboBox comboBox = CarRoads.instance().getComboBox();

	public static final String DISPOSE = "dispose" ;
	public static final int MAX_NAME_LENGTH = 25;

	public YardEditFrame() {
		super();
	}

	public void initComponents(Location location, SecondaryLocation yard) {
		_location = location;
		_location.addPropertyChangeListener(this);
		_yard = yard;

		// load managers
		managerXml = LocationManagerXml.instance();

		// the following code sets the frame's initial state
		
		textName.setText(rb.getString("Name"));
		textName.setVisible(true);

		textLength.setText(rb.getString("Length"));
		textLength.setVisible(true);
		textTrain.setText(rb.getString("TrainYard"));
		textTrain.setVisible(true);
		northCheckBox.setText(rb.getString("North"));
		southCheckBox.setText(rb.getString("South"));
		eastCheckBox.setText(rb.getString("East"));
		westCheckBox.setText(rb.getString("West"));
		textType.setText(rb.getString("TypesYard"));
		textType.setVisible(true);
		textRoad.setText(rb.getString("RoadsYard"));
		textRoad.setVisible(true);
		
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

		deleteYardButton.setText(rb.getString("DeleteYard"));
		deleteYardButton.setVisible(true);
		addYardButton.setText(rb.getString("AddYard"));
		addYardButton.setVisible(true);
		saveYardButton.setText(rb.getString("SaveYard"));
		saveYardButton.setVisible(true);
		addRoadButton.setText(rb.getString("AddRoad"));
		addRoadButton.setVisible(true);
		deleteRoadButton.setText(rb.getString("DeleteRoad"));
		deleteRoadButton.setVisible(true);
		
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

	    //      Set up the panels
    	JPanel p1 = new JPanel();
    	p1.setLayout(new GridBagLayout());
				
		// Layout the panel by rows
		// row 1
		addItem(p1, textName, 0, 1);
		addItemFull(p1, yardNameTextField, 1, 1);

		// row 2
		addItem(p1, textLength, 0, 2);
		addItemLeft(p1, yardLengthTextField, 1, 2);
		
		// row 3
		panelTrainDir.setLayout(new GridBagLayout());
		updateTrainDir();
		
		// row 4
		

		// row 5
	   	panelCheckBoxes.setLayout(new GridBagLayout());
		updateCheckboxes();
		
		panelRoadNames.setLayout(new GridBagLayout());
		roadGroup.add(roadNameAll);
		roadGroup.add(roadNameInclude);
		roadGroup.add(roadNameExclude);
		updateRoadNames();
 		
    	JPanel p4 = new JPanel();
    	p4.setLayout(new GridBagLayout());
		int y = 0;
		
		// row 10
		addItem (p4, space1, 0, ++y);
    	
		// row 11
		addItem(p4, textComment, 0, ++y);
		addItemFull(p4, commentTextField, 1, y);
				
		// row 12
		addItem(p4, space2, 0, ++y);
		// row 13
		addItem(p4, deleteYardButton, 0, ++y);
		addItem(p4, addYardButton, 1, y);
		addItem(p4, saveYardButton, 3, y);
		
		getContentPane().add(p1);
		getContentPane().add(panelTrainDir);
		getContentPane().add(panelCheckBoxes);
		getContentPane().add(panelRoadNames);
       	getContentPane().add(p4);
		
		// setup buttons
		addButtonAction(setButton);
		addButtonAction(clearButton);
		addButtonAction(deleteYardButton);
		addButtonAction(addYardButton);
		addButtonAction(saveYardButton);
		addButtonAction(deleteRoadButton);
		addButtonAction(addRoadButton);
		
		addRadioButtonAction(roadNameAll);
		addRadioButtonAction(roadNameInclude);
		addRadioButtonAction(roadNameExclude);
		
		// load fields and enable buttons
		if (_yard !=null){
			yardNameTextField.setText(_yard.getName());
			commentTextField.setText(_yard.getComment());
			yardLengthTextField.setText(Integer.toString(_yard.getLength()));
			enableButtons(true);
		} else {
			enableButtons(false);
		}
 
		// build menu
		JMenuBar menuBar = new JMenuBar();
//		JMenu toolMenu = new JMenu("Tools");
//		menuBar.add(toolMenu);
//		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Yards", true);

		//	 get notified if combo box gets modified
		
		// set frame size and location for display
		pack();
//		if((getWidth()<600)) setSize(600, getHeight());
//		setSize(getWidth(), 600);
		setLocation(500, 300);
//		setAlwaysOnTop(true);	// this blows up in Java 1.4
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
		if (ae.getSource() == saveYardButton){
			log.debug("yard save button actived");
			if (_yard != null){
				saveYard(_yard);
			} else {
				addNewYard();
			}
		}
		if (ae.getSource() == deleteYardButton){
			log.debug("yard delete button actived");
//			SecondaryLocation y = _location.getSecondaryLocationByName(yardNameTextField.getText());
			if (_yard != null){
				int cars = _yard.getNumberCars();
				if (cars > 0){
					if (JOptionPane.showConfirmDialog(this,
							"There are " + cars + " cars at this location, delete?", "Delete location?",
							JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
						return;
					}
				}
				selectCheckboxes(false);
				_location.deleteSecondaryLocation(_yard);
				_yard = null;
				enableButtons(false);
				// save location file
				managerXml.writeOperationsLocationFile();
			}
		}
		if (ae.getSource() == addYardButton){
			addNewYard();
		}
		if (ae.getSource() == addRoadButton){
			_yard.addRoadName((String) comboBox.getSelectedItem());
			updateRoadNames();
		}
		if (ae.getSource() == deleteRoadButton){
			_yard.deleteRoadName((String) comboBox.getSelectedItem());
			updateRoadNames();
		}
		if (ae.getSource() == setButton){
			selectCheckboxes(true);
		}
		if (ae.getSource() == clearButton){
			selectCheckboxes(false);
		}
	}

	private void addNewYard(){
		// check that yard name is valid
		if (!checkName())
			return;
		// check to see if yard already exsists
		SecondaryLocation check = _location.getSecondaryLocationByName(yardNameTextField.getText(), SecondaryLocation.YARD);
		if (check != null){
			reportYardExists("add");
			return;
		}
		// add yard to this location
		_yard =_location.addSecondaryLocation(yardNameTextField.getText(), SecondaryLocation.YARD);
		// check yard length
		checkLength(_yard);
		// copy checkboxes
		copyCheckboxes();
		// store comment
		_yard.setComment(commentTextField.getText());
		// enable 
		enableButtons(true);
		// setup checkboxes
		selectCheckboxes(true);
		updateTrainDir();
		// save location file
		managerXml.writeOperationsLocationFile();
	}

	private void saveYard (SecondaryLocation y){
		// check that yard name is valid
		if (!checkName())
			return;
		// check to see if yard already exsists
		SecondaryLocation check = _location.getSecondaryLocationByName(yardNameTextField.getText(), SecondaryLocation.YARD);
		if (check != null && check != y){
			reportYardExists("save");
			return;
		}
		// check yard length
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
		y.setName(yardNameTextField.getText());
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
		if (yardNameTextField.getText().length() > MAX_NAME_LENGTH){
			log.error("Yard name must be less than "+ Integer.toString(MAX_NAME_LENGTH+1) +" charaters");
			JOptionPane.showMessageDialog(this,
					"Yard name must be less than "+ Integer.toString(MAX_NAME_LENGTH+1) +" charaters", "Can not add location!",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private boolean checkLength (SecondaryLocation y){	
		// convert yard length if in inches
		String length = yardLengthTextField.getText();
		if (length.endsWith("\"")){
			length = length.substring(0, length.length()-1);
			try {
				double inches = Double.parseDouble(length);
				int feet = (int)(inches * Setup.getScaleRatio() / 12);
				length = Integer.toString(feet);
			} catch (NumberFormatException e){
				log.error("Can not convert from inches to feet");
				JOptionPane.showMessageDialog(this,
						"Can not convert from inches to feet", "Yard length incorrect",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		// confirm that length is a number and less than 10000 feet
		int yardLength = 0;
		try {
			yardLength = Integer.parseInt(length);
			if (yardLength > 99999){
				log.error("Yard length must be less than 100,000 feet");
				JOptionPane.showMessageDialog(this,
						"Yard length must be less than 100,000 feet", "Yard length incorrect",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} catch (NumberFormatException e){
			log.error("yard length not an integer");
			JOptionPane.showMessageDialog(this,
					"Yard length must be a number", "Yard length incorrect",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// if everything is okay, save length
		y.setLength(yardLength);
		return true;
	}
	
	private void reportYardExists(String s){
		log.info("Can not " + s + ", yard already exists");
		JOptionPane.showMessageDialog(this,
				"Yard with this name already exists", "Can not " + s + " yard!",
				JOptionPane.ERROR_MESSAGE);
	}

	private void enableButtons(boolean enabled){
		northCheckBox.setEnabled(enabled);
		southCheckBox.setEnabled(enabled);
		eastCheckBox.setEnabled(enabled);
		westCheckBox.setEnabled(enabled);
		clearButton.setEnabled(enabled);
		setButton.setEnabled(enabled);
		deleteYardButton.setEnabled(enabled);
		saveYardButton.setEnabled(enabled);
		roadNameAll.setEnabled(enabled);
		roadNameInclude.setEnabled(enabled);
		roadNameExclude.setEnabled(enabled);
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
		if (ae.getSource() == roadNameAll)
			_yard.setRoadOption(_yard.ALLROADS);
		if (ae.getSource() == roadNameInclude)
			_yard.setRoadOption(_yard.INCLUDEROADS);
		if (ae.getSource() == roadNameExclude)
			_yard.setRoadOption(_yard.EXCLUDEROADS);
		
		updateRoadNames();
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
			if(_yard != null){
				if (enable)
					_yard.addTypeName(checkBox.getText());
				else
					_yard.deleteTypeName(checkBox.getText());
			}
		}
	}
	
	private void copyCheckboxes(){
		for (int i=0; i < checkBoxes.size(); i++){
			checkBox = (JCheckBox)checkBoxes.get(i);
			if (checkBox.isSelected() && _yard != null)
				_yard.addTypeName(checkBox.getText());
			else
				_yard.deleteTypeName(checkBox.getText());
		}
	}
	
	private void updateCheckboxes(){
		checkBoxes.clear();
		panelCheckBoxes.removeAll();
		int y = 0;		// vertical position in panel
		addItemFull(panelCheckBoxes, textType, 1, y++);

		String[]carTypes = CarTypes.instance().getNames();
		int x = 0;
		for (int i =0; i<carTypes.length; i++){
			if(_location.acceptsTypeName(carTypes[i])){
				JCheckBox checkBox = new javax.swing.JCheckBox();
				checkBoxes.add(checkBox);
				checkBox.setText(carTypes[i]);
				addCheckBoxAction(checkBox);
				addItemLeft(panelCheckBoxes, checkBox, x++, y);
				if(_yard != null && _yard.acceptsTypeName(carTypes[i]))
					checkBox.setSelected(true);
				if (x > 5){
					y++;
					x = 0;
				}
			}
		}
		enableCheckboxes(_yard != null);

		addItem (panelCheckBoxes, clearButton, 1, ++y);
		addItem (panelCheckBoxes, setButton, 4, y);

		Border border = BorderFactory.createEtchedBorder();
		panelCheckBoxes.setBorder(border);
		panelCheckBoxes.revalidate();

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

		if(_yard != null){
			// set radio button
			roadNameAll.setSelected(_yard.getRoadOption().equals(_yard.ALLROADS));
			roadNameInclude.setSelected(_yard.getRoadOption().equals(_yard.INCLUDEROADS));
			roadNameExclude.setSelected(_yard.getRoadOption().equals(_yard.EXCLUDEROADS));
			
			if (!roadNameAll.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	p.add(comboBox);
		    	p.add(addRoadButton);
		    	p.add(deleteRoadButton);
				gc.gridy = y++;
		    	panelRoadNames.add(p, gc);
		    	y++;

		    	String[]carRoads = _yard.getRoadNames();
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
		
		if (_yard != null){
			northCheckBox.setSelected((_yard.getTrainDirections() & _yard.NORTH) > 0);
			southCheckBox.setSelected((_yard.getTrainDirections() & _yard.SOUTH) > 0);
			eastCheckBox.setSelected((_yard.getTrainDirections() & _yard.EAST) > 0);
			westCheckBox.setSelected((_yard.getTrainDirections() & _yard.WEST) > 0);
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
			_yard.addTypeName(b.getText());
		}else{
			_yard.deleteTypeName(b.getText());
		}
	}
	
	private void addItem(JPanel p, JComponent c, int x, int y) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		p.add(c, gc);
	}
	
	private void addItemLeft(JPanel p, JComponent c, int x, int y) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		gc.anchor = gc.WEST;
		p.add(c, gc);
	}
	
	private void addItemFull(JPanel p, JComponent c, int x, int y) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.gridwidth = 3;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		gc.anchor = gc.WEST;
		p.add(c, gc);
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
			.getInstance(YardEditFrame.class.getName());
}
