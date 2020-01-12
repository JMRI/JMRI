package jmri.configurexml;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");
    static private File currentFile = null;

    public LoadXmlUserAction() {
        this(rb.getString("MenuItemLoad"));
    }

    public LoadXmlUserAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser userFileChooser = getUserFileChooser();
        userFileChooser.setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
        userFileChooser.setApproveButtonText(Bundle.getMessage("ButtonOpen")); // is in jmri.NBBundle
        // Cancel button can't be localized like userFileChooser.setCancelButtonText() TODO
        userFileChooser.setDialogTitle(rb.getString("LoadPanelTitle"));

        boolean results = loadFile(userFileChooser);
        if (results) {
            log.debug("load was successful");
            setCurrentFile(userFileChooser.getSelectedFile());
        } else {
            log.debug("load failed");
            JOptionPane.showMessageDialog(null,
                    rb.getString("PanelHasErrors") + "\n"
                    + rb.getString("CheckPreferences") + "\n"
                    + rb.getString("ConsoleWindowHasInfo"),
                    rb.getString("PanelLoadError"), JOptionPane.ERROR_MESSAGE);
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

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LoadXmlUserAction.class);

}
