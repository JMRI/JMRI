// EngineAttributeEditFrame.java

 package jmri.jmrit.operations.engines;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JComboBox; 

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.SecondaryLocation;
import jmri.jmrit.operations.cars.CarRoads;
import jmri.jmrit.operations.cars.CarOwners;
import java.util.List;

/**
 * Frame for adding and editing the engine roster for operations.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version             $Revision: 1.3 $
 */
public class EngineAttributeEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener{
	
	final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.engines.JmritOperationsEnginesBundle");
	
	EngineManager manager = EngineManager.instance();
	
	// labels
	javax.swing.JLabel textAttribute = new javax.swing.JLabel();
	javax.swing.JLabel textSep = new javax.swing.JLabel();

	// major buttons
	javax.swing.JButton addButton = new javax.swing.JButton();
	javax.swing.JButton deleteButton = new javax.swing.JButton();
	javax.swing.JButton replaceButton = new javax.swing.JButton();
	
	// combo box
	javax.swing.JComboBox comboBox;
	
	// text box
	javax.swing.JTextField addTextBox = new javax.swing.JTextField(10);

    public EngineAttributeEditFrame() {}
    
    String _comboboxName;		// track which combo box is being edited
    boolean menuActive = false;
    
    public void initComponents(String comboboxName) {
    	
    	getContentPane().removeAll();
     	
        setTitle(rb.getString("TitleEngineAttributeEdit")+" "+ comboboxName);
        
        // track which combo box is being edited 
        _comboboxName = comboboxName;
        loadCombobox();
        
        // general GUI config
        getContentPane().setLayout(new GridBagLayout());
        
        textAttribute.setText(comboboxName);

		addButton.setText(rb.getString("Add"));
		addButton.setVisible(true);
        deleteButton.setText(rb.getString("Delete"));
		deleteButton.setVisible(true);
        replaceButton.setText(rb.getString("Replace"));
        replaceButton.setVisible(true);
        
		// row 1
		addItem(textAttribute,1,1);
		// row 2
		addItem(addTextBox, 1, 2);
        addItem(addButton, 2, 2);
        
        // row 3
        addItem(comboBox, 1, 3);
        addItem(deleteButton, 2, 3);
        
        // row 4 
        addItem(replaceButton, 2, 4);
        
		addButtonAction(addButton);
        addButtonAction(deleteButton);
		addButtonAction(replaceButton);
 
        // add help menu to window
		addHelpMenu("package.jmri.jmrit.operations.Operations_Engines", true);
		
    	pack();
    	if ((getWidth()<150)) 
    		setSize(200, getHeight()+10);
    	else
    		setSize(getWidth()+50, getHeight()+10);
// 		setAlwaysOnTop(true); // this blows up in Java 1.4
    	setVisible(true);
    }
 
	private void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
	}
	
	// add or delete button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("edit frame button actived");
		if (ae.getSource() == addButton){
			String addItem = addTextBox.getText();
			if (addItem.length() > 12){
				JOptionPane.showMessageDialog(this,rb.getString("newEngineText"),
						"Can not add " + _comboboxName,
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			addItemToCombobox (addItem);
		}
		if (ae.getSource() == deleteButton){
			String deleteItem = (String)comboBox.getSelectedItem();
			deleteItemFromCombobox (deleteItem);
		}
		if (ae.getSource() == replaceButton){
			String newItem = addTextBox.getText();
			if (newItem.length() > 12){
				JOptionPane.showMessageDialog(this,rb.getString("newEngineText"),
						"Can not add " + _comboboxName,
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			String oldItem = (String) comboBox.getSelectedItem();
			if (JOptionPane.showConfirmDialog(this,
							"Replace \"" + oldItem + "\" with \"" + newItem + "\"",
							"Replace All?", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				return;
			}
			
			addItemToCombobox (newItem);
			replaceItem(oldItem, newItem);
			deleteItemFromCombobox (oldItem);
		}
	}

	private void deleteItemFromCombobox (String deleteItem){
		if(_comboboxName == EnginesEditFrame.ROAD){
			CarRoads.instance().deleteName(deleteItem);
			roadReplace(deleteItem, null);
		}
		if(_comboboxName == EnginesEditFrame.MODEL){
			EngineModels.instance().deleteName(deleteItem);
		}
		if(_comboboxName == EnginesEditFrame.LENGTH){
			EngineLengths.instance().deleteName(deleteItem);
		}
		if(_comboboxName == EnginesEditFrame.OWNER){
			CarOwners.instance().deleteName(deleteItem);
		}
		if(_comboboxName == EnginesEditFrame.CONSIST){
			manager.deleteConsist(deleteItem);
		}
	}
	
	private void addItemToCombobox (String addItem){
		if(_comboboxName == EnginesEditFrame.ROAD){
			CarRoads.instance().addName(addItem);
		}
		if(_comboboxName == EnginesEditFrame.MODEL){
			EngineModels.instance().addName(addItem);
		}
		if(_comboboxName == EnginesEditFrame.LENGTH){
			// convert from inches to feet if needed
			if (addItem.endsWith("\"")){
				addItem = addItem.substring(0, addItem.length()-1);
				double inches = Double.parseDouble(addItem);
				try {
					int feet = (int)(inches * Setup.getScaleRatio() / 12);
					addItem = Integer.toString(feet);
				} catch (NumberFormatException e){
					log.error("can not convert from inches to feet");
					return;
				}
			}
			// confirm that length is a number and less than 10000 feet
			try {
				int engineLength = Integer.parseInt(addItem);
				if (engineLength > 9999){
					log.error("engine length must be less than 10,000 feet");
					return;
				}
			} catch (NumberFormatException e){
				log.error("length not an integer");
				return;
			}
			EngineLengths.instance().addName(addItem);
		}
		if(_comboboxName == EnginesEditFrame.CONSIST){
			manager.newConsist(addItem);
		}
		if(_comboboxName == EnginesEditFrame.OWNER){
			CarOwners.instance().addName(addItem);
		}
	}
	
	private void replaceItem (String oldItem, String newItem){
		List engines = manager.getEnginesByNumberList();
		for (int i=0; i<engines.size(); i++){
			Engine engine = manager.getEngineById((String)engines.get(i));

			if(_comboboxName == EnginesEditFrame.ROAD){
				if (engine.getRoad().equals(oldItem))
					engine.setRoad(newItem);
			}
			if(_comboboxName == EnginesEditFrame.MODEL){
				if (engine.getModel().equals(oldItem))
					engine.setModel(newItem);
			}
			if(_comboboxName == EnginesEditFrame.LENGTH){
				if (engine.getLength().equals(oldItem))
					engine.setLength(newItem);
			}
			if(_comboboxName == EnginesEditFrame.OWNER){
				if (engine.getOwner().equals(oldItem))
					engine.setOwner(newItem);
			}
			if(_comboboxName == EnginesEditFrame.CONSIST){
				if (engine.getKernelName().equals(oldItem)){
					Consist consist = manager.newConsist(newItem);
					engine.setConsist(consist);
				}
			}
		}
		//		 now adjust locations
		if(_comboboxName == EnginesEditFrame.MODEL){
			LocationManager manager = LocationManager.instance();
			List locs = manager.getLocationsByIdList();
			for (int i=0; i<locs.size(); i++){
				String id = (String)locs.get(i);
				Location loc = manager.getLocationById(id);
				if (loc.acceptsTypeName(oldItem)){
					loc.deleteTypeName(oldItem);
					loc.addTypeName(newItem);
					// now adjust any secondary locations
					List sls = loc.getSecondaryLocationsByNameList(null);
					for (int j=0; j<sls.size(); j++){
						String slId = (String)sls.get(j);
						SecondaryLocation sl = loc.getSecondaryLocationById(slId);
						if (sl.acceptsTypeName(oldItem)){
							sl.deleteTypeName(oldItem);
							sl.addTypeName(newItem);
						}
					}
				}
			}
		}
		if(_comboboxName == EnginesEditFrame.ROAD){
			roadReplace(oldItem, newItem);
		}
	}
	
	private void roadReplace(String oldItem, String newItem){
		LocationManager manager = LocationManager.instance();
		List locs = manager.getLocationsByIdList();
		for (int i=0; i<locs.size(); i++){
			String id = (String)locs.get(i);
			Location loc = manager.getLocationById(id);
			// now adjust any secondary locations
			List sls = loc.getSecondaryLocationsByNameList(null);
			for (int j=0; j<sls.size(); j++){
				String slId = (String)sls.get(j);
				SecondaryLocation sl = loc.getSecondaryLocationById(slId);
				if(sl.containsRoadName(oldItem)){
					sl.deleteRoadName(oldItem);
					if(newItem != null)
						sl.addRoadName(newItem);
				}
			}
		}
	}
	
	private void loadCombobox(){ 
		if(_comboboxName == EnginesEditFrame.ROAD){
			comboBox = CarRoads.instance().getComboBox();
			CarRoads.instance().addPropertyChangeListener(this);
		}
		if(_comboboxName == EnginesEditFrame.MODEL){
			comboBox = EngineModels.instance().getComboBox();
			EngineModels.instance().addPropertyChangeListener(this);
		}
		if(_comboboxName == EnginesEditFrame.LENGTH){
			comboBox = EngineLengths.instance().getComboBox();
			EngineLengths.instance().addPropertyChangeListener(this);
		}
		if(_comboboxName == EnginesEditFrame.OWNER){
			comboBox = CarOwners.instance().getComboBox();
			CarOwners.instance().addPropertyChangeListener(this);
		}
		if(_comboboxName == EnginesEditFrame.CONSIST){
			comboBox = manager.getConsistComboBox();
			manager.addPropertyChangeListener(this);
		}
	}

    public void dispose() {
    	CarRoads.instance().removePropertyChangeListener(this);
    	EngineModels.instance().removePropertyChangeListener(this);
		EngineLengths.instance().removePropertyChangeListener(this);
    	CarOwners.instance().removePropertyChangeListener(this);
    	manager.removePropertyChangeListener(this);
		firePcs ("dispose", null, _comboboxName);
        super.dispose();
    }

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("EnginesAttributeFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());
		if (e.getPropertyName().equals(CarRoads.CARROADS))
			CarRoads.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(EngineModels.ENGINEMODELS))
			EngineModels.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(EngineLengths.ENGINELENGTHS))
			EngineLengths.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(CarOwners.CAROWNERS))
			CarOwners.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(EngineManager.CONSISTLISTLENGTH))
			manager.updateConsistComboBox(comboBox);
	}
	
	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(
			this);

	public synchronized void addPropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	//	note firePropertyChange occurs during frame creation
	private void firePcs(String p, Object old, Object n) {
		log.debug("EngineAttribute firePropertyChange " + p +" " );
		pcs.firePropertyChange(p, old, n);
	}
    
	static org.apache.log4j.Category log = org.apache.log4j.Category
	.getInstance(EngineAttributeEditFrame.class.getName());
}
