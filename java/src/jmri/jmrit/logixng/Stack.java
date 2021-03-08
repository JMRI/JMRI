package jmri.jmrit.logixng;

/**
 * A table that is a stack
 * 
 * @author Daniel Bergqvist 2020
 */
public interface Stack {
    
    /**
     * Pushes a value on the top of the stack so the stack grow.
     * @param value the value to push to the stack
     */
    public void push(Object value);
    
    /**
     * Pops the topmost value off the top of the stack so the stack shrinks.
     * @return the value that is on the top of the stack
     */
    public Object pop();
    
    /**
     * Get the value at an index
     * @param index the index from bottom of the table
     * @return value the new value
     */
    public Object getValueAtIndex(int index);
    
    /**
     * Set the value at an index
     * @param index the index from bottom of the table
     * @param value the new value
     */
    public void setValueAtIndex(int index, Object value);
    
    /**
     * Get the number of items on the stack
     * @return the number of items
     */
    public int getCount();
    
    /**
     * Reset the number of items on the stack.
     * This is used when parameters are put on the stack before a call to a
     * module and those parameters needs to be removed when the module returns.
     * The new count must be less than or equal to the current number of items.
     * @param newCount the new number of items
     */
    public void setCount(int newCount);
    
}
