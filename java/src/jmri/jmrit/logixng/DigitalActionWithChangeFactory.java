package jmri.jmrit.logixng;

import java.util.Map;
import java.util.Set;

/**
 * Factory class for DigitalActionWithChange classes.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface DigitalActionWithChangeFactory {

    /**
     * Get a set of classes that implements the DigitalActionWithChange interface.
     * 
     * @return a set of entries with category and class
     */
    public Set<Map.Entry<Category, Class<? extends Base>>> getClasses();
    
}
