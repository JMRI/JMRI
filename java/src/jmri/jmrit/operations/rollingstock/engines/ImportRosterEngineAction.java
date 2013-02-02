// ImportEngineRosterAction.java

package jmri.jmrit.operations.rollingstock.engines;
import org.apache.log4j.Logger;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


/**
 * Starts the ImportRosterEngines thread
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class ImportRosterEngineAction extends AbstractAction {
	
    public ImportRosterEngineAction(String actionName, Component frame) {
        super(actionName);
    }
	
	public void actionPerformed(ActionEvent ae) {
		Thread mb = new ImportRosterEngines();
		mb.setName("Import Roster Engines"); // NOI18N
		mb.start();
	}

	static Logger log = org.apache.log4j.Logger
	.getLogger(ImportRosterEngineAction.class.getName());
}
