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
