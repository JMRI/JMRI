// ImportCarRosterAction.java

package jmri.jmrit.operations.rollingstock.cars;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrix.nce.consist.NceConsistBackup;

import javax.swing.*;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ResourceBundle;

import jmri.util.StringUtil;


/**
 * Starts the ImportCars thread
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.2 $
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
