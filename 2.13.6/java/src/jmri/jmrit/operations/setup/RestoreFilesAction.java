//RestoreFilesAction.java

package jmri.jmrit.operations.setup;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import jmri.jmrit.operations.trains.TrainsTableFrame;
import apps.Apps;


/**
 * Swing action to backup operation files to a
 * directory selected by the user.
 * 
 * @author Daniel Boudreau Copyright (C) 2011
 * @version $Revision$
 */
public class RestoreFilesAction extends AbstractAction {

    public RestoreFilesAction(String s) {
    	super(s);
    }

    public void actionPerformed(ActionEvent e) {
    	restore();
    }
    
    private void restore(){
		// first backup the users data in case they forgot
	   	Backup backup = new Backup();
 
		// get file to write to
		JFileChooser fc = new JFileChooser(backup.getBackupDirectoryName());
		fc.addChoosableFileFilter(new fileFilter());
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int retVal = fc.showOpenDialog(null);
		if (retVal != JFileChooser.APPROVE_OPTION)
			return; // Canceled
		if (fc.getSelectedFile() == null)
			return; // Canceled
		
    	// now backup files
	   	String backupName = backup.createBackupDirectoryName();
    	boolean success = backup.backupFiles(backupName);
    	if(!success){
    		log.error("Could not backup files");
    		return;
    	}

		File directory = fc.getSelectedFile();
		success = backup.restore(directory);
		
		if (success){
			JOptionPane.showMessageDialog(null, "You must restart JMRI to complete the restore operation",
					"Restore successful!" ,
					JOptionPane.INFORMATION_MESSAGE);
	    	// now clear dirty bit
			try {
				jmri.InstanceManager.shutDownManagerInstance().deregister(TrainsTableFrame.trainDirtyTask);
			} catch (IllegalArgumentException e){
				
			}
			Apps.handleRestart();
		} else {
			JOptionPane.showMessageDialog(null, "Could not restore operation files",
					"Restore failed!" ,
					JOptionPane.ERROR_MESSAGE);
		}
    }
    
	private static class fileFilter extends javax.swing.filechooser.FileFilter {
		
		public boolean accept(File f){
			if (f.isDirectory())
				return true;
			String name = f.getName();
			if (name.matches(".*\\.xml"))
				return true;
			else
				return false;
		}
		
		public String getDescription() {
			return "Backup Folders";
		}
	}
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(RestoreFilesAction.class.getName());
}

/* @(#)RestoreFilesAction.java */
