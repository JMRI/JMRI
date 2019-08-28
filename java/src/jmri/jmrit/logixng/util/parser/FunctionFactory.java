package jmri.jmrit.logixng.util.parser;

import java.util.Set;

/**
 * Factory class for DigitalExpression classes.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface FunctionFactory {

    /**
     * Get a set of classes that implements the Function interface.
     * 
     * @return a set of functions
     */
    public Set<Function> getFunctions();
    
}
