// LoadXmlConfigAction.java

package jmri.configurexml;

import jmri.InstanceManager;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

/**
 * Load configuration information from an XML file.
 * <P>
 * This will load whatever information types are present in the file.
 * See {@link jmri.ConfigureManager} for information on the various
 * types of information stored in configuration files.
 *
 * @author	    Bob Jacobsen   Copyright (C) 2002
 * @version	    $Revision: 1.7 $
 * @see             jmri.jmrit.XmlFile
 */
public class LoadXmlConfigAction extends AbstractAction {

    public LoadXmlConfigAction() {
        super("Load ...");
    }

    public LoadXmlConfigAction(String s) {
        super(s);
        // ensure that an XML config manager exists
        if (InstanceManager.configureManagerInstance()==null)
            InstanceManager.setConfigureManager(new ConfigXmlManager());
    }

    public void actionPerformed(ActionEvent e) {
        LoadStoreBase.fileChooser.rescanCurrentDirectory();
        int retVal = LoadStoreBase.fileChooser.showOpenDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
        if (log.isDebugEnabled()) log.debug("Open config file: "+LoadStoreBase.fileChooser.getSelectedFile().getPath());
        InstanceManager.configureManagerInstance().load(LoadStoreBase.fileChooser.getSelectedFile());
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LoadXmlConfigAction.class.getName());

}
