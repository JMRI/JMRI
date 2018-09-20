package jmri.util.managers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;

/**
 * This manager always throws an IllegalArgumentException from provide and get methods.
 * It is used for testing exception handling in tests.
 */
public class OBlockManagerThrowException extends OBlockManager {

    public OBlockManagerThrowException() {
        super();
    }
    
    /** {@inheritDoc} */
    @Override
    public OBlock createNewOBlock(String systemName, String userName) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull public OBlock provideOBlock(String name) throws IllegalArgumentException {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public OBlock getOBlock(@Nonnull String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public OBlock getBySystemName(@Nonnull String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public OBlock getByUserName(String key) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
}
