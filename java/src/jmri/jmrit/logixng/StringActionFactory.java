package jmri.jmrit.logixng;

import java.util.Map;
import java.util.Set;

/**
 * Factory class for StringActionBean classes.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface StringActionFactory {

    /**
     * Get a set of classes that implements the StringActionBean interface.
     * 
     * @return a set of entries with category and class
     */
    public Set<Map.Entry<Category, Class<? extends Base>>> getClasses();
    
}
