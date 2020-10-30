package jmri.jmrit.logixng;

/**
 * A table that is a stack
 * 
 * @author Daniel Bergqvist 2020
 */
public interface Stack extends NamedTable {
    
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
    
}
