package jmri.jmrit.display.logixng;

import jmri.jmrit.logixng.Category;

/**
 * Defines the category Display
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public final class CategoryDisplay extends Category {
    
    /**
     * An item related to panels.
     */
    public static final CategoryDisplay DISPLAY = new CategoryDisplay();
    
    
    public CategoryDisplay() {
        super("DISPLAY", Bundle.getMessage("CategoryDisplay"), 200);
    }
    
    public static void registerCategory() {
        if (!Category.values().contains(DISPLAY)) {
            Category.registerCategory(DISPLAY);
        }
    }
    
}
