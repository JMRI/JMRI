package jmri.jmrit.logixng.util.parser;

import jmri.jmrit.logixng.SymbolTable;

/**
 * A parsed expression that always return true
 */
public class ExpressionNodeTrue implements ExpressionNode {

    public ExpressionNodeTrue() {
    }
    
    @Override
    public Object calculate(SymbolTable symbolTable) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return "true";
    }
    
}
