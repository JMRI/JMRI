//RestoreFilesAction.java

package jmri.jmrit.operations.setup;

import org.apache.log4j.Logger;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import jmri.jmrit.operations.ExceptionContext;
import jmri.jmrit.operations.ExceptionDisplayFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.trains.TrainsTableFrame;
import apps.Apps;

/**
 * Swing action to backup operation files to a directory selected by the user.
 * 
 * @author Daniel Boudreau Copyright (C) 2011
 * @author Gregory Madsen Copyright (C) 2012
 * @version $Revision$
 */
public class RestoreFilesAction extends AbstractAction {

	static Logger log = org.apache.log4j.Logger
			.getLogger(RestoreFilesAction.class.getName());

	public RestoreFilesAction(String s) {
		super(s);
	}

	public void actionPerformed(ActionEvent e) {
		restore();
	}

	private void restore() {
		// This method can restore files from any directory selected by the File
		// Chooser.

		// check to see if files are dirty
		if (OperationsXml.areFilesDirty()) {
			if (JOptionPane
					.showConfirmDialog(
							null,
							Bundle.getMessage("OperationsFilesModified"),
							Bundle.getMessage("SaveOperationFiles"),
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				OperationsXml.save();
			}
		}

		// first backup the users data in case they forgot
		BackupBase backup = new DefaultBackup();

		// get file to write to
		JFileChooser fc = new JFileChooser(backup.getBackupRoot());
		fc.addChoosableFileFilter(new fileFilter());
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int retVal = fc.showOpenDialog(null);
		if (retVal != JFileChooser.APPROVE_OPTION)
			return; // Canceled
		if (fc.getSelectedFile() == null)
			return; // Canceled

		// now backup files
		AutoBackup autoBackup = new AutoBackup();

		try {
			autoBackup.autoBackup();

			File directory = fc.getSelectedFile();
			
			// now delete the current operations files in case the restore isn't a full set of files
			backup.deleteOperationsFiles();

			backup.restoreFilesFromDirectory(directory);

			JOptionPane.showMessageDialog(null,
					Bundle.getMessage("YouMustRestartAfterRestore"),
					Bundle.getMessage("RestoreSuccessful"), JOptionPane.INFORMATION_MESSAGE);

			// now deregister shut down task
			// If Trains window was opened, then task is active
			// otherwise it is normal to not have the task running
			try {
				if (TrainsTableFrame.trainDirtyTask != null) {
					jmri.InstanceManager.shutDownManagerInstance().deregister(
							TrainsTableFrame.trainDirtyTask);
				}
			} catch (IllegalArgumentException e) {
				log.debug("Trying to deregister Train Dirty Task after Operations files restore");
			}

			Apps.handleRestart();

		} catch (Exception ex) {
			ExceptionContext context = new ExceptionContext(ex,
					Bundle.getMessage("RestoreDialog.restore.files"),
					Bundle.getMessage("RestoreDialog.makeSure"));
			new ExceptionDisplayFrame(context);
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

/* @(#)RestoreFilesAction.java */
