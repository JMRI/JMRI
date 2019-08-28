package jmri.jmrit.logixng;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * A factory for creation of FemaleSockets.
 */
public interface FemaleSocketFactory {
    
    /**
     * Create a FemaleSocket.
     * 
     * @param parent the parent of this object
     * @param listener the listener of the female socket
     */
    public FemaleSocket create(Base parent, FemaleSocketListener listener);
    
    /**
     * Get a named bean by system name.
     * The bean must be wrapped in a male socket.
     */
    @CheckReturnValue
    @CheckForNull
    public MaleSocket getBeanBySystemName(@Nonnull String systemName);
    
}
