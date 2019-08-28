package jmri.jmrit.logixng;

/**
 * The category of expressions and actions.
 * 
 * It's used to group expressions or actions then the user creates a new
 * expression or action.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public enum Category {

    /**
     * A item on the layout, for example turnout, sensor and signal mast.
     */
    ITEM("CategoryItem"),
    
    /**
     * Common.
     */
    COMMON("CategoryCommon"),
    
    /**
     * Other things.
     */
    OTHER("CategoryOther"),
    
    /**
     * Extravaganza. Things seldom used, included mostly for fun, but maybe
     * useful in some cases.
     */
    EXRAVAGANZA("CategoryExtravaganza");
    
    
    private final String _bundleKey;
    
    private Category(String bundleKey) {
        _bundleKey = bundleKey;
    }
    
    @Override
    public String toString() {
        return Bundle.getMessage(_bundleKey);
    }
    
}
