// NceConsistEngineAction.java

package jmri.jmrit.operations.rollingstock.engines;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrix.nce.NceUSB;
import jmri.jmrix.nce.ActiveFlag;
import jmri.jmrix.nce.consist.NceConsistBackup;

import javax.swing.*;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ResourceBundle;

import jmri.util.StringUtil;


/**
 * This routine can synchronize NCE consist with the operation's
 * engines.
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.1 $
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
