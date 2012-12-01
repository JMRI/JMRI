// LocationEditFrame.java

package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;

import java.awt.*;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Frame for user edit of location
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2012
 * @version $Revision$
 */

public class LocationEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	YardTableModel yardModel = new YardTableModel();
	JTable yardTable = new JTable(yardModel);
	JScrollPane yardPane;
	SidingTableModel sidingModel = new SidingTableModel();
	JTable sidingTable = new JTable(sidingModel);
	JScrollPane sidingPane;
	InterchangeTableModel interchangeModel = new InterchangeTableModel();
	JTable interchangeTable = new JTable(interchangeModel);
	JScrollPane interchangePane;
	StagingTableModel stagingModel = new StagingTableModel();
	JTable stagingTable = new JTable(stagingModel);
	JScrollPane stagingPane;
	
	LocationManager manager;
	LocationManagerXml managerXml;

	Location _location = null;
	ArrayList<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
	JPanel panelCheckBoxes = new JPanel();
	JScrollPane typePane;
	JPanel directionPanel = new JPanel();

	// major buttons
	JButton clearButton = new JButton(rb.getString("Clear"));
	JButton setButton = new JButton(rb.getString("Select"));
	JButton autoSelectButton = new JButton(rb.getString("AutoSelect"));
	JButton saveLocationButton = new JButton(rb.getString("SaveLocation"));
	JButton deleteLocationButton = new JButton(rb.getString("DeleteLocation"));
	JButton addLocationButton = new JButton(rb.getString("AddLocation"));
	JButton addYardButton = new JButton(rb.getString("AddYard"));
	JButton addSidingButton = new JButton(rb.getString("AddSiding"));
	JButton addInterchangeButton = new JButton(rb.getString("AddInterchange"));
	JButton addStagingButton = new JButton(rb.getString("AddStaging"));
	
	// check boxes
	JCheckBox checkBox;
	JCheckBox northCheckBox = new JCheckBox(rb.getString("North"));
	JCheckBox southCheckBox = new JCheckBox(rb.getString("South"));
	JCheckBox eastCheckBox = new JCheckBox(rb.getString("East"));
	JCheckBox westCheckBox = new JCheckBox(rb.getString("West"));
	
	// radio buttons
    JRadioButton stageRadioButton = new JRadioButton(rb.getString("Staging"));
    JRadioButton interchangeRadioButton = new JRadioButton(rb.getString("Interchange"));
    JRadioButton yardRadioButton = new JRadioButton(rb.getString("Yards"));
    JRadioButton sidingRadioButton = new JRadioButton(rb.getString("Sidings"));
        
	// text field
	JTextField locationNameTextField = new JTextField(20);
	
	// text area
	JTextArea commentTextArea	= new JTextArea(2,80);
	JScrollPane commentScroller = new JScrollPane(commentTextArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	Dimension minScrollerDim = new Dimension(800,42);
	
	// combo boxes

	public static final String NAME = rb.getString("Name");
	public static final int MAX_NAME_LENGTH = Control.max_len_string_location_name;
	public static final String DISPOSE = "dispose" ;

	public LocationEditFrame() {
		super();
	}

	public void initComponents(Location location) {
		_location = location;

		// load managers
		manager = LocationManager.instance();
		managerXml = LocationManagerXml.instance();
		
	   	// Set up the jtable in a Scroll Pane..
    	typePane = new JScrollPane(panelCheckBoxes);
    	typePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	typePane.setBorder(BorderFactory.createTitledBorder(rb.getString("Types")));
    	
    	yardPane = new JScrollPane(yardTable);
    	yardPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	yardPane.setBorder(BorderFactory.createTitledBorder(""));
        	
    	sidingPane = new JScrollPane(sidingTable);
    	sidingPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	sidingPane.setBorder(BorderFactory.createTitledBorder(""));
    	
    	interchangePane = new JScrollPane(interchangeTable);
    	interchangePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	interchangePane.setBorder(BorderFactory.createTitledBorder(""));
    	
    	stagingPane = new JScrollPane(stagingTable);
    	stagingPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	stagingPane.setBorder(BorderFactory.createTitledBorder(""));
    	
    	// button group
		ButtonGroup opsGroup = new ButtonGroup();
		opsGroup.add(sidingRadioButton);
		opsGroup.add(yardRadioButton);
		opsGroup.add(interchangeRadioButton);
		opsGroup.add(stageRadioButton);
		
		// Location name for tools menu
		String locationName = null;
		
		if (_location != null){
			enableButtons(true);
			locationNameTextField.setText(_location.getName());
			commentTextArea.setText(_location.getComment());
	      	yardModel.initTable(yardTable, location);
	      	sidingModel.initTable(sidingTable, location);
	      	interchangeModel.initTable(interchangeTable, location);
	      	stagingModel.initTable(stagingTable, location);
	      	_location.addPropertyChangeListener(this);
	      	locationName = _location.getName();
			if (_location.getLocationOps() == Location.NORMAL){
				if (sidingModel.getRowCount()>0)
					sidingRadioButton.setSelected(true);
				else if (yardModel.getRowCount()>0)
					yardRadioButton.setSelected(true);
				else if (interchangeModel.getRowCount()>0)
					interchangeRadioButton.setSelected(true);
				else if (stagingModel.getRowCount()>0)
					stageRadioButton.setSelected(true);
				else
					sidingRadioButton.setSelected(true);
			}else{
				stageRadioButton.setSelected(true);
			}
			setTrainDirectionBoxes();
		} else {
			enableButtons(false);
			sidingRadioButton.setSelected(true);
		}
		
		setVisibleLocations();
	
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

		// Layout the panel by rows
		// row 1
    	JPanel p1 = new JPanel();
    	p1.setLayout(new BoxLayout(p1,BoxLayout.X_AXIS));
       	JScrollPane p1Pane = new JScrollPane(p1);
       	p1Pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
       	p1Pane.setMinimumSize(new Dimension(300,3*locationNameTextField.getPreferredSize().height));
       	p1Pane.setBorder(BorderFactory.createTitledBorder(""));
       	
		// row 1a
    	JPanel pName = new JPanel();
    	pName.setLayout(new GridBagLayout());
    	pName.setBorder(BorderFactory.createTitledBorder(rb.getString("Name")));
				
		addItem(pName, locationNameTextField, 0, 0);

		// row 1b
    	directionPanel.setLayout(new GridBagLayout());
    	directionPanel.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainLocation")));
		addItem(directionPanel, northCheckBox, 1, 0);
		addItem(directionPanel, southCheckBox, 2, 0);
		addItem(directionPanel, eastCheckBox, 3, 0);
		addItem(directionPanel, westCheckBox, 4, 0);
		
		p1.add(pName);
		p1.add(directionPanel);

		// row 5
	   	panelCheckBoxes.setLayout(new GridBagLayout());
		updateCheckboxes();
		
		// row 9
		JPanel pOp = new JPanel();
		pOp.setLayout(new GridBagLayout());
		pOp.setBorder(BorderFactory.createTitledBorder(rb.getString("Ops")));
		pOp.add(sidingRadioButton);
		pOp.add(yardRadioButton);
		pOp.add(interchangeRadioButton);
		pOp.add(stageRadioButton);

		// row 11
    	JPanel pC = new JPanel();
    	pC.setLayout(new GridBagLayout());
    	pC.setBorder(BorderFactory.createTitledBorder(rb.getString("Comment")));
    	commentScroller.setMinimumSize(minScrollerDim);
		addItem(pC, commentScroller, 0, 0);
				
		// row 12
	   	JPanel pB = new JPanel();
    	pB.setLayout(new GridBagLayout());
		addItem(pB, deleteLocationButton, 0, 0);
		addItem(pB, addLocationButton, 1, 0);
		addItem(pB, saveLocationButton, 3, 0);
		
		getContentPane().add(p1Pane);
		getContentPane().add(typePane);
		getContentPane().add(pOp);
       	getContentPane().add(yardPane);
       	getContentPane().add(addYardButton);
       	getContentPane().add(sidingPane);
       	getContentPane().add(addSidingButton);
       	getContentPane().add(interchangePane);
       	getContentPane().add(addInterchangeButton);
       	getContentPane().add(stagingPane);
       	getContentPane().add(addStagingButton);
       	getContentPane().add(pC);
       	getContentPane().add(pB);
		
		// setup buttons
		addButtonAction(setButton);
		addButtonAction(clearButton);
		addButtonAction(autoSelectButton);
		addButtonAction(deleteLocationButton);
		addButtonAction(addLocationButton);
		addButtonAction(saveLocationButton);
		addButtonAction(addYardButton);
		addButtonAction(addSidingButton);
		addButtonAction(addInterchangeButton);
		addButtonAction(addStagingButton);
		
		// add tool tips
		autoSelectButton.setToolTipText(rb.getString("TipAutoSelect"));
		
		addRadioButtonAction(sidingRadioButton);
		addRadioButtonAction(yardRadioButton);
		addRadioButtonAction(interchangeRadioButton);
		addRadioButtonAction(stageRadioButton);
		
		addCheckBoxTrainAction(northCheckBox);
		addCheckBoxTrainAction(southCheckBox);
		addCheckBoxTrainAction(eastCheckBox);
		addCheckBoxTrainAction(westCheckBox);

		// add property listeners
		CarTypes.instance().addPropertyChangeListener(this);
		EngineTypes.instance().addPropertyChangeListener(this);      	

		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(rb.getString("Tools"));
		toolMenu.add(new ModifyLocationsAction(rb.getString("TitleModifyLocation"), _location));	
		toolMenu.add(new ShowCarsByLocationAction(false, locationName, null));
		toolMenu.add(new ChangeTracksTypeAction(this));
		if (Setup.isVsdPhysicalLocationEnabled())
			toolMenu.add(new SetPhysicalLocationAction(rb.getString("MenuSetPhysicalLocation"), _location));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Locations", true);

		pack();
		if (getWidth()<750)
			setSize(750, getHeight());
		if (getHeight()<Control.panelHeight)
			setSize(getWidth(), Control.panelHeight);
		setMinimumSize(new Dimension(750, Control.panelHeight));
		setVisible(true);
		
        // create ShutDownTasks
        createShutDownTask();
	}
	
	YardEditFrame yef = null;
	SidingEditFrame sef = null;
	InterchangeEditFrame ief = null;
	StagingEditFrame stef = null;
	
	// Save, Delete, Add 
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == addYardButton){
			yef = new YardEditFrame();
			yef.initComponents(_location, null);
			yef.setTitle(rb.getString("AddYard"));
		}
		if (ae.getSource() == addSidingButton){
			sef = new SidingEditFrame();
			sef.initComponents(_location, null);
			sef.setTitle(rb.getString("AddSiding"));
		}
		if (ae.getSource() == addInterchangeButton){
			ief = new InterchangeEditFrame();
			ief.initComponents(_location, null);
			ief.setTitle(rb.getString("AddInterchange"));
		}
		if (ae.getSource() == addStagingButton){
			stef = new StagingEditFrame();
			stef.initComponents(_location, null);
			stef.setTitle(rb.getString("AddStaging"));
		}

		if (ae.getSource() == saveLocationButton){
			log.debug("location save button activated");
			Location l = manager.getLocationByName(locationNameTextField.getText());
			if (_location == null && l == null){
				saveNewLocation();
			} else {
				if (l != null && l != _location){
					reportLocationExists(rb.getString("save"));
					return;
				}
				saveLocation();
				if (Setup.isCloseWindowOnSaveEnabled())
					dispose();
			}
		}
		if (ae.getSource() == deleteLocationButton){
			log.debug("location delete button activated");
			Location l = manager.getLocationByName(locationNameTextField.getText());
			if (l == null)
				return;
			int rs = l.getNumberRS();
			if (rs > 0){
				if (JOptionPane.showConfirmDialog(this,
						MessageFormat.format(rb.getString("ThereAreCars"),new Object[]{Integer.toString(rs)}), rb.getString("deletelocation?"),
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
					return;
				}
			} else {
				if (JOptionPane.showConfirmDialog(this,
						MessageFormat.format(rb.getString("DoYouWantToDeleteLocation"),new Object[]{locationNameTextField.getText()}), rb.getString("deletelocation?"),
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
					return;
				}
			}
			
			yardModel.dispose();
			sidingModel.dispose();
			interchangeModel.dispose();
			stagingModel.dispose();
			
			if (yef != null)
				yef.dispose();
			if (sef != null)
				sef.dispose();
			if (ief != null)
				ief.dispose();
			if (stef != null)
				stef.dispose();
			
			manager.deregister(l);
			_location = null;
			selectCheckboxes(false);
			enableCheckboxes(false);
			enableButtons(false);
			// save location file
			OperationsXml.save();
		}
		if (ae.getSource() == addLocationButton){
			Location l = manager.getLocationByName(locationNameTextField.getText());
			if (l != null){
				reportLocationExists(rb.getString("add"));
				return;
			}
			saveNewLocation();
		}
		if (ae.getSource() == setButton){
			selectCheckboxes(true);
		}
		if (ae.getSource() == clearButton){
			selectCheckboxes(false);
		}
		if (ae.getSource() == autoSelectButton){
			log.debug("auto select button pressed");
			if (JOptionPane.showConfirmDialog(this,
					rb.getString("autoSelectCarTypes?"), rb.getString("autoSelectLocations?"),
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
				return;
			}
			autoSelectCheckboxes();
		}
	}
	
	private void saveNewLocation(){
		if (!checkName(rb.getString("add")))
			return;
		Location location = manager.newLocation(locationNameTextField.getText());
		yardModel.initTable(yardTable, location);
      	sidingModel.initTable(sidingTable, location);
      	interchangeModel.initTable(interchangeTable, location);
      	stagingModel.initTable(stagingTable, location);
		_location = location;
		// enable check boxes
		updateCheckboxes();
		//enableCheckboxes(true);
		enableButtons(true);
		setTrainDirectionBoxes();
		saveLocation();
	}
	
	private void saveLocation (){
		if (!checkName(rb.getString("save")))
			return;
		_location.setName(locationNameTextField.getText());
		_location.setComment(commentTextArea.getText());

		if (sidingRadioButton.isSelected() || yardRadioButton.isSelected() || interchangeRadioButton.isSelected()){
			_location.setLocationOps(Location.NORMAL);
		}
		if (stageRadioButton.isSelected()){
			_location.setLocationOps(Location.STAGING);
		}
		/* all JMRI window position and size are now saved
		// save frame size and position
		manager.setLocationEditFrame(this);
		*/
		// save location file
		OperationsXml.save();
	}
	

	/**
	 * 
	 * @return true if name is less than 26 characters
	 */
	private boolean checkName(String s){
		if (locationNameTextField.getText().trim().equals("")){
			log.debug("Must enter a location name");
			JOptionPane.showMessageDialog(this,
					rb.getString("MustEnterName"),
					MessageFormat.format(rb.getString("CanNotLocation"),new Object[]{s }),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (locationNameTextField.getText().length() > MAX_NAME_LENGTH){
			log.error("Location name must be less than "+ Integer.toString(MAX_NAME_LENGTH+1) +" characters");
			JOptionPane.showMessageDialog(this,
					MessageFormat.format(rb.getString("LocationNameLengthMax"),new Object[]{Integer.toString(MAX_NAME_LENGTH+1)}),
					MessageFormat.format(rb.getString("CanNotLocation"),new Object[]{s }),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private void reportLocationExists(String s){
		log.info("Can not " + s + ", location already exists");
		JOptionPane.showMessageDialog(this,
				rb.getString("LocationAlreadyExists"), MessageFormat.format(rb.getString("CanNotLocation"),new Object[]{s }),
				JOptionPane.ERROR_MESSAGE);
	}
	
	private void enableButtons(boolean enabled){
		northCheckBox.setEnabled(enabled);
		southCheckBox.setEnabled(enabled);
		eastCheckBox.setEnabled(enabled);
		westCheckBox.setEnabled(enabled);
		clearButton.setEnabled(enabled);
		setButton.setEnabled(enabled);
		autoSelectButton.setEnabled(enabled);
		addYardButton.setEnabled(enabled);
		addSidingButton.setEnabled(enabled);
		addInterchangeButton.setEnabled(enabled);
		addStagingButton.setEnabled(enabled);
		saveLocationButton.setEnabled(enabled);
		deleteLocationButton.setEnabled(enabled);
		// the inverse!
		addLocationButton.setEnabled(!enabled);
		// enable radio buttons
		sidingRadioButton.setEnabled(enabled);
		yardRadioButton.setEnabled(enabled);
		interchangeRadioButton.setEnabled(enabled);
		stageRadioButton.setEnabled(enabled);
		//
		yardTable.setEnabled(enabled);
	}
	
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		setVisibleLocations();
	}
	
	private void setVisibleLocations(){
		setEnabledLocations();
		interchangePane.setVisible(interchangeRadioButton.isSelected());
		addInterchangeButton.setVisible(interchangeRadioButton.isSelected());
		stagingPane.setVisible(stageRadioButton.isSelected());
		addStagingButton.setVisible(stageRadioButton.isSelected());
		yardPane.setVisible(yardRadioButton.isSelected());
		addYardButton.setVisible(yardRadioButton.isSelected());
		sidingPane.setVisible(sidingRadioButton.isSelected());
		addSidingButton.setVisible(sidingRadioButton.isSelected());
	}
	
	private void setEnabledLocations(){
		log.debug("set radio button");
		if (sidingModel.getRowCount()>0 || yardModel.getRowCount()>0 || interchangeModel.getRowCount()>0){
			if(stageRadioButton.isSelected())
				sidingRadioButton.setSelected(true);
			stageRadioButton.setEnabled(false);
		}
		else if (stagingModel.getRowCount()>0){
			stageRadioButton.setSelected(true);
			sidingRadioButton.setEnabled(false);
			yardRadioButton.setEnabled(false);
			interchangeRadioButton.setEnabled(false);
		} 
		else {
			sidingRadioButton.setEnabled(true);
			yardRadioButton.setEnabled(true);
			interchangeRadioButton.setEnabled(true);
			stageRadioButton.setEnabled(true);
		}
	}
	
	private void enableCheckboxes(boolean enable){
		for (int i=0; i < checkBoxes.size(); i++){
			checkBox = checkBoxes.get(i);
			checkBox.setEnabled(enable);
		}
	}
	
	private void selectCheckboxes(boolean select){
		for (int i=0; i < checkBoxes.size(); i++){
			checkBox = checkBoxes.get(i);
			checkBox.setSelected(select);
			if(_location != null){
				if (select)
					_location.addTypeName(checkBox.getText());
				else
					_location.deleteTypeName(checkBox.getText());
			}
		}
	}
	
	private void updateCheckboxes(){
		 x = 0;
		 y = 0;
		checkBoxes.clear();
		panelCheckBoxes.removeAll();
		loadTypes(CarTypes.instance().getNames());
		loadTypes(EngineTypes.instance().getNames());
    	addItem(panelCheckBoxes, clearButton, 1, ++y);
    	addItem(panelCheckBoxes, setButton, 3, y);
    	addItem(panelCheckBoxes, autoSelectButton, 5, y);
		panelCheckBoxes.revalidate();
		repaint();
	}
	
	int x = 0;
	int y = 0;	// vertical position in panel
	private void loadTypes(String[] types){
		int numberOfCheckBoxes = getNumberOfCheckboxes();
		for (int i =0; i<types.length; i++){
			JCheckBox checkBox = new JCheckBox();
			checkBoxes.add(checkBox);
			checkBox.setText(types[i]);
			addCheckBoxAction(checkBox);
			addItemLeft(panelCheckBoxes, checkBox, x++, y);
			if (_location != null){
				if(_location.acceptsTypeName(types[i]))
					checkBox.setSelected(true);
			} else {
				checkBox.setEnabled(false);
			}
			// default is seven types per row
			if (x > numberOfCheckBoxes){
				y++;
				x = 0;
			}
		}
	}
	
	/**
	 * Adjust the location's car service types to only reflect 
	 * the car types serviced by the location's tracks. 
	 */
	private void autoSelectCheckboxes(){
		for (int i=0; i < checkBoxes.size(); i++){
			checkBoxes.get(i).setSelected(false);
			// check each track to determine which car types are serviced by this location
			List<String> tracks = _location.getTrackIdsByNameList(null);
			for (int j=0; j<tracks.size(); j++){
				Track track = _location.getTrackById(tracks.get(j));
				if (track.acceptsTypeName(checkBoxes.get(i).getText()))
					checkBoxes.get(i).setSelected(true);
			}
			// this type of car isn't serviced by any of the tracks, so delete
			if (!checkBoxes.get(i).isSelected()){
				_location.deleteTypeName(checkBoxes.get(i).getText());
			}
		}
	}
	
	 LocationsByCarTypeFrame f = null;
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		JCheckBox b = (JCheckBox) ae.getSource();
		log.debug("checkbox change " + b.getText());
		if (_location == null)
			return;
		_location.removePropertyChangeListener(this);
		if (b.isSelected()) {
			_location.addTypeName(b.getText());
			// show which tracks will service this car type
			if (CarTypes.instance().containsName(b.getText())) {
				if (f != null)
					f.dispose();
				f = new LocationsByCarTypeFrame();
				f.initComponents(_location, b.getText());
			}
		} else {
			_location.deleteTypeName(b.getText());
		}
		_location.addPropertyChangeListener(this);
	}
	
	
	private void addCheckBoxTrainAction(JCheckBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				checkBoxActionTrainPerformed(e);
			}
		});
	}
	
	private void checkBoxActionTrainPerformed(java.awt.event.ActionEvent ae) {
		// save train directions serviced by this location
		if (_location == null)
			return;
		int direction = 0;
		if (northCheckBox.isSelected()){
			direction += Location.NORTH;
		}
		if (southCheckBox.isSelected()){
			direction += Location.SOUTH;
		}
		if (eastCheckBox.isSelected()){
			direction += Location.EAST;
		}
		if (westCheckBox.isSelected()){
			direction += Location.WEST;
		}
		_location.setTrainDirections(direction);
		
	}
	
	private void setTrainDirectionBoxes(){
		northCheckBox.setVisible((Setup.getTrainDirection() & Setup.NORTH)>0);
		southCheckBox.setVisible((Setup.getTrainDirection() & Setup.SOUTH)>0);
		eastCheckBox.setVisible((Setup.getTrainDirection() & Setup.EAST)>0);
		westCheckBox.setVisible((Setup.getTrainDirection() & Setup.WEST)>0);
		
		northCheckBox.setSelected((_location.getTrainDirections() & Location.NORTH)>0);
		southCheckBox.setSelected((_location.getTrainDirections() & Location.SOUTH)>0);
		eastCheckBox.setSelected((_location.getTrainDirections() & Location.EAST)>0);
		westCheckBox.setSelected((_location.getTrainDirections() & Location.WEST)>0);
	}
	
	public void dispose() {
		if (_location != null)
			_location.removePropertyChangeListener(this);
		CarTypes.instance().removePropertyChangeListener(this);
		EngineTypes.instance().removePropertyChangeListener(this);
		yardModel.dispose();
		sidingModel.dispose();
		interchangeModel.dispose();
		stagingModel.dispose();
		super.dispose();
	}
	
 	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled()) 
			log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(EngineTypes.ENGINETYPES_LENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY)){
			updateCheckboxes();
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(LocationEditFrame.class.getName());
}
