package jmri.configurexml;

import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load configuration information from an XML file.
 * <p>
 * The file context for this is the "config" file chooser.
 * <p>
 * This will load whatever information types are present in the file. See
 * {@link jmri.ConfigureManager} for information on the various types of
 * information stored in configuration files.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @see jmri.jmrit.XmlFile
 */
public class LoadXmlConfigAction extends LoadStoreBaseAction {

    public LoadXmlConfigAction() {
        this("Open Panel File ...");
    }

    public LoadXmlConfigAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        loadFile(getConfigFileChooser());
    }

    /**
     *
     * @param fileChooser {@link JFileChooser} to use for file selection
     * @return true if successful
     */
    protected boolean loadFile(JFileChooser fileChooser) {
        boolean results = false;
        java.io.File file = getFile(fileChooser);
        if (file != null) {
            try {
                ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
                if (cm == null) {
                    log.error("Failed to get default configure manager");
                } else {
                    results = cm.load(file);
                    if (results) {
                        // insure logix etc fire up
                        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
                        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();
                        new jmri.jmrit.catalog.configurexml.DefaultCatalogTreeManagerXml().readCatalogTrees();
                    }
                }
            } catch (JmriException e) {
                log.error("Unhandled problem in loadFile: " + e);
            }
        } else {
            results = true;   // We assume that as the file is null then the user has clicked cancel.
        }
        return results;
    }

    static public java.io.File getFile(JFileChooser fileChooser) {
        fileChooser.setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
        return getFileCustom(fileChooser);
    }

    static public java.io.File getFileCustom(JFileChooser fileChooser) {
        fileChooser.rescanCurrentDirectory();
        int retVal = fileChooser.showDialog(null, null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return null;  // give up if no file selected
        }
        if (log.isDebugEnabled()) {
            log.debug("Open file: " + fileChooser.getSelectedFile().getPath());
        }
        return fileChooser.getSelectedFile();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LoadXmlConfigAction.class);

}
