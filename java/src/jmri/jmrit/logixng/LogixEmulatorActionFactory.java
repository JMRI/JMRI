package jmri.jmrit.logixng;

import java.util.Map;
import java.util.Set;

/**
 * Factory class for LogixEmulatorAction classes.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface LogixEmulatorActionFactory {

    /**
     * Get a set of classes that implements the LogixEmulatorAction interface.
     * 
     * @return a set of entries with category and class
     */
    public Set<Map.Entry<Category, Class<? extends Base>>> getClasses();
    
}
