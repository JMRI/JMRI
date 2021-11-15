package jmri.jmrit.logixng.util.parser;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;
import jmri.util.TypeConversionUtil;

/**
 * A parsed expression
 */
public class ExpressionNodeIncreaseDecreaseOperator implements ExpressionNode {

    private final ExpressionNode _exprNode;
    private final Operator _operator;
    
    
    public ExpressionNodeIncreaseDecreaseOperator(TokenType tokenType, ExpressionNode exprNode, boolean before) {
        _exprNode = exprNode;
        
        if (_exprNode == null) {
            throw new IllegalArgumentException("exprNode must not be null for operators ++ and --");
        }
        
        if (! _exprNode.canBeAssigned()) {
            throw new IllegalArgumentException("exprNode must assignable");
        }
        
        // Verify that the token is of the correct type
        switch (tokenType) {
            case INCREMENT:
                _operator = before ? Operator.PRE_INCREMENT : Operator.POST_INCREMENT;
                break;
            case DECREMENT:
                _operator = before ? Operator.PRE_DECREMENT : Operator.POST_DECREMENT;
                break;
            default:
                throw new IllegalArgumentException("Unknown arithmetic operator: "+tokenType.name());
        }
    }
    
    
    @Override
    public Object calculate(SymbolTable symbolTable) throws JmriException {
        
        Object value = _exprNode.calculate(symbolTable);
        
        if (TypeConversionUtil.isIntegerNumber(value)) {
            long v = ((Number)value).longValue();
            long result;
            
            switch (_operator) {
                case PRE_INCREMENT:  result = ++v; break;
                case PRE_DECREMENT:  result = --v; break;
                case POST_INCREMENT: result = v++; break;
                case POST_DECREMENT: result = v--; break;
                default:
                    throw new CalculateException("Unknown operator: "+_operator.name());
            }
            _exprNode.assignValue(symbolTable, v);
            return result;
        } else {
            throw new CalculateException(Bundle.getMessage("ArithmeticNotIntegerNumberError", value));
        }
    }
    
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        String leftOperStr = "";
        String rightOperStr = "";
        switch (_operator) {
            case PRE_INCREMENT:
                leftOperStr = "++";
                break;
                
            case PRE_DECREMENT:
                leftOperStr = "--";
                break;
                
            case POST_INCREMENT:
                rightOperStr = "++";
                break;
                
            case POST_DECREMENT:
                rightOperStr = "--";
                break;
                
            default:
                throw new UnsupportedOperationException("Unknown operator: "+_operator.name());
        }
        
        String exprNodeString = _exprNode != null ? _exprNode.getDefinitionString() : "null";
        return leftOperStr + "("+exprNodeString+")" + rightOperStr;
    }
    
    
    
    private enum Operator {
        PRE_INCREMENT,
        PRE_DECREMENT,
        POST_INCREMENT,
        POST_DECREMENT,
    }
    
}
