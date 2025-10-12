package jmri.jmrit.logixng;

import java.util.Map;
import java.util.Set;

/**
 * Factory class for AnalogExpressionBean classes.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface AnalogExpressionFactory {

    /**
     * Init the factory, for example create categories.
     */
    default void init() {}
    
    /**
     * Get a set of classes that implements the AnalogExpression interface.
     * 
     * @return a set of entries with category and class
     */
    Set<Map.Entry<LogixNG_Category, Class<? extends Base>>> getClasses();
    
}
