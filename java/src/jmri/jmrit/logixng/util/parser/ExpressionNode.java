package jmri.jmrit.logixng.util.parser;

import jmri.JmriException;

/**
 * A parsed expression
 */
public interface ExpressionNode {

    public Object calculate() throws JmriException;
    
    /**
     * Get a String that defines this expression node.
     * @return the string
     */
    public String getDefinitionString();
    
}
