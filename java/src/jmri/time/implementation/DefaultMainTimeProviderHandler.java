package jmri.time.implementation;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import jmri.time.TimeProvider;
import jmri.time.MainTimeProviderHandler;

/**
 * Default main time provider handler.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class DefaultMainTimeProviderHandler implements MainTimeProviderHandler {

    private boolean _showPrimaryTimeProvider = true;
    private TimeProvider _primaryTimeProvider;
    private TimeProvider _secondaryTimeProvider;
    private final List<PropertyChangeListener> _listeners = new ArrayList<>();


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
        _primaryTimeProvider = clock;
    }

    /** {@inheritDoc} */
    @Override
    public TimeProvider getPrimaryTimeProvider() {
        return _primaryTimeProvider;
    }

    /** {@inheritDoc} */
    @Override
    public void setSecondaryTimeProvider(TimeProvider clock) {
        _secondaryTimeProvider = clock;
    }

    /** {@inheritDoc} */
    @Override
    public TimeProvider getSecondaryTimeProvider() {
        return _secondaryTimeProvider;
    }

    @Override
    public void addMinuteChangeListener(PropertyChangeListener l) {
        _listeners.add(l);
    }

    @Override
    public void removeMinuteChangeListener(PropertyChangeListener l) {
        _listeners.remove(l);
    }

    @Override
    public PropertyChangeListener[] getMinuteChangeListeners() {
        return _listeners.toArray(PropertyChangeListener[]::new);
    }

}
