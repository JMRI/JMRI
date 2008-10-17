// DeleteEngineRosterAction.java

package jmri.jmrit.operations.rollingstock.engines;
import javax.swing.*;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ResourceBundle;

import jmri.util.StringUtil;
import java.util.List;


/**
 * This routine will remove all engines from the operation database.
 * 
 * @author Dan Boudreau Copyright (C) 2007
 * @version $Revision: 1.1 $
 */


public class DeleteEngineRosterAction extends AbstractAction {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");
	
	EngineManager manager = EngineManager.instance();
	
	javax.swing.JLabel textLine = new javax.swing.JLabel();
	javax.swing.JLabel lineNumber = new javax.swing.JLabel();
	
    public DeleteEngineRosterAction(String actionName, Component frame) {
        super(actionName);

    }
	
	public void actionPerformed(ActionEvent ae) {
		if (JOptionPane.showConfirmDialog(null,
				"Are you sure you want to delete all the engines in your roster?", "Delete all engines?",
				JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
			log.debug("removing all engines from roster");
			List engines = manager.getEnginesByNumberList();
			for (int i=0; i<engines.size(); i++){
				Engine engine = manager.getEngineById((String)engines.get(i));
				manager.deregister(engine);
			}
		}
	}




	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(DeleteEngineRosterAction.class.getName());
}
