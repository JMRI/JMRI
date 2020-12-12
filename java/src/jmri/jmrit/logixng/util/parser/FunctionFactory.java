package jmri.jmrit.logixng.util.parser;

import java.util.Set;

/**
 * Factory class for Function classes.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface FunctionFactory {

    /**
     * Get the module of the functions in this factory, for example "Math" or
     * "Conversion".
     * @return the module name
     */
    public String getModule();
    
    /**
     * Get a set of classes that implements the Function interface.
     * 
     * @return a set of functions
     */
    public Set<Function> getFunctions();
    
}
