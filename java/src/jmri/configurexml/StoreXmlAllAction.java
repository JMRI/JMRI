package jmri.configurexml;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store the entire JMRI status in an XML file.
 * <p>
 * See {@link jmri.ConfigureManager} for information on the various types of
 * information stored in configuration files.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @see jmri.jmrit.XmlFile
 */
public class StoreXmlAllAction extends StoreXmlConfigAction {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

    public StoreXmlAllAction() {
        this("Store all ...");
    }

    public StoreXmlAllAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        java.io.File file = getFileName(getAllFileChooser());
        if (file == null) {
            return;
        }
        
        // and finally store
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm == null) {
            log.error("Failed to get default configure manager");
        } else {
            boolean results = cm.storeAll(file);
            log.debug(results ? "store was successful" : "store failed");
            if (!results) {
                JOptionPane.showMessageDialog(null,
                        rb.getString("PanelStoreHasErrors") + "\n"
                        + rb.getString("PanelStoreIncomplete") + "\n"
                        + rb.getString("ConsoleWindowHasInfo"),
                        rb.getString("PanelStoreError"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(StoreXmlAllAction.class);
}
