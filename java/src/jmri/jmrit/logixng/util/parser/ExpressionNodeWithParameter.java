package jmri.jmrit.logixng.util.parser;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;

/**
 * An expression that takes a parameter.
 * This interface are used for ExpressionNodeFunction
 */
public interface ExpressionNodeWithParameter extends ExpressionNode {

    /** {@inheritDoc} */
    @Override
    public default Object calculate(SymbolTable symbolTable) throws JmriException {
        throw new UnsupportedOperationException("Not implemented. Use calculate(parameter, symbolTable) instead");
    }
    
    /**
     * Calculate the expression from a parameter.
     * @param parameter the parameter
     * @param symbolTable the symbol table
     * @return the result
     * @throws JmriException if an error occurs
     */
    public Object calculate(Object parameter, SymbolTable symbolTable) throws JmriException;
    
    /**
     * Assign a value to this expression from a parameter.
     * @param parameter the parameter
     * @param symbolTable the symbol table
     * @param value the value to assign
     * @throws jmri.JmriException if an error occurs
     */
    public default void assignValue(Object parameter, SymbolTable symbolTable, Object value) throws JmriException {
        throw new UnsupportedOperationException("This expression can't be assigned");
    }
    
}
