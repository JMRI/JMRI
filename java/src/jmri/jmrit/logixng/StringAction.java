package jmri.jmrit.logixng;

import javax.annotation.Nonnull;

/**
 * A LogixNG string action.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public interface StringAction extends Base {

    /**
     * Set a string value.
     */
    public void setValue(@Nonnull String value) throws Exception;
    
}
