package jmri.jmrit.logixng.implementation;

/**
 * Common methods for the implementation classes.
 * These methods should never be called by the user.
 */
public interface InternalBase {

    /**
     * Register listeners if this object needs that.
     * <P>
     * Important: This method may be called more than once. Methods overriding
     * this method must ensure that listeners are not registered more than once.
     */
    public void registerListeners();
    
    /**
     * Unregister listeners if this object needs that.
     * <P>
     * Important: This method may be called more than once. Methods overriding
     * this method must ensure that listeners are not unregistered more than once.
     */
    public void unregisterListeners();
    
}
