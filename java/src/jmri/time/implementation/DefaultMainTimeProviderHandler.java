package jmri.time.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.SwingPropertyChangeSupport;

import jmri.time.TimeProvider;
import jmri.time.MainTimeProviderHandler;

/**
 * Default main time provider handler.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class DefaultMainTimeProviderHandler
        implements MainTimeProviderHandler, PropertyChangeListener {

    private final SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);

    private boolean _showPrimaryTimeProvider = true;
    private TimeProvider _primaryTimeProvider;
    private TimeProvider _secondaryTimeProvider;


    /** {@inheritDoc} */
    @Override
    public TimeProvider getCurrentTimeProvider() {
        return _showPrimaryTimeProvider ? _primaryTimeProvider : _secondaryTimeProvider;
    }

    /** {@inheritDoc} */
    @Override
    public void setUsePrimaryTimeProvider(boolean select) {
        this._showPrimaryTimeProvider = select;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPrimaryTimeProviderSelected() {
        return _showPrimaryTimeProvider;
    }

    /** {@inheritDoc} */
    @Override
    public void setPrimaryTimeProvider(TimeProvider clock) {
        if (_primaryTimeProvider != null) {
            _primaryTimeProvider.removePropertyChangeListener(this);
        }
        _primaryTimeProvider = clock;

        if (_primaryTimeProvider != null) {
            _primaryTimeProvider.addPropertyChangeListener(this);
        }
    }

    /** {@inheritDoc} */
    @Override
    public TimeProvider getPrimaryTimeProvider() {
        return _primaryTimeProvider;
    }

    /** {@inheritDoc} */
    @Override
    public void setSecondaryTimeProvider(TimeProvider clock) {
        if (_secondaryTimeProvider != null) {
            _secondaryTimeProvider.removePropertyChangeListener(this);
        }
        _secondaryTimeProvider = clock;

        if (_secondaryTimeProvider != null) {
            _secondaryTimeProvider.addPropertyChangeListener(this);
        }
    }

    /** {@inheritDoc} */
    @Override
    public TimeProvider getSecondaryTimeProvider() {
        return _secondaryTimeProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return propertyChangeSupport.getPropertyChangeListeners();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return propertyChangeSupport.getPropertyChangeListeners(propertyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Only fire events from the current time provider
        if (evt.getSource() == this.getCurrentTimeProvider()) {
            propertyChangeSupport.firePropertyChange(evt);
        }
    }

}
