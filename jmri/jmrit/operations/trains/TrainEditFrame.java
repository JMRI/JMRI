// TrainsEditFrame.java

package jmri.jmrit.operations.trains;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.EngineModels;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteEditFrame;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;


/**
 * Frame for user edit of a train
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.33 $
 */

public class TrainEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	static final ResourceBundle rbr = ResourceBundle.getBundle("jmri.jmrit.operations.routes.JmritOperationsRoutesBundle");
	
	TrainManager manager;
	TrainManagerXml managerXml;
	RouteManager routeManager;

	Train _train = null;
	List<JCheckBox> typeCarCheckBoxes = new ArrayList<JCheckBox>();
	List<JCheckBox> typeEngineCheckBoxes = new ArrayList<JCheckBox>();
	List<JCheckBox> locationCheckBoxes = new ArrayList<JCheckBox>();
	JPanel typeCarPanelCheckBoxes = new JPanel();
	JPanel typeEnginePanelCheckBoxes = new JPanel();
	JPanel panelRoadNames = new JPanel();
	JPanel locationPanelCheckBoxes = new JPanel();
	JScrollPane typeCarPane;
	JScrollPane typeEnginePane;
	JScrollPane locationsPane;

	// labels
	JLabel textName = new JLabel();
	JLabel textDescription = new JLabel();
	JLabel textDepartTime = new JLabel();
	JLabel textRoute = new JLabel();
	JLabel textCarType = new JLabel();
	JLabel textEngineType = new JLabel();
	JLabel textModel = new JLabel();
	JLabel textRoad = new JLabel();
	JLabel textRoad2 = new JLabel();
	JLabel textRoad3 = new JLabel();
	JLabel textEngine = new JLabel();
	JLabel textStops = new JLabel();
	JLabel textTrainRequires = new JLabel();
	JLabel textCars = new JLabel();
	JLabel textComment = new JLabel();

	// major buttons
	JButton editButton = new JButton();
	JButton clearButton = new JButton();
	JButton setButton = new JButton();
	JButton addRoadButton = new JButton();
	JButton deleteRoadButton = new JButton();
	JButton JLabel = new JButton();
	JButton resetButton = new JButton();
	JButton saveTrainButton = new JButton();
	JButton deleteTrainButton = new JButton();
	JButton addTrainButton = new JButton();

	// check boxes

	// radio buttons
    JRadioButton noneRadioButton = new JRadioButton(rb.getString("None"));
    JRadioButton cabooseRadioButton = new JRadioButton(rb.getString("Caboose"));
    JRadioButton fredRadioButton = new JRadioButton(rb.getString("FRED"));
    ButtonGroup group = new ButtonGroup();
    
    JRadioButton roadNameAll = new JRadioButton("Accept all");
    JRadioButton roadNameInclude = new JRadioButton("Accept only");
    JRadioButton roadNameExclude = new JRadioButton("Exclude");
    ButtonGroup roadGroup = new ButtonGroup();
	
	// text field
	JTextField trainNameTextField = new JTextField(18);
	JTextField trainDescriptionTextField = new JTextField(30);
	JTextField commentTextField = new JTextField(35);

	// for padding out panel
	JLabel space0 = new JLabel("     ");
	JLabel space1 = new JLabel("     ");
	JLabel space2 = new JLabel("     ");
	JLabel space3 = new JLabel("     ");
	JLabel space4 = new JLabel("     ");
	JLabel space5 = new JLabel("     ");
	
	// combo boxes
	JComboBox hourBox = new JComboBox();
	JComboBox minuteBox = new JComboBox();
	JComboBox routeBox = RouteManager.instance().getComboBox();
	JComboBox roadCabooseBox = new JComboBox();
	JComboBox roadBox = CarRoads.instance().getComboBox();
	JComboBox roadEngineBox = CarRoads.instance().getComboBox();
	JComboBox modelEngineBox = EngineModels.instance().getComboBox();
	JComboBox numEnginesBox = new JComboBox();

	public static final String DISPOSE = "dispose" ;

	public TrainEditFrame() {
		super();
    	// Set up the jtable in a Scroll Pane..
    	locationsPane = new JScrollPane(locationPanelCheckBoxes);
    	locationsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
       	typeCarPane = new JScrollPane(typeCarPanelCheckBoxes);
    	typeCarPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
       	typeEnginePane = new JScrollPane(typeEnginePanelCheckBoxes);
    	typeEnginePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	}

	public void initComponents(Train train) {
		_train = train;

		// load managers
		manager = TrainManager.instance();
		managerXml = TrainManagerXml.instance();
		routeManager = RouteManager.instance();
		
		textName.setText(rb.getString("Name"));
		textName.setVisible(true);
		textDescription.setText(rb.getString("Description"));
		textDescription.setVisible(true);
		textDepartTime.setText(rb.getString("DepartTime"));
		textDepartTime.setVisible(true);
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
		textComment.setText(rb.getString("Comment"));
		textComment.setVisible(true);
		textCarType.setText(rb.getString("TypesCar"));
		textCarType.setVisible(true);
		textEngineType.setText(rb.getString("TypesEngine"));
		textEngineType.setVisible(true);
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
    	p1.setPreferredSize(new Dimension(550, 50)); // this sets the minimum panel width
				
		// Layout the panel by rows
		// row 1
		addItem(p1, textName, 0, 1);
		addItemWidth(p1, trainNameTextField, 3, 1, 1);

		// row 2
		addItem(p1, textDescription, 0, 3);
		addItemWidth(p1, trainDescriptionTextField, 3, 1, 3);
		Border border = BorderFactory.createEtchedBorder();
		p1.setBorder(border);
		
		// row 3
	   	JPanel pdt = new JPanel();
	   	pdt.setLayout(new GridBagLayout());
		// build hour and minute menus
		for (int i=0; i<24; i++){
			if (i<10)
				hourBox.addItem("0"+Integer.toString(i));
			else
				hourBox.addItem(Integer.toString(i));
		}
		hourBox.setMinimumSize(new Dimension(100,25));
		
		for (int i=0; i<60; i+=5){
			if (i<10)
				minuteBox.addItem("0"+Integer.toString(i));
			else
				minuteBox.addItem(Integer.toString(i));
		}
		addItem(pdt, textDepartTime, 0, 5);
		addItem(pdt, hourBox, 1, 5);
		addItemLeft(pdt, minuteBox, 2, 5);
		pdt.setBorder(border);

		// row 4
		// BUG! routeBox needs its own panel when resizing frame!
	   	JPanel p2 = new JPanel();
    	p2.setLayout(new GridBagLayout());
		addItem(p2, textRoute, 0, 5);
		addItem(p2, routeBox, 1, 5);
		addItem(p2, editButton, 2, 5);
		p2.setBorder(border);

		// row 5
	   	locationPanelCheckBoxes.setLayout(new GridBagLayout());

		// row 6
	   	typeCarPanelCheckBoxes.setLayout(new GridBagLayout());
		
		// row 7
		panelRoadNames.setLayout(new GridBagLayout());
		roadGroup.add(roadNameAll);
		roadGroup.add(roadNameInclude);
		roadGroup.add(roadNameExclude);
		
		// row 8
		typeEnginePanelCheckBoxes.setLayout(new GridBagLayout());
		
		// row 9
		JPanel trainReq = new JPanel();
    	trainReq.setLayout(new GridBagLayout());
    	addItem (trainReq, textTrainRequires, 0, 1);
    	for (int i=0; i<Setup.getEngineSize()+1; i++){
    		numEnginesBox.addItem(Integer.toString(i));
    	}
    	numEnginesBox.addItem(Train.AUTO);
    	numEnginesBox.setMinimumSize(new Dimension(100,25));
    	addItem (trainReq, textEngine, 1, 1);
    	addItem (trainReq, numEnginesBox, 2, 1);
    	addItem (trainReq, textModel, 3, 1);
    	modelEngineBox.insertItemAt("",0);
    	modelEngineBox.setSelectedIndex(0);
    	modelEngineBox.setToolTipText(rb.getString("ModelEngineTip"));
     	addItem (trainReq, modelEngineBox, 4, 1);
    	addItem (trainReq, textRoad2, 5, 1);
    	roadEngineBox.insertItemAt("",0);
    	roadEngineBox.setSelectedIndex(0);
    	roadEngineBox.setToolTipText(rb.getString("RoadEngineTip"));
    	addItem (trainReq, roadEngineBox, 6, 1);
    	
    	addItem (trainReq, noneRadioButton, 2, 2);
    	addItem (trainReq, fredRadioButton, 3, 2);
    	addItem (trainReq, cabooseRadioButton, 4, 2);
     	addItem (trainReq, textRoad3, 5, 2);
    	roadCabooseBox.setToolTipText(rb.getString("RoadCabooseTip"));
    	addItem (trainReq, roadCabooseBox, 6, 2);
    	group.add(noneRadioButton);
    	group.add(cabooseRadioButton);
    	group.add(fredRadioButton);
     	noneRadioButton.setSelected(true);
     	trainReq.setBorder(border);

		// row 11
    	//JPanel p3 = new JPanel();
    	//p3.setLayout(new GridBagLayout());
    	//addItem (p3, textCars, 0, 1);
    	
		//p3.setBorder(border);
		
    	JPanel p4 = new JPanel();
    	p4.setLayout(new GridBagLayout());
		
		// row 12
		int y = 12;
		//addItem (p4, space1, 0, ++y);
    	
		// row 13
		addItem(p4, textComment, 0, ++y);
		addItemWidth(p4, commentTextField, 3, 1, y);
				
		// row 14
		addItem(p4, space2, 0, ++y);
		// row 15
		addItem(p4, deleteTrainButton, 0, ++y);
		addItem (p4, resetButton, 1, y);
		addItem(p4, addTrainButton, 2, y);
		addItem(p4, saveTrainButton, 3, y);
		
		getContentPane().add(p1);
		getContentPane().add(pdt);
		getContentPane().add(p2);
		getContentPane().add(locationsPane);
		getContentPane().add(typeCarPane);
		getContentPane().add(panelRoadNames);
		getContentPane().add(typeEnginePane);
		getContentPane().add(trainReq);
		//getContentPane().add(p3);
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
		addRadioButtonAction(noneRadioButton);
		addRadioButtonAction(cabooseRadioButton);
		addRadioButtonAction(fredRadioButton);
		
		if (_train != null){
			trainNameTextField.setText(_train.getName());
			trainDescriptionTextField.setText(_train.getDescription());
			hourBox.setSelectedItem(_train.getDepartureTimeHour());
			minuteBox.setSelectedItem(_train.getDepartureTimeMinute());
			routeBox.setSelectedItem(_train.getRoute());
			numEnginesBox.setSelectedItem(_train.getNumberEngines());
			modelEngineBox.setSelectedItem(_train.getEngineModel());
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
		
		modelEngineBox.setEnabled(!numEnginesBox.getSelectedItem().equals("0"));
		roadEngineBox.setEnabled(!numEnginesBox.getSelectedItem().equals("0"));

		// build menu
//		JMenuBar menuBar = new JMenuBar();
//		JMenu toolMenu = new JMenu("Tools");
//		menuBar.add(toolMenu);
//		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Trains", true);

		// load route location checkboxes
		updateLocationCheckboxes();
		updateCarTypeCheckboxes();
		updateEngineTypeCheckboxes();
		updateRoadNames();
		updateNumberCars();
		updateCabooseRoadComboBox();
		updateEngineRoadComboBox();
		
		// set frame size and train for display
		packFrame();
		
		// setup combobox
		addComboBoxAction(numEnginesBox);
		addComboBoxAction(routeBox);
		addComboBoxAction(modelEngineBox);
		
		//	 get notified if combo box gets modified
		routeManager.addPropertyChangeListener(this);
		// get notified if car types or roads gets modified
		CarTypes.instance().addPropertyChangeListener(this);
		CarRoads.instance().addPropertyChangeListener(this);
		EngineTypes.instance().addPropertyChangeListener(this);
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
					reportTrainExists(rb.getString("save"));
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
			if (JOptionPane.showConfirmDialog(this,
					MessageFormat.format(rb.getString("deleteMsg"),new Object[]{train.getName()}),
					rb.getString("deleteTrain"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				return;
			}	
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
				reportTrainExists(rb.getString("add"));
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
			if (_train != null){
				_train.addRoadName((String) roadBox.getSelectedItem());
				updateRoadNames();
			}
		}
		if (ae.getSource() == deleteRoadButton){
			if (_train != null){
				_train.deleteRoadName((String) roadBox.getSelectedItem());
				updateRoadNames();
			}
		}
	}
	
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button activated");
		if (_train != null){
			if (ae.getSource() == roadNameAll){
				_train.setRoadOption(Train.ALLROADS);
				updateRoadNames();
			}
			if (ae.getSource() == roadNameInclude){
				_train.setRoadOption(Train.INCLUDEROADS);
				updateRoadNames();
			}
			if (ae.getSource() == roadNameExclude){
				_train.setRoadOption(Train.EXCLUDEROADS);
				updateRoadNames();
			}
			if (ae.getSource() == noneRadioButton 
					|| ae.getSource() == cabooseRadioButton 
					|| ae.getSource() == fredRadioButton){
				updateCabooseRoadComboBox();
			}
		}
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
			roadNameAll.setSelected(_train.getRoadOption().equals(Train.ALLROADS));
			roadNameInclude.setSelected(_train.getRoadOption().equals(Train.INCLUDEROADS));
			roadNameExclude.setSelected(_train.getRoadOption().equals(Train.EXCLUDEROADS));
			
			if (!roadNameAll.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	p.add(roadBox);
		    	p.add(addRoadButton);
		    	p.add(deleteRoadButton);
				gc.gridy = y++;
		    	panelRoadNames.add(p, gc);

		    	String[]carRoads = _train.getRoadNames();
		    	int x = 0;
		    	for (int i =0; i<carRoads.length; i++){
		    		JLabel road = new JLabel();
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
		packFrame();
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
		if(numEnginesBox.getSelectedItem().equals(Train.AUTO) && !_train.getNumberEngines().equals(Train.AUTO)){
			JOptionPane.showMessageDialog(this,
					rb.getString("AutoEngines"), "Feature still under development!",
					JOptionPane.INFORMATION_MESSAGE);
			//return;
		}
		_train.setDepartureTime((String)hourBox.getSelectedItem(), (String)minuteBox.getSelectedItem());
		_train.setNumberEngines((String)numEnginesBox.getSelectedItem());
		_train.setEngineRoad((String)roadEngineBox.getSelectedItem());
		_train.setEngineModel((String)modelEngineBox.getSelectedItem());
		if (cabooseRadioButton.isSelected())
			_train.setRequirements(Train.CABOOSE);
		if (fredRadioButton.isSelected())
			_train.setRequirements(Train.FRED);
		if (noneRadioButton.isSelected())
			_train.setRequirements(Train.NONE);
		_train.setCabooseRoad((String)roadCabooseBox.getSelectedItem());
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
		if (trainNameTextField.getText().trim().equals(""))
				return false;
		if (trainNameTextField.getText().length() > 25){
			log.error("Train name must be less than 26 charaters");
			JOptionPane.showMessageDialog(this,
					rb.getString("TrainNameLess26"), rb.getString("CanNotAdd"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private void reportTrainExists(String s){
		log.info("Can not " + s + ", train already exists");
		JOptionPane.showMessageDialog(this,
				rb.getString("TrainNameExists"), MessageFormat.format(rb.getString("CanNot"),
						new Object[] {s}),
				JOptionPane.ERROR_MESSAGE);
	}
	
	private void enableButtons(boolean enabled){
		editButton.setEnabled(enabled);
		routeBox.setEnabled(enabled);
		clearButton.setEnabled(enabled);
		resetButton.setEnabled(enabled);
		setButton.setEnabled(enabled);
		roadNameAll.setEnabled(enabled);
		roadNameInclude.setEnabled(enabled);
		roadNameExclude.setEnabled(enabled);
		addRoadButton.setEnabled(enabled);
		deleteRoadButton.setEnabled(enabled);
		saveTrainButton.setEnabled(enabled);
		deleteTrainButton.setEnabled(enabled);
		numEnginesBox.setEnabled(enabled);
		enableCheckboxes(enabled);

		// the inverse!
		addTrainButton.setEnabled(!enabled);
	}
	
	private void selectCheckboxes(boolean enable){
		for (int i=0; i < typeCarCheckBoxes.size(); i++){
			JCheckBox checkBox = typeCarCheckBoxes.get(i);
			checkBox.setSelected(enable);
			if(_train != null){
				if (enable)
					_train.addTypeName(checkBox.getText());
				else
					_train.deleteTypeName(checkBox.getText());
			}
		}
	}
	
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (_train == null)
			return;
		if (ae.getSource() == numEnginesBox){
			modelEngineBox.setEnabled(!numEnginesBox.getSelectedItem().equals("0"));
			roadEngineBox.setEnabled(!numEnginesBox.getSelectedItem().equals("0"));
		}
		if (ae.getSource() == modelEngineBox){
			updateEngineRoadComboBox();
		}
		if (ae.getSource() == routeBox){
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
	}
	
	private void enableCheckboxes(boolean enable){
		for (int i=0; i < typeCarCheckBoxes.size(); i++){
			JCheckBox checkBox = typeCarCheckBoxes.get(i);
			checkBox.setEnabled(enable);
		}
		for (int i=0; i < typeEngineCheckBoxes.size(); i++){
			JCheckBox checkBox = typeEngineCheckBoxes.get(i);
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
			textCars.setText(MessageFormat.format(rb.getString("CarMoves"),
					new Object[] {Integer.toString(_train.getNumberCarsWorked())}));
		}
	}
	
	private void updateCarTypeCheckboxes(){
		typeCarCheckBoxes.clear();
		typeCarPanelCheckBoxes.removeAll();
		addItemWidth(typeCarPanelCheckBoxes, textCarType, 3, 1, 0);
		loadCarTypes(CarTypes.instance().getNames());
		enableCheckboxes(_train != null);
		Border border = BorderFactory.createEtchedBorder();
		typeCarPanelCheckBoxes.setBorder(border);
		typeCarPanelCheckBoxes.revalidate();
		repaint();
	}
	
	private void loadCarTypes(String[] types){
		int x = 0;
		int y = 1;	// vertical position in panel
		for (int i =0; i<types.length; i++){
			JCheckBox checkBox = new javax.swing.JCheckBox();
			typeCarCheckBoxes.add(checkBox);
			checkBox.setText(types[i]);
			addTypeCheckBoxAction(checkBox);
			addItemLeft(typeCarPanelCheckBoxes, checkBox, x++, y);
			if(_train != null && _train.acceptsTypeName(types[i]))
				checkBox.setSelected(true);
			if (x > 5){
				y++;
				x = 0;
			}
		}
		addItem (typeCarPanelCheckBoxes, clearButton, 1, ++y);
		addItem (typeCarPanelCheckBoxes, setButton, 4, y);
	}
	
	private void updateEngineTypeCheckboxes(){
		typeEngineCheckBoxes.clear();
		typeEnginePanelCheckBoxes.removeAll();
		addItemWidth(typeEnginePanelCheckBoxes, textEngineType, 3, 1, 0);
		loadEngineTypes(EngineTypes.instance().getNames());
		enableCheckboxes(_train != null);
		Border border = BorderFactory.createEtchedBorder();
		typeEnginePanelCheckBoxes.setBorder(border);
		typeEnginePanelCheckBoxes.revalidate();
		repaint();
	}
	
	private void loadEngineTypes(String[] types){
		int x = 0;
		int y = 1;
		for (int i =0; i<types.length; i++){
			JCheckBox checkBox = new javax.swing.JCheckBox();
			typeEngineCheckBoxes.add(checkBox);
			checkBox.setText(types[i]);
			addTypeCheckBoxAction(checkBox);
			addItemLeft(typeEnginePanelCheckBoxes, checkBox, x++, y);
			if(_train != null && _train.acceptsTypeName(types[i]))
				checkBox.setSelected(true);
			if (x > 5){
				y++;
				x = 0;
			}
		}
	}
	
	// there are three road combo boxes to update
	private void updateRoadComboBoxes(){
		updateCabooseRoadComboBox();
		updateEngineRoadComboBox();
		CarRoads.instance().updateComboBox(roadBox);
	}
	
	// update caboose road box based on radio selection
	private void updateCabooseRoadComboBox(){
		roadCabooseBox.removeAllItems();
		roadCabooseBox.addItem("");
		if (noneRadioButton.isSelected()){
			roadCabooseBox.setEnabled(false);
			return;
		}
		roadCabooseBox.setEnabled(true);
		List<String> roads;
		if (cabooseRadioButton.isSelected())
			roads = CarManager.instance().getCabooseRoadNames();
		else
			roads = CarManager.instance().getFredRoadNames();
		for (int i=0; i<roads.size(); i++){
	   		roadCabooseBox.addItem(roads.get(i));
		}
		if (_train != null){
			roadCabooseBox.setSelectedItem(_train.getCabooseRoad());
		}
	}
	
	private void updateEngineRoadComboBox(){
		roadEngineBox.removeAllItems();
		roadEngineBox.addItem("");
		List<String> roads = EngineManager.instance().getEngineRoadNames((String)modelEngineBox.getSelectedItem());
		for (int i=0; i<roads.size(); i++){
	   		roadEngineBox.addItem(roads.get(i));
		}
		if (_train != null){
			roadEngineBox.setSelectedItem(_train.getEngineRoad());
		}
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
			List<String> locations = route.getLocationsBySequenceList();
			for (int i =0; i<locations.size(); i++){
				RouteLocation rl = route.getLocationById(locations.get(i));
				JCheckBox checkBox = new javax.swing.JCheckBox();
				locationCheckBoxes.add(checkBox);
				checkBox.setText(rl.toString());
				checkBox.setName(rl.getId());
				// check can drop and pickup, and moves > 0
				if ((rl.canDrop() || rl.canPickup()) && rl.getMaxCarMoves()>0)
					checkBox.setSelected(!_train.skipsLocation(rl.getId()));
				else
					checkBox.setEnabled(false);
				addLocationCheckBoxAction(checkBox);
				addItemLeft(locationPanelCheckBoxes, checkBox, 0, y++);
			}
		}
		Border border = BorderFactory.createEtchedBorder();
		locationPanelCheckBoxes.setBorder(border);
		locationPanelCheckBoxes.revalidate();
		packFrame();
	}
	
    private void editAddRoute (){
    	log.debug("Edit/add route");
    	RouteEditFrame lef = new RouteEditFrame();
    	Object selected =  routeBox.getSelectedItem();
		if (selected != null && !selected.equals("")){
			Route route = (Route)selected;
			lef.setTitle(rbr.getString("TitleRouteEdit"));
			lef.initComponents(route);
		} else {
			lef.setTitle(rbr.getString("TitleRouteAdd"));
			lef.initComponents(null);
		}
    }
    
    private void packFrame(){
 		pack();
		if (getHeight() < 700)
			setSize(getWidth(), getHeight()+ 50);
		setVisible(true);
    }
	
	public void dispose() {
		EngineTypes.instance().removePropertyChangeListener(this);
		CarTypes.instance().removePropertyChangeListener(this);
		CarRoads.instance().removePropertyChangeListener(this);
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
		if (e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY)){
			updateCarTypeCheckboxes();
		}
		if (e.getPropertyName().equals(EngineTypes.ENGINETYPES_LENGTH_CHANGED_PROPERTY)){
			updateEngineTypeCheckboxes();
		}
		if (e.getPropertyName().equals(routeManager.LISTLENGTH_CHANGED_PROPERTY)){
			updateComboBoxes();
		}
		if (e.getPropertyName().equals(Route.LISTCHANGE_CHANGED_PROPERTY) || e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY)){
			updateLocationCheckboxes();
		}
		if (e.getPropertyName().equals(Train.NUMBERCARS_CHANGED_PROPERTY) || e.getPropertyName().equals(Train.STATUS_CHANGED_PROPERTY)){
			updateNumberCars();
		}
		if (e.getPropertyName().equals(CarRoads.CARROADS_LENGTH_CHANGED_PROPERTY)){
			updateRoadComboBoxes();
			updateRoadNames();
		}
	}
 	
	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(TrainEditFrame.class.getName());
}
