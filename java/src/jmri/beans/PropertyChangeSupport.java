package jmri.beans;

import java.beans.PropertyChangeEvent;
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
public class PropertyChangeSupport extends PropertyChangeProviderImpl implements PropertyChangeFirer {

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
