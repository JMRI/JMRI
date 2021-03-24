package jmri.jmrit.logixng.util.parser;

import jmri.jmrit.logixng.SymbolTable;

/**
 * A parsed expression that always return false
 */
public class ExpressionNodeFalse implements ExpressionNode {

    public ExpressionNodeFalse() {
    }
    
    @Override
    public Object calculate(SymbolTable symbolTable) {
        return false;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return "false";
    }
    
}
