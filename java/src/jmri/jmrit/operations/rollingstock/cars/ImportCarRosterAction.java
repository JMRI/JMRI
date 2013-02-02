// ImportCarRosterAction.java

package jmri.jmrit.operations.rollingstock.cars;
import org.apache.log4j.Logger;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


/**
 * Starts the ImportCars thread
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision$
 */


public class ImportCarRosterAction extends AbstractAction {
	
    public ImportCarRosterAction(String actionName, Component frame) {
        super(actionName);
    }
	
	public void actionPerformed(ActionEvent ae) {
		Thread mb = new ImportCars();
		mb.setName("Import Cars"); // NOI18N
		mb.start();
	}

	static Logger log = org.apache.log4j.Logger
	.getLogger(ImportCarRosterAction.class.getName());
}
