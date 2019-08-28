package jmri.jmrit.logixng;

/**
 * A LogixNG male socket.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface MaleSocket extends Debugable {

    public enum ErrorHandlingType {
        
        SHOW_DIALOG_BOX,
        LOG_ERROR,
        LOG_ERROR_ONCE,
        THROW,
    }
    
    /**
     * Set whenether this male socket is enabled or disabled.
     * <P>
     * This method must call registerListeners() / unregisterListeners().
     * 
     * @param enable true if this male socket should be enabled, false otherwise
     */
    public void setEnabled(boolean enable);
    
    /**
     * Determines whether this male socket is enabled.
     * 
     * @return true if the male socket is enabled, false otherwise
     */
    public boolean isEnabled();
    
    /**
     * Get the object that this male socket holds.
     * This method is used when the object is going to be configured.
     * 
     * @return the object this male socket holds
     */
    public Base getObject();

    /** {@inheritDoc} */
    @Override
    default public void setup() {
        getObject().setup();
    }

}
