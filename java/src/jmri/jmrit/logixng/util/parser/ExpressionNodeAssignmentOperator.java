package jmri.jmrit.logixng.util.parser;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.util.TypeConversionUtil;

/**
 * A parsed expression
 */
public class ExpressionNodeAssignmentOperator implements ExpressionNode {

    private final TokenType _tokenType;
    private final ExpressionNode _leftSide;
    private final ExpressionNode _rightSide;
    
    
    public ExpressionNodeAssignmentOperator(TokenType tokenType, ExpressionNode leftSide, ExpressionNode rightSide) {
        _tokenType = tokenType;
        _leftSide = leftSide;
        _rightSide = rightSide;
        
        if (_leftSide == null) {
            throw new IllegalArgumentException("leftSide must not be null");
        }
        
        if (! _leftSide.canBeAssigned()) {
            throw new IllegalArgumentException("leftSide must assignable");
        }
        
        // Verify that the token is of the correct type
        switch (_tokenType) {
            case ASSIGN:
            case ASSIGN_ADD:
            case ASSIGN_SUBTRACKT:
                break;
                
            case ASSIGN_MULTIPLY:
            case ASSIGN_DIVIDE:
            case ASSIGN_MODULO:
                if (_leftSide == null) {
                    throw new IllegalArgumentException("leftSide must not be null for operators *, / and %");
                }
                break;
                
            default:
                throw new IllegalArgumentException("Unknown arithmetic operator: "+_tokenType.name());
        }
    }
    
    
    private Object add(Object left, Object right) throws CalculateException {
        if (TypeConversionUtil.isIntegerNumber(left)
                && TypeConversionUtil.isIntegerNumber(right)) {
            return ((Number)left).longValue() + ((Number)right).longValue();

        } else if (TypeConversionUtil.isFloatingNumber(left)
                && TypeConversionUtil.isFloatingNumber(right)) {
            return ((Number)left).doubleValue() + ((Number)right).doubleValue();

        } else {
            if (TypeConversionUtil.isString(left) && TypeConversionUtil.isString(right)) {
                return ((String)left) + ((String)right);
            } else {
                throw new CalculateException(Bundle.getMessage("ArithmeticNotCompatibleOperands", left, right));
            }
        }
    }
    
    
    private Object subtract(Object left, Object right) throws CalculateException {
        if (TypeConversionUtil.isIntegerNumber(left)) {
            if (TypeConversionUtil.isIntegerNumber(right)) {
                return ((Number)left).longValue() - ((Number)right).longValue();
            } else if (TypeConversionUtil.isFloatingNumber(right)) {
                return ((Number)left).doubleValue() - ((Number)right).doubleValue();
            } else {
                throw new CalculateException(Bundle.getMessage("ArithmeticNotNumberError", right));
            }
        } else if (TypeConversionUtil.isFloatingNumber(left)) {
            if (TypeConversionUtil.isFloatingNumber(right)) {
                return ((Number)left).doubleValue() - ((Number)right).doubleValue();
            } else {
                throw new CalculateException(Bundle.getMessage("ArithmeticNotNumberError", right));
            }
        } else {
            throw new CalculateException(Bundle.getMessage("ArithmeticNotNumberError", left));
        }
    }
    
    
    private Object multiply(Object left, Object right) throws CalculateException {
        if (TypeConversionUtil.isIntegerNumber(left)) {
            if (TypeConversionUtil.isIntegerNumber(right)) {
                return ((Number)left).longValue() * ((Number)right).longValue();
            } else if (TypeConversionUtil.isFloatingNumber(right)) {
                return ((Number)left).doubleValue() * ((Number)right).doubleValue();
            } else {
                throw new CalculateException(Bundle.getMessage("ArithmeticNotNumberError", right));
            }
        } else if (TypeConversionUtil.isFloatingNumber(left)) {
            if (TypeConversionUtil.isFloatingNumber(right)) {
                return ((Number)left).doubleValue() * ((Number)right).doubleValue();
            } else {
                throw new CalculateException(Bundle.getMessage("ArithmeticNotNumberError", right));
            }
        } else {
            throw new CalculateException(Bundle.getMessage("ArithmeticNotNumberError", left));
        }
    }
    
    
    private Object divide(Object left, Object right) throws CalculateException {
        if (TypeConversionUtil.isIntegerNumber(left)) {
            if (TypeConversionUtil.isIntegerNumber(right)) {
                return ((Number)left).longValue() / ((Number)right).longValue();
            } else if (TypeConversionUtil.isFloatingNumber(right)) {
                return ((Number)left).doubleValue() / ((Number)right).doubleValue();
            } else {
                throw new CalculateException(Bundle.getMessage("ArithmeticNotNumberError", right));
            }
        } else if (TypeConversionUtil.isFloatingNumber(left)) {
            if (TypeConversionUtil.isFloatingNumber(right)) {
                return ((Number)left).doubleValue() / ((Number)right).doubleValue();
            } else {
                throw new CalculateException(Bundle.getMessage("ArithmeticNotNumberError", right));
            }
        } else {
            throw new CalculateException(Bundle.getMessage("ArithmeticNotNumberError", left));
        }
    }
    
    
    private Object modulo(Object left, Object right) throws CalculateException {
        if (TypeConversionUtil.isIntegerNumber(left)) {
            if (TypeConversionUtil.isIntegerNumber(right)) {
                return ((Number)left).longValue() % ((Number)right).longValue();
            } else {
                throw new CalculateException(Bundle.getMessage("ArithmeticNotIntegerNumberError", right));
            }
        } else {
            throw new CalculateException(Bundle.getMessage("ArithmeticNotIntegerNumberError", left));
        }
    }
    
    
    @Override
    public Object calculate(SymbolTable symbolTable) throws JmriException {
        
        if (_tokenType == TokenType.ASSIGN) {
            Object value = _rightSide.calculate(symbolTable);
            _leftSide.assignValue(symbolTable, value);
            return value;
        }
        
        Object left = _leftSide.calculate(symbolTable);
        Object right = _rightSide.calculate(symbolTable);
        
        // Convert a boolean value to an integer value
        if (left instanceof Boolean) {
            left = ((Boolean)left) ? 1 : 0;
        }
        if (right instanceof Boolean) {
            right = ((Boolean)right) ? 1 : 0;
        }
        
        Object result;
        
        if (_tokenType == TokenType.ASSIGN_ADD) {
            // Add can handle String concatenation
            result = add(left, right);
        } else {
            // For the other arithmetic operators, except add, only numbers can
            // be handled. For other types, return 0.
            if (! TypeConversionUtil.isFloatingNumber(left)) {
                result = 0;
            } else if (! TypeConversionUtil.isFloatingNumber(right)) {
                result = 0;
            } else {
                switch (_tokenType) {
                    case ASSIGN_SUBTRACKT:
                        result = subtract(left, right);
                        break;
                    case ASSIGN_MULTIPLY:
                        result = multiply(left, right);
                        break;
                    case ASSIGN_DIVIDE:
                        result = divide(left, right);
                        break;
                    case ASSIGN_MODULO:
                        result = modulo(left, right);
                        break;
                    default:
                        throw new CalculateException("Unknown arithmetic operator: "+_tokenType.name());
                }
            }
        }
        
        _leftSide.assignValue(symbolTable, result);
        return result;
    }
    
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        String operStr;
        switch (_tokenType) {
            case ADD:
                operStr = "+";
                break;
                
            case SUBTRACKT:
                operStr = "-";
                break;
                
            case MULTIPLY:
                operStr = "*";
                break;
                
            case DIVIDE:
                operStr = "/";
                break;
                
            case MODULO:
                operStr = "%";
                break;
                
            default:
                throw new UnsupportedOperationException("Unknown arithmetic operator: "+_tokenType.name());
        }
        
        String leftSideString = _leftSide != null ? _leftSide.getDefinitionString() : "null";
        String rightSideString = _rightSide != null ? _rightSide.getDefinitionString() : "null";
        return "("+leftSideString+")" + operStr + "("+rightSideString+")";
    }
    
}
