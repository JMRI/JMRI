//EngineEditFrame.java

package jmri.jmrit.operations.rollingstock.engines;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Frame for user edit of engine
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2011
 * @version $Revision$
 */

public class EngineEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	EngineManager manager = EngineManager.instance();
	EngineManagerXml managerXml = EngineManagerXml.instance();
	EngineModels engineModels = EngineModels.instance();
	EngineTypes engineTypes = EngineTypes.instance();
	EngineLengths engineLengths = EngineLengths.instance();
	CarManagerXml carManagerXml = CarManagerXml.instance();
	LocationManager locationManager = LocationManager.instance();

	Engine _engine;

	// major buttons
	JButton editRoadButton = new JButton(Bundle.getMessage("Edit"));
	JButton clearRoadNumberButton = new JButton(Bundle.getMessage("Clear"));
	JButton editModelButton = new JButton(Bundle.getMessage("Edit"));
	JButton editTypeButton = new JButton(Bundle.getMessage("Edit"));
	JButton editLengthButton = new JButton(Bundle.getMessage("Edit"));
	JButton fillWeightButton = new JButton();
	JButton editConsistButton = new JButton(Bundle.getMessage("Edit"));
	JButton editOwnerButton = new JButton(Bundle.getMessage("Edit"));

	JButton saveButton = new JButton(Bundle.getMessage("Save"));
	JButton deleteButton = new JButton(Bundle.getMessage("Delete"));
	JButton addButton = new JButton(Bundle.getMessage("Add"));

	// check boxes

	// text field
	JTextField roadNumberTextField = new JTextField(Control.max_len_string_road_number);
	JTextField builtTextField = new JTextField(Control.max_len_string_built_name + 3);
	JTextField hpTextField = new JTextField(8);
	JTextField weightTextField = new JTextField(Control.max_len_string_weight_name);
	JTextField commentTextField = new JTextField(35);
	JTextField valueTextField = new JTextField(8);
	JTextField rfidTextField = new JTextField(16);

	// combo boxes
	JComboBox roadComboBox = CarRoads.instance().getComboBox();
	JComboBox modelComboBox = engineModels.getComboBox();
	JComboBox typeComboBox = engineTypes.getComboBox();
	JComboBox lengthComboBox = engineLengths.getComboBox();
	JComboBox ownerComboBox = CarOwners.instance().getComboBox();
	JComboBox locationBox = locationManager.getComboBox();
	JComboBox trackLocationBox = new JComboBox();
	JComboBox consistComboBox = manager.getConsistComboBox();

	public static final String ROAD = Bundle.getMessage("Road");
	public static final String MODEL = Bundle.getMessage("Model");
	public static final String TYPE = Bundle.getMessage("Type");
	public static final String COLOR = Bundle.getMessage("Color");
	public static final String LENGTH = Bundle.getMessage("Length");
	public static final String OWNER = Bundle.getMessage("Owner");
	public static final String CONSIST = Bundle.getMessage("Consist");

	public EngineEditFrame() {
		super();
	}

	public void initComponents() {
		// set tips
		builtTextField.setToolTipText(Bundle.getMessage("buildDateTip"));
		rfidTextField.setToolTipText(Bundle.getMessage("TipRfid"));

		// create panel
		JPanel pPanel = new JPanel();
		pPanel.setLayout(new BoxLayout(pPanel, BoxLayout.Y_AXIS));

		// Layout the panel by rows
		// row 1
		JPanel pRoad = new JPanel();
		pRoad.setLayout(new GridBagLayout());
		pRoad.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Road")));
		addItem(pRoad, roadComboBox, 1, 0);
		addItem(pRoad, editRoadButton, 2, 0);
		pPanel.add(pRoad);

		// row 2
		JPanel pRoadNumber = new JPanel();
		pRoadNumber.setLayout(new GridBagLayout());
		pRoadNumber.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RoadNumber")));
		addItem(pRoadNumber, roadNumberTextField, 1, 0);
		addItem(pRoadNumber, clearRoadNumberButton, 2, 0);
		pPanel.add(pRoadNumber);

		// row 3
		JPanel pModel = new JPanel();
		pModel.setLayout(new GridBagLayout());
		pModel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Model")));
		addItem(pModel, modelComboBox, 1, 0);
		addItem(pModel, editModelButton, 2, 0);
		pPanel.add(pModel);

		// row 4
		JPanel pType = new JPanel();
		pType.setLayout(new GridBagLayout());
		pType.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Type")));
		addItem(pType, typeComboBox, 1, 0);
		addItem(pType, editTypeButton, 2, 0);
		pPanel.add(pType);

		// row 5
		JPanel pLength = new JPanel();
		pLength.setLayout(new GridBagLayout());
		pLength.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Length")));
		addItem(pLength, lengthComboBox, 1, 0);
		addItem(pLength, editLengthButton, 2, 0);
		pPanel.add(pLength);

		// row 6
		JPanel pLocation = new JPanel();
		pLocation.setLayout(new GridBagLayout());
		pLocation.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("LocationAndTrack")));
		addItem(pLocation, locationBox, 1, 0);
		addItem(pLocation, trackLocationBox, 2, 0);
		pPanel.add(pLocation);

		// optional panel
		JPanel pOptional = new JPanel();
		pOptional.setLayout(new BoxLayout(pOptional, BoxLayout.Y_AXIS));
		JScrollPane optionPane = new JScrollPane(pOptional);
		optionPane.setBorder(BorderFactory.createTitledBorder(Bundle
				.getMessage("BorderLayoutOptional")));

		// row 11
		JPanel pWeightTons = new JPanel();
		pWeightTons.setLayout(new GridBagLayout());
		pWeightTons.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("WeightTons")));
		addItem(pWeightTons, weightTextField, 0, 0);
		pOptional.add(pWeightTons);

		// row 12
		JPanel pHp = new JPanel();
		pHp.setLayout(new GridBagLayout());
		pHp.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Hp")));
		addItem(pHp, hpTextField, 0, 0);
		pOptional.add(pHp);

		// row 13
		JPanel pConsist = new JPanel();
		pConsist.setLayout(new GridBagLayout());
		pConsist.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Consist")));
		addItem(pConsist, consistComboBox, 1, 0);
		addItem(pConsist, editConsistButton, 2, 0);
		pOptional.add(pConsist);

		// row 14
		JPanel pBuilt = new JPanel();
		pBuilt.setLayout(new GridBagLayout());
		pBuilt.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Built")));
		addItem(pBuilt, builtTextField, 1, 0);
		pOptional.add(pBuilt);

		// row 15
		JPanel pOwner = new JPanel();
		pOwner.setLayout(new GridBagLayout());
		pOwner.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Owner")));
		addItem(pOwner, ownerComboBox, 1, 0);
		addItem(pOwner, editOwnerButton, 2, 0);
		pOptional.add(pOwner);

		// row 18
		if (Setup.isValueEnabled()) {
			JPanel pValue = new JPanel();
			pValue.setLayout(new GridBagLayout());
			pValue.setBorder(BorderFactory.createTitledBorder(Setup.getValueLabel()));
			addItem(pValue, valueTextField, 1, 0);
			pOptional.add(pValue);
		}

		// row 20
		if (Setup.isRfidEnabled()) {
			JPanel pRfid = new JPanel();
			pRfid.setLayout(new GridBagLayout());
			pRfid.setBorder(BorderFactory.createTitledBorder(Setup.getRfidLabel()));
			addItem(pRfid, rfidTextField, 1, 0);
			pOptional.add(pRfid);
		}

		// row 22
		JPanel pComment = new JPanel();
		pComment.setLayout(new GridBagLayout());
		pComment.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
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
		addEditButtonAction(editModelButton);
		addEditButtonAction(editTypeButton);
		addEditButtonAction(editLengthButton);
		addEditButtonAction(editConsistButton);
		addEditButtonAction(editOwnerButton);

		addButtonAction(deleteButton);
		addButtonAction(addButton);
		addButtonAction(saveButton);
		addButtonAction(fillWeightButton);

		// setup combobox
		addComboBoxAction(modelComboBox);
		addComboBoxAction(locationBox);

		// setup checkbox

		// build menu
		// JMenuBar menuBar = new JMenuBar();
		// JMenu toolMenu = new JMenu("Tools");
		// menuBar.add(toolMenu);
		// setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_LocomotivesAdd", true); // NOI18N

		// get notified if combo box gets modified
		CarRoads.instance().addPropertyChangeListener(this);
		engineModels.addPropertyChangeListener(this);
		engineTypes.addPropertyChangeListener(this);
		engineLengths.addPropertyChangeListener(this);
		CarOwners.instance().addPropertyChangeListener(this);
		locationManager.addPropertyChangeListener(this);
		manager.addPropertyChangeListener(this);

		pack();
		if (getWidth() < 450)
			setSize(450, getHeight());
		if (getHeight() < 500)
			setSize(getWidth(), 500);
		setMinimumSize(new Dimension(450, Control.panelHeight));
		setVisible(true);
	}

	public void loadEngine(Engine engine) {
		_engine = engine;

		if (!CarRoads.instance().containsName(engine.getRoad())) {
			String msg = MessageFormat.format(Bundle.getMessage("roadNameNotExist"),
					new Object[] { engine.getRoad() });
			if (JOptionPane.showConfirmDialog(this, msg, Bundle.getMessage("engineAddRoad"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				CarRoads.instance().addName(engine.getRoad());
			}
		}
		roadComboBox.setSelectedItem(engine.getRoad());

		roadNumberTextField.setText(engine.getNumber());

		if (!engineModels.containsName(engine.getModel())) {
			String msg = MessageFormat.format(Bundle.getMessage("modelNameNotExist"),
					new Object[] { engine.getModel() });
			if (JOptionPane.showConfirmDialog(this, msg, Bundle.getMessage("engineAddModel"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				engineModels.addName(engine.getModel());
			}
		}
		modelComboBox.setSelectedItem(engine.getModel());

		if (!engineTypes.containsName(engine.getType())) {
			String msg = MessageFormat.format(Bundle.getMessage("typeNameNotExist"),
					new Object[] { engine.getType() });
			if (JOptionPane.showConfirmDialog(this, msg, Bundle.getMessage("engineAddType"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				engineTypes.addName(engine.getType());
			}
		}
		typeComboBox.setSelectedItem(engine.getType());

		if (!engineLengths.containsName(engine.getLength())) {
			String msg = MessageFormat.format(Bundle.getMessage("lengthNameNotExist"),
					new Object[] { engine.getLength() });
			if (JOptionPane.showConfirmDialog(this, msg, Bundle.getMessage("engineAddLength"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				engineLengths.addName(engine.getLength());
			}
		}
		lengthComboBox.setSelectedItem(engine.getLength());
		weightTextField.setText(engine.getWeightTons());
		hpTextField.setText(engine.getHp());

		locationBox.setSelectedItem(engine.getLocation());
		Location l = locationManager.getLocationById(engine.getLocationId());
		if (l != null) {
			l.updateComboBox(trackLocationBox);
			trackLocationBox.setSelectedItem(engine.getTrack());
		} else {
			trackLocationBox.removeAllItems();
		}

		builtTextField.setText(engine.getBuilt());

		if (!CarOwners.instance().containsName(engine.getOwner())) {
			String msg = MessageFormat.format(Bundle.getMessage("ownerNameNotExist"),
					new Object[] { engine.getOwner() });
			if (JOptionPane.showConfirmDialog(this, msg, Bundle.getMessage("addOwner"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				CarOwners.instance().addName(engine.getOwner());
			}
		}
		consistComboBox.setSelectedItem(engine.getConsistName());

		ownerComboBox.setSelectedItem(engine.getOwner());
		valueTextField.setText(engine.getValue());
		rfidTextField.setText(engine.getRfid());
		commentTextField.setText(engine.getComment());
	}

	// combo boxes
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == modelComboBox) {
			if (modelComboBox.getSelectedItem() != null) {
				String model = (String) modelComboBox.getSelectedItem();
				// load the default hp and length for the model selected
				hpTextField.setText(engineModels.getModelHorsepower(model));
				weightTextField.setText(engineModels.getModelWeight(model));
				if (engineModels.getModelLength(model) != null
						&& !engineModels.getModelLength(model).equals(""))
					lengthComboBox.setSelectedItem(engineModels.getModelLength(model));
				if (engineModels.getModelType(model) != null
						&& !engineModels.getModelType(model).equals(""))
					typeComboBox.setSelectedItem(engineModels.getModelType(model));
			}
		}
		if (ae.getSource() == locationBox) {
			if (locationBox.getSelectedItem() != null) {
				if (locationBox.getSelectedItem().equals("")) {
					trackLocationBox.removeAllItems();
				} else {
					log.debug("EnginesSetFrame sees location: " + locationBox.getSelectedItem());
					Location l = ((Location) locationBox.getSelectedItem());
					l.updateComboBox(trackLocationBox);
				}
			}
		}
	}

	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		JCheckBox b = (JCheckBox) ae.getSource();
		log.debug("checkbox change " + b.getText());
	}

	// Save, Delete, Add, Clear, Calculate buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton) {
			// log.debug("engine save button activated");
			String roadNum = roadNumberTextField.getText();
			if (roadNum.length() > 10) {
				JOptionPane.showMessageDialog(this, Bundle.getMessage("engineRoadNum"),
						Bundle.getMessage("engineRoadLong"), JOptionPane.ERROR_MESSAGE);
				return;
			}
			// check to see if engine with road and number already exists
			Engine engine = manager.getByRoadAndNumber(roadComboBox.getSelectedItem().toString(),
					roadNumberTextField.getText());
			if (engine != null) {
				if (_engine == null || !engine.getId().equals(_engine.getId())) {
					JOptionPane.showMessageDialog(this, Bundle.getMessage("engineExists"),
							Bundle.getMessage("engineCanNotUpdate"), JOptionPane.ERROR_MESSAGE);
					return;
				}
			}

			// if the road or number changes, the loco needs a new id
			if (_engine != null
					&& _engine.getRoad() != null
					&& !_engine.getRoad().equals("")
					&& (!_engine.getRoad().equals(roadComboBox.getSelectedItem().toString()) || !_engine
							.getNumber().equals(roadNumberTextField.getText()))) {
				String road = roadComboBox.getSelectedItem().toString();
				String number = roadNumberTextField.getText();
				manager.changeId(_engine, road, number);
				_engine.setRoad(road);
				_engine.setNumber(number);
			}
			addEngine();
			/*
			 * all JMRI window position and size are now saved // save frame size and position
			 * manager.setEditFrame(this);
			 */
			writeFiles();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
		if (ae.getSource() == deleteButton) {
			log.debug("engine delete button activated");
			if (_engine != null
					&& _engine.getRoad().equals(roadComboBox.getSelectedItem().toString())
					&& _engine.getNumber().equals(roadNumberTextField.getText())) {
				manager.deregister(_engine);
				_engine = null;
				// save engine file
				writeFiles();
			} else {
				Engine e = manager.getByRoadAndNumber(roadComboBox.getSelectedItem().toString(),
						roadNumberTextField.getText());
				if (e != null) {
					manager.deregister(e);
					// save engine file
					writeFiles();
				}
			}
		}
		if (ae.getSource() == addButton) {
			String roadNum = roadNumberTextField.getText();
			if (roadNum.length() > 10) {
				JOptionPane.showMessageDialog(this, Bundle.getMessage("engineRoadNum"),
						Bundle.getMessage("engineRoadLong"), JOptionPane.ERROR_MESSAGE);
				return;
			}
			Engine e = manager.getByRoadAndNumber(roadComboBox.getSelectedItem().toString(),
					roadNumberTextField.getText());
			if (e != null) {
				log.info("Can not add, engine already exists");
				JOptionPane.showMessageDialog(this, Bundle.getMessage("engineExists"),
						Bundle.getMessage("engineCanNotUpdate"), JOptionPane.ERROR_MESSAGE);
				return;
			}
			addEngine();
			// save engine file
			writeFiles();
		}
		if (ae.getSource() == clearRoadNumberButton) {
			roadNumberTextField.setText("");
			roadNumberTextField.requestFocus();
		}
	}

	private void addEngine() {
		if (roadComboBox.getSelectedItem() != null
				&& !roadComboBox.getSelectedItem().toString().equals("")) {
			if (_engine == null
					|| !_engine.getRoad().equals(roadComboBox.getSelectedItem().toString())
					|| !_engine.getNumber().equals(roadNumberTextField.getText())) {
				_engine = manager.newEngine(roadComboBox.getSelectedItem().toString(),
						roadNumberTextField.getText());
			}
			if (modelComboBox.getSelectedItem() != null)
				_engine.setModel(modelComboBox.getSelectedItem().toString());
			if (typeComboBox.getSelectedItem() != null)
				_engine.setType(typeComboBox.getSelectedItem().toString());
			if (lengthComboBox.getSelectedItem() != null)
				_engine.setLength(lengthComboBox.getSelectedItem().toString());
			_engine.setBuilt(builtTextField.getText());
			if (ownerComboBox.getSelectedItem() != null)
				_engine.setOwner(ownerComboBox.getSelectedItem().toString());
			if (consistComboBox.getSelectedItem() != null) {
				if (consistComboBox.getSelectedItem().equals(""))
					_engine.setConsist(null);
				else
					_engine.setConsist(manager.getConsistByName((String) consistComboBox
							.getSelectedItem()));
			}
			// confirm that weight is a number
			if (!weightTextField.getText().equals("")) {
				try {
					Integer.parseInt(weightTextField.getText());
					_engine.setWeightTons(weightTextField.getText());
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, Bundle.getMessage("engineWeight"),
							Bundle.getMessage("engineCanNotWeight"), JOptionPane.ERROR_MESSAGE);
				}
			}
			// confirm that horsepower is a number
			if (!hpTextField.getText().equals("")) {
				try {
					Integer.parseInt(hpTextField.getText());
					_engine.setHp(hpTextField.getText());
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, Bundle.getMessage("engineHorsepower"),
							Bundle.getMessage("engineCanNotHp"), JOptionPane.ERROR_MESSAGE);
				}
			}
			if (locationBox.getSelectedItem() != null) {
				if (locationBox.getSelectedItem().equals("")) {
					_engine.setLocation(null, null);
				} else {
					if (trackLocationBox.getSelectedItem() == null
							|| trackLocationBox.getSelectedItem().equals("")) {
						JOptionPane.showMessageDialog(this, Bundle.getMessage("rsFullySelect"),
								Bundle.getMessage("rsCanNotLoc"), JOptionPane.ERROR_MESSAGE);

					} else {
						String status = _engine.setLocation(
								(Location) locationBox.getSelectedItem(),
								(Track) trackLocationBox.getSelectedItem());
						if (!status.equals(Track.OKAY)) {
							log.debug("Can't set engine's location because of " + status);
							JOptionPane.showMessageDialog(this, MessageFormat.format(
									Bundle.getMessage("rsCanNotLocMsg"),
									new Object[] { _engine.toString(), status }), Bundle
									.getMessage("rsCanNotLoc"), JOptionPane.ERROR_MESSAGE);
							// does the user want to force the rolling stock to this track?
							int results = JOptionPane.showOptionDialog(this, MessageFormat.format(
									Bundle.getMessage("rsForce"), new Object[] { _engine.toString(),
											(Track) trackLocationBox.getSelectedItem() }),
									MessageFormat.format(Bundle.getMessage("rsOverride"),
											new Object[] { status }), JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE, null, null, null);
							if (results == JOptionPane.YES_OPTION) {
								log.debug("Force rolling stock to track");
								_engine.setLocation((Location) locationBox.getSelectedItem(),
										(Track) trackLocationBox.getSelectedItem(), true);
							}
						}
					}
				}
			}
			_engine.setComment(commentTextField.getText());
			_engine.setValue(valueTextField.getText());
			_engine.setRfid(rfidTextField.getText());
		}
	}

	private void addEditButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonEditActionPerformed(e);
			}
		});
	}

	/**
	 * Need to also write the location and train files if a road name was deleted. Need to also write files if car type
	 * was changed.
	 */
	private void writeFiles() {
		OperationsXml.save(); // save engine file
	}

	private boolean editActive = false;
	EngineAttributeEditFrame f;

	// edit buttons only one frame active at a time
	public void buttonEditActionPerformed(java.awt.event.ActionEvent ae) {
		if (editActive) {
			f.dispose();
		}
		f = new EngineAttributeEditFrame();
		f.setLocationRelativeTo(this);
		f.addPropertyChangeListener(this);
		editActive = true;

		if (ae.getSource() == editRoadButton)
			f.initComponents(ROAD, (String) roadComboBox.getSelectedItem());
		if (ae.getSource() == editModelButton)
			f.initComponents(MODEL, (String) modelComboBox.getSelectedItem());
		if (ae.getSource() == editTypeButton)
			f.initComponents(TYPE, (String) typeComboBox.getSelectedItem());
		if (ae.getSource() == editLengthButton)
			f.initComponents(LENGTH, (String) lengthComboBox.getSelectedItem());
		if (ae.getSource() == editOwnerButton)
			f.initComponents(OWNER, (String) ownerComboBox.getSelectedItem());
		if (ae.getSource() == editConsistButton)
			f.initComponents(CONSIST, (String) consistComboBox.getSelectedItem());
	}

	public void dispose() {
		removePropertyChangeListeners();
		super.dispose();
	}

	private void removePropertyChangeListeners() {
		CarRoads.instance().removePropertyChangeListener(this);
		engineModels.removePropertyChangeListener(this);
		engineTypes.removePropertyChangeListener(this);
		engineLengths.removePropertyChangeListener(this);
		CarOwners.instance().removePropertyChangeListener(this);
		locationManager.removePropertyChangeListener(this);
		manager.removePropertyChangeListener(this);
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug("EngineEditFrame sees propertyChange " + e.getPropertyName() + " "
				+ e.getNewValue());
		if (e.getPropertyName().equals(CarRoads.CARROADS_LENGTH_CHANGED_PROPERTY)) {
			CarRoads.instance().updateComboBox(roadComboBox);
			if (_engine != null)
				roadComboBox.setSelectedItem(_engine.getRoad());
		}
		if (e.getPropertyName().equals(EngineModels.ENGINEMODELS_CHANGED_PROPERTY)) {
			engineModels.updateComboBox(modelComboBox);
			if (_engine != null)
				modelComboBox.setSelectedItem(_engine.getModel());
		}
		if (e.getPropertyName().equals(EngineTypes.ENGINETYPES_LENGTH_CHANGED_PROPERTY)) {
			engineTypes.updateComboBox(typeComboBox);
			if (_engine != null)
				typeComboBox.setSelectedItem(_engine.getType());
		}
		if (e.getPropertyName().equals(EngineLengths.ENGINELENGTHS_CHANGED_PROPERTY)) {
			engineLengths.updateComboBox(lengthComboBox);
			if (_engine != null)
				lengthComboBox.setSelectedItem(_engine.getLength());
		}
		if (e.getPropertyName().equals(EngineManager.CONSISTLISTLENGTH_CHANGED_PROPERTY)) {
			manager.updateConsistComboBox(consistComboBox);
			if (_engine != null)
				consistComboBox.setSelectedItem(_engine.getConsistName());
		}
		if (e.getPropertyName().equals(CarOwners.CAROWNERS_LENGTH_CHANGED_PROPERTY)) {
			CarOwners.instance().updateComboBox(ownerComboBox);
			if (_engine != null)
				ownerComboBox.setSelectedItem(_engine.getOwner());
		}
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)) {
			LocationManager.instance().updateComboBox(locationBox);
			if (_engine != null)
				locationBox.setSelectedItem(_engine.getLocation());
		}
		if (e.getPropertyName().equals(EngineAttributeEditFrame.DISPOSE)) {
			editActive = false;
		}
	}

	static Logger log = LoggerFactory.getLogger(EngineEditFrame.class
			.getName());
}
