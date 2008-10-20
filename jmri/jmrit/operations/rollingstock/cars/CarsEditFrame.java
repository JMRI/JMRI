//CarsEditFrame.java

package jmri.jmrit.operations.rollingstock.cars;

import jmri.jmrit.operations.locations.LocationManager; 
import jmri.jmrit.operations.locations.Location; 
import jmri.jmrit.operations.locations.Track; 
import jmri.jmrit.operations.setup.Setup; 
import jmri.jmrit.operations.setup.Control; 
import jmri.jmrit.operations.OperationsFrame; 

import java.awt.*;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.*;

import java.io.*;
import java.util.ResourceBundle;


/**
 * Frame for user edit of car
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.2 $
 */

public class CarsEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");

	CarManager manager = CarManager.instance();
	CarManagerXml managerXml = CarManagerXml.instance();
	LocationManager locationManager = LocationManager.instance();

	Car _car;

	// labels
	javax.swing.JLabel textRoad = new javax.swing.JLabel();
	javax.swing.JLabel textRoadNumber = new javax.swing.JLabel();
	javax.swing.JLabel textColor = new javax.swing.JLabel();
	javax.swing.JLabel textBuilt = new javax.swing.JLabel();
	javax.swing.JLabel textLength = new javax.swing.JLabel();
	javax.swing.JLabel textType = new javax.swing.JLabel();
	javax.swing.JLabel textWeight = new javax.swing.JLabel();
	javax.swing.JLabel textWeightTons = new javax.swing.JLabel();
	javax.swing.JLabel textLocation = new javax.swing.JLabel();
	javax.swing.JLabel textOptional = new javax.swing.JLabel();
	javax.swing.JLabel textKernel = new javax.swing.JLabel();
	javax.swing.JLabel textOwner = new javax.swing.JLabel();
	javax.swing.JLabel textComment = new javax.swing.JLabel();

	// major buttons
	javax.swing.JButton editRoadButton = new javax.swing.JButton();
	javax.swing.JButton clearRoadNumberButton = new javax.swing.JButton();
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
	javax.swing.JCheckBox autoCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox cabooseCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox fredCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox hazardousCheckBox = new javax.swing.JCheckBox();

	// text field
	javax.swing.JTextField roadNumberTextField = new javax.swing.JTextField(8);
	javax.swing.JTextField builtTextField = new javax.swing.JTextField(8);
	javax.swing.JTextField weightTextField = new javax.swing.JTextField(4);
	javax.swing.JTextField weightTonsTextField = new javax.swing.JTextField(4);
	javax.swing.JTextField commentTextField = new javax.swing.JTextField(35);

	// for padding out panel
	javax.swing.JLabel space1 = new javax.swing.JLabel();
	javax.swing.JLabel space2 = new javax.swing.JLabel();
	javax.swing.JLabel space3 = new javax.swing.JLabel();

	// combo boxes
	javax.swing.JComboBox roadComboBox = CarRoads.instance().getComboBox();
	javax.swing.JComboBox typeComboBox = CarTypes.instance().getComboBox();
	javax.swing.JComboBox colorComboBox = CarColors.instance().getComboBox();
	javax.swing.JComboBox lengthComboBox = CarLengths.instance().getComboBox();
	javax.swing.JComboBox ownerComboBox = CarOwners.instance().getComboBox();
	javax.swing.JComboBox locationBox = locationManager.getComboBox();
	javax.swing.JComboBox trackLocationBox = new javax.swing.JComboBox();
	javax.swing.JComboBox kernelComboBox = manager.getKernelComboBox(); 

	public static final String ROAD = rb.getString("Road");
	public static final String TYPE = rb.getString("Type");
	public static final String COLOR = rb.getString("Color");
	public static final String LENGTH = rb.getString("Length");
	public static final String OWNER = rb.getString("Owner");
	public static final String KERNEL = rb.getString("Kernel");
	public static final String DISPOSE = "dispose" ;

	public CarsEditFrame() {
		super();
	}

	public void initComponents() {
		// the following code sets the frame's initial state
		textRoad.setText(rb.getString("Road"));
		textRoad.setVisible(true);
		textRoadNumber.setText(rb.getString("RoadNumber"));
		textRoadNumber.setVisible(true);
		textType.setText(rb.getString("Type"));
		textType.setVisible(true);
		textColor.setText(rb.getString("Color"));
		textColor.setVisible(true);
		textBuilt.setText(rb.getString("BuildDate"));
		textBuilt.setVisible(true);
		textLength.setText(rb.getString("Length"));
		textLength.setVisible(true);
		textWeight.setText(rb.getString("Weight"));
		textWeight.setVisible(true);
		weightTextField.setToolTipText(rb.getString("carWeightOz"));
		textWeightTons.setText(rb.getString("WeightTons"));
		textWeightTons.setVisible(true);
		weightTonsTextField.setToolTipText(rb.getString("carWeightTons"));
		autoCheckBox.setText(rb.getString("Auto"));
		autoCheckBox.setSelected(true);
		cabooseCheckBox.setText(rb.getString("Caboose"));
		cabooseCheckBox.setSelected(false);
		fredCheckBox.setText(rb.getString("Fred"));
		fredCheckBox.setSelected(false);
		hazardousCheckBox.setText(rb.getString("Hazardous"));
		hazardousCheckBox.setSelected(false);
		textLocation.setText(rb.getString("Location"));
		textLocation.setVisible(true);
		textOptional.setText("-------------------------------- Optional ------------------------------------");
		textOptional.setVisible(true);
		textKernel.setText(rb.getString("Kernel"));
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
		editTypeButton.setText(rb.getString("Edit"));
		editTypeButton.setVisible(true);
		editColorButton.setText(rb.getString("Edit"));
		editColorButton.setVisible(true);
		editLengthButton.setText(rb.getString("Edit"));
		editLengthButton.setVisible(true);
		fillWeightButton.setText(rb.getString("Calculate"));
		fillWeightButton.setToolTipText(rb.getString("calculateCarWeight"));
		fillWeightButton.setVisible(true);
		editKernelButton.setText(rb.getString("Edit"));
		editKernelButton.setVisible(true);
		builtTextField.setToolTipText(rb.getString("buildDate"));
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
		addItem(textType, 0, 3);
		addItem(typeComboBox, 1, 3);
		addItem(editTypeButton, 2, 3);
		// row 4
		addItem(textLength, 0, 4);
		addItem(lengthComboBox, 1, 4);
		addItem(editLengthButton, 2, 4);
		// row 5
		addItem(textColor, 0, 5);
		addItem(colorComboBox, 1, 5);
		addItem(editColorButton, 2, 5);

		// row 7
		addItem(textWeight, 0, 7);
		addItem(weightTextField, 1, 7);
		addItem(fillWeightButton, 2, 7);
		addItem(autoCheckBox, 3, 7);
		
		// row 8
		addItem(textWeightTons, 0, 8);
		addItem(weightTonsTextField, 1, 8);
		
		// row 10
		addItem(cabooseCheckBox, 1, 10);
		addItem(fredCheckBox, 2, 10);
		addItem(hazardousCheckBox, 3, 10);

		// row 11
		addItem(textLocation, 0, 11);
		addItem(locationBox, 1, 11);
		addItem(trackLocationBox, 2, 11);

		// Separator row 12
		addItemWidth (textOptional, 3, 0, 12);
		
		// row 13
		addItem(textKernel, 0, 13);
		addItem(kernelComboBox, 1, 13);
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
		addEditButtonAction(editTypeButton);
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
		addCheckBoxAction(cabooseCheckBox);
		addCheckBoxAction(fredCheckBox);

		// build menu
		JMenuBar menuBar = new JMenuBar();
//		JMenu toolMenu = new JMenu("Tools");
//		menuBar.add(toolMenu);
//		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Cars", true);

		//	 get notified if combo box gets modified
		CarRoads.instance().addPropertyChangeListener(this);
		CarTypes.instance().addPropertyChangeListener(this);
		CarLengths.instance().addPropertyChangeListener(this);
		CarColors.instance().addPropertyChangeListener(this);
		CarOwners.instance().addPropertyChangeListener(this);
		locationManager.addPropertyChangeListener(this);
		manager.addPropertyChangeListener(this);

		// set frame size and location for display
		pack();
		if ( (getWidth()<400)) 
			setSize(450, getHeight()+20);
		else
			setSize(getWidth()+50, getHeight()+20);
		setLocation(500, 300);
// 		setAlwaysOnTop(true);	// this blows up in Java 1.4
		setVisible(true);	
	}

	public void loadCar(Car car){
		_car = car;

		if (!CarRoads.instance().containsName(car.getRoad())){
			if (JOptionPane.showConfirmDialog(this,
					"This car's road name \""+ car.getRoad() + "\" doesn't exist in your roster, add? ", "Add road name?",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarRoads.instance().addName(car.getRoad());
			}
		}
		roadComboBox.setSelectedItem(car.getRoad());

		roadNumberTextField.setText(car.getNumber());

		if (!CarTypes.instance().containsName(car.getType())){
			if (JOptionPane.showConfirmDialog(this,
					"This car's type \"\"" + car.getType() + "\" doesn't exist in your roster, add? ", "Add car type?",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarTypes.instance().addName(car.getType());
			}
		}
		typeComboBox.setSelectedItem(car.getType());

		if (!CarLengths.instance().containsName(car.getLength())){
			if (JOptionPane.showConfirmDialog(this,
					"This car's length \"" + car.getLength() + "\" doesn't exist in your roster, add? ", "Add car length?",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarLengths.instance().addName(car.getLength());
			}
		}
		lengthComboBox.setSelectedItem(car.getLength());

		if (!CarColors.instance().containsName(car.getColor())){
			if (JOptionPane.showConfirmDialog(this,
					"This car's color \"" + car.getColor() + "\" doesn't exist in your roster, add? ", "Add car color?",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarColors.instance().addName(car.getColor());
			}
		}
		colorComboBox.setSelectedItem(car.getColor());
		weightTextField.setText(car.getWeight());
		weightTonsTextField.setText(car.getWeightTons());
		cabooseCheckBox.setSelected(car.isCaboose());
		fredCheckBox.setSelected(car.hasFred());
		hazardousCheckBox.setSelected(car.isHazardous());

		locationBox.setSelectedItem(car.getLocation());
		Location l = locationManager.getLocationById(car.getLocationId());
		if (l != null){
			l.updateComboBox(trackLocationBox);
			trackLocationBox.setSelectedItem(car.getTrack());
		} else {
			trackLocationBox.removeAllItems();
		}

		builtTextField.setText(car.getBuilt());

		if (!CarOwners.instance().containsName(car.getOwner())){
			if (JOptionPane.showConfirmDialog(this,
					"This car's owner \"" + car.getOwner() + "\" doesn't exist in your roster, add? ", "Add car owner?",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarOwners.instance().addName(car.getOwner());
			}
		}
		kernelComboBox.setSelectedItem(car.getKernelName());
				
		ownerComboBox.setSelectedItem(car.getOwner());

		commentTextField.setText(car.getComment());
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
					trackLocationBox.removeAllItems();
				}else{
					log.debug("CarsSetFrame sees location: "+ locationBox.getSelectedItem());
					Location l = ((Location)locationBox.getSelectedItem());
					l.updateComboBox(trackLocationBox);
				}
			}
		}
		if (ae.getSource() == lengthComboBox && autoCheckBox.isSelected()){
			calculateWeight();
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
		if (ae.getSource() == cabooseCheckBox && cabooseCheckBox.isSelected()){
			fredCheckBox.setSelected(false);
		}
		if (ae.getSource() == fredCheckBox && fredCheckBox.isSelected()){
			cabooseCheckBox.setSelected(false);
		}
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
			// log.debug("car save button actived");
			String roadNum = roadNumberTextField.getText();
			if (roadNum.length() > 10){
				JOptionPane.showMessageDialog(this,rb.getString("carRoadNum"),
						"Car road number too long!",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			// check to see if car with road and number already exists
			Car car = manager.getCarByRoadAndNumber(roadComboBox.getSelectedItem().toString(), roadNumberTextField.getText());
			if (car != null){
				if (_car == null || !car.getId().equals(_car.getId())){
					JOptionPane.showMessageDialog(this,
							"Car with road name and number already exists", "Can not save car!",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			// check car's weight has proper format
			try{
				Double.parseDouble(weightTextField.getText());
			}catch (Exception e){
				JOptionPane.showMessageDialog(this,
						"Car's weight must be in the format of xx.x oz",
						"Car's actual weight incorrect",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			// check car's weight in tons has proper format
			try{
				Integer.parseInt(weightTonsTextField.getText());
			}catch (Exception e){
				JOptionPane.showMessageDialog(this,
						"Car's weight must be in the format of xx tons",
						"Car weight in tons incorrect",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			// delete car if edit and road or road number has changed
			if (_car != null){
				if (_car.getRoad() != null && !_car.getRoad().equals("")){
					if (!_car.getRoad().equals(roadComboBox.getSelectedItem().toString())
							|| !_car.getNumber().equals(roadNumberTextField.getText())) {
						// transfer car attributes since road name and number have changed
						Car oldcar = manager.newCar(_car.getRoad(), _car.getNumber());
						Car newCar = addCar();
						// set the car's destination and train
						newCar.setDestination(oldcar.getDestination(), oldcar.getDestinationTrack());
						newCar.setTrain(oldcar.getTrain());
						manager.deregister(oldcar);
						managerXml.writeOperationsCarFile();
						return;
					}
				}
			}
			addCar ();
			// save car file
			managerXml.writeOperationsCarFile();
		}
		if (ae.getSource() == deleteButton){
			log.debug("car delete button actived");
			Car c = manager.newCar(roadComboBox.getSelectedItem().toString(), roadNumberTextField.getText() );
			manager.deregister(c);
			// save car file
			managerXml.writeOperationsCarFile();
		}
		if (ae.getSource() == addButton){
			String roadNum = roadNumberTextField.getText();
			if (roadNum.length() > 10){
				JOptionPane.showMessageDialog(this,rb.getString("carRoadNum"),
						"Car road number too long!",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			Car c = manager.getCarByRoadAndNumber(roadComboBox.getSelectedItem().toString(), roadNumberTextField.getText() );
			if (c != null){
				log.info("Can not add, car already exists");
				JOptionPane.showMessageDialog(this,
						"Car with road name and number already exists", "Can not add car!",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			addCar();
			// save car file
			managerXml.writeOperationsCarFile();
		}
		if (ae.getSource() == clearRoadNumberButton){
			roadNumberTextField.setText("");
			roadNumberTextField.requestFocus();
		}

		if (ae.getSource() == fillWeightButton){
			calculateWeight ();
		}
	}

	private void calculateWeight() {
		if (lengthComboBox.getSelectedItem() != null) {
			String item = (String) lengthComboBox.getSelectedItem();
			try {
				double carLength = Double.parseDouble(item)*12/Setup.getScaleRatio();
				double carWeight = (double) (Setup.getInitalWeight() + carLength
						* Setup.getAddWeight()) / 1000;
				NumberFormat nf = NumberFormat.getNumberInstance();
				nf.setMaximumFractionDigits(1);
				weightTextField.setText((nf.format(carWeight)));
				weightTonsTextField.setText(Integer.toString((int)(carWeight*Setup.getScaleTonRatio())));
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this,
						"Car length must be a number in feet",
						"Can not calculate car weight!",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private Car addCar() {
		if (roadComboBox.getSelectedItem() != null
				&& !roadComboBox.getSelectedItem().toString().equals("")) {
			Car c = manager.newCar(roadComboBox.getSelectedItem().toString(),
					roadNumberTextField.getText());
			_car = c;
			if (typeComboBox.getSelectedItem() != null)
				c.setType(typeComboBox.getSelectedItem().toString());
			if (lengthComboBox.getSelectedItem() != null)
				c.setLength(lengthComboBox.getSelectedItem().toString());
			if (colorComboBox.getSelectedItem() != null)
				c.setColor(colorComboBox.getSelectedItem().toString());
			c.setWeight(weightTextField.getText());
			c.setWeightTons(weightTonsTextField.getText());
			c.setCaboose(cabooseCheckBox.isSelected());
			c.setFred(fredCheckBox.isSelected());
			c.setHazardous(hazardousCheckBox.isSelected());
			c.setBuilt(builtTextField.getText());
			if (ownerComboBox.getSelectedItem() != null)
				c.setOwner(ownerComboBox.getSelectedItem().toString());
			if (kernelComboBox.getSelectedItem() != null){
				if (kernelComboBox.getSelectedItem().equals(""))
					c.setKernel(null);
				else
					c.setKernel(manager.getKernelByName((String)kernelComboBox.getSelectedItem()));
			}
			if (locationBox.getSelectedItem() != null){
				if (locationBox.getSelectedItem().equals("")) {
					_car.setLocation(null, null);
				} else {
					if (trackLocationBox.getSelectedItem() == null
							|| trackLocationBox.getSelectedItem()
							.equals("")) {
						JOptionPane.showMessageDialog(this,
								"Must fully select a car's location",
								"Can not update car location",
								JOptionPane.ERROR_MESSAGE);

					} else {
						String status = _car.setLocation((Location)locationBox.getSelectedItem(),
								(Track)trackLocationBox.getSelectedItem());
						if (!status.equals(Car.OKAY)){
							log.debug ("Can't set car's location because of "+ status);
							JOptionPane.showMessageDialog(this,
									"Can't set car's location because of location's "+ status,
									"Can not update car location",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			c.setComment(commentTextField.getText());
			return c;
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
	CarAttributeEditFrame f;

	// edit buttons only one frame active at a time
	public void buttonEditActionPerformed(java.awt.event.ActionEvent ae) {
		if (editActive){
			f.dispose();
		}
		f = new CarAttributeEditFrame();
		f.setLocationRelativeTo(this);
		f.addPropertyChangeListener(this);
		editActive = true;

		if(ae.getSource() == editRoadButton)
			f.initComponents(ROAD);
		if(ae.getSource() == editTypeButton)
			f.initComponents(TYPE);
		if(ae.getSource() == editColorButton)
			f.initComponents(COLOR);
		if(ae.getSource() == editLengthButton)
			f.initComponents(LENGTH);
		if(ae.getSource() == editOwnerButton)
			f.initComponents(OWNER);
		if(ae.getSource() == editKernelButton)
			f.initComponents(KERNEL);
	}

	public void dispose(){
		removePropertyChangeListeners();
		super.dispose();
	}

	private void removePropertyChangeListeners(){
		CarRoads.instance().removePropertyChangeListener(this);
		CarTypes.instance().removePropertyChangeListener(this);
		CarLengths.instance().removePropertyChangeListener(this);
		CarColors.instance().removePropertyChangeListener(this);
		CarOwners.instance().removePropertyChangeListener(this);
		locationManager.removePropertyChangeListener(this);
		manager.removePropertyChangeListener(this);
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if(Control.showProperty && log.isDebugEnabled()) 
			log.debug ("CarsEditFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());
		if (e.getPropertyName().equals(CarRoads.CARROADS)){
			CarRoads.instance().updateComboBox(roadComboBox);
			if (_car != null)
			roadComboBox.setSelectedItem(_car.getRoad());
		}
		if (e.getPropertyName().equals(CarTypes.CARTYPES)){
			CarTypes.instance().updateComboBox(typeComboBox);
			if (_car != null)
				typeComboBox.setSelectedItem(_car.getType());
		}
		if (e.getPropertyName().equals(CarColors.CARCOLORS)){
			CarColors.instance().updateComboBox(colorComboBox);
			if (_car != null)
				colorComboBox.setSelectedItem(_car.getColor());
		}
		if (e.getPropertyName().equals(CarLengths.CARLENGTHS)){
			CarLengths.instance().updateComboBox(lengthComboBox);
			if (_car != null)
				lengthComboBox.setSelectedItem(_car.getLength());
		}
		if (e.getPropertyName().equals(CarManager.KERNELLISTLENGTH)){
			manager.updateKernelComboBox(kernelComboBox);
			if (_car != null) 
				kernelComboBox.setSelectedItem(_car.getKernelName());
		}
		if (e.getPropertyName().equals(CarOwners.CAROWNERS)){
			CarOwners.instance().updateComboBox(ownerComboBox);
			if (_car != null)
				ownerComboBox.setSelectedItem(_car.getOwner());
		}
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH)){
			LocationManager.instance().updateComboBox(locationBox);
			if (_car != null)
				locationBox.setSelectedItem(_car.getLocation());
		}
		if (e.getPropertyName().equals(DISPOSE)){
			editActive = false;
		}
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
	.getInstance(CarsEditFrame.class.getName());
}
