//CarsEditFrame.java

package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;


/**
 * Frame for user edit of car
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.21 $
 */

public class CarsEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");

	CarManager manager = CarManager.instance();
	CarManagerXml managerXml = CarManagerXml.instance();
	LocationManager locationManager = LocationManager.instance();

	Car _car;

	// labels
	JLabel textRoad = new JLabel();
	JLabel textRoadNumber = new JLabel();
	JLabel textColor = new JLabel();
	JLabel textBuilt = new JLabel();
	JLabel textLength = new JLabel();
	JLabel textType = new JLabel();
	JLabel textWeight = new JLabel();
	JLabel textWeightTons = new JLabel();
	JLabel textLocation = new JLabel();
	JLabel textOptional = new JLabel();
	JLabel textLoad = new JLabel();
	JLabel textKernel = new JLabel();
	JLabel textOwner = new JLabel();
	JLabel textComment = new JLabel();

	// major buttons
	JButton editRoadButton = new JButton();
	JButton clearRoadNumberButton = new JButton();
	JButton editTypeButton = new JButton();
	JButton editColorButton = new JButton();
	JButton editLengthButton = new JButton();
	JButton fillWeightButton = new JButton();
	JButton editLoadButton = new JButton();
	JButton editKernelButton = new JButton();
	JButton editOwnerButton = new JButton();

	JButton saveButton = new JButton();
	JButton deleteButton = new JButton();
	JButton addButton = new JButton();

	// check boxes
	JCheckBox autoCheckBox = new JCheckBox();
	JCheckBox cabooseCheckBox = new JCheckBox();
	JCheckBox fredCheckBox = new JCheckBox();
	JCheckBox hazardousCheckBox = new JCheckBox();

	// text field
	JTextField roadNumberTextField = new JTextField(8);
	JTextField builtTextField = new JTextField(8);
	JTextField weightTextField = new JTextField(4);
	JTextField weightTonsTextField = new JTextField(4);
	JTextField commentTextField = new JTextField(35);

	// for padding out panel
	JLabel space1 = new JLabel();
	JLabel space2 = new JLabel();
	JLabel space3 = new JLabel();

	// combo boxes
	JComboBox roadComboBox = CarRoads.instance().getComboBox();
	JComboBox typeComboBox = CarTypes.instance().getComboBox();
	JComboBox colorComboBox = CarColors.instance().getComboBox();
	JComboBox lengthComboBox = CarLengths.instance().getComboBox();
	JComboBox ownerComboBox = CarOwners.instance().getComboBox();
	JComboBox locationBox = locationManager.getComboBox();
	JComboBox trackLocationBox = new JComboBox();
	JComboBox loadComboBox = CarLoads.instance().getComboBox(null);
	JComboBox kernelComboBox = manager.getKernelComboBox(); 
	
 
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
		weightTextField.setToolTipText(rb.getString("TipCarWeightOz"));
		textWeightTons.setText(rb.getString("WeightTons"));
		textWeightTons.setVisible(true);
		weightTonsTextField.setToolTipText(rb.getString("TipCarWeightTons"));
		autoCheckBox.setText(rb.getString("Auto"));
		autoCheckBox.setSelected(true);
		autoCheckBox.setToolTipText(rb.getString("TipCarAutoCalculate"));
		cabooseCheckBox.setText(rb.getString("Caboose"));
		cabooseCheckBox.setSelected(false);
		fredCheckBox.setText(rb.getString("Fred"));
		fredCheckBox.setSelected(false);
		fredCheckBox.setToolTipText(rb.getString("TipCarFred"));
		hazardousCheckBox.setText(rb.getString("Hazardous"));
		hazardousCheckBox.setSelected(false);
		textLocation.setText(rb.getString("Location"));
		textLocation.setVisible(true);
		textOptional.setText(rb.getString("Optional"));
		textOptional.setVisible(true);
		textLoad.setText(rb.getString("Load"));
		textLoad.setVisible(true);
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
		fillWeightButton.setToolTipText(rb.getString("TipCalculateCarWeight"));
		fillWeightButton.setVisible(true);
		editLoadButton.setText(rb.getString("Edit"));
		editLoadButton.setVisible(true);
		editKernelButton.setText(rb.getString("Edit"));
		editKernelButton.setVisible(true);
		builtTextField.setToolTipText(rb.getString("TipBuildDate"));
		editOwnerButton.setText(rb.getString("Edit"));
		editOwnerButton.setVisible(true);
		deleteButton.setText(rb.getString("Delete"));
		deleteButton.setVisible(true);
		addButton.setText(rb.getString("Add"));
		addButton.setVisible(true);
		saveButton.setText(rb.getString("Save"));
		saveButton.setVisible(true);

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
		addItemWidth(trackLocationBox, 2, 2, 11);

		// Separator row 12
		addItemWidth (textOptional, 3, 0, 12);
		
		// row 13
		addItem(textLoad, 0, 13);
		addItem(loadComboBox, 1, 13);
		addItem(editLoadButton, 2, 13);
		
		// row 15
		addItem(textKernel, 0, 15);
		addItem(kernelComboBox, 1, 15);
		addItem(editKernelButton, 2, 15);

		// row 17
		addItem(textBuilt, 0, 17);
		addItem(builtTextField, 1, 17);
		
		// row 19
		addItem(textOwner, 0, 19);
		addItem(ownerComboBox, 1, 19);
		addItem(editOwnerButton, 2, 19);

		// row 21
		addItem(textComment, 0, 21);
		addItemWidth(commentTextField, 3, 1, 21);

		// row 23
		addItem(space1, 0, 23);
		
		// row 25
		addItem(deleteButton, 0, 25);
		addItem(addButton, 1, 25);
		addItem(saveButton, 3, 25);

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
		addButtonAction(saveButton);
		addButtonAction(fillWeightButton);
		addButtonAction(editLoadButton);

		// setup combobox
		addComboBoxAction(typeComboBox);
		addComboBoxAction(lengthComboBox);
		addComboBoxAction(locationBox);
		
		// setup checkbox
		addCheckBoxAction(cabooseCheckBox);
		addCheckBoxAction(fredCheckBox);
		
		// build menu
//		JMenuBar menuBar = new JMenuBar();
//		JMenu toolMenu = new JMenu("Tools");
//		menuBar.add(toolMenu);
//		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Cars", true);

		//	 get notified if combo box gets modified
		CarRoads.instance().addPropertyChangeListener(this);
		CarLoads.instance().addPropertyChangeListener(this);
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
		setLocation(400, 200);
		setVisible(true);	
	}

	public void loadCar(Car car){
		_car = car;

		if (!CarRoads.instance().containsName(car.getRoad())){
			if (JOptionPane.showConfirmDialog(this,
					MessageFormat.format(rb.getString("roadNameNotExist"),new Object[]{car.getRoad()}),
					rb.getString("carAddRoad"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarRoads.instance().addName(car.getRoad());
			}
		}
		roadComboBox.setSelectedItem(car.getRoad());

		roadNumberTextField.setText(car.getNumber());

		if (!CarTypes.instance().containsName(car.getType())){
			if (JOptionPane.showConfirmDialog(this,
					MessageFormat.format(rb.getString("typeNameNotExist"),new Object[]{car.getType()}),
					rb.getString("carAddType"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarTypes.instance().addName(car.getType());
			}
		}
		typeComboBox.setSelectedItem(car.getType());

		if (!CarLengths.instance().containsName(car.getLength())){
			if (JOptionPane.showConfirmDialog(this,
					MessageFormat.format(rb.getString("lengthNameNotExist"),new Object[]{car.getLength()}),
					rb.getString("carAddLength"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarLengths.instance().addName(car.getLength());
			}
		}
		lengthComboBox.setSelectedItem(car.getLength());

		if (!CarColors.instance().containsName(car.getColor())){
			if (JOptionPane.showConfirmDialog(this,
					MessageFormat.format(rb.getString("colorNameNotExist"),new Object[]{car.getColor()}),
					rb.getString("carAddColor"),
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
					MessageFormat.format(rb.getString("ownerNameNotExist"),new Object[]{car.getOwner()}),
					rb.getString("addOwner"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarOwners.instance().addName(car.getOwner());
			}
		}
		ownerComboBox.setSelectedItem(car.getOwner());
		
		if (!CarLoads.instance().containsName(car.getType(), car.getLoad())){
			if (JOptionPane.showConfirmDialog(this,
					MessageFormat.format(rb.getString("loadNameNotExist"),new Object[]{car.getLoad()}),
					rb.getString("addLoad"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarLoads.instance().addName(car.getType(), car.getLoad());
			}
		}
		// listen for changes in car load
		car.addPropertyChangeListener(this);
		CarLoads.instance().updateComboBox(car.getType(), loadComboBox);
		loadComboBox.setSelectedItem(car.getLoad());
		
		kernelComboBox.setSelectedItem(car.getKernelName());
				
		

		commentTextField.setText(car.getComment());
	}

	// combo boxes
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource()== typeComboBox && typeComboBox.getSelectedItem() != null){
			log.debug("Type comboBox sees change, update car loads");
			CarLoads.instance().updateComboBox((String)typeComboBox.getSelectedItem(), loadComboBox);
		}
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

	// Save, Delete, Add, Clear, Calculate, Edit Load buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton){
			// log.debug("car save button actived");
			if (!checkCar(_car))
				return;
			// delete car if edit and road or road number has changed
			if (_car != null){
				if (_car.getRoad() != null && !_car.getRoad().equals("")){
					if (!_car.getRoad().equals(roadComboBox.getSelectedItem().toString())
							|| !_car.getNumber().equals(roadNumberTextField.getText())) {
						// transfer car attributes since road name and number have changed
						Car oldCar = _car;
						Car newCar = addCar();
						// set the car's destination and train
						newCar.setDestination(oldCar.getDestination(), oldCar.getDestinationTrack());
						newCar.setTrain(oldCar.getTrain());
						oldCar.removePropertyChangeListener(this);
						manager.deregister(oldCar);
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
			if (_car != null
					&& _car.getRoad().equals(roadComboBox.getSelectedItem().toString())
					&& _car.getNumber().equals(roadNumberTextField.getText())) {
				manager.deregister(_car);
				_car = null;
				// save car file
				managerXml.writeOperationsCarFile();
			} else {
				Car car = manager.getCarByRoadAndNumber(roadComboBox.getSelectedItem().toString(),
						roadNumberTextField.getText());
				if (car != null){
					manager.deregister(car);
					// save car file
					managerXml.writeOperationsCarFile();
				}
			}
		}
		if (ae.getSource() == addButton){
			if (!checkCar(null))
				return;
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
		if (ae.getSource() == editLoadButton){
			if (lef != null)
				lef.dispose();
			lef = new CarLoadEditFrame();
			lef.setLocationRelativeTo(this);
			lef.initComponents((String)typeComboBox.getSelectedItem());
		}
	}
	
	CarLoadEditFrame lef = null;
	
	private boolean checkCar(Car c){
		String roadNum = roadNumberTextField.getText();
		if (roadNum.length() > 10){
			JOptionPane.showMessageDialog(this,rb.getString("carRoadNum"),
					rb.getString("carRoadLong"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// check to see if car with road and number already exists
		Car car = manager.getCarByRoadAndNumber(roadComboBox.getSelectedItem().toString(),
				roadNumberTextField.getText());
		if (car != null){
			if (c == null || !car.getId().equals(c.getId())){
				JOptionPane.showMessageDialog(this,
						rb.getString("carRoadExists"), rb.getString("carCanNotUpdate"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		// check car's weight has proper format
		try{
			Double.parseDouble(weightTextField.getText());
		}catch (Exception e){
			JOptionPane.showMessageDialog(this,
					rb.getString("carWeightFormat"), rb.getString("carActualWeight"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// check car's weight in tons has proper format
		try{
			Integer.parseInt(weightTonsTextField.getText());
		}catch (Exception e){
			JOptionPane.showMessageDialog(this,
					rb.getString("carWeightFormatTon"), rb.getString("carWeightTon"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private void calculateWeight() {
		if (lengthComboBox.getSelectedItem() != null) {
			String item = (String) lengthComboBox.getSelectedItem();
			try {
				double carLength = Double.parseDouble(item)*12/Setup.getScaleRatio();
				double carWeight = (Setup.getInitalWeight() + carLength
						* Setup.getAddWeight()) / 1000;
				NumberFormat nf = NumberFormat.getNumberInstance();
				nf.setMaximumFractionDigits(1);
				weightTextField.setText((nf.format(carWeight)));
				weightTonsTextField.setText(Integer.toString((int)(carWeight*Setup.getScaleTonRatio())));
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this,
						rb.getString("carLengthMustBe"), rb.getString("carWeigthCanNot"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private Car addCar() {
		if (roadComboBox.getSelectedItem() != null
				&& !roadComboBox.getSelectedItem().toString().equals("")) {
			if (_car == null
					|| !_car.getRoad().equals(roadComboBox.getSelectedItem().toString())
					|| !_car.getNumber().equals(roadNumberTextField.getText())) {
				_car = manager.newCar(roadComboBox.getSelectedItem().toString(),
						roadNumberTextField.getText());
				_car.addPropertyChangeListener(this);
			}
			if (typeComboBox.getSelectedItem() != null)
				_car.setType(typeComboBox.getSelectedItem().toString());
			if (lengthComboBox.getSelectedItem() != null)
				_car.setLength(lengthComboBox.getSelectedItem().toString());
			if (colorComboBox.getSelectedItem() != null)
				_car.setColor(colorComboBox.getSelectedItem().toString());
			_car.setWeight(weightTextField.getText());
			_car.setWeightTons(weightTonsTextField.getText());
			_car.setCaboose(cabooseCheckBox.isSelected());
			_car.setFred(fredCheckBox.isSelected());
			_car.setHazardous(hazardousCheckBox.isSelected());
			_car.setBuilt(builtTextField.getText());
			if (ownerComboBox.getSelectedItem() != null)
				_car.setOwner(ownerComboBox.getSelectedItem().toString());
			if (loadComboBox.getSelectedItem() != null)
				_car.setLoad(loadComboBox.getSelectedItem().toString());
			if (kernelComboBox.getSelectedItem() != null){
				if (kernelComboBox.getSelectedItem().equals(""))
					_car.setKernel(null);
				else
					_car.setKernel(manager.getKernelByName((String)kernelComboBox.getSelectedItem()));
			}
			if (locationBox.getSelectedItem() != null){
				if (locationBox.getSelectedItem().equals("")) {
					_car.setLocation(null, null);
				} else {
					if (trackLocationBox.getSelectedItem() == null
							|| trackLocationBox.getSelectedItem().equals("")) {
						JOptionPane.showMessageDialog(this,
								rb.getString("carFullySelect"), rb.getString("carCanNotLoc"),
								JOptionPane.ERROR_MESSAGE);

					} else {
						String status = _car.setLocation((Location)locationBox.getSelectedItem(),
								(Track)trackLocationBox.getSelectedItem());
						if (!status.equals(Car.OKAY)){
							log.debug ("Can't set car's location because of "+ status);
							JOptionPane.showMessageDialog(this,
									rb.getString("carCanNotLocMsg")+ status,
									rb.getString("carCanNotLoc"),
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			_car.setComment(commentTextField.getText());
			return _car;
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
		CarLoads.instance().removePropertyChangeListener(this);
		CarTypes.instance().removePropertyChangeListener(this);
		CarLengths.instance().removePropertyChangeListener(this);
		CarColors.instance().removePropertyChangeListener(this);
		CarOwners.instance().removePropertyChangeListener(this);
		locationManager.removePropertyChangeListener(this);
		manager.removePropertyChangeListener(this);
		if (_car != null)
			_car.removePropertyChangeListener(this);
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if(Control.showProperty && log.isDebugEnabled()) 
			log.debug ("CarsEditFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());
		if (e.getPropertyName().equals(CarRoads.CARROADS_LENGTH_CHANGED_PROPERTY)){
			CarRoads.instance().updateComboBox(roadComboBox);
			if (_car != null)
			roadComboBox.setSelectedItem(_car.getRoad());
		}
		if (e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY)){
			CarTypes.instance().updateComboBox(typeComboBox);
			if (_car != null)
				typeComboBox.setSelectedItem(_car.getType());
		}
		if (e.getPropertyName().equals(CarColors.CARCOLORS_CHANGED_PROPERTY)){
			CarColors.instance().updateComboBox(colorComboBox);
			if (_car != null)
				colorComboBox.setSelectedItem(_car.getColor());
		}
		if (e.getPropertyName().equals(CarLengths.CARLENGTHS_CHANGED_PROPERTY)){
			CarLengths.instance().updateComboBox(lengthComboBox);
			if (_car != null)
				lengthComboBox.setSelectedItem(_car.getLength());
		}
		if (e.getPropertyName().equals(CarManager.KERNELLISTLENGTH_CHANGED_PROPERTY)){
			manager.updateKernelComboBox(kernelComboBox);
			if (_car != null) 
				kernelComboBox.setSelectedItem(_car.getKernelName());
		}
		if (e.getPropertyName().equals(CarOwners.CAROWNERS_CHANGED_PROPERTY)){
			CarOwners.instance().updateComboBox(ownerComboBox);
			if (_car != null)
				ownerComboBox.setSelectedItem(_car.getOwner());
		}
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)){
			LocationManager.instance().updateComboBox(locationBox);
			if (_car != null)
				locationBox.setSelectedItem(_car.getLocation());
		}
		if (e.getPropertyName().equals(Car.LOAD_CHANGED_PROPERTY)){
			if (_car != null)
				loadComboBox.setSelectedItem(_car.getLoad());
		}
		if (e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY)){
			if (_car != null){
				CarLoads.instance().updateComboBox((String)typeComboBox.getSelectedItem(), loadComboBox);
				loadComboBox.setSelectedItem(_car.getLoad());
			}
		}
		if (e.getPropertyName().equals(DISPOSE)){
			editActive = false;
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(CarsEditFrame.class.getName());
}
