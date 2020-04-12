package jmri.beans;

import java.beans.PropertyChangeEvent;

/**
 * Interface that defines the methods needed to fire property changes.
 *
 * @author Randall Wood Copyright 2020
 */
//This interface exists so that multiple implementations can inherit the Javadocs
public interface PropertyChangeFirer {

    /**
     * Fire an indexed property change. Despite being public due to limitations
     * in Java 8, this method should only be called by the subclasses of
     * implementing classes, as this will become a protected class when JMRI
     * requires Java 11 or newer.
     *
     * @param propertyName the programmatic name of the property that was
     *                     changed
     * @param index        the index of the property element that was changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     */
    void fireIndexedPropertyChange(String propertyName, int index, boolean oldValue, boolean newValue);

    /**
     * Fire an indexed property change. Despite being public due to limitations
     * in Java 8, this method should only be called by the subclasses of
     * implementing classes, as this will become a protected class when JMRI
     * requires Java 11 or newer.
     *
     * @param propertyName the programmatic name of the property that was
     *                     changed
     * @param index        the index of the property element that was changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     */
    void fireIndexedPropertyChange(String propertyName, int index, int oldValue, int newValue);

    /**
     * Fire an indexed property change. Despite being public due to limitations
     * in Java 8, this method should only be called by the subclasses of
     * implementing classes, as this will become a protected class when JMRI
     * requires Java 11 or newer.
     *
     * @param propertyName the programmatic name of the property that was
     *                     changed
     * @param index        the index of the property element that was changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     */
    void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue);

    /**
     * Fire a property change. Despite being public due to limitations
     * in Java 8, this method should only be called by the subclasses of
     * implementing classes, as this will become a protected class when JMRI
     * requires Java 11 or newer.
     *
     * @param propertyName the programmatic name of the property that was
     *                     changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     */
    void firePropertyChange(String propertyName, boolean oldValue, boolean newValue);

    /**
     * Fire a property change. Despite being public due to limitations
     * in Java 8, this method should only be called by the subclasses of
     * implementing classes, as this will become a protected class when JMRI
     * requires Java 11 or newer.
     *
     * @param event the PropertyChangeEvent to be fired
     */
    void firePropertyChange(PropertyChangeEvent event);

    /**
     * Fire a property change. Despite being public due to limitations
     * in Java 8, this method should only be called by the subclasses of
     * implementing classes, as this will become a protected class when JMRI
     * requires Java 11 or newer.
     *
     * @param propertyName the programmatic name of the property that was
     *                     changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     */
    void firePropertyChange(String propertyName, int oldValue, int newValue);

    /**
     * Fire a property change. Despite being public due to limitations
     * in Java 8, this method should only be called by the subclasses of
     * implementing classes, as this will become a protected class when JMRI
     * requires Java 11 or newer.
     *
     * @param propertyName the programmatic name of the property that was
     *                     changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     */
    void firePropertyChange(String propertyName, Object oldValue, Object newValue);

}
