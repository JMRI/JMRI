package jmri.jmrit.logixng.util.parser;

/**
 * A parsed expression that always return false
 */
public class ExpressionNodeFalse implements ExpressionNode {

    public ExpressionNodeFalse() {
    }
    
    @Override
    public Object calculate() {
        return false;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDefinitionString() {
        return "false";
    }
    
}
