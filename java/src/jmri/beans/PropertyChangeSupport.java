package jmri.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.annotation.CheckForNull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Implementation of {@link java.beans.PropertyChangeSupport} that can be
 * extended by classes that would normally need to implement the methods of
 * PropertyChangeSupport independently.
 * <p>
 * This class is designed to support retrofitting an existing class with the
 * PropertyChangeProvider and PropertyChangeFirer interfaces without introducing
 * the complexity of the other implementations of PropertyChangeProvider in this
 * package. When designing new classes, it would be preferable to subclass
 * {@link ArbitraryBean}, {@link Bean}, or {@link ConstrainedBean} depending on
 * the design requirements of the new class.
 * <p>
 * This class is thread safe.
 *
 * @author Randall Wood Copyright 2020
 */
@ThreadSafe
public class PropertyChangeSupport implements PropertyChangeProvider, PropertyChangeFirer {

    /**
     * Provide a {@link java.beans.PropertyChangeSupport} helper.
     */
    protected final java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

    /** {@inheritDoc} */
    @Override
    public void addPropertyChangeListener(@CheckForNull PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void addPropertyChangeListener(@CheckForNull String propertyName,
            @CheckForNull PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /** {@inheritDoc} */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return propertyChangeSupport.getPropertyChangeListeners();
    }

    /** {@inheritDoc} */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(@CheckForNull String propertyName) {
        return propertyChangeSupport.getPropertyChangeListeners(propertyName);
    }

    /** {@inheritDoc} */
    @Override
    public void removePropertyChangeListener(@CheckForNull PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void removePropertyChangeListener(@CheckForNull String propertyName,
            @CheckForNull PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fireIndexedPropertyChange(String propertyName, int index, boolean oldValue, boolean newValue) {
        propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fireIndexedPropertyChange(String propertyName, int index, int oldValue, int newValue) {
        propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
        propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void firePropertyChange(PropertyChangeEvent event) {
        propertyChangeSupport.firePropertyChange(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

}
