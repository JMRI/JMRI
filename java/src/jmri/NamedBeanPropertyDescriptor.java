package jmri;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Describes metadata about a given property key for a NamedBean.
 * <p>
 * This metadata is used by the
 * BeanTable actions to display and edit the properties in question.
 *
 * @param <E> class of describer, e.g. Boolean.class
 * @author Balazs Racz Copyright (C) 2018
 */
public abstract class NamedBeanPropertyDescriptor<E> {
    
    /**
     * Key of the property, to be used in the setProperty and getProperty functions on the
     * NamedBean.
     */
    public final String propertyKey;
    
    /** What should be displayed when a given Bean does not have this property set. */
    public final E defaultValue;

    protected NamedBeanPropertyDescriptor(
            @Nonnull String propertyKey, @Nonnull E defaultValue) {
        this.propertyKey = Objects.requireNonNull(propertyKey);
        this.defaultValue = Objects.requireNonNull(defaultValue);
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
     * Get the Class of the property.
     * <p>
     * This class is used to find a matching Renderer for
     * the BeanTable column to display and edit the value of this property. 
     * For example returning Boolean.class will show a checkbox.
     * @return Class for the property values.
     */
    public Class<?> getValueClass() {
        return defaultValue.getClass();
    }
    
    /**
     * Equals based on Property Key and Default value Class.
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof NamedBeanPropertyDescriptor && obj.hashCode() == this.hashCode());
    }

    /**
     * hashCode based on Property Key and Default value Class.
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(this.propertyKey) * Objects.hashCode(getValueClass());
    }
    
}
