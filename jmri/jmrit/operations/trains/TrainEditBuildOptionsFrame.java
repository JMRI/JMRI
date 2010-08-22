// TrainEditBuildOptionsFrame.java

package jmri.jmrit.operations.trains;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ResourceBundle;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTextField;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.setup.Control;


/**
 * Frame for user edit of a train's build options
 * 
 * @author Dan Boudreau Copyright (C) 2010
 * @version $Revision: 1.5 $
 */

public class TrainEditBuildOptionsFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	static final ResourceBundle rbr = ResourceBundle.getBundle("jmri.jmrit.operations.routes.JmritOperationsRoutesBundle");
	
	TrainManager manager;
	TrainManagerXml managerXml;

	Train _train = null;
	TrainEditFrame _trainEditFrame;

	JPanel panelRoadNames = new JPanel();
	JPanel panelLoadNames = new JPanel();
	JPanel panelOwnerNames = new JPanel();
	JPanel panelBuilt = new JPanel();
	JScrollPane roadPane;
	JScrollPane loadPane;
	JScrollPane ownerPane;
	JScrollPane builtPane;

	// labels
	JLabel trainName = new JLabel();
	JLabel trainDescription = new JLabel();
	JLabel before = new JLabel(rb.getString("Before"));
	JLabel after = new JLabel(rb.getString("After"));

	// major buttons
	JButton addRoadButton = new JButton(rb.getString("AddRoad"));
	JButton deleteRoadButton = new JButton(rb.getString("DeleteRoad"));
	
	JButton addLoadButton = new JButton(rb.getString("AddLoad"));
	JButton deleteLoadButton = new JButton(rb.getString("DeleteLoad"));
	JButton deleteAllLoadsButton = new JButton(rb.getString("DeleteAllLoads"));
	
	JButton addOwnerButton = new JButton(rb.getString("AddOwner"));
	JButton deleteOwnerButton = new JButton(rb.getString("DeleteOwner"));
	
	JButton saveTrainButton = new JButton(rb.getString("SaveTrain"));

	// radio buttons    
    JRadioButton roadNameAll = new JRadioButton(rb.getString("AcceptAll"));
    JRadioButton roadNameInclude = new JRadioButton(rb.getString("AcceptOnly"));
    JRadioButton roadNameExclude = new JRadioButton(rb.getString("Exclude"));
    JRadioButton loadNameAll = new JRadioButton(rb.getString("AcceptAll"));
    JRadioButton loadNameInclude = new JRadioButton(rb.getString("AcceptOnly"));
    JRadioButton loadNameExclude = new JRadioButton(rb.getString("Exclude"));
    JRadioButton ownerNameAll = new JRadioButton(rb.getString("AcceptAll"));
    JRadioButton ownerNameInclude = new JRadioButton(rb.getString("AcceptOnly"));
    JRadioButton ownerNameExclude = new JRadioButton(rb.getString("Exclude"));
    JRadioButton builtDateAll = new JRadioButton(rb.getString("AcceptAll"));
    JRadioButton builtDateAfter = new JRadioButton(rb.getString("After"));
    JRadioButton builtDateBefore = new JRadioButton(rb.getString("Before"));
    JRadioButton builtDateRange = new JRadioButton(rb.getString("Range"));

    ButtonGroup roadGroup = new ButtonGroup();
    ButtonGroup loadGroup = new ButtonGroup();
    ButtonGroup ownerGroup = new ButtonGroup();
    ButtonGroup builtGroup = new ButtonGroup();
	
	// text field
    JTextField builtAfterTextField = new JTextField(10);
    JTextField builtBeforeTextField = new JTextField(10);
		
	// for padding out panel
	JLabel space1 = new JLabel("       ");
	JLabel space2 = new JLabel("       ");
	JLabel space3 = new JLabel("       ");
	JLabel space4 = new JLabel("       ");
	JLabel space5 = new JLabel("       ");
	JLabel space6 = new JLabel("       ");
	
	// combo boxes
	JComboBox roadBox = CarRoads.instance().getComboBox();
	JComboBox typeBox = CarTypes.instance().getComboBox();
	JComboBox loadBox = CarLoads.instance().getComboBox(null);
	JComboBox ownerBox = CarOwners.instance().getComboBox();

	public static final String DISPOSE = "dispose" ;

	public TrainEditBuildOptionsFrame() {
		super();
    	// Set up the jtable in a Scroll Pane..
      	roadPane = new JScrollPane(panelRoadNames);
    	roadPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	roadPane.setBorder(BorderFactory.createTitledBorder(rb.getString("RoadsTrain")));
    	
      	loadPane = new JScrollPane(panelLoadNames);
      	loadPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      	loadPane.setBorder(BorderFactory.createTitledBorder(rb.getString("LoadsTrain")));
      	
      	ownerPane = new JScrollPane(panelOwnerNames);
      	ownerPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      	ownerPane.setBorder(BorderFactory.createTitledBorder(rb.getString("OwnersTrain")));
      	
      	builtPane = new JScrollPane(panelBuilt);
      	builtPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      	builtPane.setBorder(BorderFactory.createTitledBorder(rb.getString("BuiltTrain")));
 	}

	public void initComponents(Frame parent) {

		_trainEditFrame = (TrainEditFrame)parent;
		_trainEditFrame.setChildFrame(this);
		_train = _trainEditFrame._train;

		// load managers
		manager = TrainManager.instance();
		managerXml = TrainManagerXml.instance();
	
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
				
		// Layout the panel by rows
	   	JPanel p1 = new JPanel();
    	p1.setLayout(new BoxLayout(p1,BoxLayout.X_AXIS));
				
		// Layout the panel by rows
		// row 1a
       	JPanel pName = new JPanel();
    	pName.setLayout(new GridBagLayout());
    	pName.setBorder(BorderFactory.createTitledBorder(rb.getString("Name")));
    	addItem(pName, trainName, 0, 0);

		// row 1b
       	JPanel pDesc = new JPanel();
    	pDesc.setLayout(new GridBagLayout());
    	pDesc.setBorder(BorderFactory.createTitledBorder(rb.getString("Description")));
    	addItem(pDesc, trainDescription, 0, 0);
		
    	p1.add(pName);
    	p1.add(pDesc);
    		
		// row 7
		panelRoadNames.setLayout(new GridBagLayout());
		roadGroup.add(roadNameAll);
		roadGroup.add(roadNameInclude);
		roadGroup.add(roadNameExclude);
		
		// row 8
		panelLoadNames.setLayout(new GridBagLayout());
		loadGroup.add(loadNameAll);
		loadGroup.add(loadNameInclude);
		loadGroup.add(loadNameExclude);
		
		// row 9
		panelOwnerNames.setLayout(new GridBagLayout());
		ownerGroup.add(ownerNameAll);
		ownerGroup.add(ownerNameInclude);
		ownerGroup.add(ownerNameExclude);
		
		// row 11
		panelBuilt.setLayout(new GridBagLayout());
		builtAfterTextField.setToolTipText(rb.getString("EnterYearTip"));
		builtBeforeTextField.setToolTipText(rb.getString("EnterYearTip"));
		addItem(panelBuilt, builtDateAll, 0, 0);
		addItem(panelBuilt, builtDateAfter, 1, 0);
		addItem(panelBuilt, builtDateBefore, 2, 0);
		addItem(panelBuilt, builtDateRange, 3, 0);
		addItem(panelBuilt, after, 1, 1);
		addItem(panelBuilt, builtAfterTextField, 2, 1);
		addItem(panelBuilt, before, 1, 2);
		addItem(panelBuilt, builtBeforeTextField, 2, 2);
		builtGroup.add(builtDateAll);
		builtGroup.add(builtDateAfter);
		builtGroup.add(builtDateBefore);
		builtGroup.add(builtDateRange);
		
		// row 15 buttons
	   	JPanel pB = new JPanel();
    	pB.setLayout(new GridBagLayout());		
		addItem(pB, saveTrainButton, 3, 0);
		
		getContentPane().add(p1);
		getContentPane().add(roadPane);
		getContentPane().add(loadPane);
		getContentPane().add(ownerPane);
		getContentPane().add(builtPane);
      	getContentPane().add(pB);
		
		// setup buttons
		//addButtonAction(setButton);
		//addButtonAction(clearButton);
		addButtonAction(deleteRoadButton);
		addButtonAction(addRoadButton);
		addButtonAction(deleteLoadButton);
		addButtonAction(deleteAllLoadsButton);
		addButtonAction(addLoadButton);
		addButtonAction(deleteOwnerButton);
		addButtonAction(addOwnerButton);
		addButtonAction(saveTrainButton);
		
		addRadioButtonAction(roadNameAll);
		addRadioButtonAction(roadNameInclude);
		addRadioButtonAction(roadNameExclude);
		addRadioButtonAction(loadNameAll);
		addRadioButtonAction(loadNameInclude);
		addRadioButtonAction(loadNameExclude);
		addRadioButtonAction(ownerNameAll);
		addRadioButtonAction(ownerNameInclude);
		addRadioButtonAction(ownerNameExclude);
		addRadioButtonAction(builtDateAll);		
		addRadioButtonAction(builtDateAfter);
		addRadioButtonAction(builtDateBefore);
		addRadioButtonAction(builtDateRange);
		
		addComboBoxAction(typeBox);
		
		if (_train != null){
			trainName.setText(_train.getName());
			trainDescription.setText(_train.getDescription());
			builtAfterTextField.setText(_train.getBuiltStartYear());
			builtBeforeTextField.setText(_train.getBuiltEndYear());
			setBuiltRadioButton();
			enableButtons(true);
			// listen for train changes
			_train.addPropertyChangeListener(this);
		} else {
			enableButtons(false);
		}
		addHelpMenu("package.jmri.jmrit.operations.Operations_Trains", true);
		updateRoadNames();
		updateLoadComboBoxes();
		updateLoadNames();
		updateOwnerNames();
		updateBuilt();

		// get notified if car roads, loads, and owners gets modified
		CarTypes.instance().addPropertyChangeListener(this);
		CarRoads.instance().addPropertyChangeListener(this);
		CarLoads.instance().addPropertyChangeListener(this);
		CarOwners.instance().addPropertyChangeListener(this);
		setVisible(true);
	}
	
	// Save
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (_train != null){
			if (ae.getSource() == saveTrainButton){
				log.debug("train save button actived");
				saveTrain();
			}
			if (ae.getSource() == addRoadButton){
				if(_train.addRoadName((String) roadBox.getSelectedItem()))
					updateRoadNames();
				selectNextItemComboBox(roadBox);
			}
			if (ae.getSource() == deleteRoadButton){
				if(_train.deleteRoadName((String) roadBox.getSelectedItem()))
					updateRoadNames();
				selectNextItemComboBox(roadBox);
			}
			if (ae.getSource() == addLoadButton){
				if(_train.addLoadName((String) loadBox.getSelectedItem()))
					updateLoadNames();
				selectNextItemComboBox(loadBox);
			}
			if (ae.getSource() == deleteLoadButton){
				if(_train.deleteLoadName((String) loadBox.getSelectedItem()))
					updateLoadNames();
				selectNextItemComboBox(loadBox);
			}
			if (ae.getSource() == deleteAllLoadsButton){
				deleteAllRoads();
			}
			if (ae.getSource() == addOwnerButton){
				if(_train.addOwnerName((String) ownerBox.getSelectedItem()))
					updateOwnerNames();
				selectNextItemComboBox(ownerBox);
			}
			if (ae.getSource() == deleteOwnerButton){
				if(_train.deleteOwnerName((String) ownerBox.getSelectedItem()))
					updateOwnerNames();
				selectNextItemComboBox(ownerBox);
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
			if (ae.getSource() == loadNameAll){
				_train.setLoadOption(Train.ALLLOADS);
				updateLoadNames();
			}
			if (ae.getSource() == loadNameInclude){
				_train.setLoadOption(Train.INCLUDELOADS);
				updateLoadNames();
			}
			if (ae.getSource() == loadNameExclude){
				_train.setLoadOption(Train.EXCLUDELOADS);
				updateLoadNames();
			}
			if (ae.getSource() == ownerNameAll){
				_train.setOwnerOption(Train.ALLOWNERS);
				updateOwnerNames();
			}
			if (ae.getSource() == ownerNameInclude){
				_train.setOwnerOption(Train.INCLUDEOWNERS);
				updateOwnerNames();
			}
			if (ae.getSource() == ownerNameExclude){
				_train.setOwnerOption(Train.EXCLUDEOWNERS);
				updateOwnerNames();
			}
			if (ae.getSource() == builtDateAll ||
					ae.getSource() == builtDateAfter ||
					ae.getSource() == builtDateBefore ||
					ae.getSource() == builtDateRange )
				updateBuilt();	
		}
	}
	
	// Car type combo box has been changed, show loads associated with this car type
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		updateLoadComboBoxes();
	}
	
	private void updateRoadNames(){
		panelRoadNames.removeAll();
		
    	JPanel p = new JPanel();
    	p.setLayout(new GridBagLayout());
    	p.add(roadNameAll, 0);
    	p.add(roadNameInclude, 1);
    	p.add(roadNameExclude, 2);
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
		    		if (x > 6){
		    			y++;
		    			x = 0;
		    		}
		    	}
			}
		} else {
			roadNameAll.setSelected(true);
		}
		panelRoadNames.revalidate();
		packFrame();
	}
	
	private void updateLoadNames(){
		panelLoadNames.removeAll();
		
    	JPanel p = new JPanel();
    	p.setLayout(new GridBagLayout());
    	p.add(loadNameAll, 0);
    	p.add(loadNameInclude, 1);
    	p.add(loadNameExclude, 2);
    	GridBagConstraints gc = new GridBagConstraints();
    	gc.gridwidth = 6;
    	panelLoadNames.add(p, gc);
		
		int y = 1;		// vertical position in panel

		if(_train != null){
			// set radio button
			loadNameAll.setSelected(_train.getLoadOption().equals(Train.ALLLOADS));
			loadNameInclude.setSelected(_train.getLoadOption().equals(Train.INCLUDEROADS));
			loadNameExclude.setSelected(_train.getLoadOption().equals(Train.EXCLUDEROADS));
			
			if (!loadNameAll.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	p.add(typeBox);
		    	p.add(loadBox);
		    	p.add(addLoadButton);
		    	p.add(deleteLoadButton);
		    	p.add(deleteAllLoadsButton);
				gc.gridy = y++;
		    	panelLoadNames.add(p, gc);

		    	String[]carLoads = _train.getLoadNames();
		    	int x = 0;
		    	for (int i =0; i<carLoads.length; i++){
		    		JLabel load = new JLabel();
		    		load.setText(carLoads[i]);
		    		addItem(panelLoadNames, load, x++, y);
		    		if (x > 6){
		    			y++;
		    			x = 0;
		    		}
		    	}
			}
		} else {
			loadNameAll.setSelected(true);
		}
		panelLoadNames.revalidate();
		packFrame();
	}
	
	private void deleteAllRoads(){
		if(_train != null){
			String [] trainLoads = _train.getLoadNames();
			for (int i=0; i<trainLoads.length; i++){
				_train.deleteLoadName(trainLoads[i]);
			}
		}
		updateLoadNames();
	}
	
	private void updateOwnerNames(){
		panelOwnerNames.removeAll();
		
    	JPanel p = new JPanel();
    	p.setLayout(new GridBagLayout());
    	p.add(ownerNameAll, 0);
    	p.add(ownerNameInclude, 1);
    	p.add(ownerNameExclude, 2);
    	GridBagConstraints gc = new GridBagConstraints();
    	gc.gridwidth = 6;
    	panelOwnerNames.add(p, gc);
		
		int y = 1;		// vertical position in panel

		if(_train != null){
			// set radio button
			ownerNameAll.setSelected(_train.getOwnerOption().equals(Train.ALLOWNERS));
			ownerNameInclude.setSelected(_train.getOwnerOption().equals(Train.INCLUDEOWNERS));
			ownerNameExclude.setSelected(_train.getOwnerOption().equals(Train.EXCLUDEOWNERS));
			
			if (!ownerNameAll.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	p.add(ownerBox);
		    	p.add(addOwnerButton);
		    	p.add(deleteOwnerButton);
				gc.gridy = y++;
		    	panelOwnerNames.add(p, gc);

		    	String[]carOwners = _train.getOwnerNames();
		    	int x = 0;
		    	for (int i =0; i<carOwners.length; i++){
		    		JLabel owner = new JLabel();
		    		owner.setText(carOwners[i]);
		    		addItem(panelOwnerNames, owner, x++, y);
		    		if (x > 6){
		    			y++;
		    			x = 0;
		    		}
		    	}
			}
		} else {
			ownerNameAll.setSelected(true);
		}
		panelOwnerNames.revalidate();
		packFrame();
	}
	
	private void setBuiltRadioButton(){
		if (_train.getBuiltStartYear().equals("") && _train.getBuiltEndYear().equals(""))
			builtDateAll.setSelected(true);
		else if (!_train.getBuiltStartYear().equals("") && !_train.getBuiltEndYear().equals(""))
			builtDateRange.setSelected(true);
		else if (!_train.getBuiltStartYear().equals(""))
			builtDateAfter.setSelected(true);
		else if (!_train.getBuiltEndYear().equals(""))
			builtDateBefore.setSelected(true);		
	}
	
	private void updateBuilt(){
		builtAfterTextField.setVisible(false);
		builtBeforeTextField.setVisible(false);
		after.setVisible(false);
		before.setVisible(false);
		if (builtDateAll.isSelected()){
			builtAfterTextField.setText("");
			builtBeforeTextField.setText("");
		}else if (builtDateAfter.isSelected()){
			builtBeforeTextField.setText("");
			builtAfterTextField.setVisible(true);
			after.setVisible(true);
		}else if (builtDateBefore.isSelected()){
			builtAfterTextField.setText("");
			builtBeforeTextField.setVisible(true);
			before.setVisible(true);
		}else if (builtDateRange.isSelected()){
			after.setVisible(true);
			before.setVisible(true);
			builtAfterTextField.setVisible(true);
			builtBeforeTextField.setVisible(true);
		}
		packFrame();
	}
	
	private void saveTrain (){
		_train.setBuiltStartYear(builtAfterTextField.getText().trim());
		_train.setBuiltEndYear(builtBeforeTextField.getText().trim());
		manager.save();
	}
	
	private void enableButtons(boolean enabled){
		//clearButton.setEnabled(enabled);
		//setButton.setEnabled(enabled);
		roadNameAll.setEnabled(enabled);
		roadNameInclude.setEnabled(enabled);
		roadNameExclude.setEnabled(enabled);
		addRoadButton.setEnabled(enabled);
		deleteRoadButton.setEnabled(enabled);
		ownerNameAll.setEnabled(enabled);
		ownerNameInclude.setEnabled(enabled);
		ownerNameExclude.setEnabled(enabled);
		builtDateAll.setEnabled(enabled);
		builtDateAfter.setEnabled(enabled);
		builtDateBefore.setEnabled(enabled);
		builtDateRange.setEnabled(enabled);		
		saveTrainButton.setEnabled(enabled);
	}
	
	private void updateTypeComboBoxes(){
		CarTypes.instance().updateComboBox(typeBox);
	}
	
	private void updateRoadComboBoxes(){
		CarRoads.instance().updateComboBox(roadBox);
	}
	
	private void updateLoadComboBoxes(){
		String carType = (String)typeBox.getSelectedItem();
		CarLoads.instance().updateComboBox(carType, loadBox);
	}
	
	private void updateOwnerComboBoxes(){
		CarOwners.instance().updateComboBox(ownerBox);
	}
	
    private void packFrame(){
    	setVisible(false);
 		pack();
 		if(getWidth()<300)
 			setSize(300, getHeight());
 		else
 			setSize(getWidth()+50, getHeight());
		setVisible(true);
    }
	
	public void dispose() {
		CarTypes.instance().removePropertyChangeListener(this);	
		CarRoads.instance().removePropertyChangeListener(this);	
		CarLoads.instance().removePropertyChangeListener(this);
		CarOwners.instance().removePropertyChangeListener(this);	
		if (_train != null){
			_train.removePropertyChangeListener(this);
		}
		_trainEditFrame.setChildFrame(null);
		super.dispose();
	}

 	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()
				+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(CarRoads.CARROADS_LENGTH_CHANGED_PROPERTY)){
			updateRoadComboBoxes();
			updateRoadNames();
		}
		if (e.getPropertyName().equals(CarLoads.LOAD_NAME_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY)){
			updateLoadComboBoxes();
			updateLoadNames();
		}
		if (e.getPropertyName().equals(CarOwners.CAROWNERS_LENGTH_CHANGED_PROPERTY)){
			updateOwnerComboBoxes();
			updateOwnerNames();
		}
		if (e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY)){
			updateTypeComboBoxes();
		}
	}
 	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainEditBuildOptionsFrame.class.getName());
}
