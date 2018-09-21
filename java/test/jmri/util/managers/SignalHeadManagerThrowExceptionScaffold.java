package jmri.util.managers;

import jmri.SignalHead;
import jmri.managers.AbstractSignalHeadManager;

/**
 * This manager always throws an IllegalArgumentException from provide and get methods.
 * It is used for testing exception handling in tests.
 * 
 * The class name ends with 'Scaffold' to exclude it from the coverage statistics,
 * since it is part of the testing infrastructure.
 */
public class SignalHeadManagerThrowExceptionScaffold extends AbstractSignalHeadManager {

    public SignalHeadManagerThrowExceptionScaffold() {
        super();
    }
    
    /** {@inheritDoc} */
    @Override
    public SignalHead getSignalHead(String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public SignalHead getBySystemName(String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public SignalHead getByUserName(String key) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
}
