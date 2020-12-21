package jmri.jmrit.logixng.util.parser;

import jmri.JmriException;

/**
 * A parsed expression
 */
public interface ExpressionNode {

    /**
     * Calculate the expression
     * @return the result
     * @throws JmriException if an error occurs
     */
    public Object calculate() throws JmriException;
    
    /**
     * Can this expression be assigned a value?
     * @return true if it's possible to assign a value to this expression,
     *         false otherwise
     */
    public default boolean canBeAssigned() {
        return false;
    }
    
    /**
     * Assign a value to this expression
     * @param value the value to assign
     * @throws jmri.JmriException if an error occurs
     */
    public default void assignValue(Object value) throws JmriException {
        throw new UnsupportedOperationException("This expression can't be assigned");
    }
    
    /**
     * Get a String that defines this expression node.
     * @return the string
     */
    public String getDefinitionString();
    
}
