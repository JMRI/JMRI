package jmri.jmrit.logixng.util.parser;

import java.lang.reflect.Array;
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

    @SuppressWarnings("unchecked")
    @Override
    public Object calculate(Object parameter, SymbolTable symbolTable) throws JmriException {
        if (parameter == null) throw new NullPointerException("Parameter is null");

        int index = (int) TypeConversionUtil.convertToLong(_exprNode.calculate(symbolTable));

        // JSON array node
        if (parameter instanceof com.fasterxml.jackson.databind.node.ArrayNode) {
            return ((com.fasterxml.jackson.databind.node.ArrayNode)parameter).get(index);
        }

        if (parameter.getClass().isArray()) {
            return Array.get(parameter, index);
        } else if (parameter instanceof List) {
            return ((List<Object>)parameter).get(index);
        } else {
            throw new IllegalArgumentException("Parameter is not a List nor an array");
        }
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
    @SuppressWarnings("unchecked")
    @Override
    public void assignValue(Object parameter, SymbolTable symbolTable, Object value) throws JmriException {
        int index = (int) TypeConversionUtil.convertToLong(_exprNode.calculate(symbolTable));

        if (parameter == null) throw new NullPointerException("Parameter is null");

        if (parameter.getClass().isArray()) {
            Array.set(parameter, index, value);
        } else if (parameter instanceof List) {
            ((List<Object>)parameter).set(index, value);
        } else {
            throw new IllegalArgumentException("Parameter is not a List nor an array");
        }
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
