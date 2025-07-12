package jmri.jmrit.operations.logixng;

import jmri.jmrit.logixng.LogixNG_Category;

/**
 * Defines the category Operations
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public final class CategoryOperations extends LogixNG_Category {

    /**
     * A item on the layout, for example turnout, sensor and signal mast.
     */
    public static final CategoryOperations OPERATIONS = new CategoryOperations();


    public CategoryOperations() {
        super("OPERATIONS", Bundle.getMessage("CategoryOperations"), 300);
    }

    public static void registerCategory() {
        LogixNG_Category.registerCategory(OPERATIONS);
    }

}
