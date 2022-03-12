package jmri.jmrit.logixng;

import java.util.Map;
import java.util.Set;

/**
 * Factory class for DigitalAction classes.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface DigitalActionFactory {

    /**
     * Init the factory, for example create categories.
     */
    public default void init() {}
    
    /**
     * Get a set of classes that implements the DigitalAction interface.
     * 
     * @return a set of entries with category and class
     */
    public Set<Map.Entry<Category, Class<? extends DigitalActionBean>>> getActionClasses();
    
}
