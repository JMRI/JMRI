package jmri.jmrit.logixng.util.parser;

import jmri.jmrit.logixng.SymbolTable;

/**
 * A parsed expression that always return false
 */
public class ExpressionNodeFalse implements ExpressionNode {

    private final Token _token;

    // This constructor is used by tests
    public ExpressionNodeFalse() {
        _token = new Token();
    }

    public ExpressionNodeFalse(Token token) {
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
        return _token.getPos() + _token.getString().length();
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
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return "false";
    }

}
