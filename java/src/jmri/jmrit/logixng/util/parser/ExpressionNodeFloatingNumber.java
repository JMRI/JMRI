package jmri.jmrit.logixng.util.parser;

import jmri.jmrit.logixng.SymbolTable;

/**
 * A parsed expression
 */
public class ExpressionNodeFloatingNumber implements ExpressionNode {

    private final Token _token;
    private final double _value;
    
    public ExpressionNodeFloatingNumber(Token token) {
        _token = token;
        _value = Double.parseDouble(token.getString());
    }
    
    @Override
    public Object calculate(SymbolTable symbolTable) {
        return _value;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return "FloatNumber:"+_token.getString();
    }
    
}
