// ExportEngineRosterAction.java

package jmri.jmrit.operations.rollingstock.engines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


/**
 * Starts the ImportEngines thread
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision$
 */


public class ExportEngineRosterAction extends AbstractAction {
	
    public ExportEngineRosterAction(String actionName, Component frame) {
        super(actionName);
    }
	
	public void actionPerformed(ActionEvent ae) {
		ExportEngines ex = new ExportEngines();
		ex.writeOperationsEngineFile();
	}

	static Logger log = LoggerFactory
	.getLogger(ExportEngineRosterAction.class.getName());
}
