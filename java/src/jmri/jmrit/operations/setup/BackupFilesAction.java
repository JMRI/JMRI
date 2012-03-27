//BackupFilesAction.java

package jmri.jmrit.operations.setup;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import jmri.jmrit.operations.OperationsXml;

/**
 * Swing action to backup operation files to a
 * directory selected by the user.
 * 
 * @author Daniel Boudreau Copyright (C) 2011
 * @version $Revision$
 */
public class BackupFilesAction extends AbstractAction {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");

    public BackupFilesAction(String s) {
    	super(s);
    }

    public void actionPerformed(ActionEvent e) {
    	backUp();
    }
    
    private void backUp(){
		// check to see if files are dirty
		if (OperationsXml.areFilesDirty()){
			if(JOptionPane.showConfirmDialog(null, rb.getString("OperationsFilesModified"),
					rb.getString("SaveOperationFiles"), JOptionPane.YES_NO_OPTION)== JOptionPane.YES_OPTION) {
				OperationsXml.save();
			}
		}	
        Backup backup = new Backup();
		// get file to write to
		JFileChooser fc = new JFileChooser(backup.getBackupDirectoryName());
		fc.addChoosableFileFilter(new fileFilter());
		
		File fs = new File (backup.getDirectoryName());
		fc.setSelectedFile(fs);
		
		int retVal = fc.showSaveDialog(null);
		if (retVal != JFileChooser.APPROVE_OPTION)
			return; // Canceled
		if (fc.getSelectedFile() == null)
			return; // Canceled

		File directory = fc.getSelectedFile();
		backup.backupFiles(directory);
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
			return rb.getString("BackupFolders");
		}
	}
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(BackupFilesAction.class.getName());
}

/* @(#)BackupFilesAction.java */
