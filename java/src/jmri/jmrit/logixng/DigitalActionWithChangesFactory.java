package jmri.jmrit.logixng;

import java.util.Map;
import java.util.Set;

/**
 * Factory class for DigitalAction classes.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface DigitalActionWithChangesFactory {

    /**
     * Get a set of classes that implements the DigitalActionWithChanges interface.
     * 
     * @return a set of entries with category and class
     */
    public Set<Map.Entry<Category, Class<? extends Base>>> getClasses();
    
}
