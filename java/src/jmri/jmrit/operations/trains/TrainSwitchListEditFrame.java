// TrainSwitchListEditFrame.java

package jmri.jmrit.operations.trains;


import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.ScrollPaneConstants;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

import java.beans.PropertyChangeEvent;


/**
 * Frame for user selection of switch lists
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2012
 * @version $Revision$
 */

public class TrainSwitchListEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	
	JScrollPane switchPane;
	
	// load managers
	LocationManager manager = LocationManager.instance(); 
	List<JCheckBox> locationCheckBoxes = new ArrayList<JCheckBox>();
	List<JComboBox> locationComboBoxes = new ArrayList<JComboBox>();
	JPanel locationPanelCheckBoxes = new JPanel();

	// labels
	JLabel textName = new JLabel(rb.getString("Location"));
	JLabel textStatus = new JLabel(rb.getString("Status"));
	JLabel textComment = new JLabel(rb.getString("Comment"));
	JLabel textPrinter = new JLabel(rb.getString("Printer"));
	JLabel space1 = new JLabel("        ");
	JLabel space2 = new JLabel("        ");
	JLabel space3 = new JLabel("        ");

	// major buttons
	JButton clearButton = new JButton(rb.getString("Clear"));
	JButton setButton = new JButton(rb.getString("Select"));
	JButton printButton = new JButton(rb.getString("PrintSwitchLists"));
	JButton previewButton = new JButton(rb.getString("PreviewSwitchLists"));
	JButton changeButton = new JButton(rb.getString("PrintChanges"));
	JButton csvGenerateButton = new JButton(rb.getString("CsvGenerate"));
	JButton csvChangeButton = new JButton(rb.getString("CsvChanges"));
	JButton updateButton = new JButton(rb.getString("Update"));
	JButton saveButton = new JButton(rb.getString("Save"));

	// text field
	
	// combo boxes

	public TrainSwitchListEditFrame() {
		super();
    	// Set up the jtable in a Scroll Pane..
	}

	public void initComponents() {
		// listen for any changes in the number of locations
		manager.addPropertyChangeListener(this);
		
		// the following code sets the frame's initial state
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
	    
    	switchPane = new JScrollPane(locationPanelCheckBoxes);
    	switchPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	getContentPane().add(switchPane);
		
	    // Layout the panel by rows
	   	locationPanelCheckBoxes.setLayout(new GridBagLayout());
		updateLocationCheckboxes();
		enableChangeButtons();
		
		// row 2
    	JPanel controlpanel = new JPanel();
    	controlpanel.setLayout(new GridBagLayout());
 
    	addItem(controlpanel, clearButton, 0, 1);
    	addItem(controlpanel, setButton, 1, 1);
    	addItem(controlpanel, saveButton, 2, 1);
    	
    	// row 3
    	addItem(controlpanel, previewButton, 0, 2);
    	addItem(controlpanel, printButton, 1, 2);
    	addItem(controlpanel, changeButton, 2, 2);
    	
    	changeButton.setToolTipText(rb.getString("PrintChangesTip"));
    	
    	// row 4
    	if (!Setup.isSwitchListRealTime()){
    		addItem(controlpanel, updateButton, 0, 3);
    	}
    	if (Setup.isGenerateCsvSwitchListEnabled()){
    		addItem(controlpanel, csvGenerateButton, 1, 3);
    		addItem(controlpanel, csvChangeButton, 2, 3);
    		csvChangeButton.setToolTipText(rb.getString("CsvChangesTip"));
    	}
    	
		getContentPane().add(controlpanel);
		
		// setup buttons
		addButtonAction(clearButton);
		addButtonAction(setButton);
       	addButtonAction(printButton);
		addButtonAction(previewButton);
		addButtonAction(changeButton);
		addButtonAction(csvGenerateButton);
		addButtonAction(csvChangeButton);
		addButtonAction(updateButton);
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
			buildSwitchList(true, false, false, false);
		}
		if (ae.getSource() == printButton){
			buildSwitchList(false, false, false, false);
		}
		if (ae.getSource() == changeButton){
			buildSwitchList(false, true, false, false);
		}
		if (ae.getSource() == csvGenerateButton){
			buildSwitchList(false, false, true, false);
		}
		if (ae.getSource() == csvChangeButton){
			buildSwitchList(false, true, true, false);
		}
		if (ae.getSource() == updateButton){
			buildSwitchList(true, false, false, true);
		}
		if (ae.getSource() == saveButton){
			save();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}
	
	// save printer selection
	private void save(){
		for (int i =0; i<locationCheckBoxes.size(); i++){
			String locationName = locationCheckBoxes.get(i).getName();
			Location l = manager.getLocationByName(locationName);
			JComboBox comboBox = locationComboBoxes.get(i);
			String printerName = (String)comboBox.getSelectedItem();
			if (printerName.equals(TrainPrintUtilities.getDefaultPrinterName())){
				l.setDefaultPrinterName("");
			} else {
				log.debug("Location "+l.getName()+" has selected printer "+printerName);
				l.setDefaultPrinterName(printerName);
			}
		}
		// save location file
		OperationsXml.save();
	}
	
	private void buildSwitchList(boolean isPreview, boolean isChanged, boolean isCsv, boolean isUpdate){
		TrainSwitchLists ts = new TrainSwitchLists();
		for (int i =0; i<locationCheckBoxes.size(); i++){
			String locationName = locationCheckBoxes.get(i).getName();
			Location location = manager.getLocationByName(locationName);
			if (location.isSwitchListEnabled()){
				if (!isCsv) {
					ts.buildSwitchList(location);
					if (!isUpdate && !isChanged ||
							(!isUpdate && isChanged && !location.getStatus().equals(Location.PRINTED)))
						ts.printSwitchList(location, isPreview);
					if (!isPreview){
						location.setStatus(Location.PRINTED);
						location.setSwitchListState(Location.SW_PRINTED);
					}
				} else if (Setup.isGenerateCsvSwitchListEnabled()){
					TrainCsvSwitchLists tCSVs = new TrainCsvSwitchLists();
					tCSVs.buildSwitchList(location);
					location.setStatus(Location.CSV_GENERATED);
				}
			}
		}
		// set trains switch lists printed
		TrainManager trainManager = TrainManager.instance();
		List<String> trains = trainManager.getTrainsByTimeList();
		for (int i=0; i<trains.size(); i++){
			Train train = trainManager.getTrainById(trains.get(i));
			if (!train.isBuilt())
				continue;	// train wasn't built so skip
			train.setSwitchListStatus(Train.PRINTED);
		}
		
	}
	
	private void selectCheckboxes(boolean enable){
		for (int i =0; i<locationCheckBoxes.size(); i++){
			String locationName = locationCheckBoxes.get(i).getName();
			Location l = manager.getLocationByName(locationName);
			l.setSwitchListEnabled(enable);
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
		locationComboBoxes.clear();		// remove printer selection
		locationPanelCheckBoxes.removeAll();
		
	    addItem(locationPanelCheckBoxes, textName, 0, 0);
	    addItem(locationPanelCheckBoxes, space1, 1, 0);
	    addItem(locationPanelCheckBoxes, textStatus, 2, 0);
	    addItem(locationPanelCheckBoxes, space2, 3, 0);
	    addItem(locationPanelCheckBoxes, textComment, 4, 0);
	    addItem(locationPanelCheckBoxes, space3, 5, 0);
	    addItem(locationPanelCheckBoxes, textPrinter, 6, 0);
		
		int y = 1;		// vertical position in panel

		String previousName = "";
		
		for (int i =0; i<locations.size(); i++){
			Location l = manager.getLocationById(locations.get(i));
			String name = TrainCommon.splitString(l.getName());
			if (name.equals(previousName))
				continue;
			previousName = name;
			
			JCheckBox checkBox = new JCheckBox();
			locationCheckBoxes.add(checkBox);
			checkBox.setSelected(l.isSwitchListEnabled());
			checkBox.setText(name);
			checkBox.setName(l.getName());
			addLocationCheckBoxAction(checkBox);
			addItemLeft(locationPanelCheckBoxes, checkBox, 0, y);
			
			JLabel status = new JLabel(l.getStatus());
			if (l.getStatus().equals(Location.MODIFIED) && l.isSwitchListEnabled()){
				changeButton.setEnabled(true);
				csvChangeButton.setEnabled(true);
			}
			addItem(locationPanelCheckBoxes, status, 2, y);
			
			JButton button = new JButton(rb.getString("Add"));
			if (!l.getSwitchListComment().equals(""))
				button.setText(rb.getString("Edit"));
			button.setName(l.getName());
			addCommentButtonAction(button);
			addItem(locationPanelCheckBoxes, button, 4, y);
			
			JComboBox comboBox = TrainPrintUtilities.getPrinterJComboBox();
			locationComboBoxes.add(comboBox);
			comboBox.setSelectedItem(l.getDefaultPrinterName());
			addItem(locationPanelCheckBoxes, comboBox, 6, y++);
			
			l.addPropertyChangeListener(this);					
		}

		Border border = BorderFactory.createEtchedBorder();
		locationPanelCheckBoxes.setBorder(border);
		locationPanelCheckBoxes.revalidate();
		pack();
		repaint();
	}
	
	private void enableChangeButtons(){
		changeButton.setEnabled(false);
		csvChangeButton.setEnabled(false);
		List<String> locations = manager.getLocationsByNameList();
		for (int i =0; i<locations.size(); i++){
			Location l = manager.getLocationById(locations.get(i));
			if (l.getStatus().equals(Location.MODIFIED) && l.isSwitchListEnabled()){
				changeButton.setEnabled(true);
				csvChangeButton.setEnabled(true);
			}
		}
	}
	
	// The print switch list for a location has changed
	private void changeLocationCheckboxes(PropertyChangeEvent e){
		Location l = (Location)e.getSource();
		for (int i=0; i<locationCheckBoxes.size(); i++){
			JCheckBox checkBox = locationCheckBoxes.get(i);
			if (checkBox.getName().equals(l.getName())){
				checkBox.setSelected(l.isSwitchListEnabled());
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
		log.debug("checkbox change "+ b.getName());
		Location l = manager.getLocationByName(b.getName());
		l.setSwitchListEnabled(b.isSelected());
	}
	
	private void addCommentButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				commentButtonActionPerformed(e);
			}
		});
	}
	
	public void commentButtonActionPerformed(ActionEvent ae) {
		JButton b =  (JButton)ae.getSource();
		log.debug("button action "+ b.getName());
		Location l = manager.getLocationByName(b.getName());
		new TrainSwitchListCommentFrame(l);
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
		if(e.getPropertyName().equals(Location.SWITCHLIST_CHANGED_PROPERTY)){
			changeLocationCheckboxes(e);
			enableChangeButtons();
		}
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Location.STATUS_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Location.SWITCHLIST_COMMENT_CHANGED_PROPERTY)){
			updateLocationCheckboxes();
			enableChangeButtons();
		}
	}
	
	private static class TrainSwitchListCommentFrame extends OperationsFrame {
		
		// text area
		JTextArea commentTextArea	= new JTextArea(10,90);
		JScrollPane commentScroller = new JScrollPane(commentTextArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Dimension minScrollerDim = new Dimension(1200, 500);
		JButton saveButton = new JButton(rb.getString("Save"));
		
		Location _location;
		
		private TrainSwitchListCommentFrame(Location location) {
			super();
			initComponents(location);
		}
		
		private void initComponents(Location location) {
			_location = location;
			// the following code sets the frame's initial state
		    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		    
	     	JPanel pC = new JPanel();
	    	pC.setBorder(BorderFactory.createTitledBorder(rb.getString("Comment")));
	    	pC.setLayout(new GridBagLayout());
	    	commentScroller.setMinimumSize(minScrollerDim);
			addItem(pC, commentScroller, 1, 0);
			
			commentTextArea.setText(location.getSwitchListComment());
			
		   	JPanel pB = new JPanel();
	    	pB.setLayout(new GridBagLayout());	
	    	addItem(pB, saveButton, 0, 0);
			
			getContentPane().add(pC);
			getContentPane().add(pB);
			
			addButtonAction(saveButton);
			
			pack();
			setTitle(location.getName());
			setVisible(true);
		}
		
		// Buttons 
		public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
			if (ae.getSource() == saveButton){
				_location.setSwitchListComment(commentTextArea.getText());
				// save location file
				OperationsXml.save();
				if (Setup.isCloseWindowOnSaveEnabled())
					super.dispose();
			}				
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrainSwitchListEditFrame.class.getName());
}
