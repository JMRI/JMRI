//EngineEditFrame.java

package jmri.jmrit.operations.rollingstock.engines;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.jmrit.operations.OperationsFrame;
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
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.3 $
 */

public class EngineEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");

	EngineManager manager = EngineManager.instance();
	EngineManagerXml managerXml = EngineManagerXml.instance();
	EngineModels engineModels = EngineModels.instance();
	EngineTypes engineTypes = EngineTypes.instance();
	EngineLengths engineLengths = EngineLengths.instance();
	CarManagerXml carManagerXml = CarManagerXml.instance();
	LocationManager locationManager = LocationManager.instance();

	Engine _engine;

	// labels
	JLabel textRoad = new JLabel(rb.getString("Road"));
	JLabel textRoadNumber = new JLabel(rb.getString("RoadNumber"));
	JLabel textBuilt = new JLabel(rb.getString("BuildDate"));
	JLabel textLength = new JLabel(rb.getString("Length"));
	JLabel textModel = new JLabel(rb.getString("Model"));
	JLabel textHp = new JLabel(rb.getString("Hp"));
	JLabel textType = new JLabel(rb.getString("Type"));
	JLabel textLocation = new JLabel(rb.getString("Location"));
	JLabel textConsist = new JLabel(rb.getString("Consist"));
	JLabel textOwner = new JLabel(rb.getString("Owner"));
	JLabel textComment = new JLabel(rb.getString("Comment"));
	JLabel textRfid = new JLabel(rb.getString("Rfid"));

	// major buttons
	JButton editRoadButton = new JButton(rb.getString("Edit"));
	JButton clearRoadNumberButton = new JButton(rb.getString("Clear"));
	JButton editModelButton = new JButton(rb.getString("Edit"));
	JButton editTypeButton = new JButton(rb.getString("Edit"));
	JButton editColorButton = new JButton(rb.getString("Edit"));
	JButton editLengthButton = new JButton(rb.getString("Edit"));
	JButton fillWeightButton = new JButton();
	JButton editKernelButton = new JButton(rb.getString("Edit"));
	JButton editOwnerButton = new JButton(rb.getString("Edit"));

	JButton saveButton = new JButton(rb.getString("Save"));
	JButton deleteButton = new JButton(rb.getString("Delete"));
	JButton copyButton = new JButton(rb.getString("Copy"));
	JButton addButton = new JButton(rb.getString("Add"));

	// check boxes

	// text field
	JTextField roadNumberTextField = new JTextField(8);
	JTextField builtTextField = new JTextField(8);
	JTextField hpTextField = new JTextField(8);
	JTextField weightTextField = new JTextField(4);
	JTextField commentTextField = new JTextField(35);
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

	public static final String ROAD = rb.getString("Road");
	public static final String MODEL = rb.getString("Model");
	public static final String TYPE = rb.getString("Type");
	public static final String COLOR = rb.getString("Color");
	public static final String LENGTH = rb.getString("Length");
	public static final String OWNER = rb.getString("Owner");
	public static final String CONSIST = rb.getString("Consist");
	public static final String DISPOSE = "dispose" ;

	public EngineEditFrame() {
		super();
	}

	public void initComponents() {
		// set tips
		builtTextField.setToolTipText(rb.getString("buildDateTip"));
		rfidTextField.setToolTipText(rb.getString("TipRfid"));

		// create panel
		JPanel pPanel = new JPanel();
		pPanel.setLayout(new GridBagLayout());
		
		// Layout the panel by rows
		// row 1
		addItem(pPanel, textRoad, 0, 1);
		addItem(pPanel, roadComboBox, 1, 1);
		addItem(pPanel, editRoadButton, 2, 1);
		// row 2
		addItem(pPanel, textRoadNumber, 0, 2);
		addItem(pPanel, roadNumberTextField, 1, 2);
		addItem(pPanel, clearRoadNumberButton, 2, 2);
		// row 3
		addItem(pPanel, textModel, 0, 3);
		addItem(pPanel, modelComboBox, 1, 3);
		addItem(pPanel, editModelButton, 2, 3);
		// row4
		addItem(pPanel, textType, 0, 4);
		addItem(pPanel, typeComboBox, 1, 4);
		addItem(pPanel, editTypeButton, 2, 4);
		// row 5
		addItem(pPanel, textLength, 0, 5);
		addItem(pPanel, lengthComboBox, 1, 5);
		addItem(pPanel, editLengthButton, 2, 5);

		// row 7
		addItem(pPanel, textHp, 0, 7);
		addItem(pPanel, hpTextField, 1, 7);

		// row 8

		// row 9
		addItem(pPanel, textLocation, 0, 9);
		addItem(pPanel, locationBox, 1, 9);
		addItemWidth(pPanel, trackLocationBox, 2, 2, 9);

		// optional panel
		JPanel pOptional = new JPanel();
		pOptional.setLayout(new GridBagLayout());
		pOptional.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutOptional")));
		
		// row 13
		addItem(pOptional, textConsist, 0, 13);
		addItem(pOptional, consistComboBox, 1, 13);
		addItem(pOptional, editKernelButton, 2, 13);

		// row 14
		addItem(pOptional, textBuilt, 0, 14);
		addItem(pOptional, builtTextField, 1, 14);
		
		// row 15
		addItem(pOptional, textOwner, 0, 15);
		addItem(pOptional, ownerComboBox, 1, 15);
		addItem(pOptional, editOwnerButton, 2, 15);
		
		// row 18
		if(Setup.isRfidEnabled()){
			addItem(pOptional, textRfid, 0, 18);
			addItem(pOptional, rfidTextField, 1, 18);
		}

		// row 20
		addItem(pOptional, textComment, 0, 20);
		addItemWidth(pOptional, commentTextField, 2, 1, 20);

		// button panel
		JPanel pButtons = new JPanel();
		pButtons.setLayout(new GridBagLayout());
		addItem(pButtons, deleteButton, 0, 25);
		addItem(pButtons, addButton, 1, 25);
		addItem(pButtons, saveButton, 3, 25);
		
		// add panels
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		getContentPane().add(pPanel);
		getContentPane().add(pOptional);
		getContentPane().add(pButtons);

		// setup buttons
		addEditButtonAction(editRoadButton);
		addButtonAction(clearRoadNumberButton);
		addEditButtonAction(editModelButton);
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
		addComboBoxAction(modelComboBox);
		addComboBoxAction(locationBox);
		
		// setup checkbox

		// build menu
//		JMenuBar menuBar = new JMenuBar();
//		JMenu toolMenu = new JMenu("Tools");
//		menuBar.add(toolMenu);
//		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Engines", true);

		//	 get notified if combo box gets modified
		CarRoads.instance().addPropertyChangeListener(this);
		engineModels.addPropertyChangeListener(this);
		engineTypes.addPropertyChangeListener(this);
		engineLengths.addPropertyChangeListener(this);
		CarOwners.instance().addPropertyChangeListener(this);
		locationManager.addPropertyChangeListener(this);
		manager.addPropertyChangeListener(this);

		// set frame size and location for display
		pack();
		if ( (getWidth()<400)) 
			setSize(450, getHeight()+50);
		else
			setSize(getWidth()+50, getHeight()+50);
		setLocation(Control.panelX, Control.panelY);
		setVisible(true);	
	}

	public void loadEngine(Engine engine){
		_engine = engine;

		if (!CarRoads.instance().containsName(engine.getRoad())){
			String msg = MessageFormat.format(rb.getString("roadNameNotExist"),new Object[]{engine.getRoad()});
			if (JOptionPane.showConfirmDialog(this,
					msg, rb.getString("engineAddRoad"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarRoads.instance().addName(engine.getRoad());
			}
		}
		roadComboBox.setSelectedItem(engine.getRoad());

		roadNumberTextField.setText(engine.getNumber());

		if (!engineModels.containsName(engine.getModel())){
			String msg = MessageFormat.format(rb.getString("modelNameNotExist"),new Object[]{engine.getModel()});
			if (JOptionPane.showConfirmDialog(this,
					msg, rb.getString("engineAddModel"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				engineModels.addName(engine.getModel());
			}
		}
		modelComboBox.setSelectedItem(engine.getModel());
		
		if (!engineTypes.containsName(engine.getType())){
			String msg = MessageFormat.format(rb.getString("typeNameNotExist"),new Object[]{engine.getType()});
			if (JOptionPane.showConfirmDialog(this,
					msg, rb.getString("engineAddType"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				engineTypes.addName(engine.getType());
			}
		}
		typeComboBox.setSelectedItem(engine.getType());

		if (!engineLengths.containsName(engine.getLength())){
			String msg = MessageFormat.format(rb.getString("lengthNameNotExist"),new Object[]{engine.getLength()});
			if (JOptionPane.showConfirmDialog(this,
					msg, rb.getString("engineAddLength"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				engineLengths.addName(engine.getLength());
			}
		}
		lengthComboBox.setSelectedItem(engine.getLength());
		hpTextField.setText(engine.getHp());

		locationBox.setSelectedItem(engine.getLocation());
		Location l = locationManager.getLocationById(engine.getLocationId());
		if (l != null){
			l.updateComboBox(trackLocationBox);
			trackLocationBox.setSelectedItem(engine.getTrack());
		} else {
			trackLocationBox.removeAllItems();
		}

		builtTextField.setText(engine.getBuilt());

		if (!CarOwners.instance().containsName(engine.getOwner())){
			String msg = MessageFormat.format(rb.getString("ownerNameNotExist"),new Object[]{engine.getOwner()});
			if (JOptionPane.showConfirmDialog(this,
					msg, rb.getString("addOwner"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				CarOwners.instance().addName(engine.getOwner());
			}
		}
		consistComboBox.setSelectedItem(engine.getConsistName());
				
		ownerComboBox.setSelectedItem(engine.getOwner());
		rfidTextField.setText(engine.getRfid());
		commentTextField.setText(engine.getComment());
	}

	// combo boxes
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource()== modelComboBox){
			if (modelComboBox.getSelectedItem() != null){
				String model = (String)modelComboBox.getSelectedItem();
				// load the default hp and length for the model selected
				hpTextField.setText(engineModels.getModelHorsepower(model));
				if(engineModels.getModelLength(model)!= null && !engineModels.getModelLength(model).equals(""))
					lengthComboBox.setSelectedItem(engineModels.getModelLength(model));
				if(engineModels.getModelType(model)!= null && !engineModels.getModelType(model).equals(""))
					typeComboBox.setSelectedItem(engineModels.getModelType(model));
			}
		}
		if (ae.getSource()== locationBox){
			if (locationBox.getSelectedItem() != null){
				if (locationBox.getSelectedItem().equals("")){
					trackLocationBox.removeAllItems();
				}else{
					log.debug("EnginesSetFrame sees location: "+ locationBox.getSelectedItem());
					Location l = ((Location)locationBox.getSelectedItem());
					l.updateComboBox(trackLocationBox);
				}
			}
		}
	}
	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		JCheckBox b =  (JCheckBox)ae.getSource();
		log.debug("checkbox change "+ b.getText());
	}

	// Save, Delete, Add, Clear, Calculate buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton){
			// log.debug("engine save button actived");
			String roadNum = roadNumberTextField.getText();
			if (roadNum.length() > 10){
				JOptionPane.showMessageDialog(this,rb.getString("engineRoadNum"),
						rb.getString("engineRoadLong"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			// check to see if engine with road and number already exists
			Engine engine = manager.getEngineByRoadAndNumber(roadComboBox.getSelectedItem().toString(), roadNumberTextField.getText());
			if (engine != null){
				if (_engine == null || !engine.getId().equals(_engine.getId())){
					JOptionPane.showMessageDialog(this,
							rb.getString("engineExists"), rb.getString("engineCanNotUpdate"),
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
						Engine oldengine = _engine;
						Engine newEngine = addEngine();
						newEngine.setDestination(oldengine.getDestination(), oldengine.getDestinationTrack());
						newEngine.setTrain(oldengine.getTrain());
						manager.deregister(oldengine);
						managerXml.writeOperationsEngineFile();
						return;
					}
				}
			}
			addEngine();
			managerXml.writeOperationsEngineFile();		//save engine file
			carManagerXml.writeOperationsCarFile(); 	//save road names, and owners
		}
		if (ae.getSource() == deleteButton){
			log.debug("engine delete button actived");
			if (_engine != null
					&& _engine.getRoad().equals(roadComboBox.getSelectedItem().toString())
					&& _engine.getNumber().equals(roadNumberTextField.getText())) {
				manager.deregister(_engine);
				// save engine file
				managerXml.writeOperationsEngineFile();
			} else {
				Engine e = manager.getEngineByRoadAndNumber(roadComboBox.getSelectedItem().toString(), roadNumberTextField.getText());
				if (e != null){
					manager.deregister(e);
					// save engine file
					managerXml.writeOperationsEngineFile();
				}
			}
		}
		if (ae.getSource() == addButton){
			String roadNum = roadNumberTextField.getText();
			if (roadNum.length() > 10){
				JOptionPane.showMessageDialog(this, rb.getString("engineRoadNum"),
						rb.getString("engineRoadLong"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			Engine e = manager.getEngineByRoadAndNumber(roadComboBox.getSelectedItem().toString(), roadNumberTextField.getText() );
			if (e != null){
				log.info("Can not add, engine already exists");
				JOptionPane.showMessageDialog(this,
						rb.getString("engineExists"), rb.getString("engineCanNotUpdate"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			addEngine();
			// save engine file
			managerXml.writeOperationsEngineFile();
		}
		if (ae.getSource() == clearRoadNumberButton){
			roadNumberTextField.setText("");
			roadNumberTextField.requestFocus();
		}
	}

	private Engine addEngine() {
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
			if (consistComboBox.getSelectedItem() != null){
				if (consistComboBox.getSelectedItem().equals(""))
					_engine.setConsist(null);
				else
					_engine.setConsist(manager.getConsistByName((String)consistComboBox.getSelectedItem()));
			}
			// confirm that horsepower is a number
			if (!hpTextField.getText().equals("") ){
				try{
					Integer.parseInt(hpTextField.getText());
					_engine.setHp(hpTextField.getText());
				} catch (Exception e){
					JOptionPane.showMessageDialog(this,
							rb.getString("engineHorsepower"), rb.getString("engineCanNotHp"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
			if (locationBox.getSelectedItem() != null){
				if (locationBox.getSelectedItem().equals("")) {
					_engine.setLocation(null, null);
				} else {
					if (trackLocationBox.getSelectedItem() == null
							|| trackLocationBox.getSelectedItem()
							.equals("")) {
						JOptionPane.showMessageDialog(this,
								rb.getString("engineFullySelect"), rb.getString("engineCanNotLoc"),
								JOptionPane.ERROR_MESSAGE);

					} else {
						String status = _engine.setLocation((Location)locationBox.getSelectedItem(),
								(Track)trackLocationBox.getSelectedItem());
						if (!status.equals(Engine.OKAY)){
							log.debug ("Can't set engine's location because of "+ status);
							JOptionPane.showMessageDialog(this,
									rb.getString("engineCanNotLocMsg")+ status, rb.getString("engineCanNotLoc"),
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			_engine.setComment(commentTextField.getText());
			_engine.setRfid(rfidTextField.getText());
			return _engine;
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
		if(ae.getSource() == editTypeButton)
			f.initComponents(TYPE);
		if(ae.getSource() == editColorButton)
			f.initComponents(COLOR);
		if(ae.getSource() == editLengthButton)
			f.initComponents(LENGTH);
		if(ae.getSource() == editOwnerButton)
			f.initComponents(OWNER);
		if(ae.getSource() == editKernelButton)
			f.initComponents(CONSIST);
	}

	public void dispose(){
		removePropertyChangeListeners();
		super.dispose();
	}

	private void removePropertyChangeListeners(){
		CarRoads.instance().removePropertyChangeListener(this);
		engineModels.removePropertyChangeListener(this);
		engineTypes.removePropertyChangeListener(this);
		engineLengths.removePropertyChangeListener(this);
		CarOwners.instance().removePropertyChangeListener(this);
		locationManager.removePropertyChangeListener(this);
		manager.removePropertyChangeListener(this);
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("EngineEditFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());
		if (e.getPropertyName().equals(CarRoads.CARROADS_LENGTH_CHANGED_PROPERTY)){
			CarRoads.instance().updateComboBox(roadComboBox);
			if (_engine != null)
			roadComboBox.setSelectedItem(_engine.getRoad());
		}
		if (e.getPropertyName().equals(EngineModels.ENGINEMODELS_CHANGED_PROPERTY)){
			engineModels.updateComboBox(modelComboBox);
			if (_engine != null)
				modelComboBox.setSelectedItem(_engine.getModel());
		}
		if (e.getPropertyName().equals(EngineTypes.ENGINETYPES_LENGTH_CHANGED_PROPERTY)){
			engineTypes.updateComboBox(typeComboBox);
			if (_engine != null)
				typeComboBox.setSelectedItem(_engine.getType());
		}
		if (e.getPropertyName().equals(EngineLengths.ENGINELENGTHS_CHANGED_PROPERTY)){
			engineLengths.updateComboBox(lengthComboBox);
			if (_engine != null)
				lengthComboBox.setSelectedItem(_engine.getLength());
		}
		if (e.getPropertyName().equals(EngineManager.CONSISTLISTLENGTH_CHANGED_PROPERTY)){
			manager.updateConsistComboBox(consistComboBox);
			if (_engine != null) 
				consistComboBox.setSelectedItem(_engine.getConsistName());
		}
		if (e.getPropertyName().equals(CarOwners.CAROWNERS_CHANGED_PROPERTY)){
			CarOwners.instance().updateComboBox(ownerComboBox);
			if (_engine != null)
				ownerComboBox.setSelectedItem(_engine.getOwner());
		}
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)){
			LocationManager.instance().updateComboBox(locationBox);
			if (_engine != null)
				locationBox.setSelectedItem(_engine.getLocation());
		}
		if (e.getPropertyName().equals(DISPOSE)){
			editActive = false;
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(EngineEditFrame.class.getName());
}
