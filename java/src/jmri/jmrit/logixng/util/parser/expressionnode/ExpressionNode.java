package jmri.jmrit.logixng.util.parser.expressionnode;

/**
 * A parsed expression
 */
public interface ExpressionNode {

    public Object calculate() throws Exception;
    
    /**
     * Get a String that defines this expression node.
     * @return the string
     */
    public String getDefinitionString();
    
}
