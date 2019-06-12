package jmri.configurexml;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ResourceBundle;
import javax.annotation.CheckForNull;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store the JMRI configuration information as XML.
 * <p>
 * Note that this does not store preferences, tools or user information in the
 * file. This is not a complete store! See {@link jmri.ConfigureManager} for
 * information on the various types of information stored in configuration
 * files.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @see jmri.jmrit.XmlFile
 */
public class StoreXmlConfigAction extends LoadStoreBaseAction {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

    public StoreXmlConfigAction() {
        this("Store configuration...");
    }

    public StoreXmlConfigAction(String s) {
        super(s);
    }

    static public File getFileName(JFileChooser fileChooser) {
        fileChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        return getFileCustom(fileChooser);
    }

    /**
     * Do the filename handling:
     * <ol>
     * <li>rescan directory to see any new files
     * <li>Prompt user to select a file
     * <li>adds .xml extension if needed
     * <li>if that file exists, check with user
     * </ol>
     *
     * @param fileChooser the file chooser to use
     * @return the file to store or null if the user declined to store a file
     */
    @CheckForNull
    public static File getFileCustom(JFileChooser fileChooser) {
        fileChooser.rescanCurrentDirectory();
        int retVal = fileChooser.showDialog(null, null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return null;  // give up if no file selected
        }
        File file = fileChooser.getSelectedFile();
        if (fileChooser.getFileFilter() != fileChooser.getAcceptAllFileFilter()) {
            // append .xml to file name if needed
            String fileName = file.getAbsolutePath();
            String fileNameLC = fileName.toLowerCase();
            if (!fileNameLC.endsWith(".xml")) {
                fileName = fileName + ".xml";
                file = new File(fileName);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Save file: " + file.getPath());
        }
        // check for possible overwrite
        if (file.exists()) {
            int selectedValue = JOptionPane.showConfirmDialog(null,
                    Bundle.getMessage("FileOverwriteWarning", file.getName()),
                    Bundle.getMessage("OverwriteFile"),
                    JOptionPane.OK_CANCEL_OPTION);
            if (selectedValue != JOptionPane.OK_OPTION) {
                return null;
            }
        }
        return file;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        File file = getFileName(getConfigFileChooser());
        if (file == null) {
            return;
        }

        // and finally store
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm == null) {
            log.error("Failed to get default configure manager");
        } else {
            boolean results = cm.storeConfig(file);
            log.debug(results ? "store was successful" : "store failed");
            if (!results) {
                JOptionPane.showMessageDialog(null,
                        rb.getString("StoreHasErrors") + "\n"
                        + rb.getString("StoreIncomplete") + "\n"
                        + rb.getString("ConsoleWindowHasInfo"),
                        rb.getString("StoreError"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(StoreXmlConfigAction.class);

}
