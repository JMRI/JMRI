// ResetAction.java

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
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class ResetAction extends AbstractAction {
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");

    public ResetAction(String s) {
    	super(s);
    }

    public void actionPerformed(ActionEvent e) {
       	int results = JOptionPane.showConfirmDialog(null, 
       			rb.getString("AreYouSureDeleteAll"),
       			rb.getString("ResetOperations") ,
    			JOptionPane.OK_CANCEL_OPTION);
       	if (results != JOptionPane.OK_OPTION)
       		return;
    	Backup backup = new Backup();
    	String backupName = backup.createBackupDirectoryName();
    	// now backup files
    	boolean success = backup.backupFiles(backupName);
    	if(!success){
    		log.error("Could not backup operation files");
    		return;
    	}
    	// now delete the operations files
    	backup.reset();

    	JOptionPane.showMessageDialog(null, rb.getString("YouMustRestartAfterReset"),
    			rb.getString("ResetSuccessful") ,
    			JOptionPane.INFORMATION_MESSAGE);
    	Apps.handleRestart();		
    }
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(ResetAction.class.getName());
}

/* @(#)ResetAction.java */
