package jmri.util.managers;

import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.managers.DefaultMemoryManager;

/**
 * This manager always throws an IllegalArgumentException from provide and get methods.
 * It is used for testing exception handling in tests.
 * 
 * The class name ends with 'Scaffold' to exclude it from the coverage statistics,
 * since it is part of the testing infrastructure.
 */
public class MemoryManagerThrowExceptionScaffold extends DefaultMemoryManager {

    public MemoryManagerThrowExceptionScaffold() {
        super(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }
    
    /** {@inheritDoc} */
    @Override
    protected Memory createNewMemory(String systemName, String userName) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Memory provideMemory(@Nonnull String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Memory getMemory(@Nonnull String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Memory getBySystemName(@Nonnull String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Memory getByUserName(String key) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Memory newMemory(@Nonnull String systemName, @CheckForNull String userName) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
}
