package jmri.jmrit.logixng.util.parser;

import java.util.*;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.util.TypeConversionUtil;

/**
 * A parsed expression
 * 
 * @author Daniel Bergqvist 2021
 */
public class ExpressionNodeArray implements ExpressionNodeWithParameter {

    private final ExpressionNode _exprNode;
    
    public ExpressionNodeArray(ExpressionNode exprNode) throws FunctionNotExistsException {
        _exprNode = exprNode;
    }
    
    @Override
    public Object calculate(Object parameter, SymbolTable symbolTable) throws JmriException {
        if (parameter == null) throw new NullPointerException("Parameter is null");
        if (!(parameter instanceof List)) throw new IllegalArgumentException("Parameter is not a List");
        
        int index = (int) TypeConversionUtil.convertToLong(_exprNode.calculate(symbolTable));
        
        return ((List<Object>)parameter).get(index);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean canBeAssigned() {
        // We don't know so we assume that it is.
        return true;
    }
    
    /**
     * Assign a value to this expression from a parameter.
     * @param parameter the parameter
     * @param symbolTable the symbol table
     * @param value the value to assign
     * @throws jmri.JmriException if an error occurs
     */
    @Override
    public void assignValue(Object parameter, SymbolTable symbolTable, Object value) throws JmriException {
        if (parameter == null) throw new NullPointerException("Parameter is null");
        if (!(parameter instanceof List)) throw new IllegalArgumentException("Parameter is not a List");
        
        int index = (int) TypeConversionUtil.convertToLong(_exprNode.calculate(symbolTable));
        
        ((List<Object>)parameter).set(index, value);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        StringBuilder str = new StringBuilder();
        str.append("[");
        str.append(_exprNode.getDefinitionString());
        str.append("]");
        return str.toString();
    }
    
}
