// SidingEditFrame.java

package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsXml;
import jmri.jmrit.operations.setup.Setup;
import java.awt.*;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.Border;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Frame for user edit of a location sidings
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.6 $
 */

public class SidingEditFrame extends jmri.util.JmriJFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	LocationManager manager;
	LocationManagerXml managerXml;

	Location _location = null;
	Track _siding = null;
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
	javax.swing.JButton saveSidingButton = new javax.swing.JButton();
	javax.swing.JButton deleteSidingButton = new javax.swing.JButton();
	javax.swing.JButton addSidingButton = new javax.swing.JButton();
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
	javax.swing.JTextField sidingNameTextField = new javax.swing.JTextField(20);
	javax.swing.JTextField sidingLengthTextField = new javax.swing.JTextField(5);
	javax.swing.JTextField commentTextField = new javax.swing.JTextField(35);

	// for padding out panel
	javax.swing.JLabel space1 = new javax.swing.JLabel();
	javax.swing.JLabel space2 = new javax.swing.JLabel();
	javax.swing.JLabel space3 = new javax.swing.JLabel();
	
	// combo box
	javax.swing.JComboBox comboBox = CarRoads.instance().getComboBox();

	public static final String DISPOSE = "dispose" ;
	public static final int MAX_NAME_LENGTH = 25;

	public SidingEditFrame() {
		super();
	}

	public void initComponents(Location location, Track siding) {
		_location = location;
		_location.addPropertyChangeListener(this);
		_siding = siding;

		// load managers
		managerXml = LocationManagerXml.instance();

		// the following code sets the frame's initial state
		
		textName.setText(rb.getString("Name"));
		textName.setVisible(true);

		textLength.setText(rb.getString("Length"));
		textLength.setVisible(true);
		textTrain.setText(rb.getString("TrainSiding"));
		textTrain.setVisible(true);
		northCheckBox.setText(rb.getString("North"));
		southCheckBox.setText(rb.getString("South"));
		eastCheckBox.setText(rb.getString("East"));
		westCheckBox.setText(rb.getString("West"));
		textType.setText(rb.getString("TypesSiding"));
		textType.setVisible(true);
		textRoad.setText(rb.getString("RoadsSiding"));
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

		deleteSidingButton.setText(rb.getString("DeleteSiding"));
		deleteSidingButton.setVisible(true);
		addSidingButton.setText(rb.getString("AddSiding"));
		addSidingButton.setVisible(true);
		saveSidingButton.setText(rb.getString("SaveSiding"));
		saveSidingButton.setVisible(true);
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
		addItemFull(p1, sidingNameTextField, 1, 1);

		// row 2
		addItem(p1, textLength, 0, 2);
		addItemLeft(p1, sidingLengthTextField, 1, 2);
		
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
		addItem(p4, deleteSidingButton, 0, ++y);
		addItem(p4, addSidingButton, 1, y);
		addItem(p4, saveSidingButton, 3, y);
		
		getContentPane().add(p1);
		getContentPane().add(panelTrainDir);
		getContentPane().add(panelCheckBoxes);
		getContentPane().add(panelRoadNames);
       	getContentPane().add(p4);
		
		// setup buttons
		addButtonAction(setButton);
		addButtonAction(clearButton);
		addButtonAction(deleteSidingButton);
		addButtonAction(addSidingButton);
		addButtonAction(saveSidingButton);
		addButtonAction(deleteRoadButton);
		addButtonAction(addRoadButton);
		
		addRadioButtonAction(roadNameAll);
		addRadioButtonAction(roadNameInclude);
		addRadioButtonAction(roadNameExclude);
		
		// load fields and enable buttons
		if (_siding !=null){
			sidingNameTextField.setText(_siding.getName());
			commentTextField.setText(_siding.getComment());
			sidingLengthTextField.setText(Integer.toString(_siding.getLength()));
			enableButtons(true);
		} else {
			enableButtons(false);
		}
 
		// build menu
		JMenuBar menuBar = new JMenuBar();
//		JMenu toolMenu = new JMenu("Tools");
//		menuBar.add(toolMenu);
//		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Sidings", true);

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
		if (ae.getSource() == saveSidingButton){
			log.debug("siding save button actived");
			if (_siding != null){
				saveSiding(_siding);
			} else {
				addNewSiding();
			}
		}
		if (ae.getSource() == deleteSidingButton){
			log.debug("siding delete button actived");
//			Track y = _location.getTrackByName(sidingNameTextField.getText());
			if (_siding != null){
				int cars = _siding.getNumberRS();
				if (cars > 0){
					if (JOptionPane.showConfirmDialog(this,
							"There are " + cars + " cars at this location, delete?", "Delete location?",
							JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
						return;
					}
				}
				selectCheckboxes(false);
				_location.deleteTrack(_siding);
				_siding = null;
				enableButtons(false);
				// save location file
				managerXml.writeOperationsLocationFile();
			}
		}
		if (ae.getSource() == addSidingButton){
			addNewSiding();
		}
		if (ae.getSource() == addRoadButton){
			_siding.addRoadName((String) comboBox.getSelectedItem());
			updateRoadNames();
		}
		if (ae.getSource() == deleteRoadButton){
			_siding.deleteRoadName((String) comboBox.getSelectedItem());
			updateRoadNames();
		}
		if (ae.getSource() == setButton){
			selectCheckboxes(true);
		}
		if (ae.getSource() == clearButton){
			selectCheckboxes(false);
		}
	}

	private void addNewSiding(){
		// check that siding name is valid
		if (!checkName())
			return;
		// check to see if siding already exsists
		Track check = _location.getTrackByName(sidingNameTextField.getText(), Track.SIDING);
		if (check != null){
			reportSidingExists("add");
			return;
		}
		// add siding to this location
		_siding =_location.addTrack(sidingNameTextField.getText(), Track.SIDING);
		// check siding length
		checkLength(_siding);
		// copy checkboxes
		copyCheckboxes();
		// store comment
		_siding.setComment(commentTextField.getText());
		// enable 
		enableButtons(true);
		// setup checkboxes
		selectCheckboxes(true);
		updateTrainDir();
		// save location file
		managerXml.writeOperationsLocationFile();
	}

	private void saveSiding (Track track){
		// check that siding name is valid
		if (!checkName())
			return;
		// check to see if siding already exsists
		Track check = _location.getTrackByName(sidingNameTextField.getText(), Track.SIDING);
		if (check != null && check != track){
			reportSidingExists("save");
			return;
		}
		// check siding length
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
		track.setName(sidingNameTextField.getText());
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
		if (sidingNameTextField.getText().length() > MAX_NAME_LENGTH){
			log.error("Siding name must be less than "+ Integer.toString(MAX_NAME_LENGTH+1) +" charaters");
			JOptionPane.showMessageDialog(this,
					"Siding name must be less than "+ Integer.toString(MAX_NAME_LENGTH+1) +" charaters", "Can not add location!",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private boolean checkLength (Track track){	
		// convert siding length if in inches
		String length = sidingLengthTextField.getText();
		if (length.endsWith("\"")){
			length = length.substring(0, length.length()-1);
			try {
				double inches = Double.parseDouble(length);
				int feet = (int)(inches * Setup.getScaleRatio() / 12);
				length = Integer.toString(feet);
			} catch (NumberFormatException e){
				log.error("Can not convert from inches to feet");
				JOptionPane.showMessageDialog(this,
						"Can not convert from inches to feet", "Siding length incorrect",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		// confirm that length is a number and less than 10000 feet
		int sidingLength = 0;
		try {
			sidingLength = Integer.parseInt(length);
			if (sidingLength > 99999){
				log.error("Siding length must be less than 100,000 feet");
				JOptionPane.showMessageDialog(this,
						"Siding length must be less than 100,000 feet", "Siding length incorrect",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} catch (NumberFormatException e){
			log.error("siding length not an integer");
			JOptionPane.showMessageDialog(this,
					"Siding length must be a number", "Siding length incorrect",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// if everything is okay, save length
		track.setLength(sidingLength);
		return true;
	}
	
	private void reportSidingExists(String s){
		log.info("Can not " + s + ", siding already exists");
		JOptionPane.showMessageDialog(this,
				"Siding with this name already exists", "Can not " + s + " siding!",
				JOptionPane.ERROR_MESSAGE);
	}

	private void enableButtons(boolean enabled){
		northCheckBox.setEnabled(enabled);
		southCheckBox.setEnabled(enabled);
		eastCheckBox.setEnabled(enabled);
		westCheckBox.setEnabled(enabled);
		clearButton.setEnabled(enabled);
		setButton.setEnabled(enabled);
		deleteSidingButton.setEnabled(enabled);
		saveSidingButton.setEnabled(enabled);
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
			_siding.setRoadOption(_siding.ALLROADS);
		if (ae.getSource() == roadNameInclude)
			_siding.setRoadOption(_siding.INCLUDEROADS);
		if (ae.getSource() == roadNameExclude)
			_siding.setRoadOption(_siding.EXCLUDEROADS);
		
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
			if(_siding != null){
				if (enable)
					_siding.addTypeName(checkBox.getText());
				else
					_siding.deleteTypeName(checkBox.getText());
			}
		}
	}
	
	private void copyCheckboxes(){
		for (int i=0; i < checkBoxes.size(); i++){
			checkBox = (JCheckBox)checkBoxes.get(i);
			if (checkBox.isSelected() && _siding != null)
				_siding.addTypeName(checkBox.getText());
			else
				_siding.deleteTypeName(checkBox.getText());
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
				if(_siding != null && _siding.acceptsTypeName(carTypes[i]))
					checkBox.setSelected(true);
				if (x > 5){
					y++;
					x = 0;
				}
			}
		}
		enableCheckboxes(_siding != null);

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

		if(_siding != null){
			// set radio button
			roadNameAll.setSelected(_siding.getRoadOption().equals(_siding.ALLROADS));
			roadNameInclude.setSelected(_siding.getRoadOption().equals(_siding.INCLUDEROADS));
			roadNameExclude.setSelected(_siding.getRoadOption().equals(_siding.EXCLUDEROADS));
			
			if (!roadNameAll.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	p.add(comboBox);
		    	p.add(addRoadButton);
		    	p.add(deleteRoadButton);
				gc.gridy = y++;
		    	panelRoadNames.add(p, gc);

		    	String[]carRoads = _siding.getRoadNames();
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
		
		if (_siding != null){
			northCheckBox.setSelected((_siding.getTrainDirections() & _siding.NORTH) > 0);
			southCheckBox.setSelected((_siding.getTrainDirections() & _siding.SOUTH) > 0);
			eastCheckBox.setSelected((_siding.getTrainDirections() & _siding.EAST) > 0);
			westCheckBox.setSelected((_siding.getTrainDirections() & _siding.WEST) > 0);
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
			_siding.addTypeName(b.getText());
		}else{
			_siding.deleteTypeName(b.getText());
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
		if (e.getPropertyName().equals(Location.TYPES)){
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
			.getInstance(SidingEditFrame.class.getName());
}
