// StoreXmlConfigAction.java

package jmri.configurexml;

import jmri.InstanceManager;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import java.io.File;

/**
 * Store the JMRI config as XML
 *
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version			$Revision: 1.1 $
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
		File fp = new File("temp.config.xml");
        InstanceManager.configureManagerInstance().store(fp);
	}

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(StoreLayoutAction.class.getName());

}
