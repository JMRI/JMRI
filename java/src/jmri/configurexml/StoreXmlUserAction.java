// StoreXmlUserAction.java
package jmri.configurexml;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store the JMRI user-level information as XML.
 * <P>
 * Note that this does not store preferences, configuration, or tool information
 * in the file. This is not a complete store! See {@link jmri.ConfigureManager}
 * for information on the various types of information stored in configuration
 * files.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version	$Revision$
 * @see jmri.jmrit.XmlFile
 */
public class StoreXmlUserAction extends StoreXmlConfigAction {

    /**
     *
     */
    private static final long serialVersionUID = 2422199264501755141L;
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

    public StoreXmlUserAction() {
        this(rb.getString("MenuItemStore"));
    }

    public StoreXmlUserAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser userFileChooser = getUserFileChooser();
        userFileChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        userFileChooser.setApproveButtonText(rb.getString("StorePanelTitle"));
        userFileChooser.setDialogTitle(rb.getString("StorePanelTitle"));
        java.io.File file = getFileCustom(userFileChooser);

        if (file == null) {
            return;
        }

        // make a backup file
        InstanceManager.configureManagerInstance().makeBackup(file);
        // and finally store
        boolean results = InstanceManager.configureManagerInstance().storeUser(file);
        log.debug(results ? "store was successful" : "store failed");
        if (!results) {
            JOptionPane.showMessageDialog(null,
                    rb.getString("StoreHasErrors") + "\n"
                    + rb.getString("StoreIncomplete") + "\n"
                    + rb.getString("ConsoleWindowHasInfo"),
                    rb.getString("StoreError"), JOptionPane.ERROR_MESSAGE);
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(StoreXmlUserAction.class.getName());
}
