package jmri.jmrit.logixng;

import java.util.Map;
import java.util.Set;

/**
 * Factory class for DigitalExpressionBean classes.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface DigitalExpressionFactory {

    /**
     * Init the factory, for example create categories.
     */
    public default void init() {}
    
    /**
     * Get a set of classes that implements the DigitalExpressionBean interface.
     * 
     * @return a set of entries with category and class
     */
    public Set<Map.Entry<Category, Class<? extends DigitalExpressionBean>>> getExpressionClasses();
    
}
