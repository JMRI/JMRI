package jmri.jmrit.logixng;

import javax.annotation.Nonnull;
import jmri.JmriException;

/**
 * A LogixNG string action.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public interface StringAction extends Base {

    /**
     * Set a string value.
     * 
     * @param value the value.
     * @throws JmriException when an exception occurs
     */
    public void setValue(@Nonnull String value) throws JmriException;
    
}
