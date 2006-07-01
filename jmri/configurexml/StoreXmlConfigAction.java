// StoreXmlConfigAction.java

package jmri.configurexml;

import jmri.InstanceManager;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

/**
 * Store the JMRI config as XML
 *
 * @author	Bob Jacobsen   Copyright (C) 2002
 * @version	$Revision: 1.4 $
 * @see             jmri.jmrit.XmlFile
 */
public class StoreXmlConfigAction extends AbstractAction {

	public StoreXmlConfigAction(String s) {
		super(s);
        // ensure that an XML config manager exists
        if (InstanceManager.configureManagerInstance()==null)
            InstanceManager.setConfigureManager(new ConfigXmlManager());
	}

    public void actionPerformed(ActionEvent e) {
        int retVal = LoadStoreBase.fileChooser.showSaveDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
        log.debug("Save config file: "+LoadStoreBase.fileChooser.getSelectedFile().getPath());
        InstanceManager.configureManagerInstance().store(LoadStoreBase.fileChooser.getSelectedFile());
	}

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(StoreXmlConfigAction.class.getName());

}
