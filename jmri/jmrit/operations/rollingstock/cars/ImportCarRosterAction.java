// ImportCarRosterAction.java

package jmri.jmrit.operations.rollingstock.cars;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


/**
 * Starts the ImportCars thread
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.3 $
 */


public class ImportCarRosterAction extends AbstractAction {
	
    public ImportCarRosterAction(String actionName, Component frame) {
        super(actionName);
    }
	
	public void actionPerformed(ActionEvent ae) {
		Thread mb = new ImportCars();
		mb.setName("ImportCars");
		mb.start();
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(ImportCarRosterAction.class.getName());
}
