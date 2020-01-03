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
     * @return the female socket
     */
    @Nonnull
    public FemaleSocket create(@Nonnull Base parent, @Nonnull FemaleSocketListener listener);
    
}
