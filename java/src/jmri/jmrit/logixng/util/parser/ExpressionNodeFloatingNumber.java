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

    /** {@inheritDoc} */
    @Override
    public Token getToken() {
        return _token;
    }

    /** {@inheritDoc} */
    @Override
    public int getStartPos() {
        return _token.getPos();
    }

    /** {@inheritDoc} */
    @Override
    public int getEndPos() {
        return _token.getPos() + _token.getString().length();
    }

    @Override
    public ExpressionNode getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
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
