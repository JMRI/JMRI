package jmri.jmrit.logixng;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The category of expressions and actions.
 * <P>
 * It's used to group expressions or actions then the user creates a new
 * expression or action.
 * <P>
 * This class is intended to be an Enum, but implemented as an abstract class
 * to allow adding more categories later without needing to change this class.
 * For example, external programs using JMRI as a lib might want to add their
 * own categories.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public abstract class Category implements Comparable<Category> {

    /**
     * A item on the layout, for example turnout, sensor and signal mast.
     */
    public static final Item ITEM = new Item();
    
    /**
     * Common.
     */
    public static final Common COMMON = new Common();
    
    /**
     * Other things.
     */
    public static final Other OTHER = new Other();
    
    /**
     * Get all the registered Categories
     * @return a list of categories
     */
    public static List<Category> values() {
        if (_categories == null) {
            synchronized(Category.class) {
                if (_categories == null) {
                    // It's not often any item is added to this list so we use CopyOnWriteArrayList
                    _categories = new CopyOnWriteArrayList<>();
                    registerCategory(ITEM);
                    registerCategory(COMMON);
                    registerCategory(OTHER);
                }
            }
        }
        return Collections.unmodifiableList(_categories);
    }
    
    /**
     * Register a category
     * @param category the category
     */
    public static void registerCategory(Category category) {
        // Ensure that the _categories variable is initialized
        values();
        
        _categories.add(category);
    }
    
    
    private static volatile List<Category> _categories;
    
    private final String _name;
    private final String _description;
    private final int _order;
    
    
    protected Category(String name, String description, int order) {
        _name = name;
        _description = description;
        _order = order;
    }
    
    public String name() {
        return _name;
    }
    
    @Override
    public final String toString() {
        return _description;
    }
    
    public int order() {
        return _order;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Category) {
            Category c = (Category)o;
            return _description.equals(c._description) && _name.equals(c._name);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return _description.hashCode();
    }
    
    @Override
    public int compareTo(Category c) {
        if (_order < c.order()) return -1;
        if (_order > c.order()) return 1;
        return 0;
    }
    
    
    public static final class Item extends Category {

        public Item() {
            super("ITEM", Bundle.getMessage("CategoryItem"), 100);
        }
    }
    
    
    public static final class Common extends Category {

        public Common() {
            super("COMMON", Bundle.getMessage("CategoryCommon"), 200);
        }
    }
    
    
    public static final class Other extends Category {

        public Other() {
            super("OTHER", Bundle.getMessage("CategoryOther"), 300);
        }
    }
    
}
