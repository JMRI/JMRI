// ExportCarRosterAction.java

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


public class ExportCarRosterAction extends AbstractAction {
	
    public ExportCarRosterAction(String actionName, Component frame) {
        super(actionName);
    }
	
	public void actionPerformed(ActionEvent ae) {
		ExportCars ex = new ExportCars();
		ex.writeOperationsCarFile();
	}

	static Logger log = org.apache.log4j.Logger
	.getLogger(ExportCarRosterAction.class.getName());
}
