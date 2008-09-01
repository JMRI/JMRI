// DeleteCarRosterAction.java

package jmri.jmrit.operations.cars;
import javax.swing.*;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ResourceBundle;

import jmri.util.StringUtil;
import java.util.List;


/**
 * This routine will remove all cars from the operation database.
 * 
 * @author Dan Boudreau Copyright (C) 2007
 * @version $Revision: 1.1 $
 */


public class DeleteCarRosterAction extends AbstractAction {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.cars.JmritOperationsCarsBundle");
	
	CarManager manager = CarManager.instance();
	
	javax.swing.JLabel textLine = new javax.swing.JLabel();
	javax.swing.JLabel lineNumber = new javax.swing.JLabel();
	
    public DeleteCarRosterAction(String actionName, Component frame) {
        super(actionName);

    }
	
	public void actionPerformed(ActionEvent ae) {
		if (JOptionPane.showConfirmDialog(null,
				"Are you sure you want to delete all the cars in your roster?", "Delete all cars?",
				JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
			log.debug("removing all cars from roster");
			List cars = manager.getCarsByNumberList();
			for (int i=0; i<cars.size(); i++){
				Car car = manager.getCarById((String)cars.get(i));
				manager.deregister(car);
			}
		}
	}




	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(DeleteCarRosterAction.class.getName());
}
