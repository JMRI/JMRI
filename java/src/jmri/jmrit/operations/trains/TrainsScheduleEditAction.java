//TrainsScheduleEditAction.java

package jmri.jmrit.operations.trains;


import java.awt.GridBagLayout;
import java.awt.event.*;

import javax.swing.*;

import java.util.ResourceBundle;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;


/**
 * Action to edit timetable
 * @author Daniel Boudreau Copyright (C) 2010
 * @version     $Revision$
 */
public class TrainsScheduleEditAction extends AbstractAction {
		
	protected static final String getString(String key) {
		return ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle")
				.getString(key);
	}
	
	public TrainsScheduleEditAction(){
		super(getString("MenuItemEditSchedule"));
	}
	
	 public void actionPerformed(ActionEvent e) {
		new TrainsScheduleEditFrame();
	 }
	
}

class TrainsScheduleEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {
	
	protected static final String getString(String key) {
		return ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle")
				.getString(key);
	}
	
	// text box
	JTextField addTextBox = new JTextField(Control.max_len_string_attibute);

	// combo box
	JComboBox comboBox;
    
    // major buttons
    JButton addButton = new JButton(getString("Add"));
    JButton deleteButton = new JButton(getString("Delete"));
    JButton replaceButton = new JButton(getString("Replace"));
    
    TrainScheduleManager trainScheduleManager = TrainScheduleManager.instance();
	
    
	public TrainsScheduleEditFrame(){
		super();
		
		// the following code sets the frame's initial state
	    getContentPane().setLayout(new GridBagLayout());
	    
	    trainScheduleManager.addPropertyChangeListener(this);
	    comboBox = trainScheduleManager.getComboBox();
	    
		// row 1

		addItem(addTextBox, 2, 2);
        addItem(addButton, 3, 2);
        
        // row 3
        addItem(comboBox, 2, 3);
        addItem(deleteButton, 3, 3);
        
        // row 4 
        addItem(replaceButton, 3, 4);
        
		addButtonAction(addButton);
        addButtonAction(deleteButton);
		addButtonAction(replaceButton);
		
		setTitle(getString("MenuItemEditSchedule"));
		
		pack();
	   	if ((getWidth()<225)) 
    		setSize(getWidth()+50, getHeight()+10);
		setVisible(true);

	}
	
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == deleteButton && comboBox.getSelectedItem() != null){
			trainScheduleManager.deregister((TrainSchedule)comboBox.getSelectedItem());
		}
		
		// check for valid name
		String s = addTextBox.getText();
		s = s.trim();
		if (s.equals(""))
			return;	// done
		
		if (ae.getSource() == addButton){
			trainScheduleManager.newSchedule(s);
		}
		if (ae.getSource() == replaceButton && comboBox.getSelectedItem() != null){
			TrainSchedule ts = ((TrainSchedule)comboBox.getSelectedItem());
			ts.setName(s);
		}		
	}
	
	public void dispose(){
		trainScheduleManager.removePropertyChangeListener(this);
		super.dispose();
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("TrainsScheduleEditFrame sees propertyChange "+e.getPropertyName()+" old: "+e.getOldValue()+" new: "+e.getNewValue());
		trainScheduleManager.updateComboBox(comboBox);
	}

	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrainsScheduleEditFrame.class.getName());
}
