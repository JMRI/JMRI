package jmri.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.annotation.CheckForNull;

/**
 * Minimal implementation of {@link PropertyChangeProvider} that only implements
 * the required methods, and does not provide abstract support for notifying
 * {@link PropertyChangeListener}s of {@link java.beans.PropertyChangeEvent}s.
 * <p>
 * This class is designed to support retrofitting an existing class with the
 * PropertyChangeProvider interface without introducing the complexity of the
 * other implementations of PropertyChangeProvider in this package. When
 * designing new classes, it would be preferable to subclass
 * {@link ArbitraryBean}, {@link Bean}, or {@link ConstrainedBean} depending on
 * the design requirements of the new class.
 * <p>
 * This class is thread safe.
 * 
 * @author Randall Wood Copyright 2020
 * @see ArbitraryBean
 * @see Bean
 * @see ConstrainedBean
 */
public class PropertyChangeProviderImpl implements PropertyChangeProvider {

    /**
     * Provide a {@link java.beans.PropertyChangeSupport} helper.
     */
    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

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

}
