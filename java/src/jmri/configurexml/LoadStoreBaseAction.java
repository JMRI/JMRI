// LoadStoreBaseAction.java
package jmri.configurexml;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import jmri.InstanceManager;
import jmri.implementation.JmriConfigurationManager;
import jmri.util.FileChooserFilter;
import jmri.util.FileUtil;

/**
 * Base implementation for the load and store actions.
 * <P>
 * Primarily provides file checking services to the specific subclasses that
 * load/store particular types of data.
 * <P>
 * Also used to hold common information, specifically common instances of the
 * JFileChooser. These bring the user back to the same place in the file system
 * each time an action is invoked.
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 * @version	$Revision$
 * @see jmri.jmrit.XmlFile
 */
abstract public class LoadStoreBaseAction extends AbstractAction {

    private static final long serialVersionUID = -5757646065294988765L;

    public LoadStoreBaseAction(String s) {
        super(s);
        // ensure that an XML config manager exists
        if (InstanceManager.configureManagerInstance() == null) {
            InstanceManager.setConfigureManager(new JmriConfigurationManager());
        }
    }

    /*
     * These JFileChoosers are retained so that multiple actions can all open
     * the JFileChoosers at the last used location for the context that the
     * action supports.
     */
    private static JFileChooser allFileChooser = null;
    private static JFileChooser configFileChooser = null;
    private static JFileChooser userFileChooser = null;

    private JFileChooser getXmlFileChooser(String path) {
        FileChooserFilter xmlFilter = new FileChooserFilter("XML files");
        xmlFilter.addExtension("xml"); // NOI18N
        JFileChooser chooser = new JFileChooser(path);
        chooser.setFileFilter(xmlFilter);
        return chooser;
    }

    protected JFileChooser getAllFileChooser() {
        if (allFileChooser == null) {
            allFileChooser = getXmlFileChooser(FileUtil.getUserFilesPath());
        }
        return allFileChooser;
    }

    protected JFileChooser getConfigFileChooser() {
        if (configFileChooser == null) {
            configFileChooser = getXmlFileChooser(FileUtil.getUserFilesPath());
        }
        return configFileChooser;
    }

    protected JFileChooser getUserFileChooser() {
        if (userFileChooser == null) {
            userFileChooser = getXmlFileChooser(FileUtil.getUserFilesPath());
        }
        return userFileChooser;
    }

}
