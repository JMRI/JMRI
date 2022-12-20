package jmri.jmrit.logixng.util.parser;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.util.TypeConversionUtil;

/**
 * A parsed expression
 */
public class ExpressionNodeTernaryOperator implements ExpressionNode {

    private final Token _token = new Token(TokenType.TERNARY_TOKEN, "? :", 0);
    private final ExpressionNode _leftSide;
    private final ExpressionNode _middleSide;
    private final ExpressionNode _rightSide;

    public ExpressionNodeTernaryOperator(
            ExpressionNode leftSide, ExpressionNode middleSide, ExpressionNode rightSide) {
        _leftSide = leftSide;
        _middleSide = middleSide;
        _rightSide = rightSide;

        if (_leftSide == null) {
            throw new IllegalArgumentException("leftSide must not be null");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Token getToken() {
        return _token;
    }

    /** {@inheritDoc} */
    @Override
    public int getStartPos() {
        return _leftSide.getStartPos();
    }

    /** {@inheritDoc} */
    @Override
    public int getEndPos() {
        return _rightSide.getEndPos();
    }

    /** {@inheritDoc} */
    @Override
    public ExpressionNode getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0: return _leftSide;
            case 1: return _middleSide;
            case 2: return _rightSide;
            default: throw new IllegalArgumentException(
                    String.format("index has invalid value: %d", index));
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getChildCount() {
        return 3;
    }

    @Override
    public Object calculate(SymbolTable symbolTable) throws JmriException {

        Object leftValue = _leftSide.calculate(symbolTable);
        if (!(leftValue instanceof Boolean)) {
            if (TypeConversionUtil.isIntegerNumber(leftValue)) {
                // Convert to true or false
                leftValue = ((Number)leftValue).longValue() != 0;
            } else {
                throw new CalculateException(Bundle.getMessage("ArithmeticNotBooleanOrIntegerNumberError", leftValue));
            }
        }
        boolean left = (Boolean)leftValue;

        if (left) {
            return _middleSide.calculate(symbolTable);
        } else {
            return _rightSide.calculate(symbolTable);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return "("
                + _leftSide.getDefinitionString() + ")?("
                + _middleSide.getDefinitionString() + "):("
                + _rightSide.getDefinitionString() + ")";
    }

}
