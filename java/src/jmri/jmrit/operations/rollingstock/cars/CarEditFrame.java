//CarEditFrame.java

package jmri.jmrit.operations.rollingstock.cars;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Frame for user edit of car
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011
 * @version $Revision$
 */

public class CarEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	CarManager carManager = CarManager.instance();
	CarManagerXml managerXml = CarManagerXml.instance();
	LocationManager locationManager = LocationManager.instance();

	Car _car;

	// labels
	JLabel textWeightOz = new JLabel(Bundle.getString("WeightOz"));
	JLabel textWeightTons = new JLabel(Bundle.getString("WeightTons"));

	// major buttons
	JButton editRoadButton = new JButton(Bundle.getString("Edit"));
	JButton clearRoadNumberButton = new JButton(Bundle.getString("Clear"));
	JButton editTypeButton = new JButton(Bundle.getString("Edit"));
	JButton editColorButton = new JButton(Bundle.getString("Edit"));
	JButton editLengthButton = new JButton(Bundle.getString("Edit"));
	JButton fillWeightButton = new JButton(Bundle.getString("Calculate"));
	JButton editLoadButton = new JButton(Bundle.getString("Edit"));
	JButton editKernelButton = new JButton(Bundle.getString("Edit"));
	JButton editOwnerButton = new JButton(Bundle.getString("Edit"));

	JButton saveButton = new JButton(Bundle.getString("Save"));
	JButton deleteButton = new JButton(Bundle.getString("Delete"));
	JButton addButton = new JButton(Bundle.getString("Add"));

	// check boxes
	JCheckBox autoCheckBox = new JCheckBox(Bundle.getString("Auto"));
	JCheckBox autoTrackCheckBox = new JCheckBox(Bundle.getString("Auto"));
	JCheckBox passengerCheckBox = new JCheckBox(Bundle.getString("Passenger"));
	JCheckBox cabooseCheckBox = new JCheckBox(Bundle.getString("Caboose"));
	JCheckBox fredCheckBox = new JCheckBox(Bundle.getString("Fred"));
	JCheckBox utilityCheckBox = new JCheckBox(Bundle.getString("Utility"));
	JCheckBox hazardousCheckBox = new JCheckBox(Bundle.getString("Hazardous"));

	// text field
	JTextField roadNumberTextField = new JTextField(8);
	JTextField builtTextField = new JTextField(8);
	JTextField weightTextField = new JTextField(4);
	JTextField weightTonsTextField = new JTextField(4);
	JTextField commentTextField = new JTextField(35);
	JTextField valueTextField = new JTextField(8);
	JTextField rfidTextField = new JTextField(16);

	// combo boxes
	JComboBox roadComboBox = CarRoads.instance().getComboBox();
	JComboBox typeComboBox = CarTypes.instance().getComboBox();
	JComboBox colorComboBox = CarColors.instance().getComboBox();
	JComboBox lengthComboBox = CarLengths.instance().getComboBox();
	JComboBox ownerComboBox = CarOwners.instance().getComboBox();
	JComboBox locationBox = locationManager.getComboBox();
	JComboBox trackLocationBox = new JComboBox();
	JComboBox loadComboBox = CarLoads.instance().getComboBox(null);
	JComboBox kernelComboBox = carManager.getKernelComboBox();

	CarLoadEditFrame lef = null;

	public static final String ROAD = Bundle.getString("Road");
	public static final String TYPE = Bundle.getString("Type");
	public static final String COLOR = Bundle.getString("Color");
	public static final String LENGTH = Bundle.getString("Length");
	public static final String OWNER = Bundle.getString("Owner");
	public static final String KERNEL = Bundle.getString("Kernel");

	public CarEditFrame() {
		super();
	}

	public void initComponents() {
		// the following code sets the frame's initial state

		// load tool tips
		weightTextField.setToolTipText(Bundle.getString("TipCarWeightOz"));
		weightTonsTextField.setToolTipText(Bundle.getString("TipCarWeightTons"));
		autoCheckBox.setToolTipText(Bundle.getString("TipCarAutoCalculate"));
		passengerCheckBox.setToolTipText(Bundle.getString("TipCarPassenger"));
		cabooseCheckBox.setToolTipText(Bundle.getString("TipCarCaboose"));
		fredCheckBox.setToolTipText(Bundle.getString("TipCarFred"));
		utilityCheckBox.setToolTipText(Bundle.getString("TipCarUtility"));
		hazardousCheckBox.setToolTipText(Bundle.getString("TipCarHazardous"));
		fillWeightButton.setToolTipText(Bundle.getString("TipCalculateCarWeight"));
		builtTextField.setToolTipText(Bundle.getString("TipBuildDate"));
		valueTextField.setToolTipText(Bundle.getString("TipValue"));
		rfidTextField.setToolTipText(Bundle.getString("TipRfid"));

		// default check box selections
		autoCheckBox.setSelected(true);
		passengerCheckBox.setSelected(false);
		cabooseCheckBox.setSelected(false);
		fredCheckBox.setSelected(false);
		hazardousCheckBox.setSelected(false);

		// create panel
		JPanel pPanel = new JPanel();
		pPanel.setLayout(new BoxLayout(pPanel, BoxLayout.Y_AXIS));

		// Layout the panel by rows
		// row 1
		JPanel pRoad = new JPanel();
		pRoad.setLayout(new GridBagLayout());
		pRoad.setBorder(BorderFactory.createTitledBorder(Bundle.getString("Road")));
		addItem(pRoad, roadComboBox, 1, 0);
		addItem(pRoad, editRoadButton, 2, 0);
		pPanel.add(pRoad);

		// row 2
		JPanel pRoadNumber = new JPanel();
		pRoadNumber.setLayout(new GridBagLayout());
		pRoadNumber.setBorder(BorderFactory.createTitledBorder(Bundle.getString("RoadNumber")));
		addItem(pRoadNumber, roadNumberTextField, 1, 0);
		addItem(pRoadNumber, clearRoadNumberButton, 2, 0);
		pPanel.add(pRoadNumber);

		// row 3
		JPanel pType = new JPanel();
		pType.setLayout(new GridBagLayout());
		pType.setBorder(BorderFactory.createTitledBorder(Bundle.getString("Type")));
		addItem(pType, typeComboBox, 0, 0);
		addItem(pType, editTypeButton, 2, 0);
		addItem(pType, hazardousCheckBox, 3, 0);
		addItem(pType, passengerCheckBox, 0, 1);
		addItem(pType, cabooseCheckBox, 1, 1);
		addItem(pType, fredCheckBox, 2, 1);
		addItem(pType, utilityCheckBox, 3, 1);
		pPanel.add(pType);

		// row 4
		JPanel pLength = new JPanel();
		pLength.setLayout(new GridBagLayout());
		pLength.setBorder(BorderFactory.createTitledBorder(Bundle.getString("Length")));
		addItem(pLength, lengthComboBox, 1, 0);
		addItem(pLength, editLengthButton, 2, 0);
		pPanel.add(pLength);

		// row 5

		// row 7
		JPanel pWeight = new JPanel();
		pWeight.setLayout(new GridBagLayout());
		pWeight.setBorder(BorderFactory.createTitledBorder(Bundle.getString("Weight")));
		addItem(pWeight, textWeightOz, 0, 0);
		addItem(pWeight, weightTextField, 1, 0);
		addItem(pWeight, fillWeightButton, 2, 0);
		addItem(pWeight, autoCheckBox, 3, 0);
		addItem(pWeight, textWeightTons, 0, 1);
		addItem(pWeight, weightTonsTextField, 1, 1);
		pPanel.add(pWeight);

		// row 11
		JPanel pLocation = new JPanel();
		pLocation.setLayout(new GridBagLayout());
		pLocation.setBorder(BorderFactory.createTitledBorder(Bundle.getString("LocationAndTrack")));
		addItem(pLocation, locationBox, 1, 0);
		addItem(pLocation, trackLocationBox, 2, 0);
		addItem(pLocation, autoTrackCheckBox, 3, 0);
		pPanel.add(pLocation);

		// optional panel
		JPanel pOptional = new JPanel();
		pOptional.setLayout(new BoxLayout(pOptional, BoxLayout.Y_AXIS));
		JScrollPane optionPane = new JScrollPane(pOptional);
		optionPane.setBorder(BorderFactory.createTitledBorder(Bundle
				.getString("BorderLayoutOptional")));

		// row 12
		JPanel pColor = new JPanel();
		pColor.setLayout(new GridBagLayout());
		pColor.setBorder(BorderFactory.createTitledBorder(Bundle.getString("Color")));
		addItem(pColor, colorComboBox, 1, 0);
		addItem(pColor, editColorButton, 2, 0);
		pOptional.add(pColor);

		// row 13
		JPanel pLoad = new JPanel();
		pLoad.setLayout(new GridBagLayout());
		pLoad.setBorder(BorderFactory.createTitledBorder(Bundle.getString("Load")));
		addItem(pLoad, loadComboBox, 1, 0);
		addItem(pLoad, editLoadButton, 2, 0);
		pOptional.add(pLoad);

		// row 15
		JPanel pKernel = new JPanel();
		pKernel.setLayout(new GridBagLayout());
		pKernel.setBorder(BorderFactory.createTitledBorder(Bundle.getString("Kernel")));
		addItem(pKernel, kernelComboBox, 1, 0);
		addItem(pKernel, editKernelButton, 2, 0);
		pOptional.add(pKernel);

		// row 17
		JPanel pBuilt = new JPanel();
		pBuilt.setLayout(new GridBagLayout());
		pBuilt.setBorder(BorderFactory.createTitledBorder(Bundle.getString("Built")));
		addItem(pBuilt, builtTextField, 1, 0);
		pOptional.add(pBuilt);

		// row 19
		JPanel pOwner = new JPanel();
		pOwner.setLayout(new GridBagLayout());
		pOwner.setBorder(BorderFactory.createTitledBorder(Bundle.getString("Owner")));
		addItem(pOwner, ownerComboBox, 1, 0);
		addItem(pOwner, editOwnerButton, 2, 0);
		pOptional.add(pOwner);

		// row 20
		if (Setup.isValueEnabled()) {
			JPanel pValue = new JPanel();
			pValue.setLayout(new GridBagLayout());
			pValue.setBorder(BorderFactory.createTitledBorder(Setup.getValueLabel()));
			addItem(pValue, valueTextField, 1, 0);
			pOptional.add(pValue);
		}

		// row 22
		if (Setup.isRfidEnabled()) {
			JPanel pRfid = new JPanel();
			pRfid.setLayout(new GridBagLayout());
			pRfid.setBorder(BorderFactory.createTitledBorder(Setup.getRfidLabel()));
			addItem(pRfid, rfidTextField, 1, 0);
			pOptional.add(pRfid);
		}

		// row 24
		JPanel pComment = new JPanel();
		pComment.setLayout(new GridBagLayout());
		pComment.setBorder(BorderFactory.createTitledBorder(Bundle.getString("Comment")));
		addItem(pComment, commentTextField, 1, 0);
		pOptional.add(pComment);

		// button panel
		JPanel pButtons = new JPanel();
		pButtons.setLayout(new GridBagLayout());
		addItem(pButtons, deleteButton, 0, 25);
		addItem(pButtons, addButton, 1, 25);
		addItem(pButtons, saveButton, 3, 25);

		// add panels
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(pPanel);
		getContentPane().add(optionPane);
		getContentPane().add(pButtons);

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
		addCheckBoxAction(autoTrackCheckBox);
		autoTrackCheckBox.setEnabled(false);

		// build menu
		// JMenuBar menuBar = new JMenuBar();
		// JMenu toolMenu = new JMenu("Tools");
		// menuBar.add(toolMenu);
		// setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_CarsEdit", true);

		// get notified if combo box gets modified
		CarRoads.instance().addPropertyChangeListener(this);
		CarLoads.instance().addPropertyChangeListener(this);
		CarTypes.instance().addPropertyChangeListener(this);
		CarLengths.instance().addPropertyChangeListener(this);
		CarColors.instance().addPropertyChangeListener(this);
		CarOwners.instance().addPropertyChangeListener(this);
		locationManager.addPropertyChangeListener(this);
		carManager.addPropertyChangeListener(this);

		pack();
		if (getWidth() < 450)
			setSize(450, getHeight());
		if (getHeight() < 500)
			setSize(getWidth(), 500);
		setMinimumSize(new Dimension(450, Control.panelHeight));
		setVisible(true);
	}

	public void loadCar(Car car) {
		_car = car;

		if (!CarRoads.instance().containsName(car.getRoad())) {
			if (JOptionPane.showConfirmDialog(
					this,
					MessageFormat.format(Bundle.getString("roadNameNotExist"),
							new Object[] { car.getRoad() }), Bundle.getString("carAddRoad"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				CarRoads.instance().addName(car.getRoad());
			}
		}
		roadComboBox.setSelectedItem(car.getRoad());

		roadNumberTextField.setText(car.getNumber());

		if (!CarTypes.instance().containsName(car.getType())) {
			if (JOptionPane.showConfirmDialog(
					this,
					MessageFormat.format(Bundle.getString("typeNameNotExist"),
							new Object[] { car.getType() }), Bundle.getString("carAddType"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				CarTypes.instance().addName(car.getType());
			}
		}
		typeComboBox.setSelectedItem(car.getType());

		if (!CarLengths.instance().containsName(car.getLength())) {
			if (JOptionPane.showConfirmDialog(
					this,
					MessageFormat.format(Bundle.getString("lengthNameNotExist"),
							new Object[] { car.getLength() }), Bundle.getString("carAddLength"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				CarLengths.instance().addName(car.getLength());
			}
		}
		lengthComboBox.setSelectedItem(car.getLength());

		if (!CarColors.instance().containsName(car.getColor())) {
			if (JOptionPane.showConfirmDialog(
					this,
					MessageFormat.format(Bundle.getString("colorNameNotExist"),
							new Object[] { car.getColor() }), Bundle.getString("carAddColor"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				CarColors.instance().addName(car.getColor());
			}
		}
		colorComboBox.setSelectedItem(car.getColor());
		weightTextField.setText(car.getWeight());
		weightTonsTextField.setText(car.getWeightTons());
		passengerCheckBox.setSelected(car.isPassenger());
		cabooseCheckBox.setSelected(car.isCaboose());
		utilityCheckBox.setSelected(car.isUtility());
		utilityCheckBox.setSelected(car.isUtility());
		fredCheckBox.setSelected(car.hasFred());
		hazardousCheckBox.setSelected(car.isHazardous());

		locationBox.setSelectedItem(car.getLocation());
		updateTrackLocationBox();

		builtTextField.setText(car.getBuilt());

		if (!CarOwners.instance().containsName(car.getOwner())) {
			if (JOptionPane.showConfirmDialog(
					this,
					MessageFormat.format(Bundle.getString("ownerNameNotExist"),
							new Object[] { car.getOwner() }), Bundle.getString("addOwner"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				CarOwners.instance().addName(car.getOwner());
			}
		}
		ownerComboBox.setSelectedItem(car.getOwner());

		if (!CarLoads.instance().containsName(car.getType(), car.getLoad())) {
			if (JOptionPane.showConfirmDialog(
					this,
					MessageFormat.format(Bundle.getString("loadNameNotExist"),
							new Object[] { car.getLoad() }), Bundle.getString("addLoad"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				CarLoads.instance().addName(car.getType(), car.getLoad());
			}
		}
		// listen for changes in car load
		car.addPropertyChangeListener(this);
		CarLoads.instance().updateComboBox(car.getType(), loadComboBox);
		loadComboBox.setSelectedItem(car.getLoad());

		kernelComboBox.setSelectedItem(car.getKernelName());

		commentTextField.setText(car.getComment());
		valueTextField.setText(car.getValue());
		rfidTextField.setText(car.getRfid());
		autoTrackCheckBox.setEnabled(true);
	}

	// combo boxes
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == typeComboBox && typeComboBox.getSelectedItem() != null) {
			log.debug("Type comboBox sees change, update car loads");
			CarLoads.instance().updateComboBox((String) typeComboBox.getSelectedItem(),
					loadComboBox);
			// turnout off auto for location tracks
			autoTrackCheckBox.setSelected(false);
			autoTrackCheckBox.setEnabled(false);
			updateTrackLocationBox();
		}
		if (ae.getSource() == locationBox) {
			updateTrackLocationBox();
		}
		if (ae.getSource() == lengthComboBox && autoCheckBox.isSelected()) {
			calculateWeight();
		}
	}

	private void updateTrackLocationBox() {
		if (locationBox.getSelectedItem() != null) {
			if (locationBox.getSelectedItem().equals("")) {
				trackLocationBox.removeAllItems();
			} else {
				log.debug("Update tracks for location: " + locationBox.getSelectedItem());
				Location l = ((Location) locationBox.getSelectedItem());
				l.updateComboBox(trackLocationBox, _car, autoTrackCheckBox.isSelected(), false);
				if (_car != null && _car.getLocation() == l)
					trackLocationBox.setSelectedItem(_car.getTrack());
			}
		}
	}

	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		JCheckBox b = (JCheckBox) ae.getSource();
		log.debug("checkbox change " + b.getText());
		if (ae.getSource() == cabooseCheckBox && cabooseCheckBox.isSelected()) {
			fredCheckBox.setSelected(false);
		}
		if (ae.getSource() == fredCheckBox && fredCheckBox.isSelected()) {
			cabooseCheckBox.setSelected(false);
		}
		if (ae.getSource() == autoTrackCheckBox) {
			updateTrackLocationBox();
		}
	}

	// Save, Delete, Add, Clear, Calculate, Edit Load buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton) {
			// log.debug("car save button pressed");
			if (!checkCar(_car))
				return;
			// if the road or number changes, the car needs a new id
			if (_car != null
					&& _car.getRoad() != null
					&& !_car.getRoad().equals("")
					&& (!_car.getRoad().equals(roadComboBox.getSelectedItem().toString()) || !_car
							.getNumber().equals(roadNumberTextField.getText()))) {
				String road = roadComboBox.getSelectedItem().toString();
				String number = roadNumberTextField.getText();
				carManager.changeId(_car, road, number);
				_car.setRoad(road);
				_car.setNumber(number);
			}
			addCar();
			/*
			 * all JMRI window position and size are now saved // save frame size and position
			 * carManager.setEditFrame(this);
			 */
			// save car file
			writeFiles();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
		if (ae.getSource() == deleteButton) {
			log.debug("car delete button activated");
			if (_car != null && _car.getRoad().equals(roadComboBox.getSelectedItem().toString())
					&& _car.getNumber().equals(roadNumberTextField.getText())) {
				carManager.deregister(_car);
				_car = null;
				// save car file
				writeFiles();
			} else {
				Car car = carManager.getByRoadAndNumber(roadComboBox.getSelectedItem().toString(),
						roadNumberTextField.getText());
				if (car != null) {
					carManager.deregister(car);
					// save car file
					writeFiles();
				}
			}
		}
		if (ae.getSource() == addButton) {
			if (!checkCar(null))
				return;
			addCar();
			// save car file
			writeFiles();
		}
		if (ae.getSource() == clearRoadNumberButton) {
			roadNumberTextField.setText("");
			roadNumberTextField.requestFocus();
		}

		if (ae.getSource() == fillWeightButton) {
			calculateWeight();
		}
		if (ae.getSource() == editLoadButton) {
			if (lef != null)
				lef.dispose();
			lef = new CarLoadEditFrame();
			lef.setLocationRelativeTo(this);
			lef.initComponents((String) typeComboBox.getSelectedItem(),
					(String) loadComboBox.getSelectedItem());
		}
	}

	/**
	 * Need to also write the location and train files if a road name was deleted. Need to also write files if car type
	 * was changed.
	 */
	private void writeFiles() {
		OperationsXml.save();
	}

	private boolean checkCar(Car c) {
		String roadNum = roadNumberTextField.getText();
		if (roadNum.length() > Control.max_len_string_road_number) {
			JOptionPane.showMessageDialog(this, MessageFormat.format(
					Bundle.getString("carRoadNum"),
					new Object[] { Control.max_len_string_road_number + 1 }), Bundle
					.getString("carRoadLong"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// check to see if car with road and number already exists
		Car car = carManager.getByRoadAndNumber(roadComboBox.getSelectedItem().toString(),
				roadNumberTextField.getText());
		if (car != null) {
			// new car?
			if (c == null) {
				JOptionPane.showMessageDialog(this, Bundle.getString("carRoadExists"),
						Bundle.getString("carCanNotAdd"), JOptionPane.ERROR_MESSAGE);
				return false;
			}
			// old car with new road or number?
			if (!car.getId().equals(c.getId())) {
				JOptionPane.showMessageDialog(this, Bundle.getString("carRoadExists"),
						Bundle.getString("carCanNotUpdate"), JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		// check car's weight has proper format
		try {
			Double.parseDouble(weightTextField.getText());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, Bundle.getString("carWeightFormat"),
					Bundle.getString("carActualWeight"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// check car's weight in tons has proper format
		try {
			Integer.parseInt(weightTonsTextField.getText());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, Bundle.getString("carWeightFormatTon"),
					Bundle.getString("carWeightTon"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private void calculateWeight() {
		if (lengthComboBox.getSelectedItem() != null) {
			String length = (String) lengthComboBox.getSelectedItem();
			try {
				double carLength = Double.parseDouble(length) * 12 / Setup.getScaleRatio();
				double carWeight = (Setup.getInitalWeight() + carLength * Setup.getAddWeight()) / 1000;
				NumberFormat nf = NumberFormat.getNumberInstance();
				nf.setMaximumFractionDigits(1);
				weightTextField.setText((nf.format(carWeight))); // car weight in ounces.
				int tons = (int) (carWeight * Setup.getScaleTonRatio());
				// adjust weight for caboose
				if (cabooseCheckBox.isSelected() || passengerCheckBox.isSelected())
					tons = (int) (Double.parseDouble(length) * .9); // .9 tons/foot
				weightTonsTextField.setText(Integer.toString(tons));
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, Bundle.getString("carLengthMustBe"),
						Bundle.getString("carWeigthCanNot"), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void addCar() {
		if (roadComboBox.getSelectedItem() == null
				|| roadComboBox.getSelectedItem().toString().equals(""))
			return;
		if (_car == null || !_car.getRoad().equals(roadComboBox.getSelectedItem().toString())
				|| !_car.getNumber().equals(roadNumberTextField.getText())) {
			_car = carManager.newCar(roadComboBox.getSelectedItem().toString(),
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
		// ask if all cars of this type should be passenger
		if (_car.isPassenger() ^ passengerCheckBox.isSelected()) {
			if (JOptionPane.showConfirmDialog(this, MessageFormat.format(
					passengerCheckBox.isSelected() ? Bundle.getString("carModifyTypePassenger")
							: Bundle.getString("carRemoveTypePassenger"), new Object[] { _car
							.getType() }), MessageFormat.format(
					Bundle.getString("carModifyAllType"), new Object[] { _car.getType() }),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				// go through the entire list and change the caboose setting for all cars of this type
				List<String> cars = carManager.getList();
				for (int i = 0; i < cars.size(); i++) {
					Car c = carManager.getById(cars.get(i));
					if (c.getType().equals(_car.getType()))
						c.setPassenger(passengerCheckBox.isSelected());
				}
			}
		}
		_car.setPassenger(passengerCheckBox.isSelected());
		// ask if all cars of this type should be caboose
		if (_car.isCaboose() ^ cabooseCheckBox.isSelected()) {
			if (JOptionPane.showConfirmDialog(this, MessageFormat.format(
					cabooseCheckBox.isSelected() ? Bundle.getString("carModifyTypeCaboose")
							: Bundle.getString("carRemoveTypeCaboose"), new Object[] { _car
							.getType() }), MessageFormat.format(
					Bundle.getString("carModifyAllType"), new Object[] { _car.getType() }),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				// go through the entire list and change the caboose setting for all cars of this type
				List<String> cars = carManager.getList();
				for (int i = 0; i < cars.size(); i++) {
					Car c = carManager.getById(cars.get(i));
					if (c.getType().equals(_car.getType()))
						c.setCaboose(cabooseCheckBox.isSelected());
				}
			}
		}
		_car.setCaboose(cabooseCheckBox.isSelected());
		// ask if all cars of this type should be utility
		if (_car.isUtility() ^ utilityCheckBox.isSelected()) {
			if (JOptionPane.showConfirmDialog(this, MessageFormat.format(
					utilityCheckBox.isSelected() ? Bundle.getString("carModifyTypeUtility")
							: Bundle.getString("carRemoveTypeUtility"), new Object[] { _car
							.getType() }), MessageFormat.format(
					Bundle.getString("carModifyAllType"), new Object[] { _car.getType() }),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				// go through the entire list and change the utility for all cars of this type
				List<String> cars = carManager.getList();
				for (int i = 0; i < cars.size(); i++) {
					Car c = carManager.getById(cars.get(i));
					if (c.getType().equals(_car.getType()))
						c.setUtility(utilityCheckBox.isSelected());
				}
			}
		}
		_car.setUtility(utilityCheckBox.isSelected());
		// ask if all cars of this type should be hazardous
		if (_car.isHazardous() ^ hazardousCheckBox.isSelected()) {
			if (JOptionPane.showConfirmDialog(this, MessageFormat.format(
					hazardousCheckBox.isSelected() ? Bundle.getString("carModifyTypeHazardous")
							: Bundle.getString("carRemoveTypeHazardous"), new Object[] { _car
							.getType() }), MessageFormat.format(
					Bundle.getString("carModifyAllType"), new Object[] { _car.getType() }),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				// go through the entire list and change the hazardous setting for all cars of this type
				List<String> cars = carManager.getList();
				for (int i = 0; i < cars.size(); i++) {
					Car c = carManager.getById(cars.get(i));
					if (c.getType().equals(_car.getType()))
						c.setHazardous(hazardousCheckBox.isSelected());
				}
			}
		}
		_car.setHazardous(hazardousCheckBox.isSelected());
		_car.setFred(fredCheckBox.isSelected());
		_car.setBuilt(builtTextField.getText());
		if (ownerComboBox.getSelectedItem() != null)
			_car.setOwner(ownerComboBox.getSelectedItem().toString());
		if (kernelComboBox.getSelectedItem() != null) {
			if (kernelComboBox.getSelectedItem().equals("")) {
				_car.setKernel(null);
			} else {
				_car.setKernel(carManager.getKernelByName((String) kernelComboBox.getSelectedItem()));
				// if car has FRED make lead
				if (_car.hasFred())
					_car.getKernel().setLead(_car);
			}
		}
		if (loadComboBox.getSelectedItem() != null) {
			String oldLoad = _car.getLoad();
			_car.setLoad(loadComboBox.getSelectedItem().toString());
			// check to see if car is part of kernel, and ask if all the other cars in the kernel should be changed
			if (_car.getKernel() != null && !oldLoad.equals(loadComboBox.getSelectedItem())) {
				if (JOptionPane.showConfirmDialog(this, Bundle.getString("carInKernel"),
						Bundle.getString("carPartKernel"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					// go through the entire list and change the loads for all cars
					List<Car> cars = _car.getKernel().getCars();
					for (int i = 0; i < cars.size(); i++) {
						Car c = cars.get(i);
						if (c.getType().equals(_car.getType())
								|| _car.getLoad().equals(CarLoads.instance().getDefaultEmptyName())
								|| _car.getLoad().equals(CarLoads.instance().getDefaultLoadName()))
							c.setLoad(_car.getLoad());
					}
				}
			}
		}
		_car.setComment(commentTextField.getText());
		_car.setValue(valueTextField.getText());
		_car.setRfid(rfidTextField.getText());
		autoTrackCheckBox.setEnabled(true);
		if (locationBox.getSelectedItem() != null) {
			if (locationBox.getSelectedItem().equals("")) {
				_car.setLocation(null, null);
			} else if (trackLocationBox.getSelectedItem() == null
					|| trackLocationBox.getSelectedItem().equals("")) {
				JOptionPane.showMessageDialog(this, Bundle.getString("rsFullySelect"),
						Bundle.getString("rsCanNotLoc"), JOptionPane.ERROR_MESSAGE);

			} else {
				// update location only if it has changed
				if (_car.getLocation() == null
						|| !_car.getLocation().equals(locationBox.getSelectedItem())
						|| _car.getTrack() == null
						|| !_car.getTrack().equals(trackLocationBox.getSelectedItem())) {
					String status = _car.setLocation((Location) locationBox.getSelectedItem(),
							(Track) trackLocationBox.getSelectedItem());
					if (!status.equals(Track.OKAY)) {
						log.debug("Can't set car's location because of " + status);
						JOptionPane.showMessageDialog(this, MessageFormat.format(
								Bundle.getString("rsCanNotLocMsg"), new Object[] { _car.toString(),
										status }), Bundle.getString("rsCanNotLoc"),
								JOptionPane.ERROR_MESSAGE);
						// does the user want to force the rolling stock to this track?
						int results = JOptionPane.showOptionDialog(this, MessageFormat.format(
								Bundle.getString("rsForce"), new Object[] { _car.toString(),
										(Track) trackLocationBox.getSelectedItem() }),
								MessageFormat.format(Bundle.getString("rsOverride"),
										new Object[] { status }), JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, null, null);
						if (results == JOptionPane.YES_OPTION) {
							log.debug("Force rolling stock to track");
							_car.setLocation((Location) locationBox.getSelectedItem(),
									(Track) trackLocationBox.getSelectedItem(), true);
						}
					}
				}
			}
		}
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
		if (editActive) {
			f.dispose();
		}
		f = new CarAttributeEditFrame();
		f.setLocationRelativeTo(this);
		f.addPropertyChangeListener(this);
		editActive = true;

		if (ae.getSource() == editRoadButton)
			f.initComponents(ROAD, (String) roadComboBox.getSelectedItem());
		if (ae.getSource() == editTypeButton)
			f.initComponents(TYPE, (String) typeComboBox.getSelectedItem());
		if (ae.getSource() == editColorButton)
			f.initComponents(COLOR, (String) colorComboBox.getSelectedItem());
		if (ae.getSource() == editLengthButton)
			f.initComponents(LENGTH, (String) lengthComboBox.getSelectedItem());
		if (ae.getSource() == editOwnerButton)
			f.initComponents(OWNER, (String) ownerComboBox.getSelectedItem());
		if (ae.getSource() == editKernelButton)
			f.initComponents(KERNEL, (String) kernelComboBox.getSelectedItem());
	}

	public void dispose() {
		removePropertyChangeListeners();
		super.dispose();
	}

	private void removePropertyChangeListeners() {
		CarRoads.instance().removePropertyChangeListener(this);
		CarLoads.instance().removePropertyChangeListener(this);
		CarTypes.instance().removePropertyChangeListener(this);
		CarLengths.instance().removePropertyChangeListener(this);
		CarColors.instance().removePropertyChangeListener(this);
		CarOwners.instance().removePropertyChangeListener(this);
		locationManager.removePropertyChangeListener(this);
		carManager.removePropertyChangeListener(this);
		if (_car != null)
			_car.removePropertyChangeListener(this);
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("CarEditFrame sees propertyChange " + e.getPropertyName() + " old: "
					+ e.getOldValue() + " new: " + e.getNewValue());
		if (e.getPropertyName().equals(CarRoads.CARROADS_LENGTH_CHANGED_PROPERTY)) {
			CarRoads.instance().updateComboBox(roadComboBox);
			if (_car != null)
				roadComboBox.setSelectedItem(_car.getRoad());
		}
		if (e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY)) {
			CarTypes.instance().updateComboBox(typeComboBox);
			if (_car != null)
				typeComboBox.setSelectedItem(_car.getType());
		}
		if (e.getPropertyName().equals(CarColors.CARCOLORS_CHANGED_PROPERTY)) {
			CarColors.instance().updateComboBox(colorComboBox);
			if (_car != null)
				colorComboBox.setSelectedItem(_car.getColor());
		}
		if (e.getPropertyName().equals(CarLengths.CARLENGTHS_CHANGED_PROPERTY)) {
			CarLengths.instance().updateComboBox(lengthComboBox);
			if (_car != null)
				lengthComboBox.setSelectedItem(_car.getLength());
		}
		if (e.getPropertyName().equals(CarManager.KERNELLISTLENGTH_CHANGED_PROPERTY)) {
			carManager.updateKernelComboBox(kernelComboBox);
			if (_car != null)
				kernelComboBox.setSelectedItem(_car.getKernelName());
		}
		if (e.getPropertyName().equals(CarOwners.CAROWNERS_LENGTH_CHANGED_PROPERTY)) {
			CarOwners.instance().updateComboBox(ownerComboBox);
			if (_car != null)
				ownerComboBox.setSelectedItem(_car.getOwner());
		}
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)) {
			LocationManager.instance().updateComboBox(locationBox);
			updateTrackLocationBox();
			if (_car != null)
				locationBox.setSelectedItem(_car.getLocation());
		}
		if (e.getPropertyName().equals(Car.LOAD_CHANGED_PROPERTY)) {
			if (_car != null)
				loadComboBox.setSelectedItem(_car.getLoad());
		}
		if (e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY)) {
			if (_car != null) {
				CarLoads.instance().updateComboBox((String) typeComboBox.getSelectedItem(),
						loadComboBox);
				loadComboBox.setSelectedItem(_car.getLoad());
			}
		}
		if (e.getPropertyName().equals(CarAttributeEditFrame.DISPOSE)) {
			editActive = false;
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CarEditFrame.class
			.getName());
}
