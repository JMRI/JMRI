package jmri;

/**
 * MemoryType defines the type of data to be stored in a Memory
 *
 * @author Daniel Bergqvist Copyright 2019
 */
public interface MemoryType {

    /**
     * Validate if this MemoryType supports this value.
     * 
     * @param value the value to be validated and possible adjusted
     * @return the value. It may have been adjusted.
     * @throws IllegalArgumentException if the value is invalid
     */
    public Object validate(Object value);

    /**
     * Return a formatted string of the value.
     */
    public String formatString(Object value);

    /**
     * Return a i18n name of this MemoryType.
     */
    public String getName();

    /**
     * Return the initial value of this MemoryType.
     * 
     * @return the initial value or null if no initial value is to be set
     */
    public Object getInitialValue();

    /**
     * Set the initial value of this MemoryType.
     * 
     * @param initialValue the initial value or null if no initial value is to be set
     */
    public void setInitialValue(Object initialValue);

}
