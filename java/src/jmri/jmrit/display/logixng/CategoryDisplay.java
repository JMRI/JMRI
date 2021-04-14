package jmri.jmrit.display.logixng;

import jmri.jmrit.logixng.Category;

/**
 * Defines the category LocoNet
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public final class CategoryDisplay extends Category {
    
    /**
     * A item on the layout, for example turnout, sensor and signal mast.
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
