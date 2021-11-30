package jmri.jmrit.logixng.util.parser;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.util.TypeConversionUtil;

/**
 * A parsed expression
 */
public class ExpressionNodeBooleanOperator implements ExpressionNode {

    private final TokenType _tokenType;
    private final ExpressionNode _leftSide;
    private final ExpressionNode _rightSide;
    
    public ExpressionNodeBooleanOperator(TokenType tokenType, ExpressionNode leftSide, ExpressionNode rightSide) {
        _tokenType = tokenType;
        _leftSide = leftSide;
        _rightSide = rightSide;
        
        if (_rightSide == null) {
            throw new IllegalArgumentException("rightSide must not be null");
        }
        
        // Verify that the token is of the correct type
        switch (_tokenType) {
            case BOOLEAN_OR:
            case BOOLEAN_AND:
                if (_leftSide == null) {
                    throw new IllegalArgumentException("leftSide must not be null for operators AND and OR");
                }
                break;
                
            case BOOLEAN_NOT:
                if (_leftSide != null) {
                    throw new IllegalArgumentException("leftSide must be null for operator NOT");
                }
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported boolean operator: "+_tokenType.name());
        }
    }
    
    @Override
    public Object calculate(SymbolTable symbolTable) throws JmriException {
        
        Object leftValue = null;
        if (_tokenType != TokenType.BOOLEAN_NOT) {
            // Left value must be calculated _before_ right value is calculated.
            // When a value is calculated, a method might be called, and the
            // order of these calls must be correct.
            // For example, if myArray is an array, the formula might be:
            //   myArray.add("Hello") || myArray.add(" ") || myArray.add("World!")
            leftValue = _leftSide.calculate(symbolTable);
        }
        if (leftValue == null) leftValue = false;
        
        Object rightValue = _rightSide.calculate(symbolTable);
        if (rightValue == null) rightValue = false;
        
        if (!(rightValue instanceof Boolean)) {
            if (TypeConversionUtil.isIntegerNumber(rightValue)) {
                // Convert to true or false
                rightValue = ((Number)rightValue).longValue() != 0;
            } else {
                throw new CalculateException(Bundle.getMessage("ArithmeticNotBooleanOrIntegerNumberError", rightValue));
            }
        }
        boolean right = (Boolean)rightValue;
        
        if (_tokenType == TokenType.BOOLEAN_NOT) {
            return ! right;
        }
        
        if (!(leftValue instanceof Boolean)) {
            if (TypeConversionUtil.isIntegerNumber(leftValue)) {
                // Convert to true or false
                leftValue = ((Number)leftValue).longValue() != 0;
            } else {
                throw new CalculateException(Bundle.getMessage("ArithmeticNotBooleanOrIntegerNumberError", leftValue));
            }
        }
        boolean left = (Boolean)leftValue;
        
        switch (_tokenType) {
            case BOOLEAN_OR:
                return left || right;
                
            case BOOLEAN_AND:
                return left && right;
                
            default:
                throw new CalculateException("Unknown boolean operator: "+_tokenType.name());
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        String operStr;
        switch (_tokenType) {
            case BOOLEAN_OR:
                operStr = "||";
                break;
                
            case BOOLEAN_AND:
                operStr = "&&";
                break;
                
            case BOOLEAN_NOT:
                operStr = "!";
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
