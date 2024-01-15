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
    void push(ValueAndType value);

    /**
     * Pops the topmost value off the top of the stack so the stack shrinks.
     * @return the value that is on the top of the stack
     */
    Object pop();

    /**
     * Get the value at an index
     * @param index the index from bottom of the table
     * @return value the new value
     */
    Object getValueAtIndex(int index);

    /**
     * Set the value at an index
     * @param index the index from bottom of the table
     * @param value the new value
     */
    void setValueAtIndex(int index, Object value);

    /**
     * Get the value and type at an index
     * @param index the index from bottom of the table
     * @return value and type the new value
     */
    ValueAndType getValueAndTypeAtIndex(int index);

    /**
     * Set the value at an index
     * @param index the index from bottom of the table
     * @param valueAndType the new value and type
     */
    void setValueAndTypeAtIndex(int index, ValueAndType valueAndType);

    /**
     * Get the number of items on the stack
     * @return the number of items
     */
    int getCount();

    /**
     * Reset the number of items on the stack.
     * This is used when parameters are put on the stack before a call to a
     * module and those parameters needs to be removed when the module returns.
     * The new count must be less than or equal to the current number of items.
     * @param newCount the new number of items
     */
    void setCount(int newCount);


    public static class ValueAndType {

        public Object _value;
        public SymbolTable.InitialValueType _type;

        public ValueAndType(SymbolTable.InitialValueType type, Object value) {
            _value = value;
            _type = type;
        }

    }

}
