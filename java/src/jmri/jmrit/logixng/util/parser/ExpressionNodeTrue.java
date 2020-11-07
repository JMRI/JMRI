package jmri.jmrit.logixng.util.parser;

/**
 * A parsed expression that always return true
 */
public class ExpressionNodeTrue implements ExpressionNode {

    public ExpressionNodeTrue() {
    }
    
    @Override
    public Object calculate() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return "true";
    }
    
}
