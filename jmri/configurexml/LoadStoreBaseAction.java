// LoadStoreBaseAction.java

package jmri.configurexml;

import jmri.InstanceManager;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

/**
 * Base implementation for the load and store actions.
 * <P>
 * Primarily provides file checking services to the 
 * specific subclasses that load/store particular types of data.
 * <P>
 * Also used to
 * hold common information, specifically common instances of
 * the JFileChooser. These bring the user back to the same
 * place in the file system each time an action is invoked.
 *
 * @author	Bob Jacobsen   Copyright (C) 2004
 * @version	$Revision: 1.1 $
 * @see         jmri.jmrit.XmlFile
 */
public class LoadStoreBaseAction extends AbstractAction {

    public LoadStoreBaseAction(String s) {
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

    static JFileChooser allFileChooser = new JFileChooser(jmri.jmrit.XmlFile.prefsDir());
    static JFileChooser configFileChooser = new JFileChooser(jmri.jmrit.XmlFile.prefsDir());
    static JFileChooser userFileChooser = new JFileChooser(jmri.jmrit.XmlFile.prefsDir());

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LoadStoreBaseAction.class.getName());

}
