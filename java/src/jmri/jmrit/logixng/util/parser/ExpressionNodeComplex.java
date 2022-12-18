package jmri.jmrit.logixng.util.parser;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;

/**
 * A parsed expression
 */
public class ExpressionNodeComplex implements ExpressionNode {

    private final ExpressionNode _firstNode;
    private final ExpressionNodeWithParameter _secondNode;

    public ExpressionNodeComplex(
            ExpressionNode firstNode,
            ExpressionNodeWithParameter secondNode) {
        _firstNode = firstNode;
        _secondNode = secondNode;
    }

    /** {@inheritDoc} */
    @Override
    public int getStartPos() {
        return _firstNode.getStartPos();
    }

    /** {@inheritDoc} */
    @Override
    public int getEndPos() {
        return _secondNode.getEndPos();
    }

    /** {@inheritDoc} */
    @Override
    public ExpressionNode getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0: return _firstNode;
            case 1: return _secondNode;
            default: throw new IllegalArgumentException(
                    String.format("index has invalid value: %d", index));
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getChildCount() {
        return 2;
    }

    @Override
    public Object calculate(SymbolTable symbolTable) throws JmriException {
        Object parameter = _firstNode.calculate(symbolTable);
        return _secondNode.calculate(parameter, symbolTable);
    }

    /** {@inheritDoc} */
    @Override
    public boolean canBeAssigned() {
        return _secondNode.canBeAssigned();
    }

    /** {@inheritDoc} */
    @Override
    public void assignValue(SymbolTable symbolTable, Object value) throws JmriException {
        Object parameter = _firstNode.calculate(symbolTable);
        _secondNode.assignValue(parameter, symbolTable, value);
    }

    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return _firstNode.getDefinitionString()
                + "->"
                + _secondNode.getDefinitionString();
    }

}
