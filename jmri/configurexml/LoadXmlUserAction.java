// LoadXmlConfigAction.java

package jmri.configurexml;

import jmri.InstanceManager;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

/**
 * Load configuration information from an XML file.
 * <P>
 * The file context for this is the "user" file chooser.
 * <P>
 * This will load whatever information types are present in the file.
 * See {@link jmri.ConfigureManager} for information on the various
 * types of information stored in configuration files.
 *
 * @author	    Bob Jacobsen   Copyright (C) 2002
 * @version	    $Revision: 1.1 $
 * @see             jmri.jmrit.XmlFile
 */
public class LoadXmlUserAction extends LoadXmlConfigAction {

    public LoadXmlUserAction() {
        this("Load ...");
    }

    public LoadXmlUserAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        loadFile(userFileChooser);
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LoadXmlUserAction.class.getName());

}
