package jmri.configurexml;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.implementation.JmriConfigurationManager;
import jmri.util.FileChooserFilter;
import jmri.util.FileUtil;

/**
 * Base implementation for the load and store actions.
 * 
 * Primarily provides file checking services to the specific subclasses that
 * load/store particular types of data.
 * <p>
 * Also used to hold common information, specifically common instances of the
 * JFileChooser. These bring the user back to the same place in the file system
 * each time an action is invoked.
 *
 * @author Bob Jacobsen Copyright (C) 2004
 * @see jmri.jmrit.XmlFile
 */
abstract public class LoadStoreBaseAction extends AbstractAction {

    public LoadStoreBaseAction(String s) {
        super(s);
        // ensure that an XML config manager exists
        if (!InstanceManager.getOptionalDefault(ConfigureManager.class).isPresent()) {
            InstanceManager.setDefault(ConfigureManager.class, new JmriConfigurationManager());
        }
    }

    /*
     * These JFileChoosers are retained so that multiple actions can all open
     * the JFileChoosers at the last used location for the context that the
     * action supports.
     */
    static private JFileChooser allFileChooser = null;
    static private JFileChooser configFileChooser = null;
    static private JFileChooser userFileChooser = null;

    static private JFileChooser getXmlFileChooser(String path) {
        FileChooserFilter xmlFilter = new FileChooserFilter("XML files");
        xmlFilter.addExtension("xml"); // NOI18N
        JFileChooser chooser = new JFileChooser(path);
        chooser.setFileFilter(xmlFilter);
        return chooser;
    }

    static protected JFileChooser getAllFileChooser() {
        if (allFileChooser == null) {
            allFileChooser = getXmlFileChooser(FileUtil.getUserFilesPath());
        }
        return allFileChooser;
    }

    static protected JFileChooser getConfigFileChooser() {
        if (configFileChooser == null) {
            configFileChooser = getXmlFileChooser(FileUtil.getUserFilesPath());
        }
        return configFileChooser;
    }

    // Made public so JmriConfigurationManager.java can set the
    // "Store Panels..." default file (to the panel file being loaded)
    static public JFileChooser getUserFileChooser() {
        if (userFileChooser == null) {
            userFileChooser = getXmlFileChooser(FileUtil.getUserFilesPath());
        }
        return userFileChooser;
    }
}
