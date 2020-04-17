package jmri.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

/**
 * Interface that defines the methods needed to fire vetoable property changes.
 *
 * @author Randall Wood Copyright 2020
 */
// This interface exists so that multiple implementations can inherit the Javadocs
public interface VetoableChangeFirer {

    /**
     * Fire a property change. Despite being public due to limitations in Java
     * 8, this method should only be called by the subclasses of implementing
     * classes, as this will become a protected class when JMRI requires Java 11
     * or newer.
     *
     * @param propertyName the programmatic name of the property that was
     *                     changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     * @throws PropertyVetoException if one of listeners vetoes the property
     *                               update
     */
    void fireVetoableChange(String propertyName, boolean oldValue, boolean newValue) throws PropertyVetoException;

    /**
     * Fire a property change. Despite being public due to limitations in Java
     * 8, this method should only be called by the subclasses of implementing
     * classes, as this will become a protected class when JMRI requires Java 11
     * or newer.
     *
     * @param event the PropertyChangeEvent to be fired
     * @throws PropertyVetoException if one of listeners vetoes the property
     *                               update
     */
    void fireVetoableChange(PropertyChangeEvent event) throws PropertyVetoException;

    /**
     * Fire a property change. Despite being public due to limitations in Java
     * 8, this method should only be called by the subclasses of implementing
     * classes, as this will become a protected class when JMRI requires Java 11
     * or newer.
     *
     * @param propertyName the programmatic name of the property that was
     *                     changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     * @throws PropertyVetoException if one of listeners vetoes the property
     *                               update
     */
    void fireVetoableChange(String propertyName, int oldValue, int newValue) throws PropertyVetoException;

    /**
     * Fire a property change. Despite being public due to limitations in Java
     * 8, this method should only be called by the subclasses of implementing
     * classes, as this will become a protected class when JMRI requires Java 11
     * or newer.
     *
     * @param propertyName the programmatic name of the property that was
     *                     changed
     * @param oldValue     the old value of the property
     * @param newValue     the new value of the property
     * @throws PropertyVetoException if one of listeners vetoes the property
     *                               update
     */
    void fireVetoableChange(String propertyName, Object oldValue, Object newValue) throws PropertyVetoException;

}
