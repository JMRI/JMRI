// TrainsByCarTypeFrame.java

package jmri.jmrit.operations.trains;

import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;

import java.awt.*;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Frame to display which trains service certain car types
 * 
 * @author Dan Boudreau Copyright (C) 2009
 * @version $Revision$
 */

public class TrainsByCarTypeFrame extends OperationsFrame implements java.beans.PropertyChangeListener {
	
	TrainManager manager;
	String Empty = "            ";

	ArrayList<JCheckBox> trainList = new ArrayList<JCheckBox>();
	JPanel trainCheckBoxes = new JPanel();
	
	// panels
	JPanel pTrains;

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

	public TrainsByCarTypeFrame() {
		super();
	}

	public void initComponents(String carType) {

		// load managers
		manager = TrainManager.instance();
		
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

    	pTrains = new JPanel();
    	pTrains.setLayout(new GridBagLayout());
    	JScrollPane trainPane = new JScrollPane(pTrains);
    	trainPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	trainPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Trains")));
    	updateTrains();
    	
    	JPanel pButtons = new JPanel();
    	pButtons.setLayout(new GridBagLayout());
    	pButtons.setBorder(BorderFactory.createEtchedBorder());
    	
    	addItem(pButtons, clearButton, 0, 0);
    	addItem(pButtons, setButton, 1, 0);
    	addItem(pButtons, saveButton, 2, 0);
    	
    	getContentPane().add(pCarType);
    	getContentPane().add(trainPane);
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
		toolMenu.add(new PrintTrainsByCarTypesAction(Bundle.getMessage("MenuItemPrintByType"), new Frame(), false, this));
		toolMenu.add(new PrintTrainsByCarTypesAction(Bundle.getMessage("MenuItemPreviewByType"), new Frame(), true, this));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_ModifyTrainsByCarType", true); // NOI18N

		setPreferredSize(null);
		pack();
		if (getWidth()<300)
			setSize(300, getHeight());
		setTitle(Bundle.getMessage("TitleModifyTrains"));
		setVisible(true);
	}
		
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("combo box action");
		updateTrains();
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
	 * Update the car types that trains and tracks service.
	 * Note that the checkbox name is the id of the train or
	 * track.
	 */
	private void save(){
		log.debug("save "+trainList.size());
		removePropertyChangeTrains();
		for (int i=0; i<trainList.size(); i++){
			JCheckBox cb = trainList.get(i);
			Train train = manager.getTrainById(cb.getName());
			if (cb.isSelected()){
				train.addTypeName((String)typeComboBox.getSelectedItem());
			} else {
				train.deleteTypeName((String)typeComboBox.getSelectedItem());
			}
		}
		OperationsXml.save();	// save files
		updateTrains();
		if (Setup.isCloseWindowOnSaveEnabled())
			dispose();
	}
	
	private void updateTrains(){
		log.debug("update trains");
		removePropertyChangeTrains();
		trainList.clear();
		int x=0;
		pTrains.removeAll();
		String carType = (String)typeComboBox.getSelectedItem();
		if (copyCheckBox.isSelected())
			carType = textCarType.getText();
		List<String> trains = manager.getTrainsByNameList();
		for (int i=0; i<trains.size(); i++){
			Train train = manager.getTrainById(trains.get(i));
			train.addPropertyChangeListener(this);
			JCheckBox cb = new JCheckBox(train.getName());
			cb.setName(train.getId());
			cb.setToolTipText(MessageFormat.format(Bundle.getMessage("TipTrainCarType"),new Object[]{carType}));
			addCheckBoxAction(cb);
			trainList.add(cb);
			boolean trainAcceptsType = train.acceptsTypeName(carType);
			cb.setSelected(trainAcceptsType);
			addItemLeft(pTrains, cb, 0, x);
			JLabel description = new JLabel(train.getDescription());
			addItemLeft(pTrains, description, 1, x++);
		}
		pTrains.revalidate();
		repaint();
	}
	
	private void updateComboBox(){
		log.debug("update combobox");
		CarTypes.instance().updateComboBox(typeComboBox);
	}
	
	private void selectCheckboxes(boolean b){
		for (int i=0; i<trainList.size(); i++){
			JCheckBox cb = trainList.get(i);
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
				updateTrains();
			}
		}else{
			JCheckBox cb = (JCheckBox)ae.getSource();
			log.debug("Checkbox "+cb.getName()+" text: "+cb.getText());
			if (trainList.contains(cb)){
				log.debug("Checkbox train "+cb.getText());
			}else{
				log.error("Error checkbox not found");
			}
		}
	}

	private void removePropertyChangeTrains() {
		if (trainList != null) {
			for (int i = 0; i < trainList.size(); i++) {
				// if object has been deleted, it's not here; ignore it
				Train t = manager.getTrainById(trainList.get(i).getName());
				if (t != null)
					t.removePropertyChangeListener(this);
			}
		}
	}

	public void dispose(){
		manager.removePropertyChangeListener(this);
		CarTypes.instance().removePropertyChangeListener(this);
		removePropertyChangeTrains();
		super.dispose();
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled()) 
			log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Train.TYPES_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Train.NAME_CHANGED_PROPERTY) || 
			e.getPropertyName().equals(Train.DESCRIPTION_CHANGED_PROPERTY))
			updateTrains();
		if (e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(CarTypes.CARTYPES_NAME_CHANGED_PROPERTY))
			updateComboBox();
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainsByCarTypeFrame.class.getName());
}
