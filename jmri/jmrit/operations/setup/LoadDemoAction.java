// RouteCopyAction.java

package jmri.jmrit.operations.setup;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

/**
 * Swing action to create and register a RouteCopyFrame object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision: 1.1 $
 */
public class LoadDemoAction extends AbstractAction {
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");

    public LoadDemoAction(String s) {
    	super(s);
    }

    public void actionPerformed(ActionEvent e) {
    	Backup backup = new Backup();
    	boolean success = backup.backupFiles(backup.getDirectoryName());
    	if(!success){
    		log.error("Could not backup files");
    		return;
    	}
    	success = backup.loadDemoFiles();
    	if(!success)
    		log.error("Could not load demo files");
    	else
			JOptionPane.showMessageDialog(null, "You must restart JMRI to complete the load demo operation",
					"Demo load successful!" ,
					JOptionPane.INFORMATION_MESSAGE);
			
    }
    
	static org.apache.log4j.Category log = org.apache.log4j.Category
	.getInstance(LoadDemoAction.class.getName());
}

/* @(#)RouteCopyAction.java */
