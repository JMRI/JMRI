package jmri.jmrit.logixng.util.parser.expressionnode;

import jmri.jmrit.logixng.util.parser.ParserException;

/**
 * A parsed expression
 */
public interface ExpressionNode {

    public Object calculate() throws ParserException;
    
    /**
     * Get a String that defines this expression node.
     * @return the string
     */
    public String getDefinitionString();
    
}
