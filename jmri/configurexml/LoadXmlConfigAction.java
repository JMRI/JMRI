// LoadXmlConfigAction.java

package jmri.configurexml;

import java.awt.event.*;
import javax.swing.*;
import jmri.*;
import jmri.jmrit.*;

/**
 * Load the JMRI config from an XML file
 *
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version			$Revision: 1.2 $
 * @see             jmri.jmrit.XmlFile
 */
public class LoadXmlConfigAction extends AbstractAction {

	public LoadXmlConfigAction(String s) {
		super(s);
        // ensure that an XML config manager exists
        if (InstanceManager.configureManagerInstance()==null)
            InstanceManager.setConfigureManager(new ConfigXmlManager());
	}

    public void actionPerformed(ActionEvent e) {
        JFileChooser inputFileChooser = new JFileChooser(XmlFile.prefsDir());
		int retVal = inputFileChooser.showOpenDialog(null);
		if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
        log.debug("Open config file: "+inputFileChooser.getSelectedFile().getPath());
        InstanceManager.configureManagerInstance().load(inputFileChooser.getSelectedFile());
	}

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LoadXmlConfigAction.class.getName());

}
