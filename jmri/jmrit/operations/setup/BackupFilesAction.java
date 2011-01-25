//BackupFilesAction.java

package jmri.jmrit.operations.setup;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;


/**
 * Swing action to backup operation files to a
 * directory selected by the user.
 * 
 * @author Daniel Boudreau Copyright (C) 2011
 * @version $Revision: 1.1 $
 */
public class BackupFilesAction extends AbstractAction {

    public BackupFilesAction(String s) {
    	super(s);
    }

    public void actionPerformed(ActionEvent e) {
    	backUp();
    }
    
    private void backUp(){
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
			return "Backup Folders";
		}
	}
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(BackupFilesAction.class.getName());
}

/* @(#)BackupFilesAction.java */
