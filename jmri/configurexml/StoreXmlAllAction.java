// StoreXmlAllAction.java

package jmri.configurexml;

import jmri.InstanceManager;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

/**
 * Store the entire JMRI status in an XML file.
 * <P>
 * See {@link jmri.ConfigureManager} for information on the various
 * types of information stored in configuration files.
 *
 * @author	Bob Jacobsen   Copyright (C) 2002
 * @version	$Revision: 1.3 $
 * @see         jmri.jmrit.XmlFile
 */
public class StoreXmlAllAction extends StoreXmlConfigAction {

    public StoreXmlAllAction() {
        this("Store all ...");
    }

    public StoreXmlAllAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        java.io.File file = getFileName();
        if (file==null) return;
        
        // and finally store
        InstanceManager.configureManagerInstance().storeAll(file);
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(StoreXmlAllAction.class.getName());
}
