package jmri.util.managers;

import javax.annotation.Nonnull;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;

/**
 * This manager always throws an IllegalArgumentException from provide and get methods.
 * It is used for testing exception handling in tests.
 * 
 * The class name ends with 'Scaffold' to exclude it from the coverage statistics,
 * since it is part of the testing infrastructure.
 */
public class OBlockManagerThrowExceptionScaffold extends OBlockManager {

    public OBlockManagerThrowExceptionScaffold() {
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
