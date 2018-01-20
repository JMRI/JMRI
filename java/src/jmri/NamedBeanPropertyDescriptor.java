package jmri;

/**
 * Describes metadata about a given property key for a NamedBean. This metadata is used by the
 * BeanTable actions to display and edit the properties in question.
 *
 * @author Balazs Racz Copyright (C) 2018
 */

public abstract class NamedBeanPropertyDescriptor {
    /**
     * Key of the property, to be used in the setProperty and getProperty functions on the
     * NamedBean.
     */
    public final String propertyKey;
    /** Class for the property values. */
    public final Class valueClass;
    /** What should be displayed when a given Bean does not have this property set. */
    public final Object defaultValue;

    protected NamedBeanPropertyDescriptor(String propertyKey, Class valueClass, Object defaultValue) {
        this.propertyKey = propertyKey;
        this.valueClass = valueClass;
        this.defaultValue = defaultValue;
    }

    /**
     * Return user-visible text to render as a column header for the BeanTable representing this
     * setting.
     * @return localized string
     */
    public abstract String getColumnHeaderText();

    /**
     * Determines whether this property is editable.
     * @param bean the Bean object of the given row.
     * @return true for editable, false for disabled.
     */
    public abstract boolean isEditable(NamedBean bean);

    /**
     * Creates the stored representation of this property.
     * @param value the typed object for the property value
     * @return string representation to save.
     */
    public abstract String renderProperty(Object value);

    /**
     * Parses the saved representation of this property.
     * @param value the saved representation of this property
     * @return object of class valueClass of this property.
     */
    public abstract Object parseProperty(String value);
}
