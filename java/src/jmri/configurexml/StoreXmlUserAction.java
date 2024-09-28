package jmri.configurexml;

import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;

import jmri.*;
import jmri.util.swing.JmriJOptionPane;

/**
 * Store the JMRI user-level information as XML.
 * <P>
 * Note that this does not store preferences, configuration, or tool information
 * in the file. This is not a complete store! See {@link jmri.ConfigureManager}
 * for information on the various types of information stored in configuration
 * files.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @see jmri.jmrit.XmlFile
 */
public class StoreXmlUserAction extends StoreXmlConfigAction {

    public StoreXmlUserAction() {
        this(Bundle.getMessage("MenuItemStore"));  // NOI18N
    }

    public StoreXmlUserAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (! InstanceManager.getDefault(PermissionManager.class)
                .ensureAtLeastPermission(LoadAndStorePermissionOwner.STORE_XML_FILE_PERMISSION,
                        BooleanPermission.BooleanValue.TRUE)) {
            return;
        }
        JFileChooser userFileChooser = getUserFileChooser();
        userFileChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        userFileChooser.setApproveButtonText(Bundle.getMessage("ButtonSave"));  // NOI18N
        userFileChooser.setDialogTitle(Bundle.getMessage("StoreTitle"));  // NOI18N
        java.io.File file = getFileCustom(userFileChooser);

        if (file == null) {
            return;
        }

        // make a backup file
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm == null) {
            log.error("Failed to make backup due to unable to get default configure manager");  // NOI18N
        } else {
            cm.makeBackup(file);
            // and finally store
            boolean results = cm.storeUser(file);
            log.debug("store {}", results ? "was successful" : "failed");  // NOI18N
            if (!results) {
                JmriJOptionPane.showMessageDialog(null,
                        Bundle.getMessage("StoreHasErrors") + "\n"  // NOI18N
                        + Bundle.getMessage("StoreIncomplete") + "\n"  // NOI18N
                        + Bundle.getMessage("ConsoleWindowHasInfo"),  // NOI18N
                        Bundle.getMessage("StoreError"), JmriJOptionPane.ERROR_MESSAGE);  // NOI18N
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StoreXmlUserAction.class);
}
