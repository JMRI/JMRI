package jmri.jmrit.operations.setup;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import jmri.jmrit.operations.OperationsXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to backup operation files to a directory selected by the user.
 *
 * @author Daniel Boudreau Copyright (C) 2011
 * @author Gregory Madsen Copyright (C) 2012
 */
public class BackupFilesAction extends AbstractAction {

    private final static Logger log = LoggerFactory.getLogger(BackupFilesAction.class);

    public BackupFilesAction(String s) {
        super(s);
    }

    @Override
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
        fc.addChoosableFileFilter(new FileFilter());

        File fs = new File(backup.suggestBackupSetName());
        fc.setSelectedFile(fs);

        int retVal = fc.showSaveDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return; // Canceled
        }
        if (fc.getSelectedFile() == null) {
            return; // Canceled
        }
        File directory = fc.getSelectedFile();

        // Fix this later....... UGH!!
        try {
            backup.backupFilesToDirectory(directory);
        } catch (IOException ex) {
            log.error("backup failed");
        }
    }

    private static class FileFilter extends javax.swing.filechooser.FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String name = f.getName();
            return name.matches(".*\\.xml"); // NOI18N
        }

        @Override
        public String getDescription() {
            return Bundle.getMessage("BackupFolders");
        }
    }

}


