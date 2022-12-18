package jmri.jmrit.logixng.util.parser;

import jmri.JmriException;
import jmri.jmrit.logixng.SymbolTable;

/**
 * A parsed expression
 */
public interface ExpressionNode {

    /**
     * Return the start position of this node.
     * @return the start position
     */
    public int getStartPos();

    /**
     * Return the start position of this node.
     * @return the start position
     */
    public int getEndPos();

    /**
     * Get a child of this node.
     * @param index the index of the child to get
     * @return the child
     * @throws IllegalArgumentException if the index is less than 0 or greater
     * or equal with the value returned by getChildCount()
     */
    public ExpressionNode getChild(int index)
            throws IllegalArgumentException, UnsupportedOperationException;

    /**
     * Get the number of children.
     * @return the number of children
     */
    public int getChildCount();

    /**
     * Calculate the expression
     * @param symbolTable the symbol table
     * @return the result
     * @throws JmriException if an error occurs
     */
    public Object calculate(SymbolTable symbolTable) throws JmriException;

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
     * @param symbolTable the symbol table
     * @param value the value to assign
     * @throws jmri.JmriException if an error occurs
     */
    public default void assignValue(SymbolTable symbolTable, Object value) throws JmriException {
        throw new UnsupportedOperationException("This expression can't be assigned");
    }

    /**
     * Get a String that defines this expression node.
     * @return the string
     */
    public String getDefinitionString();

}
