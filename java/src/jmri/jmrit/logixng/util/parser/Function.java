package jmri.jmrit.logixng.util.parser;

import java.util.List;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;

/**
 * Definition of a function used in expressions.
 * 
 * @author Daniel Bergqvist 2019
 */
public interface Function {

    /**
     * Get the module of the function, for example "Math" or "Conversion".
     * @return the module name
     */
    public String getModule();
    
    /**
     * Get the descriptions of the constants in the module.
     * @return the description of the constants
     */
    public String getConstantDescriptions();
    
    /**
     * Get name of the function, for example "sin" or "int"
     * @return the name
     */
    public String getName();
    
    /**
     * Calculate the function
     * @param symbolTable the symbol table
     * @param parameterList a list of parameters for the function
     * @return the result
     * @throws JmriException in case of an error
     */
    public Object calculate(SymbolTable symbolTable, List<ExpressionNode> parameterList)
            throws JmriException;
    
    /**
     * Get the description of the function in Markdown format
     * @return the description
     */
    public String getDescription();
    
}
