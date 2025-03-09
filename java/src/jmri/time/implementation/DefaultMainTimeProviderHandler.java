package jmri.time.implementation;

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


    /** {@inheritDoc} */
    @Override
    public TimeProvider getCurrentTimeProvider() {
        return _showPrimaryTimeProvider ? _primaryTimeProvider : _secondaryTimeProvider;
    }

    /** {@inheritDoc} */
    @Override
    public void selectPrimaryTimeProvider(boolean select) {
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

}
