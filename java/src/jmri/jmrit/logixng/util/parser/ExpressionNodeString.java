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

    /** {@inheritDoc} */
    @Override
    public int getStartPos() {
        return _token.getPos();
    }

    /** {@inheritDoc} */
    @Override
    public int getEndPos() {
        // Add 2 since the string starts and ends with double quotes (")
        return _token.getPos() + _token.getString().length() + 2;
    }

    /** {@inheritDoc} */
    @Override
    public ExpressionNode getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public Object calculate(SymbolTable symbolTable) {
        return _token.getString();
    }

    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return "String:\""+_token.getString()+"\"";
    }

}
