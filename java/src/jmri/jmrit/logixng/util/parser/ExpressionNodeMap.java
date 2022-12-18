package jmri.jmrit.logixng.util.parser;

import java.util.*;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;

/**
 * A parsed expression
 *
 * @author Daniel Bergqvist 2021
 */
public class ExpressionNodeMap implements ExpressionNodeWithParameter {

    private final Token _token;
    private final ExpressionNode _exprNode;
    private final int _endPos;

    public ExpressionNodeMap(Token token, ExpressionNode exprNode, int endPos) throws FunctionNotExistsException {
        _token = token;
        _exprNode = exprNode;
        _endPos = endPos;
    }

    /** {@inheritDoc} */
    @Override
    public int getStartPos() {
        return _token.getPos();
    }

    /** {@inheritDoc} */
    @Override
    public int getEndPos() {
        return _endPos;
    }

    @Override
    public ExpressionNode getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        if (index == 0) {
            return _exprNode;
        } else {
            throw new IllegalArgumentException(
                    String.format("index has invalid value: %d", index));
        }
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object calculate(Object parameter, SymbolTable symbolTable) throws JmriException {
        if (parameter == null) throw new NullPointerException("Parameter is null");
        if (!(parameter instanceof Map)) throw new IllegalArgumentException("Parameter is not a Map");

        Object index = _exprNode.calculate(symbolTable);

        return ((Map<Object,Object>)parameter).get(index);
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
        if (parameter == null) throw new NullPointerException("Parameter is null");
        if (!(parameter instanceof Map)) throw new IllegalArgumentException("Parameter is not a Map");

        Object index = _exprNode.calculate(symbolTable);

        ((Map<Object,Object>)parameter).put(index, value);
    }

    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        StringBuilder str = new StringBuilder();
        str.append("{");
        str.append(_exprNode.getDefinitionString());
        str.append("}");
        return str.toString();
    }

}
