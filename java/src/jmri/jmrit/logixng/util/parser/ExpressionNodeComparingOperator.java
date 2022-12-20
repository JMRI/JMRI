package jmri.jmrit.logixng.util.parser;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;

/**
 * A parsed expression
 */
public class ExpressionNodeComparingOperator implements ExpressionNode {

    private final Token _token;
    private final ExpressionNode _leftSide;
    private final ExpressionNode _rightSide;

    // This constructor is used by tests
    public ExpressionNodeComparingOperator(TokenType tokenType,  ExpressionNode leftSide, ExpressionNode rightSide) {
        this(new Token(tokenType, "", 0), leftSide, rightSide);
    }

    public ExpressionNodeComparingOperator(Token token,  ExpressionNode leftSide, ExpressionNode rightSide) {
        _token = token;
        _leftSide = leftSide;
        _rightSide = rightSide;

        if (_leftSide == null) {
            throw new IllegalArgumentException("leftSide must not be null");
        }
        if (_rightSide == null) {
            throw new IllegalArgumentException("rightSide must not be null");
        }

        // Verify that the token is of the correct type
        switch (_token._tokenType) {
            case EQUAL:
            case NOT_EQUAL:
            case LESS_THAN:
            case LESS_OR_EQUAL:
            case GREATER_THAN:
            case GREATER_OR_EQUAL:
                break;

            default:
                throw new RuntimeException("Unknown comparing operator: "+_token._tokenType.name());
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
            case 1: return _rightSide;
            default: throw new IllegalArgumentException(
                    String.format("index has invalid value: %d", index));
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getChildCount() {
        return 2;
    }

    public Object calculateNull(Object left, Object right) throws JmriException {
        if ((left != null) && (right != null)) {
            throw new RuntimeException("This method is only valid if left and/or right is null");
        }

        switch (_token._tokenType) {
            case EQUAL:
                return left == right;
            case NOT_EQUAL:
                return left != right;
            case LESS_THAN:
                return (left == null) && (right != null);
            case LESS_OR_EQUAL:
                return left == null;
            case GREATER_THAN:
                return (left != null) && (right == null);
            case GREATER_OR_EQUAL:
                return right == null;

            default:
                throw new RuntimeException("Unknown arithmetic operator: "+_token._tokenType.name());
        }
    }

    @Override
    public Object calculate(SymbolTable symbolTable) throws JmriException {
        Object left = _leftSide.calculate(symbolTable);
        Object right = _rightSide.calculate(symbolTable);

        if ((left == null) || (right == null)) return calculateNull(left, right);

        // Convert a boolean value to an integer value. false = 0 and true = 1.
        if (left instanceof Boolean) {
            left = ((Boolean)left) ? 1 : 0;
        }
        if (right instanceof Boolean) {
            right = ((Boolean)right) ? 1 : 0;
        }

        // If the operands are not numbers, ensure that they are strings
        if ((!(left instanceof Number)) && (!(left instanceof String))) {
            left = left.toString();
        }
        if ((!(right instanceof Number)) && (!(right instanceof String))) {
            right = right.toString();
        }

        // Object.toString() might return null
        if ((left == null) || (right == null)) return calculateNull(left, right);

        // A number is always less than a String. If one operand is a number
        // and the other operand is a String, we can change the operands to
        // two integers to make the check easier.
        if ((left instanceof Number) && (!(right instanceof Number))) {
            left = 1;
            right = 2;
        } else if (!(left instanceof Number) && ((right instanceof Number))) {
            left = 2;
            right = 1;
        }

        if ((left instanceof Double) && (!(right instanceof Double))) {
            right = ((Number)left).doubleValue();
        }
        if ((right instanceof Double) && (!(left instanceof Double))) {
            left = ((Number)left).doubleValue();
        }

        if ((left instanceof Long) && (!(right instanceof Long))) {
            right = ((Number)right).longValue();
        }
        if ((right instanceof Long) && (!(left instanceof Long))) {
            left = ((Number)left).longValue();
        }

        if (left instanceof Number) {
            switch (_token._tokenType) {
                case EQUAL:
                    return left.equals(right);
                case NOT_EQUAL:
                    return ! left.equals(right);
                case LESS_THAN:
                    return ((Number) left).doubleValue() < ((Number) right).doubleValue();
                case LESS_OR_EQUAL:
                    return ((Number) left).doubleValue() <= ((Number) right).doubleValue();
                case GREATER_THAN:
                    return ((Number) left).doubleValue() > ((Number) right).doubleValue();
                case GREATER_OR_EQUAL:
                    return ((Number) left).doubleValue() >= ((Number) right).doubleValue();

                default:
                    throw new RuntimeException("Unknown arithmetic operator: "+_token._tokenType.name());
            }
        } else {
            switch (_token._tokenType) {
                case EQUAL:
                    return left.equals(right);
                case NOT_EQUAL:
                    return ! left.equals(right);
                case LESS_THAN:
                    return ((String)left).compareTo(((String)right)) < 0;
                case LESS_OR_EQUAL:
                    return ((String)left).compareTo(((String)right)) <= 0;
                case GREATER_THAN:
                    return ((String)left).compareTo(((String)right)) > 0;
                case GREATER_OR_EQUAL:
                    return ((String)left).compareTo(((String)right)) >= 0;

                default:
                    throw new RuntimeException("Unknown comparing operator: "+_token._tokenType.name());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        String operStr;
        switch (_token._tokenType) {
            case EQUAL:
                operStr = "==";
                break;

            case NOT_EQUAL:
                operStr = "!=";
                break;

            case LESS_THAN:
                operStr = "<";
                break;

            case LESS_OR_EQUAL:
                operStr = "<=";
                break;

            case GREATER_THAN:
                operStr = ">";
                break;

            case GREATER_OR_EQUAL:
                operStr = ">=";
                break;

            default:
                throw new RuntimeException("Unknown comparing operator: "+_token._tokenType.name());
        }
        return "("+_leftSide.getDefinitionString()+")" + operStr + "("+_rightSide.getDefinitionString()+")";
    }

}
