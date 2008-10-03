// TrainsEditFrame.java

package jmri.jmrit.operations.trains;

import jmri.jmrit.operations.cars.CarTypes;
import jmri.jmrit.operations.cars.CarRoads;
import jmri.jmrit.operations.locations.Location;

import jmri.jmrit.operations.routes.RouteEditFrame;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.Route;

import jmri.jmrit.operations.engines.EngineModels;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.setup.OperationsXml;

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
 * Frame for user edit of route
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.5 $
 */

public class TrainEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	
	TrainManager manager;
	TrainManagerXml managerXml;
	RouteManager routeManager;

	Train _train = null;
	List typeCheckBoxes = new ArrayList();
	List locationCheckBoxes = new ArrayList();
	JPanel typePanelCheckBoxes = new JPanel();
	JPanel panelRoadNames = new JPanel();
	JPanel locationPanelCheckBoxes = new JPanel();
	JScrollPane locationsPane;

	// labels
	javax.swing.JLabel textName = new javax.swing.JLabel();
	javax.swing.JLabel textDescription = new javax.swing.JLabel();
	javax.swing.JLabel textRoute = new javax.swing.JLabel();
	javax.swing.JLabel textType = new javax.swing.JLabel();
	javax.swing.JLabel textModel = new javax.swing.JLabel();
	javax.swing.JLabel textRoad = new javax.swing.JLabel();
	javax.swing.JLabel textRoad2 = new javax.swing.JLabel();
	javax.swing.JLabel textRoad3 = new javax.swing.JLabel();
	javax.swing.JLabel textEngine = new javax.swing.JLabel();
	javax.swing.JLabel textStops = new javax.swing.JLabel();
	javax.swing.JLabel textTrainRequires = new javax.swing.JLabel();
	javax.swing.JLabel textCars = new javax.swing.JLabel();
	javax.swing.JLabel textOptional = new javax.swing.JLabel();
	javax.swing.JLabel textComment = new javax.swing.JLabel();

	// major buttons
	javax.swing.JButton editButton = new javax.swing.JButton();
	javax.swing.JButton clearButton = new javax.swing.JButton();
	javax.swing.JButton setButton = new javax.swing.JButton();
	javax.swing.JButton deleteRoadButton = new javax.swing.JButton();
	javax.swing.JButton addRoadButton = new javax.swing.JButton();
	javax.swing.JButton resetButton = new javax.swing.JButton();
	javax.swing.JButton saveTrainButton = new javax.swing.JButton();
	javax.swing.JButton deleteTrainButton = new javax.swing.JButton();
	javax.swing.JButton addTrainButton = new javax.swing.JButton();

	// check boxes

	// radio buttons
    javax.swing.JRadioButton noneRadioButton = new javax.swing.JRadioButton(rb.getString("None"));
    javax.swing.JRadioButton cabooseRadioButton = new javax.swing.JRadioButton(rb.getString("Caboose"));
    javax.swing.JRadioButton fredRadioButton = new javax.swing.JRadioButton(rb.getString("fred"));
    ButtonGroup group = new ButtonGroup();
    
    javax.swing.JRadioButton roadNameAll = new javax.swing.JRadioButton("Accept all");
    javax.swing.JRadioButton roadNameInclude = new javax.swing.JRadioButton("Accept only");
    javax.swing.JRadioButton roadNameExclude = new javax.swing.JRadioButton("Exclude");
    ButtonGroup roadGroup = new ButtonGroup();
	
	// text field
	javax.swing.JTextField trainNameTextField = new javax.swing.JTextField(18);
	javax.swing.JTextField trainDescriptionTextField = new javax.swing.JTextField(30);
	javax.swing.JTextField commentTextField = new javax.swing.JTextField(35);

	// for padding out panel
	javax.swing.JLabel space0 = new javax.swing.JLabel();
	javax.swing.JLabel space1 = new javax.swing.JLabel();
	javax.swing.JLabel space2 = new javax.swing.JLabel();
	javax.swing.JLabel space3 = new javax.swing.JLabel();
	javax.swing.JLabel space4 = new javax.swing.JLabel();
	javax.swing.JLabel space5 = new javax.swing.JLabel();
	
	// combo boxes
	javax.swing.JComboBox routeBox = RouteManager.instance().getComboBox();
	javax.swing.JComboBox roadBox = CarRoads.instance().getComboBox();
	javax.swing.JComboBox road2Box = CarRoads.instance().getComboBox();
	javax.swing.JComboBox roadEngineBox = CarRoads.instance().getComboBox();
	javax.swing.JComboBox modelEngineBox = EngineModels.instance().getComboBox();
	javax.swing.JComboBox numEnginesBox = new javax.swing.JComboBox();

	public static final String DISPOSE = "dispose" ;

	public TrainEditFrame() {
		super();
    	// Set up the jtable in a Scroll Pane..
    	locationsPane = new JScrollPane(locationPanelCheckBoxes);
    	locationsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	}

	public void initComponents(Train train) {
		_train = train;

		// load managers
		manager = TrainManager.instance();
		managerXml = TrainManagerXml.instance();
		routeManager = RouteManager.instance();
		OperationsXml.instance();					// force settings to load 

		// the following code sets the frame's initial state
		
		textName.setText(rb.getString("Name"));
		textName.setVisible(true);
		textDescription.setText(rb.getString("Description"));
		textDescription.setVisible(true);
		textRoute.setText(rb.getString("Route"));
		textRoute.setVisible(true);
		textRoad.setText(rb.getString("RoadsTrain"));
		textRoad.setVisible(true);
		textRoad2.setText(rb.getString("Road"));
		textRoad2.setVisible(true);
		textRoad3.setText(rb.getString("Road"));
		textRoad3.setVisible(true);
		textModel.setText(rb.getString("Model"));
		textModel.setVisible(true);
		textEngine.setText(rb.getString("Engines"));
		textEngine.setVisible(true);
		editButton.setText(rb.getString("Edit"));
		clearButton.setVisible(true);
		textStops.setText("       "+rb.getString("Stops"));
		textStops.setVisible(true);
		textCars.setVisible(true);
		
		textOptional.setText("-------------------------------- Optional ------------------------------------");
		textOptional.setVisible(true);
		textComment.setText(rb.getString("Comment"));
		textComment.setVisible(true);
		textType.setText(rb.getString("TypesTrain"));
		textType.setVisible(true);
		resetButton.setText(rb.getString("ClearCars"));
		resetButton.setVisible(true);
		addRoadButton.setText(rb.getString("AddRoad"));
		addRoadButton.setVisible(true);
		deleteRoadButton.setText(rb.getString("DeleteRoad"));
		deleteRoadButton.setVisible(true);
		textTrainRequires.setText(rb.getString("TrainRequires"));
		textTrainRequires.setVisible(true);
		clearButton.setText(rb.getString("Clear"));
		clearButton.setVisible(true);
		setButton.setText(rb.getString("Select"));
		setButton.setVisible(true);
		space0.setText("     ");
		space0.setVisible(true);
		space1.setText("     ");
		space1.setVisible(true);
		space2.setText("     ");
		space2.setVisible(true);
		space3.setText("     ");
		space3.setVisible(true);
		space4.setText("     ");
		space4.setVisible(true);
		space5.setText("     ");
		space5.setVisible(true);

		deleteTrainButton.setText(rb.getString("DeleteTrain"));
		deleteTrainButton.setVisible(true);
		addTrainButton.setText(rb.getString("AddTrain"));
		addTrainButton.setVisible(true);
		saveTrainButton.setText(rb.getString("SaveTrain"));
		saveTrainButton.setVisible(true);
	
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

	    //      Set up the panels
    	JPanel p1 = new JPanel();
    	p1.setLayout(new GridBagLayout());
				
		// Layout the panel by rows
		// row 1
//    	addItem(p1, space0, 0, 0);
		addItem(p1, textName, 0, 1);
		addItemWidth(p1, trainNameTextField, 3, 1, 1);

		// row 2
		addItem(p1, space3, 0, 2);
		addItem(p1, textDescription, 0, 3);
		addItemWidth(p1, trainDescriptionTextField, 3, 1, 3);
//		addItem(p1, space5, 0, 6);
//		Border border = BorderFactory.createEtchedBorder();
//		p1.setBorder(border);

		// row 3
		// BUG! routeBox needs its own panel when resizing frame!
	   	JPanel p2 = new JPanel();
    	p2.setLayout(new GridBagLayout());
//		addItem(p2, space4, 0, 4);
		addItem(p2, textRoute, 0, 5);
		addItem(p2, routeBox, 1, 5);
		addItem(p2, editButton, 2, 5);
		Border border = BorderFactory.createEtchedBorder();
		p2.setBorder(border);
		
		// row 4


		// row 5
	   	typePanelCheckBoxes.setLayout(new GridBagLayout());
		updateTypeCheckboxes();


		// row 7
	   	locationPanelCheckBoxes.setLayout(new GridBagLayout());
		updateLocationCheckboxes();
		
		// row 8
		
		panelRoadNames.setLayout(new GridBagLayout());
		roadGroup.add(roadNameAll);
		roadGroup.add(roadNameInclude);
		roadGroup.add(roadNameExclude);
		updateRoadNames();
		
		// row 9
		JPanel trainReq = new JPanel();
    	trainReq.setLayout(new GridBagLayout());
    	addItem (trainReq, textTrainRequires, 0, 1);
    	for (int i=0; i<Setup.getEngineSize()+1; i++){
    		numEnginesBox.addItem(Integer.toString(i));
    	}
    	numEnginesBox.addItem(Train.AUTO);
    	addItem (trainReq, textEngine, 1, 1);
    	addItem (trainReq, numEnginesBox, 2, 1);
    	addItem (trainReq, textModel, 3, 1);
    	modelEngineBox.insertItemAt("",0);
    	modelEngineBox.setSelectedIndex(0);
     	addItem (trainReq, modelEngineBox, 4, 1);
    	addItem (trainReq, textRoad2, 5, 1);
    	roadEngineBox.insertItemAt("",0);
    	roadEngineBox.setSelectedIndex(0);
    	addItem (trainReq, roadEngineBox, 6, 1);
    	
    	addItem (trainReq, noneRadioButton, 2, 2);
    	addItem (trainReq, fredRadioButton, 3, 2);
    	addItem (trainReq, cabooseRadioButton, 4, 2);
     	addItem (trainReq, textRoad3, 5, 2);
    	roadBox.insertItemAt("",0);
    	roadBox.setSelectedIndex(0);
    	addItem (trainReq, roadBox, 6, 2);
    	group.add(noneRadioButton);
    	group.add(cabooseRadioButton);
    	group.add(fredRadioButton);
     	noneRadioButton.setSelected(true);
     	trainReq.setBorder(border);

		// row 11
    	JPanel p3 = new JPanel();
    	p3.setLayout(new GridBagLayout());
    	addItem (p3, textCars, 0, 1);
    	addItem (p3, resetButton, 1, 1);
		p3.setBorder(border);
		updateNumberCars();
		
    	JPanel p4 = new JPanel();
    	p4.setLayout(new GridBagLayout());
		
		// row 12
		int y = 12;
		addItem (p4, space1, 0, ++y);
    	
		// row 13
		addItem(p4, textComment, 0, ++y);
		addItemWidth(p4, commentTextField, 3, 1, y);
				
		// row 14
		addItem(p4, space2, 0, ++y);
		// row 15
		addItem(p4, deleteTrainButton, 0, ++y);
		addItem(p4, addTrainButton, 1, y);
		addItem(p4, saveTrainButton, 3, y);
		
		getContentPane().add(p1);
		getContentPane().add(p2);
		getContentPane().add(locationsPane);
		getContentPane().add(typePanelCheckBoxes);
		getContentPane().add(panelRoadNames);
		getContentPane().add(trainReq);
		getContentPane().add(p3);
       	getContentPane().add(p4);
		
		// setup buttons
       	addButtonAction(editButton);
		addButtonAction(setButton);
		addButtonAction(clearButton);
		addButtonAction(deleteRoadButton);
		addButtonAction(addRoadButton);
		addButtonAction(resetButton);
		addButtonAction(deleteTrainButton);
		addButtonAction(addTrainButton);
		addButtonAction(saveTrainButton);
		
		addRadioButtonAction(roadNameAll);
		addRadioButtonAction(roadNameInclude);
		addRadioButtonAction(roadNameExclude);
 		
		if (_train != null){
			trainNameTextField.setText(_train.getName());
			trainDescriptionTextField.setText(_train.getDescription());
			routeBox.setSelectedItem(_train.getRoute());
			numEnginesBox.setSelectedItem(_train.getNumberEngines());
			roadEngineBox.setSelectedItem(_train.getEngineRoad());
			modelEngineBox.setSelectedItem(_train.getEngineModel());
			roadBox.setSelectedItem(_train.getCabooseRoad());
			commentTextField.setText(_train.getComment());
			cabooseRadioButton.setSelected((_train.getRequirements()&_train.CABOOSE)>0);
			fredRadioButton.setSelected((_train.getRequirements()&_train.FRED)>0);
			enableButtons(true);
			// listen for train changes
			_train.addPropertyChangeListener(this);
			// listen for route changes
			Route route = _train.getRoute();
			if (route != null)
				route.addPropertyChangeListener(this);
		} else {
			enableButtons(false);
		}
		
		// setup combobox
		addComboBoxAction(routeBox);

		// build menu
		JMenuBar menuBar = new JMenuBar();
//		JMenu toolMenu = new JMenu("Tools");
//		menuBar.add(toolMenu);
//		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Trains", true);

		//	 get notified if combo box gets modified
		routeManager.addPropertyChangeListener(this);
		// get notified if car types gets modified
		CarTypes.instance().addPropertyChangeListener(this);
		
		// set frame size and train for display
		pack();
		//setLocation(10, 20);
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
		if (ae.getSource() == saveTrainButton){
			log.debug("train save button actived");
			Train train = manager.getTrainByName(trainNameTextField.getText());
			if (_train == null && train == null){
				saveNewTrain();
			} else {
				if (train != null && train != _train){
					reportTrainExists("save");
					return;
				}
				saveTrain();
			}
		}
		if (ae.getSource() == deleteTrainButton){
			log.debug("train delete button actived");
			Train train = manager.getTrainByName(trainNameTextField.getText());
			if (train == null)
				return;
			selectCheckboxes(false);
			routeBox.setSelectedItem("");
			manager.deregister(train);
			_train = null;

			enableButtons(false);
			
			// save train file
			managerXml.writeOperationsTrainFile();
		}
		if (ae.getSource() == addTrainButton){
			Train train = manager.getTrainByName(trainNameTextField.getText());
			if (train != null){
				reportTrainExists("add");
				return;
			}
			saveNewTrain();
		}
		if (ae.getSource() == editButton){
			editAddRoute();
		}
		if (ae.getSource() == setButton){
			selectCheckboxes(true);
		}
		if (ae.getSource() == clearButton){
			selectCheckboxes(false);
		}
		if (ae.getSource() == resetButton){
			if (_train != null)
				_train.reset();
		}
		if (ae.getSource() == addRoadButton){
			_train.addRoadName((String) road2Box.getSelectedItem());
			updateRoadNames();
		}
		if (ae.getSource() == deleteRoadButton){
			_train.deleteRoadName((String) road2Box.getSelectedItem());
			updateRoadNames();
		}
		
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
			_train.setRoadOption(_train.ALLROADS);
		if (ae.getSource() == roadNameInclude)
			_train.setRoadOption(_train.INCLUDEROADS);
		if (ae.getSource() == roadNameExclude)
			_train.setRoadOption(_train.EXCLUDEROADS);
		
		updateRoadNames();
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

		if(_train != null){
			// set radio button
			roadNameAll.setSelected(_train.getRoadOption().equals(_train.ALLROADS));
			roadNameInclude.setSelected(_train.getRoadOption().equals(_train.INCLUDEROADS));
			roadNameExclude.setSelected(_train.getRoadOption().equals(_train.EXCLUDEROADS));
			
			if (!roadNameAll.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	p.add(road2Box);
		    	p.add(addRoadButton);
		    	p.add(deleteRoadButton);
				gc.gridy = y++;
		    	panelRoadNames.add(p, gc);

		    	String[]carRoads = _train.getRoadNames();
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
	
	
	private void saveNewTrain(){
		if (!checkName())
			return;
		Train train = manager.newTrain(trainNameTextField.getText());
		_train = train;
		if (_train != null)
			_train.addPropertyChangeListener(this);
		// setup checkboxes
		selectCheckboxes(true);
		// enable checkboxes
		enableButtons(true);
		saveTrain();
	}
	
	private void saveTrain (){
		if (!checkName())
			return;
		if(numEnginesBox.getSelectedItem().equals(Train.AUTO)){
			JOptionPane.showMessageDialog(this,
					"Auto Engines will estimate the number of engines needed", "Feature still under development!",
					JOptionPane.INFORMATION_MESSAGE);
			//return;
		}
		_train.setNumberEngines((String)numEnginesBox.getSelectedItem());
		_train.setEngineRoad((String)roadEngineBox.getSelectedItem());
		_train.setEngineModel((String)modelEngineBox.getSelectedItem());
		if (cabooseRadioButton.isSelected())
			_train.setRequirements(_train.CABOOSE);
		if (fredRadioButton.isSelected())
			_train.setRequirements(_train.FRED);
		if (noneRadioButton.isSelected())
			_train.setRequirements(_train.NONE);
		_train.setCabooseRoad((String)roadBox.getSelectedItem());
		_train.setName(trainNameTextField.getText());
		_train.setDescription(trainDescriptionTextField.getText());
		_train.setComment(commentTextField.getText());

		// save train file
		managerXml.writeOperationsTrainFile();
	}
	

	/**
	 * 
	 * @return true if name is less than 26 characters
	 */
	private boolean checkName(){
		if (trainNameTextField.getText().length() > 25){
			log.error("Train name must be less than 26 charaters");
			JOptionPane.showMessageDialog(this,
					"Train name must be less than 26 charaters", "Can not add train!",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private void reportTrainExists(String s){
		log.info("Can not " + s + ", train already exists");
		JOptionPane.showMessageDialog(this,
				"Train with this name already exists", "Can not " + s + " train!",
				JOptionPane.ERROR_MESSAGE);
	}
	
	private void enableButtons(boolean enabled){
		routeBox.setEnabled(enabled);
		clearButton.setEnabled(enabled);
		resetButton.setEnabled(enabled);
		setButton.setEnabled(enabled);
		saveTrainButton.setEnabled(enabled);
		deleteTrainButton.setEnabled(enabled);
		enableCheckboxes(enabled);

		// the inverse!
		addTrainButton.setEnabled(!enabled);
	}
	
	private void selectCheckboxes(boolean enable){
		for (int i=0; i < typeCheckBoxes.size(); i++){
			JCheckBox checkBox = (JCheckBox)typeCheckBoxes.get(i);
			checkBox.setSelected(enable);
			if(_train != null){
				if (enable)
					_train.addTypeName(checkBox.getText());
				else
					_train.deleteTypeName(checkBox.getText());
			}
		}
	}
	
	private void addComboBoxAction(JComboBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				comboBoxActionPerformed(e);
			}
		});
	}
	
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (_train == null)
			return;
		if (routeBox.isEnabled()){
			Route route = _train.getRoute();
			if (route != null)
				route.removePropertyChangeListener(this);
			Object selected =  routeBox.getSelectedItem();
			if (selected != null && !selected.equals("")){
				 route = (Route)selected;
				_train.setRoute(route);
				route.addPropertyChangeListener(this);
			}else{
				_train.setRoute(null);
			}
		}
		updateLocationCheckboxes();
	}
	
	private void enableCheckboxes(boolean enable){
		for (int i=0; i < typeCheckBoxes.size(); i++){
			JCheckBox checkBox = (JCheckBox)typeCheckBoxes.get(i);
			checkBox.setEnabled(enable);
		}
	}
		
	private void addLocationCheckBoxAction(JCheckBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				locationCheckBoxActionPerformed(e);
			}
		});
	}
	
	public void locationCheckBoxActionPerformed(java.awt.event.ActionEvent ae) {
		JCheckBox b =  (JCheckBox)ae.getSource();
		log.debug("checkbox change "+ b.getText());
		if (_train == null)
			return;
		String id = b.getName();
		if (b.isSelected())
			_train.deleteTrainSkipsLocation(id);
		else
			_train.addTrainSkipsLocation(id);
	}
	
	private void updateComboBoxes(){
		routeBox.setEnabled(false);
		routeManager.updateComboBox(routeBox);
		if (_train != null){
			routeBox.setSelectedItem(_train.getRoute());
		}
		routeBox.setEnabled(true);
	}
	
	private void updateNumberCars(){
		if (_train != null){
			String msg = java.text.MessageFormat.format(rb.getString("CarMoves"),new Object[]{Integer.toString(_train.getNumberCarsWorked())});
			textCars.setText(msg);
		}
	}
	
	private void updateTypeCheckboxes(){
		typeCheckBoxes.clear();
		typePanelCheckBoxes.removeAll();
		int y = 0;		// vertical position in panel
		addItemWidth(typePanelCheckBoxes, textType, 3, 1, y++);

		String[]carTypes = CarTypes.instance().getNames();
		int x = 0;
		for (int i =0; i<carTypes.length; i++){
			JCheckBox checkBox = new javax.swing.JCheckBox();
			typeCheckBoxes.add(checkBox);
			checkBox.setText(carTypes[i]);
			addTypeCheckBoxAction(checkBox);
			addItemLeft(typePanelCheckBoxes, checkBox, x++, y);
			if(_train != null && _train.acceptsTypeName(carTypes[i]))
				checkBox.setSelected(true);
			if (x > 5){
				y++;
				x = 0;
			}
		}
		enableCheckboxes(_train != null);
		addItem (typePanelCheckBoxes, clearButton, 1, ++y);
		addItem (typePanelCheckBoxes, setButton, 4, y);
		Border border = BorderFactory.createEtchedBorder();
		typePanelCheckBoxes.setBorder(border);
		typePanelCheckBoxes.revalidate();
		pack();
		repaint();
	}
	
	private void addTypeCheckBoxAction(JCheckBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				typeCheckBoxActionPerformed(e);
			}
		});
	}
	
	
	public void typeCheckBoxActionPerformed(java.awt.event.ActionEvent ae) {
		JCheckBox b =  (JCheckBox)ae.getSource();
		log.debug("checkbox change "+ b.getText());
		if (_train == null)
			return;
		if (b.isSelected()){
			_train.addTypeName(b.getText());
		}else{
			_train.deleteTypeName(b.getText());
		}
	}
	
	private void updateLocationCheckboxes(){
		locationCheckBoxes.clear();
		locationPanelCheckBoxes.removeAll();
		int y = 0;		// vertical position in panel
		addItemWidth(locationPanelCheckBoxes, textStops, 3, 0, y++);
		Route route = null;
		if (_train != null)
			route = _train.getRoute();
		if (route !=null){
			List locations = route.getLocationsBySequenceList();
			for (int i =0; i<locations.size(); i++){
				RouteLocation rl = route.getLocationById((String)locations.get(i));
				JCheckBox checkBox = new javax.swing.JCheckBox();
				locationCheckBoxes.add(checkBox);
				checkBox.setSelected(!_train.skipsLocation(rl.getId()));
				checkBox.setText(rl.toString());
				checkBox.setName(rl.getId());
				addLocationCheckBoxAction(checkBox);
				addItemLeft(locationPanelCheckBoxes, checkBox, 0, y++);
			}
		}
		Border border = BorderFactory.createEtchedBorder();
		locationPanelCheckBoxes.setBorder(border);
		locationPanelCheckBoxes.revalidate();
		pack();
		repaint();
	}
	
    private void editAddRoute (){
    	log.debug("Edit/add route");
    	RouteEditFrame lef = new RouteEditFrame();
    	Object selected =  routeBox.getSelectedItem();
		if (selected != null && !selected.equals("")){
			Route route = (Route)selected;
			lef.setTitle("Edit Route");
			lef.initComponents(route);
		} else {
			lef.setTitle("Add Route");
			lef.initComponents(null);
		}
    }
	
	public void dispose() {
		CarTypes.instance().removePropertyChangeListener(this);
		routeManager.removePropertyChangeListener(this);
		if (_train != null){
			_train.removePropertyChangeListener(this);
			Route route = _train.getRoute();
			if (route != null)
				route.removePropertyChangeListener(this);
		}
		super.dispose();
	}

 	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(CarTypes.CARTYPES)){
			updateTypeCheckboxes();
		}
		if (e.getPropertyName().equals(routeManager.LISTLENGTH)){
			updateComboBoxes();
		}
		if (e.getPropertyName().equals(Route.LISTCHANGE) || e.getPropertyName().equals(Location.NAME)){
			updateLocationCheckboxes();
		}
		if (e.getPropertyName().equals(Train.NUMBERCARS)){
			updateNumberCars();
		}
	}
 	
	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(TrainEditFrame.class.getName());
}
