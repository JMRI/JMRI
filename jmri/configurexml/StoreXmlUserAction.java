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
 * @version	$Revision: 1.9 $
 * @see         jmri.jmrit.XmlFile
 */
public class StoreXmlUserAction extends StoreXmlConfigAction {

    public StoreXmlUserAction() {
        this(
            java.util.ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle")
                .getString("MenuItemStore"));
    }

    public StoreXmlUserAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        String oldButtonText=userFileChooser.getApproveButtonText();
        userFileChooser.setApproveButtonText(java.util.ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle").getString("MenuItemStore"));
        java.io.File file = getFileName(userFileChooser);
 
        if (file==null) return;
        
        // make a backup file
        InstanceManager.configureManagerInstance().makeBackup(file);
        // and finally store
        InstanceManager.configureManagerInstance().storeUser(file);

        // The last thing we do is restore the Approve button text.
        userFileChooser.setApproveButtonText(oldButtonText);
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(StoreXmlUserAction.class.getName());
}
