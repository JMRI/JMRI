package jmri.jmrit.logixng.util.parser;

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
    public Object calculate() {
        return _value;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return "IntNumber:"+_token.getString();
    }
    
}
