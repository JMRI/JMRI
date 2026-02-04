package jmri.configurexml;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

import jmri.*;
import jmri.util.swing.JmriJOptionPane;

/**
 * Load configuration information from an XML file.
 * <p>
 * The file context for this is the "user" file chooser.
 * <p>
 * This will load whatever information types are present in the file. See
 * {@link jmri.ConfigureManager} for information on the various types of
 * information stored in configuration files.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @see jmri.jmrit.XmlFile
 */
public class LoadXmlUserAction extends LoadXmlConfigAction {

    private static File currentFile = null;

    public LoadXmlUserAction() {
        this(Bundle.getMessage("MenuItemLoad"));
    }

    public LoadXmlUserAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (! InstanceManager.getDefault(PermissionManager.class)
                .ensureAtLeastPermission(LoadAndStorePermissionOwner.LOAD_XML_FILE_PERMISSION,
                        BooleanPermission.BooleanValue.TRUE)) {
            return;
        }
        JFileChooser userFileChooser = getUserFileChooser();
        userFileChooser.setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
        userFileChooser.setApproveButtonText(Bundle.getMessage("ButtonOpen"));
        // Cancel button can't be localized like userFileChooser.setCancelButtonText() TODO
        userFileChooser.setDialogTitle(Bundle.getMessage("LoadTitle"));

        java.awt.Window window = JmriJOptionPane.findWindowForObject( e == null ? null : e.getSource());
        boolean results = loadFile(userFileChooser, window);
        if (results) {
            log.debug("load was successful");
            setCurrentFile(userFileChooser.getSelectedFile());
        } else {
            log.debug("load failed");
            JmriJOptionPane.showMessageDialog(window,
                    Bundle.getMessage("LoadHasErrors") + "\n"
                    + Bundle.getMessage("CheckPreferences") + "\n"
                    + Bundle.getMessage("ConsoleWindowHasInfo"),
                    Bundle.getMessage("LoadError"), JmriJOptionPane.ERROR_MESSAGE);
            setCurrentFile(null);
        }
    }

    /**
     * Used by e.g. jmri.jmrit.mailreport.ReportPanel et al to know last load
     *
     * @return the last file loaded using this action; returns null if this
     *         action was not called or if the last time this action was called,
     *         no file was loaded
     */
    public static File getCurrentFile() {
        return currentFile;
    }

    private static void setCurrentFile(File arg) {
        currentFile = arg;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoadXmlUserAction.class);

}
