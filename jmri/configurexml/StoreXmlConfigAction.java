// StoreXmlConfigAction.java

package jmri.configurexml;

import jmri.InstanceManager;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

/**
 * Store the JMRI configuration information as XML.
 * <P>
 * Note that this does not store preferences, tools or user information
 * in the file.  This is not a complete store!
 * See {@link jmri.ConfigureManager} for information on the various
 * types of information stored in configuration files.
 *
 * @author	Bob Jacobsen   Copyright (C) 2002
 * @version	$Revision: 1.7 $
 * @see         jmri.jmrit.XmlFile
 */
public class StoreXmlConfigAction extends AbstractAction {

    public StoreXmlConfigAction() {
        super("Store configuration ...");
    }

    public StoreXmlConfigAction(String s) {
        super(s);
        // ensure that an XML config manager exists
        if (InstanceManager.configureManagerInstance()==null)
            InstanceManager.setConfigureManager(new ConfigXmlManager());
    }

    public void actionPerformed(ActionEvent e) {
        LoadStoreBase.fileChooser.rescanCurrentDirectory();
        int retVal = LoadStoreBase.fileChooser.showSaveDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
        if (log.isDebugEnabled()) log.debug("Save file: "+LoadStoreBase.fileChooser.getSelectedFile().getPath());
        InstanceManager.configureManagerInstance().storeConfig(LoadStoreBase.fileChooser.getSelectedFile());
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(StoreXmlConfigAction.class.getName());
}
