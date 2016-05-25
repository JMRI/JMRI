// LocationsByCarTypeFrame.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;

import java.awt.*;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Frame to display which locations service certain car types
 * 
 * @author Dan Boudreau Copyright (C) 2009, 2011
 * @version $Revision$
 */

public class LocationsByCarTypeFrame extends OperationsFrame implements java.beans.PropertyChangeListener {
	
	LocationManager manager;
	String Empty = "            ";

	ArrayList<JCheckBox> locationList = new ArrayList<JCheckBox>();
	ArrayList<JCheckBox> trackList = new ArrayList<JCheckBox>();
	JPanel locationCheckBoxes = new JPanel();
	
	// panels
	JPanel pLocations;

	// major buttons
	JButton clearButton = new JButton(Bundle.getMessage("Clear"));
	JButton setButton = new JButton(Bundle.getMessage("Select"));
	JButton saveButton = new JButton(Bundle.getMessage("Save"));
	
	// check boxes
	JCheckBox copyCheckBox = new JCheckBox(Bundle.getMessage("Copy"));
	
	// radio buttons
        
	// text field
	JLabel textCarType = new JLabel(Empty);

	// for padding out panel
	
	// combo boxes
	JComboBox typeComboBox = CarTypes.instance().getComboBox();
	
	// selected location
	Location location;

	public LocationsByCarTypeFrame() {
		super();
	}
	
	public void initComponents(){
		initComponents("");
	}
	
	public void initComponents(Location location){
		this.location = location;
		initComponents("");
	}
	
	public void initComponents(Location location, String carType){
		this.location = location;
		initComponents(carType);
	}

	public void initComponents(String carType) {

		// load managers
		manager = LocationManager.instance();
		
		// general GUI config
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		
	    //      Set up the panels
    	JPanel pCarType = new JPanel();
    	pCarType.setLayout(new GridBagLayout());
    	pCarType.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Type")));
    	
    	addItem(pCarType, typeComboBox, 0,0);
    	addItem(pCarType, copyCheckBox, 1,0);
    	addItem(pCarType, textCarType, 2,0);
    	typeComboBox.setSelectedItem(carType);
    	copyCheckBox.setToolTipText(Bundle.getMessage("TipCopyCarType"));

    	pLocations = new JPanel();
    	pLocations.setLayout(new GridBagLayout());
    	JScrollPane locationPane = new JScrollPane(pLocations);
    	locationPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	locationPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Locations")));
    	updateLocations();
    	
    	JPanel pButtons = new JPanel();
    	pButtons.setLayout(new GridBagLayout());
    	pButtons.setBorder(BorderFactory.createTitledBorder(""));
    	
    	addItem(pButtons, clearButton, 0, 0);
    	addItem(pButtons, setButton, 1, 0);
    	addItem(pButtons, saveButton, 2, 0);
    	
    	getContentPane().add(pCarType);
    	getContentPane().add(locationPane);
    	getContentPane().add(pButtons);
    	
		// setup combo box
		addComboBoxAction(typeComboBox);
		
    	// setup buttons
		addButtonAction(setButton);
		addButtonAction(clearButton);
		addButtonAction(saveButton);
		
		// setup checkbox
		addCheckBoxAction(copyCheckBox);
		
		manager.addPropertyChangeListener(this);
		CarTypes.instance().addPropertyChangeListener(this);
		
		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(Bundle.getMessage("Tools"));
		toolMenu.add(new PrintLocationsByCarTypesAction(Bundle.getMessage("MenuItemPrintByType"), new Frame(), false, this));
		toolMenu.add(new PrintLocationsByCarTypesAction(Bundle.getMessage("MenuItemPreviewByType"), new Frame(), true, this));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_ModifyLocationsByCarType", true);	// NOI18N

		setPreferredSize(null);	// we need to resize this frame
		pack();
		setMinimumSize(new Dimension(Control.smallPanelWidth, Control.minPanelHeight));
		if (location != null)
			setTitle(Bundle.getMessage("TitleModifyLocation"));
		else
			setTitle(Bundle.getMessage("TitleModifyLocations"));
		setVisible(true);
	}
		
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("combo box action");
		updateLocations();
	}
	
	// Save, Delete, Add 
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton)
			save();
		if (ae.getSource() == setButton)
			selectCheckboxes(true);
		if (ae.getSource() == clearButton)
			selectCheckboxes(false);
	}
	
	/**
	 * Update the car types that locations and tracks service.
	 * Note that the checkbox name is the id of the location or
	 * track.
	 */
	private void save(){
		log.debug("save "+locationList.size());
		removePropertyChangeLocations();
		for (int i=0; i<locationList.size(); i++){
			JCheckBox cb = locationList.get(i);
			Location loc = manager.getLocationById(cb.getName());
			if (cb.isSelected()){
				loc.addTypeName((String)typeComboBox.getSelectedItem());
				// save tracks that have the same id as the location
				for (int j=0; j<trackList.size(); j++){
					cb = trackList.get(j);
					String[] id = cb.getName().split("s");
					if (loc.getId().equals(id[0])){
						Track track = loc.getTrackById(cb.getName());
						if (cb.isSelected()){
							track.addTypeName((String)typeComboBox.getSelectedItem());
						} else {
							track.deleteTypeName((String)typeComboBox.getSelectedItem());
						}
					}
				}
			} else {
				loc.deleteTypeName((String)typeComboBox.getSelectedItem());
			}
		}
		OperationsXml.save();
		updateLocations();
		if (Setup.isCloseWindowOnSaveEnabled())
			dispose();
	}
	
	private void updateLocations(){
		log.debug("update checkboxes");
		removePropertyChangeLocations();
		locationList.clear();
		trackList.clear();
		int x=0;
		pLocations.removeAll();
		String carType = (String)typeComboBox.getSelectedItem();
		if (copyCheckBox.isSelected())
			carType = textCarType.getText();
		List<String> locations = manager.getLocationsByNameList();
		for (int i=0; i<locations.size(); i++){
			Location loc = manager.getLocationById(locations.get(i));
			// show only one location?
			if (location != null && location != loc)
				continue;
			loc.addPropertyChangeListener(this);
			JCheckBox cb = new JCheckBox(loc.getName());
			cb.setName(loc.getId());
			cb.setToolTipText(MessageFormat.format(Bundle.getMessage("TipLocCarType"),new Object[]{carType}));
			addCheckBoxAction(cb);
			locationList.add(cb);
			boolean locAcceptsType = loc.acceptsTypeName(carType);
			cb.setSelected(locAcceptsType);
			addItemLeft(pLocations, cb, 0, x++);
			List<String> tracks = loc.getTrackIdsByNameList(null);
			for (int j=0; j<tracks.size(); j++){
				Track track = loc.getTrackById(tracks.get(j));
				track.addPropertyChangeListener(this);
				cb = new JCheckBox(track.getName());
				cb.setName(track.getId());
				cb.setToolTipText(MessageFormat.format(Bundle.getMessage("TipTrackCarType"),new Object[]{carType}));
				addCheckBoxAction(cb);
				trackList.add(cb);
				cb.setSelected(track.acceptsTypeName(carType));
				addItemLeft(pLocations, cb, 1, x++);
			}
		}
		pLocations.revalidate();
		repaint();
	}
	
	private void updateComboBox(){
		log.debug("update combobox");
		CarTypes.instance().updateComboBox(typeComboBox);
	}
	
	private void selectCheckboxes(boolean b){
		for (int i=0; i<locationList.size(); i++){
			JCheckBox cb = locationList.get(i);
			cb.setSelected(b);
		}
		for (int i=0; i<trackList.size(); i++){
			JCheckBox cb = trackList.get(i);
			cb.setSelected(b);
		}
	}
	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		// copy checkbox?
		if (ae.getSource() == copyCheckBox){
			if (copyCheckBox.isSelected()){
				textCarType.setText((String)typeComboBox.getSelectedItem());
			}else{
				textCarType.setText(Empty);
				updateLocations();
			}
		}else{
			JCheckBox cb = (JCheckBox)ae.getSource();
			log.debug("Checkbox "+cb.getName()+" text: "+cb.getText());
			if (locationList.contains(cb)){
				log.debug("Checkbox location "+cb.getText());
				// must deselect tracks if location is deselect
				if (!cb.isSelected()){
					String locId = cb.getName();
					for (int i=0; i<trackList.size(); i++){
						cb = trackList.get(i);
						String[] id = cb.getName().split("s");
						if (locId.equals(id[0])){
							cb.setSelected(false);
						}				
					}
				}

			}else if (trackList.contains(cb)){
				log.debug("Checkbox track "+cb.getText());
				// Must select location if track is selected
				if (cb.isSelected()){
					String[] loc = cb.getName().split("s");
					for (int i=0; i<locationList.size(); i++){
						cb = locationList.get(i);
						if (cb.getName().equals(loc[0])){
							cb.setSelected(true);
							break;
						}				
					}
				}
			}else{
				log.error("Error checkbox not found");
			}
		}
	}

	private void removePropertyChangeLocations() {
		if (locationList != null) {
			for (int i = 0; i < locationList.size(); i++) {
				// if object has been deleted, it's not here; ignore it
				Location loc = manager.getLocationById(locationList.get(i).getName());
				if (loc != null){
					loc.removePropertyChangeListener(this);
					List<String> tracks = loc.getTrackIdsByNameList(null);
					for (int j=0; j<tracks.size(); j++){
						Track track = loc.getTrackById(tracks.get(j));
						track.removePropertyChangeListener(this);
					}
				}
			}
		}
	}

	public void dispose(){
		manager.removePropertyChangeListener(this);
		CarTypes.instance().removePropertyChangeListener(this);
		removePropertyChangeLocations();
		super.dispose();
	}
	
 	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled()) 
			log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Location.TRACK_LISTLENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Track.NAME_CHANGED_PROPERTY))
			updateLocations();
		if (e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY))
			updateComboBox();
 	}

	static Logger log = LoggerFactory
	.getLogger(LocationsByCarTypeFrame.class.getName());
}
