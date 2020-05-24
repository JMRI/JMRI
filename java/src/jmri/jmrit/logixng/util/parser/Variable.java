package jmri.jmrit.logixng.util.parser;

import jmri.JmriException;

/**
 * A variable
 */
public interface Variable {

    public String getName();
    
    public Object getValue() throws JmriException;
    
}
