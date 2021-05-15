package jmri.jmrit.logixng.util.parser;

import jmri.jmrit.logixng.SymbolTable;

/**
 * A parsed expression
 */
public class ExpressionNodeIntegerNumber implements ExpressionNode {

    private final Token _token;
    private final long _value;
    
    public ExpressionNodeIntegerNumber(Token token) {
        _token = token;
        _value = Long.parseLong(token.getString());
    }
    
    @Override
    public Object calculate(SymbolTable symbolTable) {
        return _value;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return "IntNumber:"+_token.getString();
    }
    
}
