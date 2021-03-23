package jmri.jmrit.logixng.util.parser;

import jmri.jmrit.logixng.SymbolTable;

/**
 * A parsed expression
 */
public class ExpressionNodeString implements ExpressionNode {

    private final Token _token;
    
    public ExpressionNodeString(Token token) {
        _token = token;
    }
    
    @Override
    public Object calculate(SymbolTable symbolTable) {
        return _token.getString();
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return "String:"+_token.getString();
    }
    
}
