package jmri.jmrit.operations.setup;

import apps.Apps;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.util.swing.ExceptionContext;
import jmri.util.swing.ExceptionDisplayFrame;

/**
 * Swing action to backup operation files to a directory selected by the user.
 *
 * @author Daniel Boudreau Copyright (C) 2011
 * @author Gregory Madsen Copyright (C) 2012
 */
public class RestoreFilesAction extends AbstractAction {

//    private final static Logger log = LoggerFactory.getLogger(RestoreFilesAction.class);

    public RestoreFilesAction(String s) {
        super(s);
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

            JOptionPane.showMessageDialog(null,
                    Bundle.getMessage("YouMustRestartAfterRestore"),
                    Bundle.getMessage("RestoreSuccessful"), JOptionPane.INFORMATION_MESSAGE);

            // now deregister shut down task
            // If Trains window was opened, then task is active
            // otherwise it is normal to not have the task running
            InstanceManager.getDefault(OperationsManager.class).setShutDownTask(null);

            Apps.handleRestart();

        } catch (IOException ex) {
            ExceptionContext context = new ExceptionContext(ex,
                    Bundle.getMessage("RestoreDialog.restore.files"),
                    Bundle.getMessage("RestoreDialog.makeSure"));
            new ExceptionDisplayFrame(context, null).setVisible(true);
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

}


