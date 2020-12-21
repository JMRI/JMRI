package jmri.jmrit.logixng.util.parser;

import jmri.JmriException;
import jmri.util.TypeConversionUtil;

/**
 * A parsed expression
 */
public class ExpressionNodeTernaryOperator implements ExpressionNode {

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
    
    @Override
    public Object calculate() throws JmriException {
        
        Object leftValue = _leftSide.calculate();
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
            return _middleSide.calculate();
        } else {
            return _rightSide.calculate();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return _rightSide.getDefinitionString() + " ? ("
                + _middleSide.getDefinitionString() + ") : ("
                + _rightSide.getDefinitionString()+")";
    }
    
}
