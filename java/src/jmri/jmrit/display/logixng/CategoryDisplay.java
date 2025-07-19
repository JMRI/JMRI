package jmri.jmrit.display.logixng;

import jmri.jmrit.logixng.LogixNG_Category;

/**
 * Defines the category Display
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public final class CategoryDisplay extends LogixNG_Category {

    /**
     * An item related to panels.
     */
    public static final CategoryDisplay DISPLAY = new CategoryDisplay();


    public CategoryDisplay() {
        super("DISPLAY", Bundle.getMessage("CategoryDisplay"), 220);
    }

    public static void registerCategory() {
        if (!LogixNG_Category.values().contains(DISPLAY)) {
            LogixNG_Category.registerCategory(DISPLAY);
        }
    }

}
