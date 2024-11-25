package jmri.configurexml;

import java.io.File;
import java.util.Set;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JFileChooser;

import jmri.*;
import jmri.jmrit.logixng.LogixNGPreferences;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.util.swing.JmriJOptionPane;

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
        if (! InstanceManager.getDefault(PermissionManager.class)
                .ensureAtLeastPermission(LoadAndStorePermissionOwner.LOAD_XML_FILE_PERMISSION,
                        BooleanPermission.BooleanValue.TRUE)) {
            return;
        }
        loadFile(getConfigFileChooser(), JmriJOptionPane.findWindowForObject( e == null ? null : e.getSource()));
    }

    /**
     * Load a File from a given JFileChooser.
     * @param fileChooser {@link JFileChooser} to use for file selection
     * @param component a Component which has called the File Chooser.
     * @return true if successful
     */
    protected boolean loadFile(@Nonnull JFileChooser fileChooser, @CheckForNull Component component ) {
        Set<Editor> editors = InstanceManager.getDefault(EditorManager.class).getAll();
        if (!editors.isEmpty()) {
            InstanceManager.getDefault(jmri.UserPreferencesManager.class).showWarningMessage(
                    Bundle.getMessage("DuplicateLoadTitle"), Bundle.getMessage("DuplicateLoadMessage"),  // NOI18N
                    "jmri.jmrit.display.EditorManager",  "skipDupLoadDialog", false, true);  //NOI18N
            InstanceManager.getDefault(jmri.UserPreferencesManager.class).setPreferenceItemDetails(
                    "jmri.jmrit.display.EditorManager", "skipDupLoadDialog", Bundle.getMessage("DuplicateLoadSkip"));  // NOI18N
        }

        boolean results = false;
        File file = getFile(fileChooser, component);
        if (file != null) {
            log.info("Loading selected file: {}", file); // NOI18N
            try {
                ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
                if (cm == null) {
                    log.error("Failed to get default configure manager");  // NOI18N
                } else {
                    results = cm.load(file);

                    // If LogixNGs aren't setup, the actions and expressions will not
                    // be stored if the user stores the tables and panels. So we need
                    // to try to setup LogixNGs even if the loading failed.
                    LogixNG_Manager logixNG_Manager = InstanceManager.getDefault(LogixNG_Manager.class);
                    logixNG_Manager.setupAllLogixNGs();

                    if (results) {
                        // insure logix etc fire up
                        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
                        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();

                        if (InstanceManager.getDefault(LogixNGPreferences.class).getStartLogixNGOnStartup()
                                && logixNG_Manager.isStartLogixNGsOnLoad()) {
                            logixNG_Manager.activateAllLogixNGs();
                        }
                    }
                }
            } catch (JmriException e) {
                log.error("Unhandled problem in loadFile", e);  // NOI18N
            }
        } else {
            results = true;   // We assume that as the file is null then the user has clicked cancel.
        }
        return results;
    }

    /**
     * Get the File from a given JFileChooser.
     * @return the selected File.
     * @deprecated use {@link #getFile(JFileChooser fileChooser, Component component)}
     * @param fileChooser the JFileChooser for the file.
     */
    @CheckForNull
    @Deprecated (since="5.7.8",forRemoval=true)
    public static File getFile(@Nonnull JFileChooser fileChooser) {
        return getFile(fileChooser, null);
    }

    /**
     * Get the File from an Open File JFileChooser.
     * If a Component is provided, this helps the JFileChooser to not get stuck
     * behind an Always On Top Window.
     * @param fileChooser the FileChooser to get from.
     * @param component a Component within a JFrame / Window / Popup Menu,
     *                  or the JFrame or Window itself.
     * @return the File, may be null if none selected.
     */
    @CheckForNull
    public static File getFile(@Nonnull JFileChooser fileChooser, @CheckForNull Component component) {
        fileChooser.setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
        return getFileCustom(fileChooser, component);
    }

    /**
     * @return the selected File.
     * @deprecated use {@link #getFile(JFileChooser fileChooser, Component component)}
     * @param fileChooser the FileChooser to get from.
     */
    @CheckForNull
    @Deprecated (since="5.7.8",forRemoval=true)
    public static File getFileCustom(@Nonnull JFileChooser fileChooser) {
        return getFileCustom(fileChooser, null);
    }

    /**
     * Get the File from a JFileChooser.
     * If a Component is provided, this helps the JFileChooser to not get stuck
     * behind an Always On Top Window.
     * @param fileChooser the FileChooser to get from.
     * @param component a Component within a JFrame / Window / Popup Menu,
     *                  or the JFrame or Window itself.
     * @return the File, may be null if none selected.
     */
    @CheckForNull
    public static File getFileCustom(@Nonnull JFileChooser fileChooser, @CheckForNull Component component){
        fileChooser.rescanCurrentDirectory();
        int retVal = fileChooser.showDialog(component, Bundle.getMessage("MenuItemLoad"));  // NOI18N
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return null;  // give up if no file selected
        }
        log.debug("Open file: {}", fileChooser.getSelectedFile().getPath());
        return fileChooser.getSelectedFile();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoadXmlConfigAction.class);

}
