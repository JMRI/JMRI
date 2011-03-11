// TrackEditFrame.java

package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;

import java.awt.*;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Frame for user edit of tracks
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2010
 * @version $Revision: 1.50 $
 */

public class TrackEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	LocationManager manager;
	LocationManagerXml managerXml;

	Location _location = null;
	Track _track = null;
	String _type = "";
	JMenu _toolMenu = null;
	
	List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
	
	// panels
	JPanel panelCheckBoxes = new JPanel();
	JScrollPane paneCheckBoxes = new JScrollPane(panelCheckBoxes);
	JPanel panelTrainDir = new JPanel();
	JPanel panelRoadNames = new JPanel();
	JScrollPane paneRoadNames = new JScrollPane(panelRoadNames);
	JPanel panelLoadNames = new JPanel();
	JScrollPane paneLoadNames = new JScrollPane(panelLoadNames);
	JPanel panelOrder = new JPanel();
	
	// major buttons
	JButton clearButton = new JButton(rb.getString("Clear"));
	JButton setButton = new JButton(rb.getString("Select"));
	JButton saveTrackButton = new JButton(rb.getString("SaveTrack"));
	JButton deleteTrackButton = new JButton(rb.getString("DeleteTrack"));
	JButton addTrackButton = new JButton(rb.getString("AddTrack"));
	JButton deleteRoadButton = new JButton(rb.getString("DeleteRoad"));
	JButton addRoadButton = new JButton(rb.getString("AddRoad"));
	JButton addLoadButton = new JButton(rb.getString("AddLoad"));
	JButton deleteLoadButton = new JButton(rb.getString("DeleteLoad"));
	JButton deleteAllLoadsButton = new JButton(rb.getString("DeleteAllLoads"));
	
	// check boxes
	JCheckBox checkBox;
	JCheckBox northCheckBox = new JCheckBox(rb.getString("North"));
	JCheckBox southCheckBox = new JCheckBox(rb.getString("South"));
	JCheckBox eastCheckBox = new JCheckBox(rb.getString("East"));
	JCheckBox westCheckBox = new JCheckBox(rb.getString("West"));
	
	// radio buttons
    JRadioButton roadNameAll = new JRadioButton(rb.getString("AcceptAll"));
    JRadioButton roadNameInclude = new JRadioButton(rb.getString("AcceptOnly"));
    JRadioButton roadNameExclude = new JRadioButton(rb.getString("Exclude"));
    ButtonGroup roadGroup = new ButtonGroup();
    
    JRadioButton loadNameAll = new JRadioButton(rb.getString("AcceptAll"));
    JRadioButton loadNameInclude = new JRadioButton(rb.getString("AcceptOnly"));
    JRadioButton loadNameExclude = new JRadioButton(rb.getString("Exclude"));
    ButtonGroup loadGroup = new ButtonGroup();
    
	// car pick up order controls
	JRadioButton orderNormal = new JRadioButton(rb.getString("Normal"));
	JRadioButton orderFIFO = new JRadioButton(rb.getString("DescriptiveFIFO"));
	JRadioButton orderLIFO = new JRadioButton(rb.getString("DescriptiveLIFO"));
	ButtonGroup orderGroup = new ButtonGroup();   
    
	// text field
	JTextField trackNameTextField = new JTextField(20);
	JTextField trackLengthTextField = new JTextField(5);
	
	// text area
	JTextArea commentTextArea	= new JTextArea(2,60);
	JScrollPane commentScroller = new JScrollPane(commentTextArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	Dimension minScrollerDim = new Dimension(500,42);

	// for padding out panels
	JLabel space1 = new JLabel("     ");
	JLabel space2 = new JLabel("     ");
	JLabel space3 = new JLabel("     ");
	
	// combo box
	JComboBox comboBoxRoads = CarRoads.instance().getComboBox();
	JComboBox comboBoxLoads = CarLoads.instance().getComboBox(null);
	JComboBox comboBoxTypes = CarTypes.instance().getComboBox();

	// optional panel for sidings, staging, and interchanges
	JPanel panelOpt1 = new JPanel();
	JPanel panelOpt2 = new JPanel();
	JPanel panelOpt3 = new JPanel();		// not currently used
	JPanel panelOpt4 = new JPanel();

	public static final String DISPOSE = "dispose" ;
	public static final int MAX_NAME_LENGTH = Control.MAX_LEN_STRING_TRACK_NAME;

	public TrackEditFrame() {
		super();
	}

	public void initComponents(Location location, Track track) {
		_location = location;
		_track = track;
		
		// property changes
		_location.addPropertyChangeListener(this);
		// listen for car road name and type changes
		CarRoads.instance().addPropertyChangeListener(this);
		CarLoads.instance().addPropertyChangeListener(this);
		CarTypes.instance().addPropertyChangeListener(this);
		
		// load managers
		managerXml = LocationManagerXml.instance();

		// the following code sets the frame's initial state
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

	    //      Set up the panels
		// Layout the panel by rows
		// row 1
    	JPanel p1 = new JPanel();
    	p1.setLayout(new BoxLayout(p1,BoxLayout.X_AXIS));
	    
    	// row 1a
    	JPanel pName = new JPanel();
    	pName.setLayout(new GridBagLayout());
    	pName.setBorder(BorderFactory.createTitledBorder(rb.getString("Name")));
    	addItem(pName, trackNameTextField, 0, 0);	

		// row 1b
    	JPanel pLength = new JPanel();
    	pLength.setLayout(new GridBagLayout());
    	pLength.setBorder(BorderFactory.createTitledBorder(rb.getString("Length")));
		addItem(pLength, trackLengthTextField, 0, 0);
		
		p1.add(pName);
		p1.add(pLength);
			
		// row 3
		panelTrainDir.setLayout(new GridBagLayout());
		panelTrainDir.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainTrack")));

		// row 4
	   	panelCheckBoxes.setLayout(new GridBagLayout());
		
		// row 5
		panelRoadNames.setLayout(new GridBagLayout());
		paneRoadNames.setBorder(BorderFactory.createTitledBorder(rb.getString("RoadsTrack")));
		roadGroup.add(roadNameAll);
		roadGroup.add(roadNameInclude);
		roadGroup.add(roadNameExclude);
		
		// row 8
		panelLoadNames.setLayout(new GridBagLayout());
		paneLoadNames.setBorder(BorderFactory.createTitledBorder(rb.getString("LoadsTrack")));
		loadGroup.add(loadNameAll);
		loadGroup.add(loadNameInclude);
		loadGroup.add(loadNameExclude);
		
		// row 10
		// order panel
		panelOrder.setLayout(new GridBagLayout());
		panelOrder.setBorder(BorderFactory.createTitledBorder(rb.getString("PickupOrder")));
		panelOrder.add(orderNormal);
		panelOrder.add(orderFIFO);
		panelOrder.add(orderLIFO);
		
		orderGroup.add(orderNormal);
		orderGroup.add(orderFIFO);
		orderGroup.add(orderLIFO);
		
		// row 11
    	JPanel panelComment = new JPanel();
    	panelComment.setLayout(new GridBagLayout());
    	panelComment.setBorder(BorderFactory.createTitledBorder(rb.getString("Comment")));
    	commentScroller.setMinimumSize(minScrollerDim);
		addItem(panelComment, commentScroller, 0, 0);
				
		// row 12
		JPanel panelButtons = new JPanel();
		panelButtons.setLayout(new GridBagLayout());

		// row 13
		addItem(panelButtons, deleteTrackButton, 0, 0);
		addItem(panelButtons, addTrackButton, 1, 0);
		addItem(panelButtons, saveTrackButton, 2, 0);
			
		paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(rb.getString("TypesTrack")));
		
		getContentPane().add(p1);
		getContentPane().add(panelTrainDir);
		getContentPane().add(paneCheckBoxes);
		getContentPane().add(paneRoadNames);
		getContentPane().add(paneLoadNames);
		getContentPane().add(paneLoadNames);
		getContentPane().add(panelOrder);
		
		// add optional panels
		getContentPane().add(panelOpt1);
		getContentPane().add(panelOpt2);
		getContentPane().add(panelOpt3);
		getContentPane().add(panelOpt4);
		
       	getContentPane().add(panelComment);
       	getContentPane().add(panelButtons);
		
		// setup buttons
		addButtonAction(setButton);
		addButtonAction(clearButton);
		
		addButtonAction(deleteTrackButton);
		addButtonAction(addTrackButton);
		addButtonAction(saveTrackButton);
		addButtonAction(deleteRoadButton);
		
		addButtonAction(addRoadButton);
		addButtonAction(deleteLoadButton);
		addButtonAction(deleteAllLoadsButton);
		addButtonAction(addLoadButton);
		
		addRadioButtonAction(roadNameAll);
		addRadioButtonAction(roadNameInclude);
		addRadioButtonAction(roadNameExclude);
		
		addRadioButtonAction(loadNameAll);
		addRadioButtonAction(loadNameInclude);
		addRadioButtonAction(loadNameExclude);
		
		addRadioButtonAction(orderNormal);
		addRadioButtonAction(orderFIFO);
		addRadioButtonAction(orderLIFO);
		
		addComboBoxAction(comboBoxTypes);
		
		// track name for tools menu
		String trackName = null;
		
		// load fields and enable buttons
		if (_track !=null){
			_track.addPropertyChangeListener(this);
			trackNameTextField.setText(_track.getName());
			commentTextArea.setText(_track.getComment());
			trackLengthTextField.setText(Integer.toString(_track.getLength()));
			enableButtons(true);
			trackName = _track.getName();
		} else {
			enableButtons(false);
		}
		
		// build menu
		JMenuBar menuBar = new JMenuBar();
		_toolMenu = new JMenu(rb.getString("Tools"));
		_toolMenu.add(new ShowCarsByLocationAction(false, location.getName(), trackName));
		menuBar.add(_toolMenu);
		setJMenuBar(menuBar);
		
		// load
		updateCheckboxes();
		updateRoadNames();
		updateTypeComboBoxes();
		updateLoadComboBoxes();
		updateLoadNames();
		updateTrainDir();
		updateCarOrder();
	}
	
	// Save, Delete, Add 
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveTrackButton){
			log.debug("track save button actived");
			if (_track != null){
				if (!checkUserInputs(_track))
					return;
				saveTrack(_track);
			} else {
				addNewTrack();
			}
		}
		if (ae.getSource() == deleteTrackButton){
			log.debug("track delete button actived");
//			Track y = _location.getTrackByName(trackNameTextField.getText());
			if (_track != null){
				int rs = _track.getNumberRS();
				if (rs > 0){
					if (JOptionPane.showConfirmDialog(this,
							MessageFormat.format(rb.getString("ThereAreCars"),new Object[]{Integer.toString(rs)}), rb.getString("deleteTrack?"),
							JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
						return;
					}
				}
				selectCheckboxes(false);
				_location.deleteTrack(_track);
				_track = null;
				enableButtons(false);
				// save location file
				managerXml.writeOperationsFile();
			}
		}
		if (ae.getSource() == addTrackButton){
			addNewTrack();
		}
		if (ae.getSource() == addRoadButton){
			_track.addRoadName((String) comboBoxRoads.getSelectedItem());
			updateRoadNames();
			selectNextItemComboBox(comboBoxRoads);
		}
		if (ae.getSource() == deleteRoadButton){
			_track.deleteRoadName((String) comboBoxRoads.getSelectedItem());
			updateRoadNames();
			selectNextItemComboBox(comboBoxRoads);
		}
		if (ae.getSource() == addLoadButton){
			if(_track.addLoadName((String) comboBoxLoads.getSelectedItem()))
				updateLoadNames();
			selectNextItemComboBox(comboBoxLoads);
		}
		if (ae.getSource() == deleteLoadButton){
			if(_track.deleteLoadName((String) comboBoxLoads.getSelectedItem()))
				updateLoadNames();
			selectNextItemComboBox(comboBoxLoads);
		}
		if (ae.getSource() == deleteAllLoadsButton){
			deleteAllLoads();
		}
		if (ae.getSource() == setButton){
			selectCheckboxes(true);
		}
		if (ae.getSource() == clearButton){
			selectCheckboxes(false);
		}
	}

	private void addNewTrack(){
		// check that track name is valid
		if (!checkName(rb.getString("add")))
			return;
		// check to see if track already exists
		Track check = _location.getTrackByName(trackNameTextField.getText(), null);
		if (check != null){
			reportTrackExists(rb.getString("add"));
			return;
		}
		// add track to this location
		_track =_location.addTrack(trackNameTextField.getText(), _type);
		// check track length
		checkLength(_track);
		//update check boxes
		updateCheckboxes();

		_track.addPropertyChangeListener(this);

		// setup check boxes
		selectCheckboxes(true);
		updateTrainDir();
		// store comment
		_track.setComment(commentTextArea.getText());
		// enable 
		enableButtons(true);
		// save location file
		managerXml.writeOperationsFile();
	}

	protected void saveTrack (Track track){
		// save train directions serviced by this location
		int direction = 0;
		if (northCheckBox.isSelected()){
			direction += Track.NORTH;
		}
		if (southCheckBox.isSelected()){
			direction += Track.SOUTH;
		}
		if (eastCheckBox.isSelected()){
			direction += Track.EAST;
		}
		if (westCheckBox.isSelected()){
			direction += Track.WEST;
		}
		track.setTrainDirections(direction);
		track.setName(trackNameTextField.getText());
		
		track.setComment(commentTextArea.getText());
		// enable 
		enableButtons(true);
		// save location file
		managerXml.writeOperationsFile();
	}

	private boolean checkUserInputs(Track track){
		// check that track name is valid
		if (!checkName(rb.getString("save")))
			return false;
		// check to see if track already exists
		Track check = _location.getTrackByName(trackNameTextField.getText(), null);
		if (check != null && check != track){
			reportTrackExists(rb.getString("save"));
			return false;
		}
		// check track length
		if (!checkLength(track))
			return false;
		return true;
	}
	
	
	/**
	 * 
	 * @return true if name is less than 26 characters
	 */
	private boolean checkName(String s){
		if (trackNameTextField.getText().trim().equals("")){
			log.debug("Must enter a track name");
			JOptionPane.showMessageDialog(this,
					rb.getString("MustEnterName"),
					MessageFormat.format(rb.getString("CanNotTrack"),new Object[]{s }),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (trackNameTextField.getText().length() > MAX_NAME_LENGTH){
			log.error("Track name must be less than "+ Integer.toString(MAX_NAME_LENGTH+1) +" charaters");
			JOptionPane.showMessageDialog(this,
					MessageFormat.format(rb.getString("TrackNameLengthMax"),new Object[]{Integer.toString(MAX_NAME_LENGTH+1)}),
					MessageFormat.format(rb.getString("CanNotTrack"),new Object[]{s }),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private boolean checkLength (Track track){	
		// convert track length if in inches
		String length = trackLengthTextField.getText();
		if (length.endsWith("\"")){
			length = length.substring(0, length.length()-1);
			try {
				double inches = Double.parseDouble(length);
				int feet = (int)(inches * Setup.getScaleRatio() / 12);
				length = Integer.toString(feet);
			} catch (NumberFormatException e){
				log.error("Can not convert from inches to feet");
				JOptionPane.showMessageDialog(this,
						rb.getString("CanNotConvertFeet"), rb.getString("ErrorTrackLength"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		if (length.endsWith("cm")){
			length = length.substring(0, length.length()-2);
			try {
				double cm = Double.parseDouble(length);
				int meter = (int)(cm * Setup.getScaleRatio() / 100);
				length = Integer.toString(meter);
			} catch (NumberFormatException e){
				log.error("Can not convert from cm to meters");
				JOptionPane.showMessageDialog(this,
						rb.getString("CanNotConvertMeter"), rb.getString("ErrorTrackLength"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		// confirm that length is a number and less than 10000 feet
		int trackLength = 0;
		try {
			trackLength = Integer.parseInt(length);
			if (length.length() > Control.MAX_LEN_STRING_TRACK_LENGTH_NAME){
				log.error("Track length must be less than 100,000 feet");
				JOptionPane.showMessageDialog(this,
						rb.getString("TrackMustBeLessThan"), rb.getString("ErrorTrackLength"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} catch (NumberFormatException e){
			log.error("Track length not an integer");
			JOptionPane.showMessageDialog(this,
					rb.getString("TrackMustBeNumber"), rb.getString("ErrorTrackLength"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// track length can not be less than than the sum of used and reserved length
		if (trackLength < track.getUsedLength() + track.getReserved()){
			log.error("Track length can not be less than used and reserved");
			JOptionPane.showMessageDialog(this,
					rb.getString("TrackMustBeGreater"), rb.getString("ErrorTrackLength"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// if everything is okay, save length
		track.setLength(trackLength);
		return true;
	}
	
	private void reportTrackExists(String s){
		log.info("Can not " + s + ", track already exists");
		JOptionPane.showMessageDialog(this,
				rb.getString("TrackAlreadyExists"), MessageFormat.format(rb.getString("CanNotTrack"),new Object[]{s }),
				JOptionPane.ERROR_MESSAGE);
	}

	protected void enableButtons(boolean enabled){
		northCheckBox.setEnabled(enabled);
		southCheckBox.setEnabled(enabled);
		eastCheckBox.setEnabled(enabled);
		westCheckBox.setEnabled(enabled);
		clearButton.setEnabled(enabled);
		setButton.setEnabled(enabled);
		deleteTrackButton.setEnabled(enabled);
		saveTrackButton.setEnabled(enabled);
		roadNameAll.setEnabled(enabled);
		roadNameInclude.setEnabled(enabled);
		roadNameExclude.setEnabled(enabled);
		loadNameAll.setEnabled(enabled);
		loadNameInclude.setEnabled(enabled);
		loadNameExclude.setEnabled(enabled);
		enableCheckboxes(enabled);
	}
	
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button activated");
		if (ae.getSource() == roadNameAll){
			_track.setRoadOption(Track.ALLROADS);
			updateRoadNames();
		}
		if (ae.getSource() == roadNameInclude){
			_track.setRoadOption(Track.INCLUDEROADS);
			updateRoadNames();
		}
		if (ae.getSource() == roadNameExclude){
			_track.setRoadOption(Track.EXCLUDEROADS);
			updateRoadNames();
		}
		if (ae.getSource() == loadNameAll){
			_track.setLoadOption(Track.ALLLOADS);
			updateLoadNames();
		}
		if (ae.getSource() == loadNameInclude){
			_track.setLoadOption(Track.INCLUDELOADS);
			updateLoadNames();
		}
		if (ae.getSource() == loadNameExclude){
			_track.setLoadOption(Track.EXCLUDELOADS);
			updateLoadNames();
		}
		if (ae.getSource() == orderNormal){
			_track.setServiceOrder(Track.NORMAL);			
		}
		if (ae.getSource() == orderFIFO){
			_track.setServiceOrder(Track.FIFO);			
		}
		if (ae.getSource() == orderLIFO){
			_track.setServiceOrder(Track.LIFO);			
		}
	}
	
	// Car type combo box has been changed, show loads associated with this car type
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		updateLoadComboBoxes();
	}

	private void enableCheckboxes(boolean enable){
		for (int i=0; i < checkBoxes.size(); i++){
			checkBox = checkBoxes.get(i);
			checkBox.setEnabled(enable);
		}
	}
	
	private void selectCheckboxes(boolean enable){
		for (int i=0; i < checkBoxes.size(); i++){
			checkBox = checkBoxes.get(i);
			checkBox.setSelected(enable);
			if(_track != null){
				//_track.removePropertyChangeListener(this);
				if (enable)
					_track.addTypeName(checkBox.getText());
				else
					_track.deleteTypeName(checkBox.getText());
				//_track.addPropertyChangeListener(this);
			}
		}
	}
	
	private void updateLoadComboBoxes(){
		String carType = (String)comboBoxTypes.getSelectedItem();
		CarLoads.instance().updateComboBox(carType, comboBoxLoads);
	}
	
	private void updateCheckboxes(){
		checkBoxes.clear();
		panelCheckBoxes.removeAll();
		x = 0;
		y = 0;		// vertical position in panel
		loadTypes(CarTypes.instance().getNames());
		loadTypes(EngineTypes.instance().getNames());
		enableCheckboxes(_track != null);
		addItem (panelCheckBoxes, clearButton, 1, ++y);
		addItem (panelCheckBoxes, setButton, 4, y);
		panelCheckBoxes.revalidate();
		packFrame();
	}
	
	int x = 0;
	int y = 0;	// vertical position in panel
	private void loadTypes(String[] types){
		for (int i =0; i<types.length; i++){
			if(_location.acceptsTypeName(types[i])){
				JCheckBox checkBox = new JCheckBox();
				checkBoxes.add(checkBox);
				checkBox.setText(types[i]);
				addCheckBoxAction(checkBox);
				addItemLeft(panelCheckBoxes, checkBox, x++, y);
				if(_track != null && _track.acceptsTypeName(types[i]))
					checkBox.setSelected(true);
			} 
			if (x > 5){
				y++;
				x = 0;
			}
		}
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

		if(_track != null){
			// set radio button
			roadNameAll.setSelected(_track.getRoadOption().equals(Track.ALLROADS));
			roadNameInclude.setSelected(_track.getRoadOption().equals(Track.INCLUDEROADS));
			roadNameExclude.setSelected(_track.getRoadOption().equals(Track.EXCLUDEROADS));
			
			if (!roadNameAll.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	p.add(comboBoxRoads);
		    	p.add(addRoadButton);
		    	p.add(deleteRoadButton);
				gc.gridy = y++;
		    	panelRoadNames.add(p, gc);
		    	y++;

		    	String[]carRoads = _track.getRoadNames();
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
		panelRoadNames.repaint();
		panelRoadNames.validate();
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

		if(_track != null){
			// set radio button
			loadNameAll.setSelected(_track.getLoadOption().equals(Track.ALLLOADS));
			loadNameInclude.setSelected(_track.getLoadOption().equals(Track.INCLUDEROADS));
			loadNameExclude.setSelected(_track.getLoadOption().equals(Track.EXCLUDEROADS));
			
			if (!loadNameAll.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	p.add(comboBoxTypes);
		    	p.add(comboBoxLoads);
		    	p.add(addLoadButton);
		    	p.add(deleteLoadButton);
		    	p.add(deleteAllLoadsButton);
				gc.gridy = y++;
		    	panelLoadNames.add(p, gc);

		    	String[]carLoads = _track.getLoadNames();
		    	int x = 0;
		    	for (int i =0; i<carLoads.length; i++){
		    		JLabel load = new JLabel();
		    		load.setText(carLoads[i]);
		    		addItem(panelLoadNames, load, x++, y);
		    		if (x > 5){
		    			y++;
		    			x = 0;
		    		}
		    	}
			}
		} else {
			loadNameAll.setSelected(true);
		}
		panelLoadNames.repaint();
		panelLoadNames.validate();
		packFrame();
	}
	
	private void deleteAllLoads(){
		if(_track != null){
			String [] trackLoads = _track.getLoadNames();
			for (int i=0; i<trackLoads.length; i++){
				_track.deleteLoadName(trackLoads[i]);
			}
		}
		updateLoadNames();
	}
	
	private void updateTrainDir(){
		panelTrainDir.removeAll();

		addItem(panelTrainDir, northCheckBox, 1, 1);
		addItem(panelTrainDir, southCheckBox, 2, 1);
		addItem(panelTrainDir, eastCheckBox, 3, 1);
		addItem(panelTrainDir, westCheckBox, 4, 1);
		
		northCheckBox.setVisible(((Setup.getTrainDirection() & Setup.NORTH) & (_location.getTrainDirections() & Location.NORTH))>0);
		southCheckBox.setVisible(((Setup.getTrainDirection() & Setup.SOUTH) & (_location.getTrainDirections() & Location.SOUTH))>0);
		eastCheckBox.setVisible(((Setup.getTrainDirection() & Setup.EAST) & (_location.getTrainDirections() & Location.EAST))>0);
		westCheckBox.setVisible(((Setup.getTrainDirection() & Setup.WEST) & (_location.getTrainDirections() & Location.WEST))>0);
		
		if (_track != null){
			northCheckBox.setSelected((_track.getTrainDirections() & Track.NORTH) > 0);
			southCheckBox.setSelected((_track.getTrainDirections() & Track.SOUTH) > 0);
			eastCheckBox.setSelected((_track.getTrainDirections() & Track.EAST) > 0);
			westCheckBox.setSelected((_track.getTrainDirections() & Track.WEST) > 0);
		}
		
		panelTrainDir.revalidate();

		packFrame();
	}

	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		JCheckBox b =  (JCheckBox)ae.getSource();
		log.debug("checkbox change "+ b.getText());
		if (_location == null)
			return;
		//_track.removePropertyChangeListener(this);
		if (b.isSelected()){
			_track.addTypeName(b.getText());
		}else{
			_track.deleteTypeName(b.getText());
		}
		//_track.addPropertyChangeListener(this);
	}
	
	private void updateTypeComboBoxes(){
		CarTypes.instance().updateComboBox(comboBoxTypes);
		// remove car types not serviced by this location and track
		for (int i=comboBoxTypes.getItemCount()-1; i>=0; i--){
			String type = (String)comboBoxTypes.getItemAt(i);
			if (_track != null && !_track.acceptsTypeName(type)){
				comboBoxTypes.removeItem(type);
			}			
		}
	}
	
	private void updateRoadComboBox(){
		CarRoads.instance().updateComboBox(comboBoxRoads);
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled()) 
			log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY)){
			updateCheckboxes();
			updateTypeComboBoxes();
		}
		if (e.getPropertyName().equals(Location.TRAINDIRECTION_CHANGED_PROPERTY)){
			updateTrainDir();
		}
		if (e.getPropertyName().equals(CarRoads.CARROADS_LENGTH_CHANGED_PROPERTY) || e.getPropertyName().equals(Train.STATUS_CHANGED_PROPERTY)){
			updateRoadComboBox();
			updateRoadNames();
		}
		if (e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY)){
			updateTypeComboBoxes();
		}
		if (e.getPropertyName().equals(CarLoads.LOAD_NAME_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY)){
			updateLoadComboBoxes();
			updateLoadNames();
		}
	}
	
	// set the service order
	private void updateCarOrder(){	
		orderNormal.setSelected(true);
		if (_track != null){
			if (_track.getServiceOrder().equals(Track.FIFO))
				orderFIFO.setSelected(true);
			if (_track.getServiceOrder().equals(Track.LIFO))
				orderLIFO.setSelected(true);
		}
	}
	
    public void dispose() {
    	if (_track != null)
    		_track.removePropertyChangeListener(this);
    	_location.removePropertyChangeListener(this);
    	CarRoads.instance().removePropertyChangeListener(this);
    	CarLoads.instance().removePropertyChangeListener(this);
    	CarTypes.instance().removePropertyChangeListener(this);
    	ScheduleManager.instance().removePropertyChangeListener(this);
        super.dispose();
    }
    
    private boolean packed = false;
    protected void packFrame(){
    	setPreferredSize(null); 
		validate();
    	if (!packed){
    		pack();
    		// make some room so rolling stock type scroll window doesn't always appear

    		if (getWidth()+50 < Control.panelWidth)
    			setSize (getWidth()+50, getHeight());
    		if (getHeight()< Control.panelMaxHeight){
    			int height = getHeight()+200;
    			if (height>Control.panelMaxHeight)
    				height = Control.panelMaxHeight;
    			setSize (getWidth(), height);
    		}
    	} 
    	packed = true;
    }

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrackEditFrame.class.getName());
}
