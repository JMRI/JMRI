// StoreXmlUserAction.java

package jmri.configurexml;

import jmri.InstanceManager;
import java.awt.event.ActionEvent;

/**
 * Store the JMRI user-level information as XML.
 * <P>
 * Note that this does not store preferences, configuration, or tool information
 * in the file.  This is not a complete store!
 * See {@link jmri.ConfigureManager} for information on the various
 * types of information stored in configuration files.
 *
 * @author	Bob Jacobsen   Copyright (C) 2002
 * @version	$Revision: 1.5 $
 * @see         jmri.jmrit.XmlFile
 */
public class StoreXmlUserAction extends StoreXmlConfigAction {

    public StoreXmlUserAction() {
        this("Store panels ...");
    }

    public StoreXmlUserAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        java.io.File file = getFileName(userFileChooser);
        if (file==null) return;
        
        // and finally store
        InstanceManager.configureManagerInstance().storeUser(file);
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(StoreXmlUserAction.class.getName());
}
