package jmri.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Implementation of {@link java.beans.PropertyChangeSupport} and
 * {@link java.beans.VetoableChangeSupport} that can be extended by classes that
 * would normally need to implement the methods of PropertyChangeSupport and
 * VetoableChangeSupport independently.
 * <p>
 * This class is designed to support retrofitting an existing class with the
 * PropertyChangeProvider, PropertyChangeFirer, VetoableChangeProvider, and
 * VetoableChangeFirer interfaces without introducing the complexity of the
 * other implementations of PropertyChangeProvider in this package. When
 * designing new classes, it would be preferable to subclass
 * {@link ArbitraryBean}, {@link Bean}, or {@link ConstrainedBean} depending on
 * the design requirements of the new class.
 * <p>
 * This class is thread safe.
 *
 * @author Randall Wood Copyright 2020
 */
@ThreadSafe
public class VetoableChangeSupport extends PropertyChangeSupport implements VetoableChangeProvider, VetoableChangeFirer {

    /**
     * Provide a {@link java.beans.VetoableChangeSupport} helper.
     */
    protected final java.beans.VetoableChangeSupport vetoableChangeSupport = new java.beans.VetoableChangeSupport(this);

    /**
     * {@inheritDoc}
     */
    @Override
    public void addVetoableChangeListener(@CheckForNull VetoableChangeListener listener) {
        vetoableChangeSupport.addVetoableChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addVetoableChangeListener(@CheckForNull String propertyName, @CheckForNull VetoableChangeListener listener) {
        vetoableChangeSupport.addVetoableChangeListener(propertyName, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VetoableChangeListener[] getVetoableChangeListeners() {
        return vetoableChangeSupport.getVetoableChangeListeners();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
        return vetoableChangeSupport.getVetoableChangeListeners(propertyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeVetoableChangeListener(@CheckForNull VetoableChangeListener listener) {
        vetoableChangeSupport.removeVetoableChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeVetoableChangeListener(@CheckForNull String propertyName, @CheckForNull VetoableChangeListener listener) {
        vetoableChangeSupport.removeVetoableChangeListener(propertyName, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fireVetoableChange(String propertyName, boolean oldValue, boolean newValue) throws PropertyVetoException {
        vetoableChangeSupport.fireVetoableChange(propertyName, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fireVetoableChange(PropertyChangeEvent event) throws PropertyVetoException {
        vetoableChangeSupport.fireVetoableChange(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fireVetoableChange(String propertyName, int oldValue, int newValue) throws PropertyVetoException {
        vetoableChangeSupport.fireVetoableChange(propertyName, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fireVetoableChange(String propertyName, Object oldValue, Object newValue) throws PropertyVetoException {
        vetoableChangeSupport.fireVetoableChange(propertyName, oldValue, newValue);
    }

}
