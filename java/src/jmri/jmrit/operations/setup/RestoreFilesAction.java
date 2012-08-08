//RestoreFilesAction.java

package jmri.jmrit.operations.setup;

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

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
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
							"Operations files have been modified, do you want to save them?",
							"Save operation files?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
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
			// } catch (Exception ex) {
			// // Needs to be fixed after autobackup upgraded....
			// log.debug("Autobackup before restore from directory", ex);
			// }
			//
			// try {

			File directory = fc.getSelectedFile();

			backup.restoreFilesFromDirectory(directory);

			JOptionPane.showMessageDialog(null,
					"You must restart JMRI to complete the restore operation",
					"Restore successful!", JOptionPane.INFORMATION_MESSAGE);

			try {
				if (TrainsTableFrame.trainDirtyTask != null) {
					jmri.InstanceManager.shutDownManagerInstance().deregister(
							TrainsTableFrame.trainDirtyTask);
				}
			} catch (IllegalArgumentException e) {
				log.debug(
						"Trying to deregister Train Dirty Task after Operations files restore",
						e);
			}

			Apps.handleRestart();

		} catch (Exception ex) {
			ExceptionContext context = new ExceptionContext(ex,
					"Restore files",
					"Make sure that the backup set files exists and can be read.");
			new ExceptionDisplayFrame(context);
		}
	}

	private static class fileFilter extends javax.swing.filechooser.FileFilter {

		public boolean accept(File f) {
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

}

/* @(#)RestoreFilesAction.java */