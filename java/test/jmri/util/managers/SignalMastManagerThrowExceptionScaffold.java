package jmri.util.managers;

import javax.annotation.Nonnull;
import jmri.SignalMast;
import jmri.managers.DefaultSignalMastManager;

/**
 * This manager always throws an IllegalArgumentException from provide and get methods.
 * It is used for testing exception handling in tests.
 * 
 * The class name ends with 'Scaffold' to exclude it from the coverage statistics,
 * since it is part of the testing infrastructure.
 */
public class SignalMastManagerThrowExceptionScaffold extends DefaultSignalMastManager {

    public SignalMastManagerThrowExceptionScaffold() {
        super();
    }
    
    /** {@inheritDoc} */
    @Override
    public SignalMast provideSignalMast(@Nonnull String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public SignalMast getSignalMast(@Nonnull String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public SignalMast getBySystemName(@Nonnull String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public SignalMast getByUserName(String key) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
}
