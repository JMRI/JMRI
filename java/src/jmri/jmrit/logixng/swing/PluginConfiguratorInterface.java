package jmri.jmrit.logixng.swing;

import java.util.Map;
import javax.swing.JPanel;

/**
 * The parent interface for the configurator of plugin classes.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface PluginConfiguratorInterface {

    /**
     * Get a configuration panel for this plugin.
     * This method initializes the panel with an empty configuration.
     * 
     * @param className the name of the class for which to return
     * a configuration panel
     * @return a panel that configures this plugin
     * @throws IllegalArgumentException if this class does not support the class
     * with the name given in parameter 'className'
     */
    public JPanel getConfigPanel(String className) throws IllegalArgumentException;
    
    /**
     * Get a configuration panel for this plugin.
     * 
     * @param className the name of the class for which to return
     * a configuration panel
     * @param config the configuration for this plugin
     * @return a panel that configures this plugin
     * @throws IllegalArgumentException if this class does not support the class
     * with the name given in parameter 'className'
     */
    public JPanel getConfigPanel(String className, Map<String, String> config)
            throws IllegalArgumentException;
    
    /**
     * Update the configuration.
     * This method is called then the user has editied the configuration and
     * saves the changes.
     * 
     * @param panel the panel that was returned by getConfigPanel()
     * @return the configuration for this plugin
     */
    public Map<String, String> getConfigFromPanel(JPanel panel);

}
