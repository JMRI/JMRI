// TrainSwitchListEditFrame.java

package jmri.jmrit.operations.trains;


import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.ScrollPaneConstants;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.setup.Control;

import java.beans.PropertyChangeEvent;


/**
 * Frame for user selection of switch lists
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.12 $
 */

public class TrainSwitchListEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	
	JScrollPane trainsPane;
	
	// load managers
	LocationManager manager = LocationManager.instance(); 
	List<JCheckBox> locationCheckBoxes = new ArrayList<JCheckBox>();
	JPanel locationPanelCheckBoxes = new JPanel();

	// labels
	JLabel textName = new JLabel();

	// major buttons
	JButton clearButton = new JButton();
	JButton setButton = new JButton();
	JButton printButton = new JButton();
	JButton previewButton = new JButton();
	JButton saveButton = new JButton();

	// text field
	JTextField trainNameTextField = new JTextField(18);
	JTextField trainDescriptionTextField = new JTextField(30);
	JTextField commentTextField = new JTextField(35);

	// for padding out panel
	JLabel space0 = new JLabel();
	JLabel space1 = new JLabel();
	JLabel space2 = new JLabel();
	JLabel space3 = new JLabel();
	JLabel space4 = new JLabel();
	JLabel space5 = new JLabel();
	
	// combo boxes

	public static final String DISPOSE = "dispose" ;

	public TrainSwitchListEditFrame() {
		super();
    	// Set up the jtable in a Scroll Pane..
	}

	public void initComponents() {
		// listen for any changes in the number of locations
		manager.addPropertyChangeListener(this);
		
		// the following code sets the frame's initial state
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
    	trainsPane = new JScrollPane(locationPanelCheckBoxes);
    	trainsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	getContentPane().add(trainsPane);
		
	    // Layout the panel by rows
	   	locationPanelCheckBoxes.setLayout(new GridBagLayout());
		updateLocationCheckboxes();
		
		// row 1
    	JPanel controlpanel = new JPanel();
    	controlpanel.setLayout(new GridBagLayout());
    	clearButton.setText(rb.getString("Clear"));
    	setButton.setText(rb.getString("Select"));
    	previewButton.setText(rb.getString("PreviewSwitchLists"));
    	printButton.setText(rb.getString("PrintSwitchLists"));
    	saveButton.setText(rb.getString("Save"));
    	addItem(controlpanel, clearButton, 0, 1);
    	addItem(controlpanel, setButton, 1, 1);
    	// row 2
    	addItem(controlpanel, previewButton, 0, 2);
    	addItem(controlpanel, printButton, 1, 2);
    	addItem(controlpanel, saveButton, 2, 2);

		getContentPane().add(controlpanel);
		
		// setup buttons
		addButtonAction(clearButton);
		addButtonAction(setButton);
       	addButtonAction(printButton);
		addButtonAction(previewButton);
		addButtonAction(saveButton);
		
        // add help menu to window
    	addHelpMenu("package.jmri.jmrit.operations.Operations_SwitchList", true);
		// set frame size and train for display
    	pack();
		setTitle(rb.getString("TitleSwitchLists"));
		setVisible(true);
	}
	
	// Buttons 
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == clearButton){
			selectCheckboxes(false);
		}
		if (ae.getSource() == setButton){
			selectCheckboxes(true);
		}
		if (ae.getSource() == previewButton){
			buildSwitchList(true);
		}
		if (ae.getSource() == printButton){
			buildSwitchList(false);
		}
		if (ae.getSource() == saveButton){
			// save location file
			LocationManagerXml.instance().writeOperationsFile();
		}
	}
	
	private void buildSwitchList(boolean isPreview){
		List<String> locations = manager.getLocationsByNameList();
		TrainSwitchLists ts = new TrainSwitchLists();
		for (int i =0; i<locations.size(); i++){
			Location location = manager.getLocationById(locations.get(i));
			if (location.getSwitchList()){
				ts.buildSwitchList(location);
				ts.printSwitchList(location, isPreview);
			}
		}
	}
	
	private void selectCheckboxes(boolean enable){
		List<String> locations = manager.getLocationsByNameList();
		for (int i =0; i<locations.size(); i++){
			Location l = manager.getLocationById(locations.get(i));
			l.setSwitchList(enable);
		}
	}
	
	// name change or number of locations has changed
	private void updateLocationCheckboxes(){
		List<String> locations = manager.getLocationsByNameList();		
		synchronized (this) {
			for (int i =0; i<locations.size(); i++){
				Location l = manager.getLocationById(locations.get(i));
				l.removePropertyChangeListener(this);
			}
		}
		
		locationCheckBoxes.clear();
		locationPanelCheckBoxes.removeAll();
		int y = 0;		// vertical position in panel

		for (int i =0; i<locations.size(); i++){
			Location l = manager.getLocationById(locations.get(i));
			JCheckBox checkBox = new JCheckBox();
			locationCheckBoxes.add(checkBox);
			checkBox.setSelected(l.getSwitchList());
			checkBox.setText(l.getName());
			addLocationCheckBoxAction(checkBox);
			addItemLeft(locationPanelCheckBoxes, checkBox, 0, y++);
			l.addPropertyChangeListener(this);
		}


		Border border = BorderFactory.createEtchedBorder();
		locationPanelCheckBoxes.setBorder(border);
		locationPanelCheckBoxes.revalidate();
		pack();
		repaint();
	}
	
	// The print switch list for a location has changed
	private void changeLocationCheckboxes(PropertyChangeEvent e){
		Location l = (Location)e.getSource();
		for (int i=0; i<locationCheckBoxes.size(); i++){
			JCheckBox checkBox = locationCheckBoxes.get(i);
			if (checkBox.getText().equals(l.getName())){
				checkBox.setSelected(l.getSwitchList());
				break;
			}
		}
	}

	private void addLocationCheckBoxAction(JCheckBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				locationCheckBoxActionPerformed(e);
			}
		});
	}

	public void locationCheckBoxActionPerformed(ActionEvent ae) {
		JCheckBox b =  (JCheckBox)ae.getSource();
		log.debug("checkbox change "+ b.getText());
		Location l = manager.getLocationByName(b.getText());
		l.setSwitchList(b.isSelected());
	}

	public void dispose() {
		manager.removePropertyChangeListener(this);
		List<String> locations = manager.getLocationsByNameList();
		for (int i =0; i<locations.size(); i++){
			Location l = manager.getLocationById(locations.get(i));
			l.removePropertyChangeListener(this);
		}
		super.dispose();
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if(e.getPropertyName().equals(Location.SWITCHLIST_CHANGED_PROPERTY)) 
			changeLocationCheckboxes(e);
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY))
			updateLocationCheckboxes();
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainSwitchListEditFrame.class.getName());
}
