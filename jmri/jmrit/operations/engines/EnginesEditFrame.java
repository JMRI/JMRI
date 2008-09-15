//EnginesEditFrame.java

package jmri.jmrit.operations.engines;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.SecondaryLocation;
import jmri.jmrit.operations.setup.OperationsXml;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.jmrit.operations.cars.CarManagerXml;
import jmri.jmrit.operations.cars.CarRoads;
import jmri.jmrit.operations.cars.CarOwners;
import jmri.jmrit.operations.OperationsFrame;


import java.awt.*;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.*;

import java.io.*;
import java.util.ResourceBundle;


/**
 * Frame for user edit of engine
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.4 $
 */

public class EnginesEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.engines.JmritOperationsEnginesBundle");

	EngineManager manager = EngineManager.instance();
	EngineManagerXml managerXml = EngineManagerXml.instance();
	CarManagerXml carManagerXml = CarManagerXml.instance();
	TrainManagerXml trainManagerXml = TrainManagerXml.instance();
	LocationManager locationManager = LocationManager.instance();

	Engine _engine;

	// labels
	javax.swing.JLabel textRoad = new javax.swing.JLabel();
	javax.swing.JLabel textRoadNumber = new javax.swing.JLabel();
	javax.swing.JLabel textColor = new javax.swing.JLabel();
	javax.swing.JLabel textBuilt = new javax.swing.JLabel();
	javax.swing.JLabel textLength = new javax.swing.JLabel();
	javax.swing.JLabel textModel = new javax.swing.JLabel();
	javax.swing.JLabel textHp = new javax.swing.JLabel();
	javax.swing.JLabel textType = new javax.swing.JLabel();
	javax.swing.JLabel textEngineType = new javax.swing.JLabel();
	javax.swing.JLabel textWeight = new javax.swing.JLabel();
	javax.swing.JLabel textLocation = new javax.swing.JLabel();
	javax.swing.JLabel textOptional = new javax.swing.JLabel();
	javax.swing.JLabel textKernel = new javax.swing.JLabel();
	javax.swing.JLabel textOwner = new javax.swing.JLabel();
	javax.swing.JLabel textComment = new javax.swing.JLabel();

	// major buttons
	javax.swing.JButton editRoadButton = new javax.swing.JButton();
	javax.swing.JButton clearRoadNumberButton = new javax.swing.JButton();
	javax.swing.JButton editModelButton = new javax.swing.JButton();
	javax.swing.JButton editTypeButton = new javax.swing.JButton();
	javax.swing.JButton editColorButton = new javax.swing.JButton();
	javax.swing.JButton editLengthButton = new javax.swing.JButton();
	javax.swing.JButton fillWeightButton = new javax.swing.JButton();
	javax.swing.JButton editKernelButton = new javax.swing.JButton();
	javax.swing.JButton editOwnerButton = new javax.swing.JButton();

	javax.swing.JButton saveButton = new javax.swing.JButton();
	javax.swing.JButton deleteButton = new javax.swing.JButton();
	javax.swing.JButton copyButton = new javax.swing.JButton();
	javax.swing.JButton addButton = new javax.swing.JButton();

	// check boxes

	// text field
	javax.swing.JTextField roadNumberTextField = new javax.swing.JTextField(8);
	javax.swing.JTextField builtTextField = new javax.swing.JTextField(8);
	javax.swing.JTextField hpTextField = new javax.swing.JTextField(8);
	javax.swing.JTextField weightTextField = new javax.swing.JTextField(4);
	javax.swing.JTextField commentTextField = new javax.swing.JTextField(35);

	// for padding out panel
	javax.swing.JLabel space1 = new javax.swing.JLabel();
	javax.swing.JLabel space2 = new javax.swing.JLabel();
	javax.swing.JLabel space3 = new javax.swing.JLabel();

	// combo boxes
	javax.swing.JComboBox roadComboBox = CarRoads.instance().getComboBox();
	javax.swing.JComboBox modelComboBox = EngineModels.instance().getComboBox();
	javax.swing.JComboBox lengthComboBox = EngineLengths.instance().getComboBox();
	javax.swing.JComboBox ownerComboBox = CarOwners.instance().getComboBox();
	javax.swing.JComboBox locationBox = locationManager.getComboBox();
	javax.swing.JComboBox secondaryLocationBox = new javax.swing.JComboBox();
	javax.swing.JComboBox consistComboBox = manager.getConsistComboBox(); 

	public static final String ROAD = rb.getString("Road");
	public static final String MODEL = rb.getString("Model");
	public static final String COLOR = rb.getString("Color");
	public static final String LENGTH = rb.getString("Length");
	public static final String OWNER = rb.getString("Owner");
	public static final String CONSIST = rb.getString("Consist");
	public static final String DISPOSE = "dispose" ;

	public EnginesEditFrame() {
		super();
	}

	public void initComponents() {

		// load managers
		OperationsXml.instance();					// force settings to load 

		// the following code sets the frame's initial state

		textRoad.setText(rb.getString("Road"));
		textRoad.setVisible(true);
		textRoadNumber.setText(rb.getString("RoadNumber"));
		textRoadNumber.setVisible(true);
		textModel.setText(rb.getString("Model"));
		textModel.setVisible(true);
		textType.setText(rb.getString("Type"));
		textType.setVisible(true);
		textEngineType.setText("");
		textEngineType.setVisible(true);
		textBuilt.setText(rb.getString("BuildDate"));
		textBuilt.setVisible(true);
		textLength.setText(rb.getString("Length"));
		textLength.setVisible(true);
		textHp.setText(rb.getString("Hp"));
		textHp.setVisible(true);
		textLocation.setText(rb.getString("Location"));
		textLocation.setVisible(true);
		textOptional.setText("-------------------------------- Optional ------------------------------------");
		textOptional.setVisible(true);
		textKernel.setText(rb.getString("Consist"));
		textKernel.setVisible(true);
		textOwner.setText(rb.getString("Owner"));
		textOwner.setVisible(true);
		textComment.setText(rb.getString("Comment"));
		textComment.setVisible(true);
		space1.setText("      ");
		space1.setVisible(true);

		editRoadButton.setText(rb.getString("Edit"));
		editRoadButton.setVisible(true);
		clearRoadNumberButton.setText(rb.getString("Clear"));
		editRoadButton.setVisible(true);
		editModelButton.setText(rb.getString("Edit"));
		editModelButton.setVisible(true);
		editTypeButton.setText(rb.getString("Edit"));
		editTypeButton.setVisible(true);
		editTypeButton.setEnabled(false);			// need to add code for this button
		editColorButton.setText(rb.getString("Edit"));
		editColorButton.setVisible(true);
		editLengthButton.setText(rb.getString("Edit"));
		editLengthButton.setVisible(true);
		editKernelButton.setText(rb.getString("Edit"));
		editKernelButton.setVisible(true);
		editOwnerButton.setText(rb.getString("Edit"));
		editOwnerButton.setVisible(true);
		deleteButton.setText(rb.getString("Delete"));
		deleteButton.setVisible(true);
		addButton.setText(rb.getString("Add"));
		addButton.setVisible(true);
		saveButton.setText(rb.getString("Save"));
		saveButton.setVisible(true);
		copyButton.setText(rb.getString("Copy"));
		copyButton.setVisible(true);

		getContentPane().setLayout(new GridBagLayout());

		// Layout the panel by rows
		// row 1
		addItem(textRoad, 0, 1);
		addItem(roadComboBox, 1, 1);
		addItem(editRoadButton, 2, 1);
		// row 2
		addItem(textRoadNumber, 0, 2);
		addItem(roadNumberTextField, 1, 2);
		addItem(clearRoadNumberButton, 2, 2);
		// row 3
		addItem(textModel, 0, 3);
		addItem(modelComboBox, 1, 3);
		addItem(editModelButton, 2, 3);
		// row4
		addItem(textType, 0, 4);
		addItem(textEngineType, 1, 4);
		addItem(editTypeButton, 2, 4);
		// row 5
		addItem(textLength, 0, 5);
		addItem(lengthComboBox, 1, 5);
		addItem(editLengthButton, 2, 5);

		// row 7
		addItem(textHp, 0, 7);
		addItem(hpTextField, 1, 7);

		// row 8

		// row 9
		addItem(textLocation, 0, 9);
		addItem(locationBox, 1, 9);
		addItem(secondaryLocationBox, 2, 9);

		// Separator row 10
		addItemWidth(textOptional, 3, 0, 10);
		
		// row 13
		addItem(textKernel, 0, 13);
		addItem(consistComboBox, 1, 13);
		addItem(editKernelButton, 2, 13);

		// row 14
		addItem(textBuilt, 0, 14);
		addItem(builtTextField, 1, 14);
		
		// row 15
		addItem(textOwner, 0, 15);
		addItem(ownerComboBox, 1, 15);
		addItem(editOwnerButton, 2, 15);

		// row 16
		addItem(textComment, 0, 16);
		addItemWidth(commentTextField, 3, 1, 16);

		// row 20
		addItem(space1, 0, 20);
		// row 21
		addItem(deleteButton, 0, 21);
		addItem(addButton, 1, 21);
//		addItem(copyButton, 2, 21);
		addItem(saveButton, 3, 21);

		// setup buttons
		addEditButtonAction(editRoadButton);
		addButtonAction(clearRoadNumberButton);
		addEditButtonAction(editModelButton);
		addEditButtonAction(editLengthButton);
		addEditButtonAction(editColorButton);
		addEditButtonAction(editKernelButton);
		addEditButtonAction(editOwnerButton);

		addButtonAction(deleteButton);
		addButtonAction(addButton);
		addButtonAction(copyButton);
		addButtonAction(saveButton);
		addButtonAction(fillWeightButton);

		// setup combobox
		addComboBoxAction(lengthComboBox);
		addComboBoxAction(locationBox);
		
		// setup checkbox

		// build menu
		JMenuBar menuBar = new JMenuBar();
//		JMenu toolMenu = new JMenu("Tools");
//		menuBar.add(toolMenu);
//		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Engines", true);

		//	 get notified if combo box gets modified
		CarRoads.instance().addPropertyChangeListener(this);
		EngineModels.instance().addPropertyChangeListener(this);
		EngineLengths.instance().addPropertyChangeListener(this);
		CarOwners.instance().addPropertyChangeListener(this);
		locationManager.addPropertyChangeListener(this);
		manager.addPropertyChangeListener(this);

		// set frame size and location for display
		pack();
		if ( (getWidth()<400)) 
			setSize(450, getHeight()+50);
		else
			setSize(getWidth()+50, getHeight()+50);
		setLocation(500, 300);
		setVisible(true);	
	}

	public void loadEngine(Engine engine){
		_engine = engine;

		if (!CarRoads.instance().containsName(engine.getRoad())){
			if (JOptionPane.showConfirmDialog(this,
					"This engine's road name \""+ engine.getRoad() + "\" doesn't exist in your roster, add? ", "Add road name?",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarRoads.instance().addName(engine.getRoad());
			}
		}
		roadComboBox.setSelectedItem(engine.getRoad());

		roadNumberTextField.setText(engine.getNumber());

		if (!EngineModels.instance().containsName(engine.getModel())){
			if (JOptionPane.showConfirmDialog(this,
					"This engine's model \"\"" + engine.getModel() + "\" doesn't exist in your roster, add? ", "Add engine model?",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				EngineModels.instance().addName(engine.getModel());
			}
		}
		modelComboBox.setSelectedItem(engine.getModel());
		textEngineType.setText(engine.getType());

		if (!EngineLengths.instance().containsName(engine.getLength())){
			if (JOptionPane.showConfirmDialog(this,
					"This engine's length \"" + engine.getLength() + "\" doesn't exist in your roster, add? ", "Add engine length?",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				EngineLengths.instance().addName(engine.getLength());
			}
		}
		lengthComboBox.setSelectedItem(engine.getLength());
		hpTextField.setText(engine.getHp());

		locationBox.setSelectedItem(engine.getLocation());
		Location l = locationManager.getLocationById(engine.getLocationId());
		if (l != null){
			l.updateComboBox(secondaryLocationBox);
			secondaryLocationBox.setSelectedItem(engine.getSecondaryLocation());
		} else {
			secondaryLocationBox.removeAllItems();
		}

		builtTextField.setText(engine.getBuilt());

		if (!CarOwners.instance().containsName(engine.getOwner())){
			if (JOptionPane.showConfirmDialog(this,
					"This engine's owner \"" + engine.getOwner() + "\" doesn't exist in your roster, add? ", "Add engine owner?",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarOwners.instance().addName(engine.getOwner());
			}
		}
		consistComboBox.setSelectedItem(engine.getConsistName());
				
		ownerComboBox.setSelectedItem(engine.getOwner());

		commentTextField.setText(engine.getComment());
	}

	private void addComboBoxAction(JComboBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				comboBoxActionPerformed(e);
			}
		});
	}

	// combo boxes
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource()== locationBox){
			if (locationBox.getSelectedItem() != null){
				if (locationBox.getSelectedItem().equals("")){
					secondaryLocationBox.removeAllItems();
				}else{
					log.debug("EnginesSetFrame sees location: "+ locationBox.getSelectedItem());
					Location l = ((Location)locationBox.getSelectedItem());
					l.updateComboBox(secondaryLocationBox);
				}
			}
		}
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
	}

	private void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
	}

	// Save, Delete, Add, Clear, Calculate buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton){
			// log.debug("engine save button actived");
			String roadNum = roadNumberTextField.getText();
			if (roadNum.length() > 10){
				JOptionPane.showMessageDialog(this,rb.getString("engineRoadNum"),
						"Engine road number too long!",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			// check to see if engine with road and number already exists
			Engine engine = manager.getEngineByRoadAndNumber(roadComboBox.getSelectedItem().toString(), roadNumberTextField.getText());
			if (engine != null){
				if (_engine == null || !engine.getId().equals(_engine.getId())){
					JOptionPane.showMessageDialog(this,
							"Engine with road name and number already exists", "Can not save engine!",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}

			// delete engine if edit and road or road number has changed
			if (_engine != null){
				if (_engine.getRoad() != null && !_engine.getRoad().equals("")){
					if (!_engine.getRoad().equals(roadComboBox.getSelectedItem().toString())
							|| !_engine.getNumber().equals(roadNumberTextField.getText())) {
						// transfer engine attributes since road name and number have changed
						Engine c = manager.newEngine(_engine.getRoad(), _engine.getNumber());
						Engine newEngine = addEngine();
						newEngine.setDestination(c.getDestination(), c.getSecondaryDestination());
						newEngine.setTrain(c.getTrain());
						manager.deregister(c);
						managerXml.writeOperationsEngineFile();
						trainManagerXml.writeOperationsTrainFile();
						return;
					}
				}
			}
			addEngine ();
			// save engine file
			managerXml.writeOperationsEngineFile();
			trainManagerXml.writeOperationsTrainFile();
		}
		if (ae.getSource() == deleteButton){
			log.debug("engine delete button actived");
			Engine c = manager.newEngine(roadComboBox.getSelectedItem().toString(), roadNumberTextField.getText() );
			manager.deregister(c);
			// save engine file
			managerXml.writeOperationsEngineFile();
			trainManagerXml.writeOperationsTrainFile();
		}
		if (ae.getSource() == addButton){
			String roadNum = roadNumberTextField.getText();
			if (roadNum.length() > 10){
				JOptionPane.showMessageDialog(this,rb.getString("engineRoadNum"),
						"Engine road number too long!",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			Engine c = manager.getEngineByRoadAndNumber(roadComboBox.getSelectedItem().toString(), roadNumberTextField.getText() );
			if (c != null){
				log.info("Can not add, engine already exists");
				JOptionPane.showMessageDialog(this,
						"Engine with road name and number already exists", "Can not add engine!",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			addEngine();
			// save engine file
			managerXml.writeOperationsEngineFile();
			trainManagerXml.writeOperationsTrainFile();
		}
		if (ae.getSource() == clearRoadNumberButton){
			roadNumberTextField.setText("");
			roadNumberTextField.requestFocus();
		}
	}

	private Engine addEngine() {
		if (roadComboBox.getSelectedItem() != null
				&& !roadComboBox.getSelectedItem().toString().equals("")) {
			Engine engine = manager.newEngine(roadComboBox.getSelectedItem().toString(),
					roadNumberTextField.getText());
			_engine = engine;
			if (modelComboBox.getSelectedItem() != null)
				engine.setModel(modelComboBox.getSelectedItem().toString());
			if (lengthComboBox.getSelectedItem() != null)
				engine.setLength(lengthComboBox.getSelectedItem().toString());
			engine.setBuilt(builtTextField.getText());
			if (ownerComboBox.getSelectedItem() != null)
				engine.setOwner(ownerComboBox.getSelectedItem().toString());
			if (consistComboBox.getSelectedItem() != null){
				if (consistComboBox.getSelectedItem().equals(""))
					engine.setConsist(null);
				else
					engine.setConsist(manager.getConsistByName((String)consistComboBox.getSelectedItem()));
			}
			// confirm that horsepower is a number
			if (!hpTextField.getText().equals("") ){
				try{
					Integer.parseInt(hpTextField.getText());
					engine.setHp(hpTextField.getText());
				} catch (Exception e){
					JOptionPane.showMessageDialog(this,
							"Engine horsepower must be a number", "Can not save engine horsepower!",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			if (locationBox.getSelectedItem() != null){
				if (locationBox.getSelectedItem().equals("")) {
					_engine.setLocation(null, null);
				} else {
					if (secondaryLocationBox.getSelectedItem() == null
							|| secondaryLocationBox.getSelectedItem()
							.equals("")) {
						JOptionPane.showMessageDialog(this,
								"Must fully select a engine's location",
								"Can not update engine location",
								JOptionPane.ERROR_MESSAGE);

					} else {
						String status = _engine.setLocation((Location)locationBox.getSelectedItem(),
								(SecondaryLocation)secondaryLocationBox.getSelectedItem());
						if (!status.equals(Engine.OKAY)){
							log.debug ("Can't set engine's location because of "+ status);
							JOptionPane.showMessageDialog(this,
									"Can't set engine's location because of location's "+ status,
									"Can not update engine location",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			engine.setComment(commentTextField.getText());
			return engine;
		}
		return null;
	}

	private void addEditButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonEditActionPerformed(e);
			}
		});
	}

	private boolean editActive = false;
	EngineAttributeEditFrame f;

	// edit buttons only one frame active at a time
	public void buttonEditActionPerformed(java.awt.event.ActionEvent ae) {
		if (editActive){
			f.dispose();
		}
		f = new EngineAttributeEditFrame();
		f.setLocationRelativeTo(this);
		f.addPropertyChangeListener(this);
		editActive = true;

		if(ae.getSource() == editRoadButton)
			f.initComponents(ROAD);
		if(ae.getSource() == editModelButton)
			f.initComponents(MODEL);
		if(ae.getSource() == editColorButton)
			f.initComponents(COLOR);
		if(ae.getSource() == editLengthButton)
			f.initComponents(LENGTH);
		if(ae.getSource() == editOwnerButton)
			f.initComponents(OWNER);
		if(ae.getSource() == editKernelButton)
			f.initComponents(CONSIST);
	}

//	Need to notify EnginesTableFrame that this frame has been disposed 
	public void dispose(){
		// keep listerners, frame is reused
		//removePropertyChangeListeners();
		super.dispose();
	}

	private void removePropertyChangeListeners(){
		CarRoads.instance().removePropertyChangeListener(this);
		EngineModels.instance().removePropertyChangeListener(this);
		EngineLengths.instance().removePropertyChangeListener(this);
		CarOwners.instance().removePropertyChangeListener(this);
		locationManager.removePropertyChangeListener(this);
		manager.removePropertyChangeListener(this);
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("EnginesEditFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());
		if (e.getPropertyName().equals(CarRoads.CARROADS)){
			CarRoads.instance().updateComboBox(roadComboBox);
			if (_engine != null)
			roadComboBox.setSelectedItem(_engine.getRoad());
		}
		if (e.getPropertyName().equals(EngineModels.ENGINEMODELS)){
			EngineModels.instance().updateComboBox(modelComboBox);
			if (_engine != null)
				modelComboBox.setSelectedItem(_engine.getType());
		}
		if (e.getPropertyName().equals(EngineLengths.ENGINELENGTHS)){
			EngineLengths.instance().updateComboBox(lengthComboBox);
			if (_engine != null)
				lengthComboBox.setSelectedItem(_engine.getLength());
		}
		if (e.getPropertyName().equals(EngineManager.CONSISTLISTLENGTH)){
			manager.updateConsistComboBox(consistComboBox);
			if (_engine != null) 
				consistComboBox.setSelectedItem(_engine.getKernelName());
		}
		if (e.getPropertyName().equals(CarOwners.CAROWNERS)){
			CarOwners.instance().updateComboBox(ownerComboBox);
			if (_engine != null)
				ownerComboBox.setSelectedItem(_engine.getOwner());
		}
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH)){
			LocationManager.instance().updateComboBox(locationBox);
			if (_engine != null)
				locationBox.setSelectedItem(_engine.getLocation());
		}
		if (e.getPropertyName().equals(DISPOSE)){
			editActive = false;
		}
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
	.getInstance(EnginesEditFrame.class.getName());
}
