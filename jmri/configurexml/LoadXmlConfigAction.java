// LoadXmlConfigAction.java

package jmri.configurexml;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import java.io.File;

import jmri.InstanceManager;

/**
 * Load the JMRI config from an XML file
 *
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version			$Revision: 1.1 $
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
		File fp = new File("temp.config.xml");
        InstanceManager.configureManagerInstance().load(fp);
	}

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LoadXmlConfigAction.class.getName());

}
