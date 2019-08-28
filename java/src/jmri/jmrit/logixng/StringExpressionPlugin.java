package jmri.jmrit.logixng;

import java.util.Map;

/**
 * The parent interface for plugin StringExpressionBean classes.
 * A plugin StringExpressionBean class is a class that implements the
 StringExpressionBean interface and can be loaded from a JAR file.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface StringExpressionPlugin extends StringExpressionBean {

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
