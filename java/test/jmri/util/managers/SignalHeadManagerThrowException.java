package jmri.util.managers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jmri.SignalHead;
import jmri.managers.AbstractSignalHeadManager;

/**
 * This manager always throws an IllegalArgumentException from provide and get methods.
 * It is used for testing exception handling in tests.
 */
public class SignalHeadManagerThrowException extends AbstractSignalHeadManager {

    public SignalHeadManagerThrowException() {
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
