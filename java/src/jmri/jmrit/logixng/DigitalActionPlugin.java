package jmri.jmrit.logixng;

import java.util.Map;

/**
 * The parent interface for plugin DigitalActionBean classes.
 * A plugin DigitalActionBean class is a class that implements the DigitalActionBean interface and
 can be loaded from a JAR file.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface DigitalActionPlugin extends DigitalActionBean {
    
    /**
     * Initialize the object.
     * 
     * @param config the configuration
     */
    public void init(Map<String, String> config);
    
    /**
     * Get the configuration.
     * This method is called then the object is stored in the XML file.
     * 
     * @return the configuration
     */
    public Map<String, String> getConfig();
    
}
