package jmri.jmrit.logixng.util.parser;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.util.TypeConversionUtil;

/**
 * A parsed expression
 */
public class ExpressionNodeBinaryOperator implements ExpressionNode {

    private final TokenType _tokenType;
    private final ExpressionNode _leftSide;
    private final ExpressionNode _rightSide;
    
    public ExpressionNodeBinaryOperator(TokenType tokenType, ExpressionNode leftSide, ExpressionNode rightSide) {
        _tokenType = tokenType;
        _leftSide = leftSide;
        _rightSide = rightSide;
        
        if (_rightSide == null) {
            throw new IllegalArgumentException("rightSide must not be null");
        }
        
        // Verify that the token is of the correct type
        switch (_tokenType) {
            case BINARY_OR:
            case BINARY_XOR:
            case BINARY_AND:
                if (_leftSide == null) {
                    throw new IllegalArgumentException("leftSide must not be null for operators BINARY AND, BINARY OR and BINARY XOR");
                }
                break;
                
            case BINARY_NOT:
                if (_leftSide != null) {
                    throw new IllegalArgumentException("leftSide must be null for operator BINARY NOT");
                }
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported binary operator: "+_tokenType.name());
        }
    }
    
    @Override
    public Object calculate(SymbolTable symbolTable) throws JmriException {
        
        Object leftValue = null;
        if (_tokenType != TokenType.BINARY_NOT) {
            // Left value must be calculated _before_ right value is calculated.
            // When a value is calculated, a method might be called, and the
            // order of these calls must be correct.
            leftValue = _leftSide.calculate(symbolTable);
        }
        if (leftValue == null) leftValue = false;
        
        Object rightValue = _rightSide.calculate(symbolTable);
        if (rightValue == null) rightValue = false;
        
        if (!TypeConversionUtil.isIntegerNumber(rightValue)) {
            throw new CalculateException(Bundle.getMessage("ArithmeticNotIntegerNumberError", rightValue));
        }
        long right = TypeConversionUtil.convertToLong(rightValue);
        
        if (_tokenType == TokenType.BINARY_NOT) {
            return ~ right;
        }
        
        if (!TypeConversionUtil.isIntegerNumber(leftValue)) {
            throw new CalculateException(Bundle.getMessage("ArithmeticNotIntegerNumberError", leftValue));
        }
        long left = TypeConversionUtil.convertToLong(leftValue);
        
        switch (_tokenType) {
            case BINARY_OR:
                return left | right;
                
            case BINARY_XOR:
                return left ^ right;
                
            case BINARY_AND:
                return left & right;
                
            default:
                throw new CalculateException("Unknown binary operator: "+_tokenType.name());
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        String operStr;
        switch (_tokenType) {
            case BINARY_OR:
                operStr = "|";
                break;
                
            case BINARY_XOR:
                operStr = "^";
                break;
                
            case BINARY_AND:
                operStr = "&";
                break;
                
            case BINARY_NOT:
                operStr = "~";
                break;
                
            default:
                throw new UnsupportedOperationException("Unknown arithmetic operator: "+_tokenType.name());
        }
        if (_leftSide != null) {
            return "("+_leftSide.getDefinitionString()+")" + operStr + "("+_rightSide.getDefinitionString()+")";
        } else {
            return operStr + "("+_rightSide.getDefinitionString()+")";
        }
    }
    
}
