// CarLoadEditFrame.java

 package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.ScheduleManager;



/**
 * Frame for adding and editing the car roster for operations.
 *
 * @author Daniel Boudreau Copyright (C) 2009
 * @version             $Revision: 1.1 $
 */
public class CarLoadEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener{
	
	final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	// labels
	JLabel textLoad = new JLabel();
	JLabel textSep = new JLabel();

	// major buttons
	JButton addButton = new JButton();
	JButton deleteButton = new JButton();
	JButton replaceButton = new JButton();
	
	// combo box
	JComboBox comboBox;
	
	// text box
	JTextField addTextBox = new JTextField(10);
	
    public CarLoadEditFrame() {}
    
    String _type;
    boolean menuActive = false;
    
    public void initComponents(String type) {
    	
    	getContentPane().removeAll();
     	
        setTitle(MessageFormat.format(rb.getString("TitleCarEditLoad"),new Object[]{type}));
        
        // track which combo box is being edited 
        _type = type;
        loadCombobox();
        
        // general GUI config
        getContentPane().setLayout(new GridBagLayout());
        
        textLoad.setText(rb.getString("Load"));

		addButton.setText(rb.getString("Add"));
		addButton.setVisible(true);
        deleteButton.setText(rb.getString("Delete"));
		deleteButton.setVisible(true);
        replaceButton.setText(rb.getString("Replace"));
        replaceButton.setVisible(true);
        
		// row 1
		addItem(textLoad,1,1);
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
		addHelpMenu("package.jmri.jmrit.operations.Operations_Cars", true);
		
    	pack();
    	if ((getWidth()<150)) 
    		setSize(200, getHeight()+10);
    	else
    		setSize(getWidth()+50, getHeight()+10);
// 		setAlwaysOnTop(true); // this blows up in Java 1.4
    	setVisible(true);
    }
 
	// add or delete button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == addButton){
			String addItem = addTextBox.getText();
			if (addItem.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
				JOptionPane.showMessageDialog(this, MessageFormat.format(rb.getString("carAttribute"),new Object[]{Control.MAX_LEN_STRING_ATTRIBUTE}),
						MessageFormat.format(rb.getString("canNotAdd"),new Object[]{rb.getString("Load")}),
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
			if (newItem.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
				JOptionPane.showMessageDialog(this, MessageFormat.format(rb.getString("carAttribute"),new Object[]{Control.MAX_LEN_STRING_ATTRIBUTE}),
						MessageFormat.format(rb.getString("canNotReplace"),new Object[]{rb.getString("Load")}),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			String oldItem = (String) comboBox.getSelectedItem();
			if (JOptionPane.showConfirmDialog(this,
						MessageFormat.format(rb.getString("replaceMsg"),new Object[]{oldItem, newItem}),
						rb.getString("replaceAll"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				return;
			}	
			addItemToCombobox (newItem);
			replaceItem(oldItem, newItem);
			deleteItemFromCombobox (oldItem);
		}
	}

	private void deleteItemFromCombobox (String name){
		CarLoads.instance().deleteName(_type, name);
	}
	
	private void addItemToCombobox (String name){
		CarLoads.instance().addName(_type, name);
	}
	
	private void replaceItem(String oldItem, String newItem) {
		CarManager manager = CarManager.instance();
		List<String> cars = manager.getCarsByNumberList();
		for (int i = 0; i < cars.size(); i++) {
			Car car = manager.getCarById(cars.get(i));
			if (car.getType().equals(_type) && car.getLoad().equals(oldItem))
				car.setLoad(newItem);
		}
		//	now adjust schedules
		ScheduleManager.instance().replaceLoad(_type, oldItem, newItem);
	}
	
	private void loadCombobox(){ 
		comboBox = CarLoads.instance().getComboBox(_type);
		CarLoads.instance().addPropertyChangeListener(this);
	}

    public void dispose() {
    	CarLoads.instance().removePropertyChangeListener(this);
        super.dispose();
    }
    
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("CarsLoadEditFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());
		if (e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY))
			CarLoads.instance().updateComboBox(_type, comboBox);
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
    
	static org.apache.log4j.Category log = org.apache.log4j.Category
	.getInstance(CarLoadEditFrame.class.getName());
}
