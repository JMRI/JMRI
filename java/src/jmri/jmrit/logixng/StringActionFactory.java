package jmri.jmrit.logixng;

import java.util.Map;
import java.util.Set;

import jmri.Category;

/**
 * Factory class for StringActionBean classes.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public interface StringActionFactory {

    /**
     * Init the factory, for example create categories.
     */
    default void init() {}

    /**
     * Get a set of classes that implements the StringActionBean interface.
     *
     * @return a set of entries with category and class
     */
    Set<Map.Entry<Category, Class<? extends Base>>> getClasses();

}
