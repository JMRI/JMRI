// NceConsistEngineAction.java

package jmri.jmrit.operations.rollingstock.engines;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrix.nce.ActiveFlag;
import jmri.jmrix.nce.NceUSB;


/**
 * Starts the NceConsistEngine thread
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.3 $
 */


public class NceConsistEngineAction extends AbstractAction {
	
    public NceConsistEngineAction(String actionName, Component frame) {
        super(actionName);
        // only enable if connected to an NCE system
        setEnabled(false);
        // disable if NCE USB selected
        if(ActiveFlag.isActive() && NceUSB.getUsbSystem() == NceUSB.USB_SYSTEM_NONE)
			setEnabled(true);
    }
	
	public void actionPerformed(ActionEvent ae) {
		Thread mb = new NceConsistEngines();
		mb.setName("NceConsistSyncEngines");
		mb.start();
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(NceConsistEngineAction.class.getName());
}
