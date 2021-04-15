package jmri.jmrit.logixng;

import java.util.Map;
import java.util.Set;

/**
 * Factory class for StringExpressionBean classes.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public interface StringExpressionFactory {

    /**
     * Init the factory, for example create categories.
     */
    public default void init() {}
    
    /**
     * Get a set of classes that implements the StringExpression interface.
     * 
     * @return a set of entries with category and class
     */
    public Set<Map.Entry<Category, Class<? extends Base>>> getClasses();
    
}
