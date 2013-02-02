//BackupFilesAction.java

package jmri.jmrit.operations.setup;

import org.apache.log4j.Logger;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import jmri.jmrit.operations.OperationsXml;

/**
 * Swing action to backup operation files to a directory selected by the user.
 * 
 * @author Daniel Boudreau Copyright (C) 2011
 * @author Gregory Madsen Copyright (C) 2012
 * @version $Revision$
 */
public class BackupFilesAction extends AbstractAction {

	static Logger log = org.apache.log4j.Logger
			.getLogger(BackupFilesAction.class.getName());

	public BackupFilesAction(String s) {
		super(s);
	}

	public void actionPerformed(ActionEvent e) {
		backUp();
	}

	private void backUp() {
		// check to see if files are dirty
		if (OperationsXml.areFilesDirty()) {
			if (JOptionPane.showConfirmDialog(null,
					Bundle.getMessage("OperationsFilesModified"),
					Bundle.getMessage("SaveOperationFiles"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				OperationsXml.save();
			}
		}
		BackupBase backup = new DefaultBackup();

		// get directory to write to
		JFileChooser fc = new JFileChooser(backup.getBackupRoot());
		fc.addChoosableFileFilter(new fileFilter());

		File fs = new File(backup.suggestBackupSetName());
		fc.setSelectedFile(fs);

		int retVal = fc.showSaveDialog(null);
		if (retVal != JFileChooser.APPROVE_OPTION)
			return; // Canceled
		if (fc.getSelectedFile() == null)
			return; // Canceled

		File directory = fc.getSelectedFile();

		// Fix this later....... UGH!!
		try {
			backup.backupFilesToDirectory(directory);
		} catch (Exception ex) {
		}
	}

	private static class fileFilter extends javax.swing.filechooser.FileFilter {

		public boolean accept(File f) {
			if (f.isDirectory())
				return true;
			String name = f.getName();
			if (name.matches(".*\\.xml")) // NOI18N
				return true;
			else
				return false;
		}

		public String getDescription() {
			return Bundle.getMessage("BackupFolders");
		}
	}

}

/* @(#)BackupFilesAction.java */
