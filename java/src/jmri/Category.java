package jmri;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A category of something.
 * <P>
 * Category was created for LogixNG actions and expressions but it can be used
 * for everything in JMRI that needs "extendable enums".
 * <P>
 * This class is intended to be an Enum, but implemented as an abstract class
 * to allow adding more categories later without needing to change this class.
 * For example, external programs using JMRI as a lib might want to add their
 * own categories.
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public abstract class Category implements Comparable<Category> {

    /**
     * Other things.
     */
    public static final Other OTHER = new Other();

    static {
        // It's not often any item is added to this list so we use CopyOnWriteArrayList
        _categories = new CopyOnWriteArrayList<>();
        registerCategory(OTHER);
    }

    /**
     * Get all the registered Categories
     * @return a list of categories
     */
    public static List<Category> values() {
        return Collections.unmodifiableList(_categories);
    }

    /**
     * Register a category.
     * There must not exist any category with either the name or the description
     * of this category. Otherwise an IllegalArgumentException will be thrown.
     * @param category the category
     * @return the new category
     * @throws IllegalArgumentException if the category already is registered.
     */
    public static Category registerCategory(Category category)
            throws IllegalArgumentException {
        for (Category c : _categories) {
            if (c.equals(category)) {
                throw new IllegalArgumentException(String.format(
                        "Category '%s' with description '%s' is already registered",
                        category._name, category._description));
            }
        }
        _categories.add(category);
        return category;
    }


    private static final List<Category> _categories;

    private final String _name;
    private final String _description;
    private final int _order;


    protected Category(String name, String description, int order) {
        _name = name;
        _description = description;
        _order = order;
    }

    public final String name() {
        return _name;
    }

    @Override
    public final String toString() {
        return _description;
    }

    public final int order() {
        return _order;
    }

    @Override
    public final boolean equals(Object o) {
        if (o instanceof Category) {
            Category c = (Category)o;
            // We check during initialization that two categories isn't equal.
            return _name.equals(c._name) || _description.equals(c._description);
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return _description.hashCode();
    }

    @Override
    public final int compareTo(Category c) {
        if (_order < c.order()) return -1;
        if (_order > c.order()) return 1;
        return toString().compareTo(c.toString());
    }


    public static final class Other extends Category {

        public Other() {
            super("OTHER", Bundle.getMessage("CategoryOther"), Integer.MAX_VALUE);
        }
    }

}
