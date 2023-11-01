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
    String getModule();
    
    /**
     * Get a set of classes that implements the Function interface.
     * 
     * @return a set of functions
     */
    Set<Function> getFunctions();
    
    /**
     * Get a set of classes that implements the Constant interface.
     * 
     * @return a set of constants
     */
    Set<Constant> getConstants();
    
    /**
     * Get the description of the constants in Markdown format
     * @return the description of the constants
     */
    String getConstantDescription();
    
}
