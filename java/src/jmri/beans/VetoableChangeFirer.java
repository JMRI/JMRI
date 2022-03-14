package jmri.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

/**
 * Interface that defines the methods needed to fire vetoable property changes.
 *
 * @author Randall Wood Copyright 2020
 */
// This interface exists so that multiple implementations can inherit the Javadocs
interface VetoableChangeFirer {

    /**
     * Fire a property change.
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
     * Fire a property change.
     *
     * @param event the PropertyChangeEvent to be fired
     * @throws PropertyVetoException if one of listeners vetoes the property
     *                               update
     */
    void fireVetoableChange(PropertyChangeEvent event) throws PropertyVetoException;

    /**
     * Fire a property change.
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
     * Fire a property change.
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
