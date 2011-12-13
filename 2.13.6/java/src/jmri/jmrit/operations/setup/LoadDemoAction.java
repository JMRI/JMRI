// LoadDemoAction.java

package jmri.jmrit.operations.setup;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import apps.Apps;

/**
 * Swing action to load the operation demo files.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class LoadDemoAction extends AbstractAction {
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");

    public LoadDemoAction(String s) {
    	super(s);
    }

    public void actionPerformed(ActionEvent e) {
    	Backup backup = new Backup();
    	String backupName = backup.createBackupDirectoryName();
    	// now backup files
    	boolean success = backup.backupFiles(backupName);
    	if(!success){
    		log.error("Could not backup files");
    		return;
    	}
    	success = backup.loadDemoFiles();
    	if(!success)
    		log.error("Could not load demo files");
    	else {
			JOptionPane.showMessageDialog(null, "You must restart JMRI to complete the load demo operation",
					"Demo load successful!" ,
					JOptionPane.INFORMATION_MESSAGE);
			Apps.handleRestart();
    	}
			
    }
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(LoadDemoAction.class.getName());
}

/* @(#)LoadDemoAction.java */
