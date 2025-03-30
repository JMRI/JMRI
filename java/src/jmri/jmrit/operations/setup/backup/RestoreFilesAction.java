package jmri.jmrit.operations.setup.backup;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.util.swing.*;

/**
 * Swing action to restore operation files from a directory selected by the
 * user.
 *
 * @author Daniel Boudreau Copyright (C) 2011
 * @author Gregory Madsen Copyright (C) 2012
 */
public class RestoreFilesAction extends AbstractAction {

    public RestoreFilesAction() {
        super(Bundle.getMessage("Restore"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        restore();
    }

    private void restore() {
        // This method can restore files from any directory selected by the File
        // Chooser.

        // check to see if files are dirty
        if (OperationsXml.areFilesDirty()) {
            if (JmriJOptionPane
                    .showConfirmDialog(
                            null,
                            Bundle.getMessage("OperationsFilesModified"),
                            Bundle.getMessage("SaveOperationFiles"),
                            JmriJOptionPane.YES_NO_OPTION) == JmriJOptionPane.YES_OPTION) {
                OperationsXml.save();
            }
        }

        // first backup the users data in case they forgot
        BackupBase backup = new DefaultBackup();

        // get file to write to
        JFileChooser fc = new jmri.util.swing.JmriJFileChooser(backup.getBackupRoot());
        fc.addChoosableFileFilter(new FileFilter());
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int retVal = fc.showOpenDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return; // Canceled
        }
        if (fc.getSelectedFile() == null) {
            return; // Canceled
        }
        // now backup files
        AutoBackup autoBackup = new AutoBackup();

        try {
            autoBackup.autoBackup();

            File directory = fc.getSelectedFile();

            // now delete the current operations files in case the restore isn't a full set of files
            backup.deleteOperationsFiles();

            backup.restoreFilesFromDirectory(directory);

            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("YouMustRestartAfterRestore"),
                    Bundle.getMessage("RestoreSuccessful"), JmriJOptionPane.INFORMATION_MESSAGE);

            // now deregister shut down task
            // If Trains window was opened, then task is active
            // otherwise it is normal to not have the task running
            InstanceManager.getDefault(OperationsManager.class).setShutDownTask(null);

            try {
                InstanceManager.getDefault(jmri.ShutDownManager.class).restart();
            } catch (Exception er) {
                log.error("Continuing after error in handleRestart", er);
            }

        } catch (IOException ex) {
            ExceptionContext context = new ExceptionContext(ex,
                    Bundle.getMessage("RestoreDialog.restore.files"),
                    Bundle.getMessage("RestoreDialog.makeSure"));
            ExceptionDisplayFrame.displayExceptionDisplayFrame(null, context);
        }
    }

    private static class FileFilter extends javax.swing.filechooser.FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String name = f.getName();
            if (name.matches(".*\\.xml")) // NOI18N
            {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String getDescription() {
            return Bundle.getMessage("BackupFolders");
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RestoreFilesAction.class);
}
