package jmri.configurexml;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.jmrit.logixng.LogixNGPreferences;
import jmri.JmriException;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.EditorManager;

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
        this("Open Data File ...");  // NOI18N
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
        Set<Editor> editors = InstanceManager.getDefault(EditorManager.class).getAll();
        if (!editors.isEmpty()) {
            InstanceManager.getDefault(jmri.UserPreferencesManager.class).showWarningMessage(
                    Bundle.getMessage("DuplicateLoadTitle"), Bundle.getMessage("DuplicateLoadMessage"),  // NOI18N
                    "jmri.jmrit.display.EditorManager",  "skipDupLoadDialog", false, true);  //NOI18N
            InstanceManager.getDefault(jmri.UserPreferencesManager.class).setPreferenceItemDetails(
                    "jmri.jmrit.display.EditorManager", "skipDupLoadDialog", Bundle.getMessage("DuplicateLoadSkip"));  // NOI18N
        }

        boolean results = false;
        java.io.File file = getFile(fileChooser);
        if (file != null) {
            log.info("Loading selected file: {}", file); // NOI18N
            try {
                ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
                if (cm == null) {
                    log.error("Failed to get default configure manager");  // NOI18N
                } else {
                    results = cm.load(file);
                    if (results) {
                        // insure logix etc fire up
                        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
                        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();
                        
                        jmri.jmrit.logixng.LogixNG_Manager logixNG_Manager =
                                InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class);
                        List<String> errors = new ArrayList<>();
                        if (! logixNG_Manager.setupAllLogixNGs(errors)) {
                            for (String s : errors) log.error(s);
                        }
                        if (InstanceManager.getDefault(LogixNGPreferences.class).getStartLogixNGOnStartup()) {
                            logixNG_Manager.activateAllLogixNGs();
                        }
                    }
                }
            } catch (JmriException e) {
                log.error("Unhandled problem in loadFile: {}", e);  // NOI18N
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
        int retVal = fileChooser.showDialog(null, Bundle.getMessage("MenuItemLoad"));  // NOI18N
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return null;  // give up if no file selected
        }
        if (log.isDebugEnabled()) {
            log.debug("Open file: {}", fileChooser.getSelectedFile().getPath());  // NOI18N
        }
        return fileChooser.getSelectedFile();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LoadXmlConfigAction.class);

}
