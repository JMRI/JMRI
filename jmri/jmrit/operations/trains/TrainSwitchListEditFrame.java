// TrainSwitchListEditFrame.java

package jmri.jmrit.operations.trains;


import java.awt.*;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.Border;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.OperationsFrame;


/**
 * Frame for user selection of switch lists
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.2 $
 */

public class TrainSwitchListEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	
	JScrollPane trainsPane;
	
	// load managers
	LocationManager manager = LocationManager.instance(); 
	List locationCheckBoxes = new ArrayList();
	JPanel locationPanelCheckBoxes = new JPanel();

	// labels
	javax.swing.JLabel textName = new javax.swing.JLabel();

	// major buttons
	javax.swing.JButton clearButton = new javax.swing.JButton();
	javax.swing.JButton setButton = new javax.swing.JButton();
	javax.swing.JButton printButton = new javax.swing.JButton();
	javax.swing.JButton previewButton = new javax.swing.JButton();
	javax.swing.JButton saveButton = new javax.swing.JButton();

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
    	trainsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
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
	}
	
	private void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
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
			LocationManagerXml.instance().writeOperationsLocationFile();
		}
	}
	
	private void buildSwitchList(boolean isPreview){
		List locations = manager.getLocationsByNameList();
		TrainSwitchLists ts = new TrainSwitchLists();
		for (int i =0; i<locations.size(); i++){
			Location location = manager.getLocationById((String)locations.get(i));
			if (location.getSwitchList()){
				ts.buildSwitchList(location);
				ts.printSwitchList(location, isPreview);
			}
		}
	}
	
	private void selectCheckboxes(boolean enable){
		List locations = manager.getLocationsByNameList();
		for (int i =0; i<locations.size(); i++){
			Location l = manager.getLocationById((String)locations.get(i));
			l.setSwitchList(enable);
		}
	}
	
	private void updateLocationCheckboxes(){
		List locations = manager.getLocationsByNameList();
		for (int i =0; i<locations.size(); i++){
			Location l = manager.getLocationById((String)locations.get(i));
			l.removePropertyChangeListener(this);
		}
		locationCheckBoxes.clear();
		locationPanelCheckBoxes.removeAll();
		int y = 0;		// vertical position in panel
		
		for (int i =0; i<locations.size(); i++){
			Location l = manager.getLocationById((String)locations.get(i));
			JCheckBox checkBox = new javax.swing.JCheckBox();
			locationCheckBoxes.add(checkBox);
			checkBox.setSelected(l.getSwitchList());
			checkBox.setText(l.toString());
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
		Location l = manager.getLocationByName(b.getText());
		l.setSwitchList(b.isSelected());
	}

	public void dispose() {
		manager.removePropertyChangeListener(this);
		List locations = manager.getLocationsByNameList();
		for (int i =0; i<locations.size(); i++){
			Location l = manager.getLocationById((String)locations.get(i));
			l.removePropertyChangeListener(this);
		}
		super.dispose();
	}

 	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if(e.getPropertyName().equals(Location.SWITCHLIST) || e.getPropertyName().equals(LocationManager.LISTLENGTH))
			updateLocationCheckboxes();
	}
 	
	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(TrainSwitchListEditFrame.class.getName());
}
